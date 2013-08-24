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

import kaleidoscope.uploader.util.FileUtils;
import kaleidoscope.uploader.util.JsonUtils;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpServerRequest;

public class HttpRequestHandler implements Handler<HttpServerRequest> {
	protected static Logger log = LoggerFactory
			.getLogger(HttpRequestHandler.class);

	private File HTML_INDEX;
	private String REGEX_THUMBNAIL_URI;

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
			HTML_INDEX = new File(getClass().getClassLoader().getResource(
					"html/index.html").toURI());
			REGEX_THUMBNAIL_URI = "/[0-9a-f/-]+[.][a-z]+";
		}
		catch (URISyntaxException e) {
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
				}
				else if (path.endsWith("delete") == true) {
					req.expectMultiPart(true);
					req.endHandler(new Handler<Void>() {
						@Override
						public void handle(Void event) {
							String file = req.formAttributes().get("file");
							if (StringUtils.isEmpty(file) == true) {
								req.response().end(
										JsonUtils.getJson(500, "invalid file")
												.toString());
							}
							else if ((file = file.replaceAll(readUrl, ""))
									.matches(REGEX_THUMBNAIL_URI) != true) {
								req.response().end(
										JsonUtils.getJson(500, "invalid file")
												.toString());
							}
							else {
								FileUtils.rmdir(rootPath + "/" + file);
								req.response().end(
										JsonUtils.getJson(200).toString());
							}
						}
					});
				}
				else {
					req.response().setStatusCode(404);
					req.response().end(JsonUtils.getJson(404).toString());
				}
			}
			else if ("get".equals(method) == true) {
				String file = null;

				if (path.equals(contextPath) == true) {
					file = HTML_INDEX.getPath();
				}
				else {
					file = rootPath
							+ path.replaceAll(contextPath + "/read", "");
				}

				if (FileUtils.isExist(file) == true) {
					req.response().sendFile(file);
				}
				else {
					req.response().setStatusCode(404);
					req.response().end(JsonUtils.getJson(404).toString());
				}
			}
			else {
				req.response().setStatusCode(404);
				req.response().end(JsonUtils.getJson(404).toString());
			}
		}
		catch (Exception e) {
			req.response().setStatusCode(500);

			if (e.getMessage() != null) {
				req.response().setStatusMessage(e.getMessage());
				req.response().end(
						JsonUtils.getJson(500, e.getMessage()).toString());
			}
			else {
				req.response().end();
			}
		}
	}
}