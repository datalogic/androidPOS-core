package com.datalogic.dlapos.androidpos.common;

import com.datalogic.dlapos.confighelper.configurations.accessor.EciHelper;

import org.junit.Test;
import org.mockito.Mock;

import java.io.UnsupportedEncodingException;

import static org.junit.Assert.assertArrayEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ExtendedChannelInterpretationTest {
    @Mock
    private final EciHelper eciHelper = mock(EciHelper.class);

    @Test
    public void convert() {
        setUpEciHelper();
        ExtendedChannelInterpretation eci = new ExtendedChannelInterpretation(eciHelper);
        String data = "\\\\01234567890";
        String expResult = "\\\\01234567890";
        assertArrayEquals(expResult.getBytes(), eci.convert(data.getBytes()));

        data = "\\01234567890";
        expResult = "67890";
        assertArrayEquals(expResult.getBytes(), eci.convert(data.getBytes()));

        data = "\\000000helloworld";
        expResult = "helloworld";
        assertArrayEquals(expResult.getBytes(), eci.convert(data.getBytes()));

        try {
            data = "\\000000����🇙��\\000023����🇙��\\\\000899\\000011����🇙��";
            expResult = "����🇙�涾��🇙��\\000899" + new String("����🇙��".getBytes(), "ISO8859_9");
            //TODO: this one fail, even on JavaPOS
            /* assertArrayEquals(expResult.getBytes(), eci.convert(data.getBytes()));*/

            data = "\\000028¶¾ÀÆ\\\\ðŸ‡\\\\\\\\123456\\\\™Ñæ";
            expResult = new String("¶¾ÀÆ\\ðŸ‡\\\\123456\\™Ñæ".getBytes(), "Big5");
            assertArrayEquals(expResult.getBytes(), eci.convert(data.getBytes()));

            data = "\\000025¶¾ÀÆ\\\\ðŸ‡\\\\\\\\123456\\\\™Ñæ";
            expResult = new String("¶¾ÀÆ\\ðŸ‡\\\\123456\\™Ñæ".getBytes(), "UTF-16");
            assertArrayEquals(expResult.getBytes(), eci.convert(data.getBytes()));

        } catch (UnsupportedEncodingException ex) {
            System.out.println("ERROR: Encoding Exception");
        }

        data = "\\000025¶¾ÀÆ\\\\ðŸ‡\\\\\\\\123456\\\\™Ñæ\\000000¶¾ÀÆðŸ‡™Ñæ\\000030¶¾ÀÆðŸ‡™Ñæ\\000017¶¾ÀÆðŸ‡™Ñæ\\000024¶¾ÀÆðŸ‡™Ñæ\\000020¶¾ÀÆðŸ‡™Ñæ";

        ExtendedChannelInterpretation eci2 = new ExtendedChannelInterpretation(eciHelper);
        assertArrayEquals(eci2.convert(data.getBytes()), eci.convert(data.getBytes()));
    }

    private void setUpEciHelper() {
        when(eciHelper.getJavaCode(0)).thenReturn("");
        when(eciHelper.getJavaCode(1)).thenReturn("");
        when(eciHelper.getJavaCode(2)).thenReturn("");
        when(eciHelper.getJavaCode(3)).thenReturn("ISO8859_1");
        when(eciHelper.getJavaCode(4)).thenReturn("ISO8859_2");
        when(eciHelper.getJavaCode(5)).thenReturn("ISO8859_3");
        when(eciHelper.getJavaCode(6)).thenReturn("ISO8859_4");
        when(eciHelper.getJavaCode(7)).thenReturn("ISO8859_5");
        when(eciHelper.getJavaCode(8)).thenReturn("ISO8859_6");
        when(eciHelper.getJavaCode(9)).thenReturn("ISO8859_7");
        when(eciHelper.getJavaCode(10)).thenReturn("ISO8859_8");
        when(eciHelper.getJavaCode(11)).thenReturn("ISO8859_9");
        when(eciHelper.getJavaCode(12)).thenReturn("");
        when(eciHelper.getJavaCode(13)).thenReturn("Cp874");
        when(eciHelper.getJavaCode(14)).thenReturn("");
        when(eciHelper.getJavaCode(15)).thenReturn("ISO8859_13");
        when(eciHelper.getJavaCode(16)).thenReturn("");
        when(eciHelper.getJavaCode(17)).thenReturn("ISO8859_15");
        when(eciHelper.getJavaCode(18)).thenReturn("");
        when(eciHelper.getJavaCode(19)).thenReturn("");
        when(eciHelper.getJavaCode(20)).thenReturn("SJIS");
        when(eciHelper.getJavaCode(21)).thenReturn("Cp1250");
        when(eciHelper.getJavaCode(22)).thenReturn("Cp12501");
        when(eciHelper.getJavaCode(23)).thenReturn("Cp1252");
        when(eciHelper.getJavaCode(24)).thenReturn("Cp1256");
        when(eciHelper.getJavaCode(25)).thenReturn("UTF-16");
        when(eciHelper.getJavaCode(26)).thenReturn("UTF-8");
        when(eciHelper.getJavaCode(27)).thenReturn("");
        when(eciHelper.getJavaCode(28)).thenReturn("Big5");
        when(eciHelper.getJavaCode(29)).thenReturn("GB18030");
        when(eciHelper.getJavaCode(30)).thenReturn("Cp949");
        when(eciHelper.getJavaCode(899)).thenReturn("");
    }
}