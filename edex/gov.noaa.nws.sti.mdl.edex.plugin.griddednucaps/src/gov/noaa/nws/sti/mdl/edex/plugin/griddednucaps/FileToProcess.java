package gov.noaa.nws.sti.mdl.edex.plugin.griddednucaps;

import java.util.Date;

/**
 * A Comparable data class that holds the information about a single NUCAPS
 * file. This class is used as a container to hold onto the valid information
 * and for sorting.
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
public class FileToProcess implements Comparable {

    /** The file path. */
    String filePath;

    /** The minimum time in the file. */
    Date minTime;

    /** The maximum time in the file. */
    Date maxTime;

    /** The time the file was first ingested. */
    Date ingestTime;

    /** The satellite id for the file. */
    String satellite;

    /**
     * Instantiates a new file to process.
     *
     * @param filePath
     *            the file path
     * @param minTime
     *            the min time
     * @param maxTime
     *            the max time
     * @param ingestTime
     *            the ingest time
     * @param satellite
     *            the satellite
     */
    public FileToProcess(String filePath, Date minTime, Date maxTime,
            Date ingestTime, String satellite) {
        super();
        this.filePath = filePath;
        this.minTime = minTime;
        this.maxTime = maxTime;
        this.ingestTime = ingestTime;
        this.satellite = satellite;
    }

    /**
     * Gets the file path.
     *
     * @return the file path
     */
    public String getFilePath() {
        return filePath;
    }

    /**
     * Gets the ingest time.
     *
     * @return the ingest time
     */
    public Date getIngestTime() {
        return ingestTime;
    }

    /**
     * Gets the satellite.
     *
     * @return the satellite
     */
    public String getSatellite() {
        return satellite;
    }

    /**
     * Gets the min time.
     *
     * @return the min time
     */
    public Date getMinTime() {
        return minTime;
    }

    /**
     * Gets the max time.
     *
     * @return the max time
     */
    public Date getMaxTime() {
        return maxTime;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "FileToProcess [filePath=" + filePath + ", minTime=" + minTime
                + ", maxTime=" + maxTime + ", ingestTime=" + ingestTime
                + ", satellite=" + satellite + "]";
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(Object o) {
        if (o instanceof FileToProcess) {
            return (int) (this.minTime.getTime()
                    - ((FileToProcess) o).getMinTime().getTime());
        }
        return 0;
    }

}
