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
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.core.databinding.observable.map.IMapChangeListener;
import org.eclipse.core.databinding.observable.map.MapChangeEvent;
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
import org.eclipse.swt.widgets.Shell;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYPointerAnnotation;
import org.jfree.chart.annotations.XYTextAnnotation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.TextAnchor;
import org.jfree.util.ShapeUtilities;

import com.raytheon.uf.viz.core.VizApp;

import gov.noaa.nws.ocp.common.dataplugin.atcf.ForecastTrackRecord;
import gov.noaa.nws.ocp.viz.atcf.RecordKey;
import gov.noaa.nws.ocp.viz.atcf.WindRadii;
import gov.noaa.nws.ocp.viz.atcf.aids.AtcfTaus;
import gov.noaa.nws.ocp.viz.ui.dialogs.OcpCaveSWTDialog;

/**
 * Dialog with JFreeChart 2-D graph for "Forecast"=>"Forecast Intensity" =>
 * "View Intensity Graph / Make Forecast" to allow the user to make intensity
 * forecast for each forecast TAU.
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Feb 03, 2020 71724      jwu         Initial creation.
 * Mar 25, 2020 75391      jwu         Added default menu.
 *
 * </pre>
 *
 * @author jwu
 * @version 1.0
 *
 */
public class IntensityGraphDialog extends OcpCaveSWTDialog
        implements IGraphMenuListeners,
        IMapChangeListener<RecordKey, ForecastTrackRecord> {

    // Titles and axis labels
    private String dialogTitle;

    private String chartTitle;

    private String xAxisLabel;

    private String yAxisLabel;

    // Input for forecast.
    private ForecastInfo fcstInfo;

    // Owning dialog, used to access forecast track records and valid taus
    private ForecastIntensityDialog forecastIntensityDialog;

    // Dataset to be drawn and modified.
    private XYSeriesCollection dataset;

    // Chart Panel for the graph.
    private ChartPanel panel;

    // Color for graph background.
    private Color graphBGColor = new Color(51, 153, 255);

    // Annotation to show data point at mouse location.
    private XYTextAnnotation samplingAnnotation;

    // Annotations to show available data.
    private Map<String, XYTextAnnotation> dataAnnotations;

    // Maximum on x, y axis.
    private static final int MAX_X = 168;

    private static final int MAX_Y = 200;

    // Width and height of the graph.
    private static final int GRAPH_WIDTH = 1000;

    private static final int GRAPH_HEIGHT = 750;

    // Name to indicate data series for forecast intensity.
    private static final String FORECAST_AID = "FCST";

    // Intervals on x, y axis.
    private int xInterval = 12;

    private int yInterval = 5;

    // Current x/y value based on mouse location.
    private int xValue;

    private int yValue;

    /**
     * Constructor
     * 
     * @param parent
     * 
     * @param dialogTitle
     *            Title for the whole dialog
     * @param chartTitle
     *            Title for the chart.
     * @param intenFcst
     *            Map<Integer, Integer>
     * @param fcstInfo
     *            ForecastInfo
     */
    public IntensityGraphDialog(Shell parent, String dialogTitle,
            String chartTitle, String xAxisLbl, String yAxisLbl,
            ForecastIntensityDialog forecastIntensityDialog,
            ForecastInfo fcstInfo) {

        // Use an SWT.PRIMARY_MODAL dialog.
        super(parent, OCP_DIALOG_SWT_STYLE, OCP_DIALOG_CAVE_STYLE);

        this.dialogTitle = dialogTitle;

        this.chartTitle = chartTitle;

        this.xAxisLabel = xAxisLbl;

        this.yAxisLabel = yAxisLbl;

        this.forecastIntensityDialog = forecastIntensityDialog;

        this.fcstInfo = fcstInfo;
    }

    /**
     * Initialize the dialog components.
     * 
     * @param shell
     */
    @Override
    protected void initializeComponents(Shell shell) {
        shell.setSize(1000, 800);

        setText(dialogTitle);

        // Create wind radii menu
        GraphMenuBuilder menuBuilder = new GraphMenuBuilder(shell, this);
        menuBuilder.getTauMenu().setEnabled(false);
        menuBuilder.getRadiusMenu().setEnabled(false);
        menuBuilder.getScaleMenu().setEnabled(false);
        menuBuilder.getHelpMenu().setEnabled(false);

        Menu graphMenu = menuBuilder.getGraphMenu();

        shell.setMenuBar(graphMenu);

        generateDataset();

        createGraphComp(shell);

        getForecastTrackRecordMap().addMapChangeListener(this);
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
        graphGL.marginHeight = 20;
        graphGL.marginWidth = 20;
        mainComp.setLayout(graphGL);
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        gd.heightHint = GRAPH_HEIGHT;
        gd.widthHint = GRAPH_WIDTH;
        comp.setLayoutData(gd);

        // Create JFreeChart graph.
        Frame frame = SWT_AWT.new_Frame(comp);

        // Create control buttons.
        createControlButtons(mainComp);

        try {
            EventQueue.invokeAndWait(() -> {
                panel = createPlot(chartTitle, xAxisLabel, yAxisLabel, dataset);
                frame.add(panel);

                // Set legend at right.
                panel.getChart().getLegend().setPosition(RectangleEdge.RIGHT);

                // Customize plot's backgraound etc.
                XYPlot plot = panel.getChart().getXYPlot();

                plot.setOutlinePaint(Color.BLACK);
                plot.setOutlineStroke(new BasicStroke(2.0f));

                plot.setBackgroundPaint(graphBGColor);

                plot.setRangeGridlinesVisible(true);
                plot.setRangeGridlinePaint(Color.BLACK);

                plot.setDomainGridlinesVisible(true);
                plot.setDomainGridlinePaint(Color.BLACK);

                // Customize plot's cross-hair
                plot.setDomainCrosshairVisible(true);
                plot.setRangeCrosshairVisible(true);
                Stroke csStroke = new BasicStroke(2.0f);
                plot.setDomainCrosshairStroke(csStroke);
                plot.setRangeCrosshairStroke(csStroke);
                plot.setDomainCrosshairPaint(Color.GREEN);
                plot.setRangeCrosshairPaint(Color.GREEN);

                // Customize the interval and range on x, y axis.
                NumberAxis yAxis = (NumberAxis) plot.getRangeAxis();
                yAxis.setUpperMargin(15);
                yAxis.setTickUnit(new NumberTickUnit(yInterval));
                yAxis.setRange(0, MAX_Y);

                NumberAxis xAxis = (NumberAxis) plot.getDomainAxis();
                xAxis.setUpperMargin(15);
                xAxis.setLowerMargin(15);
                xAxis.setTickUnit(new NumberTickUnit(xInterval));
                xAxis.setMinorTickCount(1);
                xAxis.setRange(0, MAX_X);

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
            logger.error(e.getMessage(), e);
        }

    }

    /**
     * create a XYPlot with given dataset.
     * 
     * @param chartTitle
     *            Title over the chart
     * @param xLabel
     *            Label on X axis
     * @param yLabel
     *            Label on Y axis
     * @param dataset
     *            XYSeriesCollection
     * 
     * @return ChartPanel
     */
    private ChartPanel createPlot(String chartTitle, String xLabel,
            String yLabel, XYSeriesCollection dataset) {

        // Generate major JFreeChart as an XYLineChart
        boolean showLegend = true;
        boolean createTooltip = true;
        boolean createURL = false;

        JFreeChart chart = ChartFactory.createXYLineChart(chartTitle, xLabel,
                yLabel, dataset, PlotOrientation.VERTICAL, showLegend,
                createTooltip, createURL);

        // Add pointer annotation for each aid.
        XYPlot plot = chart.getXYPlot();

        XYLineAndShapeRenderer render = (XYLineAndShapeRenderer) plot
                .getRenderer();

        int ii = 0;
        for (Object obj : dataset.getSeries()) {
            XYSeries xys = (XYSeries) obj;
            String aid = (String) xys.getKey();

            int last = xys.getItemCount() - 1;
            double xx = (double) xys.getX(last);
            double yy = (double) xys.getY(last);

            // Add a pointer annotation to indicate data set.
            final XYPointerAnnotation pointer = new XYPointerAnnotation(aid, xx,
                    yy, 0);
            pointer.setBaseRadius(15.0);
            pointer.setTipRadius(5.0);
            pointer.setPaint(Color.BLACK);
            pointer.setTextAnchor(TextAnchor.HALF_ASCENT_LEFT);
            plot.addAnnotation(pointer);

            // Add an annotation to indicate forecast data set.
            if (xys.getKey().equals(FORECAST_AID)) {
                render.setSeriesStroke(ii, new BasicStroke(4.0f));
                render.setSeriesShape(ii, ShapeUtilities.createDiamond(5));
                render.setSeriesShapesVisible(ii, true);

            } else {
                render.setSeriesStroke(ii, new BasicStroke(2.5f));
            }

            ii++;
        }

        // Add horizontal markers for 34/50/64 knots.
        plot.addRangeMarker(createValueMarker(34, Color.ORANGE));
        plot.addRangeMarker(createValueMarker(50, Color.CYAN));
        plot.addRangeMarker(createValueMarker(64, Color.MAGENTA));

        return new ChartPanel(chart);
    }

    /**
     * Convert data to use in JFreeChart
     *
     * @return XYSeriesCollection data
     */
    private XYSeriesCollection generateDataset() {

        if (dataset == null) {
            dataset = new XYSeriesCollection();
        }

        boolean autoSort = true;
        boolean allowDuplicateXValues = false;

        // Add forecast track data
        addForecastToGraph();

        // Add other obj. aid data
        for (String aid : fcstInfo.getAids()) {
            Map<Integer, Integer> intenMap = fcstInfo.getIntensity(aid);
            if (intenMap != null) {
                XYSeries series = new XYSeries(aid, autoSort,
                        allowDuplicateXValues);
                for (Map.Entry<Integer, Integer> entry : intenMap.entrySet()) {
                    series.add((double) entry.getKey(),
                            (double) entry.getValue());
                }

                dataset.addSeries(series);
            }
        }

        return dataset;
    }

    /*
     * Update graph and forecast for left mouse click.
     * 
     * @param event ChartMouseEvent
     */
    private void handleMouseClicked(ChartMouseEvent event) {
        if (event.getTrigger().getButton() == 1 && xValue != -9999) {
            VizApp.runAsync(() -> {
                if (forecastIntensityDialog.getWorkingTaus()
                        .contains(AtcfTaus.getTau(xValue))) {
                    ForecastIntensityDialog.setWindMax(
                            getForecastTrackRecordMap(), xValue, yValue);
                }
            });
        }
    }

    /*
     * Sampling data at the mouse pointer when mouse moves.
     * 
     * @param event ChartMouseEvent
     */
    private void handleMouseMove(ChartMouseEvent event) {

        if (dataAnnotations == null) {
            dataAnnotations = new LinkedHashMap<>();
        }

        // Find current mouse location and convert to data point.
        Point2D p = panel
                .translateScreenToJava2D(event.getTrigger().getPoint());
        Rectangle2D plotArea = panel.getScreenDataArea();
        XYPlot plot = panel.getChart().getXYPlot();

        if (p.getX() < plotArea.getMinX() || p.getX() > plotArea.getMaxX()
                || p.getY() < plotArea.getMinY()
                || p.getY() > plotArea.getMaxY()) {

            // mouse pointer is out of the plot area
            plot.setDomainCrosshairVisible(false);
            plot.setRangeCrosshairVisible(false);
            xValue = -9999;
            yValue = -9999;
            return;
        }

        double chartX = plot.getDomainAxis().java2DToValue(p.getX(), plotArea,
                plot.getDomainAxisEdge());
        double chartY = plot.getRangeAxis().java2DToValue(p.getY(), plotArea,
                plot.getRangeAxisEdge());

        // Limit the data point within an rectangle.
        xValue = ((int) ((chartX + xInterval / 2.0) / xInterval)) * xInterval;
        yValue = ((int) ((chartY + yInterval / 2.0) / yInterval)) * yInterval;

        // Update cross-hair
        plot.setDomainCrosshairVisible(true);
        plot.setRangeCrosshairVisible(true);
        plot.setDomainCrosshairValue(chartX);
        plot.setRangeCrosshairValue(chartY);

        // Add notations to show all available data
        XYSeriesCollection tmpData = (XYSeriesCollection) plot.getDataset();
        int kk = 0;
        for (Object obj : tmpData.getSeries()) {
            XYSeries ts = (XYSeries) obj;
            String aid = (String) ts.getKey();
            String sampling = "" + aid + ":";
            int closestPoint = -1;
            if (ts.getItemCount() > 0) {
                closestPoint = 0;
                double diffVal = chartX
                        - Math.abs(ts.getDataItem(closestPoint).getXValue());
                for (int i = 1; i < ts.getItemCount(); i++) {
                    if (Math.abs(
                            ts.getDataItem(i).getXValue() - chartX) < diffVal) {
                        closestPoint = i;
                        diffVal = chartX - Math
                                .abs(ts.getDataItem(closestPoint).getXValue());
                    }
                }
            }

            if (closestPoint != -1) {
                // now found the closest point, but is it close enough
                double distance = Math
                        .abs(chartX - ts.getDataItem(closestPoint).getXValue());
                if (distance <= xInterval / 2.0) {
                    sampling += ts.getDataItem(closestPoint).getYValue();
                } else {
                    sampling += "NA";
                }
            } else {
                sampling += "NA";
            }

            XYTextAnnotation ann = dataAnnotations.get(aid);
            if (ann == null) {
                ann = new XYTextAnnotation("", 0, 0);
                ann.setFont(new Font("SansSerif", Font.PLAIN, 14));
                plot.addAnnotation(ann);
                ann.setBackgroundPaint(Color.WHITE);
                ann.setTextAnchor(TextAnchor.HALF_ASCENT_LEFT);
                dataAnnotations.put(aid, ann);
            }

            int annSize = panel.getFontMetrics(ann.getFont()).getHeight();

            double xloc = plot.getDomainAxis().getUpperBound()
                    - xInterval * 1.5;

            ann.setX(xloc);
            ann.setY(plot.getRangeAxis().getUpperBound() - 5
                    - kk * (double) annSize / 3);
            ann.setText(sampling);

            kk++;
        }

        // And an annotation to show the current data point.
        if (samplingAnnotation == null) {
            samplingAnnotation = new XYTextAnnotation("", 0, 0);
            samplingAnnotation.setFont(new Font("SansSerif", Font.PLAIN, 14));
            samplingAnnotation.setBackgroundPaint(Color.WHITE);
            plot.addAnnotation(samplingAnnotation);
        }

        if ((chartX + 12) >= plot.getDomainAxis().getUpperBound()) {
            samplingAnnotation.setX(chartX - 2);
            samplingAnnotation.setTextAnchor(TextAnchor.HALF_ASCENT_RIGHT);
        } else {
            samplingAnnotation.setX(chartX + 2);
            samplingAnnotation.setTextAnchor(TextAnchor.HALF_ASCENT_LEFT);
        }

        samplingAnnotation.setY(chartY);
        samplingAnnotation.setText("" + xValue + "," + yValue);

    }

    /*
     * Add intensity forecast data to graph
     * 
     * @param intenFcst Map<Integer, Integer> intensity forecast
     */
    private void addForecastToGraph() {

        int index = dataset.getSeriesIndex(FORECAST_AID);

        if (index < 0) {
            boolean autoSort = true;
            boolean allowDuplicateXValues = false;

            XYSeries series1 = new XYSeries(FORECAST_AID, autoSort,
                    allowDuplicateXValues);
            boolean hasForecast = false;
            for (AtcfTaus tau : forecastIntensityDialog.getWorkingTaus()) {
                int fcstHr = tau.getValue();
                ForecastTrackRecord rec = getForecastTrackRecordMap()
                        .getByTauRadii(tau, WindRadii.RADII_34_KNOT);
                int windMax = rec != null ? (int) rec.getWindMax() : 0;

                if (windMax > 0) {
                    hasForecast = true;
                    series1.add(fcstHr, windMax);
                }
            }

            if (hasForecast) {
                dataset.addSeries(series1);
            }
        }
    }

    /**
     * Creates the ok, and cancel buttons.
     * 
     * @param parent
     */
    private void createControlButtons(Composite parent) {
        Composite ctrlBtnComp = new Composite(parent, SWT.NONE);
        GridLayout ctrlCompGL = new GridLayout(1, true);
        ctrlCompGL.horizontalSpacing = 160;
        ctrlCompGL.marginWidth = 100;
        ctrlBtnComp.setLayout(ctrlCompGL);

        ctrlBtnComp.setLayoutData(
                new GridData(SWT.FILL, SWT.DEFAULT, false, false));

        Button closeBtn = new Button(ctrlBtnComp, SWT.CENTER);
        closeBtn.setText("Close");
        closeBtn.setLayoutData(
                new GridData(SWT.FILL, SWT.DEFAULT, true, false));
        closeBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                close();
            }
        });

    }

    /*
     * Create a value marker for given value and color.
     * 
     * @param value
     * 
     * @param clr Color
     * 
     */
    private Marker createValueMarker(int value, Color clr) {

        BasicStroke markerStroke = new BasicStroke(2.0f);
        final Marker valMarker = new ValueMarker(value);
        valMarker.setPaint(clr);
        valMarker.setLabel(String.valueOf(value));
        valMarker.setLabelAnchor(RectangleAnchor.BOTTOM_LEFT);
        valMarker.setLabelTextAnchor(TextAnchor.TOP_LEFT);
        valMarker.setStroke(markerStroke);

        return valMarker;
    }

    @Override
    public void handleMapChange(
            MapChangeEvent<? extends RecordKey, ? extends ForecastTrackRecord> event) {
        if (dataset != null) {
            EventQueue.invokeLater(() -> {
                XYSeries ts = dataset.getSeries(FORECAST_AID);
                if (ts != null) {
                    event.diff.getAddedKeys().stream().forEach(
                            x -> ts.addOrUpdate(x.getTau(), (int) event.diff
                                    .getNewValue(x).getWindMax()));
                    event.diff.getChangedKeys().stream().forEach(
                            x -> ts.addOrUpdate(x.getTau(), (int) event.diff
                                    .getNewValue(x).getWindMax()));
                }
            });
        }
    }

    @Override
    protected void disposed() {
        super.disposed();
        getForecastTrackRecordMap().removeMapChangeListener(this);
    }

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
                        logger.error("Error saving graphic: " + e.toString(),
                                e);
                    }
                }
            });
        }
    }

    private ForecastTrackRecordMap getForecastTrackRecordMap() {
        return forecastIntensityDialog.getForecastTrackRecordMap();
    }

}