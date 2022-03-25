package com.datalogic.dlapos.androidpos.common;

import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;


public class BrandingTest {

    @Test
    public void getBrandedDevice() {
        assertThat(Branding.getInstance().getBrandedDevice("{BP}_TEST")).isEqualTo(Branding.DEF_PREFIX.toUpperCase() + "_TEST");
    }

    @Test
    public void getBrandedProduct() {
        assertThat(Branding.getInstance().getBrandedProduct()).isEqualTo(Branding.DEF_PRODUCT);
    }

    @Test
    public void getBrandedPropName() {
        assertThat(Branding.getInstance().getBrandedPropName("{BP}_TEST")).isEqualTo(Branding.DEF_PREFIX + "_TEST");
    }

    @Test
    public void getBrandingName() {
        assertThat(Branding.getInstance().getBrandingName()).isEqualTo(Branding.DEF_NAME);
    }

    @Test
    public void getBrandingPrefix() {
        assertThat(Branding.getInstance().getBrandingPrefix()).isEqualTo(Branding.DEF_PREFIX);
    }

    @Test
    public void getManufacturerName() {
        assertThat(Branding.getInstance().getManufacturerName()).isEqualTo(Branding.DEF_MFGNAME);
    }

    @Test
    public void getManufacturerShortName() {
        assertThat(Branding.getInstance().getManufacturerShortName()).isEqualTo(Branding.DEF_MFGSNAME);
    }
}