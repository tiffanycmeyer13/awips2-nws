#!/bin/sh
#
# Script to fix the directory structure for the Tsunami EAS GUI as deployed in 21.4.1.
#
# ----------------------------------------------------------------------------
# This software is in the public domain, furnished "as is", without technical
# support, and with no warranty, express or implied, as to its usefulness for
# any purpose.
#
#
# 1) Create the /awips2/apps/TsunamiEasGui/data subdirectory, and make it writable to
# all AWIPS users. Without this directrory, the GUI cannot create temporary files, so
# it fails to generate the proper messages for NWR.
#
# 2) Move configuration files and message templates to an NFS mounted directory, under
# /awips2/edex/data/share/TsunamiEasGui/etc. This makes the config files visible to all
# LX workstations.
#
# 3) Delete directory /awips2/apps/TsunamiEasGui/etc, and replace it with a symbolic
# link to the NFS mounted directory.
#
# This script should be run only once, as user "awips", on every LX workstation.
# However, the script insures that the NFS mounted directory will only be created once.
#
# Original version by Jim Buchman, 8/29/2023, based on Betty Tai's Python script.
#
readonly TSUGUI_HOME=/awips2/apps/TsunamiEasGui
readonly DEST_DIR=/awips2/edex/data/share/TsunamiEasGui/etc

echo
echo "Tsunami EAS GUI application - DIRECTORY REPAIR SCRIPT for 21.4.1"
echo "Run this script only once, as user awips, on each of the LX workstations."
echo

if [ \! `whoami` = root -a \! `whoami` = awips ]
then
	echo "You must run this script as user awips or root."
	exit 1
fi

if [ ! -d ${TSUGUI_HOME} ]
then
	echo "ERROR - GUI home directory $TSUGUI_HOME does not exist - exiting."
	exit 1
fi

#
# Create the data/ subdirectory, if it does not already exist.
# Set the file owner and group, and make it writeable to all AWIPS users.
#
if [ -d ${TSUGUI_HOME}/data ]
then
	echo "Note: temporary file directory ${TSUGUI_HOME}/data already exists."
else
	echo "Creating directory ${TSUGUI_HOME}/data with group WRITE permission ..."
	mkdir ${TSUGUI_HOME}/data
fi

chown awips:fxalpha ${TSUGUI_HOME}/data
chmod 775 ${TSUGUI_HOME}/data
chmod 775 ${TSUGUI_HOME}/log

#
# Create the new "etc" directory on the NFS shared area. (It might already
# have been created by this script, run from another LX).
#
if [ -d ${DEST_DIR} ]
then
	echo "Note: shared config directory ${DEST_DIR} already exists."
else
	echo "Creating config file directory ${DEST_DIR} ..."
	mkdir -p ${DEST_DIR}
	chown awips:fxalpha ${DEST_DIR}
	chmod 775 ${DEST_DIR}
fi

#
# Check whether this script has already been run on this LX.
#
if [ -h ${TSUGUI_HOME}/etc -a -d ${DEST_DIR} ]
then
	echo "WARNING - it looks like this script has already been run:"
	echo "  ${TSUGUI_HOME}/etc exists and is a symbolic link"
	echo "  Exiting ..."
	exit 0
fi

#
# Move the existing configuration files to the new, shared config directory.
# In 90% of cases this will be easy because the WFO either is not in a coastal
# zone, or has not yet configured the GUI, so there will be no files to copy.
# For those who *have* configured the GUI, this is a bit problematic.
#	- If only one LX has configured the files, no problem - just copy those files.
#	- If the destination directory already has config_tsu.py, compare it to
#		the local file. If they match, do not copy.
#	- If they don't match, copy it, with a suffix indicating the LX it came from.
#
if [ ! -d ${TSUGUI_HOME}/etc ]
then
	echo "WARNING - directory ${TSUGUI_HOME}/etc does not exist (unexpected)." 
	echo "  Not an error, but no files will be copied to ${DEST_DIR}"

elif [ \! "$(ls -A ${TSUGUI_HOME}/etc)" ]
then
	echo "Note: there are no files in ${TSUGUI_HOME}/etc to copy (normal)"

elif [ \! "$(ls -A ${DEST_DIR})" -o \! -f ${DEST_DIR}/config_tsu.py ]
then

	# Destination directory is empty *or* config_tsu.py is not present.
	echo "Moving files from ${TSUGUI_HOME}/etc to ${DEST_DIR} ..."
	mv -f ${TSUGUI_HOME}/etc/* ${DEST_DIR}

else
	# If there are any template files, move them first.
	if [ "$(ls -A ${TSUGUI_HOME}/etc/template*)" ]
	then
		echo "Moving template files to ${DEST_DIR} ..."
		mv -f ${TSUGUI_HOME}/etc/template* ${DEST_DIR}
	fi

	sfx=`hostname |cut -f 1 -d -`
	if [ "$(diff -w ${TSUGUI_HOME}/etc/config_tsu.py ${DEST_DIR}/config_tsu.py)" ]
	then
		echo "WARNING: config_tsu.py in ${TSUGUI_HOME}/etc differs from the one in ${DEST_DIR}!"
		echo "    This file will be copied as config_tsu.py.${sfx}"
		mv -f ${TSUGUI_HOME}/etc/config_tsu.py ${DEST_DIR}/config_tsu.py.${sfx}
	else
		echo "config_tsu.py in ${TSUGUI_HOME}/etc is the same as the one in ${DEST_DIR}; not copied."
	fi
fi

#
# All files from the local etc directory have now been saved.
# Remove the local etc directory, and replace it with a symbolic link.
#
echo "Removing local directory ${TSUGUI_HOME}/etc ..."
rm -rf ${TSUGUI_HOME}/etc

echo "Creating symbolic link to directory ${DEST_DIR} ..."
ln -s  ${DEST_DIR} ${TSUGUI_HOME}/etc
chown awips:fxalpha ${TSUGUI_HOME}/etc

echo
echo "Contents of directory ${DEST_DIR}:"
ls -l ${DEST_DIR}
echo

echo `date` " - Tsunami EAS GUI directory structure has been updated!"
echo

exit 0
