package com.datalogic.dlapos.androidpos.interpretation;

/**
 * Interface of the listener for items received events.
 */
public interface ItemReceivedListener {
    /**
     * Function called when an item is received.
     *
     * @param rawData raw data of the item.
     */
    void onItemReceived(byte[] rawData);
}
