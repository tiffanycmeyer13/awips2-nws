/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.drawing.elements;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.locationtech.jts.geom.Coordinate;

import gov.noaa.nws.ocp.viz.drawing.display.IAttribute;
import gov.noaa.nws.ocp.viz.drawing.display.IVector.VectorType;
import gov.noaa.nws.ocp.viz.drawing.elements.tca.TCAElement;
import gov.noaa.nws.ocp.viz.drawing.elements.tcm.ITcm;
import gov.noaa.nws.ocp.viz.drawing.elements.tcm.TcmFcst;

/**
 * This factory class is used to create DrawableElement elements from its
 * concrete sub-classes. PGEN can use this factory to create the elements it
 * needs to display without knowing the details of how, as long as the caller
 * implements the IAttribute interface.
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 05, 2018 #48178     jwu         Extracted from NCEP PGEN.
 *
 * </pre>
 *
 * @author jwu
 * @version 1.0
 */
public class DrawableElementFactory {

    public AbstractDrawableComponent create(DrawableType typeName,
            IAttribute attr, String pgenCategory, String pgenType,
            List<Coordinate> locations, AbstractDrawableComponent parent) {

        AbstractDrawableComponent de = null;

        switch (typeName) {

        case LINE:
            de = new Line();
            Line ln = (Line) de;
            ln.setLinePoints(locations);
            break;

        case SYMBOL:
            de = new Symbol();
            Symbol sbl = (Symbol) de;
            sbl.setLocation(locations.get(0));
            break;

        case KINKLINE:
            de = new KinkLine();
            KinkLine kln = (KinkLine) de;
            kln.setLinePoints(locations);
            break;

        case TEXT:
            de = new Text();
            Text txt = (Text) de;
            txt.setLocation(locations.get(0));
            break;

        case ARC:
            de = new Arc();
            Arc arc = (Arc) de;
            arc.setLinePoints(locations);
            break;

        case VECTOR:
            de = new Vector();
            Vector vec = (Vector) de;
            vec.setLocation(locations.get(0));

            vec.setDirectionOnly(false);

            if ("Arrow".equalsIgnoreCase(pgenType)) {
                vec.setVectorType(VectorType.ARROW);
            } else if ("Barb".equalsIgnoreCase(pgenType)) {
                vec.setVectorType(VectorType.WIND_BARB);
            } else if ("Directional".equalsIgnoreCase(pgenType)) {
                vec.setVectorType(VectorType.ARROW);
                vec.setDirectionOnly(true);
            } else if ("Hash".equalsIgnoreCase(pgenType)) {
                vec.setVectorType(VectorType.HASH_MARK);
            } else {
                vec.setVectorType(VectorType.ARROW);
            }

            break;

        case COMBO_SYMBOL:
            de = new ComboSymbol();
            ComboSymbol combo = (ComboSymbol) de;
            combo.setLocation(locations.get(0));
            break;

        case TCA:
            de = new TCAElement();
            break;

        case TCM_FCST:
            de = new TcmFcst(locations.get(0), ((ITcm) attr).getFcstHr(),
                    ((ITcm) attr).getWindRadius());
            break;

        default:
            /*
             * Do nothing.
             */
            break;
        }

        // Set element's Type and Category
        if (de != null) {
            de.setElemCategory(pgenCategory);
            de.setElemType(pgenType);
            de.setParent(parent);
        }

        if (de instanceof DrawableElement && attr != null) {
            ((DrawableElement) de).update(attr);
        }

        return de;
    }

    public AbstractDrawableComponent create(DrawableType typeName,
            IAttribute attr, String pgenCategory, String pgenType,
            Coordinate location, AbstractDrawableComponent parent) {

        List<Coordinate> locations = new ArrayList<>();
        locations.add(location);

        return create(typeName, attr, pgenCategory, pgenType, locations,
                parent);

    }

    public AbstractDrawableComponent create(DrawableType typeName,
            IAttribute attr, String pgenCategory, String pgenType,
            Coordinate[] points, AbstractDrawableComponent parent) {

        List<Coordinate> locations = new ArrayList<>();

        locations.addAll(Arrays.asList(points));

        return create(typeName, attr, pgenCategory, pgenType, locations,
                parent);
    }

}
