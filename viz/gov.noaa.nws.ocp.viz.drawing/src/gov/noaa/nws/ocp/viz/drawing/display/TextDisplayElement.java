/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.drawing.display;

import java.util.ArrayList;
import java.util.List;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.core.DrawableString;
import com.raytheon.uf.viz.core.IGraphicsTarget;
import com.raytheon.uf.viz.core.IGraphicsTarget.TextStyle;
import com.raytheon.uf.viz.core.drawables.PaintProperties;
import com.raytheon.uf.viz.core.exception.VizException;

import gov.noaa.nws.ocp.viz.drawing.display.IText.DisplayType;

/**
 * Contains information needed to readily display "text" information to a
 * graphics target.
 * <P>
 * Objects of this class are typically created from PGEN "drawable elements"
 * using the DisplayElementFactory class.
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
public class TextDisplayElement implements IDisplayable {

    private static final IUFStatusHandler handler = UFStatus
            .getHandler(TextDisplayElement.class);

    private boolean hasBackgroundMask;

    private DisplayType displayType;

    private DrawableString dstring;

    /**
     * 
     * @param dstring
     * @param mask
     * @param dType
     * @param box
     */
    public TextDisplayElement(DrawableString dstring, boolean mask,
            DisplayType dType) {

        this.hasBackgroundMask = mask;
        this.displayType = dType;
        this.dstring = dstring;
    }

    /**
     * Disposes any graphic resources held by this object.
     * 
     * @see gov.noaa.nws.ocp.viz.drawing.display.IDisplayable#dispose()
     */
    @Override
    public void dispose() {
        dstring.font.dispose();
    }

    /**
     * Draws the text strings to the specified graphics target
     * 
     * @param target
     *            Destination graphics target
     * @see gov.noaa.nws.ocp.viz.drawing.display.IDisplayable#draw(com.raytheon.viz.core.IGraphicsTarget)
     */
    @Override
    public void draw(IGraphicsTarget target, PaintProperties paintProps) {

        List<DrawableString> listDrawableStrings = new ArrayList<>();

        try {

            switch (displayType) {

            case BOX: {
                dstring.addTextStyle(TextStyle.BOXED);
                break;
            }

            case OVERLINE: {
                dstring.addTextStyle(TextStyle.OVERLINE);
                break;
            }

            case UNDERLINE: {
                dstring.addTextStyle(TextStyle.UNDERLINE);
                break;
            }

            case NORMAL:
            default: {
                /*-
                 * case NORMAL:
                 * TextStyle.Normal is indicated by adding no other styles to a
                 * {@link DrawableString}
                 */
                break;
            }

            }

            // If the text string is to have a blank background behind it
            if (hasBackgroundMask) {
                dstring.addTextStyle(TextStyle.BLANKED);
            }

            listDrawableStrings.add(dstring);
            target.drawStrings(listDrawableStrings);

        } catch (VizException ve) {
            handler.error("TextDiaplayElement- " + ve);
        }

    }

}
