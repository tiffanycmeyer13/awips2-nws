/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.atcf;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;

/**
 * Represents ATCF Automated Tropical Cyclone Forecast (deck a, b). This
 * contains getters and setters for the main parent table atcf. This code has
 * been developed by the SIB for use in the AWIPS2 system.
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 27, 2018            pwang       Initial creation
 * Aug 23, 2018 53502      dfriedman   Modify for Hibernate implementation
 * Sep 12, 2019 68237      dfriedman   Remove stormName from unique fields
 * Apr 24, 2020 77847      jwu         Add copy constructor
 *
 *
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */
@Entity
@Table(schema = AtcfDB.SCHEMA, name = "adeck", uniqueConstraints = {
        @UniqueConstraint(name = "uk_a2atcf_adeck", columnNames = { "basin",
                "cycloneNum", "year", "refTime", "fcstHour", "technique",
        "radWind" }) }, indexes = {
                @Index(name = "adeck_reftime_index", columnList = "reftime"),
                @Index(name = "adeck_stormid_index", columnList = "basin,year,cyclonenum"),
                @Index(name = "adeck_technique_index", columnList = "technique") })
@SequenceGenerator(initialValue = 1, name = AbstractAtcfRecord.ID_GEN, sequenceName = AtcfDB.SEQUENCE_NAME_PREFIX
+ "adeckseq", schema = AtcfDB.SCHEMA)
@DynamicSerialize
public class ADeckRecord extends BaseADeckRecord {

    private static final long serialVersionUID = 1L;

    /**
     * Default Constructor
     */
    public ADeckRecord() {
    }

    /**
     * A Copy constructor for convenience.
     *
     * @param rec
     *            ADeckRecord
     * @return ADeckRecord
     */
    public ADeckRecord(ADeckRecord rec) {
        super(rec);
    }

    /**
     * Construct an ADeckRecord for a storm/tech/dtg/tau/wind Radii with given
     * lat/lon/max wind.
     *
     * @param storm
     *            Storm
     * @param tech
     *            technique name
     * @param refTime
     *            Date
     * @param tau
     *            forecast hour
     * @param radWnd
     *            wind radii (34, 50, or 64)
     * @param lat
     *            latitude
     * @param lon
     *            longitude
     * @param maxWnd
     *            max wind *
     */
    public ADeckRecord(Storm storm, String tech, Date refTime, int tau,
            int radWnd, float lat, float lon, float maxWnd) {

        super();

        setBasin(storm.getRegion());
        setYear(storm.getYear());
        setCycloneNum(storm.getCycloneNum());
        setStormName(storm.getStormName());
        setSubRegion(storm.getSubRegion());

        setRefTime(refTime);
        setFcstHour(tau);

        setTechnique(tech);

        setClat(lat);
        setClon(lon);
        setWindMax(maxWnd);

        setRadWind(radWnd);
        setQuad1WindRad(0);
        setQuad2WindRad(0);
        setQuad3WindRad(0);
        setQuad4WindRad(0);

        setMaxSeas(0);
        setEyeSize(0);
        setTechniqueNum(0);
    }

    public ADeckRecord(ForecastTrackRecord fstRec) {
        this.setTechnique("OFCL");
        this.setTechniqueNum(3);

        this.setBasin(fstRec.getBasin());
        this.setClat(fstRec.getClat());
        this.setClon(fstRec.getClon());
        this.setRefTime(fstRec.getRefTime());
        this.setCycloneNum(fstRec.getCycloneNum());
        this.setYear(fstRec.getYear());
        this.setForecaster(fstRec.getForecaster());
        this.setReportType(fstRec.getReportType());

        this.setFcstHour(fstRec.getFcstHour());
        this.setStormName(fstRec.getStormName());
        this.setClosedP(fstRec.getClosedP());
        this.setEyeSize(fstRec.getEyeSize());
        this.setGust(fstRec.getGust());
        this.setIntensity(fstRec.getIntensity());
        this.setMaxSeas(fstRec.getMaxSeas());
        this.setMaxWindRad(fstRec.getMaxWindRad());
        this.setMslp(fstRec.getMslp());
        this.setQuad1WaveRad(fstRec.getQuad1WaveRad());
        this.setQuad2WaveRad(fstRec.getQuad2WaveRad());
        this.setQuad3WaveRad(fstRec.getQuad3WaveRad());
        this.setQuad4WaveRad(fstRec.getQuad4WaveRad());
        this.setQuad1WindRad(fstRec.getQuad1WindRad());
        this.setQuad2WindRad(fstRec.getQuad2WindRad());
        this.setQuad3WindRad(fstRec.getQuad3WindRad());
        this.setQuad4WindRad(fstRec.getQuad4WindRad());
        this.setRadClosedP(fstRec.getRadClosedP());
        this.setRadWave(fstRec.getRadWave());
        this.setRadWaveQuad(fstRec.getRadWaveQuad());
        this.setRadWind(fstRec.getRadWind());
        this.setRadWindQuad(fstRec.getRadWindQuad());
        this.setStormDepth(fstRec.getStormDepth());
        this.setStormSped(fstRec.getStormSped());
        this.setStormDrct(fstRec.getStormDrct());
        this.setSubRegion(fstRec.getSubRegion());
        this.setUserData(fstRec.getUserData());
        this.setUserDefined(fstRec.getUserDefined());
        this.setWindMax(fstRec.getWindMax());
    }

}
