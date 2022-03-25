package com.datalogic.dlapos.androidpos.interpretation;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.Context;

import androidx.core.util.Pair;

import com.datalogic.dlapos.androidpos.common.Constants;
import com.datalogic.dlapos.androidpos.common.DLSDeviceInfo;
import com.datalogic.dlapos.androidpos.common.DLSException;
import com.datalogic.dlapos.androidpos.common.DLSProperties;
import com.datalogic.dlapos.androidpos.common.DLSScannerConfig;
import com.datalogic.dlapos.androidpos.common.DLSState;
import com.datalogic.dlapos.androidpos.utils.MockUtils;
import com.datalogic.dlapos.commons.constant.ScannerConstants;
import com.datalogic.dlapos.commons.support.APosException;
import com.datalogic.dlapos.confighelper.DLAPosConfigHelper;
import com.datalogic.dlapos.confighelper.configurations.accessor.LabelHelper;
import com.datalogic.dlapos.confighelper.configurations.accessor.LabelIds;
import com.datalogic.dlapos.confighelper.configurations.accessor.ProfileManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockedConstruction;

public class DLSScannerTest {

    @Mock
    DLAPosConfigHelper _dlaPosConfigHelper = mock(DLAPosConfigHelper.class);

    @Mock
    private final Context _context = mock(Context.class);

    @Mock
    ProfileManager _profileManager = mock(ProfileManager.class);

    @Mock
    private final DLSScannerConfig _scannerConfig = mock(DLSScannerConfig.class);

    @Mock
    private final LabelHelper _helper = mock(LabelHelper.class);

    @Mock
    private final DLSProperties _properties = mock(DLSProperties.class);

    @Mock
    private final DLSDeviceInfo _info = mock(DLSDeviceInfo.class);

    @Before
    public void setup() {
        MockUtils.mockDLSPropertiesSingleton(_properties);
        MockUtils.mockConfigHelperSingleton(_dlaPosConfigHelper, _profileManager);
    }

    @After
    public void clean() {
        MockUtils.cleanDLSProperties();
        MockUtils.cleanConfigHelper();
    }

    @Test(expected = IllegalArgumentException.class)
    public void itemReceivedListenerNull() {
        DummyScanner scanner = new DummyScanner();
        scanner.addItemReceivedListener(null);
    }

    @Test
    public void itemReceivedListener() {
        DummyScanner scanner = new DummyScanner();
        ItemReceivedListener listener = mock(ItemReceivedListener.class);
        scanner.addItemReceivedListener(listener);
        scanner.fireItemReceivedEvent("Test".getBytes());
        verify(listener, times(1)).onItemReceived("Test".getBytes());

        ItemReceivedListener listener1 = mock(ItemReceivedListener.class);
        scanner.addItemReceivedListener(listener1);
        scanner.fireItemReceivedEvent("Test1".getBytes());
        verify(listener, times(1)).onItemReceived("Test1".getBytes());
        verify(listener1, times(1)).onItemReceived("Test1".getBytes());

        scanner.removeItemReceivedListener(listener1);
        scanner.fireItemReceivedEvent("Test2".getBytes());
        verify(listener, times(1)).onItemReceived("Test2".getBytes());
        verify(listener1, times(0)).onItemReceived("Test2".getBytes());

        scanner.removeItemReceivedListener(listener);
        scanner.fireItemReceivedEvent("Test3".getBytes());
        verify(listener, times(0)).onItemReceived("Test3".getBytes());
        verify(listener1, times(0)).onItemReceived("Test3".getBytes());
    }

    @Test(expected = IllegalArgumentException.class)
    public void labelReceivedListenerNull() {
        DummyScanner scanner = new DummyScanner();
        scanner.addLabelReceivedListener(null);
    }

    @Test
    public void labelReceivedListener() {
        DummyScanner scanner = new DummyScanner();
        LabelReceivedListener listener = mock(LabelReceivedListener.class);
        scanner.addLabelReceivedListener(listener);
        scanner.fireLabelReceivedEvent("RawTest".getBytes(), "Test".getBytes(), 1);
        verify(listener, times(1)).onLabelReceived("RawTest".getBytes(), "Test".getBytes(), 1);

        LabelReceivedListener listener1 = mock(LabelReceivedListener.class);
        scanner.addLabelReceivedListener(listener1);
        scanner.fireLabelReceivedEvent("RawTest1".getBytes(), "Test1".getBytes(), 1);
        verify(listener, times(1)).onLabelReceived("RawTest1".getBytes(), "Test1".getBytes(), 1);
        verify(listener1, times(1)).onLabelReceived("RawTest1".getBytes(), "Test1".getBytes(), 1);

        scanner.removeLabelReceivedListener(listener1);
        scanner.fireLabelReceivedEvent("RawTest2".getBytes(), "Test2".getBytes(), 1);
        verify(listener, times(1)).onLabelReceived("RawTest2".getBytes(), "Test2".getBytes(), 1);
        verify(listener1, times(0)).onLabelReceived("RawTest2".getBytes(), "Test2".getBytes(), 1);

        scanner.removeLabelReceivedListener(listener);
        scanner.fireLabelReceivedEvent("RawTest3".getBytes(), "Test3".getBytes(), 1);
        verify(listener, times(0)).onLabelReceived("RawTest3".getBytes(), "Test3".getBytes(), 1);
        verify(listener1, times(0)).onLabelReceived("RawTest3".getBytes(), "Test3".getBytes(), 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void systemDataReceivedListenerNull() {
        DummyScanner scanner = new DummyScanner();
        scanner.addSystemDataReceivedListener(null);
    }

    @Test
    public void systemDataReceivedListener() {
        DummyScanner scanner = new DummyScanner();
        SystemDataReceivedListener listener = mock(SystemDataReceivedListener.class);
        scanner.addSystemDataReceivedListener(listener);
        scanner.fireSystemDataReceivedEvent("Test".getBytes());
        verify(listener, times(1)).onSystemDataReceived("Test".getBytes());

        SystemDataReceivedListener listener1 = mock(SystemDataReceivedListener.class);
        scanner.addSystemDataReceivedListener(listener1);
        scanner.fireSystemDataReceivedEvent("Test1".getBytes());
        verify(listener, times(1)).onSystemDataReceived("Test1".getBytes());
        verify(listener1, times(1)).onSystemDataReceived("Test1".getBytes());

        scanner.removeSystemDataReceivedListener(listener1);
        scanner.fireSystemDataReceivedEvent("Test2".getBytes());
        verify(listener, times(1)).onSystemDataReceived("Test2".getBytes());
        verify(listener1, times(0)).onSystemDataReceived("Test2".getBytes());

        scanner.removeSystemDataReceivedListener(listener);
        scanner.fireSystemDataReceivedEvent("Test3".getBytes());
        verify(listener, times(0)).onSystemDataReceived("Test3".getBytes());
        verify(listener1, times(0)).onSystemDataReceived("Test3".getBytes());
    }

    @Test
    public void extractBarcodeLabel() {
        DummyScanner scanner = new DummyScanner();
        assertThat(new String(scanner.extractBarcodeLabel("TCiao ciaoT".getBytes(), 1))).isEqualTo("Ciao ciao");
        assertThat(new String(scanner.extractBarcodeLabel("Bye bye miss american pieT".getBytes(), 4))).isEqualTo("bye miss american pie");
    }

    @Test
    public void extractBarcodeType() {
        DummyScanner scanner = new DummyScanner();
        when(_dlaPosConfigHelper.getLabelHelper()).thenReturn(_helper);
        setUpLabels();
        when(_info.getDecodingType()).thenReturn(Constants.DECODE_TYPE_OEM);
        scanner.deviceInfo = _info;
        Pair<LabelIds, Integer> label = scanner.extractBarcodeType(new byte[]{0, 24, 11});
        assertThat(label.second).isEqualTo(3);
        assertThat(label.first.getUposId()).isEqualTo(110);

        label = scanner.extractBarcodeType(new byte[]{0, 10, 11});
        assertThat(label.second).isEqualTo(3);
        assertThat(label.first.getUposId()).isEqualTo(108);

        label = scanner.extractBarcodeType(new byte[]{22, 57, 55});
        assertThat(label.second).isEqualTo(1);
        assertThat(label.first.getUposId()).isEqualTo(104);
    }

    @Test
    public void fireLabelReceivedEventNoPrefixNoSuffix() {
        DummyScanner scanner = new DummyScanner();
        LabelReceivedListener listener = mock(LabelReceivedListener.class);
        scanner.addLabelReceivedListener(listener);
        scanner.fireLabelReceivedEvent("TestRaw".getBytes(), "TestDecoded".getBytes(), 2);
        verify(listener, times(1)).onLabelReceived("TestRaw".getBytes(), "TestDecoded".getBytes(), 2);
    }

    @Test
    public void fireLabelReceivedEventPrefixNoSuffix() {
        DummyScanner scanner = new DummyScanner();
        when(_info.getDataPrefix()).thenReturn("P".getBytes()[0]);
        scanner.deviceInfo = _info;
        LabelReceivedListener listener = mock(LabelReceivedListener.class);
        scanner.addLabelReceivedListener(listener);
        scanner.fireLabelReceivedEvent("TestRaw".getBytes(), "TestDecoded".getBytes(), 2);
        verify(listener, times(1)).onLabelReceived("PTestRaw".getBytes(), "PTestDecoded".getBytes(), 2);
    }

    @Test
    public void fireLabelReceivedEventNoPrefixButSuffix() {
        DummyScanner scanner = new DummyScanner();
        when(_info.getDataSuffix()).thenReturn("E".getBytes()[0]);
        scanner.deviceInfo = _info;
        LabelReceivedListener listener = mock(LabelReceivedListener.class);
        scanner.addLabelReceivedListener(listener);
        scanner.fireLabelReceivedEvent("TestRaw".getBytes(), "TestDecoded".getBytes(), 2);
        verify(listener, times(1)).onLabelReceived("TestRawE".getBytes(), "TestDecodedE".getBytes(), 2);
    }

    @Test
    public void fireLabelReceivedEventPrefixSuffix() {
        DummyScanner scanner = new DummyScanner();
        when(_info.getDataPrefix()).thenReturn("P".getBytes()[0]);
        when(_info.getDataSuffix()).thenReturn("E".getBytes()[0]);
        scanner.deviceInfo = _info;
        LabelReceivedListener listener = mock(LabelReceivedListener.class);
        scanner.addLabelReceivedListener(listener);
        scanner.fireLabelReceivedEvent("TestRaw".getBytes(), "TestDecoded".getBytes(), 2);
        verify(listener, times(1)).onLabelReceived("PTestRawE".getBytes(), "PTestDecodedE".getBytes(), 2);
    }

    @Test
    public void futureBaudRate() {
        DummyScanner scanner = new DummyScanner();
        scanner.setFutureBaudRate(12);
        assertThat(scanner.getFutureBaudRate()).isEqualTo(12);

        scanner.setFutureBaudRate(15);
        assertThat(scanner.getFutureBaudRate()).isEqualTo(15);

        scanner.scheduleFutureBaudRateChange(2400);
        assertThat(scanner.getFutureBaudRate()).isEqualTo(2400);
        assertThat(scanner.isChangeBaudAfterUpdate()).isTrue();

        scanner.setChangeBaudAfterUpdate(false);
        scanner.scheduleFutureBaudRateChange(115200);
        assertThat(scanner.getFutureBaudRate()).isEqualTo(115200);
        assertThat(scanner.isChangeBaudAfterUpdate()).isTrue();
    }

    @Test(expected = IllegalArgumentException.class)
    public void futureBaudRateTooLow() {
        DummyScanner scanner = new DummyScanner();
        scanner.scheduleFutureBaudRateChange(2399);
    }

    @Test(expected = IllegalArgumentException.class)
    public void futureBaudRateTooHigh() {
        DummyScanner scanner = new DummyScanner();
        scanner.scheduleFutureBaudRateChange(115201);
    }

    @Test
    public void getCanCompareFirmwareVersion() {
        DummyScanner scanner = new DummyScanner();
        assertThat(scanner.getCanCompareFirmwareVersion()).isFalse();

        when(_scannerConfig.getCanCompareFirmwareVersion()).thenReturn(true);
        scanner.setScannerConfig(_scannerConfig);

        assertThat(scanner.getCanCompareFirmwareVersion()).isTrue();

        when(_scannerConfig.getCanCompareFirmwareVersion()).thenReturn(false);
        assertThat(scanner.getCanCompareFirmwareVersion()).isFalse();
    }

    @Test
    public void getCanUpdateFirmware() {
        DummyScanner scanner = new DummyScanner();
        assertThat(scanner.getCanUpdateFirmware()).isFalse();

        when(_scannerConfig.getCanUpdateFirmware()).thenReturn(true);
        scanner.setScannerConfig(_scannerConfig);

        assertThat(scanner.getCanUpdateFirmware()).isTrue();

        when(_scannerConfig.getCanUpdateFirmware()).thenReturn(false);
        assertThat(scanner.getCanUpdateFirmware()).isFalse();
    }

    @Test
    public void getDeleteImageFileAfterRead() {
        DummyScanner scanner = new DummyScanner();
        assertThat(scanner.getDeleteImageFileAfterRead()).isFalse();

        when(_scannerConfig.getDeleteImageFileAfterRead()).thenReturn(true);
        scanner.setScannerConfig(_scannerConfig);

        assertThat(scanner.getDeleteImageFileAfterRead()).isTrue();

        when(_scannerConfig.getDeleteImageFileAfterRead()).thenReturn(false);
        assertThat(scanner.getDeleteImageFileAfterRead()).isFalse();
    }

    @Test
    public void internalClaim() throws DLSException {
        DummyScanner scanner = new DummyScanner();
        when(_info.getDeviceBus()).thenReturn("USB");
        when(_info.isConfigOnClaim()).thenReturn(true);
        when(_properties.isAutoLoadConfig()).thenReturn(true);
        scanner.deviceInfo = _info;
        scanner.internalClaim();
        assertThat(scanner.reportConfigurationCalls).isEqualTo(1);
        assertThat(scanner.updateConfigurationCalls).isEqualTo(1);
    }

    @Test
    public void internalClaimNotUSB() throws DLSException {
        DummyScanner scanner = new DummyScanner();
        when(_info.getDeviceBus()).thenReturn("BLT");
        scanner.internalClaim();
        assertThat(scanner.reportConfigurationCalls).isEqualTo(0);
        assertThat(scanner.updateConfigurationCalls).isEqualTo(0);
    }

    @Test
    public void internalClaimNoConfigOnClaim() throws DLSException {
        DummyScanner scanner = new DummyScanner();
        when(_info.getDeviceBus()).thenReturn("USB");
        when(_properties.isAutoLoadConfig()).thenReturn(true);
        when(_info.isConfigOnClaim()).thenReturn(false);
        scanner.internalClaim();
        assertThat(scanner.reportConfigurationCalls).isEqualTo(0);
        assertThat(scanner.updateConfigurationCalls).isEqualTo(0);
    }

    @Test
    public void internalClaimNoAutoLoadConfig() throws DLSException {
        DummyScanner scanner = new DummyScanner();
        when(_info.getDeviceBus()).thenReturn("USB");
        when(_properties.isAutoLoadConfig()).thenReturn(false);
        when(_info.isConfigOnClaim()).thenReturn(true);
        scanner.internalClaim();
        assertThat(scanner.reportConfigurationCalls).isEqualTo(0);
        assertThat(scanner.updateConfigurationCalls).isEqualTo(0);
    }

    @Test
    public void logHealthCheckMotor() throws DLSException {
        DummyScanner scanner = new DummyScanner();
        try {
            scanner.logHealthCheck(insertSTX_ETX("mFAIL"));
            fail();
        } catch (DLSException e) {
            assertThat(e.getLocalizedMessage().contains("motor")).isTrue();
        }
        scanner.logHealthCheck(insertSTX_ETX("mOK"));
    }

    @Test
    public void logHealthCheckHorizontalLaser() throws DLSException {
        DummyScanner scanner = new DummyScanner();
        try {
            scanner.logHealthCheck(insertSTX_ETX("hFAIL"));
            fail();
        } catch (DLSException e) {
            assertThat(e.getLocalizedMessage().contains("Horizontal Laser")).isTrue();
        }
        scanner.logHealthCheck(insertSTX_ETX("hOK"));
    }

    @Test
    public void logHealthCheckVerticalLaser() throws DLSException {
        DummyScanner scanner = new DummyScanner();
        try {
            scanner.logHealthCheck(insertSTX_ETX("vFAIL"));
            fail();
        } catch (DLSException e) {
            assertThat(e.getLocalizedMessage().contains("Vertical Laser")).isTrue();
        }
        scanner.logHealthCheck(insertSTX_ETX("vOK"));
    }

    @Test
    public void logHealthCheckScale() throws DLSException {
        DummyScanner scanner = new DummyScanner();
        try {
            scanner.logHealthCheck(insertSTX_ETX("sFAIL"));
            fail();
        } catch (DLSException e) {
            assertThat(e.getLocalizedMessage().contains("Scale")).isTrue();
        }
        scanner.logHealthCheck(insertSTX_ETX("sOK"));
    }

    @Test
    public void logHealthCheckDisplay() throws DLSException {
        DummyScanner scanner = new DummyScanner();
        try {
            scanner.logHealthCheck(insertSTX_ETX("dFAIL"));
            fail();
        } catch (DLSException e) {
            assertThat(e.getLocalizedMessage().contains("Display")).isTrue();
        }
        scanner.logHealthCheck(insertSTX_ETX("dOK"));
    }

    @Test
    public void logHealthCheckEAS() throws DLSException {
        DummyScanner scanner = new DummyScanner();
        try {
            scanner.logHealthCheck(insertSTX_ETX("eFAIL"));
            fail();
        } catch (DLSException e) {
            assertThat(e.getLocalizedMessage().contains("EAS")).isTrue();
        }
        scanner.logHealthCheck(insertSTX_ETX("eOK"));
    }

    @Test
    public void logHealthCheckCamera() throws DLSException {
        DummyScanner scanner = new DummyScanner();
        try {
            scanner.logHealthCheck(insertSTX_ETX("cFAIL"));
            fail();
        } catch (DLSException e) {
            assertThat(e.getLocalizedMessage().contains("Camera")).isTrue();
        }
        scanner.logHealthCheck(insertSTX_ETX("cOK"));
    }

    //region Open
    @Test(expected = IllegalArgumentException.class)
    public void openNullContext() throws DLSException {
        DummyScanner device = new DummyScanner();
        device.open("adsa", null);
        assertThat(device.getState()).isNotEqualTo(DLSState.OPENED);
    }

    @Test(expected = IllegalArgumentException.class)
    public void openNullString() throws DLSException {
        DummyScanner device = new DummyScanner();
        device.open(null, _context);
        assertThat(device.getState()).isNotEqualTo(DLSState.OPENED);
    }

    @Test
    public void open() throws DLSException {
        DummyScanner device = new DummyScanner();
        try (MockedConstruction<DLSScannerConfig> mockedConfig = mockConstruction(DLSScannerConfig.class,
                (mock, context) -> {
                    when(mock.getLogicalName()).thenReturn("I'm the mock");
                })) {
            device.open("test", _context);
            assertThat(device.context).isEqualTo(_context);
            assertThat(device.getLogicalName()).isEqualTo("test");
            assertThat(device.properties).isEqualTo(_properties);
            assertThat(device.getConfiguration().getLogicalName()).isEqualTo("I'm the mock");
            assertThat(device.getState()).isEqualTo(DLSState.OPENED);
        }
    }

    @Test(expected = DLSException.class)
    public void openNoProfile() throws DLSException, APosException {
        DummyScanner device = new DummyScanner();
        when(_profileManager.getConfigurationForProfileId("test")).thenThrow(new APosException());
        device.open("test", _context);
        assertThat(device.getState()).isNotEqualTo(DLSState.OPENED);
    }

    @Test(expected = DLSException.class)
    public void openLoadException() throws DLSException {
        DummyScanner device = new DummyScanner();
        try (MockedConstruction<DLSScannerConfig> ignored = mockConstruction(DLSScannerConfig.class,
                (mock, context) -> {
                    when(mock.loadConfiguration(any(), any())).thenThrow(new APosException());
                })) {
            device.open("test", _context);
            assertThat(device.getState()).isNotEqualTo(DLSState.OPENED);
        }
    }

    //endregion

    @Test
    public void parseDecodeType() {
        DummyScanner scanner = new DummyScanner();
        assertThat(scanner.parseDecodeType(Constants.DECODE_TYPE_EU)).isEqualTo(LabelHelper.DecodeType.EUCODE);
        assertThat(scanner.parseDecodeType(Constants.DECODE_TYPE_SCRS232)).isEqualTo(LabelHelper.DecodeType.SCRS232);
        assertThat(scanner.parseDecodeType(Constants.DECODE_TYPE_OEM)).isEqualTo(LabelHelper.DecodeType.IBMUSBOEM);
        assertThat(scanner.parseDecodeType(Constants.DECODE_TYPE_US)).isEqualTo(LabelHelper.DecodeType.USCODE);
        assertThat(scanner.parseDecodeType("Test")).isNull();
    }

    @Test
    public void recordTimeout() {
        DummyScanner scanner = new DummyScanner();
        scanner.setRecordTimeout(12);
        assertThat(scanner.getRecordTimeout()).isEqualTo(12);

        scanner.setRecordTimeout(22);
        assertThat(scanner.getRecordTimeout()).isEqualTo(22);
    }

    @Test
    public void scannerConfig() {
        DummyScanner scanner = new DummyScanner();
        scanner.setScannerConfig(_scannerConfig);
        assertThat(scanner.getScannerConfig()).isEqualTo(_scannerConfig);
        assertThat(scanner.getConfiguration()).isEqualTo(_scannerConfig);
    }

    @Test
    public void splitMessage() {
        DummyScanner scanner = new DummyScanner();
        byte[] firstPart = insertSTX_ETX("Bye");
        byte[] secondPart = insertSTX_ETX("bye");
        byte[] message = new byte[firstPart.length + secondPart.length];
        System.arraycopy(firstPart, 0, message, 0, firstPart.length);
        System.arraycopy(secondPart, 0, message, firstPart.length, secondPart.length);

        String[] result = scanner.splitMessage(message);
        assertThat(result.length).isEqualTo(2);
        assertThat(result[0]).isEqualTo("Bye");
        assertThat(result[1]).isEqualTo("bye");
        assertThat(scanner.messageList.isEmpty()).isTrue();
        assertThat(scanner.outputStream.size()).isEqualTo(0);

        scanner.messageList.add(message);
        firstPart = insertSTX_ETX("miss american");
        secondPart = insertSTX_ETX("pie");
        byte[] secondMessage = new byte[firstPart.length + secondPart.length];
        System.arraycopy(firstPart, 0, secondMessage, 0, firstPart.length);
        System.arraycopy(secondPart, 0, secondMessage, firstPart.length, secondPart.length);
        assertThat(scanner.messageList.size()).isEqualTo(1);
        result = scanner.splitMessage(secondMessage);
        assertThat(result.length).isEqualTo(4);
        assertThat(result[0]).isEqualTo("Bye");
        assertThat(result[1]).isEqualTo("bye");
        assertThat(result[2]).isEqualTo("miss american");
        assertThat(result[3]).isEqualTo("pie");
        assertThat(scanner.messageList.isEmpty()).isTrue();
        assertThat(scanner.outputStream.size()).isEqualTo(0);
    }

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

    private byte[] insertSTX_ETX(String message) {
        byte[] data = new byte[2 + message.length()];
        data[0] = 0x02;
        data[data.length - 1] = 0x03;
        System.arraycopy(message.getBytes(), 0, data, 1, message.length());
        return data;
    }

    private class DummyScanner extends DLSScanner {

        int updateConfigurationCalls = 0;
        int reportConfigurationCalls = 0;

        /**
         * Changes the port baud rate.
         * <p>
         * Used to alter the port during a firmware update.  Does not alter the scanner settings.
         *
         * @param baud int indicating the new baud rate to set.
         */
        @Override
        public void changeBaudRate(int baud) {

        }

        /**
         * Disables the beep on good read functionality.
         * <p>
         * Only available on USB-OEM and SC-RS232 interfaces. This method has no
         * effect otherwise.
         *
         * @throws DLSException not thrown
         */
        @Override
        public void disableBeep() throws DLSException {

        }

        /**
         * Enables the beep on good read functionality.
         * <p>
         * Only available on USB-OEM and SC-RS232 interfaces. This method has no
         * effect otherwise.
         *
         * @throws DLSException not thrown
         */
        @Override
        public void enableBeep() throws DLSException {

        }

        @Override
        public DLSScannerConfig reportConfiguration() throws DLSException {
            reportConfigurationCalls++;
            return null;
        }

        /**
         * Sends a message to the scanner.
         * <p>
         * Only implemented by {@link DLS9xxxScanner}. Will throw
         * {@code UnsupportedOperationException} in all other instances.
         *
         * @param message String containing the message to send to the scanner
         * @return int indicating the value of the response status byte
         */
        @Override
        public int sendMessage(String message) {
            return 0;
        }

        /**
         * Sends a firmware record to the scanner.
         *
         * @param record String containing the record to send.
         * @return int indicating the command response from the scanner
         */
        @Override
        public int sendRecord(String record) {
            return 0;
        }

        /**
         * Updates the scanner configuration with parameters from the current
         * scanner configuration object.
         * <p>
         * Only available for USB-OEM scanners. This method has no effect otherwise.
         *
         * @throws DLSException not thrown
         */
        @Override
        public void updateConfiguration() throws DLSException {
            updateConfigurationCalls++;
        }

        /**
         * Detects when a successful reset has been completed.
         * This method returns when communication with the device has been
         * re-established or timeout has been reached.
         * <p>
         * <b> Important: this method does not cause or initiate a reset; only
         * detects when one is completed. It is necessary to call {@link #reset()}
         * prior to this method. </b>
         * <p>
         * Example Usage: <pre>
         *      try {
         *          scanner.reset();
         *      catch {DLSException e} {
         *          //handle exception
         *      }
         *      boolean resetSuccess = scanner.detectResetFinish();
         * </pre>
         *
         * @return boolean indicating reset success. {@code true} - if device
         * returns before timeout, {@code false} - if timeout is reached
         */
        @Override
        public boolean detectResetFinish() throws DLSException {
            return false;
        }

        /**
         * Sends a Direct I/O command to the device.
         * <p>
         * Depending upon the interface and the Direct I/O command being
         * executed, the {@code data} parameter may not need to be populated and
         * {@code null} can be used (refer to the JavaPOS user guide for specific
         * implementation). Any result messages or data will be passed back through
         * the {@code ByteArrayOutputStream} object.
         * <p>
         * Example Usage: <pre>
         *      {@code
         *      ByteArrayOuputStream bs = new ByteArrayOutputStream();
         *      scanner.directIO(DLSJposConst.DIO_SCALE_STATUS, dioData, bs);
         *      String result = bs.toString();}
         * </pre>
         *
         * @param command int indicating the Direct I/O command to perform
         * @param data    int array containing any data to pass with the command. The
         *                array may be populated with the status of the command upon
         *                completion.
         * @param object  ByteArrayOutputStream object used to return messages
         *                from performing the command.
         * @throws DLSException if an error occurred during execution, or an
         *                      invalid command argument was supplied
         */
        @Override
        public void directIO(int command, int[] data, Object object) throws DLSException {

        }

        /**
         * Disables the device.
         *
         * @throws DLSException if invalid state transition is attempted
         */
        @Override
        public void disable() throws DLSException {

        }

        /**
         * Performs a Health Check on the device. If check fails or timeout occurs,
         * method returns {@code false}
         *
         * @param timeout int indicating the amount of time to wait for the health
         *                check to complete
         * @return boolean indicating if scanner passed the health check
         * @throws DLSException if an error occurs
         */
        @Override
        public boolean doHealthCheck(int timeout) throws DLSException {
            return false;
        }

        /**
         * Performs a self check on the device.
         *
         * @throws DLSException if an error occurs
         */
        @Override
        public void doSelfTest() throws DLSException {

        }

        /**
         * Enables the device.
         *
         * @throws DLSException if invalid state transition is attempted
         */
        @Override
        public void enable() throws DLSException {

        }

        /**
         * Indicates whether the device is alive.
         *
         * @return boolean indicating whether a device is alive
         * @throws DLSException not thrown
         */
        @Override
        public boolean isAlive() throws DLSException {
            return false;
        }

        /**
         * Issues a reset command to the device.
         * This method returns immediately after sending the command, no response
         * is verified.
         * <p>
         * For a synchronous reset (notification when device has returned), call
         * {@link #detectResetFinish()} immediately after.
         *
         * @throws DLSException not thrown
         */
        @Override
        public void reset() throws DLSException {

        }

        @Override
        public void onDataReceived(byte[] buf, int len) {

        }

        @Override
        public void onDeviceReattached() {

        }

        @Override
        public void onDeviceRemoved() {

        }
    }
}