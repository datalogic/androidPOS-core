package com.datalogic.dlapos.androidpos.transport;

/**
 * Interface of the listener for data received.
 */
public interface DataReceivedListener {
    /**
     * Function called when new data are received.
     *
     * @param buf data received.
     * @param len length of the data.
     */
    void onDataReceived(byte[] buf, int len);
}
