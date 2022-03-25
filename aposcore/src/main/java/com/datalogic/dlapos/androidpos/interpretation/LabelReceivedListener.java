package com.datalogic.dlapos.androidpos.interpretation;

/**
 * Interface of the listener for labels events.
 */
public interface LabelReceivedListener {
    /**
     * Called when a label is received.
     *
     * @param rawData     byte array containing the raw label data including identifiers.
     * @param decodedData byte array containing the decoded label data.
     * @param type        int indicating the type of label as specified in the LabelIdentifiers.csv file.
     */
    void onLabelReceived(byte[] rawData, byte[] decodedData, int type);
}
