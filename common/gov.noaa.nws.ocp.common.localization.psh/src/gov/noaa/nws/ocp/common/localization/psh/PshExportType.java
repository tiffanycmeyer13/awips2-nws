package gov.noaa.nws.ocp.common.localization.psh;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlRootElement;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;

/**
 * Enum representing possible export locations.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 01 DEC 2017  #41620     wpaintsil   Initial creation
 * 11 JAN 2018  DCS19326   jwu         Baseline version.
 * 
 * </pre>
 * 
 * @author wpaintsil
 * @version 1.0
 */
@DynamicSerialize
@XmlRootElement(name = "PshExportType")
@XmlAccessorType(XmlAccessType.NONE)
@XmlEnum
public enum PshExportType {
    NONE("None"), LOCALIZATION("Localization"), USER("User");

    private String name;

    private PshExportType(String name) {
        this.name = name;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    public static PshExportType getType(String name) {
        switch (name) {
        case "Localization":
            return LOCALIZATION;
        case "User":
            return USER;
        default:
            return NONE;
        }
    }
}
