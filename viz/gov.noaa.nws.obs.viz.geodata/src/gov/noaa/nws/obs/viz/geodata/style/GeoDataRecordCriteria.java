package gov.noaa.nws.obs.viz.geodata.style;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.raytheon.uf.common.style.MatchCriteria;

import gov.noaa.nws.obs.common.dataplugin.geodata.GeoDataRecord;

/**
 * GeoDataRecordCriteria
 *
 * Class which defines the MatchCriteria for matching a GeoDataRecord to this
 * class' corresponding GeometryPreferences (StyleRules). The matching criteria
 * defined in this class are three lists, two representing a GeoDataRecord's
 * possible "source" and "product" fields, and the final holding the names of a
 * GeoDataRecord's Attribute(s).
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 08/20/2016     19064    mcomerford   Initial creation (DCS 19064)
 *
 * </pre>
 *
 * @author matt.comerford
 * @version 1.0
 */

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = "geoDataMatches")
public class GeoDataRecordCriteria extends MatchCriteria {

    /*
     * The following lists may contain entries that apply to multiple
     * GeoDataRecords (if a single StyleRule should apply for more than one
     * "type" of GeoDataRecord.
     */
    @XmlElement(name = "attribute")
    private List<String> attributeNames = new ArrayList<>();

    @XmlElement(name = "product")
    private List<String> products = new ArrayList<>();

    @XmlElement(name = "source")
    private List<String> sources = new ArrayList<>();

    /**
     * Default constructor.
     */
    public GeoDataRecordCriteria() {

    }

    /**
     * Constructor to build a MatchCriteria from lists (in case we are working
     * with multiple records i.e. different sources, products, Attributes, etc.
     *
     * @param sources
     *            The list of GeoDataRecord(s) source.
     * @param products
     *            The list of GeoDataRecord(s) product.
     * @param attributeNames
     *            The list of GeoDataRecord Float/Integer/StringAttribute names
     *            to match against.
     */
    public GeoDataRecordCriteria(List<String> sources, List<String> products,
            List<String> attributeNames) {
        this.sources = sources;
        this.products = products;
        this.attributeNames = attributeNames;
    }

    /**
     * Construct a MatchCriteria from a single GeoDataRecord.
     *
     * @param record
     *            The GeoDataRecord used to build the MatchCriteria.
     */
    public GeoDataRecordCriteria(GeoDataRecord record) {
        this.sources = Arrays.asList(record.getSource());
        this.products = Arrays.asList(record.getProduct());
        this.attributeNames = record.getAttNames();
    }

    @Override
    public int matches(MatchCriteria aCriteria) {

        int rval = -1;

        if (aCriteria instanceof GeoDataRecordCriteria) {
            rval = 0;
            int checkVal = 0;
            GeoDataRecordCriteria criteria = (GeoDataRecordCriteria) aCriteria;

            if (!sources.isEmpty() && !criteria.getSources().isEmpty()) {
                for (String source : sources) {
                    if (criteria.getSources().contains(source)) {
                        checkVal++;
                    }
                }
                if (checkVal == 0) {
                    return -1;
                }
            }

            rval += checkVal;
            checkVal = 0;

            if (!products.isEmpty() && !criteria.getProducts().isEmpty()) {
                for (String product : products) {
                    if (criteria.getProducts().contains(product)) {
                        checkVal++;
                    }
                }
                if (checkVal == 0) {
                    return -1;
                }
            }

            rval += checkVal;

            /*
             * Attributes are optional to a GeoDataRecord, so they are not
             * grounds for failing a match
             */
            if (!this.attributeNames.isEmpty()
                    && !criteria.getAttributeNames().isEmpty()) {
                for (String name : attributeNames) {
                    if (criteria.getAttributeNames().contains(name)) {
                        rval++;
                    }
                }
            }

        }
        return rval;
    }

    /**
     * @return the attributeNames
     */
    public List<String> getAttributeNames() {
        return attributeNames;
    }

    /**
     * @param attributeNames
     *            the attributeNames to set
     */
    public void setAttributeNames(List<String> attributeNames) {
        this.attributeNames = attributeNames;
    }

    /**
     * @return the products
     */
    public List<String> getProducts() {
        return products;
    }

    /**
     * @param products
     *            the products to set
     */
    public void setProducts(List<String> products) {
        this.products = products;
    }

    /**
     * @return the sources
     */
    public List<String> getSources() {
        return sources;
    }

    /**
     * @param sources
     *            the sources to set
     */
    public void setSources(List<String> sources) {
        this.sources = sources;
    }

    @Override
    public String toString() {

        String names = "[";
        for (String name : attributeNames) {
            names += " " + name + ",";
        }
        names += "]";

        return "product: " + String.valueOf(products) + "\t" + "source: "
                + String.valueOf(sources) + "\t" + "atts: " + names;
    }

}