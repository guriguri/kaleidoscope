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
import java.util.Map;

import kaleidoscope.util.FileUtils;
import kaleidoscope.util.JsonUtils;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonObject;

public class RestRequestHandler implements Handler<HttpServerRequest> {
	protected static Logger log = LoggerFactory
			.getLogger(RestRequestHandler.class);

	private File HTML_INDEX;
	private String REGEX_THUMBNAIL_URI;

	private String rootPath;
	private String contextPath;
	private String cmd;
	private String outfileExt;
	private String defaultResize = "300x300";
	private int maxUploadFileSize = 10 * 1024 * 1024;
	private int maxThumbnailCount = 5;
	private int expireSec = 120;
	private String readUrl;

	public RestRequestHandler() {
		super();

		try {
			HTML_INDEX = new File(getClass().getClassLoader().getResource(
					"html/index.html").toURI());
			REGEX_THUMBNAIL_URI = "/[0-9a-fx/_-]+[.][a-z]+";
		}
		catch (Exception e) {
			e.printStackTrace();
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

	public static void requestEnd(HttpServerRequest req,
			HttpResponseStatus status, Object obj, boolean isOnlyLog) {
		if (req == null) {
			log.error("req is null");
			return;
		}

		String json = null;
		String method = req.method().toLowerCase();
		String query = "";

		int statusCode = status.code();
		String statusMsg = status.reasonPhrase();

		if (obj == null) {
			json = JsonUtils.getJson(statusCode, statusMsg).toString();
		}
		else if (obj instanceof String) {
			json = JsonUtils.getJson(statusCode, (String) obj).toString();
		}
		else if (obj instanceof JsonObject) {
			json = obj.toString();
		}

		if ("get".equals(method) == true) {
			if (StringUtils.isEmpty(req.query()) != true) {
				query = req.query();
			}
		}
		else if (req.formAttributes() != null) {
			for (Map.Entry<String, String> entry : req.formAttributes()) {
				query += entry.getKey() + "=" + entry.getValue() + "&";
			}
		}

		log.info("uri={}?{}, json={}", new Object[] { req.uri(), query, json });

		if (isOnlyLog != true) {
			req.response().setStatusCode(statusCode);
			req.response().setStatusMessage(statusMsg);
			req.response().putHeader("Access-Control-Allow-Origin", "*");
			
			req.response().end(json);
		}
	}

	public static void requestEnd(HttpServerRequest req,
			HttpResponseStatus status, Object obj) {
		requestEnd(req, status, obj, false);
	}

	public static void requestEnd(HttpServerRequest req,
			HttpResponseStatus status, boolean isOnlyLog) {
		requestEnd(req, status, null, isOnlyLog);
	}

	public static void requestEnd(HttpServerRequest req,
			HttpResponseStatus status) {
		requestEnd(req, status, null);
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
				}
				else if (path.endsWith("delete") == true) {
					req.expectMultiPart(true);
					req.endHandler(new Handler<Void>() {
						@Override
						public void handle(Void event) {
							String url = req.formAttributes().get("url");
							String file = null;
							if (StringUtils.isEmpty(url) == true) {
								requestEnd(req, HttpResponseStatus.BAD_REQUEST,
										"url is empty");
							}
							else if ((file = url.replaceAll(readUrl, ""))
									.matches(REGEX_THUMBNAIL_URI) != true) {
								requestEnd(req, HttpResponseStatus.BAD_REQUEST,
										"url has invalid chars");
							}
							else {
								FileUtils.rmdir(rootPath + "/" + file);
								requestEnd(req, HttpResponseStatus.OK);
							}
						}
					});
				}
				else {
					requestEnd(req, HttpResponseStatus.NOT_FOUND);
				}
			}
			else if ("get".equals(method) == true) {
				String file = null;

				if ((path.equals("/") == true)
						|| (path.equals(contextPath) == true)) {
					file = HTML_INDEX.getPath();
				}
				else {
					file = rootPath
							+ path.replaceAll(contextPath + "/read", "");
				}

				if (FileUtils.isExist(file) == true) {
					req.response().sendFile(file);
					requestEnd(req, HttpResponseStatus.OK, true);
				}
				else {
					requestEnd(req, HttpResponseStatus.NOT_FOUND);
				}
			}
			else {
				requestEnd(req, HttpResponseStatus.NOT_FOUND);
			}
		}
		catch (Exception e) {
			requestEnd(req, HttpResponseStatus.INTERNAL_SERVER_ERROR, e
					.getMessage());
		}
	}
}