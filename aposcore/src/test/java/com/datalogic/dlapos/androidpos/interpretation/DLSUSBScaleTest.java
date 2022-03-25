package com.datalogic.dlapos.androidpos.interpretation;

import android.content.Context;

import com.datalogic.dlapos.androidpos.common.DLSDeviceInfo;
import com.datalogic.dlapos.androidpos.common.DLSException;
import com.datalogic.dlapos.androidpos.common.DLSJposConst;
import com.datalogic.dlapos.androidpos.common.DLSObjectFactory;
import com.datalogic.dlapos.androidpos.common.DLSProperties;
import com.datalogic.dlapos.androidpos.common.DLSScaleConfig;
import com.datalogic.dlapos.androidpos.common.DLSState;
import com.datalogic.dlapos.androidpos.common.DLSStatistics;
import com.datalogic.dlapos.androidpos.transport.DLSUsbPort;
import com.datalogic.dlapos.androidpos.transport.UsbPortStatusListener;
import com.datalogic.dlapos.androidpos.utils.MockUtils;
import com.datalogic.dlapos.commons.constant.CommonsConstants;
import com.datalogic.dlapos.commons.constant.ScaleConstants;

import org.junit.After;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.io.ByteArrayOutputStream;
import java.util.Map;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DLSUSBScaleTest {

    @Mock
    private final Context _context = mock(Context.class);

    @Mock
    private final DLSUsbPort _port = mock(DLSUsbPort.class);

    @Mock
    private final DLSDeviceInfo _deviceInfo = mock(DLSDeviceInfo.class);

    @Test
    public void canZeroScale() {
        DLSUSBScale scale = new DLSUSBScale();
        assertThat(scale.canZeroScale()).isTrue();
    }

    @After
    public void cleanUp() {
        DLSJposConst.updateInProgress = false;
    }

//    //region Claim
//    @Test
//    public void claim() throws DLSException {
//        DLSUSBScale scale = new DLSUSBScale();
//        scale.deviceInfo = _deviceInfo;
//        scale.context = _context;
//        scale.setState(DLSState.OPENED);
//        when(_port.openPort()).thenReturn(true);
//        try (MockedStatic<DLSObjectFactory> factoryMockedStatic = Mockito.mockStatic(DLSObjectFactory.class)) {
//            factoryMockedStatic.when(() -> DLSObjectFactory.createPort(_deviceInfo, _context)).thenReturn(_port);
//            scale.claim(100);
//            verify(_port, times(1)).registerPortStatusListener(scale);
//            verify(_port, times(1)).openPort();
//        }
//    }
//
//    @Test
//    public void claimNotNullPort() throws DLSException {
//        DLSUSBScale scale = new DLSUSBScale();
//        scale.deviceInfo = _deviceInfo;
//        scale.context = _context;
//        scale.port = _port;
//        scale.setState(DLSState.OPENED);
//        scale.setState(DLSState.CLAIMED);
//        when(_port.openPort()).thenReturn(true);
//        when(_port.isOpen()).thenReturn(true);
//        when(_port.closePort()).then(invocation -> {
//            when(_port.isOpen()).thenReturn(false);
//            return null;
//        });
//        try (MockedStatic<DLSObjectFactory> factoryMockedStatic = Mockito.mockStatic(DLSObjectFactory.class)) {
//            factoryMockedStatic.when(() -> DLSObjectFactory.createPort(_deviceInfo, _context)).thenReturn(_port);
//            scale.claim(100);
//            verify(_port, times(1)).closePort();
//            verify(_port, times(1)).registerPortStatusListener(scale);
//            verify(_port, times(1)).openPort();
//        }
//    }
//
//    @Test(expected = DLSException.class)
//    public void claimExceptionOnOpenPort() throws DLSException {
//        DLSUSBScale scale = new DLSUSBScale();
//        scale.deviceInfo = _deviceInfo;
//        scale.context = _context;
//        scale.setState(DLSState.OPENED);
//        when(_port.openPort()).thenReturn(false);
//        try (MockedStatic<DLSObjectFactory> factoryMockedStatic = Mockito.mockStatic(DLSObjectFactory.class)) {
//            factoryMockedStatic.when(() -> DLSObjectFactory.createPort(_deviceInfo, _context)).thenReturn(_port);
//            scale.claim(100);
//        }
//    }
//    //endregion

    @Test
    public void clearDisplayWhileUpgrading() throws DLSException {
        DLSUSBScale scale = new DLSUSBScale();
        scale.port = _port;
        DLSJposConst.updateInProgress = true;
        scale.clearDisplay();
        verify(_port, times(0)).sendData(DLSUSBScale.clearRemoteDisplayCmd, DLSUSBScale.clearRemoteDisplayCmd.length);
    }

    @Test
    public void clearDisplay() throws DLSException {
        DLSUSBScale scale = new DLSUSBScale();
        scale.port = _port;
        scale.clearDisplay();
        verify(_port, times(1)).sendData(DLSUSBScale.clearRemoteDisplayCmd, DLSUSBScale.clearRemoteDisplayCmd.length);
    }

    //region DirectIO

    @Test
    public void directIOWhileUpgrading() throws DLSException {
        DLSUSBScale scale = new DLSUSBScale();
        DLSJposConst.updateInProgress = true;
        scale.directIO(1, new int[]{}, new Object());
    }

    @Test
    public void directIOEnable3ByteStatus() throws DLSException {
        DLSUSBScale scale = new DLSUSBScale();
        scale.port = _port;
        when(_port.sendData(DLSUSBScale.enable3ByteStatusCmd)).then(invocation -> {
            scale.bGotStatus = true;
            scale.nStat0 = 0;
            scale.nStat1 = 7;
            scale.nStat2 = 10;
            return null;
        });
        int[] data = new int[]{9};
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        scale.directIO(DLSJposConst.DIO_SCALE_ENABLE3BYTESTATUS, data, stream);
        assertThat(data[0]).isEqualTo(0);
        assertThat(new String(stream.toByteArray())).isEqualTo("Status: 0x00 0x07 0x0A");
    }

    @Test
    public void directIODisable3ByteStatus() throws DLSException {
        DLSUSBScale scale = new DLSUSBScale();
        scale.port = _port;
        when(_port.sendData(DLSUSBScale.disable3ByteStatusCmd)).then(invocation -> {
            scale.bGotStatus = true;
            scale.nStat0 = 1;
            scale.nStat1 = 8;
            scale.nStat2 = 11;
            return null;
        });
        int[] data = new int[]{9};
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        scale.directIO(DLSJposConst.DIO_SCALE_DISABLE3BYTESTATUS, data, stream);
        assertThat(data[0]).isEqualTo(0);
        assertThat(new String(stream.toByteArray())).isEqualTo("Status: 0x01 0x08 0x0B");
    }

    @Test
    public void directIOScaleConfigureFewArgs() {
        DLSUSBScale scale = new DLSUSBScale();
        int[] data = new int[]{9, 3};
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try {
            scale.directIO(DLSJposConst.DIO_SCALE_CONFIGURE, data, stream);
            fail();
        } catch (DLSException e) {
            assertThat(e.getErrorCode()).isEqualTo(DLSJposConst.DLS_E_INVALID_ARG);
        }
    }

    @Test
    public void directIOScaleConfigure() throws DLSException {
        DLSUSBScale scale = new DLSUSBScale();
        scale.port = _port;
        when(_port.sendData(scale.configureScaleCmd)).then(invocation -> {
            scale.bGotStatus = true;
            scale.nStat0 = 77;
            scale.nStat1 = 69;
            scale.nStat2 = 71;
            return null;
        });
        int[] data = new int[]{9, 3, 6};
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        scale.directIO(DLSJposConst.DIO_SCALE_CONFIGURE, data, stream);
        verify(_port, times(1)).sendData(scale.configureScaleCmd);
        verify(_port, times(1)).sendData(DLSUSBScale.statusRequestCmd);
        assertThat(data[0]).isEqualTo(9);
        assertThat(new String(stream.toByteArray())).isEqualTo("MEG");
    }

    @Test
    public void directIOReportConfig() throws DLSException {
        DLSUSBScale scale = new DLSUSBScale();
        scale.port = _port;
        scale.scaleConfig = mock(DLSScaleConfig.class);
        when(_port.sendData(DLSUSBScale.reportScaleConfigCmd)).then(invocation -> {
            scale.bReportResp = true;
            scale.dataBuf = new byte[]{0, 1, 2, 74, 111, 101};
            return null;
        });
        int[] data = new int[]{9};
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        scale.directIO(DLSJposConst.DIO_SCALE_REPORT_CONFIG, data, stream);
        assertThat(data[0]).isEqualTo(0);
        assertThat(new String(stream.toByteArray())).isEqualTo("Joe");
    }

    @Test
    public void directIOReportConfigNoData() throws DLSException {
        DLSUSBScale scale = new DLSUSBScale();
        scale.port = _port;
        scale.scaleConfig = mock(DLSScaleConfig.class);
        when(_port.sendData(DLSUSBScale.reportScaleConfigCmd)).then(invocation -> {
            scale.bReportResp = true;
            scale.dataBuf = new byte[]{};
            return null;
        });
        int[] data = new int[]{9};
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        scale.directIO(DLSJposConst.DIO_SCALE_REPORT_CONFIG, data, stream);
        assertThat(data[0]).isEqualTo(1);
    }

    @Test
    public void directIOScaleDevProtocol() throws DLSException {
        DLSUSBScale scale = new DLSUSBScale();
        int[] data = new int[]{9};
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        scale.directIO(DLSJposConst.DIO_DEV_PROTOCOL, data, stream);
        assertThat(data[0]).isEqualTo(0);
        assertThat(new String(stream.toByteArray())).isEqualTo("USB");
    }
    //endregion

    @Test
    public void disable() throws DLSException {
        DLSUSBScale scale = new DLSUSBScale();
        scale.setState(DLSState.OPENED);
        scale.setState(DLSState.CLAIMED);
        scale.setState(DLSState.ENABLED);
        scale.disable();
        assertThat(scale.getState()).isEqualTo(DLSState.DISABLED);
    }

    @Test
    public void doHealthCheck() {
        DLSUSBScale scale = new DLSUSBScale();
        scale.port = _port;
        when(_port.sendData(DLSUSBScale.statusRequestCmd)).then(invocation -> {
            scale.bStatusResp = true;
            scale.bDeviceReady = true;
            return null;
        });
        assertThat(scale.doHealthCheck(12)).isTrue();
    }

    @Test
    public void doSelfTestWhileUpgrading() {
        DLSUSBScale scale = new DLSUSBScale();
        scale.port = _port;
        DLSJposConst.updateInProgress = true;
        when(_port.sendData(DLSUSBScale.performSelfTestCmd)).then(invocation -> {
            scale.bStatusResp = true;
            return null;
        });
        scale.doSelfTest();
        verify(_port, times(0)).sendData(DLSUSBScale.performSelfTestCmd);
    }

    @Test
    public void doSelfTest() {
        DLSUSBScale scale = new DLSUSBScale();
        scale.port = _port;
        when(_port.sendData(DLSUSBScale.performSelfTestCmd)).then(invocation -> {
            scale.bStatusResp = true;
            return null;
        });
        scale.doSelfTest();
        verify(_port, times(1)).sendData(DLSUSBScale.performSelfTestCmd);
    }

    @Test
    public void enableWhileUpgrading() throws DLSException {
        DLSUSBScale scale = new DLSUSBScale();
        DLSJposConst.updateInProgress = true;
        scale.setState(DLSState.OPENED);
        scale.setState(DLSState.CLAIMED);
        scale.enable();
        assertThat(scale.getState()).isEqualTo(DLSState.CLAIMED);
    }

    @Test
    public void enable() throws DLSException {
        DLSUSBScale scale = new DLSUSBScale();
        scale.setState(DLSState.OPENED);
        scale.setState(DLSState.CLAIMED);
        scale.enable();
        assertThat(scale.getState()).isEqualTo(DLSState.ENABLED);
    }

    @Test
    public void enable3ByteStatusWhileUpgrading() {
        DLSUSBScale scale = new DLSUSBScale();
        scale.port = _port;
        DLSJposConst.updateInProgress = true;
        when(_port.sendData(DLSUSBScale.enable3ByteStatusCmd)).then(invocation -> {
            scale.bStatusResp = true;
            return null;
        });
        scale.enable3ByteStatus();
        verify(_port, times(0)).sendData(DLSUSBScale.enable3ByteStatusCmd);
    }

    @Test
    public void enable3ByteStatus() {
        DLSUSBScale scale = new DLSUSBScale();
        scale.port = _port;
        when(_port.sendData(DLSUSBScale.enable3ByteStatusCmd)).then(invocation -> {
            scale.bStatusResp = true;
            return null;
        });
        scale.enable3ByteStatus();
        verify(_port, times(1)).sendData(DLSUSBScale.enable3ByteStatusCmd);
    }

    @Test
    public void fireWeightEvent5Digits() {
        DLSUSBScale scale = new DLSUSBScale();
        WeightReceivedListener listener = mock(WeightReceivedListener.class);
        scale.addWeightReceivedListener(listener);
        byte[] data = new byte[]{0, 1, 2, 3, 4, 5, 6, 7};
        scale.fireWeightEvent(DLSUSBScale.RESP_FIVE_DIGIT, data);
        verify(listener, times(1)).onWeightReceived(34567);
    }

    @Test
    public void fireWeightEvent4Digits() {
        DLSUSBScale scale = new DLSUSBScale();
        WeightReceivedListener listener = mock(WeightReceivedListener.class);
        scale.addWeightReceivedListener(listener);
        byte[] data = new byte[]{0, 1, 2, 3, 4, 5, 6};
        scale.fireWeightEvent(5, data);
        verify(listener, times(1)).onWeightReceived(34560);
    }

    @Test
    public void getStatistics() throws DLSException {
        DLSUSBScale scale = new DLSUSBScale();
        scale.context = _context;
        DLSStatistics statistics = mock(DLSStatistics.class);
        when(statistics.getParserName(DLSJposConst.DLS_S_INTERFACE)).thenReturn("Interface");
        try (MockedStatic<DLSStatistics> stats = Mockito.mockStatic(DLSStatistics.class)) {
            stats.when(() -> DLSStatistics.getInstance(_context)).thenReturn(statistics);
            Map<String, Object> result = scale.getStatistics();
            assertThat(result.size()).isEqualTo(3);
            assertThat(result.get(DLSJposConst.DLS_S_DEVICE_CATEGORY)).isEqualTo("Scale");
            assertThat(result.get(DLSJposConst.DLS_S_GOOD_WEIGHT_READ_COUNT)).isEqualTo("NA");
            assertThat(result.get("Interface")).isEqualTo("USB");
        }
    }

    @Test
    public void hasStatisticsReporting() {
        DLSUSBScale scale = new DLSUSBScale();
        scale.scaleConfig = mock(DLSScaleConfig.class);
        when(scale.scaleConfig.getCanAcceptStatisticsCmd()).thenReturn(true);
        assertThat(scale.hasStatisticsReporting()).isTrue();
        when(scale.scaleConfig.getCanAcceptStatisticsCmd()).thenReturn(false);
        assertThat(scale.hasStatisticsReporting()).isFalse();
    }

    @Test
    public void isAliveWhileUpgrading() {
        DLSUSBScale scale = new DLSUSBScale();
        DLSJposConst.updateInProgress = true;
        assertThat(scale.isAlive()).isFalse();
    }

    @Test
    public void isAlive() {
        DLSUSBScale scale = new DLSUSBScale();
        scale.port = _port;
        when(_port.sendData(DLSUSBScale.statusRequestCmd)).then(invocation -> {
            scale.bStatusResp = true;
            scale.bDeviceReady = true;
            return null;
        });
        assertThat(scale.isAlive()).isTrue();
        when(_port.sendData(DLSUSBScale.statusRequestCmd)).then(invocation -> {
            scale.bStatusResp = true;
            scale.bDeviceReady = false;
            return null;
        });
        assertThat(scale.isAlive()).isFalse();
    }

    //region On Data Received

    @Test
    public void onDataReceivedNull() {
        DLSUSBScale scale = new DLSUSBScale();
        scale.onDataReceived(null, 1);
    }

    @Test
    public void onDataReceivedVoid() {
        DLSUSBScale scale = new DLSUSBScale();
        scale.onDataReceived(new byte[]{}, 1);
    }

    @Test
    public void onDataReceivedZeroLength() {
        DLSUSBScale scale = new DLSUSBScale();
        scale.onDataReceived(new byte[]{1}, 0);
    }

    @Test
    public void onDataReceivedRespNotReady() {
        DLSUSBScale scale = new DLSUSBScale();
        byte[] data = new byte[]{(byte) 0X80, 0x00, 0x00};
        scale.onDataReceived(data, data.length);
        assertThat(scale.bDeviceReady).isFalse();
        assertThat(scale.bStatusResp).isTrue();
    }

    @Test
    public void onDataReceivedConfigSuccess() {
        DLSUSBScale scale = new DLSUSBScale();
        byte[] data = new byte[]{(byte) 0X00, 0x00, 0x01};
        scale.onDataReceived(data, data.length);
        assertThat(scale.bDeviceReady).isTrue();
        assertThat(scale.dataBuf).isEqualTo(data);
        assertThat(scale.bUpdateResp).isTrue();
    }

    @Test
    public void onDataReceivedConfigData() {
        DLSUSBScale scale = new DLSUSBScale();
        byte[] data = new byte[]{(byte) 0X02, 0x00, 0x00};
        scale.onDataReceived(data, data.length);
        assertThat(scale.bDeviceReady).isTrue();
        assertThat(scale.bStatusResp).isTrue();
        assertThat(scale.dataBuf).isEqualTo(data);
        assertThat(scale.bReportResp).isTrue();
    }

    @Test
    public void onDataReceivedRespCmdError() {
        DLSUSBScale scale = new DLSUSBScale();
        DeviceErrorStatusListener listener = mock(DeviceErrorStatusListener.class);
        byte[] data = new byte[]{(byte) 0X40, 0x00, 0x00};
        scale.addDeviceErrorListener(listener);
        scale.onDataReceived(data, data.length);
        verify(listener, times(1)).onDeviceError(DeviceErrorStatusListener.ERR_CMD);
    }

    @Test
    public void onDataReceivedRespDataError() {
        DLSUSBScale scale = new DLSUSBScale();
        DeviceErrorStatusListener listener = mock(DeviceErrorStatusListener.class);
        byte[] data = new byte[]{(byte) 0X00, 0x08, 0x00};
        scale.addDeviceErrorListener(listener);
        scale.onDataReceived(data, data.length);
        verify(listener, times(1)).onDeviceError(DeviceErrorStatusListener.ERR_DATA);
    }

    @Test
    public void onDataReceivedRespReadError() {
        DLSUSBScale scale = new DLSUSBScale();
        DeviceErrorStatusListener listener = mock(DeviceErrorStatusListener.class);
        byte[] data = new byte[]{(byte) 0X00, 0x10, 0x00};
        scale.addDeviceErrorListener(listener);
        scale.onDataReceived(data, data.length);
        verify(listener, times(1)).onDeviceError(DeviceErrorStatusListener.ERR_READ);
    }

    @Test
    public void onDataReceivedRespNoDisplay() {
        DLSUSBScale scale = new DLSUSBScale();
        DeviceErrorStatusListener listener = mock(DeviceErrorStatusListener.class);
        byte[] data = new byte[]{(byte) 0X00, 0x20, 0x00};
        scale.addDeviceErrorListener(listener);
        scale.onDataReceived(data, data.length);
        verify(listener, times(1)).onDeviceError(DeviceErrorStatusListener.ERR_NO_DISPLAY);
    }

    @Test
    public void onDataReceivedRespHWError() {
        DLSUSBScale scale = new DLSUSBScale();
        DeviceErrorStatusListener listener = mock(DeviceErrorStatusListener.class);
        byte[] data = new byte[]{(byte) 0X00, 0x40, 0x00};
        scale.addDeviceErrorListener(listener);
        scale.onDataReceived(data, data.length);
        verify(listener, times(1)).onDeviceError(DeviceErrorStatusListener.ERR_HARDWARE);
    }

    @Test
    public void onDataReceivedRespCmdReject() {
        DLSUSBScale scale = new DLSUSBScale();
        DeviceErrorStatusListener listener = mock(DeviceErrorStatusListener.class);
        byte[] data = new byte[]{0X00, (byte) 0x80, 0x00};
        scale.addDeviceErrorListener(listener);
        scale.onDataReceived(data, data.length);
        verify(listener, times(1)).onDeviceError(DeviceErrorStatusListener.ERR_CMD_REJECT);
    }

    @Test
    public void onDataReceivedRespOverCapacity() {
        DLSUSBScale scale = new DLSUSBScale();
        DeviceErrorStatusListener listener = mock(DeviceErrorStatusListener.class);
        byte[] data = new byte[]{0X00, 0x00, 0x04};
        scale.addDeviceErrorListener(listener);
        scale.onDataReceived(data, data.length);
        verify(listener, times(1)).onDeviceError(DeviceErrorStatusListener.ERR_CAPACITY);
    }

    @Test
    public void onDataReceivedRespUnderZero() {
        DLSUSBScale scale = new DLSUSBScale();
        DeviceErrorStatusListener listener = mock(DeviceErrorStatusListener.class);
        byte[] data = new byte[]{0X00, 0x00, 0x02};
        scale.addDeviceErrorListener(listener);
        scale.onDataReceived(data, data.length);
        verify(listener, times(1)).onDeviceError(DeviceErrorStatusListener.ERR_SCALE_UNDER_ZERO);
    }

    @Test
    public void onDataReceivedRespRequiresZeroing() {
        DLSUSBScale scale = new DLSUSBScale();
        DeviceErrorStatusListener listener = mock(DeviceErrorStatusListener.class);
        byte[] data = new byte[]{0X00, 0x00, 0x10};
        scale.addDeviceErrorListener(listener);
        scale.onDataReceived(data, data.length);
        verify(listener, times(1)).onDeviceError(DeviceErrorStatusListener.ERR_REQUIRES_ZEROING);
    }

    @Test
    public void onDataReceivedRespScaleWarmup() {
        DLSUSBScale scale = new DLSUSBScale();
        DeviceErrorStatusListener listener = mock(DeviceErrorStatusListener.class);
        byte[] data = new byte[]{0X00, 0x00, 0x20};
        scale.addDeviceErrorListener(listener);
        scale.onDataReceived(data, data.length);
        verify(listener, times(1)).onDeviceError(DeviceErrorStatusListener.ERR_WARMUP);
    }

    @Test
    public void onDataReceivedRespDuplicateWeight() {
        DLSUSBScale scale = new DLSUSBScale();
        DeviceErrorStatusListener listener = mock(DeviceErrorStatusListener.class);
        byte[] data = new byte[]{0X00, 0x00, 0x40};
        scale.addDeviceErrorListener(listener);
        scale.onDataReceived(data, data.length);
        verify(listener, times(1)).onDeviceError(DeviceErrorStatusListener.ERR_DUPLICATE);
    }

    @Test
    public void onDataReceivedRespCenterZeroValid() {
        DLSUSBScale scale = new DLSUSBScale();
        DeviceErrorStatusListener listener = mock(DeviceErrorStatusListener.class);
        WeightReceivedListener weightReceivedListener = mock(WeightReceivedListener.class);
        byte[] data = new byte[]{0X00, 0x00, 0x08, 1, 2, 3, 4};
        scale.addDeviceErrorListener(listener);
        scale.addWeightReceivedListener(weightReceivedListener);
        scale.onDataReceived(data, data.length);
        verify(listener, times(0)).onDeviceError(DeviceErrorStatusListener.ERR_SCALE_AT_ZERO);
        verify(weightReceivedListener, times(1)).onWeightReceived(12340);
    }

    @Test
    public void onDataReceivedRespCenterZero() {
        DLSUSBScale scale = new DLSUSBScale();
        scale.bZeroValid = false;
        DeviceErrorStatusListener listener = mock(DeviceErrorStatusListener.class);
        byte[] data = new byte[]{0X00, 0x00, 0x08};
        scale.addDeviceErrorListener(listener);
        scale.onDataReceived(data, data.length);
        verify(listener, times(1)).onDeviceError(DeviceErrorStatusListener.ERR_SCALE_AT_ZERO);
    }

    @Test
    public void onDataReceivedRespNoWeight() {
        DLSUSBScale scale = new DLSUSBScale();
        DeviceErrorStatusListener listener = mock(DeviceErrorStatusListener.class);
        byte[] data = new byte[]{0X00, 0x04, 0x00};
        scale.addDeviceErrorListener(listener);
        scale.onDataReceived(data, data.length);
        verify(listener, times(1)).onDeviceError(DeviceErrorStatusListener.ERR_NO_WEIGHT);
    }

    @Test
    public void onDataReceivedWeight() {
        DLSUSBScale scale = new DLSUSBScale();
        WeightReceivedListener weightReceivedListener = mock(WeightReceivedListener.class);
        byte[] data = new byte[]{0X00, 0x00, 0x00, 1, 2, 3, 4};
        scale.addWeightReceivedListener(weightReceivedListener);
        scale.onDataReceived(data, data.length);
        verify(weightReceivedListener, times(1)).onWeightReceived(12340);
    }

    @Test
    public void onDataReceivedWeightBDioCommand() {
        DLSUSBScale scale = new DLSUSBScale();
        scale.bDioCommand = true;
        WeightReceivedListener weightReceivedListener = mock(WeightReceivedListener.class);
        byte[] data = new byte[]{0X00, 0x00, 0x00, 1, 2, 3, 4};
        scale.addWeightReceivedListener(weightReceivedListener);
        scale.onDataReceived(data, data.length);
        verify(weightReceivedListener, times(0)).onWeightReceived(12340);
        assertThat(scale.bDioCommand).isFalse();
    }

    //endregion

    @Test
    public void onDeviceAdded() {
        DLSUSBScale scale = new DLSUSBScale();
        scale.onDeviceAdded();
    }

    @Test
    public void onDeviceArrival() {
        DLSUSBScale scale = new DLSUSBScale();
        DeviceErrorStatusListener listener = mock(DeviceErrorStatusListener.class);
        scale.addDeviceStatusListener(listener);
        scale.onDeviceArrival();
        verify(listener, times(1)).onDeviceStatus(CommonsConstants.SUE_POWER_ONLINE);
    }

    @Test
    public void onDeviceArrivalAlreadyEnabled() throws DLSException {
        DLSUSBScale scale = new DLSUSBScale();
        DeviceErrorStatusListener listener = mock(DeviceErrorStatusListener.class);
        scale.addDeviceStatusListener(listener);
        scale.port = _port;
        scale.setState(DLSState.OPENED);
        scale.setState(DLSState.CLAIMED);
        scale.setState(DLSState.ENABLED);
        scale.onDeviceArrival();
        verify(listener, times(1)).onDeviceStatus(CommonsConstants.SUE_POWER_ONLINE);
        assertThat(scale.getState()).isEqualTo(DLSState.ENABLED);
    }

    @Test
    public void onDeviceReattached() {
        DLSUSBScale scale = new DLSUSBScale();
        scale.port = _port;
        DeviceErrorStatusListener listener = mock(DeviceErrorStatusListener.class);
        scale.addDeviceStatusListener(listener);
        when(_port.openPort()).thenReturn(true);
        scale.onDeviceReattached();
        verify(listener, times(1)).onDeviceStatus(CommonsConstants.SUE_POWER_ONLINE);
    }

    @Test
    public void onDeviceReattachedError() {
        DLSUSBScale scale = new DLSUSBScale();
        scale.port = _port;
        DeviceErrorStatusListener listener = mock(DeviceErrorStatusListener.class);
        scale.addDeviceErrorListener(listener);
        when(_port.openPort()).thenReturn(false);
        scale.onDeviceReattached();
        verify(listener, times(1)).onDeviceError(DeviceErrorStatusListener.ERR_DEVICE_REATTACHED);
    }

    @Test
    public void onDeviceRemoved() {
        DLSUSBScale scale = new DLSUSBScale();
        scale.port = _port;
        DeviceErrorStatusListener listener = mock(DeviceErrorStatusListener.class);
        scale.addDeviceErrorListener(listener);
        scale.addDeviceStatusListener(listener);
        when(_port.closePort()).thenReturn(true);
        scale.onDeviceRemoved();
        verify(listener, times(1)).onDeviceStatus(CommonsConstants.SUE_POWER_OFF_OFFLINE);
        verify(listener, times(1)).onDeviceError(DeviceErrorStatusListener.ERR_DEVICE_REMOVED);
    }

    @Test
    public void onDeviceRemovedFail() {
        DLSUSBScale scale = new DLSUSBScale();
        scale.port = _port;
        DeviceErrorStatusListener listener = mock(DeviceErrorStatusListener.class);
        scale.addDeviceErrorListener(listener);
        scale.addDeviceStatusListener(listener);
        when(_port.closePort()).thenReturn(false);
        scale.onDeviceRemoved();
        verify(listener, times(1)).onDeviceStatus(CommonsConstants.SUE_POWER_OFF_OFFLINE);
        verify(listener, times(1)).onDeviceError(DeviceErrorStatusListener.ERR_DEVICE_REMOVED);
    }

    @Test
    public void readEnglishWeightWhileUpgrading() throws DLSException {
        DLSUSBScale scale = new DLSUSBScale();
        scale.port = _port;
        DLSJposConst.updateInProgress = true;
        scale.readEnglishWeight();
        verify(_port, times(0)).sendData(DLSUSBScale.englishWeightCmd, DLSUSBScale.englishWeightCmd.length);
    }

    @Test
    public void readEnglishWeight() throws DLSException {
        DLSUSBScale scale = new DLSUSBScale();
        scale.port = _port;
        scale.readEnglishWeight();
        verify(_port, times(1)).sendData(DLSUSBScale.englishWeightCmd, DLSUSBScale.englishWeightCmd.length);
    }

    @Test
    public void readMetricWeightWhileUpgrading() throws DLSException {
        DLSUSBScale scale = new DLSUSBScale();
        scale.port = _port;
        DLSJposConst.updateInProgress = true;
        scale.readMetricWeight();
        verify(_port, times(0)).sendData(DLSUSBScale.metricWeightCmd, DLSUSBScale.metricWeightCmd.length);
    }

    @Test
    public void readMetricWeight() throws DLSException {
        DLSUSBScale scale = new DLSUSBScale();
        scale.port = _port;
        scale.readMetricWeight();
        verify(_port, times(1)).sendData(DLSUSBScale.metricWeightCmd, DLSUSBScale.metricWeightCmd.length);
    }

    //region Read Status Weight
    @Test
    public void readStatusWeightWhileUpgrading() throws DLSException {
        DLSUSBScale scale = new DLSUSBScale();
        DLSJposConst.updateInProgress = true;
        scale.readStatusWeight();
    }

    @Test
    public void readStatusWeightNotLiveWeight() throws DLSException {
        DLSUSBScale scale = new DLSUSBScale();
        scale.readStatusWeight();
    }

    @Test
    public void readStatusWeightTimeout() {
        DLSUSBScale scale = new DLSUSBScale();
        scale.m_lwActive = true;
        scale.port = _port;
        when(_port.sendData(DLSUSBScale.englishWeightCmd)).then(invocation -> {
            scale.bGotStatus = true;
            scale.dataBuf = new byte[]{};
            scale.nLiveResp = DLSUSBScale.RESP_NOT_READY;
            return null;
        });
        try {
            scale.readStatusWeight();
            fail();
        } catch (DLSException e) {
            assertThat(e.getErrorCode()).isEqualTo(DLSJposConst.DLS_E_TIMEOUT);
        }
    }

    @Test
    public void readStatusWeightNotReady() throws DLSException {
        DLSUSBScale scale = new DLSUSBScale();
        scale.port = _port;
        scale.m_lwActive = true;
        when(_port.sendData(DLSUSBScale.englishWeightCmd)).then(invocation -> {
            scale.bGotStatus = true;
            scale.dataBuf = new byte[]{34};
            scale.nLiveResp = DLSUSBScale.RESP_NOT_READY;
            return null;
        });
        DeviceErrorStatusListener listener = mock(DeviceErrorStatusListener.class);
        scale.addDeviceStatusListener(listener);
        scale.readStatusWeight();
        verify(listener, times(1)).onDeviceStatus(ScaleConstants.SCAL_SUE_NOT_READY);
        scale.readStatusWeight();
        verify(listener, times(1)).onDeviceStatus(ScaleConstants.SCAL_SUE_NOT_READY);
        scale.setStatusValue(0);
        scale.bMetricMode = true;
        when(_port.sendData(DLSUSBScale.metricWeightCmd)).then(invocation -> {
            scale.bGotStatus = true;
            scale.dataBuf = new byte[]{34};
            scale.nLiveResp = DLSUSBScale.RESP_NOT_READY;
            return null;
        });
        scale.readStatusWeight();
        verify(listener, times(2)).onDeviceStatus(ScaleConstants.SCAL_SUE_NOT_READY);
        scale.readStatusWeight();
        verify(listener, times(2)).onDeviceStatus(ScaleConstants.SCAL_SUE_NOT_READY);
    }

    @Test
    public void readStatusWeightUnderZero() throws DLSException {
        DLSUSBScale scale = new DLSUSBScale();
        scale.port = _port;
        scale.m_lwActive = true;
        when(_port.sendData(DLSUSBScale.englishWeightCmd)).then(invocation -> {
            scale.bGotStatus = true;
            scale.dataBuf = new byte[]{34};
            scale.nLiveResp = DLSUSBScale.RESP_UNDER_ZERO;
            return null;
        });
        DeviceErrorStatusListener listener = mock(DeviceErrorStatusListener.class);
        scale.addDeviceStatusListener(listener);
        scale.readStatusWeight();
        verify(listener, times(1)).onDeviceStatus(ScaleConstants.SCAL_SUE_WEIGHT_UNDER_ZERO);
        scale.readStatusWeight();
        verify(listener, times(1)).onDeviceStatus(ScaleConstants.SCAL_SUE_WEIGHT_UNDER_ZERO);
        scale.setStatusValue(0);
        scale.bMetricMode = true;
        when(_port.sendData(DLSUSBScale.metricWeightCmd)).then(invocation -> {
            scale.bGotStatus = true;
            scale.dataBuf = new byte[]{34};
            scale.nLiveResp = DLSUSBScale.RESP_UNDER_ZERO;
            return null;
        });
        scale.readStatusWeight();
        verify(listener, times(2)).onDeviceStatus(ScaleConstants.SCAL_SUE_WEIGHT_UNDER_ZERO);
        scale.readStatusWeight();
        verify(listener, times(2)).onDeviceStatus(ScaleConstants.SCAL_SUE_WEIGHT_UNDER_ZERO);
    }

    @Test
    public void readStatusWeightOverCapacity() throws DLSException {
        DLSUSBScale scale = new DLSUSBScale();
        scale.port = _port;
        scale.m_lwActive = true;
        when(_port.sendData(DLSUSBScale.englishWeightCmd)).then(invocation -> {
            scale.bGotStatus = true;
            scale.dataBuf = new byte[]{34};
            scale.nLiveResp = DLSUSBScale.RESP_OVER_CAPACITY;
            return null;
        });
        DeviceErrorStatusListener listener = mock(DeviceErrorStatusListener.class);
        scale.addDeviceStatusListener(listener);
        scale.readStatusWeight();
        verify(listener, times(1)).onDeviceStatus(ScaleConstants.SCAL_SUE_WEIGHT_OVERWEIGHT);
        scale.readStatusWeight();
        verify(listener, times(1)).onDeviceStatus(ScaleConstants.SCAL_SUE_WEIGHT_OVERWEIGHT);
        scale.setStatusValue(0);
        scale.bMetricMode = true;
        when(_port.sendData(DLSUSBScale.metricWeightCmd)).then(invocation -> {
            scale.bGotStatus = true;
            scale.dataBuf = new byte[]{34};
            scale.nLiveResp = DLSUSBScale.RESP_OVER_CAPACITY;
            return null;
        });
        scale.readStatusWeight();
        verify(listener, times(2)).onDeviceStatus(ScaleConstants.SCAL_SUE_WEIGHT_OVERWEIGHT);
        scale.readStatusWeight();
        verify(listener, times(2)).onDeviceStatus(ScaleConstants.SCAL_SUE_WEIGHT_OVERWEIGHT);
    }

    @Test
    public void readStatusWeightNoWeight() throws DLSException {
        DLSUSBScale scale = new DLSUSBScale();
        scale.port = _port;
        scale.m_lwActive = true;
        when(_port.sendData(DLSUSBScale.englishWeightCmd)).then(invocation -> {
            scale.bGotStatus = true;
            scale.dataBuf = new byte[]{34};
            scale.nLiveResp = DLSUSBScale.RESP_NO_WEIGHT;
            return null;
        });
        DeviceErrorStatusListener listener = mock(DeviceErrorStatusListener.class);
        scale.addDeviceStatusListener(listener);
        scale.readStatusWeight();
        verify(listener, times(1)).onDeviceStatus(ScaleConstants.SCAL_SUE_WEIGHT_UNSTABLE);
        scale.readStatusWeight();
        verify(listener, times(1)).onDeviceStatus(ScaleConstants.SCAL_SUE_WEIGHT_UNSTABLE);
        scale.setStatusValue(0);
        scale.bMetricMode = true;
        when(_port.sendData(DLSUSBScale.metricWeightCmd)).then(invocation -> {
            scale.bGotStatus = true;
            scale.dataBuf = new byte[]{34};
            scale.nLiveResp = DLSUSBScale.RESP_NO_WEIGHT;
            return null;
        });
        scale.readStatusWeight();
        verify(listener, times(2)).onDeviceStatus(ScaleConstants.SCAL_SUE_WEIGHT_UNSTABLE);
        scale.readStatusWeight();
        verify(listener, times(2)).onDeviceStatus(ScaleConstants.SCAL_SUE_WEIGHT_UNSTABLE);
    }

    @Test
    public void readStatusWeightCenterOfZero() throws DLSException {
        DLSUSBScale scale = new DLSUSBScale();
        scale.port = _port;
        scale.m_lwActive = true;
        when(_port.sendData(DLSUSBScale.englishWeightCmd)).then(invocation -> {
            scale.bGotStatus = true;
            scale.dataBuf = new byte[]{34};
            scale.nLiveResp = DLSUSBScale.RESP_CENTER_OF_ZERO;
            return null;
        });
        DeviceErrorStatusListener listener = mock(DeviceErrorStatusListener.class);
        scale.addDeviceStatusListener(listener);
        scale.readStatusWeight();
        verify(listener, times(1)).onDeviceStatus(ScaleConstants.SCAL_SUE_WEIGHT_ZERO);
        scale.readStatusWeight();
        verify(listener, times(1)).onDeviceStatus(ScaleConstants.SCAL_SUE_WEIGHT_ZERO);
        scale.setStatusValue(0);
        scale.bMetricMode = true;
        when(_port.sendData(DLSUSBScale.metricWeightCmd)).then(invocation -> {
            scale.bGotStatus = true;
            scale.dataBuf = new byte[]{34};
            scale.nLiveResp = DLSUSBScale.RESP_CENTER_OF_ZERO;
            return null;
        });
        scale.readStatusWeight();
        verify(listener, times(2)).onDeviceStatus(ScaleConstants.SCAL_SUE_WEIGHT_ZERO);
        scale.readStatusWeight();
        verify(listener, times(2)).onDeviceStatus(ScaleConstants.SCAL_SUE_WEIGHT_ZERO);
    }

    @Test
    public void readStatusWeightStableWeight() throws DLSException {
        DLSUSBScale scale = new DLSUSBScale();
        scale.port = _port;
        scale.m_lwActive = true;
        when(_port.sendData(DLSUSBScale.englishWeightCmd)).then(invocation -> {
            scale.bGotStatus = true;
            scale.dataBuf = new byte[]{0, 0, 0, 1, 2, 3, 4};
            scale.nLiveResp = 0;
            return null;
        });
        DeviceErrorStatusListener listener = mock(DeviceErrorStatusListener.class);
        scale.addDeviceStatusListener(listener);
        scale.readStatusWeight();
        verify(listener, times(1)).onDeviceStatus(ScaleConstants.SCAL_SUE_STABLE_WEIGHT);
        scale.readStatusWeight();
        verify(listener, times(1)).onDeviceStatus(ScaleConstants.SCAL_SUE_STABLE_WEIGHT);
        scale.setStatusValue(1);
        scale.bMetricMode = true;
        when(_port.sendData(DLSUSBScale.metricWeightCmd)).then(invocation -> {
            scale.bGotStatus = true;
            scale.dataBuf = new byte[]{0, 0, 0, 1, 2, 3, 4};
            scale.nLiveResp = 0;
            return null;
        });
        scale.readStatusWeight();
        verify(listener, times(2)).onDeviceStatus(ScaleConstants.SCAL_SUE_STABLE_WEIGHT);
        scale.readStatusWeight();
        verify(listener, times(2)).onDeviceStatus(ScaleConstants.SCAL_SUE_STABLE_WEIGHT);
    }

    //endregion

    @Test
    public void reportConfigurationWhileUpgrading() throws DLSException {
        DLSUSBScale scale = new DLSUSBScale();
        DLSJposConst.updateInProgress = true;
        scale.reportConfiguration();
    }

    @Test
    public void resetWhileUpgrading() throws DLSException {
        DLSUSBScale scale = new DLSUSBScale();
        scale.port = _port;
        DLSJposConst.updateInProgress = true;
        scale.reset();
        verify(_port, times(0)).sendData(DLSUSBScale.resetCmd, DLSUSBScale.resetCmd.length);
    }

    @Test
    public void reset() throws DLSException {
        DLSUSBScale scale = new DLSUSBScale();
        scale.port = _port;
        scale.reset();
        verify(_port, times(1)).sendData(DLSUSBScale.resetCmd, DLSUSBScale.resetCmd.length);
    }

    @Test
    public void setMetricMode() {
        DLSUSBScale scale = new DLSUSBScale();
        scale.port = _port;
        scale.scaleConfig = mock(DLSScaleConfig.class);
        when(_port.sendData(scale.configureScaleCmd)).then(invocation -> {
            scale.bUpdateResp = true;
            return null;
        });
        when(scale.scaleConfig.getMetricWeightMode()).thenReturn(true);
        scale.setMetricMode(true);
        assertThat(scale.getMetricMode()).isTrue();
        assertThat(scale.configureScaleCmd[2]).isEqualTo(0x10);
        when(scale.scaleConfig.getMetricWeightMode()).thenReturn(false);
        scale.setMetricMode(false);
        assertThat(scale.configureScaleCmd[2]).isEqualTo(0x00);
        assertThat(scale.getMetricMode()).isFalse();
    }

    @Test
    public void setStatusNotify() {
        DLSUSBScale scale = new DLSUSBScale();
        scale.setStatusNotify(1);
        assertThat(scale.getStatusNotify()).isEqualTo(1);
    }

    //region Update configuration

    @Test
    public void updateConfigurationWhileUpgrading() throws DLSException {
        DLSUSBScale scale = new DLSUSBScale();
        scale.port = _port;
        DLSJposConst.updateInProgress = true;
        scale.scaleConfig = mock(DLSScaleConfig.class);
        scale.updateConfiguration();
        verify(_port, times(0)).sendData((byte[]) any());
    }

    @Test
    public void updateConfiguration() throws DLSException {
        DLSUSBScale scale = new DLSUSBScale();
        scale.port = _port;
        scale.scaleConfig = mock(DLSScaleConfig.class);
        when(_port.sendData(scale.configureScaleCmd)).then(invocation -> {
            scale.bUpdateResp = true;
            return null;
        });
        scale.updateConfiguration();
    }

    @Test
    public void updateConfigurationUKMode() throws DLSException {
        DLSUSBScale scale = new DLSUSBScale();
        scale.port = _port;
        scale.scaleConfig = mock(DLSScaleConfig.class);
        when(scale.scaleConfig.getOperationMode()).thenReturn(1);
        when(_port.sendData(scale.configureScaleCmd)).then(invocation -> {
            assertThat(invocation.getArgument(0, byte[].class)[2]).isEqualTo(1);
            scale.bUpdateResp = true;
            return null;
        });
        scale.updateConfiguration();
    }

    @Test
    public void updateConfigurationDisplayOn() throws DLSException {
        DLSUSBScale scale = new DLSUSBScale();
        scale.port = _port;
        scale.scaleConfig = mock(DLSScaleConfig.class);
        when(scale.scaleConfig.getDisplayRequired()).thenReturn(true);
        when(_port.sendData(scale.configureScaleCmd)).then(invocation -> {
            assertThat(invocation.getArgument(0, byte[].class)[2]).isEqualTo(4);
            scale.bUpdateResp = true;
            return null;
        });
        scale.updateConfiguration();
    }

    @Test
    public void updateConfigurationZeroLED() throws DLSException {
        DLSUSBScale scale = new DLSUSBScale();
        scale.port = _port;
        scale.scaleConfig = mock(DLSScaleConfig.class);
        when(scale.scaleConfig.getIndicateZeroWithLed()).thenReturn(true);
        when(_port.sendData(scale.configureScaleCmd)).then(invocation -> {
            assertThat(invocation.getArgument(0, byte[].class)[2]).isEqualTo(8);
            scale.bUpdateResp = true;
            return null;
        });
        scale.updateConfiguration();
    }

    @Test
    public void updateConfigurationMetricWeight() throws DLSException {
        DLSUSBScale scale = new DLSUSBScale();
        scale.port = _port;
        scale.scaleConfig = mock(DLSScaleConfig.class);
        when(scale.scaleConfig.getMetricWeightMode()).thenReturn(true);
        when(_port.sendData(scale.configureScaleCmd)).then(invocation -> {
            assertThat(invocation.getArgument(0, byte[].class)[2]).isEqualTo(0x10);
            scale.bUpdateResp = true;
            return null;
        });
        scale.updateConfiguration();
    }

    @Test
    public void updateConfigurationEnforceZeroReturn() throws DLSException {
        DLSUSBScale scale = new DLSUSBScale();
        scale.port = _port;
        scale.scaleConfig = mock(DLSScaleConfig.class);
        when(scale.scaleConfig.getEnforceZeroReturn()).thenReturn(true);
        when(_port.sendData(scale.configureScaleCmd)).then(invocation -> {
            assertThat(invocation.getArgument(0, byte[].class)[2]).isEqualTo(0x20);
            scale.bUpdateResp = true;
            return null;
        });
        scale.updateConfiguration();
    }

    @Test
    public void updateConfigurationFiveDigit() throws DLSException {
        DLSUSBScale scale = new DLSUSBScale();
        scale.port = _port;
        scale.scaleConfig = mock(DLSScaleConfig.class);
        when(scale.scaleConfig.getFiveDigitWeight()).thenReturn(true);
        when(_port.sendData(scale.configureScaleCmd)).then(invocation -> {
            assertThat(invocation.getArgument(0, byte[].class)[3]).isEqualTo(0x01);
            scale.bUpdateResp = true;
            return null;
        });
        scale.updateConfiguration();
    }

    @Test
    public void updateConfigurationData() throws DLSException {
        DLSUSBScale scale = new DLSUSBScale();
        scale.port = _port;
        scale.scaleConfig = mock(DLSScaleConfig.class);
        when(_port.sendData(scale.configureScaleCmd)).then(invocation -> {
            scale.bUpdateResp = true;
            scale.dataBuf = new byte[]{10};
            return null;
        });
        when(_port.sendData(DLSUSBScale.statusRequestCmd)).then(invocation -> {
            scale.bStatusResp = true;
            scale.bDeviceReady = true;
            return null;
        });
        scale.updateConfiguration();
    }

    //endregion

    @Test
    public void zeroScaleWhileUpgrading() throws DLSException {
        DLSUSBScale scale = new DLSUSBScale();
        DLSJposConst.updateInProgress = true;
        scale.port = _port;
        scale.zeroScale();
        verify(_port, times(0)).sendData(DLSUSBScale.zeroScaleCmd, DLSUSBScale.zeroScaleCmd.length);
    }

    @Test
    public void zeroScale() throws DLSException {
        DLSUSBScale scale = new DLSUSBScale();
        scale.port = _port;
        scale.zeroScale();
        verify(_port, times(1)).sendData(DLSUSBScale.zeroScaleCmd, DLSUSBScale.zeroScaleCmd.length);
    }

//    @Test
//    public void portOpened() throws DLSException {
//        MockUtils.mockDLSPropertiesSingleton(mock(DLSProperties.class));
//        DLSUSBScale scale = new DLSUSBScale();
//        scale.port = _port;
//        when(_port.sendData(DLSUSBScale.enable3ByteStatusCmd)).then(invocation -> {
//            scale.bStatusResp = true;
//            return null;
//        });
//        when(_port.sendData(DLSUSBScale.reportScaleConfigCmd)).then(invocation -> {
//            scale.bReportResp = true;
//            return null;
//        });
//        scale.setState(DLSState.OPENED);
//        scale.portOpened();
//        verify(_port, times(1)).addDataReceivedListener(scale);
//        verify(_port, times(1)).addDeviceRemovedListener(scale);
//        verify(_port, times(1)).addDeviceReattachedListener(scale);
//        assertThat(scale.getState()).isEqualTo(DLSState.CLAIMED);
//        MockUtils.cleanDLSProperties();
//    }
//
//    @Test
//    public void portOpenedWrongState(){
//        MockUtils.mockDLSPropertiesSingleton(mock(DLSProperties.class));
//        DLSUSBScale scale = new DLSUSBScale();
//        scale.port = _port;
//        when(_port.sendData(DLSUSBScale.enable3ByteStatusCmd)).then(invocation -> {
//            scale.bStatusResp = true;
//            return null;
//        });
//        when(_port.sendData(DLSUSBScale.reportScaleConfigCmd)).then(invocation -> {
//            scale.bReportResp = true;
//            return null;
//        });
//        scale.portOpened();
//        verify(_port, times(1)).addDataReceivedListener(scale);
//        verify(_port, times(1)).addDeviceRemovedListener(scale);
//        verify(_port, times(1)).addDeviceReattachedListener(scale);
//        assertThat(scale.getState()).isEqualTo(DLSState.CLOSED);
//        MockUtils.cleanDLSProperties();
//    }
//
//    @Test
//    public void portClosed() {
//        DLSUSBScale scale = new DLSUSBScale();
//        scale.portClosed();
//    }
//
//    @Test
//    public void portError() {
//        DLSUSBScale scale = new DLSUSBScale();
//        scale.portError(UsbPortStatusListener.ErrorStatus.CAN_NOT_FIND_COM_PORT);
//    }

}