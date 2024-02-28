package gov.noaa.nws.ocp.edex.psh.parser;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;

/**
 * MetarTextLine
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 21, 2017            pwang     Initial creation
 * Oct 20, 2020 DR22159    dhaines   Added reftime property
 *
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */
public class MetarTextLine {

    public enum ContentType {
        SLP, WIND, UNKNOWN;

    }

    private int day;

    private int hhmm;

    private long reftime;
    
    private boolean containSLP = false;

    private boolean containWind = false;

    private String textLine;

    private static final IUFStatusHandler logger = UFStatus
            .getHandler(MetarTextLine.class);

    /**
     * MetarTextLine
     */
    public MetarTextLine() {

    }
    
    public MetarTextLine (String mtrLine, long reftime) {
    	populate(mtrLine);
    	setReftime(reftime);
    }

    /**
     * populate
     * 
     * @param line
     */
    private void populate(String mline) {
        String line = mline.trim();
        String[] segs = line.split("\\s+");
        if (segs == null || segs.length < 3) {
            logger.warn("The METAR text is in an invalid format: " + line);
            return;
        }
        if (MetarStormDataParser.DTZ_EXP.matcher(segs[2]).find()) {
            this.day = Integer.parseInt(segs[2].substring(0, 2));
            this.hhmm = Integer.parseInt(segs[2].substring(2, 6));
            this.textLine = line;
        } else if (MetarStormDataParser.COR_EXP.matcher(segs[2]).find()
                && MetarStormDataParser.DTZ_EXP.matcher(segs[3]).find()) {
            // Case of COR DDHHMMZ
            this.day = Integer.parseInt(segs[3].substring(0, 2));
            this.hhmm = Integer.parseInt(segs[3].substring(2, 6));
            this.textLine = line;
        }

        // determineContentType();
    }

    /**
     * mergeToOneLine
     * 
     * @param line
     */
    public void mergeToOneLine(String line) {
        this.textLine = this.textLine + " " + line.trim();
    }

    /**
     * determineContentType
     * 
     */
    public void determineContentType() {
        if (textLine != null) {

            if (MetarStormDataParser.WIND_GROUP_EXP_KT.matcher(this.textLine)
                    .find()) {
                this.containWind = true;
            }
            if (MetarStormDataParser.SEA_LEVEL_PRESS_EXP.matcher(this.textLine)
                    .find()) {
                this.containSLP = true;
            }
        }
    }

    /**
     * @return the day
     */
    public int getDay() {
        return day;
    }

    /**
     * @param day
     *            the day to set
     */
    public void setDay(int day) {
        this.day = day;
    }

    /**
     * @return the hhmm
     */
    public int getHhmm() {
        return hhmm;
    }

    /**
     * @param hhmm
     *            the hhmm to set
     */
    public void setHhmm(int hhmm) {
        this.hhmm = hhmm;
    }

    /**
     * @return the reftime
     */
    public long getReftime() {
        return reftime;
    }

    /**
     * @param reftime
     *            the reftime to set
     */
    public void setReftime(long reftime) {
        this.reftime = reftime;
    }
    
    /**
     * @return the containSLP
     */
    public boolean isContainSLP() {
        return containSLP;
    }

    /**
     * @param containSLP
     *            the containSLP to set
     */
    public void setContainSLP(boolean containSLP) {
        this.containSLP = containSLP;
    }

    /**
     * @return the containWind
     */
    public boolean isContainWind() {
        return containWind;
    }

    /**
     * @param containWind
     *            the containWind to set
     */
    public void setContainWind(boolean containWind) {
        this.containWind = containWind;
    }

    /**
     * @return the textLine
     */
    public String getTextLine() {
        return textLine;
    }

    /**
     * @param textLine
     *            the textLine to set
     */
    public void setTextLine(String textLine) {
        this.textLine = textLine;
    }

}
