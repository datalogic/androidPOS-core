package com.datalogic.dlapos.androidpos.common;

import org.junit.Test;

import static org.junit.Assert.*;

public class DLSExceptionTest {
    /**
     * Test of getErrorCode method, of class DLSException.
     */
    @Test
    public void testGetErrorCode() {
        DLSException instance = new DLSException(1, "Hello");
        int expResult = 1;
        int result = instance.getErrorCode();
        assertEquals(expResult, result);
    }

    /**
     * Test of getMessage method, of class DLSException.
     */
    @Test
    public void testGetMessage() {
        DLSException instance = new DLSException(1, "Hello");
        String expResult = "0x1, Hello";
        String result = instance.getMessage();
        assertEquals(expResult, result);
    }
}