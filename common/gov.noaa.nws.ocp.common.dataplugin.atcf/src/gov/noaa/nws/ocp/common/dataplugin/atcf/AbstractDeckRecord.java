/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.atcf;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;
import com.raytheon.uf.common.util.Pair;

/**
 * Defines fields common to all deck records.
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 23, 2018 53502      dfriedman   Initial creation
 * Sep 12, 2019 68237      dfriedman   Add DataURI annotations.
 * Apr 24, 2020 77847      jwu         Add copy constructor
 * Jun 11, 2019 #68118     wpaintsil   Add @DynamicSerialize
 * </pre>
 *
 * @author dfriedman
 * @version 1.0
 */
@MappedSuperclass
@DynamicSerialize
public abstract class AbstractDeckRecord extends AbstractAtcfRecord {

    private static final long serialVersionUID = 1L;

    /** Report type */
    @Column(length = 32)
    @DynamicSerializeElement
    private String reportType;

    /**
     * Basin, e.g. WP, IO, SH, CP, EP, AL, SL
     */
    @Column(length = 2)
    @DynamicSerializeElement
    private String basin;

    /**
     * Storm year (YYYY) Data will come from the deck file name
     */
    @DynamicSerializeElement
    private int year;

    /**
     * Annual cyclone number; 1 through 99
     */
    @DynamicSerializeElement
    private int cycloneNum;

    /**
     * Latitude (degrees) for the DTG: -900 through 900
     */
    @Column(nullable = false)
    @DynamicSerializeElement
    private float clat;

    /**
     * Longitude (degrees) for the DTG: -1800 through 1800
     */
    @Column(nullable = false)
    @DynamicSerializeElement
    private float clon;

    /**
     * Forecaster's initials, used for forecastHour 0 WRNG, up to 3 chars
     */
    @Column(length = 8)
    @DynamicSerializeElement
    private String forecaster;

    protected AbstractDeckRecord() {
        this.reportType = "ATCF";
        this.basin = " ";
        this.cycloneNum = IMISSD;
        this.clat = RMISSD;
        this.clon = RMISSD;
        this.forecaster = " ";
    }

    /**
     * A Copy constructor
     *
     * @param rec
     *            AbstractDeckRecord
     * @return AbstractDeckRecord
     */
    protected AbstractDeckRecord(AbstractDeckRecord rec) {
        super(rec);

        this.reportType = rec.getReportType();
        this.basin = rec.getBasin();
        this.year = rec.getYear();
        this.cycloneNum = rec.getCycloneNum();
        this.clat = rec.getClat();
        this.clon = rec.getClon();
        this.forecaster = rec.getForecaster();
    }

    public String getReportType() {
        return reportType;
    }

    public void setReportType(String reportType) {
        this.reportType = reportType;
    }

    public String getBasin() {
        return basin;
    }

    public void setBasin(String basin) {
        this.basin = basin;
    }

    public int getCycloneNum() {
        return cycloneNum;
    }

    public void setCycloneNum(int cycloneNum) {
        this.cycloneNum = cycloneNum;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public float getClat() {
        return clat;
    }

    public void setClat(float clat) {
        this.clat = clat;
    }

    public float getClon() {
        return clon;
    }

    public void setClon(float clon) {
        this.clon = clon;
    }

    public String getForecaster() {
        return forecaster;
    }

    public void setForecaster(String forecaster) {
        this.forecaster = forecaster;
    }

    /**
     * Compare each filed, return fields which have different values
     *
     * @param object
     * @return
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     */
    public Map<String, Pair<String, String>> findFieldDifference(
            AbstractDeckRecord object) throws IllegalAccessException,
            IllegalArgumentException {
        Map<String, Pair<String, String>> conflictedFields = new HashMap<>();
        Field[] fields = this.getClass().getDeclaredFields();

        for (Field field : fields) {
            if (!field.get(this).equals(field.get(object))) {
                conflictedFields.put(field.getName(),
                        new Pair<>(field.get(this).toString(),
                                field.get(object).toString()));
            }
        }
        return conflictedFields;
    }

    /*
     * Returns a partial unique ID for track (i.e., A- and B-deck) records.
     */
    protected Map<String, Object> getTrackUniqueId() {
        Map<String, Object> uid = new HashMap<>();
        uid.put("refTime", getRefTime());
        uid.put("year", getYear());
        uid.put("basin", getBasin());
        uid.put("cycloneNum", getCycloneNum());
        return uid;
    }
}
