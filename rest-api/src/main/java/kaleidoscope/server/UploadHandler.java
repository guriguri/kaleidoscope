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
import java.util.Set;
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

	private RestRequestHandler handler;
	private HttpServerRequest req;

	private String rootPath;

	private String path;
	private String basename;
	private String ext;
	private String file;
	private Set<String> supportImageFormat;

	public UploadHandler(final RestRequestHandler handler,
			final HttpServerRequest req, final Set<String> supportImageFormat)
			throws URISyntaxException {
		this.handler = handler;
		this.req = req;
		this.rootPath = handler.getRootPath();
		final String outfileExt = handler.getOutfileExt();
		final String defaultResize = handler.getDefaultResize();
		final int maxUploadFileSize = handler.getMaxUploadFileSize();
		final int maxThumbnailCount = handler.getMaxThumbnailCount();
		final int expireSec = handler.getExpireSec();
		final String readUrl = handler.getReadUrl();
		this.supportImageFormat = supportImageFormat;
		final String realCmd = Paths.get(
				getClass().getClassLoader().getResource(handler.getCmd())
						.toURI()).toString();

		req.endHandler(new Handler<Void>() {
			@Override
			public void handle(Void event) {
				try {
					if (file == null) {
						handler.requestEnd(req, HttpResponseStatus.BAD_REQUEST,
								new Object[] { "required.param.file" });
						return;
					}
					else if (FileUtils.getSize(file) > maxUploadFileSize) {
						handler.requestEnd(req, HttpResponseStatus.BAD_REQUEST,
								new Object[] { "max.upload.file.size",
										maxUploadFileSize });
						return;
					}
					else if (supportImageFormat.contains(ext) != true) {
						handler.requestEnd(req, HttpResponseStatus.BAD_REQUEST,
								new Object[] { "invalid.image.format",
										supportImageFormat });
						return;
					}

					String resizes = req.formAttributes().get("resizes");
					if ((resizes == null)
							|| ((resizes = resizes.trim()).length() == 0)) {
						resizes = defaultResize;
					}

					String[] resizeList = resizes.split(",");
					if (resizeList.length > maxThumbnailCount) {
						handler.requestEnd(req, HttpResponseStatus.BAD_REQUEST,
								new Object[] { "max.thumbnail.count",
										maxThumbnailCount });
						return;
					}

					String outfilePrefix = path + "/" + basename;

					Runtime runtime = Runtime.getRuntime();
					String command = realCmd + " " + Paths.get(file) + " "
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

					handler.requestEnd(req, HttpResponseStatus.OK, json);

					FileUtils.rmdir(file);
				}
				catch (Exception e) {
					log.error("e={}", e.getMessage(), e);
					handler.requestEnd(req,
							HttpResponseStatus.INTERNAL_SERVER_ERROR, e
									.getMessage());
				}
			}
		});

		log.debug("rootPath={}, cmd={}, outfileExt={}, defaultResize={}"
				+ ", maxUploadFileSize={}, maxThumbnailCount={}"
				+ ", expireSec={}, readUrl={}, supportImageFormat={}",
				new Object[] { rootPath, realCmd, outfileExt, defaultResize,
						maxUploadFileSize, maxThumbnailCount, expireSec,
						readUrl, supportImageFormat });
	}

	@Override
	public void handle(final HttpServerFileUpload upload) {
		if ((upload == null)
				|| (StringUtils.isEmpty(upload.filename()) == true)) {
			return;
		}

		path = rootPath + "/"
				+ DateUtils.DATE_FORMAT_YYYYMMDDHHMI.format(new Date());
		basename = UUID.randomUUID().toString();
		ext = FileUtils.getExt(upload.filename());
		file = path + "/" + basename + "." + ext;

		if (supportImageFormat.contains(ext) != true) {
			return;
		}

		upload.exceptionHandler(new Handler<Throwable>() {
			@Override
			public void handle(Throwable event) {
				handler.requestEnd(req,
						HttpResponseStatus.INTERNAL_SERVER_ERROR, event
								.getMessage());
			}
		});

		FileUtils.mkdir(path);
		upload.streamToFileSystem(file);
	}
}