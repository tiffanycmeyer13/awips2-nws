#/bin/bash

#This software was developed and / or modified by NOAA/NWS/OCP/ASDT
#
#  pushToLdm.sh
#
#  Script to push ATCF files to LDAD and then from LDAD to NCEP LDM
#  server queue for processing on WCOSS and/or sending to ftp site
#  Based on similar functionality used by NWPS process
#
#  SOFTWARE HISTORY
#
#  Date         Ticket#    Engineer     Description
#  ------------ ---------- -----------  --------------------------
#  Aug 12, 2019 66071      mporricelli  Initial creation
#  Nov 20, 2019 69937      mporricelli  Reworked for sending files
#                                       to LDM rather than directly
#                                       to WCOSS; based on
#                                       runwps_ncep.sh authored
#                                       by Pablo Santos and
#                                       Joseph Maloney
# May 30, 2020 78922       mporricelli  Added priority argument,
#                                       creation of scripts
# Aug 12, 2020 76541       mporricelli  Get env from external files
# Nov 06, 2020 83637       mporricelli  Update to work with ldm scripts 
#
#-----------------------------------------------------------------
#
###################################################################################
# Function to check env var settings
###################################################################################
function checkEnvVars {
    if [ "${ATCFSTRMS}" == "" ]
    then
        ATCFSTRMS=/awips2/edex/data/atcf/storms
        logit "ATCFSTRMS not found in atcf.properties. Using default: $ATCFSTRMS"
    fi

    if [ "${ATCF_OUTGOING}" == "" ]
    then
        ATCF_OUTGOING=/awips2/edex/data/atcf/storms/outgoing
        logit "ATCF_OUTGOING not found in atcf.properties. Using default: $ATCF_OUTGOING"
    fi

    if [ "${ATCFTMP}" == "" ]
    then
        ATCFTMP=/awips2/edex/data/atcf/storms/tmp
        logit "ATCFTMP not found in atcf.properties. Using default: $ATCFTMP"
    fi

}
###################################################################################
# Function to create the script for WCOSS processing
# when 'Send Compute Data' is invoked from ATCF GUI
###################################################################################
function createSubforecastScript {
    ibmScript="IBM_subforecast_${strmid}.sh"
    dtg=`date +%Y%m%d%H%M`

    # Get script template file, nhc_forecast_wcoss.vir, location from localization

    if [ -f ${SITE_FCST_VIR} ]
    then
        nhcForecastFile=${SITE_FCST_VIR}
    else
        nhcForecastFile=${BASE_FCST_VIR}
    fi

    cat <<EOF > ${inputDir}/$ibmScript
#BSUB -J job_put_${strmid}
#BSUB -o /nhc/save/guidance/storm-data/log/${strmid}/${strmid}.aid.${dtg}
#BSUB -e /nhc/save/guidance/storm-data/log/${strmid}/${strmid}.aid.${dtg}
#BSUB -L /bin/ksh
#BSUB -q 'prod_class1'
#BSUB -n 1
#BSUB -R affinity[core]
#BSUB -R rusage[mem=5000]
#BSUB -cwd /nhc/save/guidance
#BSUB -P NHC-T2O
#BSUB -W 00:10

strmid=${strmid}

EOF
    # Append contents of nhc_forecast_wcoss.vir to IBM_subforecast script
    cat $nhcForecastFile >> ${inputDir}/$ibmScript

} # end of createSubforecastScript


###################################################################################
# Function to create the script for WCOSS processing
# when 'NWP Model Priority' is invoked from ATCF GUI
###################################################################################
function createSubstormScript {
    #create main job submission script for Model Priority submission
    ibmScript="IBM_substorm_${strmid}_${priority}.sh"

    # Get script template file, nhc_storm_wcoss.vir, location from localization

        if [ -f ${SITE_STRM_VIR} ]
    then
        nhcStormFile=${SITE_STRM_VIR}
    else
        nhcStormFile=${BASE_STRM_VIR}
    fi
    # cat the following lines into the IBM output script; maintain the left-justification
    cat <<EOF > ${inputDir}/$ibmScript
#BSUB -J job_storm_${priority}_${strmid}
#BSUB -o /nhc/save/guidance/storm-data/log/${strmid}/${strmid}.storm_${priority}
#BSUB -e /nhc/save/guidance/storm-data/log/${strmid}/${strmid}.storm_${priority}
#BSUB -L /bin/ksh
#BSUB -q 'prod_class1'
#BSUB -n 1
#BSUB -R affinity[core]
#BSUB -R rusage[mem=500]
#BSUB -cwd /nhc/save/guidance
#BSUB -P NHC-T2O
#BSUB -W 00:10

#Set StormID and Storm Priority Files as ENV variables
strmid=${strmid}
ncepfile=/nhc/save/guidance/storm-data/ncep/storm${priority}
ncepdevfile=/nhc/save/guidance/storm-data/ncep/storm.dev${priority}
archivefile=/nhc/save/guidance/storm-data/ncep/archive/storm${priority}
archivedevfile=/nhc/save/guidance/storm-data/ncep/archive/storm.dev${priority}

EOF

    # Append contents of nhc_storm_wcoss.vir to IBM_substorm script
    cat $nhcStormFile >> ${inputDir}/$ibmScript

} # End of createSubstormScript

###################################################################################
# Function to notify that creation of tar file failed
###################################################################################
function tarFailed {
    TARFILE=$1
    logit "Tarring of storm file ${TARFILE} failed. Exiting."
    echo "Tarring of storm file ${TARFILE} failed. Exiting." 1>&2
    if [ -f "${TARFILE}" ]
    then
        logit "Removing bad tar file $TARFILE"
        unlink $TARFILE
        if [ $? != 0 ]
        then
            logit "Failed to remove $TARFILE"
            echo "Failed to remove $TARFILE"  1>&2
        fi
    fi
    exit 1
}

###################################################################################
# Function to push files from LDAD server to LDM from
# which they will be sent to WCOSS and/or FTP server
###################################################################################
function LDMUpload {
    TARFILE="$( basename $1)"
    # Push storm file to LDAD server
    scp $1 ldad@ls1:/tmp/
    if [ $? != 0 ]
    then
        logit "scp of storm file ${TARFILE} to LDAD server failed. Exiting."
        echo "scp of storm file ${TARFILE} to LDAD server failed. Exiting." 1>&2
        exit 1
    fi

    logit ""
    logit "Running ${LDMSEND} to NCEP LDM servers"
    numpasses=0
    numfails=0

# Attempt to send ATCF storm tar file to LDM Server(s) defined in atcf.properties
    for s in ${LDMSERVERS}
    do
        status=$(ssh ldad@ls1 "cd /tmp; ${LDMSEND} -vxnl- -h ${s} -f EXP -o 3600 -r 3 -R 60 -T 25 -p '^ATCF_.*' ${TARFILE}" 2>> ${logfile})
        if [ $? != 0 ]
        then
            echo "Failed to send $TARFILE to LDM queue." 1>&2
            logit "Failed to send $TARFILE to LDM queue."
        fi
        if [ "${status}" == "PASS" ]
        then
            logit ""
            logit "____________________ATCF STORM DATA UPLOAD PASSED__________"
            logit ""
            logit "INFO - ATCF DATA LDM UPLOAD FOR ${TARFILE} PASSED: `date`"
            logit ""
            logit "INFO - UPLOADED ATCF DATA TO ${s} LDM SERVER"
            logit ""
            logit "______________________________________________________"
            (( numpasses++ ))
        else
            logit ""
            logit "____________________ATCF STORM DATA UPLOAD FAILED__________"
            logit ""
            logit "ERROR - ATCF DATA LDM UPLOAD FOR ${TARFILE}  FAILED: `date`"
            logit ""
            logit "ERROR - COULD NOT UPLOAD ATCF DATA TO ${s} LDM SERVER"
            logit ""
        if [ "${BACKUPLDMSERVERS}" == "" ]
        then
            logit "WARNING - WE DO NOT HAVE ANY BACKUP LDM SERVERS SETUP"
        else
            logit "INFO - WILL TRY BACKUP LDM SERVERS ${BACKUPLDMSERVERS}"
        fi
        logit ""
        logit "______________________________________________________"
        (( numfails++ ))
        fi
    done

    if [ "${BACKUPLDMSERVERS}" != "" ] && [ $numfails -gt 0 ]
    then
        logit "INFO - OUR BACKUP LDM SERVERS ARE ${BACKUPLDMSERVERS}"
    for s in ${BACKUPLDMSERVERS}
    do
        status=$(ssh ldad@ls1 "cd /tmp; ${LDMSEND} -vxnl- -h ${s} -f EXP -o 3600 -r 3 -R 60 -T 25 -p '^ATCF_.*' ${TARFILE}" 2>> ${logfile})
        if [ "${status}" == "PASS" ]
        then
            logit ""
            logit "____________________ATCF DATA UPLOAD PASSED__________"
            logit ""
            logit "INFO -  ATCF FILE ${TARFILE} HAS BEEN UPLOADED: `date`"
            logit ""
            logit "INFO - UPLOADED ATCF DATA TO ${s} BACKUP LDM SERVER"
            logit ""
            logit "______________________________________________________"
            (( numpasses++ ))
        else
            logit ""
            logit "____________________ATCF DATA UPLOAD FAILED__________"
            logit ""
            logit "ERROR - ATCF FILE ${TARFILE} HAS FAILED TO BE UPLOADED: `date`"
            logit ""
            logit "ERROR - COULD NOT UPLOAD ATCF DATA TO ${s} BACKUP LDM SERVER"
            logit ""
            (( numfails++ ))
        fi
    done
    fi

    if [ $numfails -gt 0 ]
    then
        logit ""
        logit "WARNING - $numfails LDM UPLOADS FAILED."
        logit "WARNING - HAVE YOUR ITO CHECK YOUR LDM CONFIG AND RUN AN UPLOAD TEST TO ALL LDM SERVERS."
        logit ""
    fi
    if [ $numpasses -eq 0 ]
    then
        logit ""
        logit "ERROR - NONE OF YOUR LDM UPLOADS FOR FILE ${TARFILE} WERE COMPLETED."
        logit "ERROR - CALL YOUR REGIONAL OPERATION CENTER TO REPORT LDM UPLOAD ERRORS."
        logit ""
        echo "LDM upload of ${TARFILE} failed" 1>&2
        exit 1
    else
        logit ""
        logit "NOTICE - YOU HAVE $numpasses LDM UPLOADS FOR ${TARFILE}."
        logit ""
    fi

}

###################################################################################
# Function to send message for incorrect command line usage
###################################################################################
function usage() {
    echo "Usage:   ${0} strmid priority <inputDir>"
    echo "Example: ${0} al112017 3"
    echo "Use 0 for priority if there is no need for a priority ranking"
    echo "Use 99 for sending files after retrieval from wcoss"
    exit 1
}

function getDtg() {
    # Pull current dtg from b-deck
    if [ ! -f ${inputDir}/${bdeckFile} ]
    then
        echo "${inputDir}/${bdeckFile} not found,exiting" 1>&2
        logit "${inputDir}/${bdeckFile} not found,exiting"
        exit 1
    fi
    local fulldtg=`tac ${inputDir}/${bdeckFile} |grep -m1 . |cut -f3 -d ',' |sed 's/ //g'`
    logit $fulldtg
}

###################################################################################
# Function for logging shorthand
###################################################################################
function logit () {
    echo "$@" | tee -a $logfile
}

############################ START MAIN SCRIPT #####################################

if [ $# -ne 2 ] && [ $# -ne 3 ]
then
    echo "ERROR: Invalid number of arguments provided" 1>&2
    usage
fi

ENVFILE=/awips2/edex/data/atcf/config/atcf-env

source $ENVFILE || exit1

logfile="${LOG_DIR}/`date +%Y%m%d`/pushToLdm.log"

logit ""
logit "----- BEGIN LOG ENTRY: `date` ----- "

checkEnvVars

strmid=$1
priority=$2
inputDir=${3:-$ATCFSTRMS}

comFile="${strmid}.com"
adeckFile="a${strmid}.dat"
bdeckFile="b${strmid}.dat"
edeckFile="e${strmid}.dat"
fdeckFile="f${strmid}.dat"
fstFile="${strmid}.fst"
stormsFile="storms.txt"
tcvitalsFile="${strmid}-tcvitals.dat"
tcvitalsArchFile="${strmid}-tcvitals-arch.dat"


fulldtg=$(getDtg)

if [ "${LDMSERVERS}" == "" ]
then
    logit "No LDM Servers defined. Exiting."
    echo "No LDM Servers defined. Exiting." 1>&2
    exit 1
fi

# If .com file does not exist, do not continue
if [ ! -f ${inputDir}/${comFile} ]
then
    echo "${inputDir}/${comFile} not found,exiting" 1>&2
    logit "${inputDir}/${comFile} not found,exiting"
    exit 1
fi
#touch the com file (even if contents have not changed) so WCOSS knows to re-process if computes are re-submitted quickly
touch ${inputDir}/${comFile}


###################################################################################
# Files are to be sent to WCOSS without a priority ranking, as when called from 
# 'Send Compute Data' GUI option
###################################################################################
if [ "${priority}" -eq 0 ]
then
    # Create forecast ${ibmScript}
    createSubforecastScript

    SENDCOMP_TARFILE="${ATCF_OUTGOING}/ATCF_SendCompute_${strmid}-${fulldtg}_$(date +%Y%m%d%H%M)_$$.tar.gz"

     # Create tar of files to be sent to WCOSS
    tar czf ${SENDCOMP_TARFILE} -C ${inputDir} ${comFile} ${ibmScript} ${fstFile} ${bdeckFile} ${edeckFile}\
            ${adeckFile} ${fdeckFile} ${stormsFile} ${tcvitalsFile} ${tcvitalsArchFile} 1>&2
    if [ $? != 0 ]
    then
        tarFailed ${SENDCOMP_TARFILE}
    fi

    # Send wcoss file to LDM queue for transfer to wcoss
    LDMUpload ${SENDCOMP_TARFILE}

    # Signify completion to the calling java code
    echo "Finished" 1>&2

###################################################################################
# Priority is set to non-zero value, as when called from 'NWP Model Priority' GUI
# option, so create appropriate set of files for sending
###################################################################################
elif [ "${priority}" -lt 99 ]
then
    MODEL_PRIORITY_TARFILE="${ATCF_OUTGOING}/ATCF_ModelPriority_${strmid}_${priority}_$(date +%Y%m%d%H%M)_$$.tar.gz"
    # Create substorm ${ibmScript}
    createSubstormScript
    tar czf ${MODEL_PRIORITY_TARFILE} -C ${inputDir} ${comFile} ${ibmScript} 1>&2
    if [ $? != 0 ]
    then
        tarFailed ${MODEL_PRIORITY_TARFILE}
    fi

    # Send file to LDM queue for transfer to wcoss
    LDMUpload ${MODEL_PRIORITY_TARFILE}


    # Signify completion to the calling java code
    echo "Finished" 1>&2

###################################################################################
# Priority 99 to indicate process is called after data retrieval from WCOSS and OPAH
###################################################################################
elif [ "${priority}" -eq 99 ]
then

    TARFILE="${ATCF_OUTGOING}/ATCF_PostRetrieval_${strmid}_${priority}_$(date +%Y%m%d%H%M)_$$.tar"

    shortdtg=`echo $fulldtg| cut -c3-`

    bb=`echo $strmid | cut -c1-2 | tr 'a-z' 'A-Z'`
    cc=`echo $strmid | cut -c3-4`
    yy=`echo $strmid | cut -c7-8`

    ships_prefix=${shortdtg}${bb}${cc}${yy}
    lsdiagFile="${ships_prefix}_lsdiag.dat"
    shipsFile="${ships_prefix}_ships.txt"

     # Create tar of files to be sent to WCOSS/FTP
    tar cf ${TARFILE} -C ${inputDir} ${comFile} ${fstFile} ${adeckFile} ${bdeckFile} ${edeckFile}\
            ${fdeckFile} ${stormsFile} ${tcvitalsFile} ${tcvitalsArchFile} ${lsdiagFile} ${shipsFile} 1>&2
    if [ $? != 0 ]
    then
        tarFailed ${TARFILE}
    fi

    # Add emx file to tarball if this is not CPHC
    emxFile="a${strmid}.emx"

    site=`echo ${AW_SITE_IDENTIFIER} | tr '[:upper:]' '[:lower:]'`

    if [ ${site} != "cphc" ]
    then
        logit "Appending emx file to tar ball"
        tar rf ${TARFILE} -C ${inputDir} ${emxFile} 1>&2
        if [ $? != 0 ]
        then
            tarFailed ${TARFILE}
        fi
    fi

    gzip $TARFILE
        if [ $? != 0 ]
        then
            logit "gzipping of ${TARFILE} failed. Exiting"
            exit 1
        fi

    POST_RETRIEVAL_RESEND_TARFILE="${TARFILE}.gz"

    # Send file to LDM queue for processing
    LDMUpload ${POST_RETRIEVAL_RESEND_TARFILE}

    # Signify completion to the calling code
    echo "Finished" 1>&2
fi

logit ""
logit "----- END LOG ENTRY: `date` ----- "

