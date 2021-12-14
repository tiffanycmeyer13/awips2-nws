/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.atcf;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * Genesis
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 3, 2020 # 77134     pwang       Initial creation
 * Nov 2, 2020   79571     wpaintsil   Mistake in getGenesisId()
 *
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */
@Entity
@Table(schema = AtcfDB.SCHEMA, uniqueConstraints = {
        @UniqueConstraint(name = "uk_genesis", columnNames = { "region",
                "genesisNum", "year" }) })
@DynamicSerialize
public class Genesis {

    private static final String MISSING_STR_VAL = "";

    private static final int MISSING_YYYY = 9999;

    private static final int MISSING_INT_DD = 99;

    private static final int MISSING_CYCLONE_NUM = -1;

    public static final int NAME_SIZE = 10;

    public static final String ID_GEN = "idgen";

    /*
     * genesisId will composed by RE + GN + YYYY RE -- Region / Basin CY --
     * Cyclone Number YYYY -- Cyclone Year
     */
    @Id
    @Column(length = 16, nullable = false)
    @DynamicSerializeElement
    private String genesisId;

    @Column(length = 24)
    @DynamicSerializeElement
    private String genesisName;

    /*
     * Region / basin Valid values: WP, IO, SH, CP, EP, AL, LS
     */
    @Column(length = 4, nullable = false)
    @DynamicSerializeElement
    private String region;

    /*
     * Sub-region code Valid values: W, A, B, S, P, C, E, L, Q
     */
    @Column(length = 4)
    @DynamicSerializeElement
    private String subRegion;

    /*
     * R2 - R5 are the regions of cyclone moved to Valid values are WP, IO, SH,
     * CP, EP, AL, LS
     */
    @Column(length = 4)
    @DynamicSerializeElement
    private String region2;

    @Column(length = 4)
    @DynamicSerializeElement
    private String region3;

    @Column(length = 4)
    @DynamicSerializeElement
    private String region4;

    @Column(length = 4)
    @DynamicSerializeElement
    private String region5;

    /*
     * Cyclone number will only be populated when the genesis become a TC
     */
    @Column(nullable = true)
    @DynamicSerializeElement
    private int cycloneNum;

    /*
     * Cyclone year (4 digits)
     */
    @Column(nullable = false)
    @DynamicSerializeElement
    private int year;

    /*
     * Highest level of tc development Values: TD, TS, TY, ST, TC, HU, SH, XX
     * (unknown)
     */
    @Column(length = 8)
    @DynamicSerializeElement
    private String tcHLevel;

    /*
     * Cyclone mover S, R, O; straight mover, recurver, odd mover
     */
    @Column(length = 8)
    @DynamicSerializeElement
    private String mover;

    /*
     * Starting DTG: YYYYMMDDHH
     */
    @DynamicSerializeElement
    private Calendar startDTG;

    /*
     * Ending DTG: YYYYMMDDHH Before ending, the value: 9999999999
     */
    @DynamicSerializeElement
    private Calendar endDTG;

    /*
     * Storm Size (MIDG (midget) , GIAN (giant), etc.
     */
    @Column(length = 16)
    @DynamicSerializeElement
    private String size;

    /*
     * Annual genesis number: 70 -79?.
     */
    @Column(length = 4, nullable = false)
    @DynamicSerializeElement
    private int genesisNum;

    /*
     * Unused
     */
    @Column(length = 32)
    @DynamicSerializeElement
    private String par1;

    /*
     * Unused
     */
    @Column(length = 32)
    @DynamicSerializeElement
    private String par2;

    /*
     * Priority for model runs (e.g., GFDN, GFDL, COAMPS-TC, H-WRF): 1-9
     */
    @Column(nullable = true)
    @DynamicSerializeElement
    private Integer priority;

    /*
     * Genesis state: GENESIS, TC, END, ARCHIVE
     */
    @Column(length = 8)
    @DynamicSerializeElement
    private String genesisState;

    /*
     * Minute of warning or TCFA (00-59)
     */
    @Column(nullable = true)
    @DynamicSerializeElement
    private Integer wtNum;

    /*
     * In the lagacy, YYYYMMDDHH format is used
     */
    @Transient
    private SimpleDateFormat tsFormater = new SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss");

    /**
     * Constructor
     */
    public Genesis() {

        genesisName = MISSING_STR_VAL;
        region = MISSING_STR_VAL;
        subRegion = MISSING_STR_VAL;
        region2 = MISSING_STR_VAL;
        region3 = MISSING_STR_VAL;
        region4 = MISSING_STR_VAL;
        region5 = MISSING_STR_VAL;

        // should not use 1-99 as missing cyclone number.
        cycloneNum = MISSING_CYCLONE_NUM;
        year = MISSING_YYYY;
        tcHLevel = MISSING_STR_VAL;
        mover = MISSING_STR_VAL;
        startDTG = null;
        endDTG = null;

        size = MISSING_STR_VAL;
        genesisNum = MISSING_INT_DD;
        par1 = MISSING_STR_VAL;
        par2 = MISSING_STR_VAL;
        priority = MISSING_INT_DD;
        genesisState = MISSING_STR_VAL;
        wtNum = MISSING_INT_DD;

    }

    public String getGenesisId() {
        if (genesisId == null || genesisId.isEmpty()) {
            if (!region.isEmpty() && genesisNum >= 0 && year != 9999) {
                genesisId = region + String.format("%02d", genesisNum) + year;
            } else {
                genesisId = "";
            }
        }
        return genesisId;
    }

    public void setGenesisId(String genesisId) {
        this.genesisId = genesisId;
    }

    public String getGenesisName() {
        return genesisName;
    }

    public void setGenesisName(String genesisName) {
        this.genesisName = genesisName;
    }

    /**
     * @return the region
     */
    public String getRegion() {
        return region;
    }

    /**
     * @param region
     *            the region to set
     */
    public void setRegion(String region) {
        this.region = region;
        if (region != null && !region.isEmpty()) {
            this.region = this.region.toUpperCase();
        }
    }

    /**
     * @return the subRegion
     */
    public String getSubRegion() {
        return subRegion;
    }

    /**
     * @param subRegion
     *            the subRegion to set
     */
    public void setSubRegion(String subRegion) {
        this.subRegion = subRegion;
        if (subRegion != null && !subRegion.isEmpty()) {
            this.subRegion = this.subRegion.toUpperCase();
        }
    }

    /**
     * @return the region2
     */
    public String getRegion2() {
        return region2;
    }

    /**
     * @param region2
     *            the region2 to set
     */
    public void setRegion2(String region2) {
        this.region2 = region2;
    }

    /**
     * @return the region3
     */
    public String getRegion3() {
        return region3;
    }

    /**
     * @param region3
     *            the region3 to set
     */
    public void setRegion3(String region3) {
        this.region3 = region3;
    }

    /**
     * @return the region4
     */
    public String getRegion4() {
        return region4;
    }

    /**
     * @param region4
     *            the region4 to set
     */
    public void setRegion4(String region4) {
        this.region4 = region4;
    }

    /**
     * @return the region5
     */
    public String getRegion5() {
        return region5;
    }

    /**
     * @param region5
     *            the region5 to set
     */
    public void setRegion5(String region5) {
        this.region5 = region5;
    }

    /**
     * @return the cycloneNum
     */
    public int getCycloneNum() {
        return cycloneNum;
    }

    /**
     * @param cycloneNum
     *            the cycloneNum to set
     */
    public void setCycloneNum(int cycloneNum) {
        this.cycloneNum = cycloneNum;
    }

    /**
     * @return the year
     */
    public int getYear() {
        return year;
    }

    /**
     * @param year
     *            the year to set
     */
    public void setYear(int year) {
        this.year = year;
    }

    public void setYear(Integer yearObj) {
        if (yearObj == null) {
            this.year = MISSING_YYYY;
        } else {
            this.year = yearObj;
        }
    }

    /**
     * @return the tcHLevel
     */
    public String getTcHLevel() {
        return tcHLevel;
    }

    /**
     * @param tcHLevel
     *            the tcHLevel to set
     */
    public void setTcHLevel(String tcHLevel) {
        this.tcHLevel = tcHLevel;
    }

    /**
     * @return the mover
     */
    public String getMover() {
        return mover;
    }

    /**
     * @param mover
     *            the mover to set
     */
    public void setMover(String mover) {
        this.mover = mover;
    }

    /**
     * @return the endDTG
     */
    public Calendar getEndDTG() {
        return endDTG;
    }

    /**
     * @param endDTG
     *            the endDTG to set
     */
    public void setEndDTG(Calendar endDTG) {
        this.endDTG = endDTG;
    }

    /**
     * @return the size
     */
    public String getSize() {
        return size;
    }

    /**
     * @param size
     *            the size to set
     */
    public void setSize(String size) {
        this.size = size;
    }

    public int getGenesisNum() {
        return genesisNum;
    }

    public void setGenesisNum(int genesisNum) {
        this.genesisNum = genesisNum;
    }

    /**
     * @return the par1
     */
    public String getPar1() {
        return par1;
    }

    /**
     * @param par1
     *            the par1 to set
     */
    public void setPar1(String par1) {
        this.par1 = par1;
    }

    /**
     * @return the par2
     */
    public String getPar2() {
        return par2;
    }

    /**
     * @param par2
     *            the par2 to set
     */
    public void setPar2(String par2) {
        this.par2 = par2;
    }

    /**
     * @return the priority
     */
    public int getPriority() {
        return priority != null ? priority : MISSING_INT_DD;
    }

    /**
     * @param priority
     *            the priority to set
     */
    public void setPriority(int priority) {
        this.priority = priority;
    }

    public void setPriority(Integer priorityObj) {
        if (priorityObj == null) {
            this.priority = MISSING_INT_DD;
        } else {
            this.priority = priorityObj;
        }
    }

    public String getGenesisState() {
        return genesisState;
    }

    public void setGenesisState(String genesisState) {
        this.genesisState = genesisState;
        if (genesisState != null && !genesisState.isEmpty()) {
            this.genesisState = this.genesisState.toUpperCase();
        }
    }

    /**
     * @return the wtNum
     */
    public int getWtNum() {
        return wtNum != null ? wtNum : MISSING_INT_DD;
    }

    /**
     * @param wtNum
     *            the wtNum to set
     */
    public void setWtNum(int wtNum) {
        this.wtNum = wtNum;
    }

    public void setWtNum(Integer wtNumObj) {
        if (wtNumObj == null) {
            this.wtNum = MISSING_INT_DD;
        } else {
            this.wtNum = wtNumObj;
        }
    }

    /**
     * @return the tsFormater
     */
    public SimpleDateFormat getTsFormater() {
        return tsFormater;
    }

    /**
     * @param tsFormater
     *            the tsFormater to set
     */
    public void setTsFormater(SimpleDateFormat tsFormater) {
        this.tsFormater = tsFormater;
    }

    /**
     * @return the startDTG
     */
    public Calendar getStartDTG() {
        return startDTG;
    }

    /**
     * @param startDTG
     *            the startDTG to set
     */
    public void setStartDTG(Calendar startDTG) {
        this.startDTG = startDTG;
    }

    /**
     * Create a Storm object from a Genesis.
     * 
     * @param genesis
     * @return storm
     */
    public Storm toStorm() {

        Storm storm = new Storm();

        storm.setStormId(genesisId);
        storm.setStormName(genesisName);
        storm.setRegion(region);
        storm.setSubRegion(subRegion);
        storm.setRegion2(region2);
        storm.setRegion3(region3);
        storm.setRegion4(region4);
        storm.setRegion5(region5);
        storm.setCycloneNum(cycloneNum);
        storm.setYear(year);
        storm.setTcHLevel(tcHLevel);
        storm.setMover(mover);
        storm.setStartDTG(startDTG);
        storm.setEndDTG(endDTG);
        storm.setSize(size);
        storm.setStormState(genesisState);
        storm.setGenesisNum(genesisNum);
        storm.setPar1(par1);
        storm.setPar2(par2);
        storm.setPriority(priority);
        storm.setWtNum(wtNum);

        return storm;

    }

}
