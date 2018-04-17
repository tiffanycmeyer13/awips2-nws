/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.metartoclimate.dao.data;

/**
 * Decoded METAR surface observations data quality descriptor data. From
 * QC_METAR.h.
 * 
 * <pre>
 * From QC.h:
 * 
define NO_QC_PERFORMED 'Z'  No quality control was performed on the datum. 
define COARSE_CHECKS_PASSED 'C'  The datum has passed coarse checks. 
define FAILED_VALIDITY_CHECK 'X'  The datum failed the validity check. 
define DECODER_ERROR 'D'  An error was encountered while decoding 
                             the datum. 
define INCOMPLETE_VALUE 'I'  For a cumulative element, there were 
                                some missing component observations. 
define QUESTIONABLE_VALUE 'Q'  A value is questionable. 
define MANUALLY_EDITED 'W'  A value has been manually edited.
 * </pre>
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 08 FEB 2017  28609      amoore      Initial creation
 * 21 FEB 2017  28609      amoore      Default values.
 * </pre>
 *
 * @author amoore
 * @version 1.0
 */
public class QCMetar {

    /**
     * No QC performed value.
     */
    public static final String NO_QC_PERFORMED = "Z";

    /**
     * Coarse checks passed value.
     */
    public static final String COARSE_CHECKS_PASSED = "C";

    /**
     * Failed validity check value.
     */
    public static final String FAILED_VALIDITY_CHECK = "X";

    /**
     * Decoder error value.
     */
    public static final String DECODER_ERROR = "D";

    /**
     * Incomplete value.
     */
    public static final String INCOMPLETE_VALUE = "I";

    /**
     * Questionable value.
     */
    public static final String QUESTIONABLE_VALUE = "Q";

    /**
     * Manually edited value.
     */
    public static final String MANUALLY_EDITED_VALUE = "W";

    private String lowCloudHgtDqd = NO_QC_PERFORMED;

    private String lowCloudCoverDqd = NO_QC_PERFORMED;

    private String lowCloudTypeDqd = NO_QC_PERFORMED;

    private String midCloudHgtDqd = NO_QC_PERFORMED;

    private String midCloudCoverDqd = NO_QC_PERFORMED;

    private String midCloudTypeDqd = NO_QC_PERFORMED;

    private String highCloudHgtDqd = NO_QC_PERFORMED;

    private String highCloudCoverDqd = NO_QC_PERFORMED;

    private String highCloudTypeDqd = NO_QC_PERFORMED;

    private String layer4CloudHgtDqd = NO_QC_PERFORMED;

    private String layer4CloudCoverDqd = NO_QC_PERFORMED;

    private String layer4CloudTypeDqd = NO_QC_PERFORMED;

    private String layer5CloudHgtDqd = NO_QC_PERFORMED;

    private String layer5CloudCoverDqd = NO_QC_PERFORMED;

    private String layer5CloudTypeDqd = NO_QC_PERFORMED;

    private String layer6CloudHgtDqd = NO_QC_PERFORMED;

    private String layer6CloudCoverDqd = NO_QC_PERFORMED;

    private String layer6CloudTypeDqd = NO_QC_PERFORMED;

    private String presentWxDqd = NO_QC_PERFORMED;

    private String vsbyDqd = NO_QC_PERFORMED;

    private String vertVsbyDqd = NO_QC_PERFORMED;

    private String altSettingDqd = NO_QC_PERFORMED;

    private String SLPDqd = NO_QC_PERFORMED;

    private String presChg3hrDqd = NO_QC_PERFORMED;

    private String presTendDqd = NO_QC_PERFORMED;

    private String tempDqd = NO_QC_PERFORMED;

    private String dewPtDqd = NO_QC_PERFORMED;

    private String temp2TenthsDqd = NO_QC_PERFORMED;

    private String dewPt2TenthsDqd = NO_QC_PERFORMED;

    private String maxTemp6hrDqd = NO_QC_PERFORMED;

    private String minTemp6hrDqd = NO_QC_PERFORMED;

    private String maxTemp24hrDqd = NO_QC_PERFORMED;

    private String minTemp24hrDqd = NO_QC_PERFORMED;

    private String peakWindSpdDqd = NO_QC_PERFORMED;

    private String peakWindDirDqd = NO_QC_PERFORMED;

    private String peakWindTimeDqd = NO_QC_PERFORMED;

    private String sunshineDurDqd = NO_QC_PERFORMED;

    private String precip1hrDqd = NO_QC_PERFORMED;

    private String precip3hrDqd = NO_QC_PERFORMED;

    private String precip6hrDqd = NO_QC_PERFORMED;

    private String precip24hrDqd = NO_QC_PERFORMED;

    private String snowDepthDqd = NO_QC_PERFORMED;

    private String windDirDqd = NO_QC_PERFORMED;

    private String windSpdDqd = NO_QC_PERFORMED;

    private String gustSpdDqd = NO_QC_PERFORMED;

    /**
     * Empty constructor.
     */
    public QCMetar() {
    }

    /**
     * @return the lowCloudHgtDqd
     */
    public String getLowCloudHgtDqd() {
        return lowCloudHgtDqd;
    }

    /**
     * @param lowCloudHgtDqd
     *            the lowCloudHgtDqd to set
     */
    public void setLowCloudHgtDqd(String lowCloudHgtDqd) {
        this.lowCloudHgtDqd = lowCloudHgtDqd;
    }

    /**
     * @return the lowCloudCoverDqd
     */
    public String getLowCloudCoverDqd() {
        return lowCloudCoverDqd;
    }

    /**
     * @param lowCloudCoverDqd
     *            the lowCloudCoverDqd to set
     */
    public void setLowCloudCoverDqd(String lowCloudCoverDqd) {
        this.lowCloudCoverDqd = lowCloudCoverDqd;
    }

    /**
     * @return the lowCloudTypeDqd
     */
    public String getLowCloudTypeDqd() {
        return lowCloudTypeDqd;
    }

    /**
     * @param lowCloudTypeDqd
     *            the lowCloudTypeDqd to set
     */
    public void setLowCloudTypeDqd(String lowCloudTypeDqd) {
        this.lowCloudTypeDqd = lowCloudTypeDqd;
    }

    /**
     * @return the midCloudHgtDqd
     */
    public String getMidCloudHgtDqd() {
        return midCloudHgtDqd;
    }

    /**
     * @param midCloudHgtDqd
     *            the midCloudHgtDqd to set
     */
    public void setMidCloudHgtDqd(String midCloudHgtDqd) {
        this.midCloudHgtDqd = midCloudHgtDqd;
    }

    /**
     * @return the midCloudCoverDqd
     */
    public String getMidCloudCoverDqd() {
        return midCloudCoverDqd;
    }

    /**
     * @param midCloudCoverDqd
     *            the midCloudCoverDqd to set
     */
    public void setMidCloudCoverDqd(String midCloudCoverDqd) {
        this.midCloudCoverDqd = midCloudCoverDqd;
    }

    /**
     * @return the midCloudTypeDqd
     */
    public String getMidCloudTypeDqd() {
        return midCloudTypeDqd;
    }

    /**
     * @param midCloudTypeDqd
     *            the midCloudTypeDqd to set
     */
    public void setMidCloudTypeDqd(String midCloudTypeDqd) {
        this.midCloudTypeDqd = midCloudTypeDqd;
    }

    /**
     * @return the highCloudHgtDqd
     */
    public String getHighCloudHgtDqd() {
        return highCloudHgtDqd;
    }

    /**
     * @param highCloudHgtDqd
     *            the highCloudHgtDqd to set
     */
    public void setHighCloudHgtDqd(String highCloudHgtDqd) {
        this.highCloudHgtDqd = highCloudHgtDqd;
    }

    /**
     * @return the highCloudCoverDqd
     */
    public String getHighCloudCoverDqd() {
        return highCloudCoverDqd;
    }

    /**
     * @param highCloudCoverDqd
     *            the highCloudCoverDqd to set
     */
    public void setHighCloudCoverDqd(String highCloudCoverDqd) {
        this.highCloudCoverDqd = highCloudCoverDqd;
    }

    /**
     * @return the highCloudTypeDqd
     */
    public String getHighCloudTypeDqd() {
        return highCloudTypeDqd;
    }

    /**
     * @param highCloudTypeDqd
     *            the highCloudTypeDqd to set
     */
    public void setHighCloudTypeDqd(String highCloudTypeDqd) {
        this.highCloudTypeDqd = highCloudTypeDqd;
    }

    /**
     * @return the layer4CloudHgtDqd
     */
    public String getLayer4CloudHgtDqd() {
        return layer4CloudHgtDqd;
    }

    /**
     * @param layer4CloudHgtDqd
     *            the layer4CloudHgtDqd to set
     */
    public void setLayer4CloudHgtDqd(String layer4CloudHgtDqd) {
        this.layer4CloudHgtDqd = layer4CloudHgtDqd;
    }

    /**
     * @return the layer4CloudCoverDqd
     */
    public String getLayer4CloudCoverDqd() {
        return layer4CloudCoverDqd;
    }

    /**
     * @param layer4CloudCoverDqd
     *            the layer4CloudCoverDqd to set
     */
    public void setLayer4CloudCoverDqd(String layer4CloudCoverDqd) {
        this.layer4CloudCoverDqd = layer4CloudCoverDqd;
    }

    /**
     * @return the layer4CloudTypeDqd
     */
    public String getLayer4CloudTypeDqd() {
        return layer4CloudTypeDqd;
    }

    /**
     * @param layer4CloudTypeDqd
     *            the layer4CloudTypeDqd to set
     */
    public void setLayer4CloudTypeDqd(String layer4CloudTypeDqd) {
        this.layer4CloudTypeDqd = layer4CloudTypeDqd;
    }

    /**
     * @return the layer5CloudHgtDqd
     */
    public String getLayer5CloudHgtDqd() {
        return layer5CloudHgtDqd;
    }

    /**
     * @param layer5CloudHgtDqd
     *            the layer5CloudHgtDqd to set
     */
    public void setLayer5CloudHgtDqd(String layer5CloudHgtDqd) {
        this.layer5CloudHgtDqd = layer5CloudHgtDqd;
    }

    /**
     * @return the layer5CloudCoverDqd
     */
    public String getLayer5CloudCoverDqd() {
        return layer5CloudCoverDqd;
    }

    /**
     * @param layer5CloudCoverDqd
     *            the layer5CloudCoverDqd to set
     */
    public void setLayer5CloudCoverDqd(String layer5CloudCoverDqd) {
        this.layer5CloudCoverDqd = layer5CloudCoverDqd;
    }

    /**
     * @return the layer5CloudTypeDqd
     */
    public String getLayer5CloudTypeDqd() {
        return layer5CloudTypeDqd;
    }

    /**
     * @param layer5CloudTypeDqd
     *            the layer5CloudTypeDqd to set
     */
    public void setLayer5CloudTypeDqd(String layer5CloudTypeDqd) {
        this.layer5CloudTypeDqd = layer5CloudTypeDqd;
    }

    /**
     * @return the layer6CloudHgtDqd
     */
    public String getLayer6CloudHgtDqd() {
        return layer6CloudHgtDqd;
    }

    /**
     * @param layer6CloudHgtDqd
     *            the layer6CloudHgtDqd to set
     */
    public void setLayer6CloudHgtDqd(String layer6CloudHgtDqd) {
        this.layer6CloudHgtDqd = layer6CloudHgtDqd;
    }

    /**
     * @return the layer6CloudCoverDqd
     */
    public String getLayer6CloudCoverDqd() {
        return layer6CloudCoverDqd;
    }

    /**
     * @param layer6CloudCoverDqd
     *            the layer6CloudCoverDqd to set
     */
    public void setLayer6CloudCoverDqd(String layer6CloudCoverDqd) {
        this.layer6CloudCoverDqd = layer6CloudCoverDqd;
    }

    /**
     * @return the layer6CloudTypeDqd
     */
    public String getLayer6CloudTypeDqd() {
        return layer6CloudTypeDqd;
    }

    /**
     * @param layer6CloudTypeDqd
     *            the layer6CloudTypeDqd to set
     */
    public void setLayer6CloudTypeDqd(String layer6CloudTypeDqd) {
        this.layer6CloudTypeDqd = layer6CloudTypeDqd;
    }

    /**
     * @return the presentWxDqd
     */
    public String getPresentWxDqd() {
        return presentWxDqd;
    }

    /**
     * @param presentWxDqd
     *            the presentWxDqd to set
     */
    public void setPresentWxDqd(String presentWxDqd) {
        this.presentWxDqd = presentWxDqd;
    }

    /**
     * @return the vsbyDqd
     */
    public String getVsbyDqd() {
        return vsbyDqd;
    }

    /**
     * @param vsbyDqd
     *            the vsbyDqd to set
     */
    public void setVsbyDqd(String vsbyDqd) {
        this.vsbyDqd = vsbyDqd;
    }

    /**
     * @return the vertVsbyDqd
     */
    public String getVertVsbyDqd() {
        return vertVsbyDqd;
    }

    /**
     * @param vertVsbyDqd
     *            the vertVsbyDqd to set
     */
    public void setVertVsbyDqd(String vertVsbyDqd) {
        this.vertVsbyDqd = vertVsbyDqd;
    }

    /**
     * @return the altSettingDqd
     */
    public String getAltSettingDqd() {
        return altSettingDqd;
    }

    /**
     * @param altSettingDqd
     *            the altSettingDqd to set
     */
    public void setAltSettingDqd(String altSettingDqd) {
        this.altSettingDqd = altSettingDqd;
    }

    /**
     * @return the sLPDqd
     */
    public String getSLPDqd() {
        return SLPDqd;
    }

    /**
     * @param sLPDqd
     *            the sLPDqd to set
     */
    public void setSLPDqd(String sLPDqd) {
        SLPDqd = sLPDqd;
    }

    /**
     * @return the presChg3hrDqd
     */
    public String getPresChg3hrDqd() {
        return presChg3hrDqd;
    }

    /**
     * @param presChg3hrDqd
     *            the presChg3hrDqd to set
     */
    public void setPresChg3hrDqd(String presChg3hrDqd) {
        this.presChg3hrDqd = presChg3hrDqd;
    }

    /**
     * @return the presTendDqd
     */
    public String getPresTendDqd() {
        return presTendDqd;
    }

    /**
     * @param presTendDqd
     *            the presTendDqd to set
     */
    public void setPresTendDqd(String presTendDqd) {
        this.presTendDqd = presTendDqd;
    }

    /**
     * @return the tempDqd
     */
    public String getTempDqd() {
        return tempDqd;
    }

    /**
     * @param tempDqd
     *            the tempDqd to set
     */
    public void setTempDqd(String tempDqd) {
        this.tempDqd = tempDqd;
    }

    /**
     * @return the dewPtDqd
     */
    public String getDewPtDqd() {
        return dewPtDqd;
    }

    /**
     * @param dewPtDqd
     *            the dewPtDqd to set
     */
    public void setDewPtDqd(String dewPtDqd) {
        this.dewPtDqd = dewPtDqd;
    }

    /**
     * @return the temp2TenthsDqd
     */
    public String getTemp2TenthsDqd() {
        return temp2TenthsDqd;
    }

    /**
     * @param temp2TenthsDqd
     *            the temp2TenthsDqd to set
     */
    public void setTemp2TenthsDqd(String temp2TenthsDqd) {
        this.temp2TenthsDqd = temp2TenthsDqd;
    }

    /**
     * @return the dewPt2TenthsDqd
     */
    public String getDewPt2TenthsDqd() {
        return dewPt2TenthsDqd;
    }

    /**
     * @param dewPt2TenthsDqd
     *            the dewPt2TenthsDqd to set
     */
    public void setDewPt2TenthsDqd(String dewPt2TenthsDqd) {
        this.dewPt2TenthsDqd = dewPt2TenthsDqd;
    }

    /**
     * @return the maxTemp6hrDqd
     */
    public String getMaxTemp6hrDqd() {
        return maxTemp6hrDqd;
    }

    /**
     * @param maxTemp6hrDqd
     *            the maxTemp6hrDqd to set
     */
    public void setMaxTemp6hrDqd(String maxTemp6hrDqd) {
        this.maxTemp6hrDqd = maxTemp6hrDqd;
    }

    /**
     * @return the minTemp6hrDqd
     */
    public String getMinTemp6hrDqd() {
        return minTemp6hrDqd;
    }

    /**
     * @param minTemp6hrDqd
     *            the minTemp6hrDqd to set
     */
    public void setMinTemp6hrDqd(String minTemp6hrDqd) {
        this.minTemp6hrDqd = minTemp6hrDqd;
    }

    /**
     * @return the maxTemp24hrDqd
     */
    public String getMaxTemp24hrDqd() {
        return maxTemp24hrDqd;
    }

    /**
     * @param maxTemp24hrDqd
     *            the maxTemp24hrDqd to set
     */
    public void setMaxTemp24hrDqd(String maxTemp24hrDqd) {
        this.maxTemp24hrDqd = maxTemp24hrDqd;
    }

    /**
     * @return the minTemp24hrDqd
     */
    public String getMinTemp24hrDqd() {
        return minTemp24hrDqd;
    }

    /**
     * @param minTemp24hrDqd
     *            the minTemp24hrDqd to set
     */
    public void setMinTemp24hrDqd(String minTemp24hrDqd) {
        this.minTemp24hrDqd = minTemp24hrDqd;
    }

    /**
     * @return the peakWindSpdDqd
     */
    public String getPeakWindSpdDqd() {
        return peakWindSpdDqd;
    }

    /**
     * @param peakWindSpdDqd
     *            the peakWindSpdDqd to set
     */
    public void setPeakWindSpdDqd(String peakWindSpdDqd) {
        this.peakWindSpdDqd = peakWindSpdDqd;
    }

    /**
     * @return the peakWindDirDqd
     */
    public String getPeakWindDirDqd() {
        return peakWindDirDqd;
    }

    /**
     * @param peakWindDirDqd
     *            the peakWindDirDqd to set
     */
    public void setPeakWindDirDqd(String peakWindDirDqd) {
        this.peakWindDirDqd = peakWindDirDqd;
    }

    /**
     * @return the peakWindTimeDqd
     */
    public String getPeakWindTimeDqd() {
        return peakWindTimeDqd;
    }

    /**
     * @param peakWindTimeDqd
     *            the peakWindTimeDqd to set
     */
    public void setPeakWindTimeDqd(String peakWindTimeDqd) {
        this.peakWindTimeDqd = peakWindTimeDqd;
    }

    /**
     * @return the sunshineDurDqd
     */
    public String getSunshineDurDqd() {
        return sunshineDurDqd;
    }

    /**
     * @param sunshineDurDqd
     *            the sunshineDurDqd to set
     */
    public void setSunshineDurDqd(String sunshineDurDqd) {
        this.sunshineDurDqd = sunshineDurDqd;
    }

    /**
     * @return the precip1hrDqd
     */
    public String getPrecip1hrDqd() {
        return precip1hrDqd;
    }

    /**
     * @param precip1hrDqd
     *            the precip1hrDqd to set
     */
    public void setPrecip1hrDqd(String precip1hrDqd) {
        this.precip1hrDqd = precip1hrDqd;
    }

    /**
     * @return the precip3hrDqd
     */
    public String getPrecip3hrDqd() {
        return precip3hrDqd;
    }

    /**
     * @param precip3hrDqd
     *            the precip3hrDqd to set
     */
    public void setPrecip3hrDqd(String precip3hrDqd) {
        this.precip3hrDqd = precip3hrDqd;
    }

    /**
     * @return the precip6hrDqd
     */
    public String getPrecip6hrDqd() {
        return precip6hrDqd;
    }

    /**
     * @param precip6hrDqd
     *            the precip6hrDqd to set
     */
    public void setPrecip6hrDqd(String precip6hrDqd) {
        this.precip6hrDqd = precip6hrDqd;
    }

    /**
     * @return the precip24hrDqd
     */
    public String getPrecip24hrDqd() {
        return precip24hrDqd;
    }

    /**
     * @param precip24hrDqd
     *            the precip24hrDqd to set
     */
    public void setPrecip24hrDqd(String precip24hrDqd) {
        this.precip24hrDqd = precip24hrDqd;
    }

    /**
     * @return the snowDepthDqd
     */
    public String getSnowDepthDqd() {
        return snowDepthDqd;
    }

    /**
     * @param snowDepthDqd
     *            the snowDepthDqd to set
     */
    public void setSnowDepthDqd(String snowDepthDqd) {
        this.snowDepthDqd = snowDepthDqd;
    }

    /**
     * @return the windDirDqd
     */
    public String getWindDirDqd() {
        return windDirDqd;
    }

    /**
     * @param windDirDqd
     *            the windDirDqd to set
     */
    public void setWindDirDqd(String windDirDqd) {
        this.windDirDqd = windDirDqd;
    }

    /**
     * @return the windSpdDqd
     */
    public String getWindSpdDqd() {
        return windSpdDqd;
    }

    /**
     * @param windSpdDqd
     *            the windSpdDqd to set
     */
    public void setWindSpdDqd(String windSpdDqd) {
        this.windSpdDqd = windSpdDqd;
    }

    /**
     * @return the gustSpdDqd
     */
    public String getGustSpdDqd() {
        return gustSpdDqd;
    }

    /**
     * @param gustSpdDqd
     *            the gustSpdDqd to set
     */
    public void setGustSpdDqd(String gustSpdDqd) {
        this.gustSpdDqd = gustSpdDqd;
    }
}
