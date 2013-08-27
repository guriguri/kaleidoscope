#!/bin/bash

INPUT_FILE=$1
OUT_FILE_PREFIX=$2
OUT_FORMAT=$3
RESIZES=$4

LOG_FILE=/tmp/kaleidoscope/output.txt

echo "`date`" >> $LOG_FILE
echo "$@" >> $LOG_FILE

for resize in ${RESIZES//,/ }; do
	echo "cp -rf \"${INPUT_FILE}\" \"${OUT_FILE_PREFIX}_${resize}.${OUT_FORMAT}\"" >> $LOG_FILE
	cp -rf "${INPUT_FILE}" "${OUT_FILE_PREFIX}_${resize}.${OUT_FORMAT}"
done

echo "----------" >> $LOG_FILE
