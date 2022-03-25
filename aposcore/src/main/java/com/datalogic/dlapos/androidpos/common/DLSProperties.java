package com.datalogic.dlapos.androidpos.common;

import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.util.Properties;

/**
 * Class containing android POS properties.
 */
public class DLSProperties extends Properties {
    protected static final String TAG = "DLSProperties";
    /**
     * Key of the property for auto load configuration.
     */
    public static final String KEY_AUTOLOADCONFIG = "AutoLoadConfig";
    /**
     * Key of the property to check if Avalance is enabled.
     */
    public static final String KEY_AVALANCHE = "AvalancheEnabled";
    /**
     * Key of the property for BCD to ASCII conversion.
     */
    public static final String KEY_BCDTOASCII = "ConvertBCDtoASCII";
    /**
     * Key of the property containing the bluetooth wait time.
     */
    public static final String KEY_BTWAITTIME = "BtWaitTime";
    /**
     * Key of the property for enable disable poll.
     */
    public static final String KEY_ENABLEDISABLEPOLL = "EnableDisablePoll";
    /**
     * Key of the property for enable disable poll rate.
     */
    public static final String KEY_ENABLEDISABLEPOLLRATE = "EnableDisablePollRate";
    /**
     * Key of the property to check if ECI is enabled.
     */
    public static final String KEY_ENABLEECI = "EnableECI";
    /**
     * Key of the property containing the HDL reset time.
     */
    public static final String KEY_HDLRESETTIME = "HDLResetWaitTime";
    /**
     * Key of the property containing images destination.
     */
    public static final String KEY_IMAGEDEST = "ImageDest";
    /**
     * Key of the property containing the scanner info file name.
     */
    public static final String KEY_INFOFILENAME = "ScannerInfoFilename";
    /**
     * Key of the property to use claim lock file.
     */
    public static final String KEY_LOCKFILE = "UseClaimLockFile";
    /**
     * Key of the property containing the object wait time.
     */
    public static final String KEY_OBJWAITTIME = "ObjWaitTime";
    /**
     * Key of the property containing the poll rate for port change.
     */
    public static final String KEY_PORTPOLLRATE = "PollRateForPortChange";
    /**
     * Key of the property to check if post removal error events are enabled.
     */
    public static final String KEY_POSTREMEVENTS = "PostRemovalErrorEvents";
    /**
     * Key of the property containing HDL record retry number.
     */
    public static final String KEY_RECORDRETRY = "HDLRecordRetry";
    /**
     * Key of the property containing the reset timeout.
     */
    public static final String KEY_RESETTIMEOUT = "ResetTimeout";
    /**
     * Key of the property containing the retry wait time.
     */
    public static final String KEY_RETRYWAITTIME = "RetryWaitTime";
    /**
     * Key of the property containing the RFID maximum retry number .
     */
    public static final String KEY_RFIDMAXRETRY = "RFIDMaxRetry";
    /**
     * Key of the property to check if the library throws exception on scale motion.
     */
    public static final String KEY_SCALEMOTIONEXCEPTION = "ThrowExceptionOnScaleMotion";
    /**
     * Key of the property to check if the library sends cooked data.
     */
    public static final String KEY_COOKEDDATA = "SendCookedData";
    /**
     * Key of the property to check if the firmware send reset.
     */
    public static final String KEY_SENDFWRESET = "FirmwareSendReset";
    /**
     * Key of the property to check if the firmware send nulls.
     */
    public static final String KEY_SENDNULLS = "FirmwareSendNulls";
    /**
     * Key of the property to check if the library generates statistics on every claim.
     */
    public static final String KEY_STATSONCLAIM = "GenerateStatsOnEveryClaim";
    /**
     * Key of the property to check if the library suppresses errors.
     */
    public static final String KEY_SUPPRESSERR = "SuppressErrors";
    /**
     * Key of the property containing the default timeout.
     */
    public static final String KEY_TIMEOUT = "Timeout";
    /**
     * Key of the property to check if delay disable on partial label is enabled.
     */
    protected static final String KEY_DELAYDISABLE = "DelayDisableOnPartialLabel";

    static final boolean DEF_AUTOLOADCONFIG = false;
    static final boolean DEF_AVALANCHE = false;
    static final boolean DEF_BCDTOASCII = true;
    static final int DEF_BTWAITTIME = 1000;
    static final int DEF_TIMEOUT = 5000; // CommandTimeout
    static final boolean DEF_DELAYDISABLE = false;
    static final boolean DEF_ENABLEDISABLEPOLL = false;
    static final int DEF_ENABLEDISABLEPOLLRATE = 1;
    static final boolean DEF_ENABLEECI = false;
    static final String DEF_IMAGEDEST = "";
    static final String DEF_INFOFILENAME = "info";
    static final int DEF_OBJWAITTIME = 100;
    static final int DEF_PORTPOLLRATE = 1000;
    static final boolean DEF_POSTREMEVENTS = false;
    static final int DEF_RECORDRETRY = 5;
    static final int DEF_RESETTIMEOUT = 60000;
    static final int DEF_RETRYWAITTIME = 1000;
    static final int DEF_RFIDMAXRETRY = 10;
    static final boolean DEF_SCALEMOTIONEXCEPTION = false;
    static final boolean DEF_COOKEDDATA = false;
    static final boolean DEF_SENDFWRESET = true;
    static final boolean DEF_SENDNULLS = true;
    static final boolean DEF_STATSONCLAIM = true;
    static final boolean DEF_SUPPRESSERR = false;
    static final boolean DEF_LOCKFILE = false;


    static DLSProperties sm_instance = null;

    DLSProperties() {
        super();
    }

    /**
     * Function to get a property.
     *
     * @param key of the property to get.
     * @param def default value returned if nothing matches the key.
     * @return the value associated to the key, or the default value.
     */
    public boolean get(String key, boolean def) {
        String sval = get(key, Boolean.toString(def));
        return (sval.equals("1") || sval.equals("-1") || sval.equalsIgnoreCase("true"));
    }

    /**
     * Function to get a property.
     *
     * @param key of the property to get.
     * @param def default value returned if nothing matches the key.
     * @return the value associated to the key, or the default value.
     */
    public int get(String key, int def) {
        String sval = get(key, Integer.toString(def));
        return Integer.parseInt(sval);
    }

    /**
     * Function to get a property.
     *
     * @param key of the property to get.
     * @param def default value returned if nothing matches the key.
     * @return the value associated to the key, or the default value.
     */
    public String get(String key, String def) {
        String value = getProperty(key, def);
        if (value.isEmpty()) {
            value = def;
        }
        return value;
    }

    /**
     * Function to get the bluetooth wait time (default is 1000).
     *
     * @return the bluetooth wait time.
     */
    public int getBluetoothWaitTime() {
        return get(KEY_BTWAITTIME, DEF_BTWAITTIME);
    }

    /**
     * Function to get the command timeout (default is 5000).
     *
     * @return the command timeout.
     */
    public int getCommandTimeout() {
        return get(KEY_TIMEOUT, DEF_TIMEOUT);
    }

    /**
     * Function to get the enable disable poll rate (default is 1).
     *
     * @return the enable disable poll rate
     */
    public int getEnableDisablePollRate() {
        return get(KEY_ENABLEDISABLEPOLLRATE, DEF_ENABLEDISABLEPOLLRATE);
    }

    /**
     * Instance getter.
     *
     * @param context the application context.
     * @return an instance of DLProperties.
     */
    public synchronized static DLSProperties getInstance(Context context) {
        if (sm_instance == null) {
            sm_instance = new DLSProperties();
            load(context);
        }
        return sm_instance;
    }

    private static void load(Context context) {
        try {
            DLSProperties.getInstance(context).load(context.getAssets().open("dls.properties"));
        } catch (IOException e) {
            Log.e(TAG, "Error loading properties.", e);
        }
    }

    /**
     * Function to get images destination (default value is "{@value #DEF_IMAGEDEST}").
     *
     * @return images destination path.
     */
    public String getImageDestination() {
        return get(KEY_IMAGEDEST, DEF_IMAGEDEST);
    }

    /**
     * Function to get the info file name (default value is "{@value #DEF_INFOFILENAME}").
     *
     * @return the info file name.
     */
    public String getInfoFilename() {
        return get(KEY_INFOFILENAME, DEF_INFOFILENAME);
    }

    /**
     * Function to get the object wait time (default value is {@value #DEF_OBJWAITTIME}).
     *
     * @return the object wait time.
     */
    public int getObjectWaitTime() {
        return get(KEY_OBJWAITTIME, DEF_OBJWAITTIME);
    }

    /**
     * Function to get the port poll rate (default value is {@value #DEF_PORTPOLLRATE}).
     *
     * @return the port poll rate.
     */
    public int getPortPollRate() {
        return get(KEY_PORTPOLLRATE, DEF_PORTPOLLRATE);
    }

    /**
     * Function to get the record retry (default value is {@value #DEF_RECORDRETRY}).
     *
     * @return the record retry.
     */
    public int getRecordRetry() {
        return get(KEY_RECORDRETRY, DEF_RECORDRETRY);
    }

    /**
     * Function to get the reset timeout (default value is {@value #DEF_RESETTIMEOUT}).
     *
     * @return the reset timeout.
     */
    public int getResetTimeout() {
        return get(KEY_RESETTIMEOUT, DEF_RESETTIMEOUT);
    }

    /**
     * Function to get the retry wait time (default value is {@value #DEF_RETRYWAITTIME}).
     *
     * @return the retry wait time.
     */
    public int getRetryWaitTime() {
        return get(KEY_RETRYWAITTIME, DEF_RETRYWAITTIME);
    }

    /**
     * Function to get the RFID max retry (default value is {@value #DEF_RFIDMAXRETRY}).
     *
     * @return the RFID max retry.
     */
    public int getRFIDMaxRetry() {
        return get(KEY_RFIDMAXRETRY, DEF_RFIDMAXRETRY);
    }

    /**
     * Function to check if auto load configuration is enabled (default value is {@value #DEF_AUTOLOADCONFIG}).
     *
     * @return true if auto configuration is enabled, false otherwise.
     */
    public boolean isAutoLoadConfig() {
        return get(KEY_AUTOLOADCONFIG, DEF_AUTOLOADCONFIG);
    }

    /**
     * Function to check if Avalance is enabled (default value is {@value #DEF_AVALANCHE}).
     *
     * @return true if Avalance is enabled, false otherwise.
     */
    public boolean isAvalancheEnabled() {
        return get(KEY_AVALANCHE, DEF_AVALANCHE);
    }

    /**
     * Function to check if BCD to ASCII conversion is enabled (default value is {@value #DEF_BCDTOASCII}).
     *
     * @return true if BCD to ASCII conversion is enabled, false otherwise.
     */
    public boolean isBCDtoASCII() {
        return get(KEY_BCDTOASCII, DEF_BCDTOASCII);
    }

    /**
     * Function to check if delay is disabled (default value is {@value #DEF_DELAYDISABLE}).
     *
     * @return true if delay is disabled, false otherwise.
     */
    public boolean isDelayDisable() {
        return get(KEY_DELAYDISABLE, DEF_DELAYDISABLE);
    }

    /**
     * Function to check if the enable-disable poll is used (default value is {@value #DEF_ENABLEDISABLEPOLL}).
     *
     * @return true if it the enable-disable poll is used, false otherwise.
     */
    public boolean isEnableDisablePoll() {
        return get(KEY_ENABLEDISABLEPOLL, DEF_ENABLEDISABLEPOLL);
    }

    /**
     * Function to check if ECI is enabled (default value is {@value #DEF_ENABLEECI}).
     *
     * @return true if ECI is enabled, false otherwise.
     */
    public boolean isEnableECI() {
        return get(KEY_ENABLEECI, DEF_ENABLEECI);
    }

    /**
     * Function to check if post removal events are enabled (default value is {@value #DEF_POSTREMEVENTS}).
     *
     * @return true if post removal events are enabled, false otherwise.
     */
    public boolean isPostRemovalEvents() {
        return get(KEY_POSTREMEVENTS, DEF_POSTREMEVENTS);
    }

    /**
     * Function to check if the library throws exceptions if the scale is in motion (default value is {@value #DEF_SCALEMOTIONEXCEPTION}).
     *
     * @return true if the library throws exceptions if the scale is in motion, false otherwise.
     */
    public boolean isScaleMotionException() {
        return get(KEY_SCALEMOTIONEXCEPTION, DEF_SCALEMOTIONEXCEPTION);
    }

    /**
     * Function to check if send cooked data is enabled (default value is {@value #DEF_COOKEDDATA}).
     *
     * @return true if send cooked data is enabled, false otherwise.
     */
    public boolean isSendCookedData() {
        return get(KEY_COOKEDDATA, DEF_COOKEDDATA);
    }

    /**
     * Function to check if send firmware reset is enabled (default value is {@value #DEF_SENDFWRESET}).
     *
     * @return true if send firmware reset is enabled, false otherwise.
     */
    public boolean isSendFirmwareReset() {
        return get(KEY_SENDFWRESET, DEF_SENDFWRESET);
    }

    /**
     * Function to check if send nulls is enabled (default value is {@value #DEF_SENDNULLS}).
     *
     * @return true if send nulls is enabled, false otherwise.
     */
    public boolean isSendNulls() {
        return get(KEY_SENDNULLS, DEF_SENDNULLS);
    }

    /**
     * Function to check if the library generate statistics on claim (default value is {@value #DEF_STATSONCLAIM}).
     *
     * @return true if the library generate statistics on claim, false otherwise.
     */
    public boolean isStatsOnClaim() {
        return get(KEY_STATSONCLAIM, DEF_STATSONCLAIM);
    }

    /**
     * Function to check if the library suppress errors (default value is {@value #DEF_SUPPRESSERR}).
     *
     * @return true if the library suppress errors, false otherwise.
     */
    public boolean isSuppressErrors() {
        return get(KEY_SUPPRESSERR, DEF_SUPPRESSERR);
    }

    /**
     * Function to check if the library uses claim lock file (default value is {@value #DEF_LOCKFILE}).
     *
     * @return true if the library uses claim lock file, false otherwise.
     */
    public boolean isUseClaimLockFile() {
        return get(KEY_LOCKFILE, DEF_LOCKFILE);
    }

    /**
     * Function to set a property.
     *
     * @param key   the key of the property to set.
     * @param value the desired value of the property.
     */
    public void set(String key, String value) {
        setProperty(key, value);
    }

    /**
     * Function to set a property.
     *
     * @param key   the key of the property to set.
     * @param value the desired value of the property.
     */
    public void set(String key, boolean value) {
        setProperty(key, (value) ? "true" : "false");
    }

    /**
     * Function to set a property.
     *
     * @param key   the key of the property to set.
     * @param value the desired value of the property.
     */
    public void set(String key, int value) {
        setProperty(key, Integer.toString(value));
    }

    /**
     * Function to set the auto load configuration property.
     *
     * @param value the desired value.
     */
    public void setAutoLoadConfig(boolean value) {
        set(KEY_AUTOLOADCONFIG, value);
    }

    /**
     * Function to set enabled or disabled the Avalanche property.
     *
     * @param value true to enable, false to disable.
     */
    public void setAvalancheEnabled(boolean value) {
        set(KEY_AVALANCHE, value);
    }

    /**
     * Function to enable or disable the BCD to ASCII conversion.
     *
     * @param value true to enable, false to disable.
     */
    public void setBCDtoASCII(boolean value) {
        set(KEY_BCDTOASCII, value);
    }

    /**
     * Function to set the bluetooth wait time.
     *
     * @param value the desired time.
     */
    public void setBluetoothWaitTime(int value) {
        set(KEY_BTWAITTIME, value);
    }

    /**
     * Function to set the command timeout.
     *
     * @param value the desired timeout.
     */
    public void setCommandTimeout(int value) {
        set(KEY_TIMEOUT, value);
    }

    /**
     * Function to disable the delay.
     *
     * @param value true to disable, false to enable.
     */
    public void setDelayDisable(boolean value) {
        set(KEY_DELAYDISABLE, value);
    }

    /**
     * Function to use the enable-disable poll.
     *
     * @param value true to use the enable-disable poll, false otherwise.
     */
    public void setEnableDisablePoll(boolean value) {
        set(KEY_ENABLEDISABLEPOLL, value);
    }

    /**
     * Function to set the enable-disable poll rate.
     *
     * @param value the desired rate.
     */
    public void setEnableDisablePollRate(int value) {
        set(KEY_ENABLEDISABLEPOLLRATE, value);
    }

    /**
     * Function to enable or disable ECI.
     *
     * @param value true to enable, false otherwise.
     */
    public void setEnableECI(boolean value) {
        set(KEY_ENABLEECI, value);
    }

    /**
     * Function to set the image destination.
     *
     * @param value the desired destination.
     */
    public void setImageDestination(String value) {
        set(KEY_IMAGEDEST, value);
    }

    /**
     * Function to set the info file name.
     *
     * @param value the desired file name.
     */
    public void setInfoFilename(String value) {
        set(KEY_INFOFILENAME, value);
    }

    /**
     * Function to set the object wait time.
     *
     * @param value the desired object wait time.
     */
    public void setObjectWaitTime(int value) {
        set(KEY_OBJWAITTIME, value);
    }

    /**
     * Function to set the port poll rate.
     *
     * @param value the desired rate.
     */
    public void setPortPollRate(int value) {
        set(KEY_PORTPOLLRATE, value);
    }

    /**
     * Function to enable or disable post removal events.
     *
     * @param value true to enable, false to disable.
     */
    public void setPostRemovalEvents(boolean value) {
        set(KEY_POSTREMEVENTS, value);
    }

    /**
     * Function to set the number of record retries.
     *
     * @param value the desired number of retries.
     */
    public void setRecordRetry(int value) {
        set(KEY_RECORDRETRY, value);
    }

    /**
     * Function to set reset timeout.
     *
     * @param value the desired timeout.
     */
    public void setResetTimeout(int value) {
        set(KEY_RESETTIMEOUT, value);
    }

    /**
     * Function to set the retry wait time.
     *
     * @param value the desired retry wait time.
     */
    public void setRetryWaitTime(int value) {
        set(KEY_RETRYWAITTIME, value);
    }

    /**
     * Function to set the RFID maximum retry number.
     *
     * @param value the desired RFID maximum retry number.
     */
    public void setRFIDMaxRetry(int value) {
        set(KEY_RFIDMAXRETRY, value);
    }

    /**
     * Function to set if the application has to throw exception while scale is in motion.
     *
     * @param value true to throw exception, false otherwise.
     */
    public void setScaleMotionException(boolean value) {
        set(KEY_SCALEMOTIONEXCEPTION, value);
    }

    /**
     * Function to enable cooked data sending.
     *
     * @param value true to send cooked data, false otherwise.
     */
    public void setSendCookedData(boolean value) {
        set(KEY_COOKEDDATA, value);
    }

    /**
     * Function to enable firmware resets sending.
     *
     * @param value true to send firmware resets, false otherwise.
     */
    public void setSendFirmwareReset(boolean value) {
        set(KEY_SENDFWRESET, value);
    }

    /**
     * Function to enable nulls sending.
     *
     * @param value true to send nulls, false otherwise.
     */
    public void setSendNulls(boolean value) {
        set(KEY_SENDNULLS, value);
    }

    /**
     * Function to enable statistics collection on claim.
     *
     * @param value true to enable statistics collection on claim, false otherwise.
     */
    public void setStatsOnClaim(boolean value) {
        set(KEY_STATSONCLAIM, value);
    }

    /**
     * Function to set error suppression.
     *
     * @param value true to suppress errors, false otherwise.
     */
    public void setSuppressErrors(boolean value) {
        set(KEY_SUPPRESSERR, value);
    }

    /**
     * Function to use a claim lock file.
     *
     * @param value true to use a claim lock file, false otherwise.
     */
    public void setUseClaimLockFile(boolean value) {
        set(KEY_LOCKFILE, value);
    }
}
