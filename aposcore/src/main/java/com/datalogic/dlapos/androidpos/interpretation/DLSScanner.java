package com.datalogic.dlapos.androidpos.interpretation;

import android.content.Context;

import androidx.core.util.Pair;

import com.datalogic.dlapos.androidpos.common.Constants;
import com.datalogic.dlapos.androidpos.common.DLSCConfig;
import com.datalogic.dlapos.androidpos.common.DLSDeviceInfo;
import com.datalogic.dlapos.androidpos.common.DLSException;
import com.datalogic.dlapos.androidpos.common.DLSJposConst;
import com.datalogic.dlapos.androidpos.common.DLSProperties;
import com.datalogic.dlapos.androidpos.common.DLSScannerConfig;
import com.datalogic.dlapos.androidpos.common.DLSState;
import com.datalogic.dlapos.commons.constant.ErrorConstants;
import com.datalogic.dlapos.commons.constant.ScannerConstants;
import com.datalogic.dlapos.commons.support.APosException;
import com.datalogic.dlapos.confighelper.DLAPosConfigHelper;
import com.datalogic.dlapos.confighelper.configurations.accessor.LabelHelper;
import com.datalogic.dlapos.confighelper.configurations.accessor.LabelIds;

import java.util.ArrayList;

/**
 * {@code DLSScanner} is the base class for all scanner devices. This class
 * contains methods and attributes specific to scanners.
 */
public abstract class DLSScanner extends DLSDevice {


    private final ArrayList<Object> itemReceivedListeners;
    private final ArrayList<Object> labelReceivedListeners;
    private final ArrayList<Object> systemDataReceivedListeners;

    private volatile boolean changeBaudAfterUpdate;
    private volatile int futureBaudRate;
    protected DLSScannerConfig scannerConfig;
    protected int response = 0;
    private int recordTimeout;
    protected boolean convertBCDtoASCII = true;

    /**
     * Constructor.
     */
    public DLSScanner() {
        super();
        this.recordTimeout = 60000;
        this.itemReceivedListeners = new ArrayList<>();
        this.labelReceivedListeners = new ArrayList<>();
        this.systemDataReceivedListeners = new ArrayList<>();
    }

    //region Listeners

    /**
     * Adds a listener to the collection of listeners to be notified when an
     * item is received.
     *
     * @param listener ItemReceivedListener instance to add
     */
    public void addItemReceivedListener(ItemReceivedListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("ItemReceivedListener instance must not be null.");
        }
        this.itemReceivedListeners.add(listener);
    }

    /**
     * Adds a listener to the collection of listeners to be notified when a label
     * is received.
     *
     * @param listener LabelReceivedListener instance to add
     */
    public void addLabelReceivedListener(LabelReceivedListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("LabelReceivedListener instance must not be null.");
        }
        this.labelReceivedListeners.add(listener);
    }

    /**
     * Adds a listener to the collection of listeners to be notified when system
     * data is received.
     *
     * @param listener SystemDataReceivedListener instance to add
     */
    public void addSystemDataReceivedListener(SystemDataReceivedListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("SystemDataReceivedListener instance must not be " +
                    "null.");
        }
        this.systemDataReceivedListeners.add(listener);
    }

    /**
     * Removes a listener from the collection of listeners to be notified when
     * an item is received.
     *
     * @param listener ItemReceivedListener instance to remove
     */
    public void removeItemReceivedListener(ItemReceivedListener listener) {
        itemReceivedListeners.remove(listener);
    }

    /**
     * Removes a listener from the collection of listeners to be notified when a
     * label is received.
     *
     * @param listener LabelReceivedListener instance to remove
     */
    public void removeLabelReceivedListener(LabelReceivedListener listener) {
        labelReceivedListeners.remove(listener);
    }

    /**
     * Removes a listener from the collection of listeners to be notified when
     * system data is received.
     *
     * @param listener SystemDataReceivedListener instance to remove
     */
    public void removeSystemDataReceivedListener(SystemDataReceivedListener listener) {
        systemDataReceivedListeners.remove(listener);
    }
    //endregion

    /**
     * Changes the port baud rate.
     * <p>
     * Used to alter the port during a firmware update.  Does not alter the scanner settings.
     *
     * @param baud int indicating the new baud rate to set.
     */
    public abstract void changeBaudRate(int baud);

    /**
     * Disables the beep on good read functionality.
     * <p>
     * Only available on USB-OEM and SC-RS232 interfaces. This method has no
     * effect otherwise.
     *
     * @throws DLSException not thrown
     */
    public abstract void disableBeep() throws DLSException;

    /**
     * Enables the beep on good read functionality.
     * <p>
     * Only available on USB-OEM and SC-RS232 interfaces. This method has no
     * effect otherwise.
     *
     * @throws DLSException not thrown
     */
    public abstract void enableBeep() throws DLSException;

    /**
     * Returns an extracted barcode label from the supplied buffer.  This method
     * assumes that there is a trailing character in the buffer after the
     * label to extract.
     *
     * @param inBuf  byte array containing the data to extract from
     * @param offset int indicating the position to start extracting the label
     * @return byte array containing the extracted barcode label
     */
    protected byte[] extractBarcodeLabel(byte[] inBuf, int offset) {
        // Create a buffer of the proper length
        byte[] buf = new byte[inBuf.length - 1 - (offset)];
        System.arraycopy(inBuf, offset, buf, 0, inBuf.length - (1 + offset));

        return buf;
    }

    /**
     * Returns a LabelIds instance containing the type of a barcode based on
     * a specified identifier.
     *
     * @param identifier byte array containing the identifier of the barcode
     * @return LabelRow instance containing the type of the barcode
     */
    protected Pair<LabelIds, Integer> extractBarcodeType(byte[] identifier) {
        DLSDeviceInfo info = getDeviceInfo();
        String decodeType = info.getDecodingType();
        LabelHelper helper = DLAPosConfigHelper.getInstance(context).getLabelHelper();
        int size = 3;
        LabelHelper.DecodeType type = parseDecodeType(decodeType);
        if (type != null)
            helper.setDecodeType(type);
        LabelIds ids = helper.getLabelsIds(identifier);
        if (ids.getUposId() == ScannerConstants.SCAN_SDT_UNKNOWN) {
            byte[] oneByteIdentifier = {identifier[0]};
            if (identifier.length > 2) {
                //No match for three byte identifier, try two bytes.
                byte[] twoByteIdentifier = {identifier[0], identifier[1]};
                ids = helper.getLabelsIds(twoByteIdentifier);
                size = 2;
            }

            if (ids.getUposId() == ScannerConstants.SCAN_SDT_UNKNOWN) {
                //No match for two byte identifier, try one byte.
                ids = helper.getLabelsIds(oneByteIdentifier);
                size = 1;
            }
        }
        return new Pair<>(ids, size);
    }

    protected LabelHelper.DecodeType parseDecodeType(String decodeType) {
        if (decodeType.contains(Constants.DECODE_TYPE_EU))
            return LabelHelper.DecodeType.EUCODE;
        if (decodeType.contains(Constants.DECODE_TYPE_SCRS232))
            return LabelHelper.DecodeType.SCRS232;
        if (decodeType.contains(Constants.DECODE_TYPE_OEM))
            return LabelHelper.DecodeType.IBMUSBOEM;
        if (decodeType.contains(Constants.DECODE_TYPE_US))
            return LabelHelper.DecodeType.USCODE;
        return null;
    }

    /**
     * Calls the onItemReceived method of any registered ItemReceivedListener
     * instances.
     *
     * @param rawData byte array containing the item data to pass to the
     *                listeners
     */
    protected void fireItemReceivedEvent(byte[] rawData) {
        for (Object itemReceivedListener : itemReceivedListeners) {
            ((ItemReceivedListener) itemReceivedListener).onItemReceived(rawData);
        }
    }

    /**
     * Calls the onLabelReceived method of any registered LabelReceivedListener
     * instances.
     *
     * @param raw     byte array containing the raw label data
     * @param decoded byte array containing the decoded label data
     * @param type    int indicating the label type
     */
    protected void fireLabelReceivedEvent(byte[] raw, byte[] decoded, int type) {
        DLSDeviceInfo info = getDeviceInfo();

        byte prefix = info.getDataPrefix();
        byte suffix = info.getDataSuffix();

        byte[] rawLabel;
        byte[] decodedLabel;

        // If the apos.json profile has a prefix and a suffix for the device.
        if (prefix != 0 && suffix != 0) {
            // Allocate the labels to hold the label, the prefix and the suffix.
            rawLabel = new byte[raw.length + 2];
            decodedLabel = new byte[decoded.length + 2];
            // Copy the supplied labels
            System.arraycopy(raw, 0, rawLabel, 1, raw.length);
            System.arraycopy(decoded, 0, decodedLabel, 1, decoded.length);
            // Add the prefix and suffix.
            rawLabel[0] = prefix;
            rawLabel[rawLabel.length - 1] = suffix;
            decodedLabel[0] = prefix;
            decodedLabel[decodedLabel.length - 1] = suffix;
        }

        // Otherwise, if the apos.json profile has either a prefix or a suffix for the device.
        else if (prefix != 0 || suffix != 0) {
            // Allocate the labels to hold the label and either a prefix or suffix.
            rawLabel = new byte[raw.length + 1];
            decodedLabel = new byte[decoded.length + 1];
            // If the profile has a prefix configured.
            if (prefix != 0) {
                // Copy the supplied labels, leaving room for the prefix.
                System.arraycopy(raw, 0, rawLabel, 1, raw.length);
                System.arraycopy(decoded, 0, decodedLabel, 1, decoded.length);
                // Add the prefix.
                rawLabel[0] = prefix;
                decodedLabel[0] = prefix;
            }
            // Otherwise, If the profile has a suffix configured.
            else {
                // Copy the supplied labels.
                System.arraycopy(raw, 0, rawLabel, 0, raw.length);
                System.arraycopy(decoded, 0, decodedLabel, 0, decoded.length);
                // Add the suffix.
                rawLabel[rawLabel.length - 1] = suffix;
                decodedLabel[decodedLabel.length - 1] = suffix;
            }
        }

        // Otherwise, there is no prefix or suffix configured.
        else {
            rawLabel = new byte[raw.length];
            decodedLabel = new byte[decoded.length];
            System.arraycopy(raw, 0, rawLabel, 0, raw.length);
            System.arraycopy(decoded, 0, decodedLabel, 0, decoded.length);
        }

        for (Object labelReceivedListener : labelReceivedListeners) {
            ((LabelReceivedListener) labelReceivedListener).onLabelReceived(rawLabel, decodedLabel, type);
        }
    }

    /**
     * Calls the onSystemDataReceived method of any registered
     * SystemDataReceivedListener instances.
     *
     * @param data byte array containing the system data received
     */
    protected void fireSystemDataReceivedEvent(byte[] data) {
        for (Object systemDataReceivedListener : systemDataReceivedListeners) {
            ((SystemDataReceivedListener) systemDataReceivedListener).onSystemDataReceived(data);
        }
    }

    /**
     * Indicates whether a scanner can compare firmware version.
     *
     * @return boolean indicating whether the device can compare firmware
     * version
     */
    public boolean getCanCompareFirmwareVersion() {
        DLSScannerConfig config = getScannerConfig();
        if (config == null) return false;
        return config.getCanCompareFirmwareVersion();
    }

    /**
     * Indicates whether a scanner can update firmware.
     *
     * @return boolean indicating whether the device can update firmware
     */
    public boolean getCanUpdateFirmware() {
        DLSScannerConfig config = getScannerConfig();
        if (config == null) return false;
        return config.getCanUpdateFirmware();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DLSCConfig getConfiguration() {
        return scannerConfig;
    }

    /**
     * Indicates whether the scanner is configured to delete an image file after
     * reading.
     *
     * @return boolean indicating whether image files are deleted after being
     * read
     */
    public boolean getDeleteImageFileAfterRead() {
        DLSScannerConfig config = getScannerConfig();
        if (config == null) return false;
        return config.getDeleteImageFileAfterRead();
    }

    public synchronized int getFutureBaudRate() {
        return futureBaudRate;
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
     * Returns the Scanner configuration which is loaded from apos.json.  The
     * difference between this method and the getConfiguration method is that
     * this one does not implicitly cast the scanner configuration to a
     * DLSCConfig object.
     *
     * @return DLSScannerConfig the Scanner configuration
     */
    public DLSScannerConfig getScannerConfig() {
        return scannerConfig;
    }

    public synchronized boolean isChangeBaudAfterUpdate() {
        return changeBaudAfterUpdate;
    }

    /**
     * Parse the byte array of data, logging "OK" messages to the log for each
     * device element.
     *
     * @param sohBuf byte array containing the data returned from a health check
     *               request
     * @throws DLSException if any element has a "FAIL" status
     */
    protected void logHealthCheck(byte[] sohBuf) throws DLSException {
        String str;
        String strDevice = "";
        String strResp;          // Handy strings
        StringBuilder oSb = new StringBuilder();

        for (int i = 0; i < sohBuf.length; i++) {         // For entire buffer length
            if (sohBuf[i] == 0x02) {                    // If stx char
                i++;                                        // Goto next char
                while (sohBuf[i] != 0x03) {                // Until etx reached
                    //str += (char) sohBuf[i];                   // Concat chars to string
                    oSb.append((char) sohBuf[i]);
                    i++;                                      // Next char
                }
                str = oSb.toString();

                if (str.length() > 1) {                   //
                    switch (str.charAt(0)) {                   // Check indicator char
                        case 'm':
                            strDevice = "motor";
                            break;
                        case 'h':
                            strDevice = "Horizontal Laser";
                            break;
                        case 'v':
                            strDevice = "Vertical Laser";
                            break;
                        case 's':
                            strDevice = "Scale";
                            break;
                        case 'd':
                            strDevice = "Display";
                            break;
                        case 'e':
                            strDevice = "EAS";
                            break;
                        case 'c':
                            strDevice = "Camera";
                            break;

                        default:
                    }
                    strResp = str.substring(1);       // Save the response portion of message

                    // If there was a response, and it is NOT "OK" then log an error, and
                    //  throw exception.
                    if (strResp.equals("FAIL")) {
                        throw new DLSException(DLSJposConst.DLS_E_HARDWARE,
                                "Scanner: " + strDevice);
                    }
                }
            }
        }
    }

    @Override
    void internalClaim() throws DLSException {
        DLSDeviceInfo info = getDeviceInfo();
        DLSProperties props = DLSProperties.getInstance(context);
        String bus = info.getDeviceBus();
        // If the device is using a bus with "USB".
        if (bus != null && bus.contains("USB")) {
            // If JavaPOS is set to Auto load configuration
            if (props.isAutoLoadConfig()) {
                // If the device allows config on claim
                if (info.isConfigOnClaim()) {
                    updateConfiguration();
                    reportConfiguration();
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void open(String logicalName, Context context) throws DLSException {
        super.open(logicalName, context);
        try {
            scannerConfig = new DLSScannerConfig();
            if (!DLAPosConfigHelper.getInstance(context).isInitialized())
                DLAPosConfigHelper.getInstance(context).initialize(context);
            scannerConfig.loadConfiguration(logicalName, DLAPosConfigHelper.getInstance(context).getProfileManager());
        } catch (APosException e) {
            throw new DLSException(ErrorConstants.APOS_E_FAILURE, "Can not get a profile for this device: \n" + e.getMessage());
        }
        setState(DLSState.OPENED);
    }

    /**
     * Returns a DLSScannerConfig instance containing a representation of the
     * current configuration of the scanner.
     *
     * @return DLSScannerConfig containing the configuration of the scanner.
     * @throws DLSException thrown if an Exception occurs while reading the
     *                      configuration from the scanner.
     */
    public abstract DLSScannerConfig reportConfiguration() throws DLSException;

    /**
     * Function to schedule a change of baud rate after next update.
     *
     * @param baudRate the desired baud rate in range 2400-115200.
     */
    public void scheduleFutureBaudRateChange(int baudRate) {
        if ((baudRate < 2400) || (baudRate > 115200)) {
            throw new IllegalArgumentException("changeBaudRate: Invalid Baud Rate (" + baudRate +
                    ") specified. Valid range is 2400 to 115200.");
        }
        setFutureBaudRate(baudRate);
        setChangeBaudAfterUpdate(true);
    }

    /**
     * Sends a message to the scanner.
     * <p>
     * Only implemented by {@link DLS9xxxScanner}. Will throw
     * {@code UnsupportedOperationException} in all other instances.
     *
     * @param message String containing the message to send to the scanner
     * @return int indicating the value of the response status byte
     */
    public abstract int sendMessage(String message);

    /**
     * Sends a firmware record to the scanner.
     *
     * @param record String containing the record to send.
     * @return int indicating the command response from the scanner
     */
    public abstract int sendRecord(String record);

    public synchronized void setChangeBaudAfterUpdate(boolean value) {
        changeBaudAfterUpdate = value;
    }

    public synchronized void setFutureBaudRate(int value) {
        futureBaudRate = value;
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

    protected void setScannerConfig(DLSScannerConfig scannerConfig) {
        this.scannerConfig = scannerConfig;
    }

    /**
     * Takes the message retrieved from a specific Direct I/O command and splits
     * it into multiple strings. Messages are split using 0x02/0x03 as
     * delimiters.
     *
     * @param inBuf byte array containing the message to be split
     * @return String array containing the delimited strings
     */
    public String[] splitMessage(byte[] inBuf) {
        StringBuilder oSb = new StringBuilder();
        // need to handle the case where there is only one item - ????

        parseMessages(inBuf, (byte) 0x03);                     // Split messages at the 0x03
        // Iterate through split message
        // byte[] buf; // = (byte[])it.next();                       // Skip the first message
        for (Object o : messageList) {                             // For all messages
            byte[] buf = (byte[]) o;                           // Get next message
            for (int i = 0; i < buf.length; i++) {                 // For entire lenght
                buf[i] = (byte) ((buf[i] == 0x03) ? ',' : buf[i]); // If char is a 0x03, convert to a comma
                if ((buf[i] >= ' ') && (buf[i] <= '~')) {       // let through all printable characters
                    //str += (char) buf[i];                           // Grow string with comma delimiters
                    oSb.append((char) buf[i]);
                }
            }
        }
        String str = oSb.toString();
        outputStream.reset();                                           // Get rid of leftovers
        messageList.clear();                                      // Clear out the list
        return str.split(",");                                // Split the string at the 0x02 (commas)
    }

    /**
     * Updates the scanner configuration with parameters from the current
     * scanner configuration object.
     * <p>
     * Only available for USB-OEM scanners. This method has no effect otherwise.
     *
     * @throws DLSException not thrown
     */
    public abstract void updateConfiguration() throws DLSException;

}
