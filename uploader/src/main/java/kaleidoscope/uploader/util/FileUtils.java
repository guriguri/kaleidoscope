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
package kaleidoscope.uploader.util;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileUtils {
	private static Logger log = LoggerFactory.getLogger(FileUtils.class);

	public static void rmdir(File file) {
		if ((file == null) || (file.exists() == false)) {
			return;
		}

		File[] files = file.listFiles();

		if (files != null) {
			for (File f : files) {
				if (f.isDirectory()) {
					rmdir(f);
				} else {
					log.debug("delete file={}", f.getPath());
					f.delete();
				}
			}
		}

		log.debug("delete file={}", file.getPath());
		file.delete();
	}

	public static void rmdir(String file) {
		rmdir(new File(file));
	}

	public static void mkdir(String path) {
		File file = new File(path);
		file.mkdirs();
	}

	public static String getExt(String filename) {
		String ext = null;

		int idx = filename.lastIndexOf(".");
		if (idx == -1) {
			ext = "";
		} else {
			ext = filename.substring(idx);
		}

		return ext;
	}
}