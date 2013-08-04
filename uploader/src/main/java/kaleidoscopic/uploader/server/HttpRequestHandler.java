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

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpServerRequest;

public class HttpRequestHandler implements Handler<HttpServerRequest> {
	protected static Logger log = LoggerFactory
			.getLogger(HttpRequestHandler.class);

	private String rootPath;

	public void setRootPath(String rootPath) {
		this.rootPath = rootPath;
	}

	@Override
	public void handle(HttpServerRequest req) {
		try {
			req.response.statusCode = 200;
			req.response.statusMessage = "OK";

			log.info("rootPath={}, uri={}, params={}", new Object[] { rootPath,
					req.path, req.params() });

			req.response.end(String.valueOf(new Date()));
		}
		catch (Exception e) {
			req.response.statusCode = 500;
			req.response.statusMessage = e.getMessage();
			if (e.getMessage() != null)
				req.response.end(e.getMessage());
			else
				req.response.end();
		}
	}
}
