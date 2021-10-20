/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.atcf;

import java.io.Serializable;

/**
 * Defines the components of a primary key for sandbox elements. This consists
 * of a reference to the sandbox and the ID of the original deck record.
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
public class SandboxEntryPK implements Serializable {

    private static final long serialVersionUID = 1L;

    private Sandbox sandbox;

    private int id;

    public Sandbox getSandbox() {
        return sandbox;
    }

    public void setSandbox(Sandbox sandbox) {
        this.sandbox = sandbox;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + id;
        result = prime * result
                + ((sandbox == null) ? 0 : Integer.hashCode(sandbox.getId()));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        SandboxEntryPK other = (SandboxEntryPK) obj;
        if (id != other.id) {
            return false;
        }
        if (sandbox == null) {
            if (other.sandbox != null) {
                return false;
            }
        } else if ( sandbox.getId() != other.sandbox.getId()) {
            return false;
        }
        return true;
    }
}
