@echo off

set INPUT_FILE=%1
set OUT_FILE_PREFIX=%2
set OUT_FORMAT=%3
set RESIZES=%4 %5 %6 %7 %8 %9

set LOG_FILE=/tmp/kaleidoscope/output.txt

echo "%date% %time%" >> %LOG_FILE%
echo " %*" >> %LOG_FILE%

for %%r in (%RESIZES%) do (
	echo COPY %INPUT_FILE% %OUT_FILE_PREFIX%_%%r.%OUT_FORMAT% >> %LOG_FILE%
	COPY %INPUT_FILE% %OUT_FILE_PREFIX%_%%r.%OUT_FORMAT%
)

echo "----------" >> %LOG_FILE%
