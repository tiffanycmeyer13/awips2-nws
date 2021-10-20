/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.atcf.track;

import gov.noaa.nws.ocp.common.atcf.configuration.AtcfSitePreferences;

/**
 * This class holds options to create/display a storm's best track.
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Feb 23, 2019 60613      jwu         Initial creation.
 * Mar 12, 2019 61359      jwu         Add "boldLine" option.
 * Mar 18, 2019 61605      jwu         Add track color option.
 * May 03, 2019 62845      dmanzella   minor text fixes
 * Sep 16, 2019 68603      jwu         Use site preferences.
 *
 * </pre>
 *
 * @author jwu
 * @version 1.0
 *
 */
public class BestTrackProperties {

    // List of DTGs selected via “Display Partial Track” dialog
    private String[] selectedDTGs = null;

    // List of DTGs selected via “Best Track Radii Options” dialog
    private String[] selectedRadiiDTGs = null;

    // Plot character designators at special storm type positions.
    private boolean specialTypePosition = false;

    // Display the storm typhoon icons.
    private boolean stormSymbols = true;

    // Display storm number at the start and end of each storm track.
    private boolean stormNumber = false;

    // Use different line styles based on the storm’s wind speed.
    private boolean trackLineTypes = false;

    // Display a legend defining the track lines.
    private boolean trackLineLegend = false;

    // Display track & typhoon icons in colors based on the storm’s wind speed.
    private boolean colorsOnIntensity = false;

    // Display a legend defining track line colors base on storm intensity.
    private boolean intensityColorLegend = false;

    // Display track & typhoon icons in colors based on storm’s category.
    private boolean colorsOnCategory = false;

    // Display a legend defining track line colors base on storm category.
    private boolean categoryColorLegend = false;

    // Display a label to show storm name, number, and dates.
    private boolean trackInfoLabel = false;

    // Display a label to show the best track intensities.
    private boolean trackIntensities = false;

    // Display a label to show best track date and time for each DTG.
    private boolean trackLabels = false;

    // Display Radius Outermost Closed Isobar.
    private boolean drawROCI = false;

    // Draw 34 knot radii
    private boolean radiiFor34Knot = false;

    // Draw 50 knot radii
    private boolean radiiFor50Knot = false;

    // Draw 64 knot radii
    private boolean radiiFor64Knot = false;

    // Draw Radius of Max Wind
    private boolean drawRMW = false;

    // Draw bold track line
    private boolean boldLine = false;

    // Color index for track line
    private int trackColor = -1;

    /**
     * Default Constructor
     */
    public BestTrackProperties() {
    }

    /**
     * Constructor using properties from Configuration=>Preferences
     */
    public BestTrackProperties(AtcfSitePreferences prefs) {
        if (prefs != null) {
            // btrack-special-sttype
            specialTypePosition = prefs.getBtrackSpecialStype();

            // disp-storm-symbols
            stormSymbols = prefs.getDispStormSymbols();

            // disp-storm-number
            stormNumber = prefs.getDispStormNumber();

            // strack-solidDashDot
            trackLineTypes = prefs.getBtrackDashnDotsOn();

            // disp-tracklines-legend
            trackLineLegend = prefs.getDispTracklinesLegend();

            // strack-color-intensity
            colorsOnIntensity = prefs.getBtrackColorIntensity();

            // disp-track-color-leg
            intensityColorLegend = prefs.getDispTrackColorLegend();

            // strack-colors-ss-scale
            colorsOnCategory = prefs.getStrackColorsSSScale();

            // disp-ss-color-legend
            categoryColorLegend = prefs.getDispSSColorLegend();

            // disp-track-labels
            trackInfoLabel = prefs.getDispTrackLabels();

            // btrack-intensities-on
            trackIntensities = prefs.getBtrackIntensitiesOn();

            // btrack-labels-on
            trackLabels = prefs.getBtrackLabelsOn();
        }
    }

    /**
     * @return the selectedDTGs
     */
    public String[] getSelectedDTGs() {
        return selectedDTGs;
    }

    /**
     * @param selectedDTGs
     *            the selectedDTGs to set
     */
    public void setSelectedDTGs(String[] selectedDTGs) {
        this.selectedDTGs = selectedDTGs;
    }

    /**
     * @return the selectedRadiiDTGs
     */
    public String[] getSelectedRadiiDTGs() {
        return selectedRadiiDTGs;
    }

    /**
     * @param selectedRadiiDTGs
     *            the selectedRadiiDTGs to set
     */
    public void setSelectedRadiiDTGs(String[] selectedRadiiDTGs) {
        this.selectedRadiiDTGs = selectedRadiiDTGs;
    }

    /**
     * @return the specialTypePosition
     */
    public boolean isSpecialTypePosition() {
        return specialTypePosition;
    }

    /**
     * @param specialTypePosition
     *            the specialTypePosition to set
     */
    public void setSpecialTypePosition(boolean specialTypePosition) {
        this.specialTypePosition = specialTypePosition;
    }

    /**
     * @return the stormSymbols
     */
    public boolean isStormSymbols() {
        return stormSymbols;
    }

    /**
     * @param stormSymbols
     *            the stormSymbols to set
     */
    public void setStormSymbols(boolean stormSymbols) {
        this.stormSymbols = stormSymbols;
    }

    /**
     * @return the stormNumber
     */
    public boolean isStormNumber() {
        return stormNumber;
    }

    /**
     * @param stormNumber
     *            the stormNumber to set
     */
    public void setStormNumber(boolean stormNumber) {
        this.stormNumber = stormNumber;
    }

    /**
     * @return the trackLineTypes
     */
    public boolean isTrackLineTypes() {
        return trackLineTypes;
    }

    /**
     * @param trackLineTypes
     *            the trackLineTypes to set
     */
    public void setTrackLineTypes(boolean trackLineTypes) {
        this.trackLineTypes = trackLineTypes;
    }

    /**
     * @return the trackLineLegend
     */
    public boolean isTrackLineLegend() {
        return trackLineLegend;
    }

    /**
     * @param trackLineLegend
     *            the trackLineLegend to set
     */
    public void setTrackLineLegend(boolean trackLineLegend) {
        this.trackLineLegend = trackLineLegend;
    }

    /**
     * @return the colorsOnIntensity
     */
    public boolean isColorsOnIntensity() {
        return colorsOnIntensity;
    }

    /**
     * @param colorsOnIntensity
     *            the colorsOnIntensity to set
     */
    public void setColorsOnIntensity(boolean colorsOnIntensity) {
        this.colorsOnIntensity = colorsOnIntensity;
    }

    /**
     * @return the intensityColorLegend
     */
    public boolean isIntensityColorLegend() {
        return intensityColorLegend;
    }

    /**
     * @param intensityColorLegend
     *            the intensityColorLegend to set
     */
    public void setIntensityColorLegend(boolean intensityColorLegend) {
        this.intensityColorLegend = intensityColorLegend;
    }

    /**
     * @return the colorsOnCategory
     */
    public boolean isColorsOnCategory() {
        return colorsOnCategory;
    }

    /**
     * @param colorsOnCategory
     *            the colorsOnCategory to set
     */
    public void setColorsOnCategory(boolean colorsOnCategory) {
        this.colorsOnCategory = colorsOnCategory;
    }

    /**
     * @return the categoryColorLegend
     */
    public boolean isCategoryColorLegend() {
        return categoryColorLegend;
    }

    /**
     * @param categoryColorLegend
     *            the categoryColorLegend to set
     */
    public void setCategoryColorLegend(boolean categoryColorLegend) {
        this.categoryColorLegend = categoryColorLegend;
    }

    /**
     * @return the trackInfoLabel
     */
    public boolean isTrackInfoLabel() {
        return trackInfoLabel;
    }

    /**
     * @param trackInfoLabel
     *            the trackInfoLabel to set
     */
    public void setTrackInfoLabel(boolean trackInfoLabel) {
        this.trackInfoLabel = trackInfoLabel;
    }

    /**
     * @return the trackIntensities
     */
    public boolean isTrackIntensities() {
        return trackIntensities;
    }

    /**
     * @param trackIntensities
     *            the trackIntensities to set
     */
    public void setTrackIntensities(boolean trackIntensities) {
        this.trackIntensities = trackIntensities;
    }

    /**
     * @return the trackLabels
     */
    public boolean isTrackLabels() {
        return trackLabels;
    }

    /**
     * @param trackLabels
     *            the trackLabels to set
     */
    public void setTrackLabels(boolean trackLabels) {
        this.trackLabels = trackLabels;
    }

    /**
     * @return the drawROCI
     */
    public boolean isDrawROCI() {
        return drawROCI;
    }

    /**
     * @param drawROCI
     *            the drawROCI to set
     */
    public void setDrawROCI(boolean drawROCI) {
        this.drawROCI = drawROCI;
    }

    /**
     * @return the radiiFor34Knot
     */
    public boolean isRadiiFor34Knot() {
        return radiiFor34Knot;
    }

    /**
     * @param radiiFor34Knot
     *            the radiiFor34Knot to set
     */
    public void setRadiiFor34Knot(boolean radiiFor34Knot) {
        this.radiiFor34Knot = radiiFor34Knot;
    }

    /**
     * @return the radiiFor50Knot
     */
    public boolean isRadiiFor50Knot() {
        return radiiFor50Knot;
    }

    /**
     * @param radiiFor50Knot
     *            the radiiFor50Knot to set
     */
    public void setRadiiFor50Knot(boolean radiiFor50Knot) {
        this.radiiFor50Knot = radiiFor50Knot;
    }

    /**
     * @return the radiiFor64Knot
     */
    public boolean isRadiiFor64Knot() {
        return radiiFor64Knot;
    }

    /**
     * @param radiiFor64Knot
     *            the radiiFor64Knot to set
     */
    public void setRadiiFor64Knot(boolean radiiFor64Knot) {
        this.radiiFor64Knot = radiiFor64Knot;
    }

    /**
     * @return the drawRMW
     */
    public boolean isDrawRMW() {
        return drawRMW;
    }

    /**
     * @param drawRMW
     *            the drawRMW to set
     */
    public void setDrawRMW(boolean drawRMW) {
        this.drawRMW = drawRMW;
    }

    /**
     * @return the boldLine
     */
    public boolean isBoldLine() {
        return boldLine;
    }

    /**
     * @param boldLine
     *            the boldLine to set
     */
    public void setBoldLine(boolean boldLine) {
        this.boldLine = boldLine;
    }

    /**
     * @return the trackColor
     */
    public int getTrackColor() {
        return trackColor;
    }

    /**
     * @param trackColor
     *            the trackColor to set
     */
    public void setTrackColor(int trackColor) {
        this.trackColor = trackColor;
    }

}