package com.datalogic.dlapos.androidpos.transport;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.datalogic.dlapos.androidpos.common.DLSDeviceInfo;
import com.hoho.android.usbserial.driver.CdcAcmSerialDriver;
import com.hoho.android.usbserial.driver.ProbeTable;
import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Function abstracting am USB-COM port.
 */
public class DLSCOMPort extends DLSUsbPort {

    private static final String TAG = "COMPort";

    private final ProbeTable _probeTable = new ProbeTable();
    private UsbSerialPort _port;
    private ReadingDaemon _readingDaemon = null;
    private boolean _closing = false;

    /**
     * {@inheritDoc}
     */
    public DLSCOMPort(DLSDeviceInfo info, Context context) {
        super(info, context);
        _probeTable.addProduct(1529, 16900, CdcAcmSerialDriver.class);// Scanners
        _probeTable.addProduct(1529, 16386, CdcAcmSerialDriver.class);// Magellan Scanner
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
        UsbSerialProber prober = new UsbSerialProber(_probeTable);
        List<UsbSerialDriver> availableDrivers = prober.findAllDrivers(_usbManager);
        if (availableDrivers.isEmpty()) {
            fireError(UsbPortStatusListener.ErrorStatus.CAN_NOT_FIND_DEVICE);
            return;
        }

        // Open a connection to the first available driver.
        UsbSerialDriver driver = availableDrivers.get(0);
        _usbDeviceConnection = _usbManager.openDevice(driver.getDevice());
        if (_usbDeviceConnection == null) {
            fireError(UsbPortStatusListener.ErrorStatus.CAN_NOT_OPEN_CONNECTION);
            return;
        }

        _port = driver.getPorts().get(0); // Most devices have just one port (port 0)
        if (_port == null) {
            fireError(UsbPortStatusListener.ErrorStatus.CAN_NOT_FIND_COM_PORT);
            return;
        }
        try {
            _port.open(_usbDeviceConnection);
            _port.setParameters(9600, UsbSerialPort.DATABITS_8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);
        } catch (Exception e) {
            Log.e(TAG, "Error opening port", e);
            fireError(UsbPortStatusListener.ErrorStatus.CAN_NOT_OPEN_COM_PORT);
            return;
        }

        _readingDaemon = new ReadingDaemon();
        Thread thread = new Thread(_readingDaemon);
        thread.start();

        setOpen(true);

        UsbUtils.getInstance().registerEventListener(this, context);
        fireOpenEvent();
    }

    private void close() {
        if (!isOpen())
            return;
        if (_port != null && !_disconnectedWhileOpened) {
            try {
                _port.setRTS(false);
            } catch (IOException e) {
                Log.w(TAG, "IO error disabling RTS: ", e);
            } catch (UnsupportedOperationException e) {
                Log.w(TAG, "RTS not supported.");
            } catch (Exception e) {
                Log.e(TAG, "Exception disabling RTS: ", e);
            }
            try {
                _port.close();
            } catch (IOException e) {
                Log.e(TAG, "Error closing port.", e);
            }
        }

        if (_usbDeviceConnection != null) {
            _usbDeviceConnection.close();
        }
        if (!_disconnectedWhileOpened) {
            UsbUtils.getInstance().removeEventListener(this, context);
        }
        setOpen(false);
        fireCloseEvent();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean closePort() {
        if (_readingDaemon != null && _readingDaemon.running) {
            _closing = true;
            _readingDaemon.stop();
            _readingDaemon = null;
        } else {
            close();
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int sendData(byte[] buffer, int len) {
        int res;
        try {
            res = new SendDataTask().execute(buffer).get(250, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            e.printStackTrace();
            res = -1;
        }
        return res;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setRTS(boolean enable) throws IOException {
        _port.setRTS(enable);
    }

    private class ReadingDaemon implements Runnable {

        private volatile boolean running = true;

        @Override
        public void run() {
            byte[] data = new byte[8192];
            final int maxRetries = 10;
            int retries = 0;
            while (running) {
                try {
                    int nBytes;
                    if ((nBytes = _port.read(data, 500)) > 0) {
                        retries = 0;
                        fireDataReceivedEvent(Arrays.copyOf(data, nBytes), nBytes);
                    }
                    Thread.sleep(100);
                } catch (IOException | InterruptedException e) {
                    retries++;
                    if (retries > maxRetries) {
                        retries = 0;
                        Log.e(TAG, "Multiple errors reading.", e);
                    }
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException interruptedException) {
                        interruptedException.printStackTrace();
                    }
                }
            }
            Log.d(TAG, "Exiting from reading daemon");
            if (_closing)
                close();
        }

        public void stop() {
            running = false;
        }
    }

    private class SendDataTask extends AsyncTask<byte[], Void, Integer> {

        @Override
        protected Integer doInBackground(byte[]... bytes) {

            try {
                _port.write(bytes[0], 500);
                return 0;
            } catch (IOException e) {
                Log.e(TAG, "Send command error: ", e);
                return -1;
            }
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
