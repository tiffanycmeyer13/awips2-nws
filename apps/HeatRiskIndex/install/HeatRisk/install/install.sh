#!/bin/bash
# Author: Mark Loeffelbein Email: mark.loeffelbein@noaa.gov
#         STID WRH
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

Usage()
{
  echo "install.sh [-all] [-u user]
      $version

   where user is GFE user name (default = SITE).

   This is a installation script to install version 2.0 of HeatRisk."
}

logecho () {
    # command to write to terminal and logfile
    echo "$@"
    if [ -n "$logFile" ]; then
        echo "$@" >> $logFile
    fi
}

greenecho() {
    # echo in green
    logecho -e "\033[1;38;5;40m${1}\033[0m"
}

redecho() {
    # echo in red
    logecho -e "\033[1;38;5;196m${1}\033[0m"
}

redprompt() {
    # prompt in red, no new line
    logecho -en "\033[1;38;5;160m${1}\033[0m"
}

red() {
  echo "\033[1;38;5;196m${1}\033[0m"
}
green() {
  echo "\033[1;38;5;40m${1}\033[0m"
}

#Set Directories
INSTALLDir=`dirname $scriptPath`
BASEDir=`dirname $INSTALLDir`
binDir="$BASEDir/bin"
gfeDir="$BASEDir/gfe"
configDir="$BASEDir/config"
logDir="$BASEDir/logs"
installDataDir="$BASEDir/data"
Climodir="/awips2/apps/HeatRiskIndex/runtime/HeatRisk"
dataDir="/awips2/apps/HeatRiskIndex/data"

if [[ ! -a $dataDir/deltas.tar.bz2 ]]
then
    logecho ""
    redecho "Delta file not staged correctly!!! Missing $dataDir/deltas.tar.bz2"
    exit 1
fi



#
#  Setup environment for DEFAULT_HOST
#
source /awips2/fxa/bin/setup.env

allopt="no"
user=SITE
while  [ $# -ge 1 ]
do
  if [ $1 = "-all" ]
  then
   allopt=$1
  elif [ $1 = "-u" ]
  then
    user=$2
    shift
  else
    Usage
    exit 1
  fi
  shift
done

if [ -z $GFEDIR ]
then
# Default to AWIPS install
  GFEDIR=/awips2/GFESuite
  if [ ! -f $GFEDIR/bin/ifpServerText ]
  then
      echo "ERROR: Could not find GFE in /awips2/GFESuite"
      echo "       Set environmental variable GFEDIR to GFE installation then try again."
      exit 1
  fi
fi


# Install the file dailyData to the Climodir

if [[ ! -a /awips2/apps/HeatRiskIndex/runtime/PrismHiRes/data/dailyMaxT0101.npy ]];then
    logecho ""
    redecho "PrismHiRes must be at version 4.0 or later. Install version 4.0."
    redecho "Installation STOPPED!!! Exiting"
    exit 1
fi


read -p "Install files as user $user? (y/n): " ans
if [ "$ans" != "y" -a "$ans" != "Y" ]
then
  exit 1
fi

rundate=`date +%Y%m%d%H%M`
logFile="$logDir/install_${rundate}.log"

if [[ ! -d $binDir || ! -d $gfeDir || ! -d $configDir ]]; then
   redecho "ERROR:  Ensure you have downloaded the following directories using svn from repository"
   redecho $binDir
   redecho $gfeDir
   redecho $configDir
   exit 1
fi

# Set SITE ID
# First get site ID
GFESUITE_SITEID=`echo ${SITE_IDENTIFIER} | tr a-z A-Z`

if [ -z "$GFESUITE_SITEID" ]
then
  GFESUITE_SITEID="XXX"
fi
read -p "Use $GFESUITE_SITEID for site ID? (y/n): " ans
if [ "$ans" = "y" -o "$ans" = "Y" ]
then
  site=$GFESUITE_SITEID
else
  site=""
  while [ "$site" = "" ]
  do
    read -p "Enter your 3 letter Site ID: " site
    site=`echo $site |tr '[a-z]' '[A-Z]'`
    if [ "$site" = "X" ]
    then
      exit 1
    fi

    if [ `echo $site |grep -c '^[A-Z][A-Z][A-Z]'` -gt 0 ]
    then
      logecho "Using site ID = $site"
    else
      site=""
      logecho "Invalid input!"
    fi
  done
fi

lowsite=`echo $site |tr '[A-Z]' '[a-z]'`
uppersite=`echo $site |tr '[a-z]' '[A-Z]'`

# Let's make sure the directories that were supposed to already be present are

mkdir -p $Climodir
mkdir -p ${Climodir}/data
mkdir -p ${Climodir}/bin
mkdir -p ${Climodir}/log
mkdir -p ${Climodir}/data/levels

sleep 2
logecho ""


# Install the file dailyData to the Climodir

if [[ ! -a $dataDir/datafiles.tar.bz2 ]]
then
    redecho "datafiles.tar.bz2 not staged correctly!!! Missing $dataDir/datafiles.tar.bz2"
    exit 1
fi

logecho "Installing delta text files to ${Climodir}/data"
logecho ""

sleep 2

mv $dataDir/deltas.tar.bz2 ${Climodir}/data
tar -xvf ${Climodir}/data/deltas.tar.bz2 -C ${Climodir}/data
sleep 2
mv $dataDir/datafiles.tar.bz2 ${Climodir}
tar -xvf ${Climodir}/datafiles.tar.bz2 -C ${Climodir}/data
logecho "Copying zones.pck into place"
if [[ ! -a $installDataDir/zones.pck ]]
then
    redecho "No new zones.pck available"
else
    cp $installDataDir/zones.pck ${Climodir}/data/zones.pck
fi

filelist="LoadHeatRisk.sh"
sleep 1
for file in $filelist;do
   logecho "Copying over $file to ${Climodir}/bin/$file"
   logecho ""
   cp $binDir/${file}.tmp ${Climodir}/bin/${file}
done

logecho "Installing ${Climodir}/env.sh"
sed "s/XXX/$site/g" $binDir/env.sh.tmp > ${Climodir}/bin/env.sh

logecho ""

logecho "Setting site in WeatherElementGroup"
sed "s/XXX/$site/g" $configDir/HeatRisk.WeatherElementGroup.tmp > $configDir/HeatRisk.WeatherElementGroup

logecho ""

# Installing new HeatRisk files
proclist="HeatRisk_install HeatRisk_daily HeatRisk_calculate HeatRisk_PointPlot HeatRisk_review"
elementlist="HeatRisk"
rmcolortablelist="HeatImpactLevels"
colortablelist="HeatRiskWWA HeatRisk"

for file in $proclist;do
    logecho "Installing ${file} procedure "
    $GFEDIR/bin/ifpServerText -u $user -o $site -s -n ${file} -f $gfeDir/${file}.Procedure -c Procedure
done

for file in $elementlist;do
    logecho "Installing ${file} WeatherElementGroup "
    $GFEDIR/bin/ifpServerText -u $user -o $site -s -n ${file} -f $configDir/${file}.WeatherElementGroup -c WeatherElementGroup
done

for file in $rmcolortablelist;do
    echo "Removing ${file} ColorTable "
    $GFEDIR/bin/ifpServerText -u $user -o $site -d -n ${file} -c ColorTable
done

for file in $colortablelist;do
    logecho "Installing ${file} ColorTable "
    $GFEDIR/bin/ifpServerText -u $user -o $site -s -n ${file} -f $configDir/${file}.ColorTable -c ColorTable
done




logecho "Done installing scripts."

GFEUser="SITE"
BASEConfig="gfeConfig"

PROC=$GFEDIR/bin/runProcedure

logecho "Creating HeatRisk grids. Monitor the log file ${logFile} if desired."
logecho "tail -f ${logFile}"
$PROC -site $uppersite -u $GFEUser -n HeatRisk_install -c $BASEConfig -m _Climo >> ${logFile} 2>&1

$PROC -site $uppersite -u $GFEUser -n HeatRisk_daily -c $BASEConfig -m _Climo >> ${logFile} 2>&1

$PROC -site $uppersite -u $GFEUser -n HeatRisk_calculate -c $BASEConfig -m _Fcst >> ${logFile} 2>&1

greenecho "HeatRisk installation complete!"

exit

