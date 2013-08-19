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
package kaleidoscope.uploader.server;

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
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpClient;
import org.vertx.java.core.http.HttpClientRequest;
import org.vertx.java.core.http.HttpClientResponse;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/META-INF/spring/spring-application-context-test.xml" })
public class UploaderServerTest {
	private static Logger log = LoggerFactory
			.getLogger(UploaderServerTest.class);

	@Value("${kaleidoscopic.uploader.domain}")
	private String domain;

	@Value("${kaleidoscopic.uploader.port}")
	private int port;

	@Test
	public void test() {
		Vertx vertx = VertxFactory.newVertx();
		HttpClient client = vertx.createHttpClient().setHost(domain).setPort(
				port).setMaxPoolSize(5);

		final String reqUri = "/uploader";
		HttpClientRequest request = client.post(reqUri,
				new Handler<HttpClientResponse>() {
					public void handle(HttpClientResponse resp) {
						resp.bodyHandler(new Handler<Buffer>() {
							public void handle(Buffer data) {
								log.info("{} received: {}", reqUri, data);
							}
						});
					}
				});

		request.end();

		log.info(">>>>>>>>>> request");

		try {
			Thread.sleep(300000);
		}
		catch (InterruptedException e) {
			e.printStackTrace();
		}

		client.close();
	}
}
