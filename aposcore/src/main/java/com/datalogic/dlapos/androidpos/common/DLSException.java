package com.datalogic.dlapos.androidpos.common;

/**
 * Custom Datalogic exception.
 */
public class DLSException extends Exception {
    protected int errorCode;

    /**
     * Constructor.
     *
     * @param errorCode the error code of the exception.
     * @param message   the message of the exception.
     */
    public DLSException(int errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    /**
     * Function to get the error code.
     *
     * @return the error code of the exception.
     */
    public int getErrorCode() {
        return this.errorCode;
    }

    /**
     * Function to get the error message.
     *
     * @return the exception message.
     */
    @Override
    public String getMessage() {
        return ("0x" + Integer.toHexString(getErrorCode()) + ", " + super.getMessage());
    }

}
