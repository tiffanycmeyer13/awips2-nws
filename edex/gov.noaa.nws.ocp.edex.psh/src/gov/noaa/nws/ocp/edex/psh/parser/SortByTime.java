package gov.noaa.nws.ocp.edex.psh.parser;

import java.util.Comparator;

/**
 * SortByTime
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 21, 2017            pwang     Initial creation
 * Oct 20, 2020 DR22159    dhaines	 Changed compare to use reftime
 *
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */
public class SortByTime implements Comparator<MetarTextLine> {

    @Override
    public int compare(MetarTextLine a, MetarTextLine b) {
    	long result = b.getReftime() - a.getReftime();
    	if (result == 0) {
    		return 0;
    	} else if (result > 0) {
    		return 1;
    	} else {
    		return -1;
    	}
    }

}
