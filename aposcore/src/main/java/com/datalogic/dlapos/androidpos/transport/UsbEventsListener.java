package com.datalogic.dlapos.androidpos.transport;

import android.hardware.usb.UsbDevice;

/**
 * Listener for USB devices events.
 */
public interface UsbEventsListener {

    /**
     * Function to notify a device attached.
     *
     * @param device The attached Device.
     */
    void onDeviceAttached(UsbDevice device);

    /**
     * Function to notify a device detached.
     *
     * @param device The detached Device.
     */
    void onDeviceDetached(UsbDevice device);
}
