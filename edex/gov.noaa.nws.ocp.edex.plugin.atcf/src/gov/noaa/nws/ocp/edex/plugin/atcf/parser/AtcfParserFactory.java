/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.plugin.atcf.parser;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;

import gov.noaa.nws.ocp.common.dataplugin.atcf.AtcfDeckType;

/**
 * AtcfParserFactory
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * May 15, 2018            pwang       Initial creation
 * Sep 12, 2019 #68237     dfriedman   Improve storm name handling.
 *
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */
public class AtcfParserFactory {
    private static final IUFStatusHandler logger = UFStatus
            .getHandler(AtcfParserFactory.class);

    public static AbstractAtcfParser getParser(final AtcfDeckType deckType) {
        switch (deckType) {
        case A: return new ADeckParser();
        case B: return new BDeckParser();
        case E: return new EDeckParser();
        case F: return new FDeckParser();
        default:
            throw new IllegalArgumentException("No parser for deck type " + deckType);
        }
    }

}
