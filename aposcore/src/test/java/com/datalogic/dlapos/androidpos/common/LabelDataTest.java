package com.datalogic.dlapos.androidpos.common;

import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

public class LabelDataTest {

    private final LabelData _labelDataTest = new LabelData(new byte[]{1}, new byte[]{2}, 3);

    @Test
    public void setDecodedLabel() {
        byte[] result = _labelDataTest.getDecodedLabel();
        assertThat(result.length).isEqualTo(1);
        assertThat(result[0]).isEqualTo(2);

        _labelDataTest.setDecodedLabel(new byte[] {1,2,3});
        result = _labelDataTest.getDecodedLabel();

        assertThat(result.length).isEqualTo(3);
        assertThat(result[0]).isEqualTo(1);
        assertThat(result[1]).isEqualTo(2);
        assertThat(result[2]).isEqualTo(3);
    }

    @Test
    public void setLabelType() {
        assertThat(_labelDataTest.getLabelType()).isEqualTo(3);
        _labelDataTest.setLabelType(4);
        assertThat(_labelDataTest.getLabelType()).isEqualTo(4);
    }

    @Test
    public void setRawLabel() {
        byte[] result = _labelDataTest.getRawLabel();
        assertThat(result.length).isEqualTo(1);
        assertThat(result[0]).isEqualTo(1);

        _labelDataTest.setRawLabel(new byte[] {1,2,3});
        result = _labelDataTest.getRawLabel();

        assertThat(result.length).isEqualTo(3);
        assertThat(result[0]).isEqualTo(1);
        assertThat(result[1]).isEqualTo(2);
        assertThat(result[2]).isEqualTo(3);
    }
}