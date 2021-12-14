/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.common.product.build.text;

import java.io.IOException;

import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.Options;
import com.github.jknack.handlebars.Options.Buffer;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;

/**
 * Helpers for formatting ATCF text product with Handlebars templates.
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#     Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 29, 2020 81820      wpaintsil   Initial creation.
 * Nov 23, 2020 85264      wpaintsil   Add rounding helper.
 * Jan 26, 2021 86746      jwu         Add more helpers.
 * Mar 22, 2021 88518      dfriedman   Add wrap helper.
 *
 * </pre>
 *
 * @author wpaintsil
 * @version 1.0
 *
 */
public enum TextProductHelpers implements Helper<Object> {

    /**
     * Determine a short latitude direction string (i.e. "27.3N").
     */
    shortLatitudeDirStr {

        @Override
        public Object apply(final Object value, final Options options)
                throws IOException {
            return getDirectionStr(value, options, true, false, 1);
        }
    },

    /**
     * Determine a full latitude direction string (i.e. "27.3 North").
     */
    fullLatitudeDirStr {
        @Override
        public Object apply(final Object value, final Options options)
                throws IOException {
            return getDirectionStr(value, options, true, true, 1);
        }
    },

    /**
     * Determine longitude direction string (i.e. "83.2W").
     */
    shortLongitudeDirStr {
        @Override
        public Object apply(final Object value, final Options options)
                throws IOException {
            return getDirectionStr(value, options, false, false, 1);
        }
    },

    /**
     * Determine longitude direction string (i.e. "83.2 West")
     */
    fullLongitudeDirStr {
        @Override
        public Object apply(final Object value, final Options options)
                throws IOException {
            return getDirectionStr(value, options, false, true, 1);
        }
    },

    /**
     * Determine longitude direction string (i.e. "83.2 West")
     */
    myif {
        @Override
        public Object apply(final Object context, final Options options)
                throws IOException {
            Buffer buffer = options.buffer();
            if (options.isFalsy(context)) {
                logger.debug("Context is false...");
                buffer.append(options.inverse(context));
            } else {
                logger.debug("Context is true...");
                buffer.append(options.fn());
            }
            return buffer;
        }
    },

    /**
     * Round a decimal value.
     * 
     * <pre>
     * Format: round [value] [precision]
     * </pre>
     */
    round {
        @Override
        public Object apply(final Object value, final Options options)
                throws IOException {
            float number = 0;

            Integer roundTo = getPrecision(options);

            if (value == null) {
                logger.warn(
                        "TextProductHelper: The value passed to the round helper was null.");
                return "";
            }

            try {
                number = Float.parseFloat(value.toString());
                return TextProductHelpers.round(number, roundTo);
            } catch (NumberFormatException e) {
                if (!(value instanceof Float)) {
                    logger.warn("TextProductHelper: The value ["
                            + value.toString()
                            + "] passed to the round helper was not a number.");
                }

                return value.toString();
            }

        }
    },

    /**
     *
     * Capitalize a word.
     * 
     * <pre>
     * Format: capitalize [value]
     * </pre>
     */
    capitalize {
        @Override
        public Object apply(final Object value, final Options options)
                throws IOException {
            if (value != null) {
                String word = (String) value;
                if (word.length() > 0) {
                    return word.substring(0, 1).toUpperCase()
                            + word.substring(1).toLowerCase();
                }

            }

            return "";
        }
    },

    /**
     *
     * Word-wrap paragraphs. Paragraphs are separated by one or more blank
     * lines. This is intended to be used as a block helper. Example:
     *
     * <pre>
     * {{#wrap [width=68]}}...{{/wrap}}
     * </pre>
     */
    wrap {
        @Override
        public Object apply(final Object value, final Options options)
                throws IOException {
            int width = options.hash("width", 68);
            return AtcfWordWrap.wordWrap(options.apply(options.fn), width);
        }
    };

    /**
     * Logger.
     */
    private static final IUFStatusHandler logger = UFStatus
            .getHandler(TextProductHelpers.class);

    /**
     * Directions.
     */
    private static final String NORTH = "North";

    private static final String SOUTH = "South";

    private static final String EAST = "East";

    private static final String WEST = "West";

    /**
     * Round a floating point number to a nearest increment.
     *
     * Method adapted from nhc_writeadv.f.
     *
     * @param number
     *            Floating point number to be rounded.
     * @param inc
     *            integer increment to which to round.
     * @return integer Result of 'number' rounded to the nearest increment
     *         'inc'.
     */
    private static int round(float number, int inc) {
        if (inc < 1) {
            return Math.round(number);
        }
        return inc * (int) ((number + .5 * inc) / inc);
    }

    /**
     * Form a direction string based on input value and options.
     *
     * @param value
     *            Lat or lon value.
     * @param options
     *            Options to format the value. The first option is implemented
     *            to indicate the number of digits after decimal point.
     * @param isLat
     *            Lat or lon.
     * @param full
     *            Use full or short lat/lon direction string.
     * @param prec
     *            Default number of digits after decimal point.
     * @return Formatted direction string.
     */
    private static String getDirectionStr(Object value, Options options,
            boolean isLat, boolean full, int prec) {

        String str = "";
        float number = 0;
        if (value == null) {
            return str;
        }

        int precision = getPrecision(options);
        if (precision < 0) {
            precision = Math.max(0, prec);
        }

        try {
            number = Float.parseFloat(value.toString());

            String fmt = "%d";
            if (precision > 0) {
                fmt = "%." + String.format("%df", precision);
            }

            str = String.format(fmt, Math.abs(number));

            if (isLat) {
                if (full) {
                    str += " ";
                    str += (number < 0) ? SOUTH : NORTH;
                } else {
                    str += (number < 0) ? SOUTH.substring(0, 1)
                            : NORTH.substring(0, 1);
                }
            } else {
                if (full) {
                    str += " ";
                    str += (number > 0) ? EAST : WEST;
                } else {
                    str += (number > 0) ? EAST.substring(0, 1)
                            : WEST.substring(0, 1);
                }
            }
        } catch (NumberFormatException e) {
            logger.warn("TextProductHelper: The value [" + value.toString()
                    + "] passed to the longitudeDirection helper was not a number.");
            str = value.toString();
        }

        return str;
    }

    /**
     * Find the precision passed in as the first parameter in options.
     *
     * @param Options
     *            options.
     * @return Integer, default to 0.
     */
    private static int getPrecision(final Options options) {

        int precision = -1;

        if (options != null && options.params.length > 0) {
            if (options.param(0) instanceof Integer) {
                precision = options.param(0);
            } else {
                logger.warn("TextProductHelper: The first option ["
                        + options.param(0)
                        + "] passed to getPrecision() was not an integer.");
            }
        }

        return precision;
    }

}