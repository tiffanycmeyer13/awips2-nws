/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.plugin.atcf.parser;

import java.io.InputStream;
import java.util.List;

import com.raytheon.edex.exception.DecoderException;

import gov.noaa.nws.ocp.common.dataplugin.atcf.AbstractAtcfRecord;
import gov.noaa.nws.ocp.common.dataplugin.atcf.ForecastTrackRecord;
import gov.noaa.nws.ocp.common.dataplugin.atcf.Storm;

/**
 * BDeckParser is a parser class for ATCF B deck data
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 12, 2019 #69593     pwang       Initial creation
 * Jun 16, 2020 #79546     dfriedman   Fix various parsing issues.
 *
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */
public class ForecastTrackParser extends AbstractAtcfParser {

    @Override
    public AbstractAtcfRecord[] parseStream(InputStream inputStream,
            Storm thisStorm) throws DecoderException {
        return defaultParseStream(inputStream, thisStorm,
                ForecastTrackRecord.class);
    }

    /**
     * processFields parse one a deck record
     * 
     * @param theBulletin
     * @return
     */
    @Override
    protected ForecastTrackRecord processFields(String theBulletin, int year) {
        ForecastTrackRecord frecord = new ForecastTrackRecord();
        String[] atcfField = SPLIT.split(theBulletin, 37);
        int fieldIdx = atcfField.length;

        /*
         * Fields up to and including the storm location are required. If
         * missing or invalid, will throw a RuntimeException.
         */
        frecord.setBasin(validateBasin(atcfField[0]));
        frecord.setCycloneNum(Integer.parseInt(atcfField[1]));
        frecord.setRefTime(parseDtgAsDate(atcfField[2]));
        if (!atcfField[3].isEmpty()) {
            frecord.setTechniqueNum(Integer.parseInt(atcfField[3]));
        }
        frecord.setTechnique(atcfField[4]);
        frecord.setFcstHour(Integer.parseInt(atcfField[5]));
        frecord.setClat(processLatLon(atcfField[6]));
        frecord.setClon(processLatLon(atcfField[7]));
        if (fieldIdx > 8 && !atcfField[8].isEmpty()) {
            frecord.setWindMax((float) (Integer.parseInt(atcfField[8])));
        }
        if (fieldIdx > 9 && !atcfField[9].isEmpty()) {
            frecord.setMslp((float) (Integer.parseInt(atcfField[9])));
        }
        if (fieldIdx > 10 && !atcfField[10].isEmpty()) {
            frecord.setIntensity(atcfField[10]);
        }
        if (fieldIdx > 11 && !atcfField[11].isEmpty()) {
            frecord.setRadWind((float) (Integer.parseInt(atcfField[11])));
        }
        if (fieldIdx > 12 && !atcfField[12].isEmpty()) {
            frecord.setRadWindQuad(atcfField[12]);
        }
        if (fieldIdx > 13 && !atcfField[13].isEmpty()) {
            frecord.setQuad1WindRad((float) (Integer.parseInt(atcfField[13])));
        }
        if (fieldIdx > 14 && !atcfField[14].isEmpty()) {
            frecord.setQuad2WindRad((float) (Integer.parseInt(atcfField[14])));
        }
        if (fieldIdx > 15 && !atcfField[15].isEmpty()) {
            frecord.setQuad3WindRad((float) (Integer.parseInt(atcfField[15])));
        }
        if (fieldIdx > 16 && !atcfField[16].isEmpty()) {
            frecord.setQuad4WindRad((float) (Integer.parseInt(atcfField[16])));
        }
        if (fieldIdx > 17 && !atcfField[17].isEmpty()) {
            frecord.setClosedP((float) (Integer.parseInt(atcfField[17])));
        }
        if (fieldIdx > 18 && !atcfField[18].isEmpty()) {
            frecord.setRadClosedP((float) (Integer.parseInt(atcfField[18])));
        }
        if (fieldIdx > 19 && !atcfField[19].isEmpty()) {
            frecord.setMaxWindRad((float) (Integer.parseInt(atcfField[19])));
        }
        if (fieldIdx > 20 && !atcfField[20].isEmpty()) {
            frecord.setGust((float) (Integer.parseInt(atcfField[20])));
        }
        if (fieldIdx > 21 && !atcfField[21].isEmpty()) {
            frecord.setEyeSize((float) (Integer.parseInt(atcfField[21])));
        }
        if (fieldIdx > 22 && !atcfField[22].isEmpty()) {
            frecord.setSubRegion(atcfField[22]);
        }
        if (fieldIdx > 23 && !atcfField[23].isEmpty()) {
            frecord.setMaxSeas((float) (Integer.parseInt(atcfField[23])));
        }
        if (fieldIdx > 24 && !atcfField[24].isEmpty()) {
            frecord.setForecaster(atcfField[24]);
        }
        if (fieldIdx > 25 && !atcfField[25].isEmpty()) {
            frecord.setStormDrct((float) (Integer.parseInt(atcfField[25])));
        }
        if (fieldIdx > 26 && !atcfField[26].isEmpty()) {
            frecord.setStormSped((float) (Integer.parseInt(atcfField[26])));
        }
        if (fieldIdx > 27 && !atcfField[27].isEmpty()) {
            frecord.setStormName(atcfField[27]);
        }
        if (fieldIdx > 28 && !atcfField[28].isEmpty()) {
            frecord.setStormDepth(atcfField[28]);
        }
        if (fieldIdx > 29 && !atcfField[29].isEmpty()) {
            frecord.setRadWave((float) (Integer.parseInt(atcfField[29])));
        }
        if (fieldIdx > 30 && !atcfField[30].isEmpty()) {
            frecord.setRadWaveQuad(atcfField[30]);
        }
        if (fieldIdx > 31 && !atcfField[31].isEmpty()) {
            frecord.setQuad1WaveRad((float) (Integer.parseInt(atcfField[31])));
        }
        if (fieldIdx > 32 && !atcfField[32].isEmpty()) {
            frecord.setQuad2WaveRad((float) (Integer.parseInt(atcfField[32])));
        }
        if (fieldIdx > 33 && !atcfField[33].isEmpty()) {
            frecord.setQuad3WaveRad((float) (Integer.parseInt(atcfField[33])));
        }
        if (fieldIdx > 34 && !atcfField[34].isEmpty()) {
            frecord.setQuad4WaveRad((float) (Integer.parseInt(atcfField[34])));
        }
        if (fieldIdx > 35 && !atcfField[35].isEmpty()) {
            frecord.setUserDefined(atcfField[35]);
        }
        if (fieldIdx > 36) {
            String s = atcfField[36];
            // the line should have been trimmed so only check for trailing
            // comma
            if (s.endsWith(",")) {
                s = s.substring(0, s.length() - 1);
            }
            if (!s.isEmpty()) {
                frecord.setUserData(s);
            }
        }

        // Additional data elements
        frecord.setYear(year);

        return frecord;
    }

    /**
     * populateStormInfo
     * 
     * @param drecord
     */
    @Override
    protected void populateStormInfo(List<? extends AbstractAtcfRecord> records,
            Storm thisStorm) {
        if (!records.isEmpty()) {
            ForecastTrackRecord frecord = (ForecastTrackRecord) records.get(0);
            if (frecord.getStormName() != null
                    && !frecord.getStormName().isEmpty()) {
                thisStorm.setStormName(frecord.getStormName());
            }
            if (frecord.getSubRegion() != null
                    && !frecord.getSubRegion().isEmpty()) {
                thisStorm.setSubRegion(frecord.getSubRegion());
            }
        }
    }

}
