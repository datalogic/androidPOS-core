package com.datalogic.dlapos.androidpos.common;

/**
 * Event data for system data.
 */
public class SystemData extends EventData {
    private byte[] rawSystemData;
    private int dataType;

    /**
     * Default constructor.
     *
     * @param rawSystemData the raw system data.
     * @param dataType      the data type.
     */
    public SystemData(byte[] rawSystemData, int dataType) {
        super(EventData.SYSTEM_DATA_EVENT);
        this.rawSystemData = rawSystemData;
        this.dataType = dataType;
    }

    /**
     * Function to get the data type.
     *
     * @return the data type.
     */
    public int getDataType() {
        return this.dataType;
    }

    /**
     * Function to get the raw system data.
     *
     * @return the raw system data.
     */
    public byte[] getRawSystemData() {
        return this.rawSystemData;
    }

    /**
     * Function to set the data type.
     *
     * @param dataType the data type.
     */
    public void setDataType(int dataType) {
        this.dataType = dataType;
    }

    /**
     * Function to set the raw system data.
     *
     * @param rawSystemData the raw system data.
     */
    public void setRawSystemData(byte[] rawSystemData) {
        this.rawSystemData = rawSystemData;
    }
}
