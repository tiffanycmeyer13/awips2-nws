/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.atcf.fixes;

import gov.noaa.nws.ocp.viz.atcf.fixes.DisplayFixesDialog.NonCenterFixMode;

/**
 * Attributes used to draw/display F-Deck data (fixes).
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#     Engineer    Description
 * ------------ ----------  ----------- --------------------------
 * Nov 28, 2018 #57484      jwu         Initial creation.
 *
 * </pre>
 *
 * @author jwu
 * @version 1
 */

public class DisplayFixesProperties {

    // Attributes and default values.
    private String[] availableFixSites = null;

    private String[] selectedFixSites = null;

    private String[] availableFixTypes = null;

    private String[] selectedFixTypes = null;

    private String[] availableDateTimeGroups = {};

    private String dateTimeGroup = null;

    private boolean highlightFixes = false;

    private boolean limitFixDisplay = false;

    private boolean confidence = false;

    private boolean windRadii = false;

    private boolean autoLabel = false;

    private boolean fixSiteLabels = false;

    private boolean tAndCILabels = false;

    private boolean showComments = false;

    private NonCenterFixMode nonCenterFixMode = NonCenterFixMode.USE_NON_CENTER_FIX_COLOR;

    private int plusMinusHoursForHighlightFixes = 3;

    private int plusMinusHoursForLimitFixDisplay = 3;

    private int plusMinusHoursForConfidence = 3;

    private int plusMinusHoursForWindRadii = 3;

    private int plusMinusHoursForAutoLabel = 3;

    /**
     * Default Constructor
     */
    public DisplayFixesProperties() {
    }

    /**
     * @return the availableFixSites
     */
    public String[] getAvailableFixSites() {
        return availableFixSites;
    }

    /**
     * @param availableFixSites
     *            the availableFixSites to set
     */
    public void setAvailableFixSites(String[] availableFixSites) {
        this.availableFixSites = availableFixSites;
    }

    /**
     * @return the selectedFixSites
     */
    public String[] getSelectedFixSites() {
        return selectedFixSites;
    }

    /**
     * @param selectedFixSites
     *            the selectedFixSites to set
     */
    public void setSelectedFixSites(String[] selectedFixSites) {
        this.selectedFixSites = selectedFixSites;
    }

    /**
     * @return the availableFixTypes
     */
    public String[] getAvailableFixTypes() {
        return availableFixTypes;
    }

    /**
     * @param availableFixTypes
     *            the availableFixTypes to set
     */
    public void setAvailableFixTypes(String[] availableFixTypes) {
        this.availableFixTypes = availableFixTypes;
    }

    /**
     * @return the selectedFixTypes
     */
    public String[] getSelectedFixTypes() {
        return selectedFixTypes;
    }

    /**
     * @param selectedFixTypes
     *            the selectedFixTypes to set
     */
    public void setSelectedFixTypes(String[] selectedFixTypes) {
        this.selectedFixTypes = selectedFixTypes;
    }

    /**
     * @return the availableDateTimeGroups
     */
    public String[] getAvailableDateTimeGroups() {
        return availableDateTimeGroups;
    }

    /**
     * @param availableDateTimeGroups
     *            the availableDateTimeGroups to set
     */
    public void setAvailableDateTimeGroups(String[] availableDateTimeGroups) {
        this.availableDateTimeGroups = availableDateTimeGroups;
    }

    /**
     * @return the dateTimeGroup
     */
    public String getDateTimeGroup() {
        return dateTimeGroup;
    }

    /**
     * @param dateTimeGroup
     *            the dateTimeGroup to set
     */
    public void setDateTimeGroup(String dateTimeGroup) {
        this.dateTimeGroup = dateTimeGroup;
    }

    /**
     * @return the highlightFixes
     */
    public boolean getHighlightFixes() {
        return highlightFixes;
    }

    /**
     * @param highlightFixes
     *            the highlightFixes to set
     */
    public void setHighlightFixes(boolean highlightFixes) {
        this.highlightFixes = highlightFixes;
    }

    /**
     * @return the limitFixDisplay
     */
    public boolean getLimitFixDisplay() {
        return limitFixDisplay;
    }

    /**
     * @param limitFixDisplay
     *            the limitFixDisplay to set
     */
    public void setLimitFixDisplay(boolean limitFixDisplay) {
        this.limitFixDisplay = limitFixDisplay;
    }

    /**
     * @return the confidence
     */
    public boolean getConfidence() {
        return confidence;
    }

    /**
     * @param confidence
     *            the confidence to set
     */
    public void setConfidence(boolean confidence) {
        this.confidence = confidence;
    }

    /**
     * @return the windRadii
     */
    public boolean getWindRadii() {
        return windRadii;
    }

    /**
     * @param windRadii
     *            the windRadii to set
     */
    public void setWindRadii(boolean windRadii) {
        this.windRadii = windRadii;
    }

    /**
     * @return the autoLabel
     */
    public boolean getAutoLabel() {
        return autoLabel;
    }

    /**
     * @param autoLabel
     *            the autoLabel to set
     */
    public void setAutoLabel(boolean autoLabel) {
        this.autoLabel = autoLabel;
    }

    /**
     * @return the fixSiteLabels
     */
    public boolean getFixSiteLabels() {
        return fixSiteLabels;
    }

    /**
     * @param fixSiteLabels
     *            the fixSiteLabels to set
     */
    public void setFixSiteLabels(boolean fixSiteLabels) {
        this.fixSiteLabels = fixSiteLabels;
    }

    /**
     * @return the tAndCILabels
     */
    public boolean gettAndCILabels() {
        return tAndCILabels;
    }

    /**
     * @param tAndCILabels
     *            the tAndCILabels to set
     */
    public void settAndCILabels(boolean tAndCILabels) {
        this.tAndCILabels = tAndCILabels;
    }

    /**
     * @return the showComments
     */
    public boolean getShowComments() {
        return showComments;
    }

    /**
     * @param showComments
     *            the showComments to set
     */
    public void setShowComments(boolean showComments) {
        this.showComments = showComments;
    }

    /**
     * @return the nonCenterFixMode
     */
    public NonCenterFixMode getNonCenterFixMode() {
        return nonCenterFixMode;
    }

    /**
     * @param nonCenterFixMode
     *            the nonCenterFixMode to set
     */
    public void setNonCenterFixMode(NonCenterFixMode nonCenterFixMode) {
        this.nonCenterFixMode = nonCenterFixMode;
    }

    /**
     * @return the plusMinusHoursForHighlightFixes
     */
    public int getPlusMinusHoursForHighlightFixes() {
        return plusMinusHoursForHighlightFixes;
    }

    /**
     * @param plusMinusHoursForHighlightFixes
     *            the plusMinusHoursForHighlightFixes to set
     */
    public void setPlusMinusHoursForHighlightFixes(
            int plusMinusHoursForHighlightFixes) {
        this.plusMinusHoursForHighlightFixes = plusMinusHoursForHighlightFixes;
    }

    /**
     * @return the plusMinusHoursForLimitFixDisplay
     */
    public int getPlusMinusHoursForLimitFixDisplay() {
        return plusMinusHoursForLimitFixDisplay;
    }

    /**
     * @param plusMinusHoursForLimitFixDisplay
     *            the plusMinusHoursForLimitFixDisplay to set
     */
    public void setPlusMinusHoursForLimitFixDisplay(
            int plusMinusHoursForLimitFixDisplay) {
        this.plusMinusHoursForLimitFixDisplay = plusMinusHoursForLimitFixDisplay;
    }

    /**
     * @return the plusMinusHoursForConfidence
     */
    public int getPlusMinusHoursForConfidence() {
        return plusMinusHoursForConfidence;
    }

    /**
     * @param plusMinusHoursForConfidence
     *            the plusMinusHoursForConfidence to set
     */
    public void setPlusMinusHoursForConfidence(
            int plusMinusHoursForConfidence) {
        this.plusMinusHoursForConfidence = plusMinusHoursForConfidence;
    }

    /**
     * @return the plusMinusHoursForWindRadii
     */
    public int getPlusMinusHoursForWindRadii() {
        return plusMinusHoursForWindRadii;
    }

    /**
     * @param plusMinusHoursForWindRadii
     *            the plusMinusHoursForWindRadii to set
     */
    public void setPlusMinusHoursForWindRadii(int plusMinusHoursForWindRadii) {
        this.plusMinusHoursForWindRadii = plusMinusHoursForWindRadii;
    }

    /**
     * @return the plusMinusHoursForAutoLabel
     */
    public int getPlusMinusHoursForAutoLabel() {
        return plusMinusHoursForAutoLabel;
    }

    /**
     * @param plusMinusHoursForAutoLabel
     *            the plusMinusHoursForAutoLabel to set
     */
    public void setPlusMinusHoursForAutoLabel(int plusMinusHoursForAutoLabel) {
        this.plusMinusHoursForAutoLabel = plusMinusHoursForAutoLabel;
    }

}