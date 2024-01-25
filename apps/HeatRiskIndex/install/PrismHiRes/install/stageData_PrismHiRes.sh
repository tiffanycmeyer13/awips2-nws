#!/bin/bash
#
# This script copies PrismHiRes data from VLAB to the staging area
#
#  SOFTWARE HISTORY
#
# Date          Ticket#  Engineer       Description
# ------------- -------- ---------      ---------------------------------------------
# Oct 20, 2022  23085    mgamazaychikov Created 
#
##
DataDir="/awips2/edex/data/share/HeatRiskIndex/data/stage/PrismHiRes"
echo "Copying NCEI data to $DataDir"
svn export https://vlab.noaa.gov/svn/nwsscp/Gfe/Apps/PrismHiRes/tags/data/Normals.tar.bz2 ${DataDir}/Normals.tar.bz2

echo "Copying PRISM data to $DataDir"
svn export https://vlab.noaa.gov/svn/nwsscp/Gfe/Apps/PrismHiRes/tags/data/PRISM_conus_30yr_normal_800m.grib2.bz2 ${DataDir}/PRISM_conus_30yr_normal_800m.grib2.bz2

echo "Done"
echo ""
ls -l ${DataDir}/Normals.tar.bz2 ${DataDir}/PRISM_conus_30yr_normal_800m.grib2.bz2
echo ""
exit 0
