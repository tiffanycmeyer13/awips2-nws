/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.atcf;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;
import com.raytheon.uf.common.time.util.TimeUtil;

import gov.noaa.nws.ocp.common.dataplugin.atcf.exception.AtcfException;

/**
 * AbstractAtcfRecord
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 27, 2018            pwang       Initial creation
 * Aug 23, 2018 53502      dfriedman   Modify for Hibernate implementation
 * Apr 24, 2020 77847      jwu         Add copy constructor
 * Jul 06, 2020 79696      pwang       Add toGenesisDeckRecord() method
 * Jun 25, 2021 92918      dfriedman   Refactor data classes
 *
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */
@MappedSuperclass
@DynamicSerialize
public abstract class AbstractAtcfRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final float RMISSD = 999999.f;

    public static final Integer IMISSD = -9_999_998;

    public static final int CHANG_CD_UNCHANGE = 0;

    public static final int CHANG_CD_NEW = 1;

    public static final int CHANG_CD_MODIFY = 2;

    public static final int CHANG_CD_DELETE = 3;

    public static final String ID_GEN = "idgen";

    public static final String REFTIME_ID = "refTime";

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = ID_GEN)
    @DynamicSerializeElement
    protected int id;

    /**
     * Primary observation or forecast time associated with the record.
     */
    @Column(nullable = false)
    @DynamicSerializeElement
    protected Date refTime;

    /*
     * Empty Constructor
     */
    protected AbstractAtcfRecord() {

    }

    /**
     * A copy constructor for convenience.
     *
     * Note: id will not be copied here since a new record should have its own.
     *
     * @param rec
     *            AbstractAtcfRecord
     * @return AbstractAtcfRecord
     */
    protected AbstractAtcfRecord(AbstractAtcfRecord rec) {
        super();
        this.setRefTime(new Date(rec.getRefTime().getTime()));
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public Date getRefTime() {
        return refTime;
    }

    public void setRefTime(Date refTime) {
        this.refTime = refTime;
    }

    /**
     * Convenience method to get the {@code refTime} as a Calendar. This
     * deliberately does not have a matching setter.
     *
     * @return
     */
    public Calendar getRefTimeAsCalendar() {
        return TimeUtil.newGmtCalendar(getRefTime());
    }

    public AbstractAtcfRecord toGenesisDeckRecord() throws AtcfException {
        return null;
    }

    /**
     * Returns a map representing a unique identifier for the record if the deck
     * type supports the notion of unique entries. Otherwise, returns an empty
     * map.
     *
     * @return
     */
    public abstract Map<String, Object> getUniqueId();
}