/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.psh;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.printing.PrintDialog;
import org.eclipse.swt.printing.Printer;
import org.eclipse.swt.printing.PrinterData;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;

/**
 * Class to handle printing PSH reports
 * 
 * This is based on gov.noaa.nws.ncep.ui.nctextui.palette.HandlePrinting
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#     Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Sep 14, 2017 #36920     astrakovsky  Initial creation.
 * Jan 08, 2018 DCS19326   wpaintsil    Baseline version
 * 
 * </pre>
 * 
 * @author astrakovsky
 * @version 1.0
 * 
 */

public class PshPrintUtil {

    /**
     * Logger.
     */
    private static final IUFStatusHandler logger = UFStatus
            .getHandler(PshPrintUtil.class);

    private StringBuilder wordBuilder;

    private int lineHeight = 0;

    private int tabWidth = 0;

    private int leftMargin, rightMargin, topMargin, bottomMargin;

    private int x, y;

    private int index, end;

    private String text;

    private GC gc;

    public static PshPrintUtil getPshPrinter() {
        return new PshPrintUtil();
    }

    /**
     * Print the input string
     * 
     * @param intext
     */
    public void printInput(String intext) {
        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                .getShell();
        text = intext;
        PrintDialog dialog = new PrintDialog(shell, SWT.NULL);

        PrinterData data = dialog.open();
        if (data == null) {
            return;
        }
        // Do the printing in a background thread so that spooling does not
        // freeze the UI.
        final Printer printer = new Printer(data);

        Thread printingThread = new Thread("Printing") {
            public void run() {
                print(printer);
            }
        };
        printingThread.start();
    }

    /**
     * Handle print job
     * 
     * @param printer
     */
    private void print(Printer printer) {
        String tabs;
        Font printerFont;
        Color printerForegroundColor, printerBackgroundColor;

        // the string is the job name - shows up in the printer's job list
        if (printer.startJob("PSH-product")) {
            Rectangle clientArea = printer.getClientArea();
            Rectangle trim = printer.computeTrim(0, 0, 0, 0);
            Point dpi = printer.getDPI();
            // one inch from left side of paper
            leftMargin = dpi.x + trim.x;
            // one inch from right side of paper
            rightMargin = clientArea.width - dpi.x + trim.x + trim.width;
            // one inch from top edge of paper
            topMargin = dpi.y + trim.y;
            // one inch from bottom edge of paper
            bottomMargin = clientArea.height - dpi.y + trim.y + trim.height;

            // Create a buffer for computing tab width.
            int tabSize = 4; // is tab width a user setting in your UI?
            StringBuffer tabBuffer = new StringBuffer(tabSize);
            for (int i = 0; i < tabSize; i++) {
                tabBuffer.append(' ');
            }
            tabs = tabBuffer.toString();

            // Create printer GC, and create and set the printer font &
            // foreground color.
            gc = new GC(printer);
            printerFont = new Font(printer, "Courier", 8, SWT.NORMAL);
            gc.setFont(printerFont);
            tabWidth = gc.stringExtent(tabs).x;
            lineHeight = gc.getFontMetrics().getHeight();

            RGB rgb = new RGB(0, 0, 0); // Black
            printerForegroundColor = new Color(printer, rgb);
            gc.setForeground(printerForegroundColor);
            rgb = new RGB(255, 255, 255); // White
            printerBackgroundColor = new Color(printer, rgb);
            gc.setBackground(printerBackgroundColor);

            try {
                // Print text to current GC using word wrap
                printText(printer);
                printer.endJob();
            } finally {
                // Cleanup graphics resources used in printing
                printerFont.dispose();
                printerForegroundColor.dispose();
                printerBackgroundColor.dispose();
                gc.dispose();
                printer.dispose();
            }
        } else {
            logger.warn("PSH-product print job failed.");
        }
    }

    /**
     * Print the text
     */
    private void printText(Printer printer) {
        String textToPrint;
        // Get the text to print
        textToPrint = text;
        printer.startPage();
        wordBuilder = new StringBuilder();
        x = leftMargin;
        y = topMargin;
        index = 0;
        end = textToPrint.length();
        while (index < end) {
            char c = textToPrint.charAt(index);
            index++;
            if (c != 0) {
                if (c == 0x0a || c == 0x0d) {
                    if (c == 0x0d && index < end
                            && textToPrint.charAt(index) == 0x0a) {
                        index++; // if this is cr-lf, skip the lf
                    }
                    printWordBuffer(printer);
                    newline(printer);
                } else {
                    if (c != '\t') {
                        wordBuilder.append(c);
                    }
                    if (Character.isWhitespace(c)) {
                        printWordBuffer(printer);
                        if (c == '\t') {
                            x += (tabWidth - (x % tabWidth));
                        }
                    }
                }
            }
        }
        if (y + lineHeight <= bottomMargin) {
            printer.endPage();
        }
    }

    /**
     * Print the word buffer
     */
    private void printWordBuffer(Printer printer) {
        if (wordBuilder.length() > 0) {
            String word = wordBuilder.toString();
            int wordWidth = gc.stringExtent(word).x;
            if (x + wordWidth > rightMargin) {
                // word doesn't fit on current line, so wrap
                newline(printer);
            }
            gc.drawString(word, x, y, false);
            x += wordWidth;
            wordBuilder = new StringBuilder();
        }
    }

    /**
     * Add newline
     */
    private void newline(Printer printer) {
        x = leftMargin;
        y += lineHeight;
        if (y + lineHeight > bottomMargin) {
            printer.endPage();
            if (index + 1 < end) {
                y = topMargin;
                printer.startPage();
            }
        }
    }
}
