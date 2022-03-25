package com.datalogic.dlapos.androidpos.common;

import android.util.Log;

/**
 * Class representing a scale configuration.
 */
public class DLSScaleConfig extends DLSCConfig {
    private static final String TAG = DLSScaleConfig.class.getSimpleName();

    /**
     * Key to access the accept statistics command configuration.
     */
    public static final String KEY_CANACCEPTSTATISTICSCMD = "canAcceptStatisticsCmd";
    /**
     * Key to access the can notify power change configuration.
     */
    public static final String KEY_CANNOTIFYPOWERCHANGE = "canNotifyPowerChange";
    /**
     * Key to access the can status update configuration.
     */
    public static final String KEY_CANSTATUSUPDATE = "canStatusUpdate";
    /**
     * Key to access the display required configuration.
     */
    public static final String KEY_DISPLAYREQUIRED = "displayRequired";
    /**
     * Key to access the enforce zero return configuration.
     */
    public static final String KEY_ENFORCEZERORETURN = "enforceZeroReturn";
    /**
     * Key to access the five digit weight configuration.
     */
    public static final String KEY_FIVEDIGITWEIGHT = "fiveDigitWeight";
    /**
     * Key to access the indicate zero with led configuration.
     */
    public static final String KEY_INDICATEZEROWITHLED = "indicateZeroWithLED";
    /**
     * Key to access the live weight poll rate configuration.
     */
    public static final String KEY_LIVEWEIGHTPOLLRATE = "liveWeightPollRate";
    /**
     * Key to access the MBeans enabled configuration.
     */
    public static final String KEY_MBEANSENABLED = "MBeansEnabled";
    /**
     * Key to access the metric weight mode configuration.
     */
    public static final String KEY_METRICWEIGHTMODE = "metricWeightMode";
    /**
     * Key to access the operation mode configuration.
     */
    public static final String KEY_OPERATIONMODE = "operationMode";
    /**
     * Key to access the vibration sensitivity configuration.
     */
    public static final String KEY_VIBRATIONSENSITIVITY = "vibrationSensitivity";
    /**
     * Key to access the WMI enabled configuration.
     */
    public static final String KEY_WMIENABLED = "WMIEnabled";
    /**
     * Key to access the zero valid configuration.
     */
    public static final String KEY_ZEROVALID = "zeroValid";

    private static final boolean DEF_CANACCEPTSTATISTICSCMD = false;
    private static final boolean DEF_CANNOTIFYPOWERCHANGE = false;
    private static final boolean DEF_CANSTATUSUPDATE = true;
    private static final boolean DEF_DISPLAYREQUIRED = true;
    private static final boolean DEF_ENFORCEZERORETURN = false;
    private static final boolean DEF_FIVEDIGITWEIGHT = true;
    private static final boolean DEF_INDICATEZEROWITHLED = true;
    private static final int DEF_LIVEWEIGHTPOLLRATE = 250;
    private static final boolean DEF_MBEANSENABLED = false;
    private static final boolean DEF_METRICWEIGHTMODE = false;
    private static final int DEF_OPERATIONMODE = 0;
    private static final int DEF_VIBRATIONSENSITIVITY = 2;
    private static final boolean DEF_WMIENABLED = false;
    private static final boolean DEF_ZEROVALID = false;

    /**
     * Default constructor.
     */
    public DLSScaleConfig() {
        super();
        initializeMap();
    }

    /**
     * Constructor exploiting an external configuration.
     *
     * @param config the configuration to parse and use.
     */
    public DLSScaleConfig(byte[] config) {
        this();
        loadConfig(config);
    }

    private void initializeMap() {
        setCanAcceptStatisticsCmd(DEF_CANACCEPTSTATISTICSCMD);
        setCanNotifyPowerChange(DEF_CANNOTIFYPOWERCHANGE);
        setCanStatusUpdate(DEF_CANSTATUSUPDATE);
        setDisplayRequired(DEF_DISPLAYREQUIRED);
        setEnforceZeroReturn(DEF_ENFORCEZERORETURN);
        setFiveDigitWeight(DEF_FIVEDIGITWEIGHT);
        setIndicateZeroWithLed(DEF_INDICATEZEROWITHLED);
        setLiveWeightPollRate(DEF_LIVEWEIGHTPOLLRATE);
        setMBeansEnabled(DEF_MBEANSENABLED);
        setMetricWeightMode(DEF_METRICWEIGHTMODE);
        setOperationMode(DEF_OPERATIONMODE);
        setVibrationSensitivity(DEF_VIBRATIONSENSITIVITY);
        setWMIEnabled(DEF_WMIENABLED);
        setZeroValid(DEF_ZEROVALID);
    }

    /**
     * Function to check if the scale can accept statistics commands (default value {@value #DEF_CANACCEPTSTATISTICSCMD}).
     *
     * @return true if the scale can accept statistics commands, false otherwise.
     */
    public boolean getCanAcceptStatisticsCmd() {
        return getOptionAsBool(KEY_CANACCEPTSTATISTICSCMD);
    }

    /**
     * Function to check if the scale can notify power changes (default value {@value #DEF_CANNOTIFYPOWERCHANGE}).
     *
     * @return true if the scale can notify power changes, false otherwise.
     */
    public boolean getCanNotifyPowerChange() {
        return getOptionAsBool(KEY_CANNOTIFYPOWERCHANGE);
    }

    /**
     * Function to check if the scale can notify status updates (default value {@value #DEF_CANSTATUSUPDATE}).
     *
     * @return true if the scale can notify status updates, false otherwise.
     */
    public boolean getCanStatusUpdate() {
        return getOptionAsBool(KEY_CANSTATUSUPDATE);
    }

    /**
     * Function to check if the scale requires a display (default value {@value #DEF_DISPLAYREQUIRED}).
     *
     * @return true if the scale requires a display, false otherwise.
     */
    public boolean getDisplayRequired() {
        return getOptionAsBool(KEY_DISPLAYREQUIRED);
    }

    /**
     * Function to check if the scale enforces zero return (default value {@value #DEF_ENFORCEZERORETURN}).
     *
     * @return true if the scale enforces zero return, false otherwise.
     */
    public boolean getEnforceZeroReturn() {
        return getOptionAsBool(KEY_ENFORCEZERORETURN);
    }

    /**
     * Function to check if the scale uses five digits weights (default value {@value #DEF_FIVEDIGITWEIGHT}).
     *
     * @return true if the scale uses five digits weights, false otherwise.
     */
    public boolean getFiveDigitWeight() {
        return getOptionAsBool(KEY_FIVEDIGITWEIGHT);
    }

    /**
     * Function to check if the scale indicates zero with led (default value {@value #DEF_INDICATEZEROWITHLED}).
     *
     * @return true if the scale indicates zero with led, false otherwise.
     */
    public boolean getIndicateZeroWithLed() {
        return getOptionAsBool(KEY_INDICATEZEROWITHLED);
    }

    /**
     * Function to get the live weight poll rate (default value {@value #DEF_LIVEWEIGHTPOLLRATE}).
     *
     * @return the live weight poll rate.
     */
    public int getLiveWeightPollRate() {
        return getOptionAsInt(KEY_LIVEWEIGHTPOLLRATE);
    }

    /**
     * Function to check if MBeans is enabled (default value {@value #DEF_MBEANSENABLED}).
     *
     * @return true of MBeans is enabled, false otherwise.
     */
    public boolean getMBeansEnabled() {
        return getOptionAsBool(KEY_MBEANSENABLED);
    }

    /**
     * Function to check if the scale is in metric weight mode (default value {@value #DEF_METRICWEIGHTMODE}).
     *
     * @return true if the scale is in metric weight mode, false otherwise.
     */
    public boolean getMetricWeightMode() {
        return getOptionAsBool(KEY_METRICWEIGHTMODE);
    }

    /**
     * Function to get the scale's operation mode (default value {@value #DEF_OPERATIONMODE}).
     *
     * @return the scale's operation mode.
     */
    public int getOperationMode() {
        return getOptionAsInt(KEY_OPERATIONMODE);
    }

    /**
     * Function to get the vibration sensitivity (default value {@value #DEF_VIBRATIONSENSITIVITY}).
     *
     * @return the vibration sensitivity.
     */
    public int getVibrationSensitivity() {
        return getOptionAsInt(KEY_VIBRATIONSENSITIVITY);
    }

    /**
     * Function to check if WMI is enabled (default value {@value #DEF_WMIENABLED}).
     *
     * @return true if WMI is enabled, false otherwise.
     */
    public boolean getWMIEnabled() {
        return getOptionAsBool(KEY_WMIENABLED);
    }

    /**
     * Function to check if zero is a valid weight value (default value {@value #DEF_ZEROVALID}).
     *
     * @return true if zero is a valid weight value, false otherwise.
     */
    public boolean getZeroValid() {
        return getOptionAsBool(KEY_ZEROVALID);
    }

    private void loadConfig(byte[] config) {
        if (config == null) {
            Log.e(TAG, "loadConfig: config cannot be null.");
            return;
        }

        if (config.length < 6) {
            Log.e(TAG, "loadConfig: Invalid config supplied, must be at least 6 bytes.");
            return;
        }

        setOperationMode(config[3] & 0x03);
        setDisplayRequired((config[3] & 0x04) == 0x04);
        setIndicateZeroWithLed((config[3] & 0x08) == 0x08);
        setMetricWeightMode((config[3] & 0x10) == 0x10);
        setEnforceZeroReturn((config[3] & 0x20) == 0x20);
        setVibrationSensitivity((config[3] & 0xC0) >> 6);
        setFiveDigitWeight((config[4] & 0x01) == 0x01);
    }

    /**
     * Function to set if the scale may accept statistics commands.
     *
     * @param value true to set the scale to accept statistics commands, false otherwise.
     */
    public void setCanAcceptStatisticsCmd(boolean value) {
        setOption(KEY_CANACCEPTSTATISTICSCMD, value);
    }

    /**
     * Function to set if the scale supports th notify power changes functionality.
     *
     * @param value true to support the notify power changes functionality, false otherwise.
     */
    public void setCanNotifyPowerChange(boolean value) {
        setOption(KEY_CANNOTIFYPOWERCHANGE, value);
    }

    /**
     * Function to set if the scale supports the status update functionality.
     *
     * @param value true to support the status update functionality, false otherwise.
     */
    public void setCanStatusUpdate(boolean value) {
        setOption(KEY_CANSTATUSUPDATE, value);
    }

    /**
     * Function to set if the scale requires a display.
     *
     * @param value true if the scale requires a display, false otherwise.
     */
    public void setDisplayRequired(boolean value) {
        setOption(KEY_DISPLAYREQUIRED, value);
    }

    /**
     * Function to set if the enforce zero return functionality is supported.
     *
     * @param value true if the enforce zero return functionality is supported, false otherwise.
     */
    public void setEnforceZeroReturn(boolean value) {
        setOption(KEY_ENFORCEZERORETURN, value);
    }

    /**
     * Function to set if the weight is 5 digits.
     *
     * @param value true id the weight is 5 digits, false otherwise.
     */
    public void setFiveDigitWeight(boolean value) {
        setOption(KEY_FIVEDIGITWEIGHT, value);
    }

    /**
     * Function to set if the scale indicate zero with led.
     *
     * @param value true if the scale indicate zero with led, false otherwise.
     */
    public void setIndicateZeroWithLed(boolean value) {
        setOption(KEY_INDICATEZEROWITHLED, value);
    }

    /**
     * Function to set the live weight poll rate.
     *
     * @param value the desired live weight poll rate.
     */
    public void setLiveWeightPollRate(int value) {
        setOption(KEY_LIVEWEIGHTPOLLRATE, value);
    }

    /**
     * Function to set if the scale supports the MBeans functionality.
     *
     * @param value true if the scale supports the MBeans functionality, false otherwise.
     */
    public void setMBeansEnabled(boolean value) {
        setOption(KEY_MBEANSENABLED, value);
    }

    /**
     * Function to set if the scale is in metric weight mode.
     *
     * @param value true if the scale is in metric weight mode, false otherwise.
     */
    public void setMetricWeightMode(boolean value) {
        setOption(KEY_METRICWEIGHTMODE, value);
    }

    /**
     * Function to set the operation mode.
     *
     * @param value the desired operation mode.
     */
    public void setOperationMode(int value) {
        setOption(KEY_OPERATIONMODE, value);
    }

    /**
     * Function to set the vibration sensitivity.
     *
     * @param value the desired vibration sensitivity.
     */
    public void setVibrationSensitivity(int value) {
        setOption(KEY_VIBRATIONSENSITIVITY, value);
    }

    /**
     * Function to set if the scale supports WMI functionalities.
     *
     * @param value true if the scale supports WMI functionalities, false otherwise.
     */
    public void setWMIEnabled(boolean value) {
        setOption(KEY_WMIENABLED, value);
    }

    /**
     * Function to set if zero is a weight valid value.
     *
     * @param value true to set zero as a weight valid value, false otherwise.
     */
    public void setZeroValid(boolean value) {
        setOption(KEY_ZEROVALID, value);
    }
}
