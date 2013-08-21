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

public class UploadHandler implements Handler<HttpServerFileUpload> {
	private static Logger log = LoggerFactory.getLogger(UploadHandler.class);
	private static DateFormat DIR_PATH = new SimpleDateFormat(
			"yyyy/MM/dd/HH/mm");

	private HttpServerRequest req;

	private String rootPath;
	private String cmd;
	private String outfileExt;
	private String defaultResize = "300x300";

	public UploadHandler(HttpServerRequest req, String rootPath, String cmd,
			String outfileExt, String defaultResize) {
		this.req = req;
		this.rootPath = rootPath;
		this.cmd = cmd;
		this.outfileExt = outfileExt;
		this.defaultResize = defaultResize;
	}

	private void mkdir(String path) {
		File file = new File(path);
		file.mkdirs();
	}

	private String getExt(String filename) {
		String ext = null;

		int idx = filename.lastIndexOf(".");
		if (idx == -1) {
			ext = "";
		} else {
			ext = filename.substring(idx);
		}

		return ext;
	}

	@Override
	public void handle(final HttpServerFileUpload upload) {
		Date now = new Date();

		String path = rootPath + "/" + DIR_PATH.format(now);
		mkdir(path);

		String ext = getExt(upload.filename());

		String basename = now.getTime() + "_" + UUID.randomUUID();
		String filename = basename + ext;
		final String outfilePrefix = path + "/" + basename;
		final String file = path + "/" + filename;

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
					String r = req.formAttributes().get("resizes");
					if (StringUtils.isEmpty(r) == true) {
						r = defaultResize;
					}
					String resizes = r;

					Runtime runtime = Runtime.getRuntime();
					String command = cmd + " " + file + " " + outfilePrefix
							+ " " + outfileExt + " " + resizes;
					Process process = runtime.exec(command);
					process.waitFor();

					log.debug("cmd=[{}], exitValue=[{}]", command,
							process.exitValue());

					req.response().end(
							"{\"filename\":\"" + file.replace(rootPath, "")
									+ "\"}");
				} catch (Exception e) {
					log.error("e={}", e.getMessage(), e);

					req.response().setStatusCode(500);

					if (e.getMessage() != null) {
						req.response().setStatusMessage(e.getMessage());
						req.response().end(e.getMessage());
					} else {
						req.response().end();
					}
				}
			}
		});

		log.info("uri={}, file={}, params={}", new Object[] { req.uri(), file,
				req.params() });

		upload.streamToFileSystem(file);
	}
}
