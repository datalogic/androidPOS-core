package com.datalogic.dlapos.androidpos.transport;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Test;

public class DLSPortTest {


    //region addDataReceivedListener
    @Test
    public void addDataReceivedListener() {
        DummyPort port = new DummyPort();
        DataReceivedListener listener = mock(DataReceivedListener.class);
        port.addDataReceivedListener(listener);
        assertThat(port.getDataReceivedListeners().size()).isEqualTo(1);
        byte[] arg1 = new byte[2];
        port.fireDataReceivedEvent(arg1, 2);
        verify(listener, times(1)).onDataReceived(arg1, 2);
    }

    @Test
    public void addDataReceivedListenerDouble() {
        DummyPort port = new DummyPort();

        DataReceivedListener listener = mock(DataReceivedListener.class);

        port.addDataReceivedListener(listener);
        port.addDataReceivedListener(listener);
        assertThat(port.getDataReceivedListeners().size()).isEqualTo(1);
        byte[] arg1 = new byte[2];
        port.fireDataReceivedEvent(arg1, 2);
        verify(listener, times(1)).onDataReceived(arg1, 2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void addNullDataReceivedListener() {
        DummyPort port = new DummyPort();
        port.addDataReceivedListener(null);
    }
    //endregion

    //region addDeviceAddedListener
    @Test
    public void addDeviceAddedListener() {
        DummyPort port = new DummyPort();
        DeviceAddedListener listener = mock(DeviceAddedListener.class);
        port.addDeviceAddedListener(listener);
        assertThat(port.getDeviceAddedListeners().size()).isEqualTo(1);
        port.fireDeviceAddedEvent();
        verify(listener, times(1)).onDeviceAdded();
    }

    @Test
    public void addDeviceAddedListenerDouble() {
        DummyPort port = new DummyPort();
        DeviceAddedListener listener = mock(DeviceAddedListener.class);
        port.addDeviceAddedListener(listener);
        port.addDeviceAddedListener(listener);
        assertThat(port.getDeviceAddedListeners().size()).isEqualTo(1);
        port.fireDeviceAddedEvent();
        verify(listener, times(1)).onDeviceAdded();
    }

    @Test(expected = IllegalArgumentException.class)
    public void addNullDeviceAddedListener() {
        DummyPort port = new DummyPort();

        port.addDeviceAddedListener(null);
    }
    //endregion

    //region addDeviceArrivalListener
    @Test
    public void addDeviceArrivalListener() {
        DummyPort port = new DummyPort();
        DeviceArrivalListener listener = mock(DeviceArrivalListener.class);
        port.addDeviceArrivalListener(listener);
        assertThat(port.getDeviceArrivalListeners().size()).isEqualTo(1);
        port.fireDeviceArrivalEvent();
        verify(listener, times(1)).onDeviceArrival();
    }

    @Test
    public void addDeviceArrivalListenerDouble() {
        DummyPort port = new DummyPort();
        DeviceArrivalListener listener = mock(DeviceArrivalListener.class);
        port.addDeviceArrivalListener(listener);
        port.addDeviceArrivalListener(listener);
        assertThat(port.getDeviceArrivalListeners().size()).isEqualTo(1);
        port.fireDeviceArrivalEvent();
        verify(listener, times(1)).onDeviceArrival();
    }

    @Test(expected = IllegalArgumentException.class)
    public void addNullDeviceArrivalListener() {
        DummyPort port = new DummyPort();

        port.addDeviceArrivalListener(null);
    }
    //endregion

    //region addDeviceReattachedListener
    @Test
    public void addDeviceReattachedListener() {
        DummyPort port = new DummyPort();
        DeviceReattachedListener listener = mock(DeviceReattachedListener.class);
        port.addDeviceReattachedListener(listener);
        assertThat(port.getDeviceReattachedListeners().size()).isEqualTo(1);
        port.fireDeviceReattachedEvent();
        verify(listener, times(1)).onDeviceReattached();
    }

    @Test
    public void addDeviceReattachedListenerDouble() {
        DummyPort port = new DummyPort();
        DeviceReattachedListener listener = mock(DeviceReattachedListener.class);
        port.addDeviceReattachedListener(listener);
        port.addDeviceReattachedListener(listener);
        assertThat(port.getDeviceReattachedListeners().size()).isEqualTo(1);
        port.fireDeviceReattachedEvent();
        verify(listener, times(1)).onDeviceReattached();
    }

    @Test(expected = IllegalArgumentException.class)
    public void addNullDeviceReattachedListener() {
        DummyPort port = new DummyPort();

        port.addDeviceReattachedListener(null);
    }
    //endregion

    //region addDeviceRemovedListener
    @Test
    public void addDeviceRemovedListener() {
        DummyPort port = new DummyPort();
        DeviceRemovedListener listener = mock(DeviceRemovedListener.class);
        port.addDeviceRemovedListener(listener);
        assertThat(port.getDeviceRemovedListeners().size()).isEqualTo(1);
        port.fireDeviceRemovedEvent();
        verify(listener, times(1)).onDeviceRemoved();
    }

    @Test
    public void addDeviceRemovedListenerDouble() {
        DummyPort port = new DummyPort();
        DeviceRemovedListener listener = mock(DeviceRemovedListener.class);
        port.addDeviceRemovedListener(listener);
        port.addDeviceRemovedListener(listener);
        assertThat(port.getDeviceRemovedListeners().size()).isEqualTo(1);
        port.fireDeviceRemovedEvent();
        verify(listener, times(1)).onDeviceRemoved();
    }

    @Test(expected = IllegalArgumentException.class)
    public void addNullDeviceRemovedListener() {
        DummyPort port = new DummyPort();

        port.addDeviceRemovedListener(null);
    }
    //endregion

    //region fireDataReceivedEvent
    @Test(expected = IllegalArgumentException.class)
    public void fireDataReceivedNullEvent() {
        DummyPort port = new DummyPort();
        port.fireDataReceivedEvent(null, 2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void fireDataReceivedEmptyEvent() {
        DummyPort port = new DummyPort();
        port.fireDataReceivedEvent(new byte[0], 2);
    }

    @Test
    public void fireDataReceivedEventToNoOne() {
        DummyPort port = new DummyPort();
        port.fireDataReceivedEvent(new byte[1], 2);
    }

    @Test
    public void fireDataReceivedEventSendingMessages() {
        DummyPort port = new DummyPort();
        port.setSendingMessages(true);
        byte[] buff = new byte[2];
        port.fireDataReceivedEvent(buff, 2);
        assertThat(port.getResponseBuffer()).isEqualTo(buff);
        assertThat(port.isSendingMessages()).isFalse();
    }
    //endregion

    @Test
    public void fireDeviceAddedEventToNoOne() {
        DummyPort port = new DummyPort();
        port.fireDeviceAddedEvent();
    }

    @Test
    public void fireDeviceArrivalEventToNoOne() {
        DummyPort port = new DummyPort();
        port.fireDeviceArrivalEvent();
    }

    @Test
    public void fireDeviceRemovedEventToNoOne() {
        DummyPort port = new DummyPort();
        port.fireDeviceRemovedEvent();
    }

    private class DummyPort extends DLSPort {

        /**
         * Function to change the baud rate.
         *
         * @param baudRate the desired baud rate.
         */
        @Override
        public void changeBaudRate(int baudRate) {

        }

        /**
         * Function to close the port.
         *
         * @return true if the port is closed, false otherwise.
         */
        @Override
        public boolean closePort() {
            return false;
        }

        /**
         * Function to open the port.
         *
         * @return true if the port is opened, false otherwise.
         */
        @Override
        public boolean openPort() {
            return false;
        }

        /**
         * Function to send data on the port.
         *
         * @param buffer data to be sent.
         * @param len    the length of the data.
         * @return the number of sent bytes.
         */
        @Override
        public int sendData(byte[] buffer, int len) {
            return 0;
        }
    }
}