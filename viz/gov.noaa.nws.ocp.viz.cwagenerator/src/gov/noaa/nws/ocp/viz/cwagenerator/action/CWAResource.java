/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.viz.cwagenerator.action;

import gov.noaa.nws.ncep.ui.pgen.display.DisplayElementFactory;

import gov.noaa.nws.ncep.ui.pgen.display.IDisplayable;
import gov.noaa.nws.ncep.ui.pgen.display.ISymbolSet;
import gov.noaa.nws.ncep.ui.pgen.elements.AbstractDrawableComponent;
import gov.noaa.nws.ncep.ui.pgen.elements.Symbol;
import gov.noaa.nws.ncep.ui.pgen.elements.SymbolLocationSet;
import gov.noaa.nws.ncep.ui.pgen.rsc.PgenResource;
import gov.noaa.nws.ncep.ui.pgen.rsc.PgenResourceData;

import com.raytheon.uf.viz.core.IGraphicsTarget;
import com.raytheon.uf.viz.core.drawables.PaintProperties;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import com.raytheon.uf.viz.core.rsc.LoadProperties;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateList;

/**
 * 
 * Resource class for CWA generator
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * May 22, 2020 75767      wkwock      Initial creation
 *
 * </pre>
 *
 * @author wkwock
 */
public class CWAResource extends PgenResource {
    public CWAResource(PgenResourceData resourceData,
            LoadProperties loadProperties) {
        super(resourceData, loadProperties);
    }

    @Override
    protected void drawSelected(IGraphicsTarget target,
            PaintProperties paintProps) {
        if (!getAllSelected().isEmpty()) {
            DisplayElementFactory df = new DisplayElementFactory(target,
                    descriptor);
            List<IDisplayable> displayEls = new ArrayList<>();

            Symbol defaultSymbol;

            defaultSymbol = new Symbol(null, new Color[] { Color.lightGray },
                    2.5f, 7.5, false, null, "Marker", "DOT");

            CoordinateList defaultPts = new CoordinateList();

            for (AbstractDrawableComponent el : getAllSelected()) {
                if (el == null) {
                    return;
                } else {
                    for (Coordinate point : el.getPoints()) {
                        defaultPts.add(point, true);
                    }
                }
            }

            if (!defaultPts.isEmpty()) {
                SymbolLocationSet symset = new SymbolLocationSet(defaultSymbol,
                        defaultPts.toCoordinateArray());
                displayEls.addAll(df.createDisplayElements((ISymbolSet) symset,
                        paintProps));
            }

            for (IDisplayable each : displayEls) {
                each.draw(target, paintProps);
                each.dispose();
            }
        }
    }
}
