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
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpServerFileUpload;
import org.vertx.java.core.http.HttpServerRequest;

public class HttpRequestHandler implements Handler<HttpServerRequest> {
	protected static Logger log = LoggerFactory
			.getLogger(HttpRequestHandler.class);

	private static DateFormat DIR_PATH = new SimpleDateFormat("yyyy/MM/dd");
	private String rootPath;
	private String cmd;
	private String outfileExt;
	private String defaultResize = "300x300";

	private void mkdir(String path) {
		File file = new File(path);
		file.mkdirs();
	}

	public void setRootPath(String rootPath) {
		this.rootPath = rootPath;
	}

	public void setCmd(String cmd) {
		this.cmd = cmd;
	}

	public void setOutfileExt(String outfileExt) {
		this.outfileExt = outfileExt;
	}

	public void setDefaultResize(String defaultResize) {
		this.defaultResize = defaultResize;
	}

	@Override
	public void handle(final HttpServerRequest req) {
		try {
			req.expectMultiPart(true);
			req.uploadHandler(new Handler<HttpServerFileUpload>() {
				@Override
				public void handle(final HttpServerFileUpload upload) {
					Date now = new Date();

					String path = rootPath + "/"
							+ req.remoteAddress().getAddress().getHostAddress()
							+ "/" + DIR_PATH.format(now);
					mkdir(path);

					String ext = null;
					int idx = upload.filename().lastIndexOf(".");
					if (idx == -1) {
						ext = "";
					}
					else {
						ext = upload.filename().substring(idx);
					}

					final String outfilePrefix = now.getTime() + "_"
							+ UUID.randomUUID();
					String filename = outfilePrefix + ext;
					final String file = path + "/" + filename;
					String r = req.params().get("resizes");
					if (StringUtils.isEmpty(r) == true) {
						r = defaultResize;
					}
					final String resizes = r;

					upload.exceptionHandler(new Handler<Throwable>() {
						@Override
						public void handle(Throwable event) {
							req.response().end("Upload failed");
						}
					});

					upload.endHandler(new Handler<Void>() {
						@Override
						public void handle(Void event) {
							try {
								Runtime runtime = Runtime.getRuntime();
								String command = cmd + " " + file + " "
										+ outfilePrefix + " " + outfileExt
										+ " " + resizes;
								Process process = runtime.exec(command);
								process.waitFor();

								if (log.isDebugEnabled()) {
									log.debug("INF, cmd=[" + command
											+ "], exitValue=["
											+ process.exitValue() + "]");
								}
							}
							catch (Exception e) {
								log.error("ERR, e=" + e.getMessage());
							}

							req.response().end(
									"{\"filename\":\""
											+ file.replace(rootPath, "")
											+ "\"}");
						}
					});

					log.info("uri={}, file={}, params={}", new Object[] {
							req.uri(), file, req.params() });

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
