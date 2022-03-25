package com.datalogic.dlapos.androidpos.interpretation;

import android.content.Context;

import com.datalogic.dlapos.androidpos.common.DLSDeviceInfo;
import com.datalogic.dlapos.androidpos.common.DLSException;
import com.datalogic.dlapos.androidpos.common.DLSJposConst;
import com.datalogic.dlapos.androidpos.common.DLSObjectFactory;
import com.datalogic.dlapos.androidpos.common.DLSProperties;
import com.datalogic.dlapos.androidpos.common.DLSScannerConfig;
import com.datalogic.dlapos.androidpos.common.DLSState;
import com.datalogic.dlapos.androidpos.common.DLSStatistics;
import com.datalogic.dlapos.androidpos.service.DLSScannerService;
import com.datalogic.dlapos.androidpos.transport.DLSUsbPort;
import com.datalogic.dlapos.androidpos.transport.UsbPortStatusListener;
import com.datalogic.dlapos.androidpos.utils.MockUtils;
import com.datalogic.dlapos.commons.constant.CommonsConstants;
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
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DLSUSBScannerTest {

    @Mock
    private final DLSUsbPort _port = mock(DLSUsbPort.class);

    @Mock
    private final Context _context = mock(Context.class);

    @Mock
    private final DLSDeviceInfo _deviceInfo = mock(DLSDeviceInfo.class);

    @Mock
    private final ProfileManager _profileManager = mock(ProfileManager.class);

    @Mock
    private final DLSProperties _properties = mock(DLSProperties.class);

    @Before
    public void setUp() {
        MockUtils.mockConfigHelperSingleton(mock(DLAPosConfigHelper.class), _profileManager);
        MockUtils.mockDLSPropertiesSingleton(_properties);
    }

    @After
    public void cleanUp() {
        DLSJposConst.updateInProgress = false;
        MockUtils.cleanConfigHelper();
        MockUtils.cleanDLSProperties();
    }

    @Test
    public void changeBaudRate() {
        DLSUSBScanner scanner = new DLSUSBScanner();
        scanner.changeBaudRate(2);
    }

//    //region Claim
//    @Test
//    public void claim() throws DLSException {
//        DLSUSBScanner scanner = new DLSUSBScanner();
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
//        DLSUSBScanner scanner = new DLSUSBScanner();
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
//        DLSUSBScanner scanner = new DLSUSBScanner();
//        scanner.deviceInfo = _deviceInfo;
//        scanner.context = _context;
//        scanner.setState(DLSState.OPENED);
//        when(_port.openPort()).thenReturn(false);
//        try (MockedStatic<DLSObjectFactory> factoryMockedStatic = Mockito.mockStatic(DLSObjectFactory.class)) {
//            factoryMockedStatic.when(() -> DLSObjectFactory.createPort(_deviceInfo, _context)).thenReturn(_port);
//            scanner.claim(100);
//        }
//    }
//    //endregion

    //region Direct IO
    @Test
    public void directIOWhileUpgrading() throws DLSException {
        DLSUSBScanner scanner = new DLSUSBScanner();
        DLSJposConst.updateInProgress = true;
        scanner.directIO(1, new int[]{}, new Object());
    }

    @Test
    public void directIODisableADDL() throws DLSException {
        DLSUSBScanner scanner = new DLSUSBScanner();
        int[] data = new int[]{1, 2, 3};
        byte[] command = new byte[]{
                0x30,
                (byte) 0xFF,
                (byte) 0xFF,
                1,
                2,
                3,
                0,
                0,
                0,
                0,
                0
        };
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        scanner.port = _port;
        when(_port.sendData(command, command.length)).then(invocation -> {
            scanner.dioBuf = new byte[]{4, 5, 6, 8};
            return null;
        });
        scanner.directIO(DLSJposConst.DIO_ENAB_DISAB_ADDL_SYM, data, stream);
        assertThat(data[0]).isEqualTo(0);
    }

    @Test
    public void directIODisableADDLInvalidArgs() throws DLSException {
        DLSUSBScanner scanner = new DLSUSBScanner();
        int[] data = new int[]{1, 2};
        byte[] command = new byte[]{
                0x30,
                (byte) 0xFF,
                (byte) 0xFF,
                1,
                2,
                3,
                0,
                0,
                0,
                0,
                0
        };
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        scanner.port = _port;
        when(_port.sendData(command, command.length)).then(invocation -> {
            scanner.dioBuf = new byte[]{4, 5, 6, 8};
            return null;
        });
        scanner.directIO(DLSJposConst.DIO_ENAB_DISAB_ADDL_SYM, data, stream);
        assertThat(data[0]).isEqualTo(-1);
    }

    @Test
    public void directIODisableADDLFails() throws DLSException {
        DLSUSBScanner scanner = new DLSUSBScanner();
        int[] data = new int[]{1, 2, 3};
        byte[] command = new byte[]{
                0x30,
                (byte) 0xFF,
                (byte) 0xFF,
                1,
                2,
                3,
                0,
                0,
                0,
                0,
                0
        };
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        scanner.port = _port;
        when(_port.sendData(command, command.length)).then(invocation -> {
            scanner.dioBuf = new byte[]{4, 5, 6, 1};
            return null;
        });
        scanner.directIO(DLSJposConst.DIO_ENAB_DISAB_ADDL_SYM, data, stream);
        assertThat(data[0]).isEqualTo(1);
    }

    @Test
    public void directIODeviceInfo() throws DLSException {
        DLSUSBScanner scanner = new DLSUSBScanner();
        scanner.port = _port;
        when(_port.sendData(DLSUSBScanner.CMD_DEVICE_INFO, DLSUSBScanner.CMD_DEVICE_INFO.length)).then(invocation -> {
            scanner.dioBuf = new byte[]{4, 5, 6, 7, 0, 65, 66, 67};
            return null;
        });
        int[] data = new int[]{1, 2, 3};
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        scanner.directIO(DLSJposConst.DIO_DEVICE_INFORMATION, data, stream);
        assertThat(data[0]).isEqualTo(0);
        assertThat(new String(stream.toByteArray())).isEqualTo("ABC");
    }

    @Test
    public void directIODeviceInfoFewValues() throws DLSException {
        DLSUSBScanner scanner = new DLSUSBScanner();
        scanner.port = _port;
        when(_port.sendData(DLSUSBScanner.CMD_DEVICE_INFO, DLSUSBScanner.CMD_DEVICE_INFO.length)).then(invocation -> {
            scanner.dioBuf = new byte[]{4, 5, 6, 7, 0};
            return null;
        });
        int[] data = new int[]{9, 2, 3};
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        scanner.directIO(DLSJposConst.DIO_DEVICE_INFORMATION, data, stream);
        assertThat(data[0]).isEqualTo(1);
    }

    @Test
    public void directIOScannerGenMGT() throws DLSException {
        DLSUSBScanner scanner = new DLSUSBScanner();
        scanner.port = _port;
        when(_port.sendData(DLSUSBScanner.CMD_SCAN_GEN_MGT_INFO, DLSUSBScanner.CMD_SCAN_GEN_MGT_INFO.length)).then(invocation -> {
            scanner.dioBuf = new byte[]{4, 5, 6, 7, 0, 65, 66, 67};
            return null;
        });
        int[] data = new int[]{9, 2, 3};
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        scanner.directIO(DLSJposConst.DIO_REQ_SCANNER_GEN_MGT, data, stream);
        assertThat(data[0]).isEqualTo(0);
        assertThat(new String(stream.toByteArray())).isEqualTo("ABC");
    }

    @Test
    public void directIOScannerGenMGTFewValues() throws DLSException {
        DLSUSBScanner scanner = new DLSUSBScanner();
        scanner.port = _port;
        when(_port.sendData(DLSUSBScanner.CMD_SCAN_GEN_MGT_INFO, DLSUSBScanner.CMD_SCAN_GEN_MGT_INFO.length)).then(invocation -> {
            scanner.dioBuf = new byte[]{4, 5, 6, 7, 0};
            return null;
        });
        int[] data = new int[]{9, 2, 3};
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        scanner.directIO(DLSJposConst.DIO_REQ_SCANNER_GEN_MGT, data, stream);
        assertThat(data[0]).isEqualTo(1);
    }

    @Test
    public void directIOScannerVenMGT() throws DLSException {
        DLSUSBScanner scanner = new DLSUSBScanner();
        scanner.port = _port;
        when(_port.sendData(DLSUSBScanner.CMD_SCAN_VEN_MGT_INFO, DLSUSBScanner.CMD_SCAN_VEN_MGT_INFO.length)).then(invocation -> {
            scanner.dioBuf = new byte[]{4, 5, 6, 7, 0, 65, 66, 67};
            return null;
        });
        int[] data = new int[]{9, 2, 3};
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        scanner.directIO(DLSJposConst.DIO_REQ_SCANNER_VEN_MGT, data, stream);
        assertThat(data[0]).isEqualTo(0);
        assertThat(new String(stream.toByteArray())).isEqualTo("ABC");
    }

    @Test
    public void directIOScannerVenMGTFewValues() throws DLSException {
        DLSUSBScanner scanner = new DLSUSBScanner();
        scanner.port = _port;
        when(_port.sendData(DLSUSBScanner.CMD_SCAN_VEN_MGT_INFO, DLSUSBScanner.CMD_SCAN_VEN_MGT_INFO.length)).then(invocation -> {
            scanner.dioBuf = new byte[]{4, 5, 6, 7, 0};
            return null;
        });
        int[] data = new int[]{9, 2, 3};
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        scanner.directIO(DLSJposConst.DIO_REQ_SCANNER_VEN_MGT, data, stream);
        assertThat(data[0]).isEqualTo(1);
    }

    @Test
    public void directIOScaleGenMGT() throws DLSException {
        DLSUSBScanner scanner = new DLSUSBScanner();
        scanner.port = _port;
        when(_port.sendData(DLSUSBScanner.CMD_SCALE_GEN_MGT_INFO, DLSUSBScanner.CMD_SCALE_GEN_MGT_INFO.length)).then(invocation -> {
            scanner.dioBuf = new byte[]{4, 5, 6, 7, 0, 65, 66, 67};
            return null;
        });
        int[] data = new int[]{9, 2, 3};
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        scanner.directIO(DLSJposConst.DIO_REQ_SCALE_GEN_MGT, data, stream);
        assertThat(data[0]).isEqualTo(0);
        assertThat(new String(stream.toByteArray())).isEqualTo("ABC");
    }

    @Test
    public void directIOScaleGenMGTFewValues() throws DLSException {
        DLSUSBScanner scanner = new DLSUSBScanner();
        scanner.port = _port;
        when(_port.sendData(DLSUSBScanner.CMD_SCALE_GEN_MGT_INFO, DLSUSBScanner.CMD_SCALE_GEN_MGT_INFO.length)).then(invocation -> {
            scanner.dioBuf = new byte[]{4, 5, 6, 7, 0};
            return null;
        });
        int[] data = new int[]{9, 2, 3};
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        scanner.directIO(DLSJposConst.DIO_REQ_SCALE_GEN_MGT, data, stream);
        assertThat(data[0]).isEqualTo(1);
    }

    @Test
    public void directIOScaleVenMGT() throws DLSException {
        DLSUSBScanner scanner = new DLSUSBScanner();
        scanner.port = _port;
        when(_port.sendData(DLSUSBScanner.CMD_SCALE_VEN_MGT_INFO, DLSUSBScanner.CMD_SCALE_VEN_MGT_INFO.length)).then(invocation -> {
            scanner.dioBuf = new byte[]{4, 5, 6, 7, 0, 65, 66, 67};
            return null;
        });
        int[] data = new int[]{9, 2, 3};
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        scanner.directIO(DLSJposConst.DIO_REQ_SCALE_VEN_MGT, data, stream);
        assertThat(data[0]).isEqualTo(0);
        assertThat(new String(stream.toByteArray())).isEqualTo("ABC");
    }

    @Test
    public void directIOScaleVenMGTFewValues() throws DLSException {
        DLSUSBScanner scanner = new DLSUSBScanner();
        scanner.port = _port;
        when(_port.sendData(DLSUSBScanner.CMD_SCALE_VEN_MGT_INFO, DLSUSBScanner.CMD_SCALE_VEN_MGT_INFO.length)).then(invocation -> {
            scanner.dioBuf = new byte[]{4, 5, 6, 7, 0};
            return null;
        });
        int[] data = new int[]{9, 2, 3};
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        scanner.directIO(DLSJposConst.DIO_REQ_SCALE_VEN_MGT, data, stream);
        assertThat(data[0]).isEqualTo(1);
    }

    @Test
    public void directIOReadDWMLabelFilter() throws DLSException {
        DLSUSBScanner scanner = new DLSUSBScanner();
        scanner.port = _port;
        when(_port.sendData(DLSUSBScanner.CMD_READ_DWM_LABELFILTER, DLSUSBScanner.CMD_READ_DWM_LABELFILTER.length)).then(invocation -> {
            scanner.dioBuf = new byte[]{4, 5, 6, 7, 0, 65, 66, 67};
            scanner.stat2 = 8;
            scanner.stat0 = 64;
            return null;
        });
        int[] data = new int[]{9, 2, 3};
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        scanner.directIO(DLSJposConst.DIO_READ_DWM_LABELFILTER, data, stream);
        assertThat(data[0]).isEqualTo(0);
        assertThat(new String(stream.toByteArray())).isEqualTo("ABC");
    }

    @Test
    public void directIOReadDWMLabelFilterEmpty() throws DLSException {
        DLSUSBScanner scanner = new DLSUSBScanner();
        scanner.port = _port;
        when(_port.sendData(DLSUSBScanner.CMD_READ_DWM_LABELFILTER, DLSUSBScanner.CMD_READ_DWM_LABELFILTER.length)).then(invocation -> {
            scanner.dioBuf = new byte[]{4, 5, 6, 7, 0, 65, 66, 67};
            scanner.stat2 = 8;
            scanner.stat0 = 63;
            return null;
        });
        int[] data = new int[]{9, 2, 3};
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        scanner.directIO(DLSJposConst.DIO_READ_DWM_LABELFILTER, data, stream);
        assertThat(data[0]).isEqualTo(0);
        assertThat(new String(stream.toByteArray())).contains("empty");
    }

    @Test
    public void directIOReadDWMLabelFailed() throws DLSException {
        DLSUSBScanner scanner = new DLSUSBScanner();
        scanner.port = _port;
        when(_port.sendData(DLSUSBScanner.CMD_READ_DWM_LABELFILTER, DLSUSBScanner.CMD_READ_DWM_LABELFILTER.length)).then(invocation -> {
            scanner.dioBuf = new byte[]{4, 5, 6, 7, 0, 65, 66, 67};
            scanner.stat2 = 7;
            scanner.stat0 = 63;
            return null;
        });
        int[] data = new int[]{9, 2, 3};
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        scanner.directIO(DLSJposConst.DIO_READ_DWM_LABELFILTER, data, stream);
        assertThat(data[0]).isEqualTo(1);
    }

    @Test
    public void directIOClearDWMLabel() throws DLSException {
        DLSUSBScanner scanner = new DLSUSBScanner();
        scanner.port = _port;
        when(_port.sendData(DLSUSBScanner.CMD_CLEAR_DWM_LABELFILTER, DLSUSBScanner.CMD_CLEAR_DWM_LABELFILTER.length)).then(invocation -> {
            scanner.dioBuf = new byte[]{4, 5, 6, 7, 0, 65, 66, 67};
            scanner.stat2 = 8;
            scanner.stat0 = 63;
            return null;
        });
        int[] data = new int[]{9, 2, 3};
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        scanner.directIO(DLSJposConst.DIO_CLEAR_DWM_LABELFILTER, data, stream);
        assertThat(data[0]).isEqualTo(0);
    }

    @Test
    public void directIOClearDWMLabelFails() throws DLSException {
        DLSUSBScanner scanner = new DLSUSBScanner();
        scanner.port = _port;
        when(_port.sendData(DLSUSBScanner.CMD_CLEAR_DWM_LABELFILTER, DLSUSBScanner.CMD_CLEAR_DWM_LABELFILTER.length)).then(invocation -> {
            scanner.dioBuf = new byte[]{4, 5, 6, 7, 0, 65, 66, 67};
            scanner.stat2 = 7;
            scanner.stat0 = 63;
            return null;
        });
        int[] data = new int[]{9, 2, 3};
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        scanner.directIO(DLSJposConst.DIO_CLEAR_DWM_LABELFILTER, data, stream);
        assertThat(data[0]).isEqualTo(1);
    }

    @Test
    public void directIOAppendDWMLabelFilter() throws DLSException {
        DLSUSBScanner scanner = new DLSUSBScanner();
        scanner.port = _port;
        byte[] command = new byte[]{0x35, (byte) 0xFF, (byte) 0xED, 9, 2, 3};
        when(_port.sendData(command, command.length)).then(invocation -> {
            scanner.dioBuf = new byte[]{4, 5, 6, 7, 0, 65, 66, 67};
            scanner.stat2 = 8;
            scanner.stat0 = 63;
            return null;
        });
        int[] data = new int[]{9, 2, 3};
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        scanner.directIO(DLSJposConst.DIO_APPEND_DWM_LABELFILTER, data, stream);
        assertThat(data[0]).isEqualTo(DLSJposConst.DL_ACK);
    }

    @Test
    public void directIOAppendDWMLabelFilterCommanNotAccepted() throws DLSException {
        DLSUSBScanner scanner = new DLSUSBScanner();
        scanner.port = _port;
        byte[] command = new byte[]{0x35, (byte) 0xFF, (byte) 0xED, 9, 2, 3};
        when(_port.sendData(command, command.length)).then(invocation -> {
            scanner.dioBuf = new byte[]{4, 5, 6, 7, 0, 65, 66, 67};
            scanner.stat2 = 16;
            scanner.stat0 = 63;
            return null;
        });
        int[] data = new int[]{9, 2, 3};
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        scanner.directIO(DLSJposConst.DIO_APPEND_DWM_LABELFILTER, data, stream);
        assertThat(data[0]).isEqualTo(1);
    }

    @Test
    public void directIOAppendDWMLabelFilterError() throws DLSException {
        DLSUSBScanner scanner = new DLSUSBScanner();
        scanner.port = _port;
        byte[] command = new byte[]{0x35, (byte) 0xFF, (byte) 0xED, 9, 2, 3};
        when(_port.sendData(command, command.length)).then(invocation -> {
            scanner.dioBuf = new byte[]{4, 5, 6, 7, 0, 65, 66, 67};
            scanner.stat2 = 7;
            scanner.stat0 = 63;
            return null;
        });
        int[] data = new int[]{9, 2, 3};
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        scanner.directIO(DLSJposConst.DIO_APPEND_DWM_LABELFILTER, data, stream);
        assertThat(data[0]).isEqualTo(DLSJposConst.DL_NAK);
    }

    @Test
    public void directIOEnableBeep() throws DLSException {
        DLSUSBScanner scanner = new DLSUSBScanner();
        scanner.port = _port;
        when(_port.sendData(DLSUSBScanner.enableBeepCmd)).then(invocation -> {
            scanner.gotStatus = true;
            scanner.dioBuf = new byte[]{4, 5, 6, 7, 0, 65, 66, 67};
            scanner.stat2 = 7;
            scanner.stat0 = 0x10;
            return null;
        });
        int[] data = new int[]{9, 2, 3};
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        scanner.directIO(DLSJposConst.DIO_SCANNER_ENABLE_BEEP, data, stream);
        assertThat(data[0]).isEqualTo(0);
    }

    @Test
    public void directIOEnableBeepFail() throws DLSException {
        DLSUSBScanner scanner = new DLSUSBScanner();
        scanner.port = _port;
        when(_port.sendData(DLSUSBScanner.enableBeepCmd)).then(invocation -> {
            scanner.gotStatus = true;
            scanner.dioBuf = new byte[]{4, 5, 6, 7, 0, 65, 66, 67};
            scanner.stat2 = 7;
            scanner.stat0 = 7;
            return null;
        });
        int[] data = new int[]{9, 2, 3};
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        scanner.directIO(DLSJposConst.DIO_SCANNER_ENABLE_BEEP, data, stream);
        assertThat(data[0]).isEqualTo(1);
    }

    @Test
    public void directIODisableBeep() throws DLSException {
        DLSUSBScanner scanner = new DLSUSBScanner();
        scanner.port = _port;
        when(_port.sendData(DLSUSBScanner.disableBeepCmd)).then(invocation -> {
            scanner.gotStatus = true;
            scanner.dioBuf = new byte[]{4, 5, 6, 7, 0, 65, 66, 67};
            scanner.stat2 = 7;
            scanner.stat0 = 0x00;
            return null;
        });
        int[] data = new int[]{9, 2, 3};
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        scanner.directIO(DLSJposConst.DIO_SCANNER_DISABLE_BEEP, data, stream);
        assertThat(data[0]).isEqualTo(0);
    }

    @Test
    public void directIODisableBeepFails() throws DLSException {
        DLSUSBScanner scanner = new DLSUSBScanner();
        scanner.port = _port;
        when(_port.sendData(DLSUSBScanner.disableBeepCmd)).then(invocation -> {
            scanner.gotStatus = true;
            scanner.dioBuf = new byte[]{4, 5, 6, 7, 0, 65, 66, 67};
            scanner.stat2 = 7;
            scanner.stat0 = 0x10;
            return null;
        });
        int[] data = new int[]{9, 2, 3};
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        scanner.directIO(DLSJposConst.DIO_SCANNER_DISABLE_BEEP, data, stream);
        assertThat(data[0]).isEqualTo(1);
    }

    @Test
    public void directIOScannerDisable() throws DLSException {
        DLSUSBScanner scanner = new DLSUSBScanner();
        scanner.port = _port;
        when(_port.sendData(DLSUSBScanner.disableCmd)).then(invocation -> {
            scanner.statusBuf = new byte[]{4, 5, 6, 7, 0, 65, 66, 67};
            scanner.internalDIOSuccess = true;
            return null;
        });
        int[] data = new int[]{9, 2, 3};
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        scanner.directIO(DLSJposConst.DIO_SCANNER_DISABLE, data, stream);
        assertThat(data[0]).isEqualTo(0);
    }

    @Test
    public void directIOScannerEnable() throws DLSException {
        DLSUSBScanner scanner = new DLSUSBScanner();
        scanner.port = _port;
        when(_port.sendData(DLSUSBScanner.enableCmd)).then(invocation -> {
            scanner.statusBuf = new byte[]{4, 5, 6, 7, 0, 65, 66, 67};
            scanner.internalDIOSuccess = true;
            return null;
        });
        int[] data = new int[]{9, 2, 3};
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        scanner.directIO(DLSJposConst.DIO_SCANNER_ENABLE, data, stream);
        assertThat(data[0]).isEqualTo(0);
    }

    @Test
    public void directIOScannerConfigure() throws DLSException {
        DLSUSBScanner scanner = new DLSUSBScanner();
        scanner.port = _port;
        byte[] cmd = new byte[]{0x20, 0, 10, 11, 12, 13, 14, 15, 16, 17, 18};
        when(_port.sendData(cmd)).then(invocation -> {
            scanner.gotStatus = true;
            scanner.stat2 = 1;
            scanner.stat0 = 0x10;
            return null;
        });
        int[] data = new int[]{10, 11, 12, 13, 14, 15, 16, 17, 18};
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        scanner.directIO(DLSJposConst.DIO_SCANNER_CONFIGURE, data, stream);
        assertThat(data[0]).isEqualTo(0);
    }

    @Test
    public void directIOScannerConfigureCoerced() throws DLSException {
        DLSUSBScanner scanner = new DLSUSBScanner();
        scanner.port = _port;
        byte[] cmd = new byte[]{0x20, 0, 10, 11, 12, 13, 14, 15, 16, 17, 18};
        when(_port.sendData(cmd)).then(invocation -> {
            scanner.gotStatus = true;
            scanner.stat2 = 3;
            scanner.stat0 = 0x10;
            return null;
        });
        int[] data = new int[]{10, 11, 12, 13, 14, 15, 16, 17, 18};
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        scanner.directIO(DLSJposConst.DIO_SCANNER_CONFIGURE, data, stream);
        assertThat(data[0]).isEqualTo(0);
    }

    @Test
    public void directIOScannerConfigureFailed() throws DLSException {
        DLSUSBScanner scanner = new DLSUSBScanner();
        scanner.port = _port;
        byte[] cmd = new byte[]{0x20, 0, 10, 11, 12, 13, 14, 15, 16, 17, 18};
        when(_port.sendData(cmd)).then(invocation -> {
            scanner.gotStatus = true;
            scanner.stat2 = 4;
            scanner.stat0 = 0x10;
            return null;
        });
        int[] data = new int[]{10, 11, 12, 13, 14, 15, 16, 17, 18};
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        scanner.directIO(DLSJposConst.DIO_SCANNER_CONFIGURE, data, stream);
        assertThat(data[0]).isEqualTo(1);
    }

    @Test
    public void directIOScannerConfigureFewArgs() throws DLSException {
        DLSUSBScanner scanner = new DLSUSBScanner();
        scanner.port = _port;
        int[] data = new int[]{10, 11, 12, 13, 14, 15, 16, 17};
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        scanner.directIO(DLSJposConst.DIO_SCANNER_CONFIGURE, data, stream);
        assertThat(data[0]).isEqualTo(-1);
    }

    @Test
    public void directIOScannerReportConfig() throws DLSException {
        DLSUSBScanner scanner = new DLSUSBScanner();
        scanner.port = _port;
        when(_port.sendData(DLSUSBScanner.reportConfigCmd, DLSUSBScanner.reportConfigCmd.length)).then(invocation -> {
            scanner.reportBuf = new byte[]{0, 2, 2, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0};
            return null;
        });
        int[] data = new int[]{10, 11, 12, 13, 14, 15, 16, 17, 18};
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        scanner.directIO(DLSJposConst.DIO_SCANNER_REPORT_CONFIG, data, stream);
        assertThat(data[0]).isEqualTo(0);
        assertThat(new String(stream.toByteArray())).isNotEmpty();
    }

    @Test
    public void directIOScannerReportConfigFail() throws DLSException {
        DLSUSBScanner scanner = new DLSUSBScanner();
        scanner.port = _port;
        when(_port.sendData(DLSUSBScanner.reportConfigCmd, DLSUSBScanner.reportConfigCmd.length)).then(invocation -> {
            scanner.reportBuf = new byte[]{0, 1, 2, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0};
            return null;
        });
        int[] data = new int[]{10, 11, 12, 13, 14, 15, 16, 17, 18};
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        scanner.directIO(DLSJposConst.DIO_SCANNER_REPORT_CONFIG, data, stream);
        assertThat(data[0]).isEqualTo(1);
        assertThat(new String(stream.toByteArray())).isNotEmpty();
    }

    @Test
    public void directIOConfig2LabelCmd() throws DLSException {
        DLSUSBScanner scanner = new DLSUSBScanner();
        scanner.port = _port;
        when(_port.sendData(scanner.config2LabelCmd)).then(invocation -> {
            scanner.gotStatus = true;
            scanner.stat2 = 0x04;
            scanner.stat0 = 0x10;
            return null;
        });
        int[] data = new int[]{10, 11, 12, 13, 14, 15, 16, 17};
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        scanner.directIO(DLSJposConst.DIO_SCANNER_CONFIG_2LABEL, data, stream);
        assertThat(data[0]).isEqualTo(0);
    }

    @Test
    public void directIOConfig2LabelCmdFail() throws DLSException {
        DLSUSBScanner scanner = new DLSUSBScanner();
        scanner.port = _port;
        when(_port.sendData(scanner.config2LabelCmd)).then(invocation -> {
            scanner.gotStatus = true;
            scanner.stat2 = 2;
            scanner.stat0 = 0x10;
            return null;
        });
        int[] data = new int[]{10, 11, 12, 13, 14, 15, 16, 17};
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        scanner.directIO(DLSJposConst.DIO_SCANNER_CONFIG_2LABEL, data, stream);
        assertThat(data[0]).isEqualTo(1);
    }

    @Test
    public void directIOConfig2LabelCmdFewArguments() throws DLSException {
        DLSUSBScanner scanner = new DLSUSBScanner();
        scanner.port = _port;
        int[] data = new int[]{10, 11, 12, 13, 14, 15, 16};
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        scanner.directIO(DLSJposConst.DIO_SCANNER_CONFIG_2LABEL, data, stream);
        assertThat(data[0]).isEqualTo(-1);
    }

    @Test
    public void directIOReport2LabelCmd() throws DLSException {
        DLSUSBScanner scanner = new DLSUSBScanner();
        scanner.port = _port;
        when(_port.sendData(DLSUSBScanner.reportConfig2LabelCmd, DLSUSBScanner.reportConfig2LabelCmd.length)).then(invocation -> {
            scanner.reportBuf = new byte[]{1, 4, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12};
            return null;
        });
        int[] data = new int[]{10, 11, 12, 13, 14, 15, 16, 17};
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        scanner.directIO(DLSJposConst.DIO_SCANNER_REPORT_2LABEL, data, stream);
        assertThat(data[0]).isEqualTo(0);
        assertThat(stream.toByteArray()).isEqualTo(new byte[]{5, 6, 7, 8, 9, 10, 11, 12});
    }

    @Test
    public void directIOReport2LabelCmdTimeout() throws DLSException {
        DLSUSBScanner scanner = new DLSUSBScanner();
        scanner.port = _port;
        int[] data = new int[]{10, 11, 12, 13, 14, 15, 16, 17};
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        scanner.directIO(DLSJposConst.DIO_SCANNER_REPORT_2LABEL, data, stream);
        assertThat(data[0]).isEqualTo(-1);
    }

    @Test
    public void directIOReport2LabelCmdFewData() throws DLSException {
        DLSUSBScanner scanner = new DLSUSBScanner();
        scanner.port = _port;
        when(_port.sendData(DLSUSBScanner.reportConfig2LabelCmd, DLSUSBScanner.reportConfig2LabelCmd.length)).then(invocation -> {
            scanner.reportBuf = new byte[]{1, 4, 3, 4, 5, 6, 7, 8, 9, 10, 11};
            return null;
        });
        int[] data = new int[]{10, 11, 12, 13, 14, 15, 16, 17};
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        scanner.directIO(DLSJposConst.DIO_SCANNER_REPORT_2LABEL, data, stream);
        assertThat(data[0]).isEqualTo(-1);
    }

    @Test
    public void directIOReport2LabelCmdFail() throws DLSException {
        DLSUSBScanner scanner = new DLSUSBScanner();
        scanner.port = _port;
        when(_port.sendData(DLSUSBScanner.reportConfig2LabelCmd, DLSUSBScanner.reportConfig2LabelCmd.length)).then(invocation -> {
            scanner.reportBuf = new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12};
            return null;
        });
        int[] data = new int[]{10, 11, 12, 13, 14, 15, 16, 17};
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        scanner.directIO(DLSJposConst.DIO_SCANNER_REPORT_2LABEL, data, stream);
        assertThat(data[0]).isEqualTo(1);
        assertThat(stream.toByteArray()).isEqualTo(new byte[]{5, 6, 7, 8, 9, 10, 11, 12});
    }

    @Test
    public void directIOInformation() throws DLSException {
        DLSUSBScanner scanner = new DLSUSBScanner();
        scanner.port = _port;
        byte[] cmd = new byte[]{48, 0, DLSUSBScanner.INFO_CMD, 0, 0, 0, 0, 0, 0, 0, 0};
        when(_port.sendData(cmd, cmd.length)).then(invocation -> {
            scanner.dioBuf = new byte[]{1, 4, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12};
            scanner.internalDIOSuccess = true;
            return null;
        });
        int[] data = new int[]{10, 11, 12, 13, 14, 15, 16, 17};
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        scanner.directIO(DLSJposConst.DIO_SCANNER_DIO_INFORMATION, data, stream);
        assertThat(data[0]).isEqualTo(0);
    }

    @Test
    public void directIOScannerHealth() throws DLSException {
        DLSUSBScanner scanner = new DLSUSBScanner();
        scanner.port = _port;
        byte[] cmd = new byte[]{48, 0, DLSUSBScanner.HEALTH_CMD, 0, 0, 0, 0, 0, 0, 0, 0};
        when(_port.sendData(cmd, cmd.length)).then(invocation -> {
            scanner.dioBuf = new byte[]{1, 4, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12};
            scanner.internalDIOSuccess = true;
            return null;
        });
        int[] data = new int[]{10, 11, 12, 13, 14, 15, 16, 17};
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        scanner.directIO(DLSJposConst.DIO_SCANNER_DIO_HEALTH, data, stream);
        assertThat(data[0]).isEqualTo(0);
    }

    @Test
    public void directIOScannerStats() throws DLSException {
        DLSUSBScanner scanner = new DLSUSBScanner();
        scanner.port = _port;
        byte[] cmd = new byte[]{48, 0, DLSUSBScanner.STATISTICS_CMD, 0, 0, 0, 0, 0, 0, 0, 0};
        when(_port.sendData(cmd, cmd.length)).then(invocation -> {
            scanner.dioBuf = new byte[]{1, 4, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12};
            scanner.internalDIOSuccess = true;
            return null;
        });
        int[] data = new int[]{10, 11, 12, 13, 14, 15, 16, 17};
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        scanner.directIO(DLSJposConst.DIO_SCANNER_DIO_STATS, data, stream);
        assertThat(data[0]).isEqualTo(0);
    }

    @Test
    public void directIOScannerLogCount() throws DLSException {
        DLSUSBScanner scanner = new DLSUSBScanner();
        scanner.port = _port;
        scanner.options = mock(DLSProperties.class);
        when(_port.sendData(DLSUSBScanner.getNumberStats)).then(invocation -> {
            scanner.dioBuf = new byte[]{1, 4, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12};
            scanner.internalDIO = false;
            scanner.internalDIOSuccess = true;
            return null;
        });
        int[] data = new int[]{10, 11, 12, 13, 14, 15, 16, 17};
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        scanner.directIO(DLSJposConst.DIO_READ_STAT_LOG_COUNT, data, stream);
        assertThat(data[0]).isEqualTo(0);
    }

    @Test
    public void directIOScannerLogItems() throws DLSException {
        DLSUSBScanner scanner = new DLSUSBScanner();
        scanner.port = _port;
        when(_port.sendData(DLSUSBScanner.getNumberStats, DLSUSBScanner.getNumberStats.length)).then(invocation -> {
            scanner.dioBuf = new byte[]{1, 4, 3, 4, 5, 6, 2, 8, 9, 10, 11, 12};
            scanner.internalDIOSuccess = true;
            return null;
        });
        when(_port.sendData(DLSUSBScanner.getStat, DLSUSBScanner.getStat.length)).then(invocation -> {
            scanner.dioBuf = new byte[]{12, 4, 3, 4, 5, 6, 2, 0, 0, 0, 11, 12};
            scanner.internalDIOSuccess = true;
            return null;
        });
        int[] data = new int[]{10, 11, 12, 13, 14, 15, 16, 17};
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        scanner.directIO(DLSJposConst.DIO_READ_STAT_LOG_ITEMS, data, stream);
        assertThat(data[0]).isEqualTo(0);
        String[] streams = new String(stream.toByteArray()).split(",");
        assertThat(streams[0].split(":")[1].trim()).isEqualTo("006");
        assertThat(streams[1].split(":")[1].trim()).isEqualTo("02");
        assertThat(streams[2].split(":")[1].trim()).isEqualTo("00011");
        assertThat(streams[3].split(":")[1].getBytes()[1]).isEqualTo(12);
    }

    @Test
    public void directIOScannerReadLogItems() throws DLSException {
        DLSUSBScanner scanner = new DLSUSBScanner();
        scanner.port = _port;
        when(_port.sendData(DLSUSBScanner.getStatN, DLSUSBScanner.getStatN.length)).then(invocation -> {
            scanner.dioBuf = new byte[]{1, 0x40, 3, 4, 5, 6, 2, 8, 9, 10, 11, 12};
            scanner.internalDIOSuccess = true;
            return null;
        });
        int[] data = new int[]{10, 11, 12, 13, 14, 15, 16, 17};
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        scanner.directIO(DLSJposConst.DIO_READ_EVENT_LOG_ITEMS, data, stream);
        assertThat(data[0]).isEqualTo(0);
    }


    @Test
    public void directIOScannerReadLogItemsFails() throws DLSException {
        DLSUSBScanner scanner = new DLSUSBScanner();
        scanner.port = _port;
        when(_port.sendData(DLSUSBScanner.getStatN, DLSUSBScanner.getStatN.length)).then(invocation -> {
            scanner.dioBuf = new byte[]{1, 8, 3, 4, 5, 6, 2, 8, 9, 10, 11, 12};
            scanner.internalDIOSuccess = true;
            return null;
        });
        int[] data = new int[]{10, 11, 12, 13, 14, 15, 16, 17};
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        scanner.directIO(DLSJposConst.DIO_READ_EVENT_LOG_ITEMS, data, stream);
        assertThat(data[0]).isEqualTo(1);
    }

    @Test
    public void directIOReadLogLastEvent() throws DLSException {
        DLSUSBScanner scanner = new DLSUSBScanner();
        scanner.port = _port;
        when(_port.sendData(DLSUSBScanner.getLastEvent, DLSUSBScanner.getLastEvent.length)).then(invocation -> {
            scanner.dioBuf = new byte[]{1, 0x40, 3, 4, 5, 6, 2, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20};
            scanner.internalDIOSuccess = true;
            return null;
        });
        int[] data = new int[]{10, 11, 12, 13, 14, 15, 16, 17};
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        scanner.directIO(DLSJposConst.DIO_READ_EVENT_LOG_LAST_EVENT, data, stream);
        assertThat(data[0]).isEqualTo(0);
    }

    @Test
    public void directIOClearEventLog() throws DLSException {
        DLSUSBScanner scanner = new DLSUSBScanner();
        scanner.port = _port;
        scanner.options = mock(DLSProperties.class);
        when(_port.sendData(DLSUSBScanner.clearEventLog)).then(invocation -> {
            scanner.dioBuf = new byte[]{1, 0x40, 3, 4, 5, 6, 2, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20};
            scanner.internalDIO = false;
            scanner.internalDIOSuccess = true;
            return null;
        });
        int[] data = new int[]{10, 11, 12, 13, 14, 15, 16, 17};
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        scanner.directIO(DLSJposConst.DIO_CLEAR_EVENT_LOG, data, stream);
        assertThat(data[0]).isEqualTo(0);
    }

    @Test
    public void directIOSaveEventLogDefaultName() throws DLSException {
        DLSUSBScanner scanner = new DLSUSBScanner();
        scanner.port = _port;
        scanner.scannerConfig = mock(DLSScannerConfig.class);
        when(scanner.scannerConfig.getLogicalName()).thenReturn("Test");
        when(_port.sendData(DLSUSBScanner.getStatN, DLSUSBScanner.getStatN.length)).then(invocation -> {
            scanner.dioBuf = new byte[]{1, 0x40, 3, 4, 5, 6, 2, 8, 9, 10, 11, 12};
            scanner.internalDIOSuccess = true;
            return null;
        });
        int[] data = new int[]{10, 11, 12, 13, 14, 15, 16, 17};
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        scanner.directIO(DLSJposConst.DIO_SAVE_EVENT_LOG, data, stream);
        assertThat(data[0]).isEqualTo(0);
        assertThat(new String(stream.toByteArray())).startsWith("Test_event.log");
    }

    @Test
    public void directIOSaveEventLogWithName() throws DLSException, IOException {
        DLSUSBScanner scanner = new DLSUSBScanner();
        scanner.port = _port;
        scanner.scannerConfig = mock(DLSScannerConfig.class);
        when(scanner.scannerConfig.getLogicalName()).thenReturn("Test");
        when(_port.sendData(DLSUSBScanner.getStatN, DLSUSBScanner.getStatN.length)).then(invocation -> {
            scanner.dioBuf = new byte[]{1, 0x40, 3, 4, 5, 6, 2, 8, 9, 10, 11, 12};
            scanner.internalDIOSuccess = true;
            return null;
        });
        int[] data = new int[]{10, 11, 12, 13, 14, 15, 16, 17};
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        stream.write("Input.log".getBytes());
        stream.close();
        scanner.directIO(DLSJposConst.DIO_SAVE_EVENT_LOG, data, stream);
        assertThat(data[0]).isEqualTo(0);
        assertThat(new String(stream.toByteArray())).startsWith("Input.log");
    }

    @Test
    public void directIOSaveEventLogFails() throws DLSException {
        DLSUSBScanner scanner = new DLSUSBScanner();
        scanner.port = _port;
        scanner.scannerConfig = mock(DLSScannerConfig.class);
        when(scanner.scannerConfig.getLogicalName()).thenReturn("Test");
        when(_port.sendData(DLSUSBScanner.getStatN, DLSUSBScanner.getStatN.length)).then(invocation -> {
            scanner.dioBuf = new byte[]{1, 2, 3, 4, 5, 6, 2, 8, 9, 10, 11, 12};
            scanner.internalDIOSuccess = true;
            return null;
        });
        int[] data = new int[]{10, 11, 12, 13, 14, 15, 16, 17};
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        scanner.directIO(DLSJposConst.DIO_SAVE_EVENT_LOG, data, stream);
        assertThat(data[0]).isEqualTo(1);
    }

    @Test
    public void directIOSaveStatsLogDefaultName() throws DLSException {
        DLSUSBScanner scanner = new DLSUSBScanner();
        scanner.port = _port;
        scanner.scannerConfig = mock(DLSScannerConfig.class);
        when(scanner.scannerConfig.getLogicalName()).thenReturn("Test");
        when(_port.sendData(DLSUSBScanner.getNumberStats, DLSUSBScanner.getNumberStats.length)).then(invocation -> {
            scanner.dioBuf = new byte[]{1, 4, 3, 4, 5, 6, 2, 8, 9, 10, 11, 12};
            scanner.internalDIOSuccess = true;
            return null;
        });
        when(_port.sendData(DLSUSBScanner.getStat, DLSUSBScanner.getStat.length)).then(invocation -> {
            scanner.dioBuf = new byte[]{12, 0x40, 3, 4, 5, 6, 2, 0, 0, 0, 11, 12};
            scanner.internalDIOSuccess = true;
            return null;
        });
        int[] data = new int[]{10, 11, 12, 13, 14, 15, 16, 17};
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        scanner.directIO(DLSJposConst.DIO_SAVE_STATS_LOG, data, stream);
        assertThat(data[0]).isEqualTo(0);
        assertThat(new String(stream.toByteArray())).startsWith("Test_stats.log");
    }

    @Test
    public void directIOSaveStatsLogInputName() throws DLSException, IOException {
        DLSUSBScanner scanner = new DLSUSBScanner();
        scanner.port = _port;
        scanner.scannerConfig = mock(DLSScannerConfig.class);
        when(scanner.scannerConfig.getLogicalName()).thenReturn("Test");
        when(_port.sendData(DLSUSBScanner.getNumberStats, DLSUSBScanner.getNumberStats.length)).then(invocation -> {
            scanner.dioBuf = new byte[]{1, 4, 3, 4, 5, 6, 2, 8, 9, 10, 11, 12};
            scanner.internalDIOSuccess = true;
            return null;
        });
        when(_port.sendData(DLSUSBScanner.getStat, DLSUSBScanner.getStat.length)).then(invocation -> {
            scanner.dioBuf = new byte[]{12, 0x40, 3, 4, 5, 6, 2, 0, 0, 0, 11, 12};
            scanner.internalDIOSuccess = true;
            return null;
        });
        int[] data = new int[]{10, 11, 12, 13, 14, 15, 16, 17};
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        stream.write("Input.log".getBytes());
        stream.close();
        scanner.directIO(DLSJposConst.DIO_SAVE_STATS_LOG, data, stream);
        assertThat(data[0]).isEqualTo(0);
        assertThat(new String(stream.toByteArray())).startsWith("Input.log");
    }

    @Test
    public void directIOSaveStatsLogFails() throws DLSException {
        DLSUSBScanner scanner = new DLSUSBScanner();
        scanner.port = _port;
        scanner.scannerConfig = mock(DLSScannerConfig.class);
        when(scanner.scannerConfig.getLogicalName()).thenReturn("Test");
        when(_port.sendData(DLSUSBScanner.getNumberStats, DLSUSBScanner.getNumberStats.length)).then(invocation -> {
            scanner.dioBuf = new byte[]{1, 4, 3, 4, 5, 6, 2, 8, 9, 10, 11, 12};
            scanner.internalDIOSuccess = true;
            return null;
        });
        when(_port.sendData(DLSUSBScanner.getStat, DLSUSBScanner.getStat.length)).then(invocation -> {
            scanner.dioBuf = new byte[]{12, 1, 3, 4, 5, 6, 2, 0, 0, 0, 11, 12};
            scanner.internalDIOSuccess = true;
            return null;
        });
        int[] data = new int[]{10, 11, 12, 13, 14, 15, 16, 17};
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        scanner.directIO(DLSJposConst.DIO_SAVE_STATS_LOG, data, stream);
        assertThat(data[0]).isEqualTo(1);
    }

    @Test
    public void directIOScaleSEntryStatusBlocked() throws DLSException {
        DLSUSBScanner scanner = new DLSUSBScanner();
        scanner.port = _port;
        scanner.options = mock(DLSProperties.class);
        when(_port.sendData(DLSUSBScanner.getScaleSentryStatus)).then(invocation -> {
            scanner.internalDIO = false;
            scanner.dioBuf = new byte[]{1, 4, 3, 4, 5, 6, 2, 8, 9, 10, 11, 12};
            scanner.internalDIOSuccess = true;
            return null;
        });
        int[] data = new int[]{10, 11, 12, 13, 14, 15, 16, 17};
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        scanner.directIO(DLSJposConst.DIO_GET_SCALE_SENTRY_STATUS, data, stream);
        assertThat(data[0]).isEqualTo(0);
        assertThat(new String(stream.toByteArray())).contains("BLOCKED");
    }

    @Test
    public void directIOScaleSEntryStatusNotBlocked() throws DLSException {
        DLSUSBScanner scanner = new DLSUSBScanner();
        scanner.port = _port;
        scanner.options = mock(DLSProperties.class);
        when(_port.sendData(DLSUSBScanner.getScaleSentryStatus)).then(invocation -> {
            scanner.internalDIO = false;
            scanner.dioBuf = new byte[]{1, 4, 3, 4, 5, 0x15, 2, 8, 9, 10, 11, 12};
            scanner.internalDIOSuccess = true;
            return null;
        });
        int[] data = new int[]{10, 11, 12, 13, 14, 15, 16, 17};
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        scanner.directIO(DLSJposConst.DIO_GET_SCALE_SENTRY_STATUS, data, stream);
        assertThat(data[0]).isEqualTo(0);
        assertThat(new String(stream.toByteArray())).contains("NOT blocked");
    }

    @Test
    public void directIOClearScaleSEntry() throws DLSException {
        DLSUSBScanner scanner = new DLSUSBScanner();
        scanner.port = _port;
        scanner.options = mock(DLSProperties.class);
        when(_port.sendData(DLSUSBScanner.clearScaleSentryStatus)).then(invocation -> {
            scanner.internalDIO = false;
            scanner.dioBuf = new byte[]{1, 4, 3, 4, 5, 6, 2, 8, 9, 10, 11, 12};
            scanner.internalDIOSuccess = true;
            return null;
        });
        int[] data = new int[]{10, 11, 12, 13, 14, 15, 16, 17};
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        scanner.directIO(DLSJposConst.DIO_CLEAR_SCALE_SENTRY, data, stream);
        assertThat(data[0]).isEqualTo(0);
    }

    @Test
    public void directIOReadConfigItem() throws DLSException, IOException {
        DLSUSBScanner scanner = new DLSUSBScanner();
        scanner.port = _port;
        scanner.options = mock(DLSProperties.class);
        when(_port.sendData(DLSUSBScanner.queryConfig, DLSUSBScanner.queryConfig.length)).then(invocation -> {
            scanner.internalDIO = false;
            scanner.dioBuf = new byte[]{1, 4, 3, 4, 5, 6, 2, 8, 9, 10, 11, 12};
            scanner.internalDIOSuccess = true;
            return null;
        });
        when(_port.sendData(DLSUSBScanner.readConfigNonInt, DLSUSBScanner.readConfigNonInt.length)).then(invocation -> {
            scanner.internalDIO = false;
            scanner.dioBuf = new byte[]{1, 4, 3, 4, 5, 6, 2, 8, 9, 10, 11, 12};
            scanner.internalDIOSuccess = true;
            return null;
        });
        int[] data = new int[]{10, 11, 12, 13, 14, 15, 16, 17};
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        stream.write("0502".getBytes());
        stream.close();
        scanner.directIO(DLSJposConst.DIO_READ_CONFIG_ITEM, data, stream);
        assertThat(data[0]).isEqualTo(DLSJposConst.DL_ACK);
    }

    @Test
    public void directIOReadConfigItemWrongObject() throws DLSException, IOException {
        DLSUSBScanner scanner = new DLSUSBScanner();
        scanner.port = _port;
        int[] data = new int[]{10, 11, 12, 13, 14, 15, 16, 17};
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        stream.write("0502s".getBytes());
        stream.close();
        scanner.directIO(DLSJposConst.DIO_READ_CONFIG_ITEM, data, stream);
        assertThat(data[0]).isEqualTo(-1);
    }

    @Test
    public void directIOReadConfigItemNAK() throws DLSException, IOException {
        DLSUSBScanner scanner = new DLSUSBScanner();
        scanner.port = _port;
        scanner.options = mock(DLSProperties.class);
        when(_port.sendData(DLSUSBScanner.queryConfig, DLSUSBScanner.queryConfig.length)).then(invocation -> {
            scanner.internalDIO = false;
            scanner.dioBuf = new byte[]{1, 4, 3, 4, 5, 8, 2, 8, 9, 10, 11, 12};
            scanner.internalDIOSuccess = true;
            return null;
        });
        when(_port.sendData(DLSUSBScanner.readConfigNonInt, DLSUSBScanner.readConfigNonInt.length)).then(invocation -> {
            scanner.internalDIO = false;
            scanner.dioBuf = new byte[]{1, 4, 3, 4, 5, 6, 2, 8, 9, 10, 11, 12};
            scanner.internalDIOSuccess = true;
            return null;
        });
        int[] data = new int[]{10, 11, 12, 13, 14, 15, 16, 17};
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        stream.write("0502".getBytes());
        stream.close();
        scanner.directIO(DLSJposConst.DIO_READ_CONFIG_ITEM, data, stream);
        assertThat(data[0]).isEqualTo(DLSJposConst.DL_NAK);
    }

    @Test
    public void directIOWriteConfigItem() throws IOException, DLSException {
        DLSUSBScanner scanner = new DLSUSBScanner();
        scanner.port = _port;
        scanner.options = mock(DLSProperties.class);
        when(_port.sendData(DLSUSBScanner.queryConfig, DLSUSBScanner.queryConfig.length)).then(invocation -> {
            scanner.internalDIO = false;
            scanner.dioBuf = new byte[]{1, 4, 3, 4, 5, 6, 2, 8, 9, 10, 11, 12};
            scanner.internalDIOSuccess = true;
            return null;
        });
        when(_port.sendData(DLSUSBScanner.readConfigNonInt, DLSUSBScanner.readConfigNonInt.length)).then(invocation -> {
            scanner.internalDIO = false;
            scanner.dioBuf = new byte[]{1, 4, 3, 4, 5, 6, 2, 8, 9, 10, 11, 12};
            scanner.internalDIOSuccess = true;
            return null;
        });
        int[] data = new int[]{10, 11, 12, 13, 14, 15, 16, 17};
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        stream.write("050209".getBytes());
        stream.close();
        scanner.directIO(DLSJposConst.DIO_WRITE_CONFIG_ITEM, data, stream);
        assertThat(data[0]).isEqualTo(DLSJposConst.DL_ACK);
    }

    @Test
    public void directIOWriteConfigItemEmptyVal() throws DLSException {
        DLSUSBScanner scanner = new DLSUSBScanner();
        scanner.port = _port;
        scanner.options = mock(DLSProperties.class);
        int[] data = new int[]{10, 11, 12, 13, 14, 15, 16, 17};
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        scanner.directIO(DLSJposConst.DIO_WRITE_CONFIG_ITEM, data, stream);
        assertThat(data[0]).isEqualTo(-1);
    }

    @Test
    public void directIOWriteConfigItemFewValues() throws DLSException, IOException {
        DLSUSBScanner scanner = new DLSUSBScanner();
        scanner.port = _port;
        scanner.options = mock(DLSProperties.class);
        int[] data = new int[]{10, 11, 12, 13, 14, 15, 16, 17};
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        stream.write("0502".getBytes());
        stream.close();
        scanner.directIO(DLSJposConst.DIO_WRITE_CONFIG_ITEM, data, stream);
        assertThat(data[0]).isEqualTo(DLSJposConst.DL_NAK);
    }

    @Test
    public void directIOWriteConfigItem2() throws IOException, DLSException {
        DLSUSBScanner scanner = new DLSUSBScanner();
        scanner.port = _port;
        scanner.options = mock(DLSProperties.class);
        when(_port.sendData(DLSUSBScanner.queryConfig, DLSUSBScanner.queryConfig.length)).then(invocation -> {
            scanner.internalDIO = false;
            scanner.dioBuf = new byte[]{1, 4, 3, 4, 5, 6, 2, 8, 9, 10, 11, 12};
            scanner.internalDIOSuccess = true;
            return null;
        });
        when(_port.sendData(DLSUSBScanner.readConfigNonInt, DLSUSBScanner.readConfigNonInt.length)).then(invocation -> {
            scanner.internalDIO = false;
            scanner.dioBuf = new byte[]{1, 4, 3, 4, 5, 6, 2, 8, 9, 10, 11, 12};
            scanner.internalDIOSuccess = true;
            return null;
        });
        byte[] cmd = new byte[]{48, 0, 116, 7, 5, 2, 0, 1, 6, 0, 0};
        when(_port.sendData(cmd, cmd.length)).then(invocation -> {
            scanner.internalDIO = false;
            scanner.dioBuf = new byte[]{1, 4, 3, 4, 5, 6, 2, 8, 9, 10, 11, 12};
            scanner.internalDIOSuccess = true;
            return null;
        });
        int[] data = new int[]{10, 11, 12, 13, 14, 15, 16, 17};
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        stream.write("050206".getBytes());
        stream.close();
        scanner.directIO(DLSJposConst.DIO_WRITE_CONFIG_ITEM, data, stream);
        assertThat(data[0]).isEqualTo(DLSJposConst.DL_ACK);
    }

    @Test
    public void directIOWriteConfigItem2Fail() throws IOException, DLSException {
        DLSUSBScanner scanner = new DLSUSBScanner();
        scanner.port = _port;
        scanner.options = mock(DLSProperties.class);
        when(_port.sendData(DLSUSBScanner.queryConfig, DLSUSBScanner.queryConfig.length)).then(invocation -> {
            scanner.internalDIO = false;
            scanner.dioBuf = new byte[]{1, 4, 3, 4, 5, 6, 2, 8, 9, 10, 11, 12};
            scanner.internalDIOSuccess = true;
            return null;
        });
        when(_port.sendData(DLSUSBScanner.readConfigNonInt, DLSUSBScanner.readConfigNonInt.length)).then(invocation -> {
            scanner.internalDIO = false;
            scanner.dioBuf = new byte[]{1, 4, 3, 4, 5, 6, 2, 8, 9, 10, 11, 12};
            scanner.internalDIOSuccess = true;
            return null;
        });
        byte[] cmd = new byte[]{48, 0, 116, 7, 5, 2, 0, 1, 6, 0, 0};
        when(_port.sendData(cmd, cmd.length)).then(invocation -> {
            scanner.internalDIO = false;
            scanner.dioBuf = new byte[]{};
            scanner.internalDIOSuccess = true;
            return null;
        });
        int[] data = new int[]{10, 11, 12, 13, 14, 15, 16, 17};
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        stream.write("050206".getBytes());
        stream.close();
        scanner.directIO(DLSJposConst.DIO_WRITE_CONFIG_ITEM, data, stream);
        assertThat(data[0]).isEqualTo(-1);
    }

    @Test
    public void directIOWriteConfigItemNAK() throws IOException, DLSException {
        DLSUSBScanner scanner = new DLSUSBScanner();
        scanner.port = _port;
        scanner.options = mock(DLSProperties.class);
        int[] data = new int[]{10, 11, 12, 13, 14, 15, 16, 17};
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        stream.write("05T206".getBytes());
        stream.close();
        scanner.directIO(DLSJposConst.DIO_WRITE_CONFIG_ITEM, data, stream);
        assertThat(data[0]).isEqualTo(DLSJposConst.DL_NAK);
    }

    @Test
    public void directIOWriteConfigItemNoPayload() throws IOException, DLSException {
        DLSUSBScanner scanner = new DLSUSBScanner();
        scanner.port = _port;
        scanner.options = mock(DLSProperties.class);
        when(_port.sendData(DLSUSBScanner.queryConfig, DLSUSBScanner.queryConfig.length)).then(invocation -> {
            scanner.internalDIO = false;
            scanner.dioBuf = new byte[]{2, 0, 3, 4, 5, 6, 2, 8, 9, 10, 11, 12};
            scanner.internalDIOSuccess = true;
            return null;
        });
        when(_port.sendData(DLSUSBScanner.readConfigInt, DLSUSBScanner.readConfigInt.length)).then(invocation -> {
            scanner.internalDIO = false;
            scanner.dioBuf = new byte[]{8, 4, 3, 4, 5, 6, 0, 6, 9, 10, 11, 12};
            scanner.internalDIOSuccess = true;
            return null;
        });
        int[] data = new int[]{10, 11, 12, 13, 14, 15, 16, 17};
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        stream.write("050206".getBytes());
        stream.close();
        scanner.directIO(DLSJposConst.DIO_WRITE_CONFIG_ITEM, data, stream);
        assertThat(data[0]).isEqualTo(DLSJposConst.DL_ACK);
    }

    @Test
    public void directIOWriteConfigItemNoPayload2() throws IOException, DLSException {
        DLSUSBScanner scanner = new DLSUSBScanner();
        scanner.port = _port;
        scanner.options = mock(DLSProperties.class);
        when(_port.sendData(DLSUSBScanner.queryConfig, DLSUSBScanner.queryConfig.length)).then(invocation -> {
            scanner.internalDIO = false;
            scanner.dioBuf = new byte[]{2, 0, 3, 4, 5, 6, 2, 8, 9, 10, 11, 12};
            scanner.internalDIOSuccess = true;
            return null;
        });
        when(_port.sendData(DLSUSBScanner.readConfigInt, DLSUSBScanner.readConfigInt.length)).then(invocation -> {
            scanner.internalDIO = false;
            scanner.dioBuf = new byte[]{8, 4, 3, 4, 5, 6, 0, 5, 9, 10, 11, 12};
            scanner.internalDIOSuccess = true;
            return null;
        });
        when(_port.sendData(DLSUSBScanner.writeConfigInt, DLSUSBScanner.writeConfigInt.length)).then(invocation -> {
            scanner.internalDIO = false;
            scanner.dioBuf = new byte[]{8, 4, 3, 4, 5, 6, 0, 5, 9, 10, 11, 12};
            scanner.internalDIOSuccess = true;
            return null;
        });
        int[] data = new int[]{10, 11, 12, 13, 14, 15, 16, 17};
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        stream.write("050206".getBytes());
        stream.close();
        scanner.directIO(DLSJposConst.DIO_WRITE_CONFIG_ITEM, data, stream);
        assertThat(data[0]).isEqualTo(DLSJposConst.DL_ACK);
    }

    @Test
    public void directIOWriteConfigItemNoPayload2Empty() throws IOException, DLSException {
        DLSUSBScanner scanner = new DLSUSBScanner();
        scanner.port = _port;
        scanner.options = mock(DLSProperties.class);
        when(_port.sendData(DLSUSBScanner.queryConfig, DLSUSBScanner.queryConfig.length)).then(invocation -> {
            scanner.internalDIO = false;
            scanner.dioBuf = new byte[]{2, 0, 3, 4, 5, 6, 2, 8, 9, 10, 11, 12};
            scanner.internalDIOSuccess = true;
            return null;
        });
        when(_port.sendData(DLSUSBScanner.readConfigInt, DLSUSBScanner.readConfigInt.length)).then(invocation -> {
            scanner.internalDIO = false;
            scanner.dioBuf = new byte[]{8, 4, 3, 4, 5, 6, 0, 5, 9, 10, 11, 12};
            scanner.internalDIOSuccess = true;
            return null;
        });
        when(_port.sendData(DLSUSBScanner.writeConfigInt, DLSUSBScanner.writeConfigInt.length)).then(invocation -> {
            scanner.internalDIO = false;
            scanner.dioBuf = new byte[]{};
            scanner.internalDIOSuccess = true;
            return null;
        });
        int[] data = new int[]{10, 11, 12, 13, 14, 15, 16, 17};
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        stream.write("050206".getBytes());
        stream.close();
        scanner.directIO(DLSJposConst.DIO_WRITE_CONFIG_ITEM, data, stream);
        assertThat(data[0]).isEqualTo(DLSJposConst.DL_NAK);
    }

    @Test
    public void directIOWriteConfigItemNoPayload2Invalid() throws IOException, DLSException {
        DLSUSBScanner scanner = new DLSUSBScanner();
        scanner.port = _port;
        scanner.options = mock(DLSProperties.class);
        when(_port.sendData(DLSUSBScanner.queryConfig, DLSUSBScanner.queryConfig.length)).then(invocation -> {
            scanner.internalDIO = false;
            scanner.dioBuf = new byte[]{2, 0, 3, 4, 5, 6, 2, 8, 9, 10, 11, 12};
            scanner.internalDIOSuccess = true;
            return null;
        });
        when(_port.sendData(DLSUSBScanner.readConfigInt, DLSUSBScanner.readConfigInt.length)).then(invocation -> {
            scanner.internalDIO = false;
            scanner.dioBuf = new byte[]{8, 4, 3, 4, 5, 6, 0, 5, 9, 10, 11, 12};
            scanner.internalDIOSuccess = true;
            return null;
        });
        when(_port.sendData(DLSUSBScanner.writeConfigInt, DLSUSBScanner.writeConfigInt.length)).then(invocation -> {
            scanner.internalDIO = false;
            scanner.dioBuf = new byte[]{};
            scanner.internalDIOSuccess = true;
            return null;
        });
        int[] data = new int[]{10, 11, 12, 13, 14, 15, 16, 17};
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        stream.write("0502PP".getBytes());
        stream.close();
        scanner.directIO(DLSJposConst.DIO_WRITE_CONFIG_ITEM, data, stream);
        assertThat(data[0]).isEqualTo(DLSJposConst.DL_NAK);
    }

    @Test
    public void directIOWriteConfigItemNoPayloadWrongSize() throws IOException, DLSException {
        DLSUSBScanner scanner = new DLSUSBScanner();
        scanner.port = _port;
        scanner.options = mock(DLSProperties.class);
        when(_port.sendData(DLSUSBScanner.queryConfig, DLSUSBScanner.queryConfig.length)).then(invocation -> {
            scanner.internalDIO = false;
            scanner.dioBuf = new byte[]{2, 3, 3, 4, 5, 6, 2, 8, 9, 10, 11, 12};
            scanner.internalDIOSuccess = true;
            return null;
        });
        when(_port.sendData(DLSUSBScanner.readConfigInt, DLSUSBScanner.readConfigInt.length)).then(invocation -> {
            scanner.internalDIO = false;
            scanner.dioBuf = new byte[]{8, 4, 3, 4, 5, 6, 0, 5, 9, 10, 11, 12};
            scanner.internalDIOSuccess = true;
            return null;
        });
        when(_port.sendData(DLSUSBScanner.writeConfigInt, DLSUSBScanner.writeConfigInt.length)).then(invocation -> {
            scanner.internalDIO = false;
            scanner.dioBuf = new byte[]{};
            scanner.internalDIOSuccess = true;
            return null;
        });
        int[] data = new int[]{10, 11, 12, 13, 14, 15, 16, 17};
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        stream.write("050206".getBytes());
        stream.close();
        scanner.directIO(DLSJposConst.DIO_WRITE_CONFIG_ITEM, data, stream);
        assertThat(data[0]).isEqualTo(DLSJposConst.DL_NAK);
    }

    @Test
    public void directIOCommitConfigItems() throws DLSException {
        DLSUSBScanner scanner = new DLSUSBScanner();
        int[] data = new int[]{10, 11, 12, 13, 14, 15, 16, 17};
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        scanner.directIO(DLSJposConst.DIO_COMMIT_CONFIG_ITEMS, data, stream);
        assertThat(data[0]).isEqualTo(0);
    }

    @Test
    public void directIOCellphoneEcommEnter() throws DLSException {
        DLSUSBScanner scanner = new DLSUSBScanner();
        scanner.port = _port;
        scanner.options = mock(DLSProperties.class);
        when(_port.sendData(DLSUSBScanner.cellPhoneModeECOMMEnter)).then(invocation -> {
            scanner.internalDIO = false;
            scanner.dioBuf = new byte[]{1};
            scanner.internalDIOSuccess = true;
            return null;
        });
        int[] data = new int[]{10, 11, 12, 13, 14, 15, 16, 17};
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        scanner.directIO(DLSJposConst.DIO_CELL_PHONE_ECOMM_ENTER, data, stream);
        assertThat(data[0]).isEqualTo(0);
        assertThat(new String(stream.toByteArray())).isNotEmpty();
    }

    @Test
    public void directIOCellphoneEcommExit() throws DLSException {
        DLSUSBScanner scanner = new DLSUSBScanner();
        scanner.port = _port;
        scanner.options = mock(DLSProperties.class);
        when(_port.sendData(DLSUSBScanner.cellPhoneModeECOMMExit)).then(invocation -> {
            scanner.internalDIO = false;
            scanner.dioBuf = new byte[]{1};
            scanner.internalDIOSuccess = true;
            return null;
        });
        int[] data = new int[]{10, 11, 12, 13, 14, 15, 16, 17};
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        scanner.directIO(DLSJposConst.DIO_CELL_PHONE_ECOMM_EXIT, data, stream);
        assertThat(data[0]).isEqualTo(0);
        assertThat(new String(stream.toByteArray())).isNotEmpty();
    }

    @Test
    public void directIOCellphoneScanEnter() throws DLSException {
        DLSUSBScanner scanner = new DLSUSBScanner();
        scanner.port = _port;
        scanner.options = mock(DLSProperties.class);
        when(_port.sendData(DLSUSBScanner.cellPhoneModeScannerEnter)).then(invocation -> {
            scanner.internalDIO = false;
            scanner.dioBuf = new byte[]{1};
            scanner.internalDIOSuccess = true;
            return null;
        });
        int[] data = new int[]{10, 11, 12, 13, 14, 15, 16, 17};
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        scanner.directIO(DLSJposConst.DIO_CELL_PHONE_SCAN_ENTER, data, stream);
        assertThat(data[0]).isEqualTo(0);
        assertThat(new String(stream.toByteArray())).isNotEmpty();
    }

    @Test
    public void directIOCellphoneScanExit() throws DLSException {
        DLSUSBScanner scanner = new DLSUSBScanner();
        scanner.port = _port;
        scanner.options = mock(DLSProperties.class);
        when(_port.sendData(DLSUSBScanner.cellPhoneModeScannerExit, DLSUSBScanner.cellPhoneModeScannerExit.length)).then(invocation -> {
            scanner.internalDIO = false;
            scanner.dioBuf = new byte[]{1};
            scanner.internalDIOSuccess = true;
            return null;
        });
        int[] data = new int[]{10, 11, 12, 13, 14, 15, 16, 17};
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        scanner.directIO(DLSJposConst.DIO_CELL_PHONE_SCAN_EXIT, data, stream);
        assertThat(data[0]).isEqualTo(0);
        assertThat(new String(stream.toByteArray())).isNotEmpty();
    }

    @Test
    public void directIOVideoStreamOn() throws DLSException {
        DLSUSBScanner scanner = new DLSUSBScanner();
        scanner.port = _port;
        scanner.options = mock(DLSProperties.class);
        when(_port.sendData(DLSUSBScanner.extVideoStreamOn)).then(invocation -> {
            scanner.internalDIO = false;
            scanner.dioBuf = new byte[]{1};
            scanner.internalDIOSuccess = true;
            return null;
        });
        int[] data = new int[]{10, 11, 12, 13, 14, 15, 16, 17};
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        scanner.directIO(DLSJposConst.DIO_EXT_VIDEO_STREAM_ON, data, stream);
        assertThat(data[0]).isEqualTo(0);
        assertThat(new String(stream.toByteArray())).isNotEmpty();
    }

    @Test
    public void directIOVideoStreamOff() throws DLSException {
        DLSUSBScanner scanner = new DLSUSBScanner();
        scanner.port = _port;
        scanner.options = mock(DLSProperties.class);
        when(_port.sendData(DLSUSBScanner.extVideoStreamOff)).then(invocation -> {
            scanner.internalDIO = false;
            scanner.dioBuf = new byte[]{1};
            scanner.internalDIOSuccess = true;
            return null;
        });
        int[] data = new int[]{10, 11, 12, 13, 14, 15, 16, 17};
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        scanner.directIO(DLSJposConst.DIO_EXT_VIDEO_STREAM_OFF, data, stream);
        assertThat(data[0]).isEqualTo(0);
        assertThat(new String(stream.toByteArray())).isNotEmpty();
    }

    @Test
    public void directIOBeepCommand() throws DLSException {
        DLSUSBScanner scanner = new DLSUSBScanner();
        scanner.port = _port;
        scanner.options = mock(DLSProperties.class);
        when(_port.sendData(scanner.directIOCmd, scanner.directIOCmd.length)).then(invocation -> {
            scanner.internalDIO = false;
            scanner.dioBuf = new byte[]{1};
            scanner.internalDIOSuccess = true;
            return null;
        });
        int[] data = new int[]{10, 11, 12, 13, 14, 15, 16, 17};
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        scanner.directIO(DLSJposConst.DIO_SCANNER_DIO_BEEP, data, stream);
        assertThat(data[0]).isEqualTo(0);
    }

    @Test
    public void directIOScannerNOF() throws DLSException {
        DLSUSBScanner scanner = new DLSUSBScanner();
        scanner.port = _port;
        scanner.options = mock(DLSProperties.class);
        when(_port.sendData(scanner.directIOCmd, scanner.directIOCmd.length)).then(invocation -> {
            scanner.internalDIO = false;
            scanner.dioBuf = new byte[]{1};
            scanner.internalDIOSuccess = true;
            return null;
        });
        int[] data = new int[]{10, 11, 12, 13, 14, 15, 16, 17};
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        scanner.directIO(DLSJposConst.DIO_SCANNER_DIO_NOF, data, stream);
        assertThat(data[0]).isEqualTo(0);
    }

    @Test
    public void directIOErrorBeep() throws DLSException {
        DLSUSBScanner scanner = new DLSUSBScanner();
        scanner.port = _port;
        scanner.options = mock(DLSProperties.class);
        when(_port.sendData(scanner.directIOCmd, scanner.directIOCmd.length)).then(invocation -> {
            scanner.internalDIO = false;
            scanner.dioBuf = new byte[]{1};
            scanner.internalDIOSuccess = true;
            return null;
        });
        int[] data = new int[]{10, 11, 12, 13, 14, 15, 16, 17};
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        scanner.directIO(DLSJposConst.DIO_SCANNER_DIO_ERROR_BEEP, data, stream);
        assertThat(data[0]).isEqualTo(0);
    }

    @Test
    public void directIODevProtocol() throws DLSException {
        DLSUSBScanner scanner = new DLSUSBScanner();
        int[] data = new int[]{10, 11, 12, 13, 14, 15, 16, 17};
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        scanner.directIO(DLSJposConst.DIO_DEV_PROTOCOL, data, stream);
        assertThat(data[0]).isEqualTo(0);
        assertThat(new String(stream.toByteArray())).isEqualTo("USB");
    }

    @Test
    public void directIOTwoWayHostResponse() throws DLSException, IOException {
        DLSUSBScanner scanner = new DLSUSBScanner();
        scanner.port = _port;
        int[] data = new int[]{10, 11, 12, 13, 14, 15, 16, 17};
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        stream.write("test".getBytes());
        stream.close();
        scanner.directIO(DLSJposConst.DIO_TWO_WAY_HOST_RESPONSE, data, stream);
        verify(_port, times(1)).sendData("@test\r");
    }

    @Test
    public void directIOService() throws DLSException {
        DLSUSBScanner scanner = new DLSUSBScanner();
        int[] data = new int[]{10, 11, 12, 13, 14, 15, 16, 17};
        DLSScannerService service = mock(DLSScannerService.class);
        scanner.directIO(-7, data, service);
        verify(service, times(1)).setScanner(scanner);
    }

    @Test
    public void directIOInvalid() throws DLSException {
        DLSUSBScanner scanner = new DLSUSBScanner();
        int[] data = new int[]{10, 11, 12, 13, 14, 15, 16, 17};
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        scanner.directIO(-2, data, stream);
        assertThat(data[0]).isEqualTo(-1);
    }
    //endregion

    //region Disable
    @Test
    public void disableWhileUpgrading() throws DLSException {
        DLSUSBScanner scanner = new DLSUSBScanner();
        scanner.port = _port;
        DLSJposConst.updateInProgress = true;
        scanner.disable();
        verify(_port, times(0)).sendData(DLSUSBScanner.disableCmd);
    }

    @Test
    public void disable() throws DLSException {
        DLSUSBScanner scanner = new DLSUSBScanner();
        scanner.port = _port;
        when(_port.sendData(DLSUSBScanner.disableCmd)).then(invocation -> {
            scanner.statusBuf = new byte[]{1};
            return null;
        });
        scanner.setState(DLSState.OPENED);
        scanner.setState(DLSState.CLAIMED);
        scanner.setState(DLSState.ENABLED);
        scanner.disable();
        verify(_port, times(1)).sendData(DLSUSBScanner.disableCmd);
        assertThat(scanner.getState()).isEqualTo(DLSState.DISABLED);
    }

    //endregion

    @Test
    public void disableBeepWhileUpgrading() throws DLSException {
        DLSUSBScanner scanner = new DLSUSBScanner();
        DLSJposConst.updateInProgress = true;
        scanner.disableBeep();
        verify(_port, times(0)).sendData(DLSUSBScanner.disableBeepCmd, DLSUSBScanner.disableBeepCmd.length);
    }

    @Test
    public void disableBeep() throws DLSException {
        DLSUSBScanner scanner = new DLSUSBScanner();
        scanner.port = _port;
        scanner.disableBeep();
        verify(_port, times(1)).sendData(DLSUSBScanner.disableBeepCmd, DLSUSBScanner.disableBeepCmd.length);
    }

    @Test
    public void doHealthCheck() throws DLSException {
        DLSUSBScanner scanner = new DLSUSBScanner();
        scanner.port = _port;
        when(_port.sendData(scanner.directIOCmd, scanner.directIOCmd.length)).then(invocation -> {
            scanner.dioBuf = new byte[]{67, 65, 86, 65, 76, 69, 83, 69};
            return null;
        });
        assertThat(scanner.doHealthCheck(10)).isTrue();
    }

    @Test
    public void doHealthCheckTimeout() throws DLSException {
        DLSUSBScanner scanner = new DLSUSBScanner();
        scanner.port = _port;
        assertThat(scanner.doHealthCheck(10)).isFalse();
    }

    @Test
    public void doHealthCheckWhileUpgrading() throws DLSException {
        DLSUSBScanner scanner = new DLSUSBScanner();
        DLSJposConst.updateInProgress = true;
        assertThat(scanner.doHealthCheck(10)).isTrue();
    }

    @Test
    public void doSelfTestWhileUpgrade() throws DLSException {
        DLSUSBScanner scanner = new DLSUSBScanner();
        scanner.port = _port;
        DLSJposConst.updateInProgress = true;
        scanner.doSelfTest();
        verify(_port, times(0)).sendData(DLSUSBScanner.doSelfTestCmd, DLSUSBScanner.doSelfTestCmd.length);
    }

    @Test
    public void doSelfTest() throws DLSException {
        DLSUSBScanner scanner = new DLSUSBScanner();
        scanner.port = _port;
        scanner.doSelfTest();
        verify(_port, times(1)).sendData(DLSUSBScanner.doSelfTestCmd, DLSUSBScanner.doSelfTestCmd.length);
    }

    @Test
    public void enableWhileUpgrading() throws DLSException {
        DLSUSBScanner scanner = new DLSUSBScanner();
        scanner.port = _port;
        DLSJposConst.updateInProgress = true;
        scanner.enable();
        verify(_port, times(0)).sendData(DLSUSBScanner.enableCmd);
    }

    @Test
    public void enable() throws DLSException {
        DLSUSBScanner scanner = new DLSUSBScanner();
        scanner.port = _port;
        when(_port.sendData(DLSUSBScanner.enableCmd)).then(invocation -> {
            scanner.statusBuf = new byte[]{0, 1, 2, 3};
            return null;
        });
        scanner.setState(DLSState.OPENED);
        scanner.setState(DLSState.CLAIMED);
        scanner.enable();
        assertThat(scanner.getState()).isEqualTo(DLSState.ENABLED);
    }

    @Test
    public void enableRetry() throws DLSException {
        DLSUSBScanner scanner = new DLSUSBScanner();
        scanner.port = _port;
        scanner.options = mock(DLSProperties.class);
        when(scanner.options.getRetryWaitTime()).thenReturn(10);
        final byte[] response = {1};
        when(_port.sendData(DLSUSBScanner.enableCmd)).then(invocation -> {
            scanner.statusBuf = new byte[]{0, 1, response[0], 3};
            response[0]++;
            return null;
        });
        scanner.setState(DLSState.OPENED);
        scanner.setState(DLSState.CLAIMED);
        scanner.enable();
        assertThat(scanner.getState()).isEqualTo(DLSState.ENABLED);
    }

    @Test(expected = DLSException.class)
    public void enableRetryFail() throws DLSException {
        DLSUSBScanner scanner = new DLSUSBScanner();
        scanner.port = _port;
        scanner.options = mock(DLSProperties.class);
        when(scanner.options.getRetryWaitTime()).thenReturn(10);
        when(_port.sendData(DLSUSBScanner.enableCmd)).then(invocation -> {
            scanner.statusBuf = new byte[]{0, 1, 1, 3};
            return null;
        });
        scanner.setState(DLSState.OPENED);
        scanner.setState(DLSState.CLAIMED);
        scanner.enable();
        assertThat(scanner.getState()).isEqualTo(DLSState.ENABLED);
    }

    @Test
    public void enableBeepWhileUpgrading() throws DLSException {
        DLSUSBScanner scanner = new DLSUSBScanner();
        scanner.port = _port;
        DLSJposConst.updateInProgress = true;
        scanner.enableBeep();
        verify(_port, times(0)).sendData(DLSUSBScanner.enableBeepCmd, DLSUSBScanner.enableBeepCmd.length);
    }

    @Test
    public void enableBeep() throws DLSException {
        DLSUSBScanner scanner = new DLSUSBScanner();
        scanner.port = _port;
        scanner.enableBeep();
        verify(_port, times(1)).sendData(DLSUSBScanner.enableBeepCmd, DLSUSBScanner.enableBeepCmd.length);
    }

    @Test
    public void extractBarcodeLabel() {
        DLSUSBScanner scanner = new DLSUSBScanner();
        assertThat(scanner.extractBarcodeLabel(new byte[]{1, 2, 3, 4}, 2)).isEqualTo(new byte[]{1, 2});
        assertThat(scanner.extractBarcodeLabel(new byte[]{1, 2, 3, 4}, 1)).isEqualTo(new byte[]{1, 2, 3});
        assertThat(scanner.extractBarcodeLabel(new byte[]{1, 2, 3, 4}, 3)).isEqualTo(new byte[]{1});
    }

    //region Get Statistics
    @Test
    public void getStatisticsWhileUpgrading() throws DLSException {
        DLSUSBScanner scanner = new DLSUSBScanner();
        DLSJposConst.updateInProgress = true;
        assertThat(scanner.getStatistics()).isEmpty();
    }

    @Test
    public void getStatisticsUnsupported() throws DLSException {
        DLSUSBScanner scanner = new DLSUSBScanner();
        scanner.scannerConfig = mock(DLSScannerConfig.class);
        assertThat(scanner.getStatistics()).isEmpty();
    }

    @Test
    public void getStatistics() throws DLSException {
        DLSUSBScanner scanner = new DLSUSBScanner();
        scanner.port = _port;
        scanner.context = _context;
        when(_port.sendData(scanner.directIOCmd, scanner.directIOCmd.length)).then(invocation -> {
            scanner.dioBuf = new byte[]{1, 2, 3, 4, 0, 65, 66, 67, 68};
            return null;
        });
        scanner.scannerConfig = mock(DLSScannerConfig.class);
        when(scanner.scannerConfig.getCanAcceptStatisticsCommand()).thenReturn(true);
        when(scanner.scannerConfig.getExtendedHostTimeout()).thenReturn(100);
        DLSStatistics statistics = mock(DLSStatistics.class);
        scanner.nObjWaitTime = 80;
        try (MockedStatic<DLSStatistics> stats = Mockito.mockStatic(DLSStatistics.class)) {
            stats.when(() -> DLSStatistics.getInstance(_context)).thenReturn(statistics);
            HashMap<String, Object> result = scanner.getStatistics();
            assertThat(result).isNotEmpty();
            assertThat(result.size()).isEqualTo(6);
            assertThat(result.get(DLSJposConst.DLS_S_DEVICE_CATEGORY)).isEqualTo("Scanner");
            assertThat(result.get(DLSJposConst.DLS_S_INTERFACE)).isEqualTo("USB-OEM");
            assertThat(result.get("RawInfo")).isEqualTo("# bytes received[4] 0x41 0x42 0x43 0x44");
        }
    }

    @Test
    public void getStatisticsEmptyResponseInfos() {
        DLSUSBScanner scanner = new DLSUSBScanner();
        scanner.port = _port;
        scanner.context = _context;
        scanner.options = mock(DLSProperties.class);
        when(scanner.options.getRetryWaitTime()).thenReturn(10);
        when(_port.sendData(scanner.directIOCmd, scanner.directIOCmd.length)).then(invocation -> {
            scanner.dioBuf = new byte[]{};
            return null;
        });
        scanner.scannerConfig = mock(DLSScannerConfig.class);
        when(scanner.scannerConfig.getCanAcceptStatisticsCommand()).thenReturn(true);
        when(scanner.scannerConfig.getExtendedHostTimeout()).thenReturn(100);
        DLSStatistics statistics = mock(DLSStatistics.class);
        scanner.nObjWaitTime = 80;
        try (MockedStatic<DLSStatistics> stats = Mockito.mockStatic(DLSStatistics.class)) {
            stats.when(() -> DLSStatistics.getInstance(_context)).thenReturn(statistics);
            scanner.getStatistics();
            fail();
        } catch (DLSException e) {
            assertThat(e.getErrorCode()).isEqualTo(DLSJposConst.DLS_E_TIMEOUT);
        }
    }

    @Test
    public void getStatisticsEmptyResponseHealth() throws DLSException {
        DLSUSBScanner scanner = new DLSUSBScanner();
        scanner.port = _port;
        scanner.context = _context;
        scanner.options = mock(DLSProperties.class);
        when(scanner.options.getRetryWaitTime()).thenReturn(10);
        when(_port.sendData(new byte[]{0x30, 0, DLSUSBScanner.INFO_CMD, 0, 0, 0, 0, 0, 0, 0, 0}, scanner.directIOCmd.length)).then(invocation -> {
            scanner.dioBuf = new byte[]{1, 2, 3, 4, 0, 65, 66, 67, 68};
            return null;
        });
        when(_port.sendData(new byte[]{0x30, 0, DLSUSBScanner.HEALTH_CMD, 0, 0, 0, 0, 0, 0, 0, 0}, scanner.directIOCmd.length)).then(invocation -> {
            scanner.dioBuf = new byte[]{};
            return null;
        });
        scanner.scannerConfig = mock(DLSScannerConfig.class);
        when(scanner.scannerConfig.getCanAcceptStatisticsCommand()).thenReturn(true);
        when(scanner.scannerConfig.getExtendedHostTimeout()).thenReturn(100);
        DLSStatistics statistics = mock(DLSStatistics.class);
        scanner.nObjWaitTime = 80;
        try (MockedStatic<DLSStatistics> stats = Mockito.mockStatic(DLSStatistics.class)) {
            stats.when(() -> DLSStatistics.getInstance(_context)).thenReturn(statistics);
            scanner.getStatistics();
            fail();
        } catch (DLSException e) {
            assertThat(e.getErrorCode()).isEqualTo(DLSJposConst.DLS_E_TIMEOUT);
        }
    }

    @Test
    public void getStatisticsEmptyResponseStatistics() {
        DLSUSBScanner scanner = new DLSUSBScanner();
        scanner.port = _port;
        scanner.context = _context;
        scanner.options = mock(DLSProperties.class);
        when(scanner.options.getRetryWaitTime()).thenReturn(10);
        when(_port.sendData(new byte[]{0x30, 0, DLSUSBScanner.INFO_CMD, 0, 0, 0, 0, 0, 0, 0, 0}, scanner.directIOCmd.length)).then(invocation -> {
            scanner.dioBuf = new byte[]{1, 2, 3, 4, 0, 65, 66, 67, 68};
            return null;
        });
        when(_port.sendData(new byte[]{0x30, 0, DLSUSBScanner.HEALTH_CMD, 0, 0, 0, 0, 0, 0, 0, 0}, scanner.directIOCmd.length)).then(invocation -> {
            scanner.dioBuf = new byte[]{1, 2, 3, 4, 0, 65, 66, 67, 68};
            return null;
        });
        when(_port.sendData(new byte[]{0x30, 0, DLSUSBScanner.STATISTICS_CMD, 0, 0, 0, 0, 0, 0, 0, 0}, scanner.directIOCmd.length)).then(invocation -> {
            scanner.dioBuf = new byte[]{};
            return null;
        });
        scanner.scannerConfig = mock(DLSScannerConfig.class);
        when(scanner.scannerConfig.getCanAcceptStatisticsCommand()).thenReturn(true);
        when(scanner.scannerConfig.getExtendedHostTimeout()).thenReturn(100);
        DLSStatistics statistics = mock(DLSStatistics.class);
        scanner.nObjWaitTime = 80;
        try (MockedStatic<DLSStatistics> stats = Mockito.mockStatic(DLSStatistics.class)) {
            stats.when(() -> DLSStatistics.getInstance(_context)).thenReturn(statistics);
            scanner.getStatistics();
            fail();
        } catch (DLSException e) {
            assertThat(e.getErrorCode()).isEqualTo(DLSJposConst.DLS_E_TIMEOUT);
        }
    }
    //endregion

    @Test
    public void hasStatisticsReporting() {
        DLSUSBScanner scanner = new DLSUSBScanner();
        scanner.scannerConfig = mock(DLSScannerConfig.class);
        when(scanner.scannerConfig.getCanAcceptStatisticsCommand()).thenReturn(true);
        assertThat(scanner.hasStatisticsReporting()).isTrue();
        when(scanner.scannerConfig.getCanAcceptStatisticsCommand()).thenReturn(false);
        assertThat(scanner.hasStatisticsReporting()).isFalse();
    }

    @Test
    public void isAliveTrue() throws DLSException {
        DLSUSBScanner scanner = new DLSUSBScanner();
        scanner.port = _port;
        when(_port.sendData(DLSUSBScanner.statusCmd)).then(invocation -> {
            scanner.gotStatus = true;
            scanner.stat0 = 1;
            scanner.stat1 = 2;
            scanner.stat2 = 3;
            return null;
        });
        assertThat(scanner.isAlive()).isTrue();
    }

    @Test
    public void isAliveFalse() throws DLSException {
        DLSUSBScanner scanner = new DLSUSBScanner();
        scanner.port = _port;
        assertThat(scanner.isAlive()).isFalse();
    }

    //region On Data Received
    @Test
    public void onDataReceivedAlive() {
        DLSUSBScanner scanner = new DLSUSBScanner();
        DeviceErrorStatusListener listener = mock(DeviceErrorStatusListener.class);
        scanner.addDeviceStatusListener(listener);
        scanner.onDataReceived(new byte[]{4, 0, 1, 0}, 4);
        verify(listener, times(1)).onDeviceStatus(DeviceErrorStatusListener.STATUS_ALIVE);
    }

    @Test
    public void onDataReceivedLocalAlive() {
        DLSUSBScanner scanner = new DLSUSBScanner();
        DeviceErrorStatusListener listener = mock(DeviceErrorStatusListener.class);
        scanner.addDeviceStatusListener(listener);
        scanner.localAlive = true;
        scanner.onDataReceived(new byte[]{4, 0, 0, 0}, 4);
        verify(listener, times(1)).onDeviceStatus(DeviceErrorStatusListener.STATUS_NOT_ALIVE);
    }

    @Test
    public void onDataReceivedErrorFlashing() {
        DLSUSBScanner scanner = new DLSUSBScanner();
        DeviceErrorStatusListener listener = mock(DeviceErrorStatusListener.class);
        scanner.addDeviceErrorListener(listener);
        scanner.onDataReceived(new byte[]{4, 1, 0, 0}, 4);
        verify(listener, times(1)).onDeviceError(DeviceErrorStatusListener.ERR_FLASHING);
    }

    @Test
    public void onDataReceivedHardwareError() {
        DLSUSBScanner scanner = new DLSUSBScanner();
        DeviceErrorStatusListener listener = mock(DeviceErrorStatusListener.class);
        scanner.addDeviceErrorListener(listener);
        scanner.onDataReceived(new byte[]{4, 0x20, 0, 0}, 4);
        verify(listener, times(1)).onDeviceError(DeviceErrorStatusListener.ERR_HARDWARE);
    }

    @Test
    public void onDataReceivedBusyError() {
        DLSUSBScanner scanner = new DLSUSBScanner();
        DeviceErrorStatusListener listener = mock(DeviceErrorStatusListener.class);
        scanner.addDeviceErrorListener(listener);
        scanner.onDataReceived(new byte[]{4, (byte) 0x80, 0, 0}, 4);
        verify(listener, times(1)).onDeviceError(DeviceErrorStatusListener.ERR_BUSY);
    }

    @Test
    public void onDataReceivedCheckDigitError() {
        DLSUSBScanner scanner = new DLSUSBScanner();
        DeviceErrorStatusListener listener = mock(DeviceErrorStatusListener.class);
        scanner.addDeviceErrorListener(listener);
        scanner.onDataReceived(new byte[]{4, 0, 4, 0}, 4);
        verify(listener, times(1)).onDeviceError(DeviceErrorStatusListener.ERR_CHECKDIGIT);
    }

    @Test
    public void onDataReceivedCommandRejected() {
        DLSUSBScanner scanner = new DLSUSBScanner();
        DeviceErrorStatusListener listener = mock(DeviceErrorStatusListener.class);
        scanner.addDeviceErrorListener(listener);
        scanner.onDataReceived(new byte[]{4, 0, (byte) 0x80, 0}, 4);
        verify(listener, times(1)).onDeviceError(DeviceErrorStatusListener.ERR_CMD);
    }

    @Test
    public void onDataReceivedDIONotAllowed() {
        DLSUSBScanner scanner = new DLSUSBScanner();
        DeviceErrorStatusListener listener = mock(DeviceErrorStatusListener.class);
        scanner.addDeviceErrorListener(listener);
        scanner.onDataReceived(new byte[]{4, 0, 0, 0x10}, 4);
        verify(listener, times(1)).onDeviceError(DeviceErrorStatusListener.ERR_DIO_NOT_ALLOWED);
    }

    @Test
    public void onDataReceivedDIOUndefined() {
        DLSUSBScanner scanner = new DLSUSBScanner();
        DeviceErrorStatusListener listener = mock(DeviceErrorStatusListener.class);
        scanner.addDeviceErrorListener(listener);
        scanner.onDataReceived(new byte[]{4, 0, 0, 0x20}, 4);
        verify(listener, times(1)).onDeviceError(DeviceErrorStatusListener.ERR_DIO_UNDEFINED);
    }

    @Test
    public void onDataReceivedConfigData() {
        DLSUSBScanner scanner = new DLSUSBScanner();
        DeviceErrorStatusListener listener = mock(DeviceErrorStatusListener.class);
        scanner.addDeviceErrorListener(listener);
        scanner.onDataReceived(new byte[]{4, 2, 0, 0}, 4);
        assertThat(scanner.reportBuf).isEqualTo(new byte[]{4, 2, 0, 0});
    }

    @Test
    public void onDataReceivedEANJanConfig() {
        DLSUSBScanner scanner = new DLSUSBScanner();
        DeviceErrorStatusListener listener = mock(DeviceErrorStatusListener.class);
        scanner.addDeviceErrorListener(listener);
        scanner.onDataReceived(new byte[]{4, 4, 0, 0}, 4);
        assertThat(scanner.reportBuf).isEqualTo(new byte[]{4, 4, 0, 0});
    }

    @Test
    public void onDataReceivedDIOData() {
        DLSUSBScanner scanner = new DLSUSBScanner();
        DeviceErrorStatusListener listener = mock(DeviceErrorStatusListener.class);
        scanner.internalDIO = true;
        scanner.addDeviceErrorListener(listener);
        scanner.onDataReceived(new byte[]{4, 0x40, 0, 8}, 4);
        assertThat(scanner.dioBuf).isEqualTo(new byte[]{4, 0x40, 0, 8});
        assertThat(scanner.internalDIOSuccess).isTrue();
    }

    @Test
    public void onDataReceivedDIOSuccess() {
        DLSUSBScanner scanner = new DLSUSBScanner();
        DeviceErrorStatusListener listener = mock(DeviceErrorStatusListener.class);
        scanner.internalDIO = true;
        scanner.addDeviceErrorListener(listener);
        scanner.onDataReceived(new byte[]{4, 0, 0, 8}, 4);
        assertThat(scanner.dioBuf).isEqualTo(new byte[]{4, 0, 0, 8});
        assertThat(scanner.internalDIOSuccess).isTrue();
    }

    @Test
    public void onDataReceivedHHS() {
        DLSUSBScanner scanner = new DLSUSBScanner();
        LabelReceivedListener listener = mock(LabelReceivedListener.class);
        scanner.addLabelReceivedListener(listener);
        scanner.context = _context;
        DLAPosConfigHelper configHelper = mock(DLAPosConfigHelper.class);
        LabelHelper labelHelper = mock(LabelHelper.class);
        when(configHelper.getLabelHelper()).thenReturn(labelHelper);
        setUpLabelHelperMock(labelHelper);
        try (MockedStatic<DLAPosConfigHelper> helper = Mockito.mockStatic(DLAPosConfigHelper.class)) {
            helper.when(() -> DLAPosConfigHelper.getInstance(_context)).thenReturn(configHelper);
            scanner.onDataReceived(new byte[]{18, 16, 3, 0, 57, 48, 48, 49, 52, 55, 53, 48, 54, 55, 57, 51, 51, 22, 0}, 18);
            verify(listener, times(1)).onLabelReceived(
                    new byte[]{57, 48, 48, 49, 52, 55, 53, 48, 54, 55, 57, 51, 51, 22},
                    new byte[]{57, 48, 48, 49, 52, 55, 53, 48, 54, 55, 57, 51, 51},
                    104
            );
            scanner.onDataReceived(new byte[]{15, 16, 3, 0, 48, 48, 48, 48, 52, 57, 51, 55, 0, 10, 11, 0}, 15);
            verify(listener, times(1)).onLabelReceived(
                    new byte[]{48, 48, 48, 48, 52, 57, 51, 55, 0, 10, 11},
                    new byte[]{48, 48, 48, 48, 52, 57, 51, 55},
                    108
            );
        }
    }

    @Test
    public void onDataReceivedHHSLabelComposed() {
        DLSUSBScanner scanner = new DLSUSBScanner();
        LabelReceivedListener listener = mock(LabelReceivedListener.class);
        scanner.addLabelReceivedListener(listener);
        scanner.context = _context;
        DLAPosConfigHelper configHelper = mock(DLAPosConfigHelper.class);
        LabelHelper labelHelper = mock(LabelHelper.class);
        when(configHelper.getLabelHelper()).thenReturn(labelHelper);
        setUpLabelHelperMock(labelHelper);
        try (MockedStatic<DLAPosConfigHelper> helper = Mockito.mockStatic(DLAPosConfigHelper.class)) {
            helper.when(() -> DLAPosConfigHelper.getInstance(_context)).thenReturn(configHelper);
            scanner.onDataReceived(new byte[]{13, 16, 3, 0, 57, 48, 48, 49, 52, 55, 0x10, 0x10, 22}, 14);
            scanner.onDataReceived(new byte[]{12, 16, 3, 0, 53, 48, 54, 55, 57, 51, 51, 22, 0}, 18);
            verify(listener, times(1)).onLabelReceived(
                    new byte[]{57, 48, 48, 49, 52, 55, 16, 16, 53, 48, 54, 55, 57, 51, 51, 22},
                    new byte[]{57, 48, 48, 49, 52, 55, 16, 16, 53, 48, 54, 55, 57, 51, 51},
                    104
            );
        }
    }

    @Test
    public void onDataReceivedHHSLabelComposed4BytesId() {
        DLSUSBScanner scanner = new DLSUSBScanner();
        LabelReceivedListener listener = mock(LabelReceivedListener.class);
        scanner.addLabelReceivedListener(listener);
        scanner.context = _context;
        DLAPosConfigHelper configHelper = mock(DLAPosConfigHelper.class);
        LabelHelper labelHelper = mock(LabelHelper.class);
        when(configHelper.getLabelHelper()).thenReturn(labelHelper);
        setUpLabelHelperMock(labelHelper);
        try (MockedStatic<DLAPosConfigHelper> helper = Mockito.mockStatic(DLAPosConfigHelper.class)) {
            helper.when(() -> DLAPosConfigHelper.getInstance(_context)).thenReturn(configHelper);
            scanner.onDataReceived(new byte[]{14, 16, 3, 0, 57, 48, 48, 49, 52, 55, 0x10, 0x10, 0x10, 0}, 14);
            scanner.onDataReceived(new byte[]{12, 16, 3, 0, 53, 48, 54, 55, 57, 51, 51, 22, 0}, 18);
            verify(listener, times(1)).onLabelReceived(
                    new byte[]{57, 48, 48, 49, 52, 55, 53, 48, 54, 55, 57, 51, 51, 22},
                    new byte[]{57, 48, 48, 49, 52, 55, 53, 48, 54, 55, 57, 51, 51},
                    104
            );
        }
    }

    @Test
    public void onDataReceivedTableScanner() {
        DLSUSBScanner scanner = new DLSUSBScanner();
        LabelReceivedListener listener = mock(LabelReceivedListener.class);
        scanner.addLabelReceivedListener(listener);
        scanner.context = _context;
        scanner.deviceInfo = mock(DLSDeviceInfo.class);
        when(scanner.deviceInfo.getUsage()).thenReturn((int) DLSJposConst.DEV_TABLE_SCANNER_USAGE);
        DLAPosConfigHelper configHelper = mock(DLAPosConfigHelper.class);
        LabelHelper labelHelper = mock(LabelHelper.class);
        when(configHelper.getLabelHelper()).thenReturn(labelHelper);
        setUpLabelHelperMock(labelHelper);
        DLSProperties properties = mock(DLSProperties.class);
        when(properties.isBCDtoASCII()).thenReturn(true);
        try (MockedStatic<DLAPosConfigHelper> helper = Mockito.mockStatic(DLAPosConfigHelper.class)) {
            helper.when(() -> DLAPosConfigHelper.getInstance(_context)).thenReturn(configHelper);
            MockUtils.mockDLSPropertiesSingleton(properties);
            scanner.onDataReceived(new byte[]{18, 16, 3, 0, 9, 0, 0, 1, 4, 7, 5, 0, 6, 7, 9, 3, 3, 22, 0}, 18);
            verify(listener, times(1)).onLabelReceived(
                    new byte[]{57, 48, 48, 49, 52, 55, 53, 48, 54, 55, 57, 51, 51, 22},
                    new byte[]{57, 48, 48, 49, 52, 55, 53, 48, 54, 55, 57, 51, 51},
                    104
            );
            MockUtils.cleanDLSProperties();
        }
    }
    //endregion

    @Test
    public void onDeviceArrival() {
        DLSUSBScanner scanner = new DLSUSBScanner();
        DeviceErrorStatusListener listener = mock(DeviceErrorStatusListener.class);
        scanner.addDeviceStatusListener(listener);
        scanner.onDeviceArrival();
        verify(listener, times(1)).onDeviceStatus(CommonsConstants.SUE_POWER_ONLINE);
    }

    @Test
    public void onDeviceArrivalWithReset() throws DLSException {
        DLSUSBScanner scanner = new DLSUSBScanner();
        DeviceErrorStatusListener listener = mock(DeviceErrorStatusListener.class);
        scanner.setState(DLSState.OPENED);
        scanner.setState(DLSState.CLAIMED);
        scanner.setState(DLSState.ENABLED);
        scanner.port = _port;
        when(_port.sendData(DLSUSBScanner.enableCmd)).then(invocation -> {
            scanner.statusBuf = "Response".getBytes();
            return null;
        });
        scanner.addDeviceStatusListener(listener);
        scanner.onDeviceArrival();
        assertThat(scanner.getState()).isEqualTo(DLSState.ENABLED);
        verify(listener, times(1)).onDeviceStatus(CommonsConstants.SUE_POWER_ONLINE);
    }

    @Test
    public void onDeviceReattachedError() {
        DLSUSBScanner scanner = new DLSUSBScanner();
        scanner.port = _port;
        DeviceErrorStatusListener listener = mock(DeviceErrorStatusListener.class);
        scanner.addDeviceErrorListener(listener);
        scanner.options = mock(DLSProperties.class);
        when(scanner.options.getRetryWaitTime()).thenReturn(100);
        scanner.onDeviceReattached();
        verify(listener, times(1)).onDeviceError(DeviceErrorStatusListener.ERR_DEVICE_REATTACHED);
        verify(_port, atLeast(1)).openPort();
    }

    @Test
    public void onDeviceReattached() {
        DLSUSBScanner scanner = new DLSUSBScanner();
        scanner.hotPlugged = true;
        scanner.port = _port;
        when(_port.openPort()).thenReturn(true);
        DeviceErrorStatusListener listener = mock(DeviceErrorStatusListener.class);
        scanner.addDeviceErrorListener(listener);
        scanner.onDeviceReattached();
        verify(listener, times(0)).onDeviceError(DeviceErrorStatusListener.ERR_DEVICE_REATTACHED);
        verify(_port, times(1)).openPort();
    }

    @Test
    public void onDeviceRemoved() {
        DLSUSBScanner scanner = new DLSUSBScanner();
        scanner.port = _port;
        DeviceErrorStatusListener listener = mock(DeviceErrorStatusListener.class);
        scanner.addDeviceErrorListener(listener);
        scanner.addDeviceStatusListener(listener);
        scanner.onDeviceRemoved();
        verify(listener, times(1)).onDeviceStatus(CommonsConstants.SUE_POWER_OFF_OFFLINE);
        verify(listener, times(1)).onDeviceError(DeviceErrorStatusListener.ERR_DEVICE_REMOVED);
    }

    //region Open
    @Test(expected = IllegalArgumentException.class)
    public void openNullContext() throws DLSException {
        DLSUSBScanner scanner = new DLSUSBScanner();
        scanner.open("adsa", null);
        assertThat(scanner.getState()).isNotEqualTo(DLSState.OPENED);
    }

    @Test(expected = IllegalArgumentException.class)
    public void openNullString() throws DLSException {
        DLSUSBScanner scanner = new DLSUSBScanner();
        scanner.open(null, _context);
        assertThat(scanner.getState()).isNotEqualTo(DLSState.OPENED);
    }

    @Test
    public void open() throws DLSException {
        DLSUSBScanner scanner = new DLSUSBScanner();
        try (MockedConstruction<DLSScannerConfig> mockedConfig = mockConstruction(DLSScannerConfig.class,
                (mock, context) -> {
                    when(mock.getLogicalName()).thenReturn("I'm the mock");
                })) {
            scanner.open("test", _context);
            assertThat(scanner.context).isEqualTo(_context);
            assertThat(scanner.getLogicalName()).isEqualTo("test");
            assertThat(scanner.properties).isEqualTo(_properties);
            assertThat(scanner.getConfiguration().getLogicalName()).isEqualTo("I'm the mock");
            assertThat(scanner.getState()).isEqualTo(DLSState.OPENED);
            assertThat(scanner.options).isEqualTo(_properties);
        }
    }

    @Test(expected = DLSException.class)
    public void openNoProfile() throws DLSException, APosException {
        DLSUSBScanner scanner = new DLSUSBScanner();
        when(_profileManager.getConfigurationForProfileId("test")).thenThrow(new APosException());
        scanner.open("test", _context);
        assertThat(scanner.getState()).isNotEqualTo(DLSState.OPENED);
    }

    @Test(expected = DLSException.class)
    public void openLoadException() throws DLSException {
        DLSUSBScanner scanner = new DLSUSBScanner();
        try (MockedConstruction<DLSScannerConfig> ignored = mockConstruction(DLSScannerConfig.class,
                (mock, context) -> {
                    when(mock.loadConfiguration(any(), any())).thenThrow(new APosException());
                })) {
            scanner.open("test", _context);
            assertThat(scanner.getState()).isNotEqualTo(DLSState.OPENED);
        }
    }

    //endregion

    //region Report Configuration
    @Test
    public void reportConfigurationWhileUpgrading() throws DLSException {
        DLSUSBScanner scanner = new DLSUSBScanner();
        DLSJposConst.updateInProgress = true;
        scanner.reportConfiguration();
    }

    @Test
    public void reportConfigurationTimeout() throws DLSException {
        DLSUSBScanner scanner = new DLSUSBScanner();
        scanner.port = _port;
        scanner.reportConfiguration();
    }

    @Test
    public void reportConfigurationAllZeroes() throws DLSException {
        DLSUSBScanner scanner = new DLSUSBScanner();
        scanner.port = _port;
        when(_port.sendData(DLSUSBScanner.reportConfigCmd, DLSUSBScanner.reportConfigCmd.length)).then(invocation -> {
            scanner.reportBuf = new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
            return null;
        });
        DLSScannerConfig config = scanner.reportConfiguration();
        assertThat(config.getEnableUPCEAN()).isFalse();
        assertThat(config.getEnableUPCD1D5()).isFalse();
        assertThat(config.getEnableCode39()).isFalse();
        assertThat(config.getEnableInterleaved()).isFalse();
        assertThat(config.getEnableCodabar()).isFalse();
        assertThat(config.getEnableCode93()).isFalse();
        assertThat(config.getEnableCode128()).isFalse();
        assertThat(config.getEnableUCCEAN128()).isFalse();
        assertThat(config.getEnable2DigitSups()).isFalse();
        assertThat(config.getEnable5DigitSups()).isFalse();
        assertThat(config.getEnableCode128Sups()).isFalse();
        assertThat(config.getEnableUPCACheckDigit()).isFalse();
        assertThat(config.getEnableUPCECheckDigit()).isFalse();
        assertThat(config.getEnableCode39CheckDigit()).isFalse();
        assertThat(config.getEnableITFCheckDigit()).isFalse();
        assertThat(config.getEnableEANJAN2LabelDecode()).isFalse();
        assertThat(config.getEnableUPCAtoEAN13Expansion()).isFalse();
        assertThat(config.getEnableUPCEtoEAN13Expansion()).isFalse();
        assertThat(config.getEnableUPCEtoUPCAExpansion()).isFalse();
        assertThat(config.getEnable4DigitPriceCheckDigit()).isFalse();
        assertThat(config.getEnable5DigitPriceCheckDigit()).isFalse();
        assertThat(config.getEnableGoodReadBeep()).isFalse();
        assertThat(config.getBeepVolume()).isEqualTo(0);
        assertThat(config.getBeepFrequency()).isEqualTo(0);
        assertThat(config.getBeepDuration()).isEqualTo(0);
        assertThat(config.getMotorTimeout()).isEqualTo(0);
        assertThat(config.getLaserTimeout()).isEqualTo(0);
        assertThat(config.getDoubleReadTimeout()).isEqualTo(0);
        assertThat(config.getStoreLabelSecurityLevel()).isEqualTo(0);
        assertThat(config.getITFLength1()).isEqualTo(4);
        assertThat(config.getTwoITFs()).isFalse();
        assertThat(config.getITFRange()).isFalse();
        assertThat(config.getITFLength2()).isEqualTo(4);
        assertThat(config.getLEDGoodReadDuration()).isEqualTo(0);
        assertThat(config.getEnableBarCodeProgramming()).isFalse();
        assertThat(config.getEnableLaserOnOffSwitch()).isFalse();
        assertThat(config.getEnableVolumeSwitch()).isFalse();
    }

    @Test
    public void reportConfigurationAllOnes() throws DLSException {
        DLSUSBScanner scanner = new DLSUSBScanner();
        scanner.port = _port;
        when(_port.sendData(DLSUSBScanner.reportConfigCmd, DLSUSBScanner.reportConfigCmd.length)).then(invocation -> {
            scanner.reportBuf = new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF};
            return null;
        });
        DLSScannerConfig config = scanner.reportConfiguration();
        assertThat(config.getEnableUPCEAN()).isTrue();
        assertThat(config.getEnableUPCD1D5()).isTrue();
        assertThat(config.getEnableCode39()).isTrue();
        assertThat(config.getEnableInterleaved()).isTrue();
        assertThat(config.getEnableCodabar()).isTrue();
        assertThat(config.getEnableCode93()).isTrue();
        assertThat(config.getEnableCode128()).isTrue();
        assertThat(config.getEnableUCCEAN128()).isTrue();
        assertThat(config.getEnable2DigitSups()).isTrue();
        assertThat(config.getEnable5DigitSups()).isTrue();
        assertThat(config.getEnableCode128Sups()).isTrue();
        assertThat(config.getEnableUPCACheckDigit()).isTrue();
        assertThat(config.getEnableUPCECheckDigit()).isTrue();
        assertThat(config.getEnableCode39CheckDigit()).isTrue();
        assertThat(config.getEnableITFCheckDigit()).isTrue();
        assertThat(config.getEnableEANJAN2LabelDecode()).isTrue();
        assertThat(config.getEnableUPCAtoEAN13Expansion()).isTrue();
        assertThat(config.getEnableUPCEtoEAN13Expansion()).isTrue();
        assertThat(config.getEnableUPCEtoUPCAExpansion()).isTrue();
        assertThat(config.getEnable4DigitPriceCheckDigit()).isTrue();
        assertThat(config.getEnable5DigitPriceCheckDigit()).isTrue();
        assertThat(config.getEnableGoodReadBeep()).isTrue();
        assertThat(config.getBeepVolume()).isEqualTo(3);
        assertThat(config.getBeepFrequency()).isEqualTo(3);
        assertThat(config.getBeepDuration()).isEqualTo(3);
        assertThat(config.getMotorTimeout()).isEqualTo(7);
        assertThat(config.getLaserTimeout()).isEqualTo(3);
        assertThat(config.getDoubleReadTimeout()).isEqualTo(3);
        assertThat(config.getStoreLabelSecurityLevel()).isEqualTo(3);
        assertThat(config.getITFLength1()).isEqualTo(32);
        assertThat(config.getTwoITFs()).isTrue();
        assertThat(config.getITFRange()).isTrue();
        assertThat(config.getITFLength2()).isEqualTo(32);
        assertThat(config.getLEDGoodReadDuration()).isEqualTo(3);
        assertThat(config.getEnableBarCodeProgramming()).isTrue();
        assertThat(config.getEnableLaserOnOffSwitch()).isTrue();
        assertThat(config.getEnableVolumeSwitch()).isTrue();
    }

    //endregion

    //region Reset
    @Test
    public void resetWhileUpgrading() throws DLSException {
        DLSUSBScanner scanner = new DLSUSBScanner();
        scanner.port = _port;
        DLSJposConst.updateInProgress = true;
        scanner.reset();
        verify(_port, times(0)).sendData(DLSUSBScanner.resetCmd, DLSUSBScanner.resetCmd.length);
    }

    @Test
    public void reset() throws DLSException {
        DLSUSBScanner scanner = new DLSUSBScanner();
        scanner.port = _port;
        scanner.reset();
        verify(_port, times(1)).sendData(DLSUSBScanner.resetCmd, DLSUSBScanner.resetCmd.length);
    }
    //endregion

    @Test(expected = UnsupportedOperationException.class)
    public void sendMessage() {
        DLSUSBScanner scanner = new DLSUSBScanner();
        scanner.sendMessage("");
    }

    @Test
    public void sendRecord() {
        DLSUSBScanner scanner = new DLSUSBScanner();
        assertThat(scanner.sendRecord("Test")).isEqualTo(DLSJposConst.DL_NRESP);
    }

    //region Update Configuration
    @Test
    public void updateConfigurationWhileUpgrading() throws DLSException {
        DLSUSBScanner scanner = new DLSUSBScanner();
        scanner.port = _port;
        DLSJposConst.updateInProgress = true;
        scanner.updateConfiguration();
        verify(_port, times(0)).sendData(scanner.configCmd, scanner.configCmd.length);
    }

    @Test
    public void updateConfigurationAllFalse() throws DLSException {
        DLSUSBScanner scanner = new DLSUSBScanner();
        scanner.port = _port;
        scanner.scannerConfig = mock(DLSScannerConfig.class);
        when(_port.sendData(scanner.configCmd, scanner.configCmd.length)).then(invocation -> {
            assertThat((byte[]) invocation.getArgument(0)).isEqualTo(
                    new byte[]{32, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}

            );
            scanner.statusBuf = "RESULT".getBytes();
            return null;
        });
        scanner.updateConfiguration();
        verify(_port, times(1)).sendData(scanner.configCmd, scanner.configCmd.length);
    }

    @Test
    public void updateConfigurationAllTrue() throws DLSException {
        DLSUSBScanner scanner = new DLSUSBScanner();
        scanner.port = _port;
        scanner.scannerConfig = mock(DLSScannerConfig.class);
        setUpDLSScannerConfigAllTrue(scanner.scannerConfig);
        when(_port.sendData(scanner.configCmd, scanner.configCmd.length)).then(invocation -> {
            assertThat((byte[]) invocation.getArgument(0)).isEqualTo(
                    new byte[]{32, 0, -1, 127, 63, 1, 0, 0, -60, 4, 29}

            );
            scanner.statusBuf = "RESULT".getBytes();
            return null;
        });
        scanner.updateConfiguration();
        verify(_port, times(1)).sendData(scanner.configCmd, scanner.configCmd.length);
    }
    //endregion

    //region Print to Event String
    @Test
    public void printToEventStringFewChar() {
        DLSUSBScanner scanner = new DLSUSBScanner();
        assertThat(scanner.printToEventString(new byte[]{1, 2, 3})).isEqualTo("EOF");
    }

    @Test
    public void printToEventString() {
        DLSUSBScanner scanner = new DLSUSBScanner();
        String result = scanner.printToEventString(new byte[]{20,
                1,
                2,
                3,
                4,
                5,
                6,
                7,
                8,
                9,
                1,
                2,
                3,
                4,
                5,
                6,
                7,
                8,
                9,
                10
        });

        assertThat(result).contains("EvType: 05");
        assertThat(result).contains("EvMod: 06");
        assertThat(result).contains("EvCod: 07");
        assertThat(result).contains("EvDat: 08090102");
        assertThat(result).contains("AddEvDat: 03040506");
        assertThat(result).contains("Hours: 1800");
        assertThat(result).contains("EvCount 09");
    }
    //endregion

//    @Test
//    public void portError() {
//        DLSUSBScanner scanner = new DLSUSBScanner();
//        scanner.portError(UsbPortStatusListener.ErrorStatus.CAN_NOT_FIND_COM_PORT);
//    }
//
//    @Test
//    public void portClosed() {
//        DLSUSBScanner scanner = new DLSUSBScanner();
//        scanner.portClosed();
//    }
//
//    @Test
//    public void portOpened() throws DLSException {
//        MockUtils.mockDLSPropertiesSingleton(mock(DLSProperties.class));
//        DLSUSBScanner scale = new DLSUSBScanner();
//        scale.port = _port;
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
//        DLSUSBScanner scanner = new DLSUSBScanner();
//        scanner.port = _port;
//        scanner.portOpened();
//        verify(_port, times(1)).addDataReceivedListener(scanner);
//        verify(_port, times(1)).addDeviceRemovedListener(scanner);
//        verify(_port, times(1)).addDeviceReattachedListener(scanner);
//        assertThat(scanner.getState()).isEqualTo(DLSState.CLOSED);
//        MockUtils.cleanDLSProperties();
//    }

    //region Support function
    private void setUpLabelHelperMock(LabelHelper helper) {
        when(helper.getLabelsIds(new byte[]{22})).thenReturn(new LabelIds(
                "SCAN_SDT_EAN13",
                104,
                "CI_LABEL_ID_EAN13",
                "46",
                "42",
                "16",
                "46",
                "",
                ""
        ));
        when(helper.getLabelsIds(new byte[]{0, 10, 11})).thenReturn(new LabelIds(
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
    }

    private void setUpDLSScannerConfigAllTrue(DLSScannerConfig config) {
        when(config.getEnableUPCEAN()).thenReturn(true);
        when(config.getEnableUPCD1D5()).thenReturn(true);
        when(config.getEnableCode39()).thenReturn(true);
        when(config.getEnableInterleaved()).thenReturn(true);
        when(config.getEnableCodabar()).thenReturn(true);
        when(config.getEnableCode93()).thenReturn(true);
        when(config.getEnableCode128()).thenReturn(true);
        when(config.getEnableUCCEAN128()).thenReturn(true);
        when(config.getEnable2DigitSups()).thenReturn(true);
        when(config.getEnable5DigitSups()).thenReturn(true);
        when(config.getEnableCode128Sups()).thenReturn(true);
        when(config.getEnableUPCACheckDigit()).thenReturn(true);
        when(config.getEnableUPCECheckDigit()).thenReturn(true);
        when(config.getEnableCode39CheckDigit()).thenReturn(true);
        when(config.getEnableITFCheckDigit()).thenReturn(true);
        when(config.getEnableEANJAN2LabelDecode()).thenReturn(true);
        when(config.getEnableUPCAtoEAN13Expansion()).thenReturn(true);
        when(config.getEnableUPCEtoEAN13Expansion()).thenReturn(true);
        when(config.getEnableUPCEtoUPCAExpansion()).thenReturn(true);
        when(config.getEnable4DigitPriceCheckDigit()).thenReturn(true);
        when(config.getEnable5DigitPriceCheckDigit()).thenReturn(true);
        when(config.getEnableGoodReadBeep()).thenReturn(true);
        when(config.getITFLength1()).thenReturn(4);
        when(config.getTwoITFs()).thenReturn(true);
        when(config.getITFRange()).thenReturn(true);
        when(config.getITFLength2()).thenReturn(4);
        when(config.getLEDGoodReadDuration()).thenReturn(1);
        when(config.getEnableBarCodeProgramming()).thenReturn(true);
        when(config.getEnableLaserOnOffSwitch()).thenReturn(true);
        when(config.getEnableVolumeSwitch()).thenReturn(true);
    }
    //endregion
}