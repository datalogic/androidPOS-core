package com.datalogic.dlapos.androidpos.common;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

public class DLSPropertiesTest {
    static DLSProperties oProps;

    public DLSPropertiesTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        Context context = ApplicationProvider.getApplicationContext();
        oProps = DLSProperties.getInstance(context);
    }


    @Test
    public void testGetRecordRetry() {
        System.out.println("getRecordRetry");
        int result = oProps.getRecordRetry();

        assertEquals(3, result);
    }

    /**
     * Test of get method, of class DLSProperties.
     */
    @Test
    public void testGet_String_int() {
        System.out.println("get_String_int");
        String strOption = DLSProperties.KEY_TIMEOUT;
        String empty = "";
        int nDefault = 0;
        int expResult = 5000;
        int result = oProps.get(strOption, nDefault);
        assertEquals(expResult, result);

        //corner cases

        result = oProps.get("bogus_int", 23);
        assertEquals(23, result);
        oProps.set(empty, "");
        result = oProps.get(empty, 46);
        assertEquals(46, result);
    }


    /**
     * Test of get method, of class DLSProperties.
     */
    @Test
    public void testGet_String_boolean() {
        System.out.println("get_String_boolean");
        String strOption = "ThrowExceptionOnScaleMotion";
        String test1 = "test1";
        String test2 = "test2";
        String test3 = "test3";
        String test4 = "test4";
        String empty = "empty";
        boolean result = oProps.get(strOption, true);
        assertEquals(false, result);
        oProps.set(test1, true);
        assertTrue(oProps.get(test1, false));
        oProps.set(test2, 1);
        assertTrue(oProps.get(test2, false));
        oProps.set(test3, -1);
        assertTrue(oProps.get(test3, false));
        oProps.set(test4, 2);
        assertFalse(oProps.get(test4, true));

        //corner cases
        result = oProps.get("bogus_boolean", true);
        assertTrue(result);
        oProps.set(empty, "");
        result = oProps.get(empty, true);
        assertTrue(result);
    }


    /**
     * Test of get method, of class DLSProperties.
     */
    @Test
    public void testGet_String_String() {
        System.out.println("get_String_String");
        String strDefault = "default";
        String result;

        //test corner cases
        result = oProps.get("bogus_string1", strDefault);
        assertEquals(strDefault, result);
        result = oProps.get("bogus_string2", "");
        assertTrue(result.isEmpty());
    }
}