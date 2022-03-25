package com.datalogic.dlapos.androidpos.interpretation;

import android.content.Context;
import android.util.Log;

import com.datalogic.dlapos.androidpos.common.DLSCConfig;
import com.datalogic.dlapos.androidpos.common.DLSException;
import com.datalogic.dlapos.androidpos.common.DLSJposConst;
import com.datalogic.dlapos.androidpos.common.DLSProperties;
import com.datalogic.dlapos.androidpos.common.DLSScaleConfig;
import com.datalogic.dlapos.androidpos.common.DLSState;
import com.datalogic.dlapos.commons.constant.ErrorConstants;
import com.datalogic.dlapos.commons.constant.ScaleConstants;
import com.datalogic.dlapos.commons.support.APosException;
import com.datalogic.dlapos.confighelper.DLAPosConfigHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Abstract class representing a generic Datalogic scale.
 */
public abstract class DLSScale extends DLSDevice {

    private final String Tag = "DLSScale";

    boolean bMetricMode = false;
    private int nLiveWeight = 0;
    private int nPollRate = 500;  // poll rate in milliseconds
    int nStatusNotify = ScaleConstants.SCAL_SN_DISABLED;
    private int nStatusValue = 0;
    DLSScaleConfig scaleConfig = null;
    ArrayList<Object> weightReceivedListeners;
    private final ScheduledExecutorService m_scheduler = Executors.newScheduledThreadPool(1);
    private ScheduledFuture<?> m_liveWeight = null;
    boolean m_lwActive = false;

    /**
     * Creates a new instance of DLSScale
     */
    public DLSScale() {
        super();
        weightReceivedListeners = new ArrayList<>();
    }

    /**
     * Adds a listener to the collection of listeners to be notified when a
     * weight value is received.
     *
     * @param listener WeightReceivedListener to add
     */
    public void addWeightReceivedListener(WeightReceivedListener listener) {
        weightReceivedListeners.add(listener);
    }

    /**
     * Indicates whether the scale supports the status update functionality
     * based off the {@code apos.json} configuration.
     *
     * @return boolean indicating if the scale object is able to status update
     */
    public boolean canStatusUpdate() {
        return scaleConfig.getCanStatusUpdate();
    }

    /**
     * Indicates whether or not the current scale interface supports the ability
     * to zero the scale.
     *
     * @return boolean indicating whether the scale can be zeroed
     */
    public abstract boolean canZeroScale();

    /**
     * Clears the remote scale display.
     * <p>
     * Only available for USB scales in United Kingdom operation mode. This
     * method has no effect otherwise.
     *
     * @throws DLSException not thrown
     */
    public abstract void clearDisplay() throws DLSException;

    /**
     * Fires a weight event to all listeners indicating that a weight value has
     * been received.
     *
     * @param weight int indicating the weight value to pass to the listeners
     */
    protected void fireWeightReceivedEvent(int weight) {
        WeightReceivedListener listener;
        for (Enumeration e = Collections.enumeration(weightReceivedListeners); e.hasMoreElements(); ) {
            listener = (WeightReceivedListener) e.nextElement();
            listener.onWeightReceived(weight);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DLSCConfig getConfiguration() {
        return scaleConfig;
    }

    /**
     * Gets the current live weight value.
     * <p>
     * Updated when a stable weight is read at the current poll rate.
     *
     * @return int indicating the weight value
     */
    public synchronized int getLiveWeight() {
        return nLiveWeight;
    }

    /**
     * Indicates whether metric mode is enabled.
     *
     * @return boolean indicating whether metric mode is enabled
     */
    public boolean getMetricMode() {
        return bMetricMode;
    }

    /**
     * Returns the poll rate value used to update the live weight value.
     *
     * @return int indicating the poll rate in milliseconds.
     */
    public int getPollRate() {
        return nPollRate;
    }

    /**
     * {@inheritDoc}
     * <p>
     * <b> Scale devices do not support i-h-s commands, only the following required
     * statistics will be updated:</b>
     * <ul>
     * <li> {@code DLS_S_DEVICE_CATEGORY}
     * <li> {@code DLS_S_GOOD_WEIGHT_READ_COUNT} = {@code NA}
     * <li> {@code DLS_S_INTERFACE}
     * </ul>
     */
    @Override
    public HashMap<String, Object> getStatistics() throws DLSException {
        throw new DLSException(DLSJposConst.DLS_E_NOTSUPPORTED, "getStatistics not supported for this device.");
    }

    /**
     * Returns the scales status notify property value, as defined by JPOS
     * specification.
     * <ul>
     * <li> {@code 1} - {@code SCAL_SN_DISABLED}
     * <li> {@code 2} - {@code SCAL_SN_ENABLED}
     * </ul>
     *
     * @return int indicating the value of the status notify property
     */
    public int getStatusNotify() {
        return nStatusNotify;
    }

    /**
     * Returns the current scale status value, as defined by JPOS specification.
     * <ul>
     * <li> {@code 11} - {@code SCAL_SUE_STABLE_WEIGHT}
     * <li> {@code 12} - {@code SCAL_SUE_WEIGHT_UNSTABLE}
     * <li> {@code 13} - {@code SCAL_SUE_WEIGHT_ZERO}
     * <li> {@code 14} - {@code SCAL_SUE_WEIGHT_OVERWEIGHT}
     * <li> {@code 15} - {@code SCAL_SUE_NOT_READY}
     * <li> {@code 16} - {@code SCAL_SUE_WEIGHT_UNDER_ZERO}
     * </ul>
     *
     * @return int indicating the status of the scale
     */
    protected synchronized int getStatusValue() {
        return nStatusValue;
    }

    /**
     * Returns the zeroValid property value from the current scale
     * configuration object.
     *
     * @return boolean indicating if zero is a valid weight
     */
    public boolean getZeroValid() {
        return scaleConfig.getZeroValid();
    }


    /**
     * Indicates whether or not live weight is running.
     *
     * @return boolean indicating if the live weight thread is running
     */
    public boolean isLiveWeight() {
        return m_lwActive;
    }

    @Override
    void internalClaim() throws DLSException {
        DLSProperties options = DLSProperties.getInstance(context);


        if (!isAlive()) {
            throw new DLSException(ErrorConstants.APOS_E_ILLEGAL, "Error - no response from scale");
        }

        if (options.isAutoLoadConfig()) {
            updateConfiguration();                // Update configuration from the apos.json file
        }
        reportConfiguration();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void open(String strLogicalName, Context context) throws DLSException {
        super.open(strLogicalName, context);

        try {
            scaleConfig = new DLSScaleConfig();
            if (!DLAPosConfigHelper.getInstance(context).isInitialized())
                DLAPosConfigHelper.getInstance(context).initialize(context);
            scaleConfig.loadConfiguration(strLogicalName, DLAPosConfigHelper.getInstance(context).getProfileManager());
            nPollRate = scaleConfig.getLiveWeightPollRate();
            bMetricMode = scaleConfig.getMetricWeightMode();
        } catch (APosException e) {
            throw new DLSException(ErrorConstants.APOS_ERROR, "Loading configuration: " + e.getLocalizedMessage());
        }

        setState(DLSState.OPENED);
    }

    /**
     * Requests weight data from the scale in pounds. Returns immediately.
     *
     * @throws DLSException not thrown
     */
    public abstract void readEnglishWeight() throws DLSException;

    /**
     * Requests weight data from the scale in kilograms. Returns immediately.
     *
     * @throws DLSException not thrown
     */
    public abstract void readMetricWeight() throws DLSException;


    /**
     * Requests status and weight changes for live weight.
     *
     * @throws DLSException if the request is interrupted, times out, or
     *                      weight is invalid
     */
    public abstract void readStatusWeight() throws DLSException;


    /**
     * Removes a weight received listener for this scale object.
     *
     * @param wrl WeightReceivedListener instance to remove
     */
    public void removeWeightReceivedListener(WeightReceivedListener wrl) {
        weightReceivedListeners.remove(wrl);
    }


    /**
     * Reads the current configuration from the scale and updates the
     * configuration object with the data received.
     * <p>
     * Only available for USB scales. This method has no effect otherwise.
     *
     * @throws DLSException not thrown
     */
    public abstract void reportConfiguration() throws DLSException;


    /**
     * Updates the current live weight value.
     *
     * @param nWeight int indicating the weight value
     */
    protected synchronized void setLiveWeight(int nWeight) {
        nLiveWeight = nWeight;
    }


    /**
     * Enables/disables metric mode on the scale.
     * <p>
     * Scale configuration is only updated on USB scales. Otherwise, this
     * method solely updates the configuration object, having no effect on the
     * actual scale device.
     *
     * @param bValue boolean indicating whether or not to enable metric mode
     */
    public void setMetricMode(boolean bValue) {
        bMetricMode = bValue;
    }


    /**
     * Updates the scales status notify property value, as defined by JPOS
     * specification.
     * <ul>
     * <li> {@code 1} - {@code SCAL_SN_DISABLED}
     * <li> {@code 2} - {@code SCAL_SN_ENABLED}
     * </ul>
     *
     * @param nValue int indicating the value of the status notify property
     */
    public void setStatusNotify(int nValue) {
        if (nStatusNotify != nValue) {
            if (m_lwActive) {
                m_liveWeight.cancel(true);
            }
        }
        nStatusNotify = nValue;
    }


    /**
     * Updates the current scale status value, as defined by JPOS specification.
     * <ul>
     * <li> {@code 11} - {@code SCAL_SUE_STABLE_WEIGHT}
     * <li> {@code 12} - {@code SCAL_SUE_WEIGHT_UNSTABLE}
     * <li> {@code 13} - {@code SCAL_SUE_WEIGHT_ZERO}
     * <li> {@code 14} - {@code SCAL_SUE_WEIGHT_OVERWEIGHT}
     * <li> {@code 15} - {@code SCAL_SUE_NOT_READY}
     * <li> {@code 16} - {@code SCAL_SUE_WEIGHT_UNDER_ZERO}
     * </ul>
     *
     * @param nStatus int indicating the status of the scale
     */
    protected synchronized void setStatusValue(int nStatus) {
        nStatusValue = nStatus;
    }


    /**
     * Updates the zeroValid property in the current scale configuration object.
     *
     * @param zeroValid boolean indicating if zero is a valid weight
     */
    public void setZeroValid(boolean zeroValid) {
        scaleConfig.setZeroValid(zeroValid);
    }


    /**
     * Starts the live weight polling thread.
     */
    protected void startLiveWeight() {
        if (DLSJposConst.updateInProgress) {
            return;
        }

        if (m_lwActive) {
            return;
        }


        final Runnable lwThread = () -> {
            try {
                readStatusWeight();
            } catch (DLSException ex) {
                Log.e(Tag, "Exception reading Live Weight. ", ex);
            }
        };
        m_liveWeight = m_scheduler.scheduleAtFixedRate(lwThread, nPollRate, nPollRate, TimeUnit.MILLISECONDS);
        m_lwActive = true;

    }


    /**
     * Stops the live weight polling thread.
     */
    protected void stopLiveWeight() {
        if (m_lwActive) {
            m_liveWeight.cancel(true);
            m_lwActive = false;
        }
    }


    /**
     * Updates the scale configuration with parameters from the current scale
     * configuration object.
     * <p>
     * Only available for USB scales. This method has no effect otherwise.
     *
     * @throws DLSException not thrown
     */
    public abstract void updateConfiguration() throws DLSException;


    /**
     * Zeroes the scale, setting the "no load" reading to the current weight
     * reading.
     *
     * @throws DLSException not thrown
     */
    public abstract void zeroScale() throws DLSException;
}
