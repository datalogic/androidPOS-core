package com.datalogic.dlapos.androidpos.service;

import android.content.Context;

import com.datalogic.dlapos.androidpos.common.DLSCConfig;
import com.datalogic.dlapos.androidpos.common.DLSDeviceInfo;
import com.datalogic.dlapos.androidpos.common.DLSException;
import com.datalogic.dlapos.androidpos.common.DLSJposConst;
import com.datalogic.dlapos.androidpos.common.DLSProperties;
import com.datalogic.dlapos.androidpos.common.DLSScannerConfig;
import com.datalogic.dlapos.androidpos.common.DLSState;
import com.datalogic.dlapos.androidpos.common.DLSStatistics;
import com.datalogic.dlapos.androidpos.interpretation.DLSDevice;
import com.datalogic.dlapos.androidpos.interpretation.DirectIODataListener;
import com.datalogic.dlapos.androidpos.utils.MockUtils;
import com.datalogic.dlapos.commons.constant.CommonsConstants;
import com.datalogic.dlapos.commons.constant.ErrorConstants;
import com.datalogic.dlapos.commons.control.BaseControl;
import com.datalogic.dlapos.commons.event.BaseEvent;
import com.datalogic.dlapos.commons.event.DirectIOEvent;
import com.datalogic.dlapos.commons.event.EventCallback;
import com.datalogic.dlapos.commons.support.APosException;

import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.Callable;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyVararg;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DLSBaseServiceTest {

    @Mock
    private final DLSDevice _device = mock(DLSDevice.class);

    @Mock
    private final Context _context = mock(Context.class);

    @Test
    public void getDevice() {
        DLSBaseService service = new DLSBaseService();
        assertThat(service.getDevice()).isNull();
        service.device = mock(DLSDevice.class);
        assertThat(service.getDevice()).isNotNull();
    }

    @Test
    public void isClosed() {
        DLSBaseService service = new DLSBaseService();
        service.device = mock(DLSDevice.class);
        when(service.device.getState()).thenReturn(DLSState.OPENED);
        assertThat(service.isClosed()).isFalse();
        when(service.device.getState()).thenReturn(DLSState.CLAIMED);
        assertThat(service.isClosed()).isFalse();
        when(service.device.getState()).thenReturn(DLSState.ENABLED);
        assertThat(service.isClosed()).isFalse();
        when(service.device.getState()).thenReturn(DLSState.DISABLED);
        assertThat(service.isClosed()).isFalse();
        when(service.device.getState()).thenReturn(DLSState.RELEASED);
        assertThat(service.isClosed()).isFalse();
        when(service.device.getState()).thenReturn(DLSState.CLOSED);
        assertThat(service.isClosed()).isTrue();
    }

    @Test
    public void isEnabled() {
        DLSBaseService service = new DLSBaseService();
        assertThat(service.isEnabled()).isFalse();
        service.device = mock(DLSDevice.class);
        when(service.device.getState()).thenReturn(DLSState.OPENED);
        assertThat(service.isEnabled()).isFalse();
        when(service.device.getState()).thenReturn(DLSState.CLAIMED);
        assertThat(service.isEnabled()).isFalse();
        when(service.device.getState()).thenReturn(DLSState.ENABLED);
        assertThat(service.isEnabled()).isTrue();
        when(service.device.getState()).thenReturn(DLSState.DISABLED);
        assertThat(service.isEnabled()).isFalse();
        when(service.device.getState()).thenReturn(DLSState.RELEASED);
        assertThat(service.isEnabled()).isFalse();
        when(service.device.getState()).thenReturn(DLSState.CLOSED);
        assertThat(service.isEnabled()).isFalse();
    }

    @Test
    public void getVersionString() {
        DLSBaseService service = new DLSBaseService();
        assertThat(service.getVersionString()).contains("DLAndroidPOS 1.14.061");
    }

    @Test
    public void getPortName() {
        DLSBaseService service = new DLSBaseService();
        assertThat(service.getPortName()).isEmpty();
        service.device = _device;
        DLSDeviceInfo info = mock(DLSDeviceInfo.class);
        when(_device.getDeviceInfo()).thenReturn(info);
        when(info.getPortAsString()).thenReturn("USB-OEM");
        assertThat(service.getPortName()).isEqualTo("USB-OEM");
    }

    @Test
    public void logicalName() {
        DLSBaseService service = new DLSBaseService();
        service.setLogicalName("Test");
        assertThat(service.getLogicalName()).isEqualTo("Test");
        service.setLogicalName("Test1");
        assertThat(service.getLogicalName()).isEqualTo("Test1");
    }

    @Test
    public void getOptions() {
        DLSBaseService service = new DLSBaseService();
        DLSProperties options = mock(DLSProperties.class);
        service.options = options;
        assertThat(service.getOptions()).isEqualTo(options);
    }

    @Test
    public void getDeviceInfo() {
        DLSBaseService service = new DLSBaseService();
        assertThat(service.getDeviceInfo()).isNull();
        DLSDeviceInfo info = mock(DLSDeviceInfo.class);
        service.device = _device;
        when(_device.getDeviceInfo()).thenReturn(info);
        assertThat(service.getDeviceInfo()).isEqualTo(info);
    }

    @Test(expected = APosException.class)
    public void getDeviceNumberNotOpen() throws APosException {
        DLSBaseService service = new DLSBaseService();
        service.getDeviceNumber();
    }

    @Test(expected = APosException.class)
    public void getDeviceNumberNotClaimed() throws APosException {
        DLSBaseService service = new DLSBaseService();
        service.device = _device;
        DLSDeviceInfo info = mock(DLSDeviceInfo.class);
        when(_device.getDeviceInfo()).thenReturn(info);
        service.getDeviceNumber();
    }

    @Test
    public void getDeviceNumber() throws APosException {
        DLSBaseService service = new DLSBaseService();
        service.device = _device;
        when(_device.getState()).thenReturn(DLSState.CLAIMED);
        DLSDeviceInfo info = mock(DLSDeviceInfo.class);
        when(_device.getDeviceInfo()).thenReturn(info);
        when(info.getDeviceNumber()).thenReturn(12);
        assertThat(service.getDeviceNumber()).isEqualTo(12);
    }

    @Test
    public void getBusType() {
        DLSBaseService service = new DLSBaseService();
        assertThat(service.getBusType()).isNull();
    }

    @Test
    public void eventCallbacks() {
        DLSBaseService service = new DLSBaseService();
        EventCallback callback = mock(EventCallback.class);
        assertThat(service.getEventCallbacks()).isNull();
        service.setEventCallbacks(callback);
        assertThat(service.getEventCallbacks()).isEqualTo(callback);
    }

    @Test(expected = APosException.class)
    public void setDeviceNumberNotClaimed() throws APosException {
        DLSBaseService service = new DLSBaseService();
        service.setDeviceNumber(12);
    }

    @Test
    public void setDeviceNumber() throws APosException {
        DLSBaseService service = new DLSBaseService();
        service.device = _device;
        DLSDeviceInfo info = mock(DLSDeviceInfo.class);
        when(_device.getDeviceInfo()).thenReturn(info);
        when(_device.getState()).thenReturn(DLSState.CLAIMED);
        service.setDeviceNumber(13);
        verify(info, times(1)).setDeviceNumber(13);
    }

    @Test
    public void category() {
        DLSBaseService service = new DLSBaseService();
        assertThat(service.category).isEqualTo("Scanner");
        service.category = "Test";
        assertThat(service.getCategory()).isEqualTo("Test");
    }

    @Test
    public void sendDataEvent() throws APosException {
        DLSBaseService service = new DLSBaseService();
        service.sendDataEvent();
    }

    @Test
    public void completeStatistics() throws APosException {
        DLSBaseService service = new DLSBaseService();
        service.device = _device;
        DLSDeviceInfo info = mock(DLSDeviceInfo.class);
        when(_device.getDeviceInfo()).thenReturn(info);
        when(_device.getState()).thenReturn(DLSState.CLAIMED);
        when(info.getVendorName()).thenReturn("Datalogic");
        when(info.getDeviceDescription()).thenReturn("Mocked Device");
        when(info.getProductDescription()).thenReturn("Mocked Product for test");
        when(info.getProductName()).thenReturn("Mocked Product");
        service.completeStatistics();
        assertThat(service.statistics.get(DLSJposConst.DLS_S_SERVICE_VERSION)).isNotNull();
        assertThat(service.statistics.get(DLSJposConst.DLS_S_UPOS_VERSION)).isNotNull();
        assertThat(service.statistics.get(DLSJposConst.DLS_S_CONTROL_VERSION)).isNotNull();
        assertThat(service.statistics.get(DLSJposConst.DLS_S_POWER_NOTIFY)).isEqualTo("PN_DISABLED");
        assertThat(service.statistics.get(DLSJposConst.DLS_S_POWER_STATE)).isEqualTo(CommonsConstants.PS_UNKNOWN);
        assertThat(service.statistics.get(DLSJposConst.DLS_S_CAP_PWR_REPORT)).isEqualTo("Standard");
        assertThat(service.statistics.get(DLSJposConst.DLS_S_CAP_STATS_REPORT)).isEqualTo(false);
        assertThat(service.statistics.get(DLSJposConst.DLS_S_CAP_UPDATE_STATS)).isEqualTo(false);
        assertThat(service.statistics.get(DLSJposConst.DLS_S_CAP_COMP_FW_VER)).isEqualTo(false);
        assertThat(service.statistics.get(DLSJposConst.DLS_S_CAP_UPDATE_FW)).isEqualTo(false);
        assertThat(service.statistics.get(DLSJposConst.DLS_S_CAPTION)).isEqualTo("Datalogic");
        assertThat(service.statistics.get(DLSJposConst.DLS_S_DESCRIPTION)).isEqualTo("Mocked Device");
        assertThat(service.statistics.get(DLSJposConst.DLS_S_PHYSICAL_DESCRIPTION)).isEqualTo("Mocked Product for test");
        assertThat(service.statistics.get(DLSJposConst.DLS_S_PHYSICAL_NAME)).isEqualTo("Mocked Product");
        assertThat(service.statistics.get(DLSJposConst.DLS_S_ASYNC_MODE)).isEqualTo(0);
        assertThat(service.statistics.get(DLSJposConst.DLS_S_CAPTION)).isEqualTo("Datalogic");
    }

    @Test
    public void checkOpened() throws APosException {
        DLSBaseService service = new DLSBaseService();
        testExceptionError(() -> {
            service.checkOpened();
            return null;
        }, ErrorConstants.APOS_E_CLOSED);
        service.device = _device;
        when(_device.getState()).thenReturn(DLSState.OPENED);
        service.checkOpened();
        when(_device.getState()).thenReturn(DLSState.CLAIMED);
        service.checkOpened();
        when(_device.getState()).thenReturn(DLSState.ENABLED);
        service.checkOpened();
        when(_device.getState()).thenReturn(DLSState.DISABLED);
        service.checkOpened();
        when(_device.getState()).thenReturn(DLSState.RELEASED);
        service.checkOpened();
        when(_device.getState()).thenReturn(DLSState.CLOSED);
        testExceptionError(() -> {
            service.checkOpened();
            return null;
        }, ErrorConstants.APOS_E_CLOSED);
    }

    @Test
    public void checkClaimed() throws APosException {
        DLSBaseService service = new DLSBaseService();
        testExceptionError(() -> {
            service.checkClaimed();
            return null;
        }, ErrorConstants.APOS_E_CLOSED);

        service.device = _device;
        when(_device.getState()).thenReturn(DLSState.OPENED);
        testExceptionError(() -> {
            service.checkClaimed();
            return null;
        }, ErrorConstants.APOS_E_NOTCLAIMED);

        when(_device.getState()).thenReturn(DLSState.CLAIMED);
        service.checkClaimed();

        when(_device.getState()).thenReturn(DLSState.ENABLED);
        service.checkClaimed();

        when(_device.getState()).thenReturn(DLSState.DISABLED);
        service.checkClaimed();

        when(_device.getState()).thenReturn(DLSState.RELEASED);
        testExceptionError(() -> {
            service.checkClaimed();
            return null;
        }, ErrorConstants.APOS_E_NOTCLAIMED);

        when(_device.getState()).thenReturn(DLSState.CLOSED);
        testExceptionError(() -> {
            service.checkClaimed();
            return null;
        }, ErrorConstants.APOS_E_CLOSED);

    }

    @Test
    public void checkEnabled() throws APosException {
        DLSBaseService service = new DLSBaseService();
        testExceptionError(() -> {
            service.checkEnabled();
            return null;
        }, ErrorConstants.APOS_E_DISABLED);

        service.device = _device;
        when(_device.getState()).thenReturn(DLSState.OPENED);
        testExceptionError(() -> {
            service.checkEnabled();
            return null;
        }, ErrorConstants.APOS_E_DISABLED);

        when(_device.getState()).thenReturn(DLSState.CLAIMED);
        testExceptionError(() -> {
            service.checkEnabled();
            return null;
        }, ErrorConstants.APOS_E_DISABLED);

        when(_device.getState()).thenReturn(DLSState.ENABLED);
        service.checkEnabled();

        when(_device.getState()).thenReturn(DLSState.DISABLED);
        testExceptionError(() -> {
            service.checkEnabled();
            return null;
        }, ErrorConstants.APOS_E_DISABLED);

        when(_device.getState()).thenReturn(DLSState.RELEASED);
        testExceptionError(() -> {
            service.checkEnabled();
            return null;
        }, ErrorConstants.APOS_E_DISABLED);

        when(_device.getState()).thenReturn(DLSState.CLOSED);
        testExceptionError(() -> {
            service.checkEnabled();
            return null;
        }, ErrorConstants.APOS_E_DISABLED);

    }

    //region Check Health
    @Test(expected = APosException.class)
    public void checkHealthNotClaimed() throws APosException {
        DLSBaseService service = new DLSBaseService();
        service.checkHealth(2);
    }

    @Test
    public void checkHealthInternal() throws APosException, DLSException {
        DLSBaseService service = new DLSBaseService();
        service.device = _device;
        when(_device.getState()).thenReturn(DLSState.CLAIMED);

        service.checkHealth(CommonsConstants.CH_INTERNAL);
        assertThat(service.getCheckHealthText()).isEqualTo("Internal Health Check: Not Successful.");

        when(_device.isAlive()).thenReturn(true);
        service.checkHealth(CommonsConstants.CH_INTERNAL);
        assertThat(service.getCheckHealthText()).isEqualTo("Internal Health Check: Successful.");

        when(_device.isAlive()).thenThrow(new DLSException(1, "Test"));
        service.checkHealth(CommonsConstants.CH_INTERNAL);
        assertThat(service.getCheckHealthText()).startsWith("Internal Health Check: Not Successful");
    }

    @Test
    public void checkHealthExternal() throws APosException, DLSException {
        DLSBaseService service = new DLSBaseService();
        service.device = _device;
        when(_device.getState()).thenReturn(DLSState.CLAIMED);

        service.checkHealth(CommonsConstants.CH_EXTERNAL);
        assertThat(service.getCheckHealthText()).isEqualTo("External Health Check: Not Successful.");

        when(_device.doHealthCheck()).thenReturn(true);
        service.checkHealth(CommonsConstants.CH_EXTERNAL);
        assertThat(service.getCheckHealthText()).isEqualTo("External Health Check: Successful.");

        when(_device.doHealthCheck()).thenThrow(new DLSException(1, "Test"));
        service.checkHealth(CommonsConstants.CH_EXTERNAL);
        assertThat(service.getCheckHealthText()).startsWith("External Health Check: Not Successful");
    }

    @Test
    public void checkHealthInteractive() throws APosException, DLSException {
        DLSBaseService service = new DLSBaseService();
        service.device = _device;
        when(_device.getState()).thenReturn(DLSState.CLAIMED);

        service.checkHealth(CommonsConstants.CH_INTERACTIVE);
        assertThat(service.getCheckHealthText()).isEqualTo("Interactive Health Check: Not supported.");

    }
    //endregion

    //region Claim
    @Test
    public void claimNoDevice() {
        DLSBaseService service = new DLSBaseService();
        try {
            service.claim(15);
            fail();
        } catch (APosException e) {
            assertThat(e.getErrorCode()).isEqualTo(ErrorConstants.APOS_E_NOHARDWARE);
        }
    }

    @Test
    public void claimAlreadyClaimed() throws APosException, DLSException {
        DLSBaseService service = new DLSBaseService();
        service.device = _device;
        when(_device.getState()).thenReturn(DLSState.CLAIMED);
        service.claim(15);
        verify(_device, times(0)).claim(15);
    }

    @Test
    public void claim() throws APosException, DLSException {
        DLSBaseService service = new DLSBaseService();
        service.device = _device;
        DLSDeviceInfo info = mock(DLSDeviceInfo.class);
        when(info.getDeviceCategory()).thenReturn("TestCategory");
        when(info.getDeviceBus()).thenReturn("USB");
        when(_device.getDeviceInfo()).thenReturn(info);
        service.options = mock(DLSProperties.class);
        when(_device.getState()).thenReturn(DLSState.OPENED);
        service.claim(15);
        verify(_device, times(1)).claim(15);
        verify(_device, times(1)).addDeviceErrorListener(service);
        verify(_device, times(1)).addDeviceStatusListener(service);
        verify(_device, times(1)).addDirectIODataListener(service);
        assertThat(service.getCategory()).isEqualTo("TestCategory");
        assertThat(service.getBusType()).isEqualTo("USB");
    }

    @Test
    public void claimFails() throws DLSException {
        DLSBaseService service = new DLSBaseService();
        service.device = _device;
        DLSDeviceInfo info = mock(DLSDeviceInfo.class);
        when(_device.getDeviceInfo()).thenReturn(info);
        service.options = mock(DLSProperties.class);
        when(_device.getState()).thenReturn(DLSState.OPENED);
        doThrow(new DLSException(12, "Test")).when(_device).claim(15);
        try {
            service.claim(15);
            fail();
        } catch (APosException e) {
            assertThat(e.getErrorCode()).isEqualTo(ErrorConstants.APOS_E_FAILURE);
        }
    }

    //endregion

    //region Close
    @Test
    public void closeWhileClaimed() throws APosException, DLSException {
        DLSBaseService service = new DLSBaseService();
        service.device = _device;
        when(_device.getState()).thenReturn(DLSState.CLAIMED);
        service.close();
        verify(_device, times(1)).release();
        verify(_device, times(1)).close();
    }

    @Test
    public void close() throws APosException, DLSException {
        DLSBaseService service = new DLSBaseService();
        service.device = _device;
        service.close();
        verify(_device, times(0)).release();
        verify(_device, times(1)).close();
    }
    //endregion

    @Test
    public void compareFirmwareVersion() throws APosException {
        DLSBaseService service = new DLSBaseService();
        service.device = _device;
        try {
            service.compareFirmwareVersion("Test", new int[]{1});
            fail();
        } catch (APosException e) {
            assertThat(e.getErrorCode()).isEqualTo(ErrorConstants.APOS_E_NOTCLAIMED);
        }

        when(_device.getState()).thenReturn(DLSState.OPENED);
        try {
            service.compareFirmwareVersion("Test", new int[]{1});
            fail();
        } catch (APosException e) {
            assertThat(e.getErrorCode()).isEqualTo(ErrorConstants.APOS_E_NOTCLAIMED);
        }

        when(_device.getState()).thenReturn(DLSState.CLAIMED);
        try {
            service.compareFirmwareVersion("Test", new int[]{1});
            fail();
        } catch (APosException e) {
            assertThat(e.getErrorCode()).isEqualTo(ErrorConstants.APOS_E_DISABLED);
        }

        when(_device.getState()).thenReturn(DLSState.ENABLED);
        service.compareFirmwareVersion("Test", new int[]{1});
    }

    //region Direct IO
    @Test
    public void directIONotClaimed() {
        DLSBaseService service = new DLSBaseService();
        service.device = _device;
        int[] data = new int[]{1};
        Object obj = new Object();
        try {
            service.directIO(1, data, obj);
            fail();
        } catch (APosException e) {
            assertThat(e.getErrorCode()).isEqualTo(ErrorConstants.APOS_E_NOTCLAIMED);
        }
    }

    @Test
    public void directIOSpecialCommand() throws APosException, DLSException {
        DLSBaseService service = new DLSBaseService();
        service.device = _device;
        int[] data = new int[]{1};
        Object obj = new Object();
        service.directIO(-7, data, obj);
        verify(_device, times(1)).directIO(-7, data, obj);
    }

    @Test
    public void directIO() throws APosException, DLSException {
        DLSBaseService service = new DLSBaseService();
        service.device = _device;
        when(_device.getState()).thenReturn(DLSState.CLAIMED);
        int[] data = new int[]{1};
        Object obj = new Object();
        service.directIO(1, data, obj);
        verify(_device, times(1)).directIO(1, data, obj);
    }

    @Test
    public void directIOFails() throws DLSException {
        DLSBaseService service = new DLSBaseService();
        service.device = _device;
        when(_device.getState()).thenReturn(DLSState.CLAIMED);
        int[] data = new int[]{1};
        Object obj = new Object();
        doThrow(DLSException.class).when(_device).directIO(1, data, obj);
        try {
            service.directIO(1, data, obj);
            fail();
        } catch (APosException e) {
            assertThat(e.getErrorCode()).isEqualTo(ErrorConstants.APOS_E_FAILURE);
        }
    }
    //endregion

    @Test
    public void open() throws APosException {
        MockUtils.mockDLSPropertiesSingleton(mock(DLSProperties.class));
        DLSBaseService service = new DLSBaseService();
        EventCallback callback = mock(EventCallback.class);
        service.open("Test", callback, _context);
        assertThat(service.options).isNotNull();
        assertThat(service.getLogicalName()).isEqualTo("Test");
        assertThat(service.getEventCallbacks()).isEqualTo(callback);
        assertThat(service.context).isEqualTo(_context);
        assertThat(service.getState()).isEqualTo(CommonsConstants.S_IDLE);
        MockUtils.cleanDLSProperties();
    }

    //region Release
    @Test
    public void releaseNotClaimed() {
        DLSBaseService service = new DLSBaseService();
        try {
            service.release();
            fail();
        } catch (APosException e) {
            assertThat(e.getErrorCode()).isEqualTo(ErrorConstants.APOS_E_CLOSED);
        }
    }

    @Test
    public void release() throws APosException, DLSException {
        DLSBaseService service = new DLSBaseService();
        service.device = _device;
        when(_device.getState()).thenReturn(DLSState.CLAIMED);
        service.release();
        verify(_device, times(1)).release();
        verify(_device, times(1)).removeDirectIODataListener(service);
        verify(_device, times(1)).removeDeviceStatusListener(service);
        verify(_device, times(1)).removeDeviceErrorListener(service);
    }

    @Test
    public void releaseFail() throws DLSException {
        DLSBaseService service = new DLSBaseService();
        service.device = _device;
        doThrow(DLSException.class).when(_device).release();
        when(_device.getState()).thenReturn(DLSState.CLAIMED);
        try {
            service.release();
            fail();
        } catch (APosException e) {
            assertThat(e.getErrorCode()).isEqualTo(ErrorConstants.APOS_E_FAILURE);
        }
        verify(_device, times(1)).removeDirectIODataListener(service);
        verify(_device, times(1)).removeDeviceStatusListener(service);
        verify(_device, times(1)).removeDeviceErrorListener(service);
    }
    //endregion

    //region Reset Statistics
    @Test
    public void resetStatisticsNotCapStatisticsReporting() {
        DLSBaseService service = new DLSBaseService();
        service.device = _device;
        when(_device.getState()).thenReturn(DLSState.OPENED);
        try {
            service.resetStatistics("Test");
            fail();
        } catch (APosException e) {
            assertThat(e.getErrorCode()).isEqualTo(ErrorConstants.APOS_E_ILLEGAL);
        }
    }

    @Test
    public void resetStatisticsNotCapUpdatingReporting() {
        DLSBaseService service = new DLSBaseService();
        service.device = _device;
        when(_device.getState()).thenReturn(DLSState.OPENED);
        when(_device.hasStatisticsReporting()).thenReturn(true);
        try {
            service.resetStatistics("Test");
            fail();
        } catch (APosException e) {
            assertThat(e.getErrorCode()).isEqualTo(ErrorConstants.APOS_E_ILLEGAL);
        }
    }
    //endregion

    //region retrieveStatistics
    @Test
    public void retrieveStatisticsNotOpened() {
        DLSBaseService service = new DLSBaseService();
        try {
            service.retrieveStatistics(new String[]{});
            fail();
        } catch (APosException e) {
            assertThat(e.getErrorCode()).isEqualTo(ErrorConstants.APOS_E_CLOSED);
        }
    }

    @Test
    public void retrieveStatisticsNotClaimed() {
        DLSBaseService service = new DLSBaseService();
        service.device = _device;
        when(_device.getState()).thenReturn(DLSState.OPENED);
        try {
            service.retrieveStatistics(new String[]{});
            fail();
        } catch (APosException e) {
            assertThat(e.getErrorCode()).isEqualTo(ErrorConstants.APOS_E_NOTCLAIMED);
        }
    }

    @Test
    public void retrieveStatisticsNoCapStatisticsReport() {
        DLSBaseService service = new DLSBaseService();
        service.device = _device;
        when(_device.getState()).thenReturn(DLSState.CLAIMED);
        try {
            service.retrieveStatistics(new String[]{});
            fail();
        } catch (APosException e) {
            assertThat(e.getErrorCode()).isEqualTo(ErrorConstants.APOS_E_ILLEGAL);
        }
    }

    @Test
    public void retrieveStatistics() throws APosException, DLSException {
        DLSBaseService service = new DLSBaseService();
        service.device = _device;
        service.context = _context;
        DLSStatistics statistics = mock(DLSStatistics.class);
        when(_device.getState()).thenReturn(DLSState.CLAIMED);
        when(_device.hasStatisticsReporting()).thenReturn(true);
        when(_device.getStatistics()).thenReturn(new HashMap<>());
        when(_device.getDeviceInfo()).thenReturn(mock(DLSDeviceInfo.class));
        try (MockedStatic<DLSStatistics> stats = Mockito.mockStatic(DLSStatistics.class)) {
            String[] statisticsBuffer = new String[]{"Test"};
            stats.when(() -> DLSStatistics.getInstance(_context)).thenReturn(statistics);
            service.retrieveStatistics(statisticsBuffer);
        }
    }

    @Test
    public void retrieveStatisticsNullBuffer() throws APosException, DLSException {
        DLSBaseService service = new DLSBaseService();
        service.device = _device;
        service.context = _context;
        DLSStatistics statistics = mock(DLSStatistics.class);
        when(_device.getState()).thenReturn(DLSState.CLAIMED);
        when(_device.hasStatisticsReporting()).thenReturn(true);
        when(_device.getStatistics()).thenReturn(new HashMap<>());
        when(_device.getDeviceInfo()).thenReturn(mock(DLSDeviceInfo.class));
        try (MockedStatic<DLSStatistics> stats = Mockito.mockStatic(DLSStatistics.class)) {
            String[] statisticsBuffer = null;
            stats.when(() -> DLSStatistics.getInstance(_context)).thenReturn(statistics);
            service.retrieveStatistics(statisticsBuffer);
        }
    }

    @Test
    public void retrieveStatisticsFails() throws DLSException {
        DLSBaseService service = new DLSBaseService();
        service.device = _device;
        service.context = _context;
        DLSStatistics statistics = mock(DLSStatistics.class);
        when(_device.getState()).thenReturn(DLSState.CLAIMED);
        when(_device.hasStatisticsReporting()).thenReturn(true);
        doThrow(new DLSException(2, "Test")).when(_device).getStatistics();
        when(_device.getDeviceInfo()).thenReturn(mock(DLSDeviceInfo.class));
        try (MockedStatic<DLSStatistics> stats = Mockito.mockStatic(DLSStatistics.class)) {
            String[] statisticsBuffer = new String[]{"Test"};
            stats.when(() -> DLSStatistics.getInstance(_context)).thenReturn(statistics);
            try {
                service.retrieveStatistics(statisticsBuffer);
                fail();
            } catch (APosException e) {
                assertThat(e.getErrorCode()).isEqualTo(ErrorConstants.APOS_E_ILLEGAL);
            }
        }
    }

    @Test
    public void retrieveStatisticsNullCategory() throws DLSException {
        DLSBaseService service = new DLSBaseService();
        service.device = _device;
        service.context = _context;
        DLSStatistics statistics = mock(DLSStatistics.class);
        when(_device.getState()).thenReturn(DLSState.CLAIMED);
        when(_device.hasStatisticsReporting()).thenReturn(true);
        HashMap<String, Object> statisticsMap = new HashMap<>();
        statisticsMap.put(DLSJposConst.DLS_S_DEVICE_CATEGORY, null);
        when(_device.getStatistics()).thenReturn(statisticsMap);
        when(_device.getDeviceInfo()).thenReturn(mock(DLSDeviceInfo.class));
        try (MockedStatic<DLSStatistics> stats = Mockito.mockStatic(DLSStatistics.class)) {
            String[] statisticsBuffer = new String[]{"Test"};
            stats.when(() -> DLSStatistics.getInstance(_context)).thenReturn(statistics);
            try {
                service.retrieveStatistics(statisticsBuffer);
                fail();
            } catch (APosException e) {
                assertThat(e.getErrorCode()).isEqualTo(ErrorConstants.APOS_E_ILLEGAL);
            }
        }
    }
    //endregion

    @Test
    public void updateFirmware() throws APosException {
        DLSBaseService service = new DLSBaseService();
        testExceptionError(() -> {
            service.updateFirmware("example");
            return null;
        }, ErrorConstants.APOS_E_CLOSED);

        service.device = _device;
        when(_device.getState()).thenReturn(DLSState.OPENED);
        testExceptionError(() -> {
            service.updateFirmware("example");
            return null;
        }, ErrorConstants.APOS_E_NOTCLAIMED);

        when(_device.getState()).thenReturn(DLSState.CLAIMED);
        testExceptionError(() -> {
            service.updateFirmware("example");
            return null;
        }, ErrorConstants.APOS_E_DISABLED);

        when(_device.getState()).thenReturn(DLSState.ENABLED);
        service.updateFirmware("example");
    }

    @Test
    public void updateStatistics() {
        DLSBaseService service = new DLSBaseService();
        service.device = _device;
        testExceptionError(() -> {
            service.updateStatistics("Buff");
            return null;
        }, ErrorConstants.APOS_E_NOTCLAIMED);

        when(_device.getState()).thenReturn(DLSState.OPENED);
        testExceptionError(() -> {
            service.updateStatistics("Buff");
            return null;
        }, ErrorConstants.APOS_E_NOTCLAIMED);

        when(_device.getState()).thenReturn(DLSState.CLAIMED);
        when(_device.hasStatisticsReporting()).thenReturn(false);
        testExceptionError(() -> {
            service.updateStatistics("Buff");
            return null;
        }, ErrorConstants.APOS_E_ILLEGAL);

        when(_device.hasStatisticsReporting()).thenReturn(true);
        testExceptionError(() -> {
            service.updateStatistics("Buff");
            return null;
        }, ErrorConstants.APOS_E_ILLEGAL);

    }

    @Test
    public void clearInput() throws APosException {
        DLSBaseService service = new DLSBaseService();
        service.device = _device;
        testExceptionError(() -> {
            service.clearInput();
            return null;
        }, ErrorConstants.APOS_E_NOTCLAIMED);

        when(_device.getState()).thenReturn(DLSState.OPENED);
        testExceptionError(() -> {
            service.clearInput();
            return null;
        }, ErrorConstants.APOS_E_NOTCLAIMED);

        when(_device.getState()).thenReturn(DLSState.CLAIMED);
        service.clearInput();
    }

    @Test
    public void getCapCompareFirmwareVersion() throws APosException {
        DLSBaseService service = new DLSBaseService();
        service.device = _device;
        when(_device.getState()).thenReturn(DLSState.CLOSED);
        testExceptionError(() -> {
            service.getCapCompareFirmwareVersion();
            return null;
        }, ErrorConstants.APOS_E_CLOSED);

        when(_device.getState()).thenReturn(DLSState.OPENED);

        assertThat(service.getCapCompareFirmwareVersion()).isFalse();
    }

    @Test
    public void getCapPowerReporting() throws APosException {
        DLSBaseService service = new DLSBaseService();
        service.device = _device;
        when(_device.getState()).thenReturn(DLSState.CLOSED);
        testExceptionError(() -> {
            service.getCapPowerReporting();
            return null;
        }, ErrorConstants.APOS_E_CLOSED);

        when(_device.getState()).thenReturn(DLSState.OPENED);

        assertThat(service.getCapPowerReporting()).isEqualTo(CommonsConstants.PR_STANDARD);
    }

    @Test
    public void getCapStatisticsReporting() throws APosException {
        DLSBaseService service = new DLSBaseService();
        service.device = _device;
        when(_device.getState()).thenReturn(DLSState.CLOSED);
        testExceptionError(() -> {
            service.getCapStatisticsReporting();
            return null;
        }, ErrorConstants.APOS_E_CLOSED);

        when(_device.getState()).thenReturn(DLSState.OPENED);

        assertThat(service.getCapStatisticsReporting()).isEqualTo(false);
    }

    @Test
    public void getCapUpdateFirmware() throws APosException {
        DLSBaseService service = new DLSBaseService();
        service.device = _device;
        when(_device.getState()).thenReturn(DLSState.CLOSED);
        testExceptionError(() -> {
            service.getCapUpdateFirmware();
            return null;
        }, ErrorConstants.APOS_E_CLOSED);

        when(_device.getState()).thenReturn(DLSState.OPENED);

        assertThat(service.getCapUpdateFirmware()).isEqualTo(false);
    }

    @Test
    public void getCapUpdateStatistics() throws APosException {
        DLSBaseService service = new DLSBaseService();
        service.device = _device;
        when(_device.getState()).thenReturn(DLSState.CLOSED);
        testExceptionError(() -> {
            service.getCapUpdateStatistics();
            return null;
        }, ErrorConstants.APOS_E_CLOSED);

        when(_device.getState()).thenReturn(DLSState.OPENED);

        assertThat(service.getCapUpdateStatistics()).isEqualTo(false);
    }

    @Test
    public void getCheckHealthText() throws APosException {
        DLSBaseService service = new DLSBaseService();
        service.device = _device;
        when(_device.getState()).thenReturn(DLSState.CLOSED);
        testExceptionError(() -> {
            service.getCheckHealthText();
            return null;
        }, ErrorConstants.APOS_E_CLOSED);

        when(_device.getState()).thenReturn(DLSState.OPENED);

        assertThat(service.getCheckHealthText()).isNull();
    }

    @Test
    public void autoDisable() throws APosException {
        DLSBaseService service = new DLSBaseService();
        service.device = _device;
        when(_device.getState()).thenReturn(DLSState.CLOSED);
        testExceptionError(() -> {
            service.setAutoDisable(true);
            return null;
        }, ErrorConstants.APOS_E_CLOSED);

        when(_device.getState()).thenReturn(DLSState.CLOSED);
        testExceptionError(() -> {
            service.getAutoDisable();
            return null;
        }, ErrorConstants.APOS_E_CLOSED);

        when(_device.getState()).thenReturn(DLSState.OPENED);
        service.setAutoDisable(true);
        assertThat(service.getAutoDisable()).isTrue();
        service.setAutoDisable(false);
        assertThat(service.getAutoDisable()).isFalse();
    }

    @Test
    public void getClaimed() throws APosException {
        DLSBaseService service = new DLSBaseService();
        service.device = _device;
        when(_device.getState()).thenReturn(DLSState.CLOSED);
        testExceptionError(() -> {
            service.getClaimed();
            return null;
        }, ErrorConstants.APOS_E_CLOSED);

        when(_device.getState()).thenReturn(DLSState.OPENED);
        assertThat(service.getClaimed()).isFalse();

        when(_device.getState()).thenReturn(DLSState.CLAIMED);
        assertThat(service.getClaimed()).isTrue();

        when(_device.getState()).thenReturn(DLSState.ENABLED);
        assertThat(service.getClaimed()).isTrue();

        when(_device.getState()).thenReturn(DLSState.DISABLED);
        assertThat(service.getClaimed()).isTrue();

        when(_device.getState()).thenReturn(DLSState.RELEASED);
        assertThat(service.getClaimed()).isFalse();
    }

    @Test
    public void dataEventEnabled() throws APosException {
        DLSBaseService service = new DLSBaseService();
        service.device = _device;
        when(_device.getState()).thenReturn(DLSState.CLOSED);
        testExceptionError(() -> {
            service.setDataEventEnabled(true);
            return null;
        }, ErrorConstants.APOS_E_CLOSED);
        testExceptionError(() -> {
            service.getDataEventEnabled();
            return null;
        }, ErrorConstants.APOS_E_CLOSED);

        when(_device.getState()).thenReturn(DLSState.OPENED);
        service.setDataEventEnabled(true);
        assertThat(service.getDataEventEnabled()).isTrue();
        service.setDataEventEnabled(false);
        assertThat(service.getDataEventEnabled()).isFalse();

        when(_device.getState()).thenReturn(DLSState.CLAIMED);
        service.setDataEventEnabled(true);
        assertThat(service.getDataEventEnabled()).isTrue();
        service.setDataEventEnabled(false);
        assertThat(service.getDataEventEnabled()).isFalse();
    }

    @Test
    public void deviceEnabledNotClaimed() throws APosException {
        DLSBaseService service = new DLSBaseService();
        service.device = _device;
        when(_device.getState()).thenReturn(DLSState.CLOSED);
        testExceptionError(() -> {
            service.setDeviceEnabled(true);
            return null;
        }, ErrorConstants.APOS_E_CLOSED);
        testExceptionError(() -> {
            service.getDeviceEnabled();
            return null;
        }, ErrorConstants.APOS_E_CLOSED);

        when(_device.getState()).thenReturn(DLSState.OPENED);
        testExceptionError(() -> {
            service.setDeviceEnabled(true);
            return null;
        }, ErrorConstants.APOS_E_NOTCLAIMED);
        assertThat(service.getDeviceEnabled()).isFalse();
    }

    @Test
    public void deviceEnabled() throws APosException, DLSException {
        DLSBaseService service = new DLSBaseService();
        service.device = _device;
        service.setPowerNotify(CommonsConstants.PN_ENABLED);
        when(_device.getState()).thenReturn(DLSState.ENABLED);
        when(_device.isAlive()).thenReturn(true);
        DLSCConfig config = mock(DLSCConfig.class);
        when(config.getOptionAsBool(DLSScannerConfig.KEY_CANNOTIFYPOWERCHANGE)).thenReturn(true);
        when(_device.getConfiguration()).thenReturn(config);
        service.setDeviceEnabled(true);
        service.setDeviceEnabled(false);
    }

    @Test
    public void deviceEnabledUNKNOWN() throws APosException, DLSException {
        DLSBaseService service = new DLSBaseService();
        service.device = _device;
        service.setPowerNotify(CommonsConstants.PN_ENABLED);
        when(_device.getState()).thenReturn(DLSState.CLAIMED);
        when(_device.isAlive()).thenReturn(true);
        DLSCConfig config = mock(DLSCConfig.class);
        when(config.getOptionAsBool(DLSScannerConfig.KEY_CANNOTIFYPOWERCHANGE)).thenReturn(true);
        when(_device.getConfiguration()).thenReturn(config);
        service.setDeviceEnabled(true);
    }

    @Test
    public void deviceServiceDescription() throws APosException {
        DLSBaseService service = new DLSBaseService();
        try {
            service.getDeviceServiceDescription();
            fail();
        } catch (APosException e) {
            assertThat(e.getErrorCode()).isEqualTo(ErrorConstants.APOS_E_CLOSED);
        }

        service.device = _device;
        when(_device.getState()).thenReturn(DLSState.OPENED);
        DLSDeviceInfo deviceInfo = mock(DLSDeviceInfo.class);
        when(deviceInfo.getProductDescription()).thenReturn("Test");
        when(_device.getDeviceInfo()).thenReturn(deviceInfo);
        assertThat(service.getDeviceServiceDescription()).isEqualTo("Test");
    }

    @Test
    public void deviceServiceVersion() throws APosException {
        DLSBaseService service = new DLSBaseService();
        try {
            service.getDeviceServiceVersion();
            fail();
        } catch (APosException e) {
            assertThat(e.getErrorCode()).isEqualTo(ErrorConstants.APOS_E_CLOSED);
        }

        service.device = _device;
        when(_device.getState()).thenReturn(DLSState.OPENED);
        assertThat(service.getDeviceServiceVersion()).isEqualTo(DLSBaseService.DEVICE_SERVICE_VERSION);
    }

    @Test
    public void freezeEvents() throws APosException {
        DLSBaseService service = new DLSBaseService();
        try {
            service.setFreezeEvents(true);
            fail();
        } catch (APosException e) {
            assertThat(e.getErrorCode()).isEqualTo(ErrorConstants.APOS_E_CLOSED);
        }

        try {
            service.setFreezeEvents(false);
            fail();
        } catch (APosException e) {
            assertThat(e.getErrorCode()).isEqualTo(ErrorConstants.APOS_E_CLOSED);
        }

        try {
            service.getFreezeEvents();
            fail();
        } catch (APosException e) {
            assertThat(e.getErrorCode()).isEqualTo(ErrorConstants.APOS_E_CLOSED);
        }

        service.device = _device;
        when(_device.getState()).thenReturn(DLSState.OPENED);

        service.setFreezeEvents(true);
        assertThat(service.getFreezeEvents()).isTrue();
        service.setFreezeEvents(false);
        assertThat(service.getFreezeEvents()).isFalse();
    }

    @Test
    public void physicalDeviceDescription() throws APosException {
        DLSBaseService service = new DLSBaseService();
        try {
            service.getPhysicalDeviceDescription();
            fail();
        } catch (APosException e) {
            assertThat(e.getErrorCode()).isEqualTo(ErrorConstants.APOS_E_CLOSED);
        }

        service.device = _device;
        when(_device.getState()).thenReturn(DLSState.OPENED);
        DLSDeviceInfo info = mock(DLSDeviceInfo.class);
        when(_device.getDeviceInfo()).thenReturn(info);
        when(info.getDeviceDescription()).thenReturn("Test");
        assertThat(service.getPhysicalDeviceDescription()).isEqualTo("Test");
    }

    @Test
    public void physicalDeviceName() throws APosException {
        DLSBaseService service = new DLSBaseService();
        try {
            service.getPhysicalDeviceName();
            fail();
        } catch (APosException e) {
            assertThat(e.getErrorCode()).isEqualTo(ErrorConstants.APOS_E_CLOSED);
        }

        service.device = _device;
        when(_device.getState()).thenReturn(DLSState.OPENED);
        DLSDeviceInfo info = mock(DLSDeviceInfo.class);
        when(_device.getDeviceInfo()).thenReturn(info);
        when(info.getDeviceName()).thenReturn("Test");
        assertThat(service.getPhysicalDeviceName()).isEqualTo("Test");
    }

    @Test
    public void powerNotify() throws APosException {
        DLSBaseService service = new DLSBaseService();
        try {
            service.setPowerNotify(0);
            fail();
        } catch (APosException e) {
            assertThat(e.getErrorCode()).isEqualTo(ErrorConstants.APOS_E_CLOSED);
        }
        try {
            service.setPowerNotify(1);
            fail();
        } catch (APosException e) {
            assertThat(e.getErrorCode()).isEqualTo(ErrorConstants.APOS_E_CLOSED);
        }
        try {
            service.getPowerNotify();
            fail();
        } catch (APosException e) {
            assertThat(e.getErrorCode()).isEqualTo(ErrorConstants.APOS_E_CLOSED);
        }

        service.device = _device;
        when(_device.getState()).thenReturn(DLSState.OPENED);

        service.setPowerNotify(0);
        assertThat(service.getPowerNotify()).isEqualTo(0);

        service.setPowerNotify(1);
        assertThat(service.getPowerNotify()).isEqualTo(1);
    }

    @Test
    public void powerState() throws APosException {
        DLSBaseService service = new DLSBaseService();
        try {
            service.getPowerState();
            fail();
        } catch (APosException e) {
            assertThat(e.getErrorCode()).isEqualTo(ErrorConstants.APOS_E_CLOSED);
        }

        service.device = _device;
        when(_device.getState()).thenReturn(DLSState.OPENED);

        assertThat(service.getPowerState()).isEqualTo(CommonsConstants.PS_UNKNOWN);
    }

    @Test
    public void state() {
        DLSBaseService service = new DLSBaseService();
        assertThat(service.getState()).isEqualTo(0);

        service.deviceState = 1;
        assertThat(service.getState()).isEqualTo(1);
    }

    @Test
    public void delete() throws APosException {
        DLSBaseService service = new DLSBaseService();
        service.delete();
    }

    @Test
    public void dataCount() throws APosException {
        DLSBaseService service = new DLSBaseService();
        assertThat(service.getDataCount()).isEqualTo(0);
    }

    @Test
    public void onDeviceError() {
        DLSBaseService service = new DLSBaseService();
        service.onDeviceError(3);
    }

    @Test
    public void onDeviceStatus() {
        DLSBaseService service = new DLSBaseService();
        service.onDeviceStatus(3);
    }

    @Test
    public void onDirectIOData() {
        DLSBaseService service = new DLSBaseService();
        service.onDirectIOData(0, null);

        service.onDirectIOData(1, "Ciao".getBytes());

        EventCallback callback = new EventCallback() {
            @Override
            public void fireEvent(BaseEvent event, EventType type) {
                assertThat(type).isEqualTo(EventCallback.EventType.DirectIO);
                assertThat(event instanceof DirectIOEvent).isTrue();
                assertThat(event.getSource()).isEqualTo(service);
                assertThat(event.getSource()).isEqualTo(service);
                DirectIOEvent parsedEvent = (DirectIOEvent) event;
                assertThat(parsedEvent.getData()).isEqualTo(0);
                assertThat(parsedEvent.getObj().toString()).isEqualTo("Ciao");
            }

            @Override
            public BaseControl getEventSource() {
                return null;
            }
        };
        service.setEventCallbacks(callback);
        service.onDirectIOData(0, "Ciao".getBytes());
    }

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