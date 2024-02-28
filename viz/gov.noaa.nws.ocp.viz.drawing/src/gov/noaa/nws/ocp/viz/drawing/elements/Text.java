/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.drawing.elements;

import java.awt.Color;
import java.util.Arrays;

import org.locationtech.jts.geom.Coordinate;

import gov.noaa.nws.ocp.viz.drawing.display.IAttribute;
import gov.noaa.nws.ocp.viz.drawing.display.IText;

/**
 * Class used to represent a Text drawable element.
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
public class Text extends SinglePointElement implements IText {

    /*
     * name of Font
     */
    private String fontName;

    /*
     * size of Font
     */
    private float fontSize;

    /*
     * Justify text String Right, Left, or Center
     */
    private TextJustification justification;

    /*
     * ithw for software font in legacy NMAP2 Text
     */
    private int ithw;

    /*
     * iwidth for software font in legacy NMAP2 Text
     */
    private int iwidth;

    /*
     * angle of rotation to display String relative to + Xdirection
     */
    private double rotation;

    /*
     * Text should be rotated relative to geograpohic North or X direction of
     * Screen coordinates
     */
    private TextRotation rotationRelativity;

    /*
     * Text Strings to Display: Each element of String[] is displayed on
     * separate line.
     */
    private String[] text;

    /*
     * Regular, Bold, Italic, Bold-Italic
     */
    private FontStyle style;

    /*
     * Half-character offset in X-direction
     */
    private int xOffset;

    /*
     * Half-character offset in Y direction
     */
    private int yOffset;

    /*
     * Mask text background
     */
    private Boolean mask;

    /*
     * outline box around text
     */
    private DisplayType displayType;

    /*
     * Hide or display
     */
    private Boolean hide;

    /*
     * Auto-placed or manually-placed
     */
    private Boolean auto;

    public Text() {
        // default
    }

    /**
     * Constructor to set all attributes of the Text element
     *
     * @param fontName
     *            Name of the font to display
     * @param fontSize
     *            Size of the font to display
     * @param justification
     *            Specified where text is relative to position
     * @see gov.noaa.nws.ocp.viz.drawing.display.IText.TextJustification
     * @param position
     *            - Lat/lon location specifying where to display the text String
     * @param rotation
     *            - display text at this rotation angle relative to +X
     *            direction.
     * @param rotationRelativity
     *            - rotation angle is relative to North or Screen coordinates
     * @see gov.noaa.nws.ocp.viz.drawing.display.IText.TextRotation
     * @param text
     *            The text Strings to display. The text strings in each array
     *            element are displayed on a different line
     * @param style
     *            The font style to use. @see
     *            gov.noaa.nws.ocp.viz.drawing.display.IText.FontStyle
     * @param textColor
     *            Color in which text string should be displayed
     * @param offset
     *            Half-character offset in the X direction applied to @see
     *            #position
     * @param offset2
     *            Half-character offset in the Y direction applied to @see
     *            #position
     * @param mask
     *            Create a background mask behind the text Strings
     * @param outline
     *            type of display around the text Strings.
     */
    public Text(Coordinate[] range, String fontName, float fontSize,
            TextJustification justification, Coordinate position,
            double rotation, TextRotation rotationRelativity, String[] text,
            FontStyle style, Color textColor, int offset, int offset2,
            boolean mask, DisplayType outline, String pgenCategory,
            String pgenType) {
        super(range, new Color[] { textColor }, 1.0f, 0.0, false, position,
                pgenCategory, pgenType);
        this.fontName = fontName;
        this.fontSize = fontSize;
        this.justification = justification;
        this.rotation = rotation;
        this.rotationRelativity = rotationRelativity;
        this.text = text;
        this.style = style;
        xOffset = offset;
        yOffset = offset2;
        this.mask = mask;
        this.displayType = outline;

        this.hide = false;
        this.auto = false;

        this.ithw = 2;
        this.iwidth = 1;
    }

    /**
     * @see gov.noaa.nws.ocp.viz.drawing.display.IText#getFontName()
     */
    @Override
    public String getFontName() {
        return fontName;
    }

    /**
     * @see gov.noaa.nws.ocp.viz.drawing.display.IText#getFontSize()
     */
    @Override
    public float getFontSize() {
        return fontSize;
    }

    /**
     * @see gov.noaa.nws.ocp.viz.drawing.IText#getJustification()
     */
    @Override
    public TextJustification getJustification() {
        return justification;
    }

    /**
     * @see gov.noaa.nws.ocp.viz.drawing.display.IText#getPosition()
     */
    @Override
    public Coordinate getPosition() {
        return getLocation();
    }

    /**
     * @see gov.noaa.nws.ocp.viz.drawing.IText#getRotation()
     */
    @Override
    public double getRotation() {
        return rotation;
    }

    /**
     * @see gov.noaa.nws.ocp.viz.drawing.IText#getRotationRelativity()
     */
    @Override
    public TextRotation getRotationRelativity() {
        return rotationRelativity;
    }

    /**
     * @see gov.noaa.nws.ocp.viz.drawing.IText#getString()
     */
    @Override
    public String[] getString() {
        return text;
    }

    /**
     * @see gov.noaa.nws.ocp.viz.drawing.IText#getStyle()
     */
    @Override
    public FontStyle getStyle() {
        return style;
    }

    /**
     * @see gov.noaa.nws.ncep.ui.pgen.display.IText#getTextColor()
     */
    @Override
    public Color getTextColor() {
        return colors[0];
    }

    /**
     * @see gov.noaa.nws.ocp.viz.drawing.IText#getXOffset()
     */
    @Override
    public int getXOffset() {
        return xOffset;
    }

    /**
     * @see gov.noaa.nws.ocp.viz.drawing.IText#getYOffset()
     */
    @Override
    public int getYOffset() {
        return yOffset;
    }

    /**
     * @see gov.noaa.nws.ocp.viz.drawing.IText#maskText()
     */
    @Override
    public Boolean maskText() {
        return mask;
    }

    /**
     * @see gov.noaa.nws.ocp.viz.drawing.IText#getDisplayType()
     */
    @Override
    public DisplayType getDisplayType() {
        return displayType;
    }

    /**
     * @param fontName
     *            the fontName to set
     */
    public void setFontName(String fontName) {
        if (fontName != null) {
            this.fontName = fontName;
        }
    }

    /**
     * @param fontSize
     *            the fontSize to set
     */
    public void setFontSize(float fontSize) {
        this.fontSize = fontSize;
    }

    /**
     * @param justification
     *            the justification to set
     */
    public void setJustification(TextJustification justification) {
        if (justification != null) {
            this.justification = justification;
        }
    }

    /**
     * @return the ithw
     */
    @Override
    public int getIthw() {
        return ithw;
    }

    /**
     * @param ithw
     *            the ithw to set
     */
    public void setIthw(int ithw) {
        this.ithw = ithw;
    }

    /**
     * @return the iwidth
     */
    @Override
    public int getIwidth() {
        return iwidth;
    }

    /**
     * @param iwidth
     *            the iwidth to set
     */
    public void setIwidth(int iwidth) {
        this.iwidth = iwidth;
    }

    /**
     * @param rotation
     *            the rotation to set
     */
    public void setRotation(double rotation) {
        this.rotation = rotation;
    }

    /**
     * @param rotationRelativity
     *            the rotationRelativity to set
     */
    public void setRotationRelativity(TextRotation rotationRelativity) {
        if (rotationRelativity != null) {
            this.rotationRelativity = rotationRelativity;
        }
    }

    /**
     * @param style
     *            the style to set
     */
    public void setStyle(FontStyle style) {
        if (style != null) {
            this.style = style;
        }
    }

    /**
     * @param offset
     *            the xOffset to set
     */
    public void setXOffset(int offset) {
        xOffset = offset;
    }

    /**
     * @param offset
     *            the yOffset to set
     */
    public void setYOffset(int offset) {
        yOffset = offset;
    }

    /**
     * @param mask
     *            the mask to set
     */
    public void setMask(Boolean mask) {
        if (mask != null) {
            this.mask = mask;
        }
    }

    /**
     * @param outline
     *            the outline to set
     */
    public void setDisplayType(DisplayType outline) {
        if (outline != null) {
            this.displayType = outline;
        }
    }

    /**
     * @param hide
     *            the hide to set
     */
    public void setHide(Boolean hide) {
        this.hide = hide;
    }

    /**
     * @return the hide
     */
    @Override
    public Boolean getHide() {
        return hide;
    }

    /**
     * @param auto
     *            the auto to set
     */
    public void setAuto(Boolean auto) {
        this.auto = auto;
    }

    /**
     * @return the auto
     */
    @Override
    public Boolean getAuto() {
        return auto;
    }

    /**
     * @return the text
     */
    public String[] getText() {
        return text;
    }

    /**
     * @param text
     *            the text to set
     */
    public void setText(String[] text) {
        if (text != null) {
            this.text = text;
        }
    }

    /**
     * Update the attributes
     */
    @Override
    public void update(IAttribute iattr) {
        if (iattr instanceof IText) {
            super.update(iattr);
            IText attr = (IText) iattr;
            this.setFontName(attr.getFontName());
            this.setFontSize(attr.getFontSize());
            this.setJustification(attr.getJustification());
            this.setRotation(attr.getRotation());
            this.setRotationRelativity(attr.getRotationRelativity());
            this.setStyle(attr.getStyle());
            this.setXOffset(attr.getXOffset());
            this.setYOffset(attr.getYOffset());
            this.setMask(attr.maskText());
            this.setDisplayType(attr.getDisplayType());
            this.setHide(attr.getHide());
            this.setAuto(attr.getAuto());
            this.setText(attr.getString());
        }
    }

    /**
     * Creates a copy of this object. This is a deep copy and new objects are
     * created so that we are not just copying references of objects
     */
    @Override
    public DrawableElement copy() {

        /*
         * create a new Text object and initially set its attributes to this
         * one's
         */
        Text newText = new Text();
        newText.update(this);

        /*
         * Set new Color, Strings and Coordinate so that we don't just set
         * references to this object's attributes.
         */
        newText.setColors(new Color[] { new Color(this.getColors()[0].getRed(),
                this.getColors()[0].getGreen(),
                this.getColors()[0].getBlue()) });
        newText.setLocation(new Coordinate(this.getLocation()));
        newText.setFontName(this.getFontName());

        /*
         * new text Strings are created and set, so we don't just set references
         */
        String[] textCopy = Arrays.copyOf(this.getText(),
                this.getText().length);
        newText.setText(textCopy);

        newText.setElemCategory(this.getElemCategory());
        newText.setElemType(this.getElemType());

        newText.setHide(this.hide);
        newText.setAuto(this.auto);

        newText.setIwidth(this.getIwidth());
        newText.setIthw(this.getIthw());

        newText.setParent(this.getParent());

        return newText;
    }

    /**
     * @return the string
     */
    @Override
    public String toString() {
        StringBuilder result = new StringBuilder(getClass().getSimpleName());

        result.append("\nCategory:\t" + elemCategory + "\n");
        result.append("Type:\t" + elemType + "\n");
        if (text != null) {
            for (String st : text) {
                result.append(st + "\n");
            }
        }
        result.append("Color:\t" + colors[0] + "\n");
        result.append("FontName:\t" + fontName + "\n");
        result.append("FontSize:\t" + fontSize + "\n");
        result.append("Justification:\t" + justification + "\n");
        result.append("Rotation:\t" + rotation + "\n");
        result.append("RotationRelativity:\t" + rotationRelativity + "\n");
        result.append("Style:\t" + style + "\n");
        result.append("XOffset:\t" + xOffset + "\n");
        result.append("YOffset:\t" + yOffset + "\n");
        result.append("Mask:\t" + mask + "\n");
        result.append("Outline:\t" + displayType + "\n");
        result.append("Hide:\t" + hide + "\n");
        result.append("Auto:\t" + hide + "\n");

        if (location != null) {
            result.append(
                    "Position:\t" + location.y + "\t" + location.x + "\n");
        } else {
            result.append("Position:\t not defined \n");
        }

        return result.toString();
    }

}
