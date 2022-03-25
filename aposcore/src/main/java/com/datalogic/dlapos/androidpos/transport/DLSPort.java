package com.datalogic.dlapos.androidpos.transport;

import android.content.Context;

import com.datalogic.dlapos.androidpos.common.DLSDeviceInfo;
import com.datalogic.dlapos.androidpos.common.FunctionLib;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;

/**
 * Class abstracting a generic port of an Android device.
 */
public abstract class DLSPort {
    private final ArrayList<Object> dataReceivedListeners;
    private final ArrayList<Object> deviceAddedListeners;
    private final ArrayList<Object> deviceArrivalListeners;
    private final ArrayList<Object> deviceReattachedListeners;
    private final ArrayList<Object> deviceRemovedListeners;

    private DLSDeviceInfo deviceInfo;
    protected Context context;
    private boolean sendingMessages = false;
    private byte[] responseBuffer;
    private boolean open;

    protected final Object commandResponseMutex = new Object();

    /**
     * Constructor.
     *
     * @param deviceInfo the device info to configure the port.
     * @param context    the application context.
     */
    public DLSPort(DLSDeviceInfo deviceInfo, Context context) {
        this();
        this.deviceInfo = deviceInfo;
        this.context = context;
    }


    /**
     * No args constructor.
     */
    protected DLSPort() {
        this.dataReceivedListeners = new ArrayList<>();
        this.deviceAddedListeners = new ArrayList<>();
        this.deviceArrivalListeners = new ArrayList<>();
        this.deviceReattachedListeners = new ArrayList<>();
        this.deviceRemovedListeners = new ArrayList<>();
        this.open = false;
    }

    /**
     * Function to add a lister for data received on the port.
     *
     * @param listener the listener to add.
     */
    public void addDataReceivedListener(DataReceivedListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("DataReceivedListener instance cannot be null.");
        }
        if (!dataReceivedListeners.contains(listener))
            this.dataReceivedListeners.add(listener);
    }

    /**
     * Function to add a listener for new device connected to the port.
     *
     * @param listener the listener to add.
     */
    public void addDeviceAddedListener(DeviceAddedListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("DeviceAddedListener instance cannot be null.");
        }
        if (!deviceAddedListeners.contains(listener))
            this.deviceAddedListeners.add(listener);
    }

    /**
     * Function to add a listener for a new device arrival on the port.
     *
     * @param listener the listener to add.
     */
    public void addDeviceArrivalListener(DeviceArrivalListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("DeviceArrivalListener instance cannot be null.");
        }
        if (!deviceArrivalListeners.contains(listener))
            this.deviceArrivalListeners.add(listener);
    }

    /**
     * Function to add a listener for device reattached to the port.
     *
     * @param listener the listener to add.
     */
    public void addDeviceReattachedListener(DeviceReattachedListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("DeviceReattachedListener instance cannot be null.");
        }
        if (!deviceReattachedListeners.contains(listener))
            this.deviceReattachedListeners.add(listener);
    }

    /**
     * Function to add a listener for device removal to the port.
     *
     * @param listener the listener to add.
     */
    public void addDeviceRemovedListener(DeviceRemovedListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("DeviceRemovedListener instance cannot be null.");
        }
        if (!deviceRemovedListeners.contains(listener))
            this.deviceRemovedListeners.add(listener);
    }

    /**
     * Function to change the baud rate.
     *
     * @param baudRate the desired baud rate.
     */
    public abstract void changeBaudRate(int baudRate);

    /**
     * Function to close the port.
     *
     * @return true if the port is closed, false otherwise.
     */
    public abstract boolean closePort();

    @Override
    protected void finalize() {
        closePort();
    }

    /**
     * Function to fire a data received event.
     *
     * @param buffer the data received.
     * @param len    the length of the data.
     */
    protected void fireDataReceivedEvent(byte[] buffer, int len) {
        DataReceivedListener listener;

        if (buffer == null || buffer.length == 0) {
            throw new IllegalArgumentException("Buffer can not be null or empty.");
        }
        if (isSendingMessages()) {
            synchronized (commandResponseMutex) {
                setSendingMessages(false);
                setResponseBuffer(buffer.clone());
                commandResponseMutex.notifyAll();
            }
            return;
        }

        Enumeration<Object> e = Collections.enumeration(this.dataReceivedListeners);
        while (e.hasMoreElements()) {
            listener = (DataReceivedListener) e.nextElement();
            String rawBytes = FunctionLib.byteArrayToString(buffer);
            /*
            This is a hack that I am not happy with.  The FunctionLib.byteArrayToString method
            will return a specially formatted string if the supplied buffer is either null or
            empty.  This code looks for that special formatting to determine whether to call
            onDataReceived.  It is ugly, and a better method should be determined as this is
            liable to create a hard to find issue later.
             */
            if (!rawBytes.toUpperCase().contains("**INVALID")) {
                listener.onDataReceived(buffer, len);
            }
        }
    }

    /**
     * Function to fire a device added event.
     */
    public void fireDeviceAddedEvent() {
        DeviceAddedListener listener;
        Enumeration<Object> e = Collections.enumeration(this.deviceAddedListeners);
        while (e.hasMoreElements()) {
            listener = (DeviceAddedListener) e.nextElement();
            listener.onDeviceAdded();
        }
    }

    /**
     * Function to fire a device arrival event.
     */
    public void fireDeviceArrivalEvent() {
        DeviceArrivalListener listener;
        Enumeration<Object> e = Collections.enumeration(this.deviceArrivalListeners);
        while (e.hasMoreElements()) {
            listener = (DeviceArrivalListener) e.nextElement();
            listener.onDeviceArrival();
        }
    }

    /**
     * Function to fire a device reattached event.
     */
    public void fireDeviceReattachedEvent() {
        DeviceReattachedListener listener;
        Enumeration<Object> e = Collections.enumeration(this.deviceReattachedListeners);
        while (e.hasMoreElements()) {
            listener = (DeviceReattachedListener) e.nextElement();
            listener.onDeviceReattached();
        }
    }

    /**
     * Function to fire a device removed event.
     */
    public void fireDeviceRemovedEvent() {
        DeviceRemovedListener listener;
        Enumeration<Object> e = Collections.enumeration(this.deviceRemovedListeners);
        while (e.hasMoreElements()) {
            listener = (DeviceRemovedListener) e.nextElement();
            listener.onDeviceRemoved();
        }
    }

    /**
     * Function to get all data received listeners.
     *
     * @return all data received listeners.
     */
    public ArrayList<Object> getDataReceivedListeners() {
        return dataReceivedListeners;
    }

    /**
     * Function to get all device added listeners.
     *
     * @return all device added listeners.
     */
    public ArrayList<Object> getDeviceAddedListeners() {
        return deviceAddedListeners;
    }

    /**
     * Function to get all device arrival listeners.
     *
     * @return all device arrival listeners.
     */
    public ArrayList<Object> getDeviceArrivalListeners() {
        return deviceArrivalListeners;
    }

    /**
     * Function to get info of the device connected to the port.
     *
     * @return info of the device connected to the port.
     */
    public DLSDeviceInfo getDeviceInfo() {
        return deviceInfo;
    }

    /**
     * Function to get all device reattached listeners.
     *
     * @return all device reattached listeners.
     */
    public ArrayList<Object> getDeviceReattachedListeners() {
        return deviceReattachedListeners;
    }

    /**
     * Function to get all device removed listeners.
     *
     * @return all device removed listeners.
     */
    public ArrayList<Object> getDeviceRemovedListeners() {
        return deviceRemovedListeners;
    }

    /**
     * Function to get the response buffer.
     *
     * @return the response buffer.
     */
    protected byte[] getResponseBuffer() {
        return this.responseBuffer;
    }

//    public boolean isDSR() {
//        return false;
//    }

    /**
     * Function to check if the port is open.
     *
     * @return true if the port is open, false otherwise.
     */
    public boolean isOpen() {
        return this.open;
    }

    /**
     * Function to check if the port is used sending messages.
     *
     * @return true if the port is used sending messages.
     */
    protected boolean isSendingMessages() {
        return this.sendingMessages;
    }

    /**
     * Function to open the port.
     *
     * @return true if the port is opened, false otherwise.
     */
    public abstract boolean openPort();

    /**
     * Function to remove a data received listener.
     *
     * @param listener the listener to be removed.
     */
    public void removeDataReceivedListener(DataReceivedListener listener) {
        this.dataReceivedListeners.remove(listener);
    }

    /**
     * Function to remove a device added listener.
     *
     * @param listener the listener to be removed.
     */
    public void removeDeviceAddedListener(DeviceAddedListener listener) {
        this.deviceAddedListeners.remove(listener);
    }

    /**
     * Function to remove a device arrival listener.
     *
     * @param listener the listener to be removed.
     */
    public void removeDeviceArrivalListener(DeviceArrivalListener listener) {
        this.deviceArrivalListeners.remove(listener);
    }

    /**
     * Function to remove a device reattached listener.
     *
     * @param listener the listener to be remove.
     */
    public void removeDeviceReattachedListener(DeviceReattachedListener listener) {
        this.deviceReattachedListeners.remove(listener);
    }

    /**
     * Function to remove a device removed listener.
     *
     * @param listener the listener to be removed.
     */
    public void removeDeviceRemovedListener(DeviceRemovedListener listener) {
        this.deviceRemovedListeners.remove(listener);
    }

    /**
     * Function to remove all internal listeners.
     */
    // TODO: Determine if this method can be removed.
    public void removeInternalListeners() {
    }

    /**
     * Function to send data on the port.
     *
     * @param buffer data to be sent.
     * @param len    the length of the data.
     * @return the number of sent bytes.
     */
    public abstract int sendData(byte[] buffer, int len);

    /**
     * Function to send data on the port
     *
     * @param buffer the data send.
     * @return the number of sent bytes.
     */
    public int sendData(byte[] buffer) {
        if (buffer == null) {
            throw new IllegalArgumentException("Supplied buffer cannot be null.");
        }
        return sendData(buffer, buffer.length);
    }

    /**
     * Function to send data on the port.
     *
     * @param buffer the data to be sent.
     * @return the number of sent bytes.
     */
    public int sendData(String buffer) {
        if (buffer == null) {
            throw new IllegalArgumentException("Supplied buffer cannot be null.");
        }
        return sendData(buffer.getBytes(), buffer.length());
    }

    /**
     * Function to set the port opened.
     *
     * @param value true to set the port open, false otherwise.
     */
    protected void setOpen(boolean value) {
        this.open = value;
    }

    /**
     * Function to set the response buffer.
     *
     * @param buffer the response buffer to set.
     */
    protected void setResponseBuffer(byte[] buffer) {
        this.responseBuffer = buffer;
    }

    /**
     * Function to set RTS on a COM device.
     *
     * @param enable desired value.
     * @throws IOException when it is not possible to set RTS.
     */
    public void setRTS(boolean enable) throws IOException {

    }

    /**
     * Function to set the port flag od sending messages.
     *
     * @param value true to set the flag, false otherwise.
     */
    protected void setSendingMessages(boolean value) {
        this.sendingMessages = value;
    }
}
