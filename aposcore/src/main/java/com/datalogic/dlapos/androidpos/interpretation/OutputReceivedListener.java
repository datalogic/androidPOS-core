package com.datalogic.dlapos.androidpos.interpretation;

/**
 * Interface of the listener for output received events.
 */
public interface OutputReceivedListener {
    /**
     * Function called when output is received.
     *
     * @param id the id of the output.
     */
    void onOutputReceived(int id);
}
