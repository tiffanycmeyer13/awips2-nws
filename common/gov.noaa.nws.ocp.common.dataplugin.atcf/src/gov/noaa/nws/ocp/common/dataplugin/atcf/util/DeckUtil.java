/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.common.dataplugin.atcf.util;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import gov.noaa.nws.ocp.common.dataplugin.atcf.AtcfDeckType;
import gov.noaa.nws.ocp.common.dataplugin.atcf.Storm;
import gov.noaa.nws.ocp.common.dataplugin.atcf.StormTable;
import gov.noaa.nws.ocp.common.dataplugin.atcf.exception.AtcfException;

/**
 * DeckUtil Call DeckUtil.inferStormFromDeckfileName(fime_name String) can be
 * used in both CAVE and EDEX sides
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * May 20, 2020 78298      pwang       Initial creation
 * Nov 01, 2020 82623      jwu         Use storm.table info.
 *
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */
public class DeckUtil {
    private static final String DECKTYPE = "decktype";

    private static final String BASIN = "basin";

    private static final String NUM = "num";

    private static final String YEAR = "year";

    private static final String EXT = "ext";

    public static final Pattern ATCF_STORM_FILENAME_PATTERN = Pattern
            .compile("^(?<prefix>a2atcf\\.)?(?<" + DECKTYPE + ">[a|b|e|f])?(?<"
                    + BASIN + ">\\w{2})(?<" + NUM + ">\\d{2})(?<" + YEAR
                    + ">\\d{4}).(?<" + EXT + ">\\w{3})");

    private DeckUtil() {

    }

    /**
     * isDeckfileAccessible
     *
     * @param deckPath
     * @return
     */
    public static boolean isDeckfileAccessible(final String deckPath) {
        final Path filePath = Paths.get(deckPath);
        return Files.isReadable(filePath);
    }

    /**
     * inferStormFromDeckfileName
     *
     * @param fileName
     * @return
     * @throws Exception
     */
    public static DeckStormInfo inferStormFromDeckfileName(
            final String fileName) throws AtcfException {
        DeckStormInfo dsi = new DeckStormInfo();

        /*
         * Parse the deck file name to extract deck file type, Basin, Storm
         * number, Year
         */
        final Matcher matcher = ATCF_STORM_FILENAME_PATTERN.matcher(fileName);
        if (matcher.matches()) {

            String stormId = matcher.group(BASIN).toUpperCase()
                    + matcher.group(NUM) + matcher.group(YEAR);

            // Get storm info from storm.table
            Storm stormObj = StormTable.getStorm(stormId);

            try {
                if (matcher.group(DECKTYPE) != null) {
                    dsi.setDeckType(AtcfDeckType
                            .valueOf(matcher.group(DECKTYPE).toUpperCase()));
                }

                // Infer a storm from file name if not find in storm.table.
                if (stormObj == null) {
                    stormObj = new Storm();
                    stormObj.setRegion(matcher.group(BASIN));
                    stormObj.setCycloneNum(
                            Integer.parseInt(matcher.group(NUM)));
                    stormObj.setYear(Integer.parseInt(matcher.group(YEAR)));
                    stormObj.setStormId(stormId);
                }

                dsi.setStorm(stormObj);
                dsi.setExtType(matcher.group(EXT));
                dsi.setStatus(true);
                dsi.setMessage("successfully parsed the deck filename");
            } catch (RuntimeException e) {
                throw new AtcfException(String.format(
                        "Error parsing file name [%s]: %s", fileName, e), e);
            }
        } else {
            String errMsg = "Message: [" + fileName
                    + "] does not match expected pattern: ["
                    + ATCF_STORM_FILENAME_PATTERN + "]";
            dsi.setStatus(false);
            dsi.setMessage(errMsg);
            // stopped here
            throw new AtcfException(errMsg);
        }

        return dsi;

    }

}
