/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.product.build.text;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Provides word wrapping for ATCF text products.
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#     Engineer     Description
 * ------------ ----------- ------------ ----------------------
 * Mar 22, 2021 88518       dfriedman    Initial creation
 *
 * </pre>
 *
 * @author dfriedman
 * @version 1.0
 */
public class AtcfWordWrap {

    private static final Pattern LEADING_SPACE = Pattern.compile("\\n *");

    private static final Pattern LONG_SPACE = Pattern.compile("(?<!\\.) {2,}");

    private static final Pattern ELLIPSIS_SPACE = Pattern.compile("\\.\\.\\. ");

    /**
     * Breaking delimiters
     */
    private static final String[] DELIMS = { " ", "..." };

    /**
     * Wrap paragraphs in the given input to the given width.
     * <p>
     * Paragraphs are separated by one or more blank lines.
     * <p>
     * This is intended to be mostly equivalent to {@code wordWrap} in
     * nhc_writeadv.f. It is implemented with some logic from WarnGen's
     * WrapUtil. If other features are required such as handling lock markers,
     * this and WrapUtil should be factored out to a common class.
     *
     * @param input
     * @param width
     * @return
     */
    public static String wordWrap(CharSequence input, int width) {
        StringBuilder output = new StringBuilder(input.length() + 32);
        int i = 0;
        while (i < input.length()) {
            int pStart = findParagraphStart(input, i);
            int pEnd = findParagraphEnd(input, pStart);
            if (pStart > i) {
                output.append(input.subSequence(i, pStart));
            }
            if (pEnd > pStart) {
                wrapParagraph(input.subSequence(pStart, pEnd), width, output);
            }
            i = pEnd;
        }
        return output.toString();
    }

    /**
     * Find the start of the next paragraph in {@code input} at or after
     * {@code start}. If there is no next input, returns {@code inptu.length()}.
     *
     * @param input
     * @param start
     * @return
     */
    private static int findParagraphStart(CharSequence input, int start) {
        int i = start;
        while (i < input.length() && input.charAt(i) == '\n') {
            ++i;
        }
        return i;
    }

    /**
     * Find the end of the next paragraph at {@code start}.
     *
     * @param input
     * @param start
     * @return
     */
    private static int findParagraphEnd(CharSequence input, int start) {
        int i = start;
        while (i < input.length()) {
            if (input.charAt(i) == '\n' && (i + 1 >= input.length()
                    || input.charAt(i + 1) == '\n')) {
                return i;
            }
            ++i;
        }
        return i;
    }

    /**
     * Word wrap the given paragraph to the given width.
     *
     * @param paragraph
     * @param width
     * @param output
     */
    private static void wrapParagraph(CharSequence paragraph, int width,
            StringBuilder output) {
        String text = normalizeParagraph(paragraph);
        int i = 0;
        boolean failed = false;
        while (i < text.length()) {
            int limit = i + width;
            if (limit >= text.length()) {
                appendRTrim(text, i, text.length(), output);
                break;
            }

            String bestDelim = null;
            int bestP = -1;
            for (String delim : DELIMS) {
                int backup = " ".equals(delim) ? 0 : delim.length();
                int p = !failed ? text.lastIndexOf(delim, limit - backup)
                        : text.indexOf(delim, limit - backup);
                if ((p >= i) && ((bestDelim == null) || !failed ? p > bestP
                        : p < bestP)) {
                    bestDelim = delim;
                    bestP = p;
                }
            }
            if (bestDelim != null) {
                failed = false;
                int next = bestP + bestDelim.length();
                int segmentEnd = " ".equals(bestDelim) ? bestP : next;
                appendRTrim(text, i, segmentEnd, output);
                i = splitEndOfLine(text, next, output);
            } else if (!failed) {
                /*
                 * Failed to wrap before the margin. Try again, wrapping the
                 * line after the margin, but still as close to it as possible.
                 */
                failed = true;
            } else {
                /*
                 * Failed to find any kind of break. Just dump the rest of the
                 * text.
                 */
                appendRTrim(text, i, text.length(), output);
                break;
            }
        }
    }

    /**
     * Normalize space in the given input according to the rules from
     * nhc_writeadv.f. Also normalizes space after newlines.
     *
     * @param text
     * @return
     */
    private static String normalizeParagraph(CharSequence text) {
        CharSequence result = text;
        Matcher m;
        m = LEADING_SPACE.matcher(result);
        result = m.replaceAll(" ");
        m = LONG_SPACE.matcher(result);
        result = m.replaceAll(" ");
        m = ELLIPSIS_SPACE.matcher(result);
        return m.replaceAll("...");
    }

    /**
     * Append text.substring(start, end) to sb, removing any trailing
     * whitespace.
     */
    private static void appendRTrim(String text, int start, int end,
            StringBuilder sb) {
        int sbStart = sb.length();
        sb.append(text, start, end);
        int i = sb.length();
        while (i > sbStart) {
            if (Character.isWhitespace(sb.charAt(i - 1))) {
                i--;
                sb.deleteCharAt(i);
            } else {
                break;
            }
        }
    }

    /**
     * Handle whitespace between line breaks. Adds line break for the next line
     * as necessary.
     *
     * @return The index in text at which processing for the next line should
     *         begin.
     */
    private static int splitEndOfLine(String text, int start,
            StringBuilder sb) {
        int goodBreak = start;
        int i = start;

        while (i < text.length()) {
            if (Character.isWhitespace(text.charAt(i))) {
                ++i;
            } else {
                break;
            }
        }

        if (i >= text.length()) {
            goodBreak = i;
        }
        if (goodBreak >= start) {
            appendRTrim(text, start, goodBreak, sb);
        }
        if (i < text.length()) {
            sb.append('\n');
            appendRTrim(text, goodBreak, i, sb);
        }
        return i;
    }

}
