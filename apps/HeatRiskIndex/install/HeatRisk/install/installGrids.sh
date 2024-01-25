#!/bin/bash
# Author: Mark Loeffelbein Email: mark.loeffelbein@noaa.gov
#         STID WRH
# This is a installation script to install version 2.0 of HeatRisk.
#
#  SOFTWARE HISTORY
#
# Date          Ticket#  Engineer       Description
# ------------- -------- ---------      ---------------------------------------------
# Sep 08, 2022  23085    mgamazaychikov Baselined for awips2
#
##

scriptPath=`readlink -f $0`

version="Version: 2.0 - 05/11/2022"

unset DISPLAY

logecho () {
    # command to write to terminal and logfile
    echo "$@"
    if [ -n "$logFile" ]; then
        echo "$@" >> $logFile
    fi
}

# Default to GFE AWIPS install
GFEDIR=/awips2/GFESuite/bin
GFEUser="SITE"
BASEConfig="gfeConfig"

PROC=$GFEDIR/runProcedure

if [ ! -f $PROC ]
  then
      echo "ERROR: Could not find $PROC in $GFEDIR"
      echo "Installation STOPPED! Exiting..."
      exit 1
fi

# Setup environment for DEFAULT_HOST
source /awips2/fxa/bin/setup.env

# Set SITE ID
GFESUITE_SITEID=`echo ${SITE_IDENTIFIER} | tr a-z A-Z`

site=$GFESUITE_SITEID
logecho ""
logecho "Using $GFESUITE_SITEID for site ID"

uppersite=`echo $site |tr '[a-z]' '[A-Z]'`

#Set Directories
INSTALLDir=`dirname $scriptPath`
BASEDir=`dirname $INSTALLDir`

logDir="$BASEDir/logs"
if [[ ! -d $logDir ]]; then
   logecho ""
   logecho "Created $logDir"
   mkdir -p $logDir
fi
rundate=`date +%Y%m%d%H%M`
logFile="$logDir/install_${rundate}.log"

logecho "Creating HeatRisk grids. Monitor the log file ${logFile} if desired."
logecho "tail -f ${logFile}"
$PROC -site $uppersite -u $GFEUser -n HeatRisk_install -c $BASEConfig -m _Climo >> ${logFile} 2>&1

$PROC -site $uppersite -u $GFEUser -n HeatRisk_daily -c $BASEConfig -m _Climo >> ${logFile} 2>&1

$PROC -site $uppersite -u $GFEUser -n HeatRisk_calculate -c $BASEConfig -m _Fcst >> ${logFile} 2>&1

logecho ""
logecho "HeatRisk grids installation complete!"
logecho ""

exit
