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

import com.datalogic.dlapos.androidpos.common.DLSException;
import com.datalogic.dlapos.androidpos.common.DLSProperties;
import com.datalogic.dlapos.androidpos.common.DLSScaleConfig;
import com.datalogic.dlapos.androidpos.common.DLSState;
import com.datalogic.dlapos.androidpos.utils.MockUtils;
import com.datalogic.dlapos.commons.support.APosException;
import com.datalogic.dlapos.confighelper.DLAPosConfigHelper;
import com.datalogic.dlapos.confighelper.configurations.accessor.ProfileManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockedConstruction;

public class DLSScaleTest {

    @Mock
    private final Context _context = mock(Context.class);

    @Mock
    DLSProperties _properties = mock(DLSProperties.class);

    @Mock
    ProfileManager _profileManager = mock(ProfileManager.class);

    @Mock
    DLAPosConfigHelper _dlaPosConfigHelper = mock(DLAPosConfigHelper.class);

    @Mock
    private final DLSScaleConfig _scaleConfig = mock(DLSScaleConfig.class);

    @Before
    public void setUp() {
        MockUtils.mockDLSPropertiesSingleton(_properties);
        MockUtils.mockConfigHelperSingleton(_dlaPosConfigHelper, _profileManager);
    }

    @After
    public void clear() {
        MockUtils.cleanDLSProperties();
        MockUtils.cleanConfigHelper();
    }

    @Test
    public void weightReceivedListener() {
        DummyScale scale = new DummyScale();
        WeightReceivedListener listener = mock(WeightReceivedListener.class);
        scale.addWeightReceivedListener(listener);
        scale.fireWeightReceivedEvent(13);
        verify(listener, times(1)).onWeightReceived(13);

        WeightReceivedListener listener1 = mock(WeightReceivedListener.class);
        scale.addWeightReceivedListener(listener1);
        scale.fireWeightReceivedEvent(56);
        verify(listener, times(1)).onWeightReceived(56);
        verify(listener1, times(1)).onWeightReceived(56);

        scale.removeWeightReceivedListener(listener);
        scale.fireWeightReceivedEvent(78);
        verify(listener, times(0)).onWeightReceived(78);
        verify(listener1, times(1)).onWeightReceived(78);
    }

    @Test
    public void canStatusUpdate() {
        DummyScale scale = new DummyScale();
        scale.scaleConfig = _scaleConfig;
        when(_scaleConfig.getCanStatusUpdate()).thenReturn(false);
        assertThat(scale.canStatusUpdate()).isFalse();
        when(_scaleConfig.getCanStatusUpdate()).thenReturn(true);
        assertThat(scale.canStatusUpdate()).isTrue();
    }

    @Test
    public void internalClaim() throws DLSException {
        DummyScale scale = new DummyScale();
        scale._isAlive = true;
        scale.internalClaim();
        assertThat(scale.reportConfigCalls).isEqualTo(1);
    }

    @Test
    public void internalClaimNotResponding() {
        DummyScale scale = new DummyScale();
        try {
            scale.internalClaim();
            fail();
        } catch (DLSException e) {
            assertThat(e.getErrorCode()).isEqualTo(0x6a);
        }
    }

    @Test
    public void internalClaimAutoLoad() throws DLSException {
        DummyScale scale = new DummyScale();
        when(_properties.isAutoLoadConfig()).thenReturn(true);
        scale._isAlive = true;
        scale.internalClaim();
        assertThat(scale.reportConfigCalls).isEqualTo(1);
        assertThat(scale.updateConfigCalls).isEqualTo(1);
    }

    @Test
    public void isLiveWeight() {
        DummyScale scale = new DummyScale();
        assertThat(scale.isLiveWeight()).isFalse();
    }

    @Test
    public void liveWeight() {
        DummyScale scale = new DummyScale();
        scale.setLiveWeight(12);
        assertThat(scale.getLiveWeight()).isEqualTo(12);
        scale.setLiveWeight(43);
        assertThat(scale.getLiveWeight()).isEqualTo(43);
    }

    @Test
    public void metricMode() {
        DummyScale scale = new DummyScale();
        scale.setMetricMode(false);
        assertThat(scale.getMetricMode()).isFalse();
        scale.setMetricMode(true);
        assertThat(scale.getMetricMode()).isTrue();
    }

    //region Open
    @Test(expected = IllegalArgumentException.class)
    public void openNullContext() throws DLSException {
        DummyScale device = new DummyScale();
        device.open("adsa", null);
        assertThat(device.getState()).isNotEqualTo(DLSState.OPENED);
    }

    @Test(expected = IllegalArgumentException.class)
    public void openNullString() throws DLSException {
        DummyScale device = new DummyScale();
        device.open(null, _context);
        assertThat(device.getState()).isNotEqualTo(DLSState.OPENED);
    }

    @Test
    public void open() throws DLSException {
        DummyScale device = new DummyScale();
        try (MockedConstruction<DLSScaleConfig> mockedConfig = mockConstruction(DLSScaleConfig.class,
                (mock, context) -> {
                    when(mock.getLiveWeightPollRate()).thenReturn(57);
                    when(mock.getMetricWeightMode()).thenReturn(true);
                    when(mock.getLogicalName()).thenReturn("I'm the mock");
                })) {
            device.open("test", _context);
            assertThat(device.context).isEqualTo(_context);
            assertThat(device.getLogicalName()).isEqualTo("test");
            assertThat(device.properties).isEqualTo(_properties);
            assertThat(device.getPollRate()).isEqualTo(57);
            assertThat(device.getMetricMode()).isTrue();
            assertThat(device.getConfiguration().getLogicalName()).isEqualTo("I'm the mock");
            assertThat(device.getState()).isEqualTo(DLSState.OPENED);
        }
    }

    @Test(expected = DLSException.class)
    public void openNoProfile() throws DLSException, APosException {
        DummyScale device = new DummyScale();
        when(_profileManager.getConfigurationForProfileId("test")).thenThrow(new APosException());
        device.open("test", _context);
        assertThat(device.getState()).isNotEqualTo(DLSState.OPENED);
    }

    @Test(expected = DLSException.class)
    public void openLoadException() throws DLSException {
        DummyScale device = new DummyScale();
        try (MockedConstruction<DLSScaleConfig> ignored = mockConstruction(DLSScaleConfig.class,
                (mock, context) -> {
                    when(mock.loadConfiguration(any(), any())).thenThrow(new APosException());
                })) {
            device.open("test", _context);
            assertThat(device.getState()).isNotEqualTo(DLSState.OPENED);
        }
    }

    //endregion

    @Test
    public void statusNotify() {
        DummyScale scale = new DummyScale();
        scale.setStatusNotify(23);
        assertThat(scale.getStatusNotify()).isEqualTo(23);
        scale.setStatusNotify(56);
        assertThat(scale.getStatusNotify()).isEqualTo(56);
    }

    @Test
    public void statusValue() {
        DummyScale scale = new DummyScale();
        scale.setStatusValue(23);
        assertThat(scale.getStatusValue()).isEqualTo(23);
        scale.setStatusValue(56);
        assertThat(scale.getStatusValue()).isEqualTo(56);
    }

    @Test
    public void zeroValid() {
        DummyScale scale = new DummyScale();
        scale.scaleConfig = new DLSScaleConfig();
        scale.setZeroValid(false);
        assertThat(scale.getZeroValid()).isFalse();
        scale.setZeroValid(true);
        assertThat(scale.getZeroValid()).isTrue();
    }

    private class DummyScale extends DLSScale {

        int reportConfigCalls = 0;
        int updateConfigCalls = 0;
        boolean _isAlive = false;

        @Override
        public boolean canZeroScale() {
            return false;
        }

        @Override
        public void clearDisplay() throws DLSException {

        }

        @Override
        public void readEnglishWeight() throws DLSException {

        }

        @Override
        public void readMetricWeight() throws DLSException {

        }

        @Override
        public void readStatusWeight() throws DLSException {

        }

        @Override
        public void reportConfiguration() throws DLSException {
            reportConfigCalls++;
        }

        @Override
        public void updateConfiguration() throws DLSException {
            updateConfigCalls++;
        }

        @Override
        public void zeroScale() throws DLSException {

        }

        @Override
        public boolean detectResetFinish() throws DLSException {
            return false;
        }

        @Override
        public void directIO(int command, int[] data, Object object) throws DLSException {

        }

        @Override
        public void disable() throws DLSException {

        }

        @Override
        public boolean doHealthCheck(int timeout) throws DLSException {
            return false;
        }

        @Override
        public void doSelfTest() throws DLSException {

        }

        @Override
        public void enable() throws DLSException {

        }

        @Override
        public boolean isAlive() throws DLSException {
            return _isAlive;
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