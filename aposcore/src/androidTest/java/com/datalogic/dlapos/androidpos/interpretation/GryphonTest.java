package com.datalogic.dlapos.androidpos.interpretation;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import com.datalogic.dlapos.commons.constant.CommonsConstants;
import com.datalogic.dlapos.commons.support.APosException;
import com.datalogic.dlapos.commons.upos.RequestListener;
import com.datalogic.dlapos.control.Scanner;

import org.junit.Assert;
import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;

public class GryphonTest {

    Context context = ApplicationProvider.getApplicationContext();

    @Test
    public void openClose() throws APosException {
        Scanner scanner = new Scanner();
        scanner.open("DL-Gryphon-GD4500-USB-OEM", context);
        assertThat(scanner.getState()).isEqualTo(CommonsConstants.S_IDLE);
        assertThat(scanner.getCapCompareFirmwareVersion()).isTrue();
        assertThat(scanner.getDeviceServiceDescription()).isEqualTo("ScannerService");
        assertThat(scanner.getPhysicalDeviceDescription()).isEqualTo("DLS Gryphon GD4500 Scanner");
        assertThat(scanner.getClaimed()).isFalse();
        scanner.close();
        assertThat(scanner.getState()).isEqualTo(CommonsConstants.S_CLOSED);
        try {
            scanner.getCapCompareFirmwareVersion();
        } catch (APosException e) {
            return;
        }
        fail();
    }

    @Test
    public void openClaimReleaseClose() throws APosException, InterruptedException {
        Scanner scanner = new Scanner();
        scanner.open("DL-Gryphon-GD4500-USB-OEM", context);
        assertThat(scanner.getState()).isEqualTo(CommonsConstants.S_IDLE);
        scanner.claim(new RequestListener() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onFailure(String failureDescription) {
                fail();
            }
        });
        Thread.sleep(10000);
        scanner.setDeviceEnabled(true);
        scanner.setAutoDisable(false);
        Thread.sleep(10000);
        scanner.setDeviceEnabled(false);
        scanner.release();
        scanner.close();
        assertThat(scanner.getState()).isEqualTo(CommonsConstants.S_CLOSED);
    }

}