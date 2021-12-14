#!/bin/bash

#####################################################################################
# tony.freeman@noaa.gov : 26 April 2018
#
# Simply copy the radio ready tsunami file into nwr directory structure
# Called from nwr_tsu.py
#
# ARGV1 
#   -a : automatically transfer
#   -d : pending directory
#
# ARGV2
#   The full path to the file to copy
#
#####################################################################################

function usage () {
	echo "USAGE:"
	echo " "
	echo "$0 <-a|-d> <full path to radio ready tsunami announcement file>"
	echo " "
        echo "#   -a : automatically transfer"
        echo "#   -d : pending directory"
	echo " "
	echo "Example: "
	echo " "
	echo "  $0 -a /localapps/runtime/TsunamiEasGui/data/tsradio.txt"
	echo "  Will send tsradio.txt to READY directory"
	echo " "
	echo "  $0 -d /localapps/runtime/TsunamiEasGui/data/tsradio.txt"
	echo "  Will send tsradio.txt to PENDING directory"
	echo " "
	exit
}

######################################################### 
### Sanity Checks:
######################################################### 

ARGV1=$1
ARGV2=$2

if [[ -z $ARGV1 ]]
then
	usage
fi

if [[ -z $ARGV2 ]]
then
	usage
fi

######################################################### 
### Program Logic:
######################################################### 

if [[ $ARGV1 == "-a" ]]
then
	cp -v $ARGV2 /data/fxa/workFiles/nwr/ready/
elif [[ $ARGV1 == "-d" ]]
then
	cp -v $ARGV2 /data/fxa/workFiles/nwr/pending/
else
	echo "An ERROR has occurred"
	exit 1
fi
