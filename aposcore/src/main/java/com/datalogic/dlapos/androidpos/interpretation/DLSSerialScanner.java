package com.datalogic.dlapos.androidpos.interpretation;

import android.content.Context;
import android.util.Log;

import androidx.core.util.Pair;

import com.datalogic.dlapos.androidpos.common.Branding;
import com.datalogic.dlapos.androidpos.common.DLSDeviceInfo;
import com.datalogic.dlapos.androidpos.common.DLSException;
import com.datalogic.dlapos.androidpos.common.DLSJposConst;
import com.datalogic.dlapos.androidpos.common.DLSObjectFactory;
import com.datalogic.dlapos.androidpos.common.DLSProperties;
import com.datalogic.dlapos.androidpos.common.DLSScannerConfig;
import com.datalogic.dlapos.androidpos.common.DLSState;
import com.datalogic.dlapos.androidpos.common.DLSStatistics;
import com.datalogic.dlapos.androidpos.common.FunctionLib;
import com.datalogic.dlapos.androidpos.service.DLSScannerService;
import com.datalogic.dlapos.androidpos.transport.DLSUsbPort;
import com.datalogic.dlapos.androidpos.transport.UsbPortStatusListener;
import com.datalogic.dlapos.commons.constant.CommonsConstants;
import com.datalogic.dlapos.commons.constant.ErrorConstants;
import com.datalogic.dlapos.confighelper.configurations.accessor.LabelIds;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Class representing a generic Datalogic scanner connected using a serial port.
 */
public class DLSSerialScanner extends DLSScanner {

    private final static String Tag = "DLSSerialScanner";

    int CMD_POS = 0;
    protected static final String ENABLE_CMD = "E";
    protected static final String DISABLE_CMD = "D";
    protected static final String RESET_CMD = "R";
    protected static final String NOF_BEEP_CMD = "F";
    private static final String ENABLE_BEEP_CMD = "\u0007";
    protected static final String BEEP_IF_BEEP_ENABLED_CMD = "B";
    private static final String FORCE_BEEP_CMD = "\u0001";
    protected static final String INFO_CMD = "i";
    protected static final String HEALTH_CMD = "h";
    protected static final String STATISTICS_CMD = "s";
    // reading and writing coinfig items
    protected static final String READ_CONFIG_CMD = "$c";
    protected static final String WRITE_CONFIG_CMD = "$C";
    protected static final String COMMIT_CONFIG_CMD = "$As";
    protected static final String OPEN_SERVICE_PORT_CMD = "$S\r";
    protected static final String SAVE_AND_CLOSE_SERVICE_PORT_CMD = "$Ar\r";
    protected static final String SERVICE_ACCEPT_RESPONSE = "$>";
    private static final String SERVICE_REJECT_RESPONSE = "$#";
    // reading scanner event log
//    private static final byte[] getMaxNumberStats = {0x30, 0x00, 0x75, 0x00, 0x00};
//    private static final byte[] getStat = {0x30, 0x00, 0x75, 0x01};
//    private static final byte[] getStatN = {0x30, 0x00, 0x75, 0x02};
//    private static final byte[] getLastStat = {0x30, 0x00, 0x75, 0x03, 0x00};
    // for THD with the 3300i - unit must be configured to
    // support toggle cell phone mode
    static final byte[] cellPhoneModeControl = {0x50, 0x01, 0x43};
    volatile boolean bDIOCPMInProgress = false;
    volatile boolean bDIOWriteConfigInProgress = false;
    volatile boolean bDIOCommitConfigInProgress = false;
    private final Object objWaitDIOCPMResp = new Object();
    private final Object objWaitDIOWriteConfigResp = new Object();
    private final Object objWaitDIOReadConfigResp = new Object();
    private final Object objWaitDIOCommitConfigResp = new Object();
    private final byte[] picTakingControl = {0x50, 0x04, 0x70, 0x53, 0x42, 0x43};
    // JAVAPOS_SWCR_280 - Picture Taking Control for 9x00 series
    private final byte[] picTakingCtrl9x00 = {0x50, 0x06, 0x70, 0x53, 0x42, 0x43, 0x54, 0x46};
    volatile boolean bDIOPTCInProgress = false;
    private boolean bDIOPTCStartInProgress = false;
    private boolean bDIOPTCTransInProgress = false;
    boolean bPicDone = true;
    boolean bDIOImageInProgress = false;
    private boolean bDIOImageStartInProgress = false;
    private boolean bDIOImageTransInProgress = false;
    private boolean bLengthMoreThanResponse = false;
    protected final Object objWaitDIOPTCTrans = new Object();
    private long nPicFileSize = 0;
    private int nPicFileImageSize = 0;
    private long nPicFileWritten = 0;
    private int nPicFileImageWritten = 0;
    private long nBaudDelay;
    private boolean emptyBuf = false;
    private String picFileName;
    private FileOutputStream picFile;
    protected ByteArrayOutputStream picBuf;
    //PrintWriter picFile;
    private int nWaitingOnEot = 0;
    byte[] sohBuf;
    final Object objWaitSohBuf = new Object();
    volatile boolean bFirmware = false;  // sending an srecord
    volatile boolean bReadConfigInProgress = false;  // reading a config item
    volatile boolean bStatMessageSent = false;
    final Object objWaitAck = new Object();
    private final Object resetObj = new Object();
    private String ImageFolderFileName = null;
    String extension = "JPG";
    private int imageCount = 0;
    String decodeType = null;
    static final byte[] imagecaputurenow = {0x78, 0x30, 0x30, 0x38, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x0D};
    static final byte[] imagenexttrigger = {0x78, 0x30, 0x31, 0x38, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x0D};
    //    private static final byte[] imageontrigger = {0x78, 0x30, 0x31, 0x39, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x0D};
    static final byte[] imageonnextdecode = {0x78, 0x30, 0x32, 0x38, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x0D};
    //    private static final byte[] imageondecode = {0x78, 0x30, 0x32, 0x39, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x0D};
    static final byte[] exitImageCapture = {0x78, 0x30, 0x34, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x0D};
    DLSProperties options = null;
    private boolean resetSuccess = false;
    volatile boolean hotPlugged = true;
    private volatile boolean isCOMDevice = false;
    private volatile boolean alreadyArrived = false;
    protected int currentBaud = 0;
    private static final byte[] EMPTY_BYTE_ARRAY = {};
    private boolean delayDisable = false;
    private final Object labelLock = new Object();

    private final ScheduledExecutorService m_scheduler = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> m_resetWatcher = null;
    ScheduledFuture<?> m_hotPlugWatcher = null;


    /**
     * {@inheritDoc}
     */
    @Override
    public void changeBaudRate(int baud) {
        if ((baud < 2400) || (baud > 115200)) {
            throw new IllegalArgumentException("changeBaudRate: Invalid Baud Rate (" + baud + "), must be between 2400 - 115200");
        }

        // If currentBaud hasn't been initialized, apos.json value is what is being used.
        if (currentBaud == 0) {
            currentBaud = getDeviceInfo().getBaudRate();
        }

        // Only update if necessary
        if (baud == currentBaud) {
            return;
        }
        port.changeBaudRate(baud);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean detectResetFinish() {
        long timeout = options.getResetTimeout();
        long elapsed = 0;
        long start;
        boolean result;

        hotPlugged = false;
        m_resetWatcher = m_scheduler.scheduleAtFixedRate(new ResetWatcherRunnable(), 3000, 1000, TimeUnit.MILLISECONDS);
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
                Log.e(Tag, "Interrupted: ", ie);
            }
            result = resetSuccess;
        }

        if (!result) {
            Log.e(Tag, "detectResetFinish: Unable to communicate with scanner after reset");
        }

        m_resetWatcher.cancel(false);
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void directIO(int command, int[] data, Object object) throws DLSException {

        if (DLSJposConst.updateInProgress) {
            return;
        }
        if (data == null || data.length == 0) {
            data = new int[1];
        }

        byte[] buf;
        String strResult = "Invalid Command";
        int nResp = 0;
        // to ensure we don't get label data disable the scanner - we will enable
        // if need be after we receive the response to the directIO command
        //if(bIsEnabled){
        //    disable();
        //}

        switch (command) {
            case DLSJposConst.DIO_SCANNER_RESET:
                reset();
                if (detectResetFinish()) {
                    strResult = "Reset successful";
                } else {
                    strResult = "Unable to communicate with scanner after reset, please check device.";
                }
                break;
            case DLSJposConst.DIO_SCANNER_BEEP:
                sendMsg(BEEP_IF_BEEP_ENABLED_CMD);
                strResult = "Scanner beeped";
                break;
            // JAVAPOS_SWCR_341 - Add DIO Support for NOF Beep on USB-COM interface.
            case DLSJposConst.DIO_SCANNER_DIO_NOF:
                sendMsg(NOF_BEEP_CMD);
                strResult = "Scanner beeped NOF";
                break;
            case DLSJposConst.DIO_OPEN_SERVICE_PORT_HH:
                buf = sendGeneralCmd(OPEN_SERVICE_PORT_CMD);
                if (new String(buf).startsWith(SERVICE_ACCEPT_RESPONSE)) {
                    data[0] = 0;
                    strResult = "Service port opened";
                } else {
                    data[0] = 1;
                    strResult = "Failed to open service port: " + new String(buf);
                }
                break;
            case DLSJposConst.DIO_CLOSE_SERVICE_PORT_HH:
                buf = sendGeneralCmd(SAVE_AND_CLOSE_SERVICE_PORT_CMD);
                if (new String(buf).startsWith(SERVICE_ACCEPT_RESPONSE)) {
                    data[0] = 0;
                    strResult = "Service port closed";
                } else {
                    data[0] = 1;
                    strResult = "Failed to close service port: " + new String(buf);
                }
                break;
            case DLSJposConst.DIO_SEND_GEN_SERVICE_CMD_HH:
                buf = sendGeneralCmd(object.toString() + "\r");
                if (new String(buf).startsWith(SERVICE_ACCEPT_RESPONSE)) {
                    data[0] = 0;
                } else {
                    data[0] = 1;
                }
                strResult = "Command Response: " + new String(buf);
                break;
            case DLSJposConst.DIO_READ_CONFIG_ITEM:
                byte[] myResponse = sendReadConfigCommand(READ_CONFIG_CMD, data, object);  // Get information
                if (myResponse.length == 0) {
                    data[0] = DLSJposConst.DL_NAK;
                    strResult = "ERROR: Read Config Item failed";
                    break;
                }
                strResult = new String(myResponse);
                if (myResponse.length == 1) { // only got one char back - must be an error
                    nResp = myResponse[0];
                }
                // strResult = "Config Item Result:" + strResult;
                if (nResp != 0) {
                    // do some processing on the returned data
                    switch (nResp) {
                        case DLSJposConst.DL_BEL: // BEL
                            data[0] = DLSJposConst.DL_BEL;
                            strResult = "ERROR: Invalid config item or data out of range";
                            break;
                        case DLSJposConst.DL_NAK: // NAK
                            data[0] = DLSJposConst.DL_NAK;
                            strResult = "ERROR: Malformed config item and/or data";
                            break;
                    }
                } else {
                    data[0] = DLSJposConst.DL_ACK; // ACK
                }
                break;
            case DLSJposConst.DIO_WRITE_CONFIG_ITEM:
                nResp = sendWriteConfigCommand(WRITE_CONFIG_CMD, data, object);  // Get information
                strResult = "Write Config Item:";
                if (nResp != 0) {
                    // do some processing on the returned data
                    switch (nResp) {
                        case DLSJposConst.DL_BEL: // BEL
                            data[0] = DLSJposConst.DL_BEL;
                            strResult = "ERROR: Config item does not exist";
                            break;
                        case DLSJposConst.DL_NAK: // NAK
                            data[0] = DLSJposConst.DL_NAK;
                            strResult = "ERROR: Invalid config item or data out of range";
                            break;
                        case DLSJposConst.DL_ACK: // ACK
                            data[0] = DLSJposConst.DL_ACK;
                            strResult = "OK: Config item accepted";
                            break;
                        case DLSJposConst.DL_CAN: // CAN
                            data[0] = DLSJposConst.DL_CAN;
                            strResult = "ERROR: Malformed config item and/or data";
                            break;
                    }
                }
                break;
            case DLSJposConst.DIO_COMMIT_CONFIG_ITEMS:
                nResp = sendCommitConfigItems(COMMIT_CONFIG_CMD);  // Get information
                strResult = "Config Items commited";
                if (nResp != 0) {
                    // do some processing on the returned data
                    switch (nResp) {
                        case DLSJposConst.DL_ACK: // ACK
                            data[0] = DLSJposConst.DL_ACK;
                            strResult = "OK: Commit accepted";
                            break;
                        case DLSJposConst.DL_BEL: // BEL
                            data[0] = DLSJposConst.DL_BEL;
                            strResult = "ERROR: Commit failed";
                            break;
                        default:
                            data[0] = nResp;
                            break;
                    }
                }
                break;
            case DLSJposConst.DIO_CELL_PHONE_MODE_CONTROL:
                nResp = sendDIOCPMCommand(new String(cellPhoneModeControl));  // Get information
                strResult = "Cell phone mode control";
                if (nResp != 0) {
                    // do some processing on the returned data
                    switch (nResp) {
                        case DLSJposConst.DL_BEL: // BEL
                            data[0] = DLSJposConst.DL_BEL;
                            strResult = "ERROR: Cell phone mode not configured";
                            break;
                        case DLSJposConst.DL_ETB: // ETB
                            data[0] = DLSJposConst.DL_ETB;
                            strResult = "ERROR: Scanner currently taking picture";
                            break;
                        case DLSJposConst.DL_NAK: // NAK
                            data[0] = DLSJposConst.DL_NAK;
                            strResult = "ERROR: Cell phone mode active and in progress";
                            break;
                        case DLSJposConst.DL_ACK: // ACK
                            data[0] = DLSJposConst.DL_ACK;
                            strResult = "OK: One-shot cell phone mode activated";
                            break;
                        case 0x54: // 'T'
                            data[0] = 0x54;
                            strResult = "OK: Toggle mode activated";
                            break;
                        case 0x74: // 't'
                            data[0] = 0x74;
                            strResult = "OK: Toggle mode deactivated";
                            break;
                    }
                }
                break;
            case DLSJposConst.DIO_CELL_PHONE_ECOMM_ENTER:
            case DLSJposConst.DIO_CELL_PHONE_ECOMM_EXIT:
                break;
            case DLSJposConst.DIO_CELL_PHONE_SCAN_ENTER:
                nResp = sendDIOCPMCommand(new String(cellPhoneModeControl));
                strResult = "Cell phone mode enter";
                if (nResp != 0) {
                    // do some processing on the returned data
                    switch (nResp) {
                        case DLSJposConst.DL_BEL: // BEL
                            data[0] = DLSJposConst.DL_BEL;
                            strResult = "ERROR: Cell phone mode not configured";
                            break;
                        case DLSJposConst.DL_ETB: // ETB
                            data[0] = DLSJposConst.DL_ETB;
                            strResult = "ERROR: Scanner currently taking picture";
                            break;
                        case DLSJposConst.DL_NAK: // NAK
                            data[0] = DLSJposConst.DL_NAK;
                            strResult = "ERROR: Cell phone mode active and in progress";
                            break;
                        case DLSJposConst.DL_ACK: // ACK
                            data[0] = DLSJposConst.DL_ACK;
                            strResult = "OK: One-shot cell phone mode activated";
                            break;
                        case 0x54: // 'T'
//                            data[0] = 0x54;
                            strResult = "Cell mode mode activated";
                            data[0] = DLSJposConst.DL_ACK;
                            break;
                        case 0x74: // 't'
//                            data[0] = 0x74;
                            sendDIOCPMCommand(new String(cellPhoneModeControl));
                            strResult = "Cell mode mode activated";
                            data[0] = DLSJposConst.DL_ACK;
                            break;
                    }
                }
                break;
            case DLSJposConst.DIO_CELL_PHONE_SCAN_EXIT:
                nResp = sendDIOCPMCommand(new String(cellPhoneModeControl));  // Get information
                strResult = "Cell phone mode exit";
                if (nResp != 0) {
                    switch (nResp) {
                        case DLSJposConst.DL_BEL: // BEL
                            data[0] = DLSJposConst.DL_BEL;
                            strResult = "ERROR: Cell phone mode not configured";
                            break;
                        case DLSJposConst.DL_ETB: // ETB
                            data[0] = DLSJposConst.DL_ETB;
                            strResult = "ERROR: Scanner currently taking picture";
                            break;
                        case DLSJposConst.DL_NAK: // NAK
                            data[0] = DLSJposConst.DL_NAK;
                            strResult = "ERROR: Cell phone mode active and in progress";
                            break;
                        case DLSJposConst.DL_ACK: // ACK
                            data[0] = DLSJposConst.DL_ACK;
                            strResult = "OK: One-shot cell phone mode activated";
                            break;
                        case 0x54: // 'T'
                            data[0] = 0x54; // if we come back activated then toggle it deactivated
                            sendDIOCPMCommand(new String(cellPhoneModeControl));
                            data[0] = DLSJposConst.DL_ACK;
                            strResult = "Cell mode mode deactivated";
                            break;
                        case 0x74: // 't'
                            data[0] = DLSJposConst.DL_ACK;
                            strResult = "Cell mode mode deactivated";
                            break;
                    }
                }
                break;
            case DLSJposConst.DIO_PICTURE_TAKING_CONTROL:
                String sInp = ((ByteArrayOutputStream) object).toString();
                picTakingControl[3] = (byte) sInp.charAt(0);
                picTakingControl[4] = (byte) sInp.charAt(1);
                picTakingControl[5] = (byte) sInp.charAt(2);
                picFileName = sInp.substring(3);
                if (picFileName.length() == 0) {
                    picFileName = "temp.jpg";
                }

                nResp = sendDIOPTCCommand(new String(picTakingControl));  // Get information
                strResult = "Picture Taking Control";
                if (nResp != 0) {
                    // do some processing on the returned data
                    switch (nResp) {
                        case DLSJposConst.DL_BEL: // BEL
                            data[0] = DLSJposConst.DL_BEL;
                            strResult = "ERROR: Picture taking not configured";
                            break;
                        case DLSJposConst.DL_ACK: // ACK
                            data[0] = DLSJposConst.DL_ACK;
                            strResult = "OK: Picture saved as " + picFileName;
                            break;
                        case DLSJposConst.DL_NAK: // NAK, internally generated in SO
                            data[0] = DLSJposConst.DL_NAK;
                            strResult = "ERROR: Picture transfer incomplete";
                            break;
                    }
                }
                break;
            case DLSJposConst.DIO_PICTURE_TAKING_CONTROL_9X00:
                String sInput = ((ByteArrayOutputStream) object).toString();
                picTakingCtrl9x00[3] = (byte) sInput.charAt(0);
                picTakingCtrl9x00[4] = (byte) sInput.charAt(1);
                picTakingCtrl9x00[5] = (byte) sInput.charAt(2);
                picTakingCtrl9x00[6] = (byte) sInput.charAt(3);
                picTakingCtrl9x00[7] = (byte) sInput.charAt(4);
                picFileName = sInput.substring(5);
                if (picFileName.isEmpty()) {
                    picFileName = "temp.jpg";
                }
                nResp = sendDIOPTCCommand(new String(picTakingCtrl9x00));
                strResult = "Picture Taking Control (9x00)";
                if (nResp != 0) {
                    switch (nResp) {
                        case DLSJposConst.DL_BEL:
                            data[0] = DLSJposConst.DL_BEL;
                            strResult = "ERROR: Picture taking not configured.";
                            break;
                        case DLSJposConst.DL_ACK:
                            data[0] = DLSJposConst.DL_ACK;
                            strResult = "OK: Picture saved as " + picFileName;
                            break;
                        case DLSJposConst.DL_NAK:
                            data[0] = DLSJposConst.DL_NAK;
                            strResult = "ERROR: Picture transfer incomplete.";
                            break;
                    }
                }
                break;
            case DLSJposConst.DIO_IMAGE_ON_NEXT_TRIGGER:
                picFileName = "Image_";
                sendDIOImageCommand(new String(imagenexttrigger));
                if (emptyBuf) {
                    strResult = ImageFolderFileName;
                    emptyBuf = false;
                } else {
                    strResult = "  Image create in : " + ImageFolderFileName;
                }
                break;
            case DLSJposConst.DIO_IMAGE_ON_NEXT_DECODE:
                picFileName = "Image_";
                sendDIOImageCommand(new String(imageonnextdecode));
                if (emptyBuf) {
                    strResult = ImageFolderFileName;
                    emptyBuf = false;
                } else {
                    strResult = "  Image create in : " + ImageFolderFileName;
                }
                break;
            case DLSJposConst.DIO_IMAGE_CAPTURE_NOW:
                picFileName = "Image_";
                sendDIOImageCommand(new String(imagecaputurenow));
                if (emptyBuf) {
                    strResult = ImageFolderFileName;
                    emptyBuf = false;
                } else {
                    strResult = "  Image create in : " + ImageFolderFileName;
                }
                break;
            case DLSJposConst.DIO_DISABLE_IMAGE_MODE:
                sendDIOImageCommand(new String(exitImageCapture));
                break;
            case DLSJposConst.DIO_TWO_WAY_HOST_RESPONSE:
                sendMsg("@" + object.toString() + "\r");
                strResult = "Response Sent";
                break;
            case DLSJposConst.DIO_DISPLAY_DATA:
                port.sendData(((ByteArrayOutputStream) object).toByteArray());
                data[0] = 0;
                strResult = "Display Data";
                break;
            case DLSJposConst.DIO_GENERAL_CMD:
                String inString = object.toString();
                myResponse = sendGeneralCmd(inString + "\n\r");
                data[0] = DLSJposConst.DL_ACK;
                if (myResponse.length != 0) {
                    strResult = FunctionLib.byteArrayToStringImage(myResponse);
                } else {
                    strResult = "Invalid command";
                }
                break;
            case -7:
                /* Special case to allow access to the DLSScanner instance from a jpos scanner.
                 * This was done to enable CMDFW to actually be able to change the baud rate through sending different HDL files.
                 * This case should NEVER be made public, for internal use only.
                 */
                ((DLSScannerService) object).setScanner(this);
                return;
            default:
                throw new DLSException(DLSJposConst.DLS_E_INVALID_ARG, "Unsupported/Invalid Direct I/O command received for this device");
        }

        if (object instanceof ByteArrayOutputStream) {
            ((ByteArrayOutputStream) object).reset();
            try {
                ((ByteArrayOutputStream) object).write(strResult.getBytes());
            } catch (IOException ex) {
                throw new DLSException(ErrorConstants.APOS_E_FAILURE, "ByteArrayOutputStream object write exception: " + ex.getMessage());
            }
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void disable() throws DLSException {
        if (DLSJposConst.updateInProgress) {
            return;
        }
        if (delayDisable && outputStream.size() > 0) {
            waitForLabel(100);
        }
        sendMsg(DISABLE_CMD);
        setState(DLSState.DISABLED);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void disableBeep() throws DLSException {

    }

    /**
     * {@inheritDoc}
     * Health check is performed synchronously.
     */
    @Override
    public boolean doHealthCheck(int timeout) throws DLSException {
        if (DLSJposConst.updateInProgress) {
            return true;
        }

        boolean bRc = true;
        bStatMessageSent = true;
        byte[] buf = sendReceiveMsg(HEALTH_CMD, timeout);

        if (buf.length != 0) {
            logHealthCheck(buf);
        } else {
            bRc = false;
        }
        bStatMessageSent = false;
        return bRc;
    }


    /**
     * {@inheritDoc}
     * Performed synchronously though a statistics command.
     */
    @Override
    public void doSelfTest() {
        if (DLSJposConst.updateInProgress) {
            return;
        }

        // Only check if valid, as the Magellan will accept this "s" command, but the
        //  hand helds will not.
        if (scannerConfig.getCanAcceptStatisticsCommand()) {
            bStatMessageSent = true;
            byte[] buf = sendReceiveMsg(STATISTICS_CMD);
            if (buf.length == 0) {
                fireDeviceErrorEvent(DeviceErrorStatusListener.ERR_DEVICE_REMOVED);
            }
        }
        bStatMessageSent = false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void enable() throws DLSException {
        if (DLSJposConst.updateInProgress) {
            return;
        }

        sendMsg(ENABLE_CMD);
        setState(DLSState.ENABLED);
    }

    /**
     * Empty method - not supported on this interface.
     */
    @Override
    public void enableBeep() throws DLSException {

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
        //table.put(DLSJposConst.DLS_S_GOOD_SCAN_COUNT, "NA");
        String[] strInfo;
        String str;
        int i;
        bStatMessageSent = true;
        byte[] buf = sendReceiveMsg(INFO_CMD);  // Get information
        if (buf.length == 0) {                            // If nothing returned
            throw new DLSException(DLSJposConst.DLS_E_TIMEOUT, "Error retrieving Information");
        }

        String rawBytes = FunctionLib.byteArrayToString(buf);
        table.put("RawInfo", "# bytes received" + rawBytes);

        strInfo = splitMessage(buf);
        // Ok lets look through the list of strings, find the strings we know about, and
        // dump their values into the HashMap. (see R90-0060.pdf)
        for (i = 0; i < strInfo.length; i++) {
            str = strInfo[i];
            String truncated = str.replaceAll("\\p{Cntrl}", "");
            String truncated2 = truncated.replaceAll("Sp<", "");

            if (truncated2.length() > 1) // no need to put empty fields in the output
            {
                stats.parseInfo(table, truncated2.substring(0, 1), truncated2.substring(1));
            }
        }

        bStatMessageSent = true;
        buf = sendReceiveMsg(HEALTH_CMD);        // Get health
        if (buf.length == 0) {                            // If nothing returned
            throw new DLSException(DLSJposConst.DLS_E_TIMEOUT, "Error retrieving Health");
        }

        rawBytes = FunctionLib.byteArrayToString(buf);
        table.put("RawHealth", "# bytes received" + rawBytes);

        strInfo = splitMessage(buf);
        // Ok lets look through the list of strings, find the strings we know about, and
        //  dump their values into the HashMap.
        for (i = 0; i < strInfo.length; i++) {
            str = strInfo[i];
            String truncated = str.replaceAll("\\p{Cntrl}", "");
            String truncated2 = truncated.replaceAll("Sp<", "");
            if (truncated2.length() > 1) // no need to put empty fields in the output
            {
                stats.parseHealth(table, truncated2.substring(0, 1), truncated2.substring(1));
            }
        }

        bStatMessageSent = true;
        buf = sendReceiveMsg(STATISTICS_CMD);
        if (buf.length == 0) {
            throw new DLSException(DLSJposConst.DLS_E_TIMEOUT, "Error retrieving i-h-s data.");
        }
        rawBytes = FunctionLib.byteArrayToString(buf);
        table.put("RawStats", "# bytes received" + rawBytes);

        strInfo = splitMessage(buf);
        // Ok lets look through the list of strings, find the strings we know about, and
        // dump their values into the HashMap.
        for (i = 0; i < strInfo.length; i++) {
            str = strInfo[i];
            String truncated = str.replaceAll("\\p{Cntrl}", "");
            String truncated2 = truncated.replaceAll("Sp<", "");
            if (truncated2.length() > 1) {
                stats.parseStatistic(table, truncated2.substring(0, 1), truncated2.substring(1));
            }
        }

        if (!table.containsKey(DLSJposConst.DLS_S_INTERFACE)) {
            table.put(DLSJposConst.DLS_S_INTERFACE, "RS232");
        }
        Branding oBr = Branding.getInstance();
        String sName = oBr.getManufacturerShortName();
        table.put(DLSJposConst.DLS_S_MANUFACTURE_NAME, sName);
        table.put(DLSJposConst.DLS_S_DEVICE_CATEGORY, "Scanner");

        bStatMessageSent = false;

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
    public boolean isAlive() {
        boolean result = false;
        bStatMessageSent = true;
        byte[] buf = sendReceiveMsg(HEALTH_CMD, 500);  // Get information
        if (buf.length != 0) {  // If got response
            result = true;
        }
        bStatMessageSent = false;
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDataReceived(byte[] inBuf, int inLen) {
        DLSDeviceInfo oInfo = getDeviceInfo();

        //Something may have gone terribly wrong, if so, log an error and return.
        if (inBuf == null || inBuf.length == 0) {
            return;
        }
        //If we are in firmware update mode then sync to objWaitAck put single character in nResponse
        if (bFirmware) {
            synchronized (objWaitAck) {
                response = inBuf[0];
                bFirmware = false;
                objWaitAck.notifyAll();
                return;
            }
        }

        if (bDIOCPMInProgress) {
            synchronized (objWaitDIOCPMResp) {
                response = inBuf[0];
                bDIOCPMInProgress = false;
                objWaitDIOCPMResp.notifyAll();
                return;
            }
        }

        if (bDIOPTCInProgress) {
            synchronized (objWaitDIOPTCTrans) {
                response = inBuf[0];
                if (response == DLSJposConst.DL_ACK) {
                    bLengthMoreThanResponse = inLen > 1;
                    bDIOPTCStartInProgress = true;
                } else {
                    bPicDone = true;
                    objWaitDIOPTCTrans.notifyAll();
                }
                bDIOPTCInProgress = false;
                if (!bLengthMoreThanResponse) {
                    return;
                }
            }
        }

        if (bDIOPTCStartInProgress) {
            // prepare to swallow the JPEG image coming in... <gulp>
            synchronized (objWaitDIOPTCTrans) {
                byte[] bArr;
                if (bLengthMoreThanResponse) {
                    bArr = new byte[]{inBuf[1], inBuf[2], inBuf[3], inBuf[4]};
                } else {
                    bArr = new byte[]{inBuf[0], inBuf[1], inBuf[2], inBuf[3]};
                }
                nPicFileSize = (long) ByteBuffer.wrap(bArr).getInt();

                if (nPicFileSize != 0) {
                    bDIOPTCTransInProgress = true;
                    if (bLengthMoreThanResponse) {
                        if ((inLen - 5) > 0) {
                            picBuf.write(inBuf, 5, inLen - 5);
                        }
                        bLengthMoreThanResponse = false;
                    } else {
                        picBuf.write(inBuf, 4, inLen - 4);
                    }
                    nPicFileWritten += inLen - 4;
                } else {
                    response = DLSJposConst.DL_NAK;
                    bPicDone = true;
                    objWaitDIOPTCTrans.notifyAll();
                }
                bDIOPTCStartInProgress = false;
                response = DLSJposConst.DL_ACK;
                return;
            }
        }

        if (bDIOPTCTransInProgress) {
            synchronized (objWaitDIOPTCTrans) {
                picBuf.write(inBuf, 0, inLen);
                nPicFileWritten += inLen;
                if (nPicFileWritten >= nPicFileSize) {
                    bDIOPTCTransInProgress = false;
                    bPicDone = true;
                    objWaitDIOPTCTrans.notifyAll();
                }
                return;
            }
        }


        if (bDIOImageInProgress) {
            synchronized (objWaitDIOPTCTrans) {

                String rawBytes = FunctionLib.byteArrayToStringImage(inBuf);
                if (rawBytes.startsWith("$i")) {
                    if (inLen == 17) {
                        bLengthMoreThanResponse = true;
                        String ext = rawBytes.substring(3);
                        if (ext.length() > 1) // no need to put empty fields in the output
                        {
                            switch (ext.charAt(0)) {
                                case '0':
                                    extension = "BMP";
                                    break;
                                case '1':
                                    extension = "JPG";
                                    break;
                            }
                        }

                    } else {
                        bLengthMoreThanResponse = false;
                    }
                    bDIOImageStartInProgress = true;
                } else {
                    bDIOImageInProgress = true;
                    if (!bLengthMoreThanResponse) {
                        return;
                    }

                }
                bDIOImageInProgress = false;
                if (!bLengthMoreThanResponse) {
                    return;
                }
            }
        }


        if (bDIOImageStartInProgress) {

            synchronized (objWaitDIOPTCTrans) {
                if (bLengthMoreThanResponse) {
                    String strTemp = FunctionLib.byteArrayToStringImage(inBuf);
                    String hex = strTemp.substring(8, 12);
                    nPicFileImageSize = Integer.parseInt(hex, 16);

                }
                if (nPicFileImageSize != 0) {
                    bDIOImageTransInProgress = true;
                    bLengthMoreThanResponse = false;
                    nPicFileImageWritten += inLen;
                } else {
                    response = DLSJposConst.DL_NAK;
                    bPicDone = true;
                    objWaitDIOPTCTrans.notifyAll();
                }
                bDIOImageStartInProgress = false;
                response = DLSJposConst.DL_ACK;
                return;
            }
        }

        if (bDIOImageTransInProgress) {
            synchronized (objWaitDIOPTCTrans) {
                picBuf.write(inBuf, 0, inLen);
                nPicFileImageWritten += inLen;
                //  if (nPicFileImageWritten >= nPicFileImageSize) {
                bDIOImageTransInProgress = false;
                bPicDone = true;
                objWaitDIOPTCTrans.notifyAll();
                //    }
                return;
            }
        }

        // if we are in the readConfig mode then we need to process the data differently
        if (bReadConfigInProgress) {
            synchronized (objWaitSohBuf) {
                response = inBuf[0];
                messageList.clear();
                parseReadConfigMessages(inBuf, oInfo.getRxTrailer());
                // JAVAPOS_SWCR_143 - Corrected exception throwing error.
                if (messageList.size() > 0) {
                    sohBuf = (byte[]) messageList.get(0);
                }
                bReadConfigInProgress = false;
                return;
            }
        }

        if (bDIOWriteConfigInProgress) {
            synchronized (objWaitDIOWriteConfigResp) {
                response = inBuf[0];
                bDIOWriteConfigInProgress = false;
                objWaitDIOWriteConfigResp.notifyAll();
                return;
            }
        }

        if (bDIOCommitConfigInProgress) {
            synchronized (objWaitDIOCommitConfigResp) {
                response = inBuf[0];
                bDIOCommitConfigInProgress = false;
                objWaitDIOCommitConfigResp.notifyAll();
                return;
            }
        }

        // For some reason the Status command does not follow convention of having a normal
        //  \r character as a deliniator.  It surrounds its message with a 0x01....0x04.  So
        //  we need a special case here to trap that.

        // If (this is the beginning of a message AND the first char is a SOH), OR
        //  (we already have a SOH and are waiting on an EOT).

        if ((outputStream.size() == 0 && inBuf[0] == 1) || nWaitingOnEot > 0) {
            synchronized (objWaitSohBuf) {
                nWaitingOnEot++;                        // Increment counter
                parseMessages(inBuf, (byte) 0x04);      // Attempt to parse current message
                if (messageList.size() > 0) {               // Did we find the 0x04?
                    nWaitingOnEot = 0;                  // If so, set flag to false
                    sohBuf = (byte[]) messageList.get(0);   // Set buffer to parsed message.
                    messageList.clear();                    // Clear the msg list
                    objWaitSohBuf.notifyAll();          // Wakeup listeners
                } else {
                    // This is a safety switch, if we have come through here too many times, and have not
                    //  found our 0x04, flush all and abort.
                    if (nWaitingOnEot > 30) {           // ID request could have 25+ messages
                        nWaitingOnEot = 0;              // Kill the marker
                        outputStream.reset();                     // Clear the buffer
                        messageList.clear();                // Empty the list
                    }
                }
            }

            return;                                   // Get out.
        } else {
            // Allow the base class to concatenate incomplete messages, and separate
            //  multiple ones.
            parseMessages(inBuf, oInfo.getRxTrailer());
            if (!getDeviceEnabled()) {
                if (!messageList.isEmpty()) {
                    sohBuf = (byte[]) messageList.get(0);   // Set buffer to parsed message.
                }
            }

        }

        // get decode type - standard vs. warhol
        decodeType = oInfo.getDecodingType();

        // Get a list iterator

        for (Object o : messageList) {                          // If any messages in list
            byte[] tmpBuf = (byte[]) o;              // Get buffer of message
            byte pre = oInfo.getRxPrefix();

            if (tmpBuf.length > 2) {                      // Sanity check
                if (pre == tmpBuf[0]) {
                    CMD_POS = 1;            // If prefix character exists
                }
                byte[] identifier = {tmpBuf[CMD_POS], tmpBuf[CMD_POS + 1], tmpBuf[CMD_POS + 2]};
                Pair<LabelIds, Integer> labelsIds = extractBarcodeType(identifier);
                assert labelsIds.first != null;
                int type = labelsIds.first.getUposId();
                assert labelsIds.second != null;
                int identifierLength = labelsIds.second;
                byte[] barcode = extractBarcodeLabel(tmpBuf, identifierLength);

                //Check for addons to modify type.
                //type = LabelParser.addOns(barcode, type); TODO: check if useful

                boolean sendCookedData = options.isSendCookedData();
                // make them both the same
                if (sendCookedData) {
                    tmpBuf = barcode;
                }
                if (getDeviceEnabled()) {
                    fireLabelReceivedEvent(tmpBuf, barcode, type);         // Raise event with label data
                }
                if (delayDisable) {
                    synchronized (labelLock) {
                        labelLock.notifyAll();
                    }
                }
            }
        }
        messageList.clear();                                  // Be sure to clean list after
    }

    /**
     * {@inheritDoc}
     * Performs final tasks following a DIO reset.
     */
    @Override
    public void onDeviceArrival() {
        if (isCOMDevice && alreadyArrived) { //necessary to stop resetWatcher and DevWatcher from both calling onDeviceArrival.
            return;
        }
        try {
            if (getDeviceEnabled()) { // If it was previously enabled
                setState(DLSState.DISABLED);
                enable(); // Enable device.
            } else {
                setState(DLSState.ENABLED);
                disable();
            }
        } catch (DLSException e) {
            Log.e(Tag, "onDeviceArrival: Device reattached exception: ", e);
        }
        fireDeviceStatusEvent(CommonsConstants.SUE_POWER_ONLINE);
        if (hotPlugged) {
            if (m_hotPlugWatcher != null)
                m_hotPlugWatcher.cancel(false);
        }
        synchronized (resetObj) {
            alreadyArrived = true;
            resetSuccess = true;
            hotPlugged = true;
            resetObj.notifyAll();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDeviceReattached() {
        boolean portOpened = port.openPort();

        /* JAZZ_65514 - Added retry for opening port on suspended
         * devices on Windows 10.
         */
        if (!portOpened) {
            try {
                Thread.sleep(options.getRetryWaitTime());
            } catch (InterruptedException ie) {
                Log.e(Tag, "onDeviceReattached: sleep interrupted, ", ie);
            }
            portOpened = port.openPort();
        }

        if (portOpened) {
            alreadyArrived = false;
            isCOMDevice = true;
            if (hotPlugged) {
                m_hotPlugWatcher = m_scheduler.scheduleAtFixedRate(new ResetWatcherRunnable(), 3000, 1000, TimeUnit.MILLISECONDS);
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
        fireDeviceErrorEvent(DeviceErrorStatusListener.ERR_DEVICE_REMOVED);
        if (!port.closePort()) {
            Log.w(Tag, "onDeviceRemoved: Could not close port");
        }
    }

    /**
     * Opens port for communication.
     */
    public void openPort() {
        port.openPort();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void open(String logicalName, Context context) throws DLSException {
        super.open(logicalName, context);
        options = DLSProperties.getInstance(context);
        delayDisable = options.isDelayDisable();
    }

    /**
     * Parses serial messages at the specified character separator and adds them
     * to the message list.
     *
     * @param inBuf   byte array containing the concatenated messages
     * @param endChar byte indicating the character to parse the messages on
     */
    protected void parseReadConfigMessages(byte[] inBuf, byte endChar) {
        for (byte b : inBuf) {
            outputStream.write(b);
            if (b == endChar) {         // If this is the end character
                byte[] tmpBuf = outputStream.toByteArray();       // Extract buffer
                // Add message to list
                messageList.add(tmpBuf);
                outputStream.reset();                             // Reset buffer.
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DLSScannerConfig reportConfiguration() throws DLSException {
        return scannerConfig;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void reset() throws DLSException {
        if (DLSJposConst.updateInProgress) {
            return;
        }
        sendMsg(RESET_CMD);
        /*
         * Experimental patch to see if explicitly calling onDeviceRemoved for
         * Linux clears up the port not properly closing.
         */
        //onDeviceRemoved();
    }

    /**
     * Sends configuration command to the scanner. Synchronously waits for
     * response.
     *
     * @param commitCfgCmd String containing command to send
     * @return int indicating the command response from the scanner
     */
    public int sendCommitConfigItems(String commitCfgCmd) {
        int resp;
        long timeout = (long) (DEFAULT_TIMEOUT);
        long elapsed = 0;
        long start;

        synchronized (objWaitDIOCommitConfigResp) {
            bDIOCommitConfigInProgress = true;
            response = DLSJposConst.DL_NRESP;
            sendMsg(commitCfgCmd + "\r");
            start = System.nanoTime();
            try {
                while (bDIOCommitConfigInProgress) {
                    objWaitDIOCommitConfigResp.wait(timeout - elapsed);
                    elapsed = (long) ((System.nanoTime() - start) / 1000000);
                    if (elapsed >= timeout) {
                        break;
                    }
                }
            } catch (InterruptedException ie) {
                Log.e(Tag, "sendCommitConfigItems: Interrupted exception, ", ie);
            }
            resp = response;
        }
        return resp;
    }


    /**
     * Sends a Direct I/O Cell Phone Mode command. Synchronously waits for
     * response.
     *
     * @param sDIOCmnd String containing the DIO Cell Phone Mode command to send
     * @return int indicating the command response from the scanner
     */
    public int sendDIOCPMCommand(String sDIOCmnd) {
        int resp;
        long timeout = DEFAULT_TIMEOUT;
        long elapsed = 0;
        long start;

        synchronized (objWaitDIOCPMResp) {
            bDIOCPMInProgress = true;
            response = DLSJposConst.DL_NRESP;
            sendMsg(sDIOCmnd);
            start = System.nanoTime();
            try {
                while (bDIOCPMInProgress) {
                    objWaitDIOCPMResp.wait(timeout - elapsed);
                    elapsed = (long) ((System.nanoTime() - start) / 1000000);
                    if (elapsed >= timeout) {
                        break;
                    }
                }
            } catch (InterruptedException ie) {
                Log.e(Tag, "sendDIOPMCommand: Interrupted exception, ", ie);
            }
            resp = response;
        }
        return resp;
    }


    /**
     * Sends a Direct I/O Image command. Synchronously waits for
     * response.
     *
     * @param sDIOCmnd String containing the DIO Image command to send
     * @return int indicating the command response from the scanner
     */
    public int sendDIOImageCommand(String sDIOCmnd) {
//        String tempDir = options.getTempDir();TODO: not wise to share files at this level.
        String tempDir = "DUMMY";
        long maxFileSize = 500000;

        bDIOImageInProgress = true;
        bDIOImageTransInProgress = false;
        bDIOImageStartInProgress = false;
        picBuf = new ByteArrayOutputStream(40000);
        // calculate timeout delay based on baud rate and max pic size 250K
//        nBaudDelay = (250000 / (deviceInfo.getBaud() / 10)) * 1000;

        nBaudDelay = (maxFileSize / (9600 / 10)) * 1000;
        int resp;
        long timeout = 10000L;
        long elapsed = 0;
        long start;

        synchronized (objWaitDIOPTCTrans) {
            nPicFileImageWritten = 0;
            response = DLSJposConst.DL_NRESP;
            bPicDone = false;
            sendMsg(sDIOCmnd);
            start = System.nanoTime();
            try {
                while (!bPicDone) {
                    objWaitDIOPTCTrans.wait(timeout - elapsed);
                    elapsed = (long) ((System.nanoTime() - start) / 1000000);
                    if (elapsed >= timeout) {
                        break;
                    }
                }
                if (nPicFileImageWritten < nPicFileImageSize) {

                    response = DLSJposConst.DL_NAK; // NAK
                }
                try {
                    picBuf.flush();
                    // Output an error blurb
                    // JAVAPOS_SWCR_278 - Changed windows detection method
                    /* JAVAPOS_SWCR_213 (DLSLogManager Removal) - Changed the way the
                     * the temp directory is identified, thus removing the dependence on
                     * the the version of windows being used for picFile path (6.0=Vista).
                     */
                    //if (java.io.File.separatorChar == '\\' && (log.dVer >= 6.0)) {
                    //picFile = new FileOutputStream(tempDir + picFileName + imageCount + "." + extension, false);
                    ImageFolderFileName = tempDir + picFileName + imageCount + "." + extension;

                    //} else {
                    //    picFile = new FileOutputStream(picFileName + imageCount + "." + extension, false);
                    //    ImageFolderFileName = picFileName + imageCount + "." + extension;
                    //}
                    imageCount++;

                    if (picBuf.size() == 0) {
                        emptyBuf = true;
                        ImageFolderFileName = "    image not created";
                        sendMsg(new String(exitImageCapture));
                    } else {//TODO: who is going to save the picture? where to save it??

//                        String imageDataString = Base64.encodeToString(picBuf.toByteArray(), Base64.DEFAULT);
//                        byte[] imageByteArray = Base64.decode(imageDataString, Base64.DEFAULT);
//                        try ( // Write a image byte array into file system
//                              FileOutputStream imageOutFile = picFile) {
//                            imageOutFile.write(imageByteArray);
//                            //      imageInFile.close();
//                        }
                    }
                } catch (IOException e) {
                    Log.e(Tag, "sendDIOImageCommand exception: ", e);
                }
            } catch (InterruptedException ie2) {
                Log.e(Tag, "Interrupt exception ", ie2);
            }
            resp = response;
        }
        return resp;
    }


    /**
     * Sends a Direct I/O Picture Taking command. Synchronously waits for
     * response.
     *
     * @param sDIOCmnd String containing the DIO Picture Taking command to send
     * @return int indicating the command response from the scanner
     */
    public int sendDIOPTCCommand(String sDIOCmnd) {
        DLSDeviceInfo oInfo = getDeviceInfo();
//        String tempDir = options.getTempDir();TODO: not wise to share files at this level.
        String tempDir = "DUMMY";
        long maxFileSize = 500000;

        char resType;

        bDIOPTCInProgress = true;
        bDIOPTCTransInProgress = false;
        bDIOPTCStartInProgress = false;
        picBuf = new ByteArrayOutputStream();
        // calculate timeout delay based on baud rate and max pic size
        resType = sDIOCmnd.charAt(3);
        switch (resType) {
            case '0':
                maxFileSize = 250000;
                break;
            case '1':
                maxFileSize = 400000;
                break;
            case '2':
                maxFileSize = 900000;
                break;
            case '3':
                maxFileSize = 70000;
                break;
            case 'S':
                //This needs to pull the value from scanner configuration via DIO.
                maxFileSize = 500000;
                break;
            //This needs to pull the value from scanner configuration by default.
            default:
                break;
        }
        nBaudDelay = (maxFileSize / (oInfo.getBaudRate() / 10)) * 1000;

        int resp;
        long timeout = nBaudDelay;
        long elapsed = 0;
        long start;
        synchronized (objWaitDIOPTCTrans) {
            nPicFileWritten = 0;
            response = DLSJposConst.DL_NRESP;
            bPicDone = false;
            sendMsg(sDIOCmnd);
            start = System.nanoTime();
            try {
                while (!bPicDone) {
                    objWaitDIOPTCTrans.wait(timeout - elapsed);
                    elapsed = (long) ((System.nanoTime() - start) / 1000000);
                    if (elapsed >= timeout) {
                        break;
                    }
                }
                if (nPicFileWritten < nPicFileSize) {
                    response = DLSJposConst.DL_NAK; // NAK
                }
                try {
                    picBuf.flush();
                    //TODO: who is going to use the image?? It is not wise to share files at library level.
//                    picFile = new FileOutputStream(tempDir + picFileName, false);
//                    picFile.write(picBuf.toByteArray(), 0, picBuf.size());
//                    picFile.flush();
//                    picFile.close();
                } catch (IOException e) {
                    //System.out.println(e);       // Output an error blurb
                    Log.e(Tag, "sendDIOPTCCommand Exception: ", e);
                }
            } catch (InterruptedException ie2) {
                Log.e(Tag, "Interrupt exception ", ie2);
            }
            resp = response;
        }
        return resp;
    }


    /**
     * Sends a general Direct I/O command. Synchronously waits for response.
     *
     * @param sCmd String containing the DIO command to send
     * @return byte array containing the response data. Array will be empty
     * if no response is received
     */
    public byte[] sendGeneralCmd(String sCmd) {
        bReadConfigInProgress = true;
        // bDIOCPMInProgress = true;
        byte[] myResponse;
        // transmit message
        myResponse = sendReceiveMsg(sCmd, 500);
        return myResponse;
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
     * {@inheritDoc}
     */
    protected int sendMsg(String msg) {
        return port.sendData(msg);
    }

    /**
     * Sends a Direct I/O Read Configuration Item command to the scanner.
     *
     * @param readCfgCmd String containing the Read Configuration command
     * @param data       not used
     * @param object     ByteArrayOutputStream instance containing the configuration
     *                   item to read
     * @return byte array containing the item data. Array will be empty
     * if no response is received
     */
    public byte[] sendReadConfigCommand(String readCfgCmd, int[] data, Object object) {
        bReadConfigInProgress = true;
        // build message
        ByteArrayOutputStream bsReturn = (ByteArrayOutputStream) object;
        String configItem = bsReturn.toString();
        String outCommand = readCfgCmd + configItem + "\r";
        byte[] myResponse;
        // transmit message

        response = DLSJposConst.DL_NRESP;
        myResponse = sendReceiveMsg(outCommand, 100);

        // JAVAPOS_SWCR_143 - Corrected some of what was wrong with this.
        // More correction will be needed, but this should fix it for now.
        if (myResponse.length == 0) {
            myResponse = new byte[1];
            myResponse[0] = (byte) response;
        }

        // JAVAPOS_SWCR_143 - Please leave this code in and commented out.
        // It will be used for a future, more permanent fix.
        /*
        myResponse = new byte[1];
        PortResponse oResp = new PortResponse();
        boolean bDidit = port.SendCommandResponse(outCommand, oResp, 100);
        if (bDidit) {
        myResponse[0] = (byte) oResp.FirstByte;
        }
         *
         */
        return myResponse;
    }

    /**
     * Sends a command to the scanner, synchronously waiting for a response.
     * A default timeout will be used.
     *
     * @param msg String containing command to send
     * @return byte array containing the scanner response. Array will be empty
     * if timeout is reached
     */
    protected byte[] sendReceiveMsg(String msg) {
        return sendReceiveMsg(msg, DEFAULT_TIMEOUT);
    }

    /**
     * Sends a command to the scanner, synchronously waiting for a response.
     *
     * @param msg     String containing command to send
     * @param timeout int indicating the number of milliseconds to wait for a
     *                response
     * @return byte array containing the scanner response. Array will be empty
     * if timeout is reached
     */
    protected byte[] sendReceiveMsg(String msg, int timeout) {
        byte[] response;
        long elapsed = 0;
        long start;
        synchronized (objWaitSohBuf) {
            sohBuf = EMPTY_BYTE_ARRAY;
            sendMsg(msg);
            start = System.nanoTime();
            try {
                while (sohBuf.length == 0) {
                    objWaitSohBuf.wait(timeout - elapsed);
                    elapsed = (System.nanoTime() - start) / 1000000;
                    if (elapsed >= timeout) {
                        break;
                    }
                }
            } catch (InterruptedException ie) {
                Log.e(Tag, "sendReceiveMsg: Interrupted exception, ", ie);
            }
            response = Arrays.copyOf(sohBuf, sohBuf.length);
        }
        return response;
    }

    /**
     * Sends a firmware record to the scanner.
     *
     * @param record String containing the record to send.
     * @return int indicating the command response from the scanner
     */
    @Override
    public int sendRecord(String record) {
        int resp;
        long timeout = getRecordTimeout();
        long elapsed = 0;
        long start;

        synchronized (objWaitAck) {
            bFirmware = true;
            response = DLSJposConst.DL_NRESP;
            sendMsg(record);
            start = System.nanoTime();
            try {
                while (bFirmware) {
                    objWaitAck.wait(timeout - elapsed);
                    elapsed = (System.nanoTime() - start) / 1000000;
                    if (elapsed >= timeout) {
                        break;
                    }
                }
            } catch (InterruptedException ie) {
                Log.e(Tag, "sendRecord: Interrupted exception, ", ie);
            }
            resp = response;
        }
        return resp;
    }

    /**
     * Sends a Direct I/O Write Configuration Item command to the scanner.
     *
     * @param commitCfgCmd String containing the Write Configuration command
     * @param data         not used
     * @param object       ByteArrayOutputStream instance containing the configuration
     *                     item and data to write
     * @return int indicating the command response from the scanner
     */
    public int sendWriteConfigCommand(String commitCfgCmd, int[] data, Object object) {
        String outCommand;
        // JAVPOS_SWCR_144 - Fixed numerous errors in this method including
        // the wrong substring length for itemComponent, the wrong substring
        // length for dataComponent and the fact that dataComponent was never
        // added to the outCommand if there was no "leading 0's" fix.
        String item_data = "";
        if (object instanceof ByteArrayOutputStream) {
            ByteArrayOutputStream oStream = (ByteArrayOutputStream) object;
            item_data = oStream.toString();
        }
        String itemComponent = item_data.substring(0, 4);
        String dataComponent = item_data.substring(4);
        // strip off the leading 0's
        if (dataComponent.charAt(0) == '0' && dataComponent.charAt(1) == '0') {
            outCommand = commitCfgCmd + itemComponent + dataComponent.substring(2) + "\r";
        } else {
            outCommand = commitCfgCmd + itemComponent + dataComponent + "\r";
        }

        int resp;
        long timeout = DEFAULT_TIMEOUT;
        long elapsed = 0;
        long start;

        synchronized (objWaitDIOWriteConfigResp) {
            bDIOWriteConfigInProgress = true;
            response = DLSJposConst.DL_NRESP;
            sendMsg(outCommand);
            start = System.nanoTime();
            try {
                while (bDIOWriteConfigInProgress) {
                    objWaitDIOWriteConfigResp.wait(timeout - elapsed);
                    elapsed = (long) ((System.nanoTime() - start) / 1000000);
                    if (elapsed >= timeout) {
                        break;
                    }
                }
            } catch (InterruptedException ie) {
                Log.e(Tag, "sendWriteConfigCommand: Interrupted exception, ", ie);
            }
            resp = response;
        }
        return resp;
    }

    /**
     * Empty method - not supported on this interface.
     */
    @Override
    public void updateConfiguration() throws DLSException {

    }

    private void waitForLabel(int timeout) {
        long elapsed = 0;
        long start;
        synchronized (labelLock) {
            resetSuccess = false;
            start = System.nanoTime();
            try {
                while (outputStream.size() > 0) {
                    labelLock.wait(timeout - elapsed);
                    elapsed = (long) ((System.nanoTime() - start) / 1000000);
                    if (elapsed >= timeout) {
                        break;
                    }
                }
            } catch (InterruptedException ie) {
                Log.e(Tag, "waitforLabel: Interrupted exception, ", ie);
            }
        }
    }

    /**
     * This thread is used to automatically re-enable the scanner for use after
     * a DIO reset.
     */
    private class ResetWatcherRunnable implements Runnable {
        private int passTotal = 0;
        private final int passNeeded = 3;
        private int failures = 0;

        public boolean checkHealth() {
            boolean bRc = false;
            bStatMessageSent = true; //Needed for 9xxx scanners response recieved thread notification
            if (port.isOpen()) {
                byte[] buf = sendReceiveMsg(HEALTH_CMD, 500); //500 milliseconds for resetWatcher
                bStatMessageSent = false;
                if (buf.length != 0) {
                    bRc = true;
                } else {
                    /* JAZZ_91870 - Periodically clear the buffer when we are in reset condition.
                     * This should stop erroneous bytes/messages from the scanner causing the
                     * logic to fail.
                     */
                    if (++failures % 5 == 0 && outputStream.size() > 0) {
                        outputStream.reset();
                    }
                }
            }

            return bRc;
        }


        @Override
        public void run() {
            if (scannerConfig.getHostCommandsDisabled()) { //Two-Way communication requires host commands to be off.
                fireDeviceStatusEvent(CommonsConstants.SUE_POWER_ONLINE);
                m_resetWatcher.cancel(false);
                return;
            }
            boolean deviceReady = checkHealth();

            if (deviceReady) {
                passTotal++;
                if (passTotal >= passNeeded) {
                    onDeviceArrival();
                }
            } else {
                passTotal = 0;
            }
        }
    }
}
