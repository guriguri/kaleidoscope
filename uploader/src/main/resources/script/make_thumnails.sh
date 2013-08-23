#!/bin/bash

if [ $# -ne 4 ]; then
	echo "Usage: $0 INPUT_FILE OUT_FILE_PREFIX OUT_FORMAT RESIZES"
	echo "       $0 input.jpg output_prefix png 300x300,100x100"
	exit 1
fi

INPUT_FILE=$1
OUT_FILE_PREFIX=$2
OUT_FORMAT=$3
RESIZES=$4

for resize in ${RESIZES//,/ }; do
	gm convert -size "${resize}" "${INPUT_FILE}" -resize "${resize}" +profile "*" "${OUT_FILE_PREFIX}_${resize}.${OUT_FORMAT}"
done