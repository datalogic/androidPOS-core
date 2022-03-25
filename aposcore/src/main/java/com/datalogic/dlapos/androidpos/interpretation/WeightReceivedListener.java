package com.datalogic.dlapos.androidpos.interpretation;

/**
 * Interface of the listener for weight received events.
 */
public interface WeightReceivedListener {
    /**
     * Function called when new weight is received.
     *
     * @param weight the received weight.
     */
    void onWeightReceived(int weight);
}
