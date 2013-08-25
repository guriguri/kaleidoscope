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
package kaleidoscope.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

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
				}
				else {
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
		}
		else {
			ext = filename.substring(idx);
		}

		return ext;
	}

	public static boolean isExist(String filename) {
		File file = new File(filename);
		return file.exists();
	}

	public static long getSize(String filename) {
		File file = new File(filename);
		return file.length();
	}

	public static byte[] read(InputStream is) throws Exception {
		byte[] data = null;

		byte[] buff = new byte[1024];
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		try {
			for (int readLength = 0; (readLength = is.read(buff)) > 0;) {
				baos.write(buff, 0, readLength);
			}

			data = baos.toByteArray();
		}
		finally {
			baos.close();
		}

		return data;
	}

	public static byte[] read(File file) throws Exception {
		byte[] data = null;

		if ((file.exists() == true) && (file.isDirectory() != true)) {
			InputStream in = null;

			try {
				in = new FileInputStream(file);
				data = read(in);
			}
			finally {
				if (in != null) {
					in.close();
				}
			}
		}

		return data;
	}
}