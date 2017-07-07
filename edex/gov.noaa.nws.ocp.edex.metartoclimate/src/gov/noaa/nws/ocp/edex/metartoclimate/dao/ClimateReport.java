/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.metartoclimate.dao;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * ClimateReport of Climate Database
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Sep 23, 2016            pwang       Initial creation
 * 07 FEB 2017  28609      amoore      Comment clarification.
 * 21 FEB 2017  28609      amoore      Nullability clarification in getters.
 * 09 MAY 2017  33104      amoore      Remove unneeded PDO extension.
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */
@DynamicSerialize
@XmlAccessorType(XmlAccessType.NONE)
public class ClimateReport {

    @DynamicSerializeElement
    @XmlAttribute
    private String report_type; // not null

    @DynamicSerializeElement
    @XmlAttribute
    private Integer mode;

    @DynamicSerializeElement
    @XmlAttribute
    private String icao_loc_id; // not null

    @DynamicSerializeElement
    @XmlAttribute
    private String wmo_dd; // not null

    @DynamicSerializeElement
    @XmlAttribute
    private String afos_dd;

    @DynamicSerializeElement
    @XmlAttribute
    private Calendar origin;

    @DynamicSerializeElement
    @XmlAttribute
    /**
     * Also called referred to as "ticks" in legacy right before conversion.
     */
    private Calendar date; // not null

    @DynamicSerializeElement
    @XmlAttribute
    private Calendar nominal; // not null

    @DynamicSerializeElement
    @XmlAttribute
    private String report;

    @DynamicSerializeElement
    @XmlAttribute
    private Integer prior;

    @DynamicSerializeElement
    @XmlAttribute
    private Double lat;

    @DynamicSerializeElement
    @XmlAttribute
    private Double lon;

    @DynamicSerializeElement
    @XmlAttribute
    private Double elev;

    @DynamicSerializeElement
    @XmlAttribute
    private String state;

    @DynamicSerializeElement
    @XmlAttribute
    /**
     * Also called "region" in legacy.
     */
    private String country;

    @DynamicSerializeElement
    @XmlAttribute
    private String name;

    @DynamicSerializeElement
    @XmlAttribute
    /**
     * Also called "bulletin sequence number" in legacy.
     */
    private Integer sequence_num;

    @DynamicSerializeElement
    @XmlAttribute
    /**
     * Also called "DCD status" in legacy.
     */
    private Integer status;

    public ClimateReport() {

    }

    /**
     * @return the report_type
     */
    public String getReport_type() {
        return report_type;
    }

    /**
     * @param report_type
     *            the report_type to set
     */
    public void setReport_type(String report_type) {
        this.report_type = report_type;
    }

    /**
     * Nullable
     * 
     * @return the mode
     */
    public Integer getMode() {
        return mode;
    }

    /**
     * @param mode
     *            the mode to set
     */
    public void setMode(Integer mode) {
        this.mode = mode;
    }

    /**
     * @return the icao_loc_id
     */
    public String getIcao_loc_id() {
        return icao_loc_id;
    }

    /**
     * @param icao_loc_id
     *            the icao_loc_id to set
     */
    public void setIcao_loc_id(String icao_loc_id) {
        this.icao_loc_id = icao_loc_id;
    }

    /**
     * @return the wmo_dd
     */
    public String getWmo_dd() {
        return wmo_dd;
    }

    /**
     * @param wmo_dd
     *            the wmo_dd to set
     */
    public void setWmo_dd(String wmo_dd) {
        this.wmo_dd = wmo_dd;
    }

    /**
     * @return the afos_dd
     */
    public String getAfos_dd() {
        return afos_dd;
    }

    /**
     * Nullable.
     * 
     * @param afos_dd
     *            the afos_dd to set
     */
    public void setAfos_dd(String afos_dd) {
        this.afos_dd = afos_dd;
    }

    /**
     * @return the origin
     */
    public Calendar getOrigin() {
        return origin;
    }

    /**
     * Nullable.
     * 
     * @param origin
     *            the origin to set
     */
    public void setOrigin(Calendar origin) {
        this.origin = origin;
    }

    /**
     * @return the date
     */
    public Calendar getDate() {
        return date;
    }

    /**
     * @param date
     *            the date to set
     */
    public void setDate(Calendar date) {
        this.date = date;
    }

    /**
     * @return the nominal
     */
    public Calendar getNominal() {
        return nominal;
    }

    /**
     * @param nominal
     *            the nominal to set
     */
    public void setNominal(Calendar nominal) {
        this.nominal = nominal;
    }

    /**
     * Nullable.
     * 
     * @return the report
     */
    public String getReport() {
        return report;
    }

    /**
     * @param report
     *            the report to set
     */
    public void setReport(String report) {
        if (report != null) {
            if (report.length() > 255) {
                report = report.substring(0, 255);
            }
        }
        this.report = report;
    }

    /**
     * Nullable.
     * 
     * @return the prior
     */
    public Integer getPrior() {
        return prior;
    }

    /**
     * 
     * @param prior
     *            the prior to set
     */
    public void setPrior(Integer prior) {
        this.prior = prior;
    }

    /**
     * Nullable.
     * 
     * @return the lat
     */
    public Double getLat() {
        return lat;
    }

    /**
     * @param lat
     *            the lat to set
     */
    public void setLat(Double lat) {
        this.lat = lat;
    }

    /**
     * Nullable.
     * 
     * @return the lon
     */
    public Double getLon() {
        return lon;
    }

    /**
     * @param lon
     *            the lon to set
     */
    public void setLon(Double lon) {
        this.lon = lon;
    }

    /**
     * Nullable.
     * 
     * @return the elev
     */
    public Double getElev() {
        return elev;
    }

    /**
     * @param elev
     *            the elev to set
     */
    public void setElev(Double elev) {
        this.elev = elev;
    }

    /**
     * Nullable.
     * 
     * @return the state
     */
    public String getState() {
        return state;
    }

    /**
     * @param state
     *            the state to set
     */
    public void setState(String state) {
        this.state = state;
    }

    /**
     * Nullable.
     * 
     * @return the country (region)
     */
    public String getCountry() {
        return country;
    }

    /**
     * @param country
     *            the country (region) to set
     */
    public void setCountry(String country) {
        this.country = country;
    }

    /**
     * Nullable.
     * 
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name
     *            the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Nullable.
     * 
     * @return the sequence_num
     */
    public Integer getSequence_num() {
        return sequence_num;
    }

    /**
     * @param sequence_num
     *            the sequence_num to set
     */
    public void setSequence_num(Integer sequence_num) {
        this.sequence_num = sequence_num;
    }

    /**
     * Nullable.
     * 
     * @return the status
     */
    public Integer getStatus() {
        return status;
    }

    /**
     * @param status
     *            the status to set
     */
    public void setStatus(Integer status) {
        this.status = status;
    }

    /**
     * @return a mapping of report column names to their values
     */
    public Map<String, Object> getColumnValues() {
        Map<String, Object> rval = new HashMap<>();

        if (report_type != null) {
            rval.put("report_type", report_type);
        }
        if (mode != null) {
            rval.put("mode", mode);
        } else {
            rval.put("mode", 0);
        }
        if (icao_loc_id != null) {
            rval.put("icao_loc_id", icao_loc_id);
        }
        if (wmo_dd != null) {
            rval.put("wmo_dd", wmo_dd);
        }
        if (afos_dd != null) {
            rval.put("afos_dd", afos_dd);
        } else {
            rval.put("afos_dd", "");
        }
        if (origin != null) {
            rval.put("origin", origin);
        }
        if (date != null) {
            rval.put("date", date);
        }
        if (nominal != null) {
            rval.put("nominal", nominal);
        }
        if (report != null) {
            rval.put("report", report);
        }
        if (prior != null) {
            rval.put("prior", prior);
        } else {
            rval.put("prior", 0);
        }
        if (lat != null) {
            rval.put("lat", lat);
        }
        if (lon != null) {
            rval.put("lon", lon);
        }
        if (elev != null) {
            rval.put("elev", elev);
        } else {
            rval.put("elev", 0.0);
        }
        if (state != null) {
            rval.put("state", state);
        } else {
            rval.put("state", "");
        }
        if (country != null) {
            rval.put("country", country);
        } else {
            rval.put("country", "");
        }
        if (name != null) {
            rval.put("name", name);
        } else {
            rval.put("name", "");
        }
        if (sequence_num != null) {
            rval.put("sequence_num", sequence_num);
        } else {
            rval.put("sequence_num", 0);
        }
        if (status != null) {
            rval.put("status", status);
        } else {
            rval.put("status", 0);
        }
        return rval;
    }

}
