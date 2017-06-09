/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.common.climate.util;

import java.util.Calendar;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.time.util.TimeUtil;

import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateDate;
import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateTime;
import gov.noaa.nws.ocp.common.dataplugin.climate.parameter.ParameterFormatClimate;

/**
 * Imported from sunlib.c. Utility functions for operations related to the Sun.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 2016                    xzhang      Initial creation
 * 19 AUG 2016  20753      amoore      EDEX common utils consolidation and cleanup.
 * 17 NOV 2016  21378      amoore      Clean up and complete implementation.
 * 24 JAN 2017  28499      amoore      Make final, and have private constructor.
 * 23 MAR 2017  30515      amoore      Replace constants that are already defined in AWIPS.
 * 13 APR 2017  33104      amoore      Address comments from review.
 * 18 MAY 2017  33104      amoore      Consolidate duplicate code.
 * 19 MAY 2017  33104      amoore      Fix calculations for sunrise/sunset. Bad division.
 * </pre>
 * 
 * @author xzhang
 *
 */
public final class SunLib {

    /** The logger */
    private static final IUFStatusHandler logger = UFStatus
            .getHandler(SunLib.class);

    /**
     * January 1st, 2000, at midnight.
     */
    private static final long EPOCH = 946728000;

    /**
     * Base longitude of the sun.
     */
    private static final double LONG_OF_SUN = 280.460;

    /**
     * Longitudinal correction for the sun based on aberration of light.
     */
    private static final double LONG_SUN_ABERRATION = 0.9856474;

    /**
     * Base anomaly of the sun.
     */
    private static final double ANOMALY_SUN = 357.528;

    /**
     * Anomaly correction for the sun based on aberration of light.
     */
    private static final double ANOMALY_SUN_ABERRATION = 0.9856003;

    /**
     * Ecliptic latitude correction for the sun based on aberration of light.
     */
    private static final double ECLIPTIC_SUN_ABERRATION = 0.0000004;

    /**
     * Days per century.
     */
    private static final double DAYS_PER_CENTURY = 36524.0;

    public enum Type {
        RISET, SUN_UP, SUN_DOWN
    };

    /**
     * Imported from sunlib.c.
     * 
     * <pre>
     * 
     * SOFTWARE HISTORY
     * 
     * Date         Ticket#    Engineer    Description
     * ------------ ---------- ----------- --------------------------
     * 2016                    xzhang      Initial creation
     * 17 NOV 2016  21378      amoore      Clean up and complete implementation.
     * 
     * </pre>
     * 
     * @author xzhang
     *
     */
    public static class RiseSet {
        private Type type;

        private double riset;

        /**
         * Migrated from sunlib.C.
         * 
         * calculates sunrise and sunset [sidereal days] year, month, day: date
         * lat: latitude [deg] lon: longitude [deg] riset: returned time [hour
         * LT] sunrise: 1 for sunrise, 0 for sunset zone: number of hours from
         * UTC
         * 
         * @param year
         * @param month
         * @param day
         * @param lat
         * @param lon
         * @param setRiseFlag
         *            true for sunset, false for sunrise
         * @param zone
         * @return
         */
        public static RiseSet calculateRiseSet(int year, int month, int day,
                double lat, double lon, boolean setRiseFlag, int zone) {
            double riset = 0;
            /* start value */
            /*
             * each timezone is 15 degrees of longitude, get zone offset
             */
            double zoneOffset = 12.0 - (lon / 15.0);
            double T = numCenturies(
                    absTime(year, month, day, (int) (zoneOffset)));

            /* Calculate the sunrise or sunset */
            double UT1 = calcTime(T, lat, lon, setRiseFlag, zoneOffset);
            /*
             * if no sunrise or sunset, exit this routine
             */
            if (Math.abs(UT1) == 0) {
                return new RiseSet(Type.SUN_DOWN, riset);
            } else if (Math.abs(UT1) == 180)
            /*
             * We need to consider the possibility that the sun set before it
             * rose today. This can happen at high latitudes, where the sunset
             * may set close to midnight, which is shifted into the next day
             * because the selection of time zone doesn't coincide well with the
             * position of the sun. In other words, we need to consider the
             * possibility that the sunset associated with yesterday, actually
             * occurred today.
             */
            {
                T = T - (1.0 / DAYS_PER_CENTURY);
                UT1 = calcTime(T, lat, lon, setRiseFlag, zoneOffset);
                /*
                 * if the sun is up all day or down all day as a result of the
                 * adjustment to LT, exit this routine
                 */
                if (Math.abs(UT1) == 0) {
                    return new RiseSet(Type.SUN_DOWN, riset);
                } else if (Math.abs(UT1) == 180) {
                    return new RiseSet(Type.SUN_UP, riset);
                }
                /*
                 * Otherwise, compute the sunrise/sunset time in local time
                 * coordinates
                 */
                double LT = UT1 + zone;
                /*
                 * The sunrise or sunset doesn't occur in this calendar day as a
                 * result of of the adjustment to LT
                 */
                if (LT < TimeUtil.HOURS_PER_DAY) {
                    return new RiseSet(Type.SUN_UP, riset);
                } else {
                    riset = LT - TimeUtil.HOURS_PER_DAY;
                    return new RiseSet(Type.RISET, riset);
                }
            }

            /*
             * Now check to determine if we've calculated a sunrise/sunset for
             * the correct day in local time. If not adjust the day and try
             * again.
             */
            double LT = UT1 + zone;
            if (LT > TimeUtil.HOURS_PER_DAY) {
                T = T - (1.0 / DAYS_PER_CENTURY);
                UT1 = calcTime(T, lat, lon, setRiseFlag, zoneOffset);
                /*
                 * if the sun is up all day or down all day as a result of the
                 * adjustment to LT, exit this routine
                 */
                if (Math.abs(UT1) == 0) {
                    return new RiseSet(Type.SUN_DOWN, riset);
                } else if (Math.abs(UT1) == 180) {
                    return new RiseSet(Type.SUN_UP, riset);
                }

                /*
                 * Otherwise, compute the sunrise/sunset time in local time
                 * coordinates
                 */
                LT = UT1 + zone;

                /*
                 * The sunrise or sunset doesn't occur in this calendar day as a
                 * result of of the adjustment to LT
                 */
                if (LT < TimeUtil.HOURS_PER_DAY) {
                    if (setRiseFlag) {
                        return new RiseSet(Type.SUN_DOWN, riset);
                    } else {
                        return new RiseSet(Type.SUN_UP, riset);
                    }
                }
            } else if (LT < 0) {
                T = T + (1.0 / DAYS_PER_CENTURY);
                UT1 = calcTime(T, lat, lon, setRiseFlag, zoneOffset);
                LT = UT1 + zone;
                /*
                 * The sunrise or sunset doesn't occur in this calendar day as a
                 * result of the adjustment to LT
                 */
                if (LT > 0) {
                    if (setRiseFlag) {
                        return new RiseSet(Type.SUN_DOWN, riset);
                    } else {
                        return new RiseSet(Type.SUN_UP, riset);
                    }
                }
            }
            LT = UT1 + zone;
            while (LT >= TimeUtil.HOURS_PER_DAY) {
                LT -= TimeUtil.HOURS_PER_DAY;
            }
            while (LT < 0.0) {
                LT += TimeUtil.HOURS_PER_DAY;
            }

            riset = LT;
            return new RiseSet(Type.RISET, riset);
        }

        public RiseSet(Type type, double riset) {
            this.type = type;
            this.riset = riset;
        }

        public Type getType() {
            return type;
        }

        public void setType(Type type) {
            this.type = type;
        }

        public double getRiset() {
            return riset;
        }

        public void setRiset(double riset) {
            this.riset = riset;
        }

    }

    /**
     * Private constructor. This is a utility class.
     */
    private SunLib() {
    }

    /**
     * Migrated from rise_sun.c and set_sun.c.
     * 
     * <pre>
     *  void rise_sun (int&          year,
    *                 int&          mon,
    *                 int&          day, 
    *                 double&       dlat,
    *                 double&       dlon,
    *                 int&          isun,
    *                 double&       ssday,
    *                 climate_time  *sunrise
    *                 )    
    *
    *   Jason Tuell        PRC/TDL             HP 9000/7xx
    *   Dan Zipper                 PRC/TDL\
    *   Jason Tuell                PRC/TDL                   Mar 99
    *       * Corrected problem with negative hours when the sunrise was at 00Z 
    *         and updated code to account for fractional days < 0 and > 1.
    *
    *
    *   FUNCTION DESCRIPTION
    *   ====================
    *
    *   This function will calculate the sunrise of a given station in UTC time. The variables
    *   inputted into the function are the yyyy-mm-dd. Latitude and longtitude must be converted
    *   from degrees to radians before being used.
    *
    *   This routine was adapted from a function written by George Trojan called Sun.C.
    *
    *   VARIABLES
    *   =========
    *
    *   name                   description
    *-------------------------------------------------------------------------------                   
    *    Input
    *     day                 - The two digit integer day.
    *     dlat                - latitude in degrees (double)
    *     dlon                - longitude (East positive) in degrees (double) 
    *     mon                 - The two digit integer month.
    *     year                - The four digit integer year.
    *
    *    Output
    *     isun                - Determines whether it is sunrise or sunset.
    *
    *    Local   
    *     int rc              - integer value used for deciding a case statement
    *     int temp_lat        - A temporary variable used so that a latitude in degrees
    *                           can be used later in the programs without having to 
    *                           convert back to radians.
    *     int temp_lon        - A temporary variable used so that a latitude in degrees
    *                           can be used later in the programs without having to 
    *                           convert back to radians.
    *  program output:
    *
    *  MODIFICATION HISTORY
    *  --------------------
    *   6/22/01     Doug Murphy               lat and lon no longer need to be
    *                                         converted into radians before
    *                                         being passed into RiseSet...
    *                                         riset is now returned as local
    *                                         time hrs
     * </pre>
     * 
     * @param date
     * @param dlat
     * @param dlon
     * @param numOffUTC
     * @param sunriseTime
     */
    public static void riseSun(ClimateDate date, double dlat, double dlon,
            int numOffUTC, ClimateTime sunriseTime) {
        riseSetSun(date, dlat, dlon, numOffUTC, sunriseTime, false);
    }

    /**
     * Migrated from rise_sun.c and set_sun.c.
     * 
     * <pre>
     *  *  void set_sun  (int&          year,
    *                 int&          mon,
    *                 int&          day,
    *                 double&       dlat,
    *                 double&       dlon,
    *                 climate_time  *sunrise
    *                 )    
    *
    *   Jason Tuell        PRC/TDL             HP 9000/7xx
    *   Dan Zipper                 PRC/TDL
    *   Jason Tuell                PRC/TDL                   Mar 99
    *       * Corrected problem with negative hours when the sunset was at 00Z 
    *         and updated code to account for fractional days < 0 and > 1.*
    *
    *   FUNCTION DESCRIPTION
    *   ====================
    *
    *   This function will calculate the sunset of a given station in UTC time. The variables
    *   inputted into the function are the yyyy-mm-dd. Latitude and longtitude must be converted
    *   from degrees to radians before being used.
    *
    *   This routine was adapted from a function written by George Trojan called Sun.C.
    *
    *   VARIABLES
    *   =========
    *
    *   name                   description
    *-------------------------------------------------------------------------------                   
    *    Input
    *     day                 - The two digit integer day.
    *     dlat                - latitude in degrees (double)
    *     dlon                - longitude (East positive) in degrees (double) 
    *     mon                 - The two digit integer month.
    *     year                - The four digit integer year.
    *
    *    Output
    *     isun                - Determines whether it is sunrise or sunset.
    *
    *    Local
    *     int rc              - integer value used for deciding a case statement
    *     int temp_lat        - A temporary variable used so that a latitude in degrees
    *                           can be used later in the programs without having to 
    *                           convert back to radians.
    *     int temp_lon        - A temporary variable used so that a latitude in degrees
    *                           can be used later in the programs without having to 
    *                           convert back to radians. 
    *
    *  MODIFICATION HISTORY
    *  --------------------
    *   6/22/01     Doug Murphy               lat and lon no longer need to be
    *                                         converted into radians before
    *                                         being passed into RiseSet...
    *                                         riset is now returned as local time hrs
     * </pre>
     * 
     * @param date
     * @param dlat
     * @param dlon
     * @param numOffUTC
     * @param sunsetTime
     */
    public static void setSun(ClimateDate date, double dlat, double dlon,
            int numOffUTC, ClimateTime sunsetTime) {
        riseSetSun(date, dlat, dlon, numOffUTC, sunsetTime, true);
    }

    /**
     * Migrated from rise_sun.c and set_sun.c.
     * 
     * <pre>
     *  void rise_sun (int&          year,
    *                 int&          mon,
    *                 int&          day, 
    *                 double&       dlat,
    *                 double&       dlon,
    *                 int&          isun,
    *                 double&       ssday,
    *                 climate_time  *sunrise
    *                 )    
    *
    *   Jason Tuell        PRC/TDL             HP 9000/7xx
    *   Dan Zipper                 PRC/TDL\
    *   Jason Tuell                PRC/TDL                   Mar 99
    *       * Corrected problem with negative hours when the sunrise was at 00Z 
    *         and updated code to account for fractional days < 0 and > 1.
    *
    *
    *   FUNCTION DESCRIPTION
    *   ====================
    *
    *   This function will calculate the sunrise of a given station in UTC time. The variables
    *   inputted into the function are the yyyy-mm-dd. Latitude and longtitude must be converted
    *   from degrees to radians before being used.
    *
    *   This routine was adapted from a function written by George Trojan called Sun.C.
    *
    *   VARIABLES
    *   =========
    *
    *   name                   description
    *-------------------------------------------------------------------------------                   
    *    Input
    *     day                 - The two digit integer day.
    *     dlat                - latitude in degrees (double)
    *     dlon                - longitude (East positive) in degrees (double) 
    *     mon                 - The two digit integer month.
    *     year                - The four digit integer year.
    *
    *    Output
    *     isun                - Determines whether it is sunrise or sunset.
    *
    *    Local   
    *     int rc              - integer value used for deciding a case statement
    *     int temp_lat        - A temporary variable used so that a latitude in degrees
    *                           can be used later in the programs without having to 
    *                           convert back to radians.
    *     int temp_lon        - A temporary variable used so that a latitude in degrees
    *                           can be used later in the programs without having to 
    *                           convert back to radians.
    *  program output:
    *
    *  MODIFICATION HISTORY
    *  --------------------
    *   6/22/01     Doug Murphy               lat and lon no longer need to be
    *                                         converted into radians before
    *                                         being passed into RiseSet...
    *                                         riset is now returned as local
    *                                         time hrs
     * </pre>
     * 
     * @param date
     * @param dlat
     * @param dlon
     * @param numOffUTC
     * @param setRiseTime
     * @param setRiseFlag
     *            true for sunset, false for sunrise
     */
    public static void riseSetSun(ClimateDate date, double dlat, double dlon,
            int numOffUTC, ClimateTime setRiseTime, boolean setRiseFlag) {
        /* First Convert degrees longtitude to east positive longtitude. */
        // TODO do we know for certain this needs to be done?
        dlon = -dlon;

        /*
         * Calling the function that will return the sunrise in hh:mm:ss UTC
         * format.
         */
        RiseSet riseSet = RiseSet.calculateRiseSet(date.getYear(),
                date.getMon(), date.getDay(), dlat, dlon, setRiseFlag,
                numOffUTC);
        double riset = riseSet.getRiset();

        switch (riseSet.getType()) {
        case SUN_DOWN:
            logger.info("The sun does not " + (setRiseFlag ? "set" : "rise")
                    + " at this particular" + " latitude: [" + dlat
                    + "] and longtitude: [" + dlon + "] on ["
                    + date.toFullDateString() + "] because the sun is down.");

            setRiseTime.setMin(24);
            setRiseTime.setHour(TimeUtil.HOURS_PER_DAY);
            return;
        case SUN_UP:
            logger.info("The sun does not " + (setRiseFlag ? "set" : "rise")
                    + " at this particular" + " latitude: [" + dlat
                    + "] and longtitude: [" + dlon + "] on ["
                    + date.toFullDateString() + "] because the sun is up.");

            setRiseTime.setMin(24);
            setRiseTime.setHour(TimeUtil.HOURS_PER_DAY);
            return;
        default:
            break;
        }

        int sec = (int) (riset * TimeUtil.SECONDS_PER_HOUR);
        int hh = sec / TimeUtil.SECONDS_PER_HOUR;
        int mm = (sec - hh * TimeUtil.SECONDS_PER_HOUR)
                / TimeUtil.MINUTES_PER_HOUR;
        int ss = sec - hh * TimeUtil.SECONDS_PER_HOUR
                - mm * TimeUtil.SECONDS_PER_MINUTE;

        if (hh == TimeUtil.HOURS_PER_DAY) {
            hh = 0;
        }

        /*
         * If the seconds are greater then 30, then the minutes is rounded up
         * one.
         */

        if (ss > 30 && ss < TimeUtil.SECONDS_PER_MINUTE) {
            mm = mm + 1;
        }
        /* added 1/18/00 */
        if (mm >= TimeUtil.MINUTES_PER_HOUR) {
            mm = mm - TimeUtil.MINUTES_PER_HOUR;
            hh++;
            if (hh == TimeUtil.HOURS_PER_DAY) {
                hh = 0;
            }
        }

        if (mm >= TimeUtil.MINUTES_PER_HOUR) {
            mm = ParameterFormatClimate.MISSING_MINUTE;
        }
        if (mm < 0) {
            mm = ParameterFormatClimate.MISSING_MINUTE;
        }

        /* Pointing values to the correct structure location. */

        setRiseTime.setMin(mm);
        setRiseTime.setHour(hh);
    }

    /**
     * Calculate the sunrise or sunset time
     * 
     * @param centuries
     *            Number of Centuries since Jan 1, 2001 12Z
     * @param lat
     *            Station latitude [deg]
     * @param lon
     *            Station longitude [deg]
     * @param setRiseFlag
     *            true for sunset, false for sunrise
     * @param zone
     *            adjustment to UTC in hours to obtain local time
     * @return
     */
    private static double calcTime(double centuries, double lat, double lon,
            boolean setRiseFlag, double zone) {
        /*
         * precision: 6 s
         */
        double eps = 6.0 / TimeUtil.SECONDS_PER_HOUR;
        double UT1 = 0;

        /*
         * 10 iterations max or until when the consecutive values differ < 6 s
         */
        for (int n = 0; n < 10; n++) {
            double L = meanLonOfSun(centuries);
            double G = meanAnomaly(centuries);
            double lambda = eclipticLon(G, L);
            double epsilon = oblOfEcliptic(centuries);
            double alpha = ascension(lambda, epsilon);
            double E = eqnOfTime(L, alpha);
            double delta = declination(epsilon, lambda);
            double R = distanceFromSun(G);
            double SD = semidiameterOfSun(R);
            /*
             * Assume 34 arcmin for refraction
             */
            double h = -((34. / 60.) + SD);
            double t = hourAngle(delta, lat, h);
            if ((Math.abs(t) == 0) || (Math.abs(t) == 180)) {
                return (t);
            }
            if (setRiseFlag) {
                t = -t;
            }
            UT1 = 12.0 - ((E + lon + t) / 15.0);

            centuries += (UT1 - zone)
                    / (DAYS_PER_CENTURY * TimeUtil.HOURS_PER_DAY);
            if (Math.abs(UT1 - zone) < eps) {
                return (UT1);
            }
            zone = UT1;
        }
        return (UT1);
    }

    /**
     * Hour angle: solves the equation wr to omega [deg] cos(theta) =
     * sin(delta)*sin(fi) + cos(delta)*cos(fi)*cos(omega) where: theta: zenith
     * angle delta: declination fi: latitude omega: hour angle decl: declination
     * [deg] lat: latitude [deg] h: correction for refraction + semidiameter
     * [deg]
     */
    private static double hourAngle(double decl, double lat, double h) {
        h = Math.toRadians(h);
        decl = Math.toRadians(decl);
        lat = Math.toRadians(lat);
        double tmp = (Math.sin(h) / (Math.cos(decl) * Math.cos(lat)))
                - (Math.tan(decl) * Math.tan(lat));
        if (tmp >= 1.0) {
            return (0.0);
        } else if (tmp <= -1.0) {
            return (180);
        } else {
            return (Math.toDegrees(Math.acos(tmp)));
        }
    }

    /**
     * Calculates distance of sun from earth in astronomical units meanAnom:
     * mean anomaly [deg]
     */
    private static double distanceFromSun(double meanAnom) {
        return (1.00014 - (0.01671 * Math.cos(Math.toRadians(meanAnom)))
                - (0.00014 * Math.cos(Math.toRadians(2. * meanAnom))));
    }

    /**
     * Calculates semidiameter of sun based on distance of sun from earth. dist:
     * distance of sun from earth [au]
     */
    private static double semidiameterOfSun(double dist) {
        return (0.2666 / dist);
    }

    /**
     * 
     * @param obliquity
     *            of eclipse in degrees
     * @param longitude
     *            of eclipse in degrees
     * @return
     */
    private static double declination(double obliquity, double longitude) {
        return (Math.toDegrees(Math.asin(Math.sin(Math.toRadians(obliquity))
                * Math.sin(Math.toRadians(longitude)))));
    }

    /**
     * Equation of time [deg] meanLon: meanLongitude [deg] alpha: ascension
     * [deg]
     */
    private static double eqnOfTime(double meanLon, double alpha) {
        return (meanLon - alpha);
    }

    /*
     * Right ascension oblEcl: obliquity of ecliptic [deg] eclLon: ecliptic
     * longitude [deg]
     */
    private static double ascension(double eclLon, double oblEcl) {
        eclLon = Math.toRadians(eclLon);
        oblEcl = Math.toRadians(oblEcl);
        double t = Math.tan(oblEcl / 2.) * Math.tan(oblEcl / 2.);
        return (Math.toDegrees((eclLon - (t * Math.sin(2. * eclLon)))
                + (0.5 * t * t * Math.sin(4. * eclLon))));
    }

    /**
     * Obliquity of ecliptic [deg] numCent: number of centuries from J2000.0 12h
     * UT
     */
    private static double oblOfEcliptic(double numCent) {
        double adjustment = ECLIPTIC_SUN_ABERRATION * DAYS_PER_CENTURY;
        return (23.439 - (adjustment * numCent));
    }

    /**
     * Ecliptic longitude [deg] meanAnom: mean anomaly [deg] meanLon: mean
     * longitude of Sun [deg]
     */
    private static double eclipticLon(double meanAnom, double meanLon) {
        return (meanLon + (1.915 * Math.sin(Math.toRadians(meanAnom)))
                + (0.020 * Math.sin(Math.toRadians(2.0 * meanAnom))));
    }

    /**
     * Mean anomaly [deg] numCent: number of centuries from J2000.0 12h UT
     */
    private static double meanAnomaly(double numCent) {
        double adjustment = ANOMALY_SUN_ABERRATION * DAYS_PER_CENTURY;
        double g = ANOMALY_SUN + (adjustment * numCent);
        while (g >= 360) {
            g -= 360;
        }
        while (g < 0) {
            g += 360;
        }
        return (g);
    }

    /**
     * Mean longitude of Sun, corrected for aberration, [deg] numCent: number of
     * centuries from J2000.0 12h UT
     */
    private static double meanLonOfSun(double numCent) {
        double adjustment = LONG_SUN_ABERRATION * DAYS_PER_CENTURY;
        double ml = LONG_OF_SUN + (adjustment * numCent);
        while (ml >= 360) {
            ml -= 360;
        }
        while (ml < 0) {
            ml += 360;
        }
        return (ml);
    }

    /**
     * Legacy: returns UNIX time in UTC
     * 
     * Migrated: returns in millis, not seconds.
     */
    private static long absTime(int year, int month, int day, int hour) {
        Calendar cal = TimeUtil.newCalendar();
        cal.set(year, month - 1, day, hour, 0, 0);
        // TODO legacy also subtracted system timezone
        return cal.getTimeInMillis();
    }

    /**
     * returns number of centuries from J2000.0 12h UT
     */
    private static double numCenturies(long absTime) {
        return ((absTime / 1000) - EPOCH)
                / (TimeUtil.SECONDS_PER_DAY * DAYS_PER_CENTURY);
    }
}