package com.datalogic.dlapos.androidpos.common;

import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

public class ErrorDataTest {

    private final ErrorData _data = new ErrorData(1, 2, 3, 4);

    @Test
    public void setCode() {
        assertThat(_data.getCode()).isEqualTo(1);
        _data.setCode(2);
        assertThat(_data.getCode()).isEqualTo(2);
    }

    @Test
    public void setExCode() {
        assertThat(_data.getExCode()).isEqualTo(2);
        _data.setExCode(3);
        assertThat(_data.getExCode()).isEqualTo(3);
    }

    @Test
    public void setLocus() {
        assertThat(_data.getLocus()).isEqualTo(3);
        _data.setLocus(4);
        assertThat(_data.getLocus()).isEqualTo(4);
    }

    @Test
    public void setResponse() {
        assertThat(_data.getResponse()).isEqualTo(4);
        _data.setResponse(5);
        assertThat(_data.getResponse()).isEqualTo(5);
    }
}