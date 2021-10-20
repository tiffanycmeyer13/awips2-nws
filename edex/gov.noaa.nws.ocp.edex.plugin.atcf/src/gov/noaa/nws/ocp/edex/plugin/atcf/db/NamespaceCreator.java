/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.plugin.atcf.db;

import com.raytheon.uf.edex.database.dao.CoreDao;
import com.raytheon.uf.edex.database.dao.IDaoConfigFactory;

/**
 * Creates schema before SchemaManager executes the DDL for ATCF database
 * objects.
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 23, 2018 #53502     dfriedman   Initial creation
 *
 * </pre>
 *
 * @author dfriedman
 * @version 1.0
 */
public class NamespaceCreator {
    private static final String SQL =
            "CREATE SCHEMA IF NOT EXISTS atcf;" +
            "GRANT USAGE ON SCHEMA atcf TO awips, pguser;" +
            "COMMENT ON SCHEMA atcf IS 'AWIPS ATCF Schema';" +
            "ALTER DEFAULT PRIVILEGES IN SCHEMA atcf GRANT INSERT, SELECT, UPDATE, DELETE, TRUNCATE, TRIGGER ON TABLES TO awips, pguser;" +
            "ALTER DEFAULT PRIVILEGES IN SCHEMA atcf GRANT ALL ON SEQUENCES TO awips, pguser;" +
            "ALTER DEFAULT PRIVILEGES IN SCHEMA atcf GRANT ALL ON FUNCTIONS TO awips, pguser;" +
            "ALTER DEFAULT PRIVILEGES IN SCHEMA atcf GRANT ALL ON TYPES TO awips, pguser;";

    public NamespaceCreator(IDaoConfigFactory configFactory,  String dbName) {
        CoreDao dao = new CoreDao(configFactory.forDatabase(dbName, true));
        dao.executeSQLUpdate(SQL);
    }
}
