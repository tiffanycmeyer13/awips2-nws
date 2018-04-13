/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.localization.psh;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * PSH product configuration header.
 *
 * This class holds the same info as in the legacy file "config_headers.txt",
 * which is configured via the "Program Configuration" dialog.
 *
 * <pre>
 *
 *    WFO Header:              PSHTBW
 *       WFO Node:             KTBW
 *         Header:             POST TROPICAL CYCLONE REPORT...HURRICANE
 *        Station:             NATIONAL WEATHER SERVICE TAMPA BAY AREA - RUSKIN FL
 *            PIL:             MIAPSHTBW
 *       PILFile1:             KTBWPSHTBW
 *  GUI Directory:             /data/local/PSH/
 *  LSR/Cities Dir:            /data/fxa/tstorm
 *  Time Zone(A/E/C/M/P/H/CH): E
 *  Time Difference:           5
 *  Daylight Saving (Y/N):     Y
 *  TC Basic (AT/EP/CP/WP):    AT
 *  LSR Header:                MIALSRTBW
 *  LSR WMO Node:              NWUS52
 *  Use Mixed Case:            N                         (new)
 *  Export Product:            None/Localization/User    (new)
 *  Export Directory:          Dir to export product     (new)
 *
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 26 JUN 2017  #35269     jwu         Initial creation
 * 05 SEP 2017  #37365     jwu         Add mixedCase flag and time zone.
 * 05 DEC 2017  #41620     wpaintsil   Add fields for xml export options.
 * 11 JAN 2018  DCS19326   jwu         Baseline version.
 * </pre>
 *
 * @author jwu
 * @version 1.0
 */
@DynamicSerialize
@XmlRootElement(name = "PSHConfigHeader")
@XmlAccessorType(XmlAccessType.NONE)
public class PshConfigHeader {

    /**
     * WFO header
     */
    @DynamicSerializeElement
    @XmlElement(name = "WFOHeader")
    private String wfoHeader;

    /**
     * WFO Node
     */
    @DynamicSerializeElement
    @XmlElement(name = "WFONode")
    private String wfoNode;

    /**
     * PSH product header (first line of the product)
     */
    @DynamicSerializeElement
    @XmlElement(name = "productHeader")
    private String productHeader;

    /**
     * PSH product station header (second line of the product)
     */
    @DynamicSerializeElement
    @XmlElement(name = "productStation")
    private String productStation;

    /**
     * PIL for PSH product
     */
    @DynamicSerializeElement
    @XmlElement(name = "productPIL")
    private String productPil;

    /**
     * PIL file for PSH product
     */
    @DynamicSerializeElement
    @XmlElement(name = "PILFile")
    private String pilFile;

    /**
     * GUI directory (not needed)
     */
    @DynamicSerializeElement
    @XmlElement(name = "GUIDir")
    private String guiDir;

    /**
     * Time Zone (A/E/C/M/P/H/CH)
     */
    @DynamicSerializeElement
    @XmlElement(name = "TimeZone")
    private PshTimeZone timeZone;

    /**
     * Time difference.
     */
    @DynamicSerializeElement
    @XmlElement(name = "TimeDifference")
    private int timeDifference;

    /**
     * Flag for daylight saving
     */
    @DynamicSerializeElement
    @XmlElement(name = "DaylightSaving")
    private boolean daylightSaving;

    /**
     * TC basin (AT/EP/CP/WP)
     */
    @DynamicSerializeElement
    @XmlElement(name = "TCBasin")
    private PshBasin tcBasin;

    /**
     * LSR Header
     */
    @DynamicSerializeElement
    @XmlElement(name = "LSRHeader")
    private String lsrHeader;

    /**
     * LSR WMO Node
     */
    @DynamicSerializeElement
    @XmlElement(name = "LSRWMONode")
    private String lsrWmoNode;

    /**
     * Flag for using mixed case in report.
     */
    @DynamicSerializeElement
    @XmlElement(name = "useMixedCase")
    private boolean useMixedCase;

    /**
     * Type to export PSH product (NONE, Localization, or Local)
     */
    @DynamicSerializeElement
    @XmlElement(name = "exportProduct")
    private PshExportType exportProduct;

    /**
     * Destination to PSH data/product.
     */
    @DynamicSerializeElement
    @XmlElement(name = "exportDir")
    private String exportDir;

    /**
     * Constructor.
     */
    public PshConfigHeader() {
    }

    /**
     * @return the wfoHeader
     */
    public String getWfoHeader() {
        return wfoHeader;
    }

    /**
     * @param wfoHeader
     *            the wfoHeader to set
     */
    public void setWfoHeader(String wfoHeader) {
        this.wfoHeader = wfoHeader;
    }

    /**
     * @return the wfoNode
     */
    public String getWfoNode() {
        return wfoNode;
    }

    /**
     * @param wfoNode
     *            the wfoNode to set
     */
    public void setWfoNode(String wfoNode) {
        this.wfoNode = wfoNode;
    }

    /**
     * @return the productHeader
     */
    public String getProductHeader() {
        return productHeader;
    }

    /**
     * @param productHeader
     *            the productHeader to set
     */
    public void setProductHeader(String productHeader) {
        this.productHeader = productHeader;
    }

    /**
     * @return the productStation
     */
    public String getProductStation() {
        return productStation;
    }

    /**
     * @param productStation
     *            the productStation to set
     */
    public void setProductStation(String productStation) {
        this.productStation = productStation;
    }

    /**
     * @return the productPil
     */
    public String getProductPil() {
        return productPil;
    }

    /**
     * @param productPil
     *            the productPil to set
     */
    public void setProductPil(String productPil) {
        this.productPil = productPil;
    }

    /**
     * @return the pilFile
     */
    public String getPilFile() {
        return pilFile;
    }

    /**
     * @param pilFile
     *            the pilFile to set
     */
    public void setPilFile(String pilFile) {
        this.pilFile = pilFile;
    }

    /**
     * @return the guiDir
     */
    public String getGuiDir() {
        return guiDir;
    }

    /**
     * @param guiDir
     *            the guiDir to set
     */
    public void setGuiDir(String guiDir) {
        this.guiDir = guiDir;
    }

    /**
     * @return the lsrCitiesDir
     */
    public String getLsrCitiesDir() {
        return lsrCitiesDir;
    }

    /**
     * @param lsrCitiesDir
     *            the lsrCitiesDir to set
     */
    public void setLsrCitiesDir(String lsrCitiesDir) {
        this.lsrCitiesDir = lsrCitiesDir;
    }

    /**
     * @return the timeZone
     */
    public PshTimeZone getTimeZone() {
        return timeZone;
    }

    /**
     * @param timeZone
     *            the timeZone to set
     */
    public void setTimeZone(PshTimeZone timeZone) {
        this.timeZone = timeZone;
    }

    /**
     * @return the timeDifference
     */
    public int getTimeDifference() {
        return timeDifference;
    }

    /**
     * @param timeDifference
     *            the timeDifference to set
     */
    public void setTimeDifference(int timeDifference) {
        this.timeDifference = timeDifference;
    }

    /**
     * @return the daylightSaving
     */
    public boolean isDaylightSaving() {
        return daylightSaving;
    }

    /**
     * @param daylightSaving
     *            the daylightSaving to set
     */
    public void setDaylightSaving(boolean daylightSaving) {
        this.daylightSaving = daylightSaving;
    }

    /**
     * @return the tcBasin
     */
    public PshBasin getTcBasin() {
        return tcBasin;
    }

    /**
     * @param tcBasin
     *            the tcBasin to set
     */
    public void setTcBasin(PshBasin tcBasin) {
        this.tcBasin = tcBasin;
    }

    /**
     * @return the lsrHeader
     */
    public String getLsrHeader() {
        return lsrHeader;
    }

    /**
     * @param lsrHeader
     *            the lsrHeader to set
     */
    public void setLsrHeader(String lsrHeader) {
        this.lsrHeader = lsrHeader;
    }

    /**
     * @return the lsrWmoNode
     */
    public String getLsrWmoNode() {
        return lsrWmoNode;
    }

    /**
     * @param lsrWmoNode
     *            the lsrWmoNode to set
     */
    public void setLsrWmoNode(String lsrWmoNode) {
        this.lsrWmoNode = lsrWmoNode;
    }

    /**
     * @return the useMixedCase
     */
    public boolean isUseMixedCase() {
        return useMixedCase;
    }

    /**
     * @param useMixedCase
     *            the useMixedCase to set
     */
    public void setUseMixedCase(boolean useMixedCase) {
        this.useMixedCase = useMixedCase;
    }

    /**
     * @return the exportProduct
     */
    public PshExportType getExportProduct() {
        return exportProduct;
    }

    /**
     * @param exportProduct
     *            the exportProduct to set
     */
    public void setExportProduct(PshExportType exportProduct) {
        this.exportProduct = exportProduct;
    }

    /**
     * @return the exportDir
     */
    public String getExportDir() {
        return exportDir;
    }

    /**
     * @param exportDir
     *            the exportDir to set
     */
    public void setExportDir(String exportDir) {
        this.exportDir = exportDir;
    }

    /**
     * @return a new object filled with default values.
     */
    public static PshConfigHeader getDefaultHeader() {
        PshConfigHeader header = new PshConfigHeader();
        header.setDataToDefault();
        return header;
    }

    /**
     * Set data to default values.
     */
    public void setDataToDefault() {
        this.wfoHeader = "";
        this.wfoNode = "";
        this.productHeader = "POST TROPICAL CYCLONE REPORT...HURRICANE";
        this.productStation = "NATIONAL WEATHER SERVICE TAMPA BAY AREA - RUSKIN FL";
        this.productPil = "";
        this.pilFile = "";
        this.guiDir = "";
        this.lsrCitiesDir = "";
        this.timeZone = PshTimeZone.E;
        this.timeDifference = PshTimeZone.E.getTimeOffset();
        this.daylightSaving = PshTimeZone.E.isDaylightSaving();
        this.tcBasin = PshBasin.AT;
        this.lsrHeader = "";
        this.lsrWmoNode = "";
        this.useMixedCase = false;
        this.exportDir = "";
        this.exportProduct = PshExportType.NONE;
    }

    /**
     * LSR Cities directory
     */
    @DynamicSerializeElement
    @XmlElement(name = "LSRCitiesDir")
    private String lsrCitiesDir;

    /**
     * Fill the header from a list of values.
     *
     * @param headerTxt
     *            A list of values
     */
    public void fill(List<String> headerTxt) {

        if (headerTxt.size() > 0) {
            setWfoHeader(headerTxt.get(0));
        }

        if (headerTxt.size() > 1) {
            setWfoNode(headerTxt.get(1));
        }

        if (headerTxt.size() > 2) {
            setProductHeader(headerTxt.get(2));
        }

        if (headerTxt.size() > 3) {
            setProductStation(headerTxt.get(3));
        }

        if (headerTxt.size() > 4) {
            setProductPil(headerTxt.get(4));
        }

        if (headerTxt.size() > 5) {
            setPilFile(headerTxt.get(5));
        }

        if (headerTxt.size() > 6) {
            setGuiDir(headerTxt.get(6));
        }

        if (headerTxt.size() > 7) {
            setLsrCitiesDir(headerTxt.get(7));
        }

        // Time offset and day light saving should depend on time zone.
        if (headerTxt.size() > 8) {
            String tmzn = headerTxt.get(8).trim();
            for (PshTimeZone tz : PshTimeZone.values()) {
                if (tz.toString().equals(tmzn.toUpperCase())) {
                    setTimeZone(tz);
                    setTimeDifference(tz.getTimeOffset());
                    setDaylightSaving(tz.isDaylightSaving());
                    break;
                }
            }
        }

        if (headerTxt.size() > 11) {
            String basin = headerTxt.get(11).trim();
            for (PshBasin bn : PshBasin.values()) {
                if (bn.toString().equals(basin.toUpperCase())) {
                    setTcBasin(bn);
                    break;
                }
            }
        }

        if (headerTxt.size() > 12) {
            setLsrHeader(headerTxt.get(12));
        }

        if (headerTxt.size() > 13) {
            setLsrWmoNode(headerTxt.get(13));
        }

        // New flag to format report with mixed case.
        if (headerTxt.size() > 14) {
            String td = headerTxt.get(14).trim();
            boolean mixedCase = td.length() > 0
                    && td.toUpperCase().startsWith("Y");
            setUseMixedCase(mixedCase);
        }

    }

}