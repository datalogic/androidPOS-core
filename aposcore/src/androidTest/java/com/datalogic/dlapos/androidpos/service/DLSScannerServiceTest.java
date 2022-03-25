//package com.datalogic.dlapos.androidpos.service;
//
//import static com.google.common.truth.Truth.assertThat;
//import static org.mockito.ArgumentMatchers.anyInt;
//import static org.mockito.Mockito.mock;
//import static org.mockito.Mockito.times;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.when;
//
//import android.content.Context;
//
//import androidx.test.platform.app.InstrumentationRegistry;
//
//import com.datalogic.dlapos.androidpos.common.DLSDeviceInfo;
//import com.datalogic.dlapos.androidpos.common.DLSException;
//import com.datalogic.dlapos.androidpos.common.DLSJposConst;
//import com.datalogic.dlapos.androidpos.common.DLSProperties;
//import com.datalogic.dlapos.androidpos.interpretation.DLSDevice;
//import com.datalogic.dlapos.androidpos.interpretation.DLSScanner;
//import com.datalogic.dlapos.androidpos.interpretation.DLSUSBFlash;
//import com.datalogic.dlapos.commons.constant.CommonsConstants;
//import com.datalogic.dlapos.commons.control.BaseControl;
//import com.datalogic.dlapos.commons.event.BaseEvent;
//import com.datalogic.dlapos.commons.event.EventCallback;
//import com.datalogic.dlapos.commons.event.StatusUpdateEvent;
//
//import org.junit.Test;
//import org.junit.runner.manipulation.Ordering;
//import org.mockito.Mock;
//
//import java.io.ByteArrayInputStream;
//import java.io.File;
//import java.io.IOException;
//import java.nio.charset.StandardCharsets;
//
//public class DLSScannerServiceTest {
//
//    @Mock
//    private final DLSScanner _scanner = mock(DLSScanner.class);
//
//    private final Context _context = InstrumentationRegistry.getInstrumentation().getContext();
//
//    @Test
//    public void doUpdateUSBComplete() throws DLSException, IOException {
//        DLSScannerService service = new DLSScannerService();
//
//        DLSJposConst.updateInProgress = true; // set singleton so a shared port won't interrupt
//
//        DLSDevice device = mock(DLSDevice.class);
//        when(device.getDeviceInfo()).thenReturn(mock(DLSDeviceInfo.class));
//        service.device = device;
//
//        DLSUSBFlash flash = mock(DLSUSBFlash.class);
//        service.flash = flash;
//        service.isUsbScanner = true;
//        service.setScanner(_scanner);
//        service.options = mock(DLSProperties.class);
//        when(flash.sendRecord(anyString())).thenReturn(DLSJposConst.DL_ACK);
//        when(flash.startCommand()).thenReturn(DLSJposConst.DL_ACK);
//        when(service.options.isSendNulls()).thenReturn(true);
//        when(service.options.isSendFirmwareReset()).thenReturn(true);
//        when(_scanner.detectResetFinish()).thenReturn(true);
//        when(_scanner.isChangeBaudAfterUpdate()).thenReturn(true);
//
//        final int[] eventArrived = {0, 0};
//
//        service.setEventCallbacks(new EventCallback() {
//
//            @Override
//            public void fireEvent(BaseEvent event, EventType type) {
//                eventArrived[0]++;
//                assertThat(type).isEqualTo(EventType.StatusUpdate);
//                assertThat(event instanceof StatusUpdateEvent).isTrue();
//                if (((StatusUpdateEvent) event).getStatus() >= CommonsConstants.SUE_UF_PROGRESS && ((StatusUpdateEvent) event).getStatus() <= (CommonsConstants.SUE_UF_PROGRESS + 100)) {
//                    assertThat(((StatusUpdateEvent) event).getStatus() >= eventArrived[1]).isTrue();
//                    eventArrived[1] = ((StatusUpdateEvent) event).getStatus();
//                }
//            }
//
//            @Override
//            public BaseControl getEventSource() {
//                return null;
//            }
//        });
//
//        File file = _context.;
//        service.doUpdate(new ByteArrayInputStream(file);
//        assertThat(eventArrived[0]).isEqualTo(21);
//        assertThat(eventArrived[1]).isEqualTo(CommonsConstants.SUE_UF_PROGRESS + 100);
//        assertThat(service.getDeviceState()).isEqualTo(CommonsConstants.S_IDLE);
//        assertThat(DLSJposConst.updateInProgress).isFalse();
//        verify(flash, times(1)).startCommand();
//        verify(flash, times(1)).release();
//        verify(flash, times(1)).sendRecord("\0\0\0\0");
//        verify(flash, times(1)).resetCommand();
//        verify(_scanner, times(1)).changeBaudRate(anyInt());
//        verify(_scanner, times(1)).setChangeBaudAfterUpdate(false);
//    }
//
//
//    @Test
//    public void doUpdateUSBCompleteNoReset() throws DLSException {
//        String fileContent = "122F0024J000S0220000FF HDFGEN2 2.39.0.0 Application Copyright 2019 by DATALOGIC INC \n" +
//                "P000S0250000FF Firmware Generator 2.39.0.0 Library Copyright 2019 by DATALOGIC INC\n" +
//                "*000S0120000FF App=610099025  Cfg=          \n" +
//                "&000S0100000FF VID=05F9 PID=122F EC=0024\n" +
//                "&000S0100000FF FileName=\"610098920.BIN\" \n" +
//                "&000S0100000FF FileName=\"610099025.BIN\" \n" +
//                ">000S01C0000FF FileName=\"..\\..\\..\\TOOL\\ALADDIN\\OPTIONENABLE.BIN\"\n" +
//                "2000S016000000000500470045001200130044004D004000431F\n" +
//                "\\000S02B0000010220202020202020202020363130303939303235202020202020202020202000000000FFFFFFFF65\n" +
//                "\u0012000S00600000466206F\n" +
//                "p000S3350000000001010102000000030166200000000000000300000001CCE0202020363130303938393230202020202020202020202030C8\n" +
//                "p000S33500000030026620000000000000344E4E00314C0E20202036313030393930323520202020202020202020203007662000000000006A\n" +
//                "P000S325000000600000000100000001202020363130303939303235202020202020202020202030B8\n" +
//                "Ð000S36500000000210000EA050000EA050000EA050000EA050000EA00000100050000EA030000EAFEFFFFEAFEFFFFEAFEFFFFEAFEFFFFEAFEFFFFEA04E04EE200402DE900E04FE101402DE9A8E09FE510009EE510E08EE513F021E31E502DE904100DE201D04DE019\n" +
//                "Ð000S3650000006002402DE930FF2FE10240BDE801D08DE01E50BDE8D2F021E374E09FE538E08EE50140BDE80EF06FE10080FDE8632900FA5F2900FA100F11EE010AC0E30400C0E30100C0E3100F01EED2F021E344109FE501D0A0E1D3F021E33C109FE501D0A0E1A3\n" +
//                "Ð000S365000000C0DFF021E334109FE501D0A0E1500F11EE0F0680E3500F01EE6FF07FF50101A0E3100AE8EE18009FE530FF2FE114C09FE53CFF2FE1000002FC68CE210068C9210068C42100BC0120000C012000000000FA210000FA0AA090E8000C82448344AAF19A\n" +
//                "Ð000S365000001200107DA4501D100F039F8AFF2090EBAE80F0013F0010F18BFFB1A43F0010318476CAF00008CAF0000103A24BF78C878C1FAD8520724BF30C830C144BF04680C60704700000023002400250026103A28BF78C1FBD8520728BF30C148BF0B607047B3\n" +
//                "Ð000S365000001801FB50AF053FA024802490AF0FEF81FBD6884210068C4210010B510BDDFF80CD0FFF7EEFF00F06CE80AF092F968C4210003B4FFF7F1FF03BC0AF0C6F870402DE90C004FE2020250E320D04DE20900002A30009FE57F1090E87F108DE80D00A0E316";
//
//        DLSScannerService service = new DLSScannerService();
//
//        DLSJposConst.updateInProgress = true; // set singleton so a shared port won't interrupt
//
//        DLSDevice device = mock(DLSDevice.class);
//        when(device.getDeviceInfo()).thenReturn(mock(DLSDeviceInfo.class));
//        service.device = device;
//
//        DLSUSBFlash flash = mock(DLSUSBFlash.class);
//        service.flash = flash;
//        service.isUsbScanner = true;
//        service.setScanner(_scanner);
//        service.options = mock(DLSProperties.class);
//        when(flash.sendRecord(anyString())).thenReturn(DLSJposConst.DL_ACK);
//        when(flash.startCommand()).thenReturn(DLSJposConst.DL_ACK);
//        when(service.options.isSendNulls()).thenReturn(true);
//        when(_scanner.isChangeBaudAfterUpdate()).thenReturn(true);
//
//        final int[] eventArrived = {0, 0};
//
//        service.setEventCallbacks(new EventCallback() {
//
//            @Override
//            public void fireEvent(BaseEvent event, EventType type) {
//                eventArrived[0]++;
//                assertThat(type).isEqualTo(EventType.StatusUpdate);
//                assertThat(event instanceof StatusUpdateEvent).isTrue();
//                if (((StatusUpdateEvent) event).getStatus() >= CommonsConstants.SUE_UF_PROGRESS && ((StatusUpdateEvent) event).getStatus() <= (CommonsConstants.SUE_UF_PROGRESS + 100)) {
//                    assertThat(((StatusUpdateEvent) event).getStatus() >= eventArrived[1]).isTrue();
//                    eventArrived[1] = ((StatusUpdateEvent) event).getStatus();
//                }
//            }
//
//            @Override
//            public BaseControl getEventSource() {
//                return null;
//            }
//        });
//
//        service.doUpdate(new ByteArrayInputStream(fileContent.getBytes(StandardCharsets.UTF_8)), "TEST.DAT");
//        assertThat(eventArrived[0]).isEqualTo(20);
//        assertThat(eventArrived[1]).isEqualTo(CommonsConstants.SUE_UF_PROGRESS + 100);
//        assertThat(service.getDeviceState()).isEqualTo(CommonsConstants.S_IDLE);
//        assertThat(DLSJposConst.updateInProgress).isFalse();
//        verify(flash, times(1)).startCommand();
//        verify(flash, times(1)).release();
//        verify(flash, times(1)).sendRecord("\0\0\0\0");
//        verify(flash, times(0)).resetCommand();
//        verify(_scanner, times(1)).changeBaudRate(anyInt());
//        verify(_scanner, times(1)).setChangeBaudAfterUpdate(false);
//    }
//
//    @Test
//    public void doUpdateUSBCompleteNoSendNull() throws DLSException {
//        String fileContent = "122F0024J000S0220000FF HDFGEN2 2.39.0.0 Application Copyright 2019 by DATALOGIC INC \n" +
//                "P000S0250000FF Firmware Generator 2.39.0.0 Library Copyright 2019 by DATALOGIC INC\n" +
//                "*000S0120000FF App=610099025  Cfg=          \n" +
//                "&000S0100000FF VID=05F9 PID=122F EC=0024\n" +
//                "&000S0100000FF FileName=\"610098920.BIN\" \n" +
//                "&000S0100000FF FileName=\"610099025.BIN\" \n" +
//                ">000S01C0000FF FileName=\"..\\..\\..\\TOOL\\ALADDIN\\OPTIONENABLE.BIN\"\n" +
//                "2000S016000000000500470045001200130044004D004000431F\n" +
//                "\\000S02B0000010220202020202020202020363130303939303235202020202020202020202000000000FFFFFFFF65\n" +
//                "\u0012000S00600000466206F\n" +
//                "p000S3350000000001010102000000030166200000000000000300000001CCE0202020363130303938393230202020202020202020202030C8\n" +
//                "p000S33500000030026620000000000000344E4E00314C0E20202036313030393930323520202020202020202020203007662000000000006A\n" +
//                "P000S325000000600000000100000001202020363130303939303235202020202020202020202030B8\n" +
//                "Ð000S36500000000210000EA050000EA050000EA050000EA050000EA00000100050000EA030000EAFEFFFFEAFEFFFFEAFEFFFFEAFEFFFFEAFEFFFFEA04E04EE200402DE900E04FE101402DE9A8E09FE510009EE510E08EE513F021E31E502DE904100DE201D04DE019\n" +
//                "Ð000S3650000006002402DE930FF2FE10240BDE801D08DE01E50BDE8D2F021E374E09FE538E08EE50140BDE80EF06FE10080FDE8632900FA5F2900FA100F11EE010AC0E30400C0E30100C0E3100F01EED2F021E344109FE501D0A0E1D3F021E33C109FE501D0A0E1A3\n" +
//                "Ð000S365000000C0DFF021E334109FE501D0A0E1500F11EE0F0680E3500F01EE6FF07FF50101A0E3100AE8EE18009FE530FF2FE114C09FE53CFF2FE1000002FC68CE210068C9210068C42100BC0120000C012000000000FA210000FA0AA090E8000C82448344AAF19A\n" +
//                "Ð000S365000001200107DA4501D100F039F8AFF2090EBAE80F0013F0010F18BFFB1A43F0010318476CAF00008CAF0000103A24BF78C878C1FAD8520724BF30C830C144BF04680C60704700000023002400250026103A28BF78C1FBD8520728BF30C148BF0B607047B3\n" +
//                "Ð000S365000001801FB50AF053FA024802490AF0FEF81FBD6884210068C4210010B510BDDFF80CD0FFF7EEFF00F06CE80AF092F968C4210003B4FFF7F1FF03BC0AF0C6F870402DE90C004FE2020250E320D04DE20900002A30009FE57F1090E87F108DE80D00A0E316";
//
//        DLSScannerService service = new DLSScannerService();
//
//        DLSJposConst.updateInProgress = true; // set singleton so a shared port won't interrupt
//
//        DLSDevice device = mock(DLSDevice.class);
//        when(device.getDeviceInfo()).thenReturn(mock(DLSDeviceInfo.class));
//        service.device = device;
//
//        DLSUSBFlash flash = mock(DLSUSBFlash.class);
//        service.flash = flash;
//        service.isUsbScanner = true;
//        service.setScanner(_scanner);
//        service.options = mock(DLSProperties.class);
//        when(flash.sendRecord(anyString())).thenReturn(DLSJposConst.DL_ACK);
//        when(flash.startCommand()).thenReturn(DLSJposConst.DL_ACK);
//        when(service.options.isSendFirmwareReset()).thenReturn(true);
//        when(_scanner.detectResetFinish()).thenReturn(true);
//        when(_scanner.isChangeBaudAfterUpdate()).thenReturn(true);
//
//        final int[] eventArrived = {0, 0};
//
//        service.setEventCallbacks(new EventCallback() {
//
//            @Override
//            public void fireEvent(BaseEvent event, EventType type) {
//                eventArrived[0]++;
//                assertThat(type).isEqualTo(EventType.StatusUpdate);
//                assertThat(event instanceof StatusUpdateEvent).isTrue();
//                if (((StatusUpdateEvent) event).getStatus() >= CommonsConstants.SUE_UF_PROGRESS && ((StatusUpdateEvent) event).getStatus() <= (CommonsConstants.SUE_UF_PROGRESS + 100)) {
//                    assertThat(((StatusUpdateEvent) event).getStatus() >= eventArrived[1]).isTrue();
//                    eventArrived[1] = ((StatusUpdateEvent) event).getStatus();
//                }
//            }
//
//            @Override
//            public BaseControl getEventSource() {
//                return null;
//            }
//        });
//
//        service.doUpdate(new ByteArrayInputStream(fileContent.getBytes(StandardCharsets.UTF_8)), "TEST.DAT");
//        assertThat(eventArrived[0]).isEqualTo(21);
//        assertThat(eventArrived[1]).isEqualTo(CommonsConstants.SUE_UF_PROGRESS + 100);
//        assertThat(service.getDeviceState()).isEqualTo(CommonsConstants.S_IDLE);
//        assertThat(DLSJposConst.updateInProgress).isFalse();
//        verify(flash, times(1)).startCommand();
//        verify(flash, times(1)).release();
//        verify(flash, times(0)).sendRecord("\0\0\0\0");
//        verify(flash, times(1)).resetCommand();
//        verify(_scanner, times(1)).changeBaudRate(anyInt());
//        verify(_scanner, times(1)).setChangeBaudAfterUpdate(false);
//    }
//
//    @Test
//    public void doUpdateUSBCompleteNoChangeBaudRate() throws DLSException {
//        String fileContent = "122F0024J000S0220000FF HDFGEN2 2.39.0.0 Application Copyright 2019 by DATALOGIC INC \n" +
//                "P000S0250000FF Firmware Generator 2.39.0.0 Library Copyright 2019 by DATALOGIC INC\n" +
//                "*000S0120000FF App=610099025  Cfg=          \n" +
//                "&000S0100000FF VID=05F9 PID=122F EC=0024\n" +
//                "&000S0100000FF FileName=\"610098920.BIN\" \n" +
//                "&000S0100000FF FileName=\"610099025.BIN\" \n" +
//                ">000S01C0000FF FileName=\"..\\..\\..\\TOOL\\ALADDIN\\OPTIONENABLE.BIN\"\n" +
//                "2000S016000000000500470045001200130044004D004000431F\n" +
//                "\\000S02B0000010220202020202020202020363130303939303235202020202020202020202000000000FFFFFFFF65\n" +
//                "\u0012000S00600000466206F\n" +
//                "p000S3350000000001010102000000030166200000000000000300000001CCE0202020363130303938393230202020202020202020202030C8\n" +
//                "p000S33500000030026620000000000000344E4E00314C0E20202036313030393930323520202020202020202020203007662000000000006A\n" +
//                "P000S325000000600000000100000001202020363130303939303235202020202020202020202030B8\n" +
//                "Ð000S36500000000210000EA050000EA050000EA050000EA050000EA00000100050000EA030000EAFEFFFFEAFEFFFFEAFEFFFFEAFEFFFFEAFEFFFFEA04E04EE200402DE900E04FE101402DE9A8E09FE510009EE510E08EE513F021E31E502DE904100DE201D04DE019\n" +
//                "Ð000S3650000006002402DE930FF2FE10240BDE801D08DE01E50BDE8D2F021E374E09FE538E08EE50140BDE80EF06FE10080FDE8632900FA5F2900FA100F11EE010AC0E30400C0E30100C0E3100F01EED2F021E344109FE501D0A0E1D3F021E33C109FE501D0A0E1A3\n" +
//                "Ð000S365000000C0DFF021E334109FE501D0A0E1500F11EE0F0680E3500F01EE6FF07FF50101A0E3100AE8EE18009FE530FF2FE114C09FE53CFF2FE1000002FC68CE210068C9210068C42100BC0120000C012000000000FA210000FA0AA090E8000C82448344AAF19A\n" +
//                "Ð000S365000001200107DA4501D100F039F8AFF2090EBAE80F0013F0010F18BFFB1A43F0010318476CAF00008CAF0000103A24BF78C878C1FAD8520724BF30C830C144BF04680C60704700000023002400250026103A28BF78C1FBD8520728BF30C148BF0B607047B3\n" +
//                "Ð000S365000001801FB50AF053FA024802490AF0FEF81FBD6884210068C4210010B510BDDFF80CD0FFF7EEFF00F06CE80AF092F968C4210003B4FFF7F1FF03BC0AF0C6F870402DE90C004FE2020250E320D04DE20900002A30009FE57F1090E87F108DE80D00A0E316";
//
//        DLSScannerService service = new DLSScannerService();
//
//        DLSJposConst.updateInProgress = true; // set singleton so a shared port won't interrupt
//
//        DLSDevice device = mock(DLSDevice.class);
//        when(device.getDeviceInfo()).thenReturn(mock(DLSDeviceInfo.class));
//        service.device = device;
//
//        DLSUSBFlash flash = mock(DLSUSBFlash.class);
//        service.flash = flash;
//        service.isUsbScanner = true;
//        service.setScanner(_scanner);
//        service.options = mock(DLSProperties.class);
//        when(flash.sendRecord(anyString())).thenReturn(DLSJposConst.DL_ACK);
//        when(flash.startCommand()).thenReturn(DLSJposConst.DL_ACK);
//        when(service.options.isSendNulls()).thenReturn(true);
//        when(service.options.isSendFirmwareReset()).thenReturn(true);
//        when(_scanner.detectResetFinish()).thenReturn(true);
//
//        final int[] eventArrived = {0, 0};
//
//        service.setEventCallbacks(new EventCallback() {
//
//            @Override
//            public void fireEvent(BaseEvent event, EventType type) {
//                eventArrived[0]++;
//                assertThat(type).isEqualTo(EventType.StatusUpdate);
//                assertThat(event instanceof StatusUpdateEvent).isTrue();
//                if (((StatusUpdateEvent) event).getStatus() >= CommonsConstants.SUE_UF_PROGRESS && ((StatusUpdateEvent) event).getStatus() <= (CommonsConstants.SUE_UF_PROGRESS + 100)) {
//                    assertThat(((StatusUpdateEvent) event).getStatus() >= eventArrived[1]).isTrue();
//                    eventArrived[1] = ((StatusUpdateEvent) event).getStatus();
//                }
//            }
//
//            @Override
//            public BaseControl getEventSource() {
//                return null;
//            }
//        });
//
//        service.doUpdate(new ByteArrayInputStream(fileContent.getBytes(StandardCharsets.UTF_8)), "TEST.DAT");
//        assertThat(eventArrived[0]).isEqualTo(21);
//        assertThat(eventArrived[1]).isEqualTo(CommonsConstants.SUE_UF_PROGRESS + 100);
//        assertThat(service.getDeviceState()).isEqualTo(CommonsConstants.S_IDLE);
//        assertThat(DLSJposConst.updateInProgress).isFalse();
//        verify(flash, times(1)).startCommand();
//        verify(flash, times(1)).release();
//        verify(flash, times(1)).sendRecord("\0\0\0\0");
//        verify(flash, times(1)).resetCommand();
//        verify(_scanner, times(0)).changeBaudRate(anyInt());
//        verify(_scanner, times(0)).setChangeBaudAfterUpdate(false);
//    }
//
//    @Test
//    public void doUpdateUSBDeviceNotRestored() throws DLSException {
//        String fileContent = "122F0024J000S0220000FF HDFGEN2 2.39.0.0 Application Copyright 2019 by DATALOGIC INC \n" +
//                "P000S0250000FF Firmware Generator 2.39.0.0 Library Copyright 2019 by DATALOGIC INC\n" +
//                "*000S0120000FF App=610099025  Cfg=          \n" +
//                "&000S0100000FF VID=05F9 PID=122F EC=0024\n" +
//                "&000S0100000FF FileName=\"610098920.BIN\" \n" +
//                "&000S0100000FF FileName=\"610099025.BIN\" \n" +
//                ">000S01C0000FF FileName=\"..\\..\\..\\TOOL\\ALADDIN\\OPTIONENABLE.BIN\"\n" +
//                "2000S016000000000500470045001200130044004D004000431F\n" +
//                "\\000S02B0000010220202020202020202020363130303939303235202020202020202020202000000000FFFFFFFF65\n" +
//                "\u0012000S00600000466206F\n" +
//                "p000S3350000000001010102000000030166200000000000000300000001CCE0202020363130303938393230202020202020202020202030C8\n" +
//                "p000S33500000030026620000000000000344E4E00314C0E20202036313030393930323520202020202020202020203007662000000000006A\n" +
//                "P000S325000000600000000100000001202020363130303939303235202020202020202020202030B8\n" +
//                "Ð000S36500000000210000EA050000EA050000EA050000EA050000EA00000100050000EA030000EAFEFFFFEAFEFFFFEAFEFFFFEAFEFFFFEAFEFFFFEA04E04EE200402DE900E04FE101402DE9A8E09FE510009EE510E08EE513F021E31E502DE904100DE201D04DE019\n" +
//                "Ð000S3650000006002402DE930FF2FE10240BDE801D08DE01E50BDE8D2F021E374E09FE538E08EE50140BDE80EF06FE10080FDE8632900FA5F2900FA100F11EE010AC0E30400C0E30100C0E3100F01EED2F021E344109FE501D0A0E1D3F021E33C109FE501D0A0E1A3\n" +
//                "Ð000S365000000C0DFF021E334109FE501D0A0E1500F11EE0F0680E3500F01EE6FF07FF50101A0E3100AE8EE18009FE530FF2FE114C09FE53CFF2FE1000002FC68CE210068C9210068C42100BC0120000C012000000000FA210000FA0AA090E8000C82448344AAF19A\n" +
//                "Ð000S365000001200107DA4501D100F039F8AFF2090EBAE80F0013F0010F18BFFB1A43F0010318476CAF00008CAF0000103A24BF78C878C1FAD8520724BF30C830C144BF04680C60704700000023002400250026103A28BF78C1FBD8520728BF30C148BF0B607047B3\n" +
//                "Ð000S365000001801FB50AF053FA024802490AF0FEF81FBD6884210068C4210010B510BDDFF80CD0FFF7EEFF00F06CE80AF092F968C4210003B4FFF7F1FF03BC0AF0C6F870402DE90C004FE2020250E320D04DE20900002A30009FE57F1090E87F108DE80D00A0E316";
//
//        DLSScannerService service = new DLSScannerService();
//
//        DLSJposConst.updateInProgress = true; // set singleton so a shared port won't interrupt
//
//        DLSDevice device = mock(DLSDevice.class);
//        when(device.getDeviceInfo()).thenReturn(mock(DLSDeviceInfo.class));
//        service.device = device;
//
//        DLSUSBFlash flash = mock(DLSUSBFlash.class);
//        service.flash = flash;
//        service.isUsbScanner = true;
//        service.setScanner(_scanner);
//        service.options = mock(DLSProperties.class);
//        when(flash.sendRecord(anyString())).thenReturn(DLSJposConst.DL_ACK);
//        when(flash.startCommand()).thenReturn(DLSJposConst.DL_ACK);
//        when(service.options.isSendNulls()).thenReturn(true);
//        when(service.options.isSendFirmwareReset()).thenReturn(true);
//        when(_scanner.detectResetFinish()).thenReturn(false);
//        when(_scanner.isChangeBaudAfterUpdate()).thenReturn(true);
//
//        final int[] eventArrived = {0, 0};
//
//        service.setEventCallbacks(new EventCallback() {
//
//            @Override
//            public void fireEvent(BaseEvent event, EventType type) {
//                eventArrived[0]++;
//                assertThat(type).isEqualTo(EventType.StatusUpdate);
//                assertThat(event instanceof StatusUpdateEvent).isTrue();
//                if (((StatusUpdateEvent) event).getStatus() == CommonsConstants.SUE_UF_COMPLETE_DEV_NOT_RESTORED) {
//                    eventArrived[1] = ((StatusUpdateEvent) event).getStatus();
//                }
//            }
//
//            @Override
//            public BaseControl getEventSource() {
//                return null;
//            }
//        });
//
//        service.doUpdate(new ByteArrayInputStream(fileContent.getBytes(StandardCharsets.UTF_8)), "TEST.DAT");
//        assertThat(eventArrived[0]).isEqualTo(21);
//        assertThat(eventArrived[1]).isEqualTo(CommonsConstants.SUE_UF_COMPLETE_DEV_NOT_RESTORED);
//        assertThat(service.getDeviceState()).isEqualTo(CommonsConstants.S_IDLE);
//        assertThat(DLSJposConst.updateInProgress).isFalse();
//        verify(flash, times(1)).startCommand();
//        verify(flash, times(1)).release();
//        verify(flash, times(1)).sendRecord("\0\0\0\0");
//        verify(flash, times(1)).resetCommand();
//        verify(_scanner, times(1)).changeBaudRate(anyInt());
//        verify(_scanner, times(1)).setChangeBaudAfterUpdate(false);
//    }
//
//    @Test
//    public void doUpdateUSBFailedDevOk() throws DLSException {
//        String fileContent = "122F0024J000S0220000FF HDFGEN2 2.39.0.0 Application Copyright 2019 by DATALOGIC INC \n" +
//                "P000S0250000FF Firmware Generator 2.39.0.0 Library Copyright 2019 by DATALOGIC INC\n" +
//                "*000S0120000FF App=610099025  Cfg=          \n" +
//                "&000S0100000FF VID=05F9 PID=122F EC=0024\n" +
//                "&000S0100000FF FileName=\"610098920.BIN\" \n" +
//                "&000S0100000FF FileName=\"610099025.BIN\" \n" +
//                ">000S01C0000FF FileName=\"..\\..\\..\\TOOL\\ALADDIN\\OPTIONENABLE.BIN\"\n" +
//                "2000S016000000000500470045001200130044004D004000431F\n" +
//                "\\000S02B0000010220202020202020202020363130303939303235202020202020202020202000000000FFFFFFFF65\n" +
//                "\u0012000S00600000466206F\n" +
//                "p000S3350000000001010102000000030166200000000000000300000001CCE0202020363130303938393230202020202020202020202030C8\n" +
//                "p000S33500000030026620000000000000344E4E00314C0E20202036313030393930323520202020202020202020203007662000000000006A\n" +
//                "P000S325000000600000000100000001202020363130303939303235202020202020202020202030B8\n" +
//                "Ð000S36500000000210000EA050000EA050000EA050000EA050000EA00000100050000EA030000EAFEFFFFEAFEFFFFEAFEFFFFEAFEFFFFEAFEFFFFEA04E04EE200402DE900E04FE101402DE9A8E09FE510009EE510E08EE513F021E31E502DE904100DE201D04DE019\n" +
//                "Ð000S3650000006002402DE930FF2FE10240BDE801D08DE01E50BDE8D2F021E374E09FE538E08EE50140BDE80EF06FE10080FDE8632900FA5F2900FA100F11EE010AC0E30400C0E30100C0E3100F01EED2F021E344109FE501D0A0E1D3F021E33C109FE501D0A0E1A3\n" +
//                "Ð000S365000000C0DFF021E334109FE501D0A0E1500F11EE0F0680E3500F01EE6FF07FF50101A0E3100AE8EE18009FE530FF2FE114C09FE53CFF2FE1000002FC68CE210068C9210068C42100BC0120000C012000000000FA210000FA0AA090E8000C82448344AAF19A\n" +
//                "Ð000S365000001200107DA4501D100F039F8AFF2090EBAE80F0013F0010F18BFFB1A43F0010318476CAF00008CAF0000103A24BF78C878C1FAD8520724BF30C830C144BF04680C60704700000023002400250026103A28BF78C1FBD8520728BF30C148BF0B607047B3\n" +
//                "Ð000S365000001801FB50AF053FA024802490AF0FEF81FBD6884210068C4210010B510BDDFF80CD0FFF7EEFF00F06CE80AF092F968C4210003B4FFF7F1FF03BC0AF0C6F870402DE90C004FE2020250E320D04DE20900002A30009FE57F1090E87F108DE80D00A0E316";
//
//        DLSScannerService service = new DLSScannerService();
//
//        DLSJposConst.updateInProgress = true; // set singleton so a shared port won't interrupt
//
//        DLSDevice device = mock(DLSDevice.class);
//        when(device.getDeviceInfo()).thenReturn(mock(DLSDeviceInfo.class));
//        service.device = device;
//
//        DLSUSBFlash flash = mock(DLSUSBFlash.class);
//        service.flash = flash;
//        service.isUsbScanner = true;
//        service.setScanner(_scanner);
//        service.options = mock(DLSProperties.class);
//        when(flash.sendRecord(anyString())).thenReturn(DLSJposConst.DL_INVALID);
//        when(flash.startCommand()).thenReturn(DLSJposConst.DL_ACK);
//        when(service.options.isSendNulls()).thenReturn(true);
//        when(service.options.isSendFirmwareReset()).thenReturn(true);
//        when(_scanner.detectResetFinish()).thenReturn(true);
//        when(_scanner.isChangeBaudAfterUpdate()).thenReturn(true);
//
//        final int[] eventArrived = {0, 0};
//
//        service.setEventCallbacks(new EventCallback() {
//
//            @Override
//            public void fireEvent(BaseEvent event, EventType type) {
//                eventArrived[0]++;
//                assertThat(type).isEqualTo(EventType.StatusUpdate);
//                assertThat(event instanceof StatusUpdateEvent).isTrue();
//                eventArrived[1] = ((StatusUpdateEvent) event).getStatus();
//            }
//
//            @Override
//            public BaseControl getEventSource() {
//                return null;
//            }
//        });
//
//        service.doUpdate(new ByteArrayInputStream(fileContent.getBytes(StandardCharsets.UTF_8)), "TEST.DAT");
//        assertThat(eventArrived[0]).isEqualTo(3);
//        assertThat(eventArrived[1]).isEqualTo(CommonsConstants.SUE_UF_FAILED_DEV_OK);
//        assertThat(service.getDeviceState()).isEqualTo(CommonsConstants.S_IDLE);
//        assertThat(DLSJposConst.updateInProgress).isFalse();
//        verify(flash, times(1)).startCommand();
//        verify(flash, times(1)).release();
//        verify(flash, times(1)).sendRecord("\0\0\0\0");
//        verify(flash, times(1)).resetCommand();
//        verify(_scanner, times(1)).changeBaudRate(anyInt());
//        verify(_scanner, times(1)).setChangeBaudAfterUpdate(false);
//    }
//
//    @Test
//    public void doUpdateUSBFailedDevNeedsFirmware() throws DLSException {
//        String fileContent = "122F0024J000S0220000FF HDFGEN2 2.39.0.0 Application Copyright 2019 by DATALOGIC INC \n" +
//                "P000S0250000FF Firmware Generator 2.39.0.0 Library Copyright 2019 by DATALOGIC INC\n" +
//                "*000S0120000FF App=610099025  Cfg=          \n" +
//                "&000S0100000FF VID=05F9 PID=122F EC=0024\n" +
//                "&000S0100000FF FileName=\"610098920.BIN\" \n" +
//                "&000S0100000FF FileName=\"610099025.BIN\" \n" +
//                ">000S01C0000FF FileName=\"..\\..\\..\\TOOL\\ALADDIN\\OPTIONENABLE.BIN\"\n" +
//                "2000S016000000000500470045001200130044004D004000431F\n" +
//                "\\000S02B0000010220202020202020202020363130303939303235202020202020202020202000000000FFFFFFFF65\n" +
//                "\u0012000S00600000466206F\n" +
//                "p000S3350000000001010102000000030166200000000000000300000001CCE0202020363130303938393230202020202020202020202030C8\n" +
//                "p000S33500000030026620000000000000344E4E00314C0E20202036313030393930323520202020202020202020203007662000000000006A\n" +
//                "P000S325000000600000000100000001202020363130303939303235202020202020202020202030B8\n" +
//                "Ð000S36500000000210000EA050000EA050000EA050000EA050000EA00000100050000EA030000EAFEFFFFEAFEFFFFEAFEFFFFEAFEFFFFEAFEFFFFEA04E04EE200402DE900E04FE101402DE9A8E09FE510009EE510E08EE513F021E31E502DE904100DE201D04DE019\n" +
//                "Ð000S3650000006002402DE930FF2FE10240BDE801D08DE01E50BDE8D2F021E374E09FE538E08EE50140BDE80EF06FE10080FDE8632900FA5F2900FA100F11EE010AC0E30400C0E30100C0E3100F01EED2F021E344109FE501D0A0E1D3F021E33C109FE501D0A0E1A3\n" +
//                "Ð000S365000000C0DFF021E334109FE501D0A0E1500F11EE0F0680E3500F01EE6FF07FF50101A0E3100AE8EE18009FE530FF2FE114C09FE53CFF2FE1000002FC68CE210068C9210068C42100BC0120000C012000000000FA210000FA0AA090E8000C82448344AAF19A\n" +
//                "Ð000S365000001200107DA4501D100F039F8AFF2090EBAE80F0013F0010F18BFFB1A43F0010318476CAF00008CAF0000103A24BF78C878C1FAD8520724BF30C830C144BF04680C60704700000023002400250026103A28BF78C1FBD8520728BF30C148BF0B607047B3\n" +
//                "Ð000S365000001801FB50AF053FA024802490AF0FEF81FBD6884210068C4210010B510BDDFF80CD0FFF7EEFF00F06CE80AF092F968C4210003B4FFF7F1FF03BC0AF0C6F870402DE90C004FE2020250E320D04DE20900002A30009FE57F1090E87F108DE80D00A0E316";
//
//        DLSScannerService service = new DLSScannerService();
//
//        DLSJposConst.updateInProgress = true; // set singleton so a shared port won't interrupt
//
//        DLSDevice device = mock(DLSDevice.class);
//        when(device.getDeviceInfo()).thenReturn(mock(DLSDeviceInfo.class));
//        service.device = device;
//
//        DLSUSBFlash flash = mock(DLSUSBFlash.class);
//        service.flash = flash;
//        service.isUsbScanner = true;
//        service.setScanner(_scanner);
//        service.options = mock(DLSProperties.class);
//        when(flash.sendRecord(anyString())).thenReturn(DLSJposConst.DL_CAN);
//        when(flash.startCommand()).thenReturn(DLSJposConst.DL_ACK);
//        when(service.options.isSendNulls()).thenReturn(true);
//        when(service.options.isSendFirmwareReset()).thenReturn(true);
//        when(_scanner.detectResetFinish()).thenReturn(true);
//        when(_scanner.isChangeBaudAfterUpdate()).thenReturn(true);
//
//        final int[] eventArrived = {0, 0};
//
//        service.setEventCallbacks(new EventCallback() {
//
//            @Override
//            public void fireEvent(BaseEvent event, EventType type) {
//                eventArrived[0]++;
//                assertThat(type).isEqualTo(EventType.StatusUpdate);
//                assertThat(event instanceof StatusUpdateEvent).isTrue();
//                eventArrived[1] = ((StatusUpdateEvent) event).getStatus();
//            }
//
//            @Override
//            public BaseControl getEventSource() {
//                return null;
//            }
//        });
//
//        service.doUpdate(new ByteArrayInputStream(fileContent.getBytes(StandardCharsets.UTF_8)), "TEST.DAT");
//        assertThat(eventArrived[0]).isEqualTo(3);
//        assertThat(eventArrived[1]).isEqualTo(CommonsConstants.SUE_UF_FAILED_DEV_NEEDS_FIRMWARE);
//        assertThat(service.getDeviceState()).isEqualTo(CommonsConstants.S_IDLE);
//        assertThat(DLSJposConst.updateInProgress).isFalse();
//        verify(flash, times(1)).startCommand();
//        verify(flash, times(1)).release();
//        verify(flash, times(1)).sendRecord("\0\0\0\0");
//        verify(flash, times(1)).resetCommand();
//        verify(_scanner, times(1)).changeBaudRate(anyInt());
//        verify(_scanner, times(1)).setChangeBaudAfterUpdate(false);
//    }
//
//    @Test
//    public void doUpdateSerialComplete() throws DLSException {
//        String fileContent = "122F0024J000S0220000FF HDFGEN2 2.39.0.0 Application Copyright 2019 by DATALOGIC INC \n" +
//                "P000S0250000FF Firmware Generator 2.39.0.0 Library Copyright 2019 by DATALOGIC INC\n" +
//                "*000S0120000FF App=610099025  Cfg=          \n" +
//                "&000S0100000FF VID=05F9 PID=122F EC=0024\n" +
//                "&000S0100000FF FileName=\"610098920.BIN\" \n" +
//                "&000S0100000FF FileName=\"610099025.BIN\" \n" +
//                ">000S01C0000FF FileName=\"..\\..\\..\\TOOL\\ALADDIN\\OPTIONENABLE.BIN\"\n" +
//                "2000S016000000000500470045001200130044004D004000431F\n" +
//                "\\000S02B0000010220202020202020202020363130303939303235202020202020202020202000000000FFFFFFFF65\n" +
//                "\u0012000S00600000466206F\n" +
//                "p000S3350000000001010102000000030166200000000000000300000001CCE0202020363130303938393230202020202020202020202030C8\n" +
//                "p000S33500000030026620000000000000344E4E00314C0E20202036313030393930323520202020202020202020203007662000000000006A\n" +
//                "P000S325000000600000000100000001202020363130303939303235202020202020202020202030B8\n" +
//                "Ð000S36500000000210000EA050000EA050000EA050000EA050000EA00000100050000EA030000EAFEFFFFEAFEFFFFEAFEFFFFEAFEFFFFEAFEFFFFEA04E04EE200402DE900E04FE101402DE9A8E09FE510009EE510E08EE513F021E31E502DE904100DE201D04DE019\n" +
//                "Ð000S3650000006002402DE930FF2FE10240BDE801D08DE01E50BDE8D2F021E374E09FE538E08EE50140BDE80EF06FE10080FDE8632900FA5F2900FA100F11EE010AC0E30400C0E30100C0E3100F01EED2F021E344109FE501D0A0E1D3F021E33C109FE501D0A0E1A3\n" +
//                "Ð000S365000000C0DFF021E334109FE501D0A0E1500F11EE0F0680E3500F01EE6FF07FF50101A0E3100AE8EE18009FE530FF2FE114C09FE53CFF2FE1000002FC68CE210068C9210068C42100BC0120000C012000000000FA210000FA0AA090E8000C82448344AAF19A\n" +
//                "Ð000S365000001200107DA4501D100F039F8AFF2090EBAE80F0013F0010F18BFFB1A43F0010318476CAF00008CAF0000103A24BF78C878C1FAD8520724BF30C830C144BF04680C60704700000023002400250026103A28BF78C1FBD8520728BF30C148BF0B607047B3\n" +
//                "Ð000S365000001801FB50AF053FA024802490AF0FEF81FBD6884210068C4210010B510BDDFF80CD0FFF7EEFF00F06CE80AF092F968C4210003B4FFF7F1FF03BC0AF0C6F870402DE90C004FE2020250E320D04DE20900002A30009FE57F1090E87F108DE80D00A0E316";
//
//        DLSScannerService service = new DLSScannerService();
//
//        DLSJposConst.updateInProgress = true; // set singleton so a shared port won't interrupt
//
//        DLSDevice device = mock(DLSDevice.class);
//        when(device.getDeviceInfo()).thenReturn(mock(DLSDeviceInfo.class));
//        service.device = device;
//
//        service.setScanner(_scanner);
//        service.options = mock(DLSProperties.class);
//        when(_scanner.sendRecord(anyString())).thenReturn(DLSJposConst.DL_ACK);
//        when(service.options.isSendNulls()).thenReturn(true);
//        when(service.options.isSendFirmwareReset()).thenReturn(true);
//        when(_scanner.detectResetFinish()).thenReturn(true);
//        when(_scanner.isChangeBaudAfterUpdate()).thenReturn(true);
//
//        final int[] eventArrived = {0, 0};
//
//        service.setEventCallbacks(new EventCallback() {
//
//            @Override
//            public void fireEvent(BaseEvent event, EventType type) {
//                eventArrived[0]++;
//                assertThat(type).isEqualTo(EventType.StatusUpdate);
//                assertThat(event instanceof StatusUpdateEvent).isTrue();
//                if (((StatusUpdateEvent) event).getStatus() >= CommonsConstants.SUE_UF_PROGRESS && ((StatusUpdateEvent) event).getStatus() <= (CommonsConstants.SUE_UF_PROGRESS + 100)) {
//                    assertThat(((StatusUpdateEvent) event).getStatus() >= eventArrived[1]).isTrue();
//                    eventArrived[1] = ((StatusUpdateEvent) event).getStatus();
//                }
//            }
//
//            @Override
//            public BaseControl getEventSource() {
//                return null;
//            }
//        });
//
//        service.doUpdate(new ByteArrayInputStream(fileContent.getBytes(StandardCharsets.UTF_8)), "TEST.DAT");
//        assertThat(eventArrived[0]).isEqualTo(21);
//        assertThat(eventArrived[1]).isEqualTo(CommonsConstants.SUE_UF_PROGRESS + 100);
//        assertThat(service.getDeviceState()).isEqualTo(CommonsConstants.S_IDLE);
//        assertThat(DLSJposConst.updateInProgress).isFalse();
//        verify(_scanner, times(0)).sendRecord("\0\0\0\0");
//        verify(_scanner, times(1)).reset();
//        verify(_scanner, times(1)).changeBaudRate(anyInt());
//        verify(_scanner, times(1)).setChangeBaudAfterUpdate(false);
//    }
//
//    @Test
//    public void doUpdateSerialCompleteNoSendNull() throws DLSException {
//        String fileContent = "122F0024J000S0220000FF HDFGEN2 2.39.0.0 Application Copyright 2019 by DATALOGIC INC \n" +
//                "P000S0250000FF Firmware Generator 2.39.0.0 Library Copyright 2019 by DATALOGIC INC\n" +
//                "*000S0120000FF App=610099025  Cfg=          \n" +
//                "&000S0100000FF VID=05F9 PID=122F EC=0024\n" +
//                "&000S0100000FF FileName=\"610098920.BIN\" \n" +
//                "&000S0100000FF FileName=\"610099025.BIN\" \n" +
//                ">000S01C0000FF FileName=\"..\\..\\..\\TOOL\\ALADDIN\\OPTIONENABLE.BIN\"\n" +
//                "2000S016000000000500470045001200130044004D004000431F\n" +
//                "\\000S02B0000010220202020202020202020363130303939303235202020202020202020202000000000FFFFFFFF65\n" +
//                "\u0012000S00600000466206F\n" +
//                "p000S3350000000001010102000000030166200000000000000300000001CCE0202020363130303938393230202020202020202020202030C8\n" +
//                "p000S33500000030026620000000000000344E4E00314C0E20202036313030393930323520202020202020202020203007662000000000006A\n" +
//                "P000S325000000600000000100000001202020363130303939303235202020202020202020202030B8\n" +
//                "Ð000S36500000000210000EA050000EA050000EA050000EA050000EA00000100050000EA030000EAFEFFFFEAFEFFFFEAFEFFFFEAFEFFFFEAFEFFFFEA04E04EE200402DE900E04FE101402DE9A8E09FE510009EE510E08EE513F021E31E502DE904100DE201D04DE019\n" +
//                "Ð000S3650000006002402DE930FF2FE10240BDE801D08DE01E50BDE8D2F021E374E09FE538E08EE50140BDE80EF06FE10080FDE8632900FA5F2900FA100F11EE010AC0E30400C0E30100C0E3100F01EED2F021E344109FE501D0A0E1D3F021E33C109FE501D0A0E1A3\n" +
//                "Ð000S365000000C0DFF021E334109FE501D0A0E1500F11EE0F0680E3500F01EE6FF07FF50101A0E3100AE8EE18009FE530FF2FE114C09FE53CFF2FE1000002FC68CE210068C9210068C42100BC0120000C012000000000FA210000FA0AA090E8000C82448344AAF19A\n" +
//                "Ð000S365000001200107DA4501D100F039F8AFF2090EBAE80F0013F0010F18BFFB1A43F0010318476CAF00008CAF0000103A24BF78C878C1FAD8520724BF30C830C144BF04680C60704700000023002400250026103A28BF78C1FBD8520728BF30C148BF0B607047B3\n" +
//                "Ð000S365000001801FB50AF053FA024802490AF0FEF81FBD6884210068C4210010B510BDDFF80CD0FFF7EEFF00F06CE80AF092F968C4210003B4FFF7F1FF03BC0AF0C6F870402DE90C004FE2020250E320D04DE20900002A30009FE57F1090E87F108DE80D00A0E316";
//
//        DLSScannerService service = new DLSScannerService();
//
//        DLSJposConst.updateInProgress = true; // set singleton so a shared port won't interrupt
//
//        DLSDevice device = mock(DLSDevice.class);
//        when(device.getDeviceInfo()).thenReturn(mock(DLSDeviceInfo.class));
//        service.device = device;
//
//        service.setScanner(_scanner);
//        service.options = mock(DLSProperties.class);
//        when(_scanner.sendRecord(anyString())).thenReturn(DLSJposConst.DL_ACK);
//        when(service.options.isSendFirmwareReset()).thenReturn(true);
//        when(_scanner.detectResetFinish()).thenReturn(true);
//        when(_scanner.isChangeBaudAfterUpdate()).thenReturn(true);
//
//        final int[] eventArrived = {0, 0};
//
//        service.setEventCallbacks(new EventCallback() {
//
//            @Override
//            public void fireEvent(BaseEvent event, EventType type) {
//                eventArrived[0]++;
//                assertThat(type).isEqualTo(EventType.StatusUpdate);
//                assertThat(event instanceof StatusUpdateEvent).isTrue();
//                if (((StatusUpdateEvent) event).getStatus() >= CommonsConstants.SUE_UF_PROGRESS && ((StatusUpdateEvent) event).getStatus() <= (CommonsConstants.SUE_UF_PROGRESS + 100)) {
//                    assertThat(((StatusUpdateEvent) event).getStatus() >= eventArrived[1]).isTrue();
//                    eventArrived[1] = ((StatusUpdateEvent) event).getStatus();
//                }
//            }
//
//            @Override
//            public BaseControl getEventSource() {
//                return null;
//            }
//        });
//
//        service.doUpdate(new ByteArrayInputStream(fileContent.getBytes(StandardCharsets.UTF_8)), "TEST.DAT");
//        assertThat(eventArrived[0]).isEqualTo(21);
//        assertThat(eventArrived[1]).isEqualTo(CommonsConstants.SUE_UF_PROGRESS + 100);
//        assertThat(service.getDeviceState()).isEqualTo(CommonsConstants.S_IDLE);
//        assertThat(DLSJposConst.updateInProgress).isFalse();
//        verify(_scanner, times(0)).sendRecord("\0\0\0\0");
//        verify(_scanner, times(1)).reset();
//        verify(_scanner, times(1)).changeBaudRate(anyInt());
//        verify(_scanner, times(1)).setChangeBaudAfterUpdate(false);
//    }
//
//    @Test
//    public void doUpdateSerialCompleteNoChangeBaudRate() throws DLSException {
//        String fileContent = "122F0024J000S0220000FF HDFGEN2 2.39.0.0 Application Copyright 2019 by DATALOGIC INC \n" +
//                "P000S0250000FF Firmware Generator 2.39.0.0 Library Copyright 2019 by DATALOGIC INC\n" +
//                "*000S0120000FF App=610099025  Cfg=          \n" +
//                "&000S0100000FF VID=05F9 PID=122F EC=0024\n" +
//                "&000S0100000FF FileName=\"610098920.BIN\" \n" +
//                "&000S0100000FF FileName=\"610099025.BIN\" \n" +
//                ">000S01C0000FF FileName=\"..\\..\\..\\TOOL\\ALADDIN\\OPTIONENABLE.BIN\"\n" +
//                "2000S016000000000500470045001200130044004D004000431F\n" +
//                "\\000S02B0000010220202020202020202020363130303939303235202020202020202020202000000000FFFFFFFF65\n" +
//                "\u0012000S00600000466206F\n" +
//                "p000S3350000000001010102000000030166200000000000000300000001CCE0202020363130303938393230202020202020202020202030C8\n" +
//                "p000S33500000030026620000000000000344E4E00314C0E20202036313030393930323520202020202020202020203007662000000000006A\n" +
//                "P000S325000000600000000100000001202020363130303939303235202020202020202020202030B8\n" +
//                "Ð000S36500000000210000EA050000EA050000EA050000EA050000EA00000100050000EA030000EAFEFFFFEAFEFFFFEAFEFFFFEAFEFFFFEAFEFFFFEA04E04EE200402DE900E04FE101402DE9A8E09FE510009EE510E08EE513F021E31E502DE904100DE201D04DE019\n" +
//                "Ð000S3650000006002402DE930FF2FE10240BDE801D08DE01E50BDE8D2F021E374E09FE538E08EE50140BDE80EF06FE10080FDE8632900FA5F2900FA100F11EE010AC0E30400C0E30100C0E3100F01EED2F021E344109FE501D0A0E1D3F021E33C109FE501D0A0E1A3\n" +
//                "Ð000S365000000C0DFF021E334109FE501D0A0E1500F11EE0F0680E3500F01EE6FF07FF50101A0E3100AE8EE18009FE530FF2FE114C09FE53CFF2FE1000002FC68CE210068C9210068C42100BC0120000C012000000000FA210000FA0AA090E8000C82448344AAF19A\n" +
//                "Ð000S365000001200107DA4501D100F039F8AFF2090EBAE80F0013F0010F18BFFB1A43F0010318476CAF00008CAF0000103A24BF78C878C1FAD8520724BF30C830C144BF04680C60704700000023002400250026103A28BF78C1FBD8520728BF30C148BF0B607047B3\n" +
//                "Ð000S365000001801FB50AF053FA024802490AF0FEF81FBD6884210068C4210010B510BDDFF80CD0FFF7EEFF00F06CE80AF092F968C4210003B4FFF7F1FF03BC0AF0C6F870402DE90C004FE2020250E320D04DE20900002A30009FE57F1090E87F108DE80D00A0E316";
//
//        DLSScannerService service = new DLSScannerService();
//
//        DLSJposConst.updateInProgress = true; // set singleton so a shared port won't interrupt
//
//        DLSDevice device = mock(DLSDevice.class);
//        when(device.getDeviceInfo()).thenReturn(mock(DLSDeviceInfo.class));
//        service.device = device;
//
//        service.setScanner(_scanner);
//        service.options = mock(DLSProperties.class);
//        when(_scanner.sendRecord(anyString())).thenReturn(DLSJposConst.DL_ACK);
//        when(service.options.isSendNulls()).thenReturn(true);
//        when(service.options.isSendFirmwareReset()).thenReturn(true);
//        when(_scanner.detectResetFinish()).thenReturn(true);
//
//        final int[] eventArrived = {0, 0};
//
//        service.setEventCallbacks(new EventCallback() {
//
//            @Override
//            public void fireEvent(BaseEvent event, EventType type) {
//                eventArrived[0]++;
//                assertThat(type).isEqualTo(EventType.StatusUpdate);
//                assertThat(event instanceof StatusUpdateEvent).isTrue();
//                if (((StatusUpdateEvent) event).getStatus() >= CommonsConstants.SUE_UF_PROGRESS && ((StatusUpdateEvent) event).getStatus() <= (CommonsConstants.SUE_UF_PROGRESS + 100)) {
//                    assertThat(((StatusUpdateEvent) event).getStatus() >= eventArrived[1]).isTrue();
//                    eventArrived[1] = ((StatusUpdateEvent) event).getStatus();
//                }
//            }
//
//            @Override
//            public BaseControl getEventSource() {
//                return null;
//            }
//        });
//
//        service.doUpdate(new ByteArrayInputStream(fileContent.getBytes(StandardCharsets.UTF_8)), "TEST.DAT");
//        assertThat(eventArrived[0]).isEqualTo(21);
//        assertThat(eventArrived[1]).isEqualTo(CommonsConstants.SUE_UF_PROGRESS + 100);
//        assertThat(service.getDeviceState()).isEqualTo(CommonsConstants.S_IDLE);
//        assertThat(DLSJposConst.updateInProgress).isFalse();
//        verify(_scanner, times(0)).sendRecord("\0\0\0\0");
//        verify(_scanner, times(1)).reset();
//        verify(_scanner, times(0)).changeBaudRate(anyInt());
//        verify(_scanner, times(0)).setChangeBaudAfterUpdate(false);
//    }
//
//    @Test
//    public void doUpdateSerialDeviceNotRestored() throws DLSException {
//        String fileContent = "122F0024J000S0220000FF HDFGEN2 2.39.0.0 Application Copyright 2019 by DATALOGIC INC \n" +
//                "P000S0250000FF Firmware Generator 2.39.0.0 Library Copyright 2019 by DATALOGIC INC\n" +
//                "*000S0120000FF App=610099025  Cfg=          \n" +
//                "&000S0100000FF VID=05F9 PID=122F EC=0024\n" +
//                "&000S0100000FF FileName=\"610098920.BIN\" \n" +
//                "&000S0100000FF FileName=\"610099025.BIN\" \n" +
//                ">000S01C0000FF FileName=\"..\\..\\..\\TOOL\\ALADDIN\\OPTIONENABLE.BIN\"\n" +
//                "2000S016000000000500470045001200130044004D004000431F\n" +
//                "\\000S02B0000010220202020202020202020363130303939303235202020202020202020202000000000FFFFFFFF65\n" +
//                "\u0012000S00600000466206F\n" +
//                "p000S3350000000001010102000000030166200000000000000300000001CCE0202020363130303938393230202020202020202020202030C8\n" +
//                "p000S33500000030026620000000000000344E4E00314C0E20202036313030393930323520202020202020202020203007662000000000006A\n" +
//                "P000S325000000600000000100000001202020363130303939303235202020202020202020202030B8\n" +
//                "Ð000S36500000000210000EA050000EA050000EA050000EA050000EA00000100050000EA030000EAFEFFFFEAFEFFFFEAFEFFFFEAFEFFFFEAFEFFFFEA04E04EE200402DE900E04FE101402DE9A8E09FE510009EE510E08EE513F021E31E502DE904100DE201D04DE019\n" +
//                "Ð000S3650000006002402DE930FF2FE10240BDE801D08DE01E50BDE8D2F021E374E09FE538E08EE50140BDE80EF06FE10080FDE8632900FA5F2900FA100F11EE010AC0E30400C0E30100C0E3100F01EED2F021E344109FE501D0A0E1D3F021E33C109FE501D0A0E1A3\n" +
//                "Ð000S365000000C0DFF021E334109FE501D0A0E1500F11EE0F0680E3500F01EE6FF07FF50101A0E3100AE8EE18009FE530FF2FE114C09FE53CFF2FE1000002FC68CE210068C9210068C42100BC0120000C012000000000FA210000FA0AA090E8000C82448344AAF19A\n" +
//                "Ð000S365000001200107DA4501D100F039F8AFF2090EBAE80F0013F0010F18BFFB1A43F0010318476CAF00008CAF0000103A24BF78C878C1FAD8520724BF30C830C144BF04680C60704700000023002400250026103A28BF78C1FBD8520728BF30C148BF0B607047B3\n" +
//                "Ð000S365000001801FB50AF053FA024802490AF0FEF81FBD6884210068C4210010B510BDDFF80CD0FFF7EEFF00F06CE80AF092F968C4210003B4FFF7F1FF03BC0AF0C6F870402DE90C004FE2020250E320D04DE20900002A30009FE57F1090E87F108DE80D00A0E316";
//
//        DLSScannerService service = new DLSScannerService();
//
//        DLSJposConst.updateInProgress = true; // set singleton so a shared port won't interrupt
//
//        DLSDevice device = mock(DLSDevice.class);
//        when(device.getDeviceInfo()).thenReturn(mock(DLSDeviceInfo.class));
//        service.device = device;
//
//        service.setScanner(_scanner);
//        service.options = mock(DLSProperties.class);
//        when(_scanner.sendRecord(anyString())).thenReturn(DLSJposConst.DL_ACK);
//        when(service.options.isSendNulls()).thenReturn(true);
//        when(service.options.isSendFirmwareReset()).thenReturn(true);
//        when(_scanner.detectResetFinish()).thenReturn(false);
//        when(_scanner.isChangeBaudAfterUpdate()).thenReturn(true);
//
//        final int[] eventArrived = {0, 0};
//
//        service.setEventCallbacks(new EventCallback() {
//
//            @Override
//            public void fireEvent(BaseEvent event, EventType type) {
//                eventArrived[0]++;
//                assertThat(type).isEqualTo(EventType.StatusUpdate);
//                assertThat(event instanceof StatusUpdateEvent).isTrue();
//                if (((StatusUpdateEvent) event).getStatus() == CommonsConstants.SUE_UF_COMPLETE_DEV_NOT_RESTORED) {
//                    eventArrived[1] = ((StatusUpdateEvent) event).getStatus();
//                }
//            }
//
//            @Override
//            public BaseControl getEventSource() {
//                return null;
//            }
//        });
//
//        service.doUpdate(new ByteArrayInputStream(fileContent.getBytes(StandardCharsets.UTF_8)), "TEST.DAT");
//        assertThat(eventArrived[0]).isEqualTo(21);
//        assertThat(eventArrived[1]).isEqualTo(CommonsConstants.SUE_UF_COMPLETE_DEV_NOT_RESTORED);
//        assertThat(service.getDeviceState()).isEqualTo(CommonsConstants.S_IDLE);
//        assertThat(DLSJposConst.updateInProgress).isFalse();
//
//        verify(_scanner, times(0)).sendRecord("\0\0\0\0");
//        verify(_scanner, times(1)).reset();
//        verify(_scanner, times(1)).changeBaudRate(anyInt());
//        verify(_scanner, times(1)).setChangeBaudAfterUpdate(false);
//    }
//
//    @Test
//    public void doUpdateSerialFailedDevOk() throws DLSException {
//        String fileContent = "122F0024J000S0220000FF HDFGEN2 2.39.0.0 Application Copyright 2019 by DATALOGIC INC \n" +
//                "P000S0250000FF Firmware Generator 2.39.0.0 Library Copyright 2019 by DATALOGIC INC\n" +
//                "*000S0120000FF App=610099025  Cfg=          \n" +
//                "&000S0100000FF VID=05F9 PID=122F EC=0024\n" +
//                "&000S0100000FF FileName=\"610098920.BIN\" \n" +
//                "&000S0100000FF FileName=\"610099025.BIN\" \n" +
//                ">000S01C0000FF FileName=\"..\\..\\..\\TOOL\\ALADDIN\\OPTIONENABLE.BIN\"\n" +
//                "2000S016000000000500470045001200130044004D004000431F\n" +
//                "\\000S02B0000010220202020202020202020363130303939303235202020202020202020202000000000FFFFFFFF65\n" +
//                "\u0012000S00600000466206F\n" +
//                "p000S3350000000001010102000000030166200000000000000300000001CCE0202020363130303938393230202020202020202020202030C8\n" +
//                "p000S33500000030026620000000000000344E4E00314C0E20202036313030393930323520202020202020202020203007662000000000006A\n" +
//                "P000S325000000600000000100000001202020363130303939303235202020202020202020202030B8\n" +
//                "Ð000S36500000000210000EA050000EA050000EA050000EA050000EA00000100050000EA030000EAFEFFFFEAFEFFFFEAFEFFFFEAFEFFFFEAFEFFFFEA04E04EE200402DE900E04FE101402DE9A8E09FE510009EE510E08EE513F021E31E502DE904100DE201D04DE019\n" +
//                "Ð000S3650000006002402DE930FF2FE10240BDE801D08DE01E50BDE8D2F021E374E09FE538E08EE50140BDE80EF06FE10080FDE8632900FA5F2900FA100F11EE010AC0E30400C0E30100C0E3100F01EED2F021E344109FE501D0A0E1D3F021E33C109FE501D0A0E1A3\n" +
//                "Ð000S365000000C0DFF021E334109FE501D0A0E1500F11EE0F0680E3500F01EE6FF07FF50101A0E3100AE8EE18009FE530FF2FE114C09FE53CFF2FE1000002FC68CE210068C9210068C42100BC0120000C012000000000FA210000FA0AA090E8000C82448344AAF19A\n" +
//                "Ð000S365000001200107DA4501D100F039F8AFF2090EBAE80F0013F0010F18BFFB1A43F0010318476CAF00008CAF0000103A24BF78C878C1FAD8520724BF30C830C144BF04680C60704700000023002400250026103A28BF78C1FBD8520728BF30C148BF0B607047B3\n" +
//                "Ð000S365000001801FB50AF053FA024802490AF0FEF81FBD6884210068C4210010B510BDDFF80CD0FFF7EEFF00F06CE80AF092F968C4210003B4FFF7F1FF03BC0AF0C6F870402DE90C004FE2020250E320D04DE20900002A30009FE57F1090E87F108DE80D00A0E316";
//
//        DLSScannerService service = new DLSScannerService();
//
//        DLSJposConst.updateInProgress = true; // set singleton so a shared port won't interrupt
//
//        DLSDevice device = mock(DLSDevice.class);
//        when(device.getDeviceInfo()).thenReturn(mock(DLSDeviceInfo.class));
//        service.device = device;
//
//        service.setScanner(_scanner);
//        service.options = mock(DLSProperties.class);
//        when(_scanner.sendRecord(anyString())).thenReturn(DLSJposConst.DL_INVALID);
//        when(service.options.isSendNulls()).thenReturn(true);
//        when(service.options.isSendFirmwareReset()).thenReturn(true);
//        when(_scanner.detectResetFinish()).thenReturn(true);
//        when(_scanner.isChangeBaudAfterUpdate()).thenReturn(true);
//
//        final int[] eventArrived = {0, 0};
//
//        service.setEventCallbacks(new EventCallback() {
//
//            @Override
//            public void fireEvent(BaseEvent event, EventType type) {
//                eventArrived[0]++;
//                assertThat(type).isEqualTo(EventType.StatusUpdate);
//                assertThat(event instanceof StatusUpdateEvent).isTrue();
//                eventArrived[1] = ((StatusUpdateEvent) event).getStatus();
//            }
//
//            @Override
//            public BaseControl getEventSource() {
//                return null;
//            }
//        });
//
//        service.doUpdate(new ByteArrayInputStream(fileContent.getBytes(StandardCharsets.UTF_8)), "TEST.DAT");
//        assertThat(eventArrived[0]).isEqualTo(3);
//        assertThat(eventArrived[1]).isEqualTo(CommonsConstants.SUE_UF_FAILED_DEV_OK);
//        assertThat(service.getDeviceState()).isEqualTo(CommonsConstants.S_IDLE);
//        assertThat(DLSJposConst.updateInProgress).isFalse();
//        verify(_scanner, times(0)).sendRecord("\0\0\0\0");
//        verify(_scanner, times(1)).reset();
//        verify(_scanner, times(1)).changeBaudRate(anyInt());
//        verify(_scanner, times(1)).setChangeBaudAfterUpdate(false);
//    }
//
//    @Test
//    public void doUpdateSerialFailedDevNeedsFirmware() throws DLSException {
//        String fileContent = "122F0024J000S0220000FF HDFGEN2 2.39.0.0 Application Copyright 2019 by DATALOGIC INC \n" +
//                "P000S0250000FF Firmware Generator 2.39.0.0 Library Copyright 2019 by DATALOGIC INC\n" +
//                "*000S0120000FF App=610099025  Cfg=          \n" +
//                "&000S0100000FF VID=05F9 PID=122F EC=0024\n" +
//                "&000S0100000FF FileName=\"610098920.BIN\" \n" +
//                "&000S0100000FF FileName=\"610099025.BIN\" \n" +
//                ">000S01C0000FF FileName=\"..\\..\\..\\TOOL\\ALADDIN\\OPTIONENABLE.BIN\"\n" +
//                "2000S016000000000500470045001200130044004D004000431F\n" +
//                "\\000S02B0000010220202020202020202020363130303939303235202020202020202020202000000000FFFFFFFF65\n" +
//                "\u0012000S00600000466206F\n" +
//                "p000S3350000000001010102000000030166200000000000000300000001CCE0202020363130303938393230202020202020202020202030C8\n" +
//                "p000S33500000030026620000000000000344E4E00314C0E20202036313030393930323520202020202020202020203007662000000000006A\n" +
//                "P000S325000000600000000100000001202020363130303939303235202020202020202020202030B8\n" +
//                "Ð000S36500000000210000EA050000EA050000EA050000EA050000EA00000100050000EA030000EAFEFFFFEAFEFFFFEAFEFFFFEAFEFFFFEAFEFFFFEA04E04EE200402DE900E04FE101402DE9A8E09FE510009EE510E08EE513F021E31E502DE904100DE201D04DE019\n" +
//                "Ð000S3650000006002402DE930FF2FE10240BDE801D08DE01E50BDE8D2F021E374E09FE538E08EE50140BDE80EF06FE10080FDE8632900FA5F2900FA100F11EE010AC0E30400C0E30100C0E3100F01EED2F021E344109FE501D0A0E1D3F021E33C109FE501D0A0E1A3\n" +
//                "Ð000S365000000C0DFF021E334109FE501D0A0E1500F11EE0F0680E3500F01EE6FF07FF50101A0E3100AE8EE18009FE530FF2FE114C09FE53CFF2FE1000002FC68CE210068C9210068C42100BC0120000C012000000000FA210000FA0AA090E8000C82448344AAF19A\n" +
//                "Ð000S365000001200107DA4501D100F039F8AFF2090EBAE80F0013F0010F18BFFB1A43F0010318476CAF00008CAF0000103A24BF78C878C1FAD8520724BF30C830C144BF04680C60704700000023002400250026103A28BF78C1FBD8520728BF30C148BF0B607047B3\n" +
//                "Ð000S365000001801FB50AF053FA024802490AF0FEF81FBD6884210068C4210010B510BDDFF80CD0FFF7EEFF00F06CE80AF092F968C4210003B4FFF7F1FF03BC0AF0C6F870402DE90C004FE2020250E320D04DE20900002A30009FE57F1090E87F108DE80D00A0E316";
//
//        DLSScannerService service = new DLSScannerService();
//
//        DLSJposConst.updateInProgress = true; // set singleton so a shared port won't interrupt
//
//        DLSDevice device = mock(DLSDevice.class);
//        when(device.getDeviceInfo()).thenReturn(mock(DLSDeviceInfo.class));
//        service.device = device;
//
//        service.setScanner(_scanner);
//        service.options = mock(DLSProperties.class);
//        when(_scanner.sendRecord(anyString())).thenReturn(DLSJposConst.DL_CAN);
//        when(service.options.isSendNulls()).thenReturn(true);
//        when(service.options.isSendFirmwareReset()).thenReturn(true);
//        when(_scanner.detectResetFinish()).thenReturn(true);
//        when(_scanner.isChangeBaudAfterUpdate()).thenReturn(true);
//
//        final int[] eventArrived = {0, 0};
//
//        service.setEventCallbacks(new EventCallback() {
//
//            @Override
//            public void fireEvent(BaseEvent event, EventType type) {
//                eventArrived[0]++;
//                assertThat(type).isEqualTo(EventType.StatusUpdate);
//                assertThat(event instanceof StatusUpdateEvent).isTrue();
//                eventArrived[1] = ((StatusUpdateEvent) event).getStatus();
//            }
//
//            @Override
//            public BaseControl getEventSource() {
//                return null;
//            }
//        });
//
//        service.doUpdate(new ByteArrayInputStream(fileContent.getBytes(StandardCharsets.UTF_8)), "TEST.DAT");
//        assertThat(eventArrived[0]).isEqualTo(3);
//        assertThat(eventArrived[1]).isEqualTo(CommonsConstants.SUE_UF_FAILED_DEV_NEEDS_FIRMWARE);
//        assertThat(service.getDeviceState()).isEqualTo(CommonsConstants.S_IDLE);
//        assertThat(DLSJposConst.updateInProgress).isFalse();
//        verify(_scanner, times(0)).sendRecord("\0\0\0\0");
//        verify(_scanner, times(1)).reset();
//        verify(_scanner, times(1)).changeBaudRate(anyInt());
//        verify(_scanner, times(1)).setChangeBaudAfterUpdate(false);
//    }
//}
