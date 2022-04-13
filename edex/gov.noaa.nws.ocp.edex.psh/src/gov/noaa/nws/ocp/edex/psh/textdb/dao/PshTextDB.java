/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.psh.textdb.dao;

import java.util.ArrayList;
import java.util.List;

import com.raytheon.uf.common.dataplugin.text.db.StdTextProduct;
import com.raytheon.uf.edex.plugin.text.db.TextDB;

import gov.noaa.nws.ocp.common.dataplugin.psh.PshLSRProduct;

/**
 * PSHTextDBDao
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 17, 2017            pwang     Initial creation
 *
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */
public class PshTextDB {

    private static TextDB tdb = new TextDB();

    public PshTextDB() {

    }

    /**
     * retrieveMetarProduct
     * 
     * @param awispCommand
     * @param site
     * @param operationMode
     * @return
     */
    public static List<StdTextProduct> retrieveMetarProduct(String node,
            String station, String site, boolean operationalMode) {
        StringBuilder sb = new StringBuilder("ALL:");
        sb.append(node);
        sb.append("MTR");
        sb.append(station);

        return tdb.executeAFOSCommand(sb.toString(), null, operationalMode);
        // return tdb.executeAWIPSCommand(sb.toString(), site, operationalMode);

    }

    /**
     * Retrieve LSR products
     * 
     * @return list of PshLSRProducts
     */
    public static List<PshLSRProduct> retrieveLSRProducts(String lsrHeader,
            boolean operationalMode) {
        List<StdTextProduct> products = tdb
                .executeAFOSCommand("ALL:" + lsrHeader, null, operationalMode);
        List<PshLSRProduct> lsrProducts = new ArrayList<>();
        for (StdTextProduct product : products) {
            lsrProducts.add(new PshLSRProduct(product.getInsertTime(),
                    product.getProduct()));
        }
        return lsrProducts;
    }

}
