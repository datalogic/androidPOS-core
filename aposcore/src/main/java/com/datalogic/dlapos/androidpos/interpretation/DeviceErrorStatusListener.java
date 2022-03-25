package com.datalogic.dlapos.androidpos.interpretation;

/**
 * Interface of the listener for errors and status events.
 */
public interface DeviceErrorStatusListener {

    //region Errors
    /**
     * Generic command error.
     */
    int ERR_CMD = -100;
    /**
     * No weight on the scale.
     */
    int ERR_NO_WEIGHT = -101;
    /**
     * Data related error.
     */
    int ERR_DATA = -102;
    /**
     * Error reading from the device.
     */
    int ERR_READ = -103;
    /**
     * No display connected to the scale.
     */
    int ERR_NO_DISPLAY = -104;
    /**
     * Generic hardware error.
     */
    int ERR_HARDWARE = -105;
    /**
     * Command rejected by the device.
     */
    int ERR_CMD_REJECT = -106;
    /**
     * Weight capacity error for scales.
     */
    int ERR_CAPACITY = -107;
    /**
     * Scale requires zeroing.
     */
    int ERR_REQUIRES_ZEROING = -108;
    /**
     * Can not perform an operation while warming up.
     */
    int ERR_WARMUP = -109;
    /**
     * Duplicate weight scale's error.
     */
    int ERR_DUPLICATE = -110;
    /**
     * Can not perform an operation while the device is flashing.
     */
    int ERR_FLASHING = -111;
    /**
     * Can not perform an operation because the device is busy.
     */
    int ERR_BUSY = -112;
    /**
     * Check digit is not correct.
     */
    int ERR_CHECKDIGIT = -113;
    /**
     * Direct IO command is not allowed.
     */
    int ERR_DIO_NOT_ALLOWED = -114;
    /**
     * Direct IO command is undefined.
     */
    int ERR_DIO_UNDEFINED = -115;
    /**
     * The device has been removed.
     */
    int ERR_DEVICE_REMOVED = -116;
    /**
     * Scale reach zero when not allowed.
     */
    int ERR_SCALE_AT_ZERO = -117;
    /**
     * Weight under zero.
     */
    int ERR_SCALE_UNDER_ZERO = -118;
    /**
     * The device has been reattached.
     */
    int ERR_DEVICE_REATTACHED = -120;
    /**
     * Configuration command rejected.
     */
    int ERR_CFG_CMD_REJECTED = -121;
    /**
     * Timeout occured.
     */
    int ERR_TIMEOUT = -122;
    //endregion

    //region Status
    /**
     * The device is alive.
     */
    int STATUS_ALIVE = -200;
    /**
     * The device is not alive.
     */
    int STATUS_NOT_ALIVE = -201;
    /**
     * The device is enabled.
     */
    int STATUS_ENABLED = -202;
    /**
     * The device is not enabled.
     */
    int STATUS_NOT_ENABLED = -203;

    //endregion

    /**
     * Function called when an error occurs.
     *
     * @param nErrorCode the error code of the event.
     */
    void onDeviceError(int nErrorCode);

    /**
     * Function called when a status event occurs.
     *
     * @param nStatusCode the status code of the event
     */
    void onDeviceStatus(int nStatusCode);
}
