package com.datalogic.dlapos.androidpos.common;

import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

public class SystemDataTest {

    private final SystemData _systemData = new SystemData(new byte[]{1}, 2);

    @Test
    public void setDataType() {
        assertThat(_systemData.getDataType()).isEqualTo(2);
        _systemData.setDataType(3);
        assertThat(_systemData.getDataType()).isEqualTo(3);
    }

    @Test
    public void setRawSystemData() {
        byte[] result = _systemData.getRawSystemData();
        assertThat(result.length).isEqualTo(1);
        assertThat(result[0]).isEqualTo(1);

        _systemData.setRawSystemData(new byte[]{2, 3});
        result = _systemData.getRawSystemData();
        assertThat(result.length).isEqualTo(2);
        assertThat(result[0]).isEqualTo(2);
        assertThat(result[1]).isEqualTo(3);
    }
}