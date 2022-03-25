package com.datalogic.dlapos.androidpos.common;

import android.content.Context;

import com.datalogic.dlapos.androidpos.interpretation.DLS9xxxScanner;
import com.datalogic.dlapos.androidpos.interpretation.DLSScale;
import com.datalogic.dlapos.androidpos.interpretation.DLSScanner;
import com.datalogic.dlapos.androidpos.interpretation.DLSSerialScale;
import com.datalogic.dlapos.androidpos.interpretation.DLSSerialScanner;
import com.datalogic.dlapos.androidpos.interpretation.DLSUSBScale;
import com.datalogic.dlapos.androidpos.interpretation.DLSUSBScanner;
import com.datalogic.dlapos.androidpos.transport.DLSCOMPort;
import com.datalogic.dlapos.androidpos.transport.DLSOEMPort;
import com.datalogic.dlapos.androidpos.transport.DLSPort;
import com.datalogic.dlapos.commons.constant.CommonsConstants;
import com.datalogic.dlapos.commons.support.APosException;
import com.datalogic.dlapos.confighelper.DLAPosConfigHelper;

/**
 * Factory class for ports, scales and scanners.
 */
public class DLSObjectFactory {

    /**
     * Creates a new port instance, using the parameters defined in the
     * {@code apos.json} profile.
     *
     * @param deviceInfo DLSDeviceInfo instance populated with the {@code apos.json}
     *                   profile
     * @return DLSPort instance
     * @throws DLSException if USB usage ID is invalid
     */
    public static DLSPort createPort(DLSDeviceInfo deviceInfo, Context context) throws DLSException {
        if (deviceInfo == null) {
            throw new IllegalArgumentException("DLSDeviceInfo instance cannot be null.");
        }

        String bus = deviceInfo.getDeviceBus();
        switch (bus) {
            case CommonsConstants.USB_DEVICE_BUS:
            case CommonsConstants.HID_DEVICE_BUS:
                return new DLSOEMPort(deviceInfo, context);
            case CommonsConstants.RS232_DEVICE_BUS:
                return new DLSCOMPort(deviceInfo, context);
        }

        throw new DLSException(DLSJposConst.DLS_E_NOTSUPPORTED, "Unsupported bus type.");
    }

    /**
     * Creates a new DLSScanner instance, using the parameters defined in the
     * {@code apos.json} profile.
     *
     * @param logicalName String containing the {@code logicalName} of the
     *                    {@code apos.json} profile
     * @param context     The application context
     * @return DLSScanner instance
     * @throws DLSException  if there is a configuration error with the
     *                       {@code apos.json} profile
     * @throws APosException if can not load the apos.json profile
     */
    public static DLSScanner createScanner(String logicalName, Context context) throws DLSException, APosException {
        if (logicalName == null)
            throw new IllegalArgumentException("logicalName can not be null");
        DLSDeviceInfo info = new DLSDeviceInfo(); // Create device information object
        info.loadConfiguration(logicalName, DLAPosConfigHelper.getInstance(context).getProfileManager());

        return createScanner(info);
    }

    static DLSScanner createScanner(DLSDeviceInfo deviceInfo) throws DLSException {
        switch (deviceInfo.getDeviceBus()) {
            case CommonsConstants.USB_DEVICE_BUS:
            case CommonsConstants.HID_DEVICE_BUS:
                return new DLSUSBScanner();
//            case BT_DEVICE_BUS:
//                scanner = new DLSBluetoothScanner();
//                break;
            case CommonsConstants.RS232_DEVICE_BUS:
//                if (info.get8xxx()) {
//                    scanner = new DLS8xxxScanner();
                /*} else*/
                if (deviceInfo.is9xxx()) {
                    return new DLS9xxxScanner();
                } else {
                    return new DLSSerialScanner();
                }
//            case TCPIP_DEVICE_BUS:
//                scanner = new DLSPortalScanner();           // Create portal object
//                break;
            default:
                throw new DLSException(DLSJposConst.DLS_E_CONFIGURATION, "apos.json config error");
        }
    }

    /**
     * Creates a new DLSScale instance, using the parameters defined in the
     * {@code apos.json} profile.
     *
     * @param strLogicalName String containing the {@code logicalName} of the
     *                       {@code apos.json} profile
     * @return DLSScale instance
     * @throws DLSException if there is a configuration error with the
     *                      {@code apos.json} profile
     */
    public static DLSScale createScale(String strLogicalName, Context context) throws DLSException, APosException {
        DLSDeviceInfo info = new DLSDeviceInfo(); // Create device information object
        info.loadConfiguration(strLogicalName, DLAPosConfigHelper.getInstance(context).getProfileManager());   // Lookup info in apos.json
        return createScale(info);
    }

    static DLSScale createScale(DLSDeviceInfo deviceInfo) throws DLSException {
// If this is a USB configured scale
        switch (deviceInfo.getDeviceBus()) {
            case CommonsConstants.USB_DEVICE_BUS:
            case CommonsConstants.HID_DEVICE_BUS:
                return new DLSUSBScale();
            case CommonsConstants.RS232_DEVICE_BUS:
                return new DLSSerialScale();
            default:
                throw new DLSException(DLSJposConst.DLS_E_CONFIGURATION, "apos.json config error");
        }
    }
}
