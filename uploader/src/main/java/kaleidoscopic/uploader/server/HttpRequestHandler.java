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

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpServerFileUpload;
import org.vertx.java.core.http.HttpServerRequest;

public class HttpRequestHandler implements Handler<HttpServerRequest> {
	protected static Logger log = LoggerFactory
			.getLogger(HttpRequestHandler.class);

	private static DateFormat DIR_PATH = new SimpleDateFormat(
			"yyyy/MM/dd/HH/mm/ss");
	private String rootPath;

	private void mkdir(String path) {
		File file = new File(path);
		file.mkdirs();
	}

	public void setRootPath(String rootPath) {
		this.rootPath = rootPath;
	}

	@Override
	public void handle(final HttpServerRequest req) {
		try {
			req.expectMultiPart(true);
			req.uploadHandler(new Handler<HttpServerFileUpload>() {
				@Override
				public void handle(final HttpServerFileUpload upload) {
					upload.exceptionHandler(new Handler<Throwable>() {
						@Override
						public void handle(Throwable event) {
							req.response().end("Upload failed");
						}
					});

					upload.endHandler(new Handler<Void>() {
						@Override
						public void handle(Void event) {
							req.response().end(
									"OK, " + String.valueOf(new Date()));
						}
					});

					String path = rootPath + "/"
							+ DIR_PATH.format((new Date()));
					mkdir(path);
					String file = path + "/" + upload.filename();

					log.info("rootPath={}, uri={}, file={}, params={}",
							new Object[] { rootPath, req.uri(), file,
									req.params() });

					upload.streamToFileSystem(file);
				}
			});
		}
		catch (Exception e) {
			req.response().setStatusCode(500);
			req.response().setStatusMessage(e.getMessage());
			if (e.getMessage() != null)
				req.response().end(e.getMessage());
			else
				req.response().end();
		}
	}
}
