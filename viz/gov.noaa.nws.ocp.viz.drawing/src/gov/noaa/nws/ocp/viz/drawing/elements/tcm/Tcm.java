/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.drawing.elements.tcm;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.locationtech.jts.geom.Coordinate;

import gov.noaa.nws.ocp.viz.drawing.elements.AbstractDrawableComponent;
import gov.noaa.nws.ocp.viz.drawing.elements.MultiPointElement;

/**
 * Implementation for TCM
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

public class Tcm extends MultiPointElement implements ITcm {

    private String stormName;

    private String stormType;

    private int stormNumber;

    private int advisoryNumber;

    private String basin;

    private int eyeSize;

    private int positionAccuracy;

    private boolean correction;

    private Calendar advisoryTime;

    private int centralPressure;

    private TcmWindQuarters waveQuatro;

    private ArrayList<TcmFcst> tcmFcst;

    public Tcm() {
        tcmFcst = new ArrayList<>();
    }

    /**
     * @param stormType
     * @param stormNum
     * @param advisoryNum
     * @param name
     * @param basin
     * @param time
     */
    public Tcm(String stormType, int stormNum, int advisoryNum, String name,
            String basin, Calendar time) {

        this.elemCategory = "MET";
        this.elemType = "TCM";

        this.basin = basin;
        this.stormType = stormType;
        this.stormNumber = stormNum;
        this.advisoryNumber = advisoryNum;
        this.stormName = name;
        this.setAdvisoryTime(time);

        tcmFcst = new ArrayList<>();
    }

    public void addTcmFcst(TcmFcst fcst) {
        tcmFcst.add(fcst);
    }

    @Override
    public AbstractDrawableComponent copy() {
        Tcm newTcm = new Tcm(this.stormType, this.stormNumber,
                this.advisoryNumber, this.stormName, this.basin,
                this.getAdvisoryTime());

        newTcm.setCentralPressure(this.centralPressure);
        newTcm.setEyeSize(this.eyeSize);
        newTcm.setPositionAccuracy(this.positionAccuracy);
        this.setCorrection(this.isCorrection());

        newTcm.setWaveQuatro((TcmWindQuarters) this.waveQuatro.copy());

        for (TcmFcst fcst : tcmFcst) {
            newTcm.addTcmFcst((TcmFcst) fcst.copy());
        }

        newTcm.setStormType(this.getStormType());
        newTcm.setStormNumber(this.getStormNumber());
        newTcm.setAdvisoryNumber(this.getAdvisoryNumber());

        return newTcm;
    }

    @Override
    public double[][] getWindRadius() {
        return new double[1][1];
    }

    @Override
    public List<TcmFcst> getTcmFcst() {
        return tcmFcst;
    }

    public void setTcmFcst(List<TcmFcst> fcst) {
        this.tcmFcst = (ArrayList<TcmFcst>) fcst;
    }

    @Override
    public String getStormName() {
        return stormName;
    }

    @Override
    public int getCentralPressure() {
        return centralPressure;
    }

    public void setAdvisoryTime(Calendar time) {
        this.advisoryTime = time;
    }

    @Override
    public Calendar getAdvisoryTime() {
        return advisoryTime;
    }

    @Override
    public int getFcstHr() {
        return 0;
    }

    public void setWaveQuatro(TcmWindQuarters waveQuatro) {
        this.waveQuatro = waveQuatro;
    }

    @Override
    public TcmWindQuarters getWaveQuarters() {
        return waveQuatro;
    }

    public void setStormType(String stormType) {
        this.stormType = stormType;
    }

    @Override
    public String getStormType() {
        return stormType;
    }

    public void setStormNumber(int stormNumber) {
        this.stormNumber = stormNumber;
    }

    @Override
    public int getStormNumber() {
        return stormNumber;
    }

    public void setAdvisoryNumber(int advisoryNumber) {
        this.advisoryNumber = advisoryNumber;
    }

    @Override
    public int getAdvisoryNumber() {
        return advisoryNumber;
    }

    public void setBasin(String basin) {
        this.basin = basin;
    }

    @Override
    public String getBasin() {
        return basin;
    }

    public void setEyeSize(int eyeSize) {
        this.eyeSize = eyeSize;
    }

    @Override
    public int getEyeSize() {
        return eyeSize;
    }

    @Override
    public int getPositionAccuracy() {
        return positionAccuracy;
    }

    public void setPositionAccuracy(int positionAccuracy) {
        this.positionAccuracy = positionAccuracy;
    }

    public void setCorrection(boolean correction) {
        this.correction = correction;
    }

    @Override
    public boolean isCorrection() {
        return correction;
    }

    public void setStormName(String stormName) {
        this.stormName = stormName;
    }

    public void setCentralPressure(int centralPressure) {
        this.centralPressure = centralPressure;
    }

    @Override
    public List<Coordinate> getPoints() {
        ArrayList<Coordinate> ret = new ArrayList<>();
        for (TcmFcst fcst : tcmFcst) {
            ret.add(fcst.getLocation());
        }
        return ret;
    }

    @Override
    public Coordinate[] getLinePoints() {
        return this.getPoints()
                .toArray(new Coordinate[this.getPoints().size()]);

    }

}
