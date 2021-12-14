/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
*/
package gov.noaa.nws.ocp.viz.drawing.elements;

import java.util.ArrayList;
import java.util.List;

/**
 * Define a Product Class - containing a list of Layers and properties.
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 05, 2018 #48178     jwu         Extracted from NCEP PGEN.
 *
 * </pre>
 *
 * @author jwu
 * @version 1.0
 */
public class Product {

    private static final String DEFAULT = "Default";

    /** The fields */
    private String name;

    private String type;

    private String forecaster;

    private String center;

    private String status;

    private List<Layer> layers;

    private boolean onOff;

    private boolean inUse;

    public Product() {
        super();
        name = DEFAULT;
        type = DEFAULT;
        forecaster = DEFAULT;
        setCenter(DEFAULT);
        status = "UNKNOWN";
        layers = new ArrayList<>();
        onOff = true;
        inUse = true;
    }

    public Product(String myName, String myType, String myForecaster,
            List<Layer> myLayers) {
        name = myName;
        type = myType;
        forecaster = myForecaster;
        setCenter(DEFAULT);
        setStatus("UNKNOWN");
        layers = myLayers;
        onOff = true;
        inUse = true;
    }

    public String getName() {
        return name;
    }

    public void setName(String myName) {
        name = myName;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public String getForecaster() {
        return forecaster;
    }

    public void setForecaster(String myForecaster) {
        forecaster = myForecaster;
    }

    public List<Layer> getLayers() {
        return layers;
    }

    public void setLayers(List<Layer> myLayers) {
        layers = myLayers;
    }

    public Layer getLayer(int index) {
        return layers.get(index);
    }

    public Layer getLayer(String layerName) {
        for (Layer ly : layers) {
            if (ly.getName().equals(layerName)) {
                return ly;
            }
        }

        return null;
    }

    public void addLayer(Layer layer) {
        layers.add(layer);
    }

    public void addLayer(int index, Layer layer) {
        layers.add(index, layer);
    }

    public void removeLayer(int index) {
        layers.remove(index);
    }

    /**
     * Removes the specified layer from this product
     * 
     * @param lyr
     *            The Layer to remove
     */
    public void removeLayer(Layer lyr) {
        layers.remove(lyr);
    }

    /**
     * Removes all layers.
     */
    public void clear() {
        layers.clear();
    }

    /**
     * @return the center
     */
    public String getCenter() {
        return center;
    }

    /**
     * @param center
     *            the center to set
     */
    public void setCenter(String center) {
        this.center = center;
    }

    /**
     * @return the status
     */
    public String getStatus() {
        return status;
    }

    /**
     * @param status
     *            the status to set
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * @return the onOff
     */
    public boolean isOnOff() {
        return onOff;
    }

    /**
     * @param onOff
     *            the onOff to set
     */
    public void setOnOff(boolean onOff) {
        this.onOff = onOff;
    }

    /**
     * @return the inUse
     */
    public boolean isInUse() {
        return inUse;
    }

    /**
     * @param inUse
     *            the inUse to set
     */
    public void setInUse(boolean inUse) {
        this.inUse = inUse;
    }

    public String toString() {
        StringBuilder result = new StringBuilder("\n");
        result.append("name:\t\t" + name + "\n");
        result.append("type:\t\t" + type + "\n");
        result.append("forecaster:\t\t" + forecaster + "\n");
        result.append("center:\t\t" + center + "\n");
        result.append("OnOff:\t\t" + onOff + "\n");
        result.append("InUse:\t\t" + inUse + "\n");

        result.append("\nTotal Layers:\t" + layers.size() + "\n");

        int ii = 0;
        for (Layer ly : layers) {
            result.append("Layer:\t" + ii);
            result.append(ly);
            result.append("\n");
            ii++;
        }

        return result.toString();
    }

    /**
     * Checks if this product contains the specified Layer
     * 
     * @param lyr
     *            - Layer to check
     * @return true, if lyr exists in this product
     */
    public boolean contains(Layer lyr) {
        return layers.contains(lyr);
    }

    /**
     * Test if this product contains any layers
     * 
     * @return true, if layer is empty
     */
    public boolean isEmpty() {
        return layers.isEmpty();
    }

    /**
     * Deep copy of the product
     */
    public Product copy() {

        Product prd = new Product();

        prd.setName(this.getName());
        prd.setType(this.getType());
        prd.setForecaster(this.getForecaster());
        prd.setCenter(this.getCenter());
        prd.setOnOff(this.isOnOff());
        prd.setInUse(this.isInUse());

        for (Layer lyr : this.getLayers()) {
            prd.addLayer(lyr.copy());
        }

        return prd;
    }

}