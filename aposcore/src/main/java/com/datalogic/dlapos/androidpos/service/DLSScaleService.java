package com.datalogic.dlapos.androidpos.service;

import android.content.Context;
import android.util.Log;

import com.datalogic.dlapos.androidpos.common.DLSCConfig;
import com.datalogic.dlapos.androidpos.common.DLSException;
import com.datalogic.dlapos.androidpos.common.DLSJposConst;
import com.datalogic.dlapos.androidpos.common.DLSObjectFactory;
import com.datalogic.dlapos.androidpos.common.DLSProperties;
import com.datalogic.dlapos.androidpos.common.DLSScaleConfig;
import com.datalogic.dlapos.androidpos.interpretation.DLSScale;
import com.datalogic.dlapos.androidpos.interpretation.WeightReceivedListener;
import com.datalogic.dlapos.commons.constant.CommonsConstants;
import com.datalogic.dlapos.commons.constant.ErrorConstants;
import com.datalogic.dlapos.commons.constant.ScaleConstants;
import com.datalogic.dlapos.commons.event.DataEvent;
import com.datalogic.dlapos.commons.event.ErrorEvent;
import com.datalogic.dlapos.commons.event.EventCallback;
import com.datalogic.dlapos.commons.event.StatusUpdateEvent;
import com.datalogic.dlapos.commons.service.ScaleBaseService;
import com.datalogic.dlapos.commons.support.APosException;
import com.datalogic.dlapos.commons.upos.RequestListener;

/**
 * Service for a generic Datalogic scale.
 */
public class DLSScaleService extends DLSBaseService implements ScaleBaseService, WeightReceivedListener {

    private static final String Tag = "DLSScaleService";
    static final int CMD_PENDING = -1;
    static final int CMD_ABORT = -2;
    static final int CMD_COMPLETE = -3;
    static final int CMD_MOTION = -4;
    static final int CMD_OVERWEIGHT = -5;
    static final int CMD_ZERO = -6;
    static final int CMD_AT_ZERO = -7;
    static final int CMD_TIMEOUT = -8;
    static final int CMD_UNDERZERO = -9;
    static final int SCALE_IN_MOTION = 112;
    static final int SCALE_NEEDS_ZEROING = 113;
    static final int SCALE_AT_ZERO = 114;
    static final int SCALE_OVERWEIGHT = 201;
    static final int SCALE_UNDERZERO = 202;
    private static final int SCALE_READ_ERROR = 1;
    private static final int MAXIMUM_READ_TIMEOUT = 5000;
    private static final int DEFAULT_READ_TIMEOUT = 1000;
    final WeightData currentWeightData = new WeightData();
    private final Object weightLock = new Object();
    private final Object sleepLock = new Object();
    private boolean bWeightLock = false;
    DLSScale scale = null;
    private boolean bAsyncMode = false;
    private boolean bAutoDisable = false;
    private int nMaxWeight = 3000;
    private int nWeightUnit;
    boolean bMetricMode;
    //protected int nMaxWeight = 30000;
    //protected int nWeightUnit = SCAL_WU_POUND;
    private int nTareWeight = 0;
    private long lUnitPrice = 0;
    private boolean bDataEventEnabled = false;
    //private boolean bMetricMode = false;
    private final Object scaleResp = new Object();
    boolean bExceptionOnMotion = false;
    private boolean bCanAcceptStats = false;

    class WeightData {
        public int nWeight = 0;                  // Actual scale weight
        public int nState = CMD_COMPLETE;        // Scale read state
        public boolean bSendPending = false;     // If Async mode, is a event send pending?
        public long nEndTime = 0;                // used to test async read collision
        public boolean bRetry = false;           // set if timeout is 0 and sync
    }

    /**
     * Creates a new instance of DLSScaleService
     */
    public DLSScaleService() {
        super();
        nMaxWeight = 30000;
        nWeightUnit = ScaleConstants.SCAL_WU_POUND;
        bMetricMode = false;
    }

    @Override
    public void claim(RequestListener listener) throws APosException {
        super.claim(listener);
        //String[] temp = {""};
        scale.addWeightReceivedListener(this);

        DLSCConfig dConfig = scale.getConfiguration();
        bCanAcceptStats = dConfig.getOptionAsBool(DLSScaleConfig.KEY_CANACCEPTSTATISTICSCMD);

        //TODO: is this onlt WMI related?? CAP_TARE requires enable.
//        if (bCanAcceptStats) {
//            retrieveScaleStatistics(temp);
//            super.statistics.put(DLSJposConst.DLS_S_SCALE_CAP_DISP_TEXT, (this.getCapDisplayText() ? "True" : "False"));
//            super.statistics.put(DLSJposConst.DLS_S_SCALE_CAP_PRICE_CALC, (this.getCapPriceCalculating() ? "True" : "False"));
//            super.statistics.put(DLSJposConst.DLS_S_SCALE_CAP_TARE_WT, (this.getCapTareWeight() ? "True" : "False"));
//            super.statistics.put(DLSJposConst.DLS_S_SCALE_CAP_ZERO, (this.getCapZeroScale() ? "True" : "False"));
//            super.statistics.put(DLSJposConst.DLS_S_SCALE_STAT_UPDATE, (this.getCapStatusUpdate() ? "True" : "False"));
//        }
//        try {
//            if (!scale.isAlive()) {
//                throw new APosException("Error - no response from scale", ErrorConstants.APOS_E_ILLEGAL);
//            }
//        } catch (DLSException ex) {
//            throw new APosException(ex.getMessage(), ErrorConstants.APOS_E_ILLEGAL);
//        }
    }

    // 1.2

    /**
     * {@inheritDoc}
     */
    @Override
    public void compareFirmwareVersion(String firmwareFileName, int[] result) throws APosException {
        super.compareFirmwareVersion(firmwareFileName, result);
        throw new APosException("Firmware comparison not available for this device", ErrorConstants.APOS_E_ILLEGAL);
    }

    /**
     * Indicate whether a scale device has a display capability.
     *
     * @return boolean indicating whether display is supported.
     * @throws APosException thrown if the device is not opened.
     */
    @Override
    public boolean getCapDisplay() throws APosException {
        checkOpened();
        return false;
    }

    // 1.9

    /**
     * Indicate whether a scale device has the update status capability.
     *
     * @return boolean indicating whether status update is supported.
     * @throws APosException thrown if the device is not opened.
     */
    @Override
    public boolean getCapStatusUpdate() throws APosException {
        checkOpened();
        return scale.canStatusUpdate();
    }

    /**
     * Return the maximum weight of a scale device.
     *
     * @return int indicating the maximum weight of the device.
     * @throws APosException thrown if the device is not opened.
     */
    @Override
    public int getMaximumWeight() throws APosException {
        checkOpened();
        return nMaxWeight;
    }

    /**
     * Return the current power state of a scale.
     *
     * @return int indicating the current power state of the scale.
     * @throws APosException thrown always.  This method is meant to be
     *                       overridden.  Calling this method will always throw an exception.
     */
    @Override
    public int getPowerState() throws APosException {
        throw new UnsupportedOperationException("Not supported yet.");
    }


    // 1.9

    /**
     * Return the current live weight from a scale device.
     *
     * @return int indicating the current live weight from the scale device.
     * @throws APosException thrown if the device is not opened, claimed and
     *                       enabled.
     */
    @Override
    public int getScaleLiveWeight() throws APosException {
        checkClaimed();
        checkEnabled();
        return scale.getLiveWeight();
    }

    /**
     * Return the current status notify property of a scale device.
     *
     * @return int indicating the current status notify property.
     * @throws APosException thrown if the device is not opened.
     */
    @Override
    public int getStatusNotify() throws APosException {
        checkOpened();
        return scale.getStatusNotify();
    }

    /**
     * Return the weight unit used by a scale device.
     *
     * @return int indicating the weight unit used by the device.
     * @throws APosException thrown if the device is not opened.
     */
    @Override
    public int getWeightUnit() throws APosException {
        checkOpened();
        return nWeightUnit;
    }

    /**
     * Open the device, do not open the port for communications
     *
     * @throws APosException thrown if an exception is encountered while opening
     *                       the device.
     */
    @Override
    public void open(String logicalName, EventCallback callbacks, Context context) throws APosException {
        super.open(logicalName, callbacks, context);
        try {
            DLSProperties oOpt = DLSProperties.getInstance(context);
            bExceptionOnMotion = oOpt.isScaleMotionException();
            device = DLSObjectFactory.createScale(logicalName, context);   // Create a scale object
            scale = (DLSScale) device;                             // Save as a DLSScale object
            device.open(logicalName, context);                             // Open the device

            bAsyncMode = false;
            bDataEventEnabled = false;

            // Retrieve the configuration from the device object.
            //  The configuration comes from the apos.json file.
            DLSScaleConfig oConfig = (DLSScaleConfig) device.getConfiguration();
            if (oConfig != null) {                               // If config found
                if (oConfig.getMetricWeightMode()) {               // If metric weight mode
                    if (oConfig.getFiveDigitWeight()) {              // If five digit weight mode
                        nMaxWeight = 15000;                             // Set max weight
                    } else {
                        nMaxWeight = 1500;                              // Set max weight
                    }
                    nWeightUnit = ScaleConstants.SCAL_WU_KILOGRAM;                   // Set units
                    bMetricMode = true;
                } else {
                    if (oConfig.getFiveDigitWeight()) {              // If five digit weight mode
                        nMaxWeight = 30000;                             // Set max weight
                    } else {
                        nMaxWeight = 3000;                              // Set max weight
                    }
                    nWeightUnit = ScaleConstants.SCAL_WU_POUND;                      // Set units
                    bMetricMode = false;
                }
            }
        } catch (DLSException e) {
            throw new APosException(e.getMessage(), ErrorConstants.APOS_E_FAILURE); // Throw exception on error
        }
    }

    /**
     * Release a scale device, removing any listeners.
     *
     * @throws APosException
     */
    @Override
    public void release() throws APosException {
        scale.removeWeightReceivedListener(this);
        //scale.removeDeviceStatusListener(this);

        super.release();
    }

    /**
     * Assign the current status notify property of a scale device.
     *
     * @param nValue int indicating the value to assign.
     * @throws APosException thrown if the device is not open, if the capability
     *                       is not supported, or if the device is enabled.
     */
    public void setStatusNotify(int nValue) throws APosException {
        checkOpened();

        if (!getCapStatusUpdate()) {
            throw new APosException("Capability not available", ErrorConstants.APOS_E_ILLEGAL);
        }

        if (getDeviceEnabled()) {
            throw new APosException("Device is enabled", ErrorConstants.APOS_E_ILLEGAL);
        }

        scale.setStatusNotify(nValue);
    }

    /**
     * Instruct a scale device to read the current weight.
     *
     * @throws APosException thrown if an exception is thrown while reading the
     *                       weight.
     */
    protected void getWeight() throws APosException {
        try {
            if (bMetricMode) {
                scale.readMetricWeight();
            } else {
                scale.readEnglishWeight();
            }
        } catch (DLSException e) {
            throw new APosException(e.getMessage(), ErrorConstants.APOS_E_FAILURE);
        }
    }

    /**
     * Instruct a scale device to asynchronously read the current weight.
     *
     * @param nTimeout int indicating a timeout for the read command.
     * @throws APosException thrown if the weight could not be read.
     */
    protected void getWeightAsync(int nTimeout) throws APosException {
        synchronized (weightLock) {
            long currTime = System.currentTimeMillis();

            if ((currentWeightData.nEndTime > currTime)
                    && (currentWeightData.nState == CMD_PENDING)) {
                throw new APosException("Read command pending", ErrorConstants.APOS_E_BUSY);
            } else {
                if (nTimeout > 0) {
                    currentWeightData.nEndTime = currTime + nTimeout;
                } else {
                    currentWeightData.nEndTime = 0;
                }
                currentWeightData.nState = CMD_PENDING;
            }
        }

        getWeight();                                  // Issue command to get weight
    }

    /**
     * Instruct a scale device to synchronously read the current weight,
     * populating the specified array.
     *
     * @param weightData int array the weight data to be populated.
     * @param nTimeout   int the timeout for the synchronous read.
     * @throws APosException thrown if an error is encountered while reading
     *                       the weight from the device.
     */
    protected void getWeightSync(int[] weightData, int nTimeout) throws APosException {

        long timeout = MAXIMUM_READ_TIMEOUT;
        int state;
        long elapsed = 0;
        long start = 0;
        boolean retry;
        if (nTimeout == 0) {
            retry = false; // don't allow retries
            timeout = DEFAULT_READ_TIMEOUT;     // use default wait period
        } else {
            retry = true;
            if ((nTimeout > 0) && (nTimeout < MAXIMUM_READ_TIMEOUT)) {
                timeout = nTimeout;
            }
        }
        synchronized (weightLock) {             // Lock object
            // we don't handle UPOS_FOREVER(-1)
            bWeightLock = false;
            currentWeightData.bRetry = retry;
            currentWeightData.nState = CMD_PENDING;       // Set weight to command pending
            getWeight();                                  // Issue command to get weight

            try {
                while (!bWeightLock) {
                    weightLock.wait(timeout - elapsed);
                    elapsed = (long) ((System.nanoTime() - start) / 1000000);
                    if (elapsed >= timeout) {
                        break;
                    }
                }
            } catch (InterruptedException ie) {
                Log.e(Tag, "getWeightSync: Interrupted exception, ", ie);
            }

            // 1.8.14
            // user could use synchronous read, then use setdataevent to generate the event,
            // but we prevent the event by clearing the data here
            currentWeightData.bSendPending = false;    // clear data count
            weightData[0] = currentWeightData.nWeight; // Save weight
            state = currentWeightData.nState;
        } // synchronized
        // If an error occurred retrieving the weight, then we likely need to
        //  throw a APosException here.
        if (state != CMD_COMPLETE) { // If error or Timeout
            weightData[0] = 0;                            // Reset to zero

            switch (state) {                      // Check errorcode.
                case CMD_PENDING:             // Command still pending, has to be a timeout
                    throw new APosException("Timeout on scale read", ErrorConstants.APOS_E_TIMEOUT);

                case CMD_COMPLETE:            // Should not see this here, but could happen
                    break;

                case CMD_ABORT:               // Async read was aborted
                    break;

                case CMD_ZERO:
                    throw new APosException("Scale needs zeroing", SCALE_NEEDS_ZEROING);

                case CMD_AT_ZERO:
                    throw new APosException("Scale at zero", SCALE_AT_ZERO);

                case CMD_MOTION:
                    throw new APosException("Scale in motion", SCALE_IN_MOTION);

                case CMD_OVERWEIGHT:
                    throw new APosException("Scale over capacity", SCALE_OVERWEIGHT);

                case CMD_UNDERZERO:
                    throw new APosException("Scale under zero", SCALE_UNDERZERO);


                default:                      // Throw exception to caller
                    throw new APosException("Scale read error", state);
            }
        }
    }


    /**
     * Read the weight from a scale device, populating the specified array.
     *
     * @param weightData int array the array to populate with the read data.
     * @param nTimeout   int the timeout for the read.
     * @throws APosException thrown if the device is not opened, claimed or
     *                       enabled.
     */
    @Override
    public void readWeight(int[] weightData, int nTimeout) throws APosException {
        checkClaimed();
        checkEnabled();

        if (bAsyncMode) {                             // If we are in async mode
            getWeightAsync(nTimeout);                      // Call the async method
        } else {
            //getWeightAsync(timeout);                      // Call the async method
            getWeightSync(weightData, nTimeout);            // Else call the sync method
        }
    }

    // 1.3

    /**
     * Indicate whether a scale device has the Display Text capability.
     *
     * @return boolean indicating whether the device has the Display Text
     * capability.
     * @throws APosException thrown if the device is not opened.
     */
    @Override
    public boolean getCapDisplayText() throws APosException {
        checkOpened();
        return false;
    }

    /**
     * Indicate whether a scale device has the Price Calculating capability.
     *
     * @return boolean indicating whether the device has the Price Calculating
     * capability.
     * @throws APosException thrown if the device is not opened.
     */
    @Override
    public boolean getCapPriceCalculating() throws APosException {
        checkOpened();
        return false;
    }

    /**
     * Indicate whether a scale device has the Tare Weight capability.
     *
     * @return boolean indicating whether the device has the Tare Weight
     * capability.
     * @throws APosException thrown if the device is not opened, not claimed
     *                       or not enabled.
     */
    @Override
    public boolean getCapTareWeight() throws APosException {
        checkClaimed();
        checkEnabled();

        return false;
    }

    /**
     * Indicate whether a scale device has the Zero Scale capability.
     *
     * @return boolean indicating whether the device has the Zero Scale
     * capability.
     * @throws APosException thrown if the device is not opened.
     */
    @Override
    public boolean getCapZeroScale() throws APosException {
        checkOpened();
        return scale.canZeroScale();
    }

    /**
     * Indicate whether a scale device has the Asynchronous Mode capability.
     *
     * @return boolean indicating whether the device has the Asynchronous Mode
     * capability.
     * @throws APosException thrown if the device is not opened.
     */
    @Override
    public boolean getAsyncMode() throws APosException {
        checkOpened();
        return bAsyncMode;
    }

    /**
     * Assign whether a scale device has the Asynchronous Mode capability.
     *
     * @param asyncMode boolean indicating whether the device has the
     *                  Asynchronous Mode capability.
     * @throws APosException thrown if the device is not opened.
     */
    @Override
    public void setAsyncMode(boolean asyncMode) throws APosException {
        checkOpened();

        bAsyncMode = asyncMode;
    }

    /**
     * Indicate whether a device has the Auto Disable capability.
     *
     * @return boolean indicating whether the device has Auto Disable.
     * @throws APosException thrown if the device is not opened.
     */
    @Override
    public boolean getAutoDisable() throws APosException {
        checkOpened();

        return bAutoDisable;
    }

    /**
     * Assign whether a scale device has the Auto Disable capability.
     *
     * @param autoDisable boolean indicating whether to Auto Disable.
     * @throws APosException thrown if the device is not opened.
     */
    @Override
    public void setAutoDisable(boolean autoDisable) throws APosException {
        checkOpened();

        bAutoDisable = autoDisable;
    }

    /**
     * Indicate whether a scale device has pending data to be sent.
     *
     * @return int indicating a 1 if data is pending, or 0 if not.
     * @throws APosException if the device is not opened.
     */
    @Override
    public int getDataCount() throws APosException {
        checkOpened();
        return (currentWeightData.bSendPending) ? 1 : 0;
    }

    /**
     * Indicate whether a scale device has Data Events enabled.  If enabled,
     * events will be raised to callbacks when data is available.
     *
     * @return boolean indicating whether Data Events are enabled.
     * @throws APosException thrown if the device is not opened.
     */
    @Override
    public boolean getDataEventEnabled() throws APosException {
        checkOpened();

        return bDataEventEnabled;
    }

    /**
     * Assign whether a scale device has Data Events enabled.  If enabled,
     * events will be raised to callbacks when data is available.
     *
     * @param dataEventEnabled boolean indicating whether Data Events are
     *                         enabled.
     * @throws APosException thrown if the device is not opened.
     */
    @Override
    public void setDataEventEnabled(boolean dataEventEnabled) throws APosException {
        checkOpened();

        bDataEventEnabled = dataEventEnabled;

        if (dataEventEnabled && getClaimed() && !getFreezeEvents()) {
            sendDataEvent();
        }
    }

    /**
     * Indicate the maximum number of text characters that can be displayed on
     * a scale device.
     *
     * @return int indicating the maximum number of text characters to display.
     * @throws APosException thrown if the device is not opened.
     */
    @Override
    public int getMaxDisplayTextChars() throws APosException {
        checkOpened();
        return 0;
    }

    /**
     * Return the Sales Price from a scale device.
     *
     * @return long indicating the Sales Price.
     * @throws APosException thrown if the device is not opened, not claimed or
     *                       not enabled.
     */
    @Override
    public long getSalesPrice() throws APosException {
        checkClaimed();
        checkEnabled();
        return 0;
    }

    /**
     * Return the Tare Weight from a scale device.
     *
     * @return int indicating the Tare Weight of a scale.
     * @throws APosException thrown if the device is not opened, not claimed or
     *                       not enabled.
     */
    @Override
    public int getTareWeight() throws APosException {
        checkClaimed();
        checkEnabled();
        return nTareWeight;
    }

    /**
     * Assign the Tare Weight of a scale device.
     *
     * @param tareWeight int indicating the Tare Weight to assign.
     * @throws APosException thrown if the device is not opened, not claimed or
     *                       not enabled.
     */
    @Override
    public void setTareWeight(int tareWeight) throws APosException {
        checkClaimed();
        checkEnabled();
        nTareWeight = tareWeight;
    }

    /**
     * Return the Unit Price from a scale device.
     *
     * @return long indicating the Unit Price of the scale device.
     * @throws APosException thrown if the device is not opened, not claimed or
     *                       not enabled.
     */
    @Override
    public long getUnitPrice() throws APosException {
        checkClaimed();
        checkEnabled();
        return lUnitPrice;
    }

    /**
     * Assign the Unit Price of a scale device.
     *
     * @param unitPrice long indicating the Unit Price to assign.
     * @throws APosException thrown if the device is not opened, not claimed or
     *                       not enabled.
     */
    @Override
    public void setUnitPrice(long unitPrice) throws APosException {
        checkClaimed();
        checkEnabled();
        lUnitPrice = unitPrice;
    }

    /**
     * Indicate whether zero is a valid weight on a scale device.
     *
     * @return boolean indicating whether zero is a valid weight.
     * @throws APosException thrown by the scale implementation.
     */
    @Override
    public boolean getZeroValid() throws APosException {
        checkOpened();
        return scale.getZeroValid();
    }

    /**
     * Assign whether zero is a valid weight on a scale device.
     *
     * @param zeroValid boolean indicating whether zero is a valid weight.
     * @throws APosException thrown by the scale implementation.
     */
    @Override
    public void setZeroValid(boolean zeroValid) throws APosException {
        checkOpened();
        scale.setZeroValid(zeroValid);
    }

    /**
     * Clear the Input on a scale device.
     *
     * @throws APosException thrown if the device is not opened or not claimed.
     */
    @Override
    public void clearInput() throws APosException {
        checkClaimed();

        synchronized (weightLock) {
            currentWeightData.nState = CMD_ABORT;
            currentWeightData.bSendPending = false;
            currentWeightData.nEndTime = 0;
            bWeightLock = true;
            weightLock.notifyAll();            // Should not need, for Sync reads,
            //  but just incase this is called by
            //  a separate thread, let's kill the wait.
        }
    }

    /**
     * Display specified text on a scale device.
     *
     * @param data String containing the text to display.
     * @throws APosException thrown if the device is not opened, not claimed or
     *                       not enabled.
     */
    @Override
    public void displayText(String data) throws APosException {
        checkClaimed();
        checkEnabled();
    }

    /**
     * Zero a scale device.
     *
     * @throws APosException thrown if the device is not opened, not claimed,
     *                       not enabled, or if an exception occurs zeroing the scale, or if zeroing
     *                       the scale is not supported.
     */
    @Override
    public void zeroScale() throws APosException {
        checkClaimed();
        checkEnabled();

        if (scale.canZeroScale()) {
            try {
                scale.zeroScale();
            } catch (DLSException e) {
                throw new APosException(e.getMessage(), ErrorConstants.APOS_E_FAILURE);
            }
        } else {
            throw new APosException("Zero scale not supported", ErrorConstants.APOS_E_ILLEGAL);
        }
    }

    /**
     * Delete a DLSScaleService instance.
     *
     * @throws APosException not actually thrown in this class.
     */
    @Override
    public void delete() throws APosException {
    }

    /**
     * Notified by device when there is a status event.
     * 1.9 for live weight
     *
     * @param nStatusCode int the status code.
     */
    @Override
    public void onDeviceStatus(int nStatusCode) {
        // don't log live weight events
        //log.trace(this, "onDeviceStatus: "+nStatusCode);
        if (scale.getStatusNotify() == ScaleConstants.SCAL_SN_ENABLED) {
            getEventCallbacks().fireEvent(new StatusUpdateEvent(getEventCallbacks().getEventSource(), nStatusCode), EventCallback.EventType.StatusUpdate);
        }
    }

    /**
     * Event listener called when an error occurs on the device.
     *
     * @param nErrorCode int the Error Code.
     */
    @Override
    public void onDeviceError(int nErrorCode) {

        synchronized (weightLock) {
            if (nErrorCode == ERR_NO_WEIGHT || nErrorCode == ERR_SCALE_AT_ZERO) {
                if (bExceptionOnMotion) {
                    currentWeightData.nWeight = 0;
                    if (nErrorCode == ERR_NO_WEIGHT) {
                        currentWeightData.nState = CMD_MOTION;
                    } else {
                        currentWeightData.nState = CMD_AT_ZERO;
                    }

                    currentWeightData.bSendPending = false;
                    currentWeightData.bRetry = false;
                    currentWeightData.nEndTime = 0;
                    bWeightLock = true;
                    weightLock.notifyAll();
                } else if (currentWeightData.nState == CMD_PENDING) {
                    // If still waiting on a weight
                    // If we got an error saying that the scale is moving, and we are currently
                    //  in a CMD_PENDING state, then we need to re-issue the read command inorder
                    //  to get a weight from the scale.

                    // Due to the fact that if the scale is shaking, we can actually send
                    //  the re-read command upto 20 times per second, lets put a quick 1/10th
                    //  of a second slow down in here.

                    if (bAsyncMode) {
                        long currTime = System.currentTimeMillis();
                        if (currentWeightData.nEndTime < currTime) {
                            currentWeightData.nState = CMD_TIMEOUT;
                            currentWeightData.nEndTime = 0;
                            return;
                        }
                    } else {
                        if (!currentWeightData.bRetry) {
                            currentWeightData.nWeight = 0;
                            currentWeightData.bSendPending = false;
                            currentWeightData.nEndTime = 0;
                            bWeightLock = true;
                            weightLock.notifyAll();
                        }
                    }

                    //sleep for 100 ms
                    long timeout = 100;
                    long elapsed = 0;
                    long start = System.nanoTime();
                    synchronized (sleepLock) {
                        try {
                            while (elapsed < timeout) {
                                sleepLock.wait(timeout - elapsed);
                                elapsed = (long) ((System.nanoTime() - start) / 1000000);
                            }
                        } catch (InterruptedException ie) {
                            Log.e(Tag, "onDeviceError: Interrupted. ", ie);
                        }
                    }

                    try {
                        getWeight();                                // Re-issue a get weight command
                    } catch (APosException jpe) {
                        currentWeightData.nState = ErrorConstants.APOS_E_FAILURE;  // If error, set failure
                        bWeightLock = true;
                        weightLock.notifyAll();              // Notify waiters
                    }
                }
            } else {
                currentWeightData.nState = ErrorConstants.APOS_E_FAILURE;
                ErrorEvent event = null;
                switch (nErrorCode) {
                    case ERR_CMD:
                        event = new ErrorEvent(
                                this,
                                ErrorConstants.APOS_E_FAILURE,
                                ERR_CMD,
                                ErrorConstants.APOS_EL_OUTPUT,
                                ErrorConstants.APOS_ER_CLEAR);

                        break;
                    case ERR_DATA:
                        event = new ErrorEvent(
                                this,
                                ErrorConstants.APOS_E_FAILURE,
                                ERR_DATA,
                                ErrorConstants.APOS_EL_OUTPUT,
                                ErrorConstants.APOS_ER_CLEAR);
                        break;
                    case ERR_READ:
                        event = new ErrorEvent(
                                this,
                                ErrorConstants.APOS_E_FAILURE,
                                ERR_READ,
                                ErrorConstants.APOS_EL_INPUT,
                                ErrorConstants.APOS_ER_CLEAR);
                        break;
                    case ERR_NO_DISPLAY:
                        event = new ErrorEvent(
                                this,
                                ErrorConstants.APOS_E_NOHARDWARE,
                                ERR_NO_DISPLAY,
                                ErrorConstants.APOS_EL_INPUT,
                                ErrorConstants.APOS_ER_CLEAR);
                        break;
                    case ERR_HARDWARE:
                        event = new ErrorEvent(
                                this,
                                ErrorConstants.APOS_E_FAILURE,
                                ERR_HARDWARE,
                                ErrorConstants.APOS_EL_INPUT,
                                ErrorConstants.APOS_ER_CLEAR);
                        break;
                    case ERR_CMD_REJECT:
                        event = new ErrorEvent(
                                this,
                                ErrorConstants.APOS_E_FAILURE,
                                ERR_CMD_REJECT,
                                ErrorConstants.APOS_EL_OUTPUT,
                                ErrorConstants.APOS_ER_CLEAR);
                        break;
                    case ERR_CAPACITY:
                        currentWeightData.nWeight = 0;
                        currentWeightData.nState = CMD_OVERWEIGHT;
                        currentWeightData.bSendPending = false;
                        currentWeightData.nEndTime = 0;
                        event = new ErrorEvent(this,
                                ErrorConstants.APOS_ESCAL_OVERWEIGHT,
                                ERR_CAPACITY,
                                ErrorConstants.APOS_EL_INPUT,
                                ErrorConstants.APOS_ER_CLEAR);
                        break;

                    case ERR_SCALE_UNDER_ZERO:
                        currentWeightData.nWeight = 0;
                        currentWeightData.nState = CMD_UNDERZERO;
                        currentWeightData.bSendPending = false;
                        currentWeightData.nEndTime = 0;
                        event = new ErrorEvent(this,
                                ErrorConstants.APOS_ESCAL_UNDER_ZERO,
                                ERR_SCALE_UNDER_ZERO,
                                ErrorConstants.APOS_EL_INPUT,
                                ErrorConstants.APOS_ER_CLEAR);
                        break;
                    case ERR_REQUIRES_ZEROING:
                        currentWeightData.nWeight = 0;
                        currentWeightData.nState = CMD_ZERO;
                        currentWeightData.bSendPending = false;
                        currentWeightData.nEndTime = 0;
                        event = new ErrorEvent(this,
                                ErrorConstants.APOS_E_FAILURE,
                                ERR_REQUIRES_ZEROING,
                                ErrorConstants.APOS_EL_INPUT,
                                ErrorConstants.APOS_ER_CLEAR);
                        break;

                    case ERR_WARMUP:
                        event = new ErrorEvent(this,
                                ErrorConstants.APOS_E_FAILURE,
                                ERR_WARMUP,
                                ErrorConstants.APOS_EL_INPUT,
                                ErrorConstants.APOS_ER_CLEAR);
                        break;
                    case ERR_DUPLICATE:
                        event = new ErrorEvent(this,
                                ErrorConstants.APOS_E_FAILURE,
                                ERR_DUPLICATE,
                                ErrorConstants.APOS_EL_INPUT,
                                ErrorConstants.APOS_ER_CLEAR);
                        break;
                    case ERR_DEVICE_REMOVED:
                        event = new ErrorEvent(this,
                                ErrorConstants.APOS_E_NOHARDWARE,
                                ERR_DEVICE_REMOVED,
                                ErrorConstants.APOS_EL_INPUT,
                                ErrorConstants.APOS_ER_CLEAR);
                        break;
                }
                if (getEventCallbacks() != null && event != null) {
                    getEventCallbacks().fireEvent(event,
                            EventCallback.EventType.Error);
                } else {
                    Log.e(Tag, "Can not fire error event for error: " + nErrorCode);
                }
                bWeightLock = true;
                weightLock.notifyAll();
            }
        }
    }

    /**
     * Event callback for when a weight is received from a scale device.
     *
     * @param nWeight int indicating the weight received.
     */
    @Override
    public void onWeightReceived(int nWeight) {

        try {
            if (getAutoDisable()) {
                setDeviceEnabled(false);
            }
        } catch (APosException e) {
            Log.e(Tag, "onWeightReceived: APosException. ", e);
        }

        synchronized (weightLock) {
            currentWeightData.nWeight = nWeight;
            currentWeightData.nState = CMD_COMPLETE;
            currentWeightData.bSendPending = true;
            currentWeightData.bRetry = false;
            if (bAsyncMode) {
                if (currentWeightData.nEndTime > 0) {
                    long currTime = System.currentTimeMillis();
                    if (currentWeightData.nEndTime < currTime) {
                        currentWeightData.nState = CMD_TIMEOUT;
                    }
                }
            }
            currentWeightData.nEndTime = 0;
            bWeightLock = true;
            weightLock.notifyAll();
        }

        // 1.8.14
        // check mode here because we want setDataEventEnabled to fire event
        // but prevent the event if a synchronous read
        if (bAsyncMode) { // prevent data event when synchronous
            if (currentWeightData.nState == CMD_COMPLETE) {
                sendDataEvent();
            }
        }
    }

    /**
     * Fire event telling app that data is available
     */
    @Override
    public void sendDataEvent() {
        try {
            if (getDataCount() != 0) {                   // If data in queue
                if (bDataEventEnabled && !getFreezeEvents()) {
                    bDataEventEnabled = false;
                    synchronized (weightLock) {
                        getEventCallbacks().fireEvent(new DataEvent(getEventCallbacks().getEventSource(), currentWeightData.nWeight), EventCallback.EventType.Data);
                        currentWeightData.bSendPending = false;
                    }
                }
            }
        } catch (APosException e) {
            Log.e(Tag, "sendDataEvent: APosException. ", e);
        }
    }

    //NEW price calculating scale stubs

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getCapFreezeValue() throws APosException {
        checkOpened();

        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getCapReadLiveWeightWithTare() throws APosException {
        checkOpened();

        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getCapSetPriceCalculationMode() throws APosException {
        checkOpened();

        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getCapSetUnitPriceWithWeightUnit() throws APosException {
        checkOpened();
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getCapSpecialTare() throws APosException {
        checkOpened();
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getCapTarePriority() throws APosException {
        checkOpened();
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getMinimumWeight() throws APosException {
        checkOpened();
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void doPriceCalculating(int[] ints, int[] ints1, long[] longs, long[] longs1, int[] ints2, int[] ints3, int[] ints4, long[] longs2, int i) throws APosException {
        throw new APosException("doPriceCalculating() Not Supported", ErrorConstants.APOS_E_ILLEGAL);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void freezeValue(int i, boolean bln) throws APosException {
        throw new APosException("freezeValue() - Not supported", ErrorConstants.APOS_E_ILLEGAL);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void readLiveWeightWithTare(int[] ints, int[] ints1, int i) throws APosException {
        throw new APosException("readLiveWeightWithTare() Not Supported", ErrorConstants.APOS_E_ILLEGAL);
    }

    /**
     * Populate an array with the scale specific i-h-s data retrieved from a
     * device.
     *
     * @param statisticsBuffer String array denoting the array to populate.
     * @throws APosException thrown if the device is not opened and not claimed,
     *                       or if an exception is encountered while retrieving i-h-s data.
     */
    void retrieveScaleStatistics(String[] statisticsBuffer) throws APosException {
        /*
         TODO: This method does not populate the statisticsBuffer variable.
         I am not sure what the buffer is supposed to be populated with as the
         statistics instance is a HashMap of Strings.  The statisticsBuffer
         variable is a flat String array.  It would be perfectly acceptable
         to populate the variable with a flattened HashMap.
         The array itself is unspecified size, so it is very difficult to actually
         populate it from this method as it might require re-allocating the
         array to accommodate the size of the statistics.
         */
        if (statisticsBuffer == null) {
            return;
        }

        if (!getCapStatisticsReporting()) {
            return;
        }

        checkClaimed();

        try {
            statistics = device.getStatistics();
        } catch (DLSException de) {
            throw new APosException(de.getMessage(), ErrorConstants.APOS_E_FAILURE);
        }
        statistics.put("UnifiedPOSVersion", VERSION);
        statistics.put(DLSJposConst.DLS_S_CAP_PWR_REPORT, (getCapPowerReporting() == CommonsConstants.PR_ADVANCED ? "Advanced" : (getCapPowerReporting() == CommonsConstants.PR_STANDARD ? "Standard" : "None")));
        statistics.put(DLSJposConst.DLS_S_CAP_STATS_REPORT, (getCapStatisticsReporting()));
        statistics.put(DLSJposConst.DLS_S_CAP_UPDATE_STATS, (getCapUpdateStatistics()));
        statistics.put(DLSJposConst.DLS_S_CAP_COMP_FW_VER, (getCapCompareFirmwareVersion()));
        statistics.put(DLSJposConst.DLS_S_CAP_UPDATE_FW, (getCapUpdateFirmware()));

        String model, serial, firmware, controlVersion, sServiceVersion;
        model = (String) statistics.get(DLSJposConst.DLS_S_MODEL_NAME);            // Get model name
        serial = (String) statistics.get(DLSJposConst.DLS_S_SERIAL_NUMBER);        // Get serial number
        firmware = (String) statistics.get(DLSJposConst.DLS_S_FIRMWARE_VERSION);   // Get firmware
        if (model == null) {
            model = "";                        // If null, set to empty string
            //TODO: It was DLSJposConst.DLS_S_SERIAL_NUMBER but probably it should be DLSJposConst.DLS_S_MODEL_NAME
            statistics.put(DLSJposConst.DLS_S_MODEL_NAME, model);
        }
        if (serial == null) {
            serial = "";                      // If null, set to empty string
            statistics.put(DLSJposConst.DLS_S_SERIAL_NUMBER, serial);
        }
        if (firmware == null) {
            firmware = "";                   // If null, set to empty string
            statistics.put(DLSJposConst.DLS_S_FIRMWARE_VERSION, firmware);
        }
        String ver = Integer.toString(getDeviceServiceVersion());
        controlVersion = Integer.valueOf(ver.substring(0, 1)) + "." + Integer.valueOf(ver.substring(1, 4));
        sServiceVersion = Integer.valueOf(ver.substring(0, 1)) + "." + Integer.valueOf(ver.substring(1, 4)) + "." + Integer.valueOf(ver.substring(5, 7));
        statistics.put(DLSJposConst.DLS_S_SERVICE_VERSION, sServiceVersion);
        float fcv = Float.parseFloat(controlVersion.trim());
        int cv = (int) (fcv * 100000);
        statistics.put(DLSJposConst.DLS_S_CONTROL_VERSION, cv);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setPriceCalculationMode(int i) throws APosException {
        throw new APosException("setPriceCalculationMode() Not Supported", ErrorConstants.APOS_E_ILLEGAL);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSpecialTare(int i, int i1) throws APosException {
        throw new APosException("setSpecialTare() Not Supported", ErrorConstants.APOS_E_ILLEGAL);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setTarePriority(int i) throws APosException {
        throw new APosException("setTarePriority() Not Supported", ErrorConstants.APOS_E_ILLEGAL);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setUnitPriceWithWeightUnit(long l, int i, int i1, int i2) throws APosException {
        throw new APosException("setUnitPriceWithWeightUnit() Not Supported", ErrorConstants.APOS_E_ILLEGAL);
    }
}
