/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.drawing.elements.tcm;

import java.util.Calendar;
import java.util.List;

import gov.noaa.nws.ocp.viz.drawing.display.IAttribute;

/**
 * Interface for TCM
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 05, 2018 #48178     jwu         Extracted from NCEP PGEN.
 *
 * </pre>
 *
 * @author B. Yin
 * @version 1.0
 */
public interface ITcm extends IAttribute {
    public double[][] getWindRadius();

    public TcmWindQuarters getWaveQuarters();

    public List<TcmFcst> getTcmFcst();

    public String getStormName();

    public Calendar getAdvisoryTime();

    public int getCentralPressure();

    public int getFcstHr();

    public String getStormType();

    public int getStormNumber();

    public int getAdvisoryNumber();

    public String getBasin();

    public int getEyeSize();

    public int getPositionAccuracy();

    public boolean isCorrection();

}
