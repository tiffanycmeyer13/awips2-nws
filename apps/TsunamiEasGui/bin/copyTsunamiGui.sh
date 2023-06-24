#!/bin/sh
#
# To be run during initial installation of the Tsunami EAS GUI as a part
# of the AWIPS baseline.
# Copy the GUI's configuration files (if they exist) from the local app
# directory to the AWIPS baseline apps directory.
#
# Original version by Jim Buchman, 10/4/2021.
# 3/28/2023	Added check to prevent overwriting new config file.
# 4/14/2023	Always exit with status 0 to prevent RPM from halting in error
#
ORIGINAL_DIR=/localapps/runtime/TsunamiEasGui/etc
DEST_DIR=/awips2/apps/TsunamiEasGui/etc

echo
echo "Tsunami EAS GUI application."
echo "Copying configuration file and templates from ${ORIGINAL_DIR} to ${DEST_DIR}."
echo "You should only run this script once, and only if you previously used the GUI as a local application."
echo

if [ ! -d ${ORIGINAL_DIR} ]
then
	echo "No previous installation of the Tsunami EAS GUI was found; exiting."
	exit 0
fi

if [ -f ${DEST_DIR}/config_tsu.py ]
then
	echo "A configuration file ${DEST_DIR}/config_tsu.py already exists!"

	#
	# If this is an interactive session, ask the user whether to continue.
	#
	if tty -s
	then

		echo -n "    Are you sure you want to overwrite this file with the version in ${ORIGINAL_DIR}? "

		read reply
		`echo "y Y yes Yes YES" |grep -w -q "$reply"`		# Clever way to check for five kinds of 'yes'
		a=$?
	else
		#
		# If running in batch mode (e.g., during RPM installation), do not
		# overwrite the existing configuration file.
		#
		a=1
	fi
	if [ ! $a = 0 ]
	then
		echo "Exiting; no files copied."
		exit 0
	fi
fi

echo
cp ${ORIGINAL_DIR}/config_tsu.py ${ORIGINAL_DIR}/template*.txt ${DEST_DIR}
chown awips:fxalpha ${DEST_DIR}/config_tsu.py ${DEST_DIR}/template*.txt
echo "Tsunami EAS GUI configuration files were copied!"
echo
echo  ${DEST_DIR}
ls -l ${DEST_DIR}
echo

exit 0
