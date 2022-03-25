package com.datalogic.dlapos.androidpos.common;

import android.util.Log;

/**
 * Class representing a scanner configuration.
 */
public class DLSScannerConfig extends DLSCConfig {
    private final static String TAG = DLSScannerConfig.class.getSimpleName();

    /**
     * Key to access the beep duration configuration.
     */
    public final static String KEY_BEEPDURATION = "beepDuration";
    /**
     * Key to access the beep frequency configuration.
     */
    public final static String KEY_BEEPFREQUENCY = "beepFrequency";
    /**
     * Key to access the beep volume configuration.
     */
    public final static String KEY_BEEPVOLUME = "beepVolume";
    /**
     * Key to access the bluetooth delay statistics configuration.
     */
    public final static String KEY_BTDELAYSTATS = "bluetoothDelayStatistics";
    /**
     * Key to access the bluetooth scanner always enabled configuration.
     */
    public final static String KEY_BTSCANNERALWAYSENABLED = "bluetoothScannerAlwaysEnabled";
    /**
     * Key to access the accept configuration items configuration.
     */
    public final static String KEY_CANACCEPTCONFIGITEMS = "canAcceptConfigItems";
    /**
     * Key to access the accept statistics commands configuration.
     */
    public final static String KEY_CANACCEPTSTATISTICSCMD = "canAcceptStatisticsCmd";
    /**
     * Key to access the compare firmware version configuration.
     */
    public final static String KEY_CANCOMPAREFIRMWAREVERSION = "canCompareFirmwareVersion";
    /**
     * Key to access the notify power change configuration.
     */
    public final static String KEY_CANNOTIFYPOWERCHANGE = "canNotifyPowerChange";
    /**
     * Key to access the program configuration on claim configuration.
     */
    public final static String KEY_CANPROGRAMCONFIGONCLAIM = "canProgramConfigOnClaim";
    /**
     * Key to access the update firmware configuration.
     */
    public final static String KEY_CANUPDATEFIRMWARE = "canUpdateFirmware";
    /**
     * Key to access the delete image file after read configuration.
     */
    public final static String KEY_DELETEIMAGEFILEAFTERREAD = "deleteImageFileAfterRead";
    /**
     * Key to access the double read timeout configuration.
     */
    public final static String KEY_DOUBLEREADTIMEOUT = "doubleReadTimeout";
    /**
     * Key to access the enable 2 digit sups configuration.
     */
    public final static String KEY_ENABLE2DIGITSUPS = "enable2DigitSups";
    /**
     * Key to access the enable 4 digits price check digit configuration.
     */
    public final static String KEY_ENABLE4DIGITPRICECHECKDIGIT = "enable4DigitPriceCheckDigit";
    /**
     * Key to access the enable 5 digits price check digit configuration.
     */
    public final static String KEY_ENABLE5DIGITPRICECHECKDIGIT = "enable5DigitPriceCheckDigit";
    /**
     * Key to access the enable 5 digit sups configuration.
     */
    public final static String KEY_ENABLE5DIGITSUPS = "enable5DigitSups";
    /**
     * Key to access the enable barcode programming configuration.
     */
    public final static String KEY_ENABLEBARCODEPROGRAMMING = "enableBarCodeProgramming";
    /**
     * Key to access the enable codabar configuration.
     */
    public final static String KEY_ENABLECODABAR = "enableCodabar";
    /**
     * Key to access the enable code128 configuration.
     */
    public final static String KEY_ENABLECODE128 = "enableCode128";
    /**
     * Key to access the enable code128 sups configuration.
     */
    public final static String KEY_ENABLECODE128SUPS = "enableCode128Sups";
    /**
     * Key to access the enable code39 configuration.
     */
    public final static String KEY_ENABLECODE39 = "enableCode39";
    /**
     * Key to access the enable code39 check digit configuration.
     */
    public final static String KEY_ENABLECODE39CHECKDIGIT = "enableCode39CheckDigit";
    /**
     * Key to access the enable code 93 configuration.
     */
    public final static String KEY_ENABLECODE93 = "enableCode93";
    /**
     * Key to access the enable eanjan2 label decode configuration.
     */
    public final static String KEY_ENABLEEANJAN2LABELDECODE = "enableEANJAN2LabelDecode";
    /**
     * Key to access the enable good read beep configuration.
     */
    public final static String KEY_ENABLEGOODREADBEEP = "enableGoodReadBeep";
    /**
     * Key to access the interleaved configuration.
     */
    public final static String KEY_ENABLEINTERLEAVED = "enableInterleaved";
    /**
     * Key to access the enable ITF check digit configuration.
     */
    public final static String KEY_ENABLEITFCHECKDIGIT = "enableITFCheckDigit";
    /**
     * Key to access the enable laser on off switch configuration.
     */
    public final static String KEY_ENABLELASERONOFFSWITCH = "enableLaserOnOffSwitch";
    /**
     * Key to access the enable UCCEAN128 configuration.
     */
    public final static String KEY_ENABLEUCCEAN128 = "enableUCCEAN128";
    /**
     * Key to access the enable UPCA check digit configuration.
     */
    public final static String KEY_ENABLEUPCACHECKDIGIT = "enableUPCACheckDigit";
    /**
     * Key to access the enable UPCA to EAN13 expansion configuration.
     */
    public final static String KEY_ENABLEUPCATOEAN13EXPANSION = "enableUPCAtoEAN13Expansion";
    /**
     * Key to access the enable UPCD1D5 configuration.
     */
    public final static String KEY_ENABLEUPCD1D5 = "enableUPCD1D5";
    /**
     * Key to access the enable UPCEAN configuration.
     */
    public final static String KEY_ENABLEUPCEAN = "enableUPCEAN";
    /**
     * Key to access the enable UPCE check digit configuration.
     */
    public final static String KEY_ENABLEUPCECHECKDIGIT = "enableUPCECheckDigit";
    /**
     * Key to access the enable UPCE to EAN13 expansion configuration.
     */
    public final static String KEY_ENABLEUPCETOEAN13EXPANSION = "enableUPCEtoEAN13Expansion";
    /**
     * Key to access the enable UPCE to UPCA expansion configuration.
     */
    public final static String KEY_ENABLEUPCETOUPCAEXPANSION = "enableUPCEtoUPCAExpansion";
    /**
     * Key to access the enable volume switch configuration.
     */
    public final static String KEY_ENABLEVOLUMESWITCH = "enableVolumeSwitch";
    /**
     * Key to access the extended host timeout configuration.
     */
    public final static String KEY_EXTHOSTTIMEOUT = "extendedHostTimeout";
    /**
     * Key to access the ITF length1 configuration.
     */
    public final static String KEY_ITFLENGTH1 = "itfLength1";
    /**
     * Key to access the ITF length2 configuration.
     */
    public final static String KEY_ITFLENGTH2 = "itfLength2";
    /**
     * Key to access the ITF range configuration.
     */
    public final static String KEY_ITFRANGE = "itfRange";
    /**
     * Key to access the laser timeout configuration.
     */
    public final static String KEY_LASERTIMEOUT = "laserTimeout";
    /**
     * Key to access the led good read duration configuration.
     */
    public final static String KEY_LEDGOODREADDURATION = "ledGoodReadDuration";
    /**
     * Key to access the logical flash name configuration.
     */
    public final static String KEY_LOGICALFLASHNAME = "logicalFlashName";
    /**
     * Key to access the MBeans enabled configuration.
     */
    public final static String KEY_MBEANSENABLED = "MBeansEnabled";
    /**
     * Key to access the motor timeout configuration.
     */
    public final static String KEY_MOTORTIMEOUT = "motorTimeout";
    /**
     * Key to access the send IHS on claim configuration.
     */
    public final static String KEY_SENDIHSONCLAIM = "sendIHSOnClaim";
    /**
     * Key to access the store label security level configuration.
     */
    public final static String KEY_STORELABELSECURITYLEVEL = "storeLabelSecurityLevel";
    /**
     * Key to access the two ITFS configuration.
     */
    public final static String KEY_TWOITFS = "twoItfs";
    /**
     * Key to access the WMI enabled configuration.
     */
    public final static String KEY_WMIENABLED = "WMIEnabled";
    /**
     * Key to access the host command disable configuration.
     */
    public final static String KEY_HOSTCOMMANDSDISABLED = "hostCommandsDisabled";

    private static final boolean DEF_ENABLEUPCEAN = true;
    private static final boolean DEF_ENABLEUPCD1D5 = true;
    private static final boolean DEF_ENABLECODE39 = true;
    private static final boolean DEF_ENABLEINTERLEAVED = true;
    private static final boolean DEF_ENABLECODABAR = true;
    private static final boolean DEF_ENABLECODE93 = true;
    private static final boolean DEF_ENABLECODE128 = true;
    private static final boolean DEF_ENABLEUCCEAN128 = true;
    private static final boolean DEF_ENABLE2DIGITSUPS = false;
    private static final boolean DEF_ENABLE5DIGITSUPS = false;
    private static final boolean DEF_ENABLECODE128SUPS = false;
    private static final boolean DEF_ENABLEUPCACHECKDIGIT = false;
    private static final boolean DEF_ENABLEUPCECHECKDIGIT = false;
    private static final boolean DEF_ENABLECODE39CHECKDIGIT = false;
    private static final boolean DEF_ENABLEITFCHECKDIGIT = false;
    private static final boolean DEF_ENABLEEANJAN2LABELDECODE = false;
    private static final boolean DEF_ENABLEUPCATOEAN13EXPANSION = false;
    private static final boolean DEF_ENABLEUPCETOEAN13EXPANSION = false;
    private static final boolean DEF_ENABLEUPCETOUPCAEXPANSION = false;
    private static final boolean DEF_ENABLE4DIGITPRICECHECKDIGIT = false;
    private static final boolean DEF_ENABLE5DIGITPRICECHECKDIGIT = false;
    private static final boolean DEF_ENABLEGOODREADBEEP = true;
    private static final boolean DEF_ENABLEBARCODEPROGRAMMING = true;
    private static final boolean DEF_ENABLELASERONOFFSWITCH = true;
    private static final boolean DEF_ENABLEVOLUMESWITCH = true;
    private static final boolean DEF_CANACCEPTSTATISTICSCOMMAND = false;
    private static final boolean DEF_CANACCEPTCONFIGITEMS = false;
    private static final boolean DEF_CANPROGRAMCONFIGONCLAIM = false;
    private static final boolean DEF_CANNOTIFYPOWERCHANGE = false;
    private static final boolean DEF_CANCOMPAREFIRMWAREVERSION = false;
    private static final boolean DEF_CANUPDATEFIRMWARE = false;
    private static final boolean DEF_WMIENABLED = false;
    private static final boolean DEF_MBEANSENABLED = false;
    private static final int DEF_BEEPVOLUME = 2;
    private static final int DEF_BEEPFREQUENCY = 2;
    private static final int DEF_BEEPDURATION = 1;
    private static final int DEF_MOTORTIMEOUT = 1;
    private static final int DEF_LASERTIMEOUT = 1;
    private static final int DEF_DOUBLEREADTIMEOUT = 2;
    private static final int DEF_STORELABELSECURITYLEVEL = 0;
    private static final int DEF_ITFLENGTH1 = 0;
    private static final int DEF_ITFLENGTH2 = 0;
    private static final boolean DEF_TWOITFS = false;
    private static final boolean DEF_ITFRANGE = false;
    private static final int DEF_LEDGOODREADDURATION = 1;
    private static final boolean DEF_DELETEIMAGEFILEAFTERREAD = true;
    private static final boolean DEF_SENDIHSONCLAIM = true;
    private static final int DEF_EXTENDEDHOSTTIMEOUT = 0;
    private static final boolean DEF_HOSTCOMMANDSDISABLED = false;
    private static final boolean DEF_BLUETOOTHDELAYSTATISTICS = false;
    private static final boolean DEF_BLUETOOTHSCANNERALWAYSENABLED = false;


    /**
     * Default constructor.
     */
    public DLSScannerConfig() {
        super();
        initializeMap();
    }

    private void initializeMap() {
        Branding branding = Branding.getInstance();
        setEnableUPCEAN(DEF_ENABLEUPCEAN);
        setEnableUPCD1D5(DEF_ENABLEUPCD1D5);
        setEnableCode39(DEF_ENABLECODE39);
        setEnableInterleaved(DEF_ENABLEINTERLEAVED);
        setEnableCodabar(DEF_ENABLECODABAR);
        setEnableCode93(DEF_ENABLECODE93);
        setEnableCode128(DEF_ENABLECODE128);
        setEnableUCCEAN128(DEF_ENABLEUCCEAN128);
        setEnable2DigitSups(DEF_ENABLE2DIGITSUPS);
        setEnable5DigitSups(DEF_ENABLE5DIGITSUPS);
        setEnableCode128Sups(DEF_ENABLECODE128SUPS);
        setEnableUPCACheckDigit(DEF_ENABLEUPCACHECKDIGIT);
        setEnableUPCECheckDigit(DEF_ENABLEUPCECHECKDIGIT);
        setEnableCode39CheckDigit(DEF_ENABLECODE39CHECKDIGIT);
        setEnableITFCheckDigit(DEF_ENABLEITFCHECKDIGIT);
        setEnableEANJAN2LabelDecode(DEF_ENABLEEANJAN2LABELDECODE);
        setEnableUPCAtoEAN13Expansion(DEF_ENABLEUPCATOEAN13EXPANSION);
        setEnableUPCEtoEAN13Expansion(DEF_ENABLEUPCETOEAN13EXPANSION);
        setEnableUPCEtoUPCAExpansion(DEF_ENABLEUPCETOUPCAEXPANSION);
        setEnable4DigitPriceCheckDigit(DEF_ENABLE4DIGITPRICECHECKDIGIT);
        setEnable5DigitPriceCheckDigit(DEF_ENABLE5DIGITPRICECHECKDIGIT);
        setEnableGoodReadBeep(DEF_ENABLEGOODREADBEEP);
        setEnableBarCodeProgramming(DEF_ENABLEBARCODEPROGRAMMING);
        setEnableLaserOnOffSwitch(DEF_ENABLELASERONOFFSWITCH);
        setEnableVolumeSwitch(DEF_ENABLEVOLUMESWITCH);
        setCanAcceptStatisticsCommand(DEF_CANACCEPTSTATISTICSCOMMAND);
        setCanAcceptConfigItems(DEF_CANACCEPTCONFIGITEMS);
        setCanProgramConfigOnClaim(DEF_CANPROGRAMCONFIGONCLAIM);
        setCanNotifyPowerChange(DEF_CANNOTIFYPOWERCHANGE);
        setCanCompareFirmwareVersion(DEF_CANCOMPAREFIRMWAREVERSION);
        setCanUpdateFirmware(DEF_CANUPDATEFIRMWARE);
        setWMIEnabled(DEF_WMIENABLED);
        setMBeansEnabled(DEF_MBEANSENABLED);
        setLogicalFlashName(branding.getBrandedDevice(Branding.DELIM_PREFIX + "-USB-Flash"));
        setBeepVolume(DEF_BEEPVOLUME);
        setBeepFrequency(DEF_BEEPFREQUENCY);
        setBeepDuration(DEF_BEEPDURATION);
        setMotorTimeout(DEF_MOTORTIMEOUT);
        setLaserTimeout(DEF_LASERTIMEOUT);
        setDoubleReadTimeout(DEF_DOUBLEREADTIMEOUT);
        setStoreLabelSecurityLevel(DEF_STORELABELSECURITYLEVEL);
        setITFLength1(DEF_ITFLENGTH1);
        setITFLength2(DEF_ITFLENGTH2);
        setTwoITFs(DEF_TWOITFS);
        setITFRange(DEF_ITFRANGE);
        setLEDGoodReadDuration(DEF_LEDGOODREADDURATION);
        setDeleteImageFileAfterRead(DEF_DELETEIMAGEFILEAFTERREAD);
        setSendIHSOnClaim(DEF_SENDIHSONCLAIM);
        setExtendedHostTimeout(DEF_EXTENDEDHOSTTIMEOUT);
        setHostCommandsDisabled(DEF_HOSTCOMMANDSDISABLED);
        setBluetoothDelayStatistics(DEF_BLUETOOTHDELAYSTATISTICS);
        setBluetoothScannerAlwaysEnabled(DEF_BLUETOOTHSCANNERALWAYSENABLED);
    }

    /**
     * Function to get the beep duration (default value {@value #DEF_BEEPDURATION}).
     *
     * @return the beep duration.
     */
    public int getBeepDuration() {
        return getOptionAsInt(KEY_BEEPDURATION);
    }

    /**
     * Function to get beep frequency (default value {@value #DEF_BEEPFREQUENCY}).
     *
     * @return the beep frequency.
     */
    public int getBeepFrequency() {
        return getOptionAsInt(KEY_BEEPFREQUENCY);
    }

    /**
     * Function to get beep volume (default value {@value #DEF_BEEPVOLUME}).
     *
     * @return the beep volume.
     */
    public int getBeepVolume() {
        return getOptionAsInt(KEY_BEEPVOLUME);
    }

    /**
     * Function to check if the bluetooth delay statistics are enabled (default value {@value #DEF_BLUETOOTHDELAYSTATISTICS}).
     *
     * @return true if the bluetooth delay statistics are enabled, false otherwise.
     */
    public boolean getBluetoothDelayStatistics() {
        return getOptionAsBool(KEY_BTDELAYSTATS);
    }

    /**
     * Function to check if bluetooth scanner is always enabled (default value {@value #DEF_BLUETOOTHSCANNERALWAYSENABLED}).
     *
     * @return true if bluetooth scanner is always enabled, false otherwise.
     */
    public boolean getBluetoothScannerAlwaysEnabled() {
        return getOptionAsBool(KEY_BTSCANNERALWAYSENABLED);
    }

    /**
     * Function to check if the scanner can accept configuration items (default value {@value #DEF_CANACCEPTCONFIGITEMS}).
     *
     * @return true if the scanner can accept configuration items, false otherwise.
     */
    public boolean getCanAcceptConfigItems() {
        return getOptionAsBool(KEY_CANACCEPTCONFIGITEMS);
    }

    /**
     * Function to check if the scanner can accept statistics commands (default value {@value #DEF_CANACCEPTSTATISTICSCOMMAND}).
     *
     * @return true if the scanner can accept statistics commands, false otherwise.
     */
    public boolean getCanAcceptStatisticsCommand() {
        return getOptionAsBool(KEY_CANACCEPTSTATISTICSCMD);
    }

    /**
     * Function to check if the scanner can compare firmware version (default value {@value #DEF_CANCOMPAREFIRMWAREVERSION}).
     *
     * @return true if the scanner can compare firmware version, false otherwise.
     */
    public boolean getCanCompareFirmwareVersion() {
        return getOptionAsBool(KEY_CANCOMPAREFIRMWAREVERSION);
    }

    /**
     * Function to check if the scanner can notify power change (default value {@value #DEF_CANNOTIFYPOWERCHANGE}).
     *
     * @return true if the scanner can notify power change, false otherwise.
     */
    public boolean getCanNotifyPowerChange() {
        return getOptionAsBool(KEY_CANNOTIFYPOWERCHANGE);
    }

    /**
     * Function to check if the scanner can program configuration on claim (default value {@value #DEF_CANPROGRAMCONFIGONCLAIM}).
     *
     * @return true  if the scanner can program configuration on claim, false otherwise.
     */
    public boolean getCanProgramConfigOnClaim() {
        return getOptionAsBool(KEY_CANPROGRAMCONFIGONCLAIM);
    }

    /**
     * Function to check if the scanner can update firmware (default value {@value #DEF_CANCOMPAREFIRMWAREVERSION}).
     *
     * @return true if the scanner can update firmware, false otherwise.
     */
    public boolean getCanUpdateFirmware() {
        return getOptionAsBool(KEY_CANUPDATEFIRMWARE);
    }

    /**
     * Function to check if the scanner deletes image file after read (default value {@value #DEF_DELETEIMAGEFILEAFTERREAD}).
     *
     * @return true if the scanner deletes image file after read, false otherwise.
     */
    public boolean getDeleteImageFileAfterRead() {
        return getOptionAsBool(KEY_DELETEIMAGEFILEAFTERREAD);
    }

    /**
     * Function to get the double read timeout (default value {@value #DEF_DOUBLEREADTIMEOUT}).
     *
     * @return the double read timeout.
     */
    public int getDoubleReadTimeout() {
        return getOptionAsInt(KEY_DOUBLEREADTIMEOUT);
    }

    /**
     * Function to check if 2 digit sups is enabled (default value {@value #DEF_ENABLE2DIGITSUPS}).
     *
     * @return true if 2 digit sups is enabled, false otherwise.
     */
    public boolean getEnable2DigitSups() {
        return getOptionAsBool(KEY_ENABLE2DIGITSUPS);
    }

    /**
     * Function to check if 4 digit price check digit is enabled (default value {@value #DEF_ENABLE4DIGITPRICECHECKDIGIT}).
     *
     * @return true if 4 digit price check digit is enabled, false otherwise.
     */
    public boolean getEnable4DigitPriceCheckDigit() {
        return getOptionAsBool(KEY_ENABLE4DIGITPRICECHECKDIGIT);
    }

    /**
     * Function to check if 5 digit price check digit is enabled (default value {@value #DEF_ENABLE5DIGITPRICECHECKDIGIT}).
     *
     * @return true if 5 digit price check digit is enabled, false otherwise.
     */
    public boolean getEnable5DigitPriceCheckDigit() {
        return getOptionAsBool(KEY_ENABLE5DIGITPRICECHECKDIGIT);
    }

    /**
     * Function to check if 5 digit sups is enabled (default value {@value #DEF_ENABLE5DIGITSUPS}).
     *
     * @return true if 5 digit sups is enabled, false otherwise.
     */
    public boolean getEnable5DigitSups() {
        return getOptionAsBool(KEY_ENABLE5DIGITSUPS);
    }

    /**
     * Function to check if the barcode programming is enabled (default value {@value #DEF_ENABLEBARCODEPROGRAMMING}).
     *
     * @return true if the barcode programming is enabled, false otherwise.
     */
    public boolean getEnableBarCodeProgramming() {
        return getOptionAsBool(KEY_ENABLEBARCODEPROGRAMMING);
    }

    /**
     * Function to check if codabar symbology is enabled (default value {@value #DEF_ENABLECODABAR}).
     *
     * @return true if codabar symbology is enabled, false otherwise.
     */
    public boolean getEnableCodabar() {
        return getOptionAsBool(KEY_ENABLECODABAR);
    }

    /**
     * Function to check if code128 symbology is enabled (default value {@value #DEF_ENABLECODE128}).
     *
     * @return true if code128 symbology is enabled, false otherwise.
     */
    public boolean getEnableCode128() {
        return getOptionAsBool(KEY_ENABLECODE128);
    }

    /**
     * Function to check if code128 sups is enabled (default value {@value #DEF_ENABLECODE128SUPS}).
     *
     * @return true if code128 sups is enabled, false otherwise.
     */
    public boolean getEnableCode128Sups() {
        return getOptionAsBool(KEY_ENABLECODE128SUPS);
    }

    /**
     * Function to check if code39 symbology is enabled (default value {@value #DEF_ENABLECODE39}).
     *
     * @return true if code39 symbology is enabled, false otherwise.
     */
    public boolean getEnableCode39() {
        return getOptionAsBool(KEY_ENABLECODE39);
    }

    /**
     * Function to check if code39 check digit is enabled (default value {@value #DEF_ENABLECODE39CHECKDIGIT}).
     *
     * @return true if code39 check digit is enabled, false otherwise.
     */
    public boolean getEnableCode39CheckDigit() {
        return getOptionAsBool(KEY_ENABLECODE39CHECKDIGIT);
    }

    /**
     * Function to check if EANJAN2 label decode is enabled (default value {@value #DEF_ENABLEEANJAN2LABELDECODE}).
     *
     * @return true if EANJAN2 label decode is enabled, false otherwise.
     */
    public boolean getEnableEANJAN2LabelDecode() {
        return getOptionAsBool(KEY_ENABLEEANJAN2LABELDECODE);
    }

    /**
     * Function to check if code93 symbology is enabled (default value {@value #DEF_ENABLECODE93}).
     *
     * @return true if code93 symbology is enabled, false otherwise.
     */
    public boolean getEnableCode93() {
        return getOptionAsBool(KEY_ENABLECODE93);
    }

    /**
     * Function to check if good read beep is enabled (default value {@value #DEF_ENABLEGOODREADBEEP}).
     *
     * @return true if good read beep is enabled, false otherwise.
     */
    public boolean getEnableGoodReadBeep() {
        return getOptionAsBool(KEY_ENABLEGOODREADBEEP);
    }

    /**
     * Function to check if interleaved is enabled (default value {@value #DEF_ENABLEINTERLEAVED}).
     *
     * @return true if interleaved is enabled, false otherwise.
     */
    public boolean getEnableInterleaved() {
        return getOptionAsBool(KEY_ENABLEINTERLEAVED);
    }

    /**
     * Function to check if ITF check digit is enabled (default value {@value #DEF_ENABLEITFCHECKDIGIT}).
     *
     * @return true if ITF check digit is enabled, false otherwise.
     */
    public boolean getEnableITFCheckDigit() {
        return getOptionAsBool(KEY_ENABLEITFCHECKDIGIT);
    }

    /**
     * Function to check if the laser on off switch is enabled (default value {@value #DEF_ENABLELASERONOFFSWITCH}).
     *
     * @return true if the laser on off switch is enabled, false otherwise.
     */
    public boolean getEnableLaserOnOffSwitch() {
        return getOptionAsBool(KEY_ENABLELASERONOFFSWITCH);
    }

    /**
     * Function to check if the UCCEAN128 symbology is enabled (default value {@value #DEF_ENABLEUCCEAN128}).
     *
     * @return true if the UCCEAN128 symbology is enabled, false otherwise.
     */
    public boolean getEnableUCCEAN128() {
        return getOptionAsBool(KEY_ENABLEUCCEAN128);
    }

    /**
     * Function to check if UPCA check digit is enabled (default value {@value #DEF_ENABLEUPCACHECKDIGIT}).
     *
     * @return true if UPCA check digit is enabled, false otherwise.
     */
    public boolean getEnableUPCACheckDigit() {
        return getOptionAsBool(KEY_ENABLEUPCACHECKDIGIT);
    }

    /**
     * Function to check if the UPC to EAN13 expansion is enabled (default value {@value #DEF_ENABLEUPCATOEAN13EXPANSION}).
     *
     * @return true if the UPC to EAN13 expansion is enabled, false otherwise.
     */
    public boolean getEnableUPCAtoEAN13Expansion() {
        return getOptionAsBool(KEY_ENABLEUPCATOEAN13EXPANSION);
    }

    /**
     * Function to check if UPCD1D5 symbology is enabled (default value {@value #DEF_ENABLEUPCD1D5}).
     *
     * @return true if UPCD1D5 symbology is enabled, false otherwise.
     */
    public boolean getEnableUPCD1D5() {
        return getOptionAsBool(KEY_ENABLEUPCD1D5);
    }

    /**
     * Function to check if the UPCEAN symbology is enabled (default value {@value #DEF_ENABLEUPCEAN}).
     *
     * @return true if the UPCEAN symbology is enabled, false otherwise.
     */
    public boolean getEnableUPCEAN() {
        return getOptionAsBool(KEY_ENABLEUPCEAN);
    }

    /**
     * Function to check if the UPCE check digit is enabled (default value {@value #DEF_ENABLEUPCECHECKDIGIT}).
     *
     * @return true if the UPCE check digit is enabled, false otherwise.
     */
    public boolean getEnableUPCECheckDigit() {
        return getOptionAsBool(KEY_ENABLEUPCECHECKDIGIT);
    }

    /**
     * Function check if the UPCE to EAN13 expansion is enabled (default value {@value #DEF_ENABLEUPCETOEAN13EXPANSION}).
     *
     * @return true if the UPCE to EAN13 expansion is enabled, false otherwise.
     */
    public boolean getEnableUPCEtoEAN13Expansion() {
        return getOptionAsBool(KEY_ENABLEUPCETOEAN13EXPANSION);
    }

    /**
     * Function to check if the UPCE to UPCA expansion is enabled (default value is {@value #DEF_ENABLEUPCETOUPCAEXPANSION}).
     *
     * @return true if the UPCE to UPCA expansion is enabled, false otherwise.
     */
    public boolean getEnableUPCEtoUPCAExpansion() {
        return getOptionAsBool(KEY_ENABLEUPCETOUPCAEXPANSION);
    }

    /**
     * Function to check if the volume switch is enabled (default value is {@value #DEF_ENABLEVOLUMESWITCH}).
     *
     * @return true if the volume switch is enabled, false otherwise.
     */
    public boolean getEnableVolumeSwitch() {
        return getOptionAsBool(KEY_ENABLEVOLUMESWITCH);
    }

    /**
     * Function to get the extended host timeout (default value is {@value #DEF_EXTENDEDHOSTTIMEOUT}).
     *
     * @return the extended host timeout.
     */
    public int getExtendedHostTimeout() {
        return getOptionAsInt(KEY_EXTHOSTTIMEOUT);
    }

    /**
     * Function to check if host commands are disabled (default value {@value #DEF_HOSTCOMMANDSDISABLED}).
     *
     * @return true if host commands are disabled, false otherwise.
     */
    public boolean getHostCommandsDisabled() {
        return getOptionAsBool(KEY_HOSTCOMMANDSDISABLED);
    }

    /**
     * Function to get ITF length 1 (default value {@value #DEF_ITFLENGTH1}).
     *
     * @return the ITF length1.
     */
    public int getITFLength1() {
        return getOptionAsInt(KEY_ITFLENGTH1);
    }

    /**
     * Function to get ITF length 2 (default value {@value #DEF_ITFLENGTH2}).
     *
     * @return the ITF length2.
     */
    public int getITFLength2() {
        return getOptionAsInt(KEY_ITFLENGTH2);
    }

    /**
     * Function to check if the ITF range is enabled (default value {@value #DEF_ITFRANGE}).
     *
     * @return true if the ITF range is enabled, false otherwise.
     */
    public boolean getITFRange() {
        return getOptionAsBool(KEY_ITFRANGE);
    }

    /**
     * Function to get the laser timeout (default value {@value #DEF_LASERTIMEOUT}).
     *
     * @return the laser timeout.
     */
    public int getLaserTimeout() {
        return getOptionAsInt(KEY_LASERTIMEOUT);
    }

    /**
     * Function to get the good read LED's duration (default value {@value #DEF_LEDGOODREADDURATION}).
     *
     * @return the good read LED's duration.
     */
    public int getLEDGoodReadDuration() {
        return getOptionAsInt(KEY_LEDGOODREADDURATION);
    }

    /**
     * Function to get the logical flash name.
     *
     * @return the logical flash name.
     */
    public String getLogicalFlashName() {
        return getOption(KEY_LOGICALFLASHNAME);
    }

    /**
     * Function to check if MBeans is enabled (default value {@value #DEF_MBEANSENABLED}).
     *
     * @return true if MBeans is enabled, false otherwise.
     */
    public boolean getMBeansEnabled() {
        return getOptionAsBool(KEY_MBEANSENABLED);
    }

    /**
     * Function to get the motor timeout (default value {@value #DEF_MOTORTIMEOUT}).
     *
     * @return the motor timeout.
     */
    public int getMotorTimeout() {
        return getOptionAsInt(KEY_MOTORTIMEOUT);
    }

    /**
     * Function to check if the scanner sends IHS on claim (default value {@value #DEF_SENDIHSONCLAIM}).
     *
     * @return true if the scanner sends IHS on claim, false otherwise.
     */
    public boolean getSendIHSOnClaim() {
        return getOptionAsBool(KEY_SENDIHSONCLAIM);
    }

    /**
     * Function to get the store label security level (default value {@value #DEF_STORELABELSECURITYLEVEL}).
     *
     * @return the store label security level.
     */
    public int getStoreLabelSecurityLevel() {
        return getOptionAsInt(KEY_STORELABELSECURITYLEVEL);
    }

    /**
     * Function to check if two ITFs is enabled (default value {@value #DEF_TWOITFS}).
     *
     * @return true if two ITFs is enabled, false otherwise.
     */
    public boolean getTwoITFs() {
        return getOptionAsBool(KEY_TWOITFS);
    }

    /**
     * Function to check if WMI is enabled (default value {@value #DEF_WMIENABLED}).
     *
     * @return true if WMI is enabled, false otherwise.
     */
    public boolean getWMIEnabled() {
        return getOptionAsBool(KEY_WMIENABLED);
    }

    /**
     * Function to set the beep duration. If the value is outside the range 0-3, 0 is used.
     *
     * @param value the desired beep duration, in the range 0-3.
     */
    public void setBeepDuration(int value) {
        if (value < 0 || value > 3) {
            Log.w(TAG, "setBeepDuration: value outside of range 0-3. Setting to 0.");
            setOption(KEY_BEEPDURATION, 0);
        } else {
            setOption(KEY_BEEPDURATION, value);
        }
    }

    /**
     * Function to set the beep frequency. If the value is outside the range 0-3, 0 is used.
     *
     * @param value the desired beep frequency, in the range 0-3.
     */
    public void setBeepFrequency(int value) {
        if (value < 0 || value > 3) {
            Log.w(TAG, "setBeepFrequency: value outside of range 0-3. Setting to 0.");
            setOption(KEY_BEEPFREQUENCY, 0);
        } else {
            setOption(KEY_BEEPFREQUENCY, value);
        }
    }

    /**
     * Function to set the beep volume. If the value is outside the range 0-3, 0 is used.
     *
     * @param value the desired beep volume, in the range 0-3.
     */
    public void setBeepVolume(int value) {
        if (value < 0 || value > 3) {
            Log.w(TAG, "setBeepVolume: value outside of range 0-3. Setting to 0.");
            setOption(KEY_BEEPVOLUME, 0);
        } else {
            setOption(KEY_BEEPVOLUME, value);
        }
    }

    /**
     * Function to configure the scanner to support the bluetooth delay statistics functionality.
     *
     * @param value true to configure the scanner to support the bluetooth delay statistics functionality, false otherwise.
     */
    public void setBluetoothDelayStatistics(boolean value) {
        setOption(KEY_BTDELAYSTATS, value);
    }

    /**
     * Function to configure the bluetooth scanner always enabled.
     *
     * @param value true to configure the bluetooth scanner always enabled, false otherwise.
     */
    public void setBluetoothScannerAlwaysEnabled(boolean value) {
        setOption(KEY_BTSCANNERALWAYSENABLED, value);
    }

    /**
     * Function to configure the scanner to accept config items.
     *
     * @param value true to configure the scanner to accept config items, false otherwise.
     */
    public void setCanAcceptConfigItems(boolean value) {
        setOption(KEY_CANACCEPTCONFIGITEMS, value);
    }

    /**
     * Function to configure the scanner to accept statistics commands.
     *
     * @param value true to configure the scanner to accept statistics commands, false otherwise.
     */
    public void setCanAcceptStatisticsCommand(boolean value) {
        setOption(KEY_CANACCEPTSTATISTICSCMD, value);
    }

    /**
     * Function to configure the scanner to support the compare firmware version functionality.
     *
     * @param value true to configure the scanner to support the compare firmware version functionality, false otherwise.
     */
    public void setCanCompareFirmwareVersion(boolean value) {
        setOption(KEY_CANCOMPAREFIRMWAREVERSION, value);
    }

    /**
     * Function to configure che scanner to support the notify power change functionality.
     *
     * @param value true to configure che scanner to support the notify power change functionality, false otherwise.
     */
    public void setCanNotifyPowerChange(boolean value) {
        setOption(KEY_CANNOTIFYPOWERCHANGE, value);
    }

    /**
     * Function to configure the scanner to support the program config on claim functionality.
     *
     * @param value true to configure the scanner to support the program config on claim functionality, false otherwise.
     */
    public void setCanProgramConfigOnClaim(boolean value) {
        setOption(KEY_CANPROGRAMCONFIGONCLAIM, value);
    }

    /**
     * Function to configure the scanner to support the firmware update functionality.
     *
     * @param value true to configure the scanner to support the firmware update functionality, false otherwise.
     */
    public void setCanUpdateFirmware(boolean value) {
        setOption(KEY_CANUPDATEFIRMWARE, value);
    }

    /**
     * Function to configure the scanner to support the delete image file after read functionality.
     *
     * @param value true to configure the scanner to support the delete image file after read functionality, false otherwise.
     */
    public void setDeleteImageFileAfterRead(boolean value) {
        setOption(KEY_DELETEIMAGEFILEAFTERREAD, value);
    }

    /**
     * Function to set the double read timeout. If the value is outside range 0-3, 0 is used.
     *
     * @param value the desired double read timeout in range 0-3.
     */
    public void setDoubleReadTimeout(int value) {
        if (value < 0 || value > 3) {
            Log.w(TAG, "setDoubleReadTimeout: value outside range of 0-3. Setting to 0.");
            setOption(KEY_DOUBLEREADTIMEOUT, 0);
        } else {
            setOption(KEY_DOUBLEREADTIMEOUT, value);
        }
    }

    /**
     * Function to configure the scanner to support the 2 digit sups functionality.
     *
     * @param value true to configure the scanner to support the 2 digit sups functionality, false otherwise.
     */
    public void setEnable2DigitSups(boolean value) {
        setOption(KEY_ENABLE2DIGITSUPS, value);
    }

    /**
     * Function to configure the scanner to support the 4 digit price check digit functionality.
     *
     * @param value true to configure the scanner to support the 4 digit price check digit functionality, false otherwise.
     */
    public void setEnable4DigitPriceCheckDigit(boolean value) {
        setOption(KEY_ENABLE4DIGITPRICECHECKDIGIT, value);
    }

    /**
     * Function to configure the scanner to support the 5 digit price check digit functionality.
     *
     * @param value true to configure the scanner to support the 5 digit price check digit functionality, false otherwise.
     */
    public void setEnable5DigitPriceCheckDigit(boolean value) {
        setOption(KEY_ENABLE5DIGITPRICECHECKDIGIT, value);
    }

    /**
     * Function to configure the scanner to support the 5 digit sups functionality.
     *
     * @param value true to configure the scanner to support the 5 digit sups functionality, false otherwise.
     */
    public void setEnable5DigitSups(boolean value) {
        setOption(KEY_ENABLE5DIGITSUPS, value);
    }

    /**
     * Function to configure the scanner to support the barcode programming functionality.
     *
     * @param value true to configure the scanner to support the barcode programming functionality, false otherwise.
     */
    public void setEnableBarCodeProgramming(boolean value) {
        setOption(KEY_ENABLEBARCODEPROGRAMMING, value);
    }

    /**
     * Function to configure the scanner to support codabar symbology.
     *
     * @param value true to configure the scanner to support codabar symbology, false otherwise.
     */
    public void setEnableCodabar(boolean value) {
        setOption(KEY_ENABLECODABAR, value);
    }

    /**
     * Function to configure the scanner to support code128 symbology.
     *
     * @param value true to configure the scanner to support code128 symbology, false otherwise.
     */
    public void setEnableCode128(boolean value) {
        setOption(KEY_ENABLECODE128, value);
    }

    /**
     * Function to configure the scanner to support code128 sups functionality.
     *
     * @param value true to configure the scanner to support code128 sups functionality, false otherwise.
     */
    public void setEnableCode128Sups(boolean value) {
        setOption(KEY_ENABLECODE128SUPS, value);
    }

    /**
     * Function to configure the scanner to support code39 symbology.
     *
     * @param value true to configure the scanner to support code39 symbology, false otherwise.
     */
    public void setEnableCode39(boolean value) {
        setOption(KEY_ENABLECODE39, value);
    }

    /**
     * Function to configure the scanner to support the code39 check digit functionality.
     *
     * @param value true to configure the scanner to support the code39 check digit functionality, false otherwise.
     */
    public void setEnableCode39CheckDigit(boolean value) {
        setOption(KEY_ENABLECODE39CHECKDIGIT, value);
    }

    /**
     * Function to configure the scanner to support code93 symbology.
     *
     * @param value true to configure the scanner to support code93 symbology, false otherwise.
     */
    public void setEnableCode93(boolean value) {
        setOption(KEY_ENABLECODE93, value);
    }

    /**
     * Function to configure the scanner to support the EANJAN2 label decode functionality.
     *
     * @param value true to configure the scanner to support the EANJAN2 label decode functionality, false otherwise.
     */
    public void setEnableEANJAN2LabelDecode(boolean value) {
        setOption(KEY_ENABLEEANJAN2LABELDECODE, value);
    }

    /**
     * Function to configure the scanner to support the good read beep functionality.
     *
     * @param value true to configure the scanner to support the good read beep functionality, false otherwise.
     */
    public void setEnableGoodReadBeep(boolean value) {
        setOption(KEY_ENABLEGOODREADBEEP, value);
    }

    /**
     * Function to configure the scanner to support the interleaved functionality.
     *
     * @param value true to configure the scanner to support the interleaved functionality, false otherwise.
     */
    public void setEnableInterleaved(boolean value) {
        setOption(KEY_ENABLEINTERLEAVED, value);
    }

    /**
     * Function to configure the scanner to support the ITF check digit functionality.
     *
     * @param value true to configure the scanner to support the ITF check digit functionality, false otherwise.
     */
    public void setEnableITFCheckDigit(boolean value) {
        setOption(KEY_ENABLEITFCHECKDIGIT, value);
    }

    /**
     * Function to configure the scanner to support the laser on off switch functionality.
     *
     * @param value true to configure the scanner to support the laser on off switch functionality, false otherwise.
     */
    public void setEnableLaserOnOffSwitch(boolean value) {
        setOption(KEY_ENABLELASERONOFFSWITCH, value);
    }

    /**
     * Function to configure the scanner to support the UCCEAN128 symbology.
     *
     * @param value true to configure the scanner to support the UCCEAN128 symbology, false otherwise.
     */
    public void setEnableUCCEAN128(boolean value) {
        setOption(KEY_ENABLEUCCEAN128, value);
    }

    /**
     * Function to configure the scanner to support the UPCA check digit functionality.
     *
     * @param value true to configure the scanner to support the UPCA check digit functionality, false otherwise.
     */
    public void setEnableUPCACheckDigit(boolean value) {
        setOption(KEY_ENABLEUPCACHECKDIGIT, value);
    }

    /**
     * Function to configure the scanner to support the UPCA to EAN13 expansion functionality.
     *
     * @param value true to configure the scanner to support the UPCA to EAN13 expansion functionality, false otherwise.
     */
    public void setEnableUPCAtoEAN13Expansion(boolean value) {
        setOption(KEY_ENABLEUPCATOEAN13EXPANSION, value);
    }

    /**
     * Function to configure the scanner to support UPCD1D5 symbology.
     *
     * @param value true to configure the scanner to support UPCD1D5 symbology, false otherwise.
     */
    public void setEnableUPCD1D5(boolean value) {
        setOption(KEY_ENABLEUPCD1D5, value);
    }

    /**
     * Function to configure the scanner to support UPCEAN symbology.
     *
     * @param value true to configure the scanner to support UPCEAN symbology, false otherwise.
     */
    public void setEnableUPCEAN(boolean value) {
        setOption(KEY_ENABLEUPCEAN, value);
    }

    /**
     * Function to configure the scanner to support UPCE check digit functionality.
     *
     * @param value true to configure the scanner to support UPCE check digit functionality, false otherwise.
     */
    public void setEnableUPCECheckDigit(boolean value) {
        setOption(KEY_ENABLEUPCECHECKDIGIT, value);
    }

    /**
     * Function to configure the scanner to support the UPCE to EAN13 expansion functionality.
     *
     * @param value true to configure the scanner to support the UPCE to EAN13 expansion functionality, false otherwise.
     */
    public void setEnableUPCEtoEAN13Expansion(boolean value) {
        setOption(KEY_ENABLEUPCETOEAN13EXPANSION, value);
    }

    /**
     * Function to configure the scanner to support the UPCE to UPCA expansion functionality.
     *
     * @param value true to configure the scanner to support the UPCE to UPCA expansion functionality, false otherwise.
     */
    public void setEnableUPCEtoUPCAExpansion(boolean value) {
        setOption(KEY_ENABLEUPCETOUPCAEXPANSION, value);
    }

    /**
     * Function to configure the scanner to support the volume switch functionality.
     *
     * @param value true to configure the scanner to support the volume switch functionality, false otherwise.
     */
    public void setEnableVolumeSwitch(boolean value) {
        setOption(KEY_ENABLEVOLUMESWITCH, value);
    }

    /**
     * Function to configure the scanner to support the extended host timeout functionality.
     *
     * @param value true to configure the scanner to support the extended host timeout functionality, false otherwise.
     */
    public void setExtendedHostTimeout(int value) {
        setOption(KEY_EXTHOSTTIMEOUT, value);
    }

    /**
     * Function to configure the scanner to not support host commands.
     *
     * @param value true to configure the scanner to not support host commands, false otherwise.
     */
    public void setHostCommandsDisabled(boolean value) {
        setOption(KEY_HOSTCOMMANDSDISABLED, value);
    }

    /**
     * Function to set ITF length 1. The value must be even and in range 4-32. If the value is less than 4, 4 is used. If the value is more than 32, 32 is used.
     *
     * @param value the desired value, an even number in range 4-32.
     */
    public void setITFLength1(int value) {
        int i = value;
        if (i % 2 != 0) {
            Log.w(TAG, "setITFLength1: value must be an even number. Changing value to " + i);
            i++;
        }
        if (i < 4) {
            Log.w(TAG, "setITFLength1: value less than 4. Changing value to 4.");
            i = 4;
        }
        if (i > 32) {
            Log.w(TAG, "setITFLength1: value greater than 32. Changing value to 32.");
            i = 32;
        }
        setOption(KEY_ITFLENGTH1, i);
    }

    /**
     * Function to set ITF length 2. The value must be even and in range 4-32. If the value is less than 4, 4 is used. If the value is more than 32, 32 is used.
     *
     * @param value the desired value, an even number in range 4-32.
     */
    public void setITFLength2(int value) {
        int i = value;
        if (i % 2 != 0) {
            Log.w(TAG, "setITFLength2: value must be an even number. Changing value to " + i);
            i++;
        }
        if (i < 4) {
            Log.w(TAG, "setITFLength2: value less than 4. Changing value to 4.");
            i = 4;
        }
        if (i > 32) {
            Log.w(TAG, "setITFLength2: value greater than 32. Changing value to 32.");
            i = 32;
        }
        setOption(KEY_ITFLENGTH2, i);
    }

    /**
     * Function to configure the scanner to support the ITF range functionality.
     *
     * @param value true to configure the scanner to support the ITF range functionality, false otherwise.
     */
    public void setITFRange(boolean value) {
        setOption(KEY_ITFRANGE, value);
    }

    /**
     * Function to set the laser timeout. If the value is outside range 0-3, 0 is used.
     *
     * @param value the desired laser timeout, a value in range 0-3.
     */
    public void setLaserTimeout(int value) {
        if (value < 0 || value > 3) {
            Log.w(TAG, "setLaserTimeout: value outside range of 0-3. Setting to 0.");
            setOption(KEY_LASERTIMEOUT, 0);
        } else {
            setOption(KEY_LASERTIMEOUT, value);
        }
    }

    /**
     * Function to set the LED good read duration. If the value is outside range 0-3, 0 is used.
     *
     * @param value the desired LED good read duration, a calue in range 0-3.
     */
    public void setLEDGoodReadDuration(int value) {
        if (value < 0 || value > 3) {
            Log.w(TAG, "setLEDGoodReadDuration: value outside range of 0-3. Setting to 0.");
            setOption(KEY_LEDGOODREADDURATION, 0);
        } else {
            setOption(KEY_LEDGOODREADDURATION, value);
        }
    }

    /**
     * Function to set the logical flash name.
     *
     * @param value the desired logical flash name.
     */
    public void setLogicalFlashName(String value) {
        setOption(KEY_LOGICALFLASHNAME, value);
    }

    /**
     * Function to configure the scanner to support the MBeans functionality.
     *
     * @param value true to configure the scanner to support the MBeans functionality, false otherwise.
     */
    public void setMBeansEnabled(boolean value) {
        setOption(KEY_MBEANSENABLED, value);
    }

    /**
     * Function to set the motor timeout. If the value is outside range 0-7, 0 is used.
     *
     * @param value the desired motor timeout in range 0-7.
     */
    public void setMotorTimeout(int value) {
        if (value < 0 || value > 7) {
            Log.w(TAG, "setMotorTimeout: value outside of range 0-7. Setting to 0.");
            setOption(KEY_MOTORTIMEOUT, 0);
        } else {
            setOption(KEY_MOTORTIMEOUT, value);
        }
    }

    /**
     * Function to configure the scanner to support the send IHS on claim functionality.
     *
     * @param value true to configure the scanner to support the send IHS on claim functionality, false otherwise.
     */
    public void setSendIHSOnClaim(boolean value) {
        setOption(KEY_SENDIHSONCLAIM, value);
    }

    /**
     * Function to set store label security level. If the value is outside range 0-7, 0 is used.
     *
     * @param value the desired store label security level in range 0-7.
     */
    public void setStoreLabelSecurityLevel(int value) {
        if (value < 0 || value > 7) {
            Log.w(TAG, "setStoreLabelSecurityLevel: value outside range of 0-7. Setting to 0.");
            setOption(KEY_STORELABELSECURITYLEVEL, 0);
        } else {
            setOption(KEY_STORELABELSECURITYLEVEL, value);
        }
    }

    /**
     * Function to configure the scanner to support the two ITFs functionality.
     *
     * @param value true to configure the scanner to support the two ITFs functionality, false otherwise.
     */
    public void setTwoITFs(boolean value) {
        setOption(KEY_TWOITFS, value);
    }

    /**
     * Function to configure the scanner to support the WMI functionality.
     *
     * @param value true to configure the scanner to support the WMI functionality, false otherwise.
     */
    public void setWMIEnabled(boolean value) {
        setOption(KEY_WMIENABLED, value);
    }

}
