package com.datalogic.dlapos.androidpos.transport;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;

import com.datalogic.dlapos.androidpos.common.DLSDeviceInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utils class for USB.
 */
public class UsbUtils extends BroadcastReceiver {

    /**
     * Enumerator for USB communication types.
     */
    public enum UsbType {
        /**
         * COM communication.
         */
        COM,
        /**
         * IBM OEM communication.
         */
        OEM
    }

    public static final String ACTION_USB_PERMISSION = "com.datalogic.USB_PERMISSION";

    private boolean _listening = false;
    private final Set<UsbEventsListener> _listeners = new HashSet<>();

    private UsbUtils() {
    }

    /**
     * Instance getter.
     *
     * @return an instance of the UsbUtils.
     */
    public static UsbUtils getInstance() {
        return InstanceHolder.INSTANCE;
    }

    /**
     * Function to register a listener for Attach and Detach events.
     *
     * @param listener the listener to register.
     * @param context  the application context.
     */
    public void registerEventListener(UsbEventsListener listener, Context context) {
        if (!_listening) {
            startListener(context);
        }
        _listeners.add(listener);
    }

    /**
     * Function to remove a listener for Attach and Detach events.
     *
     * @param listener the listener to remove.
     * @param context  the application context.
     */
    public void removeEventListener(UsbEventsListener listener, Context context) {
        _listeners.remove(listener);
        if (_listening && _listeners.isEmpty()) {
            stopListener(context);
        }
    }

    /**
     * Function to obtain all connected devices.
     *
     * @param context the application context.
     * @return a list containing all connected devices.
     */
    public static List<UsbDevice> getUsbDevices(Context context) {
        UsbManager manager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        if (manager != null) {
            HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
            return new ArrayList<>(deviceList.values());
        } else {
            return new ArrayList<>();
        }
    }

    /**
     * Function to ask for USB access permission for a device. The operation result is notified using the receiver.
     *
     * @param device   the device to ask permissions for.
     * @param context  the application context.
     * @param receiver the receiver to notify at the end of the operation.
     */
    public void requestDevice(UsbDevice device, Context context, BroadcastReceiver receiver) {
        UsbManager usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        PendingIntent permissionIntent = PendingIntent.getBroadcast(context, 0, new Intent(ACTION_USB_PERMISSION), 0);
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        context.registerReceiver(receiver, filter);
        usbManager.requestPermission(device, permissionIntent);
    }

    /**
     * Function responsible to notify listeners for USB events.
     *
     * @param context the application context.
     * @param intent  an intent for USB events.
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null) {
            UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
            if (device != null) {
                for (UsbEventsListener listener : _listeners) {
                    if (intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_ATTACHED))
                        listener.onDeviceAttached(device);
                    else if (intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_DETACHED)) {
                        listener.onDeviceDetached(device);
                    }
                }
            }
        }
    }

    /**
     * Function to get the HID interface of a device.
     *
     * @param device the device to query.
     * @return the HID interface of the device.
     */
    public static UsbInterface getHIDInterface(UsbDevice device, DLSDeviceInfo info) {
        if (device == null)
            throw new IllegalArgumentException("Device can not be null");
        String desiredUsage = Integer.toHexString(info.getUsage());
        for (int i = 0; i < device.getInterfaceCount(); i++) {
            if (device.getInterface(i).getInterfaceClass() == UsbConstants.USB_CLASS_HID && extractUsage(device.getInterface(i).getName()).equalsIgnoreCase(desiredUsage)) {
                return device.getInterface(i);
            }
        }
        return null;
    }

    static String extractUsage(String interfaceString) {
        String usage = "";
        Pattern rx = Pattern.compile("(Usage = )(\\w{4})(h)");
        Matcher m = rx.matcher(interfaceString);
        if (m.find()) {
            usage = m.group(2);
        }
        return usage;
    }

    /**
     * Function to get the output endpoint of an interface.
     *
     * @param usbInterface the interface to query.
     * @return the output endpoint of the interface.
     */
    public static UsbEndpoint getOutEndpoint(UsbInterface usbInterface) {
        for (int i = 0; i < usbInterface.getEndpointCount(); i++) {
            UsbEndpoint endpoint = usbInterface.getEndpoint(i);
            if (/*endpoint.getType() == UsbConstants.USB_ENDPOINT_XFER_INT &&*/ endpoint.getDirection() == UsbConstants.USB_DIR_OUT)
                return endpoint;
        }
        return null;
    }

    /**
     * Function to get the input endpoint
     *
     * @param usbInterface the interface to query.
     * @return the input enrpoint of the interface.
     */
    public static UsbEndpoint getInEndpoint(UsbInterface usbInterface) {
        for (int i = 0; i < usbInterface.getEndpointCount(); i++) {
            UsbEndpoint endpoint = usbInterface.getEndpoint(i);
            if (endpoint.getType() == UsbConstants.USB_ENDPOINT_XFER_INT && endpoint.getDirection() == UsbConstants.USB_DIR_IN)
                return endpoint;
        }
        return null;
    }

    private void startListener(Context context) {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        intentFilter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        context.registerReceiver(this, intentFilter);
        _listening = true;
    }

    private void stopListener(Context context) {
        context.unregisterReceiver(this);
        _listening = false;
    }

    private static boolean validDevice(UsbDevice device) {
        return (device.getProductName() != null && !device.getProductName().isEmpty());
    }

    private static class InstanceHolder {
        private static final UsbUtils INSTANCE = new UsbUtils();
    }
}
