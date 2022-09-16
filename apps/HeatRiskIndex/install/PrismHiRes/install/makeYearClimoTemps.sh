#  SOFTWARE HISTORY
#
# Date          Ticket#  Engineer       Description
# ------------- -------- ---------      ---------------------------------------------
# Sep 08, 2022  23085    mgamazaychikov Baselined for awips2 
#
##

NOTTY=`tty | grep -ci not`
#
#  C O N F I G U R A T I O N   S E C T I O N
#

start=`date +%s`

. /awips2/GFESuite/bin/setup.env
. /awips2/apps/HeatRiskIndex/runtime/PrismHiRes/env.sh
unset DISPLAY

scriptPath=`readlink -f $0`

#Set Directories
export INSTALLDir=`dirname $scriptPath`
export BASEDir=`dirname $INSTALLDir`
export binDir=$BASEDir/bin
export gfeDir=$BASEDir/gfe
export logDir=$BASEDir/logs
rundate=`date +%Y%m%d%H%M`
logFile="$logDir/install_${rundate}.log"


echo "Watch the log file for progress $logFile"
echo "tail -f $logFile"

#
#  Setup environment for DEFAULT_HOST
#
#source $runDir/env.sh
if [ -d $GFEDIR ]
then
  echo "Directory $GFEDIR exists."
  if [ ! -f $GFEDIR/runProcedure ]
  then
    echo "runProcedure was not found in $GFEDIR."
    echo "Enter the correct path for runProcedure in env.sh and try again."
   exit 1
  fi
else
  echo "Directory $GFEDIR does not exist."
  echo "Enter the correct path for runProcedure in env.sh and try again."
  exit 1
fi

cd $GFEDIR
PROC=${GFEDIR}/runProcedure

echo "Creating a year worth of climate data."
echo "Run time will vary from office to office but should take from 5 to 10 minutes depending on your domain size."
sleep 2
echo "" 
$PROC -site $SITEID -n MakeClimoTemps -c gfeConfig -m _Climo >> ${logFile} 2>&1

$PROC -site $SITEID -n LoadClimoTemps -c gfeConfig -m _Climo >> ${logFile} 2>&1
