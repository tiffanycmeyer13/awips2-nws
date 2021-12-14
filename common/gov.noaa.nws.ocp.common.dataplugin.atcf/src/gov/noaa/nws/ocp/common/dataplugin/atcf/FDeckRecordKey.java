/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.common.dataplugin.atcf;

/**
 * Class to identify an F-deck record within a given storm.
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 24, 2020 81449      jwu         Initial creation.
 *
 * </pre>
 *
 * @author jwu
 * @version 1.0
 */
public class FDeckRecordKey {

    private String type;

    private String format;

    private String dtg;

    private String site;

    private int windRad;

    /**
     * Default constructor
     */
    public FDeckRecordKey() {
    }

    /**
     * Constructor
     *
     * @param type
     * @param format
     * @param dtg
     * @param site
     * @param windRad
     */
    public FDeckRecordKey(String type, String format, String dtg, String site,
            int windRad) {
        this.type = type;
        this.format = format;
        this.dtg = dtg;
        this.site = site;
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
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        result = prime * result + ((format == null) ? 0 : format.hashCode());
        result = prime * result + ((dtg == null) ? 0 : dtg.hashCode());
        result = prime * result + ((site == null) ? 0 : site.hashCode());
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
        if (obj == null || this.getClass() != obj.getClass()) {
            return false;
        }

        FDeckRecordKey other = (FDeckRecordKey) obj;
        if (!isNullableStringEqual(this.type, other.type)) {
            return false;
        }

        if (!isNullableStringEqual(this.format, other.format)) {
            return false;
        }

        if (!isNullableStringEqual(this.dtg, other.dtg)) {
            return false;
        }
        if (!isNullableStringEqual(this.site, other.site)) {
            return false;
        }

        return windRad == other.windRad;

    }

    /**
     *
     * @param a
     * @param b
     * @return
     */
    private boolean isNullableStringEqual(String a, String b) {
        if (a == null) {
            if (b != null) {
                return false;
            }
        }
        else if (b == null) {
            return false;
        }
        else {
            if (!a.equals(b)) {
                return false;
            }
        }
        return true;
    }

    /**
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * @param type
     *            the type to set
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * @return the format
     */
    public String getFormat() {
        return format;
    }

    /**
     * @param format
     *            the format to set
     */
    public void setFormat(String format) {
        this.format = format;
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
     * @return the site
     */
    public String getSite() {
        return site;
    }

    /**
     * @param site
     *            the site to set
     */
    public void setSite(String site) {
        this.site = site;
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