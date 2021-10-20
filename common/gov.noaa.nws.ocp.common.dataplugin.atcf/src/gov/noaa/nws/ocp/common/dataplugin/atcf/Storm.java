/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.atcf;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.time.util.TimeUtil;

/**
 * Storm class
 *
 * <pre>
 *
 * A storm is uniquely defined by Basin + year + cycloneNum
 *
 * Each storm has one descriptive record which is a brief summary of the storm
 * history including storm name, track type, maximum development level, etc.
 *
 * The information is stored in the file storms.txt for all active storms, and
 * storms.archive (storm.table) for all storms for a selected year.
 *
 * The data format is as follows:
 *
 * STORM NAME,RE,X,R2,R3,R4,R5,CY,YYYY,TY,I,YYY1MMDDHH,YYY2MMDDHH,SIZE,GENESIS_NUM,PAR1,PAR2,PRIORITY,STORM_STATE,WT_NUMBER,STORMID
 * where
 *     STORM NAME = Literal storm name, "INVEST", or "GENESISxxx" where xxx is a number
 *     RE = Region (basin) code: WP, IO, SH, CP, EP, AL, LS.
 *     X =  Subregion code: W, A, B, S, P, C, E, L, Q.
 *     R2 = Region 2 code: WP, IO, SH, CP, or EP. This and R3-R5 are codes for basins entered
 *                         subsequent to the original basin where the storm was generated.
 *     R3 = Region 3 code: WP, IO, SH, CP, or EP.
 *     R4 = Region 4 code: WP, IO, SH, CP, or EP.
 *     R5 = Region 5 code: WP, IO, SH, CP, or EP.
 *     CY = Annual cyclone number: 01 through 99.
 *     YYYY = Cyclone Year: 0000 through 9999.
 *     TY = Highest level of tc development: TD, TS, TY, ST, TC, HU, SH, XX (unknown).
 *     I = S, R, O; straight mover, recurver, odd mover.
 *     YYY1MMDDHH = Starting DTG: 0000010100 through 9999123123.
 *     YYY2MMDDHH = Ending DTG: 0000010100 through 9999123123.
 *     SIZE = Storm Size (MIDG (midget) , GIAN (giant), etc.).
 *     GENESIS_NUM = Annual genesis number: 001 through 999.
 *     PAR1 = UNUSED.
 *     PAR2 = UNUSED.
 *     PRIORITY = Priority for model runs (e.g., GFDN, GFDL, COAMPS-TC, H-WRF): 1-9.
 *     STORM_STATE = Storm state: METWATCH,TCFA,WARNING or ARCHIVE
 *     WT_NUMBER = Minute of warning or TCFA (00-59)
 *     STORMID = Storm ID composed of basin designator and annual cyclone number (e.g. WP081993)
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * May 16, 2018            pwang       Initial creation
 * Aug 23, 2018 #53502     dfriedman   Modify for Hibernate implementation
 * Apr 16, 2020 #71986     mporricelli Added CSV formatting for storms
 *                                     data and sorting to create
 *                                     storms.txt output
 * Nov 01, 2020 82623      jwu         add copy/build and update CSV formatting.
 *
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */
@Entity
@Table(schema = AtcfDB.SCHEMA, uniqueConstraints = {
        @UniqueConstraint(name = "uk_storm", columnNames = { "region",
                "cycloneNum", "year" }) })
@DynamicSerialize
public class Storm {

    private static final IUFStatusHandler logger = UFStatus
            .getHandler(Storm.class);

    public static final int NAME_SIZE = 10;

    private static final String MISSING_DTG_VAL = "9999999999";

    private static final int MISSING_YYYY = 9999;

    private static final int MISSING_INT_DD = 99;

    private static final int MISSING_GENESIS_NUM = 999;

    private static final String MISSING_STR_VAL = "";

    private static final String CSV_SEP = ",";

    private static final DateTimeFormatter DTG_FORMAT = DateTimeFormatter
            .ofPattern("yyyyMMddHH");

    /*
     * stormId will composed by RE + CY + YYYY RE -- Region / Basin CY --
     * Cyclone Number YYYY -- Cyclone Year
     */
    @Id
    @Column(length = 16, nullable = false)
    @DynamicSerializeElement
    private String stormId;

    @Column(length = 24)
    @DynamicSerializeElement
    private String stormName;

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
     * Cyclone number of the year Valid value: 01 to 99
     */
    @Column(nullable = false)
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
     * Annual genesis number: 001 through 999.
     */
    @Column(length = 4)
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
     * Storm state: METWATCH,TCFA,WARNING or ARCHIVE
     */
    @Column(length = 8)
    @DynamicSerializeElement
    private String stormState;

    /*
     * Minute of warning or TCFA (00-59)
     */
    @Column(nullable = true)
    @DynamicSerializeElement
    private Integer wtNum;

    /*
     * In the legacy, YYYYMMDDHH format is used
     */
    @Transient
    private SimpleDateFormat tsFormater = new SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss");

    /**
     * Constructor
     */
    public Storm() {

        stormId = MISSING_STR_VAL;
        stormName = MISSING_STR_VAL;
        region = MISSING_STR_VAL;
        subRegion = MISSING_STR_VAL;
        region2 = MISSING_STR_VAL;
        region3 = MISSING_STR_VAL;
        region4 = MISSING_STR_VAL;
        region5 = MISSING_STR_VAL;

        cycloneNum = MISSING_INT_DD;
        year = MISSING_YYYY;
        tcHLevel = MISSING_STR_VAL;
        mover = MISSING_STR_VAL;
        startDTG = null;
        endDTG = null;

        size = MISSING_STR_VAL;
        genesisNum = MISSING_GENESIS_NUM;
        par1 = MISSING_STR_VAL;
        par2 = MISSING_STR_VAL;
        priority = MISSING_INT_DD;
        stormState = MISSING_STR_VAL;
        wtNum = MISSING_INT_DD;

    }

    // Getter and setter

    /**
     * @return the stormId
     */
    public String getStormId() {
        if (stormId.isEmpty() && !region.isEmpty() && cycloneNum >= 0
                && year != 9999) {
            stormId = region + String.format("%02d", cycloneNum) + year;
        }
        return stormId;
    }

    /**
     * @param stormId
     *            the stormId to set
     */
    public void setStormId(String stormId) {
        this.stormId = stormId;
    }

    /**
     * @return the stormName
     */
    public String getStormName() {
        return stormName;
    }

    /**
     * @param stormName
     *            the stormName to set
     */
    public void setStormName(String stormName) {
        this.stormName = stormName;
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

    /**
     * @return the stormState
     */
    public String getStormState() {
        return stormState;
    }

    /**
     * @param stormState
     *            the stormState to set
     */
    public void setStormState(String stormState) {
        this.stormState = stormState;
        if (stormState != null && !stormState.isEmpty()) {
            this.stormState = this.stormState.toUpperCase();
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
     * Constructs a CSV string representation of the data in the storm table
     * compatible with ATCF legacy storms.txt file. Sample line (wrapped):
     *
     * HAZEL, AL, L, , , , , 09, 1954, HU, O, 1954100506, 1954101812, , , , , ,
     * ARCHIVE, , AL091954
     *
     * @return the csv-formatted string for the storm entry
     */

    public String toCsvStormString() {
        final String sep = CSV_SEP + " ";
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%10s", stormName));
        sb.append(sep);
        sb.append(String.format("%2s", getRegion()));
        sb.append(sep);

        if (!getSubRegion().isEmpty()) {
            sb.append(String.format("%s", getSubRegion()));
        }
        sb.append(sep);

        if (!getRegion2().isEmpty()) {
            sb.append(String.format("%s", getRegion2()));
        }
        sb.append(sep);

        if (!getRegion3().isEmpty()) {
            sb.append(String.format("%s", getRegion3()));
        }
        sb.append(sep);

        if (!getRegion4().isEmpty()) {
            sb.append(String.format("%s", getRegion4()));
        }
        sb.append(sep);

        if (!getRegion5().isEmpty()) {
            sb.append(String.format("%s", getRegion5()));
        }
        sb.append(sep);

        // use leading zero
        sb.append(String.format("%02d", getCycloneNum()));
        sb.append(sep);

        sb.append(String.format("%4d", getYear()));
        sb.append(sep);

        if (!getTcHLevel().isEmpty()) {
            sb.append(String.format("%2s", getTcHLevel()));
        }
        sb.append(sep);

        if (!getMover().isEmpty()) {
            sb.append(String.format("%s", getMover()));
        }
        sb.append(sep);

        SimpleDateFormat datef = new SimpleDateFormat("yyyyMMddHH");
        sb.append((getStartDTG() == null ? String.format("%10s", MISSING_DTG_VAL)
                : String.format("%10s",
                        datef.format(getStartDTG().getTime()))));
        sb.append(sep);

        sb.append((getEndDTG() == null ? String.format("%10s", MISSING_DTG_VAL)
                : String.format("%10s", datef.format(getEndDTG().getTime()))));
        sb.append(sep);

        if (!getSize().isEmpty()) {
            sb.append(String.format("%4s", getSize()));
        }
        sb.append(sep);

        if (getGenesisNum() != MISSING_GENESIS_NUM) {
            // use leading zero
            sb.append(String.format("%03d", getGenesisNum()));
        }
        sb.append(sep);

        if (!getPar1().isEmpty()) {
            sb.append(String.format("%s", getPar1()));
        }
        sb.append(sep);

        if (!getPar2().isEmpty()) {
            sb.append(String.format("%s", getPar2()));
        }
        sb.append(sep);

        if (getPriority() != MISSING_INT_DD) {
            sb.append(String.format("%d", getPriority()));
        }
        sb.append(sep);

        if (!getStormState().isEmpty()) {
            sb.append(String.format("%7s", getStormState()));
        }
        sb.append(sep);

        if (getWtNum() != MISSING_INT_DD) {
            sb.append(String.format("%d", getWtNum()));
        }
        sb.append(sep);

        sb.append(String.format("%8s", getStormId()));

        return sb.toString();
    }

    /**
     * Sort storm records by several fields to make storm list output match
     * format of Legacy ATCF storms.txt
     *
     * @param stormRecs
     */
    public static void sortStormList(List<Storm> stormRecs) {
        Comparator<Storm> startDtgCmp = Comparator.comparing(Storm::getStartDTG,
                Comparator.nullsLast(Comparator.naturalOrder()));
        Comparator<Storm> strmYrCmp = Comparator.comparing(Storm::getYear);
        Comparator<Storm> cycNumCmp = Comparator
                .comparing(Storm::getCycloneNum);

        Collections.sort(stormRecs,
                startDtgCmp.thenComparing(strmYrCmp).thenComparing(cycNumCmp));

    }

    /**
     *
     * @param newStorm
     * @return
     */
    public Map<String, Object> stormChangedFields(Storm newStorm) {
        Map<String, Object> changedFields = new HashMap<>();
        /* Storm only fields */
        if (!this.stormName.equals(newStorm.getStormName())) {
            changedFields.put("stormName", newStorm.getStormName());
        } else {
            changedFields.put("stormName", "NaN");
        }
        if (!this.subRegion.equalsIgnoreCase(newStorm.getSubRegion())) {
            changedFields.put("subRegion", newStorm.getSubRegion());
        } else {
            changedFields.put("subRegion", "NaN");
        }
        if (!this.mover.equalsIgnoreCase(newStorm.getMover())) {
            changedFields.put("mover", newStorm.getMover());
        } else {
            changedFields.put("mover", "NaN");
        }
        if (!this.stormState.equalsIgnoreCase(newStorm.getStormState())) {
            changedFields.put("stormState", newStorm.getStormState());
        } else {
            changedFields.put("stormState", "NaN");
        }
        if (this.wtNum != newStorm.getWtNum()) {
            changedFields.put("wtNum", newStorm.getWtNum());
        } else {
            changedFields.put("wtNum", -1);
        }

        /* Storm, ABEF decks and related sandbox */
        if (!this.region.equalsIgnoreCase(newStorm.getRegion())) {
            changedFields.put("region", newStorm.getRegion());
        } else {
            changedFields.put("region", "NaN");
        }
        if (this.cycloneNum != newStorm.getCycloneNum()) {
            changedFields.put("cycloneNum", newStorm.getCycloneNum());
        } else {
            changedFields.put("cycloneNum", -1);
        }

        return changedFields;
    }

    /**
     * Copy a storm.
     */
    public Storm copy() {

        Storm newStorm = new Storm();

        newStorm.setStormName( this.stormName);
        newStorm.setRegion( this.region);
        newStorm.setSubRegion( this.subRegion);
        newStorm.setRegion2( this.region2);
        newStorm.setRegion3( this.region3);
        newStorm.setRegion4( this.region4);
        newStorm.setRegion5( this.region5);
        newStorm.setCycloneNum(this.cycloneNum);
        newStorm.setYear(this.year);
        newStorm.setTcHLevel( this.tcHLevel);
        newStorm.setMover( this.mover);

        if (this.startDTG != null) {
            newStorm.setStartDTG((Calendar) this.startDTG.clone());
        }

        if (this.endDTG != null) {
            newStorm.setEndDTG((Calendar) this.endDTG.clone());
        }

        newStorm.setSize( this.size);
        newStorm.setGenesisNum(this.genesisNum);
        newStorm.setPar1( this.par1);
        newStorm.setPar2( this.par2);
        newStorm.setPriority(this.priority);
        newStorm.setStormState( this.stormState);
        newStorm.setWtNum(this.wtNum);

        return newStorm;
    }

    /**
     * Build a storm from a CSV string entry.
     *
     * @param entry
     *            A CSV storm entry
     *
     * @return Storm Storm built from the entry.
     */
    public static Storm build(String entry) {

        Storm storm = null;
        String[] items = entry.split(CSV_SEP);
        int cycloneNum = MISSING_INT_DD;
        int year = MISSING_YYYY;
        boolean toBuild = true;

        // Sanity check for items in entry, cyclone number, & storm year.
        if (items.length < 21) {
            logger.warn("Storm.build(): Storm entry has less than 21 fields: <"
                    + entry + ">");
            toBuild = false;
        } else {
            try {
                cycloneNum = Integer.parseInt(items[7].trim());
            } catch (NumberFormatException ne) {
                toBuild = false;
                logger.warn(
                        "Storm.build(): Storm entry has invalid cyclone number: <"
                                + entry + ">");
            }

            try {
                year = Integer.parseInt(items[8].trim());
            } catch (NumberFormatException ne) {
                logger.warn("Storm.build(): Storm entry has invalid year: <"
                        + entry + ">");
                toBuild = false;
            }
        }

        // Build a new storm with information.
        if (toBuild) {

            storm = new Storm();

            Integer genesisNum = null;
            try {
                genesisNum = Integer.parseInt(items[14].trim());
            } catch (NumberFormatException ne) {
                // If invalid, do not set.
            }

            Integer priority = null;
            try {
                priority = Integer.parseInt(items[17].trim());
            } catch (NumberFormatException ne) {
                // If invalid, it will be auto-set to missing value 99.
            }

            Integer wtNum = null;
            try {
                wtNum = Integer.parseInt(items[19].trim());
            } catch (NumberFormatException ne) {
                // If invalid, it will be auto-set to missing value 99.
            }

            Calendar startCal = null;
            if (!Storm.MISSING_DTG_VAL.equals(items[11].trim())) {
                try {
                    startCal = getCalendar(items[11].trim());
                } catch (DateTimeParseException ne) {
                    // Null is acceptable.
                }
            }

            Calendar endCal = null;
            if (!Storm.MISSING_DTG_VAL.equals(items[12].trim())) {
                try {
                    endCal = getCalendar(items[12].trim());
                } catch (DateTimeParseException ne) {
                    // Null is acceptable.
                }
            }

            storm.setStormName(items[0].trim());
            storm.setRegion(items[1].trim());
            storm.setSubRegion(items[2].trim());
            storm.setRegion2(items[3].trim());
            storm.setRegion3(items[4].trim());
            storm.setRegion4(items[5].trim());
            storm.setRegion5(items[6].trim());
            storm.setCycloneNum(cycloneNum); // item[7]
            storm.setYear(year); // item[8]
            storm.setTcHLevel(items[9].trim());
            storm.setMover(items[10].trim());
            storm.setStartDTG(startCal); // items[11]
            storm.setEndDTG(endCal); // items[12]
            storm.setSize(items[13].trim());
            if (genesisNum != null) {
                storm.setGenesisNum(genesisNum); // items[14]
            }
            storm.setPar1(items[15].trim());
            storm.setPar2(items[16].trim());
            storm.setPriority(priority); // items[17]
            storm.setStormState(items[18].trim());
            storm.setWtNum(wtNum); // items[19]
            storm.setStormId(items[20].trim());
        }

        return storm;
    }

    /*
     * Build a Calendar from a DTG (yyyyMMddHH).
     *
     * @param dtg Date time group
     *
     * @return Calendar
     */
    private static Calendar getCalendar(String dtg)
            throws DateTimeParseException {

        Date date = Date.from(
                LocalDateTime.parse(dtg, DTG_FORMAT).toInstant(ZoneOffset.UTC));
        return TimeUtil.newGmtCalendar(date);
    }

}
