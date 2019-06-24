/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.ocp.viz.psh.data;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;

/**
 * Class to hold CWA information
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#     Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 11, 2017 #35268     astrakovsky  Initial creation.
 * 
 * </pre>
 * 
 * @author astrakovsky
 * @version 1.0
 * 
 */

public class PshCwa {
		
	private String cwaName; 	
	private String wfoName;		
	private Coordinate centroid;
	private Geometry shape;
	
	/**
	 * Constructor
	 */
	public PshCwa() {
		
	}
	
	/**
	 * Constructor
	 * 
	 * @param cwa
	 * @param wfo
	 * @param centroid
	 * @param shape
	 */
	public PshCwa( String cwa, String wfo, Coordinate centroid, Geometry shape ) {
		this.setCwaName(cwa);
		this.setWfoName(wfo);
		this.setCentroid(centroid);
		this.setShape(shape);
	}
	
	/**
	 * Set Centroid
	 * @param centriod
	 */
	public void setCentroid(Coordinate centroid) {
		this.centroid = centroid;
	}

	/**
	 * Get Centroid
	 * @return
	 */
	public Coordinate getCentroid() {
		return centroid;
	}

	/**
	 * set shape
	 * @param shape
	 */
	public void setShape(Geometry shape) {
		this.shape = shape;
	}

	/**
	 * get shape
	 * @return
	 */
	public Geometry getShape() {
		return shape;
	}
	
	/**
	 * find the 
	 * @param geo
	 * @return
	 */
	public boolean intersectGeometry(Geometry geo){
		if ( shape != null ){
			return shape.intersects(geo);
		}
		else return false;
	}


	/**
	 * set cwa name
	 * @param cwa
	 */
	public void setCwaName(String cwa) {
		this.cwaName = cwa;
	}

	/**
	 * get cwa name
	 * @return
	 */
	public String getCwaName() {
		return cwaName;
	}

	/**
	 * set wfo name
	 * @param wfo
	 */
	public void setWfoName(String wfo) {
		this.wfoName = wfo;
	}

	/**
	 * get wfo name
	 * @return
	 */
	public String getWfoName() {
		return wfoName;
	}
}