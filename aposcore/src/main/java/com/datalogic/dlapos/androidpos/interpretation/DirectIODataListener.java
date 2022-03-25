package com.datalogic.dlapos.androidpos.interpretation;

/**
 * Interface of the listener for DirectIO data events.
 */
public interface DirectIODataListener {

    /**
     * Method called when data is received for a Direct I/O command.
     *
     * @param cmd int indicating the Direct I/O command.
     * @param buf byte array containing the data payload.
     */
    void onDirectIOData(int cmd, byte[] buf);
}
