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

import java.io.File;
import java.net.URISyntaxException;
import java.util.Map;

import kaleidoscope.util.FileUtils;
import kaleidoscope.util.JsonUtils;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonObject;

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
			HTML_INDEX = new File(getClass().getClassLoader()
					.getResource("html/index.html").toURI());
			REGEX_THUMBNAIL_URI = "/[0-9a-fx/_-]+[.][a-z]+";
		} catch (URISyntaxException e) {
			log.error("e={}", e.getMessage(), e);
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

	public static void requestEnd(HttpServerRequest req, int code, Object obj,
			boolean isOnlyLog) {
		if (req == null) {
			log.error("req is null");
			return;
		}

		String json = null;
		String method = req.method().toLowerCase();
		String query = "";

		if (obj == null) {
			json = JsonUtils.getJson(code).toString();
		} else if (obj instanceof String) {
			json = JsonUtils.getJson(code, (String) obj).toString();
		} else if (obj instanceof JsonObject) {
			json = obj.toString();
		}

		if ("get".equals(method) == true) {
			if (StringUtils.isEmpty(req.query()) != true) {
				query = req.query();
			}
		} else if (req.formAttributes() != null) {
			for (Map.Entry<String, String> entry : req.formAttributes()) {
				query += entry.getKey() + "=" + entry.getValue() + "&";
			}
		}

		log.info("uri={}?{}, json={}", new Object[] { req.uri(), query, json });

		if (isOnlyLog != true) {
			req.response().setStatusCode(code);
			req.response().end(json);
		}
	}

	public static void requestEnd(HttpServerRequest req, int code, Object obj) {
		requestEnd(req, code, obj, false);
	}

	public static void requestEnd(HttpServerRequest req, int code) {
		requestEnd(req, code, null);
	}

	@Override
	public void handle(final HttpServerRequest req) {
		String method = req.method().toLowerCase();
		String path = req.path();

		try {
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
							String file = req.formAttributes().get("file");
							if (StringUtils.isEmpty(file) == true) {
								requestEnd(req, 500,
										"invalid file, file is empty");
							} else if ((file = file.replaceAll(readUrl, ""))
									.matches(REGEX_THUMBNAIL_URI) != true) {
								requestEnd(req, 500, "invalid file, file="
										+ file);
							} else {
								FileUtils.rmdir(rootPath + "/" + file);
								requestEnd(req, 200);
							}
						}
					});
				} else {
					requestEnd(req, 404);
				}
			} else if ("get".equals(method) == true) {
				String file = null;

				if (path.equals(contextPath) == true) {
					file = HTML_INDEX.getPath();
				} else {
					file = rootPath
							+ path.replaceAll(contextPath + "/read", "");
				}

				if (FileUtils.isExist(file) == true) {
					req.response().sendFile(file);
					requestEnd(req, 200, "success", true);
				} else {
					requestEnd(req, 404);
				}
			} else {
				requestEnd(req, 404);
			}
		} catch (Exception e) {
			requestEnd(req, 500, e.getMessage());
		}
	}
}