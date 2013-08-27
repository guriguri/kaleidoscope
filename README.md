# Kaleidoscope
## 소개
* Kaleidoscope는 [http://vertx.io/](Vert.x)와 [http://www.graphicsmagick.org/](GraphicsMagick)을 이용한 Web 기반 쎔네일 생성기입니다.

## 요구사항
* JDK 7+
* Maven
* GraphicsMagick


## 실행방법

	cd $KALEIDOSCOPE_GIT_HOME
	mvn -e clean install -DskipTests=true --no-snapshot-updates
	
	cd $KALEIDOSCOPE_GIT_HOME/engine
	mvn -e clean install assembly:single -DskipTests=true --no-snapshot-updates
	
	cp target/kaleidoscope-engine-0.0.1-SNAPSHOT-stand-alone.zip $KALEIDOSCOPE_APP_HOME
	cd $KALEIDOSCOPE_APP_HOME
	
	unzip kaleidoscope-engine-0.0.1-SNAPSHOT-stand-alone.zip
	bin/runner start

## 환경설정

	kaleidoscope.domain=0.0.0.0
	kaleidoscope.port=6487
	kaleidoscope.read.url=http://100dream.net:6487/kaleidoscope/read
	kaleidoscope.root.path=/tmp/kaleidoscope
	#kaleidoscope.root.path=/home/max/server/nginx/html
	kaleidoscope.cmd=script/make_thumbnails.sh
	#kaleidoscope.cmd=script/echo_shell.sh
	#kaleidoscope.cmd=script/echo_shell.bat
	kaleidoscope.outfile.ext=jpg
	kaleidoscope.default.resize=300x300
	kaleidoscope.max.upload.file.size=10485760
	kaleidoscope.max.thumbnail.count=5
	kaleidoscope.expire.sec=-120
	kaleidoscope.context.path=/kaleidoscope
	kaleidoscope.auto.remove=true


| 구분                                                                       | 설명           |
|----------------------------------|--------|
|kaleidoscope.domain               |        |
|kaleidoscope.port                 | 6487   |
|kaleidoscope.read.url             | http://100dream.net:6487/kaleidoscope/read |
|kaleidoscope.root.path            | /tmp/kaleidoscope |
|kaleidoscope.cmd                  | script/make_thumbnails.sh |
|kaleidoscope.outfile.ext          | jpg |
|kaleidoscope.default.resize       | 300x300 |
|kaleidoscope.max.upload.file.size | 10485760 |
|kaleidoscope.max.thumbnail.count  | 5 |
|kaleidoscope.expire.sec           | -120 |
|kaleidoscope.context.path         | /kaleidoscope |
|kaleidoscope.auto.remove          | true |