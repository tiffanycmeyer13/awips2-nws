/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.edex.plugin.odim.menus;

import com.raytheon.uf.edex.menus.AbstractMenuUtil;

/**
 * Generates menus for the ODIM plugin.
 *
 * Delegates to ODIMMenuUtilImpl for thread safety.
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Sep 12, 2022 DCS 21569  dfriedman   Initial creation
 * </pre>
 *
 * @author dfriedman
 */
public class ODIMMenuUtil extends AbstractMenuUtil {

    @Override
    public void createMenus(String site) {
        ODIMMenuUtilImpl impl = new ODIMMenuUtilImpl();
        impl.createMenus(site);
    }

    @Override
    public void createMenus() {
        throw new UnsupportedOperationException();
    }

    @Override
    protected boolean checkCreated() {
        throw new UnsupportedOperationException();
    }

}
