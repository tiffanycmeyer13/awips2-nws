/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.localization.climate.producttype;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

import gov.noaa.nws.ocp.common.dataplugin.climate.PeriodType;
import gov.noaa.nws.ocp.common.dataplugin.climate.Station;

/**
 * Climate product type.
 * 
 * The top-level class to hold all configuration parameters for a climate
 * product type. In legacy, a product type were consisted of three files (1) a
 * header file (e.g. "header_am_BWI"), (2) a control file (e.g.
 * "control_am_BWI"), and (3) a station file (e.g. "stations_am_BWI"). Now each
 * climate product type will have only one XML file, which combines contents in
 * three legacy files (e.g., "product_am_BWI_NWWS.xml"). A few attributes used
 * to identify itself (report period, report source, report station,, itype, and
 * file name), followed by and a list of stations, a product header, a product
 * control.
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer     Description
 * ----------- ----------- ------------ ----------------------
 * Nov 22, 2016 20640      jwu          Initial creation
 * Feb 16, 2017 21099      wpaintsil    Serialization
 * </pre>
 *
 * @author jwu
 * @version 1.0
 */
@DynamicSerialize
@XmlRootElement(name = "ClimateProductType")
@XmlAccessorType(XmlAccessType.NONE)
public class ClimateProductType {

    /**
     * Report type (including source - NWR or NWWS, and period (am, im, pm, mon,
     * sea, ann)
     */
    @DynamicSerializeElement
    @XmlAttribute(name = "ReportType")
    private PeriodType reportType;

    /**
     * Product ID (the three letter code for the station that is being
     * broadcasted).
     */
    @DynamicSerializeElement
    @XmlAttribute(name = "ProdID")
    private String prodId;

    /**
     * Itype (a unique number used in legacy to identify each product type - see
     * PeriodType).
     */
    @DynamicSerializeElement
    @XmlAttribute(name = "Itype")
    private int itype;

    /**
     * File name (the name of XML file this product type is saved, normally in
     * pattern of "product_[period]_[prodID]_[source].xml.
     */
    @DynamicSerializeElement
    @XmlAttribute(name = "FileName")
    private String fileName;

    /**
     * Product header info.
     */
    @DynamicSerializeElement
    @XmlElement(name = "Header")
    private ClimateProductHeader header;

    /**
     * Product control flags.
     */
    @DynamicSerializeElement
    @XmlElement(name = "Control")
    private ClimateProductControl control;

    /**
     * Selected stations for the product.
     */
    @DynamicSerializeElement
    @XmlElements({ @XmlElement(name = "Station", type = Station.class) })
    private List<Station> stations;

    /**
     * @return the reportType
     */
    public PeriodType getReportType() {
        return reportType;
    }

    /**
     * @param reportType
     *            the reportType to set
     */
    public void setReportType(PeriodType reportType) {
        this.reportType = reportType;
    }

    /**
     * @return the prodId
     */
    public String getProdId() {
        return prodId;
    }

    /**
     * @param prodId
     *            the prodId to set
     */
    public void setProdId(String prodId) {
        this.prodId = prodId;
    }

    /**
     * @return the itype
     */
    public int getItype() {
        return itype;
    }

    /**
     * @param itype
     *            the itype to set
     */
    public void setItype(int itype) {
        this.itype = itype;
    }

    /**
     * @return the fileName
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * @param fileName
     *            the fileName to set
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /**
     * @return the stations
     */
    public List<Station> getStations() {
        return stations;
    }

    /**
     * @param stations
     *            the stations to set
     */
    public void setStations(List<Station> stations) {
        this.stations = stations;
    }

    /**
     * @return the header
     */
    public ClimateProductHeader getHeader() {
        return header;
    }

    /**
     * @param header
     *            the header to set
     */
    public void setHeader(ClimateProductHeader header) {
        this.header = header;
    }

    /**
     * @return the control
     */
    public ClimateProductControl getControl() {
        return control;
    }

    /**
     * @param control
     *            the control to set
     */
    public void setControl(ClimateProductControl control) {
        this.control = control;
    }

    /**
     * Get the name of this product type, which is used to identity a unique
     * product type.
     * 
     * @return getName() a string
     */
    public String getName() {
        return buildName(reportType.getPeriodName(), reportType.getSource(),
                prodId);
    }

    /**
     * Build a name from, in form of:
     * 
     * "[rtype] _[prodtId]_[source]".
     * 
     * @param rtype
     *            report period type (am, im, pm, mon, sea, ann)
     * @param source
     *            report source (NWR or NWWS)
     * @param prodId
     *            report id (station ID which is being broadcasted)
     * 
     * @return getName() a string
     */
    public static String buildName(String rtype, String source, String prodId) {
        return rtype + "_" + prodId + "_" + source;
    }

    /**
     * Build a preferred file name from, in form of:
     * 
     * "product_[rtype] _[prodtId]_[source].xml".
     * 
     * @return getName() a string
     */
    public String getPreferedFileName() {
        return buildPreferedName(reportType.getPeriodName(),
                reportType.getSource(), prodId);
    }

    /**
     * Build a preferred file name from, in form of:
     * 
     * "product_[rtype] _[prodtId]_[source].xml".
     * 
     * @param rtype
     *            report period type (am, im, pm, mon, sea, ann)
     * @param source
     *            report source (NWR or NWWS)
     * @param prodId
     *            report id (station ID which is being broadcasted)
     * 
     * @return getName() a string
     */
    public static String buildPreferedName(String rtype, String source,
            String prodId) {
        return "product_" + rtype + "_" + prodId + "_" + source + ".xml";
    }

}
