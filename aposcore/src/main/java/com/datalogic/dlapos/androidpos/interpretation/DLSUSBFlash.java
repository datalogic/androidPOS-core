package com.datalogic.dlapos.androidpos.interpretation;

import android.content.Context;
import android.util.Log;

import com.datalogic.dlapos.androidpos.common.DLSCConfig;
import com.datalogic.dlapos.androidpos.common.DLSException;
import com.datalogic.dlapos.androidpos.common.DLSJposConst;
import com.datalogic.dlapos.androidpos.common.DLSState;
import com.datalogic.dlapos.commons.constant.CommonsConstants;

/**
 * {@code DLSUSBFlash} provides specific implementation for firmware updates of
 * USB devices.
 */
public class DLSUSBFlash extends DLSDevice {

    private final static String TAG = "DLSUSBFlash";

    static final int RESP_COMMAND_COMPLETE = 0x01;
    static final int RESP_FLASH_LOADING = 0x02;
    static final int RESP_OPERATION_ERROR = 0x40;
    static final int RESP_COMMAND_REJECT = 0x80;
    private static final int REPORT_COUNT = 263; // maximum report size

    private final Object objStatus = new Object();

    private int recordTimeout = 60000;
    byte status;
    volatile boolean responseReceived = false;
    private int response = 0;          // response from srecord msg
    boolean reattachPending = true; // hot plug fix 1.8.13


    /**
     * Empty method - not supported on this interface.
     *
     * @return always {@code true}
     */
    @Override
    public boolean detectResetFinish() {
        return true;
    }

    /**
     * Empty method - not supported for this implementation.
     */
    @Override
    public void directIO(int command, int[] data, Object object) throws DLSException {
    }

    /**
     * Empty method - not supported for this implementation.
     */
    @Override
    public void disable() throws DLSException {
    }

    /**
     * Empty method - not supported for this implementation.
     */
    @Override
    public boolean doHealthCheck(int timeout) throws DLSException {
        return true;
    }

    /**
     * Empty method - not supported for this implementation.
     */
    @Override
    public void doSelfTest() throws DLSException {

    }

    /**
     * Empty method - not supported for this implementation.
     */
    @Override
    public void enable() throws DLSException {

    }

    /**
     * Not supported for this implementation.
     */
    @Override
    public DLSCConfig getConfiguration() {
        return null;
    }

    /**
     * Returns the number of milliseconds that will elapse before a record is
     * considered timed out.
     *
     * @return int indicating the timeout value in milliseconds
     */
    public int getRecordTimeout() {
        return recordTimeout;
    }

    /**
     * {@inheritDoc}
     *
     * @return always {@code true}
     */
    @Override
    public boolean isAlive() throws DLSException {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDataReceived(byte[] inBuf, int inLen) {

        synchronized (objStatus) {             // Lock the object
            if (inLen > 0) {
                status = inBuf[0];                 // Save status byte 0
            }
            responseReceived = true;
            objStatus.notifyAll();                // Notify waits.
        }
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
     * Called by the {@link com.datalogic.dlapos.androidpos.transport.DLSPort} object, when the
     * same device is re-attached, before the object is destroyed.
     */
    @Override
    public void onDeviceReattached() {
        fireDeviceStatusEvent(CommonsConstants.SUE_POWER_ONLINE);
        if (reattachPending) {                 // If an automatic re-attach is pending
            reattachPending = true;                // due to a re-set or flash (hot plug fix 1.8.13)
            port.openPort();                        // Re-open the port
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void onDeviceRemoved() {
        fireDeviceStatusEvent(CommonsConstants.SUE_POWER_OFF_OFFLINE);
        if (!reattachPending) {
            port.closePort();
            fireDeviceErrorEvent(DeviceErrorStatusListener.ERR_DEVICE_REMOVED);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void open(String strLogicalName, Context context) throws DLSException {
        super.open(strLogicalName, context);                       // Call base class
        setState(DLSState.OPENED);
    }

    /**
     * * Empty method - not supported for this implementation.
     */
    @Override
    public void reset() throws DLSException {
    }

    /**
     * Sends a reset command to the device to finish update process.
     */
    public void resetCommand() {
        try {
            synchronized (objStatus) {
                response = DLSJposConst.DL_NRESP;
                status = 0;

                byte[] outBuf = new byte[]{0x00, 0x00, 0x00, 0x40};

                port.sendData(outBuf, outBuf.length);
//                objStatus.wait(8000);
//
//                if (status == (RESP_COMMAND_COMPLETE | RESP_FLASH_LOADING)) {
//                    response = DLSJposConst.DL_ACK;
//                } else if ((status & RESP_OPERATION_ERROR) != 0) {
//                    response = DLSJposConst.DL_BEL;
//                } else if ((status & RESP_COMMAND_REJECT) != 0) {
//                    response = DLSJposConst.DL_NAK;
//                } else {
//                    response = DLSJposConst.DL_UNKWN;
//                }
            }
        } catch (RuntimeException re) {
            throw re;
        } catch (Exception e) {
            //TODO: rise exception
        }
//        return response;
    }


    /**
     * Sends a firmware record to the scanner.
     *
     * @param sRecord String containing the record to send.
     * @return int indicating the command response from the scanner
     */
    public int sendRecord(String sRecord) {
        int resp;
        long timeout = getRecordTimeout(); //some records on 9800 take over 40 seconds to get a response from.
        long elapsed = 0;
        long start;
        byte[] buf;
        buf = new byte[sRecord.length() + 4];

        int i;
        buf[0] = 0;
        buf[1] = 0;
        buf[2] = 0x02; // write record command
        buf[3] = 0;

        /* JAZZ_60736 - changed the casting of a char to byte with a temp
         * buf that utilizes getBytes with the correct encoding.
         */
        byte[] temp = sRecord.getBytes(DLSJposConst.HDL_CHARSET);
        for (i = 0; i < sRecord.length(); i++) {
            buf[i + 4] = temp[i];
        }

        synchronized (objStatus) {
            responseReceived = false;
            response = DLSJposConst.DL_NRESP;
            status = 0;
            port.sendData(buf, sRecord.length() + 4);
            start = System.nanoTime();
            try {
                while (!responseReceived) {
                    objStatus.wait(timeout - elapsed);
                    elapsed = (long) ((System.nanoTime() - start) / 1000000);
                    if (elapsed >= timeout) {
                        break;
                    }
                }
            } catch (InterruptedException ie) {
                Log.e(TAG, "Interrupted while waiting for response");
            }

            if (responseReceived)
                response = evaluateStatus(status);
            resp = response;
        }

        return resp;
    }

    /**
     * Sets the number of milliseconds that will be allowed to elapse before an
     * update firmware record is considered timed out.
     *
     * @param timeout int indicating the number of milliseconds to wait
     */
    public void setRecordTimeout(int timeout) {
        recordTimeout = timeout;
    }

    /**
     * Sends a REPORT_COUNT record to the device to start a firmware update.
     *
     * @return int indicating the command response from the scanner
     */
    public int startCommand() {
        int resp;
        long timeout = getRecordTimeout();
        long elapsed = 0;
        long start;
        byte[] buf = new byte[]{0, 0, 1, 0};

        synchronized (objStatus) {
            responseReceived = false;
            response = DLSJposConst.DL_NRESP;
            status = 0;
            port.sendData(buf);
            start = System.nanoTime();
            try {
                while (!responseReceived) {
                    objStatus.wait(timeout - elapsed);
                    elapsed = ((System.nanoTime() - start) / 1000000);
                    if (elapsed >= timeout) {
                        break;
                    }
                }
            } catch (InterruptedException ie) {
                Log.e(TAG, "Interrupted while waiting for response");
            }

            if (responseReceived)
                response = evaluateStatus(status);

            resp = response;
        }

        return resp;
    }

    private int evaluateStatus(byte statusToEvaluate) {
        if (statusToEvaluate == (RESP_COMMAND_COMPLETE | RESP_FLASH_LOADING)) {
            return DLSJposConst.DL_ACK;
        } else if ((statusToEvaluate & RESP_OPERATION_ERROR) != 0) {
            return DLSJposConst.DL_BEL;
        } else if ((statusToEvaluate & RESP_COMMAND_REJECT) != 0) {
            return DLSJposConst.DL_NAK;
        } else {
            return DLSJposConst.DL_INVALID;
        }
    }
}
