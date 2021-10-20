/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.drawing.display;

/**
 * Class contains static methods for fitting a curve to a set of data points.
 * <P>
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
public class CurveFitter {

    // Private constructor
    private CurveFitter() {
    }

    /**
     * Performs a parametric curve fit to the input data points.
     * <P>
     * "Parametric curve fitting: An alternative to Lagrange interpolation and
     * splines", Y. Akyildiz, Computers in Physics, Vol 8, No 6, Nov/Dec 1994,
     * pp 722-729
     * 
     * @param pts
     *            X, Y coordinates of data points
     * @param density
     *            Used to calculate the number of new points in each segment
     * @return data points defining the curve
     */
    public static double[][] fitParametricCurve(double[][] pts, float density) {

        // Holds evaluated points defining the curve
        double[][] newpts;
        int n = pts.length - 1;
        int[] npts = new int[n];

        /*
         * Do nothing if only one or two data points are given
         */
        if (n < 2) {
            return pts;
        }

        /*
         * Compute the number of intermediate points for each segment
         */
        int sumpts = 0;
        for (int i = 0; i < n; i++) {
            double xdiff = pts[i + 1][0] - pts[i][0];
            double ydiff = pts[i + 1][1] - pts[i][1];
            double chord = Math.sqrt((xdiff * xdiff) + (ydiff * ydiff));
            npts[i] = (int) Math.floor(chord / density) + 1;
            sumpts += npts[i];
        }

        /*
         * Calculate additional endpoints
         */
        double[] first = new double[3];
        double[] last = new double[3];
        if (pts[0][0] == pts[n][0] && pts[0][1] == pts[n][1]) {
            first[0] = pts[n - 1][0];
            first[1] = pts[n - 1][1];
            last[0] = pts[1][0];
            last[1] = pts[1][1];
        } else {
            first[0] = (5 * pts[0][0] - 4 * pts[1][0] + pts[2][0]) / 2.0;
            first[1] = (5 * pts[0][1] - 4 * pts[1][1] + pts[2][1]) / 2.0;
            last[0] = (5 * pts[n][0] - 4 * pts[n - 1][0] + pts[n - 2][0]) / 2.0;
            last[1] = (5 * pts[n][1] - 4 * pts[n - 1][1] + pts[n - 2][1]) / 2.0;
        }

        /*
         * Create a temporary array a data points with the new endpoints
         */
        double[][] tmp = new double[pts.length + 2][3];
        tmp[0] = first;
        for (int j = 0; j < pts.length; j++) {
            tmp[j + 1] = pts[j];
        }
        tmp[pts.length + 1] = last;

        /*
         * Calculate points for the curve.
         */
        int i = 0;
        newpts = new double[sumpts + 1][3];
        newpts[i] = pts[0];
        i++;
        for (int k = 1; k <= n; k++) {
            for (int j = 0; j < npts[k - 1]; j++) {
                double t = (double) (j + 1) / (double) npts[k - 1];
                double[] out = new double[3];
                out[0] = tmp[k][0] + 0.5 * (tmp[k + 1][0] - tmp[k - 1][0]) * t
                        - 0.5 * (tmp[k + 2][0] - 4 * tmp[k + 1][0]
                                + 5 * tmp[k][0] - 2 * tmp[k - 1][0]) * (t * t)
                        + 0.5 * (tmp[k + 2][0] - 3 * tmp[k + 1][0]
                                + 3 * tmp[k][0] - tmp[k - 1][0]) * (t * t * t);
                out[1] = tmp[k][1] + 0.5 * (tmp[k + 1][1] - tmp[k - 1][1]) * t
                        - 0.5 * (tmp[k + 2][1] - 4 * tmp[k + 1][1]
                                + 5 * tmp[k][1] - 2 * tmp[k - 1][1]) * (t * t)
                        + 0.5 * (tmp[k + 2][1] - 3 * tmp[k + 1][1]
                                + 3 * tmp[k][1] - tmp[k - 1][1]) * (t * t * t);
                out[2] = 0.;
                newpts[i] = out;
                i++;
            }
        }

        return newpts;
    }
}
