package gov.noaa.nws.obs.edex.plugin.geodata.description;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * ProductDescriptions (class)
 * 
 * A list of GeoData/GeoDB Product Descriptions. This class serves as a
 * container for individual Product Description entries, with the appropriate
 * methods for adding, removing, etc.
 * 
 * See {@link PointSetProductDescriptions}, after which this class has been
 * modeled, for extra information.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 07/27/2016     19064    mcomerford  Initial creation (DCS 19064)
 * 
 * </pre>
 * 
 * @author matt.comerford
 * @version 1.0
 */

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class ProductDescriptions {

    @XmlElement(name = "description")
    private List<ProductDescription> descriptions;

    public List<ProductDescription> getDescriptions() {
        return descriptions;
    }

    /**
     * @return the list of ProductDescription entries.
     */
    public String listDescriptions() {
        return descriptions.toArray().toString();
    }

    /**
     * Set the list of ProductDescription entries.
     * 
     * @param descriptions
     *            The list of ProductDescription entries to set for this
     *            container.
     */
    public void setDescriptions(List<ProductDescription> descriptions) {
        this.descriptions = descriptions;
    }

    /**
     * Add a ProductDescription to the ProductDescriptions container.
     * 
     * @param description
     *            The ProductDescription to add to the container.
     */
    public void addDescription(ProductDescription description) {
        if (this.descriptions == null) {
            this.descriptions = new ArrayList<>();
        }
        this.descriptions.add(description);
    }

    /**
     * Add a ProductDescriptions list to this container.
     * 
     * @param descriptionsList
     *            The ProductDescriptions instance to add into this container.
     */
    public void addDescriptions(ProductDescriptions descriptionsList) {
        if (this.descriptions == null) {
            this.descriptions = new ArrayList<>();
        }
        this.descriptions.addAll(descriptionsList.getDescriptions());
    }

}
