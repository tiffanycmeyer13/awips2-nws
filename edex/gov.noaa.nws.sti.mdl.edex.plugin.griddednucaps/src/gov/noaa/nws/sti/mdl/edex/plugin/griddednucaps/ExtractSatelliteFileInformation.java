package gov.noaa.nws.sti.mdl.edex.plugin.griddednucaps;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.commons.io.FileUtils;

import ucar.nc2.NetcdfFile;

/**
 * Extracts a few pieces from the satellite hdf file for use in aggregating the
 * files.
 *
 * <pre>
*
* SOFTWARE HISTORY
*
* Date          Ticket#  Engineer  Description
* ------------- -------- --------- -----------------
* Oct 23, 2018   DCS-18691 jburks  Initial creation
 *
 * </pre>
 *
 * @author jburks
 */
public class ExtractSatelliteFileInformation implements Processor {

    /**
     * The formatter to extract the date out of the date string global attribute
     * of the hdf file.
     */
    private static SimpleDateFormat formatter = new SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm:ss'Z'");

    private static Pattern titleFormat = Pattern.compile("IUTN(\\d{2})");

    /**
     * For each file pull out of the HDF file several attributes to be used for
     * aggregation
     * 
     * @param exchange
     *            the exchange to be processed.
     */
    @Override
    public void process(Exchange exchange) throws Exception {
        Object body = exchange.getIn().getBody();
        if (!(body instanceof File)) {
            return;
        }
        File file = (File) body;
        String fileToProcess = file.getAbsolutePath();

        NetcdfFile ncFile = NetcdfFile.open(fileToProcess);
        String satelliteId = ncFile.findGlobalAttribute("satellite_name")
                .getStringValue();
        String title = ncFile.findGlobalAttribute("title").getStringValue();
        Matcher matcher = titleFormat.matcher(title);
        if (matcher.find()) {
            try {
                int value = Integer.parseInt(matcher.group(1));
                // Convert to satellite number
                int satellite_number = (int) (value / 10.) - 1;
                if (satellite_number > 0) {
                    satelliteId = "NOAA" + (20 + (satellite_number));
                }
            } catch (Exception e) {
                // noop Just protecting in case there is a parseInt issue.
            }
        }
        int ascend = ncFile.findGlobalAttribute("ascend_descend_data_flag")
                .getNumericValue().intValue();
        int orbit_start_number = ncFile
                .findGlobalAttribute("start_orbit_number").getNumericValue()
                .intValue();
        Date startDate = formatter.parse(ncFile
                .findGlobalAttribute("time_coverage_start").getStringValue());
        Date endDate = formatter.parse(ncFile
                .findGlobalAttribute("time_coverage_end").getStringValue());
        ncFile.close();
        // Put outgoing information in the the OutBound Message since I don't
        // need any of the
        // existing data.
        exchange.getOut().setHeader("satelliteId", satelliteId);
        exchange.getOut().setHeader("minTime", startDate.getTime());
        exchange.getOut().setHeader("maxTime", endDate.getTime());
        exchange.getOut().setHeader("ascend", ascend);
        exchange.getOut().setHeader("orbit_start_number", orbit_start_number);

        exchange.getOut().setHeader("ingestTime",
                exchange.getIn().getHeader("enqueueTime"));
        File fileForDecoder = new File(fileToProcess + ".gn.nc");
        FileUtils.moveFile(file, fileForDecoder);
        exchange.getOut().setHeader("filePath",
                fileForDecoder.getAbsolutePath());

    }

}
