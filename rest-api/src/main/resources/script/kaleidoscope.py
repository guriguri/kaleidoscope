#!/usr/bin/python
"""
usage: kaleidoscope.py file resizes
   ex) kaleidoscope.py test.jpg 10x10,20x20,30x30
"""
import os, sys, subprocess, json

HOST = "100dream.net:6487"
URL_CREATE = HOST + "/kaleidoscope/create"

if len(sys.argv) != 3:
	print __doc__
	raise SystemExit

FILE = sys.argv[1]
RESIZES = sys.argv[2]
TMP_FILE = None

def getFilePath(file):
	if file.startswith('file://') == True:
		return file.replace('file://', '')
	elif file.startswith('http://') == True or file.startswith('https://') == True:
		global TMP_FILE
		TMP_FILE = fetch(file)
		return TMP_FILE
	else:
		return file

def create(file, resizes):
	cmd = "curl -F upload=@" + getFilePath(file) + " -F resizes=" + resizes + " " + URL_CREATE
	process = subprocess.Popen(cmd.split(), stdout=subprocess.PIPE)
	return process.communicate()[0]

def fetch(url):
	cmd = "curl -O " + url
	process = subprocess.Popen(cmd.split(), stdout=subprocess.PIPE)
	ret = process.wait()
	if ret == 0:
		return os.path.basename(url)
	else:
		return ret

jsonObj = json.loads(create(FILE, RESIZES))
result = jsonObj['result']

if TMP_FILE is not None:
	os.remove(TMP_FILE)	

if result == 200:
	for thumbnail in jsonObj['thumbnails']:
		fetch(thumbnail)
else:
	print jsonObj['msg']
