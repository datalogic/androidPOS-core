package com.datalogic.dlapos.androidpos.common;

import org.junit.After;
import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

public class DLSPropertiesTest {

    private final DLSProperties _dlsProperties = new DLSProperties();

    @After
    public void cleanUp() {
        _dlsProperties.clear();
    }

    @Test
    public void getInt() {
        assertThat(_dlsProperties.get("TestInt", 2)).isEqualTo(2);
        _dlsProperties.set("TestInt", 1);
        assertThat(_dlsProperties.get("TestInt", 2)).isEqualTo(1);
        _dlsProperties.set("TestInt", 13);
        assertThat(_dlsProperties.get("TestInt", 2)).isEqualTo(13);
        assertThat(_dlsProperties.get("TestInt1", 2)).isEqualTo(2);
        _dlsProperties.set("TestInt1", 14);
        assertThat(_dlsProperties.get("TestInt1", 2)).isEqualTo(14);
    }

    @Test
    public void getString() {
        _dlsProperties.set("TestString", "");
        assertThat(_dlsProperties.get("TestString", "DEF")).isEqualTo("DEF");
        assertThat(_dlsProperties.get("TestString", "DEF")).isEqualTo("DEF");
        _dlsProperties.set("TestString", "VAL1");
        assertThat(_dlsProperties.get("TestString", "DEF")).isEqualTo("VAL1");
        _dlsProperties.set("TestString", "VAL13");
        assertThat(_dlsProperties.get("TestString", "DEF")).isEqualTo("VAL13");
        assertThat(_dlsProperties.get("TestString1", "DEF")).isEqualTo("DEF");
        _dlsProperties.set("TestString1", "VAL14");
        assertThat(_dlsProperties.get("TestString1", "DEF")).isEqualTo("VAL14");
    }

    @Test
    public void getBoolean() {
        assertThat(_dlsProperties.get("TestBoolean", false)).isEqualTo(false);
        _dlsProperties.set("TestBoolean", true);
        assertThat(_dlsProperties.get("TestBoolean", false)).isEqualTo(true);
        _dlsProperties.set("TestBoolean", false);
        assertThat(_dlsProperties.get("TestBoolean", true)).isEqualTo(false);
        assertThat(_dlsProperties.get("TestBoolean1", true)).isEqualTo(true);
        _dlsProperties.set("TestBoolean1", true);
        assertThat(_dlsProperties.get("TestBoolean1", false)).isEqualTo(true);
    }

    @Test
    public void getBluetoothWaitTime() {
        assertThat(_dlsProperties.getBluetoothWaitTime()).isEqualTo(DLSProperties.DEF_BTWAITTIME);
        _dlsProperties.setBluetoothWaitTime(25);
        assertThat(_dlsProperties.getBluetoothWaitTime()).isEqualTo(25);
    }

    @Test
    public void getCommandTimeout() {
        assertThat(_dlsProperties.getCommandTimeout()).isEqualTo(DLSProperties.DEF_TIMEOUT);
        _dlsProperties.setCommandTimeout(253);
        assertThat(_dlsProperties.getCommandTimeout()).isEqualTo(253);
    }

    @Test
    public void getEnableDisablePollRate() {
        assertThat(_dlsProperties.getEnableDisablePollRate()).isEqualTo(DLSProperties.DEF_ENABLEDISABLEPOLLRATE);
        _dlsProperties.setEnableDisablePollRate(4);
        assertThat(_dlsProperties.getEnableDisablePollRate()).isEqualTo(4);
    }

    @Test
    public void getImageDestination() {
        assertThat(_dlsProperties.getImageDestination()).isEqualTo(DLSProperties.DEF_IMAGEDEST);
        _dlsProperties.setImageDestination("trallalla");
        assertThat(_dlsProperties.getImageDestination()).isEqualTo("trallalla");
    }

    @Test
    public void getInfoFilename() {
        assertThat(_dlsProperties.getInfoFilename()).isEqualTo(DLSProperties.DEF_INFOFILENAME);
        _dlsProperties.setInfoFilename("tirulero");
        assertThat(_dlsProperties.getInfoFilename()).isEqualTo("tirulero");
    }

    @Test
    public void getObjectWaitTime() {
        assertThat(_dlsProperties.getObjectWaitTime()).isEqualTo(DLSProperties.DEF_OBJWAITTIME);
        _dlsProperties.setObjectWaitTime(14);
        assertThat(_dlsProperties.getObjectWaitTime()).isEqualTo(14);
    }

    @Test
    public void getPortPollRate() {
        assertThat(_dlsProperties.getPortPollRate()).isEqualTo(DLSProperties.DEF_PORTPOLLRATE);
        _dlsProperties.setPortPollRate(34);
        assertThat(_dlsProperties.getPortPollRate()).isEqualTo(34);
    }

    @Test
    public void getRecordRetry() {
        assertThat(_dlsProperties.getRecordRetry()).isEqualTo(DLSProperties.DEF_RECORDRETRY);
        _dlsProperties.setRecordRetry(67);
        assertThat(_dlsProperties.getRecordRetry()).isEqualTo(67);
    }

    @Test
    public void getResetTimeout() {
        assertThat(_dlsProperties.getResetTimeout()).isEqualTo(DLSProperties.DEF_RESETTIMEOUT);
        _dlsProperties.setResetTimeout(38);
        assertThat(_dlsProperties.getResetTimeout()).isEqualTo(38);
    }

    @Test
    public void getRetryWaitTime() {
        assertThat(_dlsProperties.getRetryWaitTime()).isEqualTo(DLSProperties.DEF_RETRYWAITTIME);
        _dlsProperties.setRetryWaitTime(844);
        assertThat(_dlsProperties.getRetryWaitTime()).isEqualTo(844);
    }

    @Test
    public void getRFIDMaxRetry() {
        assertThat(_dlsProperties.getRFIDMaxRetry()).isEqualTo(DLSProperties.DEF_RFIDMAXRETRY);
        _dlsProperties.setRFIDMaxRetry(58);
        assertThat(_dlsProperties.getRFIDMaxRetry()).isEqualTo(58);
    }

    @Test
    public void isAutoLoadConfig() {
        assertThat(_dlsProperties.isAutoLoadConfig()).isEqualTo(DLSProperties.DEF_AUTOLOADCONFIG);
        _dlsProperties.setAutoLoadConfig(!DLSProperties.DEF_AUTOLOADCONFIG);
        assertThat(_dlsProperties.isAutoLoadConfig()).isEqualTo(!DLSProperties.DEF_AUTOLOADCONFIG);
    }

    @Test
    public void isAvalancheEnabled() {
        assertThat(_dlsProperties.isAvalancheEnabled()).isEqualTo(DLSProperties.DEF_AVALANCHE);
        _dlsProperties.setAvalancheEnabled(!DLSProperties.DEF_AVALANCHE);
        assertThat(_dlsProperties.isAvalancheEnabled()).isEqualTo(!DLSProperties.DEF_AVALANCHE);
    }

    @Test
    public void isBCDtoASCII() {
        assertThat(_dlsProperties.isBCDtoASCII()).isEqualTo(DLSProperties.DEF_BCDTOASCII);
        _dlsProperties.setBCDtoASCII(!DLSProperties.DEF_BCDTOASCII);
        assertThat(_dlsProperties.isBCDtoASCII()).isEqualTo(!DLSProperties.DEF_BCDTOASCII);
    }

    @Test
    public void isDelayDisable() {
        assertThat(_dlsProperties.isDelayDisable()).isEqualTo(DLSProperties.DEF_DELAYDISABLE);
        _dlsProperties.setDelayDisable(!DLSProperties.DEF_DELAYDISABLE);
        assertThat(_dlsProperties.isDelayDisable()).isEqualTo(!DLSProperties.DEF_DELAYDISABLE);
    }

    @Test
    public void isEnableDisablePoll() {
        assertThat(_dlsProperties.isEnableDisablePoll()).isEqualTo(DLSProperties.DEF_ENABLEDISABLEPOLL);
        _dlsProperties.setEnableDisablePoll(!DLSProperties.DEF_ENABLEDISABLEPOLL);
        assertThat(_dlsProperties.isEnableDisablePoll()).isEqualTo(!DLSProperties.DEF_ENABLEDISABLEPOLL);
    }

    @Test
    public void isEnableECI() {
        assertThat(_dlsProperties.isEnableECI()).isEqualTo(DLSProperties.DEF_ENABLEECI);
        _dlsProperties.setEnableECI(!DLSProperties.DEF_ENABLEECI);
        assertThat(_dlsProperties.isEnableECI()).isEqualTo(!DLSProperties.DEF_ENABLEECI);
    }

    @Test
    public void isPostRemovalEvents() {
        assertThat(_dlsProperties.isPostRemovalEvents()).isEqualTo(DLSProperties.DEF_POSTREMEVENTS);
        _dlsProperties.setPostRemovalEvents(!DLSProperties.DEF_POSTREMEVENTS);
        assertThat(_dlsProperties.isPostRemovalEvents()).isEqualTo(!DLSProperties.DEF_POSTREMEVENTS);
    }

    @Test
    public void isScaleMotionException() {
        assertThat(_dlsProperties.isScaleMotionException()).isEqualTo(DLSProperties.DEF_SCALEMOTIONEXCEPTION);
        _dlsProperties.setScaleMotionException(!DLSProperties.DEF_SCALEMOTIONEXCEPTION);
        assertThat(_dlsProperties.isScaleMotionException()).isEqualTo(!DLSProperties.DEF_SCALEMOTIONEXCEPTION);
    }

    @Test
    public void isSendCookedData() {
        assertThat(_dlsProperties.isSendCookedData()).isEqualTo(DLSProperties.DEF_COOKEDDATA);
        _dlsProperties.setSendCookedData(!DLSProperties.DEF_COOKEDDATA);
        assertThat(_dlsProperties.isSendCookedData()).isEqualTo(!DLSProperties.DEF_COOKEDDATA);
    }

    @Test
    public void isSendFirmwareReset() {
        assertThat(_dlsProperties.isSendFirmwareReset()).isEqualTo(DLSProperties.DEF_SENDFWRESET);
        _dlsProperties.setSendFirmwareReset(!DLSProperties.DEF_SENDFWRESET);
        assertThat(_dlsProperties.isSendFirmwareReset()).isEqualTo(!DLSProperties.DEF_SENDFWRESET);
    }

    @Test
    public void isSendNulls() {
        assertThat(_dlsProperties.isSendNulls()).isEqualTo(DLSProperties.DEF_SENDNULLS);
        _dlsProperties.setSendNulls(!DLSProperties.DEF_SENDNULLS);
        assertThat(_dlsProperties.isSendNulls()).isEqualTo(!DLSProperties.DEF_SENDNULLS);
    }

    @Test
    public void isStatsOnClaim() {
        assertThat(_dlsProperties.isStatsOnClaim()).isEqualTo(DLSProperties.DEF_STATSONCLAIM);
        _dlsProperties.setStatsOnClaim(!DLSProperties.DEF_STATSONCLAIM);
        assertThat(_dlsProperties.isStatsOnClaim()).isEqualTo(!DLSProperties.DEF_STATSONCLAIM);
    }

    @Test
    public void isSuppressErrors() {
        assertThat(_dlsProperties.isSuppressErrors()).isEqualTo(DLSProperties.DEF_SUPPRESSERR);
        _dlsProperties.setSuppressErrors(!DLSProperties.DEF_SUPPRESSERR);
        assertThat(_dlsProperties.isSuppressErrors()).isEqualTo(!DLSProperties.DEF_SUPPRESSERR);
    }

    @Test
    public void isUseClaimLockFile() {
        assertThat(_dlsProperties.isUseClaimLockFile()).isEqualTo(DLSProperties.DEF_LOCKFILE);
        _dlsProperties.setUseClaimLockFile(!DLSProperties.DEF_LOCKFILE);
        assertThat(_dlsProperties.isUseClaimLockFile()).isEqualTo(!DLSProperties.DEF_LOCKFILE);
    }
}