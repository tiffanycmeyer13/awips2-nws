/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.atcf;

/**
 * Advisory types that could be edited/saved in the AtcfTextEditor in ATCF, the
 * their template file names used for formatting, and the file suffix used to
 * save them.
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 19, 2020 82721      jwu         Initial creation
 * Nov 12, 2020 84446      dfriedman   Add isFullProduct.
 * Jan 26, 2021 86746      jwu         Add advNumPhrase.
 * May 14, 2021 88584      wpaintsil   Add constants for TCP_A
 *
 * </pre>
 *
 * @author jwu
 * @version 1.0
 */
public enum AdvisoryType {

    // Public advisory
    TCP(
            "TC Public Advisory",
            Constants.PUBLIC_ADVISORY,
            ".public.new",
            "Advisory Number",
            true),

    // Intermediate Public advisory A
    TCP_A(
            "TC Public Advisory A",
            Constants.PUBLIC_ADVISORY,
            ".public_a.new",
            "Intermediate Advisory Number",
            true),

    // Intermediate Public advisory B (Deprecated)
    TCP_B(
            "TC Public Advisory B",
            "public_advisory_b",
            ".public_b.new",
            "Intermediate Advisory Number",
            true),

    // Forecast/Advisory
    TCM(
            "TC Forecast/Advisory",
            "forecast_advisory",
            ".fstadv.new",
            "Forecast/Advisory Number",
            true),

    // Forecast Discussion
    TCD(
            "TC Forecast Discussion",
            "discussion",
            ".discus.new",
            "Discussion Number",
            true),

    // Wind Speed Probabilities
    PWS(
            "TC Wind Speed Probabilities",
            "wind_speed_probabilities",
            ".wdsprb",
            "Wind Speed Probabilities Number",
            true),

    // Tropical Cyclone Update
    TCU("TC Update", "tc_update", ".update", "", true),

    // ICAO Aviation Advisory
    TCA(
            "TC ICAO Advisory",
            "aviation_advisory",
            ".icaoms.new",
            "ICAO Advisory Number",
            true),

    // ICAO Aviation Advisory data file
    TCA_DAT(
            "TC ICAO Advisory Data",
            "aviation_advisory",
            ".icaoms.dat",
            "",
            false),

    // Tropical Weather Outlook
    TWO(
            "Tropical Weather Outlook",
            "tropical_weather_outlook",
            ".warn",
            "",
            true),

    // Advisory information (stormId.adv)
    TCP_ADV(
            "Advisory Information",
            Constants.PUBLIC_ADVISORY,
            ".adv",
            "",
            true),

    // Advisory summary (stormId.summary)
    TCP_SUM(
            "Advisory Summary",
            Constants.PUBLIC_ADVISORY,
            ".summary",
            "",
            true),

    // Advisory discussion in TCP (stormId.discus)
    TCP_DISCUSS(
            "Advisory Discussion",
            Constants.PUBLIC_ADVISORY,
            ".discus",
            "",
            true),

    // Warning (stormId.warn)
    WARNINGS(
            "Watches and Warnings",
            Constants.PUBLIC_ADVISORY,
            ".warn",
            "",
            false),

    // Head Line (stormId.hdline)
    HEADLINE("Headline", Constants.PUBLIC_ADVISORY, ".hdline", "", false),

    // Hazards (stormId.hazards)
    HAZARDS("Hazards", Constants.PUBLIC_ADVISORY, ".hazards", "", false);

    // Full name of the advisory
    private String name;

    // Handlebar template file name to format advisory, without ".hbs" suffix.
    private String template;

    // Suffix for the file to store the advisory --- stormId.(suffix)
    private String suffix;

    // String part appeared as "ADVISORY NUMBER" in the advisory.
    private String advNumPhrase;

    // True if full product (as opposed to a product fragment)
    private boolean fullProduct;

    private static class Constants {
        public static final String PUBLIC_ADVISORY = "public_advisory";
    }

    /**
     * @param name
     * @param template
     * @param suffix
     */
    private AdvisoryType(String name, String template, String suffix,
            String advNumPhrase, boolean fullPrroduct) {
        this.name = name;
        this.template = template;
        this.suffix = suffix;
        this.advNumPhrase = advNumPhrase;
        this.fullProduct = fullPrroduct;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the name for its handlebar template file.
     */
    public String getTemplate() {
        return template;
    }

    /**
     * @return the file suffix for the advisory.
     */
    public String getSuffix() {
        return suffix;
    }

    /**
     * @return the advNumPhrase
     */
    public String getAdvNumPhrase() {
        return advNumPhrase;
    }

    /**
     * @return true if type is a full product (as opposed to a product
     *         fragment).
     */
    public boolean isFullProduct() {
        return fullProduct;
    }
}