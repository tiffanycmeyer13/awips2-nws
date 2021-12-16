/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.plugin.atcf.parser;

import java.io.InputStream;
import java.util.List;

import com.raytheon.edex.exception.DecoderException;

import gov.noaa.nws.ocp.common.dataplugin.atcf.ADeckRecord;
import gov.noaa.nws.ocp.common.dataplugin.atcf.AbstractAtcfRecord;
import gov.noaa.nws.ocp.common.dataplugin.atcf.Storm;

/**
 * ADeckParser is a parser class for ATCF A deck data
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 27, 2018            pwang       Initial creation
 * Sep 12, 2019 #68237     dfriedman   Improve storm name handling.
 * Jun 16, 2020 #79546     dfriedman   Fix various parsing issues.
 *
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */
public class ADeckParser extends AbstractAtcfParser {

    @Override
    public AbstractAtcfRecord[] parseStream(InputStream inputStream, Storm thisStorm)
            throws DecoderException {
        return defaultParseStream(inputStream, thisStorm, ADeckRecord.class);
    }

    /**
     * processFields parse one a deck record
     *
     * @param theBulletin
     * @return
     */
    @Override
    protected ADeckRecord processFields(String theBulletin, int year) {
        ADeckRecord drecord = new ADeckRecord();
        String[] atcfField = SPLIT.split(theBulletin, 37);
        int fieldIdx = atcfField.length;

        /*
         * Fields up to and including the storm location are required. If
         * missing or invalid, will throw a RuntimeException.
         */
        drecord.setBasin(validateBasin(atcfField[0]));
        drecord.setCycloneNum(Integer.parseInt(atcfField[1]));
        drecord.setRefTime(parseDtgAsDate(atcfField[2]));
        if (!atcfField[3].isEmpty()) {
            drecord.setTechniqueNum(Integer.parseInt(atcfField[3]));
        }
        drecord.setTechnique(atcfField[4]);
        drecord.setFcstHour(Integer.parseInt(atcfField[5]));
        drecord.setClat(processLatLon(atcfField[6]));
        drecord.setClon(processLatLon(atcfField[7]));
        if (fieldIdx > 8 && !atcfField[8].isEmpty()) {
            drecord.setWindMax((Integer.parseInt(atcfField[8])));
        }
        if (fieldIdx > 9 && !atcfField[9].isEmpty()) {
            drecord.setMslp((Integer.parseInt(atcfField[9])));
        }
        if (fieldIdx > 10 && !atcfField[10].isEmpty()) {
            drecord.setIntensity(atcfField[10]);
        }
        if (fieldIdx > 11 && !atcfField[11].isEmpty()) {
            drecord.setRadWind((Integer.parseInt(atcfField[11])));
        }
        if (fieldIdx > 12 && !atcfField[12].isEmpty()) {
            drecord.setRadWindQuad(atcfField[12]);
        }
        if (fieldIdx > 13 && !atcfField[13].isEmpty()) {
            drecord.setQuad1WindRad((Integer.parseInt(atcfField[13])));
        }
        if (fieldIdx > 14 && !atcfField[14].isEmpty()) {
            drecord.setQuad2WindRad((Integer.parseInt(atcfField[14])));
        }
        if (fieldIdx > 15 && !atcfField[15].isEmpty()) {
            drecord.setQuad3WindRad((Integer.parseInt(atcfField[15])));
        }
        if (fieldIdx > 16 && !atcfField[16].isEmpty()) {
            drecord.setQuad4WindRad((Integer.parseInt(atcfField[16])));
        }
        if (fieldIdx > 17 && !atcfField[17].isEmpty()) {
            drecord.setClosedP((Integer.parseInt(atcfField[17])));
        }
        if (fieldIdx > 18 && !atcfField[18].isEmpty()) {
            drecord.setRadClosedP((Integer.parseInt(atcfField[18])));
        }
        if (fieldIdx > 19 && !atcfField[19].isEmpty()) {
            drecord.setMaxWindRad((Integer.parseInt(atcfField[19])));
        }
        if (fieldIdx > 20 && !atcfField[20].isEmpty()) {
            drecord.setGust((Integer.parseInt(atcfField[20])));
        }
        if (fieldIdx > 21 && !atcfField[21].isEmpty()) {
            drecord.setEyeSize((Integer.parseInt(atcfField[21])));
        }
        if (fieldIdx > 22 && !atcfField[22].isEmpty()) {
            drecord.setSubRegion(atcfField[22]);
        }
        if (fieldIdx > 23 && !atcfField[23].isEmpty()) {
            drecord.setMaxSeas((Integer.parseInt(atcfField[23])));
        }
        if (fieldIdx > 24 && !atcfField[24].isEmpty()) {
            drecord.setForecaster(atcfField[24]);
        }
        if (fieldIdx > 25 && !atcfField[25].isEmpty()) {
            drecord.setStormDrct((Integer.parseInt(atcfField[25])));
        }
        if (fieldIdx > 26 && !atcfField[26].isEmpty()) {
            drecord.setStormSped((Integer.parseInt(atcfField[26])));
        }
        if (fieldIdx > 27 && !atcfField[27].isEmpty()) {
            drecord.setStormName(atcfField[27]);
        }
        if (fieldIdx > 28 && !atcfField[28].isEmpty()) {
            drecord.setStormDepth(atcfField[28]);
        }
        if (fieldIdx > 29 && !atcfField[29].isEmpty()) {
            drecord.setRadWave((Integer.parseInt(atcfField[29])));
        }
        if (fieldIdx > 30 && !atcfField[30].isEmpty()) {
            drecord.setRadWaveQuad(atcfField[30]);
        }
        if (fieldIdx > 31 && !atcfField[31].isEmpty()) {
            drecord.setQuad1WaveRad((Integer.parseInt(atcfField[31])));
        }
        if (fieldIdx > 32 && !atcfField[32].isEmpty()) {
            drecord.setQuad2WaveRad((Integer.parseInt(atcfField[32])));
        }
        if (fieldIdx > 33 && !atcfField[33].isEmpty()) {
            drecord.setQuad3WaveRad((Integer.parseInt(atcfField[33])));
        }
        if (fieldIdx > 34 && !atcfField[34].isEmpty()) {
            drecord.setQuad4WaveRad((Integer.parseInt(atcfField[34])));
        }
        if (fieldIdx > 35 && !atcfField[35].isEmpty()) {
            drecord.setUserDefined(atcfField[35]);
        }
        if (fieldIdx > 36) {
            String s = atcfField[36];
            // the line should have been trimmed so only check for trailing comma
            if (s.endsWith(",")) {
                s = s.substring(0, s.length() - 1);
            }
            if (!s.isEmpty()) {
                drecord.setUserData(s);
            }
        }

        // Additional data elements
        drecord.setYear(year);

        return drecord;
    }

    @Override
    protected void populateStormInfo(List<? extends AbstractAtcfRecord> records,
            Storm thisStorm) {
        populateStormInfoFromTrack(records, thisStorm);
    }

}
