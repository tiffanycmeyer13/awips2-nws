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

unset DISPLAY

logecho () {
    # command to write to terminal and logfile
    echo "$@"
    if [ -n "$logFile" ]; then
        echo "$@" >> $logFile
    fi
}

if [[ ! -d "/awips2/edex/data/manual" ]];then
   echo "/awips2/edex/data/manual/ not available."
   echo "Make sure this script is ran from pv1."
   exit 1
fi

# Setup environment for DEFAULT_HOST
source /awips2/fxa/bin/setup.env

# Default to GFE AWIPS install
GFEDIR=/awips2/GFESuite/bin
if [ ! -f $GFEDIR/ifpServerText ]
  then
      echo "ERROR: Could not find GFE in $GFEDIR"
      echo "Installation STOPPED! Exiting..."
      exit 1
fi

user=SITE
logecho ""
logecho "Installing files as $user"

# Set SITE ID
GFESUITE_SITEID=`echo ${SITE_IDENTIFIER} | tr a-z A-Z`

site=$GFESUITE_SITEID
logecho ""
logecho "Using $GFESUITE_SITEID for site ID"

#Set Directories
INSTALLDir=`dirname $scriptPath`
BASEDir=`dirname $INSTALLDir`
DataDir="/awips2/edex/data/share/HeatRiskIndex/data/stage/PrismHiRes"

logDir=$BASEDir/logs
if [[ ! -d $logDir ]]; then
   logecho ""
   logecho "Created $logDir"
   mkdir -p $logDir
fi
rundate=`date +%Y%m%d%H%M`
logFile="$logDir/install_${rundate}.log"

binDir=$BASEDir/bin
gfeDir=$BASEDir/gfe
if [[ ! -d $binDir || ! -d $gfeDir ]]; then
   echo "ERROR:  Ensure the following directories were created during the installation:"
   echo $binDir
   echo $gfeDir
   echo "Installation STOPPED! Exiting..."
   exit 1
fi

ClimoDir="/awips2/edex/data/share/HeatRiskIndex/runtime/PrismHiRes"
if [[ ! -d $ClimoDir ]]; then
   logecho ""
   logecho "Created $ClimoDir"
   mkdir -p $ClimoDir
   sleep 2
fi

# Install NCEI Point Data to the ClimoDir
if [[ ! -a ${ClimoDir}/data/Normals.MaxT.0101.txt ]];then

    # Check to make sure NCEI data is staged correctly
    if [[ ! -e ${DataDir}/Normals.tar.bz2 ]]; then
      echo "ERROR: NCEI data not found. Missing ${DataDir}/Normals.tar.bz2"
      echo "Copy NCEI data by running the following command:"
      echo "svn export https://vlab.noaa.gov/svn/nwsscp/Gfe/Apps/PrismHiRes/tags/data/Normals.tar.bz2 ${DataDir}/Normals.tar.bz2"
      echo "Installation STOPPED! Exiting..."
      exit 1
    fi

    logecho ""
    logecho "Installing NCEI Point Data to ${ClimoDir}/data"

    mkdir -p "${ClimoDir}/data"
    cp $DataDir/Normals.tar.bz2 ${ClimoDir}/data/Normals.tar.bz2
    tar -jxf ${ClimoDir}/data/Normals.tar.bz2 -C ${ClimoDir}/data
    rm -f ${ClimoDir}/data/Normals.tar.bz2

fi

# Install PRISM800m
# Check to make sure PRISM800mUS data is staged correctly
if [[ ! -e ${DataDir}/PRISM_conus_30yr_normal_800m.grib2.bz2 ]]; then
  echo "ERROR: PRISM data not found. Missing ${DataDir}/PRISM_conus_30yr_normal_800m.grib2.bz2"
  echo "Copy PRISM data by running the following command:"
  echo "svn export https://vlab.noaa.gov/svn/nwsscp/Gfe/Apps/PrismHiRes/tags/data/PRISM_conus_30yr_normal_800m.grib2.bz2 ${DataDir}/PRISM_conus_30yr_normal_800m.grib2.bz2"
  echo "Installation STOPPED! Exiting..."
  exit 1
fi
logecho ""
logecho "Installing PRISM CONUS 30-year Normals"
logecho "......Copying ${DataDir}/PRISM_conus_30yr_normal_800m.grib2.bz2 to ${ClimoDir}"
cp ${DataDir}/PRISM_conus_30yr_normal_800m.grib2.bz2 ${ClimoDir}/PRISM_conus_30yr_normal_800m.grib2.bz2
logecho "......Unziping ${ClimoDir}/PRISM_conus_30yr_normal_800m.grib2.bz2 in place"
bzip2 -d ${ClimoDir}/PRISM_conus_30yr_normal_800m.grib2.bz2
logecho "......Moving PRISM_conus_30yr_normal_800m.grib2 to /awips2/edex/data/manual"
mv ${ClimoDir}/PRISM_conus_30yr_normal_800m.grib2 /awips2/edex/data/manual/LDAD-GRIB_PRISM_conus_30yr_normal_800m.grib2

sleep 2

filelist="LoadClimateGrids.sh"
ScriptDir="/awips2/edex/data/share/HeatRiskIndex/scripts/PrismHiRes"
sleep 1
let num=0

for file in $filelist;do
   if [[ ! -a $ScriptDir/$file ]]; then
     logecho ""
     logecho "Installing ${ScriptDir}/$file"
     cp $binDir/$file ${ScriptDir}/$file
     let num=num+1 
   else
      cp $binDir/$file ${ScriptDir}/$file.new
      test=`diff --brief ${ScriptDir}/$file.new $ScriptDir/$file`
      if [ -n "$test" ]; then
         logecho ""
         logecho "Updating ${ScriptDir}/$file"
         mv ${ScriptDir}/$file.new ${ScriptDir}/$file
         let num=num+1 
      else
         rm ${ScriptDir}/$file.new
      fi 
   fi
done

logecho ""
logecho "Installing MakeClimoTemps procedure "
$GFEDIR/ifpServerText -u $user -o $site -s -n MakeClimoTemps -f $gfeDir/MakeClimoTemps.Procedure -c Procedure

sleep 2

logecho ""
logecho "Installing LoadClimoTemps procedure "
$GFEDIR/ifpServerText -u $user -o $site -s -n LoadClimoTemps -f $gfeDir/LoadClimoTemps.Procedure -c Procedure

sleep 2

logecho ""
logecho "PrismHiRes installation complete!"
logecho ""

exit
