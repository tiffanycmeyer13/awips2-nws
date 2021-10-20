/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.drawing.display;

import java.awt.Color;

/**
 * Contains properties determining how drawable elements should be displayed,
 * such as Layer attributes.
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
public class DisplayProperties {

    /**
     * Color mode, color, and fill mode used to draw all elements in a layer
     */
    private Boolean layerMonoColor = false;

    private Color layerColor = null;

    private Boolean layerFilled = false;

    /**
     * Constructor
     */
    public DisplayProperties() {
    }

    /**
     * @param layerMonoColor
     * @param layerColor
     * @param layerFilled
     */
    public DisplayProperties(Boolean layerMonoColor, Color layerColor,
            Boolean layerFilled) {
        this.layerMonoColor = layerMonoColor;
        this.layerColor = layerColor;
        this.layerFilled = layerFilled;
    }

    /**
     * @return the layerMonoColor
     */
    public Boolean getLayerMonoColor() {
        return layerMonoColor;
    }

    /**
     * @param layerMonoColor
     *            the layerMonoColor to set
     */
    public void setLayerMonoColor(Boolean layerMonoColor) {
        this.layerMonoColor = layerMonoColor;
    }

    /**
     * @return the layerColor
     */
    public Color getLayerColor() {
        return layerColor;
    }

    /**
     * @param layerColor
     *            the layerColor to set
     */
    public void setLayerColor(Color layerColor) {
        this.layerColor = layerColor;
    }

    /**
     * @return the layerFilled
     */
    public Boolean getLayerFilled() {
        return layerFilled;
    }

    /**
     * @param layerFilled
     *            the layerFilled to set
     */
    public void setLayerFilled(Boolean layerFilled) {
        this.layerFilled = layerFilled;
    }

    /**
     * Two DisplayProperties are equal if their instance fields contain the same
     * values. The LayerColor field can be null.
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {

        if (obj == null) {
            return false;
        }

        if (this.getClass() != obj.getClass()) {
            return false;
        }

        DisplayProperties dp = (DisplayProperties) obj;
        boolean sameColor;
        if ((this.layerColor == null) && (dp.getLayerColor() == null)) {
            sameColor = true;
        } else if ((this.layerColor == null) || (dp.getLayerColor() == null)) {
            sameColor = false;
        } else {
            sameColor = this.layerColor.equals(dp.getLayerColor());
        }

        return (sameColor && this.layerMonoColor.equals(dp.getLayerMonoColor())
                && this.layerFilled.equals(dp.getLayerFilled()));
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((layerColor == null) ? 0 : layerColor.hashCode());
        result = prime * result
                + ((layerFilled == null) ? 0 : layerFilled.hashCode());
        result = prime * result
                + ((layerMonoColor == null) ? 0 : layerMonoColor.hashCode());
        return result;
    }

}
