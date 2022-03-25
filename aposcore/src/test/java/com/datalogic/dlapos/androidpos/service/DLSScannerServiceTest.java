package com.datalogic.dlapos.androidpos.service;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.Context;

import com.datalogic.dlapos.androidpos.common.DLSCConfig;
import com.datalogic.dlapos.androidpos.common.DLSDeviceInfo;
import com.datalogic.dlapos.androidpos.common.DLSException;
import com.datalogic.dlapos.androidpos.common.DLSJposConst;
import com.datalogic.dlapos.androidpos.common.DLSObjectFactory;
import com.datalogic.dlapos.androidpos.common.DLSProperties;
import com.datalogic.dlapos.androidpos.common.DLSScannerConfig;
import com.datalogic.dlapos.androidpos.common.DLSState;
import com.datalogic.dlapos.androidpos.common.ErrorData;
import com.datalogic.dlapos.androidpos.common.EventData;
import com.datalogic.dlapos.androidpos.common.ExtendedChannelInterpretation;
import com.datalogic.dlapos.androidpos.common.ItemData;
import com.datalogic.dlapos.androidpos.common.LabelData;
import com.datalogic.dlapos.androidpos.interpretation.DLSDevice;
import com.datalogic.dlapos.androidpos.interpretation.DLSScanner;
import com.datalogic.dlapos.androidpos.interpretation.DLSUSBFlash;
import com.datalogic.dlapos.androidpos.interpretation.DeviceErrorStatusListener;
import com.datalogic.dlapos.androidpos.utils.MockUtils;
import com.datalogic.dlapos.commons.constant.CommonsConstants;
import com.datalogic.dlapos.commons.constant.ErrorConstants;
import com.datalogic.dlapos.commons.constant.ScannerConstants;
import com.datalogic.dlapos.commons.control.BaseControl;
import com.datalogic.dlapos.commons.event.BaseEvent;
import com.datalogic.dlapos.commons.event.DataEvent;
import com.datalogic.dlapos.commons.event.ErrorEvent;
import com.datalogic.dlapos.commons.event.EventCallback;
import com.datalogic.dlapos.commons.event.StatusUpdateEvent;
import com.datalogic.dlapos.commons.support.APosException;

import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;

public class DLSScannerServiceTest {

    @Mock
    private final DLSScanner _scanner = mock(DLSScanner.class);

    @Mock
    private final Context _context = mock(Context.class);

    @Test
    public void addCheckDigit() {
        DLSScannerService service = new DLSScannerService();
        assertThat(service.addCheckDigit(new byte[]{0, 0, 0})).isEqualTo(new byte[]{0, 0, 0, 64});
        assertThat(service.addCheckDigit(new byte[]{0, 0, 1})).isEqualTo(new byte[]{0, 0, 1, 61});
        assertThat(service.addCheckDigit(new byte[]{0, 0, 2})).isEqualTo(new byte[]{0, 0, 2, 48});
        assertThat(service.addCheckDigit(new byte[]{0, 0, 3})).isEqualTo(new byte[]{0, 0, 3, 65});
        assertThat(service.addCheckDigit(new byte[]{0, 0, 4})).isEqualTo(new byte[]{0, 0, 4, 62});
        assertThat(service.addCheckDigit(new byte[]{0, 0, 5})).isEqualTo(new byte[]{0, 0, 5, 59});
        assertThat(service.addCheckDigit(new byte[]{0, 0, 6})).isEqualTo(new byte[]{0, 0, 6, 66});
        assertThat(service.addCheckDigit(new byte[]{0, 0, 7})).isEqualTo(new byte[]{0, 0, 7, 63});
        assertThat(service.addCheckDigit(new byte[]{0, 0, 8})).isEqualTo(new byte[]{0, 0, 8, 60});
        assertThat(service.addCheckDigit(new byte[]{0, 0, 9})).isEqualTo(new byte[]{0, 0, 9, 67});
        assertThat(service.addCheckDigit(new byte[]{0, 0, 0, 0})).isEqualTo(new byte[]{0, 0, 0, 0, 62});
    }

    @Test
    public void checkScanner() throws APosException {
        DLSScannerService service = new DLSScannerService();
        try {
            service.checkScanner();
            fail();
        } catch (APosException e) {
            assertThat(e.getErrorCode()).isEqualTo(ErrorConstants.APOS_E_FAILURE);
        }

        service.setScanner(_scanner);
        service.checkScanner();
    }

    //region Claim
    @Test
    public void claimNoDevice() {
        DLSScannerService service = new DLSScannerService();
        try {
            service.claim(15);
            fail();
        } catch (APosException e) {
            assertThat(e.getErrorCode()).isEqualTo(ErrorConstants.APOS_E_NOHARDWARE);
        }
    }

    @Test
    public void claimAlreadyClaimed() throws APosException, DLSException {
        DLSScannerService service = new DLSScannerService();
        service.device = _scanner;
        service.setScanner(_scanner);
        when(_scanner.getDeviceInfo()).thenReturn(mock(DLSDeviceInfo.class));
        service.listData = new LinkedList<>();
        when(_scanner.getState()).thenReturn(DLSState.CLAIMED);
        service.claim(15);
        verify(_scanner, times(0)).claim(15);
    }

    @Test
    public void claim() throws APosException, DLSException {
        DLSScannerService service = new DLSScannerService();
        service.device = _scanner;
        service.setScanner(_scanner);
        service.listData = new LinkedList<>();
        DLSDeviceInfo info = mock(DLSDeviceInfo.class);
        when(info.getDeviceCategory()).thenReturn("TestCategory");
        when(info.getDeviceBus()).thenReturn("USB");
        when(_scanner.getDeviceInfo()).thenReturn(info);
        service.options = mock(DLSProperties.class);
        when(_scanner.getState()).thenReturn(DLSState.OPENED);
        service.claim(15);
        verify(_scanner, times(1)).claim(15);
        verify(_scanner, times(1)).addDeviceErrorListener(service);
        verify(_scanner, times(1)).addDeviceStatusListener(service);
        verify(_scanner, times(1)).addDirectIODataListener(service);
        assertThat(service.getCategory()).isEqualTo("TestCategory");
        assertThat(service.getBusType()).isEqualTo("USB");
    }

    @Test
    public void claimFails() throws DLSException {
        DLSScannerService service = new DLSScannerService();
        service.device = _scanner;
        DLSDeviceInfo info = mock(DLSDeviceInfo.class);
        when(_scanner.getDeviceInfo()).thenReturn(info);
        service.options = mock(DLSProperties.class);
        when(_scanner.getState()).thenReturn(DLSState.OPENED);
        doThrow(new DLSException(12, "Test")).when(_scanner).claim(15);
        try {
            service.claim(15);
            fail();
        } catch (APosException e) {
            assertThat(e.getErrorCode()).isEqualTo(ErrorConstants.APOS_E_FAILURE);
        }
    }

    //endregion

    @Test
    public void clearInput() throws APosException {
        DLSScannerService service = new DLSScannerService();
        service.device = _scanner;
        when(_scanner.getState()).thenReturn(DLSState.CLOSED);
        testExceptionError(() -> {
            service.clearInput();
            return null;
        }, ErrorConstants.APOS_E_CLOSED);

        when(_scanner.getState()).thenReturn(DLSState.OPENED);
        testExceptionError(() -> {
            service.clearInput();
            return null;
        }, ErrorConstants.APOS_E_NOTCLAIMED);

        when(_scanner.getState()).thenReturn(DLSState.CLAIMED);
        service.listData = new LinkedList<>();
        service.listData.add(new EventData(5));
        service.clearInput();
        assertThat(service.getDeviceState()).isEqualTo(CommonsConstants.S_IDLE);
        assertThat(service.listData).isEmpty();
    }

    @Test
    public void clearInputProperties() throws APosException {
        DLSScannerService service = new DLSScannerService();
        service.device = _scanner;
        when(_scanner.getState()).thenReturn(DLSState.CLOSED);
        testExceptionError(() -> {
            service.clearInputProperties();
            return null;
        }, ErrorConstants.APOS_E_CLOSED);

        when(_scanner.getState()).thenReturn(DLSState.OPENED);
        testExceptionError(() -> {
            service.clearInputProperties();
            return null;
        }, ErrorConstants.APOS_E_NOTCLAIMED);

        when(_scanner.getState()).thenReturn(DLSState.CLAIMED);
        service.clearInputProperties();
        assertThat(service.getCurrentLabelData()).isNull();
        assertThat(service.getDeviceState()).isEqualTo(CommonsConstants.S_IDLE);
    }

    //region count file records
    @Test
    public void countFileRecords() {
        DLSScannerService service = new DLSScannerService();
        String fileContent = "122F0024J000S0220000FF HDFGEN2 2.39.0.0 Application Copyright 2019 by DATALOGIC INC \n" +
                "P000S0250000FF Firmware Generator 2.39.0.0 Library Copyright 2019 by DATALOGIC INC\n" +
                "*000S0120000FF App=610099025  Cfg=  APID=0898        \n" +
                "&000S0100000FF VID=05F9 PID=122F EC=0024\n" +
                "&000S0100000FF FileName=\"610098920.BIN\" \n" +
                "&000S0100000FF FileName=\"610099025.BIN\" \n" +
                ">000S01C0000FF FileName=\"..\\..\\..\\TOOL\\ALADDIN\\OPTIONENABLE.BIN\"\n" +
                "2000S016000000000500470045001200130044004D004000431F\n" +
                "\\000S02B0000010220202020202020202020363130303939303235202020202020202020202000000000FFFFFFFF65\n" +
                "\u0012000S00600000466206F\n" +
                "p000S3350000000001010102000000030166200000000000000300000001CCE0202020363130303938393230202020202020202020202030C8\n" +
                "p000S33500000030026620000000000000344E4E00314C0E20202036313030393930323520202020202020202020203007662000000000006A\n" +
                "P000S325000000600000000100000001202020363130303939303235202020202020202020202030B8\n" +
                "Ð000S36500000000210000EA050000EA050000EA050000EA050000EA00000100050000EA030000EAFEFFFFEAFEFFFFEAFEFFFFEAFEFFFFEAFEFFFFEA04E04EE200402DE900E04FE101402DE9A8E09FE510009EE510E08EE513F021E31E502DE904100DE201D04DE019\n" +
                "Ð000S3650000006002402DE930FF2FE10240BDE801D08DE01E50BDE8D2F021E374E09FE538E08EE50140BDE80EF06FE10080FDE8632900FA5F2900FA100F11EE010AC0E30400C0E30100C0E3100F01EED2F021E344109FE501D0A0E1D3F021E33C109FE501D0A0E1A3\n" +
                "Ð000S365000000C0DFF021E334109FE501D0A0E1500F11EE0F0680E3500F01EE6FF07FF50101A0E3100AE8EE18009FE530FF2FE114C09FE53CFF2FE1000002FC68CE210068C9210068C42100BC0120000C012000000000FA210000FA0AA090E8000C82448344AAF19A\n" +
                "Ð000S365000001200107DA4501D100F039F8AFF2090EBAE80F0013F0010F18BFFB1A43F0010318476CAF00008CAF0000103A24BF78C878C1FAD8520724BF30C830C144BF04680C60704700000023002400250026103A28BF78C1FBD8520728BF30C148BF0B607047B3\n" +
                "Ð000S365000001801FB50AF053FA024802490AF0FEF81FBD6884210068C4210010B510BDDFF80CD0FFF7EEFF00F06CE80AF092F968C4210003B4FFF7F1FF03BC0AF0C6F870402DE90C004FE2020250E320D04DE20900002A30009FE57F1090E87F108DE80D00A0E316\n" +
                "AAAAAAAAA";
        assertThat(service.countFileRecords(new ByteArrayInputStream(fileContent.getBytes(StandardCharsets.UTF_8)))).isEqualTo(18);

        fileContent = "122F0024J000S0220000FF HDFGEN2 2.39.0.0 Application Copyright 2019 by DATALOGIC INC \n" +
                "P000S0250000FF Firmware Generator 2.39.0.0 Library Copyright 2019 by DATALOGIC INC\n" +
                "*000S0120000FF App=610099025  Cfg=  APID=0898        \n" +
                "&000S0100000FF VID=05F9 PID=122F EC=0024\n" +
                "&000S0100000FF FileName=\"610098920.BIN\" \n" +
                "&000S0100000FF FileName=\"610099025.BIN\" \n" +
                ">000S01C0000FF FileName=\"..\\..\\..\\TOOL\\ALADDIN\\OPTIONENABLE.BIN\"\n" +
                "2000S016000000000500470045001200130044004D004000431F\n" +
                "\\000S02B0000010220202020202020202020363130303939303235202020202020202020202000000000FFFFFFFF65\n" +
                "\u0012000S00600000466206F\n" +
                "p000S3350000000001010102000000030166200000000000000300000001CCE0202020363130303938393230202020202020202020202030C8\n" +
                "p000S33500000030026620000000000000344E4E00314C0E20202036313030393930323520202020202020202020203007662000000000006A\n" +
                "P000S325000000600000000100000001202020363130303939303235202020202020202020202030B8\n" +
                "Ð000S36500000000210000EA050000EA050000EA050000EA050000EA00000100050000EA030000EAFEFFFFEAFEFFFFEAFEFFFFEAFEFFFFEAFEFFFFEA04E04EE200402DE900E04FE101402DE9A8E09FE510009EE510E08EE513F021E31E502DE904100DE201D04DE019\n" +
                "Ð000S3650000006002402DE930FF2FE10240BDE801D08DE01E50BDE8D2F021E374E09FE538E08EE50140BDE80EF06FE10080FDE8632900FA5F2900FA100F11EE010AC0E30400C0E30100C0E3100F01EED2F021E344109FE501D0A0E1D3F021E33C109FE501D0A0E1A3\n" +
                "Ð000S365000000C0DFF021E334109FE501D0A0E1500F11EE0F0680E3500F01EE6FF07FF50101A0E3100AE8EE18009FE530FF2FE114C09FE53CFF2FE1000002FC68CE210068C9210068C42100BC0120000C012000000000FA210000FA0AA090E8000C82448344AAF19A\n" +
                "Ð000S365000001200107DA4501D100F039F8AFF2090EBAE80F0013F0010F18BFFB1A43F0010318476CAF00008CAF0000103A24BF78C878C1FAD8520724BF30C830C144BF04680C60704700000023002400250026103A28BF78C1FBD8520728BF30C148BF0B607047B3\n" +
                "AAAAAAAAA\n" +
                "Ð000S365000001801FB50AF053FA024802490AF0FEF81FBD6884210068C4210010B510BDDFF80CD0FFF7EEFF00F06CE80AF092F968C4210003B4FFF7F1FF03BC0AF0C6F870402DE90C004FE2020250E320D04DE20900002A30009FE57F1090E87F108DE80D00A0E316";

        assertThat(service.countFileRecords(new ByteArrayInputStream(fileContent.getBytes(StandardCharsets.UTF_8)))).isEqualTo(17);
    }

    @Test
    public void countFileRecordsDoubleCall() {
        DLSScannerService service = new DLSScannerService();
        String fileContent = "122F0024J000S0220000FF HDFGEN2 2.39.0.0 Application Copyright 2019 by DATALOGIC INC \n" +
                "P000S0250000FF Firmware Generator 2.39.0.0 Library Copyright 2019 by DATALOGIC INC\n" +
                "*000S0120000FF App=610099025  Cfg=  APID=0898        \n" +
                "&000S0100000FF VID=05F9 PID=122F EC=0024\n" +
                "&000S0100000FF FileName=\"610098920.BIN\" \n" +
                "&000S0100000FF FileName=\"610099025.BIN\" \n" +
                ">000S01C0000FF FileName=\"..\\..\\..\\TOOL\\ALADDIN\\OPTIONENABLE.BIN\"\n" +
                "2000S016000000000500470045001200130044004D004000431F\n" +
                "\\000S02B0000010220202020202020202020363130303939303235202020202020202020202000000000FFFFFFFF65\n" +
                "\u0012000S00600000466206F\n" +
                "p000S3350000000001010102000000030166200000000000000300000001CCE0202020363130303938393230202020202020202020202030C8\n" +
                "p000S33500000030026620000000000000344E4E00314C0E20202036313030393930323520202020202020202020203007662000000000006A\n" +
                "P000S325000000600000000100000001202020363130303939303235202020202020202020202030B8\n" +
                "Ð000S36500000000210000EA050000EA050000EA050000EA050000EA00000100050000EA030000EAFEFFFFEAFEFFFFEAFEFFFFEAFEFFFFEAFEFFFFEA04E04EE200402DE900E04FE101402DE9A8E09FE510009EE510E08EE513F021E31E502DE904100DE201D04DE019\n" +
                "Ð000S3650000006002402DE930FF2FE10240BDE801D08DE01E50BDE8D2F021E374E09FE538E08EE50140BDE80EF06FE10080FDE8632900FA5F2900FA100F11EE010AC0E30400C0E30100C0E3100F01EED2F021E344109FE501D0A0E1D3F021E33C109FE501D0A0E1A3\n" +
                "Ð000S365000000C0DFF021E334109FE501D0A0E1500F11EE0F0680E3500F01EE6FF07FF50101A0E3100AE8EE18009FE530FF2FE114C09FE53CFF2FE1000002FC68CE210068C9210068C42100BC0120000C012000000000FA210000FA0AA090E8000C82448344AAF19A\n" +
                "Ð000S365000001200107DA4501D100F039F8AFF2090EBAE80F0013F0010F18BFFB1A43F0010318476CAF00008CAF0000103A24BF78C878C1FAD8520724BF30C830C144BF04680C60704700000023002400250026103A28BF78C1FBD8520728BF30C148BF0B607047B3\n" +
                "Ð000S365000001801FB50AF053FA024802490AF0FEF81FBD6884210068C4210010B510BDDFF80CD0FFF7EEFF00F06CE80AF092F968C4210003B4FFF7F1FF03BC0AF0C6F870402DE90C004FE2020250E320D04DE20900002A30009FE57F1090E87F108DE80D00A0E316\n" +
                "AAAAAAAAA";
        InputStream stream = new ByteArrayInputStream(fileContent.getBytes(StandardCharsets.UTF_8));
        assertThat(service.countFileRecords(stream)).isEqualTo(18);
        assertThat(service.countFileRecords(stream)).isEqualTo(18);
    }
    //endregion

    @Test
    public void currentLabelData() {
        DLSScannerService service = new DLSScannerService();
        assertThat(service.getCurrentLabelData()).isNull();
        LabelData label = new LabelData(new byte[]{2}, new byte[]{4}, 4);
        service.setCurrentLabelData(label);
        assertThat(service.getCurrentLabelData()).isEqualTo(label);
    }

    @Test
    public void decodeData() throws APosException {
        DLSScannerService service = new DLSScannerService();
        service.device = _scanner;
        when(_scanner.getState()).thenReturn(DLSState.CLOSED);
        testExceptionError(() -> {
            service.setDecodeData(true);
            return null;
        }, ErrorConstants.APOS_E_CLOSED);
        testExceptionError(() -> {
            service.getDecodeData();
            return null;
        }, ErrorConstants.APOS_E_CLOSED);

        when(_scanner.getState()).thenReturn(DLSState.OPENED);
        service.setDecodeData(false);
        assertThat(service.getDecodeData()).isFalse();
        service.setDecodeData(true);
        assertThat(service.getDecodeData()).isTrue();
    }

    @Test
    public void delete() throws APosException {
        DLSScannerService service = new DLSScannerService();
        service.delete();
    }

    @Test
    public void deviceState() {
        DLSScannerService service = new DLSScannerService();
        service.setDeviceState(1);
        assertThat(service.getDeviceState()).isEqualTo(1);

        service.setDeviceState(2);
        assertThat(service.getDeviceState()).isEqualTo(2);
    }

    //region EnqueueError
    @Test
    public void enqueueError() {
        DLSScannerService service = new DLSScannerService();
        service.device = _scanner;
        service.listData = new LinkedList<>();
        when(_scanner.getState()).thenReturn(DLSState.OPENED);
        service.enqueueError(10, 11, 12, 13);
        assertThat(service.listData).isNotNull();
        assertThat(service.listData.size()).isEqualTo(1);
        EventData data = service.listData.getFirst();
        assertThat(data instanceof ErrorData);
        assertThat(data.isErrorEvent()).isTrue();
        assertThat(((ErrorData) data).getCode()).isEqualTo(10);
        assertThat(((ErrorData) data).getExCode()).isEqualTo(11);
        assertThat(((ErrorData) data).getLocus()).isEqualTo(12);
        assertThat(((ErrorData) data).getResponse()).isEqualTo(13);

        service.enqueueError(20, 21, 22, 23);
        assertThat(service.listData.size()).isEqualTo(2);
    }

    @Test
    public void enqueueErrorSendData() throws APosException {
        DLSScannerService service = new DLSScannerService();
        service.device = _scanner;
        service.listData = new LinkedList<>();
        service.setDataEventEnabled(true);
        final int[] callbacksCount = {0};
        service.setEventCallbacks(new EventCallback() {
            @Override
            public void fireEvent(BaseEvent event, EventType type) {
                assertThat(event instanceof ErrorEvent).isTrue();
                assertThat(event.getSource()).isEqualTo(service);
                callbacksCount[0]++;
                if (((ErrorEvent) event).getErrorCode() == 10) {
                    assertThat(((ErrorEvent) event).getErrorCodeExtended()).isEqualTo(11);
                    assertThat(((ErrorEvent) event).getErrorLocus()).isEqualTo(12);
                    assertThat(((ErrorEvent) event).getErrorResponse()).isEqualTo(13);
                } else if (((ErrorEvent) event).getErrorCode() == 20) {
                    assertThat(((ErrorEvent) event).getErrorCodeExtended()).isEqualTo(21);
                    assertThat(((ErrorEvent) event).getErrorLocus()).isEqualTo(22);
                    assertThat(((ErrorEvent) event).getErrorResponse()).isEqualTo(23);
                } else
                    fail();
            }

            @Override
            public BaseControl getEventSource() {
                return null;
            }
        });
        when(_scanner.getState()).thenReturn(DLSState.OPENED);
        service.enqueueError(10, 11, 12, 13);
        assertThat(service.listData).isNotNull();
        assertThat(service.listData.size()).isEqualTo(0);
        assertThat(callbacksCount[0]).isEqualTo(1);

        service.setDataEventEnabled(true);
        service.enqueueError(20, 21, 22, 23);
        assertThat(service.listData.size()).isEqualTo(0);
        assertThat(callbacksCount[0]).isEqualTo(2);
    }

    @Test
    public void enqueueErrorLabelErrorAlreadyIn() {
        DLSScannerService service = new DLSScannerService();
        service.device = _scanner;
        when(_scanner.getState()).thenReturn(DLSState.OPENED);
        service.listData = new LinkedList<>();
        service.listData.add(new LabelData(new byte[0], new byte[0], 5));
        service.enqueueError(10, 11, 12, 13);
        assertThat(service.listData.size()).isEqualTo(3);
        assertThat(service.listData.getFirst().isErrorEvent()).isTrue();
        assertThat(service.listData.getLast().isErrorEvent()).isTrue();
        assertThat(service.listData.get(1).isLabelEvent()).isTrue();
    }

    @Test
    public void enqueueErrorClosed() {
        DLSScannerService service = new DLSScannerService();
        service.device = _scanner;
        service.listData = new LinkedList<>();
        when(_scanner.getState()).thenReturn(DLSState.CLOSED);
        service.enqueueError(10, 11, 12, 13);
    }
    //endregion

    @Test
    public void expandUPCE() {
        DLSScannerService service = new DLSScannerService();
        byte[] data = new byte[10];
        assertThat(service.expandUPCE(data)).isEqualTo(new byte[11]);

        data = new byte[]{48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48};
        assertThat(service.expandUPCE(data)).isEqualTo(new byte[]{48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48});

        data = new byte[]{48, 49, 48, 48, 49, 48, 48, 49, 48, 49, 48};
        assertThat(service.expandUPCE(data)).isEqualTo(new byte[]{48, 49, 48, 48, 48, 48, 48, 48, 48, 49, 48});

        data = new byte[]{48, 49, 48, 48, 49, 48, 51, 49, 48, 49, 48};
        assertThat(service.expandUPCE(data)).isEqualTo(new byte[]{48, 49, 48, 48, 48, 48, 48, 48, 48, 49, 48});

        data = new byte[]{48, 49, 48, 48, 49, 48, 52, 49, 48, 49, 48};
        assertThat(service.expandUPCE(data)).isEqualTo(new byte[]{48, 49, 48, 48, 49, 48, 48, 48, 48, 48, 48});

        data = new byte[]{48, 49, 48, 48, 49, 48, 53, 49, 48, 49, 48};
        assertThat(service.expandUPCE(data)).isEqualTo(new byte[]{48, 49, 48, 48, 49, 48, 48, 48, 48, 48, 53});
    }

    //region findFromPatternInFirmwarePackage

    @Test
    public void findFromPatternInFirmwarePackage() {
        DLSScannerService service = new DLSScannerService();
        String fileContent = "122F0024J000S0220000FF HDFGEN2 2.39.0.0 Application Copyright 2019 by DATALOGIC INC \n" +
                "P000S0250000FF Firmware Generator 2.39.0.0 Library Copyright 2019 by DATALOGIC INC\n" +
                "*000S0120000FF App=610099025  Cfg=          \n" +
                "&000S0100000FF VID=05F9 PID=122F EC=0024\n" +
                "&000S0100000FF FileName=\"610098920.BIN\" \n" +
                "&000S0100000FF FileName=\"610099025.BIN\" \n" +
                ">000S01C0000FF FileName=\"..\\..\\..\\TOOL\\ALADDIN\\OPTIONENABLE.BIN\"\n" +
                "2000S016000000000500470045001200130044004D004000431F\n" +
                "\\000S02B0000010220202020202020202020363130303939303235202020202020202020202000000000FFFFFFFF65\n" +
                "\u0012000S00600000466206F\n" +
                "p000S3350000000001010102000000030166200000000000000300000001CCE0202020363130303938393230202020202020202020202030C8\n" +
                "p000S33500000030026620000000000000344E4E00314C0E20202036313030393930323520202020202020202020203007662000000000006A\n" +
                "P000S325000000600000000100000001202020363130303939303235202020202020202020202030B8\n" +
                "Ð000S36500000000210000EA050000EA050000EA050000EA050000EA00000100050000EA030000EAFEFFFFEAFEFFFFEAFEFFFFEAFEFFFFEAFEFFFFEA04E04EE200402DE900E04FE101402DE9A8E09FE510009EE510E08EE513F021E31E502DE904100DE201D04DE019\n" +
                "Ð000S3650000006002402DE930FF2FE10240BDE801D08DE01E50BDE8D2F021E374E09FE538E08EE50140BDE80EF06FE10080FDE8632900FA5F2900FA100F11EE010AC0E30400C0E30100C0E3100F01EED2F021E344109FE501D0A0E1D3F021E33C109FE501D0A0E1A3\n" +
                "Ð000S365000000C0DFF021E334109FE501D0A0E1500F11EE0F0680E3500F01EE6FF07FF50101A0E3100AE8EE18009FE530FF2FE114C09FE53CFF2FE1000002FC68CE210068C9210068C42100BC0120000C012000000000FA210000FA0AA090E8000C82448344AAF19A\n" +
                "Ð000S365000001200107DA4501D100F039F8AFF2090EBAE80F0013F0010F18BFFB1A43F0010318476CAF00008CAF0000103A24BF78C878C1FAD8520724BF30C830C144BF04680C60704700000023002400250026103A28BF78C1FBD8520728BF30C148BF0B607047B3\n" +
                "Ð000S365000001801FB50AF053FA024802490AF0FEF81FBD6884210068C4210010B510BDDFF80CD0FFF7EEFF00F06CE80AF092F968C4210003B4FFF7F1FF03BC0AF0C6F870402DE90C004FE2020250E320D04DE20900002A30009FE57F1090E87F108DE80D00A0E316";
        Pattern pattern = Pattern.compile("(VID=)(\\w{4})");
        assertThat(service.findFromPatternInFirmwarePackage(new BufferedReader(new StringReader(fileContent)), pattern)).isEqualTo("05F9");

        pattern = Pattern.compile("(PID=)(\\w{4})");
        assertThat(service.findFromPatternInFirmwarePackage(new BufferedReader(new StringReader(fileContent)), pattern)).isEqualTo("122F");

        pattern = Pattern.compile("(EC=)(\\w{4})");
        assertThat(service.findFromPatternInFirmwarePackage(new BufferedReader(new StringReader(fileContent)), pattern)).isEqualTo("0024");
    }

    @Test
    public void findFromPatternInFirmwarePackageNotFound() {
        DLSScannerService service = new DLSScannerService();
        String fileContent = "122F0024J000S0220000FF HDFGEN2 2.39.0.0 Application Copyright 2019 by DATALOGIC INC \n" +
                "P000S0250000FF Firmware Generator 2.39.0.0 Library Copyright 2019 by DATALOGIC INC\n" +
                "*000S0120000FF App=610099025  Cfg=          \n" +
                "REM &000S0100000FF VID=05F9 PID=122F EC=0024\n" +
                "&000S0100000FF FileName=\"610098920.BIN\" \n" +
                "&000S0100000FF FileName=\"610099025.BIN\" \n" +
                ">000S01C0000FF FileName=\"..\\..\\..\\TOOL\\ALADDIN\\OPTIONENABLE.BIN\"\n" +
                "2000S016000000000500470045001200130044004D004000431F\n" +
                "\\000S02B0000010220202020202020202020363130303939303235202020202020202020202000000000FFFFFFFF65\n" +
                "\u0012000S00600000466206F\n" +
                "p000S3350000000001010102000000030166200000000000000300000001CCE0202020363130303938393230202020202020202020202030C8\n" +
                "p000S33500000030026620000000000000344E4E00314C0E20202036313030393930323520202020202020202020203007662000000000006A\n" +
                "P000S325000000600000000100000001202020363130303939303235202020202020202020202030B8\n" +
                "Ð000S36500000000210000EA050000EA050000EA050000EA050000EA00000100050000EA030000EAFEFFFFEAFEFFFFEAFEFFFFEAFEFFFFEAFEFFFFEA04E04EE200402DE900E04FE101402DE9A8E09FE510009EE510E08EE513F021E31E502DE904100DE201D04DE019\n" +
                "Ð000S3650000006002402DE930FF2FE10240BDE801D08DE01E50BDE8D2F021E374E09FE538E08EE50140BDE80EF06FE10080FDE8632900FA5F2900FA100F11EE010AC0E30400C0E30100C0E3100F01EED2F021E344109FE501D0A0E1D3F021E33C109FE501D0A0E1A3\n" +
                "Ð000S365000000C0DFF021E334109FE501D0A0E1500F11EE0F0680E3500F01EE6FF07FF50101A0E3100AE8EE18009FE530FF2FE114C09FE53CFF2FE1000002FC68CE210068C9210068C42100BC0120000C012000000000FA210000FA0AA090E8000C82448344AAF19A\n" +
                "Ð000S365000001200107DA4501D100F039F8AFF2090EBAE80F0013F0010F18BFFB1A43F0010318476CAF00008CAF0000103A24BF78C878C1FAD8520724BF30C830C144BF04680C60704700000023002400250026103A28BF78C1FBD8520728BF30C148BF0B607047B3\n" +
                "Ð000S365000001801FB50AF053FA024802490AF0FEF81FBD6884210068C4210010B510BDDFF80CD0FFF7EEFF00F06CE80AF092F968C4210003B4FFF7F1FF03BC0AF0C6F870402DE90C004FE2020250E320D04DE20900002A30009FE57F1090E87F108DE80D00A0E316";
        Pattern pattern = Pattern.compile("(vid=)(\\w{4})");
        assertThat(service.findFromPatternInFirmwarePackage(new BufferedReader(new StringReader(fileContent)), pattern)).isEqualTo("");

        pattern = Pattern.compile("(VID=)(\\w{4})");
        assertThat(service.findFromPatternInFirmwarePackage(new BufferedReader(new StringReader(fileContent)), pattern)).isEqualTo("");

        pattern = Pattern.compile("(Cfg=)(\\w{4})");
        assertThat(service.findFromPatternInFirmwarePackage(new BufferedReader(new StringReader(fileContent)), pattern)).isEqualTo("");

        pattern = Pattern.compile("(INC)(\\w{4})");
        assertThat(service.findFromPatternInFirmwarePackage(new BufferedReader(new StringReader(fileContent)), pattern)).isEqualTo("");

        assertThat(service.findFromPatternInFirmwarePackage(null, pattern)).isEqualTo("");
    }
    //endregion

    //region getItemData

    @Test
    public void getItemDataNotOpened() {
        DLSScannerService service = new DLSScannerService();
        service.device = _scanner;
        when(_scanner.getState()).thenReturn(DLSState.CLOSED);
        testExceptionError(() -> {
            service.getItemData();
            return null;
        }, ErrorConstants.APOS_E_CLOSED);
    }

    @Test
    public void getItemDataNullData() throws APosException {
        DLSScannerService service = new DLSScannerService();
        service.device = _scanner;
        when(_scanner.getState()).thenReturn(DLSState.OPENED);
        assertThat(service.getItemData()).isEmpty();
    }

    @Test
    public void getItemDataValidValue() throws APosException {
        DLSScannerService service = new DLSScannerService();
        service.device = _scanner;
        when(_scanner.getState()).thenReturn(DLSState.OPENED);
        byte[] data = new byte[]{1, 2, 3};
        service.currentItemData = new ItemData(data, 2);
        assertThat(service.getItemData()).isEqualTo(data);

        data = new byte[]{1, 2, 3, 4};
        service.currentItemData = new ItemData(data, 2);
        assertThat(service.getItemData()).isEqualTo(data);
    }

    //endregion

    @Test
    public void getCapCompareFirmwareVersion() throws APosException {
        DLSScannerService service = new DLSScannerService();
        service.device = _scanner;
        when(_scanner.getState()).thenReturn(DLSState.CLOSED);
        testExceptionError(() -> {
            service.getCapCompareFirmwareVersion();
            return null;
        }, ErrorConstants.APOS_E_CLOSED);

        when(_scanner.getState()).thenReturn(DLSState.OPENED);
        testExceptionError(() -> {
            service.getCapCompareFirmwareVersion();
            return null;
        }, ErrorConstants.APOS_E_FAILURE);

        service.setScanner(_scanner);
        when(_scanner.getState()).thenReturn(DLSState.CLAIMED);
        assertThat(service.getCapCompareFirmwareVersion()).isFalse();

        when(_scanner.getCanCompareFirmwareVersion()).thenReturn(true);
        assertThat(service.getCapCompareFirmwareVersion()).isTrue();
    }

    @Test
    public void getCurrentItemData() {
        DLSScannerService service = new DLSScannerService();
        assertThat(service.getCurrentItemData()).isNull();
        service.currentItemData = new ItemData(new byte[]{1}, 3);
        ItemData result = service.getCurrentItemData();
        assertThat(result.getRawItemData()).isEqualTo(new byte[]{1});
        assertThat(result.getItemType()).isEqualTo(3);
    }

    @Test
    public void getDataCount() throws APosException {
        DLSScannerService service = new DLSScannerService();
        service.device = _scanner;

        when(_scanner.getState()).thenReturn(DLSState.CLOSED);
        testExceptionError(() -> {
            service.getDataCount();
            return null;
        }, ErrorConstants.APOS_E_CLOSED);

        when(_scanner.getState()).thenReturn(DLSState.OPENED);
        service.listData = new LinkedList<>();
        assertThat(service.getDataCount()).isEqualTo(0);
        service.listData.add(new EventData(2));
        assertThat(service.getDataCount()).isEqualTo(1);
        service.listData.add(new EventData(3));
        assertThat(service.getDataCount()).isEqualTo(2);
    }

    //region getFileECVersion
    @Test
    public void getFileECVersion() {
        DLSScannerService service = new DLSScannerService();
        String fileContent = "122F0024J000S0220000FF HDFGEN2 2.39.0.0 Application Copyright 2019 by DATALOGIC INC \n" +
                "P000S0250000FF Firmware Generator 2.39.0.0 Library Copyright 2019 by DATALOGIC INC\n" +
                "*000S0120000FF App=610099025  Cfg=          \n" +
                "&000S0100000FF VID=05F9 PID=122F EC=0024\n" +
                "&000S0100000FF FileName=\"610098920.BIN\" \n" +
                "&000S0100000FF FileName=\"610099025.BIN\" \n" +
                ">000S01C0000FF FileName=\"..\\..\\..\\TOOL\\ALADDIN\\OPTIONENABLE.BIN\"\n" +
                "2000S016000000000500470045001200130044004D004000431F\n" +
                "\\000S02B0000010220202020202020202020363130303939303235202020202020202020202000000000FFFFFFFF65\n" +
                "\u0012000S00600000466206F\n" +
                "p000S3350000000001010102000000030166200000000000000300000001CCE0202020363130303938393230202020202020202020202030C8\n" +
                "p000S33500000030026620000000000000344E4E00314C0E20202036313030393930323520202020202020202020203007662000000000006A\n" +
                "P000S325000000600000000100000001202020363130303939303235202020202020202020202030B8\n" +
                "Ð000S36500000000210000EA050000EA050000EA050000EA050000EA00000100050000EA030000EAFEFFFFEAFEFFFFEAFEFFFFEAFEFFFFEAFEFFFFEA04E04EE200402DE900E04FE101402DE9A8E09FE510009EE510E08EE513F021E31E502DE904100DE201D04DE019\n" +
                "Ð000S3650000006002402DE930FF2FE10240BDE801D08DE01E50BDE8D2F021E374E09FE538E08EE50140BDE80EF06FE10080FDE8632900FA5F2900FA100F11EE010AC0E30400C0E30100C0E3100F01EED2F021E344109FE501D0A0E1D3F021E33C109FE501D0A0E1A3\n" +
                "Ð000S365000000C0DFF021E334109FE501D0A0E1500F11EE0F0680E3500F01EE6FF07FF50101A0E3100AE8EE18009FE530FF2FE114C09FE53CFF2FE1000002FC68CE210068C9210068C42100BC0120000C012000000000FA210000FA0AA090E8000C82448344AAF19A\n" +
                "Ð000S365000001200107DA4501D100F039F8AFF2090EBAE80F0013F0010F18BFFB1A43F0010318476CAF00008CAF0000103A24BF78C878C1FAD8520724BF30C830C144BF04680C60704700000023002400250026103A28BF78C1FBD8520728BF30C148BF0B607047B3\n" +
                "Ð000S365000001801FB50AF053FA024802490AF0FEF81FBD6884210068C4210010B510BDDFF80CD0FFF7EEFF00F06CE80AF092F968C4210003B4FFF7F1FF03BC0AF0C6F870402DE90C004FE2020250E320D04DE20900002A30009FE57F1090E87F108DE80D00A0E316";


        assertThat(service.getFileECVersion(fileContent)).isEqualTo(36);

        fileContent = "122F0024J000S0220000FF HDFGEN2 2.39.0.0 Application Copyright 2019 by DATALOGIC INC \n" +
                "P000S0250000FF Firmware Generator 2.39.0.0 Library Copyright 2019 by DATALOGIC INC\n" +
                "*000S0120000FF App=610099025  Cfg=          \n" +
                "&000S0100000FF VID=05F9 PID=122F EC=0875\n" +
                "&000S0100000FF FileName=\"610098920.BIN\" \n" +
                "&000S0100000FF FileName=\"610099025.BIN\" \n" +
                ">000S01C0000FF FileName=\"..\\..\\..\\TOOL\\ALADDIN\\OPTIONENABLE.BIN\"\n" +
                "2000S016000000000500470045001200130044004D004000431F\n" +
                "\\000S02B0000010220202020202020202020363130303939303235202020202020202020202000000000FFFFFFFF65\n" +
                "\u0012000S00600000466206F\n" +
                "p000S3350000000001010102000000030166200000000000000300000001CCE0202020363130303938393230202020202020202020202030C8\n" +
                "p000S33500000030026620000000000000344E4E00314C0E20202036313030393930323520202020202020202020203007662000000000006A\n" +
                "P000S325000000600000000100000001202020363130303939303235202020202020202020202030B8\n" +
                "Ð000S36500000000210000EA050000EA050000EA050000EA050000EA00000100050000EA030000EAFEFFFFEAFEFFFFEAFEFFFFEAFEFFFFEAFEFFFFEA04E04EE200402DE900E04FE101402DE9A8E09FE510009EE510E08EE513F021E31E502DE904100DE201D04DE019\n" +
                "Ð000S3650000006002402DE930FF2FE10240BDE801D08DE01E50BDE8D2F021E374E09FE538E08EE50140BDE80EF06FE10080FDE8632900FA5F2900FA100F11EE010AC0E30400C0E30100C0E3100F01EED2F021E344109FE501D0A0E1D3F021E33C109FE501D0A0E1A3\n" +
                "Ð000S365000000C0DFF021E334109FE501D0A0E1500F11EE0F0680E3500F01EE6FF07FF50101A0E3100AE8EE18009FE530FF2FE114C09FE53CFF2FE1000002FC68CE210068C9210068C42100BC0120000C012000000000FA210000FA0AA090E8000C82448344AAF19A\n" +
                "Ð000S365000001200107DA4501D100F039F8AFF2090EBAE80F0013F0010F18BFFB1A43F0010318476CAF00008CAF0000103A24BF78C878C1FAD8520724BF30C830C144BF04680C60704700000023002400250026103A28BF78C1FBD8520728BF30C148BF0B607047B3\n" +
                "Ð000S365000001801FB50AF053FA024802490AF0FEF81FBD6884210068C4210010B510BDDFF80CD0FFF7EEFF00F06CE80AF092F968C4210003B4FFF7F1FF03BC0AF0C6F870402DE90C004FE2020250E320D04DE20900002A30009FE57F1090E87F108DE80D00A0E316";

        assertThat(service.getFileECVersion(fileContent)).isEqualTo(2165);
    }

    @Test
    public void getFileECVersionNotAnInteger() {
        DLSScannerService service = new DLSScannerService();
        String fileContent = "122F0024J000S0220000FF HDFGEN2 2.39.0.0 Application Copyright 2019 by DATALOGIC INC \n" +
                "P000S0250000FF Firmware Generator 2.39.0.0 Library Copyright 2019 by DATALOGIC INC\n" +
                "*000S0120000FF App=610099025  Cfg=          \n" +
                "&000S0100000FF VID=05F9 PID=122F EC=TRAS\n" +
                "&000S0100000FF FileName=\"610098920.BIN\" \n" +
                "&000S0100000FF FileName=\"610099025.BIN\" \n" +
                ">000S01C0000FF FileName=\"..\\..\\..\\TOOL\\ALADDIN\\OPTIONENABLE.BIN\"\n" +
                "2000S016000000000500470045001200130044004D004000431F\n" +
                "\\000S02B0000010220202020202020202020363130303939303235202020202020202020202000000000FFFFFFFF65\n" +
                "\u0012000S00600000466206F\n" +
                "p000S3350000000001010102000000030166200000000000000300000001CCE0202020363130303938393230202020202020202020202030C8\n" +
                "p000S33500000030026620000000000000344E4E00314C0E20202036313030393930323520202020202020202020203007662000000000006A\n" +
                "P000S325000000600000000100000001202020363130303939303235202020202020202020202030B8\n" +
                "Ð000S36500000000210000EA050000EA050000EA050000EA050000EA00000100050000EA030000EAFEFFFFEAFEFFFFEAFEFFFFEAFEFFFFEAFEFFFFEA04E04EE200402DE900E04FE101402DE9A8E09FE510009EE510E08EE513F021E31E502DE904100DE201D04DE019\n" +
                "Ð000S3650000006002402DE930FF2FE10240BDE801D08DE01E50BDE8D2F021E374E09FE538E08EE50140BDE80EF06FE10080FDE8632900FA5F2900FA100F11EE010AC0E30400C0E30100C0E3100F01EED2F021E344109FE501D0A0E1D3F021E33C109FE501D0A0E1A3\n" +
                "Ð000S365000000C0DFF021E334109FE501D0A0E1500F11EE0F0680E3500F01EE6FF07FF50101A0E3100AE8EE18009FE530FF2FE114C09FE53CFF2FE1000002FC68CE210068C9210068C42100BC0120000C012000000000FA210000FA0AA090E8000C82448344AAF19A\n" +
                "Ð000S365000001200107DA4501D100F039F8AFF2090EBAE80F0013F0010F18BFFB1A43F0010318476CAF00008CAF0000103A24BF78C878C1FAD8520724BF30C830C144BF04680C60704700000023002400250026103A28BF78C1FBD8520728BF30C148BF0B607047B3\n" +
                "Ð000S365000001801FB50AF053FA024802490AF0FEF81FBD6884210068C4210010B510BDDFF80CD0FFF7EEFF00F06CE80AF092F968C4210003B4FFF7F1FF03BC0AF0C6F870402DE90C004FE2020250E320D04DE20900002A30009FE57F1090E87F108DE80D00A0E316";

        assertThat(service.getFileECVersion(fileContent)).isEqualTo(0);
    }

    @Test
    public void getFileECVersionNotFound() {
        DLSScannerService service = new DLSScannerService();
        String fileContent = "122F0024J000S0220000FF HDFGEN2 2.39.0.0 Application Copyright 2019 by DATALOGIC INC \n" +
                "P000S0250000FF Firmware Generator 2.39.0.0 Library Copyright 2019 by DATALOGIC INC\n" +
                "*000S0120000FF App=610099025  Cfg=          \n" +
                "&000S0100000FF VID=122F PID=122F\n" +
                "&000S0100000FF FileName=\"610098920.BIN\" \n" +
                "&000S0100000FF FileName=\"610099025.BIN\" \n" +
                ">000S01C0000FF FileName=\"..\\..\\..\\TOOL\\ALADDIN\\OPTIONENABLE.BIN\"\n" +
                "2000S016000000000500470045001200130044004D004000431F\n" +
                "\\000S02B0000010220202020202020202020363130303939303235202020202020202020202000000000FFFFFFFF65\n" +
                "\u0012000S00600000466206F\n" +
                "p000S3350000000001010102000000030166200000000000000300000001CCE0202020363130303938393230202020202020202020202030C8\n" +
                "p000S33500000030026620000000000000344E4E00314C0E20202036313030393930323520202020202020202020203007662000000000006A\n" +
                "P000S325000000600000000100000001202020363130303939303235202020202020202020202030B8\n" +
                "Ð000S36500000000210000EA050000EA050000EA050000EA050000EA00000100050000EA030000EAFEFFFFEAFEFFFFEAFEFFFFEAFEFFFFEAFEFFFFEA04E04EE200402DE900E04FE101402DE9A8E09FE510009EE510E08EE513F021E31E502DE904100DE201D04DE019\n" +
                "Ð000S3650000006002402DE930FF2FE10240BDE801D08DE01E50BDE8D2F021E374E09FE538E08EE50140BDE80EF06FE10080FDE8632900FA5F2900FA100F11EE010AC0E30400C0E30100C0E3100F01EED2F021E344109FE501D0A0E1D3F021E33C109FE501D0A0E1A3\n" +
                "Ð000S365000000C0DFF021E334109FE501D0A0E1500F11EE0F0680E3500F01EE6FF07FF50101A0E3100AE8EE18009FE530FF2FE114C09FE53CFF2FE1000002FC68CE210068C9210068C42100BC0120000C012000000000FA210000FA0AA090E8000C82448344AAF19A\n" +
                "Ð000S365000001200107DA4501D100F039F8AFF2090EBAE80F0013F0010F18BFFB1A43F0010318476CAF00008CAF0000103A24BF78C878C1FAD8520724BF30C830C144BF04680C60704700000023002400250026103A28BF78C1FBD8520728BF30C148BF0B607047B3\n" +
                "Ð000S365000001801FB50AF053FA024802490AF0FEF81FBD6884210068C4210010B510BDDFF80CD0FFF7EEFF00F06CE80AF092F968C4210003B4FFF7F1FF03BC0AF0C6F870402DE90C004FE2020250E320D04DE20900002A30009FE57F1090E87F108DE80D00A0E316";

        assertThat(service.getFileECVersion(fileContent)).isEqualTo(0);
    }
    //endregion

    //region getFilePID
    @Test
    public void getFilePID() {
        DLSScannerService service = new DLSScannerService();
        String fileContent = "122F0024J000S0220000FF HDFGEN2 2.39.0.0 Application Copyright 2019 by DATALOGIC INC \n" +
                "P000S0250000FF Firmware Generator 2.39.0.0 Library Copyright 2019 by DATALOGIC INC\n" +
                "*000S0120000FF App=610099025  Cfg=          \n" +
                "&000S0100000FF VID=05F9 PID=122F EC=0024\n" +
                "&000S0100000FF FileName=\"610098920.BIN\" \n" +
                "&000S0100000FF FileName=\"610099025.BIN\" \n" +
                ">000S01C0000FF FileName=\"..\\..\\..\\TOOL\\ALADDIN\\OPTIONENABLE.BIN\"\n" +
                "2000S016000000000500470045001200130044004D004000431F\n" +
                "\\000S02B0000010220202020202020202020363130303939303235202020202020202020202000000000FFFFFFFF65\n" +
                "\u0012000S00600000466206F\n" +
                "p000S3350000000001010102000000030166200000000000000300000001CCE0202020363130303938393230202020202020202020202030C8\n" +
                "p000S33500000030026620000000000000344E4E00314C0E20202036313030393930323520202020202020202020203007662000000000006A\n" +
                "P000S325000000600000000100000001202020363130303939303235202020202020202020202030B8\n" +
                "Ð000S36500000000210000EA050000EA050000EA050000EA050000EA00000100050000EA030000EAFEFFFFEAFEFFFFEAFEFFFFEAFEFFFFEAFEFFFFEA04E04EE200402DE900E04FE101402DE9A8E09FE510009EE510E08EE513F021E31E502DE904100DE201D04DE019\n" +
                "Ð000S3650000006002402DE930FF2FE10240BDE801D08DE01E50BDE8D2F021E374E09FE538E08EE50140BDE80EF06FE10080FDE8632900FA5F2900FA100F11EE010AC0E30400C0E30100C0E3100F01EED2F021E344109FE501D0A0E1D3F021E33C109FE501D0A0E1A3\n" +
                "Ð000S365000000C0DFF021E334109FE501D0A0E1500F11EE0F0680E3500F01EE6FF07FF50101A0E3100AE8EE18009FE530FF2FE114C09FE53CFF2FE1000002FC68CE210068C9210068C42100BC0120000C012000000000FA210000FA0AA090E8000C82448344AAF19A\n" +
                "Ð000S365000001200107DA4501D100F039F8AFF2090EBAE80F0013F0010F18BFFB1A43F0010318476CAF00008CAF0000103A24BF78C878C1FAD8520724BF30C830C144BF04680C60704700000023002400250026103A28BF78C1FBD8520728BF30C148BF0B607047B3\n" +
                "Ð000S365000001801FB50AF053FA024802490AF0FEF81FBD6884210068C4210010B510BDDFF80CD0FFF7EEFF00F06CE80AF092F968C4210003B4FFF7F1FF03BC0AF0C6F870402DE90C004FE2020250E320D04DE20900002A30009FE57F1090E87F108DE80D00A0E316";


        assertThat(service.getFilePID(fileContent)).isEqualTo(4655);

        fileContent = "122F0024J000S0220000FF HDFGEN2 2.39.0.0 Application Copyright 2019 by DATALOGIC INC \n" +
                "P000S0250000FF Firmware Generator 2.39.0.0 Library Copyright 2019 by DATALOGIC INC\n" +
                "*000S0120000FF App=610099025  Cfg=          \n" +
                "&000S0100000FF VID=05F9 PID=0875 EC=0024\n" +
                "&000S0100000FF FileName=\"610098920.BIN\" \n" +
                "&000S0100000FF FileName=\"610099025.BIN\" \n" +
                ">000S01C0000FF FileName=\"..\\..\\..\\TOOL\\ALADDIN\\OPTIONENABLE.BIN\"\n" +
                "2000S016000000000500470045001200130044004D004000431F\n" +
                "\\000S02B0000010220202020202020202020363130303939303235202020202020202020202000000000FFFFFFFF65\n" +
                "\u0012000S00600000466206F\n" +
                "p000S3350000000001010102000000030166200000000000000300000001CCE0202020363130303938393230202020202020202020202030C8\n" +
                "p000S33500000030026620000000000000344E4E00314C0E20202036313030393930323520202020202020202020203007662000000000006A\n" +
                "P000S325000000600000000100000001202020363130303939303235202020202020202020202030B8\n" +
                "Ð000S36500000000210000EA050000EA050000EA050000EA050000EA00000100050000EA030000EAFEFFFFEAFEFFFFEAFEFFFFEAFEFFFFEAFEFFFFEA04E04EE200402DE900E04FE101402DE9A8E09FE510009EE510E08EE513F021E31E502DE904100DE201D04DE019\n" +
                "Ð000S3650000006002402DE930FF2FE10240BDE801D08DE01E50BDE8D2F021E374E09FE538E08EE50140BDE80EF06FE10080FDE8632900FA5F2900FA100F11EE010AC0E30400C0E30100C0E3100F01EED2F021E344109FE501D0A0E1D3F021E33C109FE501D0A0E1A3\n" +
                "Ð000S365000000C0DFF021E334109FE501D0A0E1500F11EE0F0680E3500F01EE6FF07FF50101A0E3100AE8EE18009FE530FF2FE114C09FE53CFF2FE1000002FC68CE210068C9210068C42100BC0120000C012000000000FA210000FA0AA090E8000C82448344AAF19A\n" +
                "Ð000S365000001200107DA4501D100F039F8AFF2090EBAE80F0013F0010F18BFFB1A43F0010318476CAF00008CAF0000103A24BF78C878C1FAD8520724BF30C830C144BF04680C60704700000023002400250026103A28BF78C1FBD8520728BF30C148BF0B607047B3\n" +
                "Ð000S365000001801FB50AF053FA024802490AF0FEF81FBD6884210068C4210010B510BDDFF80CD0FFF7EEFF00F06CE80AF092F968C4210003B4FFF7F1FF03BC0AF0C6F870402DE90C004FE2020250E320D04DE20900002A30009FE57F1090E87F108DE80D00A0E316";

        assertThat(service.getFilePID(fileContent)).isEqualTo(2165);
    }

    @Test
    public void getFilePIDSkipAPID() {
        DLSScannerService service = new DLSScannerService();
        String fileContent = "122F0024J000S0220000FF HDFGEN2 2.39.0.0 Application Copyright 2019 by DATALOGIC INC \n" +
                "P000S0250000FF Firmware Generator 2.39.0.0 Library Copyright 2019 by DATALOGIC INC\n" +
                "*000S0120000FF App=610099025  Cfg=  APID=0898        \n" +
                "&000S0100000FF VID=05F9 PID=122F EC=0024\n" +
                "&000S0100000FF FileName=\"610098920.BIN\" \n" +
                "&000S0100000FF FileName=\"610099025.BIN\" \n" +
                ">000S01C0000FF FileName=\"..\\..\\..\\TOOL\\ALADDIN\\OPTIONENABLE.BIN\"\n" +
                "2000S016000000000500470045001200130044004D004000431F\n" +
                "\\000S02B0000010220202020202020202020363130303939303235202020202020202020202000000000FFFFFFFF65\n" +
                "\u0012000S00600000466206F\n" +
                "p000S3350000000001010102000000030166200000000000000300000001CCE0202020363130303938393230202020202020202020202030C8\n" +
                "p000S33500000030026620000000000000344E4E00314C0E20202036313030393930323520202020202020202020203007662000000000006A\n" +
                "P000S325000000600000000100000001202020363130303939303235202020202020202020202030B8\n" +
                "Ð000S36500000000210000EA050000EA050000EA050000EA050000EA00000100050000EA030000EAFEFFFFEAFEFFFFEAFEFFFFEAFEFFFFEAFEFFFFEA04E04EE200402DE900E04FE101402DE9A8E09FE510009EE510E08EE513F021E31E502DE904100DE201D04DE019\n" +
                "Ð000S3650000006002402DE930FF2FE10240BDE801D08DE01E50BDE8D2F021E374E09FE538E08EE50140BDE80EF06FE10080FDE8632900FA5F2900FA100F11EE010AC0E30400C0E30100C0E3100F01EED2F021E344109FE501D0A0E1D3F021E33C109FE501D0A0E1A3\n" +
                "Ð000S365000000C0DFF021E334109FE501D0A0E1500F11EE0F0680E3500F01EE6FF07FF50101A0E3100AE8EE18009FE530FF2FE114C09FE53CFF2FE1000002FC68CE210068C9210068C42100BC0120000C012000000000FA210000FA0AA090E8000C82448344AAF19A\n" +
                "Ð000S365000001200107DA4501D100F039F8AFF2090EBAE80F0013F0010F18BFFB1A43F0010318476CAF00008CAF0000103A24BF78C878C1FAD8520724BF30C830C144BF04680C60704700000023002400250026103A28BF78C1FBD8520728BF30C148BF0B607047B3\n" +
                "Ð000S365000001801FB50AF053FA024802490AF0FEF81FBD6884210068C4210010B510BDDFF80CD0FFF7EEFF00F06CE80AF092F968C4210003B4FFF7F1FF03BC0AF0C6F870402DE90C004FE2020250E320D04DE20900002A30009FE57F1090E87F108DE80D00A0E316";


        assertThat(service.getFilePID(fileContent)).isEqualTo(4655);

        fileContent = "122F0024J000S0220000FF HDFGEN2 2.39.0.0 Application Copyright 2019 by DATALOGIC INC \n" +
                "P000S0250000FF Firmware Generator 2.39.0.0 Library Copyright 2019 by DATALOGIC INC\n" +
                "*000S0120000FF App=610099025  Cfg=          \n" +
                "&000S0100000FF VID=05F9 APID=0898 PID=0875 EC=0024\n" +
                "&000S0100000FF FileName=\"610098920.BIN\" \n" +
                "&000S0100000FF FileName=\"610099025.BIN\" \n" +
                ">000S01C0000FF FileName=\"..\\..\\..\\TOOL\\ALADDIN\\OPTIONENABLE.BIN\"\n" +
                "2000S016000000000500470045001200130044004D004000431F\n" +
                "\\000S02B0000010220202020202020202020363130303939303235202020202020202020202000000000FFFFFFFF65\n" +
                "\u0012000S00600000466206F\n" +
                "p000S3350000000001010102000000030166200000000000000300000001CCE0202020363130303938393230202020202020202020202030C8\n" +
                "p000S33500000030026620000000000000344E4E00314C0E20202036313030393930323520202020202020202020203007662000000000006A\n" +
                "P000S325000000600000000100000001202020363130303939303235202020202020202020202030B8\n" +
                "Ð000S36500000000210000EA050000EA050000EA050000EA050000EA00000100050000EA030000EAFEFFFFEAFEFFFFEAFEFFFFEAFEFFFFEAFEFFFFEA04E04EE200402DE900E04FE101402DE9A8E09FE510009EE510E08EE513F021E31E502DE904100DE201D04DE019\n" +
                "Ð000S3650000006002402DE930FF2FE10240BDE801D08DE01E50BDE8D2F021E374E09FE538E08EE50140BDE80EF06FE10080FDE8632900FA5F2900FA100F11EE010AC0E30400C0E30100C0E3100F01EED2F021E344109FE501D0A0E1D3F021E33C109FE501D0A0E1A3\n" +
                "Ð000S365000000C0DFF021E334109FE501D0A0E1500F11EE0F0680E3500F01EE6FF07FF50101A0E3100AE8EE18009FE530FF2FE114C09FE53CFF2FE1000002FC68CE210068C9210068C42100BC0120000C012000000000FA210000FA0AA090E8000C82448344AAF19A\n" +
                "Ð000S365000001200107DA4501D100F039F8AFF2090EBAE80F0013F0010F18BFFB1A43F0010318476CAF00008CAF0000103A24BF78C878C1FAD8520724BF30C830C144BF04680C60704700000023002400250026103A28BF78C1FBD8520728BF30C148BF0B607047B3\n" +
                "Ð000S365000001801FB50AF053FA024802490AF0FEF81FBD6884210068C4210010B510BDDFF80CD0FFF7EEFF00F06CE80AF092F968C4210003B4FFF7F1FF03BC0AF0C6F870402DE90C004FE2020250E320D04DE20900002A30009FE57F1090E87F108DE80D00A0E316";

        assertThat(service.getFilePID(fileContent)).isEqualTo(2165);
    }

    @Test
    public void getFilePIDNotAnInteger() {
        DLSScannerService service = new DLSScannerService();
        String fileContent = "122F0024J000S0220000FF HDFGEN2 2.39.0.0 Application Copyright 2019 by DATALOGIC INC \n" +
                "P000S0250000FF Firmware Generator 2.39.0.0 Library Copyright 2019 by DATALOGIC INC\n" +
                "*000S0120000FF App=610099025  Cfg=          \n" +
                "&000S0100000FF VID=05F9 PID=TRAS EC=0024\n" +
                "&000S0100000FF FileName=\"610098920.BIN\" \n" +
                "&000S0100000FF FileName=\"610099025.BIN\" \n" +
                ">000S01C0000FF FileName=\"..\\..\\..\\TOOL\\ALADDIN\\OPTIONENABLE.BIN\"\n" +
                "2000S016000000000500470045001200130044004D004000431F\n" +
                "\\000S02B0000010220202020202020202020363130303939303235202020202020202020202000000000FFFFFFFF65\n" +
                "\u0012000S00600000466206F\n" +
                "p000S3350000000001010102000000030166200000000000000300000001CCE0202020363130303938393230202020202020202020202030C8\n" +
                "p000S33500000030026620000000000000344E4E00314C0E20202036313030393930323520202020202020202020203007662000000000006A\n" +
                "P000S325000000600000000100000001202020363130303939303235202020202020202020202030B8\n" +
                "Ð000S36500000000210000EA050000EA050000EA050000EA050000EA00000100050000EA030000EAFEFFFFEAFEFFFFEAFEFFFFEAFEFFFFEAFEFFFFEA04E04EE200402DE900E04FE101402DE9A8E09FE510009EE510E08EE513F021E31E502DE904100DE201D04DE019\n" +
                "Ð000S3650000006002402DE930FF2FE10240BDE801D08DE01E50BDE8D2F021E374E09FE538E08EE50140BDE80EF06FE10080FDE8632900FA5F2900FA100F11EE010AC0E30400C0E30100C0E3100F01EED2F021E344109FE501D0A0E1D3F021E33C109FE501D0A0E1A3\n" +
                "Ð000S365000000C0DFF021E334109FE501D0A0E1500F11EE0F0680E3500F01EE6FF07FF50101A0E3100AE8EE18009FE530FF2FE114C09FE53CFF2FE1000002FC68CE210068C9210068C42100BC0120000C012000000000FA210000FA0AA090E8000C82448344AAF19A\n" +
                "Ð000S365000001200107DA4501D100F039F8AFF2090EBAE80F0013F0010F18BFFB1A43F0010318476CAF00008CAF0000103A24BF78C878C1FAD8520724BF30C830C144BF04680C60704700000023002400250026103A28BF78C1FBD8520728BF30C148BF0B607047B3\n" +
                "Ð000S365000001801FB50AF053FA024802490AF0FEF81FBD6884210068C4210010B510BDDFF80CD0FFF7EEFF00F06CE80AF092F968C4210003B4FFF7F1FF03BC0AF0C6F870402DE90C004FE2020250E320D04DE20900002A30009FE57F1090E87F108DE80D00A0E316";

        assertThat(service.getFilePID(fileContent)).isEqualTo(0);
    }

    @Test
    public void getFilePIDNotFound() {
        DLSScannerService service = new DLSScannerService();
        String fileContent = "122F0024J000S0220000FF HDFGEN2 2.39.0.0 Application Copyright 2019 by DATALOGIC INC \n" +
                "P000S0250000FF Firmware Generator 2.39.0.0 Library Copyright 2019 by DATALOGIC INC\n" +
                "*000S0120000FF App=610099025  Cfg=          \n" +
                "&000S0100000FF VID=122F EC=0024\n" +
                "&000S0100000FF FileName=\"610098920.BIN\" \n" +
                "&000S0100000FF FileName=\"610099025.BIN\" \n" +
                ">000S01C0000FF FileName=\"..\\..\\..\\TOOL\\ALADDIN\\OPTIONENABLE.BIN\"\n" +
                "2000S016000000000500470045001200130044004D004000431F\n" +
                "\\000S02B0000010220202020202020202020363130303939303235202020202020202020202000000000FFFFFFFF65\n" +
                "\u0012000S00600000466206F\n" +
                "p000S3350000000001010102000000030166200000000000000300000001CCE0202020363130303938393230202020202020202020202030C8\n" +
                "p000S33500000030026620000000000000344E4E00314C0E20202036313030393930323520202020202020202020203007662000000000006A\n" +
                "P000S325000000600000000100000001202020363130303939303235202020202020202020202030B8\n" +
                "Ð000S36500000000210000EA050000EA050000EA050000EA050000EA00000100050000EA030000EAFEFFFFEAFEFFFFEAFEFFFFEAFEFFFFEAFEFFFFEA04E04EE200402DE900E04FE101402DE9A8E09FE510009EE510E08EE513F021E31E502DE904100DE201D04DE019\n" +
                "Ð000S3650000006002402DE930FF2FE10240BDE801D08DE01E50BDE8D2F021E374E09FE538E08EE50140BDE80EF06FE10080FDE8632900FA5F2900FA100F11EE010AC0E30400C0E30100C0E3100F01EED2F021E344109FE501D0A0E1D3F021E33C109FE501D0A0E1A3\n" +
                "Ð000S365000000C0DFF021E334109FE501D0A0E1500F11EE0F0680E3500F01EE6FF07FF50101A0E3100AE8EE18009FE530FF2FE114C09FE53CFF2FE1000002FC68CE210068C9210068C42100BC0120000C012000000000FA210000FA0AA090E8000C82448344AAF19A\n" +
                "Ð000S365000001200107DA4501D100F039F8AFF2090EBAE80F0013F0010F18BFFB1A43F0010318476CAF00008CAF0000103A24BF78C878C1FAD8520724BF30C830C144BF04680C60704700000023002400250026103A28BF78C1FBD8520728BF30C148BF0B607047B3\n" +
                "Ð000S365000001801FB50AF053FA024802490AF0FEF81FBD6884210068C4210010B510BDDFF80CD0FFF7EEFF00F06CE80AF092F968C4210003B4FFF7F1FF03BC0AF0C6F870402DE90C004FE2020250E320D04DE20900002A30009FE57F1090E87F108DE80D00A0E316";

        assertThat(service.getFilePID(fileContent)).isEqualTo(0);
    }
    //endregion

    //region getFileVID
    @Test
    public void getFileVID() {
        DLSScannerService service = new DLSScannerService();
        String fileContent = "122F0024J000S0220000FF HDFGEN2 2.39.0.0 Application Copyright 2019 by DATALOGIC INC \n" +
                "P000S0250000FF Firmware Generator 2.39.0.0 Library Copyright 2019 by DATALOGIC INC\n" +
                "*000S0120000FF App=610099025  Cfg=          \n" +
                "&000S0100000FF VID=05F9 PID=122F EC=0024\n" +
                "&000S0100000FF FileName=\"610098920.BIN\" \n" +
                "&000S0100000FF FileName=\"610099025.BIN\" \n" +
                ">000S01C0000FF FileName=\"..\\..\\..\\TOOL\\ALADDIN\\OPTIONENABLE.BIN\"\n" +
                "2000S016000000000500470045001200130044004D004000431F\n" +
                "\\000S02B0000010220202020202020202020363130303939303235202020202020202020202000000000FFFFFFFF65\n" +
                "\u0012000S00600000466206F\n" +
                "p000S3350000000001010102000000030166200000000000000300000001CCE0202020363130303938393230202020202020202020202030C8\n" +
                "p000S33500000030026620000000000000344E4E00314C0E20202036313030393930323520202020202020202020203007662000000000006A\n" +
                "P000S325000000600000000100000001202020363130303939303235202020202020202020202030B8\n" +
                "Ð000S36500000000210000EA050000EA050000EA050000EA050000EA00000100050000EA030000EAFEFFFFEAFEFFFFEAFEFFFFEAFEFFFFEAFEFFFFEA04E04EE200402DE900E04FE101402DE9A8E09FE510009EE510E08EE513F021E31E502DE904100DE201D04DE019\n" +
                "Ð000S3650000006002402DE930FF2FE10240BDE801D08DE01E50BDE8D2F021E374E09FE538E08EE50140BDE80EF06FE10080FDE8632900FA5F2900FA100F11EE010AC0E30400C0E30100C0E3100F01EED2F021E344109FE501D0A0E1D3F021E33C109FE501D0A0E1A3\n" +
                "Ð000S365000000C0DFF021E334109FE501D0A0E1500F11EE0F0680E3500F01EE6FF07FF50101A0E3100AE8EE18009FE530FF2FE114C09FE53CFF2FE1000002FC68CE210068C9210068C42100BC0120000C012000000000FA210000FA0AA090E8000C82448344AAF19A\n" +
                "Ð000S365000001200107DA4501D100F039F8AFF2090EBAE80F0013F0010F18BFFB1A43F0010318476CAF00008CAF0000103A24BF78C878C1FAD8520724BF30C830C144BF04680C60704700000023002400250026103A28BF78C1FBD8520728BF30C148BF0B607047B3\n" +
                "Ð000S365000001801FB50AF053FA024802490AF0FEF81FBD6884210068C4210010B510BDDFF80CD0FFF7EEFF00F06CE80AF092F968C4210003B4FFF7F1FF03BC0AF0C6F870402DE90C004FE2020250E320D04DE20900002A30009FE57F1090E87F108DE80D00A0E316";


        assertThat(service.getFileVID(fileContent)).isEqualTo(1529);

        fileContent = "122F0024J000S0220000FF HDFGEN2 2.39.0.0 Application Copyright 2019 by DATALOGIC INC \n" +
                "P000S0250000FF Firmware Generator 2.39.0.0 Library Copyright 2019 by DATALOGIC INC\n" +
                "*000S0120000FF App=610099025  Cfg=          \n" +
                "&000S0100000FF VID=0875 PID=122F EC=0024\n" +
                "&000S0100000FF FileName=\"610098920.BIN\" \n" +
                "&000S0100000FF FileName=\"610099025.BIN\" \n" +
                ">000S01C0000FF FileName=\"..\\..\\..\\TOOL\\ALADDIN\\OPTIONENABLE.BIN\"\n" +
                "2000S016000000000500470045001200130044004D004000431F\n" +
                "\\000S02B0000010220202020202020202020363130303939303235202020202020202020202000000000FFFFFFFF65\n" +
                "\u0012000S00600000466206F\n" +
                "p000S3350000000001010102000000030166200000000000000300000001CCE0202020363130303938393230202020202020202020202030C8\n" +
                "p000S33500000030026620000000000000344E4E00314C0E20202036313030393930323520202020202020202020203007662000000000006A\n" +
                "P000S325000000600000000100000001202020363130303939303235202020202020202020202030B8\n" +
                "Ð000S36500000000210000EA050000EA050000EA050000EA050000EA00000100050000EA030000EAFEFFFFEAFEFFFFEAFEFFFFEAFEFFFFEAFEFFFFEA04E04EE200402DE900E04FE101402DE9A8E09FE510009EE510E08EE513F021E31E502DE904100DE201D04DE019\n" +
                "Ð000S3650000006002402DE930FF2FE10240BDE801D08DE01E50BDE8D2F021E374E09FE538E08EE50140BDE80EF06FE10080FDE8632900FA5F2900FA100F11EE010AC0E30400C0E30100C0E3100F01EED2F021E344109FE501D0A0E1D3F021E33C109FE501D0A0E1A3\n" +
                "Ð000S365000000C0DFF021E334109FE501D0A0E1500F11EE0F0680E3500F01EE6FF07FF50101A0E3100AE8EE18009FE530FF2FE114C09FE53CFF2FE1000002FC68CE210068C9210068C42100BC0120000C012000000000FA210000FA0AA090E8000C82448344AAF19A\n" +
                "Ð000S365000001200107DA4501D100F039F8AFF2090EBAE80F0013F0010F18BFFB1A43F0010318476CAF00008CAF0000103A24BF78C878C1FAD8520724BF30C830C144BF04680C60704700000023002400250026103A28BF78C1FBD8520728BF30C148BF0B607047B3\n" +
                "Ð000S365000001801FB50AF053FA024802490AF0FEF81FBD6884210068C4210010B510BDDFF80CD0FFF7EEFF00F06CE80AF092F968C4210003B4FFF7F1FF03BC0AF0C6F870402DE90C004FE2020250E320D04DE20900002A30009FE57F1090E87F108DE80D00A0E316";

        assertThat(service.getFileVID(fileContent)).isEqualTo(2165);

    }

    @Test
    public void getFileVIDNotAnInteger() {
        DLSScannerService service = new DLSScannerService();
        String fileContent = "122F0024J000S0220000FF HDFGEN2 2.39.0.0 Application Copyright 2019 by DATALOGIC INC \n" +
                "P000S0250000FF Firmware Generator 2.39.0.0 Library Copyright 2019 by DATALOGIC INC\n" +
                "*000S0120000FF App=610099025  Cfg=          \n" +
                "&000S0100000FF VID=TRAS PID=122F EC=0024\n" +
                "&000S0100000FF FileName=\"610098920.BIN\" \n" +
                "&000S0100000FF FileName=\"610099025.BIN\" \n" +
                ">000S01C0000FF FileName=\"..\\..\\..\\TOOL\\ALADDIN\\OPTIONENABLE.BIN\"\n" +
                "2000S016000000000500470045001200130044004D004000431F\n" +
                "\\000S02B0000010220202020202020202020363130303939303235202020202020202020202000000000FFFFFFFF65\n" +
                "\u0012000S00600000466206F\n" +
                "p000S3350000000001010102000000030166200000000000000300000001CCE0202020363130303938393230202020202020202020202030C8\n" +
                "p000S33500000030026620000000000000344E4E00314C0E20202036313030393930323520202020202020202020203007662000000000006A\n" +
                "P000S325000000600000000100000001202020363130303939303235202020202020202020202030B8\n" +
                "Ð000S36500000000210000EA050000EA050000EA050000EA050000EA00000100050000EA030000EAFEFFFFEAFEFFFFEAFEFFFFEAFEFFFFEAFEFFFFEA04E04EE200402DE900E04FE101402DE9A8E09FE510009EE510E08EE513F021E31E502DE904100DE201D04DE019\n" +
                "Ð000S3650000006002402DE930FF2FE10240BDE801D08DE01E50BDE8D2F021E374E09FE538E08EE50140BDE80EF06FE10080FDE8632900FA5F2900FA100F11EE010AC0E30400C0E30100C0E3100F01EED2F021E344109FE501D0A0E1D3F021E33C109FE501D0A0E1A3\n" +
                "Ð000S365000000C0DFF021E334109FE501D0A0E1500F11EE0F0680E3500F01EE6FF07FF50101A0E3100AE8EE18009FE530FF2FE114C09FE53CFF2FE1000002FC68CE210068C9210068C42100BC0120000C012000000000FA210000FA0AA090E8000C82448344AAF19A\n" +
                "Ð000S365000001200107DA4501D100F039F8AFF2090EBAE80F0013F0010F18BFFB1A43F0010318476CAF00008CAF0000103A24BF78C878C1FAD8520724BF30C830C144BF04680C60704700000023002400250026103A28BF78C1FBD8520728BF30C148BF0B607047B3\n" +
                "Ð000S365000001801FB50AF053FA024802490AF0FEF81FBD6884210068C4210010B510BDDFF80CD0FFF7EEFF00F06CE80AF092F968C4210003B4FFF7F1FF03BC0AF0C6F870402DE90C004FE2020250E320D04DE20900002A30009FE57F1090E87F108DE80D00A0E316";

        assertThat(service.getFileVID(fileContent)).isEqualTo(0);
    }

    @Test
    public void getFileVIDNotFound() {
        DLSScannerService service = new DLSScannerService();
        String fileContent = "122F0024J000S0220000FF HDFGEN2 2.39.0.0 Application Copyright 2019 by DATALOGIC INC \n" +
                "P000S0250000FF Firmware Generator 2.39.0.0 Library Copyright 2019 by DATALOGIC INC\n" +
                "*000S0120000FF App=610099025  Cfg=          \n" +
                "&000S0100000FF PID=122F EC=0024\n" +
                "&000S0100000FF FileName=\"610098920.BIN\" \n" +
                "&000S0100000FF FileName=\"610099025.BIN\" \n" +
                ">000S01C0000FF FileName=\"..\\..\\..\\TOOL\\ALADDIN\\OPTIONENABLE.BIN\"\n" +
                "2000S016000000000500470045001200130044004D004000431F\n" +
                "\\000S02B0000010220202020202020202020363130303939303235202020202020202020202000000000FFFFFFFF65\n" +
                "\u0012000S00600000466206F\n" +
                "p000S3350000000001010102000000030166200000000000000300000001CCE0202020363130303938393230202020202020202020202030C8\n" +
                "p000S33500000030026620000000000000344E4E00314C0E20202036313030393930323520202020202020202020203007662000000000006A\n" +
                "P000S325000000600000000100000001202020363130303939303235202020202020202020202030B8\n" +
                "Ð000S36500000000210000EA050000EA050000EA050000EA050000EA00000100050000EA030000EAFEFFFFEAFEFFFFEAFEFFFFEAFEFFFFEAFEFFFFEA04E04EE200402DE900E04FE101402DE9A8E09FE510009EE510E08EE513F021E31E502DE904100DE201D04DE019\n" +
                "Ð000S3650000006002402DE930FF2FE10240BDE801D08DE01E50BDE8D2F021E374E09FE538E08EE50140BDE80EF06FE10080FDE8632900FA5F2900FA100F11EE010AC0E30400C0E30100C0E3100F01EED2F021E344109FE501D0A0E1D3F021E33C109FE501D0A0E1A3\n" +
                "Ð000S365000000C0DFF021E334109FE501D0A0E1500F11EE0F0680E3500F01EE6FF07FF50101A0E3100AE8EE18009FE530FF2FE114C09FE53CFF2FE1000002FC68CE210068C9210068C42100BC0120000C012000000000FA210000FA0AA090E8000C82448344AAF19A\n" +
                "Ð000S365000001200107DA4501D100F039F8AFF2090EBAE80F0013F0010F18BFFB1A43F0010318476CAF00008CAF0000103A24BF78C878C1FAD8520724BF30C830C144BF04680C60704700000023002400250026103A28BF78C1FBD8520728BF30C148BF0B607047B3\n" +
                "Ð000S365000001801FB50AF053FA024802490AF0FEF81FBD6884210068C4210010B510BDDFF80CD0FFF7EEFF00F06CE80AF092F968C4210003B4FFF7F1FF03BC0AF0C6F870402DE90C004FE2020250E320D04DE20900002A30009FE57F1090E87F108DE80D00A0E316";

        assertThat(service.getFileVID(fileContent)).isEqualTo(0);
    }

    //endregion

    @Test
    public void getScanData() throws APosException {
        DLSScannerService service = new DLSScannerService();
        service.device = _scanner;
        when(_scanner.getState()).thenReturn(DLSState.CLOSED);
        testExceptionError(() -> {
            service.getScanData();
            return null;
        }, ErrorConstants.APOS_E_CLOSED);

        when(_scanner.getState()).thenReturn(DLSState.OPENED);
        assertThat(service.getScanData()).isEqualTo(new byte[0]);

        service.currentLabelData = mock(LabelData.class);
        when(service.currentLabelData.getRawLabel()).thenReturn(new byte[]{1, 2});
        assertThat(service.getScanData()).isEqualTo(new byte[]{1, 2});
    }

    @Test
    public void getScanDataLabel() throws APosException {
        DLSScannerService service = new DLSScannerService();
        service.device = _scanner;
        when(_scanner.getState()).thenReturn(DLSState.CLOSED);
        testExceptionError(() -> {
            service.getScanDataLabel();
            return null;
        }, ErrorConstants.APOS_E_CLOSED);

        when(_scanner.getState()).thenReturn(DLSState.OPENED);
        assertThat(service.getScanDataLabel()).isEqualTo(new byte[0]);

        service.currentLabelData = mock(LabelData.class);
        when(service.currentLabelData.getDecodedLabel()).thenReturn(new byte[]{1, 2});
        assertThat(service.getScanDataLabel()).isEqualTo(new byte[]{1, 2});
    }

    @Test
    public void getScanDataType() throws APosException {
        DLSScannerService service = new DLSScannerService();
        service.device = _scanner;
        when(_scanner.getState()).thenReturn(DLSState.CLOSED);
        testExceptionError(() -> {
            service.getScanDataType();
            return null;
        }, ErrorConstants.APOS_E_CLOSED);

        when(_scanner.getState()).thenReturn(DLSState.OPENED);
        assertThat(service.getScanDataType()).isEqualTo(ScannerConstants.SCAN_SDT_UNKNOWN);

        service.currentLabelData = mock(LabelData.class);
        when(service.currentLabelData.getLabelType()).thenReturn(5);
        assertThat(service.getScanDataType()).isEqualTo(5);
    }

    @Test
    public void onLabelReceived() {
        DLSScannerService service = new DLSScannerService();
        service.device = _scanner;
        service.listData = new LinkedList<>();
        service.options = mock(DLSProperties.class);
        byte[] data = new byte[12];
        service.onLabelReceived(data, data, 2);
    }

    @Test
    public void onLabelReceivedException() {
        DLSScannerService service = new DLSScannerService();
        service.device = _scanner;
        when(_scanner.getState()).thenReturn(DLSState.CLOSED);
        service.listData = new LinkedList<>();
        service.options = mock(DLSProperties.class);
        byte[] data = new byte[12];
        service.onLabelReceived(data, data, 2);
    }

    //region onDeviceError
    @Test
    public void onDeviceErrorDeviceRemoved() {
        DLSScannerService service = new DLSScannerService();
        service.device = _scanner;
        service.listData = new LinkedList<>();
        service.options = mock(DLSProperties.class);
        service.onDeviceError(DeviceErrorStatusListener.ERR_DEVICE_REMOVED);
        assertThat(service.getDeviceState()).isEqualTo(CommonsConstants.S_IDLE);
        assertThat(service.listData).isEmpty();

        when(service.options.isPostRemovalEvents()).thenReturn(true);
        service.onDeviceError(DeviceErrorStatusListener.ERR_DEVICE_REMOVED);
        assertThat(service.listData.size()).isEqualTo(1);
        assertThat(service.listData.get(0) instanceof ErrorData).isTrue();
        assertThat(((ErrorData) service.listData.get(0)).getCode()).isEqualTo(ErrorConstants.APOS_E_NOHARDWARE);
        assertThat(((ErrorData) service.listData.get(0)).getExCode()).isEqualTo(DeviceErrorStatusListener.ERR_DEVICE_REMOVED);
        assertThat(((ErrorData) service.listData.get(0)).getLocus()).isEqualTo(ErrorConstants.APOS_EL_INPUT);
        assertThat(((ErrorData) service.listData.get(0)).getResponse()).isEqualTo(ErrorConstants.APOS_ER_CLEAR);
    }

    @Test
    public void onDeviceErrorDeviceReattached() {
        DLSScannerService service = new DLSScannerService();
        service.device = _scanner;
        service.listData = new LinkedList<>();
        service.options = mock(DLSProperties.class);
        service.onDeviceError(DeviceErrorStatusListener.ERR_DEVICE_REATTACHED);
        assertThat(service.getDeviceState()).isEqualTo(CommonsConstants.S_IDLE);
        assertThat(service.listData).isEmpty();

        when(service.options.isPostRemovalEvents()).thenReturn(true);
        service.onDeviceError(DeviceErrorStatusListener.ERR_DEVICE_REATTACHED);
        assertThat(service.listData.size()).isEqualTo(1);
        assertThat(service.listData.get(0) instanceof ErrorData).isTrue();
        assertThat(((ErrorData) service.listData.get(0)).getCode()).isEqualTo(ErrorConstants.APOS_E_NOHARDWARE);
        assertThat(((ErrorData) service.listData.get(0)).getExCode()).isEqualTo(DeviceErrorStatusListener.ERR_DEVICE_REATTACHED);
        assertThat(((ErrorData) service.listData.get(0)).getLocus()).isEqualTo(ErrorConstants.APOS_EL_INPUT);
        assertThat(((ErrorData) service.listData.get(0)).getResponse()).isEqualTo(ErrorConstants.APOS_ER_CLEAR);
    }

    @Test
    public void onDeviceErrorCheckDigit() {
        DLSScannerService service = new DLSScannerService();
        service.device = _scanner;
        service.listData = new LinkedList<>();
        service.options = mock(DLSProperties.class);
        when(service.options.isPostRemovalEvents()).thenReturn(true);
        service.onDeviceError(DeviceErrorStatusListener.ERR_CHECKDIGIT);
        assertThat(service.listData.size()).isEqualTo(1);
        assertThat(service.listData.get(0) instanceof ErrorData).isTrue();
        assertThat(((ErrorData) service.listData.get(0)).getCode()).isEqualTo(ErrorConstants.APOS_E_FAILURE);
        assertThat(((ErrorData) service.listData.get(0)).getExCode()).isEqualTo(DeviceErrorStatusListener.ERR_CHECKDIGIT);
        assertThat(((ErrorData) service.listData.get(0)).getLocus()).isEqualTo(ErrorConstants.APOS_EL_INPUT);
        assertThat(((ErrorData) service.listData.get(0)).getResponse()).isEqualTo(ErrorConstants.APOS_ER_CLEAR);
    }

    @Test
    public void onDeviceErrorHardware() {
        DLSScannerService service = new DLSScannerService();
        service.device = _scanner;
        service.listData = new LinkedList<>();
        service.options = mock(DLSProperties.class);
        when(service.options.isPostRemovalEvents()).thenReturn(true);
        service.onDeviceError(DeviceErrorStatusListener.ERR_HARDWARE);
        assertThat(service.listData.size()).isEqualTo(1);
        assertThat(service.listData.get(0) instanceof ErrorData).isTrue();
        assertThat(((ErrorData) service.listData.get(0)).getCode()).isEqualTo(ErrorConstants.APOS_E_FAILURE);
        assertThat(((ErrorData) service.listData.get(0)).getExCode()).isEqualTo(DeviceErrorStatusListener.ERR_HARDWARE);
        assertThat(((ErrorData) service.listData.get(0)).getLocus()).isEqualTo(ErrorConstants.APOS_EL_INPUT);
        assertThat(((ErrorData) service.listData.get(0)).getResponse()).isEqualTo(ErrorConstants.APOS_ER_CLEAR);
    }

    @Test
    public void onDeviceErrorFlashing() {
        DLSScannerService service = new DLSScannerService();
        service.device = _scanner;
        service.listData = new LinkedList<>();
        service.options = mock(DLSProperties.class);
        when(service.options.isPostRemovalEvents()).thenReturn(true);
        service.onDeviceError(DeviceErrorStatusListener.ERR_FLASHING);
        assertThat(service.listData.size()).isEqualTo(1);
        assertThat(service.listData.get(0) instanceof ErrorData).isTrue();
        assertThat(((ErrorData) service.listData.get(0)).getCode()).isEqualTo(ErrorConstants.APOS_E_BUSY);
        assertThat(((ErrorData) service.listData.get(0)).getExCode()).isEqualTo(DeviceErrorStatusListener.ERR_FLASHING);
        assertThat(((ErrorData) service.listData.get(0)).getLocus()).isEqualTo(ErrorConstants.APOS_EL_INPUT);
        assertThat(((ErrorData) service.listData.get(0)).getResponse()).isEqualTo(ErrorConstants.APOS_ER_CONTINUEINPUT);
    }

    @Test
    public void onDeviceErrorBusy() {
        DLSScannerService service = new DLSScannerService();
        service.device = _scanner;
        service.listData = new LinkedList<>();
        service.options = mock(DLSProperties.class);
        when(service.options.isPostRemovalEvents()).thenReturn(true);
        service.onDeviceError(DeviceErrorStatusListener.ERR_BUSY);
        assertThat(service.listData.size()).isEqualTo(1);
        assertThat(service.listData.get(0) instanceof ErrorData).isTrue();
        assertThat(((ErrorData) service.listData.get(0)).getCode()).isEqualTo(ErrorConstants.APOS_E_BUSY);
        assertThat(((ErrorData) service.listData.get(0)).getExCode()).isEqualTo(DeviceErrorStatusListener.ERR_BUSY);
        assertThat(((ErrorData) service.listData.get(0)).getLocus()).isEqualTo(ErrorConstants.APOS_EL_INPUT);
        assertThat(((ErrorData) service.listData.get(0)).getResponse()).isEqualTo(ErrorConstants.APOS_ER_CONTINUEINPUT);
    }

    @Test
    public void onDeviceErrorDioUndefined() {
        DLSScannerService service = new DLSScannerService();
        service.device = _scanner;
        service.listData = new LinkedList<>();
        service.options = mock(DLSProperties.class);
        when(service.options.isPostRemovalEvents()).thenReturn(true);
        service.onDeviceError(DeviceErrorStatusListener.ERR_DIO_UNDEFINED);
        assertThat(service.listData.size()).isEqualTo(1);
        assertThat(service.listData.get(0) instanceof ErrorData).isTrue();
        assertThat(((ErrorData) service.listData.get(0)).getCode()).isEqualTo(ErrorConstants.APOS_E_ILLEGAL);
        assertThat(((ErrorData) service.listData.get(0)).getExCode()).isEqualTo(DeviceErrorStatusListener.ERR_DIO_UNDEFINED);
        assertThat(((ErrorData) service.listData.get(0)).getLocus()).isEqualTo(ErrorConstants.APOS_EL_OUTPUT);
        assertThat(((ErrorData) service.listData.get(0)).getResponse()).isEqualTo(ErrorConstants.APOS_ER_CONTINUEINPUT);
    }

    @Test
    public void onDeviceErrorDioNotAllowed() {
        DLSScannerService service = new DLSScannerService();
        service.device = _scanner;
        service.listData = new LinkedList<>();
        service.options = mock(DLSProperties.class);
        when(service.options.isPostRemovalEvents()).thenReturn(true);
        service.onDeviceError(DeviceErrorStatusListener.ERR_DIO_NOT_ALLOWED);
        assertThat(service.listData.size()).isEqualTo(1);
        assertThat(service.listData.get(0) instanceof ErrorData).isTrue();
        assertThat(((ErrorData) service.listData.get(0)).getCode()).isEqualTo(ErrorConstants.APOS_E_ILLEGAL);
        assertThat(((ErrorData) service.listData.get(0)).getExCode()).isEqualTo(DeviceErrorStatusListener.ERR_DIO_NOT_ALLOWED);
        assertThat(((ErrorData) service.listData.get(0)).getLocus()).isEqualTo(ErrorConstants.APOS_EL_OUTPUT);
        assertThat(((ErrorData) service.listData.get(0)).getResponse()).isEqualTo(ErrorConstants.APOS_ER_CONTINUEINPUT);
    }

    @Test
    public void onDeviceErrorCmd() {
        DLSScannerService service = new DLSScannerService();
        service.device = _scanner;
        service.listData = new LinkedList<>();
        service.options = mock(DLSProperties.class);
        when(service.options.isPostRemovalEvents()).thenReturn(true);
        service.onDeviceError(DeviceErrorStatusListener.ERR_CMD);
        assertThat(service.listData.size()).isEqualTo(1);
        assertThat(service.listData.get(0) instanceof ErrorData).isTrue();
        assertThat(((ErrorData) service.listData.get(0)).getCode()).isEqualTo(ErrorConstants.APOS_E_ILLEGAL);
        assertThat(((ErrorData) service.listData.get(0)).getExCode()).isEqualTo(DeviceErrorStatusListener.ERR_CMD);
        assertThat(((ErrorData) service.listData.get(0)).getLocus()).isEqualTo(ErrorConstants.APOS_EL_OUTPUT);
        assertThat(((ErrorData) service.listData.get(0)).getResponse()).isEqualTo(ErrorConstants.APOS_ER_CONTINUEINPUT);
    }

    @Test
    public void onDeviceErrorUnknown() {
        DLSScannerService service = new DLSScannerService();
        service.device = _scanner;
        service.listData = new LinkedList<>();
        service.options = mock(DLSProperties.class);
        service.onDeviceError(-8);
        assertThat(service.getDeviceState()).isEqualTo(CommonsConstants.S_IDLE);
        assertThat(service.listData).isEmpty();
    }
    //endregion

    //region onDeviceStatus
    @Test
    public void onDeviceStatusOnline() {
        DLSScannerService service = new DLSScannerService();
        service.device = _scanner;

        when(_scanner.getState()).thenReturn(DLSState.ENABLED);
        service.onDeviceStatus(CommonsConstants.SUE_POWER_ONLINE);
    }

    @Test
    public void onDeviceStatusOnlineNotify() throws APosException {
        DLSScannerService service = new DLSScannerService();
        service.device = _scanner;
        final int[] eventCounter = {0};
        EventCallback callback = new EventCallback() {
            @Override
            public void fireEvent(BaseEvent event, EventType type) {
                eventCounter[0]++;
                assertThat(event instanceof StatusUpdateEvent).isTrue();
                assertThat(type).isEqualTo(EventCallback.EventType.StatusUpdate);
                assertThat(event.getSource()).isEqualTo(service);
                assertThat(((StatusUpdateEvent) event).getStatus()).isEqualTo(CommonsConstants.SUE_POWER_ONLINE);
            }

            @Override
            public BaseControl getEventSource() {
                return null;
            }
        };
        service.setEventCallbacks(callback);

        when(_scanner.getState()).thenReturn(DLSState.ENABLED);
        service.setPowerNotify(CommonsConstants.PN_ENABLED);
        service.onDeviceStatus(CommonsConstants.SUE_POWER_ONLINE);
        assertThat(eventCounter[0]).isEqualTo(1);
        assertThat(service.getPowerState()).isEqualTo(CommonsConstants.PS_ONLINE);

        service.setFreezeEvents(true);
        service.onDeviceStatus(CommonsConstants.SUE_POWER_ONLINE);
        assertThat(eventCounter[0]).isEqualTo(1);
    }

    @Test
    public void onDeviceStatusOffline() {
        DLSScannerService service = new DLSScannerService();
        service.device = _scanner;

        when(_scanner.getState()).thenReturn(DLSState.ENABLED);
        service.onDeviceStatus(CommonsConstants.SUE_POWER_OFF_OFFLINE);
    }

    @Test
    public void onDeviceStatusOfflineNotify() throws APosException {
        DLSScannerService service = new DLSScannerService();
        service.device = _scanner;
        final int[] eventCounter = {0};
        EventCallback callback = new EventCallback() {
            @Override
            public void fireEvent(BaseEvent event, EventType type) {
                eventCounter[0]++;
                assertThat(event instanceof StatusUpdateEvent).isTrue();
                assertThat(type).isEqualTo(EventCallback.EventType.StatusUpdate);
                assertThat(event.getSource()).isEqualTo(service);
                assertThat(((StatusUpdateEvent) event).getStatus()).isEqualTo(CommonsConstants.SUE_POWER_OFF_OFFLINE);
            }

            @Override
            public BaseControl getEventSource() {
                return null;
            }
        };
        service.setEventCallbacks(callback);

        when(_scanner.getState()).thenReturn(DLSState.ENABLED);
        service.setPowerNotify(CommonsConstants.PN_ENABLED);
        service.onDeviceStatus(CommonsConstants.SUE_POWER_OFF_OFFLINE);
        assertThat(eventCounter[0]).isEqualTo(1);
        assertThat(service.getPowerState()).isEqualTo(CommonsConstants.PS_OFF_OFFLINE);

        service.setFreezeEvents(true);
        service.onDeviceStatus(CommonsConstants.SUE_POWER_OFF_OFFLINE);
        assertThat(eventCounter[0]).isEqualTo(1);
    }

    @Test
    public void onDeviceStatusExceptions() {
        DLSScannerService service = new DLSScannerService();
        service.onDeviceStatus(CommonsConstants.SUE_POWER_ONLINE);
    }

    @Test
    public void onDeviceStatusFirmwareStatus() {
        DLSScannerService service = new DLSScannerService();
        service.device = _scanner;
        service.updateFirmwareStatus = true;

        when(_scanner.getState()).thenReturn(DLSState.ENABLED);
        service.onDeviceStatus(CommonsConstants.SUE_POWER_ONLINE);
    }
    //endregion

    //region Open
    @Test
    public void open() throws APosException {
        MockUtils.mockDLSPropertiesSingleton(mock(DLSProperties.class));
        DLSScannerService service = new DLSScannerService();
        EventCallback callback = mock(EventCallback.class);
        try (MockedStatic<DLSObjectFactory> factory = mockStatic(DLSObjectFactory.class)) {
            factory.when(() -> DLSObjectFactory.createScanner("Test", _context)).thenReturn(_scanner);
            service.open("Test", callback, _context);
            assertThat(service.options).isNotNull();
            assertThat(service.getLogicalName()).isEqualTo("Test");
            assertThat(service.getEventCallbacks()).isEqualTo(callback);
            assertThat(service.context).isEqualTo(_context);
            assertThat(service.getState()).isEqualTo(CommonsConstants.S_IDLE);
        }
        MockUtils.cleanDLSProperties();
    }

    @Test
    public void openECI() throws APosException {
        DLSProperties options = mock(DLSProperties.class);
        MockUtils.mockDLSPropertiesSingleton(options);
        DLSScannerService service = new DLSScannerService();
        EventCallback callback = mock(EventCallback.class);
        try (MockedConstruction<ExtendedChannelInterpretation> mockedConstruction = mockConstruction(ExtendedChannelInterpretation.class)) {
            try (MockedStatic<DLSObjectFactory> factory = mockStatic(DLSObjectFactory.class)) {
                factory.when(() -> DLSObjectFactory.createScanner("Test", _context)).thenReturn(_scanner);
                when(options.isEnableECI()).thenReturn(true);
                service.open("Test", callback, _context);
                assertThat(service.options).isNotNull();
                assertThat(service.getLogicalName()).isEqualTo("Test");
                assertThat(service.getEventCallbacks()).isEqualTo(callback);
                assertThat(service.context).isEqualTo(_context);
                assertThat(service.getState()).isEqualTo(CommonsConstants.S_IDLE);
            }
        }
        MockUtils.cleanDLSProperties();
    }

    @Test
    public void openException() throws DLSException {
        MockUtils.mockDLSPropertiesSingleton(mock(DLSProperties.class));
        DLSScannerService service = new DLSScannerService();
        EventCallback callback = mock(EventCallback.class);
        try (MockedStatic<DLSObjectFactory> factory = mockStatic(DLSObjectFactory.class)) {
            factory.when(() -> DLSObjectFactory.createScanner("Test", _context)).thenReturn(_scanner);
            doThrow(new DLSException(12, "13")).when(_scanner).open("Test", _context);
            service.open("Test", callback, _context);
            fail();
        } catch (APosException e) {
            assertThat(e.getErrorCode()).isEqualTo(ErrorConstants.APOS_E_NOSERVICE);
        }
        MockUtils.cleanDLSProperties();
    }

    @Test
    public void openExceptionCreating() throws DLSException {
        MockUtils.mockDLSPropertiesSingleton(mock(DLSProperties.class));
        DLSScannerService service = new DLSScannerService();
        EventCallback callback = mock(EventCallback.class);
        try (MockedStatic<DLSObjectFactory> factory = mockStatic(DLSObjectFactory.class)) {
            factory.when(() -> DLSObjectFactory.createScanner("Test", _context)).thenThrow(new DLSException(12, "13"));
            service.open("Test", callback, _context);
            fail();
        } catch (APosException e) {
            assertThat(e.getErrorCode()).isEqualTo(ErrorConstants.APOS_E_NOSERVICE);
        }
        MockUtils.cleanDLSProperties();
    }

    @Test
    public void openWithFWUpdate() throws APosException, DLSException {
        MockUtils.mockDLSPropertiesSingleton(mock(DLSProperties.class));
        DLSScannerService service = new DLSScannerService();
        EventCallback callback = mock(EventCallback.class);
        DLSDeviceInfo info = mock(DLSDeviceInfo.class);
        when(info.getDeviceBus()).thenReturn(CommonsConstants.USB_DEVICE_BUS);
        when(info.getDeviceCategory()).thenReturn("Category");
        when(info.getDeviceName()).thenReturn("Name");
        when(info.getProductId()).thenReturn(12);
        when(info.getVendorId()).thenReturn(8);
        when(_scanner.getDeviceInfo()).thenReturn(info);
        when(_scanner.getCanUpdateFirmware()).thenReturn(true);
        try (MockedConstruction<DLSUSBFlash> flash = mockConstruction(DLSUSBFlash.class,
                (mock, context) -> {
                    when(mock.getDeviceInfo()).thenReturn(mock(DLSDeviceInfo.class));
                })) {
            try (MockedStatic<DLSObjectFactory> factory = mockStatic(DLSObjectFactory.class)) {
                factory.when(() -> DLSObjectFactory.createScanner("Test", _context)).thenReturn(_scanner);
                service.open("Test", callback, _context);
                assertThat(service.device).isNotNull();
                verify(_scanner, times(1)).open("Test", _context);
                assertThat(service.options).isNotNull();
                assertThat(service.getLogicalName()).isEqualTo("Test");
                assertThat(service.getEventCallbacks()).isEqualTo(callback);
                assertThat(service.context).isEqualTo(_context);
                assertThat(service.getState()).isEqualTo(CommonsConstants.S_IDLE);
            }
        }
        MockUtils.cleanDLSProperties();
    }

    @Test
    public void openWithFWUpdateException() {
        MockUtils.mockDLSPropertiesSingleton(mock(DLSProperties.class));
        DLSScannerService service = new DLSScannerService();
        EventCallback callback = mock(EventCallback.class);
        DLSDeviceInfo info = mock(DLSDeviceInfo.class);
        when(info.getDeviceBus()).thenReturn(CommonsConstants.USB_DEVICE_BUS);
        when(info.getDeviceCategory()).thenReturn("Category");
        when(info.getDeviceName()).thenReturn("Name");
        when(info.getProductId()).thenReturn(12);
        when(info.getVendorId()).thenReturn(8);
        when(_scanner.getDeviceInfo()).thenReturn(info);
        when(_scanner.getCanUpdateFirmware()).thenReturn(true);
        try (MockedConstruction<DLSUSBFlash> flash = mockConstruction(DLSUSBFlash.class,
                (mock, context) -> {
                    doThrow(new DLSException(1, "er")).when(mock).open(DLSScannerService.DLS_USB_FLASH, _context);
                })) {
            try (MockedStatic<DLSObjectFactory> factory = mockStatic(DLSObjectFactory.class)) {
                factory.when(() -> DLSObjectFactory.createScanner("Test", _context)).thenReturn(_scanner);
                service.open("Test", callback, _context);
                fail();
            } catch (APosException e) {
                assertThat(e.getErrorCode()).isEqualTo(ErrorConstants.APOS_E_NOSERVICE);
            }
        }
        MockUtils.cleanDLSProperties();
    }
    //endregion

    @Test
    public void powerState() throws APosException {
        DLSScannerService service = new DLSScannerService();
        service.device = _scanner;
        when(_scanner.getState()).thenReturn(DLSState.CLOSED);
        testExceptionError(() -> {
            service.setPowerState(1);
            return null;
        }, ErrorConstants.APOS_E_CLOSED);
        testExceptionError(() -> {
            service.getPowerState();
            return null;
        }, ErrorConstants.APOS_E_CLOSED);

        when(_scanner.getState()).thenReturn(DLSState.OPENED);
        testExceptionError(() -> {
            service.setPowerState(1);
            return null;
        }, ErrorConstants.APOS_E_NOTCLAIMED);
        testExceptionError(() -> {
            service.getPowerState();
            return null;
        }, ErrorConstants.APOS_E_NOTCLAIMED);

        when(_scanner.getState()).thenReturn(DLSState.CLAIMED);
        service.setPowerState(1);
        assertThat(service.getPowerState()).isEqualTo(1);
        service.setPowerState(2);
        assertThat(service.getPowerState()).isEqualTo(2);
    }

    //region Release
    @Test
    public void release() throws DLSException, APosException {
        DLSScannerService service = new DLSScannerService();
        service.setScanner(_scanner);
        service.device = _scanner;
        when(_scanner.getState()).thenReturn(DLSState.CLOSED);
        testExceptionError(() -> {
            service.release();
            return null;
        }, ErrorConstants.APOS_E_CLOSED);

        when(_scanner.getState()).thenReturn(DLSState.OPENED);
        testExceptionError(() -> {
            service.release();
            return null;
        }, ErrorConstants.APOS_E_NOTCLAIMED);

        when(_scanner.getState()).thenReturn(DLSState.CLAIMED);
        service.release();

        verify(_scanner, times(1)).release();
    }

    @Test
    public void releaseNotifyPower() throws DLSException, APosException {
        DLSScannerService service = new DLSScannerService();
        service.setScanner(_scanner);
        service.device = _scanner;
        service.config = mock(DLSCConfig.class);
        when(service.config.getOptionAsBool(DLSScannerConfig.KEY_CANNOTIFYPOWERCHANGE)).thenReturn(true);
        when(_scanner.getState()).thenReturn(DLSState.CLOSED);
        testExceptionError(() -> {
            service.release();
            return null;
        }, ErrorConstants.APOS_E_CLOSED);

        when(_scanner.getState()).thenReturn(DLSState.OPENED);
        testExceptionError(() -> {
            service.release();
            return null;
        }, ErrorConstants.APOS_E_NOTCLAIMED);

        when(_scanner.getState()).thenReturn(DLSState.CLAIMED);
        service.setPowerNotify(2);
        service.release();

        verify(_scanner, times(1)).release();
        assertThat(service.getPowerState()).isEqualTo(CommonsConstants.PS_OFF_OFFLINE);
    }

    //endregion

    @Test
    public void releaseFlash() throws DLSException {
        DLSScannerService service = new DLSScannerService();

        service.releaseFlash();

        DLSUSBFlash flash = mock(DLSUSBFlash.class);
        service.flash = flash;
        service.releaseFlash();
        verify(flash, times(1)).release();

        doThrow(new DLSException(2, "fred")).when(flash).release();
        service.releaseFlash();
    }

    @Test
    public void scanner() {
        DLSScannerService service = new DLSScannerService();
        assertThat(service.getScanner()).isNull();
        service.setScanner(_scanner);
        assertThat(service.getScanner()).isEqualTo(_scanner);
    }

    //region sendDataEvent
    @Test
    public void sendDataEventException() {
        DLSScannerService service = new DLSScannerService();
        service.device = _scanner;
        when(_scanner.getState()).thenReturn(DLSState.CLOSED);
        testExceptionError(() -> {
            service.sendDataEvent();
            return null;
        }, ErrorConstants.APOS_E_CLOSED);

    }

    @Test
    public void sendDataEventEmpty() throws APosException {
        DLSScannerService service = new DLSScannerService();
        service.device = _scanner;
        when(_scanner.getState()).thenReturn(DLSState.OPENED);
        service.sendDataEvent();

        service.listData = new LinkedList<>();
        service.sendDataEvent();
    }


    @Test
    public void sendDataEventLabel() throws APosException {
        DLSScannerService service = new DLSScannerService();
        service.device = _scanner;
        when(_scanner.getState()).thenReturn(DLSState.OPENED);
        service.listData = new LinkedList<>();
        service.sendDataEvent();
        service.setDataEventEnabled(true);

        BaseControl control = mock(BaseControl.class);
        final int[] eventCount = {0};
        EventCallback callback = new EventCallback() {
            @Override
            public void fireEvent(BaseEvent event, EventType type) {
                eventCount[0]++;
                assertThat(event instanceof DataEvent).isTrue();
                assertThat(type).isEqualTo(EventCallback.EventType.Data);
                assertThat(((DataEvent) event).getStatus()).isEqualTo(0);
            }

            @Override
            public BaseControl getEventSource() {
                return control;
            }
        };
        service.setEventCallbacks(callback);

        byte[] rawData = new byte[]{1, 2, 3, 4};
        byte[] decodeData = new byte[]{1, 2, 3, 4};
        LabelData data = new LabelData(rawData, decodeData, EventData.LABEL_EVENT);
        service.listData.add(data);
        service.sendDataEvent();
        assertThat(service.listData).isEmpty();
        assertThat(eventCount[0]).isEqualTo(1);
        assertThat(service.getCurrentLabelData()).isEqualTo(data);
    }

    @Test
    public void sendItemEventLabel() throws APosException {
        DLSScannerService service = new DLSScannerService();
        service.device = _scanner;
        when(_scanner.getState()).thenReturn(DLSState.OPENED);
        service.listData = new LinkedList<>();
        service.sendDataEvent();
        service.setDataEventEnabled(true);

        BaseControl control = mock(BaseControl.class);
        final int[] eventCount = {0};
        EventCallback callback = new EventCallback() {
            @Override
            public void fireEvent(BaseEvent event, EventType type) {
                eventCount[0]++;
                assertThat(event instanceof DataEvent).isTrue();
                assertThat(type).isEqualTo(EventCallback.EventType.Data);
                assertThat(((DataEvent) event).getStatus()).isEqualTo(0);
            }

            @Override
            public BaseControl getEventSource() {
                return control;
            }
        };
        service.setEventCallbacks(callback);

        byte[] rawData = new byte[]{1, 2, 3, 4};
        ItemData data = new ItemData(rawData, EventData.ITEM_EVENT);
        service.listData.add(data);
        service.sendDataEvent();
        assertThat(service.listData).isEmpty();
        assertThat(eventCount[0]).isEqualTo(1);
        assertThat(service.getCurrentItemData()).isEqualTo(data);
    }

    @Test
    public void sendItemEventErrorClear() throws APosException {
        DLSScannerService service = new DLSScannerService();
        service.device = _scanner;
        when(_scanner.getState()).thenReturn(DLSState.OPENED);
        service.listData = new LinkedList<>();
        service.sendDataEvent();
        service.setDataEventEnabled(true);

        BaseControl control = mock(BaseControl.class);
        final int[] eventCount = {0};
        EventCallback callback = new EventCallback() {
            @Override
            public void fireEvent(BaseEvent event, EventType type) {
                eventCount[0]++;
                assertThat(event instanceof ErrorEvent).isTrue();
                assertThat(type).isEqualTo(EventType.Error);
                assertThat(((ErrorEvent) event).getErrorCode()).isEqualTo(1);
                assertThat(((ErrorEvent) event).getErrorCodeExtended()).isEqualTo(2);
                assertThat(((ErrorEvent) event).getErrorLocus()).isEqualTo(3);
                assertThat(((ErrorEvent) event).getErrorResponse()).isEqualTo(ErrorConstants.APOS_ER_CLEAR);
            }

            @Override
            public BaseControl getEventSource() {
                return control;
            }
        };
        service.setEventCallbacks(callback);


        service.listData.add(new ErrorData(1, 2, 3, ErrorConstants.APOS_ER_CLEAR));
        service.listData.add(new ErrorData(4, 5, 6, ErrorConstants.APOS_ER_CLEAR));
        service.sendDataEvent();
        assertThat(service.listData).isEmpty();
        assertThat(eventCount[0]).isEqualTo(1);
        assertThat(service.getDeviceState()).isEqualTo(CommonsConstants.S_IDLE);
    }

    @Test
    public void sendItemEventErrorContinue() throws APosException {
        DLSScannerService service = new DLSScannerService();
        service.device = _scanner;
        when(_scanner.getState()).thenReturn(DLSState.OPENED);
        service.listData = new LinkedList<>();
        service.sendDataEvent();
        service.setDataEventEnabled(true);

        BaseControl control = mock(BaseControl.class);
        final int[] eventCount = {0};
        EventCallback callback = new EventCallback() {
            @Override
            public void fireEvent(BaseEvent event, EventType type) {
                eventCount[0]++;
                assertThat(event instanceof ErrorEvent).isTrue();
                assertThat(type).isEqualTo(EventType.Error);
                assertThat(((ErrorEvent) event).getErrorCode()).isEqualTo(1);
                assertThat(((ErrorEvent) event).getErrorCodeExtended()).isEqualTo(2);
                assertThat(((ErrorEvent) event).getErrorLocus()).isEqualTo(3);
                assertThat(((ErrorEvent) event).getErrorResponse()).isEqualTo(ErrorConstants.APOS_ER_CONTINUEINPUT);
            }

            @Override
            public BaseControl getEventSource() {
                return control;
            }
        };
        service.setEventCallbacks(callback);


        service.listData.add(new ErrorData(1, 2, 3, ErrorConstants.APOS_ER_CONTINUEINPUT));
        service.listData.add(new ErrorData(4, 5, 6, ErrorConstants.APOS_ER_CONTINUEINPUT));
        service.sendDataEvent();
        assertThat(service.listData.size()).isEqualTo(1);
        assertThat(eventCount[0]).isEqualTo(1);
        assertThat(service.getDeviceState()).isEqualTo(CommonsConstants.S_IDLE);
    }

    @Test
    public void sendItemEventErrorRetry() throws APosException {
        DLSScannerService service = new DLSScannerService();
        service.device = _scanner;
        when(_scanner.getState()).thenReturn(DLSState.OPENED);
        service.listData = new LinkedList<>();
        service.sendDataEvent();
        service.setDataEventEnabled(true);

        BaseControl control = mock(BaseControl.class);
        final int[] eventCount = {0};
        EventCallback callback = new EventCallback() {
            @Override
            public void fireEvent(BaseEvent event, EventType type) {
                eventCount[0]++;
                assertThat(event instanceof ErrorEvent).isTrue();
                assertThat(type).isEqualTo(EventType.Error);
                assertThat(((ErrorEvent) event).getErrorCode()).isEqualTo(1);
                assertThat(((ErrorEvent) event).getErrorCodeExtended()).isEqualTo(2);
                assertThat(((ErrorEvent) event).getErrorLocus()).isEqualTo(3);
                assertThat(((ErrorEvent) event).getErrorResponse()).isEqualTo(ErrorConstants.APOS_ER_RETRY);
            }

            @Override
            public BaseControl getEventSource() {
                return control;
            }
        };
        service.setEventCallbacks(callback);


        service.listData.add(new ErrorData(1, 2, 3, ErrorConstants.APOS_ER_RETRY));
        service.listData.add(new ErrorData(4, 5, 6, ErrorConstants.APOS_ER_RETRY));
        service.sendDataEvent();
        assertThat(service.listData.size()).isEqualTo(1);
        assertThat(eventCount[0]).isEqualTo(1);
        assertThat(service.getDeviceState()).isEqualTo(CommonsConstants.S_IDLE);
    }
    //endregion

    //region validateLabel
    @Test
    public void validateLabelUPCE() {
        DLSScannerService service = new DLSScannerService();
        assertThat(service.validateLabel(new byte[7], new byte[7], ScannerConstants.SCAN_SDT_UPCE)).
                isEqualTo(new byte[]{0, 0, 0, 0, 0, 0, 0, 62});

        assertThat(service.validateLabel(new byte[6], new byte[6], ScannerConstants.SCAN_SDT_UPCE)).
                isEqualTo(new byte[]{0, 0, 0, 0, 0, 0});

        assertThat(service.validateLabel(new byte[9], new byte[9], ScannerConstants.SCAN_SDT_UPCE)).
                isEqualTo(new byte[9]);
    }

    @Test
    public void validateLabelUPCA() {
        DLSScannerService service = new DLSScannerService();
        assertThat(service.validateLabel(new byte[7], new byte[7], ScannerConstants.SCAN_SDT_UPCA)).
                isEqualTo(new byte[]{0, 0, 0, 0, 0, 0, 0, 48});

        assertThat(service.validateLabel(new byte[13], new byte[13], ScannerConstants.SCAN_SDT_UPCA)).
                isEqualTo(new byte[13]);
    }

    @Test
    public void validateLabelEAN8() {
        DLSScannerService service = new DLSScannerService();
        assertThat(service.validateLabel(new byte[7], new byte[7], ScannerConstants.SCAN_SDT_EAN8)).
                isEqualTo(new byte[]{0, 0, 0, 0, 0, 0, 0, 48});

        assertThat(service.validateLabel(new byte[9], new byte[9], ScannerConstants.SCAN_SDT_EAN8)).
                isEqualTo(new byte[9]);
    }

    @Test
    public void validateLabelEAN13() {
        DLSScannerService service = new DLSScannerService();
        assertThat(service.validateLabel(new byte[7], new byte[7], ScannerConstants.SCAN_SDT_EAN13)).
                isEqualTo(new byte[]{0, 0, 0, 0, 0, 0, 0, 48});

        assertThat(service.validateLabel(new byte[19], new byte[19], ScannerConstants.SCAN_SDT_EAN13)).
                isEqualTo(new byte[19]);
    }
    //endregion

    //region verifyFirmwareFileName
    @Test
    public void verifyFirmwareFileName() throws APosException {
        DLSScannerService service = new DLSScannerService();
        service.verifyFirmwareFileName("Test.S37");

        service.isUsbScanner = true;
        service.verifyFirmwareFileName("Test.DAT");
    }

    @Test
    public void verifyFirmwareFileNameWrongFormat() {
        DLSScannerService service = new DLSScannerService();

        try {
            service.verifyFirmwareFileName("Test.PNG");
            fail();
        } catch (APosException e) {
            assertThat(e.getErrorCode()).isEqualTo(ErrorConstants.APOS_E_EXTENDED);
            assertThat(e.getErrorCodeExtension()).isEqualTo(ErrorConstants.APOS_EFIRMWARE_BAD_FILE);
        }

        try {
            service.verifyFirmwareFileName("TestPNG");
            fail();
        } catch (APosException e) {
            assertThat(e.getErrorCode()).isEqualTo(ErrorConstants.APOS_E_EXTENDED);
            assertThat(e.getErrorCodeExtension()).isEqualTo(ErrorConstants.APOS_EFIRMWARE_BAD_FILE);
        }

        try {
            service.verifyFirmwareFileName("Test.DAT");
            fail();
        } catch (APosException e) {
            assertThat(e.getErrorCode()).isEqualTo(ErrorConstants.APOS_E_EXTENDED);
            assertThat(e.getErrorCodeExtension()).isEqualTo(ErrorConstants.APOS_EFIRMWARE_BAD_FILE);
        }

        service.isUsbScanner = true;
        try {
            service.verifyFirmwareFileName("Test.S37");
            fail();
        } catch (APosException e) {
            assertThat(e.getErrorCode()).isEqualTo(ErrorConstants.APOS_E_EXTENDED);
            assertThat(e.getErrorCodeExtension()).isEqualTo(ErrorConstants.APOS_EFIRMWARE_BAD_FILE);
        }
    }

    @Test
    public void verifyFirmwareFileNameTooShort() {
        DLSScannerService service = new DLSScannerService();
        try {
            service.verifyFirmwareFileName("Tes");
            fail();
        } catch (APosException e) {
            assertThat(e.getErrorCode()).isEqualTo(ErrorConstants.APOS_E_EXTENDED);
            assertThat(e.getErrorCodeExtension()).isEqualTo(ErrorConstants.APOS_EFIRMWARE_BAD_FILE);
        }
    }

    //endregion
    private void testExceptionError(Callable<Void> callable, int errorCode) {
        try {
            callable.call();
            fail();
        } catch (APosException e) {
            assertThat(e.getErrorCode()).isEqualTo(errorCode);
        } catch (Exception e) {
            fail();
        }
    }
}