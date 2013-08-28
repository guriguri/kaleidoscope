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

import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import kaleidoscope.util.DateUtils;
import kaleidoscope.util.FileUtils;
import kaleidoscope.util.JsonUtils;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpServerFileUpload;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

public class UploadHandler implements Handler<HttpServerFileUpload> {
	private static Logger log = LoggerFactory.getLogger(UploadHandler.class);

	private HttpServerRequest req;

	private String rootPath;
	private String cmd;
	private String outfileExt;
	private String defaultResize = "300x300";
	private int maxUploadFileSize = 10 * 1024 * 1024;
	private int maxThumbnailCount = 5;
	private int expireSec = 120;
	private String readUrl;

	public UploadHandler(HttpServerRequest req, String rootPath, String cmd,
			String outfileExt, String defaultResize, int maxUploadFileSize,
			int maxThumbnailCount, int expireSec, String readUrl)
			throws URISyntaxException {
		this.req = req;
		this.rootPath = rootPath;
		this.cmd = Paths.get(
				getClass().getClassLoader().getResource(cmd).toURI())
				.toString();
		this.outfileExt = outfileExt;
		this.defaultResize = defaultResize;
		this.maxUploadFileSize = maxUploadFileSize;
		this.maxThumbnailCount = maxThumbnailCount;
		this.expireSec = -1 * expireSec;
		this.readUrl = readUrl;

		log.debug("rootPath={}, cmd={}, outfileExt={}, defaultResize={}"
				+ ", maxUploadFileSize={}, maxThumbnailCount={}"
				+ ", expireSec={}, readUrl={}", new Object[] { rootPath,
				this.cmd, outfileExt, defaultResize, maxUploadFileSize,
				maxThumbnailCount, expireSec, readUrl });
	}

	@Override
	public void handle(final HttpServerFileUpload upload) {
		if (StringUtils.isEmpty(upload.filename()) == true) {
			RestRequestHandler.requestEnd(req, HttpResponseStatus.BAD_REQUEST,
					"need to file");
			return;
		}

		String path = rootPath + "/"
				+ DateUtils.DATE_FORMAT_YYYYMMDDHHMI.format(new Date());
		FileUtils.mkdir(path);

		String ext = FileUtils.getExt(upload.filename());

		String basename = UUID.randomUUID().toString();
		final String filename = basename + ext;
		final String outfilePrefix = path + "/" + basename;
		final String file = path + "/" + filename;

		upload.exceptionHandler(new Handler<Throwable>() {
			@Override
			public void handle(Throwable event) {
				RestRequestHandler.requestEnd(req,
						HttpResponseStatus.INTERNAL_SERVER_ERROR, event
								.getMessage());
			}
		});

		upload.endHandler(new Handler<Void>() {
			@Override
			public void handle(Void event) {
				if (FileUtils.getSize(file) > maxUploadFileSize) {
					RestRequestHandler.requestEnd(req,
							HttpResponseStatus.BAD_REQUEST,
							"The file's size is limited to "
									+ maxUploadFileSize);
				}
			}
		});

		upload.streamToFileSystem(file);

		req.endHandler(new Handler<Void>() {
			@Override
			public void handle(Void event) {
				try {
					String resizes = req.formAttributes().get("resizes");
					if ((resizes == null)
							|| ((resizes = resizes.trim()).length() == 0)) {
						resizes = defaultResize;
					}

					String[] resizeList = resizes.split(",");
					if (resizeList.length > maxThumbnailCount) {
						RestRequestHandler.requestEnd(req,
								HttpResponseStatus.BAD_REQUEST,
								"The thumbnails is limited to "
										+ maxThumbnailCount);
						return;
					}

					Runtime runtime = Runtime.getRuntime();
					String command = cmd + " " + Paths.get(file) + " "
							+ Paths.get(outfilePrefix) + " " + outfileExt + " "
							+ resizes;
					Process process = runtime.exec(command);
					process.waitFor();

					log.debug("cmd=[{}], exitValue=[{}]", command, process
							.exitValue());

					JsonArray arr = new JsonArray();
					for (int i = 0; i < resizeList.length; i++) {
						arr.add(readUrl
								+ outfilePrefix.replaceAll(rootPath, "") + "_"
								+ resizeList[i] + "." + outfileExt);
					}

					Calendar expireDate = DateUtils.getCalendar(expireSec);
					expireDate.set(Calendar.SECOND, 0);
					JsonObject json = JsonUtils.getJson(HttpResponseStatus.OK)
							.putArray("thumbnails", arr).putString(
									"expireDate",
									DateUtils.DATE_FORMAT_ISO8601FMT
											.format(expireDate.getTime()));

					RestRequestHandler.requestEnd(req, HttpResponseStatus.OK,
							json);

					FileUtils.rmdir(file);
				}
				catch (Exception e) {
					log.error("e={}", e.getMessage(), e);
					RestRequestHandler.requestEnd(req,
							HttpResponseStatus.INTERNAL_SERVER_ERROR, e
									.getMessage());
				}
			}
		});
	}
}