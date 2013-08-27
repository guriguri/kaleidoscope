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
	
	cd rest-api
	mvn -e clean install assembly:single -DskipTests=true --no-snapshot-updates
	
	cp target/kaleidoscope-rest-api-0.0.1-SNAPSHOT-stand-alone.zip $KALEIDOSCOPE_APP_HOME
	cd $KALEIDOSCOPE_APP_HOME
	
	unzip kaleidoscope-rest-api-0.0.1-SNAPSHOT-stand-alone.zip
	bin/runner start

## 환경설정

	kaleidoscope.domain=0.0.0.0
	kaleidoscope.port=6487
	kaleidoscope.context.path=/kaleidoscope
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
	kaleidoscope.auto.remove=true
	kaleidoscope.expire.sec=-120


| 구분                              | 설명                                                                        |
|-----------------------------------|-----------------------------------------------------------------------------|
| kaleidoscope.domain               | Listen IP 혹은domain. 공백 혹은 0.0.0.0 일 경우 모든IP 에서 Listen          |
| kaleidoscope.port                 | port                                                                        |
| kaleidoscope.context.path         | context path                                                                |
| kaleidoscope.read.url             | 썸네일 생성후 조회를 위한 url로 썸네일 생성과 조회 domain 이 다를 경우 유용 |
| kaleidoscope.root.path            | 썸네일이 생성될 경로                                                        |
| kaleidoscope.cmd                  | 썸네일 생성을 위한 script 상대경로                                          |
| kaleidoscope.outfile.ext          | 썸네일 포멧                                                                 |
| kaleidoscope.default.resize       | default 썸네일 크기                                                         |
| kaleidoscope.max.upload.file.size | 업로드 파일의 최대 사이즈 (단위: byte)                                      |
| kaleidoscope.max.thumbnail.count  | 한번에 생성할 수 있는 썸네일의 수                                           |
| kaleidoscope.auto.remove          | 생성된 썸네일 자동 삭제 활성화 여부                                         |
| kaleidoscope.expire.sec           | 생성된 썸네일이 자동 삭제될 시간 (단위: 초)                                 |


## API
* 썸네일 생성
 * request
  * url: http://$kaleidoscope.domain:$kaleidoscope.port/$kaleidoscope.context.path/create
  * method: post
  * param
   * file: 전송할 파일
   * resizes
    * 생성할 썸네일의 사이즈(Width x Height)로 콤마(,) 리스트. (예: 300x300,400x400)
    * 리스트는 $kaleidoscope.max.thumbnail.count 값 이하여야 함.
 * response
  * result
   * 성공(200)
   * 실패(500)
  * msg
   * 성공: "success"
   * 실패
    * file 이 없을 경우: "need to file"
    * file 사이즈가 $kaleidoscope.max.upload.file.size 보다 클 경우: "The file's size is limited to $kaleidoscope.max.upload.file.size"
    * 요청한 썸네일 수가 $kaleidoscope.max.thumbnail.count 보다 많을 경우: "The thumbnails is limited to $kaleidoscope.max.thumbnail.count"
  * thumbnails
   * 생성된 썸네일 array
  * expireDate
   * 생성된 썸네일이 삭제될 시간 (ISO8601 포맷)
  * example
   * 성공

	{
		"result": 200,
		"msg": "success",
		"thumbnails": ["http://100dream.net:6487/kaleidoscope/read/2013/08/27/22/10/07526b2b-d92b-4ed6-9cb2-a91844aeef3a_300x300.jpg"],
		"expireDate": "2013-08-27T22:12:00+0900"
	}

   * 실패

	{
		"result": 500,
		"msg": "need to file"
	}
	
	{
		"result": 500,
		"msg": "The file's size is limited to 10485760"
	}
	
	{
		"result": 500,
		"msg": "The thumbnails is limited to 5"
	}


* 썸네일 조회
 * request
  * url: http://$kaleidoscope.domain:$kaleidoscope.port/$kaleidoscope.context.path/read/yyyy/MM/dd/HH/mm/$FILENAME
  * method: get
 * response
  * result
   * 성공(없음. 성공시 썸네일 이미지가 전송)
   * 실패(404)
  * msg
   * 실패
    * 조회할 썸네일이 없을 경우: "not found"
  * example

	{
		"result": 404,
		"msg": "not found"
	}
  

* 썸네일 삭제
 * request
  * url: http://$kaleidoscope.domain:$kaleidoscope.port/$kaleidoscope.context.path/delete
  * method: post
  * param
   * url: 썸네일 url
 * response
  * result
   * 성공(200)
   * 실패(500)
  * msg
   * 성공: "success"
   * 실패
    * url이 공백일 경우: "invalid url, url is empty"
    * url에 허용되지 않은 문자가 포함될 경우: "invalid url, url has invalid chars"
  * example

	{
		"result": 200,
		"msg": "success"
	}
	
	{
		"result": 500,
		"msg": "invalid url, url is empty"
	}
	
	{
		"result": 500,
		"msg": "invalid url, url has invalid chars"
	}

## Demo
* http://100dream.net:6487
