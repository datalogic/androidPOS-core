package com.datalogic.dlapos.androidpos.interpretation;

import android.content.Context;
import android.util.Log;

import com.datalogic.dlapos.androidpos.common.Branding;
import com.datalogic.dlapos.androidpos.common.DLSDeviceInfo;
import com.datalogic.dlapos.androidpos.common.DLSException;
import com.datalogic.dlapos.androidpos.common.DLSJposConst;
import com.datalogic.dlapos.androidpos.common.DLSProperties;
import com.datalogic.dlapos.androidpos.common.DLSScannerConfig;
import com.datalogic.dlapos.androidpos.common.DLSState;
import com.datalogic.dlapos.androidpos.common.DLSStatistics;
import com.datalogic.dlapos.androidpos.common.FunctionLib;
import com.datalogic.dlapos.androidpos.service.DLSScannerService;
import com.datalogic.dlapos.commons.constant.CommonsConstants;
import com.datalogic.dlapos.commons.constant.ErrorConstants;
import com.datalogic.dlapos.commons.constant.ScannerConstants;
import com.datalogic.dlapos.confighelper.DLAPosConfigHelper;
import com.datalogic.dlapos.confighelper.configurations.accessor.LabelHelper;
import com.datalogic.dlapos.confighelper.configurations.accessor.LabelIds;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class representing a generic Datalogic scanner connected using an USB port.
 */
public class DLSUSBScanner extends DLSScanner {

    DLSProperties options;

    private final String TAG = "DLSUSBScanner";
    private final Object resetObj = new Object();
    private boolean resetSuccess = false;
    volatile boolean hotPlugged = true;

    private final ScheduledExecutorService m_scheduler = Executors.newSingleThreadScheduledExecutor();
    ScheduledFuture<?> m_hotPlugWatcher = null;

    int nObjWaitTime;
    private static final byte[] EMPTY_BYTE_ARRAY = {};
    private final Object dioResp = new Object();
    private final Object dioReset = new Object();
    private final Object reportResp = new Object();
    byte[] dioBuf = null;
    boolean internalDIO = false;
    boolean internalDIOSuccess = false;
    //private boolean statMessageSent = false;
    boolean gotStatus = false;
    private final Object objStatus = new Object();
    private final Object statusResp = new Object();
    byte stat0;
    byte stat1;
    byte stat2;
    byte[] statusBuf = null;
    byte[] reportBuf = null;
    boolean localAlive = false;
    private final boolean raiseBusy = true;//TODO: is always true. Is it necessary?

    // fast CFR
    static final byte[] cellPhoneModeECOMMEnter = {0x30, 0x00, 0x70, 0x01, 0, 0, 0, 0, 0, 0, 0};
    static final byte[] cellPhoneModeECOMMExit = {0x30, 0x00, 0x70, 0x00, 0, 0, 0, 0, 0, 0, 0};
    // cell phone mode
    static final byte[] cellPhoneModeScannerEnter = {0x30, 0x00, 0x71, 0x01, 0, 0, 0, 0, 0, 0, 0};
    static final byte[] cellPhoneModeScannerExit = {0x30, 0x00, 0x71, 0x00, 0, 0, 0, 0, 0, 0, 0};
    // reading scanner event log
    static final byte[] getNumberStats = {0x30, 0x00, 0x74, 0x00, 0, 0, 0, 0, 0, 0, 0};
    static final byte[] getStat = {0x30, 0x00, 0x74, 0x01, 0, 0, 0, 0, 0, 0, 0};
    static final byte[] getStatN = {0x30, 0x00, 0x74, 0x02, 0, 0, 0, 0, 0, 0, 0};
    static final byte[] getLastEvent = {0x30, 0x00, 0x74, 0x03, 0, 0, 0, 0, 0, 0, 0};
    static final byte[] clearEventLog = {0x30, 0x00, 0x74, 0x04, 0, 0, 0, 0, 0, 0, 0};
    //    private static final byte[] statusRequestCmd = {0, 0x20, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    // read/write/commit config items
    static final byte[] writeConfigInt = {0x30, 0x00, 0x74, 0x05, 0, 0, 0, 0, 0, 0, 0};
    static final byte[] readConfigInt = {0x30, 0x00, 0x74, 0x06, 0, 0, 0, 0, 0, 0, 0};
    private static final byte[] writeConfigNonInt = {0x30, 0x00, 0x74, 0x07, 0, 0, 0, 0, 0, 0, 0};
    static final byte[] readConfigNonInt = {0x30, 0x00, 0x74, 0x08, 0, 0, 0, 0, 0, 0, 0};
    static final byte[] queryConfig = {0x30, 0x00, 0x74, 0x09, 0, 0, 0, 0, 0, 0, 0};
    //    private static final byte[] commitConfig = {0x30, 0x00, 0x74, 0x05, 0x7F, (byte) 0xFD, 0x01, 0x00, 0, 0, 0}; // action tag causes commit
    private static final int ACTION_TAG_DELAY = 4000;
    // Scale Sentry
    static final byte[] getScaleSentryStatus = {0x30, 0x00, 0x72, 0x00, 0, 0, 0, 0, 0, 0, 0};
    static final byte[] clearScaleSentryStatus = {0x30, 0x00, 0x73, 0x00, 0, 0, 0, 0, 0, 0, 0};
    // Beagle bone black video streaming control
    static final byte[] extVideoStreamOn = {0x30, 0x00, 0x78, 0x01, 0, 0, 0, 0, 0, 0, 0};
    static final byte[] extVideoStreamOff = {0x30, 0x00, 0x78, 0x00, 0, 0, 0, 0, 0, 0, 0};
    static final byte[] enableCmd = {0x11, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    static final byte[] disableCmd = {0x12, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    static final byte[] resetCmd = {0, 0x40, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    static final byte[] statusCmd = {0, 0x20, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    static final byte[] reportConfigCmd = {0x21, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    final byte[] configCmd = {0x20, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    static final byte[] reportConfig2LabelCmd = {0x34, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    final byte[] config2LabelCmd = {0x23, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    static final byte[] enableBeepCmd = {0x14, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    static final byte[] disableBeepCmd = {0x18, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    static final byte[] doSelfTestCmd = {0, 0x10, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    final byte[] directIOCmd = {0x30, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    static final byte INFO_CMD = 0x01;
    static final byte HEALTH_CMD = 0x02;
    static final byte STATISTICS_CMD = 0x03;
    private static final byte BEEP_CMD = (byte) 0x04;
    private static final byte NOF_BEEP_CMD = (byte) 0x06;
    private static final byte ERROR_BEEP_CMD = (byte) 0x05;

    static final byte[] CMD_DEVICE_INFO = {0x30, (byte) 0xFF, (byte) 0xFE, 0, 0, 0, 0, 0, 0, 0, 0};
    static final byte[] CMD_SCAN_GEN_MGT_INFO = {0x30, (byte) 0xFF, (byte) 0xFD, 0, 0, 0, 0, 0, 0, 0, 0};
    static final byte[] CMD_SCAN_VEN_MGT_INFO = {0x30, (byte) 0xFF, (byte) 0xFC, 0, 0, 0, 0, 0, 0, 0, 0};
    static final byte[] CMD_SCALE_GEN_MGT_INFO = {0x30, (byte) 0xFF, (byte) 0xFB, 0, 0, 0, 0, 0, 0, 0, 0};
    static final byte[] CMD_SCALE_VEN_MGT_INFO = {0x30, (byte) 0xFF, (byte) 0xFA, 0, 0, 0, 0, 0, 0, 0, 0};
    static final byte[] CMD_READ_DWM_LABELFILTER = {0x30, (byte) 0xFF, (byte) 0xEF, 0, 0, 0, 0, 0, 0, 0, 0};
    static final byte[] CMD_CLEAR_DWM_LABELFILTER = {0x30, (byte) 0xFF, (byte) 0xEE, 0, 0, 0, 0, 0, 0, 0, 0};

    // status byte zero
    static final int RESP_FIRMWARE_FLASH = 0x00000001;
    private static final int RESP_CONFIG_DATA = 0x00000002;
    private static final int RESP_EANJANCONFIG_DATA = 0x00000004;
    private static final int RESP_HARDWARE_ERROR = 0x00000020;
    private static final int RESP_DIO_DATA = 0x00000040;
    private static final int RESP_NOT_READY = 0x00000080;
    // status byte one
    static final int RESP_ALIVE = 0x00000100;
    private static final int RESP_ENABLED = 0x00000200;
    private static final int RESP_CHECKDIGIT_ERROR = 0x00000400;
    private static final int RESP_CMD_REJECT = 0x00008000;
    // status byte two
    private static final int RESP_CONFIG_SUCCESS = 0x00010000;
    private static final int RESP_CONFIG_COERCED = 0x00030000;
    private static final int RESP_EANJANCONFIG_SUCCESS = 0x00040000;
    private static final int RESP_DIO_SUCCESS = 0x00080000;
    private static final int RESP_DIO_NOT_ALLOWED = 0x00100000;
    private static final int RESP_DIO_UNDEFINED = 0x00200000;

    /**
     * Changes the port baud rate.
     * <p>
     * Used to alter the port during a firmware update.  Does not alter the scanner settings.
     *
     * @param baud int indicating the new baud rate to set.
     */
    @Override
    public void changeBaudRate(int baud) {
        //Not supported.
    }

//    /**
//     * {@inheritDoc}
//     */
//    @Override
//    public void claim(long timeout) throws DLSException {
//        if (this.port != null && this.port.isOpen()) {
//            this.port.closePort();
//            release();
//        }
//        setPort(DLSObjectFactory.createPort(getDeviceInfo(), context));
//        ((DLSUsbPort) port).registerPortStatusListener(this);
//
//        if (!this.port.openPort()) {
//            throw new DLSException(DLSJposConst.DLS_E_OPENPORT, "Port open error.");
//        }
//
//    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean detectResetFinish() throws DLSException {
        long timeout = options.getResetTimeout();
        long elapsed = 0;
        long start;
        boolean result;
        hotPlugged = false;

        ScheduledFuture<?> m_resetWatcher = m_scheduler.scheduleAtFixedRate(new ResetWatcherThread(), 1000, 1000, TimeUnit.MILLISECONDS);

        synchronized (resetObj) {
            resetSuccess = false;
            start = System.nanoTime();
            try {
                while (!resetSuccess) {
                    resetObj.wait(timeout - elapsed);
                    elapsed = ((System.nanoTime() - start) / 1000000);
                    if (elapsed >= timeout) {
                        break;
                    }
                }
            } catch (InterruptedException ie) {
                throw new DLSException(ErrorConstants.APOS_ERROR, "detectResetFinish: Interrupted exception: " + ie.getMessage());
            }
            result = resetSuccess;
        }

        if (!result) {
            throw new DLSException(ErrorConstants.APOS_E_NOHARDWARE, "detectResetFinish: Unable to communicate with scanner after reset");
        }

        m_resetWatcher.cancel(false);

        return result;
    }

    /**
     * {@inheritDoc}
     * <p>
     * <b> {@code data} parameter may not be {@code null} or empty. It is always
     * used to return the command status at {@code data[0]}. </b>
     * <p>
     * Supported commands: <ul>
     * <li> {@code DIO_SCANNER_RESET}
     * <li> {@code DIO_SCANNER_ENABLE_BEEP}
     * <li> {@code DIO_SCANNER_DISABLE_BEEP}
     * <li> {@code DIO_SCANNER_DISABLE}
     * <li> {@code DIO_SCANNER_ENABLE}
     * <li> {@code DIO_SCANNER_CONFIGURE}
     * <li> {@code DIO_SCANNER_REPORT_CONFIG}
     * <li> {@code DIO_SCANNER_CONFIG_2LABEL}
     * <li> {@code DIO_SCANNER_REPORT_2LABEL}
     * <li> {@code DIO_SCANNER_DIO_INFORMATION}
     * <li> {@code DIO_SCANNER_DIO_HEALTH}
     * <li> {@code DIO_SCANNER_DIO_STATS}
     * <li> {@code DIO_READ_STAT_LOG_ITEMS}
     * <li> {@code DIO_READ_EVENT_LOG_ITEMS}
     * <li> {@code DIO_READ_EVENT_LOG_LAST_EVENT}
     * <li> {@code DIO_CLEAR_EVENT_LOG}
     * <li> {@code DIO_SAVE_EVENT_LOG}
     * <li> {@code DIO_SAVE_STATS_LOG}
     * <li> {@code DIO_GET_SCALE_SENTRY_STATUS}
     * <li> {@code DIO_CLEAR_SCALE_SENTRY}
     * <li> {@code DIO_READ_CONFIG_ITEM}
     * <li> {@code DIO_WRITE_CONFIG_ITEM}
     * <li> {@code DIO_CELL_PHONE_ECOMM_ENTER}
     * <li> {@code DIO_CELL_PHONE_ECOMM_EXIT}
     * <li> {@code DIO_CELL_PHONE_SCAN_ENTER}
     * <li> {@code DIO_CELL_PHONE_SCAN_EXIT}
     * <li> {@code DIO_EXT_VIDEO_STREAM_ON}
     * <li> {@code DIO_EXT_VIDEO_STREAM_OFF}
     * <li> {@code DIO_SCANNER_DIO_BEEP}
     * <li> {@code DIO_SCANNER_DIO_NOF}
     * <li> {@code DIO_SCANNER_DIO_ERROR_BEEP}
     * <li> {@code DIO_DEV_PROTOCOL}
     * </ul>
     *
     * @see DLSJposConst
     */
    @Override
    public void directIO(int command, int[] data, Object object) throws DLSException {
        if (DLSJposConst.updateInProgress) {
            return;
        }

        if (data == null || data.length == 0) {
            data = new int[1];
        }
        // Commands as per the SRS 2.2 doc
        byte[] buf = EMPTY_BYTE_ARRAY;
        String tempString;
        String resultString;
        String fileName;
        boolean bCopyObjectToBuf = false;
        BufferedWriter output = null;

        byte[] abCmd;
        byte[] abResp;
        boolean bSuccess;

        try {

            switch (command) {
                /*
                 * Enable / Disable Additional Symbologies
                 *  USB-OEM 3.1
                 */
                case DLSJposConst.DIO_ENAB_DISAB_ADDL_SYM:
                    if (data.length >= 3) {
                        abCmd = new byte[11];
                        abCmd[0] = (byte) 0x30;
                        abCmd[1] = (byte) 0xFF;
                        abCmd[2] = (byte) 0xFF;
                        abCmd[3] = (byte) data[0];
                        abCmd[4] = (byte) data[1];
                        abCmd[5] = (byte) data[2];
                        abCmd[6] = 0;
                        abCmd[7] = 0;
                        abCmd[8] = 0;
                        abCmd[9] = 0;
                        abCmd[10] = 0;
                        buf = doInternalMultiByteDirectIO(abCmd);
                        bSuccess = false;
                        if (buf.length != 0) {
                            if ((buf[3] & 8) == 8) {
                                bSuccess = true;
                                data[0] = 0;
                            }
                        }
                        if (bSuccess) {
                            buf = "Command succeeded.".getBytes();
                        } else {
                            buf = "ERROR: Command failed.".getBytes();
                        }
                    } else {
                        throw new DLSException(DLSJposConst.DLS_E_INVALID_ARG, "Must provide at least 3 bytes of data.");
                    }
                    break;
                /*
                 * Device Information
                 *  USB-OEM 3.1
                 */
                case DLSJposConst.DIO_DEVICE_INFORMATION:
                    abResp = doDirectIOExtended(CMD_DEVICE_INFO);
                    if (abResp.length != 0) {
                        buf = abResp;
                        data[0] = 0;
                    } else {
                        buf = "ERROR: No command response received.".getBytes();
                        data[0] = 1;
                    }
                    break;
                /*
                 * Request Scanner Generic Management Information
                 *  USB-OEM 3.1
                 */
                case DLSJposConst.DIO_REQ_SCANNER_GEN_MGT:
                    abResp = doDirectIOExtended(CMD_SCAN_GEN_MGT_INFO);
                    if (abResp.length != 0) {
                        buf = abResp;
                        data[0] = 0;
                    } else {
                        buf = "ERROR: No command response received.".getBytes();
                        data[0] = 1;
                    }
                    break;
                /*
                 * Request Scanner Vendor-specific Management Information
                 * USB-OEM 3.1
                 */
                case DLSJposConst.DIO_REQ_SCANNER_VEN_MGT:
                    abResp = doDirectIOExtended(CMD_SCAN_VEN_MGT_INFO, 1000);
                    if (abResp.length != 0) {
                        buf = abResp;
                        data[0] = 0;
                    } else {
                        buf = "ERROR: No command response received.".getBytes();
                        data[0] = 1;
                    }
                    break;
                /*
                 * Request Scale Generic Management Information
                 *  USB-OEM 3.1
                 */
                case DLSJposConst.DIO_REQ_SCALE_GEN_MGT:
                    abResp = doDirectIOExtended(CMD_SCALE_GEN_MGT_INFO);
                    if (abResp.length != 0) {
                        buf = abResp;
                        data[0] = 0;
                    } else {
                        buf = "ERROR: No command response received.".getBytes();
                        data[0] = 1;
                    }
                    break;
                /*
                 * Request Scale Vendor-specific Management Information
                 *  USB-OEM 3.1
                 */
                case DLSJposConst.DIO_REQ_SCALE_VEN_MGT:
                    abResp = doDirectIOExtended(CMD_SCALE_VEN_MGT_INFO, 1000);
                    if (abResp.length != 0) {
                        buf = abResp;
                        data[0] = 0;
                    } else {
                        buf = "ERROR: No command response received.".getBytes();
                        data[0] = 1;
                    }
                    break;
                /*
                 * Request Read of the Digital Watermark Label Filter
                 *  USB-OEM 3.1
                 */
                case DLSJposConst.DIO_READ_DWM_LABELFILTER:
                    abResp = doDirectIOExtended(CMD_READ_DWM_LABELFILTER, 1000);
                    if ((stat2 & 8) == 8) {
                        if ((stat0 & 64) == 0) {
                            buf = "Label Filter List is empty.".getBytes();
                        } else {
                            buf = abResp;
                        }
                        data[0] = 0;
                    } else {
                        buf = "ERROR: Command failed.".getBytes();
                        data[0] = 1;
                    }
                    break;
                /*
                 * Request Clear of the Digital Watermark Label Filter
                 *  USB-OEM 3.1
                 */
                case DLSJposConst.DIO_CLEAR_DWM_LABELFILTER:
                    doInternalMultiByteDirectIO(CMD_CLEAR_DWM_LABELFILTER, 1000);
                    if ((stat2 & 8) == 8) {
                        buf = "Label Filter cleared.".getBytes();
                        data[0] = 0;
                    } else {
                        buf = "ERROR: Command failed.".getBytes();
                        data[0] = 1;
                    }
                    break;
                /*
                 * Request Append to the Digital Watermark Label Filter
                 *  USB-OEM 3.1
                 */
                case DLSJposConst.DIO_APPEND_DWM_LABELFILTER:
                    //if (data.length > 0) {
                    abCmd = new byte[data.length + 3];
                    abCmd[0] = (byte) 0x35;
                    abCmd[1] = (byte) 0xFF;
                    abCmd[2] = (byte) 0xED;
                    for (int i = 0; i < data.length; i++) {
                        abCmd[i + 3] = (byte) data[i];
                    }
                    abResp = doDirectIOExtended(abCmd, 1000);
                    if ((stat2 & 8) == 8) {
                        buf = ("Bytes written = " + Integer.toString(abResp[0] & 0xFF)).getBytes();
                        data[0] = DLSJposConst.DL_ACK;
                    } else if ((stat2 & 16) == 16) {
                        buf = "Error: Command accepted but could not be processed at time of delivery, please try again.".getBytes();
                        data[0] = 1;
                    } else {
                        buf = "Error: Command failed.".getBytes();
                        data[0] = DLSJposConst.DL_NAK;
                    }
//                    } else {
//                        throw new DLSException(DLSJposConst.DLS_E_INVALID_ARG, "Input must be at least one byte.");
//                    }
                    break;
                case DLSJposConst.DIO_SCANNER_RESET:     // Reset
                    // In the case of a USB reset, the device will get sudo removed, then re-attached,
                    //  we need to be able to allow for this, and allow the device to get re-attached.
                    reset();
                    if (detectResetFinish()) {
                        buf = "Reset successful".getBytes();
                    } else {
                        buf = "Unable to communicate with scanner after reset, please check device.".getBytes();
                    }
                    break;
                case DLSJposConst.DIO_SCANNER_ENABLE_BEEP:        // Beeper enable
                    buf = sendReceiveCmd(enableBeepCmd);
                    data[0] = -1;
                    if (buf.length > 0) {
                        if ((buf[0] & 0x10) == 0x10) {
                            data[0] = 0;
                        } else {
                            data[0] = 1;
                        }
                    }
                    break;
                case DLSJposConst.DIO_SCANNER_DISABLE_BEEP:      // Beeper disable
                    buf = sendReceiveCmd(disableBeepCmd);
                    data[0] = -1;
                    if (buf.length > 0) {
                        if ((buf[0] & 0x10) == 0) {//TODO: is it correct??
                            data[0] = 0;
                        } else {
                            data[0] = 1;
                        }
                    }
                    break;
                case DLSJposConst.DIO_SCANNER_DISABLE:           // scanner disable
                    buf = doInternalSend(disableCmd);
                    dioResults(data, buf);
                    break;
                case DLSJposConst.DIO_SCANNER_ENABLE:      // scanner enable
                    buf = doInternalSend(enableCmd);
                    dioResults(data, buf);
                    break;
                case DLSJposConst.DIO_SCANNER_CONFIGURE:                 // Configure scanner
                    // The input array must contain the 9 bytes used to configure the scanner.
                    // No verification is done on these bytes, so they better be correct by the caller.
                    if (data.length >= 9) {
                        configCmd[2] = (byte) data[0];
                        configCmd[3] = (byte) data[1];
                        configCmd[4] = (byte) data[2];
                        configCmd[5] = (byte) data[3];
                        configCmd[6] = (byte) data[4];
                        configCmd[7] = (byte) data[5];
                        configCmd[8] = (byte) data[6];
                        configCmd[9] = (byte) data[7];
                        configCmd[10] = (byte) data[8];
                        //F70000013900000003
                        // Update the configuration in the scanner.
                        buf = sendReceiveCmd(configCmd);
                        data[0] = -1;
                        if (buf.length > 0) {
                            if ((buf[2] & 0x01) == 0x01) {
                                data[0] = 0;
                                if ((buf[2] & 0x02) == 0x02) {
                                    buf = "Configuration coerced".getBytes();
                                } else {
                                    buf = "Configuration successful".getBytes();
                                }
                            } else {
                                buf = "Configuration failed".getBytes();
                                data[0] = 1;
                            }
                        }
                    } else {
                        throw new DLSException(DLSJposConst.DLS_E_INVALID_ARG, "Must provide 9 bytes of configuration data");
                    }
                    break;
                case DLSJposConst.DIO_SCANNER_REPORT_CONFIG:             // Report configuration
                    DLSScannerConfig oCfg = reportConfiguration();                    // Update our internal config object from device
                    data[0] = -1;
                    if (reportBuf.length > 0) {
                        if ((reportBuf[1] & 0x02) == 0x02) {
                            data[0] = 0;
                        } else {
                            data[0] = 1;
                        }
                    }
                    String sRpt = dumpConfig(oCfg, "");
                    buf = sRpt.getBytes();
                    break;
                case DLSJposConst.DIO_SCANNER_CONFIG_2LABEL:   // Configure 2-label flags
                    if (data.length >= 8) {
                        config2LabelCmd[2] = (byte) data[0];
                        config2LabelCmd[3] = (byte) data[1];
                        config2LabelCmd[4] = (byte) data[2];
                        config2LabelCmd[5] = (byte) data[3];
                        config2LabelCmd[6] = (byte) data[4];
                        config2LabelCmd[7] = (byte) data[5];
                        config2LabelCmd[8] = (byte) data[6];
                        config2LabelCmd[9] = (byte) data[7];
                    } else {
                        throw new DLSException(DLSJposConst.DLS_E_INVALID_ARG, "Data array, invalid size");
                    }
                    buf = sendReceiveCmd(config2LabelCmd);
                    data[0] = -1;
                    if (buf.length > 0) {
                        if ((buf[2] & 0x04) == 0x04) {
                            data[0] = 0;
                            buf = "Configuration successful".getBytes();
                        } else {
                            data[0] = 1;
                            buf = "Configuration failed".getBytes();
                        }
                    }
                    break;
                case DLSJposConst.DIO_SCANNER_REPORT_2LABEL:  // Report 2-label flags
                    byte[] temp;
                    long timeout = DEFAULT_TIMEOUT;
                    long elapsed = 0;
                    long lStart;
                    synchronized (reportResp) {
                        reportBuf = EMPTY_BYTE_ARRAY;
                        port.sendData(reportConfig2LabelCmd, reportConfig2LabelCmd.length);
                        lStart = System.nanoTime();
                        try {
                            while (reportBuf.length == 0) {
                                reportResp.wait(timeout - elapsed);
                                elapsed = ((System.nanoTime() - lStart) / 1000000);
                                if (elapsed >= timeout) {
                                    break;
                                }
                            }
                        } catch (InterruptedException ie) {
                            throw new DLSException(ErrorConstants.APOS_E_FAILURE, "Interrupted wait while ScannerReport2Label: " + ie.getMessage());
                        }
                        temp = Arrays.copyOf(reportBuf, reportBuf.length);
                    }
                    if (temp.length > 11) {
                        buf = new byte[8];
                        buf[0] = temp[4];
                        buf[1] = temp[5];
                        buf[2] = temp[6];
                        buf[3] = temp[7];
                        buf[4] = temp[8];
                        buf[5] = temp[9];
                        buf[6] = temp[10];
                        buf[7] = temp[11];
                    } else {
                        throw new DLSException(ErrorConstants.APOS_E_FAILURE, "directIO.DIO_SCANNER_REPORT_2LABEL: Incomplete response received from scanner");
                    }
                    data[0] = -1;
                    if ((temp[1] & 0x04) == 0x04) {
                        data[0] = 0;
                    } else {
                        data[0] = 1;
                    }
//                    dioResults("DIO_SCANNER_REPORT_2LABEL", "sendData", data, buf);
                    break;
                case DLSJposConst.DIO_SCANNER_DIO_INFORMATION: // DirectIO information
                    // doInternalDirectIO will collect all data before returning.
                    buf = doInternalDirectIO(INFO_CMD);// Collects entire response, of multiple messages
                    dioResults(data, buf);
                    break;
                case DLSJposConst.DIO_SCANNER_DIO_HEALTH:                                       // DirectIO health
                    // doInternalDirectIO will collect all data before returning.
                    buf = doInternalDirectIO(HEALTH_CMD);// Collects entire response, of multiple messages
                    dioResults(data, buf);
                    break;
                case DLSJposConst.DIO_SCANNER_DIO_STATS:
                    // doInternalDirectIO will collect all data before returning.
                    buf = doInternalDirectIO(STATISTICS_CMD);
                    dioResults(data, buf);
                    break;
                case DLSJposConst.DIO_READ_STAT_LOG_COUNT:
                    buf = executeDIO(getNumberStats, "DIO_READ_STAT_LOG_COUNT", data, buf);
                    if (buf.length > 6) {
                        buf = Integer.toString((buf[5] * 256) + buf[6]).getBytes();
                    }
                    break;
                case DLSJposConst.DIO_READ_STAT_LOG_ITEMS:
                    StringBuilder oSb = new StringBuilder();

                    buf = doInternalMultiByteDirectIO(getNumberStats);
                    int iLength = buf[6];
                    for (int i = 0; i < iLength; i++) {
                        getStat[4] = (byte) i; // put the item number requested
                        buf = doInternalMultiByteDirectIO(getStat);
                        tempString = printToStatString(buf);
                        //resultString += intString;
                        oSb.append(tempString);
                    }
                    resultString = oSb.toString();
                    // place the resultString into the object
                    if (object instanceof ByteArrayOutputStream) {
                        ((ByteArrayOutputStream) object).reset();
                        try {
                            ((ByteArrayOutputStream) object).write(resultString.getBytes());
                        } catch (IOException ex) {
                            throw new DLSException(ErrorConstants.APOS_E_FAILURE, "During read_stats_log_items: " + ex.getMessage());
                        }
                    }
                    dioResults(data, buf);
                    bCopyObjectToBuf = true;
                    break;
                case DLSJposConst.DIO_READ_EVENT_LOG_ITEMS:
                    oSb = new StringBuilder();
                    for (int i = 0; i < 0xFE; i++) { // max possible number of events is 0xFE
                        getStatN[4] = (byte) i; // put the item number requested
                        buf = doInternalMultiByteDirectIO(getStatN);
                        tempString = printToEventString(buf);
                        if (tempString.contains("EOF")) {

                            break;
                        }
                        //resultString += intString;
                        oSb.append(tempString);
                    }
                    resultString = oSb.toString();
                    // place the resultString into the object
                    if (object instanceof ByteArrayOutputStream) {
                        ((ByteArrayOutputStream) object).reset();
                        try {
                            ((ByteArrayOutputStream) object).write(resultString.getBytes());

                        } catch (IOException ex) {
                            throw new DLSException(ErrorConstants.APOS_E_FAILURE, "During read_stats_log_items: " + ex.getMessage());
                        }
                    }
                    data[0] = -1;
                    if (buf.length > 0) {
                        if ((buf[1] & 0x40) == 0x40) {
                            data[0] = 0;
                        } else {
                            data[0] = 1;
                        }
                    }
                    bCopyObjectToBuf = true;

                    break;
                case DLSJposConst.DIO_READ_EVENT_LOG_LAST_EVENT:
                    buf = doInternalMultiByteDirectIO(getLastEvent);
                    tempString = printLastEventString(buf);
                    if (tempString.contains("EOF")) {
                        tempString = "Empty";
                    }
                    // place the resultString into the object
                    if (object instanceof ByteArrayOutputStream) {
                        ((ByteArrayOutputStream) object).reset();
                        try {
                            ((ByteArrayOutputStream) object).write(tempString.getBytes());
                        } catch (IOException ex) {
                            throw new DLSException(ErrorConstants.APOS_E_FAILURE, "During read_event_log_last_event: " + ex.getMessage());
                        }
                    }
                    dioResults(data, buf);
                    bCopyObjectToBuf = true;
                    break;
                case DLSJposConst.DIO_CLEAR_EVENT_LOG:
                    buf = executeDIO(clearEventLog, "DIO_CLEAR_EVENT_LOG", data, buf);
                    break;
                case DLSJposConst.DIO_SAVE_EVENT_LOG:
                    if (object.toString().isEmpty()) {
                        fileName = scannerConfig.getLogicalName() + "_event.log";
                    } else {
                        fileName = object.toString();
                    }
                    resultString = fileName + separator;
                    oSb = new StringBuilder();
                    for (int i = 0; i < 0xFE; i++) { // max possible number of events is 0xFE
                        getStatN[4] = (byte) i; // put the item number requested
                        buf = doInternalMultiByteDirectIO(getStatN);
                        tempString = printToEventString(buf);
                        if (tempString.contains("EOF")) {
                            break;
                        }
                        //resultString += intString;
                        oSb.append(tempString);
                    }
                    resultString += oSb.toString();
//                    try {
//                        //TODO: We have access only to internal memory of the application. Can we just return in the object the output and let the user to decide what to do with it?
//                        File eventFile = new File(fileName);
//                        output = new BufferedWriter(new FileWriter(eventFile));
//                        output.write(resultString);
//                    } catch (IOException e) {
//                        data[0] = -1;
//                        return;
//                    } finally {
//                        FunctionLib.cleanup(output);
//                    }
                    // place the resultString into the object
                    if (object instanceof ByteArrayOutputStream) {
                        ((ByteArrayOutputStream) object).reset();
                        try {
                            ((ByteArrayOutputStream) object).write(resultString.getBytes());
                        } catch (IOException ex) {
                            throw new DLSException(ErrorConstants.APOS_E_FAILURE, "During dio_save_event_event: " + ex.getMessage());
                        }
                    }
                    data[0] = -1;
                    if (buf.length > 0) {
                        if ((buf[1] & 0x40) == 0x40) {
                            data[0] = 0;
                        } else {
                            data[0] = 1;
                        }
                    }
                    bCopyObjectToBuf = true;
                    break;
                case DLSJposConst.DIO_SAVE_STATS_LOG:
                    if (object.toString().isEmpty()) {
                        fileName = scannerConfig.getLogicalName() + "_stats.log";
                    } else {
                        fileName = object.toString();
                    }
                    resultString = fileName + separator;
                    buf = doInternalMultiByteDirectIO(getNumberStats);
                    iLength = buf[6];
                    oSb = new StringBuilder();
                    for (int i = 0; i < iLength; i++) {
                        getStat[4] = (byte) i; // put the item number requested
                        buf = doInternalMultiByteDirectIO(getStat);
                        tempString = printToStatString(buf);
                        //resultString += intString;
                        oSb.append(tempString);
                    }
                    resultString += oSb.toString();

//                    try {
//                        //TODO: We have access only to internal memory of the application. Can we just return in the object the output and let the user to decide what to do with it?
//                        File statFile = new File(fileName);
//                        output = new BufferedWriter(new FileWriter(statFile));
//                        output.write(resultString);
//                    } catch (IOException e) {
//                        data[0] = -1;
//                        return;
//                    } finally {
//                        FunctionLib.cleanup(output);
//                    }
                    // place the resultString into the object
                    if (object instanceof ByteArrayOutputStream) {
                        ((ByteArrayOutputStream) object).reset();
                        try {
                            ((ByteArrayOutputStream) object).write(resultString.getBytes());
                        } catch (IOException ex) {
                            throw new DLSException(ErrorConstants.APOS_E_FAILURE, "During dio_save_stats_log: " + ex.getMessage());
                        }
                    }
                    data[0] = -1;
                    if (buf.length > 0) {
                        if ((buf[1] & 0x40) == 0x40) {
                            data[0] = 0;
                        } else {
                            data[0] = 1;
                        }
                    }
                    bCopyObjectToBuf = true;
                    break;
                case DLSJposConst.DIO_GET_SCALE_SENTRY_STATUS:
                    //NAK means that the scale sentry is "Not Blocked".
                    buf = executeDIO(getScaleSentryStatus, "DIO_GET_SCALE_SENTRY_STATUS", data, buf);
                    data[0] = -1;
                    if (buf.length > 0) {
                        data[0] = 0;
                        if (buf[5] == 0x15) {
                            buf = "Scale sentry NOT blocked".getBytes();
                        } else if (buf[5] == 0x06) {
                            buf = "Scale sentry BLOCKED".getBytes();
                        }
                    }
                    break;
                case DLSJposConst.DIO_CLEAR_SCALE_SENTRY:
                    buf = executeDIO(clearScaleSentryStatus, "DIO_CLEAR_SCALE_SENTRY", data, buf);
                    break;
                case DLSJposConst.DIO_READ_CONFIG_ITEM:
                    int sizeOfPayload;
                    // can't have a config item greater than 4 nibbles
                    if (object.toString().isEmpty() || (object.toString().length() > 4)) {
                        data[0] = -1;
                    } else {
                        String inString = object.toString();
                        byte[] tagID = FunctionLib.hexStringToByteArray(inString);
                        try {
                            sizeOfPayload = queryConfig(tagID);
                        } catch (Exception e) {
                            data[0] = DLSJposConst.DL_NAK;
                            return;
                        }
                        //Read the config item if no error occurred
                        if (sizeOfPayload >= 0) {
                            buf = readConfigItem(tagID, sizeOfPayload, data);
                        }

                    }
                    break;
                case DLSJposConst.DIO_WRITE_CONFIG_ITEM:
                    /*
                     * Writing a configuration tag item requires several steps.
                     * 1. Ensure the input is not empty, otherwise error. 2. At
                     * a minimum the value must be five characters. 3.
                     * Validation of tag ID (Must be four hex characters). 4.
                     * Determination of tag item type via query config command.
                     * 5. Validation of data (8 hex characters for int N
                     * characters for non-int). 6. Pad integer values with zeros
                     * to ensure 8 hex characters. 7. Check if the value has
                     * changed (The read is quicker than the write, this is just
                     * a performance optimization). 8. Write tag item value
                     * based on the type referenced in step 4 above.
                     */
                    byte[] tempBuf;

                    //Step 1: Ensure the input is not empty, otherwise error.
                    if (object.toString().isEmpty()) {
                        // can't have an empty tag item
                        data[0] = -1;
                        return;
                    }

                    //Step 2: At a minimum the value must be five characters.
                    String input = object.toString();
                    if (input.length() < 6) {
                        data[0] = DLSJposConst.DL_NAK;
                        return;
                    }

                    //Step 3: Validation of tag ID (Must be four hex characters).
                    String tagID = input.substring(0, 4);
                    byte[] byteArrayTagID = FunctionLib.hexStringToByteArray(tagID);
                    Pattern pattern = Pattern.compile("[0-9a-fA-F]{4}");
                    Matcher matcher = pattern.matcher(tagID);
                    if (!matcher.find()) {
                        data[0] = DLSJposConst.DL_NAK;
                        return;
                    }

                    //Step 4: Determination of tag item type via query config command.
                    //Note that the sizeOfPayload variable indicates both type and in the case
                    //of a non-integer value the length of the value of the tag item.
                    String value = input.substring(4);
                    int sizeOfInputValue = value.length() / 2;
                    byte[] configItem = FunctionLib.hexStringToByteArray(input);
                    sizeOfPayload = queryConfig(byteArrayTagID);

                    //Tag item is an integer (payload size zero indicates int).
                    if (sizeOfPayload == 0) {
                        //INTEGER TAG ITEM
                        //Step 5: Validation of data (8 hex characters for int N characters for non-int).
                        pattern = Pattern.compile("[0-9a-fA-F]+");
                        matcher = pattern.matcher(value);
                        if (!matcher.find() || value.length() > 8) {
                            data[0] = DLSJposConst.DL_NAK;
                            return;
                        }

                        //Step 6: Pad integer values with zeros to ensure 8 hex characters.
                        //input: 12 nibbles (6 bytes) includes tagID & value.
                        //tagID: 4 nibbles (2 bytes).
                        //value: 8 nibbles (4 bytes - for integer tag items only).
                        tempBuf = readConfigItem(byteArrayTagID, sizeOfPayload, data);
                        int intCurrent = new BigInteger(tempBuf).intValue();
                        int inputValue = new BigInteger(FunctionLib.hexStringToByteArray(value)).intValue();
                        String hexStringCurrent = String.format("%08x", intCurrent).replaceAll(" ", "0");
                        String hexStringInput = String.format("%08x", inputValue).replaceAll(" ", "0");

                        if (hexStringCurrent.equals(hexStringInput)) {
                            //Step 7: Check if the value has changed (The read is quicker than the write, this is just a performance optimization).
                            data[0] = DLSJposConst.DL_ACK;
                            return;
                        } else {
                            //Step 8: Write tag item value based on the type referenced in step 4 above.
                            System.arraycopy(configItem, 0, writeConfigInt, 4, 2);
                            System.arraycopy(FunctionLib.hexStringToByteArray(hexStringInput), 0, writeConfigInt, 6, 4);
                            tempBuf = doInternalMultiByteDirectIODelayed(writeConfigInt);
                            if (tempBuf.length == 0) {
                                data[0] = DLSJposConst.DL_NAK;
                                return;
                            }
                        }
                    } else if (sizeOfPayload != sizeOfInputValue) {
                        //The above condition implies that non-integer tag items are fixed length.
                        data[0] = DLSJposConst.DL_NAK;
                        break;
                    } else {
                        //NON-INTEGER TAG ITEM
                        //We have to see if the size is greater than the number
                        //of bytes we can send out in one directIO message. The
                        //message is of the form:
                        //0x30, 0x00, 0x74, 0x07, <item:2>, <offset:1>, <size:1>,<data:0-3>

                        //Step 7: Check if the value has changed (The read is quicker than the write, this is just a performance optimization).
                        tempBuf = readConfigItem(byteArrayTagID, sizeOfPayload, data);
                        if (Arrays.equals(tempBuf, FunctionLib.hexStringToByteArray(value))) {
                            data[0] = DLSJposConst.DL_ACK;
                            return;
                        } else {
                            //Step 8: Write tag item value based on the type referenced in step 4 above.
                            byte[] outBuf = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
                            System.arraycopy(writeConfigNonInt, 0, outBuf, 0, 4);
                            System.arraycopy(configItem, 0, outBuf, 4, 2);
                            for (int i = 0; i < configItem.length - 2; i++) {
                                outBuf[6] = (byte) i;
                                outBuf[7] = (byte) 1;
                                outBuf[8] = configItem[i + 2];
                                tempBuf = doInternalMultiByteDirectIODelayed(outBuf);
                            }
                        }
                    }
                    // lets tear apart the buf and properly pass the data back
                    // format of data in buf
                    // byte [0] = length (N)
                    // bytes [1-3] = status
                    // byte [4] = continuation byte
                    // byte [5] = readConfig status response
                    // bytes [6-N] = readConfig data response
                    if (tempBuf.length == 0) {
                        data[0] = -1;
                    } else {
                        data[0] = tempBuf[5];
                    }
                    buf = EMPTY_BYTE_ARRAY;
                    break;
                case DLSJposConst.DIO_COMMIT_CONFIG_ITEMS:
                    data[0] = 0;
                    break;
                case DLSJposConst.DIO_CELL_PHONE_ECOMM_ENTER:
                    //Enable Fast CFR
                    buf = executeDIO(cellPhoneModeECOMMEnter, "DIO_CELL_PHONE_ECOMM_ENTER", data, buf);
                    break;
                case DLSJposConst.DIO_CELL_PHONE_ECOMM_EXIT:
                    //Disable Fast CFR
                    buf = executeDIO(cellPhoneModeECOMMExit, "DIO_CELL_PHONE_ECOMM_EXIT", data, buf);
                    break;
                case DLSJposConst.DIO_CELL_PHONE_SCAN_ENTER:
                    buf = executeDIO(cellPhoneModeScannerEnter, "DIO_CELL_PHONE_SCAN_ENTER", data, buf);
                    Thread.sleep(1500);
                    break;
                case DLSJposConst.DIO_CELL_PHONE_SCAN_EXIT:
                    buf = doInternalMultiByteDirectIODelayed(cellPhoneModeScannerExit);
                    dioResults(data, buf);
                    Thread.sleep(1500);
                    break;
                case DLSJposConst.DIO_EXT_VIDEO_STREAM_ON:
                    buf = executeDIO(extVideoStreamOn, "DIO_EXT_VIDEO_STREAM_ON", data, buf);
                    break;
                case DLSJposConst.DIO_EXT_VIDEO_STREAM_OFF:
                    buf = executeDIO(extVideoStreamOff, "DIO_EXT_VIDEO_STREAM_OFF", data, buf);
                    break;
                case DLSJposConst.DIO_SCANNER_DIO_BEEP:
                    // Collects entire response, of multiple messages - why (all we have is header info here)?
                    buf = doInternalDirectIO(BEEP_CMD);
                    dioResults(data, buf);
                    break;
                case DLSJposConst.DIO_SCANNER_DIO_NOF:
                    // DirectIO "Not On File (NOF)"
                    // Collects entire response, of multiple messages - why (all we have is header info here)?
                    buf = doInternalDirectIO(NOF_BEEP_CMD);
                    dioResults(data, buf);
                    break;
                case DLSJposConst.DIO_SCANNER_DIO_ERROR_BEEP:
                    // Collects entire response, of multiple messages - why (all we have is header info here)?
                    buf = doInternalDirectIO(ERROR_BEEP_CMD);
                    dioResults(data, buf);
                    break;
                case DLSJposConst.DIO_DEV_PROTOCOL:
                    buf = "USB".getBytes();
                    data[0] = 0;
                    break;
                case DLSJposConst.DIO_TWO_WAY_HOST_RESPONSE:
                    String sMsg = object.toString();
                    sMsg = "@" + sMsg + "\r";
                    port.sendData(sMsg);
                    break;
                case -7:
                    /*
                     * Special case to allow access to the DLSScanner instance
                     * from a jpos scanner. This was done to enable CMDFW to
                     * actually be able to change the baud rate through sending
                     * different HDL files. This case should NEVER be made
                     * public, for internal use only.
                     */
                    ((DLSScannerService) object).setScanner(this);
                    return;
                default:
                    throw new DLSException(DLSJposConst.DLS_E_INVALID_ARG, "Unsupported/Invalid Direct I/O command received for this device");
            }

            if (buf.length != 0 && object instanceof ByteArrayOutputStream) {  // If there is data available to return to caller.
                // A ByteArrayOutputStream object must be passed in, that can be used to write
                //  the data to, for return.
                if (bCopyObjectToBuf) {
                    buf = ((ByteArrayOutputStream) object).toByteArray();
                }
                try {
                    ByteArrayOutputStream oBs = (ByteArrayOutputStream) object;
                    oBs.reset(); // must reset so that the data can overwrite the object
                    oBs.write(buf);
                } catch (IOException e) {
                    throw new DLSException(DLSJposConst.DLS_E_INVALID_ARG, "Need a ByteArrayOutStream object to return data");
                }
            }
        } catch (RuntimeException re) {
            throw re;
        } catch (Exception e) {
            data[0] = -1;
        }
    }

    /**
     * Disables the device.
     *
     * @throws DLSException if invalid state transition is attempted
     */
    @Override
    public void disable() throws DLSException {
        if (DLSJposConst.updateInProgress) {
            return;
        }

        doInternalSend(disableCmd);
        //JAVAPOS_SWCR_314 reverting to version without wait due to calls on reader thread.
        //byte[] buf = sendCommandAndWaitForResponse(disableCmd);

        /*
         * JAZZ_61648 - Since DLSUsbJavax does not currently support automatic
         * renabling of the scanner after it is disconnected, the response from
         * the disable command cannot currently be checked since it will be
         * locked in ENABLED state. Ignoring the response allows the user to
         * manually release and reclaim the scanner to establish communication
         * again. If Javax is changed to support automatic reenable, this can be
         * reimplemented.
         *
         * if (buf.length != 0) { if ((buf[2] & 0x02) == 0x00) {
         * setState(DLSState.DISABLED); LOG.trace("enable (out)"); return; } }
         * throw new DLSException(DLSJposConst.DLS_E_DISABLE, "Failed to disable
         * device.");
         */
        setState(DLSState.DISABLED);
    }

    /**
     * Disables the beep on good read functionality.
     * <p>
     * Only available on USB-OEM and SC-RS232 interfaces. This method has no
     * effect otherwise.
     *
     * @throws DLSException not thrown
     */
    @Override
    public void disableBeep() throws DLSException {
        if (DLSJposConst.updateInProgress) {
            return;
        }
        port.sendData(disableBeepCmd, disableBeepCmd.length);
    }

    /**
     * Performs a Health Check on the device. If check fails or timeout occurs,
     * method returns {@code false}
     *
     * @param timeout int indicating the amount of time to wait for the health
     *                check to complete
     * @return boolean indicating if scanner passed the health check
     * @throws DLSException if an error occurs
     */
    @Override
    public boolean doHealthCheck(int timeout) throws DLSException {
        if (DLSJposConst.updateInProgress) {
            return true;
        }
        boolean result = true;                               // Default to happy

        synchronized (dioResp) {                         // Lock object
            dioBuf = doInternalDirectIO(HEALTH_CMD, timeout);
            if (dioBuf.length > 0) {                       // If we got a response.
                logHealthCheck(dioBuf);                   // Call base class
            } else {
                result = false;
            }
        }

        return result;
    }

    /**
     * Performs a self check on the device.
     *
     * @throws DLSException if an error occurs
     */
    @Override
    public void doSelfTest() throws DLSException {
        if (DLSJposConst.updateInProgress) {
            return;
        }
        port.sendData(doSelfTestCmd, doSelfTestCmd.length);
    }

    /**
     * Enables the device.
     *
     * @throws DLSException if invalid state transition is attempted
     */
    @Override
    public void enable() throws DLSException {
        if (DLSJposConst.updateInProgress) {
            return;
        }

        byte[] buf = doInternalSend(enableCmd);

        if (buf.length != 0) {
            if ((buf[2] & 0x02) == 0x02) {
                setState(DLSState.ENABLED);
                return;
            }
        }

        //JAVAPOS_SWCR_314 reverting to version without wait due to calls on reader thread.
        //byte[] buf = sendCommandAndWaitForResponse(enableCmd);
        /*
         * JAZZ_58381 - Added retry on first enable command failure when a
         * device has been suspended on Windows 10. Command was being sent
         * before the device had enumerated properly.
         */
        int wait = options.getRetryWaitTime();
        try {
            Thread.sleep(wait);
        } catch (InterruptedException ex) {
            throw new DLSException(ErrorConstants.APOS_E_FAILURE, "Interrupted while sleeping.");
        }
        buf = doInternalSend(enableCmd);

        if (buf.length != 0) {
            if ((buf[2] & 0x02) == 0x02) {
                setState(DLSState.ENABLED);
                return;
            }
        }

        throw new DLSException(DLSJposConst.DLS_E_ENABLE, "Failed to enable device.");
    }

    /**
     * Enables the beep on good read functionality.
     * <p>
     * Only available on USB-OEM and SC-RS232 interfaces. This method has no
     * effect otherwise.
     *
     * @throws DLSException not thrown
     */
    @Override
    public void enableBeep() throws DLSException {
        if (DLSJposConst.updateInProgress) {
            return;
        }
        port.sendData(enableBeepCmd, enableBeepCmd.length);
    }

    /**
     * Returns an extracted barcode label from the supplied buffer.
     *
     * @param inBuf  {@inheritDoc}
     * @param offset int indicating the length of the label identifier
     *               that will be removed
     * @return byte array containing the extracted barcode label
     */
    @Override
    protected byte[] extractBarcodeLabel(byte[] inBuf, int offset) {
        // Create a buffer of the proper length
        byte[] buf = new byte[inBuf.length - offset];
        System.arraycopy(inBuf, 0, buf, 0, inBuf.length - offset);
        return buf;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HashMap<String, Object> getStatistics() throws DLSException {
        HashMap<String, Object> table = new HashMap<>();

        if (DLSJposConst.updateInProgress) {
            return table;
        }

        if (!scannerConfig.getCanAcceptStatisticsCommand()) {
            return table;
        }

        DLSStatistics stats = DLSStatistics.getInstance(context);
        String[] strInfo;
        String[] strStat;
        byte[] buf;
        String str;
        String truncated;
        String truncated2;
        int i;

        //table.put(DLSJposConst.DLS_S_GOOD_SCAN_COUNT, "NA");
        table.put(DLSJposConst.DLS_S_DEVICE_CATEGORY, "Scanner");
        table.put(DLSJposConst.DLS_S_INTERFACE, "USB-OEM");

        /*
         * JAVAPOS_SWCR_330 - For the Tahiti scanner, this will not work without
         * at least a 250 ms delay. The first command sent to the Tahiti takes
         * 250 ms to respond. The SWCR here asks for a configurable delay in
         * sending a command. Because of this, this will need to have a
         * configurable timeout to respond to this command. What happens now
         * with the Tahiti is that the command is sent, but then buf comes back
         * and is, of course, not NULL but contains no data. It is passed to the
         * FunctionLib.byteArrayToString() method as an empty buffer, and logs a
         * message complaining that the byte array is empty. If it is sent
         * again, there is no problem as the scanner is now past that initial
         * delay.
         */
        int standardTimeout = nObjWaitTime;
        if (standardTimeout < 100) {
            standardTimeout = 100;
        }

        DLSScannerConfig oConfig = getScannerConfig();
        int timeOut = oConfig.getExtendedHostTimeout();
        if (timeOut != 0) {
            buf = doDirectIOExtended(INFO_CMD, timeOut);
        } else {
            buf = doDirectIOExtended(INFO_CMD, standardTimeout);           // Get information via Directio command
        }

        /*
         * JAZZ_58381 - Added retry on first 'i' command failure to mimic OPOS
         * behavior when a device has been suspended on Windows 10. Command was
         * being sent before the device had enumerated properly.
         */
        if (buf.length == 0 && hasStatisticsReporting()) {
            try {
                Thread.sleep(options.getRetryWaitTime());
            } catch (InterruptedException ex) {
                throw new DLSException(ErrorConstants.APOS_E_FAILURE, "Interrupted when sleeping.");
            }
            if (timeOut != 0) {
                buf = doDirectIOExtended(INFO_CMD, timeOut);
            } else {
                buf = doDirectIOExtended(INFO_CMD, standardTimeout);
            }
        }

        String rawBytes;
        if (buf.length == 0) {         // If nothing returned
//            if (hasStatisticsReporting()) {
            throw new DLSException(DLSJposConst.DLS_E_TIMEOUT, "Unable to get statistical data.");
//            }
//            return table;                                 // Return map with a few fields
        } else {
            rawBytes = FunctionLib.byteArrayToString(buf);
            table.put("RawInfo", "# bytes received" + rawBytes);
        }
        String strInfoString = new String(buf);                    // Split results into string array

        strInfo = strInfoString.split("\03");

        // Ok lets look through the list of strings, find the strings we know about, and
        //  dump their values into the HashMap. (see R90-0060.pdf)
        for (i = 0; i < strInfo.length; i++) {
            str = strInfo[i];
            truncated = str.replaceAll("\\p{Cntrl}", "");
            truncated2 = truncated.replaceAll("Sp<", "");

            if (truncated2.length() > 1) {
                stats.parseInfo(table, truncated2.substring(0, 1), truncated2.substring(1));
            }
        }

        //statMessageSent = true;
        buf = doDirectIOExtended(HEALTH_CMD);  // Get health via Directio command
        if (buf.length == 0) {                            // If nothing returned
            throw new DLSException(DLSJposConst.DLS_E_TIMEOUT, "Error retrieving Health via DirectIO");
        }

        rawBytes = FunctionLib.byteArrayToString(buf);
        table.put("RawHealth", "# bytes received" + rawBytes);

        strInfoString = new String(buf);                    // Split results into string array
        strStat = strInfoString.split("\03");
        // Ok lets look through the list of strings, find the strings we know about, and
        //  dump their values into the HashMap.
        for (i = 0; i < strStat.length; i++) {
            str = strStat[i];
            truncated = str.replaceAll("\\p{Cntrl}", "");
            // JAVAPOS_SWCR_114 - Corrected copy / paste error.
            truncated2 = truncated.replaceAll("Sp=", "");

            if (truncated2.length() > 1) {
                stats.parseHealth(table, truncated2.substring(0, 1), truncated2.substring(1));
            }
        }

        //statMessageSent = true;
        buf = doDirectIOExtended(STATISTICS_CMD);
        if (buf.length == 0) {
            throw new DLSException(DLSJposConst.DLS_E_TIMEOUT, "Error retrieving Statistical data via DirectIO.");
        }

        rawBytes = FunctionLib.byteArrayToString(buf);
        table.put("RawStats", "# bytes received" + rawBytes);

        strInfoString = new String(buf);
        strStat = strInfoString.split("\03");
        // Ok lets look through the list of strings, find the strings we know about, and
        // dump their values into the HashMap.
        for (i = 0; i < strStat.length; i++) {
            str = strStat[i];
            truncated = str.replaceAll("\\p{Cntrl}", "");
            // JAVAPOS_SWCR_114 - Corrected copy / paste error.
            truncated2 = truncated.replaceAll("Sp>", "");
            if (truncated2.length() > 1) {
                stats.parseStatistic(table, truncated2.substring(0, 1), truncated2.substring(1));
            }
        }

        if (!table.containsKey(DLSJposConst.DLS_S_INTERFACE)) {
            table.put(DLSJposConst.DLS_S_INTERFACE, "USB");
        }
        Branding oBr = Branding.getInstance();
        String sName = oBr.getManufacturerShortName();
        table.put(DLSJposConst.DLS_S_MANUFACTURE_NAME, sName);
        table.put(DLSJposConst.DLS_S_DEVICE_CATEGORY, "Scanner");

        //statMessageSent = false;
        statistics = table;

        return table;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasStatisticsReporting() {
        return scannerConfig.getCanAcceptStatisticsCommand();
    }

    @Override
    void internalRelease() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAlive() throws DLSException {
        boolean alive = false;
        byte[] buf = sendReceiveCmd(statusCmd, 1000);
        if (buf[0] + buf[1] + buf[2] != 0) {
            alive = true;
        }
        return alive;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDataReceived(byte[] buf, int len) {
        DLSDeviceInfo oInfo = getDeviceInfo();
        int nResp = 0;                          // Amalgomated response bytes.
        if (buf == null || len == 0) {
            return;
        }
        if (buf[0] < buf.length) {
            buf = Arrays.copyOf(buf, buf[0]);
        }
        synchronized (objStatus) {                // Lock the object
            nResp |= (buf[1]);                  // Lets put all three bytes
            nResp |= ((buf[2]) << 8);           // of the status response into
            nResp |= ((buf[3]) << 16);          // a 4 byte integer for easy comparisons.
            stat0 = buf[1];                    // Save status byte 0
            stat1 = buf[2];                    // Save status byte 1
            stat2 = buf[3];                    // Save status byte 2
            gotStatus = true;
            objStatus.notifyAll();                // Notify waits.
        }

        /*
         * JAVAPOS_SWCR_314 reverting to version without wait due to calls on
         * reader thread. if (bEnableDisable == true) { synchronized
         * (enableDisableResp) { bEnableDisable = false; enableDisableBuf =
         * inBuf; enableDisableStatus(nResp, false);
         * enableDisableResp.notifyAll(); return; } } else { // And it is
         * unknown if this is needed but here I am going to just check // to
         * make sure that the device enabled status has not changed when the
         * request // was not an enable/disable request [SHOULD NEVER HAPPEN]!
         * // If the enabled status changes, then raise an event //
         * ********************************************* // NOTE: valid
         * enable/disable status events occur in // the syncronized block above.
         *
         * // @TODO: Fix handheld firmware so that it will never report enabled
         * when the device is not enabled. // See JAVAPOS_SWCR_176
         * enableDisableStatus(nResp, true);
        }
         */
        // Set default label type to unknown
        int nType;

        // If the Alive status changes, then raise an event
        if (((nResp & RESP_ALIVE) == RESP_ALIVE)) {
            if (!localAlive) {
                fireDeviceStatusEvent(DeviceErrorStatusListener.STATUS_ALIVE);
                localAlive = true;
            }
        } else if (localAlive) {
            fireDeviceStatusEvent(DeviceErrorStatusListener.STATUS_NOT_ALIVE);
        }

        if ((nResp & RESP_FIRMWARE_FLASH) == RESP_FIRMWARE_FLASH) {
            fireDeviceErrorEvent(DeviceErrorStatusListener.ERR_FLASHING);
        } else if ((nResp & RESP_HARDWARE_ERROR) == RESP_HARDWARE_ERROR) {
            fireDeviceErrorEvent(DeviceErrorStatusListener.ERR_HARDWARE);
        } else if ((nResp & RESP_NOT_READY) == RESP_NOT_READY) {
            if (raiseBusy) {
                fireDeviceErrorEvent(DeviceErrorStatusListener.ERR_BUSY);
            }
        } else if ((nResp & RESP_CHECKDIGIT_ERROR) == RESP_CHECKDIGIT_ERROR) {
            fireDeviceErrorEvent(DeviceErrorStatusListener.ERR_CHECKDIGIT);
        } else if ((nResp & RESP_CMD_REJECT) == RESP_CMD_REJECT) {
            fireDeviceErrorEvent(DeviceErrorStatusListener.ERR_CMD);
        } else if ((nResp & RESP_DIO_NOT_ALLOWED) == RESP_DIO_NOT_ALLOWED) {
            fireDeviceErrorEvent(DeviceErrorStatusListener.ERR_DIO_NOT_ALLOWED);
        } else if ((nResp & RESP_DIO_UNDEFINED) == RESP_DIO_UNDEFINED) {
            fireDeviceErrorEvent(DeviceErrorStatusListener.ERR_DIO_UNDEFINED);
        } else if ((nResp & RESP_CONFIG_DATA) == RESP_CONFIG_DATA) {
            synchronized (reportResp) {
                reportBuf = Arrays.copyOf(buf, buf.length);
                reportResp.notifyAll();
            }
        } else if ((nResp & RESP_EANJANCONFIG_DATA) == RESP_EANJANCONFIG_DATA) {
            synchronized (reportResp) {
                reportBuf = Arrays.copyOf(buf, buf.length);
                reportResp.notifyAll();
            }
        } else if ((nResp & RESP_DIO_DATA) == RESP_DIO_DATA) {
            synchronized (dioResp) {
                if (internalDIO) {
                    internalDIO = false;
                    //we check for DIO ACCEPTED regardless of if there is data or not. Every time. Why not?!
                    if ((nResp & RESP_DIO_SUCCESS) == RESP_DIO_SUCCESS) {
                        internalDIOSuccess = true;
                    }
                    dioBuf = Arrays.copyOf(buf, buf.length);
                    dioResp.notifyAll();
                }
            }
        } else if ((nResp & RESP_DIO_SUCCESS) == RESP_DIO_SUCCESS) {
            synchronized (dioResp) {
                if (internalDIO) {
                    internalDIO = false;
                    internalDIOSuccess = true;
                    dioBuf = Arrays.copyOf(buf, buf.length);
                    dioResp.notifyAll();
                }
            }
        } else if ((nResp & RESP_CONFIG_COERCED) == RESP_CONFIG_COERCED) {
        } else if ((nResp & RESP_CONFIG_SUCCESS) == RESP_CONFIG_SUCCESS) {
        } else if ((nResp & RESP_EANJANCONFIG_SUCCESS) == RESP_EANJANCONFIG_SUCCESS) {
        } else if (buf[0] == 4) {
            // This must be a status response
            synchronized (statusResp) {
                statusBuf = Arrays.copyOf(buf, buf.length);
                statusResp.notifyAll();
            }
        } /*else if (nResp == 0) {
        }*/ else {
            // This must be a barcode
            int bufLen = buf[0];
            byte[] identifier;
            int identifierLength;
            if (buf[bufLen - 1] == 0x00) {
                identifierLength = 4;
                identifier = new byte[4];
                identifier[0] = buf[bufLen - 4];
                identifier[1] = buf[bufLen - 3];
                identifier[2] = buf[bufLen - 2];
                identifier[3] = buf[bufLen - 1];
                nType = ScannerConstants.SCAN_SDT_OTHER;
            } else {
                if (buf[bufLen - 1] == 0x0B) {
                    identifier = new byte[3];
                    //identifier[0] = inBuf[bufLen - 3];
                    identifier[0] = 0;
                    identifier[1] = buf[bufLen - 2];
                    identifier[2] = buf[bufLen - 1];
                } else {
                    identifier = new byte[1];
                    identifier[0] = buf[bufLen - 1];
                }
                LabelHelper labelHelper = DLAPosConfigHelper.getInstance(context).getLabelHelper();
                labelHelper.setDecodeType(LabelHelper.DecodeType.IBMUSBOEM);
                LabelIds labelsIds = labelHelper.getLabelsIds(identifier);
                identifierLength = identifier.length;
                nType = labelsIds.getUposId();
            }

            int moreDataByte = buf[bufLen - 3];
            if (identifierLength == 4) {
                moreDataByte = buf[bufLen - 4];
            }

            // The barcode labels returned from the table scanner are binary, but
            // the barcodes returned from the hand scanner are text. It is up to
            // this interpretation layer to convert them so they are both the same.
            /*
             * JAVAPOS_SWCR_326 - Multiple segments mean that the identifier has
             * to be extracted from every segment, otherwise it appears in the
             * middle of the ScanData. This alters the labelData extraction to
             * take the identifier into account.
             */
            byte[] labelData;
            labelData = new byte[bufLen - 4 - identifierLength];
            if (bufLen - identifierLength - 4 >= 0) {
                System.arraycopy(buf, 4, labelData, 0, bufLen - identifierLength - 4);
            }

            if (oInfo.getUsage() == DLSJposConst.DEV_TABLE_SCANNER_USAGE) {
                switch (nType) {
                    case ScannerConstants.SCAN_SDT_EAN13:
                    case ScannerConstants.SCAN_SDT_EAN8:
                    case ScannerConstants.SCAN_SDT_UPCA:
                    case ScannerConstants.SCAN_SDT_UPCE:
                    case ScannerConstants.SCAN_SDT_UPCA_S:
                    case ScannerConstants.SCAN_SDT_UPCE_S:
                    case ScannerConstants.SCAN_SDT_EAN8_S:
                    case ScannerConstants.SCAN_SDT_EAN13_S:
                        DLSProperties oOpt = DLSProperties.getInstance(context);
                        convertBCDtoASCII = oOpt.isBCDtoASCII();
                        if (convertBCDtoASCII) {
                            // These barcodes are in binary form,
                            // convert to text.
                            for (int i = 0; i < labelData.length; i++) {
                                if ((labelData[i] >= 0 && labelData[i] <= 9)) {
                                    labelData[i] = (byte) (labelData[i] + 0x30);
                                }
                            }
                        }
                        break;
                    default:
                        break;
                }
            }

            boolean hasMore = false;
            if (identifierLength == 4) {
                /*
                 * JAVAPOS_SWCR_349, JAVAPOS_SWCR_347 This patch is intended to
                 * address an error in the HP version of the Gryphon scanner.
                 * Certain labels are identified with a broken 4 byte identifier
                 * (0x04 0x00 0x00 0x00). This can cause several problems with
                 * the packet concatenation bit so this patch addresses the
                 * issue by masking against the bit for 4 byte identifiers or
                 * just comparing the byte to 0x10 for 3-byte and single byte
                 * identifiers.
                 */
                if ((moreDataByte & 0x10) == 0x10) {
                    hasMore = true;
                }
            } else {
                if (moreDataByte == 0x10) {
                    hasMore = true;
                }
            }

            if (hasMore) {
                concatUSBMessages(labelData, false);
            } else {
                concatUSBMessages(labelData, true);
                for (Object o : messageList) {
                    byte[] tmpBuf = (byte[]) o;
                    if (tmpBuf.length > 0) {
                        /*
                         * JAVAPOS_SWCR_326 - Created a new rawLabel array that
                         * concatenates the tmpBuf (reassembled ScanDataLabel)
                         * and the identifier. This is then extracted and passed
                         * on to the fireLabelReceivedEvent method.
                         */
                        byte[] rawLabel = new byte[tmpBuf.length + identifierLength];
                        System.arraycopy(tmpBuf, 0, rawLabel, 0, tmpBuf.length);
                        System.arraycopy(identifier, 0, rawLabel, tmpBuf.length, identifierLength);
                        byte[] barcode = extractBarcodeLabel(rawLabel, identifierLength);
                        fireLabelReceivedEvent(rawLabel, barcode, nType);
                    }
                }
                messageList.clear();                                  // Be sure to clean list after
            }
        } // end of barcode
    }

    /**
     * {@inheritDoc} Performs final tasks following a DIO reset.
     */
    @Override
    public void onDeviceArrival() {
        try {
            if (getDeviceEnabled()) { // If it was previously enabled
//                setState(DLSState.DISABLED);
                enable(); // Enable device.
            }
            if (m_hotPlugWatcher != null) {
                m_hotPlugWatcher.cancel(false);
                m_hotPlugWatcher = null;
            }
        } catch (DLSException e) {
            e.printStackTrace();
            fireDeviceErrorEvent(ErrorConstants.APOS_E_FAILURE);
        }
        fireDeviceStatusEvent(CommonsConstants.SUE_POWER_ONLINE);
        synchronized (resetObj) {
            resetSuccess = true;
            hotPlugged = true;
            resetObj.notifyAll();
        }
    }

    /**
     * {@inheritDoc} Called by the {@link com.datalogic.dlapos.androidpos.transport.DLSPort}
     * object, when the same device is re-attached, before the object is
     * destroyed.
     */
    @Override
    public void onDeviceReattached() {
//        if (port.openPort()) {     // Re-open the port
//            if (hotPlugged) { //Necessary for Power Notify on Linux
//                onDeviceArrival();
//            }
//        } else {
//            fireDeviceErrorEvent(DeviceErrorStatusListener.ERR_DEVICE_REATTACHED);
//        }

        boolean portOpened = port.openPort();

        /* JAZZ_65514 - Added retry for opening port on suspended
         * devices on Windows 10.
         */
        if (!portOpened) {
            try {
                Thread.sleep(options.getRetryWaitTime());
            } catch (InterruptedException ie) {
                Log.e(TAG, "onDeviceReattached: sleep interrupted, ", ie);
            }
            portOpened = port.openPort();
        }
        if (portOpened) {
            if (hotPlugged) {
                m_hotPlugWatcher = m_scheduler.scheduleAtFixedRate(new ResetWatcherThread(), 3000, 1000, TimeUnit.MILLISECONDS);
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
        // stop timer
        fireDeviceStatusEvent(CommonsConstants.SUE_POWER_OFF_OFFLINE);
        fireDeviceErrorEvent(DeviceErrorStatusListener.ERR_DEVICE_REMOVED);
        port.closePort();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void open(String logicalName, Context context) throws DLSException {
        super.open(logicalName, context);
        options = DLSProperties.getInstance(context);
        nObjWaitTime = options.getObjectWaitTime();
    }

    /**
     * Returns a DLSScannerConfig instance containing a representation of the
     * current configuration of the scanner.
     *
     * @return DLSScannerConfig containing the configuration of the scanner.
     * @throws DLSException thrown if an Exception occurs while reading the
     *                      configuration from the scanner.
     */
    @Override
    public DLSScannerConfig reportConfiguration() throws DLSException {
        DLSScannerConfig sConfig = new DLSScannerConfig();
        if (DLSJposConst.updateInProgress) {
            return sConfig;
        }

        byte[] buf;
        long timeout = DEFAULT_TIMEOUT;
        long elapsed = 0;
        long start;
        synchronized (reportResp) {
            reportBuf = EMPTY_BYTE_ARRAY;
            port.sendData(reportConfigCmd, reportConfigCmd.length);
            start = System.nanoTime();
            try {
                while (reportBuf.length == 0) {
                    reportResp.wait(timeout - elapsed);
                    elapsed = ((System.nanoTime() - start) / 1000000);
                    if (elapsed >= timeout) {
                        break;
                    }
                }
            } catch (InterruptedException ie) {
                throw new DLSException(ErrorConstants.APOS_E_FAILURE, "Interrupted waiting statistics: " + ie.getMessage());
            }
            buf = Arrays.copyOf(reportBuf, reportBuf.length);
        }

        if (buf.length == 0) {
            return sConfig;
        }
        // did we get a valid response?
        byte cfg0 = buf[4];
        byte cfg1 = buf[5];
        byte cfg2 = buf[6];
        byte cfg3 = buf[7];
        byte cfg4 = buf[8];
        byte cfg5 = buf[9];
        byte cfg6 = buf[10];
        byte cfg7 = buf[11];
        byte cfg8 = buf[12];

        // It has become apparent that the Config returned from the scale/scanner
        //  does not necessarily show the accurate config, but is just an echo of the
        //  previous set config command.  So lets use a temporary config object here
        sConfig.setEnableUPCEAN((cfg0 & 0x01) == 0x01);
        sConfig.setEnableUPCD1D5((cfg0 & 0x02) == 0x02);
        sConfig.setEnableCode39((cfg0 & 0x04) == 0x04);
        sConfig.setEnableInterleaved((cfg0 & 0x08) == 0x08);
        sConfig.setEnableCodabar((cfg0 & 0x10) == 0x10);
        sConfig.setEnableCode93((cfg0 & 0x20) == 0x20);
        sConfig.setEnableCode128((cfg0 & 0x40) == 0x40);
        sConfig.setEnableUCCEAN128((cfg0 & 0x80) == 0x80);

        sConfig.setEnable2DigitSups((cfg1 & 0x01) == 0x01);
        sConfig.setEnable5DigitSups((cfg1 & 0x02) == 0x02);
        sConfig.setEnableCode128Sups((cfg1 & 0x04) == 0x04);
        sConfig.setEnableUPCACheckDigit((cfg1 & 0x08) == 0x08);
        sConfig.setEnableUPCECheckDigit((cfg1 & 0x10) == 0x10);
        sConfig.setEnableCode39CheckDigit((cfg1 & 0x20) == 0x20);
        sConfig.setEnableITFCheckDigit((cfg1 & 0x40) == 0x40);

        sConfig.setEnableEANJAN2LabelDecode((cfg2 & 0x01) == 0x01);
        sConfig.setEnableUPCAtoEAN13Expansion((cfg2 & 0x02) == 0x02);
        sConfig.setEnableUPCEtoEAN13Expansion((cfg2 & 0x04) == 0x04);
        sConfig.setEnableUPCEtoUPCAExpansion((cfg2 & 0x08) == 0x08);
        sConfig.setEnable4DigitPriceCheckDigit((cfg2 & 0x10) == 0x10);
        sConfig.setEnable5DigitPriceCheckDigit((cfg2 & 0x20) == 0x20);

        sConfig.setEnableGoodReadBeep((cfg3 & 0x01) == 0x01);
        sConfig.setBeepVolume((cfg3 & 0x06) >> 1);
        sConfig.setBeepFrequency((cfg3 & 0x18) >> 3);
        sConfig.setBeepDuration((cfg3 & 0x60) >> 5);

        sConfig.setMotorTimeout((cfg4 & 0x07));
        sConfig.setLaserTimeout((cfg4 & 0x18) >> 3);
        sConfig.setDoubleReadTimeout((cfg4 & 0x60) >> 5);

        sConfig.setStoreLabelSecurityLevel(cfg5 & 0x03);

        sConfig.setITFLength1(cfg6 & 0x3f);
        sConfig.setTwoITFs((cfg6 & 0x40) == 0x40);
        sConfig.setITFRange((cfg6 & 0x80) == 0x80);

        sConfig.setITFLength2(cfg7 & 0x3f);

        sConfig.setLEDGoodReadDuration((cfg8 & 0x03));
        sConfig.setEnableBarCodeProgramming((cfg8 & 0x04) == 0x04);
        sConfig.setEnableLaserOnOffSwitch((cfg8 & 0x08) == 0x08);
        sConfig.setEnableVolumeSwitch((cfg8 & 0x10) == 0x10);

        return sConfig;
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
    @Override
    public void reset() throws DLSException {
        if (DLSJposConst.updateInProgress) {
            return;
        }
        port.sendData(resetCmd, resetCmd.length);
    }

    /**
     * <b> Not implemented, will throw UnsupportedOperationException at runtime.
     * </b>
     */
    @Override
    public int sendMessage(String message) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Method has no effect, implemented by {@link DLSUSBFlash} for this
     * interface.
     *
     * @param record {@inheritDoc}
     * @return Always {@link DLSJposConst#DL_NRESP}
     */
    @Override
    public int sendRecord(String record) {
        return DLSJposConst.DL_NRESP;
    }

    /**
     * Updates the scanner configuration with parameters from the current
     * scanner configuration object.
     * <p>
     * Only available for USB-OEM scanners. This method has no effect otherwise.
     *
     * @throws DLSException not thrown
     */
    @Override
    public void updateConfiguration() throws DLSException {
        if (DLSJposConst.updateInProgress) {
            return;
        }
        byte cfg0 = 0, cfg1 = 0, cfg2 = 0, cfg3 = 0, cfg4 = 0, cfg5 = 0, cfg6 = 0, cfg7 = 0, cfg8 = 0;

        if (scannerConfig.getEnableUPCEAN()) {
            cfg0 |= 0x01;
        }
        if (scannerConfig.getEnableUPCD1D5()) {
            cfg0 |= 0x02;
        }
        if (scannerConfig.getEnableCode39()) {
            cfg0 |= 0x04;
        }
        if (scannerConfig.getEnableInterleaved()) {
            cfg0 |= 0x08;
        }
        if (scannerConfig.getEnableCodabar()) {
            cfg0 |= 0x10;
        }
        if (scannerConfig.getEnableCode93()) {
            cfg0 |= 0x20;
        }
        if (scannerConfig.getEnableCode128()) {
            cfg0 |= 0x40;
        }
        if (scannerConfig.getEnableUCCEAN128()) {
            cfg0 |= 0x80;
        }

        if (scannerConfig.getEnable2DigitSups()) {
            cfg1 |= 0x01;
        }
        if (scannerConfig.getEnable5DigitSups()) {
            cfg1 |= 0x02;
        }
        if (scannerConfig.getEnableCode128Sups()) {
            cfg1 |= 0x04;
        }
        if (scannerConfig.getEnableUPCACheckDigit()) {
            cfg1 |= 0x08;
        }
        if (scannerConfig.getEnableUPCECheckDigit()) {
            cfg1 |= 0x10;
        }
        if (scannerConfig.getEnableCode39CheckDigit()) {
            cfg1 |= 0x20;
        }
        if (scannerConfig.getEnableITFCheckDigit()) {
            cfg1 |= 0x40;
        }

        if (scannerConfig.getEnableEANJAN2LabelDecode()) {
            cfg2 |= 0x01;
        }
        if (scannerConfig.getEnableUPCAtoEAN13Expansion()) {
            cfg2 |= 0x02;
        }
        if (scannerConfig.getEnableUPCEtoEAN13Expansion()) {
            cfg2 |= 0x04;
        }
        if (scannerConfig.getEnableUPCEtoUPCAExpansion()) {
            cfg2 |= 0x08;
        }
        if (scannerConfig.getEnable4DigitPriceCheckDigit()) {
            cfg2 |= 0x10;
        }
        if (scannerConfig.getEnable5DigitPriceCheckDigit()) {
            cfg2 |= 0x20;
        }

        if (scannerConfig.getEnableGoodReadBeep()) {
            cfg3 |= 0x01;
        }
        cfg3 |= (scannerConfig.getBeepVolume()) << 1;
        cfg3 |= (scannerConfig.getBeepFrequency()) << 3;
        cfg3 |= (scannerConfig.getBeepDuration()) << 5;

        cfg4 |= (scannerConfig.getMotorTimeout());
        cfg4 |= (scannerConfig.getLaserTimeout()) << 3;
        cfg4 |= (scannerConfig.getDoubleReadTimeout()) << 5;

        cfg5 |= (scannerConfig.getStoreLabelSecurityLevel());

        cfg6 |= (scannerConfig.getITFLength1());
        if (scannerConfig.getTwoITFs()) {
            cfg6 |= 0x40;
        }
        if (scannerConfig.getITFRange()) {
            cfg6 |= 0x80;
        }

        cfg7 |= (scannerConfig.getITFLength2());

        cfg8 |= (scannerConfig.getLEDGoodReadDuration());
        if (scannerConfig.getEnableBarCodeProgramming()) {
            cfg8 |= 0x04;
        }
        if (scannerConfig.getEnableLaserOnOffSwitch()) {
            cfg8 |= 0x08;
        }
        if (scannerConfig.getEnableVolumeSwitch()) {
            cfg8 |= 0x10;
        }

        configCmd[2] = cfg0;
        configCmd[3] = cfg1;
        configCmd[4] = cfg2;
        configCmd[5] = cfg3;
        configCmd[6] = cfg4;
        configCmd[7] = cfg5;
        configCmd[8] = cfg6;
        configCmd[9] = cfg7;
        configCmd[10] = cfg8;

        long timeout = DEFAULT_TIMEOUT;
        long elapsed = 0;
        long start;
        synchronized (statusResp) {
            statusBuf = EMPTY_BYTE_ARRAY;
            port.sendData(configCmd, configCmd.length);
            start = System.nanoTime();
            try {
                while (statusBuf.length == 0) {
                    statusResp.wait(timeout - elapsed);
                    elapsed = ((System.nanoTime() - start) / 1000000);
                    if (elapsed >= timeout) {
                        break;
                    }
                }
            } catch (InterruptedException ie) {
                throw new DLSException(ErrorConstants.APOS_E_FAILURE, "Interrupted updating statistics: " + ie.getMessage());
            }
        }

    }

    //region Support functions

    /**
     * Formats the configuration object into a single string with each item on a
     * new line.
     * <p>
     * Format: {@code <prefix> <config_item>: ................ <value>}
     *
     * @param config DLSScannerConfig object containing configuration data
     * @param prefix String containing any additional formatting to prepend to
     *               each line
     * @return String containing formatted configuration data
     */
    public String dumpConfig(DLSScannerConfig config, String prefix) {
        String result = "";
        result += prefix + " EnableUPCEAN: ................ " + config.getEnableUPCEAN() + separator;
        result += prefix + " EnableUPCD1toD5: ............. " + config.getEnableUPCD1D5() + separator;
        result += prefix + " EnableCode39: ................ " + config.getEnableCode39() + separator;
        result += prefix + " EnableInterleaved: ........... " + config.getEnableInterleaved() + separator;
        result += prefix + " EnableCodabar: ............... " + config.getEnableCodabar() + separator;
        result += prefix + " EnableCode93: ................ " + config.getEnableCode93() + separator;
        result += prefix + " EnableCode128: ............... " + config.getEnableCode128() + separator;
        result += prefix + " EnableUCCEAN128: ............. " + config.getEnableUCCEAN128() + separator;
        result += prefix + " Enable2DigitSups: ............ " + config.getEnable2DigitSups() + separator;
        result += prefix + " Enable5DigitSups: ............ " + config.getEnable5DigitSups() + separator;
        result += prefix + " EnableCode128Sups: ........... " + config.getEnableCode128Sups() + separator;
        result += prefix + " EnableUPCACheckDigit: ........ " + config.getEnableUPCACheckDigit() + separator;
        result += prefix + " EnableUPCECheckDigit: ........ " + config.getEnableUPCECheckDigit() + separator;
        result += prefix + " EnableCode39CheckDigit: ...... " + config.getEnableCode39CheckDigit() + separator;
        result += prefix + " EnableItfCheckDigit: ......... " + config.getEnableITFCheckDigit() + separator;
        result += prefix + " EnableEANJAN2LabelDecode: .... " + config.getEnableEANJAN2LabelDecode() + separator;
        result += prefix + " EnableUPCAtoEAN13Expansion: .. " + config.getEnableUPCAtoEAN13Expansion() + separator;
        result += prefix + " EnableUPCEtoUPCAExpansion: ... " + config.getEnableUPCEtoUPCAExpansion() + separator;
        result += prefix + " Enable4DigitPriceCheckDigit: . " + config.getEnable4DigitPriceCheckDigit() + separator;
        result += prefix + " Enable5DigitPriceCheckDigit: . " + config.getEnable5DigitPriceCheckDigit() + separator;
        result += prefix + " EnableGoodReadBeep: .......... " + config.getEnableGoodReadBeep() + separator;
        result += prefix + " BeepVolume: .................. " + config.getBeepVolume() + separator;
        result += prefix + " BeepFrequency: ............... " + config.getBeepFrequency() + separator;
        result += prefix + " BeepDuration: ................ " + config.getBeepDuration() + separator;
        result += prefix + " MotorTimeout: ................ " + config.getMotorTimeout() + separator;
        result += prefix + " LaserTimeout: ................ " + config.getLaserTimeout() + separator;
        result += prefix + " DoubleReadTimeout: ........... " + config.getDoubleReadTimeout() + separator;
        result += prefix + " StoreLabelSecurityLevel: ..... " + config.getStoreLabelSecurityLevel() + separator;
        result += prefix + " ITFLength1: .................. " + config.getITFLength1() + separator;
        result += prefix + " ITFLength2: .................. " + config.getITFLength2() + separator;
        result += prefix + " Two Itfs specified: .......... " + config.getTwoITFs() + separator;
        result += prefix + " ITFisRange: .................. " + config.getITFRange() + separator;
        return result;
    }

    private byte[] doDirectIOExtended(byte cmd) {
        return doDirectIOExtended(cmd, nObjWaitTime);
    }

    private byte[] doDirectIOExtended(byte[] cmd) {
        return doDirectIOExtended(cmd, nObjWaitTime);
    }

    private byte[] doDirectIOExtended(byte cmd, long timeout) {
        directIOCmd[1] = 0x00;                       // Set command position 1
        directIOCmd[2] = cmd;                        // Set command position 2
        return doDirectIOExtended(directIOCmd, timeout);
    }

    private byte[] doDirectIOExtended(byte[] cmd, long timeout) {
        byte[] abResult = EMPTY_BYTE_ARRAY;
        boolean bFinished = false;
        ByteArrayOutputStream oStream = new ByteArrayOutputStream();

        if (DLSJposConst.updateInProgress) {
            return abResult;
        }
        long elapsed;
        long start;
        synchronized (dioResp) {
            internalDIO = true;
            dioBuf = EMPTY_BYTE_ARRAY;
            port.sendData(cmd, cmd.length);
            start = System.nanoTime();
            elapsed = 0;
            while (!bFinished) {
                internalDIO = true;
                while (dioBuf.length == 0) {
                    try {
                        dioResp.wait(timeout - elapsed);
                    } catch (InterruptedException oIe) {
                        return abResult;
                    }
                    elapsed = (long) ((System.nanoTime() - start) / 1000000);
                    if (elapsed >= timeout) {
                        bFinished = true;
                        break;
                    }
                }
                //Check the continuation byte and if it is not set, mark the response as finished.
                if (dioBuf.length > 4) {
                    if (dioBuf[4] == 0) {
                        bFinished = true;
                    }
                    try {
                        oStream.write(dioBuf, 5, dioBuf.length - 5);
                    } catch (IndexOutOfBoundsException iobe) {
                        break;
                    }

                }
                dioBuf = EMPTY_BYTE_ARRAY;
            }
            abResult = oStream.toByteArray();
            internalDIO = false;
        }

        return abResult;
    }

    /**
     * Attempt to abstract the most common form of direct IO call where there is
     * a multibyte command and a single packet returned which typically simply
     * contains the header info and an ACK/NAK/CAN/BEL.
     * <p>
     * This function also handles the results of the DIO call in a standard form
     * using DIOResults() and performs some standard logging as well.
     *
     * @param cmd
     * @param cmdConstant
     * @param data
     * @param buf
     */
    private byte[] executeDIO(byte[] cmd, String cmdConstant, int[] data, byte[] buf) throws DLSException {
        //Execute the command.
        buf = doInternalMultiByteDirectIOSinglePacket(cmd);

        dioResults(data, buf);

        return buf;
    }

    /**
     * Executes a Direct I/O command, synchronously waiting for a response. A
     * default timeout set by the {@code objWaitTime} property will be used.
     *
     * @param cmd byte indicating the command to be sent
     * @return byte array containing the scanner response. Array will be empty
     * if timeout is reached
     */
    private byte[] doInternalDirectIO(byte cmd) throws DLSException {
        return doInternalDirectIO(cmd, nObjWaitTime);
    }

    /**
     * Executes a Direct I/O command, synchronously waiting for a response.
     *
     * @param cmd     byte indicating the command to be sent
     * @param timeOut int indicating the number of milliseconds to wait for a
     *                response
     * @return byte array containing the scanner response. Array will be empty
     * if timeout is reached
     */
    private byte[] doInternalDirectIO(byte cmd, int timeOut) throws DLSException {
        byte[] buf = EMPTY_BYTE_ARRAY;
        if (DLSJposConst.updateInProgress) {
            return buf;
        }
        long elapsed = 0;
        long start;
        synchronized (dioResp) {
            internalDIO = true;                          // Set internal Direct IO command
            directIOCmd[1] = 0x00;                        // Set command position 1
            directIOCmd[2] = cmd;                        // Set command to check health
            dioBuf = EMPTY_BYTE_ARRAY;                                  // Clear buffer
            port.sendData(directIOCmd, directIOCmd.length);// Issue direct io command to device
            start = System.nanoTime();
            try {
                while (dioBuf.length == 0) {
                    dioResp.wait((long) timeOut - elapsed);
                    elapsed = (long) ((System.nanoTime() - start) / 1000000);
                    if (elapsed >= (long) timeOut) {
                        break;
                    }
                }
            } catch (InterruptedException ie) {
                throw new DLSException(ErrorConstants.APOS_E_FAILURE, "Interrupted while sending");
            }
            if (dioBuf.length > 6) {
                buf = Arrays.copyOfRange(dioBuf, 5, dioBuf.length - 1);
            }
            internalDIO = false;
        }
        return buf;
    }

    private byte[] doInternalMultiByteDirectIO(byte[] cmd) throws DLSException {
        return doInternalMultiByteDirectIO(cmd, nObjWaitTime);
    }

    private byte[] doInternalMultiByteDirectIO(byte[] cmd, long timeout) throws DLSException {
        byte[] buf = EMPTY_BYTE_ARRAY;
        if (DLSJposConst.updateInProgress) {
            return buf;
        }
        long elapsed = 0;
        long start;
        synchronized (dioResp) {
            internalDIO = true;                          // Set internal Direct IO command
            dioBuf = EMPTY_BYTE_ARRAY;                                   // Set buffer to notta
            port.sendData(cmd, cmd.length);// Issue direct io command to device
            start = System.nanoTime();
            try {
                while (dioBuf.length == 0) {
                    dioResp.wait(timeout - elapsed);
                    elapsed = ((System.nanoTime() - start) / 1000000);
                    if (elapsed >= timeout) {
                        break;
                    }
                }
            } catch (InterruptedException ie) {
                throw new DLSException(ErrorConstants.APOS_E_FAILURE, "Interrupted while sending.");
            }
            buf = Arrays.copyOf(dioBuf, dioBuf.length);
            internalDIO = false;
        }
        return buf;
    }

    private byte[] doInternalMultiByteDirectIODelayed(byte[] cmd) throws DLSException {
        return doInternalMultiByteDirectIO(cmd, nObjWaitTime + 3000);
    }

    private byte[] doInternalMultiByteDirectIOSinglePacket(byte[] cmd) throws DLSException {
        byte[] buf = EMPTY_BYTE_ARRAY;
        if (DLSJposConst.updateInProgress) {
            return buf;
        }
        // Lock object
        long timeout = options.getCommandTimeout();
        long elapsed = 0;
        long start;
        synchronized (dioResp) {
            internalDIO = true;
            dioBuf = EMPTY_BYTE_ARRAY;                                   // Set buffer to notta
            port.sendData(cmd);
            start = System.nanoTime();
            try {
                while (internalDIO) {
                    dioResp.wait(timeout - elapsed);
                    elapsed = (long) ((System.nanoTime() - start) / 1000000);
                    if (elapsed >= timeout) {
                        break;
                    }
                }
            } catch (InterruptedException ie) {
                throw new DLSException(ErrorConstants.APOS_E_FAILURE, "Interrupted while sending.");
            }
            buf = Arrays.copyOf(dioBuf, dioBuf.length);
        }
        return buf;
    }

    private byte[] doInternalMultiByteDirectIOWithDelay(byte[] cmd) throws DLSException {
        return doInternalMultiByteDirectIO(cmd, nObjWaitTime + ACTION_TAG_DELAY);
    }

    /**
     * Sends a command to the scanner, synchronously waiting for a status
     * response. A default timeout will be used.
     *
     * @param cmd byte array containing command to send
     * @return byte array containing the scanner status bytes. Bytes will be
     * {@code 0} if timeout is reached
     */
    protected byte[] sendReceiveCmd(byte[] cmd) throws DLSException {
        return sendReceiveCmd(cmd, DEFAULT_TIMEOUT);
    }

    /**
     * Sends a command to the scanner, synchronously waiting for a status
     * response.
     *
     * @param cmd     byte array containing command to send
     * @param timeout int indicating the number of milliseconds to wait for a
     *                response
     * @return byte array containing the scanner status bytes. Bytes will be
     * {@code 0} if timeout is reached
     */
    protected byte[] sendReceiveCmd(byte[] cmd, int timeout) throws DLSException {
        byte[] response = new byte[3];
        long elapsed = 0;
        long start;
        synchronized (objStatus) {
            gotStatus = false;
            stat0 = 0;
            stat1 = 0;
            stat2 = 0;
            port.sendData(cmd);
            start = System.nanoTime();
            try {
                while (!gotStatus) {
                    objStatus.wait(timeout - elapsed);
                    elapsed = (long) ((System.nanoTime() - start) / 1000000);
                    if (elapsed >= timeout) {
                        break;
                    }
                }
            } catch (InterruptedException ie) {
                throw new DLSException(ErrorConstants.APOS_E_FAILURE, "Interrupted while waiting for response");
            }
            response[0] = stat0;
            response[1] = stat1;
            response[2] = stat2;
        }
        return response;
    }

    /**
     * Do a command, trapping the response and returning the buffer.
     *
     * @param cmd
     * @return
     */
    private byte[] doInternalSend(byte[] cmd) throws DLSException {
        byte[] buf = EMPTY_BYTE_ARRAY;
        if (DLSJposConst.updateInProgress) {
            return buf;
        }
        long timeout = nObjWaitTime;
        long elapsed = 0;
        long start;
        synchronized (statusResp) {
            statusBuf = EMPTY_BYTE_ARRAY;
            port.sendData(cmd); // Issue direct io command to device
            start = System.nanoTime();
            try {
                while (statusBuf.length == 0) {
                    statusResp.wait(timeout - elapsed);
                    elapsed = (long) ((System.nanoTime() - start) / 1000000);
                    if (elapsed >= timeout) {
                        break;
                    }
                }
            } catch (InterruptedException ie) {
                throw new DLSException(ErrorConstants.APOS_E_FAILURE, "Interrupted while wating for response");
            }
            buf = Arrays.copyOf(statusBuf, statusBuf.length);
        }
        return buf;
    }

    private void dioResults(int[] data, byte[] buf) {
        if (buf == null) {
            return;
        }

        if (internalDIOSuccess) {
            data[0] = 0;
            return;
        }

        //Evaluate return value and set data for return by reference.
        if (buf.length == 0) {
            //Report "Error: -1" to host [implies actual error halted execution].
            //No response returned.
            data[0] = -1;
        } else if (buf.length > 5) {
            //bInternalDIOSuccess is evaluated here to confirm command success
            //for both commands that return ACK/NAK or not.
            if (buf[5] == DLSJposConst.DL_NAK || !internalDIOSuccess) {
                /*
                 * EXAMPLE FOR DIO_CELL_PHONE_ECOMM_ENTER
                 *
                 * NAK means one of the following [implies action not
                 * performed]: Scanning disabled state active A picture is in
                 * progress CI_ECOM_IMAGERS != ecom only TDR is not operational,
                 * ECOM is disabled Unable to successfully complete the cell
                 * enter/exit command
                 *
                 * Report "NOT OK" to host
                 */
                data[0] = 1;
            } else if ((buf[5] == DLSJposConst.DL_ACK) || (buf[5] == 0x00)) {
                /*
                 * EXAMPLE FOR DIO_CELL_PHONE_ECOMM_ENTER
                 *
                 * ACK means: CI_ECOM_FUNCTION is always on (transparent) cell
                 * exit command received when not in cell mode cell enter
                 * command received when in cell mode enter/exit command
                 * completed successfully
                 *
                 * Report "OK" to host
                 */
                data[0] = 0;
            } else {
                //No ACK no NAK no DIOSuccess no nothing report "Error: -1" to host.
                //Essentially this case means we got something other than ACK/NAK at buf[5]
                //And we also did not have the bInternalDIOSuccess var set to true.
                data[0] = -1;
            }
        }
        internalDIOSuccess = false;
    }

    private String printToStatString(byte[] buf) {
        String resultString;
        int StatID = buf[5];
        byte InterID = buf[6];
        int StatValue = Integer.parseInt(String.format("%1$02X%2$02X%3$02X%4$02X", buf[7], buf[8], buf[9], buf[10]), 16);

        // find the end of the string
        int dataLength = buf[0];
        // just the string
        byte[] bBuf = new byte[dataLength - 11];
        System.arraycopy(buf, 11, bBuf, 0, dataLength - 11);

        String strBuf = new String(bBuf);

        resultString = String.format("StatID: %03d, InterID: %02X, Value: %05d, String: %s%n",
                StatID, InterID, StatValue, strBuf);

        return resultString;
    }

    String printToEventString(byte[] buf) {
        String resultString;
        if ((buf.length < 0x13) || (buf[0] < 0x13)) {
            return "EOF";
        }
        int i = 5;
        int EventType = buf[i++];
        int EventModule = buf[i++];

        int EventCode = buf[i++];
        int EventData = (buf[i + 0] << 24)
                | (buf[i + 1] << 16)
                | (buf[i + 2] << 8)
                | (buf[i + 3]);

        i += 4;

        int EventAdditionalData
                = (buf[i + 0] << 24)
                | (buf[i + 1] << 16)
                | (buf[i + 2] << 8)
                | (buf[i + 3]);
        i += 4;

        int EventHours
                = (buf[i + 0] << 8)
                | ((buf[i + 1]) & 0xFF);
        i += 2;

        int EventCount = buf[i++];

        //"{0,-7}{1,-10}{2,-30}{3,-8}:{4,-8}{5,10}{6,8}"
        resultString = String.format("EvType: %02X, EvMod: %02X, EvCod: %02X, EvDat: %08X, AddEvDat: %08X, Hours: %04d, EvCount %02X%n",
                EventType, EventModule, EventCode, EventData, EventAdditionalData, EventHours, EventCount);
        return resultString;
    }

    private String printLastEventString(byte[] buf) {
        String resultString;
        int i = 5;
        int EventType = buf[i++];
        int EventModule = buf[i++];

        int EventCode = buf[i++];
        int EventData = (buf[i + 0] << 24)
                | (buf[i + 1] << 16)
                | (buf[i + 2] << 8)
                | (buf[i + 3]);

        i += 4;

        int EventAdditionalData
                = (buf[i + 0] << 24)
                | (buf[i + 1] << 16)
                | (buf[i + 2] << 8)
                | (buf[i + 3]);
        i += 4;

        int EventHours
                = (buf[i + 0] << 8)
                | ((buf[i + 1]) & 0xFF);
        i += 2;

        int EventCount = buf[i++];

        //"{0,-7}{1,-10}{2,-30}{3,-8}:{4,-8}{5,10}{6,8}"
        resultString = String.format("EvType: %02X, EvMod: %02X, EvCod: %02X, EvDat: %08X, AddEvDat: %08X, Hours: %04d, EvCount %02X%n",
                EventType, EventModule, EventCode, EventData, EventAdditionalData, EventHours, EventCount);

        return resultString;
    }

    private int queryConfig(byte[] input) throws Exception {
        System.arraycopy(input, 0, queryConfig, 4, 2);
        byte[] tempBuf = doInternalMultiByteDirectIO(queryConfig);
        int sizeOfPayload = 0;
        byte statusByte;
        if (tempBuf.length > 1) {
            int lengthOffset = tempBuf[0] - 1;
            statusByte = tempBuf[5];
            sizeOfPayload = tempBuf[lengthOffset];
            if (statusByte != DLSJposConst.DL_ACK) {
                throw new Exception("The status byte of the query config command was not acknowledged.");
            }
        }
        return sizeOfPayload;
    }

    private byte[] readConfigItem(byte[] tagID, int sizeOfPayload, int[] data) throws DLSException {
        byte[] buf = EMPTY_BYTE_ARRAY;
        byte[] tempBuf = EMPTY_BYTE_ARRAY;
        internalDIO = true;
        if (sizeOfPayload == 0) {
            System.arraycopy(tagID, 0, readConfigInt, 4, 2);
            tempBuf = doInternalMultiByteDirectIO(readConfigInt);
            // lets tear apart the buf and properly pass the data back
            // format of data in buf
            // byte [0] = length (N)
            // bytes [1-3] = status
            // byte [4] = readConfig status response
            // byte [5] = continuation byte
            // bytes [6-N] = readConfig data response
            if (tempBuf.length > 6) {
                data[0] = tempBuf[5];
                int len = (int) tempBuf[0];
                buf = new byte[len - 6];
                System.arraycopy(tempBuf, 6, buf, 0, len - 6);
            }
        } else {
            System.arraycopy(tagID, 0, readConfigNonInt, 4, 2);
            buf = new byte[sizeOfPayload];
            for (int i = 0; i < sizeOfPayload; i++) {
                readConfigNonInt[6] = (byte) i; // offset
                readConfigNonInt[7] = (byte) 1; // 1 byte at a time
                tempBuf = doInternalMultiByteDirectIO(readConfigNonInt);
                if (tempBuf.length > 7) {
                    if (tempBuf[5] == DLSJposConst.DL_ACK) {
                        buf[i] = tempBuf[8];
                    }
                }
            }
            if (tempBuf.length > 4) {
                data[0] = tempBuf[5];
            }
        }
        return buf;
    }

    /**
     * This thread is used to determine if the device has returned after a DIO
     * Reset NOTE: only used on Linux
     */
    private class ResetWatcherThread implements Runnable {

        public boolean checkHealth() throws DLSException {
            boolean bRc = false;
            if (port.isOpen()) {
                byte[] buf = doInternalDirectIO(HEALTH_CMD, 500); //500 milliseconds for resetWatcher
                if (buf.length != 0) {
                    bRc = true;
                }
            }
            return bRc;
        }

        @Override
        public void run() {
            boolean deviceReady = false;
            try {
                deviceReady = checkHealth();
            } catch (DLSException e) {
                Log.e(TAG, "Exception checking ready: ", e);
            }
            if (deviceReady) {
                onDeviceArrival();
            }
        }
    }
}
