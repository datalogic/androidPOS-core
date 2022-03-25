package com.datalogic.dlapos.androidpos.common;

import android.content.Context;

import androidx.core.util.Pair;

import com.datalogic.dlapos.confighelper.configurations.accessor.IhsHelper;

import org.junit.Test;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DLSStatisticsTest {

    @Mock
    private final IhsHelper helper = mock(IhsHelper.class);
    private final List<String> infoNames = new ArrayList<>(Arrays.asList("ApplicationROM",
            "TopModelNumber",
            "Interface",
            "TDRType",
            "RFScannerRadioVersion",
            "ConfigurationFileID"
    ));

    @Mock
    private final Context context = mock(Context.class);

    /**
     * This is not a real test, but can be used to see the output of the function, and to check that it does not explode.
     */
    @Test
    public void buildXMLString() {
        Map<String, Object> stats = new HashMap<>();
        String[] buf = new String[1];
        buf[0] = "";
        stats.put(DLSJposConst.DLS_S_DEVICE_CATEGORY, "Scanner");
        stats.put(DLSJposConst.DLS_S_UPOS_VERSION, "1.14");
        stats.put(DLSJposConst.DLS_S_CAP_PWR_REPORT, true);
        stats.put(DLSJposConst.DLS_S_CAP_STATS_REPORT, true);
        stats.put(DLSJposConst.DLS_S_CAP_UPDATE_STATS, true);
        stats.put(DLSJposConst.DLS_S_CAP_COMP_FW_VER, true);
        stats.put(DLSJposConst.DLS_S_CAP_UPDATE_FW, true);
        stats.put(DLSJposConst.DLS_S_MANUFACTURE_NAME, "Datalogic");
        stats.put("TopModelNumber", "DLScanner");
        stats.put(DLSJposConst.DLS_S_SERIAL_NUMBER, "12345");
        stats.put("ApplicationROM", "12.345");
        stats.put("ConfigurationFileID", "12.345");
        stats.put(DLSJposConst.DLS_S_INTERFACE, "OEM");

        when(helper.getFieldName("M", IhsHelper.FrameType.INFORMATION)).thenReturn("TopModelNumber");
        when(helper.getFieldName("A", IhsHelper.FrameType.INFORMATION)).thenReturn("ApplicationROM");
        when(helper.getFieldName("C", IhsHelper.FrameType.INFORMATION)).thenReturn("ConfigurationFileID");

        DLSStatistics statistics = new DLSStatistics(helper);
        statistics.buildXMLString(stats, buf);
        assertThat(buf[0]).isNotNull();
        System.out.println(buf[0]);
    }

    /**
     * This is not a real test, but can be used to see the output of the function, and to check that it does not explode.
     */
    @Test
    public void scannerInfoFile() {
        IhsHelper helper = mock(IhsHelper.class);
        DLSStatistics statistics = new DLSStatistics(helper);
        Map<String, Object> data = new HashMap<>();
        data.put("RawInfo", "TestRawInfo");
        data.put("RawHealth", "TestRawHealth");
        data.put("RawStats", "TestRawStats");
        when(helper.getFieldName("M", IhsHelper.FrameType.INFORMATION)).thenReturn("Vendor");
        data.put("Vendor", "datalogic-test");
        data.put(DLSJposConst.DLS_S_MANUFACTURE_NAME, "Datalogic");
        data.put(DLSJposConst.DLS_S_DEVICE_CATEGORY, "Dummy test device");
        data.put(DLSJposConst.DLS_S_UPOS_VERSION, "1.Test");
        data.put(DLSJposConst.DLS_S_CONTROL_VERSION, 2);
        data.put(DLSJposConst.DLS_S_SERVICE_VERSION, "3.Test");

        String result = statistics.scannerInfoFile(data, false);
        System.out.println(result);
        assertThat(result.contains("vendor = datalogic" + System.lineSeparator())).isTrue();
        assertThat(result.contains(DLSJposConst.DLS_S_MANUFACTURE_NAME + " = Datalogic" + System.lineSeparator())).isTrue();
        assertThat(result.contains(DLSJposConst.DLS_S_DEVICE_CATEGORY + " = Dummy test device" + System.lineSeparator())).isTrue();
        assertThat(result.contains(DLSJposConst.DLS_S_UPOS_VERSION + " = 1.Test" + System.lineSeparator())).isTrue();
        assertThat(result.contains(DLSJposConst.DLS_S_CONTROL_VERSION + " = 2" + System.lineSeparator())).isTrue();
        assertThat(result.contains(DLSJposConst.DLS_S_SERVICE_VERSION + " = 3.Test" + System.lineSeparator())).isTrue();

        data.put("Vendor", "");
        result = statistics.scannerInfoFile(data, false);
        assertThat(result.contains("vendor = Datalogic" + System.lineSeparator())).isTrue();

        data.remove("Vendor");
        result = statistics.scannerInfoFile(data, false);
        assertThat(result.contains("vendor = Datalogic" + System.lineSeparator())).isTrue();
    }

    @Test
    public void parseHealth() {
        Map<String, Object> result = new HashMap<>();
        String tag1 = "Tag1";
        String content = "CONTENT1";
        Pair<String, String> result1 = Pair.create("Tag1", "CONTENT1");
        when(helper.parse(tag1, content, IhsHelper.FrameType.HEALTH)).thenReturn(result1);
        DLSStatistics statistics = new DLSStatistics(helper);
        statistics.parseHealth(result, "TEST", content);
        assertThat(result.isEmpty()).isTrue();
        statistics.parseHealth(result, tag1, content);
        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(tag1)).isEqualTo(content);
        statistics.parseHealth(result, "TEST", content);
        assertThat(result.size()).isEqualTo(1);
    }

    @Test
    public void parseInfo() {
        Map<String, Object> result = new HashMap<>();
        String tag1 = "Tag1";
        String content = "CONTENT1";
        Pair<String, String> result1 = Pair.create("Tag1", "CONTENT1");
        when(helper.parse(tag1, content, IhsHelper.FrameType.INFORMATION)).thenReturn(result1);
        DLSStatistics statistics = new DLSStatistics(helper);
        statistics.parseInfo(result, "TEST", content);
        assertThat(result.isEmpty()).isTrue();
        statistics.parseInfo(result, tag1, content);
        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(tag1)).isEqualTo(content);
        statistics.parseInfo(result, "TEST", content);
        assertThat(result.size()).isEqualTo(1);
    }

    @Test
    public void parseStatistic() {
        Map<String, Object> result = new HashMap<>();
        String tag1 = "Tag1";
        String content = "CONTENT1";
        Pair<String, String> result1 = Pair.create("Tag1", "CONTENT1");
        when(helper.parse(tag1, content, IhsHelper.FrameType.STATISTICS)).thenReturn(result1);
        DLSStatistics statistics = new DLSStatistics(helper);
        statistics.parseStatistic(result, "TEST", content);
        assertThat(result.isEmpty()).isTrue();
        statistics.parseStatistic(result, tag1, content);
        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(tag1)).isEqualTo(content);
        statistics.parseStatistic(result, "TEST", content);
        assertThat(result.size()).isEqualTo(1);
    }

    @Test
    public void stringifyMatchesInfo() {
        when(helper.getAllFieldNamesForType(IhsHelper.FrameType.INFORMATION)).thenReturn(infoNames);
        Map<String, Object> stats = new HashMap<>();
        stats.put("TEST", "TEST");
        stats.put("TopModelNumber", "ModelNumber");
        stats.put("TEST1", "TEST1");
        stats.put("Interface", "OEM");
        stats.put("TDRType", "type");
        stats.put("RFScannerRadioVersion", "1.2");
        stats.put("ConfigurationFileID", "das.txt");
        DLSStatistics statistics = new DLSStatistics(helper);
        String result = statistics.stringifyMatches(stats, IhsHelper.FrameType.INFORMATION);
        assertThat(result).isNotNull();
        String[] lines = result.split("" + System.getProperty("line.separator"));
        assertThat(lines.length).isEqualTo(5);
        assertThat(lines[0].split("=")[0].trim()).isEqualTo("TopModelNumber");
        assertThat(lines[0].split("=")[1].trim()).isEqualTo("ModelNumber");
        assertThat(lines[1].split("=")[0].trim()).isEqualTo("Interface");
        assertThat(lines[1].split("=")[1].trim()).isEqualTo("OEM");
        assertThat(lines[2].split("=")[0].trim()).isEqualTo("TDRType");
        assertThat(lines[2].split("=")[1].trim()).isEqualTo("type");
        assertThat(lines[3].split("=")[0].trim()).isEqualTo("RFScannerRadioVersion");
        assertThat(lines[3].split("=")[1].trim()).isEqualTo("1.2");
        assertThat(lines[4].split("=")[0].trim()).isEqualTo("ConfigurationFileID");
        assertThat(lines[4].split("=")[1].trim()).isEqualTo("das.txt");

    }

    @Test
    public void getParserNameTest() {
        initTestISHHelper();

        DLSStatistics statistics = new DLSStatistics(helper);
        assertThat(statistics.getParserName(DLSJposConst.DLS_S_SERIAL_NUMBER)).isEqualTo("S_VALUE");
        assertThat(statistics.getParserName(DLSJposConst.DLS_S_MODEL_NAME)).isEqualTo("M_VALUE");
        assertThat(statistics.getParserName(DLSJposConst.DLS_S_SCANNER_BOARD_SERIAL)).isEqualTo("m_VALUE");
        assertThat(statistics.getParserName(DLSJposConst.DLS_S_FIRMWARE_VERSION)).isEqualTo("A_VALUE");
        assertThat(statistics.getParserName(DLSJposConst.DLS_S_INTERFACE)).isEqualTo("I_VALUE");
        assertThat(statistics.getParserName(DLSJposConst.DLS_S_HOURS_POWERED_COUNT)).isEqualTo("P_VALUE");
        assertThat(statistics.getParserName(DLSJposConst.DLS_S_H_LASER_HEALTH)).isEqualTo("h_VALUE");

    }

    @Test
    public void getParserNameTestInvalid() {
        DLSStatistics statistics = new DLSStatistics(helper);
        assertThat(statistics.getParserName("TEST")).isEqualTo("TEST");
    }

    @Test
    public void singleton() {
        assertThat(DLSStatistics.getInstance(context)).isNotNull();
    }

    @Test
    public void getVal() {

        Map<String, Object> startingMap = new HashMap<>();
        startingMap.put("StringTest", "StringValue");
        startingMap.put("StringPairTest", new DLSStatistics.StringPair("StringKey", "StringValuePair"));
        startingMap.put("NullTest", null);

        DLSStatistics statistics = new DLSStatistics(helper);
        assertThat(statistics.getVal(startingMap, "StringTest")).isEqualTo("StringValue");
        assertThat(statistics.getVal(startingMap, "StringPairTest")).isEqualTo("StringKey");
        assertThat(statistics.getVal(startingMap, "NullTest")).isEqualTo("");
    }

    private void initTestISHHelper() {
        when(helper.getFieldName("S", IhsHelper.FrameType.INFORMATION)).thenReturn("S_VALUE");
        when(helper.getFieldName("M", IhsHelper.FrameType.INFORMATION)).thenReturn("M_VALUE");
        when(helper.getFieldName("m", IhsHelper.FrameType.INFORMATION)).thenReturn("m_VALUE");
        when(helper.getFieldName("A", IhsHelper.FrameType.INFORMATION)).thenReturn("A_VALUE");
        when(helper.getFieldName("I", IhsHelper.FrameType.INFORMATION)).thenReturn("I_VALUE");
        when(helper.getFieldName("P", IhsHelper.FrameType.STATISTICS)).thenReturn("P_VALUE");
        when(helper.getFieldName("h", IhsHelper.FrameType.HEALTH)).thenReturn("h_VALUE");
    }
}