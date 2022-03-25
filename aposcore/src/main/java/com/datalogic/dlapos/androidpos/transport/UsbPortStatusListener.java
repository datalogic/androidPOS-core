package com.datalogic.dlapos.androidpos.transport;

/**
 * Listener for port status changes.
 */
public interface UsbPortStatusListener {
    /**
     * Enumerator containing error status codes.
     */
    enum ErrorStatus {
        /**
         * Missing USB access permission.
         */
        MISSING_PERMISSION,
        /**
         * The USB interface can not be found.
         */
        CAN_NOT_FIND_INTERFACE,
        /**
         * The USB endpoint can not be found.
         */
        CAN_NOT_FIND_ENDPOINTS,
        /**
         * Can not open a USB connection.
         */
        CAN_NOT_OPEN_CONNECTION,
        /**
         * Can not find the required device.
         */
        CAN_NOT_FIND_DEVICE,
        /**
         * The USB-COM port can not be found.
         */
        CAN_NOT_FIND_COM_PORT,
        /**
         * The USB-COM port can not be open.
         */
        CAN_NOT_OPEN_COM_PORT
    }

    /**
     * Function to notify the opening of the port.
     */
    void portOpened();

    /**
     * Function to notify an error on the port.
     *
     * @param status The error status of the failure.
     */
    void portError(ErrorStatus status);

    /**
     * Function to notify the closing of the port.
     */
    void portClosed();
}
