/*
 * Copyright guriguri(guriguri.kr@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package kaleidoscope.server;

import io.netty.handler.codec.http.HttpResponseStatus;

import java.io.File;
import java.net.URISyntaxException;
import java.util.UUID;

import junit.framework.Assert;
import kaleidoscope.util.FileUtils;

import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.VertxFactory;
import org.vertx.java.core.VoidHandler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpClient;
import org.vertx.java.core.http.HttpClientRequest;
import org.vertx.java.core.http.HttpClientResponse;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/META-INF/spring/spring-application-context-test.xml" })
public class RestServerTest {
	private static Logger log = LoggerFactory.getLogger(RestServerTest.class);

	private static String FILENAME = "guriguri.png";
	private static int THREAD_SLEEP_MSEC = 5000;

	@Value("${kaleidoscope.domain}")
	private String domain;

	@Value("${kaleidoscope.port}")
	private int port;

	@Value("${kaleidoscope.context.path}")
	private String contextPath;

	private byte[] content;

	@Before
	public void setup() throws URISyntaxException, Exception {
		if (StringUtils.isEmpty(domain) == true) {
			domain = "localhost";
		}

		content = FileUtils.read(new File(getClass().getClassLoader()
				.getResource(FILENAME).toURI()));
	}

	@Test
	public void test() {
		create();
	}

	private void create() {
		Vertx vertx = VertxFactory.newVertx();
		HttpClient client = vertx.createHttpClient().setHost(domain).setPort(
				port);
		HttpClientRequest req = client.post(contextPath + "/create",
				new Handler<HttpClientResponse>() {
					@Override
					public void handle(HttpClientResponse resp) {
						Assert.assertEquals(HttpResponseStatus.OK.code(), resp
								.statusCode());

						resp.bodyHandler(new Handler<Buffer>() {
							public void handle(Buffer body) {
								JsonObject json = new JsonObject(body
										.toString());
								JsonArray arr = json.getArray("thumbnails");

								read(arr);
							}
						});
					}
				});

		String boundary = UUID.randomUUID().toString();
		String resizes = "10x10";

		Buffer buffer = new Buffer();

		buffer.appendString("--" + boundary + "\r\n");
		buffer.appendString("Content-Disposition: form-data; name=\"file\"; filename=\""
				+ FILENAME + "\"\r\n");
		buffer.appendString("Content-Type: image/gif\r\n\r\n");
		buffer.appendBytes(content).appendString("\r\n");

		buffer.appendString("--" + boundary + "\r\n");
		buffer.appendString("Content-Disposition: form-data; name=\"resizes\"\r\n\r\n");
		buffer.appendString(resizes + "\r\n");

		buffer.appendString("--" + boundary + "--\r\n");

		req.headers().set("Content-Length", String.valueOf(buffer.length()));
		req.headers().set("Content-Type",
				"multipart/form-data; boundary=" + boundary);
		req.write(buffer).end();

		try {
			Thread.sleep(THREAD_SLEEP_MSEC);
		}
		catch (InterruptedException e) {
			e.printStackTrace();
		}

		client.close();
	}

	private void read(JsonArray arr) {
		Vertx vertx = VertxFactory.newVertx();
		HttpClient client = vertx.createHttpClient().setHost(domain).setPort(
				port);

		for (final Object reqUrl : arr) {
			HttpClientRequest request = client.get((String) reqUrl,
					new Handler<HttpClientResponse>() {
						public void handle(final HttpClientResponse resp) {
							Assert.assertEquals(HttpResponseStatus.OK.code(),
									resp.statusCode());

							final Buffer buff = new Buffer(0);

							resp.dataHandler(new Handler<Buffer>() {
								public void handle(Buffer data) {
									buff.appendBuffer(data);
								}
							});

							resp.endHandler(new VoidHandler() {
								public void handle() {
									Assert.assertEquals(content.length, buff
											.length());
									for (int i = 0, size = content.length; i < size; i++) {
										Assert.assertEquals(content[i], buff
												.getByte(i));
									}

									delete((String) reqUrl);
								}
							}

							);
						}
					});

			request.end();
		}

		try {
			Thread.sleep(THREAD_SLEEP_MSEC);
		}
		catch (InterruptedException e) {
			e.printStackTrace();
		}

		client.close();
	}

	private void delete(final String url) {
		Vertx vertx = VertxFactory.newVertx();
		HttpClient client = vertx.createHttpClient().setHost(domain).setPort(
				port);

		final String reqUrl = contextPath + "/delete";
		HttpClientRequest request = client.post(reqUrl,
				new Handler<HttpClientResponse>() {
					public void handle(HttpClientResponse resp) {
						Assert.assertEquals(HttpResponseStatus.OK.code(), resp
								.statusCode());

						resp.bodyHandler(new Handler<Buffer>() {
							public void handle(Buffer data) {
								log.info("delete, {} received: {}", url, data);
							}
						});
					}
				});

		Buffer buffer = new Buffer();
		buffer.appendString("url=" + url);
		request.headers()
				.set("content-length", String.valueOf(buffer.length()));
		request.headers().set("content-type",
				"application/x-www-form-urlencoded");
		request.write(buffer).end();

		try {
			Thread.sleep(THREAD_SLEEP_MSEC);
		}
		catch (InterruptedException e) {
			e.printStackTrace();
		}

		client.close();
	}
}
