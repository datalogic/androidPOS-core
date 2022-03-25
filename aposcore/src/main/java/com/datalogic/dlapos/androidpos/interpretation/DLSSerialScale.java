package com.datalogic.dlapos.androidpos.interpretation;

import android.util.Log;

import com.datalogic.dlapos.androidpos.common.DLSDeviceInfo;
import com.datalogic.dlapos.androidpos.common.DLSException;
import com.datalogic.dlapos.androidpos.common.DLSJposConst;
import com.datalogic.dlapos.androidpos.common.DLSObjectFactory;
import com.datalogic.dlapos.androidpos.common.DLSState;
import com.datalogic.dlapos.androidpos.common.DLSStatistics;
import com.datalogic.dlapos.androidpos.transport.DLSUsbPort;
import com.datalogic.dlapos.commons.constant.ErrorConstants;
import com.datalogic.dlapos.commons.constant.ScaleConstants;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Class representing a generic Datalogic scale connected using a serial port.
 */
public class DLSSerialScale extends DLSScale {

    private final static String TAG = "DLSSerialScale";

    private int CMD_POS = 1;
    protected volatile boolean bConfidenceTest = false;
    protected volatile boolean bGetWeight = false;
    protected boolean bZeroValid = false;
    protected byte confidenceByte = 0;
    protected final Object confidenceTest = new Object();
    protected byte[] dataBuf = null;
    protected final Object dataReceived = new Object();
    protected static final String ENTER_ECHO_MODE_CMD = "E";
    protected static final String EXIT_ECHO_MODE_CMD = "F";
    protected static final String GET_CONFIDENCE_TEST_RESULTS_CMD = "B";
    protected static final String GET_WEIGHT_CMD = "W";
    protected static final String INITIATE_CONFIDENCE_TEST_CMD = "A";
    protected static final String ZERO_SCALE_CMD = "Z";
    protected int timeOut = 2000;
    private static final byte[] EMPTY_BYTE_ARRAY = {};
    protected boolean bAlive = false;


    /**
     * Creates a new instance of DLSSerialScale
     */
    public DLSSerialScale() {
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


    /**
     * Empty method - not supported on this interface.
     */
    @Override
    public void clearDisplay() {
    }


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
     * {@inheritDoc}
     * <p>
     * Supported commands: <ul>
     * <li> {@code DIO_SCALE_STATUS}
     * <li> {@code DIO_SCALE_SELF_TEST}
     * <li> {@code DIO_DEV_PROTOCOL}
     * </ul>
     *
     * @throws DLSException not thrown
     * @see DLSJposConst
     */
    @Override
    public void directIO(int command, int[] data, Object object) throws DLSException {
        if (DLSJposConst.updateInProgress) {
            return;
        }
        String strResult = "Invalid Command";

        switch (command) {
            case DLSJposConst.DIO_SCALE_STATUS:
                sendMsg(GET_WEIGHT_CMD, true);
                strResult = "Status";
                if (dataBuf.length != 0) {
                    if (dataBuf[CMD_POS] == '?') {
                        byte status = dataBuf[CMD_POS + 1];
                        if ((status & DLSJposConst.SCALE_STATUS_UNDER_ZERO) == DLSJposConst.SCALE_STATUS_UNDER_ZERO) {
                            strResult += ", under zero";
                        }
                        if ((status & DLSJposConst.SCALE_STATUS_RANGE) == DLSJposConst.SCALE_STATUS_RANGE) {
                            strResult += ", overweight";
                        }
                        if ((status & DLSJposConst.SCALE_STATUS_MOTION) == DLSJposConst.SCALE_STATUS_MOTION) {
                            strResult += ", in motion";
                        }
                        if ((status & DLSJposConst.SCALE_STATUS_OUTSIDE_ZERO) == DLSJposConst.SCALE_STATUS_OUTSIDE_ZERO) {
                            strResult += ", out of zero capture range";
                        }
                        if ((status & DLSJposConst.SCALE_STATUS_CENTEROF_ZERO) == DLSJposConst.SCALE_STATUS_CENTEROF_ZERO) {
                            strResult += ", zero weight";
                        }
                    } else {
                        //strResult = "Weight - ";
                        StringBuilder oSb = new StringBuilder("Weight - ");
                        for (int i = 1; i < dataBuf.length - 1; i++) {
                            if (dataBuf[i] >= '0' && dataBuf[i] <= '9') {
                                //strResult += (dataBuf[i] - '0');
                                oSb.append(dataBuf[i] - '0');
                            }
                        }
                        strResult = oSb.toString();
                    }
                } else {
                    strResult += ", no response";
                }
                break;

            case DLSJposConst.DIO_SCALE_SELF_TEST:
                if (isAlive()) {            // Is alive,
                    strResult = "Selftest - ";
                    strResult += ((confidenceByte & DLSJposConst.SCALE_TEST_PROCESSOR) == DLSJposConst.SCALE_TEST_PROCESSOR) ? "processor passed, " : "processor FAILED, ";
                    strResult += ((confidenceByte & DLSJposConst.SCALE_TEST_ROM) == DLSJposConst.SCALE_TEST_ROM) ? "ROM passed, " : "ROM FAILED, ";
                    strResult += ((confidenceByte & DLSJposConst.SCALE_TEST_RAM) == DLSJposConst.SCALE_TEST_RAM) ? "RAM passed, " : "RAM FAILED, ";
                    strResult += ((confidenceByte & DLSJposConst.SCALE_TEST_EEPROM1) == DLSJposConst.SCALE_TEST_EEPROM1) ? "EEPROM1 passed, " : "EEPROM1 FAILED, ";
                    strResult += ((confidenceByte & DLSJposConst.SCALE_TEST_EEPROM2) == DLSJposConst.SCALE_TEST_EEPROM2) ? "EEPROM2 passed" : "EEPROM2 FAILED";
                } else {
                    strResult = "Device not responding";
                }
                break;

            case DLSJposConst.DIO_DEV_PROTOCOL:  // Return that this is an RS232 device
                strResult = "RS232 - SASI";
                break;
        }

        try {
            ByteArrayOutputStream oBs = (ByteArrayOutputStream) object;
            oBs.write(strResult.getBytes());
        } catch (IOException e) {
            throw new DLSException(ErrorConstants.APOS_E_FAILURE, "Unable to return data to DIO caller - Are you passing in a ByteArrayOuputStream object? " + e.getLocalizedMessage());
        }

    }


    /**
     * {@inheritDoc}
     * Additionally stopping live weight if running.
     */
    @Override
    public void disable() throws DLSException {
        stopLiveWeight();
        setState(DLSState.DISABLED);
    }


    /**
     * {@inheritDoc}
     * synchronously calls {@link #isAlive()} to initiate a confidence test.
     *
     * @throws DLSException if confidence test fails
     */
    @Override
    public boolean doHealthCheck(int timeout) throws DLSException {
        int saved = commandTimeout;
        commandTimeout = timeout;
        boolean bRc = isAlive();                    // Initiate confidence test
        commandTimeout = saved;

        if ((confidenceByte & DLSJposConst.SCALE_TEST_EEPROM1) != DLSJposConst.SCALE_TEST_EEPROM1) {
            throw new DLSException(DLSJposConst.DLS_E_HARDWARE, "EEPROM 1 Test failed");
        }
        if ((confidenceByte & DLSJposConst.SCALE_TEST_EEPROM2) != DLSJposConst.SCALE_TEST_EEPROM2) {
            throw new DLSException(DLSJposConst.DLS_E_HARDWARE, "EEPROM 2 Test failed");
        }
        if ((confidenceByte & DLSJposConst.SCALE_TEST_RAM) != DLSJposConst.SCALE_TEST_RAM) {
            throw new DLSException(DLSJposConst.DLS_E_HARDWARE, "RAM Test failed");
        }
        if ((confidenceByte & DLSJposConst.SCALE_TEST_PROCESSOR) != DLSJposConst.SCALE_TEST_PROCESSOR) {
            throw new DLSException(DLSJposConst.DLS_E_HARDWARE, "Processor RAM Test failed");
        }
        if ((confidenceByte & DLSJposConst.SCALE_TEST_ROM) != DLSJposConst.SCALE_TEST_ROM) {
            throw new DLSException(DLSJposConst.DLS_E_HARDWARE, "ROM Test failed");
        }

        return bRc;                               // Return true/false
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void doSelfTest() {
        if (DLSJposConst.updateInProgress) {
            return;
        }

        sendMsg(INITIATE_CONFIDENCE_TEST_CMD, true);        // Need a response with no data

        long timeout = commandTimeout;
        long elapsed = 0;
        long start;
        synchronized (confidenceTest) {
            bAlive = false;                               // Set alive to notta
            bConfidenceTest = true;                       // Set confidence response flag to false
            confidenceByte = 0;
            sendMsg(GET_CONFIDENCE_TEST_RESULTS_CMD);         // Send message to scale
            start = System.nanoTime();
            try {
                while (bConfidenceTest) {
                    confidenceTest.wait(timeout - elapsed);
                    elapsed = (long) ((System.nanoTime() - start) / 1000000);
                    if (elapsed >= timeout) {
                        break;
                    }
                }
            } catch (InterruptedException ie) {
                Log.e(TAG, "doSelfTest: Interrupted exception, ", ie);
            }

            if ((confidenceByte & 0x1f) == 0x1f) {    // If all tests passed
                bAlive = true;                             // Then we are ok.
            } else if (confidenceByte == 0) {         // If we did not get any response at all.
                fireDeviceErrorEvent(DeviceErrorStatusListener.ERR_DEVICE_REMOVED);
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
            startLiveWeight();
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public HashMap<String, Object> getStatistics() throws DLSException {
        HashMap<String, Object> table = new HashMap<>();
        table.put(DLSJposConst.DLS_S_DEVICE_CATEGORY, "Scale");
        table.put(DLSJposConst.DLS_S_GOOD_WEIGHT_READ_COUNT, "NA");
        table.put(DLSStatistics.getInstance(context).getParserName(DLSJposConst.DLS_S_INTERFACE), "RS232");
        statistics = table;
        return table;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAlive() {

        // The other way to check to see if there is a device, and to make sure
        //  it is the device we want, is to send it a command, and get a valid response.
        doSelfTest();
        return bAlive;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void onDataReceived(byte[] inBuf, int len) {
        DLSDeviceInfo oInfo = getDeviceInfo();
        //Something may have gone terribly wrong, if so, log an error and return.
        if (inBuf == null || len == 0) {
            return;
        }

        // Allow the base class to concatenate incomplete messages, and separate
        //  multiple ones.
        parseMessages(inBuf, oInfo.getRxTrailer());  // Parse messages into a list
        // Get a list iterator
        for (Object o : messageList) {                          // If any messages in list
            byte[] tmpBuf = (byte[]) o;              // Get buffer of message

            synchronized (dataReceived) {                  // Lock the object
                dataBuf = tmpBuf;
                dataReceived.notifyAll();                     // Let any waits know there is data
            }

            // must be from live weight (or dio getweight)
            if (isLiveWeight() && !bGetWeight && !bConfidenceTest) {
                break;
            }

            bGetWeight = false;
            bZeroValid = super.getZeroValid();

            if (tmpBuf.length > CMD_POS) {                // Make sure it is long enough
                byte pre = oInfo.getRxPrefix();
                if (pre == tmpBuf[0]) {
                    CMD_POS = 1;
                } else {
                    CMD_POS = 0;
                }

                // Check command character
                if (tmpBuf[CMD_POS] == '?') {
                    byte status = tmpBuf[CMD_POS + 1];
                    synchronized (confidenceTest) {
                        if (bConfidenceTest) {
                            bConfidenceTest = false;
                            confidenceByte = status;
                            confidenceTest.notifyAll();
                        } else if ((status & DLSJposConst.SCALE_STATUS_MOTION) == DLSJposConst.SCALE_STATUS_MOTION) {
                            fireDeviceErrorEvent(DeviceErrorStatusListener.ERR_NO_WEIGHT);
                        } else if ((status & DLSJposConst.SCALE_STATUS_RANGE) == DLSJposConst.SCALE_STATUS_RANGE) {
                            fireDeviceErrorEvent(DeviceErrorStatusListener.ERR_CAPACITY);
                        } else if ((status & DLSJposConst.SCALE_STATUS_UNDER_ZERO) == DLSJposConst.SCALE_STATUS_UNDER_ZERO) {
                            fireDeviceErrorEvent(DeviceErrorStatusListener.ERR_SCALE_UNDER_ZERO);
                        } else if ((status & DLSJposConst.SCALE_STATUS_CENTEROF_ZERO) == DLSJposConst.SCALE_STATUS_CENTEROF_ZERO) {
                            if (bZeroValid) {
                                parseAndFireWeight(tmpBuf);
                            } else {
                                fireDeviceErrorEvent(DeviceErrorStatusListener.ERR_SCALE_AT_ZERO);
                            }
                        }
                    }
                } else {
                    parseAndFireWeight(tmpBuf);
                }
            }
        }

        messageList.clear();                                // Make sure msg list is cleared.
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDeviceReattached() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDeviceRemoved() {
    }

    private void parseAndFireWeight(byte[] data) {
        if (data.length < 3) {
            return;
        }

        // SASI weight for english: 02 30 xx xx . xx xx
        // SASI weight for metric : 02 xx xx . xx xx xx
        StringBuilder oSb = new StringBuilder();
        int ndxPeriod = 0;

        for (int i = CMD_POS; i < data.length; i++) {
            if (data[i] == '.') {
                ndxPeriod = i;
            }

            if (data[i] >= '0' && data[i] <= '9') {
                //strWeight += tmpBuf[i] - '0';
                oSb.append(data[i] - '0');
            }
        }

        if (oSb.length() == 0)
            return;

        int nWeight = Integer.parseInt(oSb.toString());
        if ((nWeight == 0) && !bZeroValid) {   // fire scale at zero error message
            fireDeviceErrorEvent(DeviceErrorStatusListener.ERR_SCALE_AT_ZERO);
        } else {
            if (ndxPeriod == 4) {
                nWeight *= 10;      // Always return 5 digit weight
            }
            fireWeightReceivedEvent(nWeight);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void readEnglishWeight() {
        readWeight();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void readMetricWeight() {
        readWeight();
    }


    /**
     * {@inheritDoc}
     *
     * @throws DLSException if weight is invalid or not received
     */
    @Override
    public void readStatusWeight() throws DLSException {
        if (DLSJposConst.updateInProgress) {
            return;
        }
        sendMsg(GET_WEIGHT_CMD, true);

        if (dataBuf.length == 0) {
            throw new DLSException(DLSJposConst.DLS_E_TIMEOUT, "readStatusWeight timeout");
        }

        int nCurStatus = ScaleConstants.SCAL_SUE_STABLE_WEIGHT;
        int nCurWeight = 0;

        if (dataBuf[CMD_POS] == '?') {
            byte status = dataBuf[CMD_POS + 1];

            if ((status & DLSJposConst.SCALE_STATUS_UNDER_ZERO) == DLSJposConst.SCALE_STATUS_UNDER_ZERO) {
                nCurStatus = ScaleConstants.SCAL_SUE_WEIGHT_UNDER_ZERO;
            } else if ((status & DLSJposConst.SCALE_STATUS_RANGE) == DLSJposConst.SCALE_STATUS_RANGE) {
                nCurStatus = ScaleConstants.SCAL_SUE_WEIGHT_OVERWEIGHT;
            } else if ((status & DLSJposConst.SCALE_STATUS_MOTION) == DLSJposConst.SCALE_STATUS_MOTION) {
                nCurStatus = ScaleConstants.SCAL_SUE_WEIGHT_UNSTABLE;
            } else if ((status & DLSJposConst.SCALE_STATUS_OUTSIDE_ZERO) == DLSJposConst.SCALE_STATUS_OUTSIDE_ZERO) {
                nCurStatus = ScaleConstants.SCAL_SUE_NOT_READY;
            } else if ((status & DLSJposConst.SCALE_STATUS_CENTEROF_ZERO) == DLSJposConst.SCALE_STATUS_CENTEROF_ZERO) {
                nCurStatus = ScaleConstants.SCAL_SUE_WEIGHT_ZERO;
            }
        } else {
            StringBuilder oSb = new StringBuilder();
            int ndxPeriod = 0;

            if (dataBuf.length < 3) {
                throw new DLSException(DLSJposConst.DLS_E_HARDWARE, "Live Weight invalid.");
            }
            for (int i = CMD_POS; i < dataBuf.length; i++) {
                if (dataBuf[i] == '.') {
                    ndxPeriod = i;
                }

                if (dataBuf[i] >= '0' && dataBuf[i] <= '9') {
                    oSb.append(dataBuf[i] - '0');
                }
            }
            nCurWeight = Integer.parseInt(oSb.toString());
            if (ndxPeriod == 4) {
                nCurWeight *= 10;
            }      // Always return 5 digit weight
        }

        if (nCurWeight == 0 && nCurStatus == ScaleConstants.SCAL_SUE_STABLE_WEIGHT) {
            nCurStatus = ScaleConstants.SCAL_SUE_WEIGHT_ZERO;
        }

        if ((nCurStatus != getStatusValue()) || ((nCurWeight != getLiveWeight())
                && (nCurStatus == ScaleConstants.SCAL_SUE_STABLE_WEIGHT))) {
            setStatusValue(nCurStatus);
            setLiveWeight(nCurWeight);
            fireDeviceStatusEvent(nCurStatus);  // send up the status
        }
    }

    private void readWeight() {
        if (DLSJposConst.updateInProgress) {
            return;
        }
        bGetWeight = true;
        sendMsg(GET_WEIGHT_CMD);
    }


    /**
     * Empty method - not supported on this interface.
     */
    @Override
    public void reportConfiguration() {
    }


    /**
     * Empty method - not supported on this interface.
     *
     * @throws DLSException not thrown
     */
    @Override
    public void reset() throws DLSException {
    }


    /**
     * {@inheritDoc}
     */
    protected int sendMsg(String msg) {
        return port.sendData(msg);
    }


    /**
     * Sends a command to the scale, synchronously waiting for a response. A
     * default timeout will be used.
     *
     * @param msg   String containing the command to send
     * @param bWait boolean indicating whether to wait for a scale response.
     *              Currently waits no matter the value of {@code bWait}
     * @return byte array containing the scale response. Array will be empty
     * if timeout is reached
     */
    protected byte[] sendMsg(String msg, boolean bWait) {
        byte[] response;
        long timeout = commandTimeout;
        long elapsed = 0;
        long start;
        synchronized (dataReceived) {
            dataBuf = EMPTY_BYTE_ARRAY;
            sendMsg(msg);
            start = System.nanoTime();
            try {
                while (dataBuf.length == 0) {
                    dataReceived.wait(timeout - elapsed);
                    elapsed = (long) ((System.nanoTime() - start) / 1000000);
                    if (elapsed >= timeout) {
                        break;
                    }
                }
            } catch (InterruptedException ie) {
                Log.e(TAG, "sendMsg: Interrupted exception, ", ie);
            }
            response = Arrays.copyOf(dataBuf, dataBuf.length);
        }
        return response;
    }


    /**
     * Empty method - not supported on this interface.
     */
    @Override
    public void updateConfiguration() {
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void zeroScale() {
        if (DLSJposConst.updateInProgress) {
            return;
        }
        sendMsg(ZERO_SCALE_CMD);
    }
}
