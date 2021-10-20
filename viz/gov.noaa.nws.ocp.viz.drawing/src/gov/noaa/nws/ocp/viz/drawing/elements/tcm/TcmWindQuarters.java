/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.drawing.elements.tcm;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;

import org.locationtech.jts.geom.Coordinate;

import gov.noaa.nws.ocp.viz.drawing.elements.AbstractDrawableComponent;
import gov.noaa.nws.ocp.viz.drawing.elements.SinglePointElement;

/**
 * Implements a class for TCM wind/wave quarters
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
@XmlAccessorType(XmlAccessType.NONE)
public class TcmWindQuarters extends SinglePointElement
implements ITcmWindQuarter {

    @XmlElements({ @XmlElement(name = "quatro", type = Double.class) })
    private double[] quatro;

    // Wind radii - 32 50 64 knots. 0 = 12ft wave
    @XmlAttribute
    private int windSpeed;

    public TcmWindQuarters() {

    }

    public TcmWindQuarters(Coordinate loc, int spd, double q1, double q2,
            double q3, double q4) {
        quatro = new double[4];
        quatro[0] = q1;
        quatro[1] = q2;
        quatro[2] = q3;
        quatro[3] = q4;
        this.setLocation(loc);
        this.windSpeed = spd;

    }

    @Override
    public AbstractDrawableComponent copy() {
        return new TcmWindQuarters(
                new Coordinate(this.getLocation().x, this.getLocation().y),
                this.getWindSpeed(), quatro[0], quatro[1], quatro[2],
                quatro[3]);
    }

    @Override
    public double[] getQuarters() {
        return quatro;
    }

    public void setWindSpeed(int windSpeed) {
        this.windSpeed = windSpeed;
    }

    @Override
    public int getWindSpeed() {
        return windSpeed;
    }

}
