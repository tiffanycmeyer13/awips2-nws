/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.plugin.atcf.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.raytheon.edex.exception.DecoderException;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;

import gov.noaa.nws.ocp.common.dataplugin.atcf.AbstractAtcfRecord;
import gov.noaa.nws.ocp.common.dataplugin.atcf.AtcfBasin;
import gov.noaa.nws.ocp.common.dataplugin.atcf.ITrackDeckRecord;
import gov.noaa.nws.ocp.common.dataplugin.atcf.Storm;
import gov.noaa.nws.ocp.edex.plugin.atcf.decoder.AtcfDeckProcessor;

/**
 * AbstractAtcfParser
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 27, 2018            pwang       Initial creation
 * Sep 12, 2019 68237      dfriedman   Improve storm name handling.
 * Jun 16, 2020 79546      dfriedman   Refactor and improve performance.
 * May 17, 2021 91567      jwu         Pull processWarnTimeMM() from FDeckParser.
 * Jun 25, 2021 92918      dfriedman   Rework date parsing.
 *
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */
public abstract class AbstractAtcfParser {
    protected static final IUFStatusHandler statusHandler = UFStatus
            .getHandler(AbstractAtcfParser.class);

    protected static final float RMISSD = AbstractAtcfRecord.RMISSD;

    /*
     * Format for "Warning Date-Time-Group" field.
     */
    protected static final DateTimeFormatter DTG_DATETIME_FORMAT = DateTimeFormatter
            .ofPattern("yyyyMMddHH");

    /*
     * Format for the warning date/time string with minutes (F-Deck, B-Deck
     * landing time)
     */
    private static final DateTimeFormatter DTG_MM_DATETIME_FORMAT = DateTimeFormatter
            .ofPattern("yyyyMMddHHmm");

    /*
     * Regular expression for the latitude or longitude (tenths of degrees)
     */
    protected static final Pattern LATLON_PATTERN = Pattern
            .compile("(\\d{1,4})(N|S|E|W)");

    /*
     * Regular expression for the latitude or longitude to (hundreths of
     * degrees)
     */
    protected static final Pattern LATLON_HUNDRETHS_PATTERN = Pattern
            .compile("(\\d{1,5})(N|S|E|W)");

    protected static final Pattern SPLIT = Pattern.compile("\\s*,\\s*");

    /**
     * Parse deck data from the given stream
     *
     * @param inputStream
     * @param thisStorm
     *            Storm object with year set; will have other fields set based
     *            on deck data.
     * @return
     * @throws DecoderException
     */
    public abstract AbstractAtcfRecord[] parseStream(InputStream inputStream,
            Storm thisStorm) throws DecoderException;

    /**
     * Parses the given deck record.
     *
     * @param theBulletin
     *            a line of text from a deck file with whitespace trimmed
     * @param year
     *            year of the storm (from the deck file name)
     * @return
     */
    protected abstract AbstractAtcfRecord processFields(String theBulletin,
            int year);

    /**
     * Parse deck data from the given file. This is just a convenience wrapper
     * around parseStream and does not interpret the deck file name.
     *
     * @param inputFile
     * @param thisStorm
     *            Storm object with year set; will have other fields set based
     *            on deck data.
     * @return
     * @throws DecoderException
     */
    public AbstractAtcfRecord[] parse(File inputFile, Storm thisStorm)
            throws DecoderException {
        try (FileInputStream fis = new FileInputStream(inputFile)) {
            return parseStream(fis, thisStorm);
        } catch (Exception e) {
            throw new DecoderException(
                    inputFile.toString() + ": " + e.getMessage(), e);
        }
    }

    /**
     * Basic deck stream parser.
     *
     * @param inputStream
     * @param thisStorm
     *            Storm object with year set; will have other fields set based
     *            on deck data.
     * @param recordClass
     *            expected record class; used to create returned array
     * @return
     * @throws DecoderException
     */
    @SuppressWarnings("unchecked")
    protected <T extends AbstractAtcfRecord> AbstractAtcfRecord[] defaultParseStream(
            InputStream inputStream, Storm thisStorm, Class<T> recordClass)
            throws DecoderException {
        List<T> records = new ArrayList<>();
        final int year = thisStorm.getYear();

        try {
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(inputStream));
            String oneline;
            while ((oneline = br.readLine()) != null) {
                try {
                    T r = (T) this.processFields(oneline.trim(), year);
                    records.add(r);
                } catch (RuntimeException e) {
                    statusHandler.error("parsing record \"" + oneline + "\": "
                            + e.toString(), e);
                }
            }
        } catch (IOException e) {
            throw new DecoderException("I/O exception while decoding", e);
        }

        this.populateStormInfo(records, thisStorm);

        return records
                .toArray((T[]) Array.newInstance(recordClass, records.size()));
    }

    protected static String validateBasin(String s) {
        AtcfBasin.valueOf(s);
        return s;
    }

    protected static float processLatLonCommon(String latLonStr,
            Pattern pattern, float scale) {
        Matcher latLonMatcher = pattern.matcher(latLonStr);
        if (latLonMatcher.matches()) {
            int latLonDigits = (Integer.parseInt(latLonMatcher.group(1)));
            char hemisphere = latLonMatcher.group(2).charAt(0);
            float latLon;
            if (hemisphere == 'S' || hemisphere == 'W') {
                latLon = -1 * latLonDigits * scale;
            } else {
                latLon = latLonDigits * scale;
            }
            return latLon;
        } else {
            throw new IllegalArgumentException(
                    "invalid lat or lon value \"" + latLonStr + "\"");
        }
    }

    /*
     * processLatLon: Convert the string latitude/longitude in tenths of a
     * degree with appended N|S|E|W to return the floating point
     * (+|-)latitude/longitude in degrees without N|S|E|W
     *
     * @param latLonStr latitude/longitude string such as 144N or 1056W in
     * tenths
     */
    protected float processLatLon(String latLonStr) {
        return processLatLonCommon(latLonStr, LATLON_PATTERN, 0.1f);
    }

    protected float processLatLon4(String latLonStr) {
        return processLatLonCommon(latLonStr, LATLON_HUNDRETHS_PATTERN, 0.01f);
    }

    /**
     * Parses the warning date/time group field.
     *
     * @param dtgStr
     *            warning date/time string
     */
    protected LocalDateTime parseDtg(String dtgStr) {
        return LocalDateTime.parse(dtgStr, DTG_DATETIME_FORMAT);
    }

    /**
     * Parses the warning date-time group field.
     *
     * @param dtgStr
     *            warning date/time string
     */
    protected Date parseDtgAsDate(String dtgStr) {
        return Date.from(parseDtg(dtgStr).toInstant(ZoneOffset.UTC));
    }

    /**
     * Parses the warning date/time string in format of YYYYMMDDHHMM (F-Deck).
     *
     * Note: B-Deck may have minutes for a storm landing record, but its minute
     * is stored in "technique num", not the warn date/time string, so it needs
     * to be appended to to the date/time string before calling this method.
     *
     * @param dtgMmStr
     *            warning date/time string
     */
    protected Date parseDtgMmAsDate(String dtgMmStr) {
        return Date.from(LocalDateTime.parse(dtgMmStr, DTG_MM_DATETIME_FORMAT)
                .toInstant(ZoneOffset.UTC));
    }

    protected boolean populateSubRegion(String subRegion, Storm thisStorm) {
        String cleanSubRegion = subRegion != null ? subRegion.trim() : null;
        if (cleanSubRegion != null && !cleanSubRegion.isEmpty()) {
            thisStorm.setSubRegion(cleanSubRegion);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Conditionally update a {@code Storm}'s name if the given name's rank is
     * greater than current rank.
     *
     * @param stormName
     * @param curRank
     * @param thisStorm
     * @return
     */
    protected int populateStormName(String stormName, int curRank,
            Storm thisStorm) {
        int rank = AtcfDeckProcessor.stormNameRank(stormName);
        if (rank > curRank && rank >= AtcfDeckProcessor.MIN_USABLE_STORM_RANK) {
            thisStorm.setStormName(stormName);
            return rank;
        } else {
            return curRank;
        }
    }

    protected void populateStormInfo(List<? extends AbstractAtcfRecord> records,
            Storm thisStorm) {
        // Only implemented for some parsers.
    }

    /**
     * Update the {@code Storm} stormName and and subRegion fields from the
     * given (A- or B-deck) records.
     *
     * @param records
     * @param thisStorm
     */
   protected void populateStormInfoFromTrack(
            List<? extends AbstractAtcfRecord> records, Storm thisStorm) {
        int stormNameRank = 0;
        boolean haveSubRegion = false;
        int size = records.size();

        /*
         * Useful storm names tend to be either at the end or beginning of a
         * deck file so only scan those records.
         */
        for (int dir : new int[] { -1, 1 }) {
            int i = dir < 0 ? size - 1 : 0;
            int limit = dir < 0 ? Math.max(0, size - 1000)
                    : Math.min(size - 1, 1000);

            /*
             * Scan while there are records left on this end of the file AND we
             * have not found the best kind of name or have not found a
             * subregion.
             */
            while ((dir < 0 ? i >= limit : i <= limit)
                    && (stormNameRank < AtcfDeckProcessor.MAX_STORM_NAME_RANK
                            || !haveSubRegion)) {
                AbstractAtcfRecord rec = records.get(i);
                if (rec instanceof ITrackDeckRecord) {
                    ITrackDeckRecord trackRecord = (ITrackDeckRecord) records
                            .get(i);
                    stormNameRank = populateStormName(
                            trackRecord.getStormName(), stormNameRank,
                            thisStorm);
                    if (!haveSubRegion) {
                        haveSubRegion = populateSubRegion(
                                trackRecord.getSubRegion(), thisStorm);
                    }
                }
                i += dir;
            }
        }
    }

}