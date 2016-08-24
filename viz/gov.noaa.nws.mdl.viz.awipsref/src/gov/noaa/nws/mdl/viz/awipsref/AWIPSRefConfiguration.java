package gov.noaa.nws.mdl.viz.awipsref;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Holds configuration for AWIPS II Reference System.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date          Ticket#  Engineer    Description
 * ------------- -------- ----------- --------------------------
 * Jul 01, 2016           jburks       Initial creation
 * Jul 09, 2016           jburks       Added browserPath to configuration, and refactor to awips reference
 * 
 * </pre>
 * 
 * @author jburks
 * @version 1.0
 */

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class AWIPSRefConfiguration {

    @XmlElement
    public String serverLocation;

    @XmlElement
    String browserPath;

    public String getServerLocation() {
        return serverLocation;
    }

    public void setServerLocation(String serverLocation) {
        this.serverLocation = serverLocation;
    }

    public String getBrowserPath() {
        return browserPath;
    }

    public void setBrowserPath(String browserPath) {
        this.browserPath = browserPath;
    }

}
