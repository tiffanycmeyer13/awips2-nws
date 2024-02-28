/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.drawing.elements;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Implementation of an iterator of DrawableElement in order to treat DEs and
 * DECollections the same way.
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
public class SelfIterator implements Iterator<DrawableElement> {

    private DrawableElement de;

    private boolean nextFlag;

    public SelfIterator(DrawableElement de) {
        this.de = de;
        if (de != null) {
            nextFlag = true;
        }
    }

    @Override
    public boolean hasNext() {
        return nextFlag;
    }

    @Override
    public DrawableElement next() {

        if (!hasNext()) {
            throw new NoSuchElementException();
        }

        nextFlag = false;
        return de;
    }

    @Override
    public void remove() {
        // Not used.
    }

}
