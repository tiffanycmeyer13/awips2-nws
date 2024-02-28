/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.viz.cwagenerator.drawable;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;

import org.eclipse.swt.graphics.RGB;

import com.raytheon.uf.viz.core.IGraphicsTarget;
import com.raytheon.uf.viz.core.exception.VizException;
import com.raytheon.uf.viz.core.map.MapDescriptor;

import gov.noaa.nws.ocp.viz.cwagenerator.config.PointLatLon;

/**
 * 
 * interface for drawable components
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Sep 9, 2021  28802      wkwock      Initial creation
 *
 * </pre>
 *
 * @author wkwock
 */
@XmlRootElement(name = "drawable")
@XmlSeeAlso({ PointDrawable.class, LineDrawable.class, PolygonDrawable.class })
@XmlAccessorType(XmlAccessType.NONE)
public abstract class AbstractDrawableComponent {
    protected RGB color = new RGB(255, 156, 0);

    protected RGB vorColor = new RGB(0, 255, 0);

    protected RGB selectedColor = new RGB(255, 255, 255);

    protected boolean isSelected = false;

    public void draw(IGraphicsTarget target, MapDescriptor descriptor)
            throws VizException {
    }

    public List<PointLatLon> getPoints() {
        return null;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }
}
