package com.datalogic.dlapos.androidpos.service;

import android.util.Log;

import com.datalogic.dlapos.androidpos.common.DLSException;
import com.datalogic.dlapos.androidpos.common.DLSProperties;
import com.datalogic.dlapos.androidpos.common.DLSState;
import com.datalogic.dlapos.androidpos.interpretation.DLSDevice;

public class EnableDisablePoll implements Runnable {
    private final static String TAG = EnableDisablePoll.class.getSimpleName();

    private DLSDevice device;
    private DLSProperties options;
    private boolean busy;
    private boolean sleepHack;

    public EnableDisablePoll(DLSDevice device, DLSProperties options, boolean busy,
                             boolean sleepHack) {
        this.device = device;
        this.options = options;
        this.busy = busy;
        this.sleepHack = sleepHack;
    }

    public EnableDisablePoll(DLSDevice device, DLSProperties options, boolean sleepHack) {
        this(device, options, false, sleepHack);
    }

    public EnableDisablePoll(DLSDevice device, DLSProperties options) {
        this(device, options, false, false);
    }

    /**
     * Indicate whether a device or service is busy. If this returns true, no enabling or
     * disabling will occur during the run method.
     * @return boolean indicating whether the device or service is busy.
     */
    public synchronized boolean isBusy() { return this.busy; }

    /**
     * Indicate whether the EnableDisablePoll is using the sleep hack.
     * The sleep hack is for now-obsolete devices that would return from sleep in a different
     * state than they entered sleep in.  If this returns true, enable is being sent when the
     * device is enabled and disable is being sent when the device is disabled. If this returns
     * false, the EnableDisablePoll toggles between states.
     * @return boolean indicating whether the sleep hack is in use.
     */
    public synchronized boolean isSleepHack() { return this.sleepHack; }

    /**
     * The run method does not loop.  It is meant to be run using a ScheduledExecutorService
     * with the rate of execution determined by the EnableDisablePollRate configuration item.
     */
    public void run() {
        DLSState state = device.getState();
        boolean claimed = (state == DLSState.CLAIMED || state == DLSState.ENABLED);
        if (!isBusy() && claimed) {
            if (state == DLSState.ENABLED) {
                if (isSleepHack()) {
                    try {
                        device.enable();
                    } catch (DLSException de) {
                        Log.e(TAG, "run: Exception enabling device. ", de);
                    }
                } else {
                    try {
                        device.disable();
                    } catch (DLSException de) {
                        Log.e(TAG, "run: Exception disabling device. ", de);
                    }
                }
            } else {
                if (isSleepHack()) {
                    try {
                        device.disable();
                    } catch (DLSException de) {
                        Log.e(TAG, "run: Exception disabling device. ", de);
                    }
                } else {
                    try {
                        device.enable();
                    } catch (DLSException de) {
                        Log.e(TAG, "run: Exception enabling device. ", de);
                    }
                }
            }
        }
    }

    /**
     * Indicate whether the device or service is busy.  Setting this to true will result in the
     * run method not executing the Enable or Disable command during the pass.
     * @param busy boolean indicating whether the device or service is busy.
     */
    public synchronized void setBusy(boolean busy) { this.busy = busy; }

    /**
     * Indicate whether to use the sleep hack.
     * <p>
     * Some older devices did not return from the sleep state in the same enabled state as they
     * entered.  An enabled device would sleep, then return from sleep in a disabled state.
     * The sleep hack would force a device back in sync with the service by setting the last known
     * state of the device during the EnableDisablePoll.  This would result in sending enable to an
     * enabled device and disable to a disabled device.</p>
     * <p>
     * Setting this to true will result in enable being sent to enabled devices and disable being
     * sent to disabled devices.  Setting this to false will result in disable being sent to enabled
     * devices and disable sent to enabled devices.  The property defaults to false.</p>
     * @param sleepHack boolean indicating whether to use the sleep hack.
     */
    public synchronized void setSleepHack(boolean sleepHack) { this.sleepHack = sleepHack; }
}
