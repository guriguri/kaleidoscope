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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import kaleidoscope.uploader.util.DateUtils;
import kaleidoscope.uploader.util.FileUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpServerFileUpload;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

public class UploadHandler implements Handler<HttpServerFileUpload> {
	private static Logger log = LoggerFactory.getLogger(UploadHandler.class);
	private static DateFormat DIR_PATH = new SimpleDateFormat(
			"yyyy/MM/dd/HH/mm");

	private HttpServerRequest req;

	private String rootPath;
	private String cmd;
	private String outfileExt;
	private String defaultResize = "300x300";
	private int maxUploadFileSize = 10 * 1024 * 1024;
	private int maxThumbnailCount = 5;
	private int expireSec = 120;

	public UploadHandler(HttpServerRequest req, String rootPath, String cmd,
			String outfileExt, String defaultResize, int maxUploadFileSize,
			int maxThumbnailCount, int expireSec) {
		this.req = req;
		this.rootPath = rootPath;
		this.cmd = cmd;
		this.outfileExt = outfileExt;
		this.defaultResize = defaultResize;
		this.maxUploadFileSize = maxUploadFileSize;
		this.maxThumbnailCount = maxThumbnailCount;
		this.expireSec = -1 * expireSec;

		System.out.println("rootPath=" + rootPath + ", cmd=" + cmd
				+ ", outfileExt=" + outfileExt + ", defaultResize="
				+ defaultResize + ", maxUploadFileSize=" + maxUploadFileSize
				+ ", maxThumbnailCount=" + maxThumbnailCount + ", expireSec="
				+ expireSec);
	}

	@Override
	public void handle(final HttpServerFileUpload upload) {
		String path = rootPath + "/" + DIR_PATH.format(new Date());
		FileUtils.mkdir(path);

		String ext = FileUtils.getExt(upload.filename());

		String basename = UUID.randomUUID().toString();
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
					if (upload.size() > maxUploadFileSize) {
						throw new RuntimeException("invalid file size");
					}

					String resizes = req.formAttributes().get("resizes");
					if ((resizes == null)
							|| ((resizes = resizes.trim()).length() == 0)) {
						resizes = defaultResize;
					}

					System.out
							.println("1. file.size=" + file.getBytes().length);
					String[] resizeList = resizes.split(",");
					if (resizeList.length > maxThumbnailCount) {
						throw new RuntimeException("invalid thumbnail count");
					}

					Runtime runtime = Runtime.getRuntime();
					String command = cmd + " " + file + " " + outfilePrefix
							+ " " + outfileExt + " " + resizes;
					Process process = runtime.exec(command);
					process.waitFor();

					log.debug("cmd=[{}], exitValue=[{}]", command, process
							.exitValue());

					JsonArray arr = new JsonArray();
					for (int i = 0; i < resizeList.length; i++) {
						arr.add(outfilePrefix.replaceAll(rootPath, "") + "_"
								+ resizeList[i] + "." + outfileExt);
					}

					Calendar expireDate = DateUtils.getCalendar(expireSec);
					expireDate.set(Calendar.SECOND, 0);
					JsonObject json = new JsonObject().putArray("thumbnails",
							arr).putString("expireDate",
							expireDate.getTime().toString());

					req.response().end(json.toString());

//					FileUtils.rmdir(file);
				}
				catch (Exception e) {
					log.error("e={}", e.getMessage(), e);

					req.response().setStatusCode(500);

					if (e.getMessage() != null) {
						req.response().setStatusMessage(e.getMessage());
						req.response().end(e.getMessage());
					}
					else {
						req.response().end();
					}
				}
			}
		});

		log.info("uri={}, file={}", req.uri(), file);

		upload.streamToFileSystem(file);

		System.out.println("1111. upload.size=" + upload.size());
	}
}
