package com.datalogic.dlapos.androidpos.transport;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.util.Log;

import com.datalogic.dlapos.androidpos.common.DLSDeviceInfo;

import java.io.IOException;

/**
 * Class abstracting a generic USB port. Defines commons methods.
 */
public abstract class DLSUsbPort extends DLSPort implements UsbEventsListener {

    protected UsbInterface _usbInterface;
    protected UsbDevice _usbDevice;
    protected final UsbManager _usbManager;
    protected UsbDeviceConnection _usbDeviceConnection;
    protected boolean _disconnectedWhileOpened = false;
    private final AuthorizationListener _authorizationListener = new AuthorizationListener();
    private UsbPortStatusListener _statusListener;

    /**
     * {@inheritDoc}
     */
    protected DLSUsbPort(DLSDeviceInfo info, Context context) {
        super(info, context);
        _usbManager = ((UsbManager) context.getSystemService(Context.USB_SERVICE));
        for (UsbDevice device : UsbUtils.getUsbDevices(context)) {
            if (device.getVendorId() == getDeviceInfo().getVendorId() && device.getProductId() == getDeviceInfo().getProductId()) {
                _usbDevice = device;
                break;
            }
        }
    }

    /**
     * Not supported in USB.
     */
    @Override
    public void changeBaudRate(int baudRate) {
        throw new UnsupportedOperationException("changeBaudRate not supported in USB.");
    }

    private boolean isNotMe(UsbDevice device) {
        return getDeviceInfo().getProductId() != device.getProductId() || getDeviceInfo().getVendorId() != device.getVendorId();
    }

    /**
     * Function to notify port closing events.
     */
    protected void fireCloseEvent() {
        if (_statusListener != null) {
            _statusListener.portClosed();
        }
    }

    /**
     * Function to notify port related errors.
     *
     * @param errorStatus The error status.
     */
    protected void fireError(UsbPortStatusListener.ErrorStatus errorStatus) {
        if (_statusListener != null) {
            _statusListener.portError(errorStatus);
        }
    }

    /**
     * Funtion to notify port opening events.
     */
    protected void fireOpenEvent() {
        if (_statusListener != null) {
            _statusListener.portOpened();
        }
    }

    /**
     * Function to open the port. It return always true. To have the real status port, please register
     * a {@link UsbPortStatusListener} using {@link #registerPortStatusListener(UsbPortStatusListener) registerPortStatusListener()}
     *
     * @return always true.
     */
    @Override
    public boolean openPort() {
        if (_usbDevice == null) {
            return false;
        }
        UsbUtils.getInstance().requestDevice(_usbDevice, context, _authorizationListener);
        return true;
    }

    protected abstract void open(Context context);


    @Override
    public void onDeviceAttached(UsbDevice device) {
        if (isNotMe(device))
            return;
        _usbDevice = device;
        _disconnectedWhileOpened = false;
        fireDeviceReattachedEvent();
    }

    @Override
    public void onDeviceDetached(UsbDevice device) {
        if (isNotMe(device))
            return;
        _disconnectedWhileOpened = true;
        fireDeviceRemovedEvent();
    }

    /**
     * Function to register a listener for port status changes.
     *
     * @param listener The listener to be registered.
     */
    public void registerPortStatusListener(UsbPortStatusListener listener) {
        _statusListener = listener;
    }

    /**
     * Function to unregister the listener for port status changes.
     */
    public void unregisterPortStatusListener() {
        _statusListener = null;
    }


    private class AuthorizationListener extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (UsbUtils.ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    boolean authorized = intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false);
                    if (_usbDevice != null) {
                        if (authorized) {
                            open(context);
                            try {
                                setRTS(true);
                            } catch (IOException e) {
                                Log.w("DLSUsbPort", "Error setting rts", e);
                            } catch (UnsupportedOperationException e) {
                                Log.w("DLSUsbPort", "The device does not support RTS", e);
                            } catch (Exception e) {
                                Log.e("DLSUsbPort", "Exception setting RTS: ", e);
                            }
                        } else {
                            fireError(UsbPortStatusListener.ErrorStatus.MISSING_PERMISSION);
                        }
                    }
                }
                context.unregisterReceiver(_authorizationListener);
            }
        }
    }
}
