package com.datalogic.dlapos.androidpos.common;

import com.datalogic.dlapos.commons.support.APosException;
import com.datalogic.dlapos.confighelper.configurations.accessor.ProfileManager;
import com.datalogic.dlapos.confighelper.configurations.support.DLAPosProfile;

import org.junit.Test;
import org.mockito.Mock;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DLSDeviceInfoTest {

    //region jsonProfile
    private final String jsonProfile = " {\n" +
            "    \"logicalName\": \"DLS-USB-Flash\",\n" +
            "    \"creation\": {\n" +
            "      \"factoryClass\": \"com.dls.jpos.service.DLSScannerInstanceFactory\",\n" +
            "      \"serviceClass\": \"com.dls.jpos.service.DLSScannerService\"\n" +
            "    },\n" +
            "    \"vendor\": {\n" +
            "      \"name\": \"DLA\",\n" +
            "      \"url\": \"http://www.adc.datalogic.com\"\n" +
            "    },\n" +
            "    \"apos\": {\n" +
            "      \"category\": \"Flash\",\n" +
            "      \"version\": \"1.13\"\n" +
            "    },\n" +
            "    \"product\": {\n" +
            "      \"description\": \"ScannerService\",\n" +
            "      \"name\": \"ScannerService\",\n" +
            "      \"url\": \"http://www.adc.datalogic.com\"\n" +
            "    },\n" +
            "    \"properties\": [\n" +
            "      {\n" +
            "        \"name\": \"canAcceptStatisticsCmd\",\n" +
            "        \"type\": \"String\",\n" +
            "        \"value\": \"True\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"name\": \"canCompareFirmwareVersion\",\n" +
            "        \"type\": \"String\",\n" +
            "        \"value\": \"True\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"name\": \"canUpdateFirmware\",\n" +
            "        \"type\": \"String\",\n" +
            "        \"value\": \"True\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"name\": \"deviceBus\",\n" +
            "        \"type\": \"String\",\n" +
            "        \"value\": \"USB\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"name\": \"productId\",\n" +
            "        \"type\": \"String\",\n" +
            "        \"value\": \"120A\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"name\": \"usage\",\n" +
            "        \"type\": \"String\",\n" +
            "        \"value\": \"A000\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"name\": \"vendorId\",\n" +
            "        \"type\": \"String\",\n" +
            "        \"value\": \"05f9\"\n" +
            "      }\n" +
            "    ]\n" +
            "  }";
    //endregion

    @Mock
    private final ProfileManager profileManager = mock(ProfileManager.class);


    DLSDeviceInfo m_info;

    public DLSDeviceInfoTest() throws APosException {
        setUpProfileManager();
        m_info = new DLSDeviceInfo();
        boolean bResult = m_info.loadConfiguration("DLS-USB-Flash", profileManager);
        assertTrue(bResult);
        assertThat(m_info.getLogicalName()).isEqualTo("DLS-USB-Flash");
    }


    /**
     * Test of getServiceInstanceFactoryClass method, of class DLSDeviceInfo.
     */
    @Test
    public void testGetServiceInstanceFactoryClass() {
        String expResult = "com.dls.jpos.service.DLSScannerInstanceFactory";
        String result = m_info.getServiceInstanceFactoryClass();
        assertEquals(expResult, result);
    }

    /**
     * Test of setServiceInstanceFactoryClass method, of class DLSDeviceInfo.
     */
    @Test
    public void testSetServiceInstanceFactoryClass() {
        String strValue = "TestValue";
        m_info.setServiceInstanceFactoryClass(strValue);
        assertEquals(strValue, m_info.getServiceInstanceFactoryClass());
    }

    /**
     * Test of getServiceClass method, of class DLSDeviceInfo.
     */
    @Test
    public void testGetServiceClass() {
        String expResult = "com.dls.jpos.service.DLSScannerService";
        String result = m_info.getServiceClass();
        assertEquals(expResult, result);
    }

    /**
     * Test of setServiceClass method, of class DLSDeviceInfo.
     */
    @Test
    public void testSetServiceClass() {
        String strValue = "TestValue";
        m_info.setServiceClass(strValue);
        assertEquals(strValue, m_info.getServiceClass());
    }

    /**
     * Test of getVendorName method, of class DLSDeviceInfo.
     */
    @Test
    public void testGetVendorName() {
        String expResult = "DLA";
        String result = m_info.getVendorName();
        assertEquals(expResult, result);
    }

    /**
     * Test of setVendorName method, of class DLSDeviceInfo.
     */
    @Test
    public void testSetVendorName() {
        String strValue = "TestValue";
        m_info.setVendorName(strValue);
        assertEquals(strValue, m_info.getVendorName());
    }

    @Test
    public void testSetDeviceNumber() {
        m_info.setDeviceNumber(13);
        assertThat(m_info.getDeviceNumber()).isEqualTo(13);
    }

    @Test
    public void testSetUseSymbolicLink() {
        m_info.setUseSymbolicLink(true);
        assertThat(m_info.isUseSymbolicLink()).isTrue();
    }

    /**
     * Test of getVendorUrl method, of class DLSDeviceInfo.
     */
    @Test
    public void testGetVendorUrl() {
        String expResult = "http://www.adc.datalogic.com";
        String result = m_info.getVendorUrl();
        assertEquals(expResult, result);
    }

    /**
     * Test of setVendorUrl method, of class DLSDeviceInfo.
     */
    @Test
    public void testSetVendorUrl() {
        String strValue = "TestValue";
        m_info.setVendorUrl(strValue);
        assertEquals(strValue, m_info.getVendorUrl());
    }

    /**
     * Test of getDeviceCategory method, of class DLSDeviceInfo.
     */
    @Test
    public void testGetDeviceCategory() {
        String expResult = "Flash";
        String result = m_info.getDeviceCategory();
        assertEquals(expResult, result);
    }

    /**
     * Test of setDeviceCategory method, of class DLSDeviceInfo.
     */
    @Test
    public void testSetDeviceCategory() {
        String strValue = "TestValue";
        m_info.setDeviceCategory(strValue);
        assertEquals(strValue, m_info.getDeviceCategory());
    }

    /**
     * Test of getJposVersion method, of class DLSDeviceInfo.
     */
    @Test
    public void testGetJposVersion() {
        String expResult = "1.13";
        String result = m_info.getJposVersion();
        assertEquals(expResult, result);
    }

    /**
     * Test of setJposVersion method, of class DLSDeviceInfo.
     */
    @Test
    public void testSetJposVersion() {
        String strValue = "TestValue";
        m_info.setJposVersion(strValue);
        assertEquals(strValue, m_info.getJposVersion());
    }

    /**
     * Test of getProductName method, of class DLSDeviceInfo.
     */
    @Test
    public void testGetProductName() {
        String expResult = "ScannerService";
        String result = m_info.getProductName();
        assertEquals(expResult, result);
    }

    /**
     * Test of setProductName method, of class DLSDeviceInfo.
     */
    @Test
    public void testSetProductName() {
        String strValue = "TestValue";
        m_info.setProductName(strValue);
        assertEquals(strValue, m_info.getProductName());
    }

    /**
     * Test of getProductDescription method, of class DLSDeviceInfo.
     */
    @Test
    public void testGetProductDescription() {
        String expResult = "ScannerService";
        String result = m_info.getProductDescription();
        assertEquals(expResult, result);
    }

    /**
     * Test of setProductDescription method, of class DLSDeviceInfo.
     */
    @Test
    public void testSetProductDescription() {
        String strValue = "TestValue";
        m_info.setProductDescription(strValue);
        assertEquals(strValue, m_info.getProductDescription());
    }

    /**
     * Test of getProductUrl method, of class DLSDeviceInfo.
     */
    @Test
    public void testGetProductUrl() {
        String expResult = "http://www.adc.datalogic.com";
        String result = m_info.getProductUrl();
        assertEquals(expResult, result);
    }

    /**
     * Test of setProductUrl method, of class DLSDeviceInfo.
     */
    @Test
    public void testSetProductUrl() {
        String strValue = "TestValue";
        m_info.setProductUrl(strValue);
        assertEquals(strValue, m_info.getProductUrl());
    }

    /**
     * Test of getPort method, of class DLSDeviceInfo.
     */
    @Test
    public void testGetPort() {
        int expResult = 1;
        int result = m_info.getPort();
        assertEquals(expResult, result);
    }

//    /**
//     * Test of getLocalPortAsString method, of class DLSDeviceInfo.
//     */
//    @Test
//    public void testGetLocalPortAsString() {
//        String result = m_info.getLocalPortAsString();
//        // DLSDeviceInfo defines the local port as null.
//        assertNull(result);
//    }
//
//    /**
//     * Test of getStringPort method, of class DLSDeviceInfo.
//     */
//    @Test
//    public void testGetStringPort() {
//        String expResult = "1";
//        String result = m_info.getStringPort();
//        assertEquals(expResult, result);
//    }

    /**
     * Test of setPort method, of class DLSDeviceInfo.
     */
    @Test
    public void testSetPort() {
        int nPort = 123;
        m_info.setPort(nPort);
        assertEquals(nPort, m_info.getPort());
    }

    /**
     * Test of setPortAsString method, of class DLSDeviceInfo.
     */
    @Test
    public void testSetPortAsString() {
        String sPort = "COM3";
        m_info.setPort(sPort);
        assertEquals(sPort, m_info.getPortAsString());
    }

    /**
     * Test of getPortAsString method, of class DLSDeviceInfo.
     */
    @Test
    public void testGetPortAsString() {
        String expResult = "1";
        String result = m_info.getPortAsString();
        assertEquals(expResult, result);
    }

    /**
     * Test of getBaudRate method, of class DLSDeviceInfo.
     */
    @Test
    public void testGetBaudRate() {
        int expResult = 9600;
        int result = m_info.getBaudRate();
        assertEquals(expResult, result);
    }

    /**
     * Test of setBaudRate method, of class DLSDeviceInfo.
     */
    @Test
    public void testSetBaudRate() {
        int nBaudRate = 300;
        m_info.setBaudRate(nBaudRate);
        assertEquals(nBaudRate, m_info.getBaudRate());
    }

    /**
     * Test of getParity method, of class DLSDeviceInfo.
     */
    @Test
    public void testGetParity() {
        String expResult = "None";
        String result = m_info.getParity();
        assertEquals(expResult, result);
    }

    /**
     * Test of setParity method, of class DLSDeviceInfo.
     */
    @Test
    public void testSetParity() {
        String strParity = "ODD";
        m_info.setParity(strParity);
        assertEquals(strParity, m_info.getParity());
    }

    /**
     * Test of getStopBits method, of class DLSDeviceInfo.
     */
    @Test
    public void testGetStopBits() {
        int expResult = 1;
        int result = m_info.getStopBits();
        assertEquals(expResult, result);
    }

    /**
     * Test of setStopBits method, of class DLSDeviceInfo.
     */
    @Test
    public void testSetStopBits() {
        int nStopBits = 1;
        m_info.setStopBits(nStopBits);
        assertEquals(nStopBits, m_info.getStopBits());
    }

    /**
     * Test of getDataBits method, of class DLSDeviceInfo.
     */
    @Test
    public void testGetDataBits() {
        int expResult = 8;
        int result = m_info.getDataBits();
        assertEquals(expResult, result);
    }

    /**
     * Test of setDataBits method, of class DLSDeviceInfo.
     */
    @Test
    public void testSetDataBits() {
        int nDataBits = 8;
        m_info.setDataBits(nDataBits);
        assertEquals(nDataBits, m_info.getDataBits());
    }

    /**
     * Test of getFlowControl method, of class DLSDeviceInfo.
     */
    @Test
    public void testGetFlowControl() {
        String expResult = "Xon/Xoff";
        String result = m_info.getFlowControl();
        assertEquals(expResult, result);
    }

    /**
     * Test of setFlowControl method, of class DLSDeviceInfo.
     */
    @Test
    public void testSetFlowControl() {
        String strFlowControl = "XON";
        m_info.setFlowControl(strFlowControl);
        assertEquals(strFlowControl, m_info.getFlowControl());
    }

    /**
     * Test of isFullDisable method, of class DLSDeviceInfo.
     */
    @Test
    public void testisFullDisable() {
        boolean expResult = false;
        boolean result = m_info.isFullDisable();
        assertEquals(expResult, result);
    }

    /**
     * Test of setFullDisable method, of class DLSDeviceInfo.
     */
    @Test
    public void testSetFullDisable() {
        boolean bFullDisable = true;
        m_info.setFullDisable(bFullDisable);
        assertTrue(m_info.isFullDisable());
    }

    /**
     * Test of isScanControl method, of class DLSDeviceInfo.
     */
    @Test
    public void testisScanControl() {
        boolean expResult = false;
        boolean result = m_info.isScanControl();
        assertEquals(expResult, result);
    }

    /**
     * Test of setScanControl method, of class DLSDeviceInfo.
     */
    @Test
    public void testSetScanControl() {
        boolean bScanControl = true;
        m_info.setScanControl(bScanControl);
        assertTrue(m_info.isScanControl());
    }

    /**
     * Test of getVendorId method, of class DLSDeviceInfo.
     */
    @Test
    public void testGetVendorId() {
        int expResult = 0x5f9;
        int result = m_info.getVendorId();
        assertEquals(expResult, result);
    }

    /**
     * Test of setVendorId method, of class DLSDeviceInfo.
     */
    @Test
    public void testSetVendorId() {
        int nVendorId = 3030;
        m_info.setVendorId(nVendorId);
        assertEquals(nVendorId, m_info.getVendorId());
    }

    /**
     * Test of getProductId method, of class DLSDeviceInfo.
     */
    @Test
    public void testGetProductId() {
        int expResult = 0x120A;
        int result = m_info.getProductId();
        assertEquals(expResult, result);
    }

    /**
     * Test of setProductId method, of class DLSDeviceInfo.
     */
    @Test
    public void testSetProductId() {
        int nProductId = 998;
        m_info.setProductId(nProductId);
        assertEquals(nProductId, m_info.getProductId());
    }

    /**
     * Test of getUsage method, of class DLSDeviceInfo.
     */
    @Test
    public void testGetUsage() {
        int expResult = 0xA000;
        int result = m_info.getUsage();
        assertEquals(expResult, result);
    }

    /**
     * Test of setUsage method, of class DLSDeviceInfo.
     */
    @Test
    public void testSetUsage() {
        int nUsage = 34;
        m_info.setUsage(nUsage);
        assertEquals(nUsage, m_info.getUsage());
    }

    /**
     * Test of getDeviceBus method, of class DLSDeviceInfo.
     */
    @Test
    public void testGetDeviceBus() {
        String expResult = "USB";
        String result = m_info.getDeviceBus();
        assertEquals(expResult, result);
    }

    /**
     * Test of setDeviceBus method, of class DLSDeviceInfo.
     */
    @Test
    public void testSetDeviceBus() {
        String strBus = "RS232";
        m_info.setDeviceBus(strBus);
        assertEquals(strBus, m_info.getDeviceBus());
    }

    /**
     * Test of setSingleCable method, of class DLSDeviceInfo.
     */
    @Test
    public void testSetSingleCable() {
        boolean bValue = true;
        m_info.setSingleCable(bValue);
        assertTrue(m_info.isSingleCable());
    }

    /**
     * Test of isSingleCable method, of class DLSDeviceInfo.
     */
    @Test
    public void testisSingleCable() {
        boolean expResult = false;
        boolean result = m_info.isSingleCable();
        assertEquals(expResult, result);
    }

    /**
     * Test of getDeviceName method, of class DLSDeviceInfo.
     */
    @Test
    public void testGetDeviceName() {
        String expResult = "DLS Scanner";
        String result = m_info.getDeviceName();
        assertEquals(expResult, result);
    }

    /**
     * Test of setDeviceName method, of class DLSDeviceInfo.
     */
    @Test
    public void testSetDeviceName() {
        String strValue = "Jimmy";
        m_info.setDeviceName(strValue);
        assertEquals(strValue, m_info.getDeviceName());
    }

    /**
     * Test of getDeviceDescription method, of class DLSDeviceInfo.
     */
    @Test
    public void testGetDeviceDescription() {
        String expResult = "Scanner";
        String result = m_info.getDeviceDescription();
        assertEquals(expResult, result);
    }

    /**
     * Test of setDeviceDescription method, of class DLSDeviceInfo.
     */
    @Test
    public void testSetDeviceDescription() {
        String strValue = "Go Go Gadget Description!";
        m_info.setDeviceDescription(strValue);
        assertEquals(strValue, m_info.getDeviceDescription());
    }

    /**
     * Test of isUseBCC method, of class DLSDeviceInfo.
     */
    @Test
    public void testisUseBCC() {
        boolean expResult = false;
        boolean result = m_info.isUseBCC();
        assertEquals(expResult, result);
    }

    /**
     * Test of setUseBCC method, of class DLSDeviceInfo.
     */
    @Test
    public void testSetUseBCC() {
        boolean bValue = true;
        m_info.setUseBCC(bValue);
        assertTrue(m_info.isUseBCC());
    }

    /**
     * Test of getTxPrefix method, of class DLSDeviceInfo.
     */
    @Test
    public void testGetTxPrefix() {
        byte expResult = 83;
        byte result = m_info.getTxPrefix();
        assertEquals(expResult, result);
    }

    /**
     * Test of setTxPrefix method, of class DLSDeviceInfo.
     */
    @Test
    public void testSetTxPrefix() {
        byte bValue = 4;
        m_info.setTxPrefix(bValue);
        assertEquals(bValue, m_info.getTxPrefix());
    }

    /**
     * Test of getTxTrailer method, of class DLSDeviceInfo.
     */
    @Test
    public void testGetTxTrailer() {
        byte expResult = 13;
        byte result = m_info.getTxTrailer();
        assertEquals(expResult, result);
    }

    /**
     * Test of setTxTrailer method, of class DLSDeviceInfo.
     */
    @Test
    public void testSetTxTrailer() {
        byte bValue = 9;
        m_info.setTxTrailer(bValue);
        assertEquals(bValue, m_info.getTxTrailer());
    }

    /**
     * Test of getRxPrefix method, of class DLSDeviceInfo.
     */
    @Test
    public void testGetRxPrefix() {
        byte expResult = 83;
        byte result = m_info.getRxPrefix();
        assertEquals(expResult, result);
    }

    /**
     * Test of setRxPrefix method, of class DLSDeviceInfo.
     */
    @Test
    public void testSetRxPrefix() {
        byte bValue = 73;
        m_info.setRxPrefix(bValue);
        assertEquals(bValue, m_info.getRxPrefix());
    }

    /**
     * Test of getRxTrailer method, of class DLSDeviceInfo.
     */
    @Test
    public void testGetRxTrailer() {
        byte expResult = 13;
        byte result = m_info.getRxTrailer();
        assertEquals(expResult, result);
    }

    /**
     * Test of setRxTrailer method, of class DLSDeviceInfo.
     */
    @Test
    public void testSetRxTrailer() {
        byte bValue = 12;
        m_info.setRxTrailer(bValue);
        assertEquals(bValue, m_info.getRxTrailer());
    }

    /**
     * Test of is8xxx method, of class DLSDeviceInfo.
     */
    @Test
    public void testis8xxx() {
        boolean expResult = false;
        boolean result = m_info.is8xxx();
        assertEquals(expResult, result);
    }

    /**
     * Test of set8xxx method, of class DLSDeviceInfo.
     */
    @Test
    public void testSet8xxx() {
        boolean bValue = true;
        m_info.set8xxx(bValue);
        assertTrue(m_info.is8xxx());
    }

    /**
     * Test of getDataPrefix method, of class DLSDeviceInfo.
     */
    @Test
    public void testGetDataPrefix() {
        byte expResult = 0;
        byte result = m_info.getDataPrefix();
        assertEquals(expResult, result);
    }

    /**
     * Test of setDataPrefix method, of class DLSDeviceInfo.
     */
    @Test
    public void testSetDataPrefix() {
        byte bValue = 67;
        m_info.setDataPrefix(bValue);
        assertEquals(bValue, m_info.getDataPrefix());
    }

    /**
     * Test of getDataSuffix method, of class DLSDeviceInfo.
     */
    @Test
    public void testGetDataSuffix() {
        byte expResult = 0;
        byte result = m_info.getDataSuffix();
        assertEquals(expResult, result);
    }

    /**
     * Test of setDataSuffix method, of class DLSDeviceInfo.
     */
    @Test
    public void testSetDataSuffix() {
        byte bValue = 99;
        m_info.setDataSuffix(bValue);
        assertEquals(bValue, m_info.getDataSuffix());
    }

    /**
     * Test of getWMIEnabled method, of class DLSDeviceInfo.
     */
    @Test
    public void testGetWMIEnabled() {
        boolean expResult = false;
        boolean result = m_info.isWMIEnabled();
        assertEquals(expResult, result);
    }

    /**
     * Test of setWMIEnabled method, of class DLSDeviceInfo.
     */
    @Test
    public void testSetWMIEnabled() {
        boolean bValue = true;
        m_info.setWMIEnabled(bValue);
        assertTrue(m_info.isWMIEnabled());
    }

    /**
     * Test of isMBeansEnabled method, of class DLSDeviceInfo.
     */
    @Test
    public void testisMBeansEnabled() {
        boolean expResult = false;
        boolean result = m_info.isMBeansEnabled();
        assertEquals(expResult, result);
    }

    /**
     * Test of setMBeansEnabled method, of class DLSDeviceInfo.
     */
    @Test
    public void testSetMBeansEnabled() {
        boolean bValue = true;
        m_info.setMBeansEnabled(bValue);
        assertTrue(m_info.isMBeansEnabled());
    }

    /**
     * Test of isDisableOnExit method, of class DLSDeviceInfo.
     */
    @Test
    public void testisDisableOnExit() {
        boolean expResult = false;
        boolean result = m_info.isDisableOnExit();
        assertEquals(expResult, result);
    }

    /**
     * Test of setDisableOnExit method, of class DLSDeviceInfo.
     */
    @Test
    public void testSetDisableOnExit() {
        boolean bValue = true;
        m_info.setDisableOnExit(bValue);
        assertTrue(m_info.isDisableOnExit());
    }

    /**
     * Test of getDecodingType method, of class DLSDeviceInfo.
     */
    @Test
    public void testGetDecodingType() {
        String expResult = Constants.DECODE_TYPE_US;
        String result = m_info.getDecodingType();
        assertEquals(expResult, result);
    }

    /**
     * Test of setDecodingType method, of class DLSDeviceInfo.
     */
    @Test
    public void testSetDecodingType() {
        String strValue = "asdf";
        m_info.setDecodingType(strValue);
        assertEquals(strValue, m_info.getDecodingType());
    }

    /**
     * Test of getLogicalFlashName method, of class DLSDeviceInfo.
     */
    @Test
    public void testGetLogicalFlashName() {
        String expResult = "DLS-USB-Flash";
        String result = m_info.getLogicalFlashName();
        assertEquals(expResult, result);
    }

    /**
     * Test of setLogicalFlashName method, of class DLSDeviceInfo.
     */
    @Test
    public void testSetLogicalFlashName() {
        String strValue = "qwer";
        m_info.setLogicalFlashName(strValue);
        assertEquals(strValue, m_info.getLogicalFlashName());
    }

    /**
     * Test of getUpdateUsageNumber method, of class DLSDeviceInfo.
     */
    @Test
    public void testGetUpdateUsageNumber() {
        String expResult = "A000";
        String result = m_info.getUpdateUsageNumber();
        assertEquals(expResult, result);
    }

    /**
     * Test of setUpdateUsageNumber method, of class DLSDeviceInfo.
     */
    @Test
    public void testSetUpdateUsageNumber() {
        String strValue = "23";
        m_info.setUpdateUsageNumber(strValue);
        assertEquals(strValue, m_info.getUpdateUsageNumber());
    }

    /**
     * Test of isUseVirtualPort method, of class DLSDeviceInfo.
     */
    @Test
    public void testisUseVirtualPort() {
        boolean expResult = false;
        boolean result = m_info.isUseVirtualPort();
        assertEquals(expResult, result);
    }

    /**
     * Test of setUseVirtualPort method, of class DLSDeviceInfo.
     */
    @Test
    public void testSetUseVirtualPort() {
        boolean bValue = true;
        m_info.setUseVirtualPort(bValue);
        assertTrue(m_info.isUseVirtualPort());
    }

    /**
     * Test of getDeviceClass method, of class DLSDeviceInfo.
     */
    @Test
    public void testGetDeviceClass() {
        String expResult = "RS232Scanner";
        String result = m_info.getDeviceClass();
        assertEquals(expResult, result);
    }

    /**
     * Test of setDeviceClass method, of class DLSDeviceInfo.
     */
    @Test
    public void testSetDeviceClass() {
        String strValue = "Scanner";
        m_info.setDeviceClass(strValue);
        assertEquals(strValue, m_info.getDeviceClass());
    }

    /**
     * Test of getUseCOMXOnLinux method, of class DLSDeviceInfo.
     */
    @Test
    public void testGetUseCOMXOnLinux() {
        boolean expResult = false;
        boolean result = m_info.isUseVirtualPort();
        assertEquals(expResult, result);
    }

    /**
     * Test of setUseCOMXOnLinux method, of class DLSDeviceInfo.
     */
    @Test
    public void testSetUseCOMXOnLinux() {
        boolean bValue = true;
        m_info.setUseCOMXOnLinux(bValue);
        assertTrue(m_info.isUseCOMXOnLinux());
    }

    /**
     * Test of getIPPort method, of class DLSDeviceInfo.
     */
    @Test
    public void testGetIPPort() {
        String expResult = "26666";
        String result = m_info.getIPPort();
        assertEquals(expResult, result);
    }

    /**
     * Test of setIPPort method, of class DLSDeviceInfo.
     */
    @Test
    public void testSetIPPort() {
        String strValue = "30";
        m_info.setIPPort(strValue);
        assertEquals(strValue, m_info.getIPPort());
    }

    /**
     * Test of getIPAddress method, of class DLSDeviceInfo.
     */
    @Test
    public void testGetIPAddress() {
        String expResult = "192.168.0.2";
        String result = m_info.getIPAddress();
        assertEquals(expResult, result);
    }

    /**
     * Test of setIPAddress method, of class DLSDeviceInfo.
     */
    @Test
    public void testSetIPAddress() {
        String strValue = "127.0.0.1";
        m_info.setIPAddress(strValue);
        assertEquals(strValue, m_info.getIPAddress());
    }

    /**
     * Test of getLaneNumber method, of class DLSDeviceInfo.
     */
    @Test
    public void testGetLaneNumber() {
        String expResult = "0";
        String result = m_info.getLaneNumber();
        assertEquals(expResult, result);
    }

    /**
     * Test of setLaneNumber method, of class DLSDeviceInfo.
     */
    @Test
    public void testSetLaneNumber() {
        String strValue = "11";
        m_info.setLaneNumber(strValue);
        assertEquals(strValue, m_info.getLaneNumber());
    }

    /**
     * Test of getImageBuffers method, of class DLSDeviceInfo.
     */
    @Test
    public void testGetImageBuffers() {
        String expResult = "1";
        String result = m_info.getImageBuffers();
        assertEquals(expResult, result);
    }

//    /**
//     * Test of setImageBuffers method, of class DLSDeviceInfo.
//     */
//    @Test
//    public void testSetImageBuffers() {
//        String strValue = "foo";
//        m_info.setImageBuffers(strValue);
//        assertEquals(strValue, m_info.getImageBuffers());
//    }

    /**
     * Test of getServiceVersion method, of class DLSDeviceInfo.
     */
    @Test
    public void testGetServiceVersion() {
        String expResult = "1.14.000";
        String result = m_info.getServiceVersion();
        assertEquals(expResult, result);
    }

    @Test
    public void testSetLocalPortName() {
        m_info.setLocalPortName("Test");
        assertThat(m_info.getLocalPortName()).isEqualTo("Test");
    }

    @Test
    public void testSetAutoSearchComm() {
        m_info.setAutoSearchComm(false);
        assertThat(m_info.isAutoSearchComm()).isFalse();
    }

    @Test
    public void testSetConfigOnClaim() {
        m_info.setConfigOnClaim(true);
        assertThat(m_info.isConfigOnClaim()).isTrue();
    }

    @Test
    public void testSetConfigWithDIO() {
        m_info.setConfigWithDIO(false);
        assertThat(m_info.isConfigWithDIO()).isFalse();
    }

    @Test
    public void testSetHDLRecordTimeout() {
        m_info.setHDLRecordTimeout(12);
        assertThat(m_info.getHDLRecordTimeout()).isEqualTo(12);
    }

    /**
     * Test of setServiceVersion method, of class DLSDeviceInfo.
     */
    @Test
    public void testSetServiceVersion() {
        String strValue = "5051";
        m_info.setServiceVersion(strValue);
        assertEquals(strValue, m_info.getServiceVersion());
    }

    /**
     * Test of isUseSunJavaxComm method, of class DLSDeviceInfo.
     */
    @Test
    public void testisUseSunJavaxComm() {
        boolean expResult = false;
        boolean result = m_info.isUseSunJavaxComm();
        assertEquals(expResult, result);
    }

    /**
     * Test of setUseSunJavaxComm method, of class DLSDeviceInfo.
     */
    @Test
    public void testSetUseSunJavaxComm() {
        boolean bValue = true;
        m_info.setUseSunJavaxComm(bValue);
        assertTrue(m_info.isUseSunJavaxComm());
    }

    /**
     * Test of get9xxx method, of class DLSDeviceInfo.
     */
    @Test
    public void testGet9xxx() {
        boolean expResult = false;
        boolean result = m_info.is9xxx();
        assertEquals(expResult, result);
    }

    /**
     * Test of set9xxx method, of class DLSDeviceInfo.
     */
    @Test
    public void testSet9xxx() {
        boolean bValue = true;
        m_info.set9xxx(bValue);
        assertTrue(m_info.is9xxx());
    }

    /**
     * Test of isUseBluetoothDongle method, of class DLSDeviceInfo.
     */
    @Test
    public void testisUseBluetoothDongle() {
        boolean expResult = false;
        boolean result = m_info.isUseBluetoothDongle();
        assertEquals(expResult, result);
    }

    /**
     * Test of setUseBluetoothDongle method, of class DLSDeviceInfo.
     */
    @Test
    public void testSetUseBluetoothDongle() {
        boolean bValue = true;
        m_info.setUseBluetoothDongle(bValue);
        assertTrue(m_info.isUseBluetoothDongle());
    }

//    /**
//     * Test of getHexByte method, of class DLSDeviceInfo.
//     */
//    @Test
//    public void testGetHexByte() {
//        int option = 0;
//        DLSDeviceInfo instance = new DLSDeviceInfo();
//        byte expResult = 15;
//        instance.setServiceInstanceFactoryClass("F");
//        byte result = instance.getHexByte(option);
//        assertEquals(expResult, result);
//    }
//
//    /**
//     * Test of setHexByte method, of class DLSDeviceInfo.
//     */
//    @Test
//    public void testSetHexByte() {
//        byte bValue = 14;
//        int option = 0;
//        m_info.setHexByte(bValue, option);
//        assertEquals(bValue, m_info.getHexByte(option));
//    }

    private void setUpProfileManager() throws APosException {
        DLAPosProfile profile = new DLAPosProfile();
        profile.load(jsonProfile);
        when(profileManager.getConfigurationForProfileId("DLS-USB-Flash")).thenReturn(profile);
    }

}