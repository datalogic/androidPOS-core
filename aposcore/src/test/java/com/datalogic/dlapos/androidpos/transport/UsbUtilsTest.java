package com.datalogic.dlapos.androidpos.transport;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Test;

public class UsbUtilsTest {

    @Test
    public void extractUsage() {
        assertThat(UsbUtils.extractUsage("Flash Update (Usage = A000h, Usage Page = FF45h")).isEqualTo("A000");
        assertThat(UsbUtils.extractUsage("Hand-held Scanner (Usage = 4B00h, Usage Page = FF45h")).isEqualTo("4B00");
    }
}