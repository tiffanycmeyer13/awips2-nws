/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.common.drawing.breakpoint;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.locationtech.jts.geom.Coordinate;

import com.raytheon.uf.common.geospatial.adapter.CoordAdapter;

/**
 * Breakpoints are geographic defining points specified in tropical cyclone
 * watches and warnings. (See NWSI 10-605 @ http://www.nws.noaa.gov/directives
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 05, 2018 #48178     jwu         Extracted from NCEP PGEN.
 *
 * </pre>
 *
 * @author sgilbert
 * @version 1.0
 */
@XmlAccessorType(XmlAccessType.NONE)
public class Breakpoint {

    /*
     * breakpoint name
     */
    @XmlAttribute
    private String name;

    @XmlAttribute
    private String state;

    @XmlAttribute
    private String country;

    @XmlAttribute
    @XmlJavaTypeAdapter(value = CoordAdapter.class)
    private Coordinate location;

    /*
     * indicates whether this breakpoint is considered official
     */
    @XmlAttribute
    private boolean official;

    /**
     * default constructor
     */
    public Breakpoint() {
    }

    /**
     * @param name
     *            Breakpoint name
     * @param state
     * @param country
     * @param location
     *            lat/lon coordinate
     * @param official
     *            boolean
     */
    public Breakpoint(String name, String state, String country,
            Coordinate location, boolean official) {
        this.name = name;
        this.state = state;
        this.country = country;
        this.location = location;
        this.official = official;
    }

    /**
     * @return the name of the breakpoint
     */
    public String getName() {
        return name;
    }

    /**
     * @param name
     *            the breakpoint name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the location
     */
    public Coordinate getLocation() {
        return location;
    }

    /**
     * @param location
     *            the location to set
     */
    public void setLocation(Coordinate location) {
        this.location = location;
    }

    /**
     * @return the state
     */
    public String getState() {
        return state;
    }

    /**
     * @param state
     *            the state to set
     */
    public void setState(String state) {
        this.state = state;
    }

    /**
     * @return the country
     */
    public String getCountry() {
        return country;
    }

    /**
     * @param country
     *            the country to set
     */
    public void setCountry(String country) {
        this.country = country;
    }

    /**
     * @return the official
     */
    public boolean isOfficial() {
        return official;
    }

    /**
     * @param official
     *            the official to set
     */
    public void setOfficial(boolean official) {
        this.official = official;
    }

    /**
     * Two breakpoints are considered "equal" if they have the same Name.
     */
    @Override
    public boolean equals(Object obj) {

        boolean retval = false;

        if (obj == null) {
            return false;
        }

        if (this.getClass() == obj.getClass()) {
            Breakpoint tmp = (Breakpoint) obj;
            if (this.getName().equals(tmp.getName())) {
                retval = true;
            }
        }

        return retval;
    }

    /**
     * (non-Javadoc)
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((country == null) ? 0 : country.hashCode());
        result = prime * result
                + ((location == null) ? 0 : location.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + (official ? 1231 : 1237);
        result = prime * result + ((state == null) ? 0 : state.hashCode());
        return result;
    }

}
