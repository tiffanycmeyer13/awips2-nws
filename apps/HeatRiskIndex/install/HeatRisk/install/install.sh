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

if [[ ! -d "/awips2/edex/data/manual" ]];then
   echo "/awips2/edex/data/manual/ not available."
   echo "Make sure this script is ran from pv1."
   echo "Installation STOPPED! Exiting..."
   exit 1
fi

# Default to GFE AWIPS install
GFEDIR=/awips2/GFESuite/bin
if [ ! -f $GFEDIR/ifpServerText ]
  then
      echo "ERROR: Could not find GFE in $GFEDIR"
      echo "Installation STOPPED! Exiting..."
      exit 1
fi

# Setup environment for DEFAULT_HOST
source /awips2/fxa/bin/setup.env

user=SITE
logecho ""
logecho "Installing files as $user"

# Set SITE ID
GFESUITE_SITEID=`echo ${SITE_IDENTIFIER} | tr a-z A-Z`

site=$GFESUITE_SITEID
logecho ""
logecho "Using $GFESUITE_SITEID for site ID"

uppersite=`echo $site |tr '[a-z]' '[A-Z]'`

#Set Directories
INSTALLDir=`dirname $scriptPath`
BASEDir=`dirname $INSTALLDir`
installDataDir="$BASEDir/data"
DataDir="/awips2/edex/data/share/HeatRiskIndex/data/stage/HeatRisk"

logDir="$BASEDir/logs"
if [[ ! -d $logDir ]]; then
   logecho ""
   logecho "Created $logDir"
   mkdir -p $logDir
fi
rundate=`date +%Y%m%d%H%M`
logFile="$logDir/install_${rundate}.log"

binDir="$BASEDir/bin"
gfeProcedureDir="$BASEDir/gfe"
configDir="$BASEDir/config"
if [[ ! -d $binDir || ! -d $gfeProcedureDir || ! -d $configDir ]]; then
   echo "ERROR:  Ensure the following directories were created during the installation:"
   echo $binDir
   echo $gfeProcedureDir
   echo $configDir
   echo "Installation STOPPED! Exiting..."
   exit 1
fi

Climodir="/awips2/edex/data/share/HeatRiskIndex/data/climo/HeatRisk"
if [[ ! -d $Climodir ]]; then
   logecho ""
   logecho "Created $Climodir hierarchy"
   mkdir -p $Climodir
   mkdir -p ${Climodir}/data
   mkdir -p ${Climodir}/bin
   mkdir -p ${Climodir}/log
   mkdir -p ${Climodir}/data/levels
   sleep 2
fi

# Inure the correct version of PrismHiRes has been installed
if [[ ! -a /awips2/edex/data/share/HeatRiskIndex/runtime/PrismHiRes/data/dailyMaxT0101.npy ]];then
    logecho ""
    echo "ERROR: PrismHiRes must be at version 4.0 or later. Install version 4.0."
    echo "Installation STOPPED! Exiting..."
    exit 1
fi

# Insure the deltas tar has been staged to DataDir
if [[ ! -a $DataDir/deltas.tar.bz2 ]]
then
    echo "ERROR: delta file not found. Missing $DataDir/deltas.tar.bz2"
    echo "Copy the missing data by running the following command:"
    echo "svn export https://vlab.noaa.gov/svn/nwsscp/Gfe/Apps/HeatRisk/tags/data/deltas.tar.bz2 $DataDir/deltas.tar.bz2"
    echo "Installation STOPPED! Exiting..."
    exit 1
fi

# Insure the datafiles tar has been staged to DataDir
if [[ ! -a $DataDir/datafiles.tar.bz2 ]]
then
    echo "ERROR: datafiles.tar.bz2 not found. Missing $DataDir/datafiles.tar.bz2"
    echo "Copy the missing data by running the following command:"
    echo "svn export https://vlab.noaa.gov/svn/nwsscp/Gfe/Apps/HeatRisk/tags/data/datafiles.tar.bz2 $DataDir/datafiles.tar.bz2"
    echo "Installation STOPPED! Exiting..."
    exit 1
fi

logecho "Installing delta text files to ${Climodir}/data"
logecho ""

sleep 2

cp $DataDir/deltas.tar.bz2 ${Climodir}/data
tar -xvf ${Climodir}/data/deltas.tar.bz2 -C ${Climodir}/data
sleep 2
cp $DataDir/datafiles.tar.bz2 ${Climodir}
tar -xvf ${Climodir}/datafiles.tar.bz2 -C ${Climodir}/data
logecho "Copying zones.pck into place"
if [[ ! -a $installDataDir/zones.pck ]]
then
    echo "No new zones.pck available"
else
    cp $installDataDir/zones.pck ${Climodir}/data/zones.pck
fi

filelist="LoadHeatRisk.sh"
ScriptDir="/awips2/edex/data/share/HeatRiskIndex/scripts/HeatRisk"
sleep 1
for file in $filelist;do
   logecho "Copying over $file to ${ScriptDir}/$file"
   logecho ""
   cp $binDir/${file} ${ScriptDir}/${file}
done

logecho ""
logecho "Setting site in WeatherElementGroup"
sed "s/XXX/$site/g" $configDir/HeatRisk.WeatherElementGroup > $configDir/HeatRisk.WeatherElementGroup

logecho ""

# Installing new HeatRisk files
proclist="HeatRisk_install HeatRisk_daily HeatRisk_calculate HeatRisk_PointPlot HeatRisk_review"
elementlist="HeatRisk"
rmcolortablelist="HeatImpactLevels"
colortablelist="HeatRiskWWA HeatRisk"

for file in $proclist;do
    logecho "Installing ${file} procedure "
    $GFEDIR/ifpServerText -u $user -o $site -s -n ${file} -f $gfeProcedureDir/${file}.Procedure -c Procedure
done

for file in $elementlist;do
    logecho "Installing ${file} WeatherElementGroup "
    $GFEDIR/ifpServerText -u $user -o $site -s -n ${file} -f $configDir/${file}.WeatherElementGroup -c WeatherElementGroup
done

for file in $rmcolortablelist;do
    echo "Removing ${file} ColorTable "
    $GFEDIR/ifpServerText -u $user -o $site -d -n ${file} -c ColorTable
done

for file in $colortablelist;do
    logecho "Installing ${file} ColorTable "
    $GFEDIR/ifpServerText -u $user -o $site -s -n ${file} -f $configDir/${file}.ColorTable -c ColorTable
done

logecho "Done installing scripts."

logecho ""
logecho "HeatRisk installation complete!"
logecho ""

exit
