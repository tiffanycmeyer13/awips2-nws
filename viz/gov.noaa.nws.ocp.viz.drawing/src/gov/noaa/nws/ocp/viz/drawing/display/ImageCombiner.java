/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.drawing.display;

import org.eclipse.jface.resource.CompositeImageDescriptor;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;

/**
 * This class combines two SWT images together. It adds the second image to the
 * right of the first image.
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
public class ImageCombiner extends CompositeImageDescriptor {

    private ImageData image1;

    private ImageData image2;

    /**
     * Constructor specifying the two images
     * 
     * @param image1
     * @param image2
     */
    public ImageCombiner(ImageData image1, ImageData image2) {
        this.image1 = image1;
        this.image2 = image2;
    }

    public void drawComposite() {
        // null implementation
    }

    /**
     * @see org.eclipse.jface.resource.CompositeImageDescriptor#drawCompositeImage(
     *      int, int)
     */
    @Override
    protected void drawCompositeImage(int width, int height) {
        /*
         * add image2 to the right of image1
         */
        this.drawImage(image1, 0, 0);
        this.drawImage(image2, image1.width, 0);
    }

    /**
     * @see org.eclipse.jface.resource.CompositeImageDescriptor#getSize()
     */
    @Override
    protected Point getSize() {
        return new Point(image1.width + image2.width, image1.height);
    }

}
