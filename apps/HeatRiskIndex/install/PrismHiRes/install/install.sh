#!/bin/bash
# Author: Mark Loeffelbein Email: mark.loeffelbein@noaa.gov
#         STID WRH
# Adapted from Jerry Weidenfield and Paul Jendroski's installERH_GFE_formatters.sh
#
#  SOFTWARE HISTORY
#
# Date          Ticket#  Engineer       Description
# ------------- -------- ---------      ---------------------------------------------
# Sep 08, 2022  23085    mgamazaychikov Baselined for awips2 
#
##

scriptPath=`readlink -f $0`

version="Version: 4.0 - 03/16/2022"

#
Usage()
{
  echo "install.sh [-all] [-u user]
      $version

   where user is GFE user name (default = SITE).

   This is a installation script to install the PrismHiRes Suite.
   This script must be run from the install directory."
}

#if [[ $runuser != "awips" ]]; then
#   echo "Program $0 must be run as user awips."
#   echo "Please log on as user awips."
#   exit 1
#fi


if [[ ! -d "/awips2/edex/data/manual" ]];then
   echo "/awips2/edex/data/manual/ not available."
   echo "Make sure this script is ran from pv1."
   exit 1
fi

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
      redecho "ERROR: Could not find GFE in /awips2/GFESuite"
      redecho "       Set environmental variable GFEDIR to GFE installation then try again."
      exit 1
  fi
fi

read -p "Install files as user $user? (y/n): " ans
if [ "$ans" != "y" -a "$ans" != "Y" ]
then
  exit 1
fi

#Set Directories
export INSTALLDir=`dirname $scriptPath`
export BASEDir=`dirname $INSTALLDir`
export binDir=$BASEDir/bin
export gfeDir=$BASEDir/gfe
export logDir=$BASEDir/logs
rundate=`date +%Y%m%d%H%M`
logFile="$logDir/install_${rundate}.log"


if [[ ! -d $binDir || ! -d $gfeDir ]]; then
   redecho "ERROR:  Ensure you have downloaded the following directories using svn from repository"
   redecho $binDir
   redecho $gfeDir
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

Climodir="/awips2/apps/HeatRiskIndex/runtime/PrismHiRes" 
DataDir="/awips2/apps/HeatRiskIndex/data"

logecho "Creating ${Climodir} directory"
mkdir -p $Climodir
sleep 2
logecho ""


# Install the file dailyData to the Climodir

if [[ ! -a ${Climodir}/data/Normals.MaxT.0101.txt ]];then

    # Check to make sure NECI data is staged correctly
    if [[ ! -e ${DataDir}/Normals.tar.bz2 ]]; then
      redecho "ERROR: PRISM data not found. Missing ${DataDir}/Normals.tar.bz2"
      redecho "Copy PRISM data from scp to ${DataDir}/Normals.tar.bz2"
      exit 1
    fi

    logecho "Installing NCEI Point Data to ${Climodir}/data"

    mkdir -p "${Climodir}/data"
    cp $DataDir/Normals.tar.bz2 ${Climodir}/data/Normals.tar.bz2
    tar -jxf ${Climodir}/data/Normals.tar.bz2 -C ${Climodir}/data
    rm -f $DataDir/Normals.tar.bz2
    rm -f ${Climodir}/data/Normals.tar.bz2

fi

if [[ ! -a $Climodir/env.sh ]];then
   logecho "Installing ${Climodir}/env.sh"
   sed "s/XXX/$site/g" $binDir/env.sh > ${Climodir}/env.sh
else
   sed "s/XXX/$site/g" $binDir/env.sh > ${Climodir}/env.sh.new
   test=`diff --brief ${Climodir}/env.sh.new ${Climodir}/env.sh`
   if [ -n "$test" ]; then
      logecho "Updating ${Climodir}/env.sh"
      mv ${Climodir}/env.sh.new ${Climodir}/env.sh
      let num=num+1
   else
      rm ${Climodir}/env.sh.new
   fi
fi


filelist="LoadClimateGrids.sh"
sleep 1
let num=0
mkdir -p "${Climodir}/bin"

for file in $filelist;do
   if [[ ! -a $Climodir/bin/$file ]]; then
     logecho "Installing ${Climodir}/bin/$file"
     cp $binDir/$file.tmp ${Climodir}/bin/$file
     let num=num+1 
   else
      cp $binDir/$file.tmp ${Climodir}/bin/$file.new
      test=`diff --brief ${Climodir}/bin/$file.new $Climodir/bin/$file`
      if [ -n "$test" ]; then
         logecho "Updating ${Climodir}/bin/$file"
         mv ${Climodir}/bin/$file.new ${Climodir}/bin/$file
         let num=num+1 
      else
         rm ${Climodir}/bin/$file.new
      fi 
   fi
done

# Create directory for PRISM800m

    # Check to make sure PRISM800mUS data is staged correctly
if [[ ! -e ${DataDir}/PRISM_conus_30yr_normal_800m.grib2.bz2 ]]; then
  redecho "ERROR: PRISM data not found. Missing ${DataDir}/PRISM_conus_30yr_normal_800m.grib2.bz2"
  redecho "Copy PRISM data from https://vlab.noaa.gov/svn/nwsscp/Gfe/Apps/PrismHiRes/tags/data/PRISM_conus_30yr_normal_800m.grib2.bz2 to ${DataDir}/PRISM_conus_30yr_normal_800m.grib2.bz2"
  exit 1
fi
logecho "Unziping ${DataDir}/PRISM_conus_30yr_normal_800m.grib2.bz2 in place"
bzip2 -d ${DataDir}/PRISM_conus_30yr_normal_800m.grib2.bz2
logecho "Moving PRISM_conus_30yr_normal_800m.grib2 to /awips2/edex/data/manual/LDAD-GRIB_PRISM_conus_30yr_normal_800m.grib2"
mv ${DataDir}/PRISM_conus_30yr_normal_800m.grib2 /awips2/edex/data/manual/LDAD-GRIB_PRISM_conus_30yr_normal_800m.grib2

logecho ""

sleep 2

logecho "Installing MakeClimoTemps procedure "
$GFEDIR/bin/ifpServerText -u $user -o $site -s -n MakeClimoTemps -f $gfeDir/MakeClimoTemps.Procedure -c Procedure

sleep 2
logecho ""

logecho "Installing LoadClimoTemps procedure "
$GFEDIR/bin/ifpServerText -u $user -o $site -s -n LoadClimoTemps -f $gfeDir/LoadClimoTemps.Procedure -c Procedure

sleep 1 
logecho ""

sleep 2
logecho ""

greenecho "Install Complete"
logecho ""

exit


