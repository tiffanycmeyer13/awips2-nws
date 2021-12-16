
/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.atcf.fixes.enterfixdata;

/**
 * Class to organize F-deck records by a combo of dtg/fix site/fix type.
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------- ---------- ----------- --------------------------
 * Sep 19, 2019 68738     jwu         Initial creation.
 * Oct 20, 2019 68738     dmanzella   added small functionality
 * </pre>
 *
 * @author jwu
 * @version 1.0
 */
public class FDeckRecordKey implements Comparable<FDeckRecordKey> {

    private String dtg;

    private String fixSite;

    private String fixType;

    // Satellite Type
    private String satType;

    /**
     * Default constructor
     */
    public FDeckRecordKey() {
    }

    /**
     * Constructor
     * 
     * @param aid
     * @param dtg
     * @param tau
     * @param windRad
     */
    public FDeckRecordKey(String dtg, String site, String type,
            String satType) {
        this.dtg = dtg;
        this.fixSite = site;
        this.fixType = type;
        this.satType = satType;
    }

    /**
     * Constructor
     * 
     * @param aid
     * @param dtg
     * @param tau
     */
    public FDeckRecordKey(String dtg, String site, String type) {
        this.dtg = dtg;
        this.fixSite = site;
        this.fixType = type;
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
     * @return the fixSite
     */
    public String getFixSite() {
        return fixSite;
    }

    /**
     * @param fixSite
     *            the fixSite to set
     */
    public void setFixSite(String fixSite) {
        this.fixSite = fixSite;
    }

    /**
     * @return the fixType
     */
    public String getFixType() {
        return fixType;
    }

    /**
     * @param fixType
     *            the fixType to set
     */
    public void setFixType(String fixType) {
        this.fixType = fixType;
    }

    /**
     * @return the satType
     */
    public String getSatType() {
        return satType;
    }

    /**
     * @param satType
     *            the satType to set
     */
    public void setSatType(String satType) {
        this.satType = satType;
    }

    /**
     * @return int
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result;
        result = prime * result + ((dtg == null) ? 0 : dtg.hashCode());
        result = prime * result + ((fixSite == null) ? 0 : fixSite.hashCode());
        result = prime * result + ((fixType == null) ? 0 : fixType.hashCode());
        result = prime * result + ((satType == null) ? 0 : satType.hashCode());
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

        FDeckRecordKey other = (FDeckRecordKey) obj;
        if (dtg == null) {
            if (other.dtg != null) {
                return false;
            }
        } else if (!dtg.equals(other.dtg)) {
            return false;
        }
        if (fixSite == null) {
            if (other.fixSite != null) {
                return false;
            }
        } else if (!fixSite.equals(other.fixSite)) {
            return false;
        }

        if (fixType == null) {
            if (other.fixType != null) {
                return false;
            }
        } else if (!fixType.equals(other.fixType)) {
            return false;
        }

        if (satType == null) {
            if (other.satType != null) {
                return false;
            }
        } else if (!satType.equals(other.satType)) {
            return false;
        }

        return true;
    }

    /**
     * Returns a string representation in format of "dtg|site|type|satType".
     * 
     * @return String
     */
    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        if (dtg != null) {
            result.append(dtg);
        }

        result.append("|");
        if (fixSite != null) {
            result.append(fixSite);
        }

        result.append("|");
        if (fixType != null) {
            result.append(fixType);
        }

        result.append("|");
        if (satType != null) {
            result.append(satType);
        }

        return result.toString();
    }

    /**
     * Comparator - sort in reverse order.
     * 
     * @param recKey
     */
    @Override
    public int compareTo(FDeckRecordKey recKey) {
        return recKey.toString().compareTo(this.toString());
    }

}
