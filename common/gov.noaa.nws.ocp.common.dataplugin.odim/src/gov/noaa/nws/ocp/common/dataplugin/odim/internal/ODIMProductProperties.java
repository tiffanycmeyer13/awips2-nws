/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.common.dataplugin.odim.internal;

/**
 * Data used by ODIMProductUtil
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Sep 12, 2022 DCS 21569  dfriedman   Initial creation
 * </pre>
 *
 * @author dfriedman
 */
public enum ODIMProductProperties {
    DBZH("Z", "Reflectivity", 153),
    TH("TH", "H-Total Reflectivity Factor", 153),
    TV("TV", "V-Total Reflectivity Factor", 153),
    ZDR("ZDR", "Diff Reflectivity", 159),
    VRADH("V", "Velocity", 154),
    WRADH("SW", "Spectrum Width", 155),
    KDP("KDP", "Specific Diff Phase", 163),
    PHIDP("SDP", "Raw PHIDP", 168),
    UPHIDP("SDP", "Raw PHIDP Unfiltered", 168),
    RHOHV("CC", "Correlation Coeff", 161),
    URHOHV("SDC", "Raw CC", 167),
    SQIH("CC", "H-Signal Quality", 161),
    SQIV("CC", "V-Signal Quality", 161),
    USQIH("CC", "H-Signal Quality Unflitered", 161),
    USQIV("CC", "V-Signal Quality Unflitered", 161);

    private String mnemonic;

    private String description;

    private Integer nexradProductCode;

    ODIMProductProperties(String mnemonic, String description,
            Integer productCode) {
        this.mnemonic = mnemonic;
        this.description = description;
        this.nexradProductCode = productCode;
    }

    public String getMnemonic() {
        return mnemonic;
    }

    public String getDescription() {
        return description;
    }

    public Integer getNexradProductCode() {
        return nexradProductCode;
    }
}
