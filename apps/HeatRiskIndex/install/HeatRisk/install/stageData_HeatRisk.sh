#!/bin/bash
#
# This script copies HeatRisk data from VLAB to the staging area
#
#  SOFTWARE HISTORY
#
# Date          Ticket#  Engineer       Description
# ------------- -------- ---------      ---------------------------------------------
# Oct 20, 2022  23085    mgamazaychikov Created 
#
##
DataDir="/awips2/edex/data/share/HeatRiskIndex/data/stage/HeatRisk"
echo "Copying the deltas file to $DataDir"
svn export https://vlab.noaa.gov/svn/nwsscp/Gfe/Apps/HeatRisk/tags/data/deltas.tar.bz2 $DataDir/deltas.tar.bz2

echo "Copying the datafiles to $DataDir"
svn export https://vlab.noaa.gov/svn/nwsscp/Gfe/Apps/HeatRisk/tags/data/datafiles.tar.bz2 $DataDir/datafiles.tar.bz2


echo "Done"
echo ""
ls -l ${DataDir}/deltas.tar.bz2 ${DataDir}/datafiles.tar.bz2
echo ""
exit 0
