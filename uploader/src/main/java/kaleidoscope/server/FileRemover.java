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

import java.util.Calendar;

import kaleidoscope.util.DateUtils;
import kaleidoscope.util.FileUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

public class FileRemover implements InitializingBean, DisposableBean, Runnable {
	private static Logger log = LoggerFactory.getLogger(FileRemover.class);

	private static final long THREAD_MAIN_SLEEP_MSEC = 60000L;

	private String rootPath;
	private int expireSec = -120;
	private boolean isThreadRun = true;

	public void setRootPath(String rootPath) {
		this.rootPath = rootPath;
	}

	public void setExpireSec(int expireSec) {
		this.expireSec = expireSec;
	}

	public void setIsThreadRun(boolean isThreadRun) {
		this.isThreadRun = isThreadRun;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (isThreadRun == true) {
			new Thread(this).start();
		}
	}

	private class ExpiredDate {
		int year;
		int month;
		int day;
		int hour;
		int minute;

		public ExpiredDate(int gapSec) {
			Calendar cal = DateUtils.getCalendar(gapSec);

			year = cal.get(Calendar.YEAR);
			month = cal.get(Calendar.MONTH) + 1;
			day = cal.get(Calendar.DAY_OF_MONTH);
			hour = cal.get(Calendar.HOUR_OF_DAY);
			minute = cal.get(Calendar.MINUTE);
		}

		public int getYear() {
			return year;
		}

		public int getMonth() {
			return month;
		}

		public int getDay() {
			return day;
		}

		public int getHour() {
			return hour;
		}

		public int getMinute() {
			return minute;
		}

		public String toString(int field) {
			StringBuffer buff = new StringBuffer();

			switch (field) {
				case Calendar.MINUTE:
					buff.append(minute);

					if (minute < 10) {
						buff.insert(0, "0");
					}
				case Calendar.HOUR_OF_DAY:
					buff.insert(0, hour + "/");

					if (hour < 10) {
						buff.insert(0, "0");
					}
				case Calendar.DAY_OF_MONTH:
					buff.insert(0, day + "/");

					if (day < 10) {
						buff.insert(0, "0");
					}
				case Calendar.MONTH:
					buff.insert(0, month + "/");

					if (month < 10) {
						buff.insert(0, "0");
					}
				case Calendar.YEAR:
					buff.insert(0, year + "/");
			}

			return buff.toString();
		}

		public String toString() {
			return toString(Calendar.MINUTE);
		}
	}

	@Override
	public void run() {
		log.info("START Thread, tId={}", Thread.currentThread().getId());

		ExpiredDate removedDate = new ExpiredDate(expireSec);
		FileUtils.rmdir(rootPath + "/" + removedDate);

		while (isThreadRun) {
			try {
				ExpiredDate expiredDate = new ExpiredDate(expireSec);

				if (expiredDate.getYear() != removedDate.getYear()) {
					FileUtils.rmdir(rootPath + "/"
							+ removedDate.toString(Calendar.YEAR));
				}
				else if (expiredDate.getMonth() != removedDate.getMonth()) {
					FileUtils.rmdir(rootPath + "/"
							+ removedDate.toString(Calendar.MONTH));
				}
				else if (expiredDate.getDay() != removedDate.getDay()) {
					FileUtils.rmdir(rootPath + "/"
							+ removedDate.toString(Calendar.DAY_OF_MONTH));
				}
				else if (expiredDate.getHour() != removedDate.getHour()) {
					FileUtils.rmdir(rootPath + "/"
							+ removedDate.toString(Calendar.HOUR_OF_DAY));
				}

				if (expiredDate.getMinute() != removedDate.getMinute()) {
					FileUtils.rmdir(rootPath + "/" + expiredDate);
					removedDate = expiredDate;
				}

				Thread.sleep(THREAD_MAIN_SLEEP_MSEC);
			}
			catch (Exception e) {
				log.error("e={}", e.getMessage(), e);
			}
		}
	}

	@Override
	public void destroy() throws Exception {
		isThreadRun = false;
	}
}