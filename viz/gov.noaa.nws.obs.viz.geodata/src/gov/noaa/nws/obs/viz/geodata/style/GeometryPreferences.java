package gov.noaa.nws.obs.viz.geodata.style;

import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;

import com.raytheon.uf.common.colormap.Color;
import com.raytheon.uf.common.style.image.ImagePreferences;
import com.raytheon.uf.viz.core.IGraphicsTarget.PointStyle;

import gov.noaa.nws.obs.common.dataplugin.geodata.FloatAttribute;
import gov.noaa.nws.obs.common.dataplugin.geodata.IntegerAttribute;

/**
 * Geometry Preferences
 *
 * Class which describes the StyleRules pertaining to a DBGeo Geometry. Extends
 * the base ImageryStyleRules to handle the sizing, coloring, fill, and sampling
 * of a record stored in the DBGeo plugin table.
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#     Engineer    Description
 * ------------ ----------  ----------- --------------------------
 * 08/20/2016     19064     mcomerford  Initial creation
 * </pre>
 *
 * @author matt.comerford
 * @version 1.0
 */

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = "geometryStyle")
public class GeometryPreferences extends ImagePreferences {

    /*
     * lineWidth, lineColor, and the sampling cutoff for each Geometry may be
     * user-specified (e.g. constant value), or it may be calculated by any one
     * of the Geometry's associated attributes.
     */
    @XmlElements({ @XmlElement(name = "lineWidthVal", type = Float.class),
            @XmlElement(name = "lineWidthFloatAtt", type = FloatAttribute.class),
            @XmlElement(name = "lineWidthIntAtt", type = IntegerAttribute.class) })
    private Object lineWidth;

    @XmlElements({ @XmlElement(name = "lineColoVal", type = Color.class),
            @XmlElement(name = "lineColorFloatAtt", type = FloatAttribute.class),
            @XmlElement(name = "lineColorIntAtt", type = IntegerAttribute.class) })
    private Object lineColor;

    @XmlElements({ @XmlElement(name = "sampleCutoffVal", type = Double.class),
            @XmlElement(name = "sampleCutoffFloatAtt", type = FloatAttribute.class),
            @XmlElement(name = "sampleCutoffIntAtt", type = IntegerAttribute.class) })
    private Object sampleCutoff;

    /* The list of Geometry Attribute conversion information */
    @XmlElement(name = "attributeConvert")
    private Set<GenericGeometryStyleAttribute> attConversions = new HashSet<>();

    /* Handling scaling and styling of any points rendered by the resource */
    @XmlElement(name = "minPointMag")
    private float minMagnification = 1.0f;

    @XmlElement(name = "maxPointMag")
    private float maxMagnification = 1.0f;

    /*
     * Handles how to display any PointRenderables rendered through any given
     * GeoDataResource.
     */
    @XmlElement(name = "pointDisplay")
    private PointStyle pointDisplay = PointStyle.POINT;

    /**
     * Default constructor.
     */
    public GeometryPreferences() {

    }

    /**
     * Default the GeometryPreferences (if none exists through a StyleRule).
     *
     * @param lineWidth
     *            The default value to set for the lineWidth (or radius for a
     *            point).
     * @param lineColor
     *            The default color to set for the rendered Geometry
     * @param sampleCutoff
     *            The default sampleCutoff to set for the rendered Geometry.
     * @param filled
     *            Whether or not the rendered Geometry (point) will be filled.
     */
    public GeometryPreferences(Float lineWidth, Color lineColor,
            Double sampleCutoff) {
        super();
        this.lineWidth = lineWidth;
        this.lineColor = lineColor;
        this.sampleCutoff = sampleCutoff;
    }

    /**
     * @return the lineWidth
     */
    public Object getLineWidth() {
        return lineWidth;
    }

    /**
     * @param lineWidth
     *            the lineWidth to set
     */
    public void setLineWidth(Object lineWidth) {
        this.lineWidth = lineWidth;
    }

    /**
     * @return the lineColor
     */
    public Object getLineColor() {
        return lineColor;
    }

    /**
     * @param lineColor
     *            the lineColor to set
     */
    public void setLineColor(Object lineColor) {
        this.lineColor = lineColor;
    }

    /**
     * @return the sampleCutoff
     */
    public Object getSampleCutoff() {
        return sampleCutoff;
    }

    /**
     * @param sampleCutoff
     *            the sampleCutoff to set
     */
    public void setSampleCutoff(Object sampleCutoff) {
        this.sampleCutoff = sampleCutoff;
    }

    /**
     * @return the attConversions
     */
    public Set<GenericGeometryStyleAttribute> getAttConversions() {
        return attConversions;
    }

    /**
     * @param attConversions
     *            the attConversions to set
     */
    public void setAttConversions(
            Set<GenericGeometryStyleAttribute> attConversions) {
        this.attConversions = attConversions;
    }

    /**
     * @return the minMagnification
     */
    public float getMinMagnification() {
        return minMagnification;
    }

    /**
     * @param minMagnification
     *            the minMagnification to set
     */
    public void setMinMagnification(float minMagnification) {
        this.minMagnification = minMagnification;
    }

    /**
     * @return the maxMagnification
     */
    public float getMaxMagnification() {
        return maxMagnification;
    }

    /**
     * @param maxMagnification
     *            the maxMagnification to set
     */
    public void setMaxMagnification(float maxMagnification) {
        this.maxMagnification = maxMagnification;
    }

    /**
     * @return the pointDisplay
     */
    public PointStyle getPointDisplay() {
        return pointDisplay;
    }

    /**
     * @param pointDisplay
     *            the pointDisplay to set
     */
    public void setPointDisplay(PointStyle pointDisplay) {
        this.pointDisplay = pointDisplay;
    }

}
