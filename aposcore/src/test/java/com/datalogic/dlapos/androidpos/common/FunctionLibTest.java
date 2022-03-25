package com.datalogic.dlapos.androidpos.common;

import org.junit.Test;
import org.mockito.Mock;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class FunctionLibTest {

    @Mock
    private final Closeable _closeable = mock(Closeable.class);

    //region byteArrayToStringImage
    @Test
    public void byteArrayToStringImage() {
        byte[] testInput = {1};
        assertThat(FunctionLib.byteArrayToStringImage(testInput)).isEqualTo("1");
        testInput = new byte[]{1, 2};
        assertThat(FunctionLib.byteArrayToStringImage(testInput)).isEqualTo("12");
        testInput = new byte[]{72, 101, 108, 108, 111};
        assertThat(FunctionLib.byteArrayToStringImage(testInput)).isEqualTo("Hello");
    }
    //endregion

    //region byteArrayToString
    @Test
    public void byteArrayToString() {
        byte[] testInput = {1};
        assertThat(FunctionLib.byteArrayToString(testInput)).isEqualTo("[1] 0x01");
        testInput = new byte[]{1, 2};
        assertThat(FunctionLib.byteArrayToString(testInput)).isEqualTo("[2] 0x01 0x02");
        testInput = new byte[]{72, 101, 108, 108, 111};
        assertThat(FunctionLib.byteArrayToString(testInput)).isEqualTo("[5] 0x48 0x65 0x6C 0x6C 0x6F");
    }

    @Test
    public void byteArrayToStringNullData() {
        assertThat(FunctionLib.byteArrayToString(null)).isEqualTo("[0] **invalid packet - byte array is null.");
    }

    @Test
    public void byteArrayToStringEmpty() {
        assertThat(FunctionLib.byteArrayToString(new byte[0])).isEqualTo("[0] **invalid packet - byte array is empty.");
    }

    //endregion

    //region hexStringToByteArray
    @Test
    public void hexStringToByteArray() {
        byte[] result = FunctionLib.hexStringToByteArray("01");
        assertThat(result.length).isEqualTo(1);
        assertThat(result[0]).isEqualTo(1);

        result = FunctionLib.hexStringToByteArray("0102");
        assertThat(result.length).isEqualTo(2);
        assertThat(result[0]).isEqualTo(1);
        assertThat(result[1]).isEqualTo(2);

        result = FunctionLib.hexStringToByteArray("01 02");
        assertThat(result.length).isEqualTo(2);
        assertThat(result[0]).isEqualTo(1);
        assertThat(result[1]).isEqualTo(2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void hexStringToByteArrayNullString() {
        FunctionLib.hexStringToByteArray(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void hexStringToByteArrayOddString() {
        FunctionLib.hexStringToByteArray("1");
    }

    @Test(expected = IllegalArgumentException.class)
    public void hexStringToByteArrayOddString2() {
        FunctionLib.hexStringToByteArray("101");
    }
    //endregion

    //region cleanUp
    @Test
    public void cleanUp() throws IOException {
        FunctionLib.cleanup(_closeable);
        verify(_closeable, times(1)).close();
    }

    @Test
    public void cleanUpNull() {
        FunctionLib.cleanup(null);
    }

    @Test
    public void cleanUpException() {
        AtomicBoolean called = new AtomicBoolean(false);
        Closeable exceptionThrower = () -> {
            called.set(true);
            throw new IOException();
        };
        FunctionLib.cleanup(exceptionThrower);
        assertThat(called.get()).isTrue();
    }
    //endregion
}