/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.atcf;

/**
 * Indicates an ATFC Deck type (A, B, E, or F).
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Feb 20, 2019 #60291     pwang       Initial creation
 * Mar 29, 2019 #61590     dfriedman   Add deck class references.
 *
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */
public enum AtcfDeckType {
    A("A", BaseADeckRecord.class, ADeckRecord.class, SandboxADeckRecord.class),
    B("B", BaseBDeckRecord.class, BDeckRecord.class, SandboxBDeckRecord.class),
    E("E", BaseEDeckRecord.class, EDeckRecord.class, SandboxEDeckRecord.class),
    F("F", BaseFDeckRecord.class, FDeckRecord.class, SandboxFDeckRecord.class),
    T("FST", BaseBDeckRecord.class, ForecastTrackRecord.class, SandboxForecastTrackRecord.class);

    private String value;

    private Class<? extends AbstractDeckRecord> abstractRecordClass;

    private Class<? extends AbstractDeckRecord> mainRecordClass;

    private Class<? extends ISandboxRecord> sandboxRecordClass;

    /**
     * @param iValue
     */
    private AtcfDeckType(final String iValue,
            Class<? extends AbstractDeckRecord> abstractRecordClass,
            Class<? extends AbstractDeckRecord> normalRecordClass,
            Class<? extends ISandboxRecord> sandboxRecordClass) {
        this.value = iValue;
        this.abstractRecordClass = abstractRecordClass;
        this.mainRecordClass = normalRecordClass;
        this.sandboxRecordClass = sandboxRecordClass;
    }

    /**
     * @return the value
     */
    public String getValue() {
        return value;
    }

    public Class<? extends AbstractDeckRecord> getAbstractRecordClass() {
        return abstractRecordClass;
    }

    public Class<? extends AbstractDeckRecord> getMainRecordClass() {
        return mainRecordClass;
    }

    public Class<? extends ISandboxRecord> getSandboxRecordClass() {
        return sandboxRecordClass;
    }


}
