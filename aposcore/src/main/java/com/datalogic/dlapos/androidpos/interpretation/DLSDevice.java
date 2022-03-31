package com.datalogic.dlapos.androidpos.interpretation;

import android.content.Context;

import com.datalogic.dlapos.androidpos.common.DLSCConfig;
import com.datalogic.dlapos.androidpos.common.DLSDeviceInfo;
import com.datalogic.dlapos.androidpos.common.DLSException;
import com.datalogic.dlapos.androidpos.common.DLSJposConst;
import com.datalogic.dlapos.androidpos.common.DLSObjectFactory;
import com.datalogic.dlapos.androidpos.common.DLSProperties;
import com.datalogic.dlapos.androidpos.common.DLSState;
import com.datalogic.dlapos.androidpos.transport.DLSPort;
import com.datalogic.dlapos.androidpos.transport.DLSUsbPort;
import com.datalogic.dlapos.androidpos.transport.DataReceivedListener;
import com.datalogic.dlapos.androidpos.transport.DeviceAddedListener;
import com.datalogic.dlapos.androidpos.transport.DeviceArrivalListener;
import com.datalogic.dlapos.androidpos.transport.DeviceReattachedListener;
import com.datalogic.dlapos.androidpos.transport.DeviceRemovedListener;
import com.datalogic.dlapos.androidpos.transport.UsbPortStatusListener;
import com.datalogic.dlapos.commons.constant.ErrorConstants;
import com.datalogic.dlapos.commons.support.APosException;
import com.datalogic.dlapos.commons.upos.RequestListener;
import com.datalogic.dlapos.confighelper.DLAPosConfigHelper;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;

/**
 * {@code DLSDevice} is the base class for all JavaPOS devices.  The class
 * contains methods and attributes related to Scanners and Scales.
 */
public abstract class DLSDevice
        implements DataReceivedListener,
        DeviceAddedListener,
        DeviceArrivalListener,
        DeviceReattachedListener,
        DeviceRemovedListener {

    /**
     * The default timeout.
     */
    public static final int DEFAULT_TIMEOUT = 5000;

    private final ArrayList<Object> deviceErrorListeners;
    private final ArrayList<Object> deviceStatusListeners;
    private final ArrayList<Object> directIODataListeners;
    protected final ArrayList<Object> messageList;

    protected DLSDeviceInfo deviceInfo;
    private DLSState deviceState;
    protected DLSPort port;
    private String logicalName;
    protected final ByteArrayOutputStream outputStream;
    protected DLSProperties properties;
    protected int commandTimeout;
    protected final String separator;
    protected Context context;
    protected HashMap<String, Object> statistics;

    /**
     * Constructor
     */
    public DLSDevice() {
        this.deviceErrorListeners = new ArrayList<>();
        this.deviceStatusListeners = new ArrayList<>();
        this.directIODataListeners = new ArrayList<>();
        this.messageList = new ArrayList<>();
        this.outputStream = new ByteArrayOutputStream();
        this.deviceInfo = new DLSDeviceInfo();
        this.deviceState = DLSState.CLOSED;
        this.separator = System.getProperty("line.separator");
    }


    //region Listeners

    /**
     * Adds a DeviceErrorStatusListener to the collection of Error Listeners.
     *
     * @param listener DeviceErrorStatusListener instance to add
     */
    public void addDeviceErrorListener(DeviceErrorStatusListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("DeviceErrorStatusListener instance is null.");
        }
        this.deviceErrorListeners.add(listener);
    }

    /**
     * Adds a DeviceErrorStatusListener to the collection of Status Listeners.
     *
     * @param listener DeviceErrorStatusListener instance to add
     */
    public void addDeviceStatusListener(DeviceErrorStatusListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("DeviceErrorStatusListener instance is null.");
        }
        this.deviceStatusListeners.add(listener);
    }

    /**
     * Adds a DirectIODataListener to the collection of Direct I/O listeners.
     *
     * @param listener DirectIODataListener instance to add
     */
    public void addDirectIODataListener(DirectIODataListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("DirectIODataListener instance is null.");
        }
        this.directIODataListeners.add(listener);
    }

    /**
     * Removes a DeviceErrorStatusListener from the collection of Error
     * listeners.
     *
     * @param listener DeviceErrorStatusListener to remove
     */
    public boolean removeDeviceErrorListener(DeviceErrorStatusListener listener) {
        return this.deviceErrorListeners.remove(listener);
    }

    /**
     * Removes a DeviceErrorStatusListener from the collection of Status
     * listeners.
     *
     * @param listener DeviceErrorStatusListener to remove
     */
    public boolean removeDeviceStatusListener(DeviceErrorStatusListener listener) {
        return this.deviceStatusListeners.remove(listener);
    }

    /**
     * Removes a DirectIODataListener from the collection of Direct I/O Data
     * listeners.
     *
     * @param listener DirectIODataListener to remove
     */
    public boolean removeDirectIODataListener(DirectIODataListener listener) {
        return this.directIODataListeners.remove(listener);
    }

    //endregion

    /**
     * Returns a block check character (BCC) for the supplied message.
     *
     * @param message String containing the message.
     * @return char indicating the block check character for the message.
     */
    protected char blockCheckCharacter(String message) {
        char bcc = 0;
        if (message == null) {
            return bcc;
        }

        int len = message.length();
        for (int i = 0; i < len; i++) {
            bcc ^= message.charAt(i);
        }
        return bcc;
    }

    /**
     * Claims a device.
     * <p>
     * This creates and opens a port, adds the appropriate event listeners, and
     * calls the internalClaim method.
     *
     * @param timeout long indicating the claim timeout in milliseconds
     * @throws DLSException thrown if the port cannot be opened or an invalid
     *                      state transition is attempted
     */
    public void claim(long timeout) throws DLSException {
        throw new UnsupportedOperationException();
    }

    public void claim(RequestListener listener) throws DLSException {
        if (this.port != null && this.port.isOpen()) {
            this.port.closePort();
            release();
        }
        setPort(DLSObjectFactory.createPort(getDeviceInfo(), context));
        ((DLSUsbPort) port).registerPortStatusListener(new PortListener(listener));

        if (!this.port.openPort()) {
            throw new DLSException(DLSJposConst.DLS_E_OPENPORT, "Port open error.");
        }
    }

    /**
     * Closes a device.
     * <p>
     * removes the device from the Port's data listener collection and closes
     * the Port.
     *
     * @throws DLSException thrown on an invalid state transition
     */
    public void close() throws DLSException {
        if (port != null && port.isOpen()) {
            port.removeDataReceivedListener(this);
            port.closePort();
            setPort(null);
        }
        setState(DLSState.CLOSED);
    }

    /**
     * Concatenates USB messages (intended as a helper method for subclasses).
     * End of the label is in {@code inBuf} with the bEOD flag set.
     *
     * @param inBuf byte array containing the message to concatenate
     * @param eod   boolean indicating whether this is the end of the
     *              transmission
     */
    protected void concatUSBMessages(byte[] inBuf, boolean eod) {
        if (inBuf == null) {
            throw new IllegalArgumentException("InBuf can not be null");
        }

        for (byte b : inBuf) {
            outputStream.write(b);
        }

        if (eod) {
            byte[] tmpBuf = outputStream.toByteArray();
            messageList.add(tmpBuf);
            outputStream.reset();
        }
    }

    /**
     * Detects when a successful reset has been completed.
     * This method returns when communication with the device has been
     * re-established or timeout has been reached.
     * <p>
     * <b> Important: this method does not cause or initiate a reset; only
     * detects when one is completed. It is necessary to call {@link #reset()}
     * prior to this method. </b>
     * <p>
     * Example Usage: <pre>
     *      try {
     *          scanner.reset();
     *      catch {DLSException e} {
     *          //handle exception
     *      }
     *      boolean resetSuccess = scanner.detectResetFinish();
     * </pre>
     *
     * @return boolean indicating reset success. {@code true} - if device
     * returns before timeout, {@code false} - if timeout is reached
     */
    public abstract boolean detectResetFinish() throws DLSException;

    /**
     * Sends a Direct I/O command to the device.
     * <p>
     * Depending upon the interface and the Direct I/O command being
     * executed, the {@code data} parameter may not need to be populated and
     * {@code null} can be used (refer to the JavaPOS user guide for specific
     * implementation). Any result messages or data will be passed back through
     * the {@code ByteArrayOutputStream} object.
     * <p>
     * Example Usage: <pre>
     *      {@code
     *      ByteArrayOuputStream bs = new ByteArrayOutputStream();
     *      scanner.directIO(DLSJposConst.DIO_SCALE_STATUS, dioData, bs);
     *      String result = bs.toString();}
     * </pre>
     *
     * @param command int indicating the Direct I/O command to perform
     * @param data    int array containing any data to pass with the command. The
     *                array may be populated with the status of the command upon
     *                completion.
     * @param object  ByteArrayOutputStream object used to return messages
     *                from performing the command.
     * @throws DLSException if an error occurred during execution, or an
     *                      invalid command argument was supplied
     */
    public abstract void directIO(int command, int[] data, Object object) throws DLSException;

    /**
     * Disables the device.
     *
     * @throws DLSException if invalid state transition is attempted
     */
    public abstract void disable() throws DLSException;

    /**
     * Performs a Health Check on the device. If check fails or timeout occurs,
     * method returns {@code false}
     *
     * @param timeout int indicating the amount of time to wait for the health
     *                check to complete
     * @return boolean indicating if scanner passed the health check
     * @throws DLSException if an error occurs
     */
    public abstract boolean doHealthCheck(int timeout) throws DLSException;

    /**
     * Performs a Health Check on the device. If check fails or timeout occurs,
     * method returns {@code false}. Default timeout is used.
     *
     * @return boolean indicating if scanner passed the health check
     * @throws DLSException if an error occurs
     */
    public boolean doHealthCheck() throws DLSException {
        return doHealthCheck(DEFAULT_TIMEOUT);
    }

    /**
     * Performs a self check on the device.
     *
     * @throws DLSException if an error occurs
     */
    public abstract void doSelfTest() throws DLSException;

    /**
     * Enables the device.
     *
     * @throws DLSException if invalid state transition is attempted
     */
    public abstract void enable() throws DLSException;

    /**
     * Invokes any registered DeviceErrorStatusListener instances, calling
     * the onDeviceError method with the indicated error code.
     *
     * @param errorCode int indicating the error code
     */
    protected void fireDeviceErrorEvent(int errorCode) {
        DeviceErrorStatusListener listener;
        for (Enumeration e = Collections.enumeration(deviceErrorListeners); e.hasMoreElements(); ) {
            listener = (DeviceErrorStatusListener) e.nextElement();
            listener.onDeviceError(errorCode);
        }
    }

    /**
     * Invokes any registered DeviceErrorStatusListener instances, calling
     * the onDeviceStatus method with the indicated error code.
     *
     * @param statusCode int indicating the status code
     */
    protected void fireDeviceStatusEvent(int statusCode) {
        DeviceErrorStatusListener listener;
        for (Enumeration e = Collections.enumeration(deviceStatusListeners); e.hasMoreElements(); ) {
            listener = (DeviceErrorStatusListener) e.nextElement();
            listener.onDeviceStatus(statusCode);
        }
    }

    /**
     * Invokes any registered DirectIODataListener instances, calling the
     * onDirectIOData method with the indicated command and data buffer.
     *
     * @param cmd int indicating the Direct I/O command
     * @param buf byte array containing the Direct I/O data
     */
    protected void fireDirectIODataEvent(int cmd, byte[] buf) {
        DirectIODataListener listener;
        for (Enumeration e = Collections.enumeration(directIODataListeners); e.hasMoreElements(); ) {
            listener = (DirectIODataListener) e.nextElement();
            listener.onDirectIOData(cmd, buf);
        }
    }

    /**
     * Returns the current configuration of the device.
     *
     * @return DLSCConfig instance containing the configuration of the device
     * @see DLSCConfig
     */
    public abstract DLSCConfig getConfiguration();

    /**
     * Indicates whether the device state is currently set to enabled.
     *
     * @return boolean indicating whether the device is enabled
     */
    public boolean getDeviceEnabled() {
        return getState() == DLSState.ENABLED;
    }

    public ArrayList<Object> getDeviceErrorListeners() {
        return deviceErrorListeners;
    }

    /**
     * Returns the DLSDeviceInfo instance containing the currently loaded
     * device configuration from {@code apos.json}.
     *
     * @return DLSDeviceInfo containing the current configuration of a device.
     * @see DLSDeviceInfo
     */
    public DLSDeviceInfo getDeviceInfo() {
        return deviceInfo;
    }

    /**
     * Function to get all device status listeners.
     *
     * @return device status listeners.
     */
    public ArrayList<Object> getDeviceStatusListeners() {
        return deviceStatusListeners;
    }

    /**
     * Function to get all direct IO data listeners.
     *
     * @return direct IO data listeners.
     */
    public ArrayList<Object> getDirectIODataListeners() {
        return directIODataListeners;
    }

    /**
     * Returns the assigned logical name of a device.
     *
     * @return String containing the logical name of a device
     */
    public String getLogicalName() {
        return this.logicalName;
    }

    /**
     * Returns the current port instance associated with the device.
     *
     * @return {@code DLSPort} instance
     */
    public DLSPort getPort() {
        return this.port;
    }

    /**
     * Return the current device state, enumerated to one of DLSState values:
     * <p>
     * {@code CLOSED, OPENED, CLAIMED, ENABLED, DISABLED, RELEASED}
     *
     * @return DLSState enumeration containing the current state of the device
     * @see DLSState
     */
    public synchronized DLSState getState() {
        return this.deviceState;
    }

    /**
     * Retrieves the information, health, and statistics data from the device.
     *
     * @return {@literal HashMap<String, Object>} containing the statistics data
     * @throws DLSException if an error occurs retrieving the information
     */
    public HashMap<String, Object> getStatistics() throws DLSException {
        throw new DLSException(DLSJposConst.DLS_E_NOTSUPPORTED,
                "Statistics are not supported for this device.");
    }

    /**
     * Indicates whether a device supports Statistics reporting.
     *
     * @return boolean indicating whether the device supports Statistics
     * Reporting
     */
    public boolean hasStatisticsReporting() {
        return false;
    }

    void internalClaim() throws DLSException {
    }

    void internalRelease() {
    }

    /**
     * Indicates whether the device is alive.
     *
     * @return boolean indicating whether a device is alive
     * @throws DLSException not thrown
     */
    public abstract boolean isAlive() throws DLSException;

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDeviceAdded() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDeviceArrival() {
    }

    /**
     * Assigns the logical name of a device and loads the DeviceInfo instance of
     * the specified device from the {@code apos.json} file.
     *
     * @param logicalName Containing the logical name indicating the
     *                    configuration of the device in the {@code apos.json}
     *                    file
     * @param context     The application context.
     * @throws DLSException if an invalid state transition occurs
     */
    public void open(String logicalName, Context context) throws DLSException {
        if (context == null || logicalName == null) {
            throw new IllegalArgumentException("Context and logicalName can not be null");
        }

        this.context = context;
        this.properties = DLSProperties.getInstance(context);
        this.commandTimeout = this.properties.getCommandTimeout();
        setLogicalName(logicalName);
        try {
            DLSDeviceInfo info = new DLSDeviceInfo();
            if (!DLAPosConfigHelper.getInstance(context).isInitialized())
                DLAPosConfigHelper.getInstance(context).initialize(context);
            info.loadConfiguration(logicalName, DLAPosConfigHelper.getInstance(context).getProfileManager());
            this.deviceInfo = info;
        } catch (APosException e) {
            throw new DLSException(ErrorConstants.APOS_E_FAILURE, "Can not get a profile for this device: \n" + e.getMessage());
        }
    }

    /**
     * Parses a message and adds it to the message list.
     *
     * @param inBuf   byte array containing the message to parse
     * @param endChar byte indicating the character that ends a message
     * @param useBCC  boolean indicating whether to use a block check character
     */
    protected void parseMessages(byte[] inBuf, byte endChar, boolean useBCC) {
        DLSDeviceInfo oInfo = getDeviceInfo();
        boolean bUseCR_OR_LF = oInfo.getOptionAsBool("useSunJavaxComm");//TODO: do we have to keep it?
        boolean needBCC = false;

        for (int i = 0; i < inBuf.length; i++) {
            outputStream.write(inBuf[i]);
            // If we still need a block check character (remember, it may not come
            //  in here at the same time as the rest of the message - may be on its own.
            if (needBCC) {
                needBCC = false;                         // Set flag to got it.
                byte[] tmpBuf = outputStream.toByteArray();         // Retrieve data
                // Add to message list
                messageList.add(tmpBuf);
                outputStream.reset();                               // Reset buffer
            } else if ((inBuf[i] == endChar) || (bUseCR_OR_LF && (inBuf[i] == 0x0a))) {
                // If this is the end character
                if (useBCC) {
                    // Set flag to need block check character.
                    needBCC = true;
                } else {
                    /*
                     * JAVAPOS_SWCR_329 - Only make a new message when it is not
                     * the last 0x0D or 0x0A in the packet.
                     */
                    if (i < inBuf.length - 1) {
                        if (endChar == 0x0A || endChar == 0x0D) {
                            continue;
                        }
                    }
                    // Otherwise we are done with this message.
                    byte[] tmpBuf = outputStream.toByteArray();
                    // Add message to list.
                    messageList.add(tmpBuf);
                    outputStream.reset();
                }
            }
        }
    }

    /**
     * Parses a message and adds it to the message list. No block check
     * character used by default.
     *
     * @param inBuf   byte array containing the message to parse
     * @param endChar byte indicating the character that ends a message
     */
    protected void parseMessages(byte[] inBuf, byte endChar) {
        parseMessages(inBuf, endChar, false);
    }

    /**
     * Release the device, closing the port.
     *
     * @throws DLSException thrown on an invalid state transition
     */
    public void release() throws DLSException {
        DLSPort port = getPort();
        if (port == null) {
            throw new DLSException(ErrorConstants.APOS_E_ILLEGAL, "Port is null");
        }

        if (port.isOpen()) {
            port.closePort();
        } else {
            port.removeInternalListeners();
        }
        port.removeDataReceivedListener(this);
        port.removeDeviceRemovedListener(this);
        port.removeDeviceReattachedListener(this);
        internalRelease();
        setState(DLSState.RELEASED);
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
    public abstract void reset() throws DLSException;

    /**
     * Assigns the logical name of a device. Logical name is also set in
     * {@link #open(String, Context)}.
     *
     * @param name String containing the logical name to assign
     */
    public void setLogicalName(String name) {
        this.logicalName = name;
    }

    protected void setPort(DLSPort port) {
        this.port = port;
    }

    /**
     * Sets the current device state.
     *
     * @param state DLSState enumeration to transition to
     * @throws DLSException thrown when an invalid state transition is attempted
     */
    public synchronized void setState(DLSState state) throws DLSException {
        if (state == this.deviceState) {
            return;
        }
        switch (state) {
            case CLOSED:
            case CLAIMED:
                if (deviceState != DLSState.OPENED && deviceState != DLSState.RELEASED) {
                    throw new DLSException(DLSJposConst.DLS_E_STATE_TRANSITION,
                            "Unable to transition to state: " + state);
                }
                break;
            case OPENED:
                if (deviceState != DLSState.CLOSED) {
                    throw new DLSException(DLSJposConst.DLS_E_STATE_TRANSITION,
                            "Unable to transition to state: " + state);
                }
                break;
            case ENABLED:
                if (deviceState != DLSState.CLAIMED && deviceState != DLSState.DISABLED) {
                    throw new DLSException(DLSJposConst.DLS_E_STATE_TRANSITION,
                            "Unable to transition to state: " + state);
                }
                break;
            case DISABLED:
                if (deviceState != DLSState.ENABLED && deviceState != DLSState.CLAIMED) {
                    throw new DLSException(DLSJposConst.DLS_E_STATE_TRANSITION,
                            "Unable to transition to state: " + state);
                }
                break;
            case RELEASED:
                if (deviceState != DLSState.DISABLED && deviceState != DLSState.CLAIMED) {
                    throw new DLSException(DLSJposConst.DLS_E_STATE_TRANSITION,
                            "Unable to transition to state: " + state);
                }
                break;
        }
        deviceState = state;
    }

    private class PortListener implements UsbPortStatusListener {

        private final RequestListener _requestListener;
        private boolean _firstRequest;

        PortListener(RequestListener listener) {
            _requestListener = listener;
            _firstRequest = true;
        }

        /**
         * Function to notify the opening of the port.
         */
        @Override
        public void portOpened() {
            try {
                if (getState() != DLSState.ENABLED)
                    setState(DLSState.CLAIMED);
            } catch (DLSException e) {
                _requestListener.onFailure("Error switching state from " + getState().name() + " to CLAIMED");
                return;
            }
            port.addDataReceivedListener(DLSDevice.this);
            port.addDeviceRemovedListener(DLSDevice.this);
            port.addDeviceReattachedListener(DLSDevice.this);
            try {
                internalClaim();
            } catch (DLSException e) {
                try {
                    if (getState() == DLSState.ENABLED)
                        deviceState = DLSState.RELEASED;
                    release();
                } catch (DLSException dlsException) {
                    _requestListener.onFailure("Error claiming the port, device may not work properly: " + e.getMessage());
                }
                _requestListener.onFailure("Error claiming the port, device may not work properly, retry: " + e.getMessage());
                return;
            }
            if (_firstRequest) {
                _requestListener.onSuccess();
                _firstRequest = false;
            }
        }

        /**
         * Function to notify an error on the port.
         *
         * @param status The error status of the failure.
         */
        @Override
        public void portError(ErrorStatus status) {
            _requestListener.onFailure("Error claiming the port: " + status.name());
        }

        /**
         * Function to notify the closing of the port.
         */
        @Override
        public void portClosed() {

        }
    }
}
