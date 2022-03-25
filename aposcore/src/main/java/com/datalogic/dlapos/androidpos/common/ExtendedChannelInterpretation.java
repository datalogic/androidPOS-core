package com.datalogic.dlapos.androidpos.common;

import android.content.Context;
import android.util.Log;

import com.datalogic.dlapos.commons.support.APosException;
import com.datalogic.dlapos.confighelper.DLAPosConfigHelper;
import com.datalogic.dlapos.confighelper.configurations.accessor.EciHelper;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class responsible of extended channel interpretation.
 */
public class ExtendedChannelInterpretation {
    private final static String TAG = ExtendedChannelInterpretation.class.getSimpleName();
    private final EciHelper eciHelper;

    /**
     * Creates new instance of ExtendedChannelInterpretation using the internal
     * default ECI code mapping.
     */
    public ExtendedChannelInterpretation(Context context) throws APosException {
        if (!DLAPosConfigHelper.getInstance(context).isInitialized())
            DLAPosConfigHelper.getInstance(context).initialize(context);
        eciHelper = DLAPosConfigHelper.getInstance(context).getEciHelper();
    }

    /**
     * Only for test purposes.
     *
     * @param testEciHelper mocked helper for tests
     */
    ExtendedChannelInterpretation(EciHelper testEciHelper) {
        this.eciHelper = testEciHelper;
    }


    /**
     * Searches for ECI codes within the label data and converts any characters
     * found within the scope of the ECI code. If no ECI code is found, label
     * data is passed back without any transformations. Multiple ECI character
     * set transformations is supported. Decoding process is in accordance with
     * the defined ECI specification (AIM Inc. ITS/04-001).
     *
     * @param labelData {@code byte array} containing raw label data
     * @return {@code byte array} containing label data that has undergone the
     * ECI decoding process
     */
    public byte[] convert(byte[] labelData) {
        String data = new String(labelData);
        ArrayList<ECICode> codeList = findECICodes(data);
        if (codeList.isEmpty()) { //return data if no ECI codes are present
            return labelData;
        }
        //initialize StringBuilder with any data that is prior to the first ECI code. Only data following an ECI escape sequence should be converted.
        StringBuilder sb = new StringBuilder(data.substring(0, codeList.get(0).startIndex));
        for (ECICode code : codeList) {
            //Strip off ECI code and append encoded string
            sb.append(encode(data.substring(code.startIndex + 7, code.endIndex), code.identifier));
        }

        String result = sb.toString();
        return result.getBytes();
    }


    /**
     * Encodes the data into the character set determined by the provided ECI
     * code.
     *
     * @param rawData {@code String} containing data to be converted
     * @param code    {@code int} indicating the ECI code to use
     * @return {@code String} containing the encoded data
     */
    private String encode(String rawData, int code) {
        //Undouble backslashes found in the data (doubled by ECI encoding process)
        String undoubledData = rawData.replace("\\\\", "\\");
        String encoding = eciHelper.getJavaCode(code);
        //determine if an invalid or unsupported code is trying to be used
        if (encoding == null) {
            Log.w(TAG, "encode: ECI code " + String.format("%06d", code) + " not recognized, using default encoding");
            return undoubledData;
        } else if (encoding.isEmpty()) {
            Log.w(TAG, "encode: ECI code " + String.format("%06d", code) + " not supported, using default encoding");
            return undoubledData;
        }

        String result = undoubledData;
        //Encode the data
        try {
            result = new String(undoubledData.getBytes(), encoding);
        } catch (UnsupportedEncodingException ex) {
            Log.e(TAG, "encode: Encoding Exception - ", ex);
            Log.w(TAG, "encode: using default encoding for ECI section");
        }
        return result;
    }


    /**
     * Searches for ECI codes and records their starting and ending positions.
     *
     * @param data {@code String} containing raw data
     * @return {@code ArrayList} containing any and all ECI codes found within
     * the data
     */
    private ArrayList<ECICode> findECICodes(String data) {
        ArrayList<ECICode> list = new ArrayList<>();
        //Looking for a pattern of a single backslash followed by 6 digits. Any backslashes contained within the data have been doubled during the ECI encoding process by the data carrier.
        Pattern rx = Pattern.compile("\\\\\\d{6}");
        Matcher m = rx.matcher(data);
        while (m.find()) {
            if (isSingleBackslash(data, m.start())) { //Verify that the backslash is not part of the data
                if (!list.isEmpty()) {
                    //Adjust the ending index of the last ECI code
                    list.get(list.size() - 1).endIndex = m.start();
                }
                //Add ECI code to the list, ending index is the rest of the data unless another ECI code is found by the next m.find()
                list.add(new ECICode(Integer.parseInt(m.group().substring(1)), m.start(), data.length()));
            }
        }
        return list;
    }


    /**
     * Determines if the backslash is a single or double.
     *
     * @param searchString {@code String} containing data to be searched
     * @param index        {@code int) indicating the index of the backslash within the
     *                     search string
     * @return {@code boolean} indicating {@code true} if the backslash is a
     * single backslash, {@code false} otherwise
     */
    private boolean isSingleBackslash(String searchString, int index) {
        int count = 0;
        while (--index >= 0) { //check previous characters
            if (searchString.charAt(index) == '\\') {
                count++;
            } else {
                break;
            }
        }
        //if there are an even number of preceding backslashes, return true
        return count % 2 == 0;
    }


    /**
     * Internal class for easily storing and manipulating ECI codes.
     */
    private class ECICode {

        /**
         * {@code int} indicating the ECI escape code
         */
        int identifier = 0;
        /**
         * {@code int} indicating the starting index of the ECI escape code
         */
        int startIndex = 0;
        /**
         * {@code int} indicating the ending index of the ECI escape code
         */
        int endIndex = 0;


        /**
         * Creates new ECI code instance.
         *
         * @param id    {@code int} indicating the ECI escape code
         * @param start {@code int} indicating the starting index of the ECI escape code
         * @param end   {@code int} indicating the ending index of the ECI escape code
         */
        public ECICode(int id, int start, int end) {
            identifier = id;
            startIndex = start;
            endIndex = end;
        }
    }

}
