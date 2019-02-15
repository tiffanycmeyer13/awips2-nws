package gov.noaa.nws.sti.mdl.edex.plugin.griddednucaps;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

/**
 * A Container to aggregate the files that should be processed together.
 * 
 *
 * <pre>
*
* SOFTWARE HISTORY
*
* Date          Ticket#  Engineer  Description
* ------------- -------- --------- -----------------
 * Oct 20, 2018   DCS-18691 jburks  Initial creation
 *
 * </pre>
 *
 * @author jburks
 */

public class FileHolder {

    /** The list of file aggregated for a particular satellite and time. */
    ArrayList<FileToProcess> list = new ArrayList<FileToProcess>();

    /**
     * The date the last file was added. This is used to keep track of
     * FileHolders that have not gotten files in specific amount of time.
     */
    Date lastAddedFile;

    /** The satellite id for the data. */
    String satelliteId;

    /** The minimum date for the files in this list so far. */
    Date dateMin;

    /** The maximum date for the files in the list os far. */
    Date dateMax;

    /** The lower bound of the data in the Holder. */
    long minBounds;

    /** The upper bound of the data in the holder. */
    long maxBounds;

    /**
     * The delta time from the start of the time period to the end for data
     * captured in this holder.
     */
    long deltaTime = 160000;

    /**
     * Instantiates a new file holder with the first file. That file is not
     * always the first file. It is very possible that the first file could be
     * anyplace in the holder.
     *
     * @param fileToProcess
     *            the file to process
     * @param deltaTime
     *            the delta time
     */
    public FileHolder(FileToProcess fileToProcess, long deltaTime) {
        this.deltaTime = deltaTime;
        satelliteId = fileToProcess.getSatellite();
        add(fileToProcess);
        calculateTimeBounds();
    }

    /**
     * Calculate time bounds.
     */
    private void calculateTimeBounds() {
        minBounds = ((int) (dateMin.getTime() / (deltaTime))) * deltaTime;
        maxBounds = minBounds + deltaTime;

    }

    /**
     * Adds the file to the list. NUCAPS seems to have some duplicates. I need
     * to prevent those being ingested.
     *
     * @param file
     *            the file
     */
    public void add(FileToProcess file) {
        boolean goodToAdd = true;
        for (FileToProcess fileToCompare : list) {
            if (fileToCompare.compareTo(file) == 0) {
                goodToAdd = false;
                break;
            }
        }
        if (goodToAdd) {
            list.add(file);
            lastAddedFile = new Date();
            adjustDates(file);
        }
    }

    /**
     * Adjust dates.
     *
     * @param file
     *            the file
     */
    private void adjustDates(FileToProcess file) {
        if (dateMin == null || file.getMinTime().before(dateMin)) {
            dateMin = file.getMinTime();
        }
        if (dateMax == null || file.getMaxTime().after(dateMax)) {
            dateMax = file.getMaxTime();
        }

    }

    /**
     * Gets the size of the current list.
     *
     * @return the size
     */
    public int getSize() {
        return list.size();
    }

    /**
     * Gets the sets of files to process.
     *
     * @return the sets the to process
     */
    public FileToProcess[] getFileToProcess() {
        // Sort the list
        Collections.sort(list);
        return list.toArray(new FileToProcess[0]);
    }

    /**
     * Check file to process to see if it belongs in this fileHolder.
     *
     * @param fileToProcess
     *            the file to process
     * @return true, if successful
     */
    public boolean checkFileToProcess(FileToProcess fileToProcess) {
        if (fileToProcess.getSatellite().equals(satelliteId)
                && this.checkDates(fileToProcess)) {
            return true;
        }
        return false;
    }

    /**
     * Check dates to see if they are within the minBounds and maxBounds.
     *
     * @param fileToProcess
     *            the file to process
     * @return true, if successful
     */
    private boolean checkDates(FileToProcess fileToProcess) {
        if (fileToProcess.getMinTime().getTime() >= minBounds
                && fileToProcess.getMinTime().getTime() < maxBounds) {
            return true;
        }
        return false;
    }

    /**
     * Gets the date the last file was added. This is not the date of the last
     * added file. But the time the file was added.
     *
     * @return the last added file
     */
    public Date getLastAddedFile() {
        return lastAddedFile;
    }
}
