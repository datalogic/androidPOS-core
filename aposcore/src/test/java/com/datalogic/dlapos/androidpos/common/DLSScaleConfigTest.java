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

public class DLSScaleConfigTest {

    //region Json profile
    private final String jsonProfile = " {\n" +
            "    \"logicalName\": \"DLS-Magellan-RS232-Scale\",\n" +
            "    \"creation\": {\n" +
            "      \"factoryClass\": \"com.dls.jpos.service.DLSScaleInstanceFactory\",\n" +
            "      \"serviceClass\": \"com.dls.jpos.service.DLSScaleService\"\n" +
            "    },\n" +
            "    \"vendor\": {\n" +
            "      \"name\": \"DLA\",\n" +
            "      \"url\": \"http://www.adc.datalogic.com\"\n" +
            "    },\n" +
            "    \"apos\": {\n" +
            "      \"category\": \"Scale\",\n" +
            "      \"version\": \"1.13\"\n" +
            "    },\n" +
            "    \"product\": {\n" +
            "      \"description\": \"ScaleService\",\n" +
            "      \"name\": \"ScaleService\",\n" +
            "      \"url\": \"http://www.adc.datalogic.com\"\n" +
            "    },\n" +
            "    \"properties\": [\n" +
            "      {\n" +
            "        \"name\": \"baudRate\",\n" +
            "        \"type\": \"String\",\n" +
            "        \"value\": \"9600\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"name\": \"canAcceptStatisticsCmd\",\n" +
            "        \"type\": \"String\",\n" +
            "        \"value\": \"False\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"name\": \"canCompareFirmwareVersion\",\n" +
            "        \"type\": \"String\",\n" +
            "        \"value\": \"False\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"name\": \"canNotifyPowerChange\",\n" +
            "        \"type\": \"String\",\n" +
            "        \"value\": \"False\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"name\": \"canUpdateFirmware\",\n" +
            "        \"type\": \"String\",\n" +
            "        \"value\": \"False\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"name\": \"dataBits\",\n" +
            "        \"type\": \"String\",\n" +
            "        \"value\": \"7\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"name\": \"decodeData\",\n" +
            "        \"type\": \"String\",\n" +
            "        \"value\": \"False\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"name\": \"deviceBus\",\n" +
            "        \"type\": \"String\",\n" +
            "        \"value\": \"RS232\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"name\": \"deviceClass\",\n" +
            "        \"type\": \"String\",\n" +
            "        \"value\": \"RS232Scale\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"name\": \"deviceDescription\",\n" +
            "        \"type\": \"String\",\n" +
            "        \"value\": \"DLS Magellan RS232 Scale\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"name\": \"deviceName\",\n" +
            "        \"type\": \"String\",\n" +
            "        \"value\": \"DLS Magellan RS232 Scale\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"name\": \"flowControl\",\n" +
            "        \"type\": \"String\",\n" +
            "        \"value\": \"None\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"name\": \"fullDisable\",\n" +
            "        \"type\": \"String\",\n" +
            "        \"value\": \"False\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"name\": \"liveWeightPollRate\",\n" +
            "        \"type\": \"String\",\n" +
            "        \"value\": \"500\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"name\": \"MBeansEnabled\",\n" +
            "        \"type\": \"String\",\n" +
            "        \"value\": \"False\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"name\": \"metricWeightMode\",\n" +
            "        \"type\": \"String\",\n" +
            "        \"value\": \"False\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"name\": \"parity\",\n" +
            "        \"type\": \"String\",\n" +
            "        \"value\": \"Even\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"name\": \"portName\",\n" +
            "        \"type\": \"String\",\n" +
            "        \"value\": \"1\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"name\": \"productId\",\n" +
            "        \"type\": \"String\",\n" +
            "        \"value\": \"0\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"name\": \"rxPrefix\",\n" +
            "        \"type\": \"String\",\n" +
            "        \"value\": \"2\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"name\": \"rxTrailer\",\n" +
            "        \"type\": \"String\",\n" +
            "        \"value\": \"d\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"name\": \"singleCable\",\n" +
            "        \"type\": \"String\",\n" +
            "        \"value\": \"False\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"name\": \"stopBits\",\n" +
            "        \"type\": \"String\",\n" +
            "        \"value\": \"2\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"name\": \"usage\",\n" +
            "        \"type\": \"String\",\n" +
            "        \"value\": \"0\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"name\": \"useVirtualPort\",\n" +
            "        \"type\": \"String\",\n" +
            "        \"value\": \"True\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"name\": \"vendorId\",\n" +
            "        \"type\": \"String\",\n" +
            "        \"value\": \"0\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"name\": \"WMIEnabled\",\n" +
            "        \"type\": \"String\",\n" +
            "        \"value\": \"False\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"name\": \"zeroValid\",\n" +
            "        \"type\": \"String\",\n" +
            "        \"value\": \"True\"\n" +
            "      }\n" +
            "    ]\n" +
            "  }";
    //endregion

    private DLSScaleConfig m_config;

    @Mock
    private final ProfileManager profileManager = mock(ProfileManager.class);


    public DLSScaleConfigTest() throws APosException {
        setUpProfileManager();
        m_config = new DLSScaleConfig();
        boolean bResult = m_config.loadConfiguration("DLS-Magellan-RS232-Scale", profileManager);
        assertTrue(bResult);
    }

    @Test
    public void testLoadConfig() {
        byte[] abData = new byte[6];
        abData[0] = 0;
        abData[1] = 0;
        abData[2] = 0;
        abData[3] = 0x04 | 0x08 | 0x20;
        abData[4] = 1;
        m_config = new DLSScaleConfig(abData);
        assertTrue(m_config.getDisplayRequired());
        assertTrue(m_config.getIndicateZeroWithLed());
        assertFalse(m_config.getMetricWeightMode());
        assertTrue(m_config.getEnforceZeroReturn());
        assertTrue(m_config.getFiveDigitWeight());
    }


    /**
     * Test of getOperationMode method, of class DLSScaleConfig.
     */
    @Test
    public void testGetOperationMode() {
        int expResult = 0;
        int result = m_config.getOperationMode();
        assertEquals(expResult, result);
    }

    /**
     * Test of setOperationMode method, of class DLSScaleConfig.
     */
    @Test
    public void testSetOperationMode() {
        int nValue = 2;
        m_config.setOperationMode(nValue);
        assertEquals(nValue, m_config.getOperationMode());
    }

    /**
     * Test of getDisplayRequired method, of class DLSScaleConfig.
     */
    @Test
    public void testGetDisplayRequired() {
        boolean expResult = true;
        boolean result = m_config.getDisplayRequired();
        assertEquals(expResult, result);
    }

    /**
     * Test of setDisplayRequired method, of class DLSScaleConfig.
     */
    @Test
    public void testSetDisplayRequired() {
        boolean bValue = false;
        m_config.setDisplayRequired(bValue);
        assertFalse(m_config.getDisplayRequired());
    }

    /**
     * Test of getIndicateZeroWithLed method, of class DLSScaleConfig.
     */
    @Test
    public void testGetIndicateZeroWithLed() {
        boolean expResult = true;
        boolean result = m_config.getIndicateZeroWithLed();
        assertEquals(expResult, result);
    }


    /**
     * Test of setIndicateZeroWithLed method, of class DLSScaleConfig.
     */
    @Test
    public void testSetIndicateZeroWithLed() {
        boolean bValue = false;
        m_config.setIndicateZeroWithLed(bValue);
        assertFalse(m_config.getIndicateZeroWithLed());
    }


    /**
     * Test of getMetricWeightMode method, of class DLSScaleConfig.
     */

    @Test
    public void testGetMetricWeightMode() {
        boolean expResult = false;
        boolean result = m_config.getMetricWeightMode();
        assertEquals(expResult, result);
    }


    /**
     * Test of setMetricWeightMode method, of class DLSScaleConfig.
     */
    @Test
    public void testSetMetricWeightMode() {
        boolean bValue = true;
        m_config.setMetricWeightMode(bValue);
        assertTrue(m_config.getMetricWeightMode());
    }

    /**
     * Test of getEnforceZeroReturn method, of class DLSScaleConfig.
     */
    @Test
    public void testGetEnforceZeroReturn() {
        boolean expResult = false;
        boolean result = m_config.getEnforceZeroReturn();
        assertEquals(expResult, result);
    }

    /**
     * Test of setEnforceZeroReturn method, of class DLSScaleConfig.
     */
    @Test
    public void testSetEnforceZeroReturn() {
        boolean bValue = true;
        m_config.setEnforceZeroReturn(bValue);
        assertTrue(m_config.getEnforceZeroReturn());
    }

    /**
     * Test of getVibrationSensitivity method, of class DLSScaleConfig.
     */
    @Test
    public void testGetVibrationSensitivity() {
        int expResult = 2;
        int result = m_config.getVibrationSensitivity();
        assertEquals(expResult, result);
    }

    /**
     * Test of setVibrationSensitivity method, of class DLSScaleConfig.
     */
    @Test
    public void testSetVibrationSensitivity() {
        int nValue = 1;
        m_config.setVibrationSensitivity(nValue);
        assertEquals(nValue, m_config.getVibrationSensitivity());
    }

    /**
     * Test of getFiveDigitWeight method, of class DLSScaleConfig.
     */
    @Test
    public void testGetFiveDigitWeight() {
        boolean expResult = true;
        boolean result = m_config.getFiveDigitWeight();
        assertEquals(expResult, result);
    }

    /**
     * Test of setFiveDigitWeight method, of class DLSScaleConfig.
     */
    @Test
    public void testSetFiveDigitWeight() {
        boolean bValue = false;
        m_config.setFiveDigitWeight(bValue);
        assertFalse(m_config.getFiveDigitWeight());
    }

    /**
     * Test of getCanStatusUpdate method, of class DLSScaleConfig.
     */
    @Test
    public void testGetCanStatusUpdate() {
        boolean expResult = true;
        boolean result = m_config.getCanStatusUpdate();
        assertEquals(expResult, result);
    }


    /**
     * Test of setCanStatusUpdate method, of class DLSScaleConfig.
     */
    @Test
    public void testSetCanStatusUpdate() {
        boolean nValue = false;
        m_config.setCanStatusUpdate(nValue);
        assertFalse(m_config.getCanStatusUpdate());
    }


    /**
     * Test of getLiveWeightPollRate method, of class DLSScaleConfig.
     */
    @Test
    public void testGetLiveWeightPollRate() {
        int expResult = 500;
        int result = m_config.getLiveWeightPollRate();
        assertEquals(expResult, result);
    }

    /**
     * Test of setLiveWeighPoll method, of class DLSScaleConfig.
     */
    @Test
    public void testSetLiveWeighPoll() {
        int nValue = 300;
        m_config.setLiveWeightPollRate(nValue);
        assertEquals(nValue, m_config.getLiveWeightPollRate());
    }


    /**
     * Test of getZeroValid method, of class DLSScaleConfig.
     */
    @Test
    public void testGetZeroValid() {
        boolean expResult = true;
        boolean result = m_config.getZeroValid();
        assertEquals(expResult, result);
    }

    /**
     * Test of setZeroValid method, of class DLSScaleConfig.
     */
    @Test
    public void testSetZeroValid() {
        boolean bValue = false;
        m_config.setZeroValid(bValue);
        assertFalse(m_config.getZeroValid());
    }

    @Test
    public void testSetMBeansEnabled() {
        m_config.setMBeansEnabled(true);
        assertThat(m_config.getMBeansEnabled()).isTrue();
    }

    @Test
    public void testSetWMIEnabled() {
        m_config.setWMIEnabled(false);
        assertThat(m_config.getWMIEnabled()).isFalse();
    }

    @Test
    public void testCanNotifyPower() {
        m_config.setCanNotifyPowerChange(false);
        assertThat(m_config.getCanNotifyPowerChange()).isFalse();
    }

    @Test
    public void testCanAcceptStatistics() {
        m_config.setCanAcceptStatisticsCmd(false);
        assertThat(m_config.getCanAcceptStatisticsCmd()).isFalse();
    }

    private void setUpProfileManager() throws APosException {
        DLAPosProfile profile = new DLAPosProfile();
        profile.load(jsonProfile);
        when(profileManager.getConfigurationForProfileId("DLS-Magellan-RS232-Scale")).thenReturn(profile);
    }

}
