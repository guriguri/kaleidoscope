#!/usr/bin/python
"""
usage: kaleidoscope.py file resizes
   ex) kaleidoscope.py test.jpg 10x10,20x20,30x30
"""
import sys, subprocess, json

HOST = "100dream.net:6487"
URL_CREATE = HOST + "/kaleidoscope/create"

if len(sys.argv) != 3:
	print __doc__
	raise SystemExit

FILE = sys.argv[1]
RESIZES = sys.argv[2]

def create(file, resizes):
	cmd = "curl -F upload=@" + file + " -F resizes=" + resizes + " " + URL_CREATE
	process = subprocess.Popen(cmd.split(), stdout=subprocess.PIPE)
	return process.communicate()[0]

def fetch(url):
	cmd = "curl -O " + url
	process = subprocess.Popen(cmd.split(), stdout=subprocess.PIPE)
	return process.communicate()[0]

jsonObj = json.loads(create(FILE, RESIZES))
result = jsonObj['result']

if result == 200:
	for thumbnail in jsonObj['thumbnails']:
		fetch(thumbnail)
else:
	print jsonObj['msg']
