package com.datalogic.dlapos.androidpos.service;

import android.content.Context;
import android.util.Log;

import com.datalogic.dlapos.androidpos.common.Branding;
import com.datalogic.dlapos.androidpos.common.DLSDeviceInfo;
import com.datalogic.dlapos.androidpos.common.DLSException;
import com.datalogic.dlapos.androidpos.common.DLSJposConst;
import com.datalogic.dlapos.androidpos.common.DLSObjectFactory;
import com.datalogic.dlapos.androidpos.common.DLSProperties;
import com.datalogic.dlapos.androidpos.common.DLSScannerConfig;
import com.datalogic.dlapos.androidpos.common.DLSState;
import com.datalogic.dlapos.androidpos.common.ErrorData;
import com.datalogic.dlapos.androidpos.common.EventData;
import com.datalogic.dlapos.androidpos.common.ExtendedChannelInterpretation;
import com.datalogic.dlapos.androidpos.common.ItemData;
import com.datalogic.dlapos.androidpos.common.LabelData;
import com.datalogic.dlapos.androidpos.interpretation.DLSScanner;
import com.datalogic.dlapos.androidpos.interpretation.DLSUSBFlash;
import com.datalogic.dlapos.androidpos.interpretation.LabelReceivedListener;
import com.datalogic.dlapos.commons.constant.CommonsConstants;
import com.datalogic.dlapos.commons.constant.ErrorConstants;
import com.datalogic.dlapos.commons.constant.ScannerConstants;
import com.datalogic.dlapos.commons.event.DataEvent;
import com.datalogic.dlapos.commons.event.ErrorEvent;
import com.datalogic.dlapos.commons.event.EventCallback;
import com.datalogic.dlapos.commons.event.StatusUpdateEvent;
import com.datalogic.dlapos.commons.service.ScannerBaseService;
import com.datalogic.dlapos.commons.support.APosException;
import com.datalogic.dlapos.commons.upos.RequestListener;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Extension of DLSBaseService which encapsulates the service object for
 * Scanners.
 */
public class DLSScannerService extends DLSBaseService implements ScannerBaseService, LabelReceivedListener {

    private final static String TAG = DLSScannerService.class.getSimpleName();

    private final Object eventListLock = new Object();

    ItemData currentItemData;
    LabelData currentLabelData;
    static String DLS_USB_FLASH = "DLS-USB-Flash";
    //private SystemData currentSystemData;
    LinkedList<EventData> listData;
    private DLSScanner scanner;
    private ExtendedChannelInterpretation eci;
    private boolean decodeData;
    private boolean waitOnScanner = true;
    boolean updateFirmwareStatus = false;
    boolean isUsbScanner = false;
    DLSUSBFlash flash = null;
    private DLSDeviceInfo flashDeviceInfo = new DLSDeviceInfo();

    /**
     * Constructor
     */
    public DLSScannerService() {
        super();
        this.powerState = CommonsConstants.PS_UNKNOWN;
    }

    //region Getters

    /**
     * Function to get the device state.
     *
     * @return the device state.
     */
    public int getDeviceState() {
        return this.deviceState;
    }

    /**
     * Return a byte array containing the Item data from a scanned data label in
     * raw form.
     *
     * @return byte array containing the item data.
     * @throws APosException thrown if the device is not open.
     */
    public byte[] getItemData() throws APosException {
        checkOpened();
        return getCurrentItemData() != null ? getCurrentItemData().getRawItemData() : new byte[0];
    }

    /**
     * Return the instance of DLSScanner owned and managed by a DLSScannerService.
     *
     * @return DLSScanner instance belonging to the service.
     */
    public DLSScanner getScanner() {
        return this.scanner;
    }


    /*    */

    /**
     * Return a byte array containing the system data in a raw form.
     *
     * @return byte array containing system data.
     * @throws APosException thrown if the device is not open.
     *//*
    public byte[] getSystemData() throws APosException {
        checkClosed();
        return currentSystemData != null ? currentSystemData.getRawSystemData() : new byte[0];
    }*/
    public ItemData getCurrentItemData() {
        return this.currentItemData;
    }

    /**
     * Function to get the current label data.
     *
     * @return the current label data.
     */
    public LabelData getCurrentLabelData() {
        return this.currentLabelData;
    }
    //endregion

    //region Setters

    /**
     * Function to set the current label data.
     *
     * @param labelData the desired label data.
     */
    protected void setCurrentLabelData(LabelData labelData) {
        this.currentLabelData = labelData;
    }

    /**
     * Function to set the device state.
     *
     * @param deviceState the desired device state.
     */
    protected void setDeviceState(int deviceState) {
        this.deviceState = deviceState;
    }

    /**
     * Function to set the scanner managed by the service.
     *
     * @param scanner the scanner managed by the service.
     */
    public void setScanner(DLSScanner scanner) {
        this.scanner = scanner;
    }

    //endregion

    //region Support functions

    /**
     * Function to add a check digit to label data.
     *
     * @param labelData data to use to create the check digit.
     * @return the labelData with the check digit at the end.
     */
    protected byte[] addCheckDigit(byte[] labelData) {
        int mult;
        int sum = 0;
        for (int i = labelData.length - 1; i >= 0; i--) {
            mult = ((i % 2) != (labelData.length % 2)) ? 3 : 1;
            sum += ((labelData[i] - '0') * mult);
        }
        sum %= 10;
        if (sum != 0) {
            sum = 10 - sum;
        }

        byte[] newData = new byte[labelData.length + 1];
        System.arraycopy(labelData, 0, newData, 0, labelData.length);
        newData[labelData.length] = (byte) (sum + '0');
        return newData;
    }

    private void doAutoDisable() throws APosException {
        if (getAutoDisable()) {
            final Thread currentThread = Thread.currentThread();
            final String oldName = currentThread.getName();
            currentThread.setName("AutoDisable-" + oldName);
            try {
                setDeviceEnabled(false);
            } finally {
                currentThread.setName(oldName);
            }
        }
    }

    /**
     * Enqueue an error event, as these events must follow the same path as a
     * data event, and only get raised when DataEventEnabled is true, etc.
     * Also when adding an error event to the queue, if there is already
     * data events available, then two error events must be enqueued, one
     * at the top of the stack, and one at the bottom.
     *
     * @param code     int indicating the error code.
     * @param exCode   int indicating an error code.
     * @param locus    int indicating the Locus code.
     * @param response int indicating the response.
     */
    public void enqueueError(int code, int exCode, int locus, int response) {
        if (device == null || device.getState() == DLSState.CLOSED)
            return;
        synchronized (eventListLock) {
            ErrorData err;
            if (listData.size() > 0 && locus != ErrorConstants.APOS_EL_OUTPUT) {
                boolean bdata = false;
                for (EventData listDatum : listData) {
                    if (listDatum.getEventType() == EventData.LABEL_EVENT) {
                        bdata = true;
                        break;
                    }
                }

                if (bdata) {
                    err = new ErrorData(code, exCode, ErrorConstants.APOS_EL_INPUT_DATA,
                            ErrorConstants.APOS_ER_CONTINUEINPUT);
                    listData.addFirst(err);
                }
            }

            err = new ErrorData(code, exCode, locus, response);
            listData.add(err);
        }
        try {
            sendDataEvent();
        } catch (APosException e) {
            Log.e(TAG, "Enqueing error: ", e);
        }
    }

    /**
     * Expand a UPCE code to UPCA
     *
     * @param labelData byte array containing the UPCE label data.
     * @return byte array containing the expanded UPCA data.
     */
    protected byte[] expandUPCE(byte[] labelData) {
        byte[] newLabel = new byte[11];
        switch (labelData[6]) {
            // 0 ABF00 00CDE X
            case '0':
            case '1':
            case '2':
                newLabel[0] = '0';
                newLabel[1] = labelData[1];
                newLabel[2] = labelData[2];
                newLabel[3] = labelData[6];
                newLabel[4] = '0';
                newLabel[5] = '0';
                newLabel[6] = '0';
                newLabel[7] = '0';
                newLabel[8] = labelData[3];
                newLabel[9] = labelData[4];
                newLabel[10] = labelData[5];
                break;

            // 0 ABC00 000DE X
            case '3':
                newLabel[0] = '0';
                newLabel[1] = labelData[1];
                newLabel[2] = labelData[2];
                newLabel[3] = labelData[3];
                newLabel[4] = '0';
                newLabel[5] = '0';
                newLabel[6] = '0';
                newLabel[7] = '0';
                newLabel[8] = '0';
                newLabel[9] = labelData[4];
                newLabel[10] = labelData[5];
                break;

            // 0 ABCD0 0000E X
            case '4':
                newLabel[0] = '0';
                newLabel[1] = labelData[1];
                newLabel[2] = labelData[2];
                newLabel[3] = labelData[3];
                newLabel[4] = labelData[4];
                newLabel[5] = '0';
                newLabel[6] = '0';
                newLabel[7] = '0';
                newLabel[8] = '0';
                newLabel[9] = '0';
                newLabel[10] = labelData[5];
                break;

            // 0 ABCDE 0000F X
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
                newLabel[0] = '0';
                newLabel[1] = labelData[1];
                newLabel[2] = labelData[2];
                newLabel[3] = labelData[3];
                newLabel[4] = labelData[4];
                newLabel[5] = labelData[5];
                newLabel[6] = '0';
                newLabel[7] = '0';
                newLabel[8] = '0';
                newLabel[9] = '0';
                newLabel[10] = labelData[6];
                break;
        }
        return newLabel;
    }

    byte[] validateLabel(byte[] rawLabel, byte[] decodedLabel, int nType) {
        byte[] result = decodedLabel;
        switch (nType) {
            case ScannerConstants.SCAN_SDT_UPCE:
                if (decodedLabel.length < 8) {
                    // JAVAPOS_SWCR_158 - Correction for UPCE codes that are
                    // less than a total length of 7 (includes prefix 0).
                    if (decodedLabel.length < 7) {
                        break;
                    }
                    byte[] newLabel = expandUPCE(decodedLabel);
                    newLabel = addCheckDigit(newLabel);
                    byte[] upceLabel = new byte[8];
                    System.arraycopy(decodedLabel, 0, upceLabel, 0, 7);
                    upceLabel[7] = newLabel[11];
                    result = upceLabel;
                }
                break;
            case ScannerConstants.SCAN_SDT_UPCA:
                if (decodedLabel.length < 12) {
                    result = addCheckDigit(decodedLabel);
                }
                break;
            case ScannerConstants.SCAN_SDT_EAN8:
                if (decodedLabel.length < 8) {
                    result = addCheckDigit(decodedLabel);
                }
                break;
            case ScannerConstants.SCAN_SDT_EAN13:
                if (decodedLabel.length < 13) {
                    // JAVAPOS_SWCR_308 - Skip adding a checksum when the first
                    // byte of the raw label is 0x6E, indicating ISSN or
                    // 0x40, indicating ISBN.
                    if (rawLabel[0] != 0x6E && rawLabel[0] != 0x40 && rawLabel[0] != 0x49) {
                        result = addCheckDigit(decodedLabel);
                    }
                }
                break;
        }

        return result;
    }

    /**
     * Release flash device when finished with firmware update.
     */
    protected void releaseFlash() {
        if (flash != null) {
            try {
                flash.release();
            } catch (DLSException re) {
                //TODO:Handle exception
            }
        }
    }

    //endregion

    //region Checkers

    /**
     * Function to check if the scanner is set.
     *
     * @throws APosException if the scanner is not set (error code {@link ErrorConstants#APOS_E_FAILURE APOS_E_FAILURE}).
     */
    protected void checkScanner() throws APosException {
        if (scanner == null) {
            throw new APosException("Scanner instance is null.", ErrorConstants.APOS_E_FAILURE);
        }
    }
    //endregion

    //region Methods

    /**
     * {@inheritDoc}
     */
    @Override
    public void clearInput() throws APosException {
        super.clearInput();
        setDeviceState(CommonsConstants.S_IDLE);
        listData.clear();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clearInputProperties() throws APosException {
        checkClaimed();
        setCurrentLabelData(null);
        setDeviceState(CommonsConstants.S_IDLE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void compareFirmwareVersion(String filename, int[] result) throws APosException {
        checkClaimed();
        checkEnabled();
        if (!scanner.getCanCompareFirmwareVersion()) {
            throw new APosException("Firmware comparison not available for this device", ErrorConstants.APOS_E_ILLEGAL);
        }
        if (!scanner.hasStatisticsReporting()) {
            throw new APosException("Statistical Reporting Capability not available for this device.", ErrorConstants.APOS_E_ILLEGAL);
        }

        File file = new File(filename);
        String fileName = file.getName();

        if (result == null || result.length < 5) {
            result = new int[5];
        }

        String scanECVersion;

        result[0] = CommonsConstants.CVF_FIRMWARE_UNKNOWN;
        String[] temp = {""};
        retrieveStatistics(temp);
        if (statistics == null || statistics.isEmpty() || scanner.getDeviceInfo().is8xxx()) {
            throw new APosException(
                    "Failed to compare (No statistical data returned). Does the device support i-h-s data?", ErrorConstants.APOS_E_ILLEGAL);
        }

        verifyFirmwareFileName(fileName);

        //BufferedReader reader = new BufferedReader(new InputStreamReader(fileContent));
        String header;
        try {
            header = getFirmwareHeader(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            throw new APosException("Can not find the file.", ErrorConstants.APOS_E_FAILURE);
        }
        int fileVendorID = getFileVID(header);
        int fileProductID = getFilePID(header);
        int nFileECVer = getFileECVersion(header);

        if (DLSJposConst.updateInProgress || (getState() == CommonsConstants.S_BUSY)) {
            throw new APosException("Device is busy", ErrorConstants.APOS_E_BUSY);
        }

        scanECVersion = (String) statistics.get(DLSJposConst.DLS_S_SCANNER_REVISION);

        if (scanECVersion == null) {
            scanECVersion = (String) statistics.get(DLSJposConst.DLS_S_APPLICATION_REVISION);
        }
        //The retrieval of the scanner i-h-s data may have failed.
        if (scanECVersion == null || scanECVersion.isEmpty()) {
            result[0] = CommonsConstants.CVF_FIRMWARE_UNKNOWN;
        } else {
            int nScanECVer;
            try {
                nScanECVer = Integer.decode("0x" + scanECVersion);
            } catch (NumberFormatException nfe) {
                nScanECVer = 0;
            }

            if (nFileECVer == nScanECVer) {
                result[0] = CommonsConstants.CVF_FIRMWARE_SAME;
            } else if (nFileECVer > nScanECVer) {
                result[0] = CommonsConstants.CVF_FIRMWARE_NEWER;
            } else if (nFileECVer < nScanECVer) {
                result[0] = CommonsConstants.CVF_FIRMWARE_OLDER;
            } else if (nFileECVer == 0 || nScanECVer == 0) {
                result[0] = CommonsConstants.CVF_FIRMWARE_DIFFERENT;
            }

            if (isUsbScanner) {
                int vid = flashDeviceInfo.getVendorId();
                int pid = flashDeviceInfo.getProductId();
                if (vid != fileVendorID) {
                    result[0] = DLSJposConst.DLS_CFV_INVALID_VID;
                    result[1] = vid;
                    result[2] = fileVendorID;
                }

                if (pid != fileProductID) {
                    result[0] = DLSJposConst.DLS_CFV_INVALID_PID;
                    result[1] = pid;
                    result[2] = fileProductID;
                }
            }
            result[3] = nFileECVer;
            result[4] = nScanECVer;
        }
    }

    protected void doUpdate(File file) throws FileNotFoundException {
        int sentRecords = 0;
        boolean isDAT = file.getName().endsWith(".DAT");
        double percentual;
        int maxRetries = options.getRecordRetry();
        int retry = 0;
        int restart = 0;
        int type = 0; // srecord type
        int ndxS = 1;  // index to record type
        int resp;
        int xPerc = 0;
        int fileRecords = countFileRecords(new FileInputStream(file));
        int timeout = device.getDeviceInfo().getHDLRecordTimeout();
        scanner.setRecordTimeout(timeout);
        if (isUsbScanner) {
            flash.setRecordTimeout(timeout);
        }
        onDeviceStatus(CommonsConstants.SUE_UF_PROGRESS); // status notify (1%)

        // first send a firmware update command
        if (isUsbScanner) {
            resp = flash.startCommand();
            if (resp != DLSJposConst.DL_ACK) {
                DLSJposConst.updateInProgress = false;
                releaseFlash();
                deviceState = CommonsConstants.S_IDLE;
                onDeviceStatus(CommonsConstants.SUE_UF_FAILED_DEV_OK);
                return;
            }
        }
        InputStreamReader stream = new InputStreamReader(new FileInputStream(file), DLSJposConst.HDL_CHARSET);
        BufferedReader reader = new BufferedReader(stream);
        try {
            String line = reader.readLine();
            if (line != null && isDAT) {
                line = line.substring(8); // remove vendorid,productid,ecc
            }

            while (line != null) {
                if (line.length() < 10)
                    break;

                line += '\r'; // need to restore cr
                if (isDAT)
                    line += '\n'; // need to add line feed
                retry = 0;
                resp = DLSJposConst.DL_NAK;
                try {
                    type = Integer.parseInt(line.substring(ndxS, ndxS + 1));
                } catch (NumberFormatException e) {
                    Log.e(TAG, "doUpdate: Error parsing type from record.", e);
                }

                while (retry <= maxRetries) {
                    if (isUsbScanner) {
                        resp = flash.sendRecord(line);
                    } else {
                        resp = scanner.sendRecord(line);
                    }
                    if ((resp != DLSJposConst.DL_NAK) && (resp != DLSJposConst.DL_NRESP)) {
                        break;
                    }
                    retry++;
                }

                // we need to start over
                if (resp == DLSJposConst.DL_CAN) {
                    reader.close();
                    stream = new InputStreamReader(new FileInputStream(file), DLSJposConst.HDL_CHARSET);
                    reader = new BufferedReader(stream);
                    sentRecords = 0;
                    xPerc = 0;
                    if (restart++ > 2) {
                        break;
                    }
                    continue;
                }

                if (resp != DLSJposConst.DL_ACK) {
                    break;
                }

                sentRecords++;
                percentual = 100 * (double) sentRecords / (double) fileRecords;

                if (percentual > xPerc) {
                    onDeviceStatus(CommonsConstants.SUE_UF_PROGRESS + xPerc);
                    xPerc = (int) percentual;
                }
                line = reader.readLine();
            }
        } catch (IOException e) {
            Log.e(TAG, "Exception updating firmware: ", e);
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        DLSJposConst.updateInProgress = false;// reset won't happen if busy
        boolean sentReset = false;

        if (isUsbScanner) {
            if (options.isSendNulls()) {
                String sOut = "\0\0\0\0";
                flash.sendRecord(sOut);
            }
            if (options.isSendFirmwareReset()) {
                flash.resetCommand();
                sentReset = true;
            }

            releaseFlash();
        } else {
            sentReset = true;
            try {
                scanner.reset();
            } catch (DLSException de) {
                Log.e(TAG, "doUpdate: Exception resetting scanner: ", de);
                sentReset = false;
            }
        }

        if (!freezeEvents && sentReset) {
            getEventCallbacks().fireEvent(new StatusUpdateEvent(this, DLSJposConst.DLS_SUE_UF_RESET), EventCallback.EventType.StatusUpdate);
        }

        //If baud rate was changed by the HDL file
        if (scanner.isChangeBaudAfterUpdate()) {
            scanner.changeBaudRate(scanner.getFutureBaudRate());
            scanner.setChangeBaudAfterUpdate(false);
        }

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //After waiting for the scanner to return we may proceed.
        boolean scannerAlive = true;
        if (sentReset) {
            try {
                scannerAlive = scanner.detectResetFinish();
            } catch (DLSException e) {
                Log.e(TAG, "Exception resetting: ", e);
            }
        }

        if (sentRecords == fileRecords) {
            if (scannerAlive) {
                onDeviceStatus(CommonsConstants.SUE_UF_COMPLETE); // status notify done
            } else {
                onDeviceStatus(CommonsConstants.SUE_UF_COMPLETE_DEV_NOT_RESTORED); // status notify done
            }
        } else {
            //  report device state
            if (((type == 0) && (restart == 0)) || (type == 7)) {
                onDeviceStatus(CommonsConstants.SUE_UF_FAILED_DEV_OK);
            } else if (retry > maxRetries) {
                onDeviceStatus(CommonsConstants.SUE_UF_FAILED_DEV_UNRECOVERABLE);
            } else if (restart > 2) {
                onDeviceStatus(CommonsConstants.SUE_UF_FAILED_DEV_NEEDS_FIRMWARE);
            } else {
                onDeviceStatus(CommonsConstants.SUE_UF_FAILED_DEV_UNKNOWN);
            }
        }

        deviceState = CommonsConstants.S_IDLE;        // Set back to IDLE
    }

    void verifyFirmwareFileName(String fileName) throws APosException {
        String sExt;
        try {
            // check filename and extension
            sExt = fileName.substring(fileName.length() - 4);
        } catch (StringIndexOutOfBoundsException iobe) {
            throw new APosException("Bad firmware file name", ErrorConstants.APOS_E_EXTENDED, ErrorConstants.APOS_EFIRMWARE_BAD_FILE);
        }
        // check for file type and bus type
        if (isUsbScanner) {
            if (sExt.compareToIgnoreCase(".DAT") != 0) {
                throw new APosException("Bad file extension", ErrorConstants.APOS_E_EXTENDED, ErrorConstants.APOS_EFIRMWARE_BAD_FILE);
            }
        } else {
            if (sExt.compareToIgnoreCase(".S37") != 0) {
                throw new APosException("Bad file extension", ErrorConstants.APOS_E_EXTENDED, ErrorConstants.APOS_EFIRMWARE_BAD_FILE);
            }
        }
    }

    String getFirmwareHeader(InputStream stream) {
        StringBuilder builder = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        String line;
        int lineCount = 0;
        try {
            while ((line = reader.readLine()) != null && lineCount < 29) {
                if (!line.contains(" "))
                    break;
                builder.append(line);
                lineCount++;
            }
        } catch (Exception e) {
            return "";
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return builder.toString();
    }

    String findFromPatternInFirmwarePackage(BufferedReader reader, Pattern pattern) {
        String result = "";
        int lineCount = 0;
        String line;
        Matcher matcher;
        try {
            while (((line = reader.readLine()) != null) && lineCount <= 29) {
                //End of header region
                if (!line.contains(" "))
                    break;
                lineCount++;
                matcher = pattern.matcher(line);
                if (matcher.find() && !line.contains("REM")) {
                    result = matcher.group(2);
                }
            }
        } catch (Exception e) {
            result = "";
        }
        return result;
    }

    int getFileVID(String header) {
        Pattern pattern = Pattern.compile("(VID=)(\\w{4})");
        int iVID;
        String sVid = findFromPatternInFirmwarePackage(new BufferedReader(new StringReader(header)), pattern);
        try {
            iVID = Integer.decode("0x" + sVid);
        } catch (NumberFormatException nfe) {
            iVID = 0;
        }
        return iVID;
    }

    int getFilePID(String header) {
        //Make sure the pattern does not match an APID in a pass through file by accident.
        Pattern pattern = Pattern.compile("(?<!A)(PID=)(\\w{4})");
        int iPID;
        String sPID = findFromPatternInFirmwarePackage(new BufferedReader(new StringReader(header)), pattern);
        try {
            iPID = Integer.decode("0x" + sPID);
        } catch (NumberFormatException nfe) {
            iPID = 0;
        }
        return iPID;
    }

    int getFileECVersion(String header) {
        Pattern pattern = Pattern.compile("(EC=)(\\w{4})");
        int iEC;
        String sEC = findFromPatternInFirmwarePackage(new BufferedReader(new StringReader(header)), pattern);

        try {
            iEC = Integer.decode("0x" + sEC);
        } catch (NumberFormatException nfe) {
            iEC = 0;
        }
        return iEC;
    }

    int countFileRecords(InputStream stream) {
        if (stream == null)
            return 0;
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        String line;
        int result = 0;
        try {
            while ((line = reader.readLine()) != null) {
                if (line.length() < 10)
                    break;
                result++;
            }
        } catch (IOException e) {
            return 0;
        } finally {
            try {
                stream.reset();
                //reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete() throws APosException {
        // This method is intentionally empty.
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void open(String logicalName, EventCallback cb, Context context) throws
            APosException {
        super.open(logicalName, cb, context);
        Branding branding = Branding.getInstance();
        DLS_USB_FLASH = branding.getBrandingPrefix().toUpperCase() + "-USB-Flash";

        //ECI requires that the encoding of the JVM be set to unicode, but changing the file.encoding property is not supported by Android.
        // The security manager doesn't allow this (https://stackoverflow.com/questions/48730106/e-system-ignoring-attempt-to-set-property-file-encoding-to-value-iso-8859-1)
        // Luckily in Android the default charset is always UTF-8 (https://developer.android.com/reference/java/nio/charset/Charset).

        if (options.isEnableECI()) {
            eci = new ExtendedChannelInterpretation(context);
        }

//        localLogicalName = logicalName;
        decodeData = true;                      // initialize decode to true
        freezeEvents = false;                      // initialize freeze to false
        listData = new LinkedList<>();              // Create a new data queue

        try {
            device = DLSObjectFactory.createScanner(logicalName, context);
        } catch (DLSException cse) {
            throw new APosException("Service creation error", ErrorConstants.APOS_E_NOSERVICE, cse);
        }

        scanner = (DLSScanner) device;
        try {
            device.open(logicalName, context);
        } catch (DLSException e) {
            throw new APosException("Can not open the device.", ErrorConstants.APOS_E_NOSERVICE, e);
        }

        if (scanner.getCanUpdateFirmware()) {
            DLSDeviceInfo oInfo = device.getDeviceInfo();
            String bus = oInfo.getDeviceBus();

            if (bus.equals(CommonsConstants.USB_DEVICE_BUS) || bus.equals(CommonsConstants.HID_DEVICE_BUS)) {
                isUsbScanner = true;
                flash = new DLSUSBFlash();

                try {
                    flash.open(DLS_USB_FLASH, context);
                } catch (DLSException oe) {
                    throw new APosException("Can not create the service", ErrorConstants.APOS_E_NOSERVICE, oe);
                }

                flashDeviceInfo = flash.getDeviceInfo();
                flashDeviceInfo.setDeviceCategory(oInfo.getDeviceCategory());
                flashDeviceInfo.setDeviceName(oInfo.getDeviceName());
                flashDeviceInfo.setProductId(oInfo.getProductId());
                flashDeviceInfo.setVendorId(oInfo.getVendorId());
            }
        }
    }

    @Override
    public void claim(RequestListener listener) throws APosException {
        super.claim(listener);
        /*        String[] temp = {""};*/
        scanner.addLabelReceivedListener(this);
        listData.clear();

        DLSDeviceInfo info = device.getDeviceInfo();
        super.decodeType = info.getOption(DLSDeviceInfo.KEY_DECODETYPE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void release() throws APosException {
        boolean canNotifyPower = false;
        /* In this case, setting it to false might be the wisest choice.  If there
           is no configuration instance, then we are defaulting to a device being able to
           notify when it is disconnected and reconnected.  Our newer devices all support this,
           but some of our older devices do not.  Perhaps setting this to false and allowing the
           canNotifyPowerChange to override with true is the best choice.
         */
        if (config != null)
            canNotifyPower = config.getOptionAsBool(DLSScannerConfig.KEY_CANNOTIFYPOWERCHANGE);
        int powerNotify = getPowerNotify();
        int capPowerReporting = getCapPowerReporting();

        if (canNotifyPower
                && (capPowerReporting == CommonsConstants.PR_STANDARD)
                && (powerNotify != 0)) {
            setPowerState(CommonsConstants.PS_OFF_OFFLINE);
        }
        scanner.removeLabelReceivedListener(this);
        super.release();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateFirmware(String path) throws APosException {
        //region Checks
        checkOpened();
        checkClaimed();
        checkEnabled();
        checkScanner();
        if (!scanner.getCanUpdateFirmware()) {
            throw new APosException("Capability not available.", ErrorConstants.APOS_E_ILLEGAL);
        }
        if (DLSJposConst.updateInProgress || (getState() == CommonsConstants.S_BUSY)) {
            throw new APosException("Device is busy.", ErrorConstants.APOS_E_BUSY);
        }
        //endregion

        File file = new File(path);
        if (!scanner.getDeviceInfo().is8xxx() && !scanner.getDeviceInfo().is9xxx()) {
            verifyFirmwareFileName(file.getName());
            if (isUsbScanner) {
                if (flash != null) {
                    try {
                        flash.claim(new RequestListener() {
                            @Override
                            public void onSuccess() {
                            }

                            @Override
                            public void onFailure(String failureDescription) {
                            }
                        });
                    } catch (DLSException e) {
                        throw new APosException("Flash device creation error", ErrorConstants.APOS_E_NOSERVICE);
                    }
                }
                int vid = flashDeviceInfo.getVendorId();
                int pid = flashDeviceInfo.getProductId();
                String header;
                try {
                    header = getFirmwareHeader(new FileInputStream(file));
                } catch (FileNotFoundException e) {
                    throw new APosException("File not found.", ErrorConstants.APOS_E_FAILURE, e);
                }
                int fvid = getFileVID(header);
                int fpid = getFilePID(header);
                if (vid != fvid) {
                    releaseFlash();
                    throw new APosException(
                            "Wrong vid " + String.format("%X", vid) + " != " + String.format("%X", fvid),
                            CommonsConstants.CVF_FIRMWARE_UNKNOWN);
                }

                if (pid != fpid) {
                    releaseFlash();
                    throw new APosException(
                            "Wrong pid " + String.format("%X", pid) + " != " + String.format("%X", fpid),
                            CommonsConstants.CVF_FIRMWARE_UNKNOWN);
                }

            }
            deviceState = CommonsConstants.S_BUSY;  // Set busy

            DLSJposConst.updateInProgress = true; // set singleton so a shared port won't interrupt
            updateFirmwareStatus = true;
            //Use blocking method (the thread broke jni access)
            try {
                doUpdate(file);
            } catch (FileNotFoundException e) {
                throw new APosException("File not found.", ErrorConstants.APOS_E_FAILURE, e);
            }
            updateFirmwareStatus = false;
        } else if (scanner.getDeviceInfo().is9xxx()) {
            //TODO:add here 9xxx update
        } else {
            //TODO:add here 8xxx update
        }
    }

//endregion

//region Capabilities

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getCapCompareFirmwareVersion() throws APosException {
        checkOpened();
        checkScanner();
        return scanner.getCanCompareFirmwareVersion();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getCapUpdateFirmware() throws APosException {
        checkOpened();
        checkScanner();
        return scanner.getCanUpdateFirmware();
    }

//endregion

//region Properties

    /**
     * {@inheritDoc}
     */
    @Override
    public int getDataCount() throws APosException {
        checkOpened();
        return listData.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getDecodeData() throws APosException {
        checkOpened();
        return decodeData;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setDecodeData(boolean decodeData) throws APosException {
        checkOpened();
        this.decodeData = decodeData;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getPowerState() throws APosException {
        checkClaimed();
        return powerState;
    }

    /**
     * Assign the Power State of the Scanner Service.
     *
     * @param powerState the Power State to assign.  One of:
     *                   * {@link CommonsConstants#PS_UNKNOWN},
     *                   * {@link CommonsConstants#PS_OFFLINE},
     *                   * {@link CommonsConstants#PS_ONLINE},
     *                   * {@link CommonsConstants#PS_OFF_OFFLINE},
     *                   * {@link CommonsConstants#PS_OFF}
     * @throws APosException when the device is not claimed.
     */
    public void setPowerState(int powerState) throws APosException {
        checkClaimed();
        this.powerState = powerState;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] getScanData() throws APosException {
        checkOpened();
        return currentLabelData != null ? currentLabelData.getRawLabel() : new byte[0];
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] getScanDataLabel() throws APosException {
        checkOpened();
        return currentLabelData != null ? currentLabelData.getDecodedLabel() : new byte[0];
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getScanDataType() throws APosException {
        checkOpened();
        return currentLabelData != null ? currentLabelData.getLabelType() : ScannerConstants.SCAN_SDT_UNKNOWN;
    }
//endregion

//region Listeners

    /**
     * {@inheritDoc}
     */
    @Override
    public void onLabelReceived(byte[] rawData, byte[] decodedData, int type) {
        try {
            doAutoDisable();
        } catch (APosException je) {
            Log.e(TAG, "onLabelReceived: Exception performing auto disable. ", je);
        }
        decodedData = validateLabel(rawData, decodedData, type);

        if (getOptions().isEnableECI()) {
            decodedData = this.eci.convert(decodedData);
        }

        listData.addLast(new LabelData(rawData, decodedData, type));

        try {
            sendDataEvent();
        } catch (APosException e) {
            Log.e(TAG, "Sending event: ", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDeviceError(int nErrorCode) {
        DLSProperties props = this.getOptions();
        if (!props.isSuppressErrors()) {
            setDeviceState(CommonsConstants.S_ERROR);
            switch (nErrorCode) {
                case ERR_DEVICE_REMOVED:
                    waitOnScanner = true;
                    if (props.isPostRemovalEvents()) {
                        enqueueError(ErrorConstants.APOS_E_NOHARDWARE, ERR_DEVICE_REMOVED,
                                ErrorConstants.APOS_EL_INPUT, ErrorConstants.APOS_ER_CLEAR);
                    } else {
                        setDeviceState(CommonsConstants.S_IDLE);
                    }
                    break;
                case ERR_DEVICE_REATTACHED:
                    waitOnScanner = false;
                    if (props.isPostRemovalEvents()) {
                        enqueueError(ErrorConstants.APOS_E_NOHARDWARE, ERR_DEVICE_REATTACHED,
                                ErrorConstants.APOS_EL_INPUT, ErrorConstants.APOS_ER_CLEAR);
                    } else {
                        setDeviceState(CommonsConstants.S_IDLE);
                    }
                    break;
                case ERR_CHECKDIGIT:
                    enqueueError(ErrorConstants.APOS_E_FAILURE, ERR_CHECKDIGIT, ErrorConstants.APOS_EL_INPUT,
                            ErrorConstants.APOS_ER_CLEAR);
                    break;
                case ERR_HARDWARE:
                    enqueueError(ErrorConstants.APOS_E_FAILURE, ERR_HARDWARE, ErrorConstants.APOS_EL_INPUT,
                            ErrorConstants.APOS_ER_CLEAR);
                    break;
                case ERR_FLASHING:
                    enqueueError(ErrorConstants.APOS_E_BUSY, ERR_FLASHING, ErrorConstants.APOS_EL_INPUT,
                            ErrorConstants.APOS_ER_CONTINUEINPUT);
                    break;
                case ERR_BUSY:
                    enqueueError(ErrorConstants.APOS_E_BUSY, ERR_BUSY, ErrorConstants.APOS_EL_INPUT,
                            ErrorConstants.APOS_ER_CONTINUEINPUT);
                    break;
                case ERR_DIO_UNDEFINED:
                    enqueueError(ErrorConstants.APOS_E_ILLEGAL, ERR_DIO_UNDEFINED,
                            ErrorConstants.APOS_EL_OUTPUT, ErrorConstants.APOS_ER_CONTINUEINPUT);
                    break;
                case ERR_DIO_NOT_ALLOWED:
                    enqueueError(ErrorConstants.APOS_E_ILLEGAL, ERR_DIO_NOT_ALLOWED,
                            ErrorConstants.APOS_EL_OUTPUT, ErrorConstants.APOS_ER_CONTINUEINPUT);
                    break;
                case ERR_CMD:
                    enqueueError(ErrorConstants.APOS_E_ILLEGAL, ERR_CMD, ErrorConstants.APOS_EL_OUTPUT,
                            ErrorConstants.APOS_ER_CONTINUEINPUT);
                    break;
                default:
                    setDeviceState(CommonsConstants.S_IDLE);
                    break;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDeviceStatus(int statusCode) {
        if (updateFirmwareStatus) {
            onFirmwareStatus(statusCode);
        }

        int pn = CommonsConstants.PN_DISABLED;
        try {
            pn = getPowerNotify();
        } catch (APosException je) {
            Log.e(TAG, "Exception in onDeviceStatus() Power Notify:", je);
        }

        boolean enabled = false;
        try {
            enabled = getDeviceEnabled();
        } catch (APosException je) {
            Log.e(TAG, "Exception in onDeviceStatus() Power Notify:", je);
        }

        if (pn == CommonsConstants.PN_ENABLED && enabled) {
            try {
                if (statusCode == CommonsConstants.SUE_POWER_ONLINE) {
                    setPowerState(CommonsConstants.PS_ONLINE);
                }
                if (statusCode == CommonsConstants.SUE_POWER_OFF_OFFLINE) {
                    setPowerState(CommonsConstants.PS_OFF_OFFLINE);
                }
            } catch (APosException je) {
                Log.e(TAG, "Exception in onDeviceStatus() Power Notify:", je);
            }
        } else if (statusCode == CommonsConstants.SUE_POWER_OFF_OFFLINE ||
                statusCode == CommonsConstants.SUE_POWER_ONLINE) {
            return;
        }

        if (!freezeEvents) {
            StatusUpdateEvent se = new StatusUpdateEvent(this, statusCode);
            getEventCallbacks().fireEvent(se, EventCallback.EventType.StatusUpdate);
        }
    }

    protected void onFirmwareStatus(int statusCode) {
        if (statusCode >= CommonsConstants.SUE_UF_PROGRESS &&
                statusCode < CommonsConstants.SUE_UF_COMPLETE) {
            int value = statusCode - CommonsConstants.SUE_UF_PROGRESS;
            Log.i(TAG, String.format("The update firmware process is continuing... %d%%", value));
        }
        switch (statusCode) {
            case CommonsConstants.SUE_UF_COMPLETE:
                Log.i(TAG, "The update firmware process has completed successfully.");
                break;
            case CommonsConstants.SUE_UF_COMPLETE_DEV_NOT_RESTORED:
                Log.i(TAG, "The update firmware process succeeded, however the Service and/or "
                        + "the physical device cannot be returned to the state they were in before the "
                        + "update firmware process started. The Service has restored all properties to "
                        + "their default initialization values. To ensure consistent Service and "
                        + "physical device states, the application needs to close the Service, then "
                        + "open, claim, and enable again, and also restore all custom application settings.");
                break;
            case CommonsConstants.SUE_UF_FAILED_DEV_OK:
                Log.i(TAG, "The update firmware process failed but the device is still operational.");
                break;
            case CommonsConstants.SUE_UF_FAILED_DEV_UNRECOVERABLE:
                Log.i(TAG, "The update firmware process failed and the device is neither usable "
                        + "nor recoverable through software. The device requires service to be returned to an "
                        + "operational state.");
                break;
            case CommonsConstants.SUE_UF_FAILED_DEV_NEEDS_FIRMWARE:
                Log.i(TAG, "The update firmware process failed and the device will "
                        + "not be operational until another attempt to update the "
                        + "firmware is successful.");
                break;
            case CommonsConstants.SUE_UF_FAILED_DEV_UNKNOWN:
                Log.i(TAG, "The update firmware process failed and the device is in an indeterminate state.");
                break;
        }
    }

//endregion

//region Data senders

    /**
     * {@inheritDoc }
     */
    @Override
    protected void sendDataEvent() throws APosException {
        synchronized (eventListLock) {
            if (getDataEventEnabled() && listData != null && !listData.isEmpty() && !freezeEvents) {
                setDataEventEnabled(false);
                EventData ed = listData.getFirst();
                EventCallback ec = getEventCallbacks();
                DataEvent de;
                int eventType = ed.getEventType();
                switch (eventType) {
                    case EventData.LABEL_EVENT:
                        currentLabelData = (LabelData) ed;
                        listData.removeFirst();
                        de = new DataEvent(ec.getEventSource(), 0);
                        ec.fireEvent(de, EventCallback.EventType.Data);
                        break;
                    case EventData.ITEM_EVENT:
                        currentItemData = (ItemData) ed;
                        listData.removeFirst();
                        de = new DataEvent(ec.getEventSource(), 0);
                        ec.fireEvent(de, EventCallback.EventType.Data);
                        break;
                    case EventData.ERROR_EVENT:
                        ErrorData err = (ErrorData) ed;
                        listData.removeFirst();
                        ErrorEvent ee = new ErrorEvent(this, err.getCode(),
                                err.getExCode(), err.getLocus(), err.getResponse());
                        ec.fireEvent(ee, EventCallback.EventType.Error);
                        if (!isClosed()) {
                            setDeviceState(CommonsConstants.S_IDLE);
                        }
                        switch (ee.getErrorResponse()) {
                            case ErrorConstants.APOS_ER_CLEAR:
                                listData.clear();
                                break;
                            case ErrorConstants.APOS_ER_CONTINUEINPUT:
                                // Continue processing, leaving the deviceState at error
                                break;
                            case ErrorConstants.APOS_ER_RETRY:
                                // No retry available.
                                break;
                        }
                        break;
                }
            }
        }
    }

    //endregion
}
