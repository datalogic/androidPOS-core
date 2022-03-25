package com.datalogic.dlapos.androidpos.common;

import android.util.Log;

import java.io.Closeable;
import java.io.IOException;
import java.util.Locale;

/**
 * Class exposing support functions.
 */
public class FunctionLib {

    private FunctionLib() {
    }

    /**
     * Converts a byte array to its string equivalent used for interpreting
     * images sent via serial communication. All Values are interpreted as
     * ASCII codes except for values {@code 0-9}, which are mapped to their
     * integer equivalents.
     *
     * @param data byte array containing image data
     * @return String containing interpreted data
     */
    public static String byteArrayToStringImage(byte[] data) {
        StringBuilder result = new StringBuilder();
        for (byte datum : data) {
            if (datum >= 0 && datum <= 9) {
                String format = String.format(Locale.getDefault(), "%d", datum);
                result.append(format);
            } else {
                result.append((char) datum);
            }
        }
        return result.toString();
    }

    /**
     * Function to convert a byte array into a string with the format "[<data.length>] hexString"
     *
     * @param data byte array to convert.
     * @return the converted string.
     */
    public static String byteArrayToString(byte[] data) {
        String result;
        StringBuilder sb = new StringBuilder();

        if (data == null) {
            return "[0] **invalid packet - byte array is null.";
        }

        if (data.length == 0) {
            return "[0] **invalid packet - byte array is empty.";
        }

        for (byte datum : data) {
            sb.append(String.format(" 0x%02X", datum));
        }
        result = "[" + data.length + "]" + sb.toString();
        return result;
    }

    /**
     * Converts a string representation of bytes into a byte array.
     *
     * @param sHex String containing hex representation
     * @return byte array containing hex values
     */
    public static byte[] hexStringToByteArray(String sHex) {
        if (sHex == null) {
            throw new IllegalArgumentException();
        } else {
            //Remove all whitespace and check for complete data
            sHex = sHex.replaceAll("\\s", "");
            if ((sHex.length() % 2) != 0) {
                throw new IllegalArgumentException();
            }
        }
        final char[] chars = sHex.toCharArray();
        final int len = chars.length;
        final byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(chars[i], 16) << 4) + Character.digit(chars[i + 1], 16));
        }
        return data;
    }

    /**
     * Closes a stream or reader, logging an error on failure.
     * If {@code r} is {@code null}, this method has no effect.
     *
     * @param r stream/reader to close, may be {@code null}
     */
    public static void cleanup(Closeable r) {
        try {
            if (r != null) {
                r.close();
            }
        } catch (IOException e) {
            Log.e("FunctionLib", "Closing resource: ", e);
        }
    }
}
