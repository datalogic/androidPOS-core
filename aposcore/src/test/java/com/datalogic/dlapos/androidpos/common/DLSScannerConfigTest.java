package com.datalogic.dlapos.androidpos.common;

import com.datalogic.dlapos.commons.support.APosException;
import com.datalogic.dlapos.confighelper.configurations.accessor.ProfileManager;
import com.datalogic.dlapos.confighelper.configurations.support.DLAPosProfile;

import org.junit.Test;
import org.mockito.Mock;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DLSScannerConfigTest {

    //region JsonProfile
    private final String jsonProfile = " {\n" +
            "    \"logicalName\": \"DLS-3200-USB-Scanner\",\n" +
            "    \"creation\": {\n" +
            "      \"factoryClass\": \"com.dls.jpos.service.DLSScannerInstanceFactory\",\n" +
            "      \"serviceClass\": \"com.dls.jpos.service.DLSScannerService\"\n" +
            "    },\n" +
            "    \"vendor\": {\n" +
            "      \"name\": \"DLA\",\n" +
            "      \"url\": \"http://www.adc.datalogic.com\"\n" +
            "    },\n" +
            "    \"apos\": {\n" +
            "      \"category\": \"Scanner\",\n" +
            "      \"version\": \"1.13\"\n" +
            "    },\n" +
            "    \"product\": {\n" +
            "      \"description\": \"ScannerService\",\n" +
            "      \"name\": \"ScannerService\",\n" +
            "      \"url\": \"http://www.adc.datalogic.com\"\n" +
            "    },\n" +
            "    \"properties\": [\n" +
            "      {\n" +
            "        \"name\": \"beepDuration\",\n" +
            "        \"type\": \"String\",\n" +
            "        \"value\": \"1\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"name\": \"beepFrequency\",\n" +
            "        \"type\": \"String\",\n" +
            "        \"value\": \"2\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"name\": \"beepVolume\",\n" +
            "        \"type\": \"String\",\n" +
            "        \"value\": \"2\"\n" +
            "      },\n" +
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
            "        \"name\": \"canNotifyPowerChange\",\n" +
            "        \"type\": \"String\",\n" +
            "        \"value\": \"True\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"name\": \"canUpdateFirmware\",\n" +
            "        \"type\": \"String\",\n" +
            "        \"value\": \"True\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"name\": \"decodeData\",\n" +
            "        \"type\": \"String\",\n" +
            "        \"value\": \"False\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"name\": \"decodeType\",\n" +
            "        \"type\": \"String\",\n" +
            "        \"value\": \"standard\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"name\": \"deviceBus\",\n" +
            "        \"type\": \"String\",\n" +
            "        \"value\": \"USB\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"name\": \"deviceClass\",\n" +
            "        \"type\": \"String\",\n" +
            "        \"value\": \"USBScanner\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"name\": \"deviceDescription\",\n" +
            "        \"type\": \"String\",\n" +
            "        \"value\": \"DLS Magellan 3200 USB Scanner\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"name\": \"deviceName\",\n" +
            "        \"type\": \"String\",\n" +
            "        \"value\": \"DLS Magellan 3200\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"name\": \"disableOnExit\",\n" +
            "        \"type\": \"String\",\n" +
            "        \"value\": \"False\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"name\": \"doubleReadTimeout\",\n" +
            "        \"type\": \"String\",\n" +
            "        \"value\": \"2\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"name\": \"enable2DigitSups\",\n" +
            "        \"type\": \"String\",\n" +
            "        \"value\": \"False\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"name\": \"enable4DigitPriceCheckDigit\",\n" +
            "        \"type\": \"String\",\n" +
            "        \"value\": \"False\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"name\": \"enable5DigitPriceCheckDigit\",\n" +
            "        \"type\": \"String\",\n" +
            "        \"value\": \"False\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"name\": \"enable5DigitSups\",\n" +
            "        \"type\": \"String\",\n" +
            "        \"value\": \"False\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"name\": \"enableBarCodeProgramming\",\n" +
            "        \"type\": \"String\",\n" +
            "        \"value\": \"True\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"name\": \"enableCodabar\",\n" +
            "        \"type\": \"String\",\n" +
            "        \"value\": \"True\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"name\": \"enableCode128\",\n" +
            "        \"type\": \"String\",\n" +
            "        \"value\": \"True\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"name\": \"enableCode128Sups\",\n" +
            "        \"type\": \"String\",\n" +
            "        \"value\": \"False\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"name\": \"enableCode39\",\n" +
            "        \"type\": \"String\",\n" +
            "        \"value\": \"True\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"name\": \"enableCode39CheckDigit\",\n" +
            "        \"type\": \"String\",\n" +
            "        \"value\": \"False\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"name\": \"enableCode93\",\n" +
            "        \"type\": \"String\",\n" +
            "        \"value\": \"True\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"name\": \"enableEANJAN2LabelDecode\",\n" +
            "        \"type\": \"String\",\n" +
            "        \"value\": \"False\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"name\": \"enableGoodReadBeep\",\n" +
            "        \"type\": \"String\",\n" +
            "        \"value\": \"True\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"name\": \"enableInterleaved\",\n" +
            "        \"type\": \"String\",\n" +
            "        \"value\": \"True\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"name\": \"enableITFCheckDigit\",\n" +
            "        \"type\": \"String\",\n" +
            "        \"value\": \"True\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"name\": \"enableLaserOnOffSwitch\",\n" +
            "        \"type\": \"String\",\n" +
            "        \"value\": \"True\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"name\": \"enableUCCEAN128\",\n" +
            "        \"type\": \"String\",\n" +
            "        \"value\": \"True\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"name\": \"enableUPCACheckDigit\",\n" +
            "        \"type\": \"String\",\n" +
            "        \"value\": \"False\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"name\": \"enableUPCAtoEAN13Expansion\",\n" +
            "        \"type\": \"String\",\n" +
            "        \"value\": \"False\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"name\": \"enableUPCD1D5\",\n" +
            "        \"type\": \"String\",\n" +
            "        \"value\": \"True\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"name\": \"enableUPCEAN\",\n" +
            "        \"type\": \"String\",\n" +
            "        \"value\": \"True\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"name\": \"enableUPCECheckDigit\",\n" +
            "        \"type\": \"String\",\n" +
            "        \"value\": \"False\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"name\": \"enableUPCEtoEAN13Expansion\",\n" +
            "        \"type\": \"String\",\n" +
            "        \"value\": \"False\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"name\": \"enableUPCEtoUPCAExpansion\",\n" +
            "        \"type\": \"String\",\n" +
            "        \"value\": \"False\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"name\": \"enableVolumeSwitch\",\n" +
            "        \"type\": \"String\",\n" +
            "        \"value\": \"True\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"name\": \"fullDisable\",\n" +
            "        \"type\": \"String\",\n" +
            "        \"value\": \"False\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"name\": \"itfLength1\",\n" +
            "        \"type\": \"String\",\n" +
            "        \"value\": \"4\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"name\": \"itfLength2\",\n" +
            "        \"type\": \"String\",\n" +
            "        \"value\": \"0\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"name\": \"itfRange\",\n" +
            "        \"type\": \"String\",\n" +
            "        \"value\": \"0\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"name\": \"laserTimeout\",\n" +
            "        \"type\": \"String\",\n" +
            "        \"value\": \"1\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"name\": \"ledGoodReadDuration\",\n" +
            "        \"type\": \"String\",\n" +
            "        \"value\": \"1\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"name\": \"MBeansEnabled\",\n" +
            "        \"type\": \"String\",\n" +
            "        \"value\": \"True\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"name\": \"motorTimeout\",\n" +
            "        \"type\": \"String\",\n" +
            "        \"value\": \"1\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"name\": \"productId\",\n" +
            "        \"type\": \"String\",\n" +
            "        \"value\": \"150b\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"name\": \"rxPrefix\",\n" +
            "        \"type\": \"String\",\n" +
            "        \"value\": \"0\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"name\": \"rxTrailer\",\n" +
            "        \"type\": \"String\",\n" +
            "        \"value\": \"d\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"name\": \"scanControl\",\n" +
            "        \"type\": \"String\",\n" +
            "        \"value\": \"False\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"name\": \"storeLabelSecurityLevel\",\n" +
            "        \"type\": \"String\",\n" +
            "        \"value\": \"0\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"name\": \"twoItfs\",\n" +
            "        \"type\": \"String\",\n" +
            "        \"value\": \"0\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"name\": \"usage\",\n" +
            "        \"type\": \"String\",\n" +
            "        \"value\": \"4a00\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"name\": \"vendorId\",\n" +
            "        \"type\": \"String\",\n" +
            "        \"value\": \"05f9\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"name\": \"WMIEnabled\",\n" +
            "        \"type\": \"String\",\n" +
            "        \"value\": \"True\"\n" +
            "      }\n" +
            "    ]\n" +
            "  }";
    //endregion

    private final DLSScannerConfig m_config;

    @Mock
    private final ProfileManager profileManager = mock(ProfileManager.class);

    public DLSScannerConfigTest() throws APosException {
        setUpProfileManager();
        m_config = new DLSScannerConfig();
        boolean bResult = m_config.loadConfiguration("DLS-3200-USB-Scanner", profileManager);
        assertTrue(bResult);
        assertThat(m_config.getLogicalName()).isEqualTo("DLS-3200-USB-Scanner");
    }


    /**
     * Test of getEnableUPCEAN method, of class DLSScannerConfig.
     */
    @Test
    public void testGetEnableUPCEAN() {
        boolean result = m_config.getEnableUPCEAN();
        assertTrue(result);
    }

    /**
     * Test of setEnableUPCEAN method, of class DLSScannerConfig.
     */
    @Test
    public void testSetEnableUPCEAN() {
        boolean bEnable = false;
        m_config.setEnableUPCEAN(bEnable);
        assertFalse(m_config.getEnableUPCEAN());
    }

    /**
     * Test of getEnableUPCD1D5 method, of class DLSScannerConfig.
     */
    @Test
    public void testGetEnableUPCD1D5() {
        boolean result = m_config.getEnableUPCD1D5();
        assertTrue(result);
    }

    /**
     * Test of setEnableUPCD1D5 method, of class DLSScannerConfig.
     */
    @Test
    public void testSetEnableUPCD1D5() {
        boolean bEnable = false;
        m_config.setEnableUPCD1D5(bEnable);
        assertFalse(m_config.getEnableUPCD1D5());
    }

    /**
     * Test of getEnableCode39 method, of class DLSScannerConfig.
     */
    @Test
    public void testGetEnableCode39() {
        boolean result = m_config.getEnableCode39();
        assertTrue(result);
    }

    /**
     * Test of setEnableCode39 method, of class DLSScannerConfig.
     */
    @Test
    public void testSetEnableCode39() {
        boolean bEnable = false;
        m_config.setEnableCode39(bEnable);
        assertFalse(m_config.getEnableCode39());
    }

    /**
     * Test of getEnableInterleaved method, of class DLSScannerConfig.
     */
    @Test
    public void testGetEnableInterleaved() {
        boolean result = m_config.getEnableInterleaved();
        assertTrue(result);
    }

    /**
     * Test of setEnableInterleaved method, of class DLSScannerConfig.
     */
    @Test
    public void testSetEnableInterleaved() {
        boolean bEnable = false;
        m_config.setEnableInterleaved(bEnable);
        assertFalse(m_config.getEnableInterleaved());
    }

    /**
     * Test of getEnableCodabar method, of class DLSScannerConfig.
     */
    @Test
    public void testGetEnableCodabar() {
        boolean result = m_config.getEnableCodabar();
        assertTrue(result);
    }

    /**
     * Test of setEnableCodabar method, of class DLSScannerConfig.
     */
    @Test
    public void testSetEnableCodabar() {
        boolean bEnable = false;
        m_config.setEnableCodabar(bEnable);
        assertFalse(m_config.getEnableCodabar());
    }

    /**
     * Test of getEnableCode93 method, of class DLSScannerConfig.
     */
    @Test
    public void testGetEnableCode93() {
        boolean result = m_config.getEnableCode93();
        assertTrue(result);
    }

    /**
     * Test of setEnableCode93 method, of class DLSScannerConfig.
     */
    @Test
    public void testSetEnableCode93() {
        boolean bEnable = false;
        m_config.setEnableCode93(bEnable);
        assertFalse(m_config.getEnableCode93());
    }

    /**
     * Test of getEnableCode128 method, of class DLSScannerConfig.
     */
    @Test
    public void testGetEnableCode128() {
        boolean result = m_config.getEnableCode128();
        assertTrue(result);
    }

    /**
     * Test of setEnableCode128 method, of class DLSScannerConfig.
     */
    @Test
    public void testSetEnableCode128() {
        boolean bEnable = false;
        m_config.setEnableCode128(bEnable);
        assertFalse(m_config.getEnableCode128());
    }

    /**
     * Test of getEnableUCCEAN128 method, of class DLSScannerConfig.
     */
    @Test
    public void testGetEnableUCCEAN128() {
        boolean result = m_config.getEnableUCCEAN128();
        assertTrue(result);
    }

    /**
     * Test of setEnableUCCEAN128 method, of class DLSScannerConfig.
     */
    @Test
    public void testSetEnableUCCEAN128() {
        boolean bEnable = false;
        m_config.setEnableUCCEAN128(bEnable);
        assertFalse(m_config.getEnableUCCEAN128());
    }

    /**
     * Test of getEnable2DigitSups method, of class DLSScannerConfig.
     */
    @Test
    public void testGetEnable2DigitSups() {
        boolean result = m_config.getEnable2DigitSups();
        assertFalse(result);
    }

    /**
     * Test of setEnable2DigitSups method, of class DLSScannerConfig.
     */
    @Test
    public void testSetEnable2DigitSups() {
        boolean bEnable = true;
        m_config.setEnable2DigitSups(bEnable);
        assertTrue(m_config.getEnable2DigitSups());
    }

    /**
     * Test of getEnable5DigitSups method, of class DLSScannerConfig.
     */
    @Test
    public void testGetEnable5DigitSups() {
        boolean result = m_config.getEnable5DigitSups();
        assertFalse(result);
    }

    /**
     * Test of setEnable5DigitSups method, of class DLSScannerConfig.
     */
    @Test
    public void testSetEnable5DigitSups() {
        boolean bEnable = true;
        m_config.setEnable5DigitSups(bEnable);
        assertTrue(m_config.getEnable5DigitSups());
    }

    /**
     * Test of getEnableCode128Sups method, of class DLSScannerConfig.
     */
    @Test
    public void testGetEnableCode128Sups() {
        boolean expResult = false;
        boolean result = m_config.getEnableCode128Sups();
        assertEquals(expResult, result);
    }

    /**
     * Test of setEnableCode128Sups method, of class DLSScannerConfig.
     */
    @Test
    public void testSetEnableCode128Sups() {
        boolean bEnable = true;
        m_config.setEnableCode128Sups(bEnable);
        assertTrue(m_config.getEnableCode128Sups());
    }

    /**
     * Test of getEnableUPCACheckDigit method, of class DLSScannerConfig.
     */
    @Test
    public void testGetEnableUPCACheckDigit() {
        boolean expResult = false;
        boolean result = m_config.getEnableUPCACheckDigit();
        assertEquals(expResult, result);
    }

    /**
     * Test of setEnableUPCACheckDigit method, of class DLSScannerConfig.
     */
    @Test
    public void testSetEnableUPCACheckDigit() {
        boolean bEnable = true;
        m_config.setEnableUPCACheckDigit(bEnable);
        assertTrue(m_config.getEnableUPCACheckDigit());
    }

    /**
     * Test of getEnableUPCECheckDigit method, of class DLSScannerConfig.
     */
    @Test
    public void testGetEnableUPCECheckDigit() {
        boolean expResult = false;
        boolean result = m_config.getEnableUPCECheckDigit();
        assertEquals(expResult, result);
    }

    /**
     * Test of setEnableUPCECheckDigit method, of class DLSScannerConfig.
     */
    @Test
    public void testSetEnableUPCECheckDigit() {
        boolean bEnable = true;
        m_config.setEnableUPCECheckDigit(bEnable);
        assertTrue(m_config.getEnableUPCECheckDigit());
    }

    /**
     * Test of getEnableCode39CheckDigit method, of class DLSScannerConfig.
     */
    @Test
    public void testGetEnableCode39CheckDigit() {
        boolean expResult = false;
        boolean result = m_config.getEnableCode39CheckDigit();
        assertEquals(expResult, result);
    }

    /**
     * Test of setEnableCode39CheckDigit method, of class DLSScannerConfig.
     */
    @Test
    public void testSetEnableCode39CheckDigit() {
        boolean bEnable = true;
        m_config.setEnableCode39CheckDigit(bEnable);
        assertTrue(m_config.getEnableCode39CheckDigit());
    }

    /**
     * Test of getEnableITFCheckDigit method, of class DLSScannerConfig.
     */
    @Test
    public void testGetEnableITFCheckDigit() {
        boolean expResult = true;
        boolean result = m_config.getEnableITFCheckDigit();
        assertEquals(expResult, result);
    }

    /**
     * Test of setEnableITFCheckDigit method, of class DLSScannerConfig.
     */
    @Test
    public void testSetEnableITFCheckDigit() {
        boolean bEnable = true;
        m_config.setEnableITFCheckDigit(bEnable);
        assertTrue(m_config.getEnableITFCheckDigit());
    }

    /**
     * Test of getEnableEANJAN2LabelDecode method, of class DLSScannerConfig.
     */
    @Test
    public void testGetEnableEANJAN2LabelDecode() {
        boolean expResult = false;
        boolean result = m_config.getEnableEANJAN2LabelDecode();
        assertEquals(expResult, result);
    }

    /**
     * Test of setEnableEANJAN2LabelDecode method, of class DLSScannerConfig.
     */
    @Test
    public void testSetEnableEANJAN2LabelDecode() {
        boolean bEnable = true;
        m_config.setEnableEANJAN2LabelDecode(bEnable);
        assertTrue(m_config.getEnableEANJAN2LabelDecode());
    }

    /**
     * Test of getEnableUPCAtoEAN13Expansion method, of class DLSScannerConfig.
     */
    @Test
    public void testGetEnableUPCAtoEAN13Expansion() {
        boolean expResult = false;
        boolean result = m_config.getEnableUPCAtoEAN13Expansion();
        assertEquals(expResult, result);
    }

    /**
     * Test of setEnableUPCAtoEAN13Expansion method, of class DLSScannerConfig.
     */
    @Test
    public void testSetEnableUPCAtoEAN13Expansion() {
        boolean bEnable = true;
        m_config.setEnableUPCAtoEAN13Expansion(bEnable);
        assertTrue(m_config.getEnableUPCAtoEAN13Expansion());
    }

    /**
     * Test of getEnableUPCEtoEAN13Expansion method, of class DLSScannerConfig.
     */
    @Test
    public void testGetEnableUPCEtoEAN13Expansion() {
        boolean expResult = false;
        boolean result = m_config.getEnableUPCEtoEAN13Expansion();
        assertEquals(expResult, result);
    }

    /**
     * Test of setEnableUPCEtoEAN13Expansion method, of class DLSScannerConfig.
     */
    @Test
    public void testSetEnableUPCEtoEAN13Expansion() {
        boolean bEnable = true;
        m_config.setEnableUPCEtoEAN13Expansion(bEnable);
        assertTrue(m_config.getEnableUPCEtoEAN13Expansion());
    }

    /**
     * Test of getEnableUPCEtoUPCAExpansion method, of class DLSScannerConfig.
     */
    @Test
    public void testGetEnableUPCEtoUPCAExpansion() {
        boolean expResult = false;
        boolean result = m_config.getEnableUPCEtoUPCAExpansion();
        assertEquals(expResult, result);
    }

    /**
     * Test of setEnableUPCEtoUPCAExpansion method, of class DLSScannerConfig.
     */
    @Test
    public void testSetEnableUPCEtoUPCAExpansion() {
        boolean bEnable = true;
        m_config.setEnableUPCEtoUPCAExpansion(bEnable);
        assertTrue(m_config.getEnableUPCEtoUPCAExpansion());
    }

    /**
     * Test of getEnable4DigitPriceCheckDigit method, of class DLSScannerConfig.
     */
    @Test
    public void testGetEnable4DigitPriceCheckDigit() {
        boolean expResult = false;
        boolean result = m_config.getEnable4DigitPriceCheckDigit();
        assertEquals(expResult, result);
    }

    /**
     * Test of setEnable4DigitPriceCheckDigit method, of class DLSScannerConfig.
     */
    @Test
    public void testSetEnable4DigitPriceCheckDigit() {
        boolean bEnable = true;
        m_config.setEnable4DigitPriceCheckDigit(bEnable);
        assertTrue(m_config.getEnable4DigitPriceCheckDigit());
    }

    /**
     * Test of getEnable5DigitPriceCheckDigit method, of class DLSScannerConfig.
     */
    @Test
    public void testGetEnable5DigitPriceCheckDigit() {
        boolean expResult = false;
        boolean result = m_config.getEnable5DigitPriceCheckDigit();
        assertEquals(expResult, result);
    }

    /**
     * Test of setEnable5DigitPriceCheckDigit method, of class DLSScannerConfig.
     */
    @Test
    public void testSetEnable5DigitPriceCheckDigit() {
        boolean bEnable = true;
        m_config.setEnable5DigitPriceCheckDigit(bEnable);
        assertTrue(m_config.getEnable5DigitPriceCheckDigit());
    }

    /**
     * Test of getEnableGoodReadBeep method, of class DLSScannerConfig.
     */
    @Test
    public void testGetEnableGoodReadBeep() {
        boolean expResult = true;
        boolean result = m_config.getEnableGoodReadBeep();
        assertEquals(expResult, result);
    }

    /**
     * Test of setEnableGoodReadBeep method, of class DLSScannerConfig.
     */
    @Test
    public void testSetEnableGoodReadBeep() {
        boolean bEnable = false;
        m_config.setEnableGoodReadBeep(bEnable);
        assertFalse(m_config.getEnableGoodReadBeep());
    }

    /**
     * Test of getEnableBarCodeProgramming method, of class DLSScannerConfig.
     */
    @Test
    public void testGetEnableBarCodeProgramming() {
        boolean expResult = true;
        boolean result = m_config.getEnableBarCodeProgramming();
        assertEquals(expResult, result);
    }

    /**
     * Test of setEnableBarCodeProgramming method, of class DLSScannerConfig.
     */
    @Test
    public void testSetEnableBarCodeProgramming() {
        boolean bEnable = false;
        m_config.setEnableBarCodeProgramming(bEnable);
        assertFalse(m_config.getEnableBarCodeProgramming());
    }

    /**
     * Test of getEnableLaserOnOffSwitch method, of class DLSScannerConfig.
     */
    @Test
    public void testGetEnableLaserOnOffSwitch() {
        boolean expResult = true;
        boolean result = m_config.getEnableLaserOnOffSwitch();
        assertEquals(expResult, result);
    }

    /**
     * Test of setEnableLaserOnOffSwitch method, of class DLSScannerConfig.
     */
    @Test
    public void testSetEnableLaserOnOffSwitch() {
        boolean bEnable = false;
        m_config.setEnableLaserOnOffSwitch(bEnable);
        assertFalse(m_config.getEnableLaserOnOffSwitch());
    }

    /**
     * Test of getEnableVolumeSwitch method, of class DLSScannerConfig.
     */
    @Test
    public void testGetEnableVolumeSwitch() {
        boolean expResult = true;
        boolean result = m_config.getEnableVolumeSwitch();
        assertEquals(expResult, result);
    }

    /**
     * Test of setEnableVolumeSwitch method, of class DLSScannerConfig.
     */
    @Test
    public void testSetEnableVolumeSwitch() {
        boolean bEnable = false;
        m_config.setEnableVolumeSwitch(bEnable);
        assertFalse(m_config.getEnableVolumeSwitch());
    }

    /**
     * Test of getBeepVolume method, of class DLSScannerConfig.
     */
    @Test
    public void testGetBeepVolume() {
        int expResult = 2;
        int result = m_config.getBeepVolume();
        assertEquals(expResult, result);
    }

    /**
     * Test of setBeepVolume method, of class DLSScannerConfig.
     */
    @Test
    public void testSetBeepVolume() {
        int nValue = 1;
        m_config.setBeepVolume(nValue);
        int nResult = m_config.getBeepVolume();
        assertEquals(nValue, nResult);
    }

    /**
     * Test of getBeepFrequency method, of class DLSScannerConfig.
     */
    @Test
    public void testGetBeepFrequency() {
        int expResult = 2;
        int result = m_config.getBeepFrequency();
        assertEquals(expResult, result);
    }

    /**
     * Test of setBeepFrequency method, of class DLSScannerConfig.
     */
    @Test
    public void testSetBeepFrequency() {
        int nValue = 1;
        m_config.setBeepFrequency(nValue);
        int nResult = m_config.getBeepFrequency();
        assertEquals(nValue, nResult);
    }

    /**
     * Test of getBeepDuration method, of class DLSScannerConfig.
     */
    @Test
    public void testGetBeepDuration() {
        int expResult = 1;
        int result = m_config.getBeepDuration();
        assertEquals(expResult, result);
    }

    /**
     * Test of setBeepDuration method, of class DLSScannerConfig.
     */
    @Test
    public void testSetBeepDuration() {
        int nValue = 0;
        m_config.setBeepDuration(nValue);
        int result = m_config.getBeepDuration();
        assertEquals(nValue, result);
    }

    /**
     * Test of getMotorTimeout method, of class DLSScannerConfig.
     */
    @Test
    public void testGetMotorTimeout() {
        int expResult = 1;
        int result = m_config.getMotorTimeout();
        assertEquals(expResult, result);
    }

    /**
     * Test of setMotorTimeout method, of class DLSScannerConfig.
     */
    @Test
    public void testSetMotorTimeout() {
        int nValue = 0;
        m_config.setMotorTimeout(nValue);
        int result = m_config.getMotorTimeout();
        assertEquals(nValue, result);
    }

    /**
     * Test of getLaserTimeout method, of class DLSScannerConfig.
     */
    @Test
    public void testGetLaserTimeout() {
        int expResult = 1;
        int result = m_config.getLaserTimeout();
        assertEquals(expResult, result);
    }

    /**
     * Test of setLaserTimeout method, of class DLSScannerConfig.
     */
    @Test
    public void testSetLaserTimeout() {
        int nValue = 0;
        m_config.setLaserTimeout(nValue);
        int result = m_config.getLaserTimeout();
        assertEquals(nValue, result);
    }

    /**
     * Test of getDoubleReadTimeout method, of class DLSScannerConfig.
     */
    @Test
    public void testGetDoubleReadTimeout() {
        int expResult = 2;
        int result = m_config.getDoubleReadTimeout();
        assertEquals(expResult, result);
    }

    /**
     * Test of setDoubleReadTimeout method, of class DLSScannerConfig.
     */
    @Test
    public void testSetDoubleReadTimeout() {
        int nValue = 1;
        m_config.setDoubleReadTimeout(nValue);
        int result = m_config.getDoubleReadTimeout();
        assertEquals(nValue, result);
    }

    /**
     * Test of getStoreLabelSecurityLevel method, of class DLSScannerConfig.
     */
    @Test
    public void testGetStoreLabelSecurityLevel() {
        int expResult = 0;
        int result = m_config.getStoreLabelSecurityLevel();
        assertEquals(expResult, result);
    }

    /**
     * Test of setStoreLabelSecurityLevel method, of class DLSScannerConfig.
     */
    @Test
    public void testSetStoreLabelSecurityLevel() {
        int nValue = 1;
        m_config.setStoreLabelSecurityLevel(nValue);
        int result = m_config.getStoreLabelSecurityLevel();
        assertEquals(nValue, result);
    }

    /**
     * Test of getITFLength1 method, of class DLSScannerConfig.
     */
    @Test
    public void testGetITFLength1() {
        int expResult = 4;
        int result = m_config.getITFLength1();
        assertEquals(expResult, result);
    }

    /**
     * Test of setITFLength1 method, of class DLSScannerConfig.
     */
    @Test
    public void testSetITFLength1() {
        int nValue = 8;
        m_config.setITFLength1(nValue);
        int result = m_config.getITFLength1();
        assertEquals(nValue, result);
    }

    /**
     * Test of getITFLength2 method, of class DLSScannerConfig.
     */
    @Test
    public void testGetITFLength2() {
        int expResult = 0;
        int result = m_config.getITFLength2();
        assertEquals(expResult, result);
    }

    /**
     * Test of setITFLength2 method, of class DLSScannerConfig.
     */
    @Test
    public void testSetITFLength2() {
        int nValue = 16;
        m_config.setITFLength2(nValue);
        int result = m_config.getITFLength2();
        assertEquals(nValue, result);
    }

    /**
     * Test of getTwoITFs method, of class DLSScannerConfig.
     */
    @Test
    public void testGetTwoITFs() {
        boolean expResult = false;
        boolean result = m_config.getTwoITFs();
        assertEquals(expResult, result);
    }

    /**
     * Test of setTwoITFs method, of class DLSScannerConfig.
     */
    @Test
    public void testSetTwoITFs() {
        boolean bEnable = true;
        m_config.setTwoITFs(bEnable);
        assertTrue(m_config.getTwoITFs());
    }

    /**
     * Test of getITFRange method, of class DLSScannerConfig.
     */
    @Test
    public void testGetITFRange() {
        boolean expResult = false;
        boolean result = m_config.getITFRange();
        assertEquals(expResult, result);
    }

    /**
     * Test of setITFRange method, of class DLSScannerConfig.
     */
    @Test
    public void testSetITFRange() {
        boolean bEnable = true;
        m_config.setITFRange(bEnable);
        assertTrue(m_config.getITFRange());
    }

    /**
     * Test of getLEDGoodReadDuration method, of class DLSScannerConfig.
     */
    @Test
    public void testGetLEDGoodReadDuration() {
        int expResult = 1;
        int result = m_config.getLEDGoodReadDuration();
        assertEquals(expResult, result);
    }

    /**
     * Test of setLEDGoodReadDuration method, of class DLSScannerConfig.
     */
    @Test
    public void testSetLEDGoodReadDuration() {
        int nValue = 0;
        m_config.setLEDGoodReadDuration(nValue);
        int result = m_config.getLEDGoodReadDuration();
        assertEquals(nValue, result);
    }

    /**
     * Test of getCanAcceptStatisticsCommand method, of class DLSScannerConfig.
     */
    @Test
    public void testGetCanAcceptStatisticsCommand() {
        boolean expResult = true;
        boolean result = m_config.getCanAcceptStatisticsCommand();
        assertEquals(expResult, result);
    }

    /**
     * Test of setCanAcceptStatisticsCommand method, of class DLSScannerConfig.
     */
    @Test
    public void testSetCanAcceptStatisticsCommand() {
        boolean bEnable = false;
        m_config.setCanAcceptStatisticsCommand(bEnable);
        assertFalse(m_config.getCanAcceptStatisticsCommand());
    }

    /**
     * Test of getCanAcceptConfigItems method, of class DLSScannerConfig.
     */
    @Test
    public void testGetCanAcceptConfigItems() {
        boolean expResult = false;
        boolean result = m_config.getCanAcceptConfigItems();
        assertEquals(expResult, result);
    }

    /**
     * Test of setCanAcceptConfigItems method, of class DLSScannerConfig.
     */
    @Test
    public void testSetCanAcceptConfigItems() {
        boolean bEnable = true;
        m_config.setCanAcceptConfigItems(bEnable);
        assertTrue(m_config.getCanAcceptConfigItems());
    }

    /**
     * Test of getCanProgramConfigOnClaim method, of class DLSScannerConfig.
     */
    @Test
    public void testGetCanProgramConfigOnClaim() {
        boolean expResult = false;
        boolean result = m_config.getCanProgramConfigOnClaim();
        assertEquals(expResult, result);
    }

    /**
     * Test of setCanProgramConfigOnClaim method, of class DLSScannerConfig.
     */
    @Test
    public void testSetCanProgramConfigOnClaim() {
        boolean bEnable = true;
        m_config.setCanProgramConfigOnClaim(bEnable);
        assertTrue(m_config.getCanProgramConfigOnClaim());
    }

    /**
     * Test of getCanNotifyPowerChange method, of class DLSScannerConfig.
     */
    @Test
    public void testGetCanNotifyPowerChange() {
        boolean expResult = true;
        boolean result = m_config.getCanNotifyPowerChange();
        assertEquals(expResult, result);
    }

    /**
     * Test of setCanNotifyPowerChange method, of class DLSScannerConfig.
     */
    @Test
    public void testSetCanNotifyPowerChange() {
        boolean bEnable = true;
        m_config.setCanNotifyPowerChange(bEnable);
        assertTrue(m_config.getCanNotifyPowerChange());
    }

    /**
     * Test of getCanCompareFirmwareVersion method, of class DLSScannerConfig.
     */
    @Test
    public void testGetCanCompareFirmwareVersion() {
        boolean expResult = true;
        boolean result = m_config.getCanCompareFirmwareVersion();
        assertEquals(expResult, result);
    }

    /**
     * Test of getCanUpdateFirmware method, of class DLSScannerConfig.
     */
    @Test
    public void testGetCanUpdateFirmware() {
        boolean expResult = true;
        boolean result = m_config.getCanUpdateFirmware();
        assertEquals(expResult, result);
    }

    /**
     * Test of getDeleteImageFileAfterRead method, of class DLSScannerConfig.
     */
    @Test
    public void testGetDeleteImageFileAfterRead() {
        boolean expResult = true;
        boolean result = m_config.getDeleteImageFileAfterRead();
        assertEquals(expResult, result);
    }

    /**
     * Test of setDeleteImageFileAfterRead method, of class DLSScannerConfig.
     */
    @Test
    public void testSetDeleteImageFileAfterRead() {
        boolean bDeleteAfterImageFileRead = false;
        m_config.setDeleteImageFileAfterRead(bDeleteAfterImageFileRead);
        assertFalse(m_config.getDeleteImageFileAfterRead());
    }

    @Test
    public void testGetBluetoothDelayStatistics() {
        m_config.setBluetoothDelayStatistics(false);
        assertThat(m_config.getBluetoothDelayStatistics()).isFalse();
        m_config.setBluetoothDelayStatistics(true);
        assertThat(m_config.getBluetoothDelayStatistics()).isTrue();
    }

    @Test
    public void testGetExtendedHostTimeout() {
        m_config.setExtendedHostTimeout(34);
        assertThat(m_config.getExtendedHostTimeout()).isEqualTo(34);
    }

    @Test
    public void testGetHostCommandsDisabled() {
        m_config.setHostCommandsDisabled(false);
        assertThat(m_config.getHostCommandsDisabled()).isFalse();
        m_config.setHostCommandsDisabled(true);
        assertThat(m_config.getHostCommandsDisabled()).isTrue();
    }

    @Test
    public void testGetLogicalFlashName() {
        m_config.setLogicalFlashName("TEST");
        assertThat(m_config.getLogicalFlashName()).isEqualTo("TEST");
    }

    @Test
    public void testGetMBeansEnabled() {
        m_config.setMBeansEnabled(false);
        assertThat(m_config.getMBeansEnabled()).isFalse();
        m_config.setMBeansEnabled(true);
        assertThat(m_config.getMBeansEnabled()).isTrue();
    }

    @Test
    public void testGetSendIHSOnClaim() {
        m_config.setSendIHSOnClaim(false);
        assertThat(m_config.getSendIHSOnClaim()).isFalse();
        m_config.setSendIHSOnClaim(true);
        assertThat(m_config.getSendIHSOnClaim()).isTrue();
    }

    @Test
    public void testGetWMIEnabled() {
        m_config.setWMIEnabled(false);
        assertThat(m_config.getWMIEnabled()).isFalse();
        m_config.setWMIEnabled(true);
        assertThat(m_config.getWMIEnabled()).isTrue();
    }

    @Test
    public void testGetBluetoothScannerAlwaysEnabled() {
        m_config.setBluetoothScannerAlwaysEnabled(false);
        assertThat(m_config.getBluetoothScannerAlwaysEnabled()).isFalse();
        m_config.setBluetoothScannerAlwaysEnabled(true);
        assertThat(m_config.getBluetoothScannerAlwaysEnabled()).isTrue();
    }

    @Test
    public void testSetBeepDurationInvalid() {
        m_config.setBeepDuration(-1);
        assertThat(m_config.getBeepDuration()).isEqualTo(0);
        m_config.setBeepDuration(1);
        assertThat(m_config.getBeepDuration()).isEqualTo(1);
        m_config.setBeepDuration(4);
        assertThat(m_config.getBeepDuration()).isEqualTo(0);
    }

    @Test
    public void testSetBeepFrequencyInvalid() {
        m_config.setBeepFrequency(-1);
        assertThat(m_config.getBeepFrequency()).isEqualTo(0);
        m_config.setBeepFrequency(1);
        assertThat(m_config.getBeepFrequency()).isEqualTo(1);
        m_config.setBeepFrequency(4);
        assertThat(m_config.getBeepFrequency()).isEqualTo(0);
    }

    @Test
    public void testSetBeepVolumeInvalid() {
        m_config.setBeepVolume(-1);
        assertThat(m_config.getBeepVolume()).isEqualTo(0);
        m_config.setBeepVolume(1);
        assertThat(m_config.getBeepVolume()).isEqualTo(1);
        m_config.setBeepVolume(4);
        assertThat(m_config.getBeepVolume()).isEqualTo(0);
    }

    @Test
    public void testSetDoubleReadTimeoutInvalid() {
        m_config.setDoubleReadTimeout(-1);
        assertThat(m_config.getDoubleReadTimeout()).isEqualTo(0);
        m_config.setDoubleReadTimeout(1);
        assertThat(m_config.getDoubleReadTimeout()).isEqualTo(1);
        m_config.setDoubleReadTimeout(4);
        assertThat(m_config.getDoubleReadTimeout()).isEqualTo(0);
    }

    @Test
    public void testSetITFLength1OddNumber() {
        m_config.setITFLength1(7);
        assertThat(m_config.getITFLength1()).isEqualTo(8);
    }

    @Test
    public void testSetITFLength1Invalid() {
        m_config.setITFLength1(2);
        assertThat(m_config.getITFLength1()).isEqualTo(4);

        m_config.setITFLength1(56);
        assertThat(m_config.getITFLength1()).isEqualTo(32);
    }

    @Test
    public void testSetITFLength2OddNumber() {
        m_config.setITFLength2(7);
        assertThat(m_config.getITFLength2()).isEqualTo(8);
    }

    @Test
    public void testSetITFLength2Invalid() {
        m_config.setITFLength2(2);
        assertThat(m_config.getITFLength2()).isEqualTo(4);

        m_config.setITFLength2(56);
        assertThat(m_config.getITFLength2()).isEqualTo(32);
    }

    @Test
    public void testSetLaserTimeoutInvalid(){
        m_config.setLaserTimeout(-1);
        assertThat(m_config.getLaserTimeout()).isEqualTo(0);
        m_config.setLaserTimeout(1);
        assertThat(m_config.getLaserTimeout()).isEqualTo(1);
        m_config.setLaserTimeout(4);
        assertThat(m_config.getLaserTimeout()).isEqualTo(0);
    }

    @Test
    public void testSetLEDGoodReadDurationInvalid(){
        m_config.setLEDGoodReadDuration(-1);
        assertThat(m_config.getLEDGoodReadDuration()).isEqualTo(0);
        m_config.setLEDGoodReadDuration(1);
        assertThat(m_config.getLEDGoodReadDuration()).isEqualTo(1);
        m_config.setLEDGoodReadDuration(4);
        assertThat(m_config.getLEDGoodReadDuration()).isEqualTo(0);
    }

    @Test
    public void testSetMotorTimeoutInvalid(){
        m_config.setMotorTimeout(-1);
        assertThat(m_config.getMotorTimeout()).isEqualTo(0);
        m_config.setMotorTimeout(1);
        assertThat(m_config.getMotorTimeout()).isEqualTo(1);
        m_config.setMotorTimeout(9);
        assertThat(m_config.getMotorTimeout()).isEqualTo(0);
    }

    @Test
    public void testStoreLabelSecurityLevelInvalid(){
        m_config.setStoreLabelSecurityLevel(-1);
        assertThat(m_config.getStoreLabelSecurityLevel()).isEqualTo(0);
        m_config.setStoreLabelSecurityLevel(1);
        assertThat(m_config.getStoreLabelSecurityLevel()).isEqualTo(1);
        m_config.setStoreLabelSecurityLevel(9);
        assertThat(m_config.getStoreLabelSecurityLevel()).isEqualTo(0);
    }

    private void setUpProfileManager() throws APosException {
        DLAPosProfile profile = new DLAPosProfile();
        profile.load(jsonProfile);
        when(profileManager.getConfigurationForProfileId("DLS-3200-USB-Scanner")).thenReturn(profile);
    }

}