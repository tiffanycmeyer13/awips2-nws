#!/bin/sh
#
# Tsunami GUI calls this script to determine whether AWIPS is currently running
# in Live, Test, or Practice mode.

TESTCHECK="$TMCP_HOME/bin/getTestMode"

if [ -x ${TESTCHECK} ]; then
    ${TESTCHECK} >/dev/null 2>&1
    status=${?}
    if [ $status -eq 11 ]; then
        MODE="test"
    elif [ $status -eq 12 ];then
        MODE="practice"
    elif [ $status -eq 15 ];then
        MODE="live"
    else
        MODE="live"
    fi
else
    MODE="unknown"
fi
echo $MODE
