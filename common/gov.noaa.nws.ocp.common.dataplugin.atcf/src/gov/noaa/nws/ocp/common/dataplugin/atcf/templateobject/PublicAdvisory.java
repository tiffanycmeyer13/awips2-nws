/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.common.dataplugin.atcf.templateobject;

import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

import gov.noaa.nws.ocp.common.dataplugin.atcf.AdvisoryType;

/**
 * An Object for holding Public Advisory data.
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#     Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Sep 23, 2020 81818      wpaintsil   Initial creation.
 * Oct 19, 2020 78027      jwu         Reorganize with sections.
 * Jan 26, 2021 86746      jwu         Revised a few fields.
 * Feb 22, 2021 87781      jwu         Overhaul & extends from AtcfAdvisory.
 * May 14, 2021 88584      wpaintsil   Add intermediate flag.
 *
 * </pre>
 *
 * @author wpaintsil
 * @version 1.0
 */
public class PublicAdvisory extends AtcfAdvisory {

    /**
     * A placeholder for the HSU forecasters to insert a new headline, if no
     * headline yet.
     */
    private static final String HEADERLINE_INSERT = ">-- INSERT HEADLINE HERE --<";

    /**
     * Headline
     */
    @DynamicSerializeElement
    private String headline;

    @DynamicSerializeElement
    private String insertHeadline;

    /**
     * Summary section - position/speed/dir/mslp, etc.
     */
    @DynamicSerializeElement
    private AdvisorySummary summary;

    /**
     * Watches and Warnings reminder
     */
    @DynamicSerializeElement
    private String watchWarnReminder;

    /**
     * Hazards
     */
    @DynamicSerializeElement
    private String hazards;

    /**
     * Advisory discussion info
     */
    @DynamicSerializeElement
    private AdvisoryDiscussion discus;

    /**
     * Time for next complete advisory.
     */
    @DynamicSerializeElement
    private String prevDiscus;

    /**
     * Flag for whether this is an intermediate public advisory.
     */
    @DynamicSerializeElement
    private boolean isIntermediate;

    /**
     * Default constructor
     */
    public PublicAdvisory() {
        super();

        this.setType(AdvisoryType.TCP);
        this.headline = "";
        this.insertHeadline = HEADERLINE_INSERT;
        this.summary = new AdvisorySummary();
        this.watchWarnReminder = "";
        this.hazards = "";
        this.discus = new AdvisoryDiscussion();
        this.prevDiscus = "";
        this.isIntermediate = false;
    }

    /**
     * @return the headline
     */
    public String getHeadline() {
        return headline;
    }

    /**
     * @param headline
     *            the headline to set
     */
    public void setHeadline(String headline) {
        this.headline = headline;
    }

    /**
     * @return the insertHeadline
     */
    public String getInsertHeadline() {
        return insertHeadline;
    }

    /**
     * @param insertHeadline
     *            the insertHeadline to set
     */
    public void setInsertHeadline(String insertHeadline) {
        this.insertHeadline = insertHeadline;
    }

    /**
     * @return the summary
     */
    public AdvisorySummary getSummary() {
        return summary;
    }

    /**
     * @param summary
     *            the summary to set
     */
    public void setSummary(AdvisorySummary summary) {
        this.summary = summary;
    }

    /**
     * @return the watchWarnReminder
     */
    public String getWatchWarnReminder() {
        return watchWarnReminder;
    }

    /**
     * @param watchWarnReminder
     *            the watchWarnReminder to set
     */
    public void setWatchWarnReminder(String watchWarnReminder) {
        this.watchWarnReminder = watchWarnReminder;
    }

    /**
     * @return the hazards
     */
    public String getHazards() {
        return hazards;
    }

    /**
     * @param hazards
     *            the hazards to set
     */
    public void setHazards(String hazards) {
        this.hazards = hazards;
    }

    /**
     * @return the discus
     */
    public AdvisoryDiscussion getDiscus() {
        return discus;
    }

    /**
     * @param discus
     *            the discus to set
     */
    public void setDiscus(AdvisoryDiscussion discus) {
        this.discus = discus;
    }

    /**
     * @return the prevDiscus
     */
    public String getPrevDiscus() {
        return prevDiscus;
    }

    /**
     * @param prevDiscus
     *            the prevDiscus to set
     */
    public void setPrevDiscus(String prevDiscus) {
        this.prevDiscus = prevDiscus;
    }

    /**
     * @return the isIntermediate
     */
    public boolean isIntermediate() {
        return isIntermediate;
    }

    /**
     * @param isIntermediate
     *            the isIntermediate to set
     */
    public void setIntermediate(boolean isIntermediate) {
        this.isIntermediate = isIntermediate;
        setType(isIntermediate ? AdvisoryType.TCP_A : AdvisoryType.TCP);
    }

}
