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
package kaleidoscopic.uploader.server;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.VertxFactory;
import org.vertx.java.core.http.HttpServer;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.RouteMatcher;

public class UploaderServer implements Runnable {
	private static Logger log = LoggerFactory.getLogger(UploaderServer.class);

	private static final long THREAD_MAIN_SLEEP_MSEC = 1000L;

	private int port;
	private String domain;
	private Handler<HttpServerRequest> handler;
	private boolean isRun = true;
	private HttpServer server;

	public void setPort(int port) {
		this.port = port;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public void setHandler(Handler<HttpServerRequest> handler) {
		this.handler = handler;
	}

	public void start() {
		if ((port != 0) && StringUtils.isNotEmpty(domain) && (handler != null)) {
			Vertx vertx = VertxFactory.newVertx();
			server = vertx.createHttpServer();

			RouteMatcher rm = new RouteMatcher();
			rm.post("/uploader", handler);
			rm.noMatch(new Handler<HttpServerRequest>() {
				public void handle(HttpServerRequest req) {
					req.response().setStatusCode(404);
					req.response().setStatusMessage("Not Found");
					req.response().end("Not Found");
				}
			});

			server.requestHandler(rm).listen(port, domain);

			log.info("START, listening, http://{}:{}", domain, port);
		}
		else {
			log.error("port={}, domain={}, handler.isNull={}", new Object[] {
					port, domain, (handler == null) });
		}
	}

	public void destroy() {
		isRun = false;
	}

	@Override
	public void run() {
		start();

		while (isRun) {
			try {
				Thread.sleep(THREAD_MAIN_SLEEP_MSEC);
			}
			catch (Exception e) {
				log.error("exception", e);
			}
		}

		server.close();
	}
}