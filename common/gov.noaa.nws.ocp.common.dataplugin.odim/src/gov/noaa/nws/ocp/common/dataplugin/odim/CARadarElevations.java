/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.common.dataplugin.odim;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlRootElement;

import com.raytheon.uf.common.localization.IPathManager;
import com.raytheon.uf.common.localization.LocalizationContext;
import com.raytheon.uf.common.localization.LocalizationContext.LocalizationLevel;
import com.raytheon.uf.common.localization.LocalizationContext.LocalizationType;
import com.raytheon.uf.common.localization.LocalizationFile;
import com.raytheon.uf.common.localization.LocalizationUtil;
import com.raytheon.uf.common.localization.PathManagerFactory;
import com.raytheon.uf.common.localization.exception.LocalizationException;
import com.raytheon.uf.common.serialization.SerializationException;
import com.raytheon.uf.common.serialization.SingleTypeJAXBManager;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;

/**
 * Contains descriptions of elevation angles used by Canadian S-Band radars.
 * Also defines tile angle groups.
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Sep 12, 2022 DCS 21569  dfriedman   Initial creation
 * </pre>
 *
 * @author dfriedman
 */
public class CARadarElevations {

    private static final IUFStatusHandler handler = UFStatus
            .getHandler(CARadarElevations.class);

    private static final String PATH = LocalizationUtil.join("odim",
            "caRadarElevs.xml");

    // Vcp.code -> Vcp
    private Map<String, Vcp> vcpMap;

    private Map<String, Vcp> radarMap;

    private List<TiltAngleGroup> tiltAngleGroups;

    public CARadarElevations(Map<String, Vcp> vcpMap, Map<String, Vcp> radarMap,
            List<TiltAngleGroup> tiltAngleGroups) {
        super();
        this.vcpMap = vcpMap;
        this.radarMap = radarMap;
        this.tiltAngleGroups = tiltAngleGroups;
    }

    public static CARadarElevations load()
            throws LocalizationException, SerializationException, IOException {
        return load(null);
    }

    public static CARadarElevations load(String site)
            throws LocalizationException, SerializationException, IOException {
        LocalizationFile lf = getLocalizationFile(site);

        Doc doc;
        try (InputStream i = lf.openInputStream()) {
            doc = SingleTypeJAXBManager.createWithoutException(Doc.class)
                    .unmarshalFromInputStream(i);
        }

        Map<String, Vcp> vcpMap = new HashMap<>();
        Map<String, Vcp> radarMap = new HashMap<>();
        List<TiltAngleGroup> tiltAngleGroups = doc.tiltAngleGroups != null
                ? Arrays.asList(doc.tiltAngleGroups)
                : Collections.emptyList();

        for (Vcp vcp : doc.vcpList) {
            if (vcp.elevations == null) {
                vcp.elevations = new Double[0];
            }
            vcpMap.put(vcp.code, vcp);
        }
        for (RadarAssignment ra : doc.radarAssignmentList) {
            Vcp vcp = vcpMap.get(ra.vcp);
            if (vcp != null) {
                if (ra.radars != null) {
                    for (String id : ra.radars) {
                        radarMap.put(id, vcp);
                    }
                }
            } else {
                handler.error(
                        String.format("%s: invalid vcp \"%s\"", lf, ra.vcp));
            }
        }
        for (TiltAngleGroup group : tiltAngleGroups) {
            Arrays.sort(group.angles);
        }

        return new CARadarElevations(vcpMap, radarMap, tiltAngleGroups);
    }

    public Vcp getVcp(String name) {
        return vcpMap.get(name);
    }

    public Double[] getRadarElevations(String radar) {
        Vcp vcp = radarMap.get(radar);
        return vcp != null ? vcp.elevations : null;
    }

    public double getPrimaryElevationAngle(double angle) {
        for (TiltAngleGroup group : tiltAngleGroups) {
            int i = Arrays.binarySearch(group.angles, angle);
            if (i >= 0) {
                return group.primary;
            }
        }
        return angle;
    }

    private static LocalizationFile getLocalizationFile(String site) {
        IPathManager pm = PathManagerFactory.getPathManager();
        LocalizationContext context = null;

        // if the site isn't given, default to the regular site
        if (site == null) {
            context = pm.getContext(LocalizationType.COMMON_STATIC,
                    LocalizationLevel.SITE);
        } else {
            context = pm.getContextForSite(LocalizationType.COMMON_STATIC,
                    site);
        }

        LocalizationFile lf = pm.getLocalizationFile(context, PATH);
        if (!lf.exists()) {
            LocalizationContext baseContext = pm.getContext(
                    LocalizationType.COMMON_STATIC, LocalizationLevel.BASE);
            lf = pm.getLocalizationFile(baseContext, PATH);
        }
        return lf;
    }

    @XmlAccessorType(XmlAccessType.NONE)
    public static class Vcp {
        @XmlAttribute
        private String code;

        @XmlAttribute
        private String label;

        @XmlAttribute(name = "elevs")
        @XmlList
        private Double[] elevations;
    }

    @XmlAccessorType(XmlAccessType.NONE)
    @XmlRootElement(name = "elevs")
    private static class Doc {
        @XmlElement(name = "vcp")
        private Vcp[] vcpList;

        @XmlElement(name = "radar")
        private RadarAssignment[] radarAssignmentList;

        @XmlElement(name = "group")
        private TiltAngleGroup[] tiltAngleGroups;
    }

    @XmlAccessorType(XmlAccessType.NONE)
    private static class RadarAssignment {
        @XmlAttribute
        private String[] radars;

        @XmlAttribute
        private String vcp;
    }

    @XmlAccessorType(XmlAccessType.NONE)
    private static class TiltAngleGroup {
        @XmlAttribute
        private double primary;

        @XmlAttribute
        @XmlList
        private double[] angles;
    }
}
