#!/bin/bash
export GEOMETRY="$SCREEN_WIDTH""x""$SCREEN_HEIGHT""x""$SCREEN_DEPTH"

function shutdown {
  kill -s SIGTERM $PID
  wait $PID
}

rm -f /tmp/.X*lock

DISPLAY=$DISPLAY \
       xvfb-run --server-args="-screen 0 $GEOMETRY -ac +extension RANDR" \
       /usr/local/bin/ApacheDirectoryStudio/ApacheDirectoryStudio &
PID=$!

for i in $(seq 1 10)
do
  xdpyinfo -display $DISPLAY >/dev/null 2>&1
  if [ $? -eq 0 ]; then
    break
  fi
  echo Waiting xvfb...
  sleep 0.5
done

fluxbox -display $DISPLAY &
x11vnc -usepw -forever -display $DISPLAY

trap shutdown SIGTERM SIGINT
wait $PID
