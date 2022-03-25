package com.datalogic.dlapos.androidpos.common;

/**
 * {@code DLSState} holds the valid scanner states used for device state
 * tracking.
 */
public enum DLSState {

    /**
     * The device is closed.
     */
    CLOSED,
    /**
     * The device is opened.
     */
    OPENED,
    /**
     * The device is claimed.
     */
    CLAIMED,
    /**
     * The device is enabled.
     */
    ENABLED,
    /**
     * The device is disabled.
     */
    DISABLED,
    /**
     * The device is released.
     */
    RELEASED;
}
