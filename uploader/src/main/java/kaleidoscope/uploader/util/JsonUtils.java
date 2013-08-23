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

import org.vertx.java.core.json.JsonObject;

public class JsonUtils {
	public static JsonObject getJson(int code, String msg) {
		JsonObject json = new JsonObject().putNumber("result", code).putString(
				"msg", msg);

		return json;
	}

	public static JsonObject getJson(int code) {
		String msg = null;

		switch (code) {
			case 200:
				msg = "success";
				break;
			case 404:
				msg = "not found";
				break;
			default:
				msg = "unknown error";
		}

		return getJson(code, msg);
	}
}