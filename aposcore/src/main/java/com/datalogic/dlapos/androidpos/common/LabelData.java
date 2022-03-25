package com.datalogic.dlapos.androidpos.common;

/**
 * Event data for labels.
 */
public class LabelData extends EventData {
    private byte[] rawLabel;
    private byte[] decodedLabel;
    private int labelType;

    /**
     * Default constructor.
     *
     * @param rawLabel     the raw label.
     * @param decodedLabel the decoded label.
     * @param labelType    the label type.
     */
    public LabelData(byte[] rawLabel, byte[] decodedLabel, int labelType) {
        super(EventData.LABEL_EVENT);
        this.rawLabel = rawLabel;
        this.decodedLabel = decodedLabel;
        this.labelType = labelType;
    }

    /**
     * Function to get the decoded label.
     *
     * @return the decoded label.
     */
    public byte[] getDecodedLabel() {
        return this.decodedLabel;
    }

    /**
     * Function to get the label type.
     *
     * @return the label type.
     */
    public int getLabelType() {
        return this.labelType;
    }

    /**
     * Function to get the raw label.
     *
     * @return the raw label.
     */
    public byte[] getRawLabel() {
        return this.rawLabel;
    }

    /**
     * Function to set the decoded label.
     *
     * @param decodedLabel the decoded label.
     */
    public void setDecodedLabel(byte[] decodedLabel) {
        this.decodedLabel = decodedLabel;
    }

    /**
     * Function to set the label type.
     *
     * @param labelType the label type.
     */
    public void setLabelType(int labelType) {
        this.labelType = labelType;
    }

    /**
     * Function to set the raw label.
     *
     * @param rawLabel the raw label.
     */
    public void setRawLabel(byte[] rawLabel) {
        this.rawLabel = rawLabel;
    }
}
