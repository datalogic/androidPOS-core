package com.datalogic.dlapos.androidpos.common;

import android.content.Context;

import com.datalogic.dlapos.androidpos.interpretation.DLS9xxxScanner;
import com.datalogic.dlapos.androidpos.interpretation.DLSSerialScale;
import com.datalogic.dlapos.androidpos.interpretation.DLSSerialScanner;
import com.datalogic.dlapos.androidpos.interpretation.DLSUSBScale;
import com.datalogic.dlapos.androidpos.interpretation.DLSUSBScanner;
import com.datalogic.dlapos.androidpos.transport.DLSCOMPort;
import com.datalogic.dlapos.androidpos.transport.DLSOEMPort;
import com.datalogic.dlapos.commons.constant.CommonsConstants;

import org.junit.Test;
import org.mockito.Mock;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DLSObjectFactoryTest {

    @Mock
    private final Context _context = mock(Context.class);

    @Mock
    private final DLSDeviceInfo _dlsDeviceInfo = mock(DLSDeviceInfo.class);

    //region createPort
    @Test(expected = IllegalArgumentException.class)
    public void createPortNullDeviceInfo() throws DLSException {
        assertThat(DLSObjectFactory.createPort(null, _context) instanceof DLSOEMPort).isTrue();
    }

    @Test(expected = DLSException.class)
    public void createUnknownPort() throws DLSException {
        when(_dlsDeviceInfo.getDeviceBus()).thenReturn("TEST");
        DLSObjectFactory.createPort(_dlsDeviceInfo, _context);
    }

    @Test
    public void createUSBOEMPort() throws DLSException {
        when(_dlsDeviceInfo.getDeviceBus()).thenReturn(CommonsConstants.HID_DEVICE_BUS);
        assertThat(DLSObjectFactory.createPort(_dlsDeviceInfo, _context) instanceof DLSOEMPort).isTrue();
        when(_dlsDeviceInfo.getDeviceBus()).thenReturn(CommonsConstants.USB_DEVICE_BUS);
        assertThat(DLSObjectFactory.createPort(_dlsDeviceInfo, _context) instanceof DLSOEMPort).isTrue();
    }

    @Test
    public void createUSBCOMPort() throws DLSException {
        when(_dlsDeviceInfo.getDeviceBus()).thenReturn(CommonsConstants.RS232_DEVICE_BUS);
        assertThat(DLSObjectFactory.createPort(_dlsDeviceInfo, _context) instanceof DLSCOMPort).isTrue();
    }

    //endregion

    //region createScanner
    @Test(expected = DLSException.class)
    public void createUnknownScanner() throws DLSException {
        when(_dlsDeviceInfo.getDeviceBus()).thenReturn("TEST");
        DLSObjectFactory.createScanner(_dlsDeviceInfo);
    }

    @Test
    public void createUsbScanner() throws DLSException {
        when(_dlsDeviceInfo.getDeviceBus()).thenReturn(CommonsConstants.HID_DEVICE_BUS);
        assertThat(DLSObjectFactory.createScanner(_dlsDeviceInfo) instanceof DLSUSBScanner).isTrue();
        when(_dlsDeviceInfo.getDeviceBus()).thenReturn(CommonsConstants.USB_DEVICE_BUS);
        assertThat(DLSObjectFactory.createScanner(_dlsDeviceInfo) instanceof DLSUSBScanner).isTrue();
    }

    @Test
    public void createCOMScanner() throws DLSException {
        when(_dlsDeviceInfo.getDeviceBus()).thenReturn(CommonsConstants.RS232_DEVICE_BUS);
        assertThat(DLSObjectFactory.createScanner(_dlsDeviceInfo) instanceof DLSSerialScanner).isTrue();
    }

    @Test
    public void create9xxxScanner() throws DLSException {
        when(_dlsDeviceInfo.getDeviceBus()).thenReturn(CommonsConstants.RS232_DEVICE_BUS);
        when(_dlsDeviceInfo.is9xxx()).thenReturn(true);
        assertThat(DLSObjectFactory.createScanner(_dlsDeviceInfo) instanceof DLS9xxxScanner).isTrue();
    }
    //endregion

    //region createScale
    @Test(expected = DLSException.class)
    public void createUnknownScale() throws DLSException {
        when(_dlsDeviceInfo.getDeviceBus()).thenReturn("TEST");
        DLSObjectFactory.createScale(_dlsDeviceInfo);
    }

    @Test
    public void createUsbScale() throws DLSException {
        when(_dlsDeviceInfo.getDeviceBus()).thenReturn(CommonsConstants.HID_DEVICE_BUS);
        assertThat(DLSObjectFactory.createScale(_dlsDeviceInfo) instanceof DLSUSBScale).isTrue();
        when(_dlsDeviceInfo.getDeviceBus()).thenReturn(CommonsConstants.USB_DEVICE_BUS);
        assertThat(DLSObjectFactory.createScale(_dlsDeviceInfo) instanceof DLSUSBScale).isTrue();
    }

    @Test
    public void createCOMScale() throws DLSException {
        when(_dlsDeviceInfo.getDeviceBus()).thenReturn(CommonsConstants.RS232_DEVICE_BUS);
        assertThat(DLSObjectFactory.createScale(_dlsDeviceInfo) instanceof DLSSerialScale).isTrue();
    }
    //endregion
}