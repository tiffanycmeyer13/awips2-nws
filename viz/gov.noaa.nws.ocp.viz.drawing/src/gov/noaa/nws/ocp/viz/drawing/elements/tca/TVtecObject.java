/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.drawing.elements.tca;

import java.util.Calendar;
import java.util.HashMap;

import com.raytheon.uf.viz.vtec.VtecObject;
import com.raytheon.uf.viz.vtec.VtecUtil;

/**
 * Object representing the Primary Valid Time Event Code (VTEC). Extends RTS's
 * PVtecObject to add functionality specific to the VTEC lines used in the TCV
 * message, such as allowing null ending event date/time. Also, added comparator
 * to determine priority between VTECs. TCV message.
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
public class TVtecObject extends VtecObject implements Comparable<TVtecObject> {

    private static final String VTEC_CREATE_FORMAT_TCV = "/%s.%s.%s.%s.%s.%04d.%s-000000T0000Z/";

    private static final String COMPARE_FORMAT = "%s.%s.%s";

    private static final HashMap<String, Integer> priority = new HashMap<>();

    /*
     * Priority of possible VTEC codes to appear in TCV
     */
    static {
        priority.put("NEW.HU.W", 0);
        priority.put("NEW.HU.A", 1);
        priority.put("NEW.TR.W", 2);
        priority.put("NEW.TR.A", 3);
        priority.put("CON.HU.W", 4);
        priority.put("CON.HU.A", 5);
        priority.put("CON.TR.W", 6);
        priority.put("CON.TR.A", 7);
        priority.put("CAN.HU.W", 8);
        priority.put("CAN.HU.A", 9);
        priority.put("CAN.TR.W", 10);
        priority.put("CAN.TR.A", 11);
    }

    /**
     * Constructor
     */
    public TVtecObject() {
    }

    /**
     * @param vtec
     */
    public TVtecObject(String vtec) {
        super(vtec);
    }

    /**
     * @param product
     * @param action
     * @param office
     * @param phenomenon
     * @param significance
     * @param sequence
     * @param startTime
     */
    public TVtecObject(String product, String action, String office,
            String phenomenon, String significance, int sequence,
            Calendar startTime) {
        super(action, office, phenomenon, significance, sequence);
        setProduct(product);
        setStartTime(startTime);
        setEndTime(null);
    }

    /**
     * @param office
     * @param phenomenon
     * @param significance
     * @param sequence
     */
    public TVtecObject(String office, String phenomenon, String significance,
            int sequence) {
        super(office, phenomenon, significance, sequence);
    }

    /*
     * need to override for 000000T0000Z
     * 
     * @see com.raytheon.edex.vtec.api.PVtecObject#getVtecString()
     */
    @Override
    public String getVtecString() {
        String vtecStr;

        if (getEndTime() == null) {
            vtecStr = String.format(VTEC_CREATE_FORMAT_TCV, getProduct(),
                    getAction(), getOffice(), getPhenomena(), getSignificance(),
                    getSequence(), VtecUtil.formatVtecTime(getStartTime()));
        } else {
            vtecStr = super.getVtecString();
        }

        return vtecStr;
    }

    /*
     * Compare to another PVTEC object based on a predefined priority
     * 
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(TVtecObject o) {

        if (o == null) {
            throw new IllegalArgumentException(
                    "TVtecObject: Can't compare null");
        }

        int thisPriority = getPriority(this);
        int thatPriority = getPriority(o);

        // this object has greater priority
        if (thisPriority < thatPriority) {
            return -1;
        }

        // object o has greater priority
        else if (thisPriority > thatPriority) {
            return 1;
        } else {
            return 0;
        }

    }

    /*
     * returns an integer value representing this VTECs priority relative to
     * others. Lower value indicates higher priority.
     */
    private int getPriority(TVtecObject o) {

        String str = String.format(COMPARE_FORMAT, o.getAction(),
                o.getPhenomena(), o.getSignificance());

        if (priority.containsKey(str))
            return priority.get(str).intValue();
        else
            return 100;
    }

    /*
     * two TVtecObjects are considered equal if they have the same priority.
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {

        if (obj == null) {
            throw new IllegalArgumentException(
                    "TVtecObject: Can't compare null");
        }

        if (this.getClass() == obj.getClass()) {
            TVtecObject vtec = (TVtecObject) obj;
            if (getPriority(this) == getPriority(vtec)) {
                return true;
            }
        }

        return false;
    }

    /**
     * @return int
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result;
        result = prime * result
                + ((getProduct() == null) ? 0 : getProduct().hashCode());
        result = prime * result
                + ((getAction() == null) ? 0 : getAction().hashCode());
        result = prime * result
                + ((getOffice() == null) ? 0 : getOffice().hashCode());
        result = prime * result
                + ((getPhenomena() == null) ? 0 : getPhenomena().hashCode());
        result = prime * result + ((getSignificance() == null) ? 0
                : getSignificance().hashCode());
        result = prime * result
                + ((getSequence() == null) ? 0 : getSequence().hashCode());
        result = prime * result
                + ((getStartTime() == null) ? 0 : getStartTime().hashCode());
        result = prime * result
                + ((getEndTime() == null) ? 0 : getEndTime().hashCode());
        result = prime * result + priority.hashCode();

        return result;
    }

}
