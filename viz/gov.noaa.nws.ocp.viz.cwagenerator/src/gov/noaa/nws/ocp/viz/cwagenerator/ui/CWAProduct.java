/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.viz.cwagenerator.ui;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

import com.raytheon.uf.common.dataplugin.text.StdTextProductContainer;
import com.raytheon.uf.common.dataplugin.text.db.StdTextProduct;
import com.raytheon.uf.common.dataplugin.text.request.ExecuteAfosCmdRequest;
import com.raytheon.uf.common.dataplugin.text.request.WriteProductRequest;
import com.raytheon.uf.common.dissemination.OUPRequest;
import com.raytheon.uf.common.dissemination.OUPResponse;
import com.raytheon.uf.common.dissemination.OfficialUserProduct;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;
import com.raytheon.uf.common.time.SimulatedTime;
import com.raytheon.uf.common.time.util.TimeUtil;
import com.raytheon.uf.viz.core.auth.UserController;
import com.raytheon.uf.viz.core.exception.VizException;
import com.raytheon.uf.viz.core.requests.ThriftClient;

import gov.noaa.nws.ocp.viz.cwagenerator.config.WeatherType;

/**
 * This utility class gets product from textdb, parse product, and disseminate
 * product.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date        Ticket#  Engineer    Description
 * ----------- -------- ----------- --------------------------
 * 12/02/2016  17469    wkwock      Initial creation
 * 06/27/2021  92561    wkwock      Fix local time zone issue
 * 04/05/2022  22989    wkwock      Update issuance# algorithm
 * 
 * </pre>
 * 
 * @author wkwock
 */
public class CWAProduct {
    /** logger */
    private static final IUFStatusHandler logger = UFStatus
            .getHandler(CWAProduct.class);

    /** product ID */
    private String productId;

    /** CWSU ID */
    private String CWSUid;

    /** product Text */
    private String productTxt = null;

    /** series ID string */
    private String series = null;

    /** expire time string */
    private String expire = null;

    /** Color code */
    private Color color = null;

    /** start Time */
    private ZonedDateTime insertTime = null;

    /** reference Time */
    private ZonedDateTime refTime = null;

    /** is operational */
    private boolean isOperational;

    private WeatherType weatherType = null;

    private boolean isIssuanceNumReset = false;

    /**
     * Constructor
     * 
     * @param productId
     */
    public CWAProduct(String productId, boolean isOperational) {
        this.productId = productId;
        this.isOperational = isOperational;
    }

    /**
     * Constructor
     * 
     * @param productId
     * @param CWSUid
     */
    public CWAProduct(String productId, String CWSUid, boolean isOperational,
            WeatherType weatherType) {
        this.productId = productId;
        this.CWSUid = CWSUid;
        this.isOperational = isOperational;
        this.weatherType = weatherType;
        parseProduct();
    }

    /**
     * set product text
     * 
     * @param productTxt
     */
    public void setProductTxt(String productTxt) {
        this.productTxt = productTxt;
    }

    /**
     * get product text
     * 
     * @return product text
     */
    public String getProductTxt() {
        if (productTxt == null) {
            List<StdTextProduct> productList = executeAfosCmd(productId,
                    isOperational);
            if (productList != null && !productList.isEmpty()) {
                productTxt = productList.get(0).getProduct();
                refTime = TimeUtil
                        .newGmtCalendar(productList.get(0).getRefTime())
                        .toInstant().atZone(TimeUtil.GMT_TIME_ZONE.toZoneId());
                insertTime = TimeUtil
                        .newGmtCalendar(productList.get(0).getInsertTime()
                                .getTimeInMillis())
                        .toInstant().atZone(TimeUtil.GMT_TIME_ZONE.toZoneId());
            } else {
                productTxt = "";
            }
        }
        return productTxt;
    }

    /**
     * Retrieve a text product from text database with an AFOS command.
     * 
     * @param afosCommand
     * @param operationalMode
     * @return
     */
    static public List<StdTextProduct> executeAfosCmd(String afosCommand,
            boolean operationalMode) {
        ExecuteAfosCmdRequest req = new ExecuteAfosCmdRequest();
        req.setAfosCommand(afosCommand);
        req.setOperationalMode(operationalMode);
        try {
            Object resp = ThriftClient.sendRequest(req);
            List<StdTextProduct> productList = ((StdTextProductContainer) resp)
                    .getProductList();
            return productList;
        } catch (VizException e) {
            logger.error("Failed to request data.", e);
        }

        return null;
    }

    /**
     * get series ID string
     * 
     * @return
     */
    public String getSeries() {
        return series;
    }

    /**
     * get expire time string
     * 
     * @return expire time string
     */
    public String getExpire() {
        return expire;
    }

    public ZonedDateTime getInsertTime() {
        return insertTime;
    }

    public void setInsertTime(ZonedDateTime insertTime) {
        this.insertTime = insertTime;
    }

    public WeatherType getWeatherType() {
        return weatherType;
    }

    public void setWeatherType(WeatherType weatherType) {
        this.weatherType = weatherType;
    }

    public boolean isIssuanceNumReset() {
        return isIssuanceNumReset;
    }

    public void setIssuanceNumReset(boolean isIssuanceNumReset) {
        this.isIssuanceNumReset = isIssuanceNumReset;
    }

    /**
     * get color
     * 
     * @return color
     */
    public Color getColor() {
        if (color == null) {
            Display display = Display.getCurrent();
            color = display.getSystemColor(SWT.COLOR_GRAY);
        }
        return color;
    }

    /**
     * parse a product
     */
    private void parseProduct() {
        // default series, expire, color etc
        series = "   ";
        expire = "        ";
        color = getColor();

        String[] lines = getProductTxt().split("\n");

        for (String line : lines) {
            if (line.startsWith(CWSUid + " CWA")
                    || line.startsWith(CWSUid + " UCWA")
                    || line.startsWith(CWSUid + " MIS")) {
                String[] items = line.split("\\s+");
                String expire_date = "";
                if (line.startsWith(CWSUid + " CWA")) {
                    series = items[2];
                    expire_date = items[5];
                } else if (line.startsWith(CWSUid + " UCWA")) {
                    series = items[2];
                    expire_date = items[5];
                } else if (line.startsWith(CWSUid + " MIS COR")) {
                    series = items[3];
                    expire_date = items[5].substring(0, 7);
                } else if (line.startsWith(CWSUid + " MIS")) {
                    series = items[2];
                    expire_date = items[4].substring(7);
                }

                if (!expire_date.isEmpty()) {
                    SimpleDateFormat sdfDate = new SimpleDateFormat("ddHHmm");
                    sdfDate.setTimeZone(TimeUtil.GMT_TIME_ZONE);
                    Calendar expCal = TimeUtil
                            .newGmtCalendar(refTime.toInstant().toEpochMilli());
                    try {
                        Date d = sdfDate.parse(expire_date);
                        Calendar tmpCal = TimeUtil.newGmtCalendar(d);
                        expCal.set(Calendar.DAY_OF_MONTH,
                                tmpCal.get(Calendar.DAY_OF_MONTH));
                        expCal.set(Calendar.HOUR_OF_DAY,
                                tmpCal.get(Calendar.HOUR_OF_DAY));
                        expCal.set(Calendar.MINUTE,
                                tmpCal.get(Calendar.MINUTE));
                    } catch (ParseException e) {
                        logger.error("Failed to parse " + expire_date, e);
                    }

                    expire = expire_date.substring(0, 2) + "-"
                            + expire_date.substring(2);
                    getProductColor(expCal);
                } else {
                    expire = " TEST   ";
                }
            }
        }

    }

    /**
     * Get the next series ID. The series ID consists of Phenomenon Number(1-6)
     * and sequential issuance number(1-99) for CWA. And for CWS/MIS, the
     * issuance number is the series ID.
     * 
     * @param isCor
     * @return next series ID
     */
    public int getNextSeriesId(boolean isCor, String localTimeZone,
            boolean isResetIssuance) {
        // get the Phenomenon Number
        int phenomenonNum = 0;
        String phenomenonNumStr = productId.substring(productId.length() - 1);
        if (phenomenonNumStr.matches("[0-9]")) {// this is a CWA product
            phenomenonNum = Integer.parseInt(phenomenonNumStr);
        }

        // get the sequential issuance number
        int issuanceNum = getNextIssuanceNumber(localTimeZone,
                isResetIssuance);

        // include the Phenomenon Number
        int seriesId = phenomenonNum * 100 + issuanceNum;
        return seriesId;
    }

    /**
     * get the next issuance number
     * 
     * @param phenomenonNum
     * @param localTimeZoneId
     * @param isResetIssuance
     * @return issuance number
     */
    public int getNextIssuanceNumber(String localTimeZoneId,
            boolean isResetIssuance) {
        // 1. The issuance number should be 01 to 99 only
        // 2. The issuance number should be 01 if isResetIssuance is true
        // 3. Cancel product increase issuance number to next one unless 1. or
        // 2.
        // 4. Other issuance number should start with 01 each local day

        if (insertTime == null || isResetIssuance) {
            // no product in DB or isResetIssuance is true
            logger.info("Issuance number rest to 1. isResetIssuance="
                    + isResetIssuance);
            isIssuanceNumReset = true;
            return 1;
        }
        // get current issuance number
        int currentIssuanceNum = 0;
        if (!getSeries().trim().isEmpty()) {
            try {
                currentIssuanceNum = Integer.parseInt(getSeries());
                currentIssuanceNum = currentIssuanceNum % 100;
            } catch (NumberFormatException nfe) {
                logger.error("Failed to parse " + getSeries(), nfe);
            }
        }

        // increase issuance number to next one
        int issuanceNum = currentIssuanceNum + 1;
        if (currentIssuanceNum == 99) {
            issuanceNum = 1;
            isIssuanceNumReset = true;
            logger.info(
                    "Issuance number reset to 1 due to the last one was 99.");
        }

        // Cancel product always go to the next one
        if (weatherType != null && weatherType == WeatherType.CANMAN) {
            return issuanceNum;
        }

        // localTimeZoneId is always a valid time zone ID in here
        TimeZone timeZone = TimeZone.getTimeZone(localTimeZoneId.trim());

        ZonedDateTime localCal = Instant
                .ofEpochMilli(SimulatedTime.getSystemTime().getMillis())
                .atZone(timeZone.toZoneId());

        ZonedDateTime tmpInsertTime = insertTime
                .withZoneSameInstant(timeZone.toZoneId());

        if (localCal.getYear() > tmpInsertTime.getYear()
                || ((localCal.getYear() == tmpInsertTime.getYear()) && (localCal
                        .getDayOfYear() > tmpInsertTime.getDayOfYear()))) {
            // 1st one for this phenomenon in the new day, the issuance # should
            // be 1.
            issuanceNum = 1;
            isIssuanceNumReset = true;
            logger.info(
                    "Issuance number reset to 1 due to it's the 1st local time.");
        }
        return issuanceNum;
    }

    /**
     * get color base on expiration time
     * 
     * @param expire_time
     */
    private void getProductColor(Calendar expireTime) {
        Display display = Display.getCurrent();

        Calendar currentTime = TimeUtil.newGmtCalendar();

        long remainingMinutes = (expireTime.getTimeInMillis()
                - currentTime.getTimeInMillis()) / TimeUtil.MILLIS_PER_MINUTE;

        Calendar a = TimeUtil.newCalendar();
        a.setTimeZone(TimeUtil.GMT_TIME_ZONE);
        if (remainingMinutes > 9) {
            color = display.getSystemColor(SWT.COLOR_BLACK);
        } else if (remainingMinutes > 0) {
            color = display.getSystemColor(SWT.COLOR_YELLOW);
        } else if (remainingMinutes > -24 * 60) {
            color = display.getSystemColor(SWT.COLOR_RED);
        } else {
            color = display.getSystemColor(SWT.COLOR_GRAY);
        }
    }

    /**
     * save product to textdb and disseminate to DEFAULTNCF
     * 
     * @param site
     * @param isOperational
     * @return
     */
    public boolean sendText(String site) {
        if (isOperational) {
            // transmit product for operational only
            OUPRequest req = new OUPRequest();
            OfficialUserProduct oup = new OfficialUserProduct();
            oup.setAwipsWanPil(productId);
            oup.setSource("CWA Generator");
            oup.setAddress("DEFAULTNCF");
            oup.setNeedsWmoHeader(false);
            oup.setFilename(productId + ".txt");
            oup.setProductText(productTxt);

            req.setCheckBBB(true);
            req.setProduct(oup);
            req.setUser(UserController.getUserObject());

            OUPResponse response;
            try {
                response = (OUPResponse) ThriftClient.sendRequest(req);
                boolean success = response.isSendLocalSuccess();
                if (response.hasFailure()) {
                    Priority p = Priority.EVENTA;
                    if (!response.isAttempted()) {
                        // if was never attempted to send or store even
                        // locally
                        p = Priority.CRITICAL;
                    } else if (!response.isSendLocalSuccess()) {
                        // if send/store locally failed
                        p = Priority.CRITICAL;
                    } else if (!response.isSendWANSuccess()) {
                        // if send to WAN failed
                        if (response.getNeedAcknowledgment()) {
                            // if ack was needed, if it never sent then no
                            // ack was recieved
                            p = Priority.CRITICAL;
                        } else {
                            // if no ack was needed
                            p = Priority.EVENTA;
                        }
                    } else if (response.getNeedAcknowledgment()
                            && !response.isAcknowledged()) {
                        // if sent but not acknowledged when acknowledgement
                        // is needed
                        p = Priority.CRITICAL;
                    }

                    logger.handle(p, response.getMessage());
                }

                return success;
            } catch (VizException e) {
                logger.error("Failed to transmit product " + productId, e);
            }
        } else {
            storeProduct(productId, productTxt);
        }
        return false;
    }

    /**
     * save product to textdb
     * 
     * @param textdbId
     * @param productText
     */
    public void storeProduct(String textdbId, String productText) {
        WriteProductRequest req = new WriteProductRequest();
        req.setProductId(textdbId);
        req.setReportData(productText);
        req.setOperationalMode(isOperational);
        try {
            ThriftClient.sendRequest(req);
            Priority p = Priority.EVENTA;
            logger.handle(p, "Product " + textdbId + " has been saved");
        } catch (VizException e) {
            logger.handle(Priority.CRITICAL,
                    "Failed to store product: " + textdbId + " to textdb", e);
        }
    }
}
