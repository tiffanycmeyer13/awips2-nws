/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.atcf;

/**
 * Class to identify an A-deck record within a given storm.
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 21, 2018 53949      jwu         Initial creation.
 * Jul 15, 2019 65307      jwu         Add getter/setters.
 *
 * </pre>
 *
 * @author jwu
 * @version 1.0
 */
public class RecordKey {

    private String aid;

    private String dtg;

    private int tau;

    private int windRad;

    /**
     * Default constructor
     */
    public RecordKey() {
    }

    /**
     * Constructor
     * 
     * @param aid
     * @param dtg
     * @param tau
     * @param windRad
     */
    public RecordKey(String aid, String dtg, int tau, int windRad) {
        this.aid = aid;
        this.dtg = dtg;
        this.tau = tau;
        this.windRad = windRad;
    }

    /**
     * @return int
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result;
        result = prime * result + ((aid == null) ? 0 : aid.hashCode());
        result = prime * result + ((dtg == null) ? 0 : dtg.hashCode());
        result = prime * result + tau;
        result = prime * result + windRad;
        return result;
    }

    /**
     * @param obj
     * @return true/false
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null) {
            return false;
        }

        if (getClass() != obj.getClass()) {
            return false;
        }

        RecordKey other = (RecordKey) obj;
        if (aid == null) {
            if (other.aid != null) {
                return false;
            }
        } else if (!aid.equals(other.aid)) {
            return false;
        }

        if (dtg == null) {
            if (other.dtg != null) {
                return false;
            }
        } else if (!dtg.equals(other.dtg)) {
            return false;
        }

        if (tau != other.tau) {
            return false;
        }

        if (windRad != other.windRad) {
            return false;
        }

        return true;
    }

    /**
     * Constructs a string representation of class.
     * 
     * @return String
     */
    @Override
    public String toString() {
        return String.format("<%s|%s|%s|%s>", dtg, aid, tau, windRad);
    }

    /**
     * @return the aid
     */
    public String getAid() {
        return aid;
    }

    /**
     * @param aid
     *            the aid to set
     */
    public void setAid(String aid) {
        this.aid = aid;
    }

    /**
     * @return the dtg
     */
    public String getDtg() {
        return dtg;
    }

    /**
     * @param dtg
     *            the dtg to set
     */
    public void setDtg(String dtg) {
        this.dtg = dtg;
    }

    /**
     * @return the tau
     */
    public int getTau() {
        return tau;
    }

    /**
     * @param tau
     *            the tau to set
     */
    public void setTau(int tau) {
        this.tau = tau;
    }

    /**
     * @return the windRad
     */
    public int getWindRad() {
        return windRad;
    }

    /**
     * @param windRad
     *            the windRad to set
     */
    public void setWindRad(int windRad) {
        this.windRad = windRad;
    }

}