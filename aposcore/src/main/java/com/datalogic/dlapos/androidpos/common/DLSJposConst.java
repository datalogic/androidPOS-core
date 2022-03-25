package com.datalogic.dlapos.androidpos.common;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Abstract class containing AndroidPos constants.
 */
public abstract class DLSJposConst {
    /**
     * Static variable to check if there is an update in progress.
     */
    public volatile static boolean updateInProgress = false;

    /**
     * Constant containing the Datalogic vendor ID.
     */
    public static final short DEV_VENDOR_ID1 = 0x05f9;
    /**
     * Constant containing the Datalogic vendor ID.
     */
    public static final short DEV_VENDOR_ID2 = 0x04b3;
    /**
     * Constant for the hand scanner usage.
     */
    public static final short DEV_HAND_SCANNER_USAGE = 0x4B00;
    /**
     * Constants for the table scanner usage.
     */
    public static final short DEV_TABLE_SCANNER_USAGE = 0x4A00;
    /**
     * Constants for the scale usage.
     */
    public static final short DEV_SCALE_USAGE = 0x6E00;
    /**
     * Constants for the flash usage.
     */
    public static final short DEV_FLASH_USAGE = (short) 0xA000;
    /**
     * Constants containing the max size of a buffer.
     */
    public static final int MAX_BUFFER_SIZE = 1312788; // from PC_BLOB_MAX_BUFFER_SIZE in jade/pc_def.h

    /**
     * Configuration error constant when configuration item does not exist.
     */
    public static final int DL_BEL = 0x07;  // "ERROR: Config item does not exist"
    /**
     * Configuration error constant when configuration item or data are out of range.
     */
    public static final int DL_NAK = 0x15;  // "ERROR: Invalid config item or data out of range";
    /**
     * Configuration response constant when configuration is accepted.
     */
    public static final int DL_ACK = 0x06;  // "OK: Config item accepted"
    /**
     * Configuration error constant when malformed configuration item or data.
     */
    public static final int DL_CAN = 0x18;  // "ERROR: Malformed config item and/or data"
    /**
     * Configuration error constant when scanner is taking picture.
     */
    public static final int DL_ETB = 0x17;  // "ERROR: Scanner currently taking picture"
    /**
     * Configuration response constant when an invalid response is received.
     */
    public static final int DL_INVALID = 0xEF; // invalid response
    /**
     * Configuration response constant when no response is received.
     */
    public static final int DL_NRESP = 0xFF; // no response

    /* DLSException error codes */
    /**
     * Generic DLSException error code for USB error.
     */
    public static final int DLS_E_USB = 0x80000001;
    /**
     * DLSException error code for USB access error.
     */
    public static final int DLS_E_USBACCESS = 0x80000002;
    /**
     * DLSException error code for port creation error.
     */
    public static final int DLS_E_CREATEPORT = 0x80000003;
    /**
     * DLSException error code for port opening error.
     */
    public static final int DLS_E_OPENPORT = 0x80000004;
    /**
     * DLSException error code for configuration error.
     */
    public static final int DLS_E_CONFIGURATION = 0x80000005;
    /**
     * DLSException error code for not supported operations.
     */
    public static final int DLS_E_NOTSUPPORTED = 0x80000006;
    /**
     * DLSException error for enable error.
     */
    public static final int DLS_E_ENABLE = 0x80000007;
    /**
     * DLSException error for timeouts.
     */
    public static final int DLS_E_TIMEOUT = 0x80000008;
    /**
     * DLSException error for hardware error.
     */
    public static final int DLS_E_HARDWARE = 0x80000009;
    /**
     * DLSException error when a device is removed.
     */
    public static final int DLS_E_DEVICE_REMOVED = 0x8000000A;
    /**
     * DLSException error for invalid arguments.
     */
    public static final int DLS_E_INVALID_ARG = 0x8000000B;
    /**
     * DLSException error for state transition.
     */
    public static final int DLS_E_STATE_TRANSITION = 0x8000000C;
    /**
     * DLSException error for disable error.
     */
    public static final int DLS_E_DISABLE = 0x8000000D;

    public static final int DLS_SUE_UF_RESET = 3001;

    public static final int DLS_CFV_INVALID_VID = 6;
    public static final int DLS_CFV_INVALID_PID = 7;

    /* DirectIO commands for the USB scanner */
    /**
     * Scanner reset command for DIO.
     */
    public static final int DIO_SCANNER_RESET = 1;  // ByteArrayOutputStream.toByteArray()
    /**
     * Enable beep command for DIO.
     */
    public static final int DIO_SCANNER_ENABLE_BEEP = 2;  // ByteArrayOutputStream.toByteArray()
    /**
     * Disable beep command for DIO.
     */
    public static final int DIO_SCANNER_DISABLE_BEEP = 3;  // ByteArrayOutputStream.toByteArray()
    /**
     * Scanner configuration command for DIO.
     */
    public static final int DIO_SCANNER_CONFIGURE = 4;  // ByteArrayOutputStream.toByteArray()
    /**
     * Scanner report configuration command for DIO.
     */
    public static final int DIO_SCANNER_REPORT_CONFIG = 5;  // ByteArrayOutputStream.toByteArray()
    /**
     * Scanner configuration command 2 labels for DIO.
     */
    public static final int DIO_SCANNER_CONFIG_2LABEL = 6;  // ByteArrayOutputStream.toByteArray()
    /**
     * Scanner report configuration command 2 labels for DIO.
     */
    public static final int DIO_SCANNER_REPORT_2LABEL = 7;  // ByteArrayOutputStream.toByteArray()
    /**
     * Scanner information command for DIO.
     */
    public static final int DIO_SCANNER_DIO_INFORMATION = 8;  // ByteArrayOutputStream.toByteArray()
    /**
     * Scanner health command for DIO.
     */
    public static final int DIO_SCANNER_DIO_HEALTH = 9;  // ByteArrayOutputStream.toByteArray()
    /**
     * Scanner statistics command for DIO.
     */
    public static final int DIO_SCANNER_DIO_STATS = 10; // ByteArrayOutputStream.toByteArray()
    /**
     * Scanner beep command for DIO.
     */
    //Note that DIO_SCANNER_BEEP and DIO_SCANNER_DIO_BEEP are the same value DO NOT CHANGE
    public static final int DIO_SCANNER_BEEP = 11;
    //Note that DIO_SCANNER_BEEP and DIO_SCANNER_DIO_BEEP are the same value DO NOT CHANGE
    /**
     * Scanner beep command for DIO.
     */
    public static final int DIO_SCANNER_DIO_BEEP = 11; // ByteArrayOutputStream.toByteArray()
    /**
     * Scanner nof command for DIO.
     */
    public static final int DIO_SCANNER_DIO_NOF = 12; // ByteArrayOutputStream.toByteArray()
    /**
     * Scanner error beep command for DIO.
     */
    public static final int DIO_SCANNER_DIO_ERROR_BEEP = 13; // ByteArrayOutputStream.toByteArray()

    /* DirectIO commands for the USB scale */
    /**
     * Scale reset command for DIO.
     */
    public static final int DIO_SCALE_RESET = 21; // ByteArrayOutputStream.toByteArray()
    /**
     * Scale enable 3 byte status command for DIO.
     */
    public static final int DIO_SCALE_ENABLE3BYTESTATUS = 22; // ByteArrayOutputStream.toByteArray()
    /**
     * Scale disable 3 byte status command for DIO.
     */
    public static final int DIO_SCALE_DISABLE3BYTESTATUS = 23; // ByteArrayOutputStream.toByteArray()
    /**
     * Scale configure command for DIO.
     */
    public static final int DIO_SCALE_CONFIGURE = 24; // ByteArrayOutputStream.toByteArray()
    /**
     * Scale configuration report command for DIO.
     */
    public static final int DIO_SCALE_REPORT_CONFIG = 25; // ByteArrayOutputStream.toByteArray()

    /* DirectIO Commands for the SingleCable RS232 Scale */
    /**
     * Single cable scale hard reset command for DIO.
     */
    public static final int DIO_SCALE_HARD_RESET = 21; // ByteArrayOutputStream.toString()
    /**
     * Single cable scale monitor command for DIO.
     */
    public static final int DIO_SCALE_MONITOR = 26; // ByteArrayOutputStream.toString()
    /**
     * Single cable scale monitor status command for DIO.
     */
    public static final int DIO_SCALE_STATUS = 27; // ByteArrayOutputStream.toString()
    /**
     * Single cable scale display status command for DIO.
     */
    public static final int DIO_SCALE_DISPLAY_STATUS = 28; // ByteArrayOutputStream.toString()
    /**
     * Single cable scale soft power down command for DIO.
     */
    public static final int DIO_SCALE_SOFT_POWER_DOWN = 29; // ByteArrayOutputStream.toString()
    /**
     * Single cable scale status command for DIO.
     */
    public static final int DIO_SCALE_CMP_STATUS = 601; // same as above
    /**
     * Single cable scale get live weight for DIO.
     */
    public static final int DIO_SCALE_CMP_LIVE_WEIGHT = 604; // same as monitor
    /**
     * Single cable scale read ROM command for DIO.
     */
    public static final int DIO_SCALE_CMP_READ_ROM = 602; // behavior unknown
    /**
     * Single cable scale get ROM version for DIO.
     */
    public static final int DIO_SCALE_CMP_ROM_VERSION = 603; // behavior unknown

    /* directio commands implemented for SingleCable RS232 Scanner */
    /**
     * Single cable RS232 scanner hard reset command for DIO.
     */
    public static final int DIO_SCSCANNER_HARD_RESET = 1;
    /**
     * Single cable RS232 scanner status command for DIO.
     */
    public static final int DIO_SCSCANNER_STATUS = 2;
    /**
     * Single cable RS232 scanner switch read command for DIO.
     */
    public static final int DIO_SCSCANNER_SWITCH_READ = 3;
    /**
     * Single cable RS232 scanner not on file command for DIO.
     */
    public static final int DIO_SCSCANNER_NOT_ON_FILE = 4;
    /**
     * Single cable RS232 scanner disable RDLT command for DIO.
     */
    public static final int DIO_SCSCANNER_DISABLE_RDLT = 5;
    /**
     * Single cable RS232 scanner disable data command for DIO.
     */
    public static final int DIO_SCSCANNER_DISPLAY_DATA = 8;
    /**
     * Single cable RS232 scanner display statistics command for DIO.
     */
    public static final int DIO_SCSCANNER_DISPLAY_STAT = 9;
    /**
     * Single cable RS232 scanner enable tone command for DIO.
     */
    public static final int DIO_SCSCANNER_ENABLE_TONE = 10;
    /**
     * Single cable RS232 scanner beep command for DIO.
     */
    public static final int DIO_SCSCANNER_BEEP_GOOD = 11;
    /**
     * Single cable RS232 scanner soft power down command for DIO.
     */
    public static final int DIO_SCSCANNER_SOFT_PWRDN = 12;
    /**
     * Single cable RS232 scanner disable tone command for DIO.
     */
    public static final int DIO_SCSCANNER_DISABLE_TONE = 13;
    /**
     * Single cable RS232 scanner enter T mode command for DIO.
     */
    public static final int DIO_SCSCANNER_ENTER_TMODE = 14;
    /**
     * Single cable RS232 scanner read pace command for DIO.
     */
    public static final int DIO_SCSCANNER_READ_PACE = 15;
    /**
     * Single cable RS232 scanner reset pace command for DIO.
     */
    public static final int DIO_SCSCANNER_RESET_PACE = 16;
    /**
     * Single cable RS232 scanner enable pace command for DIO.
     */
    public static final int DIO_SCSCANNER_ENABLE_PACE = 17;
    /**
     * Single cable RS232 scanner disable pace command for DIO.
     */
    public static final int DIO_SCSCANNER_DISABLE_PACE = 18;
    /**
     * Single cable RS232 scanner soft reset command for DIO.
     */
    public static final int DIO_SCSCANNER_SOFT_RESET = 19;
    /**
     * Single cable RS232 scanner identify command for DIO.
     */
    public static final int DIO_SCSCANNER_IDENTIFY = 20;
    /**
     * Single cable RS232 scanner health command for DIO.
     */
    public static final int DIO_SCSCANNER_HEALTH = 21;
    /**
     * Single cable RS232 scanner statistics command for DIO.
     */
    public static final int DIO_SCSCANNER_STATISTICS = 22;

    // the following set is used in communication with the PowerScan M8300
    // for The Home Depot
    /**
     * PowerScan 8xxx HD display single line command for DIO.
     */
    public static final int DIO_DISPLAY_SINGLE_LINE = 320;
    /**
     * PowerScan 8xxx HD two way host response for DIO.
     */
    public static final int DIO_TWO_WAY_HOST_RESPONSE = 334;
    /**
     * PowerScan 8xxx HD return data type for DIO.
     */
    public static final int DIO_RETURN_DATA_TYPE = 332;
    /**
     * PowerScan 8xxx HD return quantity for DIO.
     */
    public static final int DIO_RETURN_QUANTITY = 331;
    /**
     * PowerScan 8xxx HD display data command for DIO.
     */
    public static final int DIO_DISPLAY_DATA = 333;
    /**
     * PowerScan 8xxx HD return data for DIO.
     */
    public static final int DIO_RETURN_DATA = 335;
    /**
     * PowerScan 8xxx HD return battery data for DIO.
     */
    public static final int DIO_RETURN_BATTERY_DATA = 336;
    /**
     * PowerScan 8xxx HD check cradle status command for DIO.
     */
    public static final int DIO_CHECK_CRADLE_STATUS = 337;


    // the following set is used in communication with the Portal Scanner
    /**
     * Serial scanner health summary command for DIO.
     */
    public static final int DIO_PORTAL_HEALTH_SUMMARY = 350; // 'h'
    /**
     * Serial scanner sleep lightly command for DIO.
     */
    public static final int DIO_PORTAL_SLEEP_LIGHTLY = 351; // 'z'
    /**
     * Serial scanner read configuration item command for DIO.
     */
    public static final int DIO_PORTAL_READ_CONFIG_ITEM = 352; // 'w'
    /**
     * Serial scanner read configuration parameter command for DIO.
     */
    public static final int DIO_PORTAL_READ_CONFIG_PARAM = 353; // 'v'
    /**
     * Serial scanner display data command for DIO.
     */
    public static final int DIO_PORTAL_DISPLAY_DATA = 354;
    /**
     * Serial scanner display info command for DIO.
     */
    public static final int DIO_PORTAL_DISPLAY_INFO = 355;
    /**
     * Serial scanner write configuration item command for DIO.
     */
    public static final int DIO_PORTAL_WRITE_CONFIG_ITEM = 356; // 'c'
    /**
     * Serial scanner enable command for DIO.
     */
    public static final int DIO_PORTAL_ENABLE = 357; // 'e'
    /**
     * Serial scanner disable command for DIO.
     */
    public static final int DIO_PORTAL_DISABLE = 358; // 'd'
    /**
     * Serial scanner shutdown command for DIO.
     */
    public static final int DIO_PORTAL_SHUTDOWN = 359; // 'o'
    /**
     * Serial scanner POS connected for DIO.
     */
    public static final int DIO_PORTAL_POS_CONNECTED = 360; // 'a'
    /**
     * Serial scanner reset command for DIO.
     */
    public static final int DIO_PORTAL_RESET = 361; // 'r'
    /**
     * Serial scanner restart command for DIO.
     */
    public static final int DIO_PORTAL_RESTART = 362; // 'u'
    /**
     * Serial scanner request image for DIO.
     */
    public static final int DIO_PORTAL_REQ_IMAGE = 363; // 'q'
    /**
     * Serial scanner transit state command for DIO.
     */
    public static final int DIO_PORTAL_TRANS_STATE = 364; // 't'
    /**
     * Serial scanner sig item for DIO.
     */
    public static final int DIO_PORTAL_SIG_ITEM = 365; // 's'
    /**
     * Serial scanner id item for DIO.
     */
    public static final int DIO_PORTAL_ID_ITEM = 366; // 'i'
    /**
     * Serial scanner add item command for DIO.
     */
    public static final int DIO_PORTAL_ADD_ITEM = 367; // 'x'
    /**
     * Serial scanner identify command for DIO.
     */
    public static final int DIO_PORTAL_IDENTIFY = 368; // 'k'
    /**
     * Serial scanner health extended command for DIO.
     */
    public static final int DIO_PORTAL_HEALTH_EXTENDED = 369; // 'g'
    /**
     * Serial scanner statistics command for DIO.
     */
    public static final int DIO_PORTAL_STATISTICS = 370; // 'n'
    /**
     * Serial scanner play sound command for DIO.
     */
    public static final int DIO_PORTAL_PLAY_SOUND = 371; // 'b'
    /**
     * Serial scanner sleep deeply command for DIO.
     */
    public static final int DIO_PORTAL_SLEEP_DEEPLY = 372; // 'z'
    /**
     * Serial scanner set item id command for DIO.
     */
    // JAVAPOS_SWCR_232 - Support Set Item ID.
    public static final int DIO_PORTAL_SET_ITEM_ID = 373; // 'j'
    /**
     * Serial scanner set item color command for DIO.
     */
    // JAVAPOS_SWCR_231 - Support Set Item Color
    public static final int DIO_PORTAL_SET_ITEM_COLOR = 374; // 'l'
    /**
     * Serial scanner operator sign-on command for DIO.
     */
    // JAVAPOS_SWCR_230 - Support Operator Sign-On
    public static final int DIO_PORTAL_OPERATOR_SIGNON = 375; // 'm'
    /**
     * Serial scanner label data message for DIO.
     */
    // JAVAPOS_SWCR_229 - Support Label Data Message
    public static final int DIO_PORTAL_LABEL_DATA_MSG = 376; // 'B'

    /**
     * Serial scanner phone mode control command for DIO.
     */
    public static final int DIO_CELL_PHONE_MODE_CONTROL = 340;
    /**
     * Serial scanner picture taking control command for DIO.
     */
    public static final int DIO_PICTURE_TAKING_CONTROL = 341;
    /**
     * Serial scanner read config item command for DIO.
     */
    public static final int DIO_READ_CONFIG_ITEM = 342;
    /**
     * Serial scanner write config item command for DIO.
     */
    public static final int DIO_WRITE_CONFIG_ITEM = 343;
    /**
     * Serial scanner commit config items command for DIO.
     */
    public static final int DIO_COMMIT_CONFIG_ITEMS = 344;
    /**
     * Serial scanner query config item command for DIO.
     */
    public static final int DIO_QUERY_CONFIG_ITEM = 382;
    /**
     * Serial scanner open service port command for DIO.
     */
    public static final int DIO_OPEN_SERVICE_PORT_HH = 384;
    /**
     * Serial scanner close service port command for DIO.
     */
    public static final int DIO_CLOSE_SERVICE_PORT_HH = 385;
    /**
     * Serial scanner send generic service command for DIO.
     */
    public static final int DIO_SEND_GEN_SERVICE_CMD_HH = 386;
    /**
     * Serial scanner read statistics log count command for DIO.
     */
    public static final int DIO_READ_STAT_LOG_COUNT = 345;
    /**
     * Serial scanner get statistics log items command for DIO.
     */
    public static final int DIO_READ_STAT_LOG_ITEMS = 346;
    /**
     * Serial scanner get event log items command for DIO.
     */
    public static final int DIO_READ_EVENT_LOG_ITEMS = 347;
    /**
     * Serial scanner get last event log command for DIO.
     */
    public static final int DIO_READ_EVENT_LOG_LAST_EVENT = 348;
    /**
     * Serial scanner clear event logs for DIO.
     */
    public static final int DIO_CLEAR_EVENT_LOG = 349;
    /**
     * Serial scanner save event log command for DIO.
     */
    public static final int DIO_SAVE_EVENT_LOG = 350;
    /**
     * Serial scanner save statistics log for DIO.
     */
    public static final int DIO_SAVE_STATS_LOG = 351;
    /**
     * Serial scanner get scale sentry status command for DIO.
     */
    public static final int DIO_GET_SCALE_SENTRY_STATUS = 352;
    /**
     * Serial scanner clear scale sentry command for DIO.
     */
    public static final int DIO_CLEAR_SCALE_SENTRY = 353;
    /**
     * Serial scanner disable command for DIO.
     */
    public static final int DIO_SCANNER_DISABLE = 377;
    /**
     * Serial scanner enable command for DIO.
     */
    public static final int DIO_SCANNER_ENABLE = 378;
    /**
     * Serial scanner stream on command for DIO.
     */
    public static final int DIO_EXT_VIDEO_STREAM_ON = 379;
    /**
     * Serial scanner stream off command for DIO.
     */
    public static final int DIO_EXT_VIDEO_STREAM_OFF = 380;
    /**
     * Serial scanner get the stream status command for DIO.
     */
    public static final int DIO_EXT_VIDEO_STREAM_STATUS = 381;
    /**
     * Serial scanner enter phone ecomm command for DIO.
     */
    public static final int DIO_CELL_PHONE_ECOMM_ENTER = 373;
    /**
     * Serial scanner exit phone ecomm command for DIO.
     */
    public static final int DIO_CELL_PHONE_ECOMM_EXIT = 374;
    /**
     * Serial scanner enter phone scan command for DIO.
     */
    public static final int DIO_CELL_PHONE_SCAN_ENTER = 375;
    /**
     * Serial scanner exit phone scan command for DIO.
     */
    public static final int DIO_CELL_PHONE_SCAN_EXIT = 376;
    /**
     * Serial scanner picture taking control command for DIO.
     */
    // JAVAPOS_SWCR_280 - Implement Picture Taking Control for 9x00.
    public static final int DIO_PICTURE_TAKING_CONTROL_9X00 = 382;

    /**
     * Serial scanner reset command for DIO.
     */
    public static final int DIO_SCSCANNER_CMP_RESET = 502; // same as hard reset
    /**
     * Serial scanner status command for DIO.
     */
    public static final int DIO_SCSCANNER_CMP_STATUS = 503; // same as above
    /**
     * Serial scanner not on file for DIO.
     */
    public static final int DIO_SCSCANNER_CMP_NOT_ON_FILE = 508; // same as above
    /**
     * Serial scanner tone command for DIO.
     */
    public static final int DIO_SCSCANNER_CMP_TONE = 501; // same as beep good
    /**
     * Serial scanner read pace command for DIO.
     */
    public static final int DIO_SCSCANNER_CMP_PACESETTER = 506; // same as read pace
//    public static final int DIO_SCSCANNER_CMP_READ_ROM = 504; // behavior unknown
//    public static final int DIO_SCSCANNER_CMP_ROM_VERSION = 505; // behavior unknown

    /**
     * Serial scanner capture image now command for DIO.
     */
    public static final int DIO_IMAGE_CAPTURE_NOW = 2465;
    /**
     * Serial scanner capture image on next trigger command for DIO.
     */
    public static final int DIO_IMAGE_ON_NEXT_TRIGGER = 2466;
    /**
     * Serial scanner capture image on next decode command for DIO.
     */
    public static final int DIO_IMAGE_ON_NEXT_DECODE = 2467;
    /**
     * Serial scanner capture image on trigger mode enable command for DIO.
     */
    public static final int DIO_IMAGE_ON_TRIGGER_MODE = 2468;
    /**
     * Serial scanner capture image on decode mode enable command for DIO.
     */
    public static final int DIO_IMAGE_ON_DECODE_MODE = 2469;
    /**
     * Serial scanner disable any capture image mode command for DIO.
     */
    public static final int DIO_DISABLE_IMAGE_MODE = 2470;
    /**
     * Serial scanner general command for DIO.
     */
    public static final int DIO_GENERAL_CMD = 999;

    /* DirectIO Command for RS232 Standard Scale */
    /**
     * Serial Scale self test command for DIO.
     */
    //                      DIO_SCALE_STATUS - (defined above)
    public static final int DIO_SCALE_SELF_TEST = 30;


    /**
     * Get device bus type command for DIO. It works for all devices.
     */
    /* DirectIO command for all devices */
    public static final int DIO_DEV_PROTOCOL = 41; // "RS232-SC","RS232","USB"

    /* DirectIO command extensions for USBOEM 3.1 */
    /**
     * USB scanner enable or disable additional symbologies command for DIO.
     */
    public static final int DIO_ENAB_DISAB_ADDL_SYM = 3000;
    /**
     * USB scanner get device information command for DIO.
     */
    public static final int DIO_DEVICE_INFORMATION = 3001;
    /**
     * USB scanner request scanner generic management information command for DIO.
     */
    public static final int DIO_REQ_SCANNER_GEN_MGT = 3002;
    /**
     * USB scanner request scanner vendor-specific management information command for DIO.
     */
    public static final int DIO_REQ_SCANNER_VEN_MGT = 3003;
    /**
     * USB scanner request scale generic management information command for DIO.
     */
    public static final int DIO_REQ_SCALE_GEN_MGT = 3004;
    /**
     * USB scanner request scale vendor-specific management information for DIO.
     */
    public static final int DIO_REQ_SCALE_VEN_MGT = 3005;
    /**
     * USB scanner read the digital watermark label filter command for DIO.
     */
    public static final int DIO_READ_DWM_LABELFILTER = 3006;
    /**
     * USB scanner clear the digital watermark label filter command for DIO.
     */
    public static final int DIO_CLEAR_DWM_LABELFILTER = 3007;
    /**
     * USB scanner append to the digital watermark label filter command for DIO.
     */
    public static final int DIO_APPEND_DWM_LABELFILTER = 3008;

    /**
     * Used charset.
     */
    public static final Charset HDL_CHARSET = StandardCharsets.ISO_8859_1;

//    public static final int DLS_RFID_DISABLE_TAG = 4000;
//    public static final int DLS_RFID_LOCK_TAG = 4001;
//    public static final int DLS_RFID_READ_TAGS = 4002;
//    public static final int DLS_RFID_START_READ_TAGS = 4003;
//    public static final int DLS_RFID_STOP_READ_TAGS = 4004;
//    public static final int DLS_RFID_WRITE_TAG_DATA = 4005;
//    public static final int DLS_RFID_WRITE_TAG_ID = 4006;

    //DLSStatistics
    /**
     * Statistics tag for camera health.
     */
    public static final String DLS_S_CAMERA_HEALTH = "CameraHealth";
    /**
     * Statistics tag for eas health.
     */
    public static final String DLS_S_EAS_HEALTH = "EASHealth";
//    public static final String DLS_S_USB_HH_CONNECTED = "USBHandheldConnected";
    /**
     * Statistics tag to query if a USB serial dongle is connected.
     */
    public static final String DLS_S_USB_SERIAL_CONNECTED = "USBSerialDongleConnected";
    /**
     * Statistics tag for IPE0 health.
     */
    public static final String DLS_S_IPE_0_HEALTH = "IPE0Health";
    /**
     * Statistics tag for IPE1 health.
     */
    public static final String DLS_S_IPE_1_HEALTH = "IPE1Health";
    /**
     * Statistics tag for IPE2 health.
     */
    public static final String DLS_S_IPE_2_HEALTH = "IPE2Health";
    /**
     * Statistics tag for IPE3 health.
     */
    public static final String DLS_S_IPE_3_HEALTH = "IPE3Health";
    /**
     * Statistics tag for scale sentry health.
     */
    public static final String DLS_S_SCALE_SENTRY_HEALTH = "ScaleSentryHealth";
    /**
     * Statistics tag for firmware version comparison capability.
     */
    public static final String DLS_S_CAP_COMP_FW_VER = "CapCompareFirmwareVersion";
    /**
     * Statistics tag for power reporting capability.
     */
    public static final String DLS_S_CAP_PWR_REPORT = "CapPowerReporting";
    /**
     * Statistics tag for statistics reporting capability.
     */
    public static final String DLS_S_CAP_STATS_REPORT = "CapStatisticsReporting";
    /**
     * Statistics tag for firmware update capability.
     */
    public static final String DLS_S_CAP_UPDATE_FW = "CapUpdateFirmware";
    /**
     * Statistics tag for statistics update capability.
     */
    public static final String DLS_S_CAP_UPDATE_STATS = "CapUpdateStatistics";
    /**
     * Statistics tag for configuration file version number.
     */
    public static final String DLS_S_SCANNER_CONFIG_FILE_ID = "ConfigFileVersionNumber";
    /**
     * Statistics tag for device category.
     */
    public static final String DLS_S_DEVICE_CATEGORY = "DeviceCategory";
    /**
     * Statistics tag for display version.
     */
    public static final String DLS_S_DISPLAY_VERSION = "DisplayVersion";
    /**
     * Statistics tag for EAS deactivations.
     */
    public static final String DLS_S_EAS_DEACTIVATED = "EASDeactivations";
    /**
     * Statistics tag for EAS manual presses.
     */
    public static final String DLS_S_EAS_MANUAL = "EASManualPresses";
    /**
     * Statistics tag for EAS version.
     */
    public static final String DLS_S_EAS_VERSION = "EASVersion";
    /**
     * Statistics tag for firmware version number.
     */
    public static final String DLS_S_FIRMWARE_VERSION = "FirmwareVersionNumber";
    //public static final String DLS_S_GOOD_SCAN_COUNT        = "GoodScanCount";
    /**
     * Statistics tag for good weight read count.
     */
    public static final String DLS_S_GOOD_WEIGHT_READ_COUNT = "GoodWeightReadCount";
    /**
     * Statistics tag for horizontal laser health.
     */
    public static final String DLS_S_H_LASER_HEALTH = "HorizontalLaserHealth";
    /**
     * Statistics tag for hours powered count.
     */
    public static final String DLS_S_HOURS_POWERED_COUNT = "HoursPoweredCount";
    /**
     * Statistics tag for total resets.
     */
    public static final String DLS_S_TOTAL_RESETS = "TotalResets";
    /**
     * Statistics tag for error resets.
     */
    public static final String DLS_S_ERROR_RESETS = "ErrorResets";
    /**
     * Statistics tag for vertical IPE forced resets.
     */
    public static final String DLS_S_VERT_IPE_FORCED_RESETS = "VerticalIPEForcedResets";
    /**
     * Statistics tag for horizontal IPE forced resets.
     */
    public static final String DLS_S_HORZ_IPE_FORCED_RESETS = "HorizontalIPEForcedResets";
    /**
     * Statistics tag for forced 2D resets.
     */
    public static final String DLS_S_2D_FORCED_RESETS = "Forced2DResets";
    /**
     * Statistics tag for TDR forced resets.
     */
    public static final String DLS_S_TDR_FORCED_RESETS = "TDRForcedResets";
    /**
     * Statistics tag for vertical excessive resets.
     */
    public static final String DLS_S_VERT_EXCESSIVE_RESETS = "VertExcessiveResets";
    /**
     * Statistics tag for horizontal excessive resets.
     */
    public static final String DLS_S_HORZ_EXCESSIVE_RESETS = "HorzExcessiveResets";
    /**
     * Statistics tag for 2D excessive resets.
     */
    public static final String DLS_S_2D_EXCESSIVE_RESETS = "2DExcessiveResets";
    /**
     * Statistics tag for TRD excessive resets.
     */
    public static final String DLS_S_TDR_EXCESSIVE_RESETS = "TRDExcessiveResets";
    /**
     * Statistics tag for POS zero requests.
     */
    public static final String DLS_S_POS_ZERO_REQUESTS = "POSZeroRequests";
    /**
     * Statistics tag for enforced zero events.
     */
    public static final String DLS_S_ENFORCE_ZERO_EVENTS = "EnforcedZeroEvents";
    /**
     * Statistics tag for scale sentry events.
     */
    public static final String DLS_S_SCALE_SENTRY_EVENTS = "ScaleSentryEvents";
    /**
     * Statistics tag for EAS runtime faults.
     */
    public static final String DLS_S_EAS_RT_FAULTS = "EASRuntimeFaults";
    /**
     * Statistics tag for interface.
     */
    public static final String DLS_S_INTERFACE = "Interface";
    /**
     * Statistics tag for hardware ID.
     */
    public static final String DLS_S_HARDWARE_ID = "HardwareID";
    /**
     * Statistics tag for Comet ROM ID.
     */
    public static final String DLS_S_UNIVERSAL_ROM_ID = "CometROMID";
    /**
     * Statistics tag for Comet boot ROM ID.
     */
    public static final String DLS_S_UNIVERSAL_BOOT_ROM_ID = "CometBootROMID";
    /**
     * Statistics tag for motor time.
     */
    public static final String DLS_S_MOTOR_TIME = "MotorTime";
    /**
     * Statistics tag for laser time.
     */
    public static final String DLS_S_LASER_TIME = "LaserTime";
    /**
     * Statistics tag for custom data.
     */
    public static final String DLS_S_SCALE_CAL_TIME = "CustomData";
    /**
     * Statistics tag for model number.
     */
    public static final String DLS_S_MODEL_NAME = "ModelNumber";
    /**
     * Statistics tag for motor health.
     */
    public static final String DLS_S_MOTOR_HEALTH = "MotorHealth";
    /**
     * Statistics tag for remote display health.
     */
    public static final String DLS_S_REMOTE_DISPLAY_HEALTH = "RemoteDisplayHealth";
    /**
     * Statistics tag for scale calibrations.
     */
    public static final String DLS_S_SCALE_CALS = "ScaleCalibrations";
    /**
     * Statistics tag for scale health.
     */
    public static final String DLS_S_SCALE_HEALTH = "ScaleHealth";
    /**
     * Statistics tag for display capability.
     */
    public static final String DLS_S_SCALE_CAP_DISP = "CapDisplay";
    /**
     * Statistics tag for display text capability.
     */
    public static final String DLS_S_SCALE_CAP_DISP_TEXT = "CapDisplayText";
    /**
     * Statistics tag for price calculating capability.
     */
    public static final String DLS_S_SCALE_CAP_PRICE_CALC = "CapPriceCalculating";
    /**
     * Statistics tag for tare weight capability.
     */
    public static final String DLS_S_SCALE_CAP_TARE_WT = "CapTareWeight";
    /**
     * Statistics tag for zero scale capability.
     */
    public static final String DLS_S_SCALE_CAP_ZERO = "CapZeroScale";
    /**
     * Statistics tag for status update capability.
     */
    public static final String DLS_S_SCALE_STAT_UPDATE = "CapStatusUpdate";
    /**
     * Statistics tag for scale information.
     */
    public static final String DLS_S_SCALE_INFO = "ScaleInformation";
    /**
     * Statistics tag for zeroed scale.
     */
    public static final String DLS_S_SCALE_ZEROS = "ScaleZeroed";
    // new additions for the Control Ver 1.14
    /**
     * Statistics tag for scale freeze capability.
     */
    public static final String DLS_S_SCALE_CAP_FREEZE = "CapFreezeValue";
    /**
     * Statistics tag for scale read with tare capability.
     */
    public static final String DLS_S_SCALE_CAP_READ_WITH_TARE = "CapReadLiveWeightWithTare";
    /**
     * Statistics tag for set prize calculation mode capability.
     */
    public static final String DLS_S_SCALE_CAP_PRICE_CALC_MODE = "CapSetPriceCalculationMode";
    /**
     * Statistics tag for set unit price with weight unit capability.
     */
    public static final String DLS_S_SCALE_CAP_PRICE_WITH_WEIGHT = "CapSetUnitPriceWithWeightUnit";
    /**
     * Statistics tag for price with weight capability.
     */
    public static final String DLS_S_SCALE_CAP_SPECIAL_TARE = "CapSpecialTare";
    /**
     * Statistics tag for tare priority capability.
     */
    public static final String DLS_S_SCALE_CAP_TARE_PRIORITY = "CapTarePriority";
    /**
     * Statistics tag for scale minimum weight.
     */
    public static final String DLS_S_SCALE_MIN_WEIGHT = "MinimumWeight";
    /**
     * Statistics tag for scanner board serial number.
     */
    public static final String DLS_S_SCANNER_BOARD_SERIAL = "ScannerBoardSerialNumber";
    /**
     * Statistics tag for boot ROM ID.
     */
    public static final String DLS_S_SCANNER_BOOT_ROM_ID = "ScannerBootROMID";
    /**
     * Statistics tag for scanner interface.
     */
    public static final String DLS_S_SCANNER_INTERFACE = "ScannerInterface";
    /**
     * Statistics tag for scanner revision number.
     */
    public static final String DLS_S_SCANNER_REVISION = "ScannerRevisionNumber";
    /**
     * Statistics tag for scanner time.
     */
    public static final String DLS_S_SCANNER_TIME = "ScannerTime";
    /**
     * Statistics tag for serial number.
     */
    public static final String DLS_S_SERIAL_NUMBER = "SerialNumber";
    /**
     * Statistics tag for vertical laser health.
     */
    public static final String DLS_S_V_LASER_HEALTH = "VerticalLaserHealth";
    /**
     * Statistics tag for scale mode.
     */
    public static final String DLS_S_SCALE_MODE = "ScaleMode";
    /**
     * Statistics tag for scale max weight.
     */
    public static final String DLS_S_SCALE_MAX_WEIGHT = "MaxWeight";
    /**
     * Statistics tag for service version.
     */
    public static final String DLS_S_SERVICE_VERSION = "ServiceVersion";
    /**
     * Statistics tag for control version.
     */
    public static final String DLS_S_CONTROL_VERSION = "ControlVersion";
    /**
     * Statistics tag for manufacture date.
     */
    public static final String DLS_S_MANUFACTURE_DATE = "ManufactureDate";
    /**
     * Statistics tag for manufacturer name.
     */
    public static final String DLS_S_MANUFACTURE_NAME = "ManufacturerName";
    /**
     * Statistics tag for install date.
     */
    public static final String DLS_S_INSTALL_DATE = "InstallDate";
    /**
     * Statistics tag for communication error count.
     */
    public static final String DLS_S_COMM_ERROR_COUNT = "CommunicationErrorCount";
    /**
     * Statistics tag for power notify.
     */
    public static final String DLS_S_POWER_NOTIFY = "PowerNotify";
    /**
     * Statistics tag for power state.
     */
    public static final String DLS_S_POWER_STATE = "PowerState";
    /**
     * Statistics tag for application revision level.
     */
    public static final String DLS_S_APPLICATION_REVISION = "ApplicationRevisionLevel";
    /**
     * Statistics tag for UPOS version.
     */
    public static final String DLS_S_UPOS_VERSION = "UnifiedPOSVersion";
    /**
     * Statistics tag for caption.
     */
    public static final String DLS_S_CAPTION = "Caption";
    /**
     * Statistics tag for description.
     */
    public static final String DLS_S_DESCRIPTION = "Description";
    /**
     * Statistics tag for physical device description.
     */
    public static final String DLS_S_PHYSICAL_DESCRIPTION = "PhysicalDeviceDescription";
    /**
     * Statistics tag for physical device name.
     */
    public static final String DLS_S_PHYSICAL_NAME = "PhysicalDeviceName";
    /**
     * Statistics tag for async mode.
     */
    public static final String DLS_S_ASYNC_MODE = "AsyncMode";
    /**
     * Statistics tag for scale max display text characters.
     */
    public static final String DLS_S_SCALE_MAX_TXT_CHRS = "MaxDisplayTextChars";
    /**
     * Statistics tag for FPGA version.
     */
    public static final String DLS_S_FPGA_VERSION = "FPGAVersion";
    /**
     * Statistics tag for TDR type.
     */
    public static final String DLS_S_TDR_TYPE = "TDRType";
    /**
     * Statistics tag for TDR FPGA version.
     */
    public static final String DLS_S_TDR_FPGA_VERSION = "TDR_FPGAVersion";
    /**
     * Statistics tag for IPE application version.
     */
    public static final String DLS_S_IPE_APP_VERSION = "IPEAppVersion";
    /**
     * Statistics tag for USB loader version.
     */
    public static final String DLS_S_USB_LOADER_VERSION = "USBLoaderVersion";
    /**
     * Statistics tag for SDRAM configuration version.
     */
    public static final String DLS_S_SDRAM_CFG_VERSION = "SDRAMCfgVersion";
    /**
     * Statistics tag for vision library version.
     */
    public static final String DLS_S_VL_VERSION = "VisionLibraryVersion";
    /**
     * Statistics tag for virtual scan line library version.
     */
    public static final String DLS_S_VSL_VERSION = "VirtualScanLineLibraryVersion";
    /**
     * Statistics tag for attached devices number.
     */
    public static final String DLS_S_NUM_ATTACHED_DEVICES = "NumberOfAttachedDevices";

//    /* Constants for PID of DL-based USB COM scanners
//     * @TODO: Remove the product id versions as only using vid and excluding
//     * PN events on the service port.
//     */
//    public static final String DLA_SCANNER_VID = "VID_05F9";
//    public static final String DLA_RFID_SCANNER_VID = "VID_0403";
//    public static final String DLA_HH_SCANNER_VID = "080C";
//    public static final String TEST_PORT_PID = "PID_4000";
//
//    //are these necessary?
//    public static final String DLA_SCANNER_PID_1 = "PID_4002";
//    public static final String DLA_SCANNER_PID_2 = "PID_4202";
//    public static final String DLA_SCANNER_PID_3 = "PID_4204";
//    public static final String DLA_SCANNER_PID_4 = "PID_4502";
//    public static final String DLA_SCANNER_PID_5 = "PID_4504";
//    public static final String DLA_SCANNER_PID_6 = "PID_4505";
//    public static final String DLA_SCANNER_PID_7 = "PID_4506";
//    public static final String DLA_SCANNER_PID_8 = "PID_4507";
//    public static final String DLA_SCANNER_PID_9 = "PID_4509";
//    public static final String DLA_SCANNER_PID_10 = "PID_4601";
//    public static final String DLA_SCANNER_PID_11 = "PID_4602";
//    public static final String DLA_SCANNER_PID_12 = "PID_4000";
//    public static final String DLA_SCANNER_PID_13 = "PID_4001";
//    public static final String DLA_SCANNER_PID_14 = "PID_4003";
//    public static final String DLA_SCANNER_PID_15 = "PID_0400";
//    public static final String DLA_SCANNER_PID_16 = "PID_1511";


//    /* Const for PID of HP-based scanners */
//    public static final String HP_SCANNER_VID = "VID_03F0";
//    public static final String HP_IMAGING_SCANNER = "PID_0339";
//    public static final String HP_PRESENTATION_SCANNER = "PID_0239";
//    public static final String HP_LINEAR_SCANNER = "PID_0D39";
//    public static final String HP_INTEGRATED_SCANNER = "PID_0E39";
//    public static final String HP_KIOSK_SCANNER = "PID_1039";
//    public static final String HP_WIRELESS_SCANNER = "PID_1139";
//    public static final String HP_SCANNER_7 = "PID_1339";
//    public static final String HP_SCANNER_8 = "PID_1439";
//    public static final String HP_SCANNER_9 = "PID_1F39";
//    public static final String HP_SCANNER_10 = "PID_2439";


    /**
     * PowerScan 9xxx statistics battery charge cycles.
     */
    /* n-tary device specific fields */
    public static final String DLS_SECONDARY_BATTERY_CHG_CYCLES = "BatteryChargeCycles";

//    /* WMI Names */
//    public static final String DLS_WMI_DEVICE_ID = "DeviceID";

    /* Const for RS232 STD Scale self test response */
    /**
     * Serial scale self test response EEPROM1.
     */
    public static final int SCALE_TEST_EEPROM1 = 0x01;
    /**
     * Serial scale self test response EEPROM2.
     */
    public static final int SCALE_TEST_EEPROM2 = 0x02;
    /**
     * Serial scale self test response RAM.
     */
    public static final int SCALE_TEST_RAM = 0x04;
    /**
     * Serial scale self test response processor.
     */
    public static final int SCALE_TEST_PROCESSOR = 0x08;
    /**
     * Serial scale self test response ROM.
     */
    public static final int SCALE_TEST_ROM = 0x10;
    /**
     * Serial scale status response motion.
     */
    public static final int SCALE_STATUS_MOTION = 0x01;
    /**
     * Serial scale status response range.
     */
    public static final int SCALE_STATUS_RANGE = 0x02;
    /**
     * Serial scale status response under zero.
     */
    public static final int SCALE_STATUS_UNDER_ZERO = 0x04;
    /**
     * Serial scale status response over zero.
     */
    public static final int SCALE_STATUS_OUTSIDE_ZERO = 0x08;
    /**
     * Serial scale status response center of zero.
     */
    public static final int SCALE_STATUS_CENTEROF_ZERO = 0x10;
}
