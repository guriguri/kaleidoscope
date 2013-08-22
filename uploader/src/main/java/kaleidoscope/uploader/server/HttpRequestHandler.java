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

import kaleidoscope.uploader.util.FileUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpServerRequest;

public class HttpRequestHandler implements Handler<HttpServerRequest> {
	protected static Logger log = LoggerFactory
			.getLogger(HttpRequestHandler.class);

	private String rootPath;
	private String cmd;
	private String outfileExt;
	private String defaultResize;
	private int maxUploadFileSize;
	private int maxThumbnailCount;
	private int expireSec;

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

	public void setMaxUploadFileSize(int maxUploadFileSize) {
		this.maxUploadFileSize = maxUploadFileSize;
	}

	public void setMaxThumbnailCount(int maxThumbnailCount) {
		this.maxThumbnailCount = maxThumbnailCount;
	}

	public void setExpireSec(int expireSec) {
		this.expireSec = expireSec;
	}

	@Override
	public void handle(final HttpServerRequest req) {
		try {
			String method = req.method().toLowerCase();

			if ("post".equals(method) == true) {
				req.expectMultiPart(true);
				req.uploadHandler(new UploadHandler(req, rootPath, cmd,
						outfileExt, defaultResize, maxUploadFileSize,
						maxThumbnailCount, expireSec));
			}
			else if ("get".equals(method) == true) {
				String file = rootPath + "/"
						+ req.path().replaceAll("/uploader", "");

				log.debug("file={}", file);
				req.response().sendFile(file, "404.html");
			}
			else if ("delete".equals(method) == true) {
				String file = req.path().replaceAll("/uploader", "");

				FileUtils.rmdir(rootPath + "/" + file);
			}
		}
		catch (Exception e) {
			req.response().setStatusCode(500);

			if (e.getMessage() != null) {
				req.response().end(e.getMessage());
				req.response().setStatusMessage(e.getMessage());
			}
			else {
				req.response().end();
			}
		}
	}
}