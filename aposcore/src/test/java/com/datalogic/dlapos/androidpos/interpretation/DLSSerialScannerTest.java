package com.datalogic.dlapos.androidpos.interpretation;

import android.content.Context;

import com.datalogic.dlapos.androidpos.common.Constants;
import com.datalogic.dlapos.androidpos.common.DLSDeviceInfo;
import com.datalogic.dlapos.androidpos.common.DLSException;
import com.datalogic.dlapos.androidpos.common.DLSJposConst;
import com.datalogic.dlapos.androidpos.common.DLSObjectFactory;
import com.datalogic.dlapos.androidpos.common.DLSProperties;
import com.datalogic.dlapos.androidpos.common.DLSScannerConfig;
import com.datalogic.dlapos.androidpos.common.DLSState;
import com.datalogic.dlapos.androidpos.common.DLSStatistics;
import com.datalogic.dlapos.androidpos.transport.DLSUsbPort;
import com.datalogic.dlapos.androidpos.utils.MockUtils;
import com.datalogic.dlapos.commons.constant.CommonsConstants;
import com.datalogic.dlapos.confighelper.DLAPosConfigHelper;
import com.datalogic.dlapos.confighelper.configurations.accessor.LabelHelper;
import com.datalogic.dlapos.confighelper.configurations.accessor.LabelIds;
import com.datalogic.dlapos.confighelper.configurations.accessor.ProfileManager;

import org.junit.After;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DLSSerialScannerTest {

    @Mock
    private final Context _context = mock(Context.class);

    @Mock
    private final DLSUsbPort _port = mock(DLSUsbPort.class);

    @Mock
    private final DLSDeviceInfo _deviceInfo = mock(DLSDeviceInfo.class);

    @After
    public void cleanUp() {
        DLSJposConst.updateInProgress = false;
    }

    //region BaudRate
    @Test
    public void changeBaudRate() {
        DLSSerialScanner scanner = new DLSSerialScanner();
        scanner.port = _port;
        scanner.deviceInfo = _deviceInfo;
        scanner.changeBaudRate(2590);
        verify(_port, times(1)).changeBaudRate(2590);
    }

    @Test(expected = IllegalArgumentException.class)
    public void changeBaudRateTooLow() {
        DLSSerialScanner scanner = new DLSSerialScanner();
        scanner.port = _port;
        scanner.deviceInfo = _deviceInfo;
        scanner.changeBaudRate(2399);
    }

    @Test(expected = IllegalArgumentException.class)
    public void changeBaudRateTooHigh() {
        DLSSerialScanner scanner = new DLSSerialScanner();
        scanner.port = _port;
        scanner.deviceInfo = _deviceInfo;
        scanner.changeBaudRate(115201);
    }

    @Test
    public void changeBaudRateSameBaud() {
        DLSSerialScanner scanner = new DLSSerialScanner();
        scanner.port = _port;
        scanner.deviceInfo = _deviceInfo;
        scanner.currentBaud = 2590;
        scanner.changeBaudRate(2590);
        verify(_port, times(0)).changeBaudRate(2590);
    }

    //endregion

    //region Claim
//    @Test
//    public void claim() throws DLSException {
//        DLSSerialScanner scanner = new DLSSerialScanner();
//        scanner.deviceInfo = _deviceInfo;
//        scanner.context = _context;
//        scanner.setState(DLSState.OPENED);
//        when(_port.openPort()).thenReturn(true);
//        try (MockedStatic<DLSObjectFactory> factoryMockedStatic = Mockito.mockStatic(DLSObjectFactory.class)) {
//            factoryMockedStatic.when(() -> DLSObjectFactory.createPort(_deviceInfo, _context)).thenReturn(_port);
//            scanner.claim(100);
//            verify(_port, times(1)).registerPortStatusListener(scanner);
//            verify(_port, times(1)).openPort();
//        }
//    }
//
//    @Test
//    public void claimNotNullPort() throws DLSException {
//        DLSSerialScanner scanner = new DLSSerialScanner();
//        scanner.deviceInfo = _deviceInfo;
//        scanner.context = _context;
//        scanner.port = _port;
//        scanner.setState(DLSState.OPENED);
//        scanner.setState(DLSState.CLAIMED);
//        when(_port.openPort()).thenReturn(true);
//        when(_port.isOpen()).thenReturn(true);
//        when(_port.closePort()).then(invocation -> {
//            when(_port.isOpen()).thenReturn(false);
//            return null;
//        });
//        try (MockedStatic<DLSObjectFactory> factoryMockedStatic = Mockito.mockStatic(DLSObjectFactory.class)) {
//            factoryMockedStatic.when(() -> DLSObjectFactory.createPort(_deviceInfo, _context)).thenReturn(_port);
//            scanner.claim(100);
//            verify(_port, times(1)).closePort();
//            verify(_port, times(1)).registerPortStatusListener(scanner);
//            verify(_port, times(1)).openPort();
//        }
//    }
//
//    @Test(expected = DLSException.class)
//    public void claimExceptionOnOpenPort() throws DLSException {
//        DLSSerialScanner scanner = new DLSSerialScanner();
//        scanner.deviceInfo = _deviceInfo;
//        scanner.context = _context;
//        scanner.setState(DLSState.OPENED);
//        when(_port.openPort()).thenReturn(false);
//        try (MockedStatic<DLSObjectFactory> factoryMockedStatic = Mockito.mockStatic(DLSObjectFactory.class)) {
//            factoryMockedStatic.when(() -> DLSObjectFactory.createPort(_deviceInfo, _context)).thenReturn(_port);
//            scanner.claim(100);
//        }
//    }

    //endregion

    //region DirectIO
    @Test
    public void directIOBeepEnabled() throws DLSException {
        DLSSerialScanner scanner = new DLSSerialScanner();
        scanner.port = _port;
        int[] data = new int[]{};
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        scanner.directIO(DLSJposConst.DIO_SCANNER_BEEP, data, stream);
        verify(_port, times(1)).sendData(DLSSerialScanner.BEEP_IF_BEEP_ENABLED_CMD);
        assertThat(new String(stream.toByteArray())).isEqualTo("Scanner beeped");
    }

    @Test
    public void directIONOFBeep() throws DLSException {
        DLSSerialScanner scanner = new DLSSerialScanner();
        scanner.port = _port;
        int[] data = new int[]{};
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        scanner.directIO(DLSJposConst.DIO_SCANNER_DIO_NOF, data, stream);
        verify(_port, times(1)).sendData(DLSSerialScanner.NOF_BEEP_CMD);
        assertThat(new String(stream.toByteArray())).isEqualTo("Scanner beeped NOF");
    }

    @Test
    public void directIOOpenServicePortSuccess() throws DLSException {
        DLSSerialScanner scanner = new DLSSerialScanner();
        scanner.port = _port;
        when(_port.sendData(DLSSerialScanner.OPEN_SERVICE_PORT_CMD)).then(invocation -> {
            scanner.sohBuf = (DLSSerialScanner.SERVICE_ACCEPT_RESPONSE + "Test").getBytes();
            return null;
        });
        int[] data = new int[]{9};
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        scanner.directIO(DLSJposConst.DIO_OPEN_SERVICE_PORT_HH, data, stream);
        verify(_port, times(1)).sendData(DLSSerialScanner.OPEN_SERVICE_PORT_CMD);
        assertThat(new String(stream.toByteArray())).isEqualTo("Service port opened");
        assertThat(data[0]).isEqualTo(0);
    }

    @Test
    public void directIOOpenServicePortFailure() throws DLSException {
        DLSSerialScanner scanner = new DLSSerialScanner();
        scanner.port = _port;
        when(_port.sendData(DLSSerialScanner.OPEN_SERVICE_PORT_CMD)).then(invocation -> {
            scanner.sohBuf = ("Test").getBytes();
            return null;
        });
        int[] data = new int[]{9};
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        scanner.directIO(DLSJposConst.DIO_OPEN_SERVICE_PORT_HH, data, stream);
        verify(_port, times(1)).sendData(DLSSerialScanner.OPEN_SERVICE_PORT_CMD);
        assertThat(new String(stream.toByteArray())).startsWith("Failed to open service port: ");
        assertThat(data[0]).isEqualTo(1);
    }

    @Test
    public void directIOCloseServicePortSuccess() throws DLSException {
        DLSSerialScanner scanner = new DLSSerialScanner();
        scanner.port = _port;
        when(_port.sendData(DLSSerialScanner.SAVE_AND_CLOSE_SERVICE_PORT_CMD)).then(invocation -> {
            scanner.sohBuf = (DLSSerialScanner.SERVICE_ACCEPT_RESPONSE + "Test").getBytes();
            return null;
        });
        int[] data = new int[]{9};
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        scanner.directIO(DLSJposConst.DIO_CLOSE_SERVICE_PORT_HH, data, stream);
        verify(_port, times(1)).sendData(DLSSerialScanner.SAVE_AND_CLOSE_SERVICE_PORT_CMD);
        assertThat(new String(stream.toByteArray())).isEqualTo("Service port closed");
        assertThat(data[0]).isEqualTo(0);
    }

    @Test
    public void directIOCloseServicePortFailure() throws DLSException {
        DLSSerialScanner scanner = new DLSSerialScanner();
        scanner.port = _port;
        when(_port.sendData(DLSSerialScanner.SAVE_AND_CLOSE_SERVICE_PORT_CMD)).then(invocation -> {
            scanner.sohBuf = ("Test").getBytes();
            return null;
        });
        int[] data = new int[]{9};
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        scanner.directIO(DLSJposConst.DIO_CLOSE_SERVICE_PORT_HH, data, stream);
        verify(_port, times(1)).sendData(DLSSerialScanner.SAVE_AND_CLOSE_SERVICE_PORT_CMD);
        assertThat(new String(stream.toByteArray())).startsWith("Failed to close service port: ");
        assertThat(data[0]).isEqualTo(1);
    }

    @Test
    public void directIOGeneralCommandSuccess() throws DLSException, IOException {
        DLSSerialScanner scanner = new DLSSerialScanner();
        scanner.port = _port;
        when(_port.sendData("COMMAND\r")).then(invocation -> {
            scanner.sohBuf = (DLSSerialScanner.SERVICE_ACCEPT_RESPONSE + "Test").getBytes();
            return null;
        });
        int[] data = new int[]{9};
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        stream.write("COMMAND".getBytes());
        stream.close();
        scanner.directIO(DLSJposConst.DIO_SEND_GEN_SERVICE_CMD_HH, data, stream);
        verify(_port, times(1)).sendData("COMMAND\r");
        assertThat(new String(stream.toByteArray())).startsWith("Command Response: ");
        assertThat(data[0]).isEqualTo(0);
    }

    @Test
    public void directIOGeneralCommandFail() throws DLSException, IOException {
        DLSSerialScanner scanner = new DLSSerialScanner();
        scanner.port = _port;
        when(_port.sendData("COMMAND\r")).then(invocation -> {
            scanner.sohBuf = ("Test").getBytes();
            return null;
        });
        int[] data = new int[]{9};
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        stream.write("COMMAND".getBytes());
        stream.close();
        scanner.directIO(DLSJposConst.DIO_SEND_GEN_SERVICE_CMD_HH, data, stream);
        verify(_port, times(1)).sendData("COMMAND\r");
        assertThat(new String(stream.toByteArray())).startsWith("Command Response: ");
        assertThat(data[0]).isEqualTo(1);
    }

    @Test
    public void directIOReadConfig() throws DLSException, IOException {
        DLSSerialScanner scanner = new DLSSerialScanner();
        scanner.port = _port;
        String fullCMD = DLSSerialScanner.READ_CONFIG_CMD + "COMMAND\r";
        when(_port.sendData(fullCMD)).then(invocation -> {
            scanner.sohBuf = ("Test").getBytes();
            return null;
        });
        int[] data = new int[]{9};
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        stream.write("COMMAND".getBytes());
        stream.close();
        scanner.directIO(DLSJposConst.DIO_READ_CONFIG_ITEM, data, stream);
        verify(_port, times(1)).sendData(fullCMD);
        assertThat(new String(stream.toByteArray())).startsWith("Test");
        assertThat(data[0]).isEqualTo(DLSJposConst.DL_ACK);
    }

    @Test
    public void directIOReadConfigSingleCharResponseBEL() throws DLSException, IOException {
        DLSSerialScanner scanner = new DLSSerialScanner();
        scanner.port = _port;
        String fullCMD = DLSSerialScanner.READ_CONFIG_CMD + "COMMAND\r";
        when(_port.sendData(fullCMD)).then(invocation -> {
            scanner.sohBuf = new byte[]{DLSJposConst.DL_BEL};
            return null;
        });
        int[] data = new int[]{9};
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        stream.write("COMMAND".getBytes());
        stream.close();
        scanner.directIO(DLSJposConst.DIO_READ_CONFIG_ITEM, data, stream);
        verify(_port, times(1)).sendData(fullCMD);
        assertThat(data[0]).isEqualTo(DLSJposConst.DL_BEL);
    }

    @Test
    public void directIOReadConfigSingleCharResponseNAK() throws DLSException, IOException {
        DLSSerialScanner scanner = new DLSSerialScanner();
        scanner.port = _port;
        String fullCMD = DLSSerialScanner.READ_CONFIG_CMD + "COMMAND\r";
        when(_port.sendData(fullCMD)).then(invocation -> {
            scanner.sohBuf = new byte[]{DLSJposConst.DL_NAK};
            return null;
        });
        int[] data = new int[]{9};
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        stream.write("COMMAND".getBytes());
        stream.close();
        scanner.directIO(DLSJposConst.DIO_READ_CONFIG_ITEM, data, stream);
        verify(_port, times(1)).sendData(fullCMD);
        assertThat(data[0]).isEqualTo(DLSJposConst.DL_NAK);
    }

    @Test
    public void directIOWriteConfig() throws DLSException, IOException {
        DLSSerialScanner scanner = new DLSSerialScanner();
        scanner.port = _port;
        String fullCMD = DLSSerialScanner.WRITE_CONFIG_CMD + "0123456\r";
        when(_port.sendData(fullCMD)).then(invocation -> {
            scanner.bDIOWriteConfigInProgress = false;
            scanner.response = DLSJposConst.DL_ACK;
            return null;
        });
        int[] data = new int[]{9};
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        stream.write("0123456".getBytes());
        stream.close();
        scanner.directIO(DLSJposConst.DIO_WRITE_CONFIG_ITEM, data, stream);
        verify(_port, times(1)).sendData(fullCMD);
        assertThat(data[0]).isEqualTo(DLSJposConst.DL_ACK);
    }

    @Test
    public void directIOWriteConfigItemsNotFound() throws DLSException, IOException {
        DLSSerialScanner scanner = new DLSSerialScanner();
        scanner.port = _port;
        String fullCMD = DLSSerialScanner.WRITE_CONFIG_CMD + "0123456\r";
        when(_port.sendData(fullCMD)).then(invocation -> {
            scanner.bDIOWriteConfigInProgress = false;
            scanner.response = DLSJposConst.DL_BEL;
            return null;
        });
        int[] data = new int[]{9};
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        stream.write("0123456".getBytes());
        stream.close();
        scanner.directIO(DLSJposConst.DIO_WRITE_CONFIG_ITEM, data, stream);
        verify(_port, times(1)).sendData(fullCMD);
        assertThat(data[0]).isEqualTo(DLSJposConst.DL_BEL);
    }

    @Test
    public void directIOWriteConfigInvalid() throws DLSException, IOException {
        DLSSerialScanner scanner = new DLSSerialScanner();
        scanner.port = _port;
        String fullCMD = DLSSerialScanner.WRITE_CONFIG_CMD + "0123456\r";
        when(_port.sendData(fullCMD)).then(invocation -> {
            scanner.bDIOWriteConfigInProgress = false;
            scanner.response = DLSJposConst.DL_NAK;
            return null;
        });
        int[] data = new int[]{9};
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        stream.write("0123456".getBytes());
        stream.close();
        scanner.directIO(DLSJposConst.DIO_WRITE_CONFIG_ITEM, data, stream);
        verify(_port, times(1)).sendData(fullCMD);
        assertThat(data[0]).isEqualTo(DLSJposConst.DL_NAK);
    }

    @Test
    public void directIOWriteConfigMalformed() throws DLSException, IOException {
        DLSSerialScanner scanner = new DLSSerialScanner();
        scanner.port = _port;
        String fullCMD = DLSSerialScanner.WRITE_CONFIG_CMD + "0123456\r";
        when(_port.sendData(fullCMD)).then(invocation -> {
            scanner.bDIOWriteConfigInProgress = false;
            scanner.response = DLSJposConst.DL_CAN;
            return null;
        });
        int[] data = new int[]{9};
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        stream.write("0123456".getBytes());
        stream.close();
        scanner.directIO(DLSJposConst.DIO_WRITE_CONFIG_ITEM, data, stream);
        verify(_port, times(1)).sendData(fullCMD);
        assertThat(data[0]).isEqualTo(DLSJposConst.DL_CAN);
    }

    @Test
    public void directIOCommitConfigItems() throws DLSException, IOException {
        DLSSerialScanner scanner = new DLSSerialScanner();
        scanner.port = _port;
        String fullCMD = DLSSerialScanner.COMMIT_CONFIG_CMD + "\r";
        when(_port.sendData(fullCMD)).then(invocation -> {
            scanner.bDIOCommitConfigInProgress = false;
            scanner.response = DLSJposConst.DL_ACK;
            return null;
        });
        int[] data = new int[]{9};
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        stream.write("0123456".getBytes());
        stream.close();
        scanner.directIO(DLSJposConst.DIO_COMMIT_CONFIG_ITEMS, data, stream);
        verify(_port, times(1)).sendData(fullCMD);
        assertThat(data[0]).isEqualTo(DLSJposConst.DL_ACK);
    }

    @Test
    public void directIOCommitConfigItemsFail() throws DLSException, IOException {
        DLSSerialScanner scanner = new DLSSerialScanner();
        scanner.port = _port;
        String fullCMD = DLSSerialScanner.COMMIT_CONFIG_CMD + "\r";
        when(_port.sendData(fullCMD)).then(invocation -> {
            scanner.bDIOCommitConfigInProgress = false;
            scanner.response = DLSJposConst.DL_BEL;
            return null;
        });
        int[] data = new int[]{9};
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        stream.write("0123456".getBytes());
        stream.close();
        scanner.directIO(DLSJposConst.DIO_COMMIT_CONFIG_ITEMS, data, stream);
        verify(_port, times(1)).sendData(fullCMD);
        assertThat(data[0]).isEqualTo(DLSJposConst.DL_BEL);
    }

    @Test
    public void directIOCommitConfigItemsGeneralResponse() throws DLSException, IOException {
        DLSSerialScanner scanner = new DLSSerialScanner();
        scanner.port = _port;
        String fullCMD = DLSSerialScanner.COMMIT_CONFIG_CMD + "\r";
        when(_port.sendData(fullCMD)).then(invocation -> {
            scanner.bDIOCommitConfigInProgress = false;
            scanner.response = 99;
            return null;
        });
        int[] data = new int[]{9};
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        stream.write("0123456".getBytes());
        stream.close();
        scanner.directIO(DLSJposConst.DIO_COMMIT_CONFIG_ITEMS, data, stream);
        verify(_port, times(1)).sendData(fullCMD);
        assertThat(data[0]).isEqualTo(99);
    }

    @Test
    public void directIOPhoneModeControl() throws DLSException {
        DLSSerialScanner scanner = new DLSSerialScanner();
        scanner.port = _port;
        String fullCMD = new String(DLSSerialScanner.cellPhoneModeControl);
        when(_port.sendData(fullCMD)).then(invocation -> {
            scanner.bDIOCPMInProgress = false;
            scanner.response = DLSJposConst.DL_ACK;
            return null;
        });
        int[] data = new int[]{9};
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        scanner.directIO(DLSJposConst.DIO_CELL_PHONE_MODE_CONTROL, data, stream);
        verify(_port, times(1)).sendData(fullCMD);
        assertThat(data[0]).isEqualTo(DLSJposConst.DL_ACK);
    }

    @Test
    public void directIOPhoneModeControlNotConfigured() throws DLSException {
        DLSSerialScanner scanner = new DLSSerialScanner();
        scanner.port = _port;
        String fullCMD = new String(DLSSerialScanner.cellPhoneModeControl);
        when(_port.sendData(fullCMD)).then(invocation -> {
            scanner.bDIOCPMInProgress = false;
            scanner.response = DLSJposConst.DL_BEL;
            return null;
        });
        int[] data = new int[]{9};
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        scanner.directIO(DLSJposConst.DIO_CELL_PHONE_MODE_CONTROL, data, stream);
        verify(_port, times(1)).sendData(fullCMD);
        assertThat(data[0]).isEqualTo(DLSJposConst.DL_BEL);
    }

    @Test
    public void directIOPhoneModeControlTakingPicture() throws DLSException {
        DLSSerialScanner scanner = new DLSSerialScanner();
        scanner.port = _port;
        String fullCMD = new String(DLSSerialScanner.cellPhoneModeControl);
        when(_port.sendData(fullCMD)).then(invocation -> {
            scanner.bDIOCPMInProgress = false;
            scanner.response = DLSJposConst.DL_ETB;
            return null;
        });
        int[] data = new int[]{9};
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        scanner.directIO(DLSJposConst.DIO_CELL_PHONE_MODE_CONTROL, data, stream);
        verify(_port, times(1)).sendData(fullCMD);
        assertThat(data[0]).isEqualTo(DLSJposConst.DL_ETB);
    }

    @Test
    public void directIOPhoneModeControlAlreadyActive() throws DLSException {
        DLSSerialScanner scanner = new DLSSerialScanner();
        scanner.port = _port;
        String fullCMD = new String(DLSSerialScanner.cellPhoneModeControl);
        when(_port.sendData(fullCMD)).then(invocation -> {
            scanner.bDIOCPMInProgress = false;
            scanner.response = DLSJposConst.DL_NAK;
            return null;
        });
        int[] data = new int[]{9};
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        scanner.directIO(DLSJposConst.DIO_CELL_PHONE_MODE_CONTROL, data, stream);
        verify(_port, times(1)).sendData(fullCMD);
        assertThat(data[0]).isEqualTo(DLSJposConst.DL_NAK);
    }

    @Test
    public void directIOPhoneModeControlToggleModeOn() throws DLSException {
        DLSSerialScanner scanner = new DLSSerialScanner();
        scanner.port = _port;
        String fullCMD = new String(DLSSerialScanner.cellPhoneModeControl);
        when(_port.sendData(fullCMD)).then(invocation -> {
            scanner.bDIOCPMInProgress = false;
            scanner.response = 0x54;
            return null;
        });
        int[] data = new int[]{9};
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        scanner.directIO(DLSJposConst.DIO_CELL_PHONE_MODE_CONTROL, data, stream);
        verify(_port, times(1)).sendData(fullCMD);
        assertThat(data[0]).isEqualTo(0x54);
    }

    @Test
    public void directIOPhoneModeControlToggleModeOff() throws DLSException {
        DLSSerialScanner scanner = new DLSSerialScanner();
        scanner.port = _port;
        String fullCMD = new String(DLSSerialScanner.cellPhoneModeControl);
        when(_port.sendData(fullCMD)).then(invocation -> {
            scanner.bDIOCPMInProgress = false;
            scanner.response = 0x74;
            return null;
        });
        int[] data = new int[]{9};
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        scanner.directIO(DLSJposConst.DIO_CELL_PHONE_MODE_CONTROL, data, stream);
        verify(_port, times(1)).sendData(fullCMD);
        assertThat(data[0]).isEqualTo(0x74);
    }

    @Test
    public void directIOCellPhoneEcommEnter() throws DLSException {
        DLSSerialScanner scanner = new DLSSerialScanner();
        int[] data = new int[]{9};
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        scanner.directIO(DLSJposConst.DIO_CELL_PHONE_ECOMM_ENTER, data, stream);
        assertThat(data[0]).isEqualTo(9);
    }

    @Test
    public void directIOCellPhoneEcommExit() throws DLSException {
        DLSSerialScanner scanner = new DLSSerialScanner();
        int[] data = new int[]{9};
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        scanner.directIO(DLSJposConst.DIO_CELL_PHONE_ECOMM_EXIT, data, stream);
        assertThat(data[0]).isEqualTo(9);
    }

    @Test
    public void directIOPhoneScanEnter() throws DLSException {
        DLSSerialScanner scanner = new DLSSerialScanner();
        scanner.port = _port;
        String fullCMD = new String(DLSSerialScanner.cellPhoneModeControl);
        when(_port.sendData(fullCMD)).then(invocation -> {
            scanner.bDIOCPMInProgress = false;
            scanner.response = DLSJposConst.DL_ACK;
            return null;
        });
        int[] data = new int[]{9};
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        scanner.directIO(DLSJposConst.DIO_CELL_PHONE_SCAN_ENTER, data, stream);
        verify(_port, times(1)).sendData(fullCMD);
        assertThat(data[0]).isEqualTo(DLSJposConst.DL_ACK);
    }

    @Test
    public void directIOPhoneScanEnterNotConfigured() throws DLSException {
        DLSSerialScanner scanner = new DLSSerialScanner();
        scanner.port = _port;
        String fullCMD = new String(DLSSerialScanner.cellPhoneModeControl);
        when(_port.sendData(fullCMD)).then(invocation -> {
            scanner.bDIOCPMInProgress = false;
            scanner.response = DLSJposConst.DL_BEL;
            return null;
        });
        int[] data = new int[]{9};
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        scanner.directIO(DLSJposConst.DIO_CELL_PHONE_SCAN_ENTER, data, stream);
        verify(_port, times(1)).sendData(fullCMD);
        assertThat(data[0]).isEqualTo(DLSJposConst.DL_BEL);
    }

    @Test
    public void directIOPhoneScanEnterTakingPicture() throws DLSException {
        DLSSerialScanner scanner = new DLSSerialScanner();
        scanner.port = _port;
        String fullCMD = new String(DLSSerialScanner.cellPhoneModeControl);
        when(_port.sendData(fullCMD)).then(invocation -> {
            scanner.bDIOCPMInProgress = false;
            scanner.response = DLSJposConst.DL_ETB;
            return null;
        });
        int[] data = new int[]{9};
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        scanner.directIO(DLSJposConst.DIO_CELL_PHONE_SCAN_ENTER, data, stream);
        verify(_port, times(1)).sendData(fullCMD);
        assertThat(data[0]).isEqualTo(DLSJposConst.DL_ETB);
    }

    @Test
    public void directIOPhoneScanEnterAlreadyActive() throws DLSException {
        DLSSerialScanner scanner = new DLSSerialScanner();
        scanner.port = _port;
        String fullCMD = new String(DLSSerialScanner.cellPhoneModeControl);
        when(_port.sendData(fullCMD)).then(invocation -> {
            scanner.bDIOCPMInProgress = false;
            scanner.response = DLSJposConst.DL_NAK;
            return null;
        });
        int[] data = new int[]{9};
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        scanner.directIO(DLSJposConst.DIO_CELL_PHONE_SCAN_ENTER, data, stream);
        verify(_port, times(1)).sendData(fullCMD);
        assertThat(data[0]).isEqualTo(DLSJposConst.DL_NAK);
    }

    @Test
    public void directIOPhoneScanEnterToggleModeOn() throws DLSException {
        DLSSerialScanner scanner = new DLSSerialScanner();
        scanner.port = _port;
        String fullCMD = new String(DLSSerialScanner.cellPhoneModeControl);
        when(_port.sendData(fullCMD)).then(invocation -> {
            scanner.bDIOCPMInProgress = false;
            scanner.response = 0x54;
            return null;
        });
        int[] data = new int[]{9};
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        scanner.directIO(DLSJposConst.DIO_CELL_PHONE_SCAN_ENTER, data, stream);
        verify(_port, times(1)).sendData(fullCMD);
        assertThat(data[0]).isEqualTo(DLSJposConst.DL_ACK);
    }

    @Test
    public void directIOPhoneScanEnterToggleModeOff() throws DLSException {
        DLSSerialScanner scanner = new DLSSerialScanner();
        scanner.port = _port;
        String fullCMD = new String(DLSSerialScanner.cellPhoneModeControl);
        when(_port.sendData(fullCMD)).then(invocation -> {
            scanner.bDIOCPMInProgress = false;
            scanner.response = 0x74;
            return null;
        });
        int[] data = new int[]{9};
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        scanner.directIO(DLSJposConst.DIO_CELL_PHONE_SCAN_ENTER, data, stream);
        verify(_port, times(2)).sendData(fullCMD);
        assertThat(data[0]).isEqualTo(DLSJposConst.DL_ACK);
    }

    @Test
    public void directIOPhoneScanExit() throws DLSException {
        DLSSerialScanner scanner = new DLSSerialScanner();
        scanner.port = _port;
        String fullCMD = new String(DLSSerialScanner.cellPhoneModeControl);
        when(_port.sendData(fullCMD)).then(invocation -> {
            scanner.bDIOCPMInProgress = false;
            scanner.response = DLSJposConst.DL_ACK;
            return null;
        });
        int[] data = new int[]{9};
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        scanner.directIO(DLSJposConst.DIO_CELL_PHONE_SCAN_EXIT, data, stream);
        verify(_port, times(1)).sendData(fullCMD);
        assertThat(data[0]).isEqualTo(DLSJposConst.DL_ACK);
    }

    @Test
    public void directIOPhoneScanExitNotConfigured() throws DLSException {
        DLSSerialScanner scanner = new DLSSerialScanner();
        scanner.port = _port;
        String fullCMD = new String(DLSSerialScanner.cellPhoneModeControl);
        when(_port.sendData(fullCMD)).then(invocation -> {
            scanner.bDIOCPMInProgress = false;
            scanner.response = DLSJposConst.DL_BEL;
            return null;
        });
        int[] data = new int[]{9};
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        scanner.directIO(DLSJposConst.DIO_CELL_PHONE_SCAN_EXIT, data, stream);
        verify(_port, times(1)).sendData(fullCMD);
        assertThat(data[0]).isEqualTo(DLSJposConst.DL_BEL);
    }

    @Test
    public void directIOPhoneScanExitTakingPicture() throws DLSException {
        DLSSerialScanner scanner = new DLSSerialScanner();
        scanner.port = _port;
        String fullCMD = new String(DLSSerialScanner.cellPhoneModeControl);
        when(_port.sendData(fullCMD)).then(invocation -> {
            scanner.bDIOCPMInProgress = false;
            scanner.response = DLSJposConst.DL_ETB;
            return null;
        });
        int[] data = new int[]{9};
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        scanner.directIO(DLSJposConst.DIO_CELL_PHONE_SCAN_EXIT, data, stream);
        verify(_port, times(1)).sendData(fullCMD);
        assertThat(data[0]).isEqualTo(DLSJposConst.DL_ETB);
    }

    @Test
    public void directIOPhoneScanExitAlreadyActive() throws DLSException {
        DLSSerialScanner scanner = new DLSSerialScanner();
        scanner.port = _port;
        String fullCMD = new String(DLSSerialScanner.cellPhoneModeControl);
        when(_port.sendData(fullCMD)).then(invocation -> {
            scanner.bDIOCPMInProgress = false;
            scanner.response = DLSJposConst.DL_NAK;
            return null;
        });
        int[] data = new int[]{9};
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        scanner.directIO(DLSJposConst.DIO_CELL_PHONE_SCAN_EXIT, data, stream);
        verify(_port, times(1)).sendData(fullCMD);
        assertThat(data[0]).isEqualTo(DLSJposConst.DL_NAK);
    }

    @Test
    public void directIOPhoneScanExitToggleModeOn() throws DLSException {
        DLSSerialScanner scanner = new DLSSerialScanner();
        scanner.port = _port;
        String fullCMD = new String(DLSSerialScanner.cellPhoneModeControl);
        when(_port.sendData(fullCMD)).then(invocation -> {
            scanner.bDIOCPMInProgress = false;
            scanner.response = 0x54;
            return null;
        });
        int[] data = new int[]{9};
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        scanner.directIO(DLSJposConst.DIO_CELL_PHONE_SCAN_EXIT, data, stream);
        verify(_port, times(2)).sendData(fullCMD);
        assertThat(data[0]).isEqualTo(DLSJposConst.DL_ACK);
    }

    @Test
    public void directIOPhoneScanExitToggleModeOff() throws DLSException {
        DLSSerialScanner scanner = new DLSSerialScanner();
        scanner.port = _port;
        String fullCMD = new String(DLSSerialScanner.cellPhoneModeControl);
        when(_port.sendData(fullCMD)).then(invocation -> {
            scanner.bDIOCPMInProgress = false;
            scanner.response = 0x74;
            return null;
        });
        int[] data = new int[]{9};
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        scanner.directIO(DLSJposConst.DIO_CELL_PHONE_SCAN_EXIT, data, stream);
        verify(_port, times(1)).sendData(fullCMD);
        assertThat(data[0]).isEqualTo(DLSJposConst.DL_ACK);
    }

    @Test
    public void directIOPictureTakingControl() throws DLSException, IOException {
        DLSSerialScanner scanner = new DLSSerialScanner();
        scanner.port = _port;
        String fullCMD = new String(new byte[]{0x50, 0x04, 0x70, 0x30, 0x31, 0x32});
        when(_port.sendData(anyString())).then(invocation -> {
            scanner.bPicDone = true;
            scanner.response = DLSJposConst.DL_ACK;
            return null;
        });
        int[] data = new int[]{9};
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        stream.write("012Test.jpeg".getBytes());
        stream.close();
        scanner.directIO(DLSJposConst.DIO_PICTURE_TAKING_CONTROL, data, stream);
        verify(_port, times(1)).sendData(fullCMD);
        assertThat(data[0]).isEqualTo(DLSJposConst.DL_ACK);
    }

    @Test
    public void directIOPictureTakingControlNotConfigured() throws IOException, DLSException {
        DLSSerialScanner scanner = new DLSSerialScanner();
        scanner.port = _port;
        String fullCMD = new String(new byte[]{0x50, 0x04, 0x70, 0x30, 0x31, 0x32});
        when(_port.sendData(anyString())).then(invocation -> {
            scanner.bPicDone = true;
            scanner.response = DLSJposConst.DL_BEL;
            return null;
        });
        int[] data = new int[]{9};
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        stream.write("012Test.jpeg".getBytes());
        stream.close();
        scanner.directIO(DLSJposConst.DIO_PICTURE_TAKING_CONTROL, data, stream);
        verify(_port, times(1)).sendData(fullCMD);
        assertThat(data[0]).isEqualTo(DLSJposConst.DL_BEL);
    }

    @Test
    public void directIOPictureTakingControlIncomplete() throws IOException, DLSException {
        DLSSerialScanner scanner = new DLSSerialScanner();
        scanner.port = _port;
        String fullCMD = new String(new byte[]{0x50, 0x04, 0x70, 0x30, 0x31, 0x32});
        when(_port.sendData(anyString())).then(invocation -> {
            scanner.bPicDone = true;
            scanner.response = DLSJposConst.DL_NAK;
            return null;
        });
        int[] data = new int[]{9};
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        stream.write("012Test.jpeg".getBytes());
        stream.close();
        scanner.directIO(DLSJposConst.DIO_PICTURE_TAKING_CONTROL, data, stream);
        verify(_port, times(1)).sendData(fullCMD);
        assertThat(data[0]).isEqualTo(DLSJposConst.DL_NAK);
    }

    @Test
    public void directIOPictureTakingControl9x00() throws IOException, DLSException {
        DLSSerialScanner scanner = new DLSSerialScanner();
        scanner.port = _port;
        String fullCMD = new String(new byte[]{0x50, 0x06, 0x70, 0x30, 0x31, 0x32, 0x33, 0x34});
        when(_port.sendData(anyString())).then(invocation -> {
            scanner.bPicDone = true;
            scanner.response = DLSJposConst.DL_ACK;
            return null;
        });
        int[] data = new int[]{9};
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        stream.write("01234Test.jpeg".getBytes());
        stream.close();
        scanner.directIO(DLSJposConst.DIO_PICTURE_TAKING_CONTROL_9X00, data, stream);
        verify(_port, times(1)).sendData(fullCMD);
        assertThat(data[0]).isEqualTo(DLSJposConst.DL_ACK);
    }

    @Test
    public void directIOPictureTakingControl9x00NotConfigured() throws IOException, DLSException {
        DLSSerialScanner scanner = new DLSSerialScanner();
        scanner.port = _port;
        String fullCMD = new String(new byte[]{0x50, 0x06, 0x70, 0x30, 0x31, 0x32, 0x33, 0x34});
        when(_port.sendData(anyString())).then(invocation -> {
            scanner.bPicDone = true;
            scanner.response = DLSJposConst.DL_BEL;
            return null;
        });
        int[] data = new int[]{9};
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        stream.write("01234Test.jpeg".getBytes());
        stream.close();
        scanner.directIO(DLSJposConst.DIO_PICTURE_TAKING_CONTROL_9X00, data, stream);
        verify(_port, times(1)).sendData(fullCMD);
        assertThat(data[0]).isEqualTo(DLSJposConst.DL_BEL);
    }

    @Test
    public void directIOPictureTakingControl9x00Incomplete() throws IOException, DLSException {
        DLSSerialScanner scanner = new DLSSerialScanner();
        scanner.port = _port;
        String fullCMD = new String(new byte[]{0x50, 0x06, 0x70, 0x30, 0x31, 0x32, 0x33, 0x34});
        when(_port.sendData(anyString())).then(invocation -> {
            scanner.bPicDone = true;
            scanner.response = DLSJposConst.DL_NAK;
            return null;
        });
        int[] data = new int[]{9};
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        stream.write("01234Test.jpeg".getBytes());
        stream.close();
        scanner.directIO(DLSJposConst.DIO_PICTURE_TAKING_CONTROL_9X00, data, stream);
        verify(_port, times(1)).sendData(fullCMD);
        assertThat(data[0]).isEqualTo(DLSJposConst.DL_NAK);
    }

    @Test
    public void directIOImageOnNextTriggerNotCreated() throws IOException, DLSException {
        DLSSerialScanner scanner = new DLSSerialScanner();
        scanner.port = _port;
        when(_port.sendData(anyString())).then(invocation -> {
            scanner.bPicDone = true;
            scanner.response = DLSJposConst.DL_ACK;
            return null;
        });
        int[] data = new int[]{9};
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        stream.write("01234Test.jpeg".getBytes());
        stream.close();
        scanner.directIO(DLSJposConst.DIO_IMAGE_ON_NEXT_TRIGGER, data, stream);
        verify(_port, times(1)).sendData(new String(DLSSerialScanner.imagenexttrigger));
        assertThat(data[0]).isEqualTo(9);
        assertThat(new String(stream.toByteArray())).contains("image not created");
    }


    @Test
    public void directIOImageOnNextTrigger() throws IOException, DLSException {
        DLSSerialScanner scanner = new DLSSerialScanner();
        scanner.port = _port;
        when(_port.sendData(anyString())).then(invocation -> {
            scanner.bPicDone = true;
            scanner.picBuf.write("Test".getBytes());
            scanner.response = DLSJposConst.DL_ACK;
            return null;
        });
        int[] data = new int[]{9};
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        stream.write("01234Test.jpeg".getBytes());
        stream.close();
        scanner.directIO(DLSJposConst.DIO_IMAGE_ON_NEXT_TRIGGER, data, stream);
        verify(_port, times(1)).sendData(new String(DLSSerialScanner.imagenexttrigger));
        assertThat(data[0]).isEqualTo(9);
        assertThat(new String(stream.toByteArray())).doesNotContain("image not created");
    }

    @Test
    public void directIOImageOnNextDecodeNotCreated() throws IOException, DLSException {
        DLSSerialScanner scanner = new DLSSerialScanner();
        scanner.port = _port;
        when(_port.sendData(anyString())).then(invocation -> {
            scanner.bPicDone = true;
            scanner.response = DLSJposConst.DL_ACK;
            return null;
        });
        int[] data = new int[]{9};
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        stream.write("01234Test.jpeg".getBytes());
        stream.close();
        scanner.directIO(DLSJposConst.DIO_IMAGE_ON_NEXT_DECODE, data, stream);
        verify(_port, times(1)).sendData(new String(DLSSerialScanner.imageonnextdecode));
        assertThat(data[0]).isEqualTo(9);
        assertThat(new String(stream.toByteArray())).contains("image not created");
    }

    @Test
    public void directIOImageOnNextDecode() throws IOException, DLSException {
        DLSSerialScanner scanner = new DLSSerialScanner();
        scanner.port = _port;
        when(_port.sendData(anyString())).then(invocation -> {
            scanner.bPicDone = true;
            scanner.picBuf.write("Test".getBytes());
            scanner.response = DLSJposConst.DL_ACK;
            return null;
        });
        int[] data = new int[]{9};
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        stream.write("01234Test.jpeg".getBytes());
        stream.close();
        scanner.directIO(DLSJposConst.DIO_IMAGE_ON_NEXT_DECODE, data, stream);
        verify(_port, times(1)).sendData(new String(DLSSerialScanner.imageonnextdecode));
        assertThat(data[0]).isEqualTo(9);
        assertThat(new String(stream.toByteArray())).doesNotContain("image not created");
    }

    @Test
    public void directIOImageCaptureNowNotCreated() throws IOException, DLSException {
        DLSSerialScanner scanner = new DLSSerialScanner();
        scanner.port = _port;
        when(_port.sendData(anyString())).then(invocation -> {
            scanner.bPicDone = true;
            scanner.response = DLSJposConst.DL_ACK;
            return null;
        });
        int[] data = new int[]{9};
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        stream.write("01234Test.jpeg".getBytes());
        stream.close();
        scanner.directIO(DLSJposConst.DIO_IMAGE_CAPTURE_NOW, data, stream);
        verify(_port, times(1)).sendData(new String(DLSSerialScanner.imagecaputurenow));
        assertThat(data[0]).isEqualTo(9);
        assertThat(new String(stream.toByteArray())).contains("image not created");
    }

    @Test
    public void directIOImageCaptureNow() throws IOException, DLSException {
        DLSSerialScanner scanner = new DLSSerialScanner();
        scanner.port = _port;
        when(_port.sendData(anyString())).then(invocation -> {
            scanner.bPicDone = true;
            scanner.picBuf.write("Test".getBytes());
            scanner.response = DLSJposConst.DL_ACK;
            return null;
        });
        int[] data = new int[]{9};
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        stream.write("01234Test.jpeg".getBytes());
        stream.close();
        scanner.directIO(DLSJposConst.DIO_IMAGE_CAPTURE_NOW, data, stream);
        verify(_port, times(1)).sendData(new String(DLSSerialScanner.imagecaputurenow));
        assertThat(data[0]).isEqualTo(9);
        assertThat(new String(stream.toByteArray())).doesNotContain("image not created");
    }

    @Test
    public void directIOImageDisable() throws IOException, DLSException {
        DLSSerialScanner scanner = new DLSSerialScanner();
        scanner.port = _port;
        when(_port.sendData(anyString())).then(invocation -> {
            scanner.bPicDone = true;
            scanner.picBuf.write("Test".getBytes());
            scanner.response = DLSJposConst.DL_ACK;
            return null;
        });
        int[] data = new int[]{9};
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        stream.write("01234Test.jpeg".getBytes());
        stream.close();
        scanner.directIO(DLSJposConst.DIO_DISABLE_IMAGE_MODE, data, stream);
        verify(_port, times(1)).sendData(new String(DLSSerialScanner.exitImageCapture));
        assertThat(data[0]).isEqualTo(9);
    }

    @Test
    public void directIO2WayHostResponse() throws DLSException, IOException {
        DLSSerialScanner scanner = new DLSSerialScanner();
        scanner.port = _port;
        int[] data = new int[]{9};
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        stream.write("TestResponse".getBytes());
        stream.close();
        scanner.directIO(DLSJposConst.DIO_TWO_WAY_HOST_RESPONSE, data, stream);
        verify(_port, times(1)).sendData("@TestResponse\r");
    }

    @Test
    public void directIODisplayData() throws IOException, DLSException {
        DLSSerialScanner scanner = new DLSSerialScanner();
        scanner.port = _port;
        int[] data = new int[]{9};
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        stream.write("DataToShow".getBytes());
        stream.close();
        scanner.directIO(DLSJposConst.DIO_DISPLAY_DATA, data, stream);
        verify(_port, times(1)).sendData("DataToShow".getBytes());
        assertThat(data[0]).isEqualTo(0);
    }

    @Test
    public void directIOGeneralCommand() throws IOException, DLSException {
        DLSSerialScanner scanner = new DLSSerialScanner();
        scanner.port = _port;
        when(_port.sendData(anyString())).then(invocation -> {
            scanner.sohBuf = "Response".getBytes();
            return null;
        });
        int[] data = new int[]{9};
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        stream.write("Command".getBytes());
        stream.close();
        scanner.directIO(DLSJposConst.DIO_GENERAL_CMD, data, stream);
        verify(_port, times(1)).sendData("Command\n\r");
        assertThat(data[0]).isEqualTo(DLSJposConst.DL_ACK);
        assertThat(new String(stream.toByteArray())).isEqualTo("Response");
    }

    @Test(expected = DLSException.class)
    public void directIOInvalidCommand() throws DLSException {
        DLSSerialScanner scanner = new DLSSerialScanner();
        int[] data = new int[]{9};
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        scanner.directIO(-11, data, stream);
    }
    //endregion

    //region Disable

    @Test
    public void disableWhileUpgrading() throws DLSException {
        DLSSerialScanner scanner = new DLSSerialScanner();
        scanner.port = _port;
        DLSJposConst.updateInProgress = true;
        scanner.disable();
        verify(_port, times(0)).sendData(DLSSerialScanner.DISABLE_CMD);
        assertThat(scanner.getState()).isEqualTo(DLSState.CLOSED);
    }

    @Test
    public void disable() throws DLSException {
        DLSSerialScanner scanner = new DLSSerialScanner();
        scanner.port = _port;
        scanner.setState(DLSState.OPENED);
        scanner.setState(DLSState.CLAIMED);
        scanner.setState(DLSState.ENABLED);
        scanner.disable();
        verify(_port, times(1)).sendData(DLSSerialScanner.DISABLE_CMD);
        assertThat(scanner.getState()).isEqualTo(DLSState.DISABLED);
    }

    //endregion

    //region Do health check
    @Test
    public void doHealthCheckWhileUpgrading() throws DLSException {
        DLSSerialScanner scanner = new DLSSerialScanner();
        DLSJposConst.updateInProgress = true;
        scanner.port = _port;
        assertThat(scanner.doHealthCheck(1000)).isTrue();
        verify(_port, times(0)).sendData(DLSSerialScanner.HEALTH_CMD);
    }

    @Test
    public void doHealthCheck() throws DLSException {
        DLSSerialScanner scanner = new DLSSerialScanner();
        scanner.port = _port;
        when(_port.sendData(DLSSerialScanner.HEALTH_CMD)).then(invocation -> {
            scanner.sohBuf = "Response".getBytes();
            return null;
        });
        assertThat(scanner.doHealthCheck(1000)).isTrue();
        verify(_port, times(1)).sendData(DLSSerialScanner.HEALTH_CMD);
    }

    @Test
    public void doHealthCheckTimeout() throws DLSException {
        DLSSerialScanner scanner = new DLSSerialScanner();
        scanner.port = _port;
        assertThat(scanner.doHealthCheck(1000)).isFalse();
        verify(_port, times(1)).sendData(DLSSerialScanner.HEALTH_CMD);
    }
    //endregion

    //region Do self test

    @Test
    public void doSelfTestWhileUpgrading() {
        DLSSerialScanner scanner = new DLSSerialScanner();
        scanner.port = _port;
        DLSJposConst.updateInProgress = true;
        DeviceErrorStatusListener listener = mock(DeviceErrorStatusListener.class);
        scanner.addDeviceErrorListener(listener);
        scanner.doSelfTest();
        verify(_port, times(0)).sendData(DLSSerialScanner.STATISTICS_CMD);
        verify(listener, times(0)).onDeviceError(anyInt());
    }

    @Test
    public void doSelfTest() {
        DLSSerialScanner scanner = new DLSSerialScanner();
        scanner.port = _port;
        scanner.scannerConfig = mock(DLSScannerConfig.class);
        when(scanner.scannerConfig.getCanAcceptStatisticsCommand()).thenReturn(true);
        DeviceErrorStatusListener listener = mock(DeviceErrorStatusListener.class);
        scanner.addDeviceErrorListener(listener);
        when(_port.sendData(DLSSerialScanner.STATISTICS_CMD)).then(invocation -> {
            scanner.sohBuf = "Response".getBytes();
            return null;
        });
        scanner.doSelfTest();
        verify(_port, times(1)).sendData(DLSSerialScanner.STATISTICS_CMD);
        verify(listener, times(0)).onDeviceError(anyInt());
    }

    @Test
    public void doSelfTestTimeout() {
        DLSSerialScanner scanner = new DLSSerialScanner();
        scanner.port = _port;
        scanner.scannerConfig = mock(DLSScannerConfig.class);
        when(scanner.scannerConfig.getCanAcceptStatisticsCommand()).thenReturn(true);
        DeviceErrorStatusListener listener = mock(DeviceErrorStatusListener.class);
        scanner.addDeviceErrorListener(listener);
        scanner.doSelfTest();
        verify(_port, times(1)).sendData(DLSSerialScanner.STATISTICS_CMD);
        verify(listener, times(1)).onDeviceError(DeviceErrorStatusListener.ERR_DEVICE_REMOVED);
    }
    //endregion

    //region Enable

    @Test
    public void enableWhileUpgrading() throws DLSException {
        DLSSerialScanner scanner = new DLSSerialScanner();
        scanner.port = _port;
        DLSJposConst.updateInProgress = true;
        scanner.setState(DLSState.OPENED);
        scanner.setState(DLSState.CLAIMED);
        scanner.enable();
        verify(_port, times(0)).sendData(DLSSerialScanner.ENABLE_CMD);
    }

    @Test
    public void enable() throws DLSException {
        DLSSerialScanner scanner = new DLSSerialScanner();
        scanner.port = _port;
        scanner.setState(DLSState.OPENED);
        scanner.setState(DLSState.CLAIMED);
        scanner.enable();
        verify(_port, times(1)).sendData(DLSSerialScanner.ENABLE_CMD);
        assertThat(scanner.getState()).isEqualTo(DLSState.ENABLED);
    }
    //endregion

    //region Statistics
    @Test
    public void getStatisticsWhileUpgrading() throws DLSException {
        DLSSerialScanner scanner = new DLSSerialScanner();
        DLSJposConst.updateInProgress = true;
        assertThat(scanner.getStatistics()).isEmpty();
    }

    @Test
    public void getStatisticsCanNotGetStatistics() throws DLSException {
        DLSSerialScanner scanner = new DLSSerialScanner();
        scanner.scannerConfig = mock(DLSScannerConfig.class);
        assertThat(scanner.getStatistics()).isEmpty();
    }

    @Test(expected = DLSException.class)
    public void getStatisticsTimeoutInfo() throws DLSException {
        DLSSerialScanner scanner = new DLSSerialScanner();
        scanner.port = _port;
        scanner.context = _context;
        scanner.scannerConfig = mock(DLSScannerConfig.class);
        when(scanner.scannerConfig.getCanAcceptStatisticsCommand()).thenReturn(true);
        try (MockedStatic<DLSStatistics> statistics = Mockito.mockStatic(DLSStatistics.class)) {
            statistics.when(() -> DLSStatistics.getInstance(_context)).thenReturn(mock(DLSStatistics.class));
            scanner.getStatistics();
        }
    }

    @Test(expected = DLSException.class)
    public void getStatisticsTimeoutHealth() throws DLSException {
        DLSSerialScanner scanner = new DLSSerialScanner();
        scanner.port = _port;
        scanner.context = _context;
        when(_port.sendData(DLSSerialScanner.INFO_CMD)).then(invocation -> {
            scanner.sohBuf = insertSTX_ETX("Info");
            return null;
        });
        scanner.scannerConfig = mock(DLSScannerConfig.class);
        when(scanner.scannerConfig.getCanAcceptStatisticsCommand()).thenReturn(true);
        try (MockedStatic<DLSStatistics> statistics = Mockito.mockStatic(DLSStatistics.class)) {
            statistics.when(() -> DLSStatistics.getInstance(_context)).thenReturn(mock(DLSStatistics.class));
            scanner.getStatistics();
        }
    }

    @Test(expected = DLSException.class)
    public void getStatisticsTimeoutStatistics() throws DLSException {
        DLSSerialScanner scanner = new DLSSerialScanner();
        scanner.port = _port;
        scanner.context = _context;
        when(_port.sendData(DLSSerialScanner.INFO_CMD)).then(invocation -> {
            scanner.sohBuf = insertSTX_ETX("Info");
            return null;
        });
        when(_port.sendData(DLSSerialScanner.HEALTH_CMD)).then(invocation -> {
            scanner.sohBuf = insertSTX_ETX("Health");
            return null;
        });
        scanner.scannerConfig = mock(DLSScannerConfig.class);
        when(scanner.scannerConfig.getCanAcceptStatisticsCommand()).thenReturn(true);
        try (MockedStatic<DLSStatistics> statistics = Mockito.mockStatic(DLSStatistics.class)) {
            statistics.when(() -> DLSStatistics.getInstance(_context)).thenReturn(mock(DLSStatistics.class));
            scanner.getStatistics();
        }
    }

    @Test
    public void getStatistics() throws DLSException {
        DLSSerialScanner scanner = new DLSSerialScanner();
        scanner.port = _port;
        scanner.context = _context;
        when(_port.sendData(DLSSerialScanner.INFO_CMD)).then(invocation -> {
            scanner.sohBuf = insertSTX_ETX("Info");
            return null;
        });
        when(_port.sendData(DLSSerialScanner.HEALTH_CMD)).then(invocation -> {
            scanner.sohBuf = insertSTX_ETX("Health");
            return null;
        });
        when(_port.sendData(DLSSerialScanner.STATISTICS_CMD)).then(invocation -> {
            scanner.sohBuf = insertSTX_ETX("Stats");
            return null;
        });
        scanner.scannerConfig = mock(DLSScannerConfig.class);
        when(scanner.scannerConfig.getCanAcceptStatisticsCommand()).thenReturn(true);
        try (MockedStatic<DLSStatistics> statistics = Mockito.mockStatic(DLSStatistics.class)) {
            statistics.when(() -> DLSStatistics.getInstance(_context)).thenReturn(mock(DLSStatistics.class));
            Map<String, Object> result = scanner.getStatistics();
            assertThat(result).isNotEmpty();
            assertThat(result.size()).isEqualTo(6);
            assertThat(result.get("RawInfo")).isEqualTo("# bytes received[6] 0x02 0x49 0x6E 0x66 0x6F 0x03");
            assertThat(result.get("RawStats")).isEqualTo("# bytes received[7] 0x02 0x53 0x74 0x61 0x74 0x73 0x03");
            assertThat(result.get("ManufacturerName")).isEqualTo("DLA");
            assertThat(result.get("RawHealth")).isEqualTo("# bytes received[8] 0x02 0x48 0x65 0x61 0x6C 0x74 0x68 0x03");
            assertThat(result.get("DeviceCategory")).isEqualTo("Scanner");
            assertThat(result.get("Interface")).isEqualTo("RS232");
        }
    }
    //endregion

    @Test
    public void hasStatisticsReporting() {
        DLSSerialScanner scanner = new DLSSerialScanner();
        scanner.scannerConfig = mock(DLSScannerConfig.class);
        when(scanner.scannerConfig.getCanAcceptStatisticsCommand()).thenReturn(true);
        assertThat(scanner.hasStatisticsReporting()).isTrue();
        when(scanner.scannerConfig.getCanAcceptStatisticsCommand()).thenReturn(false);
        assertThat(scanner.hasStatisticsReporting()).isFalse();
    }

    //region Is alive
    @Test
    public void isAlive() {
        DLSSerialScanner scanner = new DLSSerialScanner();
        scanner.port = _port;
        when(_port.sendData(DLSSerialScanner.HEALTH_CMD)).then(invocation -> {
            scanner.sohBuf = "ALIVE!".getBytes();
            return null;
        });
        assertThat(scanner.isAlive()).isTrue();
    }

    @Test
    public void isNotAlive() {
        DLSSerialScanner scanner = new DLSSerialScanner();
        scanner.port = _port;
        assertThat(scanner.isAlive()).isFalse();
    }
    //endregion

    //region On data received
    @Test
    public void onDataReceivedFirmware() {
        DLSSerialScanner scanner = new DLSSerialScanner();
        scanner.bFirmware = true;
        scanner.onDataReceived(new byte[]{1}, 1);
        assertThat(scanner.bFirmware).isFalse();
        assertThat(scanner.response).isEqualTo(1);
    }

    @Test
    public void onDataReceivedCPMInProgress() {
        DLSSerialScanner scanner = new DLSSerialScanner();
        scanner.bDIOCPMInProgress = true;
        scanner.onDataReceived(new byte[]{2}, 1);
        assertThat(scanner.bDIOCPMInProgress).isFalse();
        assertThat(scanner.response).isEqualTo(2);
    }

    @Test
    public void onDataReceivedPTCInProgressDone() {
        DLSSerialScanner scanner = new DLSSerialScanner();
        scanner.bDIOPTCInProgress = true;
        scanner.onDataReceived(new byte[]{0}, 1);
        assertThat(scanner.bDIOPTCInProgress).isFalse();
        assertThat(scanner.bPicDone).isTrue();
        assertThat(scanner.response).isEqualTo(0);
    }

    @Test
    public void onDataReceivedPTCInProgressLengthNoMoreThanResponse() {
        DLSSerialScanner scanner = new DLSSerialScanner();
        scanner.bDIOPTCInProgress = true;
        scanner.picBuf = new ByteArrayOutputStream();
        scanner.onDataReceived(new byte[]{DLSJposConst.DL_ACK}, 1);
        assertThat(scanner.bDIOPTCInProgress).isFalse();
        assertThat(scanner.bPicDone).isTrue();
        assertThat(scanner.response).isEqualTo(DLSJposConst.DL_ACK);
        byte[] moreData = new byte[]{0, 0, 0, 1, 70};
        scanner.onDataReceived(moreData, moreData.length);
        assertThat(scanner.picBuf.size()).isEqualTo(1);
        assertThat(new String(scanner.picBuf.toByteArray())).isEqualTo("F");
    }

    @Test
    public void onDataReceivedPTCInProgressLengthMoreThanResponse() {
        DLSSerialScanner scanner = new DLSSerialScanner();
        scanner.bDIOPTCInProgress = true;
        scanner.picBuf = new ByteArrayOutputStream();
        scanner.onDataReceived(new byte[]{DLSJposConst.DL_ACK, 0, 0, 0, 2, 90}, 6);
        assertThat(scanner.bDIOPTCInProgress).isFalse();
        assertThat(scanner.response).isEqualTo(DLSJposConst.DL_ACK);
        assertThat(scanner.picBuf.size()).isEqualTo(1);
        assertThat(new String(scanner.picBuf.toByteArray())).isEqualTo("Z");
        scanner.onDataReceived(new byte[]{89}, 1);
        assertThat(scanner.picBuf.size()).isEqualTo(2);
        assertThat(new String(scanner.picBuf.toByteArray())).isEqualTo("ZY");
        assertThat(scanner.bPicDone).isTrue();
    }

    @Test
    public void onDataReceivedPTCInProgressLengthMoreThanResponse0Size() {
        DLSSerialScanner scanner = new DLSSerialScanner();
        scanner.bDIOPTCInProgress = true;
        scanner.picBuf = new ByteArrayOutputStream();
        scanner.onDataReceived(new byte[]{DLSJposConst.DL_ACK, 0, 0, 0, 0, 90}, 6);
        assertThat(scanner.bDIOPTCInProgress).isFalse();
        assertThat(scanner.response).isEqualTo(DLSJposConst.DL_ACK);
        assertThat(scanner.bPicDone).isTrue();
    }

    @Test
    public void onDataReceivedDIOImageBMP() {
        DLSSerialScanner scanner = new DLSSerialScanner();
        scanner.bDIOImageInProgress = true;
        scanner.picBuf = new ByteArrayOutputStream();
        byte[] data = new byte[]{36, 105, 48, 48, 2, 3, 4, 5, 0, 0, 0, 2, 10, 11, 12, 13, 14};
        scanner.onDataReceived(data, data.length);
        assertThat(scanner.extension).isEqualTo("BMP");
        scanner.onDataReceived(new byte[]{90, 89}, 2);
        assertThat(scanner.picBuf.size()).isEqualTo(2);
        assertThat(new String(scanner.picBuf.toByteArray())).isEqualTo("ZY");
        assertThat(scanner.response).isEqualTo(DLSJposConst.DL_ACK);
        assertThat(scanner.bPicDone).isTrue();
    }

    @Test
    public void onDataReceivedDIOImageJPG() {
        DLSSerialScanner scanner = new DLSSerialScanner();
        scanner.bDIOImageInProgress = true;
        scanner.picBuf = new ByteArrayOutputStream();
        byte[] data = new byte[]{36, 105, 48, 49, 2, 3, 4, 5, 0, 0, 0, 2, 10, 11, 12, 13, 14};
        scanner.onDataReceived(data, data.length);
        assertThat(scanner.extension).isEqualTo("JPG");
        scanner.onDataReceived(new byte[]{90, 89}, 2);
        assertThat(scanner.picBuf.size()).isEqualTo(2);
        assertThat(new String(scanner.picBuf.toByteArray())).isEqualTo("ZY");
        assertThat(scanner.response).isEqualTo(DLSJposConst.DL_ACK);
        assertThat(scanner.bPicDone).isTrue();
    }

    @Test
    public void onDataReceivedDIOImageJPGLessThan17Bytes() {
        DLSSerialScanner scanner = new DLSSerialScanner();
        scanner.bDIOImageInProgress = true;
        scanner.picBuf = new ByteArrayOutputStream();
        byte[] data = new byte[]{36, 105, 48, 49, 2, 3, 4, 5, 0, 0, 0, 2, 10, 11, 12};
        scanner.onDataReceived(data, data.length);
        assertThat(scanner.extension).isEqualTo("JPG");
        scanner.onDataReceived(new byte[]{90, 89}, 2);
        ;
        assertThat(scanner.response).isEqualTo(DLSJposConst.DL_ACK);
        assertThat(scanner.bPicDone).isTrue();
    }

    @Test
    public void onDataReceivedDIOImageJPGNoPrefix() {
        DLSSerialScanner scanner = new DLSSerialScanner();
        scanner.bDIOImageInProgress = true;
        scanner.picBuf = new ByteArrayOutputStream();
        byte[] data = new byte[]{105, 48, 49, 2, 3, 4, 5, 0, 0, 0, 2, 10, 11, 12};
        scanner.onDataReceived(data, data.length);
        assertThat(scanner.extension).isEqualTo("JPG");
        scanner.onDataReceived(new byte[]{90, 89}, 2);
        assertThat(scanner.bPicDone).isTrue();
    }

    @Test
    public void onDataReceivedReadConfigInProgress() {
        DLSSerialScanner scanner = new DLSSerialScanner();
        scanner.bReadConfigInProgress = true;
        scanner.deviceInfo = mock(DLSDeviceInfo.class);
        when(scanner.deviceInfo.getRxTrailer()).thenReturn((byte) 3);
        byte[] data = new byte[]{65, 110, 32, 100, 101, 108, 32, 100, 111, 105, 3};
        scanner.onDataReceived(data, data.length);
        assertThat(new String(scanner.sohBuf)).contains("An del doi");
        assertThat(scanner.bReadConfigInProgress).isFalse();
    }

    @Test
    public void onDataReceivedWriteConfigInProgress() {
        DLSSerialScanner scanner = new DLSSerialScanner();
        scanner.bDIOWriteConfigInProgress = true;
        byte[] data = new byte[]{DLSJposConst.DL_ACK};
        scanner.onDataReceived(data, data.length);
        assertThat(scanner.response).isEqualTo(DLSJposConst.DL_ACK);
    }

    @Test
    public void onDataReceivedCommitConfigInProgress() {
        DLSSerialScanner scanner = new DLSSerialScanner();
        scanner.bDIOCommitConfigInProgress = true;
        byte[] data = new byte[]{DLSJposConst.DL_ACK};
        scanner.onDataReceived(data, data.length);
        assertThat(scanner.response).isEqualTo(DLSJposConst.DL_ACK);
    }

    @Test
    public void onDataReceivedStatus() {
        DLSSerialScanner scanner = new DLSSerialScanner();
        byte[] data = new byte[]{1, 65, 110, 32, 100, 101, 108, 32, 100, 111, 105, 4};
        scanner.onDataReceived(data, data.length);
        assertThat(new String(scanner.sohBuf)).contains("An del doi");
    }

    @Test
    public void onDataReceivedStatusMultiMessage() {
        DLSSerialScanner scanner = new DLSSerialScanner();
        byte[] data = new byte[]{1, 65, 110, 32, 100, 101};
        scanner.onDataReceived(data, data.length);
        data = new byte[]{1, 108, 32, 100, 111, 105, 4};
        scanner.onDataReceived(data, data.length);
        assertThat(scanner.sohBuf).isEqualTo(new byte[]{1, 65, 110, 32, 100, 101, 1, 108, 32, 100, 111, 105, 4});
    }

    @Test
    public void onDataReceivedStatusTooMany() {
        DLSSerialScanner scanner = new DLSSerialScanner();
        byte[] data = new byte[]{1, 65, 110, 32, 100, 101};
        scanner.onDataReceived(data, data.length);
        assertThat(scanner.outputStream.size()).isEqualTo(6);
        for (int i = 0; i < 30; i++) {
            data = new byte[]{1, 9};
            scanner.onDataReceived(data, data.length);
            assertThat(scanner.messageList.size()).isEqualTo(0);
        }
        assertThat(scanner.outputStream.size()).isEqualTo(0);
    }

    @Test
    public void onDataReceived() throws DLSException {
        DLSSerialScanner scanner = new DLSSerialScanner();
        scanner.setState(DLSState.OPENED);
        scanner.setState(DLSState.CLAIMED);
        scanner.setState(DLSState.ENABLED);
        scanner.deviceInfo = mock(DLSDeviceInfo.class);
        scanner.context = _context;
        scanner.options = mock(DLSProperties.class);
        when(scanner.deviceInfo.getDecodingType()).thenReturn(Constants.DECODE_TYPE_EU);
        when(scanner.deviceInfo.getRxPrefix()).thenReturn((byte) 2);
        when(scanner.deviceInfo.getRxTrailer()).thenReturn((byte) 3);

        LabelReceivedListener listener = mock(LabelReceivedListener.class);
        scanner.addLabelReceivedListener(listener);

        DLAPosConfigHelper dlaPosConfigHelper = mock(DLAPosConfigHelper.class);
        LabelHelper helper = mock(LabelHelper.class);
        when(dlaPosConfigHelper.getLabelHelper()).thenReturn(helper);
        when(helper.getLabelsIds(new byte[]{65, 110, 32})).thenReturn(new LabelIds(
                "DUMMY",
                13,
                "DUMMY",
                "DUMMY",
                "DUMMY",
                "DUMMY",
                "DUMMY",
                "DUMMY",
                "DUMMY"
        ));
        try (MockedStatic<DLAPosConfigHelper> configHelper = Mockito.mockStatic(DLAPosConfigHelper.class)) {
            configHelper.when(() -> DLAPosConfigHelper.getInstance(_context)).thenReturn(dlaPosConfigHelper);
            byte[] data = new byte[]{65, 110, 32, 100, 101, 108, 32, 100, 111, 105, 3};
            scanner.onDataReceived(data, data.length);
            verify(listener, times(1)).onLabelReceived(data, new byte[]{100, 101, 108, 32, 100, 111, 105}, 13);
        }
    }

    @Test
    public void onDataReceivedCooked() throws DLSException {
        DLSSerialScanner scanner = new DLSSerialScanner();
        scanner.setState(DLSState.OPENED);
        scanner.setState(DLSState.CLAIMED);
        scanner.setState(DLSState.ENABLED);
        scanner.deviceInfo = mock(DLSDeviceInfo.class);
        scanner.context = _context;
        scanner.options = mock(DLSProperties.class);
        when(scanner.deviceInfo.getDecodingType()).thenReturn(Constants.DECODE_TYPE_EU);
        when(scanner.deviceInfo.getRxPrefix()).thenReturn((byte) 2);
        when(scanner.deviceInfo.getRxTrailer()).thenReturn((byte) 3);
        when(scanner.options.isSendCookedData()).thenReturn(true);

        LabelReceivedListener listener = mock(LabelReceivedListener.class);
        scanner.addLabelReceivedListener(listener);

        DLAPosConfigHelper dlaPosConfigHelper = mock(DLAPosConfigHelper.class);
        LabelHelper helper = mock(LabelHelper.class);
        when(dlaPosConfigHelper.getLabelHelper()).thenReturn(helper);
        when(helper.getLabelsIds(new byte[]{65, 110, 32})).thenReturn(new LabelIds(
                "DUMMY",
                13,
                "DUMMY",
                "DUMMY",
                "DUMMY",
                "DUMMY",
                "DUMMY",
                "DUMMY",
                "DUMMY"
        ));
        try (MockedStatic<DLAPosConfigHelper> configHelper = Mockito.mockStatic(DLAPosConfigHelper.class)) {
            configHelper.when(() -> DLAPosConfigHelper.getInstance(_context)).thenReturn(dlaPosConfigHelper);
            byte[] data = new byte[]{65, 110, 32, 100, 101, 108, 32, 100, 111, 105, 3};
            scanner.onDataReceived(data, data.length);
            verify(listener, times(1)).onLabelReceived(new byte[]{100, 101, 108, 32, 100, 111, 105}, new byte[]{100, 101, 108, 32, 100, 111, 105}, 13);
        }
    }

    @Test
    public void onDataReceivedWithPrefix() throws DLSException {
        DLSSerialScanner scanner = new DLSSerialScanner();
        scanner.setState(DLSState.OPENED);
        scanner.setState(DLSState.CLAIMED);
        scanner.setState(DLSState.ENABLED);
        scanner.deviceInfo = mock(DLSDeviceInfo.class);
        scanner.context = _context;
        scanner.options = mock(DLSProperties.class);
        when(scanner.deviceInfo.getDecodingType()).thenReturn(Constants.DECODE_TYPE_EU);
        when(scanner.deviceInfo.getRxPrefix()).thenReturn((byte) 2);
        when(scanner.deviceInfo.getRxTrailer()).thenReturn((byte) 3);

        LabelReceivedListener listener = mock(LabelReceivedListener.class);
        scanner.addLabelReceivedListener(listener);

        DLAPosConfigHelper dlaPosConfigHelper = mock(DLAPosConfigHelper.class);
        LabelHelper helper = mock(LabelHelper.class);
        when(dlaPosConfigHelper.getLabelHelper()).thenReturn(helper);
        when(helper.getLabelsIds(new byte[]{65, 110, 32})).thenReturn(new LabelIds(
                "DUMMY",
                13,
                "DUMMY",
                "DUMMY",
                "DUMMY",
                "DUMMY",
                "DUMMY",
                "DUMMY",
                "DUMMY"
        ));
        try (MockedStatic<DLAPosConfigHelper> configHelper = Mockito.mockStatic(DLAPosConfigHelper.class)) {
            configHelper.when(() -> DLAPosConfigHelper.getInstance(_context)).thenReturn(dlaPosConfigHelper);
            byte[] data = new byte[]{2, 65, 110, 32, 100, 101, 108, 32, 100, 111, 105, 3};
            scanner.onDataReceived(data, data.length);
            verify(listener, times(1)).onLabelReceived(data, new byte[]{32, 100, 101, 108, 32, 100, 111, 105}, 13);
        }
    }

    @Test
    public void onDataReceivedNotEnabled() {
        DLSSerialScanner scanner = new DLSSerialScanner();
        scanner.deviceInfo = mock(DLSDeviceInfo.class);
        scanner.context = _context;
        scanner.options = mock(DLSProperties.class);
        when(scanner.deviceInfo.getDecodingType()).thenReturn(Constants.DECODE_TYPE_EU);
        when(scanner.deviceInfo.getRxPrefix()).thenReturn((byte) 2);
        when(scanner.deviceInfo.getRxTrailer()).thenReturn((byte) 3);

        LabelReceivedListener listener = mock(LabelReceivedListener.class);
        scanner.addLabelReceivedListener(listener);

        DLAPosConfigHelper dlaPosConfigHelper = mock(DLAPosConfigHelper.class);
        LabelHelper helper = mock(LabelHelper.class);
        when(dlaPosConfigHelper.getLabelHelper()).thenReturn(helper);
        when(helper.getLabelsIds(new byte[]{65, 110, 32})).thenReturn(new LabelIds(
                "DUMMY",
                13,
                "DUMMY",
                "DUMMY",
                "DUMMY",
                "DUMMY",
                "DUMMY",
                "DUMMY",
                "DUMMY"
        ));
        try (MockedStatic<DLAPosConfigHelper> configHelper = Mockito.mockStatic(DLAPosConfigHelper.class)) {
            configHelper.when(() -> DLAPosConfigHelper.getInstance(_context)).thenReturn(dlaPosConfigHelper);
            byte[] data = new byte[]{65, 110, 32, 100, 101, 108, 32, 100, 111, 105, 3};
            scanner.onDataReceived(data, data.length);
            assertThat(new String(scanner.sohBuf)).contains("An del doi");
            verify(listener, times(0)).onLabelReceived(any(), any(), anyInt());
        }
    }
    //endregion

    //region On device arrival
    @Test
    public void onDeviceArrival() throws DLSException {
        DLSSerialScanner scanner = new DLSSerialScanner();
        scanner.port = _port;
        scanner.m_hotPlugWatcher = mock(ScheduledFuture.class);
        scanner.setState(DLSState.OPENED);
        scanner.setState(DLSState.CLAIMED);
        scanner.setState(DLSState.ENABLED);

        DeviceErrorStatusListener listener = mock(DeviceErrorStatusListener.class);
        scanner.addDeviceStatusListener(listener);

        scanner.onDeviceArrival();
        verify(listener, times(1)).onDeviceStatus(CommonsConstants.SUE_POWER_ONLINE);
        verify(scanner.m_hotPlugWatcher, times(1)).cancel(false);
    }

    @Test
    public void onDeviceArrivalDisabled() throws DLSException {
        DLSSerialScanner scanner = new DLSSerialScanner();
        scanner.port = _port;
        scanner.m_hotPlugWatcher = mock(ScheduledFuture.class);
        scanner.setState(DLSState.OPENED);
        scanner.setState(DLSState.CLAIMED);
        scanner.setState(DLSState.ENABLED);
        scanner.setState(DLSState.DISABLED);

        DeviceErrorStatusListener listener = mock(DeviceErrorStatusListener.class);
        scanner.addDeviceStatusListener(listener);

        scanner.onDeviceArrival();
        verify(listener, times(1)).onDeviceStatus(CommonsConstants.SUE_POWER_ONLINE);
        verify(scanner.m_hotPlugWatcher, times(1)).cancel(false);
    }

    //endregion

    //region On device reattached
    @Test
    public void onDeviceReattached() {
        DLSSerialScanner scanner = new DLSSerialScanner();
        scanner.port = _port;
        scanner.hotPlugged = false;

        DeviceErrorStatusListener listener = mock(DeviceErrorStatusListener.class);
        scanner.addDeviceStatusListener(listener);

        when(_port.openPort()).thenReturn(true);
        scanner.onDeviceReattached();
        verify(listener, times(0)).onDeviceError(DeviceErrorStatusListener.ERR_DEVICE_REATTACHED);
    }

    @Test
    public void onDeviceReattachedRetry() {
        DLSSerialScanner scanner = new DLSSerialScanner();
        scanner.port = _port;
        scanner.hotPlugged = false;
        scanner.options = mock(DLSProperties.class);
        when(scanner.options.getRetryWaitTime()).thenReturn(50);

        DeviceErrorStatusListener listener = mock(DeviceErrorStatusListener.class);
        scanner.addDeviceStatusListener(listener);

        AtomicBoolean alreadyCalled = new AtomicBoolean(false);
        when(_port.openPort()).then(invocation -> {
            alreadyCalled.set(true);
            return !alreadyCalled.get();
        });
        scanner.onDeviceReattached();
        verify(listener, times(0)).onDeviceError(DeviceErrorStatusListener.ERR_DEVICE_REATTACHED);
    }

    //endregion

    @Test
    public void onDeviceRemoved() {
        DLSSerialScanner scanner = new DLSSerialScanner();
        scanner.port = _port;
        DeviceErrorStatusListener errorListener = mock(DeviceErrorStatusListener.class);
        DeviceErrorStatusListener statusListener = mock(DeviceErrorStatusListener.class);

        scanner.addDeviceStatusListener(statusListener);
        scanner.addDeviceErrorListener(errorListener);
        when(_port.closePort()).thenReturn(true);

        scanner.onDeviceRemoved();
        verify(statusListener, times(1)).onDeviceStatus(CommonsConstants.SUE_POWER_OFF_OFFLINE);
        verify(errorListener, times(1)).onDeviceError(DeviceErrorStatusListener.ERR_DEVICE_REMOVED);
    }

    @Test
    public void openPort() {
        DLSSerialScanner scanner = new DLSSerialScanner();
        scanner.port = _port;
        scanner.openPort();
        verify(_port, times(1)).openPort();
    }

    @Test
    public void open() throws DLSException {
        DLAPosConfigHelper configHelper = mock(DLAPosConfigHelper.class);
        ProfileManager profileManager = mock(ProfileManager.class);
        MockUtils.mockConfigHelperSingleton(configHelper, profileManager);
        DLSProperties properties = mock(DLSProperties.class);
        MockUtils.mockDLSPropertiesSingleton(properties);

        DLSSerialScanner scanner = new DLSSerialScanner();
        try (MockedConstruction<DLSScannerConfig> mockedConfig = mockConstruction(DLSScannerConfig.class,
                (mock, context) -> {
                    when(mock.getLogicalName()).thenReturn("I'm the mock");
                })) {
            scanner.open("test", _context);
            assertThat(scanner.context).isEqualTo(_context);
            assertThat(scanner.getLogicalName()).isEqualTo("test");
            assertThat(scanner.properties).isEqualTo(properties);
            assertThat(scanner.options).isNotNull();
            assertThat(scanner.getConfiguration().getLogicalName()).isEqualTo("I'm the mock");
            assertThat(scanner.getState()).isEqualTo(DLSState.OPENED);
        }

        MockUtils.cleanConfigHelper();
        MockUtils.cleanDLSProperties();
    }

    @Test
    public void reportConfiguration() throws DLSException {
        DLSSerialScanner scanner = new DLSSerialScanner();
        DLSScannerConfig config = mock(DLSScannerConfig.class);
        scanner.scannerConfig = config;
        assertThat(scanner.reportConfiguration()).isEqualTo(config);
    }

    @Test
    public void reset() throws DLSException {
        DLSSerialScanner scanner = new DLSSerialScanner();
        scanner.port = _port;
        scanner.reset();
        verify(_port, times(1)).sendData(DLSSerialScanner.RESET_CMD);
    }

    @Test
    public void resetWhileUpgrading() throws DLSException {
        DLSSerialScanner scanner = new DLSSerialScanner();
        DLSJposConst.updateInProgress = true;
        scanner.port = _port;
        scanner.reset();
        verify(_port, times(0)).sendData(DLSSerialScanner.RESET_CMD);
    }

//    @Test
//    public void portOpened() throws DLSException {
//        DLSProperties properties = mock(DLSProperties.class);
//        MockUtils.mockDLSPropertiesSingleton(properties);
//
//        DLSSerialScanner scanner = new DLSSerialScanner();
//        scanner.port = _port;
//        scanner.setState(DLSState.OPENED);
//        scanner.portOpened();
//        verify(_port, times(1)).addDataReceivedListener(scanner);
//        verify(_port, times(1)).addDeviceReattachedListener(scanner);
//        verify(_port, times(1)).addDeviceRemovedListener(scanner);
//
//        MockUtils.cleanDLSProperties();
//    }

    private byte[] insertSTX_ETX(String message) {
        byte[] data = new byte[2 + message.length()];
        data[0] = 0x02;
        data[data.length - 1] = 0x03;
        System.arraycopy(message.getBytes(), 0, data, 1, message.length());
        return data;
    }
}