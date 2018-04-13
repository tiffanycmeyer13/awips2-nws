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
 *
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */
public class SortByTime implements Comparator<MetarTextLine> {

    @Override
    public int compare(MetarTextLine a, MetarTextLine b) {
        return ((b.getDay() * 100000) + b.getHhmm())
                - ((a.getDay() * 100000) + a.getHhmm());
    }

}
