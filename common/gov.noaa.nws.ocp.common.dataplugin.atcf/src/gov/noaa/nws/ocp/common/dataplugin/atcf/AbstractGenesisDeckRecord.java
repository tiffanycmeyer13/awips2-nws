/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.atcf;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;
import com.raytheon.uf.common.util.Pair;

/**
 * AbstractGenesisDeckRecord
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 3, 2020 # 77134     pwang       Initial creation
 * Jul 6, 2020 # 79696     pwang       Add method to convert AbstractDeckRecord to AbstractGenesisDeckRecord
 *
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */
@MappedSuperclass
public abstract class AbstractGenesisDeckRecord extends AbstractAtcfRecord {

    private static final long serialVersionUID = 1L;

    public static final String GENESISNUM_FIELD_NAME = "genesisNum";

    public static final String CYCLONENUM_FIELD_NAME = "cycloneNum";

    public static final int MIN_GENESIS_CYCLONE_NUM = 70;

    public static final int MAX_GENESIS_CYCLONE_NUM = 79;

    /** Report type */
    @Column(length = 32)
    @DynamicSerializeElement
    protected String reportType;

    /**
     * Basin, e.g. WP, IO, SH, CP, EP, AL, SL
     */
    @Column(length = 2)
    @DynamicSerializeElement
    protected String basin;

    /**
     * Storm year (YYYY) Data will come from the deck file name
     */
    @DynamicSerializeElement
    protected int year;

    /**
     * Annual cyclone number; 1 through 99
     */
    @DynamicSerializeElement
    protected int genesisNum;

    @Column(nullable = true)
    @DynamicSerializeElement
    protected int cycloneNum;

    /**
     * Latitude (degrees) for the DTG: -900 through 900
     */
    @Column(nullable = false)
    @DynamicSerializeElement
    protected float clat;

    /**
     * Longitude (degrees) for the DTG: -1800 through 1800
     */
    @Column(nullable = false)
    @DynamicSerializeElement
    protected float clon;

    /**
     * Forecaster's initials, used for forecastHour 0 WRNG, up to 3 chars
     */
    @Column(length = 8)
    @DynamicSerializeElement
    protected String forecaster;

    protected AbstractGenesisDeckRecord() {
        this.reportType = "ATCF";
        this.basin = " ";
        this.genesisNum = IMISSD;
        this.clat = RMISSD;
        this.clon = RMISSD;
        this.forecaster = " ";
    }

    protected AbstractGenesisDeckRecord(AbstractDeckRecord rec) {
        super(rec);
        this.reportType = rec.getReportType();
        this.basin = rec.getBasin();
        this.genesisNum = rec.getCycloneNum();
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

    public int getGenesisNum() {
        return genesisNum;
    }

    public void setGenesisNum(int genesisNum) {
        this.genesisNum = genesisNum;
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
     * Compare each field, return fields which have different values
     *
     * @param object
     * @return
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException
     */
    public Map<String, Pair<String, String>> findFieldDifference(AbstractDeckRecord object)
            throws IllegalAccessException, IllegalArgumentException {
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
        uid.put(GENESISNUM_FIELD_NAME, getGenesisNum());
        return uid;
    }

}
