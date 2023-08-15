#!/bin/sh
#
# My site id
SITEID=XXX

# Edex host
if [[ $PGHOST == "dv1f" ]]; then
   EDEX_HOST=ec:9581
else
   EDEX_HOST=ev:9581
fi
EDEX_HOME=/awips2/edex

# Directory containing gfeclient.sh
GFEDIR=/awips2/GFESuite/bin

# Directory containing cave
CAVE=/awips2/cave

# Bin directory containing "uengine"
BINDIR=/awips2/fxa/bin

# Home of log files
LOGHOME=/awips2/GFESuite/logs/${SITEID}
