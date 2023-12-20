#!/bin/bash 
#######################################################################
#
#  Script to Create Climate Grids from 800m PRISM data.
#  Runs GFE Procedure makeClimoTempsProc
#
#
#
#  SOFTWARE HISTORY
#
# Date          Ticket#  Engineer       Description
# ------------- -------- ---------      -------------------------------
# Sep 08, 2022  23085    mgamazaychikov Baselined for awips2 
#
##
#######################################################################

#====================================================================
#
#  Get script name and whether it is running from a tty
#
PROG=`basename $0`
NOTTY=`tty | grep -ci not`


echo "Starting  at: `date +"%Y%m%d-%T"` UTC" 

#  Setup environment for DEFAULT_HOST
source /awips2/GFESuite/bin/setup.env

# Default to GFE AWIPS install
GFEDIR=/awips2/GFESuite/bin

if [ -d $GFEDIR ]
then
  echo "Directory $GFEDIR exists."
  if [ ! -f $GFEDIR/runProcedure ]
  then
    echo "runProcedure was not found in $GFEDIR."
    echo "Execution STOPPED! Exiting..."
   exit 1
  fi
else
  echo "Directory $GFEDIR does not exist."
  echo "Execution STOPPED! Exiting..."
  exit 1
fi

# Set SITE ID
GFESUITE_SITEID=`echo ${SITE_IDENTIFIER} | tr a-z A-Z`

SITEID=$GFESUITE_SITEID
echo ""
echo "Using $GFESUITE_SITEID for site ID"
if [[ $SITEID == "" ]]; then
   echo "site ID not defined. make sure the site ID is provided."
   exit 1
fi

#
GFEUser="SITE"             # user installed under
#
# BASEConfig is the normal configuration file to run GFE with - typically this
#         is just gfeConfig
#
BASEConfig="gfeConfig"     # normal config file to use
#
#  Setup logfile for this run
#
logDir="/awips2/edex/data/share/HeatRiskIndex/scripts/logs"
DATECODE=`date -u +"%Y%m%d"`
LOGDIR=$logDir/$DATECODE
if [[ ! -d $LOGDIR ]]
then
   mkdir -m 777 -p $LOGDIR
fi
DAY=`date +"%Y%m%d"`
#

STAMP=`date +"%Y%m%d"`
LOG="LoadClimoTemps_${STAMP}.log"
LOGFILE="${LOGDIR}/${LOG}"

Logtime() {
  ctime=`date -u "+%Y/%m/%d %H:%M:%S"`
  if [ $NOTTY -gt 0 ]
  then
     echo "$ctime:$1" >>$LOGFILE
  else
     echo "$ctime:$1" | tee -a $LOGFILE
  fi
}

Logtime "$PROG started."
Logtime "HOST: `hostname` "
Logtime "USER: $USER"
Logtime "PID: $$"


echo ${LOGFile}

PROC=$GFEDIR/runProcedure

if [ $NOTTY -gt 0 ]
then
  $PROC -site $SITEID -n LoadClimoTemps -c gfeConfig -m _Climo >> $LOGFILE 2>&1
else
  $PROC -site $SITEID -n LoadClimoTemps -c gfeConfig -m _Climo 2>&1 | tee -a $LOGFILE
fi

Logtime "$PROG exiting."

exit 0
