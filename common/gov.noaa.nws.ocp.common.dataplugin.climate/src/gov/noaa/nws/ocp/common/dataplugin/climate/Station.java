/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.climate;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * Converted from rehost-adapt/adapt/climate/include/TYPE_station_list.h
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Nov 12, 2015            xzhang     Initial creation
 * OCT 06, 2016 20639      wkwock     add clone constructor
 * 12 JAN, 2017 20640      jwu        Add xml annotations
 * 18 MAY 2017  33104      amoore     Clarify coordinate names; coordinates
 *                                    are in degrees.
 * </pre>
 * 
 * @author xzhang
 * @version 1.0
 */

@DynamicSerialize
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "", propOrder = { "icaoId", "stationName", "informId",
        "numOffUTC", "dlat", "dlon" })
public class Station {
    /**
     * Informix station ID key
     */
    @DynamicSerializeElement
    @XmlAttribute(name = "Informix")
    private int informId;

    /**
     * WMO station ID
     */
    @DynamicSerializeElement
    @XmlAttribute(name = "ICAO")
    private String icaoId;

    /**
     * Plain language station name
     */
    @DynamicSerializeElement
    @XmlAttribute(name = "StationName")
    private String stationName;

    /**
     * Num off UTC
     */
    @DynamicSerializeElement
    @XmlAttribute(name = "UTC")
    private short numOffUTC;

    /**
     * flag for standard time all year
     */
    @DynamicSerializeElement
    private short stdAllYear;

    /**
     * station latitude
     */
    @DynamicSerializeElement
    @XmlAttribute(name = "Latitude")
    private double dlat;

    /**
     * station longitude
     */
    @DynamicSerializeElement
    @XmlAttribute(name = "Longitude")
    private double dlon;

    public Station() {
    }

    /**
     * clone a station
     * 
     * @param oldStation
     */
    public Station(Station oldStation) {
        if (oldStation != null) {
            this.informId = oldStation.getInformId();
            this.icaoId = oldStation.getIcaoId();
            this.stationName = oldStation.getStationName();
            this.numOffUTC = oldStation.getNumOffUTC();
            this.dlat = oldStation.getDlat();
            this.dlon = oldStation.getDlon();
            this.stdAllYear = oldStation.getStdAllYear();
        }
    }

    /**
     * @return the informId
     */
    public int getInformId() {
        return informId;
    }

    /**
     * @param informId
     *            the informId to set
     */
    public void setInformId(int informId) {
        this.informId = informId;
    }

    /**
     * @return the icaoId
     */
    public String getIcaoId() {
        return icaoId;
    }

    /**
     * @param icaoId
     *            the icaoId to set
     */
    public void setIcaoId(String icaoId) {
        this.icaoId = icaoId;
    }

    /**
     * @return the stationName
     */
    public String getStationName() {
        return stationName;
    }

    /**
     * @param stationName
     *            the stationName to set
     */
    public void setStationName(String stationName) {
        this.stationName = stationName;
    }

    /**
     * @return the numOffUTC
     */
    public short getNumOffUTC() {
        return numOffUTC;
    }

    /**
     * @param numOffUTC
     *            the numOffUTC to set
     */
    public void setNumOffUTC(short numOffUTC) {
        this.numOffUTC = numOffUTC;
    }

    /**
     * @return the stdAllYear
     */
    public short getStdAllYear() {
        return stdAllYear;
    }

    /**
     * @param stdAllYear
     *            the stdAllYear to set
     */
    public void setStdAllYear(short stdAllYear) {
        this.stdAllYear = stdAllYear;
    }

    /**
     * @return the dlat (degrees)
     */
    public double getDlat() {
        return dlat;
    }

    /**
     * @param dlat
     *            the dlat to set
     */
    public void setDlat(double dlat) {
        this.dlat = dlat;
    }

    /**
     * @return the dlon (degrees)
     */
    public double getDlon() {
        return dlon;
    }

    /**
     * @param dlon
     *            the dlon to set
     */
    public void setDlon(double dlon) {
        this.dlon = dlon;
    }

}
