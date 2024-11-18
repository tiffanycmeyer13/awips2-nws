#!/bin/bash 
#######################################################################
#
#  Script to load heat risk grids and calculates HeatRisk.
#  Runs GFE Procedure HeatRisk_daily and HeatRisk_calculate
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

unset DISPLAY

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
#  Setup logfile for this run
#
Logtime() {
  ctime=`date -u "+%Y/%m/%d %H:%M:%S"`
  echo "$ctime:$1" >>$LOGFILE
}

LOGHOME=/awips2/GFESuite/logs/${SITEID}
DATECODE=`date -u +"%Y%m%d"`
LOGDIR=$LOGHOME/$DATECODE

if [[ ! -d $LOGDIR ]]
then
   mkdir -m 777 -p $LOGDIR
fi
h=`date +%-k`
LOGFILE="${LOGDIR}/Run_LoadHeatRisk_${h}Z.log"

Logtime "$PROG started."
Logtime "HOST: `hostname` "
Logtime "USER: $USER"
Logtime "PID: $$"


echo ${LOGFile}

PROC=$GFEDIR/runProcedure

$PROC -site $SITEID -n HeatRisk_daily -c gfeConfig -m _Climo >> $LOGFILE 2>&1
sleep 2
$PROC -site $SITEID -n HeatRisk_calculate -c gfeConfig -m _Fcst >> $LOGFILE 2>&1


Logtime "$PROG exiting."

exit 0
