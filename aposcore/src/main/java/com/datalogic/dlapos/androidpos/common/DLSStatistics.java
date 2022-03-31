package com.datalogic.dlapos.androidpos.common;

import android.content.Context;

import androidx.core.util.Pair;

import com.datalogic.dlapos.confighelper.DLAPosConfigHelper;
import com.datalogic.dlapos.confighelper.configurations.accessor.IhsHelper;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * {@code DLSStatistics} provides implementation of interpreting, reporting, and
 * logging statistics data returned from the three host commands i, h, and s.
 * Together these three commands return all Value Added Features (VAF) which are
 * commonly referred to as "statistics".
 */
public class DLSStatistics {
    private IhsHelper ihsHelper;
    private final String sep = System.getProperty("line.separator");
    private final Map<String, String> parserMap = new HashMap<>();

    private static final ArrayList<String> XML_UPOS = new ArrayList<>(Arrays.asList(
            DLSJposConst.DLS_S_CAP_PWR_REPORT,
            DLSJposConst.DLS_S_CAP_STATS_REPORT,
            DLSJposConst.DLS_S_CAP_UPDATE_STATS,
            DLSJposConst.DLS_S_CAP_COMP_FW_VER,
            DLSJposConst.DLS_S_CAP_UPDATE_FW));

    private static final ArrayList<String> XML_EQUIPMENT = new ArrayList<>(Arrays.asList(
            "UnifiedPOSVersion",
            "DeviceCategory",
            "ManufacturerName",
            "ModelName",
            "SerialNumber",
            "FirmwareRevision",
            "ConfigRevision",
            "Interface",
            "InstallationDate"));

    private DLSStatistics() {
    }

    DLSStatistics(IhsHelper helper) {
        this.ihsHelper = helper;
    }

    /**
     * Returns a singleton instance of the class.
     *
     * @return {@code DLSStatistics} instance
     */
    public static DLSStatistics getInstance(Context context) {
        if (DLSStatisticsHolder.INSTANCE.ihsHelper == null) {
            DLSStatisticsHolder.INSTANCE.ihsHelper = DLAPosConfigHelper.getInstance(context).getIhsHelper();
        }
        return DLSStatisticsHolder.INSTANCE;
    }

    private void addLine(StringBuilder sb, String msg) {
        sb.append(msg);
        sb.append(sep);
    }

    private void buildParserMap() {
        parserMap.put(DLSJposConst.DLS_S_SERIAL_NUMBER, ihsHelper.getFieldName("S", IhsHelper.FrameType.INFORMATION));
//        parserMap.put(DLSJposConst.DLS_S_DEVICE_CATEGORY, ihsHelper.getFieldName("S", IhsHelper.FrameType.INFORMATION));
//        parserMap.put(DLSJposConst.DLS_S_CONTROL_VERSION, ihsHelper.getFieldName("S", IhsHelper.FrameType.INFORMATION));
        parserMap.put(DLSJposConst.DLS_S_MODEL_NAME, ihsHelper.getFieldName("M", IhsHelper.FrameType.INFORMATION));
//        parserMap.put(DLSJposConst.DLS_S_MANUFACTURE_DATE, ihsHelper.getFieldName("S", IhsHelper.FrameType.INFORMATION));
        parserMap.put(DLSJposConst.DLS_S_SCANNER_BOARD_SERIAL, ihsHelper.getFieldName("m", IhsHelper.FrameType.INFORMATION));
        parserMap.put(DLSJposConst.DLS_S_FIRMWARE_VERSION, ihsHelper.getFieldName("A", IhsHelper.FrameType.INFORMATION));
        parserMap.put(DLSJposConst.DLS_S_INTERFACE, ihsHelper.getFieldName("I", IhsHelper.FrameType.INFORMATION));
//        parserMap.put(DLSJposConst.DLS_S_INSTALL_DATE, ihsHelper.getFieldName("S", IhsHelper.FrameType.INFORMATION));
        parserMap.put(DLSJposConst.DLS_S_HOURS_POWERED_COUNT, ihsHelper.getFieldName("P", IhsHelper.FrameType.STATISTICS));
//        parserMap.put(DLSJposConst.DLS_S_COMM_ERROR_COUNT, ihsHelper.getFieldName("S", IhsHelper.FrameType.INFORMATION));
//        parserMap.put(DLSJposConst.DLS_S_CAP_COMP_FW_VER, ihsHelper.getFieldName("S", IhsHelper.FrameType.INFORMATION));
//        parserMap.put(DLSJposConst.DLS_S_CAP_STATS_REPORT, ihsHelper.getFieldName("S", IhsHelper.FrameType.INFORMATION));
//        parserMap.put(DLSJposConst.DLS_S_CAP_PWR_REPORT, ihsHelper.getFieldName("S", IhsHelper.FrameType.INFORMATION));
//        parserMap.put(DLSJposConst.DLS_S_CAP_UPDATE_FW, ihsHelper.getFieldName("S", IhsHelper.FrameType.INFORMATION));
//        parserMap.put(DLSJposConst.DLS_S_CAP_UPDATE_STATS, ihsHelper.getFieldName("S", IhsHelper.FrameType.INFORMATION));
//        parserMap.put(DLSJposConst.DLS_S_SERVICE_VERSION, ihsHelper.getFieldName("S", IhsHelper.FrameType.INFORMATION));
        parserMap.put(DLSJposConst.DLS_S_APPLICATION_REVISION, ihsHelper.getFieldName("R", IhsHelper.FrameType.INFORMATION));
        parserMap.put(DLSJposConst.DLS_S_SCANNER_BOOT_ROM_ID, ihsHelper.getFieldName("B", IhsHelper.FrameType.INFORMATION));
        parserMap.put(DLSJposConst.DLS_S_UNIVERSAL_ROM_ID, ihsHelper.getFieldName("U", IhsHelper.FrameType.INFORMATION));
        parserMap.put(DLSJposConst.DLS_S_UNIVERSAL_BOOT_ROM_ID, ihsHelper.getFieldName("u", IhsHelper.FrameType.INFORMATION));
        parserMap.put(DLSJposConst.DLS_S_MOTOR_HEALTH, ihsHelper.getFieldName("m", IhsHelper.FrameType.HEALTH));
        parserMap.put(DLSJposConst.DLS_S_H_LASER_HEALTH, ihsHelper.getFieldName("h", IhsHelper.FrameType.HEALTH));
        parserMap.put(DLSJposConst.DLS_S_V_LASER_HEALTH, ihsHelper.getFieldName("v", IhsHelper.FrameType.HEALTH));
        parserMap.put(DLSJposConst.DLS_S_EAS_HEALTH, ihsHelper.getFieldName("e", IhsHelper.FrameType.HEALTH));
        parserMap.put(DLSJposConst.DLS_S_CAMERA_HEALTH, ihsHelper.getFieldName("c", IhsHelper.FrameType.HEALTH));
        parserMap.put(DLSJposConst.DLS_S_MOTOR_TIME, ihsHelper.getFieldName("m", IhsHelper.FrameType.STATISTICS));
        parserMap.put(DLSJposConst.DLS_S_LASER_TIME, ihsHelper.getFieldName("l", IhsHelper.FrameType.STATISTICS));
        parserMap.put(DLSJposConst.DLS_S_EAS_DEACTIVATED, ihsHelper.getFieldName("E", IhsHelper.FrameType.STATISTICS));
        parserMap.put(DLSJposConst.DLS_S_EAS_MANUAL, ihsHelper.getFieldName("e", IhsHelper.FrameType.STATISTICS));
        parserMap.put(DLSJposConst.DLS_S_SCANNER_CONFIG_FILE_ID, ihsHelper.getFieldName("C", IhsHelper.FrameType.INFORMATION));
        parserMap.put(DLSJposConst.DLS_S_SCANNER_REVISION, ihsHelper.getFieldName("T", IhsHelper.FrameType.INFORMATION));
        parserMap.put(DLSJposConst.DLS_S_FPGA_VERSION, ihsHelper.getFieldName("F", IhsHelper.FrameType.INFORMATION));
        parserMap.put(DLSJposConst.DLS_S_IPE_APP_VERSION, ihsHelper.getFieldName("P", IhsHelper.FrameType.INFORMATION));
        parserMap.put(DLSJposConst.DLS_S_USB_LOADER_VERSION, ihsHelper.getFieldName("l", IhsHelper.FrameType.INFORMATION));
        parserMap.put(DLSJposConst.DLS_S_SDRAM_CFG_VERSION, ihsHelper.getFieldName("Q", IhsHelper.FrameType.INFORMATION));
        parserMap.put(DLSJposConst.DLS_S_VL_VERSION, ihsHelper.getFieldName("V", IhsHelper.FrameType.INFORMATION));
        parserMap.put(DLSJposConst.DLS_S_VSL_VERSION, ihsHelper.getFieldName("v", IhsHelper.FrameType.INFORMATION));
        parserMap.put(DLSJposConst.DLS_S_NUM_ATTACHED_DEVICES, ihsHelper.getFieldName("h", IhsHelper.FrameType.INFORMATION));
        parserMap.put(DLSJposConst.DLS_S_USB_SERIAL_CONNECTED, ihsHelper.getFieldName("D", IhsHelper.FrameType.HEALTH));
        parserMap.put(DLSJposConst.DLS_S_IPE_0_HEALTH, ihsHelper.getFieldName("0", IhsHelper.FrameType.HEALTH));
        parserMap.put(DLSJposConst.DLS_S_IPE_1_HEALTH, ihsHelper.getFieldName("1", IhsHelper.FrameType.HEALTH));
        parserMap.put(DLSJposConst.DLS_S_IPE_2_HEALTH, ihsHelper.getFieldName("2", IhsHelper.FrameType.HEALTH));
        parserMap.put(DLSJposConst.DLS_S_IPE_3_HEALTH, ihsHelper.getFieldName("3", IhsHelper.FrameType.HEALTH));
        parserMap.put(DLSJposConst.DLS_S_SCALE_SENTRY_HEALTH, ihsHelper.getFieldName("S", IhsHelper.FrameType.HEALTH));
//        parserMap.put(DLSJposConst.DLS_S_GOOD_WEIGHT_READ_COUNT, ihsHelper.getFieldName("S", IhsHelper.FrameType.INFORMATION));
        parserMap.put(DLSJposConst.DLS_S_TOTAL_RESETS, ihsHelper.getFieldName("R", IhsHelper.FrameType.STATISTICS));
        parserMap.put(DLSJposConst.DLS_S_ERROR_RESETS, ihsHelper.getFieldName("r", IhsHelper.FrameType.STATISTICS));
        parserMap.put(DLSJposConst.DLS_S_VERT_IPE_FORCED_RESETS, ihsHelper.getFieldName("V", IhsHelper.FrameType.STATISTICS));
        parserMap.put(DLSJposConst.DLS_S_HORZ_IPE_FORCED_RESETS, ihsHelper.getFieldName("H", IhsHelper.FrameType.STATISTICS));
        parserMap.put(DLSJposConst.DLS_S_2D_FORCED_RESETS, ihsHelper.getFieldName("D", IhsHelper.FrameType.STATISTICS));
        parserMap.put(DLSJposConst.DLS_S_TDR_FORCED_RESETS, ihsHelper.getFieldName("Q", IhsHelper.FrameType.STATISTICS));
        parserMap.put(DLSJposConst.DLS_S_POS_ZERO_REQUESTS, ihsHelper.getFieldName("Z", IhsHelper.FrameType.STATISTICS));
        parserMap.put(DLSJposConst.DLS_S_ENFORCE_ZERO_EVENTS, ihsHelper.getFieldName("X", IhsHelper.FrameType.STATISTICS));
        parserMap.put(DLSJposConst.DLS_S_SCALE_SENTRY_EVENTS, ihsHelper.getFieldName("S", IhsHelper.FrameType.STATISTICS));
        parserMap.put(DLSJposConst.DLS_S_EAS_RT_FAULTS, ihsHelper.getFieldName("Y", IhsHelper.FrameType.STATISTICS));
    }

    /**
     * Creates an XML formatted String and writes it to the log file.
     * <p>
     * The XML formatted string will be returned via reference through the
     * {@code statisticsBuffer} parameter.
     *
     * @param statistics       {@code HashMap<String, Object>} containing statistics
     *                         key-value pairs to log
     * @param statisticsBuffer String array containing a retrieval flag at index
     *                         {@code 0}. {@code "U_"} for UPOS, {@code "M_"} for Manufacturer, and
     *                         {@code ""} for All. Formatted String will also be returned through this
     *                         array at index {@code 0}
     */
    public void buildXMLString(Map<String, Object> statistics, String[] statisticsBuffer) {
        boolean bUPOSRetrieve = false;
        boolean bManufacturerRetrieve = false;
        boolean bRetrieveAll = false;
        boolean bStatsSpecificRetrieve = false;
        String deviceCategory = (String) statistics.get(DLSJposConst.DLS_S_DEVICE_CATEGORY);
        StringBuilder sb = new StringBuilder();
        String[] statList = new String[]{};

        switch (statisticsBuffer[0]) {
            case "U_":
                // set the UPOS retrieve flag
                bUPOSRetrieve = true;
                break;
            case "M_":
                // set the MANUFACTURER retrieve flag
                bManufacturerRetrieve = true;
                break;
            case "":
                // set the RETRIEVE_ALL flag
                bRetrieveAll = true;
                break;
            default:
                statList = statisticsBuffer[0].split(",");
                bStatsSpecificRetrieve = true;
                break;
        }

        //The following are filled in with bogus data so that there is
        //something in the WMI & MBeans databases
        Date now = new Date();
        SimpleDateFormat format = new SimpleDateFormat("MMM-dd-yyyy");//"MM/dd/yyyy"); //"HHmmssMMddyyyy");
        String date_out = format.format(now);

        statistics.put(DLSJposConst.DLS_S_INSTALL_DATE, date_out);
        statistics.put(DLSJposConst.DLS_S_MANUFACTURE_DATE, date_out);

        // Create the xml data to be returned to the caller.
        addLine(sb, "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
        addLine(sb, "<UPOSStat version=\"" + statistics.get(DLSJposConst.DLS_S_UPOS_VERSION) + "\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"http://www.nrf-arts.org/UnifiedPOS/namespace/\" xsi:schemaLocation=\"http://www.nrf-arts.org/UnifiedPOS/namespace/ UPOSStat.xsd\">");
        addLine(sb, "  <Event>");

        //Add all UPOS specific parameters
        if (bUPOSRetrieve || bRetrieveAll) {
            addLine(sb, "    <Parameter>");
            if (deviceCategory.equals("Scanner")) {           // If this is a scanner device
                //addLine(sb, "      <Name>" + DLSJposConst.DLS_S_GOOD_SCAN_COUNT + "</Name>");
                //addLine(sb, "      <Value>" + _getVal(statistics, DLSJposConst.DLS_S_GOOD_SCAN_COUNT) + "</Value>");
            } else {                                        // Else it must be a scale device
                addLine(sb, "      <Name>" + DLSJposConst.DLS_S_GOOD_WEIGHT_READ_COUNT + "</Name>");
                addLine(sb, "      <Value>" + getVal(statistics, DLSJposConst.DLS_S_GOOD_WEIGHT_READ_COUNT) + "</Value>");
            }
            addLine(sb, "    </Parameter>");

            for (String x : XML_UPOS) {
                addLine(sb, "    <Parameter>");
                addLine(sb, "      <Name>" + x + "</Name>");
                addLine(sb, "      <Value>" + getVal(statistics, x) + "</Value>");
                addLine(sb, "    </Parameter>");
            }

        }

        //Add all manufacturer stats
        if (bManufacturerRetrieve || bRetrieveAll) {
            for (String k : statistics.keySet()) {
                if (XML_UPOS.contains(k) || XML_EQUIPMENT.contains(k) || k.contains("Raw")) {
                    continue;
                }
                addLine(sb, "    <ManufacturerSpecific>");      // Create fields for xml file
                addLine(sb, "      <Name>" + k + "</Name>");    //  Name of field
                addLine(sb, "      <Value>" + getVal(statistics, k) + "</Value>");  //  Value of field
                addLine(sb, "    </ManufacturerSpecific>");     // Terminator.
            }
        }

        //Add specific stats denoted by user to retrieve
        if (bStatsSpecificRetrieve) {
            for (String s : statList) {
                if (XML_UPOS.contains(s) || XML_EQUIPMENT.contains(s)) {
                    continue;
                }
                addLine(sb, "    <ManufacturerSpecific>");      // Create fields for xml file
                addLine(sb, "      <Name>" + s + "</Name>");    //  Name of field
                addLine(sb, "      <Value>" + getVal(statistics, s) + "</Value>");  //  Value of field
                addLine(sb, "    </ManufacturerSpecific>");     // Terminator.
            }
        }

        //Add equipement section that UPOS guarantees to always be present
        addLine(sb, "  </Event>");
        addLine(sb, "  <Equipment>");

        String equipVal;
        for (String s : XML_EQUIPMENT) {
            switch (s) {
                case "ModelName":
                    equipVal = getVal(statistics, ihsHelper.getFieldName("M", IhsHelper.FrameType.INFORMATION));
                    break;
                case "FirmwareRevision":
                    equipVal = getVal(statistics, ihsHelper.getFieldName("A", IhsHelper.FrameType.INFORMATION));
                    break;
                case "ConfigRevision":
                    equipVal = getVal(statistics, ihsHelper.getFieldName("C", IhsHelper.FrameType.INFORMATION));
                    break;
                case "InstallationDate":
                    equipVal = getVal(statistics, DLSJposConst.DLS_S_INSTALL_DATE);
                    break;
                default:
                    equipVal = getVal(statistics, s);

            }
            addLine(sb, "    <" + s + ">" + equipVal + "</" + s + ">");
        }

        addLine(sb, "  </Equipment>");
        addLine(sb, "</UPOSStat>");

        //return the xml String by reference to the user
        statisticsBuffer[0] = sb.toString();
    }

    /**
     * Returns the matching field name from the IHSParser.csv that corresponds
     * to the given constant. Returns the supplied {@code key} if not found in
     * the parser.
     *
     * @param key String containing the constant to retrieve the field name for
     * @return String containing the field name
     */
    public String getParserName(String key) {
        if (parserMap.isEmpty()) {
            buildParserMap();
        }
        if (parserMap.containsKey(key)) {
            return parserMap.get(key);
        }
        return key;
    }

    /**
     * Returns the value contained in the map
     *
     * @param hm  {@code HashMap<String, Object>} object containing map to search
     * @param key String containing the key lookup
     * @return String containing value found, or empty string if it doesn't
     * exist in the map
     */
    String getVal(Map<String, Object> hm, String key) {
        Object valObject = hm.get(key);
        if (valObject instanceof String) {
            return (String) valObject;
        } else if (valObject instanceof StringPair) {
            //need the key here instead of the val. The only case we will use this method for when encountering a StringPair is to
            //get name of the statistic, otherwise we can just get the value from the current stats (which won't have a StringPair)
            return ((StringPair) valObject).key;
        } else if (valObject == null) {
            return "";
        } else {
            return valObject.toString();
        }
    }

    /**
     * Parses the given frame character and data into the given Statistics map.
     *
     * @param Stats         {@code HashMap<String, Object>} containing parsed statistics
     * @param frameID       String containing the frame identification character
     * @param frameContents String containing the frame contents
     * @param type          FrameType used for parsing the
     *                      statistic into the Statistics Map
     */
    private void parse(Map<String, Object> Stats, String frameID, String frameContents, IhsHelper.FrameType type) {
        Pair<String, String> result = ihsHelper.parse(frameID, frameContents, type);
        if (result != null)
            Stats.put(result.first, result.second);
    }

    /**
     * Parses the given frame character and data returned from the Health
     * command into the given Statistics map.
     *
     * @param Stats         {@code HashMap<String, Object>} destination for parsed
     *                      statistics
     * @param frameID       String containing the frame identification character
     * @param frameContents String containing the frame contents
     */
    public void parseHealth(Map<String, Object> Stats, String frameID, String frameContents) {
        parse(Stats, frameID, frameContents, IhsHelper.FrameType.HEALTH);
    }

    /**
     * Parses the given frame character and data returned from the Info command
     * into the given Statistics map.
     *
     * @param Stats         {@code HashMap<String, Object>} destination for parsed
     *                      statistics
     * @param frameID       String containing the frame identification character
     * @param frameContents String containing the frame contents
     */
    public void parseInfo(Map<String, Object> Stats, String frameID, String frameContents) {
        parse(Stats, frameID, frameContents, IhsHelper.FrameType.INFORMATION);
    }

    /**
     * Parses the given frame character and data returned from the Statistics
     * command into the given Statistics map.
     *
     * @param Stats         {@code HashMap<String, Object>} destination for parsed
     *                      statistics
     * @param frameID       String containing the frame identification character
     * @param frameContents String containing the frame contents
     */
    public void parseStatistic(Map<String, Object> Stats, String frameID, String frameContents) {
        parse(Stats, frameID, frameContents, IhsHelper.FrameType.STATISTICS);
    }

    /**
     * Creates a Scanner Info file with the specified {@code HashMap}, returns
     * the text of the created file.
     *
     * @param scanHT           {@code HashMap<String, Object>} containing information for
     *                         the scanner
     * @param avalancheEnabled boolean indicating if Avalanche is enabled
     * @return String containing the text of the created file
     */
    public String scannerInfoFile(Map<String, Object> scanHT, boolean avalancheEnabled) {
        StringBuilder sb = new StringBuilder();

        addLine(sb, "# Raw data from scanner/scale");

        if (scanHT.containsKey("RawInfo")) {
            addLine(sb, "# Info command");
            addLine(sb, (String) scanHT.get("RawInfo"));
        }

        if (scanHT.containsKey("RawHealth")) {
            addLine(sb, "# Health command");
            addLine(sb, (String) scanHT.get("RawHealth"));
        }

        if (scanHT.containsKey("RawStats")) {
            addLine(sb, "# Stats command");
            addLine(sb, (String) scanHT.get("RawStats"));
        }

        addLine(sb, "#");
        addLine(sb, "# Parsed data");
        addLine(sb, "#");
        addLine(sb, "#");
        sb.append("vendor = ");
        if (scanHT.containsKey(getParserName(DLSJposConst.DLS_S_MODEL_NAME))) {
            String modelNumPieces = (String) scanHT.get(getParserName(DLSJposConst.DLS_S_MODEL_NAME));
            String[] begin = modelNumPieces.split("-");
            if (begin[0].length() > 0) {
                addLine(sb, begin[0]);
            } else {
                addLine(sb, (String) scanHT.get(DLSJposConst.DLS_S_MANUFACTURE_NAME));
            }
        } else {
            addLine(sb, (String) scanHT.get(DLSJposConst.DLS_S_MANUFACTURE_NAME));
        }

        addLine(sb, "# UPOS");
        sb.append(DLSJposConst.DLS_S_MANUFACTURE_NAME);
        sb.append(" = ");
        addLine(sb, (String) scanHT.get(DLSJposConst.DLS_S_MANUFACTURE_NAME));
        sb.append(DLSJposConst.DLS_S_DEVICE_CATEGORY);
        sb.append(" = ");
        addLine(sb, (String) scanHT.get(DLSJposConst.DLS_S_DEVICE_CATEGORY));

        //add Info data
        addLine(sb, "# Info");
        sb.append(stringifyMatches(scanHT, IhsHelper.FrameType.INFORMATION));

        //add Health data
        addLine(sb, "# Health");
        sb.append(stringifyMatches(scanHT, IhsHelper.FrameType.HEALTH));

        //add Statistics data
        addLine(sb, "# Stats");
        sb.append(stringifyMatches(scanHT, IhsHelper.FrameType.STATISTICS));

        addLine(sb, "#");
        addLine(sb, "# JavaPOS");

        if (scanHT.containsKey(DLSJposConst.DLS_S_UPOS_VERSION)) {
            sb.append("# ");
            sb.append(DLSJposConst.DLS_S_UPOS_VERSION);
            sb.append(" = ");
            addLine(sb, (String) scanHT.get(DLSJposConst.DLS_S_UPOS_VERSION));
        }

        if (scanHT.containsKey(DLSJposConst.DLS_S_CONTROL_VERSION)) {
            sb.append("# ");
            sb.append(DLSJposConst.DLS_S_CONTROL_VERSION);
            sb.append(" = ");
            addLine(sb, Integer.toString((int) scanHT.get(DLSJposConst.DLS_S_CONTROL_VERSION)));
        }

        if (scanHT.containsKey(DLSJposConst.DLS_S_SERVICE_VERSION)) {
            sb.append("# ");
            sb.append(DLSJposConst.DLS_S_SERVICE_VERSION);
            sb.append(" = ");
            addLine(sb, (String) scanHT.get(DLSJposConst.DLS_S_SERVICE_VERSION));
        }

        DateFormat fDateFormat = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT);
        Date now = new Date();
        // Format the time string.
        String date_out = fDateFormat.format(now);
        addLine(sb, "#");
        addLine(sb, "# Creation date: " + date_out);
        addLine(sb, "# Copyright 2012, " + Branding.getInstance().getBrandingName());
        addLine(sb, "# EOF");

        return sb.toString();
    }

    /**
     * Converts statistics to a single string for every match found between the
     * two maps.
     *
     * @param statsMap {@code HashMap<String, Object>} containing all statistics
     *                 returned by the device
     * @param type     The type of the statistics
     *                 to match and place in the String
     * @return String containing all matched statistics in the form of
     * {@code <key> = <value>} separated by newlines
     */
    String stringifyMatches(Map<String, Object> statsMap, IhsHelper.FrameType type) {
        StringBuilder sb = new StringBuilder();

        List<String> names = ihsHelper.getAllFieldNamesForType(type);

        for (String name : names) {
            if (statsMap.containsKey(name)) {
                sb.append(name);
                sb.append(" = ");
                sb.append(statsMap.get(name));
                sb.append(sep);
            }
        }

        return sb.toString();
    }

    private static class DLSStatisticsHolder {
        private static final DLSStatistics INSTANCE = new DLSStatistics();
    }

    static final class StringPair {

        String key;
        String val;

        public StringPair(String k, String v) {
            key = k;
            val = v;
        }
    }
}
