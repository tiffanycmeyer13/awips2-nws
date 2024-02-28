/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.atcf.aids;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import gov.noaa.nws.ocp.common.atcf.configuration.AtcfConfigurationManager;
import gov.noaa.nws.ocp.common.atcf.configuration.AtcfCustomColors;
import gov.noaa.nws.ocp.common.atcf.configuration.ObjectiveAidTechEntry;

/**
 * Properties used to draw/display A-Deck data (Objective Aids).
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#     Engineer    Description
 * ------------ ----------  ----------- --------------------------
 * Apr 11, 2019 62487       jwu         Initial creation.
 *
 * </pre>
 *
 * @author jwu
 * @version 1
 */
public class ObjAidsProperties {

    private Map<String, Color> colorsForObjectiveAids = new HashMap<>();

    // List of obj aids profiles.
    private String[] availableProfiles = null;

    // Selectd obj aids profiles.
    private String selectedProfile = "";

    // All available obj aids DTGs.
    private String[] availableDateTimeGroups = {};

    // Selected obj aids DTGs.
    private String[] selectedDateTimeGroups = {};

    // All obj aids tech from current master tech file (normally techlist.dat)
    private Map<String, ObjectiveAidTechEntry> allObjectiveAids = null;

    // Default obj aids tech from default_aids.dat
    private String[] defaultObjectiveAids = null;

    // Obj aids tech found from current storm's A-Deck data
    private Set<String> activeObjectiveAids = new TreeSet<>();

    /*
     * Obj aids tech entries displayed in the list, which must appear in both
     * activeObjectiveAids and allObjectiveAids.
     */
    private List<String> displayedObjectiveAids = new ArrayList<>();

    // Obj aids tech currently selected
    private List<String> selectedObjectiveAids = new ArrayList<>();

    // Available obj aids partial times
    private String[] availablePartialTimes = AtcfTaus.getForecastTausTime();

    // Selected obj aids partial time
    private String selectedPartialTime = "";

    // Options for displaying obj aids.
    private boolean toggleBoldSelectedAids = false;

    private boolean displayAidIntensities = false;

    private boolean gpce = false;

    private boolean gpceClimatology = false;

    private boolean gpceAX = false;

    private boolean show34ktWindRadii = false;

    private boolean show50ktWindRadii = false;

    private boolean show64ktWindRadii = false;

    private boolean boldLinesAll = false;

    private boolean colorsByIntensity = false;

    private boolean colorsBySaffirSimpsonScale = false;

    /**
     * Default Constructor
     */
    public ObjAidsProperties() {
        // Default.
    }

    /**
     * @return the availableProfiles
     */
    public String[] getAvailableProfiles() {
        return availableProfiles;
    }

    /**
     * @param availableProfiles
     *            the availableProfiles to set
     */
    public void setAvailableProfiles(String[] availableProfiles) {
        this.availableProfiles = availableProfiles;
    }

    /**
     * @return the selectedProfile
     */
    public String getSelectedProfile() {
        return selectedProfile;
    }

    /**
     * @param selectedProfile
     *            the selectedProfile to set
     */
    public void setSelectedProfile(String selectedProfile) {
        this.selectedProfile = selectedProfile;
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
     * @return the selectedDateTimeGroups
     */
    public String[] getSelectedDateTimeGroups() {
        return selectedDateTimeGroups;
    }

    /**
     * @param selectedDateTimeGroups
     *            the selectedDateTimeGroups to set
     */
    public void setSelectedDateTimeGroups(String[] selectedDateTimeGroups) {
        this.selectedDateTimeGroups = selectedDateTimeGroups;
    }

    /**
     * @return the allObjectiveAids
     */
    public Map<String, ObjectiveAidTechEntry> getAllObjectiveAids() {
        return allObjectiveAids;
    }

    /**
     * @param allObjectiveAids
     *            the allObjectiveAids to set
     */
    public void setAllObjectiveAids(
            Map<String, ObjectiveAidTechEntry> allObjectiveAids) {
        this.allObjectiveAids = allObjectiveAids;

        AtcfCustomColors clrs = AtcfConfigurationManager.getInstance()
                .getAtcfCustomColors();
        for (ObjectiveAidTechEntry tech : allObjectiveAids.values()) {
            String name = tech.getName();
            Color clr = clrs.getColor(tech.getColor());
            colorsForObjectiveAids.put(name, clr);
        }
    }

    /**
     * @return the defaultObjectiveAids
     */
    public String[] getDefaultObjectiveAids() {
        return defaultObjectiveAids;
    }

    /**
     * @param defaultObjectiveAids
     *            the defaultObjectiveAids to set
     */
    public void setDefaultObjectiveAids(String[] defaultObjectiveAids) {
        this.defaultObjectiveAids = defaultObjectiveAids;
    }

    /**
     * @return the activeObjectiveAids
     */
    public Set<String> getActiveObjectiveAids() {
        return activeObjectiveAids;
    }

    /**
     * @param activeObjectiveAids
     *            the activeObjectiveAids to set
     */
    public void setActiveObjectiveAids(Set<String> activeObjectiveAids) {
        this.activeObjectiveAids = activeObjectiveAids;
    }

    /**
     * @return the displayedObjectiveAids
     */
    public List<String> getDisplayedObjectiveAids() {
        return displayedObjectiveAids;
    }

    /**
     * @param displayedObjectiveAids
     *            the displayedObjectiveAids to set
     */
    public void setDisplayedObjectiveAids(
            List<String> displayedObjectiveAids) {
        this.displayedObjectiveAids = displayedObjectiveAids;
    }

    /**
     * @return the selectedObjectiveAids
     */
    public List<String> getSelectedObjectiveAids() {
        return selectedObjectiveAids;
    }

    /**
     * @param selectedObjectiveAids
     *            the selectedObjectiveAids to set
     */
    public void setSelectedObjectiveAids(
            List<String> selectedObjectiveAids) {
        this.selectedObjectiveAids = selectedObjectiveAids;
    }

    /**
     * @return the availablePartialTimes
     */
    public String[] getAvailablePartialTimes() {
        return availablePartialTimes;
    }

    /**
     * @param availablePartialTimes
     *            the availablePartialTimes to set
     */
    public void setAvailablePartialTimes(String[] availablePartialTimes) {
        this.availablePartialTimes = availablePartialTimes;
    }

    /**
     * @return the selectedPartialTime
     */
    public String getSelectedPartialTime() {
        return selectedPartialTime;
    }

    /**
     * @param selectedPartialTime
     *            the selectedPartialTime to set
     */
    public void setSelectedPartialTime(String selectedPartialTime) {
        this.selectedPartialTime = selectedPartialTime;
    }

    /**
     * @return the toggleBoldSelectedAids
     */
    public boolean isToggleBoldSelectedAids() {
        return toggleBoldSelectedAids;
    }

    /**
     * @param toggleBoldSelectedAids
     *            the toggleBoldSelectedAids to set
     */
    public void setToggleBoldSelectedAids(boolean toggleBoldSelectedAids) {
        this.toggleBoldSelectedAids = toggleBoldSelectedAids;
    }

    /**
     * @return the displayAidIntensities
     */
    public boolean isDisplayAidIntensities() {
        return displayAidIntensities;
    }

    /**
     * @param displayAidIntensities
     *            the displayAidIntensities to set
     */
    public void setDisplayAidIntensities(boolean displayAidIntensities) {
        this.displayAidIntensities = displayAidIntensities;
    }

    /**
     * @return the gpce
     */
    public boolean isGpce() {
        return gpce;
    }

    /**
     * @param gpce
     *            the gpce to set
     */
    public void setGpce(boolean gpce) {
        this.gpce = gpce;
    }

    /**
     * @return the gpceClimatology
     */
    public boolean isGpceClimatology() {
        return gpceClimatology;
    }

    /**
     * @param gpceClimatology
     *            the gpceClimatology to set
     */
    public void setGpceClimatology(boolean gpceClimatology) {
        this.gpceClimatology = gpceClimatology;
    }

    /**
     * @return the gpceAX
     */
    public boolean isGpceAX() {
        return gpceAX;
    }

    /**
     * @param gpceAX
     *            the gpceAX to set
     */
    public void setGpceAX(boolean gpceAX) {
        this.gpceAX = gpceAX;
    }

    /**
     * @return the show34ktWindRadii
     */
    public boolean isShow34ktWindRadii() {
        return show34ktWindRadii;
    }

    /**
     * @param show34ktWindRadii
     *            the show34ktWindRadii to set
     */
    public void setShow34ktWindRadii(boolean show34ktWindRadii) {
        this.show34ktWindRadii = show34ktWindRadii;
    }

    /**
     * @return the show50ktWindRadii
     */
    public boolean isShow50ktWindRadii() {
        return show50ktWindRadii;
    }

    /**
     * @param show50ktWindRadii
     *            the show50ktWindRadii to set
     */
    public void setShow50ktWindRadii(boolean show50ktWindRadii) {
        this.show50ktWindRadii = show50ktWindRadii;
    }

    /**
     * @return the show64ktWindRadii
     */
    public boolean isShow64ktWindRadii() {
        return show64ktWindRadii;
    }

    /**
     * @param show64ktWindRadii
     *            the show64ktWindRadii to set
     */
    public void setShow64ktWindRadii(boolean show64ktWindRadii) {
        this.show64ktWindRadii = show64ktWindRadii;
    }

    /**
     * @return the boldLinesAll
     */
    public boolean isBoldLinesAll() {
        return boldLinesAll;
    }

    /**
     * @param boldLinesAll
     *            the boldLinesAll to set
     */
    public void setBoldLinesAll(boolean boldLinesAll) {
        this.boldLinesAll = boldLinesAll;
    }

    /**
     * @return the colorsByIntensity
     */
    public boolean isColorsByIntensity() {
        return colorsByIntensity;
    }

    /**
     * @param colorsByIntensity
     *            the colorsByIntensity to set
     */
    public void setColorsByIntensity(boolean colorsByIntensity) {
        this.colorsByIntensity = colorsByIntensity;
    }

    /**
     * @return the colorsBySaffirSimpsonScale
     */
    public boolean isColorsBySaffirSimpsonScale() {
        return colorsBySaffirSimpsonScale;
    }

    /**
     * @param colorsBySaffirSimpsonScale
     *            the colorsBySaffirSimpsonScale to set
     */
    public void setColorsBySaffirSimpsonScale(
            boolean colorsBySaffirSimpsonScale) {
        this.colorsBySaffirSimpsonScale = colorsBySaffirSimpsonScale;
    }

    /**
     * @return the colorsForObjectiveAids
     */
    public Map<String, Color> getColorsForObjectiveAids() {
        return colorsForObjectiveAids;
    }

    /**
     * @param colorsForObjectiveAids
     *            the colorsForObjectiveAids to set
     */
    public void setColorsForObjectiveAids(
            Map<String, Color> colorsForObjectiveAids) {
        this.colorsForObjectiveAids = colorsForObjectiveAids;
    }

}