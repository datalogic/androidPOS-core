package com.datalogic.dlapos.androidpos.interpretation;

import static com.google.common.truth.Truth.assertThat;
import static junit.framework.TestCase.fail;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
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
import com.datalogic.dlapos.androidpos.common.DLSState;
import com.datalogic.dlapos.androidpos.transport.DLSUsbPort;
import com.datalogic.dlapos.androidpos.transport.UsbPortStatusListener;
import com.datalogic.dlapos.androidpos.utils.MockUtils;
import com.datalogic.dlapos.commons.support.APosException;
import com.datalogic.dlapos.commons.upos.RequestListener;
import com.datalogic.dlapos.confighelper.DLAPosConfigHelper;
import com.datalogic.dlapos.confighelper.configurations.accessor.ProfileManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

public class DLSDeviceTest {

    @Mock
    DLSProperties _properties = mock(DLSProperties.class);

    @Mock
    DLAPosConfigHelper _dlaPosConfigHelper = mock(DLAPosConfigHelper.class);

    @Mock
    ProfileManager _profileManager = mock(ProfileManager.class);

    @Mock
    private final DLSUsbPort _port = mock(DLSUsbPort.class);

    @Mock
    private final Context _context = mock(Context.class);

    @Before
    public void setup() {
        MockUtils.mockConfigHelperSingleton(_dlaPosConfigHelper, _profileManager);
        MockUtils.mockDLSPropertiesSingleton(_properties);
    }

    @After
    public void clean() {
        MockUtils.cleanConfigHelper();
        MockUtils.cleanDLSProperties();
    }

    @Test(expected = IllegalArgumentException.class)
    public void deviceErrorListenerNull() {
        DummyDevice device = new DummyDevice();
        device.addDeviceErrorListener(null);
    }

    @Test
    public void deviceErrorListener() {
        DummyDevice device = new DummyDevice();

        DeviceErrorStatusListener listener = mock(DeviceErrorStatusListener.class);
        device.addDeviceErrorListener(listener);
        assertThat(device.getDeviceErrorListeners().size()).isEqualTo(1);
        assertThat(device.getDeviceStatusListeners().isEmpty()).isTrue();
        assertThat(device.getDirectIODataListeners().isEmpty()).isTrue();
        device.fireDeviceErrorEvent(11);
        verify(listener, times(1)).onDeviceError(11);

        DeviceErrorStatusListener listener1 = mock(DeviceErrorStatusListener.class);
        device.addDeviceErrorListener(listener1);
        assertThat(device.getDeviceErrorListeners().size()).isEqualTo(2);
        assertThat(device.getDeviceStatusListeners().isEmpty()).isTrue();
        assertThat(device.getDirectIODataListeners().isEmpty()).isTrue();
        device.fireDeviceErrorEvent(12);
        verify(listener, times(1)).onDeviceError(12);
        verify(listener1, times(1)).onDeviceError(12);

        device.removeDeviceErrorListener(listener1);
        assertThat(device.getDeviceErrorListeners().size()).isEqualTo(1);
        assertThat(device.getDeviceStatusListeners().isEmpty()).isTrue();
        assertThat(device.getDirectIODataListeners().isEmpty()).isTrue();
        device.removeDeviceErrorListener(listener);
        assertThat(device.getDeviceErrorListeners().isEmpty()).isTrue();
        assertThat(device.getDeviceStatusListeners().isEmpty()).isTrue();
        assertThat(device.getDirectIODataListeners().isEmpty()).isTrue();
        device.fireDeviceErrorEvent(13);
        verify(listener, times(0)).onDeviceError(13);
    }

    @Test(expected = IllegalArgumentException.class)
    public void deviceStatusListenerNull() {
        DummyDevice device = new DummyDevice();
        device.addDeviceStatusListener(null);
    }

    @Test
    public void deviceStatusListener() {
        DummyDevice device = new DummyDevice();

        DeviceErrorStatusListener listener = mock(DeviceErrorStatusListener.class);
        device.addDeviceStatusListener(listener);
        assertThat(device.getDeviceErrorListeners().isEmpty()).isTrue();
        assertThat(device.getDeviceStatusListeners().size()).isEqualTo(1);
        assertThat(device.getDirectIODataListeners().isEmpty()).isTrue();
        device.fireDeviceStatusEvent(100);
        verify(listener, times(1)).onDeviceStatus(100);

        DeviceErrorStatusListener listener1 = mock(DeviceErrorStatusListener.class);
        device.addDeviceStatusListener(listener1);
        assertThat(device.getDeviceStatusListeners().size()).isEqualTo(2);
        assertThat(device.getDeviceErrorListeners().isEmpty()).isTrue();
        assertThat(device.getDirectIODataListeners().isEmpty()).isTrue();
        device.fireDeviceStatusEvent(120);
        verify(listener, times(1)).onDeviceStatus(120);
        verify(listener1, times(1)).onDeviceStatus(120);

        device.removeDeviceStatusListener(listener1);
        assertThat(device.getDeviceStatusListeners().size()).isEqualTo(1);
        assertThat(device.getDeviceErrorListeners().isEmpty()).isTrue();
        assertThat(device.getDirectIODataListeners().isEmpty()).isTrue();
        device.removeDeviceStatusListener(listener);
        assertThat(device.getDeviceErrorListeners().isEmpty()).isTrue();
        assertThat(device.getDeviceStatusListeners().isEmpty()).isTrue();
        assertThat(device.getDirectIODataListeners().isEmpty()).isTrue();
        device.fireDeviceStatusEvent(130);
        verify(listener, times(0)).onDeviceStatus(130);
    }

    @Test(expected = IllegalArgumentException.class)
    public void directIODataListenerNull() {
        DummyDevice device = new DummyDevice();
        device.addDirectIODataListener(null);
    }

    @Test
    public void directIODataListener() {
        DummyDevice device = new DummyDevice();
        byte[] data = new byte[]{1, 2};

        DirectIODataListener listener = mock(DirectIODataListener.class);
        device.addDirectIODataListener(listener);
        assertThat(device.getDeviceErrorListeners().isEmpty()).isTrue();
        assertThat(device.getDirectIODataListeners().size()).isEqualTo(1);
        assertThat(device.getDeviceStatusListeners().isEmpty()).isTrue();
        device.fireDirectIODataEvent(1000, data);
        verify(listener, times(1)).onDirectIOData(1000, data);

        DirectIODataListener listener1 = mock(DirectIODataListener.class);
        device.addDirectIODataListener(listener1);
        assertThat(device.getDirectIODataListeners().size()).isEqualTo(2);
        assertThat(device.getDeviceErrorListeners().isEmpty()).isTrue();
        assertThat(device.getDeviceStatusListeners().isEmpty()).isTrue();
        device.fireDirectIODataEvent(1200, data);
        verify(listener, times(1)).onDirectIOData(1200, data);
        verify(listener1, times(1)).onDirectIOData(1200, data);

        device.removeDirectIODataListener(listener1);
        assertThat(device.getDirectIODataListeners().size()).isEqualTo(1);
        assertThat(device.getDeviceErrorListeners().isEmpty()).isTrue();
        assertThat(device.getDeviceStatusListeners().isEmpty()).isTrue();
        device.removeDirectIODataListener(listener);
        assertThat(device.getDeviceErrorListeners().isEmpty()).isTrue();
        assertThat(device.getDeviceStatusListeners().isEmpty()).isTrue();
        assertThat(device.getDirectIODataListeners().isEmpty()).isTrue();
        device.fireDirectIODataEvent(1300, data);
        verify(listener, times(0)).onDirectIOData(1300, data);
    }

    //region Claim
    @Test
    public void claim() throws DLSException {
        DLSDeviceInfo deviceInfo = mock(DLSDeviceInfo.class);
        DummyDevice device = new DummyDevice();
        device.context = _context;
        device.deviceInfo = deviceInfo;
        device.setState(DLSState.OPENED);
        //region Simulate port opening
        //Capture the UsbPortListener to simulate permission grant.
        ArgumentCaptor<UsbPortStatusListener> listenerCaptor = ArgumentCaptor.forClass(UsbPortStatusListener.class);
        doNothing().when(_port).registerPortStatusListener(listenerCaptor.capture());
        //When openPort, return true and call the callback
        when(_port.openPort()).thenAnswer((Answer<Boolean>) invocation -> {
            listenerCaptor.getValue().portOpened();
            return Boolean.TRUE;
        });
        //endregion
        final boolean[] onSuccessCalled = {false};
        RequestListener listener = new RequestListener() {
            @Override
            public void onSuccess() {
                onSuccessCalled[0] = true;
            }

            @Override
            public void onFailure(String failureDescription) {
                fail();
            }
        };
        try (MockedStatic<DLSObjectFactory> factoryMockedStatic = Mockito.mockStatic(DLSObjectFactory.class)) {
            factoryMockedStatic.when(() -> DLSObjectFactory.createPort(deviceInfo, _context)).thenReturn(_port);
            device.claim(listener);
            assertThat(onSuccessCalled[0]).isTrue();
            assertThat(device.getState()).isEqualTo(DLSState.CLAIMED);
            verify(_port, times(1)).addDataReceivedListener(device);
            verify(_port, times(1)).addDeviceRemovedListener(device);
            verify(_port, times(1)).addDeviceReattachedListener(device);
        }
    }

    @Test
    public void claimPortAlreadySet() throws DLSException {
        DLSDeviceInfo deviceInfo = mock(DLSDeviceInfo.class);
        DummyDevice device = new DummyDevice();
        device.context = _context;
        device.deviceInfo = deviceInfo;
        device.port = _port;
        device.setState(DLSState.OPENED);
        device.setState(DLSState.CLAIMED);
        //region set port
        //region Simulate port opening
        //Capture the UsbPortListener to simulate permission grant.
        ArgumentCaptor<UsbPortStatusListener> listenerCaptor = ArgumentCaptor.forClass(UsbPortStatusListener.class);
        doNothing().when(_port).registerPortStatusListener(listenerCaptor.capture());
        //When openPort, return true and call the callback
        when(_port.openPort()).thenAnswer((Answer<Boolean>) invocation -> {
            listenerCaptor.getValue().portOpened();
            return Boolean.TRUE;
        });
        //endregion
        when(_port.isOpen()).thenReturn(true);
        when(_port.closePort()).then(invocation -> {
            when(_port.isOpen()).thenReturn(false);
            return null;
        });
        //endregion
        final boolean[] onSuccessCalled = {false};
        RequestListener listener = new RequestListener() {
            @Override
            public void onSuccess() {
                onSuccessCalled[0] = true;
            }

            @Override
            public void onFailure(String failureDescription) {
                fail();
            }
        };
        try (MockedStatic<DLSObjectFactory> factoryMockedStatic = Mockito.mockStatic(DLSObjectFactory.class)) {
            factoryMockedStatic.when(() -> DLSObjectFactory.createPort(deviceInfo, _context)).thenReturn(_port);
            device.claim(listener);
            assertThat(device.getState()).isEqualTo(DLSState.CLAIMED);
            assertThat(onSuccessCalled[0]).isTrue();
            verify(_port, times(1)).addDataReceivedListener(device);
            verify(_port, times(1)).addDeviceRemovedListener(device);
            verify(_port, times(1)).addDeviceReattachedListener(device);
            verify(_port, times(1)).closePort();
        }
    }

    @Test
    public void claimPortFailChangeState() throws DLSException {
        DLSDeviceInfo deviceInfo = mock(DLSDeviceInfo.class);
        DummyDevice device = new DummyDevice();
        device.context = _context;
        device.deviceInfo = deviceInfo;
        device.port = _port;
        device.setState(DLSState.CLOSED);
        //region set port
        //region Simulate port opening
        //Capture the UsbPortListener to simulate permission grant.
        ArgumentCaptor<UsbPortStatusListener> listenerCaptor = ArgumentCaptor.forClass(UsbPortStatusListener.class);
        doNothing().when(_port).registerPortStatusListener(listenerCaptor.capture());
        //When openPort, return true and call the callback
        when(_port.openPort()).thenAnswer((Answer<Boolean>) invocation -> {
            listenerCaptor.getValue().portOpened();
            return Boolean.TRUE;
        });
        //endregion
        //endregion
        final boolean[] onFailCalled = {false};
        RequestListener listener = new RequestListener() {
            @Override
            public void onSuccess() {
                fail();
            }

            @Override
            public void onFailure(String failureDescription) {
                onFailCalled[0] = true;
            }
        };
        try (MockedStatic<DLSObjectFactory> factoryMockedStatic = Mockito.mockStatic(DLSObjectFactory.class)) {
            factoryMockedStatic.when(() -> DLSObjectFactory.createPort(deviceInfo, _context)).thenReturn(_port);
            device.claim(listener);
            assertThat(onFailCalled[0]).isTrue();
            assertThat(device.getState()).isEqualTo(DLSState.CLOSED);
            verify(_port, times(0)).addDataReceivedListener(device);
            verify(_port, times(0)).addDeviceRemovedListener(device);
            verify(_port, times(0)).addDeviceReattachedListener(device);
            verify(_port, times(0)).closePort();
        }
    }

    @Test
    public void claimInternalClaimFails() throws DLSException {
        DLSDeviceInfo deviceInfo = mock(DLSDeviceInfo.class);
        DummyDevice device = new DummyDevice();
        device.context = _context;
        device.deviceInfo = deviceInfo;
        device.port = _port;
        device.setState(DLSState.OPENED);
        device._throwException = true;
        //region set port
        //region Simulate port opening
        //Capture the UsbPortListener to simulate permission grant.
        ArgumentCaptor<UsbPortStatusListener> listenerCaptor = ArgumentCaptor.forClass(UsbPortStatusListener.class);
        doNothing().when(_port).registerPortStatusListener(listenerCaptor.capture());
        //When openPort, return true and call the callback
        when(_port.openPort()).thenAnswer((Answer<Boolean>) invocation -> {
            listenerCaptor.getValue().portOpened();
            return Boolean.TRUE;
        });
        //endregion
        //endregion
        final boolean[] onFailCalled = {false};
        RequestListener listener = new RequestListener() {
            @Override
            public void onSuccess() {
                fail();
            }

            @Override
            public void onFailure(String failureDescription) {
                onFailCalled[0] = true;
            }
        };
        try (MockedStatic<DLSObjectFactory> factoryMockedStatic = Mockito.mockStatic(DLSObjectFactory.class)) {
            factoryMockedStatic.when(() -> DLSObjectFactory.createPort(deviceInfo, _context)).thenReturn(_port);
            device.claim(listener);
            assertThat(onFailCalled[0]).isTrue();
            assertThat(device.getState()).isEqualTo(DLSState.RELEASED);
        }
    }

    @Test
    public void claimFailsOpen() throws DLSException {
        DLSDeviceInfo deviceInfo = mock(DLSDeviceInfo.class);
        DummyDevice device = new DummyDevice();
        device.context = _context;
        device.deviceInfo = deviceInfo;
        device.setState(DLSState.OPENED);
        when(_port.openPort()).thenReturn(false);
        try (MockedStatic<DLSObjectFactory> factoryMockedStatic = Mockito.mockStatic(DLSObjectFactory.class)) {
            factoryMockedStatic.when(() -> DLSObjectFactory.createPort(deviceInfo, _context)).thenReturn(_port);
            device.claim(null);
            fail();
        } catch (DLSException e) {
            assertThat(e.getErrorCode()).isEqualTo(DLSJposConst.DLS_E_OPENPORT);
        }
    }

    //endregion

    //region Close
    @Test
    public void closeAlreadyClosed() throws DLSException {
        DummyDevice device = new DummyDevice();
        device.setState(DLSState.OPENED);
        device.close();
        assertThat(device.getState()).isEqualTo(DLSState.CLOSED);
    }

    @Test
    public void close() throws DLSException {
        DummyDevice device = new DummyDevice();
        device.setState(DLSState.OPENED);
        when(_port.isOpen()).thenReturn(true);
        device.port = _port;
        device.close();
        verify(_port, times(1)).removeDataReceivedListener(device);
        verify(_port, times(1)).closePort();
        assertThat(device.getState()).isEqualTo(DLSState.CLOSED);
        assertThat(device.port).isNull();
    }
    //endregion

    //region Concat USB messages
    @Test
    public void concatUSBMessages() {
        DummyDevice device = new DummyDevice();
        device.concatUSBMessages("Ciao".getBytes(), false);
        assertThat(device.outputStream.toString()).isEqualTo("Ciao");
        assertThat(device.messageList.isEmpty()).isTrue();
        device.concatUSBMessages(" ciao".getBytes(), true);
        assertThat(device.outputStream.size()).isEqualTo(0);
        assertThat(device.messageList.isEmpty()).isFalse();
        assertThat(new String((byte[]) device.messageList.get(0))).isEqualTo("Ciao ciao");
    }

    @Test(expected = IllegalArgumentException.class)
    public void concatUSBMessageNull() {
        DummyDevice device = new DummyDevice();
        device.concatUSBMessages(null, false);
    }
    //endregion

    @Test
    public void getDeviceEnabled() throws DLSException {
        DummyDevice device = new DummyDevice();
        assertThat(device.getDeviceEnabled()).isFalse();
        device.setState(DLSState.OPENED);
        assertThat(device.getDeviceEnabled()).isFalse();
        device.setState(DLSState.CLAIMED);
        assertThat(device.getDeviceEnabled()).isFalse();
        device.setState(DLSState.ENABLED);
        assertThat(device.getDeviceEnabled()).isTrue();
        device.setState(DLSState.DISABLED);
        assertThat(device.getDeviceEnabled()).isFalse();
        device.setState(DLSState.RELEASED);
        assertThat(device.getDeviceEnabled()).isFalse();
        device.setState(DLSState.CLOSED);
        assertThat(device.getDeviceEnabled()).isFalse();
    }

    @Test
    public void getLogicalName() {
        DummyDevice device = new DummyDevice();
        device.setLogicalName("DummyName");
        assertThat(device.getLogicalName()).isEqualTo("DummyName");
        device.setLogicalName("Dummy2");
        assertThat(device.getLogicalName()).isEqualTo("Dummy2");
    }

    @Test
    public void hasStatisticsReporting() {
        DummyDevice device = new DummyDevice();
        assertThat(device.hasStatisticsReporting()).isFalse();
    }

    //region Open
    @Test(expected = IllegalArgumentException.class)
    public void openNullContext() throws DLSException {
        DummyDevice device = new DummyDevice();
        device.open("adsa", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void openNullString() throws DLSException {
        DummyDevice device = new DummyDevice();
        device.open(null, _context);
    }

    @Test
    public void open() throws DLSException {
        DummyDevice device = new DummyDevice();
        device.open("test", _context);
        assertThat(device.context).isEqualTo(_context);
        assertThat(device.getLogicalName()).isEqualTo("test");
        assertThat(device.properties).isEqualTo(_properties);
    }

    @Test(expected = DLSException.class)
    public void openNoProfile() throws DLSException, APosException {
        DummyDevice device = new DummyDevice();
        when(_profileManager.getConfigurationForProfileId("test")).thenThrow(new APosException());
        device.open("test", _context);
    }
    //endregion

    //region SetState

    //region Starting from closed
    @Test
    public void setStateFromClosedToOpened() throws DLSException {
        DummyDevice device = new DummyDevice();
        assertThat(device.getState()).isEqualTo(DLSState.CLOSED);
        device.setState(DLSState.OPENED);
        assertThat(device.getState()).isEqualTo(DLSState.OPENED);
    }

    @Test(expected = DLSException.class)
    public void setStateFromClosedToClaimed() throws DLSException {
        DummyDevice device = new DummyDevice();
        assertThat(device.getState()).isEqualTo(DLSState.CLOSED);
        device.setState(DLSState.CLAIMED);
    }

    @Test(expected = DLSException.class)
    public void setStateFromClosedToEnabled() throws DLSException {
        DummyDevice device = new DummyDevice();
        assertThat(device.getState()).isEqualTo(DLSState.CLOSED);
        device.setState(DLSState.ENABLED);
    }

    @Test(expected = DLSException.class)
    public void setStateFromClosedToReleased() throws DLSException {
        DummyDevice device = new DummyDevice();
        assertThat(device.getState()).isEqualTo(DLSState.CLOSED);
        device.setState(DLSState.RELEASED);
    }

    @Test(expected = DLSException.class)
    public void setStateFromClosedToDisabled() throws DLSException {
        DummyDevice device = new DummyDevice();
        assertThat(device.getState()).isEqualTo(DLSState.CLOSED);
        device.setState(DLSState.DISABLED);
    }

    @Test
    public void setStateFromClosedToClosed() throws DLSException {
        DummyDevice device = new DummyDevice();
        assertThat(device.getState()).isEqualTo(DLSState.CLOSED);
        device.setState(DLSState.CLOSED);
        assertThat(device.getState()).isEqualTo(DLSState.CLOSED);
    }

    //endregion

    //region Starting from opened
    @Test
    public void setStateFromOpenedToOpened() throws DLSException {
        DummyDevice device = new DummyDevice();
        goToOpened(device);
        device.setState(DLSState.OPENED);
        assertThat(device.getState()).isEqualTo(DLSState.OPENED);
    }

    @Test
    public void setStateFromOpenedToClaimed() throws DLSException {
        DummyDevice device = new DummyDevice();
        goToOpened(device);
        device.setState(DLSState.CLAIMED);
        assertThat(device.getState()).isEqualTo(DLSState.CLAIMED);
    }

    @Test(expected = DLSException.class)
    public void setStateFromOpenedToEnabled() throws DLSException {
        DummyDevice device = new DummyDevice();
        goToOpened(device);
        device.setState(DLSState.ENABLED);
    }

    @Test(expected = DLSException.class)
    public void setStateFromOpenedToDisabled() throws DLSException {
        DummyDevice device = new DummyDevice();
        goToOpened(device);
        device.setState(DLSState.DISABLED);
    }

    @Test(expected = DLSException.class)
    public void setStateFromOpenedToReleased() throws DLSException {
        DummyDevice device = new DummyDevice();
        goToOpened(device);
        device.setState(DLSState.RELEASED);
    }

    @Test
    public void setStateFromOpenedToClosed() throws DLSException {
        DummyDevice device = new DummyDevice();
        goToOpened(device);
        device.setState(DLSState.CLOSED);
        assertThat(device.getState()).isEqualTo(DLSState.CLOSED);
    }

    //endregion

    //region Starting from claimed
    @Test(expected = DLSException.class)
    public void setStateFromClaimedToOpened() throws DLSException {
        DummyDevice device = new DummyDevice();
        goToClaimed(device);
        device.setState(DLSState.OPENED);
    }

    @Test
    public void setStateFromClaimedToClaimed() throws DLSException {
        DummyDevice device = new DummyDevice();
        goToClaimed(device);
        device.setState(DLSState.CLAIMED);
        assertThat(device.getState()).isEqualTo(DLSState.CLAIMED);
    }

    @Test
    public void setStateFromClaimedToEnabled() throws DLSException {
        DummyDevice device = new DummyDevice();
        goToClaimed(device);
        device.setState(DLSState.ENABLED);
        assertThat(device.getState()).isEqualTo(DLSState.ENABLED);
    }

    @Test
    public void setStateFromClaimedToDisabled() throws DLSException {
        DummyDevice device = new DummyDevice();
        goToClaimed(device);
        device.setState(DLSState.DISABLED);
        assertThat(device.getState()).isEqualTo(DLSState.DISABLED);
    }

    @Test
    public void setStateFromClaimedToReleased() throws DLSException {
        DummyDevice device = new DummyDevice();
        goToClaimed(device);
        device.setState(DLSState.RELEASED);
        assertThat(device.getState()).isEqualTo(DLSState.RELEASED);
    }

    @Test(expected = DLSException.class)
    public void setStateFromClaimedToClose() throws DLSException {
        DummyDevice device = new DummyDevice();
        goToClaimed(device);
        device.setState(DLSState.CLOSED);
    }

    //endregion

    //region Starting from enabled
    @Test(expected = DLSException.class)
    public void setStateFromEnabledToOpened() throws DLSException {
        DummyDevice device = new DummyDevice();
        goToEnabled(device);
        device.setState(DLSState.OPENED);
    }

    @Test(expected = DLSException.class)
    public void setStateFromEnabledToClaimed() throws DLSException {
        DummyDevice device = new DummyDevice();
        goToEnabled(device);
        device.setState(DLSState.CLAIMED);
    }

    @Test
    public void setStateFromEnabledToEnabled() throws DLSException {
        DummyDevice device = new DummyDevice();
        goToEnabled(device);
        device.setState(DLSState.ENABLED);
        assertThat(device.getState()).isEqualTo(DLSState.ENABLED);
    }

    @Test
    public void setStateFromEnabledToDisabled() throws DLSException {
        DummyDevice device = new DummyDevice();
        goToEnabled(device);
        device.setState(DLSState.DISABLED);
        assertThat(device.getState()).isEqualTo(DLSState.DISABLED);
    }

    @Test(expected = DLSException.class)
    public void setStateFromEnabledToReleased() throws DLSException {
        DummyDevice device = new DummyDevice();
        goToEnabled(device);
        device.setState(DLSState.RELEASED);
    }

    @Test(expected = DLSException.class)
    public void setStateFromEnabledToClosed() throws DLSException {
        DummyDevice device = new DummyDevice();
        goToEnabled(device);
        device.setState(DLSState.CLOSED);
    }
    //endregion

    //region Starting from disabled
    @Test(expected = DLSException.class)
    public void setStateFromDisabledToOpened() throws DLSException {
        DummyDevice device = new DummyDevice();
        goToDisabled(device);
        device.setState(DLSState.OPENED);
    }

    @Test(expected = DLSException.class)
    public void setStateFromDisabledToClaimed() throws DLSException {
        DummyDevice device = new DummyDevice();
        goToDisabled(device);
        device.setState(DLSState.CLAIMED);
    }

    @Test
    public void setStateFromDisabledToEnabled() throws DLSException {
        DummyDevice device = new DummyDevice();
        goToDisabled(device);
        device.setState(DLSState.ENABLED);
        assertThat(device.getState()).isEqualTo(DLSState.ENABLED);
    }

    @Test
    public void setStateFromDisabledToDisabled() throws DLSException {
        DummyDevice device = new DummyDevice();
        goToDisabled(device);
        device.setState(DLSState.DISABLED);
        assertThat(device.getState()).isEqualTo(DLSState.DISABLED);
    }

    @Test
    public void setStateFromDisabledToReleased() throws DLSException {
        DummyDevice device = new DummyDevice();
        goToDisabled(device);
        device.setState(DLSState.RELEASED);
        assertThat(device.getState()).isEqualTo(DLSState.RELEASED);
    }

    @Test(expected = DLSException.class)
    public void setStateFromDisabledToClosed() throws DLSException {
        DummyDevice device = new DummyDevice();
        goToDisabled(device);
        device.setState(DLSState.CLOSED);
    }
    //endregion

    //region Starting from released
    @Test(expected = DLSException.class)
    public void setStateFromReleasedToOpened() throws DLSException {
        DummyDevice device = new DummyDevice();
        goToReleased(device);
        device.setState(DLSState.OPENED);
    }

    @Test
    public void setStateFromReleasedToClaimed() throws DLSException {
        DummyDevice device = new DummyDevice();
        goToReleased(device);
        device.setState(DLSState.CLAIMED);
        assertThat(device.getState()).isEqualTo(DLSState.CLAIMED);
    }

    @Test(expected = DLSException.class)
    public void setStateFromReleasedToEnabled() throws DLSException {
        DummyDevice device = new DummyDevice();
        goToReleased(device);
        device.setState(DLSState.ENABLED);
    }

    @Test(expected = DLSException.class)
    public void setStateFromReleasedToDisabled() throws DLSException {
        DummyDevice device = new DummyDevice();
        goToReleased(device);
        device.setState(DLSState.DISABLED);
        assertThat(device.getState()).isEqualTo(DLSState.DISABLED);
    }

    @Test
    public void setStateFromReleasedToReleased() throws DLSException {
        DummyDevice device = new DummyDevice();
        goToReleased(device);
        device.setState(DLSState.RELEASED);
        assertThat(device.getState()).isEqualTo(DLSState.RELEASED);
    }

    @Test
    public void setStateFromReleasedToClosed() throws DLSException {
        DummyDevice device = new DummyDevice();
        goToReleased(device);
        device.setState(DLSState.CLOSED);
        assertThat(device.getState()).isEqualTo(DLSState.CLOSED);
    }
    //endregion
    //endregion

    @Test
    public void parseMessage() {
        DummyDevice device = new DummyDevice();
        device.parseMessages("Ciao".getBytes(), "%".getBytes()[0]);
        assertThat(device.outputStream.toString()).isEqualTo("Ciao");
        device.parseMessages(" ciao%".getBytes(), "%".getBytes()[0]);
        assertThat(device.outputStream.size()).isEqualTo(0);
        assertThat(device.messageList.isEmpty()).isFalse();
        assertThat(new String((byte[]) device.messageList.get(0))).isEqualTo("Ciao ciao%");
    }

    private void goToOpened(DummyDevice device) throws DLSException {
        assertThat(device.getState()).isEqualTo(DLSState.CLOSED);
        device.setState(DLSState.OPENED);
        assertThat(device.getState()).isEqualTo(DLSState.OPENED);
    }

    private void goToClaimed(DummyDevice device) throws DLSException {
        goToOpened(device);
        device.setState(DLSState.CLAIMED);
        assertThat(device.getState()).isEqualTo(DLSState.CLAIMED);
    }

    private void goToEnabled(DummyDevice device) throws DLSException {
        goToClaimed(device);
        device.setState(DLSState.ENABLED);
        assertThat(device.getState()).isEqualTo(DLSState.ENABLED);
    }

    private void goToDisabled(DummyDevice device) throws DLSException {
        goToEnabled(device);
        device.setState(DLSState.DISABLED);
        assertThat(device.getState()).isEqualTo(DLSState.DISABLED);
    }

    private void goToReleased(DummyDevice device) throws DLSException {
        goToDisabled(device);
        device.setState(DLSState.RELEASED);
        assertThat(device.getState()).isEqualTo(DLSState.RELEASED);
    }

    private class DummyDevice extends DLSDevice {

        private boolean _throwException = false;
        private boolean _internalClaimCalled = false;

        @Override
        void internalClaim() throws DLSException {
            if (_throwException)
                throw new DLSException(1, "TEST");

            _internalClaimCalled = true;
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
        public DLSCConfig getConfiguration() {
            return null;
        }

        @Override
        public boolean isAlive() throws DLSException {
            return false;
        }

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