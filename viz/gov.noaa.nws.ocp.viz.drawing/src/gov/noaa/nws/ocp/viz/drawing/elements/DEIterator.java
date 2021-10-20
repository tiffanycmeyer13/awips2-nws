/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.drawing.elements;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.NoSuchElementException;

/**
 * Implementation of an iterator for DECollection
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
public class DEIterator implements Iterator<DrawableElement> {

    private Deque<ListIterator<AbstractDrawableComponent>> stack = new ArrayDeque<>();

    /**
     * Public constructor.
     * 
     * @param iterator
     *            - iterator of the list in the DECollection
     */
    public DEIterator(ListIterator<AbstractDrawableComponent> iterator) {

        stack.push(iterator);

    }

    @Override
    public boolean hasNext() {
        boolean status = false;

        if (!stack.isEmpty()) {

            ListIterator<AbstractDrawableComponent> iterator = stack.peek();
            if (!iterator.hasNext()) {
                stack.pop();
                return hasNext();
            } else {
                // ListIterator<AbstractDrawableComponent> it2 = iterator;
                int pIdx = iterator.previousIndex();
                while (iterator.hasNext()) {
                    AbstractDrawableComponent adc = iterator.next();
                    if (adc instanceof DrawableElement) {
                        status = true;
                        break;
                    } else if (adc instanceof DECollection) {
                        status = hasDE((DECollection) adc);
                        if (status) {
                            break;
                        }
                    }
                }

                // reset iterator
                while (iterator.hasPrevious()) {
                    if (iterator.previousIndex() == pIdx) {
                        break;
                    } else {
                        iterator.previous();
                    }
                }

                // if the last item is an empty DEC,
                // or if the current iterator is empty
                if (!status && !stack.isEmpty()) {
                    stack.pop();
                    return hasNext();
                }

            }
        }

        return status;
    }

    @Override
    public DrawableElement next() {

        if (!hasNext()) {
            throw new NoSuchElementException();
        }

        return (DrawableElement) getNext();
    }

    private AbstractDrawableComponent getNext() {
        Iterator<AbstractDrawableComponent> iterator = stack.peek();
        AbstractDrawableComponent component = (AbstractDrawableComponent) iterator
                .next();
        if (component instanceof DECollection) {
            if (hasDE((DECollection) component)) {
                stack.push(
                        ((DECollection) component).getComponentListIterator());
            }
            return getNext();
        } else {
            return component;
        }
    }

    @Override
    public void remove() {
    }

    /**
     * Check if the input collection contains a DE
     * 
     * @param dec
     * @return
     */
    private boolean hasDE(DECollection dec) {
        boolean status = false;
        Iterator<AbstractDrawableComponent> it = dec.getComponentIterator();
        while (it.hasNext()) {
            AbstractDrawableComponent adc = it.next();
            if (adc instanceof DrawableElement) {
                status = true;
                break;
            } else if (adc instanceof DECollection) {
                status = hasDE((DECollection) adc);
                if (status) {
                    break;
                }
            }
        }

        return status;
    }

}
