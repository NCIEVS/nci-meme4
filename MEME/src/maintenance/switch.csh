#!/bin/csh -f
if ("x$1" == "x-h" || "x$1" == "x-help") then
    echo "Usage: $0 (on|off)"
    exit 1
endif

if ($1 == "ON" || $1 == "on") then
    cat >! $MEME_HOME/etc/switch.txt << EOF
ON
EOF
    exit 0
endif

if ($1 == "OFF" || $1 == "off") then
    cat >! $MEME_HOME/etc/switch.txt << EOF
OFF
EOF
    exit 0
endif

cat  $MEME_HOME/etc/switch.txt 

