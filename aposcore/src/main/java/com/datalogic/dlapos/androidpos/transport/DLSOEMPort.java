package com.datalogic.dlapos.androidpos.transport;

import android.content.Context;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.os.AsyncTask;
import android.util.Log;

import com.datalogic.dlapos.androidpos.common.DLSDeviceInfo;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Class abstracting an USB-OEM (IBM) port.
 */
public class DLSOEMPort extends DLSUsbPort {

    private final static String TAG = "UsbPort";

    private UsbEndpoint _endpoint;
    private ReadingDaemon _readingDaemon = null;

    /**
     * {@inheritDoc}
     */
    public DLSOEMPort(DLSDeviceInfo info, Context context) {
        super(info, context);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean closePort() {
        if (_readingDaemon != null && _readingDaemon.running) {
            _readingDaemon.stop();
            _readingDaemon = null;
        }
        if (_usbDeviceConnection != null) {
            _usbDeviceConnection.releaseInterface(_usbInterface);
            _usbDeviceConnection.close();
        }
        if (!_disconnectedWhileOpened) {
            UsbUtils.getInstance().removeEventListener(this, context);
        }
        setOpen(false);
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void open(Context context) {
        if (isOpen()) {
            Log.d(TAG, "Already open.");
            fireOpenEvent();
            return;
        }

        _usbInterface = UsbUtils.getHIDInterface(_usbDevice, getDeviceInfo());
        if (_usbInterface == null) {
            fireError(UsbPortStatusListener.ErrorStatus.CAN_NOT_FIND_INTERFACE);
            return;
        }

        _endpoint = UsbUtils.getInEndpoint(_usbInterface);
        if (_endpoint == null) {
            fireError(UsbPortStatusListener.ErrorStatus.CAN_NOT_FIND_ENDPOINTS);
            return;
        }

        _usbDeviceConnection = _usbManager.openDevice(_usbDevice);
        if (_usbDeviceConnection == null) {
            fireError(UsbPortStatusListener.ErrorStatus.CAN_NOT_OPEN_CONNECTION);
            return;
        }

        _readingDaemon = new ReadingDaemon();
        Thread thread = new Thread(_readingDaemon);
        thread.start();

        setOpen(_usbDeviceConnection.claimInterface(_usbInterface, true));
        UsbUtils.getInstance().registerEventListener(this, context);
        fireOpenEvent();
    }

    private int sendCommandAndWait(byte[] cmd) {
        int ret;
        try {
            if (_usbInterface == null)
                ret = -1;
            else
                ret = new SendDataTask(_usbDeviceConnection, _usbInterface, _endpoint).execute(cmd).get(250, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            ret = -1;
            e.printStackTrace();
        }
        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int sendData(byte[] buffer, int len) {
        return sendCommandAndWait(buffer);
    }

    private class ReadingDaemon implements Runnable {

        private volatile boolean running = true;

        @Override
        public void run() {
            byte[] data = new byte[_endpoint.getMaxPacketSize()];
            while (running) {
                if (_usbDeviceConnection.bulkTransfer(_endpoint, data, _endpoint.getMaxPacketSize(), 50) > 0) {
                    fireDataReceivedEvent(data, data.length);
                }
            }
            Log.d(TAG, "Exiting from reading daemon");
        }

        public void stop() {
            running = false;
        }
    }

    private static class SendDataTask extends AsyncTask<byte[], Void, Integer> {

        UsbDeviceConnection devConnection;
        UsbInterface usbInterface;
        UsbEndpoint usbEndpoint;

        SendDataTask(UsbDeviceConnection udc, UsbInterface ui, UsbEndpoint ep) {
            devConnection = udc;
            usbInterface = ui;
            usbEndpoint = ep;
        }

        @Override
        protected Integer doInBackground(byte[]... bytes) {
            int requestType = UsbConstants.USB_DIR_OUT | UsbConstants.USB_TYPE_CLASS | UsbConstants.USB_INTERFACE_SUBCLASS_BOOT;

            if (usbInterface == null) {
                Log.e(TAG, "Error, null interface");
                return -1;
            }
            return devConnection.controlTransfer(requestType, 0x09, 0x200, usbInterface.getId(), bytes[0], bytes[0].length, 100);
        }

        @Override
        protected void onPostExecute(Integer integer) {
            if (integer < 0) {
                Log.e(TAG, "Command could not be sent");
            } else {
                Log.d(TAG, "Command sent successfully");
            }
        }
    }
}
