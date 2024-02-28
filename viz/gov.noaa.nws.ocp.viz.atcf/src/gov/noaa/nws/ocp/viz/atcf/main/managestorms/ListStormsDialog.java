/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.atcf.main.managestorms;

import java.util.Calendar;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.raytheon.viz.ui.dialogs.CaveSWTDialog;

import gov.noaa.nws.ocp.common.dataplugin.atcf.AtcfBasin;
import gov.noaa.nws.ocp.common.dataplugin.atcf.Storm;
import gov.noaa.nws.ocp.common.dataplugin.atcf.StormState;
import gov.noaa.nws.ocp.viz.atcf.AtcfDataUtil;

/**
 * Dialog List Active Storms
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#     Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 08, 2018  51349     wpaintsil   Initial creation.
 * Nov 09, 2020 84705      jwu         Implemented functionalities.
 *
 * </pre>
 *
 * @author wpaintsil
 * @version 1.0
 *
 */
public class ListStormsDialog extends CaveSWTDialog {

    private static final String ALL_BASINS = "All Basins";

    private static final String DEFAULT_DTG = "          ";

    private StyledText stormInfoText;

    /**
     * Constructor
     * 
     * @param parent
     */
    public ListStormsDialog(Shell parent) {
        super(parent, SWT.SHEET);
        setText("List Active Storms");
    }

    @Override
    protected void initializeComponents(Shell shell) {
        createBasinList(shell);
        createControlButtons(shell);
    }

    /**
     * Create list of storms.
     * 
     * @param parent
     */
    private void createBasinList(Composite parent) {
        Composite stormListComp = new Composite(parent, SWT.NONE);

        GridLayout listCompLayout = new GridLayout(1, false);
        stormListComp.setLayout(listCompLayout);
        stormListComp.setLayoutData(
                new GridData(SWT.CENTER, SWT.DEFAULT, false, false));

        Composite stormBasinComp = new Composite(stormListComp, SWT.NONE);
        stormBasinComp.setLayout(new GridLayout(1, false));
        Label basinLabel = new Label(stormBasinComp, SWT.NONE);
        basinLabel.setText("Storm Basin");
        CCombo basinCombo = new CCombo(stormBasinComp, SWT.BORDER);
        basinCombo.setItems(AtcfBasin.getDescriptions());
        basinCombo.add(ALL_BASINS, 0);
        basinCombo.select(0);
        basinCombo.setEditable(false);

        basinCombo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                // Update storm info.
                updateStormInfo(basinCombo.getText());
            }
        });

        Composite stormInfoComp = new Composite(parent, SWT.NONE);
        GridLayout stormInfoGL = new GridLayout(1, false);

        stormInfoComp.setLayout(stormInfoGL);
        stormInfoComp
                .setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        stormInfoText = new StyledText(stormInfoComp,
                SWT.READ_ONLY | SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER);
        GridData stormInfoGD = new GridData(SWT.FILL, SWT.FILL, true, true);
        stormInfoGD.horizontalIndent = 5;
        stormInfoGD.widthHint = 780;
        stormInfoGD.heightHint = 400;
        stormInfoText.setLayoutData(stormInfoGD);
        stormInfoText.setFont(JFaceResources.getFont(JFaceResources.TEXT_FONT));

        updateStormInfo(ALL_BASINS);
    }

    /**
     * Create bottom buttons.
     * 
     * @param parent
     */
    protected void createControlButtons(Composite parent) {
        Composite btnComp = new Composite(parent, SWT.NONE);
        GridLayout btnGL = new GridLayout(1, true);
        btnGL.marginWidth = 30;

        btnComp.setLayout(btnGL);
        btnComp.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, true));

        Button closeBtn = new Button(btnComp, SWT.PUSH);
        closeBtn.setText("Close");
        closeBtn.setLayoutData(
                new GridData(SWT.CENTER, SWT.DEFAULT, true, true));
        closeBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                close();
            }
        });
        closeBtn.setFocus();
    }

    /*
     * Update the active storms' info based on basin, excluding ended or
     * archived storms.
     * 
     * @param basin
     */
    private void updateStormInfo(String basin) {

        StringBuilder sbr = new StringBuilder();
        sbr.append("\n")
                .append(String.format("%65s", "Active Storms for " + basin))
                .append("\n\n");
        sbr.append(String.format("%45s", "Sub")).append("\n");
        sbr.append(String.format("%-3s%-12s%-22s%-5s%-5s%-6s%-6s%-14s%-14s%-9s",
                " ", " Name", "   Basin", "Num", "Bas", "Year", "Dev",
                " Begin DTG", " End DTG", " State")).append("\n\n");

        boolean include = (basin.equals(ALL_BASINS));

        for (Storm stm : AtcfDataUtil.getFullStormList()) {
            boolean sameBas = basin
                    .equalsIgnoreCase(AtcfBasin.getDescByName(stm.getRegion()));

            if ((include || sameBas) && (stm.getEndDTG() == null && !(stm
                    .getStormState().equals(StormState.ARCHIVE.name())))) {

                String sdtg = DEFAULT_DTG;
                Calendar sCal = stm.getStartDTG();

                if (sCal != null) {
                    sdtg = AtcfDataUtil.calendarToDateTimeString(sCal);
                }

                String edtg = DEFAULT_DTG;
                Calendar eCal = stm.getEndDTG();

                if (eCal != null) {
                    edtg = AtcfDataUtil.calendarToDateTimeString(eCal);
                }

                sbr.append(String.format(
                        "%-3s%-12s%-22s%-5s%-5s%-6d%-6s%-14s%-14s%-9s", " ",
                        stm.getStormName(),
                        AtcfBasin.getDescByName(stm.getRegion()),
                        String.format("%02d", stm.getCycloneNum()),
                        " " + stm.getSubRegion() + " ", stm.getYear(),
                        " " + stm.getTcHLevel(), sdtg, edtg,
                        stm.getStormState())).append("\n");
            }
        }

        stormInfoText.setText(sbr.toString());
    }

}
