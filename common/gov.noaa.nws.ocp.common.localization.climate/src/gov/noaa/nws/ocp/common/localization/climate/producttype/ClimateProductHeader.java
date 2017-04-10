package gov.noaa.nws.ocp.common.localization.climate.producttype;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateDate;
import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateTime;

/**
 * Climate product header (a component for climate product type).
 * 
 * This class holds the same information as in legacy files such as
 * "header_am_BWI".
 *
 * <pre>
 *        Header information for a climate product type. Derived from TYPE_NWR_setup.h and 
 *        TYPE_NWWS_setup.h
 *
 *   TYPE_NWR_setup.h
 *
 *   DESCRIPTION
 *
 *        NWR_NWRSETUP
 *
 *           NWR_NWRSETUP%AIS - ACTIVE/INACTIVE STORAGE UPON OCCURRENCE OF 
 *                              EXPIRATION (ACTIVE=A, INACTIVE=I).
 *           NWR_NWRSETUP%ATM - ALERT TONE TO SEND FRONT OF THE MESSAGE 
 *                              (" "=NO ALERT TONE BUT NWSAME TONE,"N" NO ALERT 
 *                              TONE AND NO NWRSAME TONE, "A"=ALERT TONE AND 
 *                              NWRSAME TONE).
 *           NWR_NWRSETUP%BMI - BEGIN MESSAGE INDICATOR.
 *           NWR_NWRSETUP%CCC - IN AFOS PRODUCT IDENTIFIER (CCCNNNXXX), THE 
 *                              CCC IS THE NODE ORIGINATION SITE.
 *           NWR_NWRSETUP%DSM - DELETE/SAVE MESSAGE (DELETE-D, SAVE-S).
 *       NWR_NWRSETUP%EXP_MIN - THIS WILL HOLD THE MINUTES UNTIL THE EXPIRATION
 *                              TIME IS ACTIAVADE.
 *           NWR_NWRSETUP%FTN - FIRST TWO SPECIFIC PRODUCT DESIGNATORS OF NNN
 *                              IN AFOS PRODUCT IDENTIFIER (CCCNNNXXX) ARE THE
 *                              CLIMATE IDENTIFIERS.
 *                              1 - CL = THIS STANDS FOR THE CLIMATE.
 *           NWR_NWRSETUP%IFM - INTERRUPT FLAG FOR THE MESSAGE (I=INTERRUPT, 
 *                              (SPACE)=NO INTERRUPT).
 *           NWR_NWRSETUP%LAC - THE LISTENING AREA CODE FIELD.
 *           NWR_NWRSETUP%NTM - THE # OF THE CURRENT LISTENING TOWERS (1 - 10).
 *           NWR_NWRSETUP%MCO - MESSAGE CONFIRMATION ON OR OFF 
 *                              (ON="C", OFF=(SPACE)).
 *           NWR_NWRSETUP%MRD - MESSAGE REFERENCE DESCRIPTOR (always 0)
 *           NWR_NWRSETUP%NNN - IN AFOS PRODUCT IDENTIFIER (CCCNNNXXX), THE NNN IS
 *                        THE PRODUCT CATEGORY.
 *     NWR_NWRSETUP%STATIONID - THE THREE LETTER CODE FOR THE STATION THAT IS 
 *                              BEING BROADCASTED.
 *           NWR_NWRSETUP%THN - THIRD SPECIFIC PRODUCT DESIGNATOR OF NNN IN 
 *                              AFOS PRODUCT IDENTIFIER (CCCNNNXXX) IS THE  
 *                              IDENTIFER FOR WHICH HEADER PRODUCT YOUR GOING TO
 *                              USE.
 *                              1 - M = CLIMATE MONTHLY
 *                              2 - I = CLIMATE DAILY
 *                              3 - S = CLIMATE SEASON
 *           NWR_NWRSETUP%VTT - MESSAGE FORMAT (V_ENG=ENGLISH VOICE, 
 *                              V_SPA=SPANISH VOICE, T_ENG=ENGLISH TEXT, 
 *                              T_SPA=SPANISH TEXT).
 *
 *   TYPE_NWR_setup.h
 *
 *        NWWS_Setup
 *            CCC - IN AFOS PRODUCT IDENTIFIER (CCCNNNXXX), THE CCC IS
 *                        THE NODE ORIGINATION SITE.
 *            NNN - IN AFOS PRODUCT IDENTIFIER (CCCNNNXXX), THE NNN IS
 *                        THE PRODUCT CATEGORY.
 *            xxx - THE THREE LETTER CODE FOR THE STATION THAT IS BEING BROADCASTED.
 *
 *            AAA - Address?
 *
 * Note:
 *     1. ISTATIONID in TYPE_NWR_setup is the same as "xxx" in NWWS_Setup (product ID)?
 *     2. Use more meaningful names for these attributes.
 *     3. "AIS" - current samples in adapt/climate/data use "C"?
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer     Description
 * ----------- ----------- ------------ ----------------------
 * Nov 30, 2016 20640      jwu          Initial creation
 * 13 APR  2017 33104      amoore       Address comments from review.
 * 11 MAY  2017 33104      amoore       Remove unneeded String copy.
 * </pre>
 *
 * @author jwu
 * @version 1.0
 */
@DynamicSerialize
@XmlRootElement(name = "ClimateProductHeader")
@XmlAccessorType(XmlAccessType.NONE)
public class ClimateProductHeader {

    /**
     * The following 5 elements appear in both NWR/NWWS headers
     */

    /**
     * Product number.
     */
    @DynamicSerializeElement
    @XmlElement(name = "ProdNum")
    private int prodNum;

    /**
     * Style
     */
    @DynamicSerializeElement
    @XmlElement(name = "Style")
    private int style;

    /**
     * CCC: Node origination site in AFOS product identifier
     */
    @DynamicSerializeElement
    @XmlElement(name = "NodeOriginationSite")
    private String nodeOrigSite;

    /**
     * NNN: product category ( FTN + THN)
     */
    @DynamicSerializeElement
    @XmlElement(name = "ProductCategory")
    private String productCategory;

    /**
     * NWR's ISTATIONID or NWWS' xxx:
     */
    @DynamicSerializeElement
    @XmlElement(name = "StationId")
    private String stationId;

    /**
     * The following elements appear in only NWR headers
     */
    @DynamicSerializeElement
    @XmlElement(name = "ExpirationMinute")
    private int expirationMin;

    /**
     * AIS: (Active = A, Inactive = I)
     */
    @DynamicSerializeElement
    @XmlElement(name = "ActiveStorage")
    private String activeStorage;

    /**
     * ATM: " " = No alert tone but NWSame tone "N" = No alert tone & no NWSame
     * tone "A" = Alert tone & NWSame tone
     */
    @DynamicSerializeElement
    @XmlElement(name = "AlertTone")
    private String alertTone;

    /**
     * BMI - Beginning Message indicator
     */
    @DynamicSerializeElement
    @XmlElement(name = "BeginMessageIndicator")
    private String beginMsgIndicator;

    /**
     * DSM: (Delete - D, Save - S)
     */
    @DynamicSerializeElement
    @XmlElement(name = "DeleteSaveMessage")
    private String delSaveMsg;

    /**
     * FTN: first two letters in NNN 1 - CL for Climate
     */
    @DynamicSerializeElement
    @XmlElement(name = "FirstTwoNs")
    private String firstTwoN;

    /**
     * THN: third letter in NNN 1 - M = Climate monthly 2 - I = Climate daily 3
     * - S = Climate season
     */
    @DynamicSerializeElement
    @XmlElement(name = "ThirdN")
    private String thirdN;

    /**
     * FN: Flag to interrupt message I = Interrupt (SPACE) = No Interrupt
     */
    @DynamicSerializeElement
    @XmlElement(name = "InterruptMessage")
    private String interruptMsg;

    /**
     * MCO: Message Confirmation on or off ON = "C", OFF = (SPACE)
     */
    @DynamicSerializeElement
    @XmlElement(name = "MessageConfirm")
    private String messageConfirm;

    /**
     * MRD: Message reference descriptor (always 0)
     */
    @DynamicSerializeElement
    @XmlElement(name = "MessageRef")
    private String msgReference;

    /**
     * NTM: # of current listening towers (1-10).
     */
    @DynamicSerializeElement
    @XmlElement(name = "NumListeningTowers")
    private String numListenTowers;

    /**
     * VTT: MESSAGE FORMAT V_ENG = English voice V_SPA = Spanish voice T_ENG =
     * English text T_SPA = Spanish text
     */
    @DynamicSerializeElement
    @XmlElement(name = "MessageFormat")
    private String messageFormat;

    /**
     * LAC: listening area code
     */
    @DynamicSerializeElement
    @XmlElement(name = "ListenAreaCode")
    private String listenAreaCode;

    /* creation date */
    @DynamicSerializeElement
    @XmlElement(name = "CreationDate")
    private ClimateDate creationDate;

    /**
     * creation time
     */
    @DynamicSerializeElement
    @XmlElement(name = "CreationTime")
    private ClimateTime creationTime;

    /**
     * Start date of the broadcast
     */
    @DynamicSerializeElement
    @XmlElement(name = "EffectiveDate")
    private ClimateDate effectiveDate;

    /**
     * Start time of the broadcast
     */
    @DynamicSerializeElement
    @XmlElement(name = "EffectiveTime")
    private ClimateTime effectiveTime;

    /**
     * End date of the broadcast
     */
    @DynamicSerializeElement
    @XmlElement(name = "ExpirationDate")
    private ClimateDate expirationDate;

    /**
     * End time of the broadcast
     */
    @DynamicSerializeElement
    @XmlElement(name = "ExpirationTime")
    private ClimateTime expirationTime;

    /**
     * Periodicity (in unit of minutes).
     */
    @DynamicSerializeElement
    @XmlElement(name = "Periodicity")
    private int periodicity; /* How often the file gets broadcast. */

    /**
     * Note: The following elements appears in NWWS only. But looks like
     * CDE4_Date/CDE4_time are the same as creationDate/createTime. We keep it
     * here for now.
     */

    /**
     * Address for NWWS.
     */
    @DynamicSerializeElement
    @XmlElement(name = "Address")
    private String address;

    @DynamicSerializeElement
    @XmlElement(name = "CDE1")
    private String CDE1; //

    @DynamicSerializeElement
    @XmlElement(name = "CDE2")
    private String CDE2; //

    @DynamicSerializeElement
    @XmlElement(name = "CDE3")
    private String CDE3; //

    /**
     * Creation date for nwws header
     */
    @DynamicSerializeElement
    @XmlElement(name = "CDE4Date")
    private ClimateDate CDE4_Date;

    /**
     * Creation time for nwws header
     */
    @DynamicSerializeElement
    @XmlElement(name = "CDE4_Time")
    private ClimateTime CDE4_Time;

    /**
     * Empty constructor.
     */
    public ClimateProductHeader() {
    }

    /**
     * Constructor.
     * 
     * @param header
     *            ClimateProductHeader to construct from
     */
    public ClimateProductHeader(ClimateProductHeader header) {

        this.prodNum = header.prodNum;
        this.style = header.style;

        this.nodeOrigSite = header.nodeOrigSite;

        this.productCategory = "";
        this.stationId = "";
        this.expirationMin = header.expirationMin;
        this.activeStorage = header.activeStorage;
        this.alertTone = header.alertTone;
        this.beginMsgIndicator = header.beginMsgIndicator;
        this.delSaveMsg = header.delSaveMsg;
        this.firstTwoN = header.firstTwoN;
        this.thirdN = header.thirdN;
        this.interruptMsg = header.interruptMsg;
        this.messageConfirm = header.messageConfirm;
        this.msgReference = header.msgReference;
        this.numListenTowers = header.numListenTowers;
        this.messageFormat = header.messageFormat;
        this.listenAreaCode = header.listenAreaCode;
        this.creationDate = new ClimateDate(header.creationDate);
        this.creationTime = new ClimateTime(header.creationTime);
        this.effectiveDate = new ClimateDate(header.effectiveDate);
        this.effectiveTime = new ClimateTime(header.effectiveTime);
        this.expirationDate = new ClimateDate(header.expirationDate);
        this.expirationTime = new ClimateTime(header.expirationTime);
        this.periodicity = header.periodicity;
        this.address = header.address;
        this.CDE1 = header.CDE1;
        this.CDE2 = header.CDE2;
        this.CDE3 = header.CDE3;
        this.CDE4_Date = new ClimateDate(header.creationDate);
        this.CDE4_Time = new ClimateTime(header.creationTime);

    }

    /**
     * @return the prodNum
     */
    public int getProdNum() {
        return prodNum;
    }

    /**
     * @param prodNum
     *            the prodNum to set
     */
    public void setProdNum(int prodNum) {
        this.prodNum = prodNum;
    }

    /**
     * @return the style
     */
    public int getStyle() {
        return style;
    }

    /**
     * @param style
     *            the style to set
     */
    public void setStyle(int style) {
        this.style = style;
    }

    /**
     * @return the nodeOrigSite
     */
    public String getNodeOrigSite() {
        return nodeOrigSite;
    }

    /**
     * @param nodeOrigSite
     *            the nodeOrigSite to set
     */
    public void setNodeOrigSite(String nodeOrigSite) {
        this.nodeOrigSite = nodeOrigSite;
    }

    /**
     * @return the productCategory
     */
    public String getProductCategory() {
        return productCategory;
    }

    /**
     * @param productCategory
     *            the productCategory to set
     */
    public void setProductCategory(String productCategory) {
        this.productCategory = productCategory;
    }

    /**
     * @return the stationId
     */
    public String getStationId() {
        return stationId;
    }

    /**
     * @param stationId
     *            the stationId to set
     */
    public void setStationId(String stationId) {
        this.stationId = stationId;
    }

    /**
     * @return the expirationMin
     */
    public int getExpirationMin() {
        return expirationMin;
    }

    /**
     * @param expirationMin
     *            the expirationMin to set
     */
    public void setExpirationMin(int expirationMin) {
        this.expirationMin = expirationMin;
    }

    /**
     * @return the activeStorage
     */
    public String getActiveStorage() {
        return activeStorage;
    }

    /**
     * @param activeStorage
     *            the activeStorage to set
     */
    public void setActiveStorage(String activeStorage) {
        this.activeStorage = activeStorage;
    }

    /**
     * @return the alertTone
     */
    public String getAlertTone() {
        return alertTone;
    }

    /**
     * @param alertTone
     *            the alertTone to set
     */
    public void setAlertTone(String alertTone) {
        this.alertTone = alertTone;
    }

    /**
     * @return the beginMsgIndicator
     */
    public String getBeginMsgIndicator() {
        return beginMsgIndicator;
    }

    /**
     * @param beginMsgIndicator
     *            the beginMsgIndicator to set
     */
    public void setBeginMsgIndicator(String beginMsgIndicator) {
        this.beginMsgIndicator = beginMsgIndicator;
    }

    /**
     * @return the delSaveMsg
     */
    public String getDelSaveMsg() {
        return delSaveMsg;
    }

    /**
     * @param delSaveMsg
     *            the delSaveMsg to set
     */
    public void setDelSaveMsg(String delSaveMsg) {
        this.delSaveMsg = delSaveMsg;
    }

    /**
     * @return the firstTwoN
     */
    public String getFirstTwoN() {
        return firstTwoN;
    }

    /**
     * @param firstTwoN
     *            the firstTwoN to set
     */
    public void setFirstTwoN(String firstTwoN) {
        this.firstTwoN = firstTwoN;
    }

    /**
     * @return the thirdN
     */
    public String getThirdN() {
        return thirdN;
    }

    /**
     * @param thirdN
     *            the thirdN to set
     */
    public void setThirdN(String thirdN) {
        this.thirdN = thirdN;
    }

    /**
     * @return the interruptMsg
     */
    public String getInterruptMsg() {
        return interruptMsg;
    }

    /**
     * @param interruptMsg
     *            the interruptMsg to set
     */
    public void setInterruptMsg(String interruptMsg) {
        this.interruptMsg = interruptMsg;
    }

    /**
     * @return the messageConfirm
     */
    public String getMessageConfirm() {
        return messageConfirm;
    }

    /**
     * @param messageConfirm
     *            the messageConfirm to set
     */
    public void setMessageConfirm(String messageConfirm) {
        this.messageConfirm = messageConfirm;
    }

    /**
     * @return the msgReference
     */
    public String getMsgReference() {
        return msgReference;
    }

    /**
     * @param msgReference
     *            the msgReference to set
     */
    public void setMsgReference(String msgReference) {
        this.msgReference = msgReference;
    }

    /**
     * @return the numListenTowers
     */
    public String getNumListenTowers() {
        return numListenTowers;
    }

    /**
     * @param numListenTowers
     *            the numListenTowers to set
     */
    public void setNumListenTowers(String numListenTowers) {
        this.numListenTowers = numListenTowers;
    }

    /**
     * @return the messageFormat
     */
    public String getMessageFormat() {
        return messageFormat;
    }

    /**
     * @param messageFormat
     *            the messageFormat to set
     */
    public void setMessageFormat(String messageFormat) {
        this.messageFormat = messageFormat;
    }

    /**
     * @return the listenAreaCode
     */
    public String getListenAreaCode() {
        return listenAreaCode;
    }

    /**
     * @param listenAreaCode
     *            the listenAreaCode to set
     */
    public void setListenAreaCode(String listenAreaCode) {
        this.listenAreaCode = listenAreaCode;
    }

    /**
     * @return the creationDate
     */
    public ClimateDate getCreationDate() {
        return creationDate;
    }

    /**
     * @param creationDate
     *            the creationDate to set
     */
    public void setCreationDate(ClimateDate creationDate) {
        this.creationDate = creationDate;
    }

    /**
     * @return the creationTime
     */
    public ClimateTime getCreationTime() {
        return creationTime;
    }

    /**
     * @param creationTime
     *            the creationTime to set
     */
    public void setCreationTime(ClimateTime creationTime) {
        this.creationTime = creationTime;
    }

    /**
     * @return the effectiveDate
     */
    public ClimateDate getEffectiveDate() {
        return effectiveDate;
    }

    /**
     * @param effectiveDate
     *            the effectiveDate to set
     */
    public void setEffectiveDate(ClimateDate effectiveDate) {
        this.effectiveDate = effectiveDate;
    }

    /**
     * @return the effectiveTime
     */
    public ClimateTime getEffectiveTime() {
        return effectiveTime;
    }

    /**
     * @param effectiveTime
     *            the effectiveTime to set
     */
    public void setEffectiveTime(ClimateTime effectiveTime) {
        this.effectiveTime = effectiveTime;
    }

    /**
     * @return the expirationDate
     */
    public ClimateDate getExpirationDate() {
        return expirationDate;
    }

    /**
     * @param expirationDate
     *            the expirationDate to set
     */
    public void setExpirationDate(ClimateDate expirationDate) {
        this.expirationDate = expirationDate;
    }

    /**
     * @return the expirationTime
     */
    public ClimateTime getExpirationTime() {
        return expirationTime;
    }

    /**
     * @param expirationTime
     *            the expirationTime to set
     */
    public void setExpirationTime(ClimateTime expirationTime) {
        this.expirationTime = expirationTime;
    }

    /**
     * @return the periodicity
     */
    public int getPeriodicity() {
        return periodicity;
    }

    /**
     * @param periodicity
     *            the periodicity to set
     */
    public void setPeriodicity(int periodicity) {
        this.periodicity = periodicity;
    }

    /**
     * @return the address
     */
    public String getAddress() {
        return address;
    }

    /**
     * @param address
     *            the address to set
     */
    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * @return the cDE1
     */
    public String getCDE1() {
        return CDE1;
    }

    /**
     * @param cDE1
     *            the cDE1 to set
     */
    public void setCDE1(String cDE1) {
        CDE1 = cDE1;
    }

    /**
     * @return the cDE2
     */
    public String getCDE2() {
        return CDE2;
    }

    /**
     * @param cDE2
     *            the cDE2 to set
     */
    public void setCDE2(String cDE2) {
        CDE2 = cDE2;
    }

    /**
     * @return the cDE3
     */
    public String getCDE3() {
        return CDE3;
    }

    /**
     * @param cDE3
     *            the cDE3 to set
     */
    public void setCDE3(String cDE3) {
        CDE3 = cDE3;
    }

    /**
     * @return the cDE4_Date
     */
    public ClimateDate getCDE4_Date() {
        return CDE4_Date;
    }

    /**
     * @param cDE4_Date
     *            the cDE4_Date to set
     */
    public void setCDE4_Date(ClimateDate cDE4_Date) {
        CDE4_Date = cDE4_Date;
    }

    /**
     * @return the cDE4_Time
     */
    public ClimateTime getCDE4_Time() {
        return CDE4_Time;
    }

    /**
     * @param cDE4_Time
     *            the cDE4_Time to set
     */
    public void setCDE4_Time(ClimateTime cDE4_Time) {
        CDE4_Time = cDE4_Time;
    }

    /**
     * @return a new object filled with default values.
     */
    public static ClimateProductHeader getDefaultHeader() {
        ClimateProductHeader header = new ClimateProductHeader();
        header.setDataToDefault();
        return header;
    }

    /**
     * Set data to default values.
     * 
     * Note: these default values are from file:
     *
     * /rehost-adapt/climate/data/header_default
     *
     * <pre>
     *  prod_num  style
     *      753     45
     *   ais atm bmi CCC DSM ftn thn IFM sid mco mrd xxx ntm   vtt
     *    A
     *        LAC
     *
     *  exp_min
     *      0
     *  eff_day eff_mon eff_yr  exp_day exp_mon exp_yr 
     *     29       2    1998      29       2    1998
     *  eff_ihr eff_min exp_ihr exp_min
     *    08      30      23      59
     *  creat_day creat_mon creat_yr
          29         2       1998
     *  creat_hour creat_min
     *       12         23
     *  period_day period_hrs period_min
     *                             60
     *  CCC  NNN  xxx  AAA    CDE1  CDE2  CDE3 CDE4_DAY CDE4_HOUR CDE4_MIN
     * </pre>
     */
    public void setDataToDefault() {

        this.prodNum = 753;
        this.style = 45;
        this.nodeOrigSite = "";
        this.productCategory = "CLI";
        this.stationId = "";
        this.expirationMin = 0;
        this.activeStorage = "C";
        this.alertTone = "";
        this.beginMsgIndicator = "$a";
        this.delSaveMsg = "D";
        this.firstTwoN = "CL";
        this.thirdN = "";
        this.interruptMsg = "";
        this.messageConfirm = "";
        this.msgReference = "0";
        this.numListenTowers = "";
        this.messageFormat = "T_ENG";
        this.listenAreaCode = "VAC999c";
        this.creationDate = new ClimateDate(29, 2, 1998);
        this.creationTime = new ClimateTime(12, 23, "PM");
        this.effectiveDate = new ClimateDate(29, 2, 1998);
        this.effectiveTime = new ClimateTime(8, 30, "AM");
        this.expirationDate = new ClimateDate(29, 2, 1998);
        this.expirationTime = new ClimateTime(23, 59, "PM");
        this.periodicity = 60;
        this.address = "ALL";
        this.CDE1 = "TTAA00";
        this.CDE2 = "K";
        this.CDE3 = "ls";
        this.CDE4_Date = new ClimateDate(29, 2, 1998);
        this.CDE4_Time = new ClimateTime(12, 23, "PM");
    }

    /**
     * @return a deep copy of this object.
     */
    public ClimateProductHeader copy() {

        ClimateProductHeader header = new ClimateProductHeader();

        header.prodNum = this.prodNum;
        header.style = this.style;
        header.nodeOrigSite = this.nodeOrigSite;
        header.productCategory = this.productCategory;
        header.stationId = this.stationId;
        header.expirationMin = this.expirationMin;
        header.activeStorage = this.activeStorage;
        header.alertTone = this.alertTone;
        header.beginMsgIndicator = this.beginMsgIndicator;
        header.delSaveMsg = this.delSaveMsg;
        header.firstTwoN = this.firstTwoN;
        header.thirdN = this.thirdN;
        header.interruptMsg = this.interruptMsg;
        header.messageConfirm = this.messageConfirm;
        header.msgReference = this.msgReference;
        header.numListenTowers = this.numListenTowers;
        header.messageFormat = this.messageFormat;
        header.listenAreaCode = this.listenAreaCode;
        header.creationDate = this.creationDate;
        header.creationTime = this.creationTime;
        header.effectiveDate = this.effectiveDate;
        header.effectiveTime = this.effectiveTime;
        header.expirationDate = this.expirationDate;
        header.expirationTime = this.expirationTime;
        header.periodicity = this.periodicity;
        header.address = this.address;
        header.CDE1 = this.CDE1;
        header.CDE2 = this.CDE2;
        header.CDE3 = this.CDE3;
        header.CDE4_Date = new ClimateDate(this.CDE4_Date);
        header.CDE4_Time = new ClimateTime(this.CDE4_Time);

        return header;
    }

}
