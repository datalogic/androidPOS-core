package com.datalogic.dlapos.androidpos.interpretation;

import android.util.Log;

import androidx.core.util.Pair;

import com.datalogic.dlapos.androidpos.common.DLSDeviceInfo;
import com.datalogic.dlapos.androidpos.common.DLSException;
import com.datalogic.dlapos.androidpos.common.DLSJposConst;
import com.datalogic.dlapos.androidpos.common.DLSProperties;
import com.datalogic.dlapos.androidpos.service.DLSScannerService;
import com.datalogic.dlapos.confighelper.configurations.accessor.LabelIds;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

/**
 * PowerScan 9xxx device for Home Depot.
 */
public class DLS9xxxScanner extends DLSSerialScanner {

    private static final String Tag = "DLS9xxxScanner";

    protected static final String RESET_CMD = "$S,Ar\r";
    private static final String INFO_CMD = "#+OWI0\r\n";
    protected static final String CRADLE_STATUS_CMD = "C";
    //private static final String joinCmd = "\r\r$+RN0$-\r"; //only used on legacy products (8xxx)
    protected static final String ACCEPT_HOST_CMD = "$S,CIFIH00,Ar\r";
    protected static final String REJECT_HOST_CMD = "$S,CIFIH01,Ar\r";
    protected static final String HOST_CMD_RESPONSE = "$>,>,>";
    protected static final String RESET_CMD_RESPONSE = "$>,>\r";
    private static final byte[] EMPTY_BYTE_ARRAY = {};
    private final boolean batteryInfo = false;
    int returnType = 0;
    int returnQty = 0;


    /**
     * Creates a new instance of DLS9xxxScanner
     **/
    public DLS9xxxScanner() {
        super();
    }


    /**
     * Enables host commands for the device. This will cause a reset to be
     * initiated to update the device.
     * <p>
     * This method should be followed by a {@link #rejectHostCommands()} call
     * to return the scanner to "normal" two way scanning mode.
     */
    public void acceptHostCommands() {
        sendMsg(ACCEPT_HOST_CMD);
        if (!detectResetFinish()) {
            Log.e(Tag, "acceptHostCommands: Error occurred while trying to enable host commands");
        }
    }


    /**
     * Empty method - not supported on this interface.
     */
    @Override
    public void changeBaudRate(int newBaud) {
        throw new UnsupportedOperationException("Not supported in this interface");
    }


    /**
     * Sends a Direct I/O command to the device.
     * <p>
     * Depending upon the interface and the Direct I/O command being
     * executed, the {@code data} parameter may not need to be populated and
     * {@code null} can be used (refer to the JavaPOS user guide for specific
     * implementation). Any result messages or data will be passed back through
     * the {@code ByteArrayOutputStream} object.
     * <p>
     * Example Usage: <pre>
     *      {@code
     *      ByteArrayOuputStream bs = new ByteArrayOutputStream();
     *      scanner.directIO(DLSJposConst.DIO_SCALE_STATUS, dioData, bs);
     *      String result = bs.toString();}
     * </pre>
     * <p>
     * <b> {@code data} parameter should not be {@code null} or empty. It is
     * often used to return the command status at {@code data[0]}. </b>
     * <p>
     * Supported commands: <ul>
     * <li> {@code DIO_DISPLAY_SINGLE_LINE}
     * <li> {@code DIO_TWO_WAY_HOST_RESPONSE}
     * <li> {@code DIO_RETURN_DATA_TYPE}
     * <li> {@code DIO_RETURN_QUANTITY}
     * <li> {@code DIO_DISPLAY_DATA}
     * <li> {@code DIO_RETURN_DATA}
     * <li> {@code DIO_RETURN_BATTERY_DATA}
     * </ul>
     *
     * @throws DLSException
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

        String strResult;
        switch (command) {
            case DLSJposConst.DIO_DISPLAY_SINGLE_LINE:
                // add the "@" to each written line to ensure all chars are displayed
                String sDispOne = object.toString();
                sendMsg("@" + sDispOne + "\r");
                strResult = "Single Line";
                break;
            case DLSJposConst.DIO_TWO_WAY_HOST_RESPONSE:
                sendMsg("@" + object.toString() + "\r");
                strResult = "Response Sent";
                break;
            case DLSJposConst.DIO_RETURN_DATA_TYPE:
                strResult = Integer.toString(returnType);
                data[0] = returnType;
                break;
            case DLSJposConst.DIO_RETURN_QUANTITY:
                data[0] = returnQty;
                strResult = Integer.toString(returnQty);
                break;
            case DLSJposConst.DIO_DISPLAY_DATA:
                port.sendData(((ByteArrayOutputStream) object).toByteArray());
                data[0] = 0;
                strResult = "Display Data";
                break;
            case DLSJposConst.DIO_RETURN_DATA:
                data[0] = returnType;
                strResult = "Return Data";
                break;
            case DLSJposConst.DIO_RETURN_BATTERY_DATA:
                if (statistics.containsKey(DLSJposConst.DLS_SECONDARY_BATTERY_CHG_CYCLES)) {
                    strResult = "Battery Charge Cycles = " + statistics.get(DLSJposConst.DLS_SECONDARY_BATTERY_CHG_CYCLES);
                    data[0] = 0;
                } else {
                    data[0] = 1;
                    strResult = "Battery Data Not Available";
                }
                break;
            case DLSJposConst.DIO_CHECK_CRADLE_STATUS:
                data[0] = 1;
                bStatMessageSent = true;
                byte[] buf = sendReceiveMsg(CRADLE_STATUS_CMD, 1000);
                bStatMessageSent = false;
                if (buf.length == 0) {
                    strResult = "Failed to retrieve cradle status";
                    break;
                }
                String sCradle = new String(buf);
                if (sCradle.matches("Scanner\\d")) {
                    data[0] = 0;
                    strResult = sCradle;
                } else {
                    strResult = "Cradle response not recognized - " + sCradle;
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
                super.directIO(command, data, object);
                return; //need to return here to not overwrite any results below
        }

        if (object instanceof ByteArrayOutputStream) {
            try {
                ByteArrayOutputStream oBs = (ByteArrayOutputStream) object;
                oBs.reset();
                oBs.write(strResult.getBytes());
            } catch (IOException e) {
                Log.e(Tag, "Unable to return data to DIO caller - Are you passing in a ByteArrayOuputStream object?");
                Log.e(Tag, "directIO: ByteArrayOutputStream object write exception: ", e);
            } catch (ClassCastException ex) {
                Log.e(Tag, "directIO: Unable to return data to DIO caller - Are you passing in a ByteArrayOuputStream object?");
                Log.e(Tag, "directIO: Exception caught: ", ex);
            }
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void onDataReceived(byte[] inBuf, int inLen) {
        DLSDeviceInfo oInfo = getDeviceInfo();
        boolean bThdTrigger = false;

        if (inBuf == null || inLen == 0) {
            return;
        }

        //Make a string value from the byte buffer
        String value = new String(inBuf);

        // if we are in firmware update mode then sync to objWaitAck
        // put single character in nResponse
        if (bFirmware) {
            synchronized (objWaitAck) {
                response = inBuf[0];
                bFirmware = false;
                objWaitAck.notifyAll();
                return;
            }
        }


        // For some reason the Status command does not follow convention of having a normal
        //  \r character as a deliniator.  It surrounds its message with a 0x01....0x04.  So
        //  we need a special case here to trap that.

        // If (this is the begginning of a message AND the first char is a SOH), OR
        //  (we already have a SOH and are waiting on an EOT).


        if (bStatMessageSent) {
            synchronized (objWaitSohBuf) {
                bStatMessageSent = false;
                sohBuf = Arrays.copyOf(inBuf, inBuf.length);
                objWaitSohBuf.notifyAll();
            }
            /*
             * JAVAPOS_SWCR_339
             * Labels are not framed, the other commands are.  Examine the
             * incoming packet for a framing character (0x01) at the beginning.
             * If it is found, go ahead and return, otherwise, carry on as it
             * is likely label data.
             */
            if (inBuf[0] == 0x01) {
                return;                                   // Get out.
            }
        }

        if (value.trim().equals(HOST_CMD_RESPONSE.trim()) || value.trim().equals(RESET_CMD_RESPONSE.trim())) {
            return;
        }
        byte[] tmpBuf = inBuf;                       // If any messages in list
        byte pre = oInfo.getRxPrefix();

        if (tmpBuf.length > 2) {                      // Sanity check
            if (pre == tmpBuf[0]) {
                CMD_POS = 1;            // If prefix character exists
            }
            byte[] identifier = {tmpBuf[CMD_POS], tmpBuf[CMD_POS + 1], tmpBuf[CMD_POS + 2]};
            Pair<LabelIds, Integer> labelRow = extractBarcodeType(identifier);
            int type;
            int identifierLength = labelRow.second;

            //Check for addons to modify type.
            /*
             * This is where we tear apart the 9500 label data.
             * We have to parse the tmpBuf into the correct parts
             *
             * FORMAT:
             * 048500001745%P1.2%V
             */
            String str = new String(inBuf);
            String[] pieces = str.split("%");
            // validate pieces to ensure that protocol data exists
            /*
             * look for a quantity in the string.
             *  <Label>%P<Qty>%Q1.2%V
             *          OR
             *  <Label>%P<Qty>%Q1.2%V%K
             *
             * NOTE: Q1.2 will always be the third item in the
             * pieces array.
             */
            //this is a QTY
            if (pieces.length > 2 && pieces[2].contains("Q1.2")) {
                try {
                    returnQty = Integer.parseInt(pieces[1].substring(1));
                } catch (NumberFormatException e) {
                    returnQty = 0;
                }
            } else {
                // always set the qty to 1 when a label comes in that
                // doesn't have the qty field
                returnQty = 1;
            }
            // switch/case on last character
            switch (pieces[pieces.length - 1].charAt(0)) {
                case 'V':
                    //returnType = 1; //scanned data
                    returnType = labelRow.first.getUposId();
                    bThdTrigger = true;
                    break;
                case 'K':
                    returnType = 2; // keypad data
                    break;
                case 'E':
                    returnType = 3; // escape key
                    str = "ESC";
                    tmpBuf = str.getBytes();
                    pieces = str.split("");
                    break;
                case 'I':
                    returnType = 4; // price inquiry key
                    str = "INQ";
                    tmpBuf = str.getBytes();
                    pieces = str.split("");
                    break;
                case 'O':
                    returnType = 5; // enter key
                    break;
                case 'S':   // Synk
                    break;
            }
            DLSProperties options = DLSProperties.getInstance(context);
            // make them both the same
            if (options.isSendCookedData()) {
                tmpBuf = pieces[0].getBytes();//barcode;
            }
            type = returnType;
            String strTmpBuf = new String(tmpBuf);
            if (strTmpBuf.contains("INQ") || strTmpBuf.contains("ESC")) {
                fireLabelReceivedEvent(tmpBuf, tmpBuf, type);
            } else {
                byte[] tLabel;
                byte[] tFixed;
                if (bThdTrigger && identifierLength > 0) {
                    tLabel = pieces[0].getBytes();
                    tFixed = new byte[tLabel.length - identifierLength];
                    int j = 0;
                    for (int i = identifierLength; i < tLabel.length; i++) {
                        tFixed[j] = tLabel[i];
                        j++;
                    }
                } else {
                    tFixed = pieces[0].getBytes();
                }
                fireLabelReceivedEvent(tmpBuf, tFixed, type);
            }       // clear out the variables
        }

        // Be sure to clean list after each time through here.
        messageList.clear();
    }


    /**
     * Disables host commands for the device. This will cause a reset to be
     * initiated to update the device.
     * <p>
     * This method should have been preceded by a {@link #acceptHostCommands()}
     * call.
     */
    public void rejectHostCommands() {
        sendMsg(REJECT_HOST_CMD);
        if (!detectResetFinish()) {
            Log.e(Tag, "rejectHostCommands: Error occurred while trying to disable host commands");
        }
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
    }


    /**
     * Sends a command to the scanner, synchronously waiting for a response.
     * A default timeout will be used.
     *
     * @param sMessage String containing command to send
     * @return int indicating the command response from the scanner
     */
    @Override
    public int sendMessage(String sMessage) {
        byte[] buf;
        switch (sMessage) {
            case "acceptHostCmd":
                acceptHostCommands();
                buf = new byte[]{DLSJposConst.DL_ACK};
                break;
            case "rejectHostCmd":
                rejectHostCommands();
                buf = new byte[]{DLSJposConst.DL_ACK};
                break;
            default:
                buf = sendReceiveMsg(sMessage);
                break;
        }
        return buf.length;
    }
}
