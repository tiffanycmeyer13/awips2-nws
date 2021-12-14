/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.atcf.forecast;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Stroke;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.core.databinding.observable.map.IMapChangeListener;
import org.eclipse.core.databinding.observable.map.MapChangeEvent;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.IValueChangeListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYLineAnnotation;
import org.jfree.chart.annotations.XYTextAnnotation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RectangleInsets;
import org.jfree.ui.TextAnchor;
import org.jfree.util.ShapeUtilities;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.core.VizApp;

import gov.noaa.nws.ocp.common.dataplugin.atcf.ADeckRecord;
import gov.noaa.nws.ocp.common.dataplugin.atcf.AbstractDeckRecord;
import gov.noaa.nws.ocp.common.dataplugin.atcf.ForecastTrackRecord;
import gov.noaa.nws.ocp.viz.atcf.AtcfDataUtil;
import gov.noaa.nws.ocp.viz.atcf.RecordKey;
import gov.noaa.nws.ocp.viz.atcf.WindRadii;
import gov.noaa.nws.ocp.viz.atcf.aids.AtcfTaus;
import gov.noaa.nws.ocp.viz.ui.dialogs.OcpCaveSWTDialog;

/**
 * Dialog with JFreeChart 2-D graph for "Forecast"=>"Forecast Wind Radii" =>
 * "Graph/Select Radii 34kt/50kt/64kt" to allow the user to make wind radii
 * forecast for each forecast TAU.
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Mar 10, 2020 75391      jwu         Initial creation.
 *
 * </pre>
 *
 * @author jwu
 * @version 1.0
 *
 */
public class WindRadiiGraphDialog extends OcpCaveSWTDialog
        implements IGraphMenuListeners,
        IMapChangeListener<RecordKey, ForecastTrackRecord> {

    // Logger.
    private static final IUFStatusHandler statusHandler = UFStatus
            .getHandler(WindRadiiGraphDialog.class);

    private static final String FONT_SANSSERIF = "SansSerif";

    // Titles and axis labels
    private String dialogTitle;

    private String chartTitle;

    // Used to hold chart title text, shared between SWT and AWT event threads.
    private AtomicReference<String> chartTitleAnnotationText = new AtomicReference<>(
            "");

    private String xAxisLabel;

    private String yAxisLabel;

    // Input for forecast.
    private ForecastTrackRecordMap windRadiiMap;

    private String currentDTG;

    // Observable "current tau" value shared with forecast dialog
    private IObservableValue<AtcfTaus> currentTau;

    private IValueChangeListener<AtcfTaus> tauChangedListener = event -> currentTauChanged(
            event.diff.getNewValue());

    private WindRadii currentWindRadii;

    private WindRadiiValidation windRadiiValidation;

    private Map<RecordKey, ADeckRecord> aDeckRecordMap;

    // Techniques selected on "Display Options" dialog.
    private List<String> selectedObjAids;

    // Maximum radii - also maximum axis range of the graph.
    private static final int MAXIMUM_WIND_RADII = 995;

    // Default scale on x, y axis.
    private static final int DEFAULT_SCALE = 300;

    // Default width and height of the graph.
    private static final int DEFAULT_GRAPH_WIDTH = 750;

    private static final int DEFAULT_GRAPH_HEIGHT = 750;

    // Data series' name for wind radii forecast.
    private static final String FORECAST_AID = "FCST";

    // Data series' name for range circle.
    private static final String RANGE_CIRCLE = "RANGE-CIRCLE";

    // Data series' names for max. wind speed/direction.
    private static final String DIR_SPD = "FCST-DIRSPD";

    private static final String DIR_SPD_ARROW = "FCST-DIRSPD-ARROW";

    // X/Y-Axis label offset and tick marker length.
    private static final int X_LABEL_OFFSET = -15;

    private static final int Y_LABEL_OFFSET = -20;

    private static final double MAJOR_TICK_MARK_LENGTH = 5;

    private static final double MINOR_TICK_MARK_LENGTH = 2.5;

    // Range on x & y axis.
    private int currentScale;

    // "currentScale" value used to create range circle or null if not created
    private Integer rangeCircleScale;

    // Interval to place labels on x & y axis.
    private int axisLabelInterval;

    // Interval to minor tick marks on x & y axis.
    private int axisMinorTickInterval;

    // Color for graph background;
    private Color graphBGColor = new Color(51, 153, 255);

    // Dataset to be drawn and modified.
    private XYSeriesCollection dataset;

    // Chart Panel/Plot for the graph.
    private ChartPanel panel;

    private XYPlot plot;

    // Menu bulider for the graph.
    private GraphMenuBuilder menuBuilder;

    // Text Annotation for chart title.
    private XYTextAnnotation chartTitleAnnotation;

    // Text Annotation for left mouse click.
    private XYTextAnnotation leftClickAnnotation;

    // Annotations acts as X-Axis & Y-Axis.
    private XYLineAnnotation xAxisLine;

    private XYLineAnnotation yAxisLine;

    // Annotations acts as X-Axis & Y-Axis.
    private XYTextAnnotation xAxisTitle;

    private XYTextAnnotation yAxisTitle;

    // Annotations to labels on x-axis.
    private Map<String, XYTextAnnotation> xAxisLblAnnotations;

    // Annotations to labels on x-axis.
    private List<XYLineAnnotation> xAxisTickMarkAnnotations;

    // Annotations to labels on y-axis.
    private Map<String, XYTextAnnotation> yAxisLblAnnotations;

    // Annotations to labels on x-axis.
    private List<XYLineAnnotation> yAxisTickMarkAnnotations;

    // Annotations to labels on x-axis.
    private List<XYTextAnnotation> quadrantMarkers;

    // Annotation to show data point at mouse location.
    private XYTextAnnotation samplingAnnotation;

    // Annotations to show available data.
    private Map<String, XYTextAnnotation> dataAnnotations;

    /*
     * Starting angles for radii quadrants (clockwise & starting from North) in
     * polar coordinate (counter-clockwise & starting from East).
     */
    private static final int[] QUADRANT_START = new int[] { 0, 270, 180, 90 };

    /*
     * Adjusting angles used for adding aids to a quadrant .
     */
    private static final int[] QUADRANT_FACTOR = new int[] { 90, 360, 270,
            180 };

    // Format to create a name for an obj. aid series.
    private static final String SERIES_KEY_FORMAT = "%s_%d_%d_%d";

    /**
     * Constructor
     * 
     * @param parent
     *            Parent shell
     * 
     * @param dialogTitle
     *            Title for the whole dialog
     * @param chartTitle
     *            Title for the chart.
     * @param xAxisLbl
     *            Title for the X-Axis of chart.
     * @param yAxisLbl
     *            Title for the y-Axis of chart.
     * @param curDTG
     *            Current DTG for forecast.
     * @param curTau
     *            AtcfTaus.
     * @param curWindRadii
     *            WindRadii
     * @param aDeckRecordMap
     *            Map of ADeckRecord
     * @param windRadiiMap
     *            Map of ForecastTrackRecord with wind radii
     * @param selectedAids
     *            List of obj. aids.
     */
    public WindRadiiGraphDialog(Shell parent, String dialogTitle,
            String chartTitle, String xAxisLbl, String yAxisLbl, String curDTG,
            IObservableValue<AtcfTaus> curTau, WindRadii curWindRadii,
            Map<RecordKey, ADeckRecord> aDeckRecordMap,
            ForecastTrackRecordMap windRadiiMap,
            WindRadiiValidation windRadiiValidation,
            List<String> selectedAids) {

        // Use an SWT.PRIMARY_MODAL dialog.
        super(parent, OCP_DIALOG_SWT_STYLE, OCP_DIALOG_CAVE_STYLE);

        this.dialogTitle = dialogTitle;

        this.chartTitle = chartTitle;

        this.xAxisLabel = xAxisLbl;

        this.yAxisLabel = yAxisLbl;

        this.currentDTG = curDTG;

        this.currentTau = curTau;

        this.currentWindRadii = curWindRadii;

        this.aDeckRecordMap = aDeckRecordMap;

        this.windRadiiMap = windRadiiMap;

        this.windRadiiValidation = windRadiiValidation;

        this.selectedObjAids = selectedAids;

        this.currentScale = DEFAULT_SCALE;

        this.axisLabelInterval = getAxisMajorTickMarkInterval(currentScale);

        this.axisMinorTickInterval = getAxisMinorTickMarkInterval(currentScale);

    }

    /**
     * Initialize the dialog components.
     * 
     * @param shell
     */
    @Override
    protected void initializeComponents(Shell shell) {
        currentTau.addValueChangeListener(tauChangedListener);
        windRadiiMap.addMapChangeListener(this);

        shell.setSize(800, 800);

        setText(dialogTitle);

        // Create wind radii menu
        menuBuilder = new GraphMenuBuilder(shell, this);
        menuBuilder.getQuadrantMenu().setEnabled(false);
        menuBuilder.getHelpMenu().setEnabled(false);

        Menu graphMenu = menuBuilder.getGraphMenu();
        updateTauMenu();

        shell.setMenuBar(graphMenu);

        dataset = new XYSeriesCollection();

        Map<Comparable<?>, XYSeries> seriesMap = new LinkedHashMap<>();

        // Put data into JFreechart dataset.
        updateChartTitle();
        generateDataset(seriesMap);
        addSeriesToDataset(seriesMap);

        // Create a dialog with JFreechart.
        createGraphComp(shell);
    }

    @Override
    protected void disposed() {
        super.disposed();
        windRadiiMap.removeMapChangeListener(this);
        currentTau.removeValueChangeListener(tauChangedListener);
    }

    /*
     * Create the graph section with JFreeChart.
     * 
     * @param parent
     */
    private void createGraphComp(Composite parent) {

        // Main composite
        Composite mainComp = new Composite(parent, SWT.NONE);
        GridLayout dtgInfoLayout = new GridLayout(1, false);
        dtgInfoLayout.verticalSpacing = 10;
        dtgInfoLayout.marginWidth = 10;
        mainComp.setLayout(dtgInfoLayout);
        mainComp.setLayoutData(
                new GridData(SWT.FILL, SWT.DEFAULT, false, false));

        Composite comp = new Composite(mainComp, SWT.EMBEDDED);
        GridLayout graphGL = new GridLayout(1, false);
        mainComp.setLayout(graphGL);
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        gd.heightHint = DEFAULT_GRAPH_HEIGHT;
        gd.widthHint = DEFAULT_GRAPH_WIDTH;
        comp.setLayoutData(gd);

        // Create JFreeChart graph.
        Frame frame = SWT_AWT.new_Frame(comp);

        // Create control buttons.
        createControlButtons(mainComp);

        try {
            EventQueue.invokeAndWait(() -> {
                panel = createPlot(xAxisLabel, yAxisLabel, dataset);
                frame.add(panel);

                // Customize plot's background etc.
                plot.setInsets(new RectangleInsets(0, 0, 0, 0));

                plot.setBackgroundPaint(graphBGColor);
                plot.setOutlinePaint(Color.BLACK);
                plot.setOutlineStroke(new BasicStroke(2.0f));

                // Customize plot's cross-hair
                plot.setDomainCrosshairVisible(false);
                plot.setRangeCrosshairVisible(false);
                Stroke csStroke = new BasicStroke(1.0f);
                plot.setDomainCrosshairStroke(csStroke);
                plot.setRangeCrosshairStroke(csStroke);
                plot.setDomainCrosshairPaint(Color.RED);
                plot.setRangeCrosshairPaint(Color.RED);

                // Hide both axis.
                plot.setRangeGridlinesVisible(false);
                plot.setDomainGridlinesVisible(false);

                // Create graph title/hint/axis/quadrant marker..
                createChartInfo();

                // Add panel to frame.
                frame.add(panel);

                // Add mouse listener on chart
                panel.addChartMouseListener(new ChartMouseListener() {
                    @Override
                    public void chartMouseClicked(ChartMouseEvent e) {
                        handleMouseClicked(e);
                    }

                    @Override
                    public void chartMouseMoved(ChartMouseEvent e) {
                        handleMouseMove(e);
                    }
                });
            });
        } catch (InvocationTargetException | InterruptedException e) {
            statusHandler.error(e.getMessage(), e);
        }

    }

    /*
     * create a XYPlot with dataset.
     * 
     * @param chartTitle Title over the chart
     * 
     * @param xLabel Label on X axis
     * 
     * @param yLabel Label on Y axis
     * 
     * @param dataset XYSeriesCollection
     * 
     * @return ChartPanel
     */
    private ChartPanel createPlot(String xLabel, String yLabel,
            XYSeriesCollection dataset) {

        if (dataAnnotations == null) {
            dataAnnotations = new LinkedHashMap<>();
        }

        // Generate major JFreeChart as an XYLineChart
        boolean showLegend = false;
        boolean createTooltip = true;
        boolean createURL = false;

        JFreeChart chart = ChartFactory.createXYLineChart("", xLabel, yLabel,
                dataset, PlotOrientation.VERTICAL, showLegend, createTooltip,
                createURL);

        // Update plot's properties (including labeling each aid).
        plot = chart.getXYPlot();
        updatePlotProperties();

        // Customize tooltip to show the wind radii.
        XYLineAndShapeRenderer render = (XYLineAndShapeRenderer) plot
                .getRenderer();
        render.setBaseToolTipGenerator(createToolTipGenerator());

        return new ChartPanel(chart);
    }

    /*
     * Creates the ok, and cancel buttons.
     *
     * @param parent
     */
    private void createControlButtons(Composite parent) {
        Composite ctrlBtnComp = new Composite(parent, SWT.NONE);
        GridLayout ctrlCompGL = new GridLayout(1, true);
        ctrlCompGL.marginWidth = 200;
        ctrlBtnComp.setLayout(ctrlCompGL);

        ctrlBtnComp.setLayoutData(
                new GridData(SWT.FILL, SWT.DEFAULT, false, false));

        Button okBtn = new Button(ctrlBtnComp, SWT.CENTER);
        okBtn.setText("Close");
        okBtn.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));
        okBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                close();
            }
        });
    }

    /*
     * left mouse click to do forecast and update graph.
     * 
     * @param event ChartMouseEvent
     */
    private void handleMouseClicked(ChartMouseEvent event) {
        if (event.getTrigger().getButton() == 1) {

            Point2D p = panel
                    .translateScreenToJava2D(event.getTrigger().getPoint());
            Rectangle2D plotArea = panel.getScreenDataArea();

            // Check if mouse pointer is out of the plot area
            if (p.getX() < plotArea.getMinX() || p.getX() > plotArea.getMaxX()
                    || p.getY() < plotArea.getMinY()
                    || p.getY() > plotArea.getMaxY()) {
                return;
            }

            // COnvert to chart coordinates
            double chartX = plot.getDomainAxis().java2DToValue(p.getX(),
                    plotArea, plot.getDomainAxisEdge());
            double chartY = plot.getRangeAxis().java2DToValue(p.getY(),
                    plotArea, plot.getRangeAxisEdge());

            // Calculate radius and round to nearest "5".
            double radii = Math.sqrt(chartX * chartX + chartY * chartY);
            int radii5 = AtcfDataUtil.roundToNearest(radii, 5);

            // Update wind radii.
            if (radii5 <= MAXIMUM_WIND_RADII) {
                VizApp.runAsync(() -> {
                    ForecastTrackRecord fRec = getForecastByTauNRadii(
                            currentTau.getValue(), currentWindRadii);
                    if (fRec != null) {
                        String radQuad = fRec.getRadWindQuad();
                        boolean isCircle = "AAA".equals(radQuad);
                        if (isCircle) {
                            fRec.setQuad1WindRad(radii5);
                            fRec.setQuad2WindRad(radii5);
                            fRec.setQuad3WindRad(radii5);
                            fRec.setQuad4WindRad(radii5);
                        } else {
                            if (chartX >= 0 && chartY >= 0) {
                                fRec.setQuad1WindRad(radii5);
                            } else if (chartX >= 0 && chartY <= 0) {
                                fRec.setQuad2WindRad(radii5);
                            } else if (chartX <= 0 && chartY <= 0) {
                                fRec.setQuad3WindRad(radii5);
                            } else {
                                fRec.setQuad4WindRad(radii5);
                            }
                        }
                        windRadiiMap.fireRecordChanged(fRec);
                    }
                });
            }
        }
    }

    /*
     * Mouse move to sample data at the mouse pointer.
     * 
     * @param event ChartMouseEvent
     */
    private void handleMouseMove(ChartMouseEvent event) {

        // Find current mouse location and convert to data point.
        Point2D p = panel
                .translateScreenToJava2D(event.getTrigger().getPoint());
        Rectangle2D plotArea = panel.getScreenDataArea();

        plot.setDomainCrosshairVisible(false);
        plot.setRangeCrosshairVisible(false);

        // Check if mouse pointer is out of the plot area
        if (p.getX() < plotArea.getMinX() || p.getX() > plotArea.getMaxX()
                || p.getY() < plotArea.getMinY()
                || p.getY() > plotArea.getMaxY()) {
            return;
        }

        // Convert to chart coordinates.
        double chartX = plot.getDomainAxis().java2DToValue(p.getX(), plotArea,
                plot.getDomainAxisEdge());
        double chartY = plot.getRangeAxis().java2DToValue(p.getY(), plotArea,
                plot.getRangeAxisEdge());

        // Calculate wind radii at the point and round to nearest "5".
        double radii = Math.sqrt(chartX * chartX + chartY * chartY);
        int radii5 = AtcfDataUtil.roundToNearest(radii, 5);
        if (radii5 > MAXIMUM_WIND_RADII) {
            return;
        }

        plot.setDomainCrosshairValue(chartX);
        plot.setRangeCrosshairValue(chartY);

        // And an annotation to sample the current data point.
        if (samplingAnnotation == null) {
            samplingAnnotation = new XYTextAnnotation("", 0, 0);
            samplingAnnotation
                    .setFont(new Font(FONT_SANSSERIF, Font.PLAIN, 14));
            samplingAnnotation.setBackgroundPaint(Color.WHITE);
            plot.addAnnotation(samplingAnnotation);
        }

        if ((chartX + 25) >= plot.getDomainAxis().getUpperBound()) {
            samplingAnnotation.setX(chartX - 25);
            samplingAnnotation.setTextAnchor(TextAnchor.HALF_ASCENT_RIGHT);
        } else {
            samplingAnnotation.setX(chartX + 5);
            samplingAnnotation.setTextAnchor(TextAnchor.HALF_ASCENT_LEFT);
        }

        samplingAnnotation.setY(chartY);
        samplingAnnotation.setText("FCST: " + radii5);
    }

    /*
     * Convert ForeastTrackRecord data to create JFreeChart
     *
     * @return XYSeriesCollection data
     */
    private void generateDataset(Map<Comparable<?>, XYSeries> seriesMap) {

        AtcfTaus tau = currentTau.getValue();

        // Add forecast track data (all radii arcs and speed/dir arrow.
        addForecastToGraph(tau.getValue(), seriesMap);

        /*
         * Sort obj. aid forecasts into quadrants
         */
        Map<Integer, List<ADeckRecord>> aidsInQuad = sortAidsDataByQuadrant(
                tau.getValue(), currentWindRadii.getValue());

        /*
         * Add obj. aid data to dataset by quadrant - the quadrants are counting
         * clockwise.
         */
        for (int jj = 0; jj <= 4; jj++) {
            addAidsSeriesToQuadrant(tau, aidsInQuad.get(jj), jj, seriesMap);
        }

        // Add a range circle
        createRangeCircle(currentScale, seriesMap);

    }

    /*
     * Sort obj. aids forecast by quadrant.
     *
     * @param tau Forecast Hour
     *
     * @param tau WindRadii (34, 50, or 64)
     *
     * @return Map of ADeckRecords by quadrant.
     */
    private Map<Integer, List<ADeckRecord>> sortAidsDataByQuadrant(int tau,
            int radii) {
        Map<Integer, List<ADeckRecord>> aidsDataByQuad = new LinkedHashMap<>();
        for (int ii = 1; ii <= 4; ii++) {
            aidsDataByQuad.put(ii, new ArrayList<>());
        }

        /*
         * Find valid obj aid forecast by quadrant (max wind >= 34 && 0 < wind
         * radii <=1200)
         */
        for (String aid : selectedObjAids) {
            RecordKey rkey = new RecordKey(aid, currentDTG, tau, radii);
            ADeckRecord rec = aDeckRecordMap.get(rkey);
            if (rec != null) {
                int maxWnd = (int) rec.getWindMax();
                if (maxWnd >= WindRadii.RADII_34_KNOT.getValue()) {
                    for (int quad = 1; quad <= 4; quad++) {
                        addAidDataInQuadrant(aidsDataByQuad.get(quad), rec,
                                quad);
                    }
                }
            }
        }

        return aidsDataByQuad;
    }

    /*
     * Add an obj. aid record into the record list of the quadrant if its
     * quadrant wind radii is valid ( 0< radii <= 1200).
     *
     * @param quadRecList ADeckRecord List for the quadrant
     * 
     * @param aidRec ADeckRecord to checked
     *
     * @param quadrant (1,2,3,4)
     */
    private void addAidDataInQuadrant(List<ADeckRecord> quadRecList,
            ADeckRecord aidRec, int quad) {
        if (aidRec != null) {
            float radii = getRadiiInQuadrant(aidRec, quad);
            if (radii > 0 && radii <= MAXIMUM_WIND_RADII) {
                quadRecList.add(aidRec);
            }
        }
    }

    /*
     * Create a circle to indicate current radius (for zoom).
     * 
     * @param radius radius selected for "Radius" menu
     */
    private void createRangeCircle(int radius,
            Map<Comparable<?>, XYSeries> seriesMap) {
        if (rangeCircleScale == null || rangeCircleScale != radius) {
            rangeCircleScale = radius;
            XYSeries rangeSeries = new XYSeries(RANGE_CIRCLE, false, true);
            putSeries(rangeSeries, seriesMap);

            // A circle to indicate current range.
            for (int kk = 0; kk <= 360; kk = kk + 5) {
                double theta = Math.toRadians(kk);
                double xx = radius * Math.cos(theta);
                double yy = radius * Math.sin(theta);
                rangeSeries.add(xx, yy);
            }
        }
    }

    /*
     * Add forecast speed/direction to graph as an arrow.
     * 
     * @param tau Forecast Hour
     */
    private void addForecastSpdToGraph(int tau,
            Map<Comparable<?>, XYSeries> seriesMap) {

        XYSeries series0 = new XYSeries(DIR_SPD, false, true);
        putSeries(series0, seriesMap);

        XYSeries series1 = new XYSeries(DIR_SPD_ARROW, false, true);
        putSeries(series1, seriesMap);

        /*
         * Always use the 34-knot record (speed/dir should be the same
         * regardless of radii)
         */
        RecordKey fkey = new RecordKey(FORECAST_AID, currentDTG, tau,
                WindRadii.RADII_34_KNOT.getValue());
        ForecastTrackRecord frec = windRadiiMap.get(fkey);

        if (frec != null) {
            // A line for speed.
            float dir = (int) frec.getStormDrct();
            float spd = frec.getStormSped();

            // Only add spd/dir arrow for valid values.
            if (spd > 0 && spd < 1000 && dir >= 0 && dir < 360) {
                double distance = spd * tau;
                double x = distance * Math.cos(Math.toRadians(90 - dir));
                double y = distance * Math.sin(Math.toRadians(90 - dir));

                series0.add(0, 0);
                series0.add(x, y);

                // A three-point arrow head.
                double dd = 3;
                double dx = dd * Math.cos(Math.toRadians(90 - dir));
                double dy = dd * Math.sin(Math.toRadians(90 - dir));

                series1.add(x, y);
                double x1 = x - dx + dd * Math.sin(Math.toRadians(90 - dir));
                double y1 = y - dy - dd * Math.cos(Math.toRadians(90 - dir));
                series1.add(x1, y1);

                double x2 = x - dx - dd * Math.sin(Math.toRadians(90 - dir));
                double y2 = y - dy + dd * Math.cos(Math.toRadians(90 - dir));
                series1.add(x2, y2);

                series1.add(x, y);
            }
        }
    }

    /*
     * Add wind radii forecast data to graph
     *
     * @param tau Forecast Hour
     *
     * @param radii Wind radii (34, 50, or 64)
     */
    private void addForecastToGraph(int tau,
            Map<Comparable<?>, XYSeries> seriesMap) {

        // Add arrow to indicate forecast speed and direction.
        addForecastSpdToGraph(tau, seriesMap);

        // Add radii for 34/50/64 knots, if any.
        for (WindRadii wRad : WindRadii.values()) {
            if (wRad.getValue() > 0) {
                int rad = wRad.getValue();
                RecordKey fkey = new RecordKey(FORECAST_AID, currentDTG, tau,
                        rad);
                ForecastTrackRecord fRec = windRadiiMap.get(fkey);

                if (fRec != null) {
                    int maxWind = (int) fRec.getWindMax();
                    if (maxWind >= rad) {
                        for (int quad = 1; quad <= 4; quad++) {
                            float qRadii = getRadiiInQuadrant(fRec, quad);
                            if (qRadii > 0 && qRadii <= MAXIMUM_WIND_RADII) {
                                addQuadrantRadii(tau, quad, (int) qRadii, rad,
                                        seriesMap);
                            }
                        }
                    }
                }
            }
        }
    }

    /*
     * Create labels and tick marks to simulate x-Axis.
     */
    private void setAxisRange() {

        // Customize the range on x, y axis.
        NumberAxis xAxis = (NumberAxis) plot.getDomainAxis();
        xAxis.setVisible(false);
        int buffer = getAxisMajorTickMarkInterval(currentScale);
        xAxis.setRange(-(currentScale + buffer),
                currentScale + (double) buffer);

        NumberAxis yAxis = (NumberAxis) plot.getRangeAxis();
        yAxis.setVisible(false);
        yAxis.setRange(-(currentScale + buffer),
                currentScale + (double) buffer);

    }

    /*
     * Create chart information - title, hint, axis, and quadrant markers.
     */
    private void createChartInfo() {

        // Update chart title for new TAU.
        createChartTitleAnnotation();

        // Add instructional annotation at lower-left.
        createLeftClickHint();

        // Customize the range on x, y axis.
        setAxisRange();

        // Create customized x-axis and y-axis with labels.
        createXAxisLblAnnotations();
        createYAxisLblAnnotations();

        // Add title for x-axis and y-axis.
        createXYAxisTitle();

        // Add quadrant indicators.
        createQuadrantMarker();
    }

    /*
     * Create chart title for a new radius.
     */
    private void updateChartTitle() {

        // Add graph title including max. wind.
        ForecastTrackRecord fRec = windRadiiMap
                .getByTauRadii(currentTau.getValue(), WindRadii.RADII_34_KNOT);

        String cTitle = "" + currentWindRadii.getName() + chartTitle
                + currentTau.getValue().getValue();
        int maxWnd = 0;
        if (fRec != null) {
            maxWnd = (int) fRec.getWindMax();
        }

        cTitle += (" (intensity " + maxWnd + " kts)");
        chartTitleAnnotationText.set(cTitle);
    }

    /*
     * Create chart title for a new radius.
     */
    private void createChartTitleAnnotation() {
        int buffer = getAxisMajorTickMarkInterval(currentScale) / 3 * 2;
        chartTitleAnnotation = new XYTextAnnotation("CHART-TITLE", 0,
                currentScale + (double) buffer);
        chartTitleAnnotation.setFont(new Font(FONT_SANSSERIF, Font.PLAIN, 14));
        chartTitleAnnotation.setTextAnchor(TextAnchor.HALF_ASCENT_CENTER);
        chartTitleAnnotation.setText(chartTitleAnnotationText.get());

        plot.addAnnotation(chartTitleAnnotation);
    }

    /*
     * Create labels and tick marks to simulate x-Axis.
     */
    private void createXAxisLblAnnotations() {

        if (xAxisLblAnnotations == null) {
            xAxisLblAnnotations = new LinkedHashMap<>();
        }

        if (xAxisTickMarkAnnotations == null) {
            xAxisTickMarkAnnotations = new ArrayList<>();
        }

        xAxisLine = new XYLineAnnotation(-currentScale, 0, currentScale, 0,
                new BasicStroke(1.0f), Color.BLACK);
        plot.addAnnotation(xAxisLine);

        // Add notations to show labels on x Axis
        for (int ii = 0; ii <= currentScale; ii += axisLabelInterval) {
            if (ii != 0) {
                XYTextAnnotation ann = new XYTextAnnotation("" + ii, ii,
                        X_LABEL_OFFSET);
                ann.setFont(new Font(FONT_SANSSERIF, Font.PLAIN, 10));

                plot.addAnnotation(ann);

                ann.setTextAnchor(TextAnchor.HALF_ASCENT_CENTER);
                xAxisLblAnnotations.put("" + ii, ann);

                XYTextAnnotation ann1 = new XYTextAnnotation("" + ii, -ii,
                        X_LABEL_OFFSET);
                ann1.setFont(new Font(FONT_SANSSERIF, Font.PLAIN, 10));

                plot.addAnnotation(ann1);

                ann1.setTextAnchor(TextAnchor.HALF_ASCENT_CENTER);
                xAxisLblAnnotations.put("-" + ii, ann1);

                XYLineAnnotation annTick = new XYLineAnnotation(ii,
                        -MAJOR_TICK_MARK_LENGTH, ii, MAJOR_TICK_MARK_LENGTH,
                        new BasicStroke(1.0f), Color.BLACK);
                xAxisTickMarkAnnotations.add(annTick);
                plot.addAnnotation(annTick);

                XYLineAnnotation annTick1 = new XYLineAnnotation(-ii,
                        -MAJOR_TICK_MARK_LENGTH, -ii, MAJOR_TICK_MARK_LENGTH,
                        new BasicStroke(1.0f), Color.BLACK);
                xAxisTickMarkAnnotations.add(annTick1);
                plot.addAnnotation(annTick1);
            }
        }

        // Add notations to show labels on x Axis
        for (int ii = 0; ii <= currentScale; ii += axisMinorTickInterval) {
            if (ii != 0) {
                XYLineAnnotation annTick = new XYLineAnnotation(ii,
                        -MINOR_TICK_MARK_LENGTH, ii, MINOR_TICK_MARK_LENGTH,
                        new BasicStroke(0.5f), Color.BLACK);
                xAxisTickMarkAnnotations.add(annTick);
                plot.addAnnotation(annTick);

                XYLineAnnotation annTick1 = new XYLineAnnotation(-ii,
                        -MINOR_TICK_MARK_LENGTH, -ii, MINOR_TICK_MARK_LENGTH,
                        new BasicStroke(0.5f), Color.BLACK);
                xAxisTickMarkAnnotations.add(annTick1);
                plot.addAnnotation(annTick1);
            }
        }
    }

    /*
     * Create labels and tick marks to simulate y-Axis.
     */
    private void createYAxisLblAnnotations() {

        if (yAxisLblAnnotations == null) {
            yAxisLblAnnotations = new LinkedHashMap<>();
        }

        if (yAxisTickMarkAnnotations == null) {
            yAxisTickMarkAnnotations = new ArrayList<>();
        }

        // Add annotation as Y-Axis.
        plot = panel.getChart().getXYPlot();

        yAxisLine = new XYLineAnnotation(0, -currentScale, 0, currentScale,
                new BasicStroke(1.0f), Color.BLACK);
        plot.addAnnotation(yAxisLine);

        // Add annotations as major tick marks & labels at each tick.
        for (int ii = 0; ii <= currentScale; ii += axisLabelInterval) {
            if (ii != 0) {
                XYTextAnnotation ann = new XYTextAnnotation("" + ii,
                        Y_LABEL_OFFSET, ii);
                ann.setFont(new Font(FONT_SANSSERIF, Font.PLAIN, 10));
                ann.setTextAnchor(TextAnchor.HALF_ASCENT_CENTER);
                plot.addAnnotation(ann);
                yAxisLblAnnotations.put("" + ii, ann);

                XYTextAnnotation ann1 = new XYTextAnnotation("" + ii,
                        Y_LABEL_OFFSET, -ii);
                ann1.setFont(new Font(FONT_SANSSERIF, Font.PLAIN, 10));
                ann1.setTextAnchor(TextAnchor.HALF_ASCENT_CENTER);
                plot.addAnnotation(ann1);
                yAxisLblAnnotations.put("-" + ii, ann1);

                XYLineAnnotation annTick = new XYLineAnnotation(
                        -MAJOR_TICK_MARK_LENGTH, ii, MAJOR_TICK_MARK_LENGTH, ii,
                        new BasicStroke(1.0f), Color.BLACK);
                yAxisTickMarkAnnotations.add(annTick);
                plot.addAnnotation(annTick);

                XYLineAnnotation annTick1 = new XYLineAnnotation(
                        -MAJOR_TICK_MARK_LENGTH, -ii, MAJOR_TICK_MARK_LENGTH,
                        -ii, new BasicStroke(1.0f), Color.BLACK);
                yAxisTickMarkAnnotations.add(annTick1);
                plot.addAnnotation(annTick1);
            }
        }

        // Add notations to show labels on x Axis
        for (int ii = 0; ii <= currentScale; ii += axisMinorTickInterval) {
            if (ii != 0) {
                XYLineAnnotation annTick = new XYLineAnnotation(
                        -MINOR_TICK_MARK_LENGTH, ii, MINOR_TICK_MARK_LENGTH, ii,
                        new BasicStroke(0.5f), Color.BLACK);
                yAxisTickMarkAnnotations.add(annTick);
                plot.addAnnotation(annTick);

                XYLineAnnotation annTick1 = new XYLineAnnotation(
                        -MINOR_TICK_MARK_LENGTH, -ii, MINOR_TICK_MARK_LENGTH,
                        -ii, new BasicStroke(0.5f), Color.BLACK);
                yAxisTickMarkAnnotations.add(annTick1);
                plot.addAnnotation(annTick1);
            }
        }
    }

    /*
     * Create annotations as x-Axis & y-Axis title.
     */
    private void createXYAxisTitle() {

        // Add title for x-axis and y-axis.
        int buffer = getAxisMajorTickMarkInterval(currentScale) / 3;
        xAxisTitle = new XYTextAnnotation(xAxisLabel,
                currentScale + (double) buffer, 0);
        xAxisTitle.setFont(new Font(FONT_SANSSERIF, Font.PLAIN, 12));
        xAxisTitle.setTextAnchor(TextAnchor.HALF_ASCENT_CENTER);
        plot.addAnnotation(xAxisTitle);

        yAxisTitle = new XYTextAnnotation(yAxisLabel, 0,
                currentScale + (double) buffer);
        yAxisTitle.setFont(new Font(FONT_SANSSERIF, Font.PLAIN, 12));
        yAxisTitle.setTextAnchor(TextAnchor.HALF_ASCENT_CENTER);
        plot.addAnnotation(yAxisTitle);
    }

    /*
     * Create annotation as x-Axis & y-Axis title.
     */
    private void createQuadrantMarker() {

        if (quadrantMarkers == null) {
            quadrantMarkers = new ArrayList<>();
        }

        XYTextAnnotation annNE = new XYTextAnnotation("NE", currentScale,
                currentScale);
        annNE.setFont(new Font(FONT_SANSSERIF, Font.PLAIN, 12));
        annNE.setTextAnchor(TextAnchor.HALF_ASCENT_CENTER);
        quadrantMarkers.add(annNE);
        plot.addAnnotation(annNE);

        XYTextAnnotation annSE = new XYTextAnnotation("SE", currentScale,
                -(currentScale));
        annSE.setFont(new Font(FONT_SANSSERIF, Font.PLAIN, 12));
        annSE.setTextAnchor(TextAnchor.HALF_ASCENT_CENTER);
        quadrantMarkers.add(annSE);
        plot.addAnnotation(annSE);

        XYTextAnnotation annSW = new XYTextAnnotation("SW", -(currentScale),
                -(currentScale));
        annSW.setFont(new Font(FONT_SANSSERIF, Font.PLAIN, 12));
        annSW.setTextAnchor(TextAnchor.HALF_ASCENT_CENTER);
        quadrantMarkers.add(annSW);
        plot.addAnnotation(annSW);

        XYTextAnnotation annNW = new XYTextAnnotation("NW", -(currentScale),
                currentScale);
        annNW.setFont(new Font(FONT_SANSSERIF, Font.PLAIN, 12));
        annNW.setTextAnchor(TextAnchor.HALF_ASCENT_CENTER);
        quadrantMarkers.add(annNW);
        plot.addAnnotation(annNW);

    }

    /*
     * Create the hint annotation for left mouse click.
     */
    private void createLeftClickHint() {
        String leftClickHint = "Left click to select "
                + currentWindRadii.getName() + " wind radii";

        int buffer = getAxisMajorTickMarkInterval(currentScale) / 3 * 2;
        leftClickAnnotation = new XYTextAnnotation("LEFT-CLICK-HINT",
                -(currentScale + buffer), -(currentScale + buffer));
        leftClickAnnotation.setFont(new Font(FONT_SANSSERIF, Font.PLAIN, 14));
        leftClickAnnotation.setBackgroundPaint(Color.ORANGE);
        leftClickAnnotation.setPaint(Color.BLACK);
        leftClickAnnotation.setTextAnchor(TextAnchor.CENTER_LEFT);
        leftClickAnnotation.setText(leftClickHint);

        plot.addAnnotation(leftClickAnnotation);
    }

    /*
     * Create a StandardXYToolTipGeneratort for the plot.
     */
    private StandardXYToolTipGenerator createToolTipGenerator() {
        @SuppressWarnings("serial")
        StandardXYToolTipGenerator toolTipGenerator = new StandardXYToolTipGenerator() {
            @Override
            public String generateToolTip(XYDataset dataset, int series,
                    int item) {
                double xx = dataset.getXValue(series, item);
                double yy = dataset.getYValue(series, item);
                String name = (String) dataset.getSeriesKey(series);

                // TODO update with current max radii & speed.
                String aid;
                if (name.contains(RANGE_CIRCLE)) {
                    aid = "MAX FCST: " + MAXIMUM_WIND_RADII;
                } else if (name.contains(DIR_SPD)) {
                    return "14 kts";
                } else {
                    aid = name.substring(0, name.lastIndexOf('_'));
                }

                return aid + ": " + (int) Math.sqrt(xx * xx + yy * yy);
            }
        };

        return toolTipGenerator;
    }

    /*
     * Update each series's property in the plot, including the range circle,
     * speed/direction arrow, creating labels as XYTextAnnottation for each aid,
     * and line/color for forecast wind radii arc.
     */
    private void updatePlotProperties() {

        XYLineAndShapeRenderer render = (XYLineAndShapeRenderer) plot
                .getRenderer();

        int nseries = dataset.getSeriesCount();
        for (int ii = 0; ii < nseries; ii++) {
            XYSeries xys = dataset.getSeries(ii);

            String skey = (String) xys.getKey();

            render.setSeriesLinesVisible(ii, false);
            render.setSeriesShape(ii, ShapeUtilities.createDiamond(5));
            render.setSeriesPaint(ii, Color.YELLOW);
            render.setSeriesShapesVisible(ii, true);

            // Circle to indicate current radii limit.
            if (skey.contains(RANGE_CIRCLE)) {
                render.setSeriesPaint(ii, Color.WHITE);
                render.setSeriesStroke(ii, new BasicStroke(0.5f));
                render.setSeriesLinesVisible(ii, true);
                render.setSeriesShapesVisible(ii, false);
            } else if (skey.equals(DIR_SPD)) {
                render.setSeriesPaint(ii, Color.WHITE);
                render.setSeriesStroke(ii, new BasicStroke(2.0f));
                render.setSeriesLinesVisible(ii, true);
                render.setSeriesShapesVisible(ii, false);
            } else if (skey.equals(DIR_SPD_ARROW)) {
                render.setSeriesPaint(ii, Color.WHITE);
                render.setSeriesShapesFilled(ii, true);

                render.setSeriesStroke(ii, new BasicStroke(2.0f));
                render.setSeriesFillPaint(ii, Color.WHITE);
                render.setSeriesLinesVisible(ii, true);
                render.setSeriesShapesVisible(ii, false);
            } else {

                // Add annotation for each obj. aid technique name.
                String aid = skey.substring(0, skey.indexOf('_'));

                render.setSeriesLinesVisible(ii, false);
                render.setSeriesShape(ii, ShapeUtilities.createDiamond(5));
                render.setSeriesPaint(ii, Color.YELLOW);
                render.setSeriesShapesVisible(ii, true);

                if (!skey.contains(FORECAST_AID)) {
                    for (int jj = 0; jj < xys.getItemCount(); jj++) {
                        double xx = (double) xys.getX(jj);
                        double yy = (double) xys.getY(jj);

                        XYTextAnnotation ann;
                        ann = new XYTextAnnotation(aid, xx + 5, yy + 10);
                        ann.setFont(new Font(FONT_SANSSERIF, Font.PLAIN, 10));
                        ann.setBackgroundPaint(graphBGColor);
                        ann.setTextAnchor(TextAnchor.TOP_LEFT);

                        plot.addAnnotation(ann);
                        dataAnnotations.put(skey, ann);
                    }
                }

                // Add an annotation to indicate forecast data set.
                if (skey.contains(FORECAST_AID)) {

                    // TODO Customize color through "Change Color"
                    render.setSeriesPaint(ii, Color.YELLOW);
                    if (skey.contains("50")) {
                        render.setSeriesPaint(ii, Color.ORANGE);
                    } else if (skey.contains("64")) {
                        render.setSeriesPaint(ii, Color.MAGENTA);
                    }

                    render.setSeriesStroke(ii, new BasicStroke(2.0f));
                    render.setSeriesLinesVisible(ii, true);

                    // TODO - set to true will overwrite series' line stroke?
                    render.setSeriesShapesVisible(ii, false);
                }

            }
        }
    }

    /*
     * Update a forecast series wind radii arc's line/color in the plot.
     * 
     * @param skey Key for the series.
     */
    private void updateForecastRadiiProperties(String skey) {

        XYLineAndShapeRenderer render = (XYLineAndShapeRenderer) plot
                .getRenderer();

        if (skey.contains(FORECAST_AID)) {

            int ii = dataset.getSeriesIndex(skey);
            if (ii >= 0) {
                // TODO Customize color through "Change Color"
                render.setSeriesPaint(ii, Color.YELLOW);
                if (skey.contains("50")) {
                    render.setSeriesPaint(ii, Color.ORANGE);
                } else if (skey.contains("64")) {
                    render.setSeriesPaint(ii, Color.MAGENTA);
                }

                render.setSeriesStroke(ii, new BasicStroke(2.0f));
                render.setSeriesLinesVisible(ii, true);

                // TODO - set to true will overwrite series' line stroke?
                render.setSeriesShapesVisible(ii, false);
            }
        }
    }

    /*
     * Update the data series for a given forecast wind radii in a quadrant. For
     * the the series does not exist yet, it is created and the plot properties
     * is updated sa well.
     *
     * This method must be called from the AWT event thread.
     *
     * @param quad Quadrant for the XYSeries to be plotted
     *
     * @param radii Wind radius for the quadrant
     */
    private void updateQuadrantRadii(int tau, int windRadii, int quad,
            int radii) {
        String skey = String.format(SERIES_KEY_FORMAT, FORECAST_AID, tau,
                windRadii, quad);
        int index = dataset.getSeriesIndex(skey);

        if (index >= 0) {
            XYSeries series = dataset.getSeries(skey);
            fillRadiiSeriesForQuad(series, quad, radii);
        } else {
            Map<Comparable<?>, XYSeries> seriesMap = new LinkedHashMap<>();
            addQuadrantRadii(tau, quad, radii, windRadii, seriesMap);
            addSeriesToDataset(seriesMap);
            updateForecastRadiiProperties(skey);
        }
    }

    /*
     * Fill the given XYSeries for a quadrant with the wind radii value.
     *
     * Note: each quadrant has its own starting angle as in QUADRANT_START.
     *
     * @param quad Quadrant for the XYSeries to be plotted
     *
     * @param radii Wind radius for the quadrant
     * 
     * @param wndRad Wind radii for the quadrant (34/50/64)
     * 
     * @return index Index of the data series.
     */
    private void addQuadrantRadii(int tau, int quad, int radii, int wndRad,
            Map<Comparable<?>, XYSeries> seriesMap) {

        String skey = String.format(SERIES_KEY_FORMAT, FORECAST_AID, tau,
                wndRad, quad);

        XYSeries series = new XYSeries(skey, false, false);
        putSeries(series, seriesMap);

        if (Math.abs(radii) <= MAXIMUM_WIND_RADII) {
            fillRadiiSeriesForQuad(series, quad, radii);
        } else {
            statusHandler.warn("WindRadiiGraphDialog: wind radii " + radii
                    + " in quadrant " + quad + "is invalid. No data added.");
        }
    }

    /*
     * Fill the given XYSeries for a quadrant with the wind radii value.
     * 
     * Note: each quadrant has its own starting angle as in QUADRANT_START.
     *
     * @param series XYSeries to be filled
     * 
     * @param quad Quadrant for the XYSeries to be plotted
     * 
     * @param radii Wind radii for the quadrant
     *
     */
    private void fillRadiiSeriesForQuad(XYSeries series, int quad, int radii) {

        series.clear();

        if (radii != 0) {
            int start = QUADRANT_START[quad - 1];
            int end = start + 90;
            for (int kk = start; kk <= end; kk++) {
                double theta = Math.toRadians(kk);
                double xx = radii * Math.cos(theta);
                double yy = radii * Math.sin(theta);
                series.add(xx, yy);
            }
        }
    }

    /*
     * Create series for obj. aids forecast in a quadrant
     */
    private void addAidsSeriesToQuadrant(AtcfTaus tau,
            List<ADeckRecord> aidsRec, int quadrant,
            Map<Comparable<?>, XYSeries> seriesMap) {
        if (aidsRec != null && !aidsRec.isEmpty()) {
            int ii = 1;
            double interval = 90.0 / (aidsRec.size() + 1);
            for (ADeckRecord rec : aidsRec) {
                float qRadii = getRadiiInQuadrant(rec, quadrant);
                if (qRadii > 0) {

                    String skey = String.format(SERIES_KEY_FORMAT,
                            rec.getTechnique(), tau.getValue(),
                            currentWindRadii.getValue(), quadrant);

                    XYSeries series = new XYSeries(skey, false, false);
                    putSeries(series, seriesMap);

                    int qFactor = QUADRANT_FACTOR[quadrant - 1];
                    double theta = Math.toRadians(qFactor - ii * interval);
                    double xx = qRadii * Math.cos(theta);
                    double yy = qRadii * Math.sin(theta);
                    series.add(xx, yy);

                    ii++;
                }
            }
        }
    }

    /*
     * Get wind radii in a quadrant from an obj aid record.
     * 
     * @param rec AbstractDeckRecord (A or F in this class)
     * 
     * @param quad (1,2,3,4)
     * 
     * @return radii Radii in the given quadrant.
     */
    private float getRadiiInQuadrant(AbstractDeckRecord rec, int quad) {
        float radii = -9999;

        if (rec != null) {
            switch (quad) {
            case 1:
                if (rec instanceof ADeckRecord) {
                    radii = ((ADeckRecord) rec).getQuad1WindRad();
                } else {
                    radii = ((ForecastTrackRecord) rec).getQuad1WindRad();
                }
                break;
            case 2:
                if (rec instanceof ADeckRecord) {
                    radii = ((ADeckRecord) rec).getQuad2WindRad();
                } else {
                    radii = ((ForecastTrackRecord) rec).getQuad2WindRad();
                }
                break;
            case 3:
                if (rec instanceof ADeckRecord) {
                    radii = ((ADeckRecord) rec).getQuad3WindRad();
                } else {
                    radii = ((ForecastTrackRecord) rec).getQuad3WindRad();
                }
                break;
            case 4:
                if (rec instanceof ADeckRecord) {
                    radii = ((ADeckRecord) rec).getQuad4WindRad();
                } else {
                    radii = ((ForecastTrackRecord) rec).getQuad4WindRad();
                }
                break;
            default:
                break;
            }
        }

        return radii;
    }

    /*
     * Remove an obj. aids series for a given wind radii.
     */
    private void removeObjAidsSeries() {
        int serieCnt = dataset.getSeriesCount();
        List<XYSeries> removableSeries = new ArrayList<>();
        for (int ii = 0; ii < serieCnt; ii++) {
            XYSeries xys = dataset.getSeries(ii);
            String skey = (String) xys.getKey();
            if (skey.contains("_") && !skey.contains(FORECAST_AID)) {
                removableSeries.add(xys);
            }
        }

        for (XYSeries xys : removableSeries) {
            dataset.removeSeries(xys);
        }
    }

    /*
     * Remove an all data series in dataset.
     */
    private void removeAllSeries() {
        dataset.removeAllSeries();
        rangeCircleScale = null;
    }

    /*
     * Remove all axis annotations from plot.
     */
    private void removeAxisAnnotations() {

        // Remove all axis-related annotations.
        plot.removeAnnotation(leftClickAnnotation);
        plot.removeAnnotation(chartTitleAnnotation);

        plot.removeAnnotation(xAxisLine);
        plot.removeAnnotation(yAxisLine);

        plot.removeAnnotation(xAxisTitle);
        plot.removeAnnotation(yAxisTitle);

        for (XYTextAnnotation ann : xAxisLblAnnotations.values()) {
            plot.removeAnnotation(ann);
        }

        for (XYLineAnnotation ann : xAxisTickMarkAnnotations) {
            plot.removeAnnotation(ann);
        }

        for (XYTextAnnotation ann : yAxisLblAnnotations.values()) {
            plot.removeAnnotation(ann);
        }

        for (XYLineAnnotation ann : yAxisTickMarkAnnotations) {
            plot.removeAnnotation(ann);
        }

        for (XYTextAnnotation ann : quadrantMarkers) {
            plot.removeAnnotation(ann);
        }
    }

    /*
     * Enable/disable radius menu items for the selected TAU.
     */
    private void updateTauMenu() {

        MenuItem[] tauMenu = menuBuilder.getTauMenu().getMenu().getItems();
        for (MenuItem item : tauMenu) {
            AtcfTaus tau = AtcfTaus.getTau((int) item.getData());
            if (tau != null) {
                item.setEnabled(windRadiiValidation.isTauAllowed(tau));
            }
        }
        updateRadiusMenu();
    }

    /*
     * Enable/disable radius menu items for the selected TAU.
     */
    private void updateRadiusMenu() {

        MenuItem[] radiusMenu = menuBuilder.getRadiusMenu().getMenu()
                .getItems();
        AtcfTaus tau = currentTau.getValue();

        radiusMenu[0].setEnabled(windRadiiValidation
                .isWindRadiiAllowed(WindRadii.RADII_34_KNOT, tau));
        radiusMenu[1].setEnabled(windRadiiValidation
                .isWindRadiiAllowed(WindRadii.RADII_50_KNOT, tau));
        radiusMenu[2].setEnabled(windRadiiValidation
                .isWindRadiiAllowed(WindRadii.RADII_64_KNOT, tau));
    }

    /*
     * Find the forecast track records stored in the map for a TAU.
     *
     * @param tau AtcfTaus (forecast hour)
     *
     * @return List<ForecastTrackRecord>
     */
    private ForecastTrackRecord getForecastByTauNRadii(AtcfTaus tau,
            WindRadii radii) {
        RecordKey key = new RecordKey(FORECAST_AID, currentDTG, tau.getValue(),
                radii.getValue());
        return windRadiiMap.get(key);
    }

    /*
     * Get the interval for placing major tick marks & labels.
     *
     * @param scale Current scale of the graph
     */
    private int getAxisMajorTickMarkInterval(int scale) {
        int interval = 5;
        if (scale > 1000) {
            interval = 250;
        } else if (scale > 750) {
            interval = 100;
        } else if (scale >= 300) {
            interval = 50;
        } else if (scale >= 100) {
            interval = 25;
        }

        return interval;
    }

    /*
     * Get the interval for placing minor tick marks.
     * 
     * @param scale Current scale of the graph
     */
    private int getAxisMinorTickMarkInterval(int scale) {
        return getAxisMajorTickMarkInterval(scale) / 5;
    }

    /**
     * Saves current graph to a file.
     */
    @Override
    public void saveGraphicToFile() {
        FileDialog dlg = new FileDialog(shell, SWT.SAVE);
        dlg.setOverwrite(true);
        String path = dlg.open();
        if (path != null) {
            EventQueue.invokeLater(() -> {
                if (panel != null) {
                    try {
                        ChartUtilities.saveChartAsPNG(new File(path),
                                panel.getChart(), panel.getWidth(),
                                panel.getHeight());
                    } catch (IOException e) {
                        statusHandler.error(
                                "Error saving graphic: " + e.toString(), e);
                    }
                }
            });
        }
    }

    /**
     * Action when a TAU (0,12,24,...) is selected from menu.
     * 
     * @param tau
     *            TAU hours
     */
    @Override
    public void tauSelected(int tau) {
        currentTau.setValue(AtcfTaus.getTau(tau));
    }

    private void currentTauChanged(AtcfTaus newTau) {
        if (newTau != null) {
            Map<Comparable<?>, XYSeries> seriesMap = new LinkedHashMap<>();

            // Generate dataset for new TAU.
            generateDataset(seriesMap);
            updateChartTitle();

            // Update radius menu items based on max wind.
            updateRadiusMenu();

            EventQueue.invokeLater(() -> {
                plot.removeAnnotation(chartTitleAnnotation);
                plot.removeAnnotation(leftClickAnnotation);
                for (XYTextAnnotation ann : dataAnnotations.values()) {
                    plot.removeAnnotation(ann);
                }

                removeAllSeries();
                addSeriesToDataset(seriesMap);

                // Generate aids symbols & annotations for new TAU.
                updatePlotProperties();

                // Update chart title for new TAU.
                createChartTitleAnnotation();

                // Update left click hint for new TAU.
                createLeftClickHint();
            });

        }
    }

    /**
     * Action when a wind radius is selected from menu.
     * 
     * @param radius
     *            Radius selected (34,50,64) knots
     */
    @Override
    public void radiusSelected(int radius) {
        if (radius != currentWindRadii.getValue()) {

            currentWindRadii = WindRadii.getWindRadii(radius);
            AtcfTaus tau = currentTau.getValue();

            updateChartTitle();

            // Sort obj. aid forecasts for current TAU & radius into quadrants.
            Map<Integer, List<ADeckRecord>> aidsInQuad = sortAidsDataByQuadrant(
                    tau.getValue(), currentWindRadii.getValue());

            EventQueue.invokeLater(() -> {
                // Remove aids and their annotations for previous radius.
                removeObjAidsSeries();
                plot.removeAnnotation(chartTitleAnnotation);
                plot.removeAnnotation(leftClickAnnotation);
                for (XYTextAnnotation ann : dataAnnotations.values()) {
                    plot.removeAnnotation(ann);
                }

                Map<Comparable<?>, XYSeries> seriesMap = new LinkedHashMap<>();
                /*
                 * Add obj. aid data to dataset by quadrant - the quadrants are
                 * counting clockwise.
                 */
                for (int jj = 1; jj <= 4; jj++) {
                    addAidsSeriesToQuadrant(tau, aidsInQuad.get(jj), jj,
                            seriesMap);
                }

                addSeriesToDataset(seriesMap);

                // Generate aids symbols & annotations for new radius.
                updatePlotProperties();

                // Update chart title for new radius.
                createChartTitleAnnotation();

                // Update left click hint for new radius.
                createLeftClickHint();
            });

        }

    }

    /**
     * Action when a scale is selected.
     * 
     * @param scale
     *            scale selected (50, 100, 200, ...) in nm.
     */
    @Override
    public void scaleSelected(int scale) {
        if (scale != currentScale) {
            // Update range circle to new scale.
            Map<Comparable<?>, XYSeries> seriesMap = new LinkedHashMap<>();
            createRangeCircle(scale, seriesMap);

            // Find axis intervals for news scale.
            currentScale = scale;

            axisLabelInterval = getAxisMajorTickMarkInterval(currentScale);
            axisMinorTickInterval = getAxisMinorTickMarkInterval(currentScale);

            EventQueue.invokeLater(() -> {
                addSeriesToDataset(seriesMap);

                // Remove all axis-related annotations.
                removeAxisAnnotations();

                // Re-create title, hint, axis, and quadrant markers.
                createChartInfo();
            });
        }
    }

    /**
     * Exit the graph window and also ask caller to update with the data from
     * the graph window.
     */
    @Override
    public void exitGraphWindow() {
        close();
    }

    /**
     * Called in response to a forecast record change. Does not handle
     * deletions. Assumes ForecastTrackRecord.getFcstHour() == associated tau
     * from the map.
     */
    @Override
    public void handleMapChange(
            MapChangeEvent<? extends RecordKey, ? extends ForecastTrackRecord> event) {
        List<ForecastTrackRecord> records = Stream
                .concat(event.diff.getAddedKeys().stream(),
                        event.diff.getChangedKeys().stream())
                .map(k -> getForecastByTauNRadii(AtcfTaus.getTau(k.getTau()),
                        WindRadii.getWindRadii(k.getWindRad())))
                .collect(Collectors.toList());

        EventQueue.invokeLater(() -> {
            for (ForecastTrackRecord fRec : records) {
                for (int quadrant = 1; quadrant <= 4; ++quadrant) {
                    float value;
                    switch (quadrant) {
                    case 1:
                        value = fRec.getQuad1WindRad();
                        break;
                    case 2:
                        value = fRec.getQuad2WindRad();
                        break;
                    case 3:
                        value = fRec.getQuad3WindRad();
                        break;
                    case 4:
                        value = fRec.getQuad4WindRad();
                        break;
                    default:
                        continue;
                    }
                    updateQuadrantRadii(fRec.getFcstHour(),
                            (int) fRec.getRadWind(), quadrant,
                            Math.round(value));
                }
            }
        });
    }

    private static void putSeries(XYSeries series,
            Map<Comparable<?>, XYSeries> seriesMap) {
        seriesMap.put(series.getKey(), series);
    }

    /**
     * Add the given series to {@code dataset}. Once {@code dataset} is owned by
     * a chart object, this must only be called from the AWT event thread.
     *
     * @param seriesMap
     */
    private void addSeriesToDataset(Map<Comparable<?>, XYSeries> seriesMap) {
        for (XYSeries series : seriesMap.values()) {
            int index = dataset.getSeriesIndex(series.getKey());
            if (index >= 0) {
                dataset.removeSeries(index);
            }
            dataset.addSeries(series);
        }
    }

}
