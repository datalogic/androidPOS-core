package com.datalogic.dlapos.androidpos.service;

import android.content.Context;

import com.datalogic.dlapos.androidpos.common.DLSCConfig;
import com.datalogic.dlapos.androidpos.common.DLSDeviceInfo;
import com.datalogic.dlapos.androidpos.common.DLSException;
import com.datalogic.dlapos.androidpos.common.DLSJposConst;
import com.datalogic.dlapos.androidpos.common.DLSObjectFactory;
import com.datalogic.dlapos.androidpos.common.DLSProperties;
import com.datalogic.dlapos.androidpos.common.DLSScaleConfig;
import com.datalogic.dlapos.androidpos.common.DLSState;
import com.datalogic.dlapos.androidpos.interpretation.DLSScale;
import com.datalogic.dlapos.androidpos.interpretation.DeviceErrorStatusListener;
import com.datalogic.dlapos.androidpos.utils.MockUtils;
import com.datalogic.dlapos.commons.constant.CommonsConstants;
import com.datalogic.dlapos.commons.constant.ErrorConstants;
import com.datalogic.dlapos.commons.constant.ScaleConstants;
import com.datalogic.dlapos.commons.control.BaseControl;
import com.datalogic.dlapos.commons.event.BaseEvent;
import com.datalogic.dlapos.commons.event.DataEvent;
import com.datalogic.dlapos.commons.event.ErrorEvent;
import com.datalogic.dlapos.commons.event.EventCallback;
import com.datalogic.dlapos.commons.event.StatusUpdateEvent;
import com.datalogic.dlapos.commons.support.APosException;
import com.datalogic.dlapos.confighelper.DLAPosConfigHelper;
import com.datalogic.dlapos.confighelper.configurations.accessor.ProfileManager;

import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.HashMap;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DLSScaleServiceTest {

    @Mock
    private final DLSScale _scale = mock(DLSScale.class);

    @Mock
    private final Context _context = mock(Context.class);

    @Test
    public void autoDisable() throws APosException {
        DLSScaleService service = new DLSScaleService();
        service.device = _scale;
        when(_scale.getState()).thenReturn(DLSState.CLOSED);
        try {
            service.setAutoDisable(true);
            fail();
        } catch (APosException e) {
            assertThat(e.getErrorCode()).isEqualTo(ErrorConstants.APOS_E_CLOSED);
        }
        try {
            service.getAutoDisable();
            fail();
        } catch (APosException e) {
            assertThat(e.getErrorCode()).isEqualTo(ErrorConstants.APOS_E_CLOSED);
        }

        when(_scale.getState()).thenReturn(DLSState.OPENED);
        service.setAutoDisable(true);
        assertThat(service.getAutoDisable()).isTrue();
        service.setAutoDisable(false);
        assertThat(service.getAutoDisable()).isFalse();
    }

    @Test
    public void asyncMode() throws APosException {
        DLSScaleService service = new DLSScaleService();
        service.device = _scale;

        when(_scale.getState()).thenReturn(DLSState.CLOSED);
        try {
            service.setAsyncMode(true);
            fail();
        } catch (APosException e) {
            assertThat(e.getErrorCode()).isEqualTo(ErrorConstants.APOS_E_CLOSED);
        }
        try {
            service.getAsyncMode();
            fail();
        } catch (APosException e) {
            assertThat(e.getErrorCode()).isEqualTo(ErrorConstants.APOS_E_CLOSED);
        }

        when(_scale.getState()).thenReturn(DLSState.OPENED);
        service.setAsyncMode(true);
        assertThat(service.getAsyncMode()).isTrue();
        service.setAsyncMode(false);
        assertThat(service.getAsyncMode()).isFalse();
    }

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
    public void claim() throws APosException, DLSException {
        DLSScaleService service = new DLSScaleService();
        service.device = _scale;
        service.scale = _scale;
        DLSCConfig config = mock(DLSCConfig.class);
        DLSDeviceInfo info = mock(DLSDeviceInfo.class);
        when(_scale.getConfiguration()).thenReturn(config);
        when(_scale.isAlive()).thenReturn(true);
        when(info.getDeviceCategory()).thenReturn("TestCategory");
        when(info.getDeviceBus()).thenReturn("USB");
        when(_scale.getDeviceInfo()).thenReturn(info);
        service.options = mock(DLSProperties.class);
        when(_scale.getState()).thenReturn(DLSState.OPENED);
        service.claim(15);
        verify(_scale, times(1)).claim(15);
        verify(_scale, times(1)).addDeviceErrorListener(service);
        verify(_scale, times(1)).addDeviceStatusListener(service);
        verify(_scale, times(1)).addDirectIODataListener(service);
        assertThat(service.getCategory()).isEqualTo("TestCategory");
        assertThat(service.getBusType()).isEqualTo("USB");
    }

    @Test
    public void claimException() throws DLSException {
        DLSScaleService service = new DLSScaleService();
        service.device = _scale;
        service.scale = _scale;
        DLSCConfig config = mock(DLSCConfig.class);
        when(config.getOptionAsBool(DLSScaleConfig.KEY_CANACCEPTSTATISTICSCMD)).thenReturn(true);
        DLSDeviceInfo info = mock(DLSDeviceInfo.class);
        when(_scale.getConfiguration()).thenReturn(config);
        doThrow(new DLSException(2, "fd")).when(_scale).isAlive();
        when(info.getDeviceCategory()).thenReturn("TestCategory");
        when(info.getDeviceBus()).thenReturn("USB");
        when(_scale.getDeviceInfo()).thenReturn(info);
        service.options = mock(DLSProperties.class);
        when(_scale.getState()).thenReturn(DLSState.CLAIMED);
        try {
            service.claim(15);
        } catch (APosException e) {
            assertThat(e.getErrorCode()).isEqualTo(ErrorConstants.APOS_E_ILLEGAL);
        }
    }

    @Test
    public void claimNotAlive() throws DLSException {
        DLSScaleService service = new DLSScaleService();
        service.device = _scale;
        service.scale = _scale;
        DLSCConfig config = mock(DLSCConfig.class);
        DLSDeviceInfo info = mock(DLSDeviceInfo.class);
        when(_scale.getConfiguration()).thenReturn(config);
        when(info.getDeviceCategory()).thenReturn("TestCategory");
        when(info.getDeviceBus()).thenReturn("USB");
        when(_scale.getDeviceInfo()).thenReturn(info);
        service.options = mock(DLSProperties.class);
        when(_scale.getState()).thenReturn(DLSState.OPENED);
        try {
            service.claim(15);
        } catch (APosException e) {
            assertThat(e.getErrorCode()).isEqualTo(ErrorConstants.APOS_E_ILLEGAL);
        }
        verify(_scale, times(1)).claim(15);
        verify(_scale, times(1)).addDeviceErrorListener(service);
        verify(_scale, times(1)).addDeviceStatusListener(service);
        verify(_scale, times(1)).addDirectIODataListener(service);
        assertThat(service.getCategory()).isEqualTo("TestCategory");
        assertThat(service.getBusType()).isEqualTo("USB");
    }

    @Test
    public void claimFails() throws DLSException {
        DLSScaleService service = new DLSScaleService();
        service.device = _scale;
        service.scale = _scale;
        DLSDeviceInfo info = mock(DLSDeviceInfo.class);
        when(_scale.getDeviceInfo()).thenReturn(info);
        service.options = mock(DLSProperties.class);
        when(_scale.getState()).thenReturn(DLSState.OPENED);
        doThrow(new DLSException(12, "Test")).when(_scale).claim(15);
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
        DLSScaleService service = new DLSScaleService();
        service.device = _scale;
        when(_scale.getState()).thenReturn(DLSState.CLOSED);
        try {
            service.clearInput();
            fail();
        } catch (APosException e) {
            assertThat(e.getErrorCode()).isEqualTo(ErrorConstants.APOS_E_CLOSED);
        }

        when(_scale.getState()).thenReturn(DLSState.OPENED);
        try {
            service.clearInput();
            fail();
        } catch (APosException e) {
            assertThat(e.getErrorCode()).isEqualTo(ErrorConstants.APOS_E_NOTCLAIMED);
        }

        when(_scale.getState()).thenReturn(DLSState.CLAIMED);
        service.currentWeightData.nState = 23;
        service.currentWeightData.nEndTime = 4684;
        service.currentWeightData.bSendPending = true;
        service.clearInput();
        assertThat(service.currentWeightData.nState).isEqualTo(DLSScaleService.CMD_ABORT);
        assertThat(service.currentWeightData.nEndTime).isEqualTo(0);
        assertThat(service.currentWeightData.bSendPending).isFalse();
    }

    @Test
    public void dataEventEnabled() throws APosException {
        DLSScaleService service = new DLSScaleService();
        service.device = _scale;
        service.scale = _scale;

        when(_scale.getState()).thenReturn(DLSState.CLOSED);
        try {
            service.setDataEventEnabled(true);
            fail();
        } catch (APosException e) {
            assertThat(e.getErrorCode()).isEqualTo(ErrorConstants.APOS_E_CLOSED);
        }
        try {
            service.getDataEventEnabled();
            fail();
        } catch (APosException e) {
            assertThat(e.getErrorCode()).isEqualTo(ErrorConstants.APOS_E_CLOSED);
        }

        when(_scale.getState()).thenReturn(DLSState.OPENED);
        service.setDataEventEnabled(true);
        assertThat(service.getDataEventEnabled()).isTrue();

        service.setDataEventEnabled(false);
        assertThat(service.getDataEventEnabled()).isFalse();

        when(_scale.getState()).thenReturn(DLSState.CLAIMED);
        service.setDataEventEnabled(true);
        assertThat(service.getDataEventEnabled()).isTrue();
    }

    @Test
    public void delete() throws APosException {
        DLSScaleService service = new DLSScaleService();
        service.delete();
    }

    @Test
    public void displayText() throws APosException {
        DLSScaleService service = new DLSScaleService();
        service.device = _scale;
        when(_scale.getState()).thenReturn(DLSState.CLOSED);
        try {
            service.displayText("tre");
            fail();
        } catch (APosException e) {
            assertThat(e.getErrorCode()).isEqualTo(ErrorConstants.APOS_E_CLOSED);
        }

        when(_scale.getState()).thenReturn(DLSState.OPENED);
        try {
            service.displayText("tre");
            fail();
        } catch (APosException e) {
            assertThat(e.getErrorCode()).isEqualTo(ErrorConstants.APOS_E_NOTCLAIMED);
        }

        when(_scale.getState()).thenReturn(DLSState.CLAIMED);
        try {
            service.displayText("tre");
            fail();
        } catch (APosException e) {
            assertThat(e.getErrorCode()).isEqualTo(ErrorConstants.APOS_E_DISABLED);
        }

        when(_scale.getState()).thenReturn(DLSState.ENABLED);
        service.displayText("tre");
    }

    @Test
    public void doPriceCalculating() {
        DLSScaleService service = new DLSScaleService();
        try {
            service.doPriceCalculating(null, null, null, null, null, null, null, null, 0);
            fail();
        } catch (APosException e) {
            assertThat(e.getErrorCode()).isEqualTo(ErrorConstants.APOS_E_ILLEGAL);
        }
    }

    @Test
    public void freezeValue() {
        DLSScaleService service = new DLSScaleService();
        try {
            service.freezeValue(1, true);
            fail();
        } catch (APosException e) {
            assertThat(e.getErrorCode()).isEqualTo(ErrorConstants.APOS_E_ILLEGAL);
        }
    }

    @Test
    public void getCapDisplay() throws APosException {
        DLSScaleService service = new DLSScaleService();
        service.device = _scale;
        when(_scale.getState()).thenReturn(DLSState.CLOSED);
        try {
            service.getCapDisplay();
            fail();
        } catch (APosException e) {
            assertThat(e.getErrorCode()).isEqualTo(ErrorConstants.APOS_E_CLOSED);
        }

        when(_scale.getState()).thenReturn(DLSState.OPENED);
        assertThat(service.getCapDisplay()).isFalse();
    }

    @Test
    public void getCapDisplayText() throws APosException {
        DLSScaleService service = new DLSScaleService();
        service.device = _scale;

        when(_scale.getState()).thenReturn(DLSState.CLOSED);
        try {
            service.getCapDisplayText();
            fail();
        } catch (APosException e) {
            assertThat(e.getErrorCode()).isEqualTo(ErrorConstants.APOS_E_CLOSED);
        }

        when(_scale.getState()).thenReturn(DLSState.OPENED);
        assertThat(service.getCapDisplayText()).isFalse();
    }

    @Test
    public void getCapFreezeValue() throws APosException {
        DLSScaleService service = new DLSScaleService();
        service.device = _scale;
        when(_scale.getState()).thenReturn(DLSState.CLOSED);
        try {
            service.getCapFreezeValue();
            fail();
        } catch (APosException e) {
            assertThat(e.getErrorCode()).isEqualTo(ErrorConstants.APOS_E_CLOSED);
        }

        when(_scale.getState()).thenReturn(DLSState.OPENED);
        assertThat(service.getCapFreezeValue()).isFalse();
    }

    @Test
    public void getMinimumWeight() throws APosException {
        DLSScaleService service = new DLSScaleService();
        service.device = _scale;

        when(_scale.getState()).thenReturn(DLSState.CLOSED);
        try {
            service.getMinimumWeight();
            fail();
        } catch (APosException e) {
            assertThat(e.getErrorCode()).isEqualTo(ErrorConstants.APOS_E_CLOSED);
        }

        when(_scale.getState()).thenReturn(DLSState.OPENED);
        assertThat(service.getMinimumWeight()).isEqualTo(0);
    }

    @Test
    public void getCapPriceCalculating() throws APosException {
        DLSScaleService service = new DLSScaleService();
        service.device = _scale;

        when(_scale.getState()).thenReturn(DLSState.CLOSED);
        try {
            service.getCapPriceCalculating();
            fail();
        } catch (APosException e) {
            assertThat(e.getErrorCode()).isEqualTo(ErrorConstants.APOS_E_CLOSED);
        }

        when(_scale.getState()).thenReturn(DLSState.OPENED);
        assertThat(service.getCapPriceCalculating()).isFalse();
    }

    @Test
    public void getCapReadLiveWeightWithTare() throws APosException {
        DLSScaleService service = new DLSScaleService();
        service.device = _scale;

        when(_scale.getState()).thenReturn(DLSState.CLOSED);
        try {
            service.getCapReadLiveWeightWithTare();
            fail();
        } catch (APosException e) {
            assertThat(e.getErrorCode()).isEqualTo(ErrorConstants.APOS_E_CLOSED);
        }

        when(_scale.getState()).thenReturn(DLSState.OPENED);
        assertThat(service.getCapReadLiveWeightWithTare()).isFalse();
    }

    @Test
    public void getCapSetPriceCalculationMode() throws APosException {
        DLSScaleService service = new DLSScaleService();
        service.device = _scale;
        when(_scale.getState()).thenReturn(DLSState.CLOSED);
        try {
            service.getCapSetPriceCalculationMode();
            fail();
        } catch (APosException e) {
            assertThat(e.getErrorCode()).isEqualTo(ErrorConstants.APOS_E_CLOSED);
        }

        when(_scale.getState()).thenReturn(DLSState.OPENED);
        assertThat(service.getCapSetPriceCalculationMode()).isFalse();
    }

    @Test
    public void getCapSetUnitPriceWithWeightUnit() throws APosException {
        DLSScaleService service = new DLSScaleService();
        service.device = _scale;
        when(_scale.getState()).thenReturn(DLSState.CLOSED);
        try {
            service.getCapSetUnitPriceWithWeightUnit();
            fail();
        } catch (APosException e) {
            assertThat(e.getErrorCode()).isEqualTo(ErrorConstants.APOS_E_CLOSED);
        }

        when(_scale.getState()).thenReturn(DLSState.OPENED);
        assertThat(service.getCapSetUnitPriceWithWeightUnit()).isFalse();
    }

    @Test
    public void getCapSpecialTare() throws APosException {
        DLSScaleService service = new DLSScaleService();
        service.device = _scale;
        when(_scale.getState()).thenReturn(DLSState.CLOSED);
        try {
            service.getCapSpecialTare();
            fail();
        } catch (APosException e) {
            assertThat(e.getErrorCode()).isEqualTo(ErrorConstants.APOS_E_CLOSED);
        }

        when(_scale.getState()).thenReturn(DLSState.OPENED);
        assertThat(service.getCapSpecialTare()).isFalse();
    }

    @Test
    public void getCapStatusUpdate() throws APosException {
        DLSScaleService service = new DLSScaleService();
        service.device = _scale;
        service.scale = _scale;
        when(_scale.getState()).thenReturn(DLSState.CLOSED);
        try {
            service.getCapStatusUpdate();
            fail();
        } catch (APosException e) {
            assertThat(e.getErrorCode()).isEqualTo(ErrorConstants.APOS_E_CLOSED);
        }

        when(_scale.getState()).thenReturn(DLSState.OPENED);
        assertThat(service.getCapStatusUpdate()).isFalse();

        when(_scale.canStatusUpdate()).thenReturn(true);
        assertThat(service.getCapStatusUpdate()).isTrue();
    }

    @Test
    public void getCapTarePriority() throws APosException {
        DLSScaleService service = new DLSScaleService();
        service.device = _scale;
        when(_scale.getState()).thenReturn(DLSState.CLOSED);
        try {
            service.getCapTarePriority();
            fail();
        } catch (APosException e) {
            assertThat(e.getErrorCode()).isEqualTo(ErrorConstants.APOS_E_CLOSED);
        }
        when(_scale.getState()).thenReturn(DLSState.OPENED);
        assertThat(service.getCapTarePriority()).isFalse();
    }

    @Test
    public void getCapTareWeight() throws APosException {
        DLSScaleService service = new DLSScaleService();
        service.device = _scale;

        when(_scale.getState()).thenReturn(DLSState.CLOSED);
        try {
            service.getCapTareWeight();
            fail();
        } catch (APosException e) {
            assertThat(e.getErrorCode()).isEqualTo(ErrorConstants.APOS_E_CLOSED);
        }

        when(_scale.getState()).thenReturn(DLSState.OPENED);
        try {
            service.getCapTareWeight();
            fail();
        } catch (APosException e) {
            assertThat(e.getErrorCode()).isEqualTo(ErrorConstants.APOS_E_NOTCLAIMED);
        }

        when(_scale.getState()).thenReturn(DLSState.CLAIMED);
        try {
            service.getCapTareWeight();
            fail();
        } catch (APosException e) {
            assertThat(e.getErrorCode()).isEqualTo(ErrorConstants.APOS_E_DISABLED);
        }

        when(_scale.getState()).thenReturn(DLSState.ENABLED);
        assertThat(service.getCapTareWeight()).isFalse();
    }

    @Test
    public void getCapZeroScale() throws APosException {
        DLSScaleService service = new DLSScaleService();
        service.device = _scale;
        service.scale = _scale;

        when(_scale.getState()).thenReturn(DLSState.CLOSED);
        try {
            service.getCapZeroScale();
            fail();
        } catch (APosException e) {
            assertThat(e.getErrorCode()).isEqualTo(ErrorConstants.APOS_E_CLOSED);
        }

        when(_scale.getState()).thenReturn(DLSState.OPENED);
        assertThat(service.getCapZeroScale()).isFalse();
    }

    @Test
    public void getDataCount() throws APosException {
        DLSScaleService service = new DLSScaleService();
        service.device = _scale;

        when(_scale.getState()).thenReturn(DLSState.CLOSED);
        try {
            service.getDataCount();
            fail();
        } catch (APosException e) {
            assertThat(e.getErrorCode()).isEqualTo(ErrorConstants.APOS_E_CLOSED);
        }

        when(_scale.getState()).thenReturn(DLSState.OPENED);
        service.currentWeightData.bSendPending = true;
        assertThat(service.getDataCount()).isEqualTo(1);

        service.currentWeightData.bSendPending = false;
        assertThat(service.getDataCount()).isEqualTo(0);
    }

    @Test
    public void getMaximumWeight() throws APosException {
        DLSScaleService service = new DLSScaleService();
        service.device = _scale;
        service.scale = _scale;
        when(_scale.getState()).thenReturn(DLSState.CLOSED);
        try {
            service.getMaximumWeight();
            fail();
        } catch (APosException e) {
            assertThat(e.getErrorCode()).isEqualTo(ErrorConstants.APOS_E_CLOSED);
        }

        when(_scale.getState()).thenReturn(DLSState.OPENED);
        assertThat(service.getMaximumWeight()).isEqualTo(30000);
    }

    @Test
    public void getMaxDisplayTextChars() throws APosException {
        DLSScaleService service = new DLSScaleService();
        service.device = _scale;
        when(_scale.getState()).thenReturn(DLSState.CLOSED);
        try {
            service.getMaxDisplayTextChars();
            fail();
        } catch (APosException e) {
            assertThat(e.getErrorCode()).isEqualTo(ErrorConstants.APOS_E_CLOSED);
        }

        when(_scale.getState()).thenReturn(DLSState.OPENED);
        assertThat(service.getMaxDisplayTextChars()).isEqualTo(0);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void getPowerState() throws APosException {
        DLSScaleService service = new DLSScaleService();
        service.getPowerState();
    }

    @Test
    public void getSalesPrice() throws APosException {
        DLSScaleService service = new DLSScaleService();
        service.device = _scale;

        when(_scale.getState()).thenReturn(DLSState.CLOSED);
        try {
            service.getSalesPrice();
            fail();
        } catch (APosException e) {
            assertThat(e.getErrorCode()).isEqualTo(ErrorConstants.APOS_E_CLOSED);
        }

        when(_scale.getState()).thenReturn(DLSState.OPENED);
        try {
            service.getSalesPrice();
            fail();
        } catch (APosException e) {
            assertThat(e.getErrorCode()).isEqualTo(ErrorConstants.APOS_E_NOTCLAIMED);
        }

        when(_scale.getState()).thenReturn(DLSState.CLAIMED);
        try {
            service.getSalesPrice();
            fail();
        } catch (APosException e) {
            assertThat(e.getErrorCode()).isEqualTo(ErrorConstants.APOS_E_DISABLED);
        }

        when(_scale.getState()).thenReturn(DLSState.ENABLED);
        assertThat(service.getSalesPrice()).isEqualTo(0);
    }

    @Test
    public void getScaleLiveWeight() throws APosException {
        DLSScaleService service = new DLSScaleService();
        service.device = _scale;
        service.scale = _scale;

        when(_scale.getState()).thenReturn(DLSState.CLOSED);
        try {
            service.getScaleLiveWeight();
            fail();
        } catch (APosException e) {
            assertThat(e.getErrorCode()).isEqualTo(ErrorConstants.APOS_E_CLOSED);
        }

        when(_scale.getState()).thenReturn(DLSState.OPENED);
        try {
            service.getScaleLiveWeight();
            fail();
        } catch (APosException e) {
            assertThat(e.getErrorCode()).isEqualTo(ErrorConstants.APOS_E_NOTCLAIMED);
        }

        when(_scale.getState()).thenReturn(DLSState.CLAIMED);
        try {
            service.getScaleLiveWeight();
            fail();
        } catch (APosException e) {
            assertThat(e.getErrorCode()).isEqualTo(ErrorConstants.APOS_E_DISABLED);
        }

        when(_scale.getState()).thenReturn(DLSState.ENABLED);
        when(_scale.getLiveWeight()).thenReturn(0);
        assertThat(service.getScaleLiveWeight()).isEqualTo(0);

        when(_scale.getLiveWeight()).thenReturn(1);
        assertThat(service.getScaleLiveWeight()).isEqualTo(1);
    }

    @Test
    public void getStatusNotify() throws APosException {
        DLSScaleService service = new DLSScaleService();
        service.device = _scale;
        service.scale = _scale;

        when(_scale.getState()).thenReturn(DLSState.CLOSED);
        try {
            service.getStatusNotify();
            fail();
        } catch (APosException e) {
            assertThat(e.getErrorCode()).isEqualTo(ErrorConstants.APOS_E_CLOSED);
        }

        when(_scale.getState()).thenReturn(DLSState.OPENED);
        when(_scale.getStatusNotify()).thenReturn(0);
        assertThat(service.getStatusNotify()).isEqualTo(0);

        when(_scale.getStatusNotify()).thenReturn(1);
        assertThat(service.getStatusNotify()).isEqualTo(1);
    }

    @Test
    public void setStatusNotify() throws APosException {
        DLSScaleService service = new DLSScaleService();
        service.device = _scale;
        service.scale = _scale;

        when(_scale.getState()).thenReturn(DLSState.CLOSED);
        try {
            service.setStatusNotify(1);
            fail();
        } catch (APosException e) {
            assertThat(e.getErrorCode()).isEqualTo(ErrorConstants.APOS_E_CLOSED);
        }

        when(_scale.canStatusUpdate()).thenReturn(true);
        when(_scale.getState()).thenReturn(DLSState.OPENED);
        service.setStatusNotify(2);
        verify(_scale, times(1)).setStatusNotify(2);

        service.setStatusNotify(3);
        verify(_scale, times(1)).setStatusNotify(3);
    }

    @Test
    public void setStatusNotifyCanNotUpdateStatus() {
        DLSScaleService service = new DLSScaleService();
        service.device = _scale;
        service.scale = _scale;

        when(_scale.getState()).thenReturn(DLSState.CLOSED);
        try {
            service.setStatusNotify(1);
            fail();
        } catch (APosException e) {
            assertThat(e.getErrorCode()).isEqualTo(ErrorConstants.APOS_E_CLOSED);
        }

        when(_scale.getState()).thenReturn(DLSState.OPENED);
        try {
            service.setStatusNotify(2);
            fail();
        } catch (APosException e) {
            assertThat(e.getErrorCode()).isEqualTo(ErrorConstants.APOS_E_ILLEGAL);
        }
    }

    @Test
    public void setStatusNotifyEnabled() {
        DLSScaleService service = new DLSScaleService();
        service.device = _scale;
        service.scale = _scale;

        when(_scale.getState()).thenReturn(DLSState.CLOSED);
        try {
            service.setStatusNotify(1);
            fail();
        } catch (APosException e) {
            assertThat(e.getErrorCode()).isEqualTo(ErrorConstants.APOS_E_CLOSED);
        }

        when(_scale.canStatusUpdate()).thenReturn(true);
        when(_scale.getState()).thenReturn(DLSState.ENABLED);
        try {
            service.setStatusNotify(2);
            fail();
        } catch (APosException e) {
            assertThat(e.getErrorCode()).isEqualTo(ErrorConstants.APOS_E_ILLEGAL);
        }
    }

    @Test
    public void getWeightUnit() throws APosException {
        DLSScaleService service = new DLSScaleService();
        service.device = _scale;
        service.scale = _scale;

        when(_scale.getState()).thenReturn(DLSState.CLOSED);
        try {
            service.getWeightUnit();
            fail();
        } catch (APosException e) {
            assertThat(e.getErrorCode()).isEqualTo(ErrorConstants.APOS_E_CLOSED);
        }

        when(_scale.getState()).thenReturn(DLSState.OPENED);
        assertThat(service.getWeightUnit()).isEqualTo(ScaleConstants.SCAL_WU_POUND);
    }

    @Test
    public void getWeight() throws DLSException, APosException {
        DLSScaleService service = new DLSScaleService();
        service.device = _scale;
        service.scale = _scale;
        service.getWeight();
        verify(_scale, times(1)).readEnglishWeight();

        service.bMetricMode = true;
        service.getWeight();
        verify(_scale, times(1)).readMetricWeight();

        doThrow(new DLSException(2, "fs")).when(_scale).readMetricWeight();
        try {
            service.getWeight();
            fail();
        } catch (APosException e) {
            assertThat(e.getErrorCode()).isEqualTo(ErrorConstants.APOS_E_FAILURE);
        }
    }

    @Test
    public void getWeightAsync() throws APosException {
        DLSScaleService service = new DLSScaleService();
        service.device = _scale;
        service.scale = _scale;
        service.getWeightAsync(100);
    }

    @Test
    public void getWeightAsyncNegative() throws APosException {
        DLSScaleService service = new DLSScaleService();
        service.device = _scale;
        service.scale = _scale;
        service.getWeightAsync(-1);
    }

    //region getWeightSync
    @Test
    public void getWeightSyncAbort() throws APosException, DLSException {
        DLSScaleService service = new DLSScaleService();
        service.scale = _scale;
        service.device = _scale;
        int[] data = new int[]{1};
        doAnswer(invocation -> {
            service.currentWeightData.nState = DLSScaleService.CMD_ABORT;
            return null;
        }).when(_scale).readEnglishWeight();

        service.getWeightSync(data, 2);
    }

    @Test
    public void getWeightSyncAbortNoTimeout() throws APosException, DLSException {
        DLSScaleService service = new DLSScaleService();
        service.scale = _scale;
        service.device = _scale;
        int[] data = new int[]{1};
        doAnswer(invocation -> {
            service.currentWeightData.nState = DLSScaleService.CMD_ABORT;
            return null;
        }).when(_scale).readEnglishWeight();

        service.getWeightSync(data, 0);
    }

    @Test
    public void getWeightSyncPending() throws DLSException {
        DLSScaleService service = new DLSScaleService();
        service.scale = _scale;
        service.device = _scale;
        int[] data = new int[]{1};
        doAnswer(invocation -> {
            service.currentWeightData.nState = DLSScaleService.CMD_PENDING;
            return null;
        }).when(_scale).readEnglishWeight();

        try {
            service.getWeightSync(data, 2);
            fail();
        } catch (APosException e) {
            assertThat(e.getErrorCode()).isEqualTo(ErrorConstants.APOS_E_TIMEOUT);
        }
    }

    @Test
    public void getWeightSyncZero() throws DLSException {
        DLSScaleService service = new DLSScaleService();
        service.scale = _scale;
        service.device = _scale;
        int[] data = new int[]{1};
        doAnswer(invocation -> {
            service.currentWeightData.nState = DLSScaleService.CMD_ZERO;
            return null;
        }).when(_scale).readEnglishWeight();

        try {
            service.getWeightSync(data, 2);
            fail();
        } catch (APosException e) {
            assertThat(e.getErrorCode()).isEqualTo(DLSScaleService.SCALE_NEEDS_ZEROING);
        }
    }

    @Test
    public void getWeightSyncAtZero() throws DLSException {
        DLSScaleService service = new DLSScaleService();
        service.scale = _scale;
        service.device = _scale;
        int[] data = new int[]{1};
        doAnswer(invocation -> {
            service.currentWeightData.nState = DLSScaleService.CMD_AT_ZERO;
            return null;
        }).when(_scale).readEnglishWeight();

        try {
            service.getWeightSync(data, 2);
            fail();
        } catch (APosException e) {
            assertThat(e.getErrorCode()).isEqualTo(DLSScaleService.SCALE_AT_ZERO);
        }
    }

    @Test
    public void getWeightSyncMotion() throws DLSException {
        DLSScaleService service = new DLSScaleService();
        service.scale = _scale;
        service.device = _scale;
        int[] data = new int[]{1};
        doAnswer(invocation -> {
            service.currentWeightData.nState = DLSScaleService.CMD_MOTION;
            return null;
        }).when(_scale).readEnglishWeight();

        try {
            service.getWeightSync(data, 2);
            fail();
        } catch (APosException e) {
            assertThat(e.getErrorCode()).isEqualTo(DLSScaleService.SCALE_IN_MOTION);
        }
    }

    @Test
    public void getWeightSyncOverweight() throws DLSException {
        DLSScaleService service = new DLSScaleService();
        service.scale = _scale;
        service.device = _scale;
        int[] data = new int[]{1};
        doAnswer(invocation -> {
            service.currentWeightData.nState = DLSScaleService.CMD_OVERWEIGHT;
            return null;
        }).when(_scale).readEnglishWeight();

        try {
            service.getWeightSync(data, 2);
            fail();
        } catch (APosException e) {
            assertThat(e.getErrorCode()).isEqualTo(DLSScaleService.SCALE_OVERWEIGHT);
        }
    }

    @Test
    public void getWeightSyncUnderZero() throws DLSException {
        DLSScaleService service = new DLSScaleService();
        service.scale = _scale;
        service.device = _scale;
        int[] data = new int[]{1};
        doAnswer(invocation -> {
            service.currentWeightData.nState = DLSScaleService.CMD_UNDERZERO;
            return null;
        }).when(_scale).readEnglishWeight();

        try {
            service.getWeightSync(data, 2);
            fail();
        } catch (APosException e) {
            assertThat(e.getErrorCode()).isEqualTo(DLSScaleService.SCALE_UNDERZERO);
        }
    }

    @Test
    public void getWeightSyncUnknown() throws DLSException {
        DLSScaleService service = new DLSScaleService();
        service.scale = _scale;
        service.device = _scale;
        int[] data = new int[]{1};
        doAnswer(invocation -> {
            service.currentWeightData.nState = -13;
            return null;
        }).when(_scale).readEnglishWeight();

        try {
            service.getWeightSync(data, 2);
            fail();
        } catch (APosException e) {
            assertThat(e.getErrorCode()).isEqualTo(-13);
        }
    }

    //endregion


    //region onDeviceError
    @Test
    public void onDeviceErrorNoWeight() {
        DLSScaleService service = new DLSScaleService();
        service.device = _scale;
        service.scale = _scale;
        service.bExceptionOnMotion = true;
        service.onDeviceError(DeviceErrorStatusListener.ERR_NO_WEIGHT);
        assertThat(service.currentWeightData.nState).isEqualTo(DLSScaleService.CMD_MOTION);
        assertThat(service.currentWeightData.nEndTime).isEqualTo(0);
        assertThat(service.currentWeightData.bSendPending).isFalse();
        assertThat(service.currentWeightData.bRetry).isFalse();
    }

    @Test
    public void onDeviceErrorAtZero() {
        DLSScaleService service = new DLSScaleService();
        service.device = _scale;
        service.scale = _scale;
        service.bExceptionOnMotion = true;
        service.onDeviceError(DeviceErrorStatusListener.ERR_SCALE_AT_ZERO);
        assertThat(service.currentWeightData.nState).isEqualTo(DLSScaleService.CMD_AT_ZERO);
        assertThat(service.currentWeightData.nEndTime).isEqualTo(0);
        assertThat(service.currentWeightData.bSendPending).isFalse();
        assertThat(service.currentWeightData.bRetry).isFalse();
    }

    @Test
    public void onDeviceErrorCmdPendingAsync() throws APosException {
        DLSScaleService service = new DLSScaleService();
        service.device = _scale;
        service.scale = _scale;
        when(_scale.getState()).thenReturn(DLSState.OPENED);
        service.currentWeightData.nState = DLSScaleService.CMD_PENDING;
        service.setAsyncMode(true);
        service.onDeviceError(DeviceErrorStatusListener.ERR_NO_WEIGHT);
        assertThat(service.currentWeightData.nState).isEqualTo(DLSScaleService.CMD_TIMEOUT);
        assertThat(service.currentWeightData.nEndTime).isEqualTo(0);
        assertThat(service.currentWeightData.bSendPending).isFalse();
        assertThat(service.currentWeightData.bRetry).isFalse();
    }

    @Test
    public void onDeviceErrorCmdPendingSync() {
        DLSScaleService service = new DLSScaleService();
        service.device = _scale;
        service.scale = _scale;
        when(_scale.getState()).thenReturn(DLSState.OPENED);
        service.currentWeightData.nState = DLSScaleService.CMD_PENDING;
        service.onDeviceError(DeviceErrorStatusListener.ERR_NO_WEIGHT);
        assertThat(service.currentWeightData.nState).isEqualTo(DLSScaleService.CMD_PENDING);
        assertThat(service.currentWeightData.nEndTime).isEqualTo(0);
        assertThat(service.currentWeightData.bSendPending).isFalse();
        assertThat(service.currentWeightData.bRetry).isFalse();
    }

    @Test
    public void onDeviceErrorCmdPendingSyncFailure() throws DLSException {
        DLSScaleService service = new DLSScaleService();
        service.device = _scale;
        service.scale = _scale;
        when(_scale.getState()).thenReturn(DLSState.OPENED);
        service.currentWeightData.nState = DLSScaleService.CMD_PENDING;
        doThrow(new DLSException(12, "re")).when(_scale).readEnglishWeight();
        service.onDeviceError(DeviceErrorStatusListener.ERR_NO_WEIGHT);
        assertThat(service.currentWeightData.nState).isEqualTo(ErrorConstants.APOS_E_FAILURE);
        assertThat(service.currentWeightData.nEndTime).isEqualTo(0);
        assertThat(service.currentWeightData.bSendPending).isFalse();
        assertThat(service.currentWeightData.bRetry).isFalse();
    }

    @Test
    public void onDeviceErrorCmdPendingErrorCommand() {
        DLSScaleService service = new DLSScaleService();
        BaseControl control = mock(BaseControl.class);
        EventCallback callback = new EventCallback() {
            @Override
            public void fireEvent(BaseEvent event, EventType type) {
                assertThat(event instanceof ErrorEvent).isTrue();
                assertThat(type).isEqualTo(EventCallback.EventType.Error);
                assertThat(event.getSource()).isEqualTo(service);
                assertThat(((ErrorEvent) event).getErrorCode()).isEqualTo(ErrorConstants.APOS_E_FAILURE);
                assertThat(((ErrorEvent) event).getErrorCodeExtended()).isEqualTo(DLSScaleService.ERR_CMD);
                assertThat(((ErrorEvent) event).getErrorLocus()).isEqualTo(ErrorConstants.APOS_EL_OUTPUT);
                assertThat(((ErrorEvent) event).getErrorResponse()).isEqualTo(ErrorConstants.APOS_ER_CLEAR);
            }

            @Override
            public BaseControl getEventSource() {
                return control;
            }
        };
        service.setEventCallbacks(callback);
        service.onDeviceError(DeviceErrorStatusListener.ERR_CMD);
    }

    @Test
    public void onDeviceErrorCmdPendingErrorData() {
        DLSScaleService service = new DLSScaleService();
        BaseControl control = mock(BaseControl.class);
        EventCallback callback = new EventCallback() {
            @Override
            public void fireEvent(BaseEvent event, EventType type) {
                assertThat(event instanceof ErrorEvent).isTrue();
                assertThat(type).isEqualTo(EventCallback.EventType.Error);
                assertThat(event.getSource()).isEqualTo(service);
                assertThat(((ErrorEvent) event).getErrorCode()).isEqualTo(ErrorConstants.APOS_E_FAILURE);
                assertThat(((ErrorEvent) event).getErrorCodeExtended()).isEqualTo(DLSScaleService.ERR_DATA);
                assertThat(((ErrorEvent) event).getErrorLocus()).isEqualTo(ErrorConstants.APOS_EL_OUTPUT);
                assertThat(((ErrorEvent) event).getErrorResponse()).isEqualTo(ErrorConstants.APOS_ER_CLEAR);
            }

            @Override
            public BaseControl getEventSource() {
                return control;
            }
        };
        service.setEventCallbacks(callback);
        service.onDeviceError(DeviceErrorStatusListener.ERR_DATA);
    }

    @Test
    public void onDeviceErrorCmdPendingErrorRead() {
        DLSScaleService service = new DLSScaleService();
        BaseControl control = mock(BaseControl.class);
        EventCallback callback = new EventCallback() {
            @Override
            public void fireEvent(BaseEvent event, EventType type) {
                assertThat(event instanceof ErrorEvent).isTrue();
                assertThat(type).isEqualTo(EventCallback.EventType.Error);
                assertThat(event.getSource()).isEqualTo(service);
                assertThat(((ErrorEvent) event).getErrorCode()).isEqualTo(ErrorConstants.APOS_E_FAILURE);
                assertThat(((ErrorEvent) event).getErrorCodeExtended()).isEqualTo(DLSScaleService.ERR_READ);
                assertThat(((ErrorEvent) event).getErrorLocus()).isEqualTo(ErrorConstants.APOS_EL_INPUT);
                assertThat(((ErrorEvent) event).getErrorResponse()).isEqualTo(ErrorConstants.APOS_ER_CLEAR);
            }

            @Override
            public BaseControl getEventSource() {
                return control;
            }
        };
        service.setEventCallbacks(callback);
        service.onDeviceError(DeviceErrorStatusListener.ERR_READ);
    }

    @Test
    public void onDeviceErrorCmdPendingErrorNoDisplay() {
        DLSScaleService service = new DLSScaleService();
        BaseControl control = mock(BaseControl.class);
        EventCallback callback = new EventCallback() {
            @Override
            public void fireEvent(BaseEvent event, EventType type) {
                assertThat(event instanceof ErrorEvent).isTrue();
                assertThat(type).isEqualTo(EventCallback.EventType.Error);
                assertThat(event.getSource()).isEqualTo(service);
                assertThat(((ErrorEvent) event).getErrorCode()).isEqualTo(ErrorConstants.APOS_E_NOHARDWARE);
                assertThat(((ErrorEvent) event).getErrorCodeExtended()).isEqualTo(DLSScaleService.ERR_NO_DISPLAY);
                assertThat(((ErrorEvent) event).getErrorLocus()).isEqualTo(ErrorConstants.APOS_EL_INPUT);
                assertThat(((ErrorEvent) event).getErrorResponse()).isEqualTo(ErrorConstants.APOS_ER_CLEAR);
            }

            @Override
            public BaseControl getEventSource() {
                return control;
            }
        };
        service.setEventCallbacks(callback);
        service.onDeviceError(DeviceErrorStatusListener.ERR_NO_DISPLAY);
    }

    @Test
    public void onDeviceErrorCmdPendingErrorNoHardware() {
        DLSScaleService service = new DLSScaleService();
        BaseControl control = mock(BaseControl.class);
        EventCallback callback = new EventCallback() {
            @Override
            public void fireEvent(BaseEvent event, EventType type) {
                assertThat(event instanceof ErrorEvent).isTrue();
                assertThat(type).isEqualTo(EventCallback.EventType.Error);
                assertThat(event.getSource()).isEqualTo(service);
                assertThat(((ErrorEvent) event).getErrorCode()).isEqualTo(ErrorConstants.APOS_E_FAILURE);
                assertThat(((ErrorEvent) event).getErrorCodeExtended()).isEqualTo(DLSScaleService.ERR_HARDWARE);
                assertThat(((ErrorEvent) event).getErrorLocus()).isEqualTo(ErrorConstants.APOS_EL_INPUT);
                assertThat(((ErrorEvent) event).getErrorResponse()).isEqualTo(ErrorConstants.APOS_ER_CLEAR);
            }

            @Override
            public BaseControl getEventSource() {
                return control;
            }
        };
        service.setEventCallbacks(callback);
        service.onDeviceError(DeviceErrorStatusListener.ERR_HARDWARE);
    }

    @Test
    public void onDeviceErrorCmdPendingErrorCommandReject() {
        DLSScaleService service = new DLSScaleService();
        BaseControl control = mock(BaseControl.class);
        EventCallback callback = new EventCallback() {
            @Override
            public void fireEvent(BaseEvent event, EventType type) {
                assertThat(event instanceof ErrorEvent).isTrue();
                assertThat(type).isEqualTo(EventCallback.EventType.Error);
                assertThat(event.getSource()).isEqualTo(service);
                assertThat(((ErrorEvent) event).getErrorCode()).isEqualTo(ErrorConstants.APOS_E_FAILURE);
                assertThat(((ErrorEvent) event).getErrorCodeExtended()).isEqualTo(DLSScaleService.ERR_CMD_REJECT);
                assertThat(((ErrorEvent) event).getErrorLocus()).isEqualTo(ErrorConstants.APOS_EL_OUTPUT);
                assertThat(((ErrorEvent) event).getErrorResponse()).isEqualTo(ErrorConstants.APOS_ER_CLEAR);
            }

            @Override
            public BaseControl getEventSource() {
                return control;
            }
        };
        service.setEventCallbacks(callback);
        service.onDeviceError(DeviceErrorStatusListener.ERR_CMD_REJECT);
    }

    @Test
    public void onDeviceErrorCmdPendingErrorCapacity() {
        DLSScaleService service = new DLSScaleService();
        BaseControl control = mock(BaseControl.class);
        EventCallback callback = new EventCallback() {
            @Override
            public void fireEvent(BaseEvent event, EventType type) {
                assertThat(event instanceof ErrorEvent).isTrue();
                assertThat(type).isEqualTo(EventCallback.EventType.Error);
                assertThat(event.getSource()).isEqualTo(service);
                assertThat(((ErrorEvent) event).getErrorCode()).isEqualTo(ErrorConstants.APOS_ESCAL_OVERWEIGHT);
                assertThat(((ErrorEvent) event).getErrorCodeExtended()).isEqualTo(DLSScaleService.ERR_CAPACITY);
                assertThat(((ErrorEvent) event).getErrorLocus()).isEqualTo(ErrorConstants.APOS_EL_INPUT);
                assertThat(((ErrorEvent) event).getErrorResponse()).isEqualTo(ErrorConstants.APOS_ER_CLEAR);
            }

            @Override
            public BaseControl getEventSource() {
                return control;
            }
        };
        service.setEventCallbacks(callback);
        service.onDeviceError(DeviceErrorStatusListener.ERR_CAPACITY);
        assertThat(service.currentWeightData.nState).isEqualTo(DLSScaleService.CMD_OVERWEIGHT);
    }

    @Test
    public void onDeviceErrorCmdPendingErrorUnderZero() {
        DLSScaleService service = new DLSScaleService();
        BaseControl control = mock(BaseControl.class);
        EventCallback callback = new EventCallback() {
            @Override
            public void fireEvent(BaseEvent event, EventType type) {
                assertThat(event instanceof ErrorEvent).isTrue();
                assertThat(type).isEqualTo(EventCallback.EventType.Error);
                assertThat(event.getSource()).isEqualTo(service);
                assertThat(((ErrorEvent) event).getErrorCode()).isEqualTo(ErrorConstants.APOS_ESCAL_UNDER_ZERO);
                assertThat(((ErrorEvent) event).getErrorCodeExtended()).isEqualTo(DLSScaleService.ERR_SCALE_UNDER_ZERO);
                assertThat(((ErrorEvent) event).getErrorLocus()).isEqualTo(ErrorConstants.APOS_EL_INPUT);
                assertThat(((ErrorEvent) event).getErrorResponse()).isEqualTo(ErrorConstants.APOS_ER_CLEAR);
            }

            @Override
            public BaseControl getEventSource() {
                return control;
            }
        };
        service.setEventCallbacks(callback);
        service.onDeviceError(DeviceErrorStatusListener.ERR_SCALE_UNDER_ZERO);
        assertThat(service.currentWeightData.nState).isEqualTo(DLSScaleService.CMD_UNDERZERO);
    }

    @Test
    public void onDeviceErrorCmdPendingErrorRequiresZeroing() {
        DLSScaleService service = new DLSScaleService();
        BaseControl control = mock(BaseControl.class);
        EventCallback callback = new EventCallback() {
            @Override
            public void fireEvent(BaseEvent event, EventType type) {
                assertThat(event instanceof ErrorEvent).isTrue();
                assertThat(type).isEqualTo(EventCallback.EventType.Error);
                assertThat(event.getSource()).isEqualTo(service);
                assertThat(((ErrorEvent) event).getErrorCode()).isEqualTo(ErrorConstants.APOS_E_FAILURE);
                assertThat(((ErrorEvent) event).getErrorCodeExtended()).isEqualTo(DLSScaleService.ERR_REQUIRES_ZEROING);
                assertThat(((ErrorEvent) event).getErrorLocus()).isEqualTo(ErrorConstants.APOS_EL_INPUT);
                assertThat(((ErrorEvent) event).getErrorResponse()).isEqualTo(ErrorConstants.APOS_ER_CLEAR);
            }

            @Override
            public BaseControl getEventSource() {
                return control;
            }
        };
        service.setEventCallbacks(callback);
        service.onDeviceError(DeviceErrorStatusListener.ERR_REQUIRES_ZEROING);
        assertThat(service.currentWeightData.nState).isEqualTo(DLSScaleService.CMD_ZERO);
    }

    @Test
    public void onDeviceErrorCmdPendingErrorWarmup() {
        DLSScaleService service = new DLSScaleService();
        BaseControl control = mock(BaseControl.class);
        EventCallback callback = new EventCallback() {
            @Override
            public void fireEvent(BaseEvent event, EventType type) {
                assertThat(event instanceof ErrorEvent).isTrue();
                assertThat(type).isEqualTo(EventCallback.EventType.Error);
                assertThat(event.getSource()).isEqualTo(service);
                assertThat(((ErrorEvent) event).getErrorCode()).isEqualTo(ErrorConstants.APOS_E_FAILURE);
                assertThat(((ErrorEvent) event).getErrorCodeExtended()).isEqualTo(DLSScaleService.ERR_WARMUP);
                assertThat(((ErrorEvent) event).getErrorLocus()).isEqualTo(ErrorConstants.APOS_EL_INPUT);
                assertThat(((ErrorEvent) event).getErrorResponse()).isEqualTo(ErrorConstants.APOS_ER_CLEAR);
            }

            @Override
            public BaseControl getEventSource() {
                return control;
            }
        };
        service.setEventCallbacks(callback);
        service.onDeviceError(DeviceErrorStatusListener.ERR_WARMUP);
    }

    @Test
    public void onDeviceErrorCmdPendingErrorDuplicate() {
        DLSScaleService service = new DLSScaleService();
        BaseControl control = mock(BaseControl.class);
        EventCallback callback = new EventCallback() {
            @Override
            public void fireEvent(BaseEvent event, EventType type) {
                assertThat(event instanceof ErrorEvent).isTrue();
                assertThat(type).isEqualTo(EventCallback.EventType.Error);
                assertThat(event.getSource()).isEqualTo(service);
                assertThat(((ErrorEvent) event).getErrorCode()).isEqualTo(ErrorConstants.APOS_E_FAILURE);
                assertThat(((ErrorEvent) event).getErrorCodeExtended()).isEqualTo(DLSScaleService.ERR_DUPLICATE);
                assertThat(((ErrorEvent) event).getErrorLocus()).isEqualTo(ErrorConstants.APOS_EL_INPUT);
                assertThat(((ErrorEvent) event).getErrorResponse()).isEqualTo(ErrorConstants.APOS_ER_CLEAR);
            }

            @Override
            public BaseControl getEventSource() {
                return control;
            }
        };
        service.setEventCallbacks(callback);
        service.onDeviceError(DeviceErrorStatusListener.ERR_DUPLICATE);
    }

    @Test
    public void onDeviceErrorCmdPendingErrorDeviceRemoved() {
        DLSScaleService service = new DLSScaleService();
        BaseControl control = mock(BaseControl.class);
        EventCallback callback = new EventCallback() {
            @Override
            public void fireEvent(BaseEvent event, EventType type) {
                assertThat(event instanceof ErrorEvent).isTrue();
                assertThat(type).isEqualTo(EventCallback.EventType.Error);
                assertThat(event.getSource()).isEqualTo(service);
                assertThat(((ErrorEvent) event).getErrorCode()).isEqualTo(ErrorConstants.APOS_E_NOHARDWARE);
                assertThat(((ErrorEvent) event).getErrorCodeExtended()).isEqualTo(DLSScaleService.ERR_DEVICE_REMOVED);
                assertThat(((ErrorEvent) event).getErrorLocus()).isEqualTo(ErrorConstants.APOS_EL_INPUT);
                assertThat(((ErrorEvent) event).getErrorResponse()).isEqualTo(ErrorConstants.APOS_ER_CLEAR);
            }

            @Override
            public BaseControl getEventSource() {
                return control;
            }
        };
        service.setEventCallbacks(callback);
        service.onDeviceError(DeviceErrorStatusListener.ERR_DEVICE_REMOVED);
    }
    //endregion

    @Test
    public void onDeviceStatus() {
        DLSScaleService service = new DLSScaleService();
        service.device = _scale;
        service.scale = _scale;
        BaseControl control = mock(BaseControl.class);
        EventCallback callback = new EventCallback() {
            @Override
            public void fireEvent(BaseEvent event, EventType type) {
                assertThat(event instanceof StatusUpdateEvent).isTrue();
                assertThat(type).isEqualTo(EventCallback.EventType.StatusUpdate);
                assertThat(((StatusUpdateEvent) event).getStatus()).isEqualTo(23);
                assertThat(event.getSource()).isEqualTo(control);
            }

            @Override
            public BaseControl getEventSource() {
                return control;
            }
        };
        service.setEventCallbacks(callback);
        service.onDeviceStatus(12);

        when(_scale.getStatusNotify()).thenReturn(ScaleConstants.SCAL_SN_ENABLED);
        service.onDeviceStatus(23);
    }

    //region onWeightReceived
    @Test
    public void onWeightReceived() {
        DLSScaleService service = new DLSScaleService();
        service.device = _scale;
        service.onWeightReceived(25);
        assertThat(service.currentWeightData.nWeight).isEqualTo(25);
        assertThat(service.currentWeightData.nState).isEqualTo(DLSScaleService.CMD_COMPLETE);
        assertThat(service.currentWeightData.bSendPending).isTrue();
        assertThat(service.currentWeightData.bRetry).isFalse();
    }

    @Test
    public void onWeightReceivedTimeout() throws APosException {
        DLSScaleService service = new DLSScaleService();
        service.device = _scale;
        service.currentWeightData.nEndTime = 12;
        when(_scale.getState()).thenReturn(DLSState.OPENED);
        service.setAsyncMode(true);
        service.onWeightReceived(25);
        assertThat(service.currentWeightData.nWeight).isEqualTo(25);
        assertThat(service.currentWeightData.nState).isEqualTo(DLSScaleService.CMD_TIMEOUT);
        assertThat(service.currentWeightData.bSendPending).isTrue();
        assertThat(service.currentWeightData.bRetry).isFalse();
    }

    @Test
    public void onWeightReceivedAsync() throws APosException {
        DLSScaleService service = new DLSScaleService();
        service.device = _scale;
        when(_scale.getState()).thenReturn(DLSState.OPENED);
        service.setAsyncMode(true);
        service.onWeightReceived(25);
        assertThat(service.currentWeightData.nWeight).isEqualTo(25);
        assertThat(service.currentWeightData.nState).isEqualTo(DLSScaleService.CMD_COMPLETE);
        assertThat(service.currentWeightData.bSendPending).isTrue();
        assertThat(service.currentWeightData.bRetry).isFalse();
    }

    @Test
    public void onWeightReceivedAutoDisable() throws APosException {
        DLSScaleService service = new DLSScaleService();
        service.device = _scale;
        when(_scale.getState()).thenReturn(DLSState.OPENED);
        service.setAutoDisable(true);
        service.onWeightReceived(25);
        assertThat(service.currentWeightData.nWeight).isEqualTo(25);
        assertThat(service.currentWeightData.nState).isEqualTo(DLSScaleService.CMD_COMPLETE);
        assertThat(service.currentWeightData.bSendPending).isTrue();
        assertThat(service.currentWeightData.bRetry).isFalse();
    }
    //endregion

    //region Open
    @Test
    public void openPoundsNullConfig() throws APosException {
        DLAPosConfigHelper helper = mock(DLAPosConfigHelper.class);
        MockUtils.mockConfigHelperSingleton(helper, mock(ProfileManager.class));
        MockUtils.mockDLSPropertiesSingleton(mock(DLSProperties.class));
        DLSScaleService service = new DLSScaleService();
        EventCallback callback = mock(EventCallback.class);
        try (MockedStatic<DLSObjectFactory> factory = Mockito.mockStatic(DLSObjectFactory.class)) {
            factory.when(() -> DLSObjectFactory.createScale("Test", _context)).thenReturn(_scale);
            service.open("Test", callback, _context);
            assertThat(service.options).isNotNull();
            assertThat(service.getLogicalName()).isEqualTo("Test");
            assertThat(service.getEventCallbacks()).isEqualTo(callback);
            assertThat(service.context).isEqualTo(_context);
            assertThat(service.getState()).isEqualTo(CommonsConstants.S_IDLE);
            assertThat(service.getMaximumWeight()).isEqualTo(30000);
            assertThat(service.getWeightUnit()).isEqualTo(ScaleConstants.SCAL_WU_POUND);
        }
        MockUtils.cleanDLSProperties();
        MockUtils.cleanConfigHelper();
    }

    @Test
    public void openPounds5Digits() throws APosException {
        DLAPosConfigHelper helper = mock(DLAPosConfigHelper.class);
        MockUtils.mockConfigHelperSingleton(helper, mock(ProfileManager.class));
        MockUtils.mockDLSPropertiesSingleton(mock(DLSProperties.class));
        DLSScaleService service = new DLSScaleService();
        EventCallback callback = mock(EventCallback.class);
        DLSScaleConfig config = mock(DLSScaleConfig.class);
        when(_scale.getConfiguration()).thenReturn(config);
        when(config.getFiveDigitWeight()).thenReturn(true);
        try (MockedStatic<DLSObjectFactory> factory = Mockito.mockStatic(DLSObjectFactory.class)) {
            factory.when(() -> DLSObjectFactory.createScale("Test", _context)).thenReturn(_scale);
            service.open("Test", callback, _context);
            assertThat(service.options).isNotNull();
            assertThat(service.getLogicalName()).isEqualTo("Test");
            assertThat(service.getEventCallbacks()).isEqualTo(callback);
            assertThat(service.context).isEqualTo(_context);
            assertThat(service.getState()).isEqualTo(CommonsConstants.S_IDLE);
            assertThat(service.getMaximumWeight()).isEqualTo(30000);
            assertThat(service.getWeightUnit()).isEqualTo(ScaleConstants.SCAL_WU_POUND);
        }
        MockUtils.cleanDLSProperties();
        MockUtils.cleanConfigHelper();
    }

    @Test
    public void openPounds4Digits() throws APosException {
        DLAPosConfigHelper helper = mock(DLAPosConfigHelper.class);
        MockUtils.mockConfigHelperSingleton(helper, mock(ProfileManager.class));
        MockUtils.mockDLSPropertiesSingleton(mock(DLSProperties.class));
        DLSScaleService service = new DLSScaleService();
        EventCallback callback = mock(EventCallback.class);
        DLSScaleConfig config = mock(DLSScaleConfig.class);
        when(_scale.getConfiguration()).thenReturn(config);
        when(config.getFiveDigitWeight()).thenReturn(false);
        try (MockedStatic<DLSObjectFactory> factory = Mockito.mockStatic(DLSObjectFactory.class)) {
            factory.when(() -> DLSObjectFactory.createScale("Test", _context)).thenReturn(_scale);
            service.open("Test", callback, _context);
            assertThat(service.options).isNotNull();
            assertThat(service.getLogicalName()).isEqualTo("Test");
            assertThat(service.getEventCallbacks()).isEqualTo(callback);
            assertThat(service.context).isEqualTo(_context);
            assertThat(service.getState()).isEqualTo(CommonsConstants.S_IDLE);
            assertThat(service.getMaximumWeight()).isEqualTo(3000);
            assertThat(service.getWeightUnit()).isEqualTo(ScaleConstants.SCAL_WU_POUND);
        }
        MockUtils.cleanDLSProperties();
        MockUtils.cleanConfigHelper();
    }

    @Test
    public void openMetric5Digits() throws APosException {
        DLAPosConfigHelper helper = mock(DLAPosConfigHelper.class);
        MockUtils.mockConfigHelperSingleton(helper, mock(ProfileManager.class));
        MockUtils.mockDLSPropertiesSingleton(mock(DLSProperties.class));
        DLSScaleService service = new DLSScaleService();
        EventCallback callback = mock(EventCallback.class);
        DLSScaleConfig config = mock(DLSScaleConfig.class);
        when(_scale.getConfiguration()).thenReturn(config);
        when(config.getMetricWeightMode()).thenReturn(true);
        when(config.getFiveDigitWeight()).thenReturn(true);
        try (MockedStatic<DLSObjectFactory> factory = Mockito.mockStatic(DLSObjectFactory.class)) {
            factory.when(() -> DLSObjectFactory.createScale("Test", _context)).thenReturn(_scale);
            service.open("Test", callback, _context);
            assertThat(service.options).isNotNull();
            assertThat(service.getLogicalName()).isEqualTo("Test");
            assertThat(service.getEventCallbacks()).isEqualTo(callback);
            assertThat(service.context).isEqualTo(_context);
            assertThat(service.getState()).isEqualTo(CommonsConstants.S_IDLE);
            assertThat(service.getMaximumWeight()).isEqualTo(15000);
            assertThat(service.getWeightUnit()).isEqualTo(ScaleConstants.SCAL_WU_KILOGRAM);
        }
        MockUtils.cleanDLSProperties();
        MockUtils.cleanConfigHelper();
    }

    @Test
    public void openMetric4Digits() throws APosException {
        DLAPosConfigHelper helper = mock(DLAPosConfigHelper.class);
        MockUtils.mockConfigHelperSingleton(helper, mock(ProfileManager.class));
        MockUtils.mockDLSPropertiesSingleton(mock(DLSProperties.class));
        DLSScaleService service = new DLSScaleService();
        EventCallback callback = mock(EventCallback.class);
        DLSScaleConfig config = mock(DLSScaleConfig.class);
        when(_scale.getConfiguration()).thenReturn(config);
        when(config.getMetricWeightMode()).thenReturn(true);
        when(config.getFiveDigitWeight()).thenReturn(false);
        try (MockedStatic<DLSObjectFactory> factory = Mockito.mockStatic(DLSObjectFactory.class)) {
            factory.when(() -> DLSObjectFactory.createScale("Test", _context)).thenReturn(_scale);
            service.open("Test", callback, _context);
            assertThat(service.options).isNotNull();
            assertThat(service.getLogicalName()).isEqualTo("Test");
            assertThat(service.getEventCallbacks()).isEqualTo(callback);
            assertThat(service.context).isEqualTo(_context);
            assertThat(service.getState()).isEqualTo(CommonsConstants.S_IDLE);
            assertThat(service.getMaximumWeight()).isEqualTo(1500);
            assertThat(service.getWeightUnit()).isEqualTo(ScaleConstants.SCAL_WU_KILOGRAM);
        }
        MockUtils.cleanDLSProperties();
        MockUtils.cleanConfigHelper();
    }

    @Test
    public void openMetricException() throws DLSException {
        DLAPosConfigHelper helper = mock(DLAPosConfigHelper.class);
        MockUtils.mockConfigHelperSingleton(helper, mock(ProfileManager.class));
        MockUtils.mockDLSPropertiesSingleton(mock(DLSProperties.class));
        DLSScaleService service = new DLSScaleService();
        EventCallback callback = mock(EventCallback.class);
        doThrow(new DLSException(3, "fd")).when(_scale).open("Test", _context);
        try (MockedStatic<DLSObjectFactory> factory = Mockito.mockStatic(DLSObjectFactory.class)) {
            factory.when(() -> DLSObjectFactory.createScale("Test", _context)).thenReturn(_scale);
            service.open("Test", callback, _context);
            fail();
        } catch (APosException e) {
            assertThat(e.getErrorCode()).isEqualTo(ErrorConstants.APOS_E_FAILURE);
        }
        MockUtils.cleanDLSProperties();
        MockUtils.cleanConfigHelper();
    }
    //endregion

    @Test
    public void readLiveWeightWithTare() {
        DLSScaleService service = new DLSScaleService();
        try {
            service.readLiveWeightWithTare(null, null, 0);
            fail();
        } catch (APosException e) {
            assertThat(e.getErrorCode()).isEqualTo(ErrorConstants.APOS_E_ILLEGAL);
        }
    }

    @Test
    public void readWeight() throws APosException, DLSException {
        DLSScaleService service = new DLSScaleService();
        service.device = _scale;
        service.scale = _scale;
        int[] data = new int[]{1};

        when(_scale.getState()).thenReturn(DLSState.CLOSED);
        try {
            service.readWeight(data, 20);
            fail();
        } catch (APosException e) {
            assertThat(e.getErrorCode()).isEqualTo(ErrorConstants.APOS_E_CLOSED);
        }

        when(_scale.getState()).thenReturn(DLSState.OPENED);
        try {
            service.readWeight(data, 20);
            fail();
        } catch (APosException e) {
            assertThat(e.getErrorCode()).isEqualTo(ErrorConstants.APOS_E_NOTCLAIMED);
        }

        when(_scale.getState()).thenReturn(DLSState.CLAIMED);
        try {
            service.readWeight(data, 20);
            fail();
        } catch (APosException e) {
            assertThat(e.getErrorCode()).isEqualTo(ErrorConstants.APOS_E_DISABLED);
        }

        when(_scale.getState()).thenReturn(DLSState.ENABLED);
        doAnswer(invocation -> {
            service.currentWeightData.nState = DLSScaleService.CMD_ABORT;
            return null;
        }).when(_scale).readEnglishWeight();
        service.readWeight(data, 20);

        service.setAsyncMode(true);
        service.readWeight(data, 20);
    }

    //region Release
    @Test
    public void releaseNotClaimed() {
        DLSScaleService service = new DLSScaleService();
        try {
            service.scale = _scale;
            service.release();
            fail();
        } catch (APosException e) {
            assertThat(e.getErrorCode()).isEqualTo(ErrorConstants.APOS_E_CLOSED);
        }
    }

    @Test
    public void release() throws APosException, DLSException {
        DLSScaleService service = new DLSScaleService();
        service.device = _scale;
        service.scale = _scale;
        when(_scale.getState()).thenReturn(DLSState.CLAIMED);
        service.release();
        verify(_scale, times(1)).release();
        verify(_scale, times(1)).removeDirectIODataListener(service);
        verify(_scale, times(1)).removeDeviceStatusListener(service);
        verify(_scale, times(1)).removeDeviceErrorListener(service);
        verify(_scale, times(1)).removeWeightReceivedListener(service);
    }

    @Test
    public void releaseFail() throws DLSException {
        DLSScaleService service = new DLSScaleService();
        service.device = _scale;
        service.scale = _scale;
        doThrow(DLSException.class).when(_scale).release();
        when(_scale.getState()).thenReturn(DLSState.CLAIMED);
        try {
            service.release();
            fail();
        } catch (APosException e) {
            assertThat(e.getErrorCode()).isEqualTo(ErrorConstants.APOS_E_FAILURE);
        }
        verify(_scale, times(1)).removeDirectIODataListener(service);
        verify(_scale, times(1)).removeDeviceStatusListener(service);
        verify(_scale, times(1)).removeDeviceErrorListener(service);
    }
    //endregion

    @Test
    public void retrieveScaleStatistics() throws APosException, DLSException {
        DLSScaleService service = new DLSScaleService();
        service.device = _scale;
        service.scale = _scale;

        when(_scale.getState()).thenReturn(DLSState.CLOSED);
        try {
            service.retrieveScaleStatistics(new String[]{"1"});
            fail();
        } catch (APosException e) {
            assertThat(e.getErrorCode()).isEqualTo(ErrorConstants.APOS_E_CLOSED);
        }

        when(_scale.getState()).thenReturn(DLSState.OPENED);
        service.retrieveScaleStatistics(new String[]{"1"});

        when(_scale.hasStatisticsReporting()).thenReturn(true);
        try {
            service.retrieveScaleStatistics(new String[]{"1"});
            fail();
        } catch (APosException e) {
            assertThat(e.getErrorCode()).isEqualTo(ErrorConstants.APOS_E_NOTCLAIMED);
        }

        HashMap<String, Object> stats = new HashMap<>();
        when(_scale.getStatistics()).thenReturn(stats);
        when(_scale.getState()).thenReturn(DLSState.CLAIMED);
        service.retrieveScaleStatistics(new String[]{"1"});
        testStatistics(service);
        assertThat(service.statistics.get(DLSJposConst.DLS_S_MODEL_NAME)).isEqualTo("");
        assertThat(service.statistics.get(DLSJposConst.DLS_S_SERIAL_NUMBER)).isEqualTo("");
        assertThat(service.statistics.get(DLSJposConst.DLS_S_FIRMWARE_VERSION)).isEqualTo("");

        service.statistics.put(DLSJposConst.DLS_S_MODEL_NAME, "testModel");
        service.statistics.put(DLSJposConst.DLS_S_SERIAL_NUMBER, "testSerial");
        service.statistics.put(DLSJposConst.DLS_S_FIRMWARE_VERSION, "testFirmware");
        service.retrieveScaleStatistics(new String[]{"1"});
        testStatistics(service);
        assertThat(service.statistics.get(DLSJposConst.DLS_S_MODEL_NAME)).isEqualTo("testModel");
        assertThat(service.statistics.get(DLSJposConst.DLS_S_SERIAL_NUMBER)).isEqualTo("testSerial");
        assertThat(service.statistics.get(DLSJposConst.DLS_S_FIRMWARE_VERSION)).isEqualTo("testFirmware");
    }

    @Test
    public void sendDataEvent() throws APosException {
        DLSScaleService service = new DLSScaleService();
        service.device = _scale;
        service.currentWeightData.bSendPending = true;
        BaseControl control = mock(BaseControl.class);
        EventCallback callback = new EventCallback() {
            @Override
            public void fireEvent(BaseEvent event, EventType type) {
                assertThat(event instanceof DataEvent).isTrue();
                assertThat(type).isEqualTo(EventCallback.EventType.Data);
            }

            @Override
            public BaseControl getEventSource() {
                return control;
            }
        };
        service.setEventCallbacks(callback);
        when(_scale.getState()).thenReturn(DLSState.OPENED);
        service.setDataEventEnabled(true);
        service.sendDataEvent();
        assertThat(service.currentWeightData.bSendPending).isFalse();
    }

    @Test
    public void setPriceCalculationMode() {
        DLSScaleService service = new DLSScaleService();
        try {
            service.setPriceCalculationMode(1);
            fail();
        } catch (APosException e) {
            assertThat(e.getErrorCode()).isEqualTo(ErrorConstants.APOS_E_ILLEGAL);
        }
    }

    @Test
    public void setSpecialTare() {
        DLSScaleService service = new DLSScaleService();
        try {
            service.setSpecialTare(1, 2);
            fail();
        } catch (APosException e) {
            assertThat(e.getErrorCode()).isEqualTo(ErrorConstants.APOS_E_ILLEGAL);
        }
    }

    @Test
    public void setTarePriority() {
        DLSScaleService service = new DLSScaleService();
        try {
            service.setTarePriority(1);
            fail();
        } catch (APosException e) {
            assertThat(e.getErrorCode()).isEqualTo(ErrorConstants.APOS_E_ILLEGAL);
        }
    }

    @Test
    public void setUnitPriceWithWeightUnit() {
        DLSScaleService service = new DLSScaleService();
        try {
            service.setUnitPriceWithWeightUnit(1L, 2, 3, 4);
            fail();
        } catch (APosException e) {
            assertThat(e.getErrorCode()).isEqualTo(ErrorConstants.APOS_E_ILLEGAL);
        }
    }

    @Test
    public void tareWeight() throws APosException {
        DLSScaleService service = new DLSScaleService();
        service.device = _scale;
        when(_scale.getState()).thenReturn(DLSState.CLOSED);
        try {
            service.setTareWeight(10);
            fail();
        } catch (APosException e) {
            assertThat(e.getErrorCode()).isEqualTo(ErrorConstants.APOS_E_CLOSED);
        }
        try {
            service.getTareWeight();
            fail();
        } catch (APosException e) {
            assertThat(e.getErrorCode()).isEqualTo(ErrorConstants.APOS_E_CLOSED);
        }

        when(_scale.getState()).thenReturn(DLSState.OPENED);
        try {
            service.setTareWeight(10);
            fail();
        } catch (APosException e) {
            assertThat(e.getErrorCode()).isEqualTo(ErrorConstants.APOS_E_NOTCLAIMED);
        }
        try {
            service.getTareWeight();
            fail();
        } catch (APosException e) {
            assertThat(e.getErrorCode()).isEqualTo(ErrorConstants.APOS_E_NOTCLAIMED);
        }

        when(_scale.getState()).thenReturn(DLSState.CLAIMED);
        try {
            service.setTareWeight(10);
            fail();
        } catch (APosException e) {
            assertThat(e.getErrorCode()).isEqualTo(ErrorConstants.APOS_E_DISABLED);
        }
        try {
            service.getTareWeight();
            fail();
        } catch (APosException e) {
            assertThat(e.getErrorCode()).isEqualTo(ErrorConstants.APOS_E_DISABLED);
        }

        when(_scale.getState()).thenReturn(DLSState.ENABLED);
        service.setTareWeight(12);
        assertThat(service.getTareWeight()).isEqualTo(12);

        service.setTareWeight(95);
        assertThat(service.getTareWeight()).isEqualTo(95);
    }

    @Test
    public void unitPrice() throws APosException {
        DLSScaleService service = new DLSScaleService();
        service.device = _scale;
        when(_scale.getState()).thenReturn(DLSState.CLOSED);
        try {
            service.setUnitPrice(10);
            fail();
        } catch (APosException e) {
            assertThat(e.getErrorCode()).isEqualTo(ErrorConstants.APOS_E_CLOSED);
        }
        try {
            service.getUnitPrice();
            fail();
        } catch (APosException e) {
            assertThat(e.getErrorCode()).isEqualTo(ErrorConstants.APOS_E_CLOSED);
        }

        when(_scale.getState()).thenReturn(DLSState.OPENED);
        try {
            service.setUnitPrice(10);
            fail();
        } catch (APosException e) {
            assertThat(e.getErrorCode()).isEqualTo(ErrorConstants.APOS_E_NOTCLAIMED);
        }
        try {
            service.getUnitPrice();
            fail();
        } catch (APosException e) {
            assertThat(e.getErrorCode()).isEqualTo(ErrorConstants.APOS_E_NOTCLAIMED);
        }

        when(_scale.getState()).thenReturn(DLSState.CLAIMED);
        try {
            service.setUnitPrice(10);
            fail();
        } catch (APosException e) {
            assertThat(e.getErrorCode()).isEqualTo(ErrorConstants.APOS_E_DISABLED);
        }
        try {
            service.getUnitPrice();
            fail();
        } catch (APosException e) {
            assertThat(e.getErrorCode()).isEqualTo(ErrorConstants.APOS_E_DISABLED);
        }

        when(_scale.getState()).thenReturn(DLSState.ENABLED);
        service.setUnitPrice(12);
        assertThat(service.getUnitPrice()).isEqualTo(12);

        service.setUnitPrice(95);
        assertThat(service.getUnitPrice()).isEqualTo(95);
    }

    @Test
    public void zeroValid() throws APosException {
        DLSScaleService service = new DLSScaleService();
        service.device = _scale;
        service.scale = _scale;
        when(_scale.getState()).thenReturn(DLSState.CLOSED);
        try {
            service.setZeroValid(true);
            fail();
        } catch (APosException e) {
            assertThat(e.getErrorCode()).isEqualTo(ErrorConstants.APOS_E_CLOSED);
        }
        try {
            service.getZeroValid();
            fail();
        } catch (APosException e) {
            assertThat(e.getErrorCode()).isEqualTo(ErrorConstants.APOS_E_CLOSED);
        }

        when(_scale.getState()).thenReturn(DLSState.OPENED);
        doAnswer(invocation -> {
            when(_scale.getZeroValid()).thenReturn(invocation.getArgument(0));
            return null;
        }).when(_scale).setZeroValid(anyBoolean());
        service.setZeroValid(true);
        assertThat(service.getZeroValid()).isTrue();
        service.setZeroValid(false);
        assertThat(service.getZeroValid()).isFalse();
    }

    //region zeroScale
    @Test
    public void zeroScale() throws APosException, DLSException {
        DLSScaleService service = new DLSScaleService();
        service.device = _scale;
        service.scale = _scale;
        when(_scale.getState()).thenReturn(DLSState.CLOSED);
        try {
            service.zeroScale();
            fail();
        } catch (APosException e) {
            assertThat(e.getErrorCode()).isEqualTo(ErrorConstants.APOS_E_CLOSED);
        }

        when(_scale.getState()).thenReturn(DLSState.OPENED);
        try {
            service.zeroScale();
            fail();
        } catch (APosException e) {
            assertThat(e.getErrorCode()).isEqualTo(ErrorConstants.APOS_E_NOTCLAIMED);
        }

        when(_scale.getState()).thenReturn(DLSState.CLAIMED);
        try {
            service.zeroScale();
            fail();
        } catch (APosException e) {
            assertThat(e.getErrorCode()).isEqualTo(ErrorConstants.APOS_E_DISABLED);
        }

        when(_scale.getState()).thenReturn(DLSState.ENABLED);
        when(_scale.canZeroScale()).thenReturn(true);
        service.zeroScale();
        verify(_scale, times(1)).zeroScale();
    }

    @Test
    public void zeroScaleNotSupported() throws DLSException {
        DLSScaleService service = new DLSScaleService();
        service.device = _scale;
        service.scale = _scale;
        when(_scale.getState()).thenReturn(DLSState.ENABLED);
        try {
            service.zeroScale();
            fail();
        } catch (APosException e) {
            assertThat(e.getErrorCode()).isEqualTo(ErrorConstants.APOS_E_ILLEGAL);
        }
        verify(_scale, times(0)).zeroScale();
    }

    @Test
    public void zeroScaleNotFails() throws DLSException {
        DLSScaleService service = new DLSScaleService();
        service.device = _scale;
        service.scale = _scale;
        when(_scale.getState()).thenReturn(DLSState.ENABLED);
        when(_scale.canZeroScale()).thenReturn(true);
        doThrow(new DLSException(2, "fd")).when(_scale).zeroScale();
        try {
            service.zeroScale();
            fail();
        } catch (APosException e) {
            assertThat(e.getErrorCode()).isEqualTo(ErrorConstants.APOS_E_FAILURE);
        }
    }
    //endregion

    private void testStatistics(DLSScaleService service) throws APosException {
        assertThat(service.statistics).isNotEmpty();
        assertThat(service.statistics.get("UnifiedPOSVersion")).isEqualTo(DLSScaleService.VERSION);
        assertThat(service.statistics.containsKey(DLSJposConst.DLS_S_CAP_PWR_REPORT)).isTrue();
        assertThat(service.statistics.get(DLSJposConst.DLS_S_CAP_STATS_REPORT)).isEqualTo(service.getCapStatisticsReporting());
        assertThat(service.statistics.get(DLSJposConst.DLS_S_CAP_UPDATE_STATS)).isEqualTo(service.getCapUpdateStatistics());
        assertThat(service.statistics.get(DLSJposConst.DLS_S_CAP_COMP_FW_VER)).isEqualTo(service.getCapCompareFirmwareVersion());
        assertThat(service.statistics.get(DLSJposConst.DLS_S_CAP_UPDATE_FW)).isEqualTo(service.getCapUpdateFirmware());
        assertThat(service.statistics.get(DLSJposConst.DLS_S_SERVICE_VERSION)).isEqualTo("1.14.61");
        assertThat(service.statistics.get(DLSJposConst.DLS_S_CONTROL_VERSION)).isEqualTo(114000);
    }
}