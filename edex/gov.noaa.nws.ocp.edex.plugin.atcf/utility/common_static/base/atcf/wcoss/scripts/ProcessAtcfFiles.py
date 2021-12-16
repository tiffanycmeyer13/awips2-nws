# This software was developed and / or modified by NOAA/NWS/OCP/ASDT
#
#  ProcessAtcfFiles.py
#
#  After files are retrieved from WCOSS and/or OPAH, they need
#  to be processed. This involves modifying some of the returned
#  deck files and merging them into the DB.
#  Some files are also subsequently sent back out to LDM for transfer
#  to WCOSS and the ftp server
#
#
#  SOFTWARE HISTORY
#
#  Date          Ticket#    Engineer     Description
#  ------------  ---------- -----------  --------------------------
#  Sep 9, 2020   80672      mporricelli  Initial creation, derived
#                                        from legacy ATCF
#                                        nhc_getaids.sh
#  Sep 29, 2020  80673      mporricelli  Add call to interpolator
#
#
#------------------------------------------------------------------
#
import tarfile
import logging
import re
import glob
import os
import shutil

from datetime import datetime, timedelta
from time import strptime
from calendar import timegm
from java.util import Date

from gov.noaa.nws.ocp.edex.plugin.atcf.dao import AtcfProcessDao
from gov.noaa.nws.ocp.edex.plugin.atcf.util import AtcfFileUtil
from gov.noaa.nws.ocp.edex.plugin.atcf.decoder import AtcfDeckProcessor
from gov.noaa.nws.ocp.common.dataplugin.atcf import AtcfDeckType
from gov.noaa.nws.ocp.common.dataplugin.atcf.interpolation import InterpolateADeckRequest, \
    InterpolationArgs

from com.raytheon.uf.common.localization import PathManagerFactory
from com.raytheon.uf.common.localization import LocalizationContext
from com.raytheon.uf.common.serialization.comm import RequestRouter


LocalizationType = LocalizationContext.LocalizationType
LocalizationLevel = LocalizationContext.LocalizationLevel

pm = PathManagerFactory.getPathManager()
lc = [pm.getContext(LocalizationType.COMMON_STATIC,
                    LocalizationLevel.SITE),
      pm.getContext(LocalizationType.COMMON_STATIC,
                    LocalizationLevel.BASE)]

# Set up logging


LOG_DIR = "/awips2/edex/data/atcf/logs"

curr_date = datetime.utcnow().strftime('%Y%m%d')
log_file = os.path.join(LOG_DIR, curr_date, "processATCF.log")

logging.basicConfig(
    filename=log_file, level=logging.INFO, format='%(message)s')
logger = logging.getLogger()


# Process a single tar ball of retrieved data for this storm and dtg
# The tar ball is found in a temporary subdirectory of ATCFSTRMS
#
# @param input_tar: tar ball of retrieved data files
# @param tempDir: temporary directory that holds files from this tar ball
# @param storm: current storm
# @param dtg: current date-time-group


def processATCF(input_tar, tempDir, storm, dtg, envCfg):

    ATCFSTRMS = envCfg.getAtcfstrms()
    ATCFAIDMESSAGES = envCfg.getAidMessagesDir()

    strmid = storm.getStormId().lower()

    # Create various substrings of date-time for use in file names and
    # processing

    shortdtg = dtg[2:10]

    dt = datetime.strptime(dtg, "%Y%m%d%H")

    full_dtg = dt.strftime("%Y%m%d%H")

    year4 = storm.getYear()
    year2 = str(year4)[2:4]

    # Calculate the dtg of the previous 6-h and 12-h cycles (dtgm1, dtgm2)

    m1 = dt - timedelta(hours=6)
    m2 = dt - timedelta(hours=12)

    full_dtgm1 = m1.strftime("%Y%m%d%H")
    full_dtgm2 = m2.strftime("%Y%m%d%H")

    stnum = storm.getCycloneNum()
    basin = storm.getRegion()

    # Create ships prefix for ships file naming
    ships_prefix = shortdtg + basin + str(stnum) + year2

    # Create path names and deck prefix for consolidating file names
    rootDir = tempDir + '/'
    aDeckPrefix = "a" + strmid
    fullPath = rootDir + aDeckPrefix

    wcoss_files = {"a_temp": fullPath + ".temp",
                   "a_supp": fullPath + ".supp",
                   "a_ncep": fullPath + ".ncep",
                   "a_hwrf": fullPath + ".hwrf",
                   "a_hwrfm2": fullPath + ".hwrfm2",
                   "a_hmon": fullPath + ".hmon",
                   "a_hmonm2": fullPath + ".hmonm2",
                   "lsdiag": rootDir + ships_prefix + "_lsdiag.dat",
                   "ships": rootDir + ships_prefix + "ships.txt",
                   "ec_ships": rootDir + "EC_" + ships_prefix + "_ships.txt",
                   "tcvitals": rootDir + strmid + "-tcvitals.dat",
                   "tcv_arch": rootDir + strmid + "-tcvitals-arch.dat"}

    opah_files = {"a_ukmogens": fullPath + ".ukmogens",
                  "a_fsu": fullPath + ".fsu",
                  "a_jma_m1": fullPath + "_" + full_dtgm1 + ".jma.dat",
                  "a_jma": fullPath + "_" + full_dtg + ".jma.dat",
                  "a_ctcx": rootDir + strmid + "_" + full_dtg + ".ctcx.dat",
                  "a_ctcx_m1": rootDir + strmid + "_" + full_dtgm1 + ".ctcx.dat",
                  "a_ctcx_m2": rootDir + strmid + "_" + full_dtgm2 + ".ctcx.dat",
                  "a_fnmoc": fullPath + ".fnmoc"}

    # extract the files from the received tar ball
    extractFiles(input_tar, tempDir)

    #---------- NCEP files -----------------

    # Pattern of lines to remove from .ncep file
    rmNcepStrings = ['[NCF][PC][0-9][0-9],']

    # Pattern of lines to remove from hwrf file
    rmHwrfStrings = [', *HWRF, *[1-9]*[13579],']

    ncep_files = [wcoss_files['a_ncep'], wcoss_files['a_hwrf'], wcoss_files[
        'a_hwrfm2'], wcoss_files['a_hmon'], wcoss_files['a_hmonm2']]

    # remove unwanted trackers from .ncep file
    deleteLinesFromFile(wcoss_files['a_ncep'], rmNcepStrings)
    # strip out extra HWRF taus from .hwrf file
    deleteLinesFromFile(wcoss_files['a_hwrf'], rmHwrfStrings)
    try:
        # combine all hwrf/hmon entries together, and add them to end of .ncep
        # file
        concatFiles(ncep_files, wcoss_files['a_ncep'])
    except IOException as e:
        raise RuntimeException(" Failed to create final .ncep file: " + \
            wcoss_files['a_ncep'] + ". Exiting. ") from e

    #-- CIRA Stuff on opah --#
    # create adeck file of only EMX for CIRA to use on opah

    emx_file = rootDir + "a" + strmid + ".emx"
    retainstrings = ['EMX']

    try:
        extractLinesFromFile(wcoss_files['a_ncep'], emx_file, retainstrings)
    except IOException:
        logger.error(strmid + ".emx file was not created")

    # pull current basin and cyclone number lines from ukmogens file into
    # mogreps file

    mogreps_file = rootDir + "a" + strmid + ".mogreps"
    basinPlusCycNum = basin + ', ' + str(stnum)

    retainstrings = [basinPlusCycNum]

    try:
        extractLinesFromFile(
            opah_files['a_ukmogens'], mogreps_file, retainstrings)
        mergeMogreps = True
    except IOException:
        mergeMogreps = False

    # merge the above-processed files into DB
    mergeFileToDB(wcoss_files['a_temp'], storm)

    mergeFileToDB(wcoss_files['a_supp'], storm)

    mergeFileToDB(wcoss_files['a_ncep'], storm)

    if mergeMogreps:
        mergeFileToDB(mogreps_file, storm)

    mergeFileToDB(opah_files['a_fsu'], storm)

    mergeFileToDB(opah_files['a_jma'], storm)

    mergeFileToDB(opah_files['a_jma_m1'], storm)

    mergeFileToDB(opah_files['a_ctcx'], storm)

    mergeFileToDB(opah_files['a_ctcx_m1'], storm)

    mergeFileToDB(opah_files['a_ctcx_m2'], storm)

    # create ATCFSTRMS destinations name for file storage
    strmDest = ATCFSTRMS

    # move the EMX, lsdiag, ships and TCvitals files to ATCFSTRMS directory
    moveFile(emx_file, strmDest)
    moveFile(wcoss_files['lsdiag'], strmDest)
    moveFile(wcoss_files['ships'], strmDest)

    moveFile(wcoss_files['tcvitals'], strmDest)
    moveFile(wcoss_files['tcv_arch'], strmDest)

    # create ATCFAIDMESSAGES destination name for file storage
    aidsDest = ATCFAIDMESSAGES + "/" + str(year4)

    if not os.path.isdir(aidsDest):
        os.mkdir(aidsDest)

    # merge the US058* files into DB and move to ATCFAIDMESSAGES
    for fnmoc_file in glob.glob(rootDir + 'US058MCUS*'):
        mergeFileToDB(fnmoc_file, storm)
        if os.path.isdir(aidsDest):
            moveFile(fnmoc_file, aidsDest)

    # move additional files to ATCFAIDMESSAGES directory
    if os.path.isdir(aidsDest):
        moveFile(mogreps_file, aidsDest)
        moveFile(opah_files['a_jma'], aidsDest)
        moveFile(opah_files["a_jma_m1"], aidsDest)
        moveFile(opah_files['a_ctcx'], aidsDest)
        moveFile(opah_files["a_ctcx_m1"], aidsDest)
        moveFile(opah_files["a_ctcx_m2"], aidsDest)

        moveFile(wcoss_files['a_temp'], aidsDest +
                 '/a' + strmid + '.nhc.' + str(full_dtg))
        moveFile(wcoss_files['a_supp'], aidsDest +
                 '/a' + strmid + ".nhc-supp." + str(full_dtg))

        moveFile(wcoss_files['a_ncep'], aidsDest)
    else:
        logger.error(
            "ATCFAIDMESSAGES directory " + aidsDest + " does not exist. Files not saved.")

    # interpolate data for this run
    interpolateADeck(storm, dtg)

    # Create deck files for sending back out to wcoss/ftp
    createDeckFiles(storm, tempDir)

    # Send files to LDM for delivery to WCOSS, FTP, OPAH (pushToLdm.sh)
    sendToLdm(strmid, tempDir)

    # remove the temp directory where this tar ball was processed, including
    # any files that were not moved elsewhere for storage
    shutil.rmtree(tempDir)


# Extract all files from the tar ball into its directory
#
# @param input_tar: tar ball of retrieved data files
# @param tempDir: temporary directory that holds files from this tar ball
# @param storm: this storm ID

def extractFiles(input_tar, tempDir):

    with tarfile.open(input_tar) as tarball:
        logger.debug('tarfile members = %s', tarball.getmembers())
        try:
            tarball.extractall(tempDir)
        except tarfile.ReadError as e:
            msg = "Failed to extract files from " + input_tar
            logger.error(msg)
            raise RuntimeError(msg) from e

# Merge the given a-deck file into the DB using the AtcfDeckProcessor.mergeDeck
# @param deckFile: the a-deck file
# @param storm: this storm


def mergeFileToDB(deckFile, storm):

    strmid = storm.getStormId()
    deckMerger = AtcfDeckProcessor()
    if os.path.isfile(deckFile):
        deckmergelogId = deckMerger.mergeDeck(AtcfDeckType.A, deckFile, storm)
        if deckmergelogId < 0:
            logger.error("ERROR: " + strmid +
                         " deck file " + deckFile + " NOT merged into DB")
        else:
            logger.info(deckFile + " was merged into DB")
    else:
        logger.error("ERROR: " + strmid + " deck file " + deckFile
                     + " NOT found, cannot be merged.")

# Rewrite a file with the given strings deleted
# @param fileName: the file to change
# @param strings: the strings to delete


def deleteLinesFromFile(fileName, strings):
    keepLines = []
    try:
        with open(fileName) as ifp:
            for item in strings:
                for line in ifp:
                    if re.search(item, line):
                        continue

                    keepLines.append(line)

        with open(fileName, 'w') as ofp:
            ofp.writelines(keepLines)
    except IOError as e:
        msg = "Problem deleting lines from " + fileName + ". " + str(e)
        logger.exception(msg)
        raise IOError(msg) from e

# Create a file consisting of lines containing of given strings
# @param inputFile: the file from which to extract the lines
# @param outputFile: file to create consisting of the lines containing the strings
# @param strings: the strings for which lines should be extracted


def extractLinesFromFile(inputFile, outputFile, strings):
    keepLines = []
    try:
        with open(inputFile) as ifp:
            for line in ifp:
                if any(item in line for item in strings):
                    keepLines.append(line)
                    continue

        with open(outputFile, 'w') as ofp:
            ofp.writelines(keepLines)
    except IOError as e:
        msg = "Problem extracting lines from " + \
            inputFile + " to create " + outputFile + ". " + str(e)
        logger.exception(msg)
        raise IOError(msg) from e

# Concatenate files
# @param filenames: array of files to concat
# @param outfile: the file containing the concatenation


def concatFiles(filenames, outfile):
    outlines = []
    for fn in filenames:
        with open(fn) as infile:
            for line in infile:
                outlines.append(line)

    with open(outfile, 'w') as outfile:
        outfile.writelines(outlines)

# Create deck files from DB
# @param storm: this storm
# @param outputDir: target directory for the files


def createDeckFiles(storm, outputDir):
    queryConditions = { "basin": storm.getRegion(), "year": storm.getYear(), "cycloneNum": storm.getCycloneNum() }
    fileUtil = AtcfFileUtil()
    strmid = storm.getStormId().lower()

    for deckType in AtcfDeckType.values():
        deckFile = outputDir + "/" + str(deckType).lower() + strmid + ".dat"
        if deckType == AtcfDeckType.T:
            deckFile = outputDir + "/" + strmid + ".fst"

        dao = AtcfProcessDao()
        deckRecs = dao.getDeckList(deckType,
                        queryConditions, -1)

        fileUtil.createDeckFile(deckRecs, deckType,
                            deckFile)

    # Create storms.txt for sending to ftp site
    stormsFile = outputDir + "/storms.txt"
    queryConditions = { "region": storm.getRegion() }
    stormsRecs = dao.getStormList(queryConditions)
    fileUtil.createStormListFile(stormsRecs, stormsFile)

# Call the pushToLdm.sh script to send various files to LDM. Use priority 99
# for this case
# @param strmid: this storm's ID
# @param fileDir: dir containing the files to process


def sendToLdm(strmid, fileDir):

    script_path = "atcf/wcoss/scripts/pushToLdm.sh"

    for ctx in lc:
        ldmUploadScript = pm.getStaticFile(ctx.getLocalizationType(),
                                           script_path)

    status = os.system(str(ldmUploadScript) + ' ' + strmid + ' ' + str(99) + ' ' + fileDir)
    if status != 0:
        logger.error("Failed to upload " + strmid + " to LDM Server")

# Move file to another directory, overwriting file if exists
# @param sourceFile: file to move
# @param dest: the destination directory or full path with file name


def moveFile(sourceFile, dest):
    if os.path.isdir(dest):
        basename = os.path.basename(sourceFile)
        destFullName = os.path.join(dest, basename)
    else:
        destFullName = dest
    if os.path.isfile(sourceFile) and os.path.isdir(dest):
        try:
            shutil.move(sourceFile, destFullName)
            logger.info(
                sourceFile + " was moved successfully to " + destFullName)

        except IOError as e:
            logger.exception(
                "Failure moving " + sourceFile + " to " + dest + ". " + str(e))

# Run interpolator on A-deck for current storm and dtg
# @param storm
# @param dtg


def interpolateADeck(storm, dtg):

    req = InterpolateADeckRequest()
    interpArgs = InterpolationArgs()

    convDtg = Date(int(timegm(strptime(dtg, '%Y%m%d%H'))) * 1000)

    interpArgs.setDtg(convDtg)

    req.setArgs(interpArgs)
    req.setStorm(storm)

    try:
        RequestRouter.route(req)
    except Exception as e:
        msg = "Exception while running interpolator for dtg " + \
            dtg + ". " + str(e)
        logger.exception(msg)
        raise RuntimeException(msg) from e
