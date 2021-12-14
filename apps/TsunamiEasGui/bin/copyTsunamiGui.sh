#!/bin/sh
#
# To be run during initial installation of the Tsunami EAS GUI as a part
# of the AWIPS baseline.
# Copy the GUI's configuration files (if they exist) from the local app
# directory to the AWIPS baseline 'apps' directory.
#
# Original version by Jim Buchman, 10/4/2021.
#
ORIGINAL_DIR=/localapps/runtime/TsunamiEasGui/etc
DEST_DIR=/awips2/apps/TsunamiEasGui/etc

if [ -d ${ORIGINAL_DIR} ]
then
	cp ${ORIGINAL_DIR}/config_tsu.py ${ORIGINAL_DIR}/template*.txt ${DEST_DIR}
	chown awips:fxalpha ${DEST_DIR}/config_tsu.py ${DEST_DIR}/template*.txt
	echo "Tsunami EAS GUI configuration files were copied from ${ORIGINAL_DIR} TO ${DEST_DIR}"
else
	echo "No previous installation of the Tsunami EAS GUI was found."
fi

exit 0
