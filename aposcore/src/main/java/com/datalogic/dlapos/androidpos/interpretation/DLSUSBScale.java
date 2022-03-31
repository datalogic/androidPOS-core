package com.datalogic.dlapos.androidpos.interpretation;

import android.util.Log;

import com.datalogic.dlapos.androidpos.common.DLSException;
import com.datalogic.dlapos.androidpos.common.DLSJposConst;
import com.datalogic.dlapos.androidpos.common.DLSObjectFactory;
import com.datalogic.dlapos.androidpos.common.DLSScaleConfig;
import com.datalogic.dlapos.androidpos.common.DLSState;
import com.datalogic.dlapos.androidpos.common.DLSStatistics;
import com.datalogic.dlapos.androidpos.transport.DLSUsbPort;
import com.datalogic.dlapos.androidpos.transport.UsbPortStatusListener;
import com.datalogic.dlapos.commons.constant.CommonsConstants;
import com.datalogic.dlapos.commons.constant.ScaleConstants;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Class representing a generic Datalogic scale connected using an USB port.
 */
public class DLSUSBScale extends DLSScale {

    private final static String Tag = "DLSUSBScale";

    static final byte[] performSelfTestCmd = {0x0, 0x10, 0, 0, 0};
    static final byte[] statusRequestCmd = {0x0, 0x20, 0, 0, 0};
    static final byte[] resetCmd = {0, 0x40, 0, 0, 0};
    static final byte[] englishWeightCmd = {0x01, 0, 0, 0, 0};
    static final byte[] metricWeightCmd = {0x02, 0, 0, 0, 0};
    static final byte[] zeroScaleCmd = {0x03, 0, 0, 0, 0};
    static final byte[] enable3ByteStatusCmd = {0x04, 0, 0, 0, 0};
    static final byte[] disable3ByteStatusCmd = {0x05, 0, 0, 0, 0};
    static final byte[] clearRemoteDisplayCmd = {0x06, 0, 0, 0, 0};
    final byte[] configureScaleCmd = {0x20, 0, 0, 0, 0};
    static final byte[] reportScaleConfigCmd = {0x21, 0, 0, 0, 0};
    // Status byte one from scale
    private static final int RESP_FIRMWARE_FLASH = 0x00000001;
    private static final int RESP_CONFIG_DATA = 0x00000002;
    private static final int RESP_XTEND_STATUS = 0x00000004;
    static final int RESP_CMD_ERROR = 0x00000040;
    static final int RESP_NOT_READY = 0x00000080;
    // status byte two from scale
    private static final int RESP_METRIC = 0x00000100;
    static final int RESP_FIVE_DIGIT = 0x00000200;
    static final int RESP_NO_WEIGHT = 0x00000400;
    private static final int RESP_DATA_ERROR = 0x00000800;
    private static final int RESP_READ_ERROR = 0x00001000;
    private static final int RESP_NO_DISPLAY = 0x00002000;
    private static final int RESP_HARDWARE_ERROR = 0x00004000;
    private static final int RESP_CMD_REJECT = 0x00008000;
    // status byte three from scale
    private static final int RESP_CONFIG_SUCCESS = 0x00010000;
    static final int RESP_UNDER_ZERO = 0x00020000;
    static final int RESP_OVER_CAPACITY = 0x00040000;
    static final int RESP_CENTER_OF_ZERO = 0x00080000;
    private static final int RESP_REQUIRES_ZEROING = 0x00100000;
    private static final int RESP_SCALE_WARMUP = 0x00200000;
    private static final int RESP_DUPLICATE_WEIGHT = 0x00400000;
    private final Object reportResp = new Object();
    boolean bReportResp = false;
    //private static final int reportTimeout = 2000;
    private final Object updateResp = new Object();
    boolean bUpdateResp = false;
    private static final int UPDATE_TIMEOUT = 2000;
    private static final int STATUS_TIMEOUT = 1000;
    private final Object statusResp = new Object();
    volatile boolean bStatusResp = false;
    private static final int LIVE_TIMEOUT = 1000;
    volatile int nLiveResp = 0;
    byte[] dataBuf = null;
    volatile boolean bDeviceReady = false;
    //private boolean bDeviceEnabled = false;
    byte nStat0, nStat1, nStat2;
    private final Object objStatus = new Object();
    boolean bGotStatus = false;
    private boolean bGetWeight = false;
    boolean bZeroValid = true;
    boolean bDioCommand = false;
    // Live Weight after that.
    private Thread monitor = null;

    private final Object resetObj = new Object();
    private boolean resetSuccess = false;
    private volatile boolean hotPlugged = true;
    private final ScheduledExecutorService m_scheduler = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> m_resetWatcher = null;

    private static final byte[] EMPTY_BYTE_ARRAY = {};


    /**
     * Creates a new instance of DLSUSBScale
     */
    public DLSUSBScale() {
        super();
    }


    /**
     * {@inheritDoc}
     *
     * @return always {@code true}
     */
    @Override
    public boolean canZeroScale() {
        return true;
    }

/*    @Override
    public void claim(long timeout) throws DLSException {
        if (this.port != null && this.port.isOpen()) {
            this.port.closePort();
            release();
        }
        setPort(DLSObjectFactory.createPort(getDeviceInfo(), context));
        ((DLSUsbPort) port).registerPortStatusListener(this);

        if (!this.port.openPort()) {
            throw new DLSException(DLSJposConst.DLS_E_OPENPORT, "Port open error.");
        }
    }*/


    /**
     * Clears the remote scale display.
     * <p>
     * Only supported when the scale is in United Kingdom operation mode. If the
     * scale is in United States operation mode, this method has no effect.
     *
     * @throws DLSException {@inheritDoc}
     */
    @Override
    public void clearDisplay() throws DLSException {
        if (DLSJposConst.updateInProgress) {
            return;
        }
        port.sendData(clearRemoteDisplayCmd, clearRemoteDisplayCmd.length);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean detectResetFinish() {
        long timeout = properties.getResetTimeout();
        long elapsed = 0;
        long start;
        boolean result;
        hotPlugged = false;
        m_resetWatcher = m_scheduler.scheduleAtFixedRate(new resetWatcherThread(), 1000, 1000, TimeUnit.MILLISECONDS);

        synchronized (resetObj) {
            resetSuccess = false;
            start = System.nanoTime();
            try {
                while (!resetSuccess) {
                    resetObj.wait(timeout - elapsed);
                    elapsed = (long) ((System.nanoTime() - start) / 1000000);
                    if (elapsed >= timeout) {
                        break;
                    }
                }
            } catch (InterruptedException ie) {
                Log.e(Tag, "detectResetFinish: Interrupted exception, ", ie);
            }
            result = resetSuccess;
        }

        if (java.io.File.separatorChar != '\\') {
            m_resetWatcher.cancel(false);
        }
        return result;
    }


    /**
     * {@inheritDoc}
     * <p>
     * Supported commands: <ul>
     * <li> {@code DIO_SCALE_RESET}
     * <li> {@code DIO_SCALE_ENABLE3BYTESTATUS}
     * <li> {@code DIO_SCALE_DISABLE3BYTESTATUS}
     * <li> {@code DIO_SCALE_CONFIGURE}
     * <li> {@code DIO_SCALE_REPORT_CONFIG}
     * <li> {@code DIO_DEV_PROTOCOL}
     * </ul>
     *
     * @see DLSJposConst
     */
    @Override
    public void directIO(int command, int[] data, Object object) throws DLSException {
        String sOut;
        long timeout;
        long elapsed;
        long start;
        // Commands as per the SRS 2.2 doc
        if (DLSJposConst.updateInProgress) {
            return;
        }
        if (data == null || data.length == 0) {
            data = new int[1];
        }
        byte[] buf = EMPTY_BYTE_ARRAY;
        bDioCommand = true;
        switch (command) {
            case DLSJposConst.DIO_SCALE_RESET:                                       // Reset
                // In the case of a USB reset, the device will get sudo removed, then re-attached,
                // we need to be able to allow for this, and allow the device to get re-attached.
//                bSaveLiveWeight = isLiveWeight();
//                saveStatusNotify = getStatusNotify();
//                setStatusNotify(ScaleConstants.SCAL_SN_DISABLED);

                reset();
                if (detectResetFinish()) {
                    buf = "Reset successful".getBytes();
                } else {
                    buf = "Unable to communicate with scale after reset, please check device.".getBytes();
                }
                break;
            case DLSJposConst.DIO_SCALE_ENABLE3BYTESTATUS:                                       // Enable 3 byte status
                timeout = commandTimeout * 2L;
                elapsed = 0;
                synchronized (objStatus) {
                    bGotStatus = false;
                    port.sendData(enable3ByteStatusCmd);
                    start = System.nanoTime();
                    try {
                        while (!bGotStatus) {
                            objStatus.wait(timeout - elapsed);
                            elapsed = (long) ((System.nanoTime() - start) / 1000000);
                            if (elapsed >= timeout) {
                                break;
                            }
                        }
                    } catch (InterruptedException ie) {
                        Log.e(Tag, "directIO: Interrupted exception, ", ie);
                    }
                    data[0] = 0;
                    sOut = String.format("Status: 0x%02X 0x%02X 0x%02X", nStat0, nStat1, nStat2);
                }
                buf = sOut.getBytes();
                break;
            case DLSJposConst.DIO_SCALE_DISABLE3BYTESTATUS:                                       // Disable 3 byte status
                timeout = commandTimeout * 2L;
                elapsed = 0;
                synchronized (objStatus) {
                    bGotStatus = false;
                    port.sendData(disable3ByteStatusCmd);
                    start = System.nanoTime();
                    try {
                        while (!bGotStatus) {
                            objStatus.wait(timeout - elapsed);
                            elapsed = (long) ((System.nanoTime() - start) / 1000000);
                            if (elapsed >= timeout) {
                                break;
                            }
                        }
                    } catch (InterruptedException ie) {
                        Log.e(Tag, "directIO: Interrupted exception, ", ie);
                    }
                    data[0] = 0;
                    sOut = String.format("Status: 0x%02X 0x%02X 0x%02X", nStat0, nStat1, nStat2);
                }
                buf = sOut.getBytes();
                break;
            case DLSJposConst.DIO_SCALE_CONFIGURE:                                       // Configure scanner
                if (data.length < 3) {                 // If length of array is long enough
                    throw new DLSException(DLSJposConst.DLS_E_INVALID_ARG, "Data array, invalid size");
                }
                buf = new byte[3];
                configureScaleCmd[2] = (byte) data[0];
                configureScaleCmd[3] = (byte) data[1];
                configureScaleCmd[4] = (byte) data[2];
                timeout = commandTimeout * 2L;
                elapsed = 0;

                synchronized (objStatus) {
                    bGotStatus = false;
                    port.sendData(configureScaleCmd);
                    port.sendData(statusRequestCmd);
                    start = System.nanoTime();
                    try {
                        while (!bGotStatus) {
                            objStatus.wait(timeout - elapsed);
                            elapsed = (long) ((System.nanoTime() - start) / 1000000);
                            if (elapsed >= timeout) {
                                break;
                            }
                        }
                    } catch (InterruptedException ie) {
                        Log.e(Tag, "directIO: Interrupted exception, ", ie);
                    }
                    buf[0] = nStat0;                       // Transfer status byte 1
                    buf[1] = nStat1;                       // Transfer status byte 2
                    buf[2] = nStat2;                       // Transfer status byte 3
                }
                break;
            case DLSJposConst.DIO_SCALE_REPORT_CONFIG:                                       // Report configuration
                reportConfiguration();
                if (dataBuf.length != 0) {
                    buf = new byte[3];
                    buf[0] = (byte) dataBuf[3];
                    buf[1] = (byte) dataBuf[4];
                    buf[2] = (byte) dataBuf[5];
                    data[0] = 0;
                } else {
                    data[0] = 1;
                    buf = "Unable to retrieve configuration data".getBytes();
                }
                break;

            case DLSJposConst.DIO_DEV_PROTOCOL:
                data[0] = 0;
                buf = "USB".getBytes();
                break;

            default:
                throw new DLSException(DLSJposConst.DLS_E_INVALID_ARG, "Invalid directio command received");
        }

        if (buf.length != 0) {  // If there is data available to return to caller.
            // A ByteArrayOutputStream object must be passed in, that can be used to write
            //  the data to, for return.
            try {
                ByteArrayOutputStream oBs = (ByteArrayOutputStream) object;
                oBs.reset(); // must reset so that the data can overwrite the object
                oBs.write(buf);
            } catch (IOException e) {
                throw new DLSException(DLSJposConst.DLS_E_INVALID_ARG, "Need a ByteArrayOutStream object to return data");
            }
        }

    }


    /**
     * {@inheritDoc}
     * Additionally stopping live weight if running.
     */
    @Override
    public void disable() throws DLSException {
        synchronized (objStatus) {
            stopLiveWeight();
        }
        setState(DLSState.DISABLED);
    }


    /**
     * Performs a pseudo health check operation - synchronously
     * initiates a status request.
     * <p>
     * There is no true health check for a scale on this interface. If a health
     * check is  desired, it should be initiated through the scanner.
     *
     * @return
     * @see DLSUSBScanner#doHealthCheck()
     */
    @Override
    public boolean doHealthCheck(int timeout) {
        boolean bRc;
        long elapsed = 0;
        long start;
        synchronized (statusResp) {
            bStatusResp = false;
            bDeviceReady = false;
            port.sendData(statusRequestCmd);
            start = System.nanoTime();
            try {
                while (!bStatusResp) {
                    statusResp.wait(timeout - elapsed);
                    elapsed = (long) ((System.nanoTime() - start) / 1000000);
                    if (elapsed >= timeout) {
                        break;
                    }
                }
            } catch (InterruptedException ie) {
                Log.e(Tag, "doHealthCheck: Interrupted exception, ", ie);
            }
            bRc = bDeviceReady;

        }
        return bRc;

    }


    /**
     * {@inheritDoc}
     * Self test response format is the same as a status request for USB scales.
     */
    @Override
    public void doSelfTest() {
        if (DLSJposConst.updateInProgress) {
            return;
        }
        long timeout = commandTimeout;
        long elapsed = 0;
        long start;
        synchronized (statusResp) {
            bStatusResp = false;
            port.sendData(performSelfTestCmd);
            start = System.nanoTime();
            try {
                while (!bStatusResp) {
                    statusResp.wait(timeout - elapsed);
                    elapsed = (long) ((System.nanoTime() - start) / 1000000);
                    if (elapsed >= timeout) {
                        break;
                    }
                }
            } catch (InterruptedException ie) {
                Log.e(Tag, "doSelfTest: Interrupted exception, ", ie);
            }
        }
    }


    /**
     * {@inheritDoc}
     * Additionally starting live weight if status notify is enabled.
     */
    @Override
    public void enable() throws DLSException {
        if (DLSJposConst.updateInProgress) {
            return;
        }
        setState(DLSState.ENABLED);
        if (getStatusNotify() == ScaleConstants.SCAL_SN_ENABLED) {
            if (isLiveWeight()) {
                return;
            }

            startLiveWeight();
        }
    }


    /**
     * Turns on 3 byte status responses for this scale.
     */
    protected void enable3ByteStatus() {
        if (DLSJposConst.updateInProgress) {
            return;
        }
        long timeout = STATUS_TIMEOUT;
        long elapsed = 0;
        long start;
        synchronized (statusResp) {
            bStatusResp = false;
            port.sendData(enable3ByteStatusCmd);
            start = System.nanoTime();
            try {
                while (!bStatusResp) {
                    statusResp.wait(timeout - elapsed);
                    elapsed = (long) ((System.nanoTime() - start) / 1000000);
                    if (elapsed >= timeout) {
                        break;
                    }
                }
            } catch (InterruptedException ie) {
                Log.e(Tag, "enable3ByteStatus: Interrupted exception, ", ie);
            }
        }
    }


    /**
     * Processes the weight data and fires a weight event to all listeners.
     * This method is required for correctly interpreting the weight data on USB
     * interfaces.
     * <p>
     * {@link #fireWeightReceivedEvent(int)} is invoked through this method.
     *
     * @param nResp integer indicating the 3-byte status response
     * @param inBuf byte array containing raw scale response to a weight
     *              request
     */
    protected void fireWeightEvent(int nResp, byte[] inBuf) {
        int digits = 4;                         // Default number of weight digits
        if (((nResp & RESP_FIVE_DIGIT) == RESP_FIVE_DIGIT)
                || (nResp & RESP_METRIC) == RESP_METRIC) { // If five digits is on
            digits = 5;                             // Set number of digits to 5
        }
        StringBuilder oSb = new StringBuilder();
        for (int i = 0; i < digits; i++) {        // Transfer data from inbuffer
            oSb.append(inBuf[i + 3]);
        }

        int nWeight = Integer.parseInt(oSb.toString());
        if (digits == 4) {                    // Always return 5 digit weights
            nWeight *= 10;
        }
        fireWeightReceivedEvent(nWeight);       // Raise weight available event
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public HashMap<String, Object> getStatistics() throws DLSException {
        HashMap<String, Object> table = new HashMap<>();
        table.put(DLSJposConst.DLS_S_DEVICE_CATEGORY, "Scale");
        table.put(DLSJposConst.DLS_S_GOOD_WEIGHT_READ_COUNT, "NA");
        table.put(DLSStatistics.getInstance(context).getParserName(DLSJposConst.DLS_S_INTERFACE), "USB");
        statistics = table;
        return table;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasStatisticsReporting() {
        return scaleConfig.getCanAcceptStatisticsCmd();
    }



    @Override
    void internalRelease() {
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAlive() {
        if (DLSJposConst.updateInProgress) {
            return false;
        }
        boolean bRc;
        long timeout = STATUS_TIMEOUT;
        long elapsed = 0;
        long start;
        synchronized (statusResp) {
            bStatusResp = false;
            bDeviceReady = false;
            port.sendData(statusRequestCmd);
            start = System.nanoTime();
            try {
                while (!bStatusResp) {
                    statusResp.wait(timeout - elapsed);
                    elapsed = (long) ((System.nanoTime() - start) / 1000000);
                    if (elapsed >= timeout) {
                        break;
                    }
                }
            } catch (InterruptedException ie) {
                Log.e(Tag, "isAlive: Interrupted exception, ", ie);
            }
            bRc = bDeviceReady;
        }
        return bRc;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void onDataReceived(byte[] inBuf, int len) {
        //super.onDataReceived(inBuf,len);
        if (inBuf == null || inBuf.length == 0 || len == 0) {
            return;
        }
        int nResp = 0;

        synchronized (objStatus) {             // Lock the object
            nResp |= (inBuf[0] & 0x000000FF);                  // Lets put all three bytes
            nResp |= ((inBuf[1] & 0x000000FF) << 8);             //  of the status response into
            nResp |= ((inBuf[2] & 0x000000FF) << 16);            //  a 4 byte integer for easy comparisons.
            nStat0 = inBuf[0];                    // Save status byte 0
            nStat1 = inBuf[1];                    // Save status byte 1
            nStat2 = inBuf[2];                    // Save status byte 2

            if (inBuf[0] == (byte) 0xff
                    && inBuf[1] == (byte) 0xff
                    && inBuf[2] == (byte) 0xff) {
                try {
                    disable();
                    close();
                    open(getLogicalName(), context);
                    enable();
                } catch (DLSException ex) {
                    Log.e(Tag, "onDataReceived: Exception. ", ex);
                }
                return;
            }

            if (isLiveWeight() && !bGetWeight) {
                nLiveResp = nResp;
                dataBuf = Arrays.copyOf(inBuf, inBuf.length);
                bGotStatus = true;
                objStatus.notifyAll();
                return;
            }

            bGotStatus = true;
            objStatus.notifyAll();                // Notify waits.
        }

        // If we get a device not ready response
        if ((nResp & RESP_NOT_READY) == RESP_NOT_READY) {
            synchronized (statusResp) {            // Get lock
                bDeviceReady = false;                 // Set device not ready
                bStatusResp = true;
                statusResp.notifyAll();
                return;
            }
        } else {                                  // Any other response other than device not ready
            synchronized (statusResp) {            // Get lock
                bDeviceReady = true;                  // Set ready to true
                bStatusResp = true;
                statusResp.notifyAll();
            }
        }

        // If returning due to successfull configuration command.
        if ((nResp & RESP_CONFIG_SUCCESS) == RESP_CONFIG_SUCCESS) {
            synchronized (updateResp) {
                dataBuf = Arrays.copyOf(inBuf, inBuf.length);
                bUpdateResp = true;
                updateResp.notifyAll();
                return; // don't handle errors
            }
        }
        // Returning with configuration data
        if ((nResp & RESP_CONFIG_DATA) == RESP_CONFIG_DATA) {
            synchronized (reportResp) {
                dataBuf = Arrays.copyOf(inBuf, inBuf.length);
                bReportResp = true;
                reportResp.notifyAll();
                return; // don't handle errors
            }
        }

        if (bDioCommand) {
            bDioCommand = false;
            return;
        }

        // Command rejected because of error
        if ((nResp & RESP_CMD_ERROR) == RESP_CMD_ERROR) {
            fireDeviceErrorEvent(DeviceErrorStatusListener.ERR_CMD);
            // Weight not returned - scale in motion
        } else if ((nResp & RESP_DATA_ERROR) == RESP_DATA_ERROR) {
            fireDeviceErrorEvent(DeviceErrorStatusListener.ERR_DATA);
            // Read error
        } else if ((nResp & RESP_READ_ERROR) == RESP_READ_ERROR) {
            fireDeviceErrorEvent(DeviceErrorStatusListener.ERR_READ);
            // No display found, but is required
        } else if ((nResp & RESP_NO_DISPLAY) == RESP_NO_DISPLAY) {
            fireDeviceErrorEvent(DeviceErrorStatusListener.ERR_NO_DISPLAY);
            // Hardware problem
        } else if ((nResp & RESP_HARDWARE_ERROR) == RESP_HARDWARE_ERROR) {
            fireDeviceErrorEvent(DeviceErrorStatusListener.ERR_HARDWARE);
            // Command rejected
        } else if ((nResp & RESP_CMD_REJECT) == RESP_CMD_REJECT) {
            fireDeviceErrorEvent(DeviceErrorStatusListener.ERR_CMD_REJECT);
            // Scale over its capacity
        } else if ((nResp & RESP_OVER_CAPACITY) == RESP_OVER_CAPACITY) {
            fireDeviceErrorEvent(DeviceErrorStatusListener.ERR_CAPACITY);
            // Scale under zero
        } else if ((nResp & RESP_UNDER_ZERO) == RESP_UNDER_ZERO) {
            fireDeviceErrorEvent(DeviceErrorStatusListener.ERR_SCALE_UNDER_ZERO);
            // Zeroing of scale is required
        } else if ((nResp & RESP_REQUIRES_ZEROING) == RESP_REQUIRES_ZEROING) {
            fireDeviceErrorEvent(DeviceErrorStatusListener.ERR_REQUIRES_ZEROING);
            // Scale is warming up
        } else if ((nResp & RESP_SCALE_WARMUP) == RESP_SCALE_WARMUP) {
            fireDeviceErrorEvent(DeviceErrorStatusListener.ERR_WARMUP);
            // Duplicate weight response.
        } else if ((nResp & RESP_DUPLICATE_WEIGHT) == RESP_DUPLICATE_WEIGHT) {
            fireDeviceErrorEvent(DeviceErrorStatusListener.ERR_DUPLICATE);
            // got weight of zero
        } else if ((nResp & RESP_CENTER_OF_ZERO) == RESP_CENTER_OF_ZERO) {
            if (bZeroValid) {
                fireWeightEvent(nResp, inBuf);
            } else {
                fireDeviceErrorEvent(DeviceErrorStatusListener.ERR_SCALE_AT_ZERO);
            }
            // scale in motion - no valid weigh data
        } else if ((nResp & RESP_NO_WEIGHT) == RESP_NO_WEIGHT) {
            fireDeviceErrorEvent(DeviceErrorStatusListener.ERR_NO_WEIGHT);
        } else {
            fireWeightEvent(nResp, inBuf);
        }
        bGetWeight = false;
    }


    /**
     * {@inheritDoc}
     * Called by the {@link com.datalogic.dlapos.androidpos.transport.DLSPort} object.
     */
    @Override
    public void onDeviceAdded() {
    }


    /**
     * {@inheritDoc}
     * Performs final tasks following a DIO reset.
     */
    @Override
    public void onDeviceArrival() {
        try {
            if (getDeviceEnabled()) { // If it was previously enabled
                setState(DLSState.DISABLED);
                enable3ByteStatus();
                enable(); // Enable device.
            }
        } catch (DLSException e) {
            Log.e(Tag, "onDeviceArrival: Device reattached exception: ", e);
        }
        fireDeviceStatusEvent(CommonsConstants.SUE_POWER_ONLINE);
//        setStatusNotify(saveStatusNotify);
//
//        // restart live weight if needed
//        if (getStatusNotify() == ScaleConstants.SCAL_SN_ENABLED) {
//            if (DLSJposConst.updateInProgress) {
//                LOG.warn("onDeviceArrival: Update in progress.");
//                LOG.trace("onDeviceArrival (out)");
//                return;
//            }
//            if (bSaveLiveWeight) {
//                try {
//                    // Delay a little, otherwise an intial status response gets logged as live weight
//                    Thread.sleep(50);
//                } catch (InterruptedException ex) {
//                    LOG.error("onDeviceArrival: Interrupted. ", ex);
//                }
//                LOG.debug("onDeviceArrival: Restarting live weight.");
//                startLiveWeight();
//            }
//
//        }
        synchronized (resetObj) {
            resetSuccess = true;
            hotPlugged = true;
            resetObj.notifyAll();
        }
    }


    /**
     * {@inheritDoc}
     * Called by the {@link com.datalogic.dlapos.androidpos.transport.DLSPort} object, when the
     * same device is re-attached, before the object is destroyed.
     */
    @Override
    public void onDeviceReattached() {
        if (port.openPort()) {     // Re-open the port
            if (hotPlugged) {
                onDeviceArrival();
            }
        } else {
            fireDeviceErrorEvent(DeviceErrorStatusListener.ERR_DEVICE_REATTACHED);
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void onDeviceRemoved() {
        fireDeviceStatusEvent(CommonsConstants.SUE_POWER_OFF_OFFLINE);
//        if (hotPlugged) {
//            bSaveLiveWeight = isLiveWeight();
//            saveStatusNotify = getStatusNotify();
//            setStatusNotify(ScaleConstants.SCAL_SN_DISABLED);
//        }
        stopLiveWeight();
        if (!port.closePort()) {
            Log.w(Tag, "onDeviceRemoved: port could not be closed properly.");
        }
        fireDeviceErrorEvent(DeviceErrorStatusListener.ERR_DEVICE_REMOVED);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void readEnglishWeight() throws DLSException {
        if (DLSJposConst.updateInProgress) {
            return;
        }
        bGetWeight = true;
        port.sendData(englishWeightCmd, englishWeightCmd.length);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void readMetricWeight() throws DLSException {
        if (DLSJposConst.updateInProgress) {
            return;
        }
        bGetWeight = true;
        port.sendData(metricWeightCmd, metricWeightCmd.length);
    }


    /**
     * {@inheritDoc}
     *
     * @throws DLSException if the request is interrupted or times out
     */
    @Override
    public void readStatusWeight() throws DLSException {
        if (DLSJposConst.updateInProgress) {
            return;
        }
        if (!isLiveWeight()) {
            return;
        }

        int nCurStatus;
        int nCurWeight = 0;
        int tempStatus;
        byte[] tempBuf;
        byte[] command;

        long timeout = (long) (getPollRate() * 0.9);
        long elapsed = 0;
        long start;

        if (getMetricMode()) {
            command = metricWeightCmd;
        } else {
            command = englishWeightCmd;
        }
        synchronized (objStatus) {
            dataBuf = EMPTY_BYTE_ARRAY;
            nLiveResp = 0;
            bGotStatus = false;
            port.sendData(command);
            start = System.nanoTime();
            try {
                while (!bGotStatus) {
                    objStatus.wait(timeout - elapsed);
                    elapsed = (long) ((System.nanoTime() - start) / 1000000);
                    if (elapsed >= timeout) {
                        break;
                    }
                }
            } catch (InterruptedException ie) {
                throw new DLSException(DLSJposConst.DLS_E_TIMEOUT, "readStatusWeight interrupted.");
            }
            tempStatus = nLiveResp;
            tempBuf = Arrays.copyOf(dataBuf, dataBuf.length);
        }

        if (tempBuf.length == 0) {
            throw new DLSException(DLSJposConst.DLS_E_TIMEOUT, "readStatusWeight timeout");
        }
        if ((tempStatus & RESP_NOT_READY) == RESP_NOT_READY) {
            nCurStatus = ScaleConstants.SCAL_SUE_NOT_READY;
        } else if ((tempStatus & RESP_UNDER_ZERO) == RESP_UNDER_ZERO) {
            nCurStatus = ScaleConstants.SCAL_SUE_WEIGHT_UNDER_ZERO;
        } else if ((tempStatus & RESP_OVER_CAPACITY) == RESP_OVER_CAPACITY) {
            nCurStatus = ScaleConstants.SCAL_SUE_WEIGHT_OVERWEIGHT;
        } else if ((tempStatus & RESP_NO_WEIGHT) == RESP_NO_WEIGHT) {
            nCurStatus = ScaleConstants.SCAL_SUE_WEIGHT_UNSTABLE;
        } else if ((tempStatus & RESP_CENTER_OF_ZERO) == RESP_CENTER_OF_ZERO) {
            nCurStatus = ScaleConstants.SCAL_SUE_WEIGHT_ZERO;
        } else {
            nCurStatus = ScaleConstants.SCAL_SUE_STABLE_WEIGHT;

            int digits = 4;                         // Default number of weight digits
            if (((tempStatus & RESP_FIVE_DIGIT) == RESP_FIVE_DIGIT)
                    || (tempStatus & RESP_METRIC) == RESP_METRIC) { // If five digits is on
                digits = 5;                             // Set number of digits to 5
            }
            StringBuilder oSb = new StringBuilder();
            for (int i = 0; i < digits; i++) {        // Transfer data from inbuffer
                oSb.append(tempBuf[i + 3]);
            }

            nCurWeight = Integer.parseInt(oSb.toString());
            if (digits == 4) {                    // Always return 5 digit weights
                nCurWeight *= 10;
            }
        }

        if ((nCurStatus != getStatusValue())
                || ((nCurWeight != getLiveWeight())
                && (nCurStatus == ScaleConstants.SCAL_SUE_STABLE_WEIGHT))) {

            setStatusValue(nCurStatus);
            if ((nCurStatus == ScaleConstants.SCAL_SUE_STABLE_WEIGHT)
                    || (nCurStatus == ScaleConstants.SCAL_SUE_WEIGHT_ZERO)) {
                setLiveWeight(nCurWeight);
            }

            fireDeviceStatusEvent(nCurStatus);  // send up the status
        }
    }


    /**
     * Reads the current configuration from the scale and updates the
     * configuration object with the data received.
     *
     * @throws DLSException {@inheritDoc}
     */
    @Override
    public void reportConfiguration() throws DLSException {
        if (DLSJposConst.updateInProgress) {
            return;
        }

        byte[] buf;
        long timeout = commandTimeout;
        long elapsed = 0;
        long start;
        synchronized (reportResp) {
            bReportResp = false;
            dataBuf = EMPTY_BYTE_ARRAY;
            // Send the configuration command to the scanner.
            port.sendData(reportScaleConfigCmd);
            start = System.nanoTime();
            try {
                while (!bReportResp) {
                    reportResp.wait(timeout - elapsed);
                    elapsed = (long) ((System.nanoTime() - start) / 1000000);
                    if (elapsed >= timeout) {
                        break;
                    }
                }
            } catch (InterruptedException ie) {
                Log.e(Tag, "reportConfiguration: Interrupted exception, ", ie);
            }
            buf = Arrays.copyOf(dataBuf, dataBuf.length);
        }

        if (buf.length != 0) {        // If response found
            DLSScaleConfig sConfig = new DLSScaleConfig(buf);
            sConfig.setZeroValid(scaleConfig.getZeroValid());
            bZeroValid = scaleConfig.getZeroValid();
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void reset() throws DLSException {
        if (DLSJposConst.updateInProgress) {
            return;
        }
        stopLiveWeight();
        port.sendData(resetCmd, resetCmd.length);
    }


    /**
     * Enables/disables metric mode on the scale.
     *
     * @param bValue boolean indicating whether or not to enable metric mode
     */
    @Override
    public void setMetricMode(boolean bValue) {
        bMetricMode = bValue;                       // Save value
        scaleConfig.setMetricWeightMode(bValue);    // Set value in config object
        try {
            updateConfiguration();                      // Update the configuration
        } catch (DLSException e) {
            Log.e(Tag, "setMetricMode: Exception. ", e);
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void setStatusNotify(int nValue) {
        if (nStatusNotify != nValue) {
            stopLiveWeight();
        }
        nStatusNotify = nValue;
    }


    /**
     * Updates the scale configuration with parameters from the current scale
     * configuration object.
     *
     * @throws DLSException {@inheritDoc}
     */
    @Override
    public void updateConfiguration() throws DLSException {

        byte cfg0 = 0, cfg1 = 0, cfg2 = 0;
        if (scaleConfig.getOperationMode() != 0) // If nonzero
        {
            cfg0 |= 0x01;                               //  put into UK operation mode
        }
        if (scaleConfig.getDisplayRequired()) // If display required on,
        {
            cfg0 |= 0x04;                               //  turn on display required bit
        }
        if (scaleConfig.getIndicateZeroWithLed()) // If indicate zero with LED
        {
            cfg0 |= 0x08;                               //  turn on flag
        }
        if (scaleConfig.getMetricWeightMode()) // If metric weight mode needed
        {
            cfg0 |= 0x10;                               //  turn on flag
        }
        if (scaleConfig.getEnforceZeroReturn()) // If enforce zero return
        {
            cfg0 |= 0x20;                               //  turn on flag
        }
        if (scaleConfig.getVibrationSensitivity() < 4) // If valid sensitivity cmd
        {
            cfg0 |= (scaleConfig.getVibrationSensitivity() << 6); //  set sensitivity
        }
        if (scaleConfig.getFiveDigitWeight()) // If 5 digit weight returned
        {
            cfg1 |= 0x01;                               //  set flag
        }
        configureScaleCmd[2] = cfg0;                  // Set config byte 1
        configureScaleCmd[3] = cfg1;                  // Set config byte 2
        configureScaleCmd[4] = cfg2;                  // Set config byte 3

        bZeroValid = scaleConfig.getZeroValid();

        if (DLSJposConst.updateInProgress) {
            return;
        }

        long timeout = UPDATE_TIMEOUT;
        long elapsed = 0;
        long start;
        synchronized (updateResp) {
            bUpdateResp = false;
            dataBuf = EMPTY_BYTE_ARRAY;
            // Send the configuration command to the scanner.
            port.sendData(configureScaleCmd);
            start = System.nanoTime();
            try {
                while (!bUpdateResp) {
                    updateResp.wait(timeout - elapsed);
                    elapsed = (long) ((System.nanoTime() - start) / 1000000);
                    if (elapsed >= timeout) {
                        break;
                    }
                }
            } catch (InterruptedException ie) {
                Log.e(Tag, "updateConfiguration: Interrupted exception, ", ie);
            }
            if (dataBuf.length != 0) {
                waitForReady();
            }
        }
    }


    /**
     * After issuing an configureScale command, the device may not respond
     * immediately. This method loops for a status response to avoid a false
     * error while waiting for the scale configuration to complete.
     */
    private void waitForReady() {
        int loops = 50;                                  // Number of times to re-try
        if (DLSJposConst.updateInProgress) {
            return;
        }

        long timeout = 500;
        long elapsed = 0;
        long start;
        while ((loops--) > 0 && !bDeviceReady) {      // Continue while not ready
            synchronized (statusResp) {
                bStatusResp = false;
                bDeviceReady = false;
                port.sendData(statusRequestCmd);
                start = System.nanoTime();
                try {
                    while (!bStatusResp) {
                        statusResp.wait(timeout - elapsed);
                        elapsed = (long) ((System.nanoTime() - start) / 1000000);
                        if (elapsed >= timeout) {
                            break;
                        }
                    }
                } catch (InterruptedException ie) {
                    Log.e(Tag, "waitForReady: Interrupted exception, ", ie);
                }
            }
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void zeroScale() throws DLSException {
        if (DLSJposConst.updateInProgress) {
            return;
        }
        port.sendData(zeroScaleCmd, zeroScaleCmd.length);
    }


    /**
     * This thread is used to determine if the device has returned after a DIO
     * Reset
     * NOTE: only used on Linux
     */
    private class resetWatcherThread implements Runnable {

        public boolean checkHealth() {
            boolean bRc = false;
            long timeout = 500;
            long elapsed = 0;
            long start;
            if (port.isOpen()) {
                synchronized (statusResp) {
                    bStatusResp = false;
                    bDeviceReady = false;
                    port.sendData(statusRequestCmd);
                    start = System.nanoTime();
                    try {
                        while (!bStatusResp) {
                            statusResp.wait(timeout - elapsed);
                            elapsed = (long) ((System.nanoTime() - start) / 1000000);
                            if (elapsed >= timeout) {
                                break;
                            }
                        }
                    } catch (InterruptedException ie) {
                        Log.e(Tag, "resetWatcherThread.checkHealth: Interrupted exception, ", ie);
                    }
                    bRc = bDeviceReady;
                }
            }

            return bRc;
        }


        @Override
        public void run() {
            boolean deviceReady = checkHealth();
            if (deviceReady) {
                onDeviceArrival();
            }
        }
    }
}
