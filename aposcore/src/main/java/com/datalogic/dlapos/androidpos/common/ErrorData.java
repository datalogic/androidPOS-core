package com.datalogic.dlapos.androidpos.common;

/**
 * Event data for errors.
 */
public class ErrorData extends EventData {
    private int code;
    private int exCode;
    private int locus;
    private int response;

    /**
     * Default constructor.
     * Known errors and errors code are in {@link com.datalogic.dlapos.commons.constant.ErrorConstants ErrorConstants}.
     * Known locus codes are:
     * <li>{@link com.datalogic.dlapos.commons.constant.ErrorConstants#APOS_EL_INPUT APOS_EL_INPUT}</li>
     * <li>{@link com.datalogic.dlapos.commons.constant.ErrorConstants#APOS_EL_INPUT_DATA APOS_EL_INPUT_DATA}</li>
     * <li>{@link com.datalogic.dlapos.commons.constant.ErrorConstants#APOS_EL_OUTPUT APOS_EL_OUTPUT}</li>
     * Known response codes are:
     * <li>{@link com.datalogic.dlapos.commons.constant.ErrorConstants#APOS_ER_RETRY APOS_ER_RETRY}</li>
     * <li>{@link com.datalogic.dlapos.commons.constant.ErrorConstants#APOS_ER_CLEAR APOS_ER_CLEAR}</li>
     * <li>{@link com.datalogic.dlapos.commons.constant.ErrorConstants#APOS_ER_CONTINUEINPUT APOS_ER_CONTINUEINPUT}</li>
     *
     * @param code     the error code.
     * @param exCode   the error extended code.
     * @param locus    the locus code where the error occurs.
     * @param response the code of the response to the event.
     */
    public ErrorData(int code, int exCode, int locus, int response) {
        super(EventData.ERROR_EVENT);
        this.code = code;
        this.exCode = exCode;
        this.locus = locus;
        this.response = response;
    }

    /**
     * Function to get the error code of the event.
     *
     * @return the error code of the event.
     */
    public int getCode() {
        return this.code;
    }

    /**
     * Function to get the extended code of the error.
     *
     * @return the extended code of the error.
     */
    public int getExCode() {
        return this.exCode;
    }

    /**
     * Function to get the locus where the error occurs. It could be one of:
     * <li>{@link com.datalogic.dlapos.commons.constant.ErrorConstants#APOS_EL_INPUT APOS_EL_INPUT}</li>
     * <li>{@link com.datalogic.dlapos.commons.constant.ErrorConstants#APOS_EL_INPUT_DATA APOS_EL_INPUT_DATA}</li>
     * <li>{@link com.datalogic.dlapos.commons.constant.ErrorConstants#APOS_EL_OUTPUT APOS_EL_OUTPUT}</li>
     *
     * @return the locus where the error occurs.
     */
    public int getLocus() {
        return this.locus;
    }

    /**
     * Function to get the response code. It could be one of:
     * <li>{@link com.datalogic.dlapos.commons.constant.ErrorConstants#APOS_ER_RETRY APOS_ER_RETRY}</li>
     * <li>{@link com.datalogic.dlapos.commons.constant.ErrorConstants#APOS_ER_CLEAR APOS_ER_CLEAR}</li>
     * <li>{@link com.datalogic.dlapos.commons.constant.ErrorConstants#APOS_ER_CONTINUEINPUT APOS_ER_CONTINUEINPUT}</li>
     *
     * @return the response code
     */
    public int getResponse() {
        return this.response;
    }

    /**
     * Function to set an error code.
     *
     * @param code the error code (known are listed in {@link com.datalogic.dlapos.commons.constant.ErrorConstants ErrorConstants}).
     */
    public void setCode(int code) {
        this.code = code;
    }

    /**
     * Function to set an extended error code.
     *
     * @param exCode the extended error code (known are listed in {@link com.datalogic.dlapos.commons.constant.ErrorConstants ErrorConstants}).
     */
    public void setExCode(int exCode) {
        this.exCode = exCode;
    }

    /**
     * Function to set an error locus. Known values are:
     * <li>{@link com.datalogic.dlapos.commons.constant.ErrorConstants#APOS_EL_INPUT APOS_EL_INPUT}</li>
     * <li>{@link com.datalogic.dlapos.commons.constant.ErrorConstants#APOS_EL_INPUT_DATA APOS_EL_INPUT_DATA}</li>
     * <li>{@link com.datalogic.dlapos.commons.constant.ErrorConstants#APOS_EL_OUTPUT APOS_EL_OUTPUT}</li>
     *
     * @param locus the locus code.
     */
    public void setLocus(int locus) {
        this.locus = locus;
    }

    /**
     * Function to set a response code. Known values are:
     * <li>{@link com.datalogic.dlapos.commons.constant.ErrorConstants#APOS_ER_RETRY APOS_ER_RETRY}</li>
     * <li>{@link com.datalogic.dlapos.commons.constant.ErrorConstants#APOS_ER_CLEAR APOS_ER_CLEAR}</li>
     * <li>{@link com.datalogic.dlapos.commons.constant.ErrorConstants#APOS_ER_CONTINUEINPUT APOS_ER_CONTINUEINPUT}</li>
     *
     * @param response the response code.
     */
    public void setResponse(int response) {
        this.response = response;
    }

}
