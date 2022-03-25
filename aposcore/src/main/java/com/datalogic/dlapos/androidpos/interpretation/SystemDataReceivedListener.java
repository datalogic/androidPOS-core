package com.datalogic.dlapos.androidpos.interpretation;

/**
 * Interface of the listener for system data events.
 */
public interface SystemDataReceivedListener {
    /**
     * Method called when System Data Received.
     *
     * @param rawData byte array containing the received system data.
     */
    void onSystemDataReceived(byte[] rawData);
}
