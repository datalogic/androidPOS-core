package com.datalogic.dlapos.androidpos.interpretation;

import android.content.Context;

import com.datalogic.dlapos.androidpos.common.DLSException;
import com.datalogic.dlapos.androidpos.common.DLSJposConst;
import com.datalogic.dlapos.androidpos.common.DLSProperties;
import com.datalogic.dlapos.androidpos.transport.DLSPort;
import com.datalogic.dlapos.androidpos.utils.MockUtils;
import com.datalogic.dlapos.commons.constant.ScannerConstants;
import com.datalogic.dlapos.confighelper.DLAPosConfigHelper;
import com.datalogic.dlapos.confighelper.configurations.accessor.LabelHelper;
import com.datalogic.dlapos.confighelper.configurations.accessor.LabelIds;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DLS9xxxScannerTest {


    @Mock
    DLSProperties _properties = mock(DLSProperties.class);

    @Mock
    DLAPosConfigHelper _dlaPosConfigHelper = mock(DLAPosConfigHelper.class);

    @Mock
    private final Context _context = mock(Context.class);

    @Mock
    private final LabelHelper _helper = mock(LabelHelper.class);

    @Mock
    private final DLSPort _port = mock(DLSPort.class);

    @Before
    public void setup() {
        MockUtils.mockConfigHelperSingleton(_dlaPosConfigHelper, null);
        MockUtils.mockDLSPropertiesSingleton(_properties);
        setUpLabels();
    }

    @After
    public void cleanup() {
        DLSJposConst.updateInProgress = false;
        MockUtils.cleanConfigHelper();
        MockUtils.cleanDLSProperties();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void changeBaudRate() {
        DLS9xxxScanner scanner = new DLS9xxxScanner();
        scanner.changeBaudRate(300);
    }

    @Test
    public void acceptHostCommands() {
        Dummy9xxxExtension scanner = new Dummy9xxxExtension();
        scanner.port = _port;
        scanner.acceptHostCommands();
        verify(_port, times(1)).sendData(Dummy9xxxExtension.ACCEPT_HOST_CMD);
    }

    @Test
    public void rejectHostCommands() {
        Dummy9xxxExtension scanner = new Dummy9xxxExtension();
        scanner.port = _port;
        scanner.rejectHostCommands();
        verify(_port, times(1)).sendData(Dummy9xxxExtension.REJECT_HOST_CMD);
    }

    //region directIO
    @Test
    public void directIOWhileUpdate() throws DLSException {
        DLSJposConst.updateInProgress = true;
        DLS9xxxScanner scanner = new DLS9xxxScanner();
        scanner.port = _port;
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        scanner.directIO(2, new int[]{1}, stream);
        verify(_port, times(0)).sendData((byte[]) any());
        verify(_port, times(0)).sendData((String) any());
        assertThat(stream.toString()).isEqualTo("");
    }

    @Test
    public void directIODISPLAY_SINGLE_LINE() throws DLSException, IOException {
        DLS9xxxScanner scanner = new DLS9xxxScanner();
        scanner.port = _port;
        String line = "TEST";
        ByteArrayOutputStream obj = new ByteArrayOutputStream();
        obj.write(line.getBytes());
        scanner.directIO(DLSJposConst.DIO_DISPLAY_SINGLE_LINE, new int[]{1}, obj);
        verify(_port, times(1)).sendData("@" + line + "\r");
        assertThat(obj.toString()).isEqualTo("Single Line");
    }

    @Test
    public void directIOTWO_WAY_HOST_RESPONSE() throws DLSException, IOException {
        DLS9xxxScanner scanner = new DLS9xxxScanner();
        scanner.port = _port;
        String line = "TEST";
        ByteArrayOutputStream obj = new ByteArrayOutputStream();
        obj.write(line.getBytes());
        scanner.directIO(DLSJposConst.DIO_TWO_WAY_HOST_RESPONSE, new int[]{1}, obj);
        verify(_port, times(1)).sendData("@" + line + "\r");
        assertThat(obj.toString()).isEqualTo("Response Sent");
    }

    @Test
    public void directIORETURN_DATA_TYPE() throws DLSException, IOException {
        DLS9xxxScanner scanner = new DLS9xxxScanner();
        scanner.port = _port;
        scanner.returnType = 5;
        ByteArrayOutputStream obj = new ByteArrayOutputStream();
        int[] data = new int[]{1};
        scanner.directIO(DLSJposConst.DIO_RETURN_DATA_TYPE, data, obj);
        assertThat(obj.toString()).isEqualTo("5");
        assertThat(data.length).isEqualTo(1);
        assertThat(data[0]).isEqualTo(5);
    }

    @Test
    public void directIORETURN_QUANTITY() throws DLSException {
        DLS9xxxScanner scanner = new DLS9xxxScanner();
        scanner.port = _port;
        scanner.returnQty = 3;
        ByteArrayOutputStream obj = new ByteArrayOutputStream();
        int[] data = new int[]{1};
        scanner.directIO(DLSJposConst.DIO_RETURN_QUANTITY, data, obj);
        assertThat(obj.toString()).isEqualTo("3");
        assertThat(data.length).isEqualTo(1);
        assertThat(data[0]).isEqualTo(3);
    }

    @Test
    public void directIODISPLAY_DATA() throws IOException, DLSException {
        DLS9xxxScanner scanner = new DLS9xxxScanner();
        scanner.port = _port;
        String line = "TEST";
        ByteArrayOutputStream obj = new ByteArrayOutputStream();
        obj.write(line.getBytes());
        int[] data = new int[]{1};
        scanner.directIO(DLSJposConst.DIO_DISPLAY_DATA, data, obj);
        verify(_port, times(1)).sendData(line.getBytes());
        assertThat(obj.toString()).isEqualTo("Display Data");
        assertThat(data.length).isEqualTo(1);
        assertThat(data[0]).isEqualTo(0);
    }

    @Test
    public void directIORETURN_DATA() throws DLSException, IOException {
        DLS9xxxScanner scanner = new DLS9xxxScanner();
        scanner.port = _port;
        scanner.returnType = 7;
        ByteArrayOutputStream obj = new ByteArrayOutputStream();
        int[] data = new int[]{1};
        scanner.directIO(DLSJposConst.DIO_RETURN_DATA, data, obj);
        assertThat(obj.toString()).isEqualTo("Return Data");
        assertThat(data.length).isEqualTo(1);
        assertThat(data[0]).isEqualTo(7);
    }

    @Test
    public void directIORETURN_BATTERY_DATA_NOT_AVAILABLE() throws DLSException {
        DLS9xxxScanner scanner = new DLS9xxxScanner();
        scanner.statistics = new HashMap<>();
        ByteArrayOutputStream obj = new ByteArrayOutputStream();
        int[] data = new int[]{4};
        scanner.directIO(DLSJposConst.DIO_RETURN_BATTERY_DATA, data, obj);
        assertThat(data.length).isEqualTo(1);
        assertThat(data[0]).isEqualTo(1);
        assertThat(obj.toString()).isEqualTo("Battery Data Not Available");
    }

    @Test
    public void directIORETURN_BATTERY_DATA_AVAILABLE() throws DLSException {
        DLS9xxxScanner scanner = new DLS9xxxScanner();
        scanner.statistics = new HashMap<>();
        scanner.statistics.put(DLSJposConst.DLS_SECONDARY_BATTERY_CHG_CYCLES, "TEST");
        ByteArrayOutputStream obj = new ByteArrayOutputStream();
        int[] data = new int[]{4};
        scanner.directIO(DLSJposConst.DIO_RETURN_BATTERY_DATA, data, obj);
        assertThat(data.length).isEqualTo(1);
        assertThat(data[0]).isEqualTo(0);
        assertThat(obj.toString()).isEqualTo("Battery Charge Cycles = " + scanner.statistics.get(DLSJposConst.DLS_SECONDARY_BATTERY_CHG_CYCLES));
    }

    @Test
    public void directIOCHECK_CRADLE_STATUS_NOT_RECOGNIZED() throws IOException, DLSException {
        DLS9xxxScanner scanner = new DLS9xxxScanner();
        scanner.port = new DummyPort(scanner, "TEST");
        String line = "TEST";
        ByteArrayOutputStream obj = new ByteArrayOutputStream();
        obj.write(line.getBytes());
        int[] data = new int[]{1};
        scanner.directIO(DLSJposConst.DIO_CHECK_CRADLE_STATUS, data, obj);
        assertThat(obj.toString()).isEqualTo("Cradle response not recognized - TEST");
        assertThat(data.length).isEqualTo(1);
        assertThat(data[0]).isEqualTo(1);
    }

    @Test
    public void directIOCHECK_CRADLE_STATUS_RECOGNIZED() throws IOException, DLSException {
        DLS9xxxScanner scanner = new DLS9xxxScanner();
        scanner.port = new DummyPort(scanner, "Scanner9");
        String line = "TEST";
        ByteArrayOutputStream obj = new ByteArrayOutputStream();
        obj.write(line.getBytes());
        int[] data = new int[]{1};
        scanner.directIO(DLSJposConst.DIO_CHECK_CRADLE_STATUS, data, obj);
        assertThat(obj.toString()).isEqualTo("Scanner9");
        assertThat(data.length).isEqualTo(1);
        assertThat(data[0]).isEqualTo(0);
    }

    @Test
    public void directIOCHECK_CRADLE_STATUS_EMPTY() throws IOException, DLSException {
        DLS9xxxScanner scanner = new DLS9xxxScanner();
        scanner.port = new DummyPort(scanner, "");
        String line = "TEST";
        ByteArrayOutputStream obj = new ByteArrayOutputStream();
        obj.write(line.getBytes());
        int[] data = new int[]{5};
        scanner.directIO(DLSJposConst.DIO_CHECK_CRADLE_STATUS, data, obj);
        assertThat(obj.toString()).isEqualTo("Failed to retrieve cradle status");
        assertThat(data.length).isEqualTo(1);
        assertThat(data[0]).isEqualTo(1);
    }

    //endregion

    //region onDataReceived
    @Test
    public void onDataReceivedFirmware() {
        DLS9xxxScanner scanner = new DLS9xxxScanner();
        scanner.bFirmware = true;
        byte[] data = new byte[]{1};
        scanner.onDataReceived(data, 1);
        assertThat(scanner.bFirmware).isFalse();
        assertThat(scanner.response).isEqualTo(1);
    }

    @Test
    public void onDataReceivedStatMessageSent() {
        DLS9xxxScanner scanner = new DLS9xxxScanner();
        scanner.bStatMessageSent = true;
        byte[] data = new byte[]{1, 2};
        scanner.onDataReceived(data, 2);
        assertThat(scanner.bStatMessageSent).isFalse();
        assertThat(scanner.sohBuf.length).isEqualTo(2);
        assertThat(scanner.sohBuf[0]).isEqualTo(1);
        assertThat(scanner.sohBuf[1]).isEqualTo(2);
    }

    @Test
    public void onDataReceivedHOST_CMD_RESPONSE() {
        DLS9xxxScanner scanner = new DLS9xxxScanner();
        byte[] data = DLS9xxxScanner.HOST_CMD_RESPONSE.getBytes();
        scanner.onDataReceived(data, data.length);
    }

    @Test
    public void onDataReceivedRESET_CMD_RESPONSE() {
        DLS9xxxScanner scanner = new DLS9xxxScanner();
        byte[] data = DLS9xxxScanner.RESET_CMD_RESPONSE.getBytes();
        scanner.onDataReceived(data, data.length);
    }

    @Test
    public void onDataReceivedLessThan2BytesData() {
        DLS9xxxScanner scanner = new DLS9xxxScanner();
        byte[] data = new byte[]{1, 2};
        scanner.messageList.add("Srta");
        scanner.onDataReceived(data, data.length);
        assertThat(scanner.messageList.isEmpty()).isTrue();
    }

    //region Label
    @Test
    public void onDataReceivedLabelCode39() {
        //region setup
        DLS9xxxScanner scanner = new DLS9xxxScanner();
        scanner.context = _context;
        when(_properties.isSendCookedData()).thenReturn(false);
        when(_dlaPosConfigHelper.getLabelHelper()).thenReturn(_helper);
        //endregion

        //region test without quantity
        byte[] data = new byte[]{0, 10, 11, 48, 48, 48, 48, 52, 57, 51, 55, 37, 86};
        scanner.addLabelReceivedListener((rawData, decodedData, type) -> {
            assertThat(decodedData.length).isEqualTo(8);
            assertThat(new String(decodedData)).isEqualTo("00004937");
            assertThat(type).isEqualTo(108);
        });
        scanner.onDataReceived(data, data.length);
        assertThat(scanner.returnQty).isEqualTo(1);
        assertThat(scanner.returnType).isEqualTo(108);
        //endregion

        //region test with quantity
        data = new byte[]{0, 10, 11, 48, 48, 48, 48, 52, 57, 51, 55, 37, 80, 50, 37, 81, 49, 46, 50, 37, 86};
        scanner.onDataReceived(data, data.length);
        assertThat(scanner.returnQty).isEqualTo(2);
        assertThat(scanner.returnType).isEqualTo(108);
        //endregion
    }

    @Test
    public void onDataReceivedLabelEAN13() {
        //region setup
        DLS9xxxScanner scanner = new DLS9xxxScanner();
        scanner.context = _context;
        when(_properties.isSendCookedData()).thenReturn(false);
        when(_dlaPosConfigHelper.getLabelHelper()).thenReturn(_helper);
        //endregion

        //region test without quantity
        byte[] data = new byte[]{22, 57, 55, 56, 56, 56, 55, 49, 57, 50, 52, 48, 52, 53, 37, 86};
        scanner.addLabelReceivedListener((rawData, decodedData, type) -> {
            assertThat(decodedData.length).isEqualTo(13);
            assertThat(new String(decodedData)).isEqualTo("9788871924045");
            assertThat(type).isEqualTo(104);
        });
        scanner.onDataReceived(data, data.length);
        assertThat(scanner.returnQty).isEqualTo(1);
        assertThat(scanner.returnType).isEqualTo(104);
        //endregion

        //region test with quantity
        data = new byte[]{22, 57, 55, 56, 56, 56, 55, 49, 57, 50, 52, 48, 52, 53, 37, 80, 50, 37, 81, 49, 46, 50, 37, 86};
        scanner.onDataReceived(data, data.length);
        assertThat(scanner.returnQty).isEqualTo(2);
        assertThat(scanner.returnType).isEqualTo(104);
        //endregion
    }

    @Test
    public void onDataReceivedLabelCode128() {
        //region setup
        DLS9xxxScanner scanner = new DLS9xxxScanner();
        scanner.context = _context;
        when(_properties.isSendCookedData()).thenReturn(false);
        when(_dlaPosConfigHelper.getLabelHelper()).thenReturn(_helper);
        //endregion

        //region test without quantity
        byte[] data = new byte[]{0, 24, 11, 56, 50, 48, 49, 49, 54, 56, 48, 49, 37, 86};
        scanner.addLabelReceivedListener((rawData, decodedData, type) -> {
            assertThat(decodedData.length).isEqualTo(9);
            assertThat(new String(decodedData)).isEqualTo("820116801");
            assertThat(type).isEqualTo(110);
        });
        scanner.onDataReceived(data, data.length);
        assertThat(scanner.returnQty).isEqualTo(1);
        assertThat(scanner.returnType).isEqualTo(110);
        //endregion

        //region test with quantity
        data = new byte[]{0, 24, 11, 56, 50, 48, 49, 49, 54, 56, 48, 49, 37, 80, 50, 37, 81, 49, 46, 50, 37, 86};
        scanner.onDataReceived(data, data.length);
        assertThat(scanner.returnQty).isEqualTo(2);
        assertThat(scanner.returnType).isEqualTo(110);
        //endregion
    }

    //endregion

    //region Keyboard
    @Test
    public void onDataReceivedKeyboard() {
        //region setup
        DLS9xxxScanner scanner = new DLS9xxxScanner();
        scanner.context = _context;
        when(_properties.isSendCookedData()).thenReturn(false);
        when(_dlaPosConfigHelper.getLabelHelper()).thenReturn(_helper);
        //endregion

        //region test without quantity
        final byte[] expected = new byte[]{0, 10, 11, 48, 48, 48, 48, 52, 57, 51, 55};
        byte[] data = new byte[]{0, 10, 11, 48, 48, 48, 48, 52, 57, 51, 55, 37, 75};
        scanner.addLabelReceivedListener((rawData, decodedData, type) -> {
            assertThat(decodedData).isEqualTo(expected);
            assertThat(type).isEqualTo(2);
        });
        scanner.onDataReceived(data, data.length);
        assertThat(scanner.returnQty).isEqualTo(1);
        assertThat(scanner.returnType).isEqualTo(2);
        //endregion

        //region test with quantity
        data = new byte[]{0, 10, 11, 48, 48, 48, 48, 52, 57, 51, 55, 37, 80, 50, 37, 81, 49, 46, 50, 37, 75};
        scanner.onDataReceived(data, data.length);
        assertThat(scanner.returnQty).isEqualTo(2);
        assertThat(scanner.returnType).isEqualTo(2);
        //endregion
    }

    //endregion

    //region Escape
    @Test
    public void onDataReceivedEscape() {
        //region setup
        DLS9xxxScanner scanner = new DLS9xxxScanner();
        scanner.context = _context;
        when(_properties.isSendCookedData()).thenReturn(false);
        when(_dlaPosConfigHelper.getLabelHelper()).thenReturn(_helper);
        //endregion

        //region test without quantity
        byte[] data = new byte[]{0, 10, 11, 48, 48, 48, 48, 52, 57, 51, 55, 37, 69};
        scanner.addLabelReceivedListener((rawData, decodedData, type) -> {
            assertThat(new String(decodedData)).isEqualTo("ESC");
            assertThat(type).isEqualTo(3);
        });
        scanner.onDataReceived(data, data.length);
        assertThat(scanner.returnQty).isEqualTo(1);
        assertThat(scanner.returnType).isEqualTo(3);
        //endregion

        //region test with quantity
        data = new byte[]{0, 10, 11, 48, 48, 48, 48, 52, 57, 51, 55, 37, 80, 50, 37, 81, 49, 46, 50, 37, 69};
        scanner.onDataReceived(data, data.length);
        assertThat(scanner.returnQty).isEqualTo(2);
        assertThat(scanner.returnType).isEqualTo(3);
        //endregion
    }
    //endregion

    //region Inquiry
    @Test
    public void onDataReceivedInquiry() {
        //region setup
        DLS9xxxScanner scanner = new DLS9xxxScanner();
        scanner.context = _context;
        when(_properties.isSendCookedData()).thenReturn(false);
        when(_dlaPosConfigHelper.getLabelHelper()).thenReturn(_helper);
        //endregion

        //region test without quantity
        byte[] data = new byte[]{0, 10, 11, 48, 48, 48, 48, 52, 57, 51, 55, 37, 73};
        scanner.addLabelReceivedListener((rawData, decodedData, type) -> {
            assertThat(new String(decodedData)).isEqualTo("INQ");
            assertThat(type).isEqualTo(4);
        });
        scanner.onDataReceived(data, data.length);
        assertThat(scanner.returnQty).isEqualTo(1);
        assertThat(scanner.returnType).isEqualTo(4);
        //endregion

        //region test with quantity
        data = new byte[]{0, 10, 11, 48, 48, 48, 48, 52, 57, 51, 55, 37, 80, 50, 37, 81, 49, 46, 50, 37, 73};
        scanner.onDataReceived(data, data.length);
        assertThat(scanner.returnQty).isEqualTo(2);
        assertThat(scanner.returnType).isEqualTo(4);
        //endregion
    }
    //endregion

    //region Enter
    @Test
    public void onDataReceivedEnter() {
        //region setup
        DLS9xxxScanner scanner = new DLS9xxxScanner();
        scanner.context = _context;
        when(_properties.isSendCookedData()).thenReturn(false);
        when(_dlaPosConfigHelper.getLabelHelper()).thenReturn(_helper);
        //endregion

        //region test without quantity
        final byte[] expected = new byte[]{0, 10, 11, 48, 48, 48, 48, 52, 57, 51, 55};
        byte[] data = new byte[]{0, 10, 11, 48, 48, 48, 48, 52, 57, 51, 55, 37, 79};
        scanner.addLabelReceivedListener((rawData, decodedData, type) -> {
            assertThat(decodedData).isEqualTo(expected);
            assertThat(type).isEqualTo(5);
        });
        scanner.onDataReceived(data, data.length);
        assertThat(scanner.returnQty).isEqualTo(1);
        assertThat(scanner.returnType).isEqualTo(5);
        //endregion

        //region test with quantity
        data = new byte[]{0, 10, 11, 48, 48, 48, 48, 52, 57, 51, 55, 37, 80, 50, 37, 81, 49, 46, 50, 37, 79};
        scanner.onDataReceived(data, data.length);
        assertThat(scanner.returnQty).isEqualTo(2);
        assertThat(scanner.returnType).isEqualTo(5);
        //endregion
    }
    //endregion
    //endregion

    //region reset
    @Test
    public void reset() throws DLSException {
        DLS9xxxScanner scanner = new DLS9xxxScanner();
        scanner.port = _port;
        scanner.reset();
        verify(_port, times(1)).sendData(DLS9xxxScanner.RESET_CMD);
    }

    @Test
    public void resetWhileUpdate() throws DLSException {
        DLS9xxxScanner scanner = new DLS9xxxScanner();
        scanner.port = _port;
        DLSJposConst.updateInProgress = true;
        scanner.reset();
        verify(_port, times(0)).sendData(DLS9xxxScanner.RESET_CMD);
    }
    //endregion

    //region sendMessage
    @Test
    public void sendMessageAcceptHostCommands() {
        Dummy9xxxExtension scanner = new Dummy9xxxExtension();
        scanner.port = _port;
        assertThat(scanner.sendMessage("acceptHostCmd")).isEqualTo(1);
        verify(_port, times(1)).sendData(DLS9xxxScanner.ACCEPT_HOST_CMD);
    }

    @Test
    public void sendMessageRejectHostCommands() {
        Dummy9xxxExtension scanner = new Dummy9xxxExtension();
        scanner.port = _port;
        assertThat(scanner.sendMessage("rejectHostCmd")).isEqualTo(1);
        verify(_port, times(1)).sendData(DLS9xxxScanner.REJECT_HOST_CMD);
    }

    @Test
    public void sendMessage() {
        Dummy9xxxExtension scanner = new Dummy9xxxExtension();
        assertThat(scanner.sendMessage("Test")).isEqualTo(4);
    }
    //endregion

    private void setUpLabels() {

        //region CODE128
        when(_helper.getLabelsIds(new byte[]{0, 24, 11})).thenReturn(new LabelIds(
                "SCAN_SDT_Code128",
                110,
                "CI_LABEL_ID_CODE128",
                "23",
                "54",
                "00180B",
                "4233",
                "",
                ""
        ));
        //endregion

        //region CODE39
        when(_helper.getLabelsIds(new byte[]{0, 10, 11})).thenReturn(new LabelIds(
                "SCAN_SDT_Code39",
                108,
                "CI_LABEL_ID_CODE39",
                "2A",
                "56",
                "000A0B",
                "4231",
                "",
                ""
        ));
        //endregion

        //region EAN13

        when(_helper.getLabelsIds(new byte[]{22, 57, 55})).thenReturn(new LabelIds(
                "UNKNOWN",
                ScannerConstants.SCAN_SDT_UNKNOWN,
                "UNKNOWN",
                "UNKNOWN",
                "UNKNOWN",
                "UNKNOWN",
                "UNKNOWN",
                "",
                ""
        ));

        when(_helper.getLabelsIds(new byte[]{22, 57})).thenReturn(new LabelIds(
                "UNKNOWN",
                ScannerConstants.SCAN_SDT_UNKNOWN,
                "UNKNOWN",
                "UNKNOWN",
                "UNKNOWN",
                "UNKNOWN",
                "UNKNOWN",
                "",
                ""
        ));

        when(_helper.getLabelsIds(new byte[]{22})).thenReturn(new LabelIds(
                "SCAN_SDT_EAN13",
                104,
                "CI_LABEL_ID_EAN13",
                "46",
                "42",
                "16",
                "16",
                "",
                ""
        ));

        //endregion

    }

    private class DummyPort extends DLSPort {
        private final DLS9xxxScanner _scanner;
        private final String _received;

        DummyPort(DLS9xxxScanner scanner, String receivedData) {
            _scanner = scanner;
            _received = receivedData;
        }

        @Override
        public void changeBaudRate(int baudRate) {

        }

        @Override
        public boolean closePort() {
            return false;
        }

        @Override
        public boolean openPort() {
            return false;
        }

        @Override
        public int sendData(byte[] buffer, int len) {
            return 0;
        }

        @Override
        public int sendData(String buffer) {
            _scanner.sohBuf = _received.getBytes();
            return _received.length();
        }
    }

    private class Dummy9xxxExtension extends DLS9xxxScanner {
        private boolean result = true;

        public void setDetectResetFinish(boolean newResult) {
            result = newResult;
        }

        @Override
        public boolean detectResetFinish() {
            return result;
        }

        @Override
        protected byte[] sendReceiveMsg(String msg) {
            return msg.getBytes();
        }
    }
}