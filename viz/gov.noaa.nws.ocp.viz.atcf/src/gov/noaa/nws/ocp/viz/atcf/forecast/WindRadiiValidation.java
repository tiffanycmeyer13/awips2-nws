/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.atcf.forecast;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import gov.noaa.nws.ocp.common.atcf.configuration.AtcfConfigurationManager;
import gov.noaa.nws.ocp.common.atcf.configuration.AtcfSitePreferences;
import gov.noaa.nws.ocp.common.dataplugin.atcf.ForecastTrackRecord;
import gov.noaa.nws.ocp.viz.atcf.WindRadii;
import gov.noaa.nws.ocp.viz.atcf.aids.AtcfTaus;

/**
 * Encapsulates rules and state used to determine if it is valid to edit
 * forecast wind radii for given forecast times and radii categories.
 * <p>
 * Note that logic based on ATCF site preferences is not kept in sync with
 * localization changes.
 *
 * <pre>
 * SOFTWARE HISTORY 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 16, 2021 88721      dfriedman   initial creation
 *
 * </pre>
 *
 * @author dfriedman
 * @version 1.0
 */
public class WindRadiiValidation {

    private AtcfSitePreferences prefs;

    private ForecastTrackRecordMap forecastTrackRecordMap;

    public WindRadiiValidation(ForecastTrackRecordMap forecastTrackRecordMap) {
        this.forecastTrackRecordMap = forecastTrackRecordMap;
        this.prefs = AtcfConfigurationManager.getInstance().getPreferences();
    }

    /**
     * Determines if it is valid, in general, to edit wind radii at the given
     * forecast time. This depends both on site rules and the associated
     * forecast state.
     *
     * @param tau
     * @return
     */
    public boolean isTauAllowed(AtcfTaus tau) {
        // (Comment from ForecastWindRadiiDialog.setTauBtnStatus:
        // TODO Disable TAU 0 since radii are the same for different aids?
        if (tau.getValue() <= 0) {
            return false;
        }
        if (tau == ForecastDialog.EXPERIMENTAL_TAU && !prefs.getUseTau60()) {
            return false;
        }
        return isWindRadiiAllowed(WindRadii.RADII_34_KNOT, tau);
    }

    /**
     * Determines if it is valid to edit the wind radii for the given radii
     * category at the given forecast time. This depends both on site rules and
     * the associated forecast state.
     * <p>
     * Note that isWindRadiiAllowed(radii, tau) does NOT imply
     * isTauAllowed(tau)!
     *
     * @param radii
     * @param tau
     * @return
     */
    public boolean isWindRadiiAllowed(WindRadii radii, AtcfTaus tau) {
        if (tau.getValue() > prefs.getTauLimitAllWindRadii()) {
            return false;
        }
        if (radii.getValue() >= 64
                && tau.getValue() > prefs.getTauLimit64ktWindRadii()) {
            return false;
        }

        ForecastTrackRecord rec34 = forecastTrackRecordMap.getByTauRadii(tau,
                WindRadii.RADII_34_KNOT);
        /*
         * Is there a record for the tau AND is the radii category non-zero AND
         * is the forecast max wind greater than or equal to the minimum for the
         * radii category?
         */
        return rec34 != null && radii.getValue() > 0
                && (int) rec34.getWindMax() >= radii.getValue();
    }

    /**
     * Check if wind radii for a given tau are missing or invalid.
     *
     * @param forecastTrackRecordMap
     * @param tau
     * @return null if radii are valid or a validation message if invalid
     */
    public String checkRadiiValid(ForecastTrackRecordMap forecastTrackRecordMap,
            AtcfTaus tau) {
        List<WindRadii> invalidRadii = null;

        for (WindRadii radii : WindRadii.values()) {
            if (radii.getValue() > 0) {
                ForecastTrackRecord rec = forecastTrackRecordMap
                        .getByTauRadii(tau, radii);
                if (rec != null) {
                    boolean isCircle = ForecastDialog.FULL_CIRCLE
                            .equals(rec.getRadWindQuad());
                    /*
                     * Validation check below assumes getWindRadii converts
                     * "missing" to zero.
                     */
                    int[] quadRadii = rec.getWindRadii();
                    boolean isValid;
                    if (isCircle) {
                        /*
                         * Note that this does not check consistency of other
                         * quadRadii.
                         */
                        isValid = quadRadii[0] != 0;
                    } else {
                        isValid = false;
                        for (int r : quadRadii) {
                            if (r != 0) {
                                isValid = true;
                            }
                        }
                    }
                    if (!isValid) {
                        if (invalidRadii == null) {
                            invalidRadii = new ArrayList<>();
                        }
                        invalidRadii.add(radii);
                    }
                }
            }
        }

        if (invalidRadii != null) {
            return String.format("Missing %s wind radii for TAU %d",
                    invalidRadii.stream().map(WindRadii::getName)
                            .collect(Collectors.joining(", ")),
                    tau.getValue()) + "\nPlease correct it.";
        } else {
            return null;
        }
    }

}
