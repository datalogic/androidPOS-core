package com.datalogic.dlapos.androidpos.interpretation;

import android.content.Context;

import com.datalogic.dlapos.androidpos.common.DLSException;
import com.datalogic.dlapos.androidpos.common.DLSJposConst;
import com.datalogic.dlapos.androidpos.common.DLSProperties;
import com.datalogic.dlapos.androidpos.common.DLSScannerConfig;
import com.datalogic.dlapos.androidpos.common.DLSState;
import com.datalogic.dlapos.androidpos.transport.DLSUsbPort;
import com.datalogic.dlapos.androidpos.utils.MockUtils;
import com.datalogic.dlapos.commons.constant.CommonsConstants;
import com.datalogic.dlapos.confighelper.DLAPosConfigHelper;
import com.datalogic.dlapos.confighelper.configurations.accessor.ProfileManager;

import org.junit.Test;
import org.mockito.MockedConstruction;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DLSUSBFlashTest {

    @Test
    public void detectResetFinish() {
        DLSUSBFlash flash = new DLSUSBFlash();
        assertThat(flash.detectResetFinish()).isTrue();
    }

    @Test
    public void directIO() throws DLSException {
        DLSUSBFlash flash = new DLSUSBFlash();
        flash.directIO(1, null, null);
    }

    @Test
    public void disable() throws DLSException {
        DLSUSBFlash flash = new DLSUSBFlash();
        flash.disable();
    }

    @Test
    public void doHealthCheck() throws DLSException {
        DLSUSBFlash flash = new DLSUSBFlash();
        assertThat(flash.doHealthCheck(1)).isTrue();
    }

    @Test
    public void doSelfTest() throws DLSException {
        DLSUSBFlash flash = new DLSUSBFlash();
        flash.doSelfTest();
    }

    @Test
    public void enable() throws DLSException {
        DLSUSBFlash flash = new DLSUSBFlash();
        flash.enable();
    }

    @Test
    public void getConfiguration() {
        DLSUSBFlash flash = new DLSUSBFlash();
        flash.getConfiguration();
    }

    @Test
    public void isAlive() throws DLSException {
        DLSUSBFlash flash = new DLSUSBFlash();
        assertThat(flash.isAlive()).isTrue();
    }

    @Test
    public void onDataReceived() {
        DLSUSBFlash flash = new DLSUSBFlash();
        byte[] buf = new byte[]{13};
        flash.onDataReceived(buf, buf.length);
        assertThat(flash.status = 13);
    }

    @Test
    public void onDeviceAdded() {
        DLSUSBFlash flash = new DLSUSBFlash();
        flash.onDeviceAdded();
    }

    @Test
    public void onDeviceReattached() {
        DLSUSBFlash flash = new DLSUSBFlash();
        flash.port = mock(DLSUsbPort.class);
        DeviceErrorStatusListener listener = mock(DeviceErrorStatusListener.class);
        flash.addDeviceStatusListener(listener);

        flash.onDeviceReattached();
        verify(flash.port, times(1)).openPort();
        verify(listener, times(1)).onDeviceStatus(CommonsConstants.SUE_POWER_ONLINE);
    }

    @Test
    public void onDeviceRemoved() {
        DLSUSBFlash flash = new DLSUSBFlash();
        flash.port = mock(DLSUsbPort.class);
        flash.reattachPending = false;
        DeviceErrorStatusListener listener = mock(DeviceErrorStatusListener.class);
        flash.addDeviceErrorListener(listener);

        flash.onDeviceRemoved();
        verify(flash.port, times(1)).closePort();
        verify(listener, times(1)).onDeviceError(DeviceErrorStatusListener.ERR_DEVICE_REMOVED);
    }

    @Test
    public void open() throws DLSException {
        DLAPosConfigHelper configHelper = mock(DLAPosConfigHelper.class);
        ProfileManager profileManager = mock(ProfileManager.class);
        MockUtils.mockConfigHelperSingleton(configHelper, profileManager);
        DLSProperties properties = mock(DLSProperties.class);
        MockUtils.mockDLSPropertiesSingleton(properties);

        Context _context = mock(Context.class);

        DLSUSBFlash flash = new DLSUSBFlash();
        try (MockedConstruction<DLSScannerConfig> mockedConfig = mockConstruction(DLSScannerConfig.class,
                (mock, context) -> {
                    when(mock.getLogicalName()).thenReturn("I'm the mock");
                })) {
            flash.open("test", _context);
            assertThat(flash.context).isEqualTo(_context);
            assertThat(flash.getLogicalName()).isEqualTo("test");
            assertThat(flash.properties).isEqualTo(properties);
            assertThat(flash.getState()).isEqualTo(DLSState.OPENED);
        }

        MockUtils.cleanConfigHelper();
        MockUtils.cleanDLSProperties();
    }

    @Test
    public void recordTimeout() {
        DLSUSBFlash flash = new DLSUSBFlash();
        flash.setRecordTimeout(1);
        assertThat(flash.getRecordTimeout()).isEqualTo(1);
        flash.setRecordTimeout(14);
        assertThat(flash.getRecordTimeout()).isEqualTo(14);
    }

    @Test
    public void reset() throws DLSException {
        DLSUSBFlash flash = new DLSUSBFlash();
        flash.reset();
    }

    @Test
    public void resetCommand() {
        DLSUSBFlash flash = new DLSUSBFlash();
        flash.port = mock(DLSUsbPort.class);
        flash.resetCommand();
        verify(flash.port, times(1)).sendData(new byte[]{0x00, 0x00, 0x00, 0x40}, 4);
    }

    @Test
    public void sendRecordCommandComplete() {
        DLSUSBFlash flash = new DLSUSBFlash();
        flash.port = mock(DLSUsbPort.class);

        String data = "ABC";
        when(flash.port.sendData(new byte[]{0, 0, 2, 0, 65, 66, 67}, 7)).then(invocation -> {
            flash.responseReceived = true;
            flash.status = (DLSUSBFlash.RESP_COMMAND_COMPLETE | DLSUSBFlash.RESP_FLASH_LOADING);
            return null;
        });

        assertThat(flash.sendRecord(data)).isEqualTo(DLSJposConst.DL_ACK);
    }

    @Test
    public void sendRecordOperationError() {
        DLSUSBFlash flash = new DLSUSBFlash();
        flash.port = mock(DLSUsbPort.class);

        String data = "ABC";
        when(flash.port.sendData(new byte[]{0, 0, 2, 0, 65, 66, 67}, 7)).then(invocation -> {
            flash.responseReceived = true;
            flash.status = DLSUSBFlash.RESP_OPERATION_ERROR;
            return null;
        });

        assertThat(flash.sendRecord(data)).isEqualTo(DLSJposConst.DL_BEL);
    }

    @Test
    public void sendRecordCommandReject() {
        DLSUSBFlash flash = new DLSUSBFlash();
        flash.port = mock(DLSUsbPort.class);

        String data = "ABC";
        when(flash.port.sendData(new byte[]{0, 0, 2, 0, 65, 66, 67}, 7)).then(invocation -> {
            flash.responseReceived = true;
            flash.status = (byte) DLSUSBFlash.RESP_COMMAND_REJECT;
            return null;
        });

        assertThat(flash.sendRecord(data)).isEqualTo(DLSJposConst.DL_NAK);
    }

    @Test
    public void sendRecordInvalid() {
        DLSUSBFlash flash = new DLSUSBFlash();
        flash.port = mock(DLSUsbPort.class);

        String data = "ABC";
        when(flash.port.sendData(new byte[]{0, 0, 2, 0, 65, 66, 67}, 7)).then(invocation -> {
            flash.responseReceived = true;
            flash.status = 0x05;
            return null;
        });

        assertThat(flash.sendRecord(data)).isEqualTo(DLSJposConst.DL_INVALID);
    }

    @Test
    public void startCommandComplete() {
        DLSUSBFlash flash = new DLSUSBFlash();
        flash.port = mock(DLSUsbPort.class);
        when(flash.port.sendData(new byte[]{0, 0, 1, 0})).then(invocation -> {
            flash.responseReceived = true;
            flash.status = (DLSUSBFlash.RESP_COMMAND_COMPLETE | DLSUSBFlash.RESP_FLASH_LOADING);
            return null;
        });
        assertThat(flash.startCommand()).isEqualTo(DLSJposConst.DL_ACK);
    }

    @Test
    public void startCommandOperationError() {
        DLSUSBFlash flash = new DLSUSBFlash();
        flash.port = mock(DLSUsbPort.class);
        when(flash.port.sendData(new byte[]{0, 0, 1, 0})).then(invocation -> {
            flash.responseReceived = true;
            flash.status = DLSUSBFlash.RESP_OPERATION_ERROR;
            return null;
        });
        assertThat(flash.startCommand()).isEqualTo(DLSJposConst.DL_BEL);
    }

    @Test
    public void startCommandReject() {
        DLSUSBFlash flash = new DLSUSBFlash();
        flash.port = mock(DLSUsbPort.class);
        when(flash.port.sendData(new byte[]{0, 0, 1, 0})).then(invocation -> {
            flash.responseReceived = true;
            flash.status = (byte) DLSUSBFlash.RESP_COMMAND_REJECT;
            return null;
        });
        assertThat(flash.startCommand()).isEqualTo(DLSJposConst.DL_NAK);
    }
}