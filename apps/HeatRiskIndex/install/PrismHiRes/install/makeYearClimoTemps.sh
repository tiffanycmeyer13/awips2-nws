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

unset DISPLAY

scriptPath=`readlink -f $0`

#Set Directories
export INSTALLDir=`dirname $scriptPath`
export BASEDir=`dirname $INSTALLDir`

export logDir=$BASEDir/logs
if [[ ! -d $logDir ]]; then
   logecho ""
   logecho "Created $logDir"
   mkdir -p $logDir
fi
rundate=`date +%Y%m%d%H%M`
logFile="$logDir/install_${rundate}.log"
echo "Watch the log file for progress $logFile"
echo "tail -f $logFile"

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

cd $GFEDIR
PROC=${GFEDIR}/runProcedure

echo "Creating a year worth of climate data."
echo "Run time will vary from office to office but should take from 5 to 10 minutes depending on your domain size."
sleep 2
echo "" 
$PROC -site $SITEID -n MakeClimoTemps -c gfeConfig -m _Climo >> ${logFile} 2>&1

$PROC -site $SITEID -n LoadClimoTemps -c gfeConfig -m _Climo >> ${logFile} 2>&1
