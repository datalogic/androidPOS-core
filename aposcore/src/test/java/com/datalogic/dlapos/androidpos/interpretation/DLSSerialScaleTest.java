package com.datalogic.dlapos.androidpos.interpretation;

import android.content.Context;

import com.datalogic.dlapos.androidpos.common.DLSDeviceInfo;
import com.datalogic.dlapos.androidpos.common.DLSException;
import com.datalogic.dlapos.androidpos.common.DLSJposConst;
import com.datalogic.dlapos.androidpos.common.DLSObjectFactory;
import com.datalogic.dlapos.androidpos.common.DLSProperties;
import com.datalogic.dlapos.androidpos.common.DLSScaleConfig;
import com.datalogic.dlapos.androidpos.common.DLSState;
import com.datalogic.dlapos.androidpos.transport.DLSUsbPort;
import com.datalogic.dlapos.androidpos.utils.MockUtils;
import com.datalogic.dlapos.commons.constant.ScaleConstants;
import com.datalogic.dlapos.commons.service.ScaleBaseService;
import com.datalogic.dlapos.confighelper.DLAPosConfigHelper;
import com.datalogic.dlapos.confighelper.configurations.accessor.IhsHelper;
import com.datalogic.dlapos.confighelper.configurations.accessor.ProfileManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.io.ByteArrayOutputStream;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DLSSerialScaleTest {

    @Mock
    private final DLSUsbPort _port = mock(DLSUsbPort.class);

    @Mock
    private final DLSDeviceInfo _info = mock(DLSDeviceInfo.class);

    @Mock
    private final Context _context = mock(Context.class);

    @Before
    public void setup() {

    }

    @After
    public void clear() {
        DLSJposConst.updateInProgress = false;
    }

    @Test
    public void canZeroScale() {
        assertThat(new DLSSerialScale().canZeroScale()).isTrue();
    }

    //region Claim
//    @Test
//    public void claim() throws DLSException {
//        DLSSerialScale scale = new DLSSerialScale();
//        scale.deviceInfo = _info;
//        scale.context = _context;
//        scale.setState(DLSState.OPENED);
//        when(_port.openPort()).thenReturn(true);
//        try (MockedStatic<DLSObjectFactory> factoryMockedStatic = Mockito.mockStatic(DLSObjectFactory.class)) {
//            factoryMockedStatic.when(() -> DLSObjectFactory.createPort(_info, _context)).thenReturn(_port);
//            scale.claim(100);
//            verify(_port, times(1)).registerPortStatusListener(scale);
//            verify(_port, times(1)).openPort();
//        }
//    }
//
//    @Test
//    public void claimNotNullPort() throws DLSException {
//        DLSSerialScale scale = new DLSSerialScale();
//        scale.deviceInfo = _info;
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
//            factoryMockedStatic.when(() -> DLSObjectFactory.createPort(_info, _context)).thenReturn(_port);
//            scale.claim(100);
//            verify(_port, times(1)).closePort();
//            verify(_port, times(1)).registerPortStatusListener(scale);
//            verify(_port, times(1)).openPort();
//        }
//    }
//
//    @Test(expected = DLSException.class)
//    public void claimExceptionOnOpenPort() throws DLSException {
//        DLSSerialScale scale = new DLSSerialScale();
//        scale.deviceInfo = _info;
//        scale.context = _context;
//        scale.setState(DLSState.OPENED);
//        when(_port.openPort()).thenReturn(false);
//        try (MockedStatic<DLSObjectFactory> factoryMockedStatic = Mockito.mockStatic(DLSObjectFactory.class)) {
//            factoryMockedStatic.when(() -> DLSObjectFactory.createPort(_info, _context)).thenReturn(_port);
//            scale.claim(100);
//        }
//    }

    //endregion

    @Test
    public void clearDisplay() {
        new DLSSerialScale().clearDisplay();
    }

    @Test
    public void detectResetFinish() {
        assertThat(new DLSSerialScale().detectResetFinish()).isTrue();
    }

    //region DirectIO

    @Test
    public void directIoWhileUpgrade() throws DLSException {
        DLSJposConst.updateInProgress = true;
        DLSSerialScale scale = new DLSSerialScale();
        scale.port = _port;
        int[] data = new int[]{};
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        scale.directIO(DLSJposConst.DIO_SCALE_STATUS, data, stream);
        verify(_port, times(0)).sendData(DLSSerialScale.GET_WEIGHT_CMD);
    }

    @Test
    public void directIOScaleStatusWeight() throws DLSException {
        DLSSerialScale scale = new DLSSerialScale();
        scale.port = _port;
        when(_port.sendData(DLSSerialScale.GET_WEIGHT_CMD)).then(invocation -> {
            scale.dataBuf = "S12T".getBytes();
            return null;
        });
        int[] data = new int[]{};
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        scale.directIO(DLSJposConst.DIO_SCALE_STATUS, data, stream);
        assertThat(new String(stream.toByteArray())).isEqualTo("Weight - 12");
        verify(_port, times(1)).sendData(DLSSerialScale.GET_WEIGHT_CMD);
    }

    @Test
    public void directIOScaleStatusUnderZero() throws DLSException {
        DLSSerialScale scale = new DLSSerialScale();
        scale.port = _port;
        when(_port.sendData(DLSSerialScale.GET_WEIGHT_CMD)).then(invocation -> {
            scale.dataBuf = new byte[]{2, 63, DLSJposConst.SCALE_STATUS_UNDER_ZERO, 3};
            return null;
        });
        int[] data = new int[]{};
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        scale.directIO(DLSJposConst.DIO_SCALE_STATUS, data, stream);
        assertThat(new String(stream.toByteArray())).isEqualTo("Status, under zero");
        verify(_port, times(1)).sendData(DLSSerialScale.GET_WEIGHT_CMD);
    }

    @Test
    public void directIOScaleStatusOverWeight() throws DLSException {
        DLSSerialScale scale = new DLSSerialScale();
        scale.port = _port;
        when(_port.sendData(DLSSerialScale.GET_WEIGHT_CMD)).then(invocation -> {
            scale.dataBuf = new byte[]{2, 63, DLSJposConst.SCALE_STATUS_RANGE, 3};
            return null;
        });
        int[] data = new int[]{};
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        scale.directIO(DLSJposConst.DIO_SCALE_STATUS, data, stream);
        assertThat(new String(stream.toByteArray())).isEqualTo("Status, overweight");
        verify(_port, times(1)).sendData(DLSSerialScale.GET_WEIGHT_CMD);
    }

    @Test
    public void directIOScaleStatusMotion() throws DLSException {
        DLSSerialScale scale = new DLSSerialScale();
        scale.port = _port;
        when(_port.sendData(DLSSerialScale.GET_WEIGHT_CMD)).then(invocation -> {
            scale.dataBuf = new byte[]{2, 63, DLSJposConst.SCALE_STATUS_MOTION, 3};
            return null;
        });
        int[] data = new int[]{};
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        scale.directIO(DLSJposConst.DIO_SCALE_STATUS, data, stream);
        assertThat(new String(stream.toByteArray())).isEqualTo("Status, in motion");
        verify(_port, times(1)).sendData(DLSSerialScale.GET_WEIGHT_CMD);
    }

    @Test
    public void directIOScaleStatusOutSideZero() throws DLSException {
        DLSSerialScale scale = new DLSSerialScale();
        scale.port = _port;
        when(_port.sendData(DLSSerialScale.GET_WEIGHT_CMD)).then(invocation -> {
            scale.dataBuf = new byte[]{2, 63, DLSJposConst.SCALE_STATUS_OUTSIDE_ZERO, 3};
            return null;
        });
        int[] data = new int[]{};
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        scale.directIO(DLSJposConst.DIO_SCALE_STATUS, data, stream);
        assertThat(new String(stream.toByteArray())).isEqualTo("Status, out of zero capture range");
        verify(_port, times(1)).sendData(DLSSerialScale.GET_WEIGHT_CMD);
    }

    @Test
    public void directIOScaleStatusZeroWeight() throws DLSException {
        DLSSerialScale scale = new DLSSerialScale();
        scale.port = _port;
        when(_port.sendData(DLSSerialScale.GET_WEIGHT_CMD)).then(invocation -> {
            scale.dataBuf = new byte[]{2, 63, DLSJposConst.SCALE_STATUS_CENTEROF_ZERO, 3};
            return null;
        });
        int[] data = new int[]{};
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        scale.directIO(DLSJposConst.DIO_SCALE_STATUS, data, stream);
        assertThat(new String(stream.toByteArray())).isEqualTo("Status, zero weight");
        verify(_port, times(1)).sendData(DLSSerialScale.GET_WEIGHT_CMD);
    }

    @Test
    public void directIOSelfTestAllPassed() throws DLSException {
        DLSSerialScale scale = new DLSSerialScale();
        scale.port = _port;
        when(_port.sendData(DLSSerialScale.INITIATE_CONFIDENCE_TEST_CMD)).then(invocation -> {
            scale.dataBuf = new byte[]{2, 63, DLSJposConst.SCALE_STATUS_CENTEROF_ZERO, 3};
            return null;
        });
        when(_port.sendData(DLSSerialScale.GET_CONFIDENCE_TEST_RESULTS_CMD)).then(invocation -> {
            scale.bConfidenceTest = false;
            scale.confidenceByte = 0x1f;
            scale.dataBuf = new byte[]{2, 63, DLSJposConst.SCALE_STATUS_CENTEROF_ZERO, 3};
            return null;
        });
        int[] data = new int[]{};
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        scale.directIO(DLSJposConst.DIO_SCALE_SELF_TEST, data, stream);
        assertThat(new String(stream.toByteArray())).isEqualTo("Selftest - processor passed, ROM passed, RAM passed, EEPROM1 passed, EEPROM2 passed");
        verify(_port, times(1)).sendData(DLSSerialScale.INITIATE_CONFIDENCE_TEST_CMD);
        verify(_port, times(1)).sendData(DLSSerialScale.GET_CONFIDENCE_TEST_RESULTS_CMD);
    }

    @Test
    public void directIOSelfTestNotPassed() throws DLSException {
        DLSSerialScale scale = new DLSSerialScale();
        scale.port = _port;
        when(_port.sendData(DLSSerialScale.INITIATE_CONFIDENCE_TEST_CMD)).then(invocation -> {
            scale.dataBuf = new byte[]{2, 63, DLSJposConst.SCALE_STATUS_CENTEROF_ZERO, 3};
            return null;
        });
        when(_port.sendData(DLSSerialScale.GET_CONFIDENCE_TEST_RESULTS_CMD)).then(invocation -> {
            scale.bConfidenceTest = false;
            scale.confidenceByte = 0x07;
            scale.dataBuf = new byte[]{2, 63, DLSJposConst.SCALE_STATUS_CENTEROF_ZERO, 3};
            return null;
        });
        int[] data = new int[]{};
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        scale.directIO(DLSJposConst.DIO_SCALE_SELF_TEST, data, stream);
        assertThat(new String(stream.toByteArray())).isEqualTo("Device not responding");
        verify(_port, times(1)).sendData(DLSSerialScale.INITIATE_CONFIDENCE_TEST_CMD);
        verify(_port, times(1)).sendData(DLSSerialScale.GET_CONFIDENCE_TEST_RESULTS_CMD);
    }

    @Test
    public void directIODevProtocol() throws DLSException {
        DLSSerialScale scale = new DLSSerialScale();
        scale.port = _port;
        int[] data = new int[]{};
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        scale.directIO(DLSJposConst.DIO_DEV_PROTOCOL, data, stream);
        assertThat(new String(stream.toByteArray())).isEqualTo("RS232 - SASI");
    }
    //endregion

    @Test
    public void disable() throws DLSException {
        DLSSerialScale scale = new DLSSerialScale();
        scale.setState(DLSState.OPENED);
        scale.setState(DLSState.CLAIMED);
        scale.setState(DLSState.ENABLED);
        scale.disable();
        assertThat(scale.getState()).isEqualTo(DLSState.DISABLED);
    }

    @Test(expected = DLSException.class)
    public void disableNotEnabled() throws DLSException {
        DLSSerialScale scale = new DLSSerialScale();
        scale.disable();
        assertThat(scale.getState()).isEqualTo(DLSState.DISABLED);
    }

    //region DoHealthCheck
    @Test
    public void doHealthCheck() throws DLSException {
        DLSSerialScale scale = new DLSSerialScale();
        scale.port = _port;
        when(_port.sendData(DLSSerialScale.INITIATE_CONFIDENCE_TEST_CMD)).then(invocation -> {
            scale.dataBuf = new byte[]{2, 63, DLSJposConst.SCALE_STATUS_CENTEROF_ZERO, 3};
            return null;
        });
        when(_port.sendData(DLSSerialScale.GET_CONFIDENCE_TEST_RESULTS_CMD)).then(invocation -> {
            scale.bConfidenceTest = false;
            scale.confidenceByte = 0x1f;
            scale.dataBuf = new byte[]{2, 63, DLSJposConst.SCALE_STATUS_CENTEROF_ZERO, 3};
            return null;
        });
        assertThat(scale.doHealthCheck(100));
        verify(_port, times(1)).sendData(DLSSerialScale.INITIATE_CONFIDENCE_TEST_CMD);
        verify(_port, times(1)).sendData(DLSSerialScale.GET_CONFIDENCE_TEST_RESULTS_CMD);
    }

    @Test
    public void doHealthCheckEEPROM1() {
        DLSSerialScale scale = new DLSSerialScale();
        scale.port = _port;
        when(_port.sendData(DLSSerialScale.INITIATE_CONFIDENCE_TEST_CMD)).then(invocation -> {
            scale.dataBuf = new byte[]{2, 63, DLSJposConst.SCALE_STATUS_CENTEROF_ZERO, 3};
            return null;
        });
        when(_port.sendData(DLSSerialScale.GET_CONFIDENCE_TEST_RESULTS_CMD)).then(invocation -> {
            scale.bConfidenceTest = false;
            scale.confidenceByte = 0x10;
            scale.dataBuf = new byte[]{2, 63, DLSJposConst.SCALE_STATUS_CENTEROF_ZERO, 3};
            return null;
        });
        try {
            scale.doHealthCheck(100);
            fail();
        } catch (DLSException e) {
            verify(_port, times(1)).sendData(DLSSerialScale.INITIATE_CONFIDENCE_TEST_CMD);
            verify(_port, times(1)).sendData(DLSSerialScale.GET_CONFIDENCE_TEST_RESULTS_CMD);
            assertThat(e.getLocalizedMessage()).contains("EEPROM 1");
        }
    }

    @Test
    public void doHealthCheckEEPROM2() {
        DLSSerialScale scale = new DLSSerialScale();
        scale.port = _port;
        when(_port.sendData(DLSSerialScale.INITIATE_CONFIDENCE_TEST_CMD)).then(invocation -> {
            scale.dataBuf = new byte[]{2, 63, DLSJposConst.SCALE_STATUS_CENTEROF_ZERO, 3};
            return null;
        });
        when(_port.sendData(DLSSerialScale.GET_CONFIDENCE_TEST_RESULTS_CMD)).then(invocation -> {
            scale.bConfidenceTest = false;
            scale.confidenceByte = 0x01;
            scale.dataBuf = new byte[]{2, 63, DLSJposConst.SCALE_STATUS_CENTEROF_ZERO, 3};
            return null;
        });
        try {
            scale.doHealthCheck(100);
            fail();
        } catch (DLSException e) {
            verify(_port, times(1)).sendData(DLSSerialScale.INITIATE_CONFIDENCE_TEST_CMD);
            verify(_port, times(1)).sendData(DLSSerialScale.GET_CONFIDENCE_TEST_RESULTS_CMD);
            assertThat(e.getLocalizedMessage()).contains("EEPROM 2");
        }
    }

    @Test
    public void doHealthCheckRAM() {
        DLSSerialScale scale = new DLSSerialScale();
        scale.port = _port;
        when(_port.sendData(DLSSerialScale.INITIATE_CONFIDENCE_TEST_CMD)).then(invocation -> {
            scale.dataBuf = new byte[]{2, 63, DLSJposConst.SCALE_STATUS_CENTEROF_ZERO, 3};
            return null;
        });
        when(_port.sendData(DLSSerialScale.GET_CONFIDENCE_TEST_RESULTS_CMD)).then(invocation -> {
            scale.bConfidenceTest = false;
            scale.confidenceByte = 0x03;
            scale.dataBuf = new byte[]{2, 63, DLSJposConst.SCALE_STATUS_CENTEROF_ZERO, 3};
            return null;
        });
        try {
            scale.doHealthCheck(100);
            fail();
        } catch (DLSException e) {
            verify(_port, times(1)).sendData(DLSSerialScale.INITIATE_CONFIDENCE_TEST_CMD);
            verify(_port, times(1)).sendData(DLSSerialScale.GET_CONFIDENCE_TEST_RESULTS_CMD);
            assertThat(e.getLocalizedMessage()).contains("RAM");
            assertThat(e.getLocalizedMessage()).doesNotContain("Processor");
        }
    }

    @Test
    public void doHealthCheckProcessorRAM() {
        DLSSerialScale scale = new DLSSerialScale();
        scale.port = _port;
        when(_port.sendData(DLSSerialScale.INITIATE_CONFIDENCE_TEST_CMD)).then(invocation -> {
            scale.dataBuf = new byte[]{2, 63, DLSJposConst.SCALE_STATUS_CENTEROF_ZERO, 3};
            return null;
        });
        when(_port.sendData(DLSSerialScale.GET_CONFIDENCE_TEST_RESULTS_CMD)).then(invocation -> {
            scale.bConfidenceTest = false;
            scale.confidenceByte = 0x07;
            scale.dataBuf = new byte[]{2, 63, DLSJposConst.SCALE_STATUS_CENTEROF_ZERO, 3};
            return null;
        });
        try {
            scale.doHealthCheck(100);
            fail();
        } catch (DLSException e) {
            verify(_port, times(1)).sendData(DLSSerialScale.INITIATE_CONFIDENCE_TEST_CMD);
            verify(_port, times(1)).sendData(DLSSerialScale.GET_CONFIDENCE_TEST_RESULTS_CMD);
            assertThat(e.getLocalizedMessage()).contains("Processor");
        }
    }

    @Test
    public void doHealthCheckROM() {
        DLSSerialScale scale = new DLSSerialScale();
        scale.port = _port;
        when(_port.sendData(DLSSerialScale.INITIATE_CONFIDENCE_TEST_CMD)).then(invocation -> {
            scale.dataBuf = new byte[]{2, 63, DLSJposConst.SCALE_STATUS_CENTEROF_ZERO, 3};
            return null;
        });
        when(_port.sendData(DLSSerialScale.GET_CONFIDENCE_TEST_RESULTS_CMD)).then(invocation -> {
            scale.bConfidenceTest = false;
            scale.confidenceByte = 0x0f;
            scale.dataBuf = new byte[]{2, 63, DLSJposConst.SCALE_STATUS_CENTEROF_ZERO, 3};
            return null;
        });
        try {
            scale.doHealthCheck(100);
            fail();
        } catch (DLSException e) {
            verify(_port, times(1)).sendData(DLSSerialScale.INITIATE_CONFIDENCE_TEST_CMD);
            verify(_port, times(1)).sendData(DLSSerialScale.GET_CONFIDENCE_TEST_RESULTS_CMD);
            assertThat(e.getLocalizedMessage()).contains("ROM Test");
        }
    }
    //endregion

    //region DoSelfTest
    @Test
    public void doSelfTestWhileUpgrade() {
        DLSSerialScale scale = new DLSSerialScale();
        scale.port = _port;
        when(_port.sendData(DLSSerialScale.INITIATE_CONFIDENCE_TEST_CMD)).then(invocation -> {
            scale.dataBuf = new byte[]{2, 63, DLSJposConst.SCALE_STATUS_CENTEROF_ZERO, 3};
            return null;
        });
        when(_port.sendData(DLSSerialScale.GET_CONFIDENCE_TEST_RESULTS_CMD)).then(invocation -> {
            scale.bConfidenceTest = false;
            scale.confidenceByte = 0x1f;
            scale.dataBuf = new byte[]{2, 63, DLSJposConst.SCALE_STATUS_CENTEROF_ZERO, 3};
            return null;
        });
        DLSJposConst.updateInProgress = true;
        scale.doSelfTest();
        verify(_port, times(0)).sendData(DLSSerialScale.INITIATE_CONFIDENCE_TEST_CMD);
        verify(_port, times(0)).sendData(DLSSerialScale.GET_CONFIDENCE_TEST_RESULTS_CMD);
    }

    @Test
    public void doSelfTest() {
        DLSSerialScale scale = new DLSSerialScale();
        DeviceErrorStatusListener listener = mock(DeviceErrorStatusListener.class);
        scale.addDeviceErrorListener(listener);
        scale.port = _port;
        when(_port.sendData(DLSSerialScale.INITIATE_CONFIDENCE_TEST_CMD)).then(invocation -> {
            scale.dataBuf = new byte[]{2, 63, DLSJposConst.SCALE_STATUS_CENTEROF_ZERO, 3};
            return null;
        });
        when(_port.sendData(DLSSerialScale.GET_CONFIDENCE_TEST_RESULTS_CMD)).then(invocation -> {
            scale.bConfidenceTest = false;
            scale.confidenceByte = 0x1f;
            scale.dataBuf = new byte[]{2, 63, DLSJposConst.SCALE_STATUS_CENTEROF_ZERO, 3};
            return null;
        });
        scale.doSelfTest();
        assertThat(scale.bAlive).isTrue();
        verify(_port, times(1)).sendData(DLSSerialScale.INITIATE_CONFIDENCE_TEST_CMD);
        verify(_port, times(1)).sendData(DLSSerialScale.GET_CONFIDENCE_TEST_RESULTS_CMD);
        verify(listener, times(0)).onDeviceError(DeviceErrorStatusListener.ERR_DEVICE_REMOVED);
    }

    @Test
    public void doSelfTestRemoved() {
        DLSSerialScale scale = new DLSSerialScale();
        DeviceErrorStatusListener listener = mock(DeviceErrorStatusListener.class);
        scale.addDeviceErrorListener(listener);
        scale.port = _port;
        when(_port.sendData(DLSSerialScale.INITIATE_CONFIDENCE_TEST_CMD)).then(invocation -> {
            scale.dataBuf = new byte[]{2, 63, DLSJposConst.SCALE_STATUS_CENTEROF_ZERO, 3};
            return null;
        });
        when(_port.sendData(DLSSerialScale.GET_CONFIDENCE_TEST_RESULTS_CMD)).then(invocation -> {
            scale.bConfidenceTest = false;
            scale.confidenceByte = 0x0;
            scale.dataBuf = new byte[]{2, 63, DLSJposConst.SCALE_STATUS_CENTEROF_ZERO, 3};
            return null;
        });
        scale.doSelfTest();
        assertThat(scale.bAlive).isFalse();
        verify(_port, times(1)).sendData(DLSSerialScale.INITIATE_CONFIDENCE_TEST_CMD);
        verify(_port, times(1)).sendData(DLSSerialScale.GET_CONFIDENCE_TEST_RESULTS_CMD);
        verify(listener, times(1)).onDeviceError(DeviceErrorStatusListener.ERR_DEVICE_REMOVED);
    }
    //endregion

    @Test
    public void enableWhileUpgrading() throws DLSException {
        DLSSerialScale scale = new DLSSerialScale();
        DLSJposConst.updateInProgress = true;
        scale.nStatusNotify = ScaleConstants.SCAL_SN_DISABLED;
        scale.enable();
        assertThat(scale.getState()).isEqualTo(DLSState.CLOSED);
    }

    @Test(expected = DLSException.class)
    public void enableWrongState() throws DLSException {
        DLSSerialScale scale = new DLSSerialScale();
        scale.nStatusNotify = ScaleConstants.SCAL_SN_DISABLED;
        scale.enable();
    }

    @Test
    public void enable() throws DLSException {
        DLSSerialScale scale = new DLSSerialScale();
        scale.setState(DLSState.OPENED);
        scale.setState(DLSState.CLAIMED);
        scale.nStatusNotify = ScaleConstants.SCAL_SN_DISABLED;
        scale.enable();
        assertThat(scale.getState()).isEqualTo(DLSState.ENABLED);
    }

//    @Test
//    public void getStatistics() throws DLSException {
//        MockUtils.mockConfigHelperSingleton(_dlaPosConfigHelper, _profileManager);
//        when(_dlaPosConfigHelper.getIhsHelper()).thenReturn(_ihsHelper);
//        DLSSerialScale scale = new DLSSerialScale();
//        scale.context = _context;
//        when(_ihsHelper.getFieldName("I", IhsHelper.FrameType.INFORMATION)).thenReturn("Interface");
//        HashMap<String, Object> result = scale.getStatistics();
//        assertThat(result).isNotEmpty();
//        assertThat(result.get(DLSJposConst.DLS_S_DEVICE_CATEGORY)).isEqualTo("Scale");
//        assertThat(result.get("Interface")).isEqualTo("RS232");
//        MockUtils.cleanConfigHelper();
//    }

    //region OnDataReceived
    @Test
    public void onDataReceivedWeightNoCommandChar() {
        DLSSerialScale scale = new DLSSerialScale();
        WeightReceivedListener listener = mock(WeightReceivedListener.class);
        scale.addWeightReceivedListener(listener);
        scale.deviceInfo = _info;
        scale.scaleConfig = mock(DLSScaleConfig.class);
        when(_info.getRxTrailer()).thenReturn((byte) 3);
        when(_info.getRxPrefix()).thenReturn((byte) 2);
        byte[] data = new byte[]{2, 49, 3};
        scale.onDataReceived(data, data.length);
        verify(listener, times(1)).onWeightReceived(1);

        data = new byte[]{2, 49, 49, 53, 3};
        scale.onDataReceived(data, data.length);
        verify(listener, times(1)).onWeightReceived(115);
        assertThat(scale.messageList.isEmpty()).isTrue();

        data = new byte[]{2, 49, 49, 53, 46, 3};
        scale.onDataReceived(data, data.length);
        verify(listener, times(1)).onWeightReceived(1150);
        assertThat(scale.messageList.isEmpty()).isTrue();

    }

    @Test
    public void onDataReceivedNonNumericalWeightNoCommandChar() {
        DLSSerialScale scale = new DLSSerialScale();
        WeightReceivedListener listener = mock(WeightReceivedListener.class);
        scale.addWeightReceivedListener(listener);
        scale.deviceInfo = _info;
        scale.scaleConfig = mock(DLSScaleConfig.class);
        when(_info.getRxTrailer()).thenReturn((byte) 3);
        when(_info.getRxPrefix()).thenReturn((byte) 2);
        byte[] data = new byte[]{2, 77, 3};
        scale.onDataReceived(data, data.length);
        verify(listener, times(0)).onWeightReceived(anyInt());
    }

    @Test
    public void onDataReceivedNoDataWeightNoCommandChar() {
        DLSSerialScale scale = new DLSSerialScale();
        WeightReceivedListener listener = mock(WeightReceivedListener.class);
        scale.addWeightReceivedListener(listener);
        scale.deviceInfo = _info;
        scale.scaleConfig = mock(DLSScaleConfig.class);
        when(_info.getRxTrailer()).thenReturn((byte) 3);
        when(_info.getRxPrefix()).thenReturn((byte) 2);
        byte[] data = new byte[]{2, 3};
        scale.onDataReceived(data, data.length);
        verify(listener, times(0)).onWeightReceived(anyInt());
    }

    @Test
    public void onDataReceivedZeroNotValidWeightNoCommandChar() {
        DLSSerialScale scale = new DLSSerialScale();
        WeightReceivedListener listener = mock(WeightReceivedListener.class);
        DeviceErrorStatusListener errorStatusListener = mock(DeviceErrorStatusListener.class);
        scale.addWeightReceivedListener(listener);
        scale.addDeviceErrorListener(errorStatusListener);
        scale.deviceInfo = _info;
        scale.scaleConfig = mock(DLSScaleConfig.class);
        when(_info.getRxTrailer()).thenReturn((byte) 3);
        when(_info.getRxPrefix()).thenReturn((byte) 2);
        byte[] data = new byte[]{2, 48, 3};
        scale.onDataReceived(data, data.length);
        verify(listener, times(0)).onWeightReceived(anyInt());
        verify(errorStatusListener, times(1)).onDeviceError(DeviceErrorStatusListener.ERR_SCALE_AT_ZERO);
    }

    @Test
    public void onDataReceivedZeroValidWeightNoCommandChar() {
        DLSSerialScale scale = new DLSSerialScale();
        WeightReceivedListener listener = mock(WeightReceivedListener.class);
        DeviceErrorStatusListener errorStatusListener = mock(DeviceErrorStatusListener.class);
        scale.addWeightReceivedListener(listener);
        scale.addDeviceErrorListener(errorStatusListener);
        scale.deviceInfo = _info;
        scale.scaleConfig = mock(DLSScaleConfig.class);
        when(scale.scaleConfig.getZeroValid()).thenReturn(true);
        when(_info.getRxTrailer()).thenReturn((byte) 3);
        when(_info.getRxPrefix()).thenReturn((byte) 2);
        byte[] data = new byte[]{2, 48, 3};
        scale.onDataReceived(data, data.length);
        verify(listener, times(1)).onWeightReceived(0);
    }

    @Test
    public void onDataReceivedZeroValidWeightCommandChar() {
        DLSSerialScale scale = new DLSSerialScale();
        WeightReceivedListener listener = mock(WeightReceivedListener.class);
        DeviceErrorStatusListener errorStatusListener = mock(DeviceErrorStatusListener.class);
        scale.addWeightReceivedListener(listener);
        scale.addDeviceErrorListener(errorStatusListener);
        scale.deviceInfo = _info;
        scale.scaleConfig = mock(DLSScaleConfig.class);
        when(scale.scaleConfig.getZeroValid()).thenReturn(true);
        when(_info.getRxTrailer()).thenReturn((byte) 3);
        when(_info.getRxPrefix()).thenReturn((byte) 2);
        byte[] data = new byte[]{2, 63, 48, 3};
        scale.onDataReceived(data, data.length);
        verify(listener, times(1)).onWeightReceived(0);
    }

    @Test
    public void onDataReceivedZeroNotValidWeightCommandChar() {
        DLSSerialScale scale = new DLSSerialScale();
        WeightReceivedListener listener = mock(WeightReceivedListener.class);
        DeviceErrorStatusListener errorStatusListener = mock(DeviceErrorStatusListener.class);
        scale.addWeightReceivedListener(listener);
        scale.addDeviceErrorListener(errorStatusListener);
        scale.deviceInfo = _info;
        scale.scaleConfig = mock(DLSScaleConfig.class);
        when(_info.getRxTrailer()).thenReturn((byte) 3);
        when(_info.getRxPrefix()).thenReturn((byte) 2);
        byte[] data = new byte[]{2, 63, 48, 3};
        scale.onDataReceived(data, data.length);
        verify(listener, times(0)).onWeightReceived(anyInt());
        verify(errorStatusListener, times(1)).onDeviceError(DeviceErrorStatusListener.ERR_SCALE_AT_ZERO);
    }

    @Test
    public void onDataReceivedNoWeight() {
        DLSSerialScale scale = new DLSSerialScale();
        WeightReceivedListener listener = mock(WeightReceivedListener.class);
        DeviceErrorStatusListener errorStatusListener = mock(DeviceErrorStatusListener.class);
        scale.addWeightReceivedListener(listener);
        scale.addDeviceErrorListener(errorStatusListener);
        scale.deviceInfo = _info;
        scale.scaleConfig = mock(DLSScaleConfig.class);
        when(_info.getRxTrailer()).thenReturn((byte) 3);
        when(_info.getRxPrefix()).thenReturn((byte) 2);
        byte[] data = new byte[]{2, 63, DLSJposConst.SCALE_STATUS_MOTION, 3};
        scale.onDataReceived(data, data.length);
        verify(listener, times(0)).onWeightReceived(anyInt());
        verify(errorStatusListener, times(1)).onDeviceError(DeviceErrorStatusListener.ERR_NO_WEIGHT);
    }

    @Test
    public void onDataReceivedCapacityError() {
        DLSSerialScale scale = new DLSSerialScale();
        WeightReceivedListener listener = mock(WeightReceivedListener.class);
        DeviceErrorStatusListener errorStatusListener = mock(DeviceErrorStatusListener.class);
        scale.addWeightReceivedListener(listener);
        scale.addDeviceErrorListener(errorStatusListener);
        scale.deviceInfo = _info;
        scale.scaleConfig = mock(DLSScaleConfig.class);
        when(_info.getRxTrailer()).thenReturn((byte) 3);
        when(_info.getRxPrefix()).thenReturn((byte) 2);
        byte[] data = new byte[]{2, 63, DLSJposConst.SCALE_STATUS_RANGE, 3};
        scale.onDataReceived(data, data.length);
        verify(listener, times(0)).onWeightReceived(anyInt());
        verify(errorStatusListener, times(1)).onDeviceError(DeviceErrorStatusListener.ERR_CAPACITY);
    }

    @Test
    public void onDataReceivedUnderZero() {
        DLSSerialScale scale = new DLSSerialScale();
        WeightReceivedListener listener = mock(WeightReceivedListener.class);
        DeviceErrorStatusListener errorStatusListener = mock(DeviceErrorStatusListener.class);
        scale.addWeightReceivedListener(listener);
        scale.addDeviceErrorListener(errorStatusListener);
        scale.deviceInfo = _info;
        scale.scaleConfig = mock(DLSScaleConfig.class);
        when(_info.getRxTrailer()).thenReturn((byte) 3);
        when(_info.getRxPrefix()).thenReturn((byte) 2);
        byte[] data = new byte[]{2, 63, DLSJposConst.SCALE_STATUS_UNDER_ZERO, 3};
        scale.onDataReceived(data, data.length);
        verify(listener, times(0)).onWeightReceived(anyInt());
        verify(errorStatusListener, times(1)).onDeviceError(DeviceErrorStatusListener.ERR_SCALE_UNDER_ZERO);
    }

    @Test
    public void onDataReceivedConfidenceTest() {
        DLSSerialScale scale = new DLSSerialScale();
        WeightReceivedListener listener = mock(WeightReceivedListener.class);
        DeviceErrorStatusListener errorStatusListener = mock(DeviceErrorStatusListener.class);
        scale.addWeightReceivedListener(listener);
        scale.addDeviceErrorListener(errorStatusListener);
        scale.deviceInfo = _info;
        scale.scaleConfig = mock(DLSScaleConfig.class);
        scale.bConfidenceTest = true;
        when(_info.getRxTrailer()).thenReturn((byte) 3);
        when(_info.getRxPrefix()).thenReturn((byte) 2);
        byte[] data = new byte[]{2, 63, 118, 3};
        scale.onDataReceived(data, data.length);
        verify(listener, times(0)).onWeightReceived(anyInt());
        verify(errorStatusListener, times(0)).onDeviceError(anyInt());
        assertThat(scale.bConfidenceTest).isFalse();
        assertThat(scale.confidenceByte).isEqualTo(118);
    }

    //endregion

    @Test
    public void readEnglishWeight() {
        DLSSerialScale scale = new DLSSerialScale();
        scale.port = _port;
        scale.readEnglishWeight();
        verify(_port, times(1)).sendData(DLSSerialScale.GET_WEIGHT_CMD);
        assertThat(scale.bGetWeight).isTrue();
    }

    @Test
    public void readEnglishWeightWhileUpgrading() {
        DLSSerialScale scale = new DLSSerialScale();
        DLSJposConst.updateInProgress = true;
        scale.port = _port;
        scale.readEnglishWeight();
        verify(_port, times(0)).sendData(DLSSerialScale.GET_WEIGHT_CMD);
        assertThat(scale.bGetWeight).isFalse();
    }

    @Test
    public void readMetricWeight() {
        DLSSerialScale scale = new DLSSerialScale();
        scale.port = _port;
        scale.readMetricWeight();
        verify(_port, times(1)).sendData(DLSSerialScale.GET_WEIGHT_CMD);
        assertThat(scale.bGetWeight).isTrue();
    }

    @Test
    public void readMetricWeightWhileUpgrading() {
        DLSSerialScale scale = new DLSSerialScale();
        DLSJposConst.updateInProgress = true;
        scale.port = _port;
        scale.readMetricWeight();
        verify(_port, times(0)).sendData(DLSSerialScale.GET_WEIGHT_CMD);
        assertThat(scale.bGetWeight).isFalse();
    }

    //region ReadStatusWeight
    @Test
    public void readStatusWeightWhileUpgrading() throws DLSException {
        DLSSerialScale scale = new DLSSerialScale();
        scale.port = _port;
        DLSJposConst.updateInProgress = true;
        DeviceErrorStatusListener listener = mock(DeviceErrorStatusListener.class);
        scale.addDeviceStatusListener(listener);
        when(_port.sendData(DLSSerialScale.GET_WEIGHT_CMD)).then(invocation -> {
            scale.dataBuf = new byte[]{2, 49, 51, 3};
            return null;
        });
        scale.readStatusWeight();
        assertThat(scale.getLiveWeight()).isEqualTo(0);
        assertThat(scale.getStatusValue()).isEqualTo(ScaleConstants.SCAL_SUE_STABLE_WEIGHT);
        verify(listener, times(0)).onDeviceStatus(ScaleConstants.SCAL_SUE_STABLE_WEIGHT);
    }

    @Test
    public void readStatusWeightInvalid() {
        DLSSerialScale scale = new DLSSerialScale();
        scale.port = _port;
        DeviceErrorStatusListener listener = mock(DeviceErrorStatusListener.class);
        scale.addDeviceStatusListener(listener);
        when(_port.sendData(DLSSerialScale.GET_WEIGHT_CMD)).then(invocation -> {
            scale.dataBuf = new byte[]{2, 3};
            return null;
        });
        try {
            scale.readStatusWeight();
            fail();
        } catch (DLSException e) {
            assertThat(e.getErrorCode()).isEqualTo(DLSJposConst.DLS_E_HARDWARE);
        }
    }

    @Test
    public void readStatusWeight() throws DLSException {
        DLSSerialScale scale = new DLSSerialScale();
        scale.port = _port;
        DeviceErrorStatusListener listener = mock(DeviceErrorStatusListener.class);
        scale.addDeviceStatusListener(listener);
        when(_port.sendData(DLSSerialScale.GET_WEIGHT_CMD)).then(invocation -> {
            scale.dataBuf = new byte[]{2, 49, 51, 3};
            return null;
        });
        scale.readStatusWeight();
        assertThat(scale.getLiveWeight()).isEqualTo(13);
        assertThat(scale.getStatusValue()).isEqualTo(ScaleConstants.SCAL_SUE_STABLE_WEIGHT);
        verify(listener, times(1)).onDeviceStatus(ScaleConstants.SCAL_SUE_STABLE_WEIGHT);
    }

    @Test
    public void readStatusWeightWithComma() throws DLSException {
        DLSSerialScale scale = new DLSSerialScale();
        scale.port = _port;
        DeviceErrorStatusListener listener = mock(DeviceErrorStatusListener.class);
        scale.addDeviceStatusListener(listener);
        when(_port.sendData(DLSSerialScale.GET_WEIGHT_CMD)).then(invocation -> {
            scale.dataBuf = new byte[]{2, 49, 51, 49, 46, 51, 3};
            return null;
        });
        scale.readStatusWeight();
        assertThat(scale.getLiveWeight()).isEqualTo(13130);
        assertThat(scale.getStatusValue()).isEqualTo(ScaleConstants.SCAL_SUE_STABLE_WEIGHT);
        verify(listener, times(1)).onDeviceStatus(ScaleConstants.SCAL_SUE_STABLE_WEIGHT);
    }

    @Test
    public void readStatusWeightUnderZero() throws DLSException {
        DLSSerialScale scale = new DLSSerialScale();
        scale.port = _port;
        DeviceErrorStatusListener listener = mock(DeviceErrorStatusListener.class);
        scale.addDeviceStatusListener(listener);
        when(_port.sendData(DLSSerialScale.GET_WEIGHT_CMD)).then(invocation -> {
            scale.dataBuf = new byte[]{2, 63, DLSJposConst.SCALE_STATUS_UNDER_ZERO, 3};
            return null;
        });
        scale.readStatusWeight();
        assertThat(scale.getLiveWeight()).isEqualTo(0);
        assertThat(scale.getStatusValue()).isEqualTo(ScaleConstants.SCAL_SUE_WEIGHT_UNDER_ZERO);
        verify(listener, times(1)).onDeviceStatus(ScaleConstants.SCAL_SUE_WEIGHT_UNDER_ZERO);
    }

    @Test
    public void readStatusWeightOverWeight() throws DLSException {
        DLSSerialScale scale = new DLSSerialScale();
        scale.port = _port;
        DeviceErrorStatusListener listener = mock(DeviceErrorStatusListener.class);
        scale.addDeviceStatusListener(listener);
        when(_port.sendData(DLSSerialScale.GET_WEIGHT_CMD)).then(invocation -> {
            scale.dataBuf = new byte[]{2, 63, DLSJposConst.SCALE_STATUS_RANGE, 3};
            return null;
        });
        scale.readStatusWeight();
        assertThat(scale.getLiveWeight()).isEqualTo(0);
        assertThat(scale.getStatusValue()).isEqualTo(ScaleConstants.SCAL_SUE_WEIGHT_OVERWEIGHT);
        verify(listener, times(1)).onDeviceStatus(ScaleConstants.SCAL_SUE_WEIGHT_OVERWEIGHT);
    }

    @Test
    public void readStatusWeightWeightUnstable() throws DLSException {
        DLSSerialScale scale = new DLSSerialScale();
        scale.port = _port;
        DeviceErrorStatusListener listener = mock(DeviceErrorStatusListener.class);
        scale.addDeviceStatusListener(listener);
        when(_port.sendData(DLSSerialScale.GET_WEIGHT_CMD)).then(invocation -> {
            scale.dataBuf = new byte[]{2, 63, DLSJposConst.SCALE_STATUS_MOTION, 3};
            return null;
        });
        scale.readStatusWeight();
        assertThat(scale.getLiveWeight()).isEqualTo(0);
        assertThat(scale.getStatusValue()).isEqualTo(ScaleConstants.SCAL_SUE_WEIGHT_UNSTABLE);
        verify(listener, times(1)).onDeviceStatus(ScaleConstants.SCAL_SUE_WEIGHT_UNSTABLE);
    }

    @Test
    public void readStatusWeightNotReady() throws DLSException {
        DLSSerialScale scale = new DLSSerialScale();
        scale.port = _port;
        DeviceErrorStatusListener listener = mock(DeviceErrorStatusListener.class);
        scale.addDeviceStatusListener(listener);
        when(_port.sendData(DLSSerialScale.GET_WEIGHT_CMD)).then(invocation -> {
            scale.dataBuf = new byte[]{2, 63, DLSJposConst.SCALE_STATUS_OUTSIDE_ZERO, 3};
            return null;
        });
        scale.readStatusWeight();
        assertThat(scale.getLiveWeight()).isEqualTo(0);
        assertThat(scale.getStatusValue()).isEqualTo(ScaleConstants.SCAL_SUE_NOT_READY);
        verify(listener, times(1)).onDeviceStatus(ScaleConstants.SCAL_SUE_NOT_READY);
    }

    @Test
    public void readStatusWeightWeightZero() throws DLSException {
        DLSSerialScale scale = new DLSSerialScale();
        scale.port = _port;
        DeviceErrorStatusListener listener = mock(DeviceErrorStatusListener.class);
        scale.addDeviceStatusListener(listener);
        when(_port.sendData(DLSSerialScale.GET_WEIGHT_CMD)).then(invocation -> {
            scale.dataBuf = new byte[]{2, 63, DLSJposConst.SCALE_STATUS_CENTEROF_ZERO, 3};
            return null;
        });
        scale.readStatusWeight();
        assertThat(scale.getLiveWeight()).isEqualTo(0);
        assertThat(scale.getStatusValue()).isEqualTo(ScaleConstants.SCAL_SUE_WEIGHT_ZERO);
        verify(listener, times(1)).onDeviceStatus(ScaleConstants.SCAL_SUE_WEIGHT_ZERO);
    }

    @Test
    public void readStatusWeightWeightZeroNoCommandChar() throws DLSException {
        DLSSerialScale scale = new DLSSerialScale();
        scale.port = _port;
        DeviceErrorStatusListener listener = mock(DeviceErrorStatusListener.class);
        scale.addDeviceStatusListener(listener);
        when(_port.sendData(DLSSerialScale.GET_WEIGHT_CMD)).then(invocation -> {
            scale.dataBuf = new byte[]{2, 48, 48, 3};
            return null;
        });
        scale.readStatusWeight();
        assertThat(scale.getLiveWeight()).isEqualTo(0);
        assertThat(scale.getStatusValue()).isEqualTo(ScaleConstants.SCAL_SUE_WEIGHT_ZERO);
        verify(listener, times(1)).onDeviceStatus(ScaleConstants.SCAL_SUE_WEIGHT_ZERO);
    }
    //endregion

    @Test
    public void zeroScaleWhileUpgrading() {
        DLSSerialScale scale = new DLSSerialScale();
        scale.port = _port;
        DLSJposConst.updateInProgress = true;
        scale.zeroScale();
        verify(_port, times(0)).sendData(DLSSerialScale.ZERO_SCALE_CMD);
    }

    @Test
    public void zeroScale() {
        DLSSerialScale scale = new DLSSerialScale();
        scale.port = _port;
        scale.zeroScale();
        verify(_port, times(1)).sendData(DLSSerialScale.ZERO_SCALE_CMD);
    }

//    @Test
//    public void portOpened() throws DLSException {
//        DLSSerialScale scale = new DLSSerialScale();
//        scale.port = _port;
//        scale.setState(DLSState.OPENED);
//        MockUtils.mockDLSPropertiesSingleton(mock(DLSProperties.class));
//        scale.portOpened();
//        assertThat(scale.getState()).isEqualTo(DLSState.CLAIMED);
//        verify(_port, times(1)).addDataReceivedListener(scale);
//        verify(_port, times(1)).addDeviceRemovedListener(scale);
//        verify(_port, times(1)).addDeviceReattachedListener(scale);
//        MockUtils.cleanDLSProperties();
//    }
//
//    @Test
//    public void portOpenedWrongState() {
//        DLSSerialScale scale = new DLSSerialScale();
//        scale.port = _port;
//        MockUtils.mockDLSPropertiesSingleton(mock(DLSProperties.class));
//        scale.portOpened();
//        assertThat(scale.getState()).isEqualTo(DLSState.CLOSED);
//        verify(_port, times(0)).addDataReceivedListener(scale);
//        verify(_port, times(0)).addDeviceRemovedListener(scale);
//        verify(_port, times(0)).addDeviceReattachedListener(scale);
//        MockUtils.cleanDLSProperties();
//    }
}