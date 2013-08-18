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
[max@lion-centos ~]$ cat gm_convert.sh 
#!/bin/bash

if [ $# -ne 3 ]; then
	echo "Usage: $0 INPUT_FILE OUT_FILE RESIZE"
	echo "       $0 input.jpg output.png 300x300"
	exit 1
fi

INPUT_FILE=$1
OUT_FILE=$2
RESIZE=$3

gm convert -size "${RESIZE}" "${INPUT_FILE}" -resize "${RESIZE}" +profile "*" "${OUT_FILE}"