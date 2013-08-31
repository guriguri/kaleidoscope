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

import java.util.Locale;
import java.util.StringTokenizer;

import org.apache.commons.lang.StringUtils;

public class HttpUtils {
	public static Locale getLocale(String acceptLanguage) {
		Locale locale = null;

		if (StringUtils.isEmpty(acceptLanguage) != true) {
			StringTokenizer langToken = new StringTokenizer(acceptLanguage,
					",; ");
			String language = langToken.nextToken().replace('-', '_');

			StringTokenizer localeToken = new StringTokenizer(language, "_");
			switch (localeToken.countTokens()) {
				case 1:
					locale = new Locale(localeToken.nextToken());
					break;
				case 2:
					locale = new Locale(localeToken.nextToken(), localeToken
							.nextToken());
					break;
				case 3:
					locale = new Locale(localeToken.nextToken(), localeToken
							.nextToken(), localeToken.nextToken());
					break;
			}
		}

		if (locale == null) {
			locale = Locale.getDefault();
		}

		return locale;
	}
}