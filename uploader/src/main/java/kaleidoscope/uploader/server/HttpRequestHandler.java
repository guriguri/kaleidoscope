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

import java.io.File;
import java.net.URISyntaxException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpServerRequest;

public class HttpRequestHandler implements Handler<HttpServerRequest> {
	protected static Logger log = LoggerFactory
			.getLogger(HttpRequestHandler.class);

	private File HTML_404;

	private String rootPath;
	private String contextPath;
	private String cmd;
	private String outfileExt;
	private String defaultResize;
	private int maxUploadFileSize;
	private int maxThumbnailCount;
	private int expireSec;
	private String readUrl;

	public HttpRequestHandler() {
		super();

		try {
			HTML_404 = new File(getClass().getClassLoader()
					.getResource("html/404.html").toURI());
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void setRootPath(String rootPath) {
		this.rootPath = rootPath;
	}

	public void setContextPath(String contextPath) {
		this.contextPath = contextPath;
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

	public void setMaxUploadFileSize(int maxUploadFileSize) {
		this.maxUploadFileSize = maxUploadFileSize;
	}

	public void setMaxThumbnailCount(int maxThumbnailCount) {
		this.maxThumbnailCount = maxThumbnailCount;
	}

	public void setExpireSec(int expireSec) {
		this.expireSec = expireSec;
	}

	public void setReadUrl(String readUrl) {
		this.readUrl = readUrl;
	}

	@Override
	public void handle(final HttpServerRequest req) {
		try {
			String method = req.method().toLowerCase();
			String path = req.path();

			if ("post".equals(method) == true) {
				if (path.endsWith("create") == true) {
					req.expectMultiPart(true);
					req.uploadHandler(new UploadHandler(req, rootPath, cmd,
							outfileExt, defaultResize, maxUploadFileSize,
							maxThumbnailCount, expireSec, readUrl));
				} else if (path.endsWith("delete") == true) {
					req.expectMultiPart(true);
					req.endHandler(new Handler<Void>() {
						@Override
						public void handle(Void event) {
							System.out.println("Got request: " + req.uri());
							System.out.println("Headers are: ");
							for (Map.Entry<String, String> entry : req
									.headers()) {
								System.out.println(entry.getKey() + ":"
										+ entry.getValue());
							}

							String file = req.params().get("file");
							System.out.println("file=" + file);
							System.out.println("query=" + req.query());
							System.out.println("form.file="
									+ req.formAttributes().get("file"));
							// FileUtils.rmdir(rootPath + "/" + file);

							req.response().end("OK");
						}
					});
				} else {
					req.response().setStatusCode(404);
					req.response().sendFile(HTML_404.getPath());
				}
			} else if ("get".equals(method) == true) {
				String file = rootPath
						+ path.replaceAll(contextPath + "/read", "");

				req.response().sendFile(file, HTML_404.getPath());
			} else {
				req.response().setStatusCode(404);
				req.response().sendFile(HTML_404.getPath());
			}
		} catch (Exception e) {
			req.response().setStatusCode(500);

			if (e.getMessage() != null) {
				req.response().end(e.getMessage());
				req.response().setStatusMessage(e.getMessage());
			} else {
				req.response().end();
			}
		}
	}
}