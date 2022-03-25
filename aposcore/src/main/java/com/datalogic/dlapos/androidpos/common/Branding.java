/*
 * Copyright 2016-2019 Datalogic USA, Inc.  All rights reserved.
 * Datalogic Confidential & Proprietary Information
 */


package com.datalogic.dlapos.androidpos.common;

/**
 * The Branding class is used to perform branding within DLSJavaPOS.  The class
 * is a singleton implementation that performs substitutions and provides
 * methods to obtain brand-specific strings for use within DLSJavaPOS.
 */
public class Branding {

    /**
     * The default Manufacturer name used.
     */
    public final static String DEF_MFGNAME = "Datalogic Scanning";
    /**
     * The default Manufacturer short name used.
     */
    public final static String DEF_MFGSNAME = "DLA";
    /**
     * The default branding name used.
     */
    public final static String DEF_NAME = "Datalogic ADC";
    /**
     * The default branding prefix used.
     */
    public final static String DEF_PREFIX = "dls";
    /**
     * The default product name used.
     */
    public final static String DEF_PRODUCT = "DLSAndroidPOS";
    /**
     * The prefix delimeter.  This is replaced with the prefix.
     */
    public final static String DELIM_PREFIX = "{BP}";
    /**
     * The property key for Manufacturer name.
     */
    public final static String KEY_MFGNAME = "ManufacturerName";
    /**
     * The property key for Manufacturer short name.
     */
    public final static String KEY_MFGSNAME = "ManufacturerShortName";
    /**
     * The property key for branding name.
     */
    public final static String KEY_NAME = "BrandingName";
    /**
     * The property key for branding prefix.
     */
    public final static String KEY_PREFIX = "BrandingPrefix";
    /**
     * The property key for branding product name.
     */
    public final static String KEY_PRODUCT = "BrandedProduct";

    private volatile static Branding sm_branding = null;

    private Branding() {
    }


    /**
     * Replace any occurrence of the Branding.DELIM_PREFIX with an uppercase
     * version of the prefix in the specified device name and return the
     * results.
     *
     * @param sSrc String containing a device name with the
     *             Branding.DELIM_PREFIX present.
     * @return String containing the branded device name.
     */
    public String getBrandedDevice(String sSrc) {
        String sRes = null;
        if (sSrc != null) {
            sRes = sSrc.replace(DELIM_PREFIX,
                    getBrandingPrefix().toUpperCase());
        }
        return sRes;
    }


    /**
     * Return the branded product name.
     *
     * @return String containing the branded product name.
     */
    public String getBrandedProduct() {
        return DEF_PRODUCT;
    }


    /**
     * Return a branded property name by replacing any occurrence of
     * Branding.DELIM_PREFIX with the loaded branding prefix.
     *
     * @param sSrc String containing a property name with the
     *             Branding.DELIM_PREFIX present.
     * @return String containing the branded property name.
     */
    public String getBrandedPropName(String sSrc) {
        String sRes = null;
        if (sSrc != null) {
            sRes = sSrc.replace(DELIM_PREFIX, getBrandingPrefix());
        }
        return sRes;
    }


    /**
     * Return the company name for branding.
     *
     * @return String containing the branded company name.
     */
    public String getBrandingName() {
        return DEF_NAME;
    }


    /**
     * Return the prefix for branding.
     *
     * @return String containing the branding prefix.
     */
    public String getBrandingPrefix() {
        return DEF_PREFIX;
    }


    /**
     * Return a singleton instance of a Branding object.  The Branding object
     * will automatically load on instantiation.
     *
     * @return com.dls.jpos.common.Branding the singleton instance of Branding.
     */
    public static Branding getInstance() {
        if (sm_branding == null) {
            sm_branding = new Branding();
        }
        return sm_branding;
    }


    /**
     * Return the Manufacturer Name for branding.
     *
     * @return String containing the Manufacturer name.
     */
    public String getManufacturerName() {
        return DEF_MFGNAME;
    }


    /**
     * Return the Manufacturer short name for branding.
     *
     * @return String containing the Manufacturer short name.
     */
    public String getManufacturerShortName() {
        return DEF_MFGSNAME;
    }

}
