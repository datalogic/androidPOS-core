package com.datalogic.dlapos.androidpos.common;

import com.datalogic.dlapos.androidpos.service.DLSBaseService;

/**
 * Class containing the info of a generic device.
 */
public class DLSDeviceInfo extends DLSCConfig {
    /**
     * Key to access the option to check if a device is a PowerScan 8xxx, or to set that option.
     */
    public final static String KEY_8XXX = "8xxx";
    /**
     * Key to access the option to check if a device is a PowerScan 9xxx, or to set that option.
     */
    public final static String KEY_9XXX = "9xxx";
    /**
     * Key to access the option containing the baud rate configuration.
     */
    public final static String KEY_BAUDRATE = "baudRate";
    /**
     * Key to access the option containing the configOnClaim configuration.
     */
    public final static String KEY_CONFIGONCLAIM = "configOnClaim";
    /**
     * Key to access the option containing the configWithDIO configuration.
     */
    public final static String KEY_CONFIGWITHDIO = "configWithDIO";
    /**
     * Key to access the option containing the dataBits configuration.
     */
    public final static String KEY_DATABITS = "dataBits";
    /**
     * Key to access the option containing the dataPrefix configuration.
     */
    public final static String KEY_DATAPREFIX = "dataPrefix";
    /**
     * Key to access the option containing the dataSuffix configuration.
     */
    public final static String KEY_DATASUFFIX = "dataSuffix";
    /**
     * Key to access the option containing the decodeType configuration.
     */
    public final static String KEY_DECODETYPE = "decodeType";
    /**
     * Key to access the option containing the deviceBus configuration.
     */
    public final static String KEY_DEVICEBUS = "deviceBus";
    /**
     * Key to access the option containing the deviceCategory configuration.
     */
    public final static String KEY_DEVICECATEGORY = "deviceCategory";
    /**
     * Key to access the option containing the deviceClass configuration.
     */
    public final static String KEY_DEVICECLASS = "deviceClass";
    /**
     * Key to access the option containing the deviceDescription configuration.
     */
    public final static String KEY_DEVICEDESCRIPTION = "deviceDescription";
    /**
     * Key to access the option containing the deviceName configuration.
     */
    public final static String KEY_DEVICENAME = "deviceName";
    /**
     * Key to access the option containing the disableOnExit configuration.
     */
    public final static String KEY_DISABLEONEXIT = "disableOnExit";
    /**
     * Key to access the option containing the flowControl configuration.
     */
    public final static String KEY_FLOWCONTROL = "flowControl";
    /**
     * Key to access the option containing the fullDisable configuration.
     */
    public final static String KEY_FULLDISABLE = "fullDisable";
    /**
     * Key to access the option containing the imageBuffers configuration.
     */
    public final static String KEY_IMAGEBUFFERS = "imageBuffers";
    /**
     * Key to access the option containing the ipAddress configuration.
     */
    public final static String KEY_IPADDRESS = "ipAddress";
    /**
     * Key to access the option containing the ipPort configuration.
     */
    public final static String KEY_IPPORT = "ipPort";
    /**
     * Key to access the option containing the jposVersion configuration.
     */
    public final static String KEY_JPOSVERSION = "jposVersion";
    /**
     * Key to access the option containing the laneNumber configuration.
     */
    public final static String KEY_LANENUMBER = "laneNumber";
    /**
     * Key to access the option containing the logicalFlashName configuration.
     */
    public final static String KEY_LOGICALFLASHNAME = "logicalFlashName";
    /**
     * Key to access the option containing the MBeansEnabled configuration.
     */
    public final static String KEY_MBEANSENABLED = "MBeansEnabled";
    /**
     * Key to access the option containing the parity configuration.
     */
    public final static String KEY_PARITY = "parity";
    /**
     * Key to access the option containing the portName configuration.
     */
    public final static String KEY_PORTNAME = "portName";
    /**
     * Key to access the option containing the productDescription configuration.
     */
    public final static String KEY_PRODUCTDESCRIPTION = "productDescription";
    /**
     * Key to access the option containing the productId configuration.
     */
    public final static String KEY_PRODUCTID = "productId";
    /**
     * Key to access the option containing the productName configuration.
     */
    public final static String KEY_PRODUCTNAME = "productName";
    /**
     * Key to access the option containing the productURL configuration.
     */
    public final static String KEY_PRODUCTURL = "productURL";
    /**
     * Key to access the option containing the rxPrefix configuration.
     */
    public final static String KEY_RXPREFIX = "rxPrefix";
    /**
     * Key to access the option containing the rxTrailer configuration.
     */
    public final static String KEY_RXTRAILER = "rxTrailer";
    /**
     * Key to access the option containing the scanControl configuration.
     */
    public final static String KEY_SCANCONTROL = "scanControl";
    /**
     * Key to access the option containing the serviceClass configuration.
     */
    public final static String KEY_SERVICECLASS = "serviceClass";
    /**
     * Key to access the option containing the serviceInstanceFactoryClass configuration.
     */
    public final static String KEY_SERVICEINSTANCEFACTORYCLASS = "serviceInstanceFactoryClass";
    /**
     * Key to access the option containing the serviceVersion configuration.
     */
    public final static String KEY_SERVICEVERSION = "serviceVersion";
    /**
     * Key to access the option containing the singleCable configuration.
     */
    public final static String KEY_SINGLECABLE = "singleCable";
    /**
     * Key to access the option containing the stopBits configuration.
     */
    public final static String KEY_STOPBITS = "stopBits";
    /**
     * Key to access the option containing the txPrefix configuration.
     */
    public final static String KEY_TXPREFIX = "txPrefix";
    /**
     * Key to access the option containing the txTrailer configuration.
     */
    public final static String KEY_TXTRAILER = "txTrailer";
    /**
     * Key to access the option containing the updateUsage configuration.
     */
    public final static String KEY_UPDATEUSAGE = "updateUsage";
    /**
     * Key to access the option containing the usage configuration.
     */
    public final static String KEY_USAGE = "usage";
    /**
     * Key to access the option containing the useBCC configuration.
     */
    public final static String KEY_USEBCC = "useBCC";
    /**
     * Key to access the option containing the useBluetoothDongle configuration.
     */
    public final static String KEY_USEBLUETOOTHDONGLE = "useBluetoothDongle";
    /**
     * Key to access the option containing the useCOMxOnLinux configuration.
     */
    public final static String KEY_USECOMXONLINUX = "useCOMxOnLinux";
    /**
     * Key to access the option containing the useSunJavaxComm configuration.
     */
    public final static String KEY_USESUNJAVAXCOMM = "useSunJavaxComm";
    /**
     * Key to access the option containing the useVirtualPort configuration.
     */
    public final static String KEY_USEVIRTUALPORT = "useVirtualPort";
    /**
     * Key to access the option containing the useSymbolicLink configuration.
     */
    public final static String KEY_USESYMBOLICLINK = "useSymbolicLink";
    /**
     * Key to access the option containing the autoSearchCOM configuration.
     */
    public final static String KEY_AUTOSEARCHCOM = "autoSearchCOM";
    /**
     * Key to access the option containing the vendorId configuration.
     */
    public final static String KEY_VENDORID = "vendorId";
    /**
     * Key to access the option containing the vendorName configuration.
     */
    public final static String KEY_VENDORNAME = "vendorName";
    /**
     * Key to access the option containing the vendorURL configuration.
     */
    public final static String KEY_VENDORURL = "vendorURL";
    /**
     * Key to access the option containing the WMIEnabled configuration.
     */
    public final static String KEY_WMIENABLED = "WMIEnabled";
    /**
     * Key to access the option containing the HDLRecordTimeout configuration.
     */
    public final static String KEY_RECORDTIMEOUT = "HDLRecordTimeout";

    private String portName;
    private int deviceNumber;

    /**
     * Default constructor.
     */
    public DLSDeviceInfo() {
        super();
        this.deviceNumber = 0;
        initializeMap();
    }

    private void initializeMap() {
        Branding branding = Branding.getInstance();
        setServiceInstanceFactoryClass("");
        setServiceClass("");
        setVendorName("");
        setVendorUrl("");
        setDeviceCategory("");
        setJposVersion("");
        setProductName("");
        setProductDescription("");
        setProductUrl("");
        setPort("1");
        setBaudRate(9600);
        setParity("None");
        setStopBits(1);
        setDataBits(8);
        setFlowControl("Xon/Xoff");
        setFullDisable(false);
        setScanControl(false);
        setVendorId(0);
        setProductId(0);
        setUsage(0);
        setDeviceBus("USB");
        setSingleCable(false);
        setDeviceName(branding.getBrandedDevice(Branding.DELIM_PREFIX + " Scanner"));
        setDeviceDescription("Scanner");
        setUseBCC(false);
        setTxPrefix((byte) 0x53);
        setTxTrailer((byte) 0xd);
        setRxPrefix((byte) 0x53);
        setRxTrailer((byte) 0xd);
        set8xxx(false);
        setDataPrefix((byte) 0);
        setDataSuffix((byte) 0);
        setWMIEnabled(false);
        setMBeansEnabled(false);
        setDisableOnExit(false);
        setDecodingType(Constants.DECODE_TYPE_US);// standard or warhol
        setLogicalFlashName(branding.getBrandedDevice(Branding.DELIM_PREFIX + "-USB-Flash"));
        setUpdateUsageNumber("A000");
        setUseVirtualPort(false);
        setDeviceClass("RS232Scanner");
        setUseCOMXOnLinux(false);
        setIPPort("26666");
        setIPAddress("192.168.0.2");
        setLaneNumber("0");
        setImageBuffers("1");
        setServiceVersion(DLSBaseService.VERSION);
        setHDLRecordTimeout(60000);
        setUseSunJavaxComm(false);
        setUseSymbolicLink(false);
        setAutoSearchComm(true);
        set9xxx(false);
        setUseBluetoothDongle(false);
        setConfigOnClaim(false);
        setConfigWithDIO(false);
    }

    /**
     * Function to get the baud rate.
     *
     * @return the baud rate, 0 if not found.
     */
    public int getBaudRate() {
        return getOptionAsInt(KEY_BAUDRATE);
    }

    /**
     * Function to get data bits.
     *
     * @return data bits, 0 if not found.
     */
    public int getDataBits() {
        return getOptionAsInt(KEY_DATABITS);
    }

    /**
     * Function to get the data prefix.
     *
     * @return data prefix, 0 if not found.
     */
    public byte getDataPrefix() {
        return getHexOptionAsByte(KEY_DATAPREFIX);
    }

    /**
     * Function to get the data suffix.
     *
     * @return data suffix, 0 if not found.
     */
    public byte getDataSuffix() {
        return getHexOptionAsByte(KEY_DATASUFFIX);
    }

    /**
     * Function to get the decoding type.
     *
     * @return decoding type, an empty string if not found.
     */
    public String getDecodingType() {
        return getOption(KEY_DECODETYPE);
    }

    /**
     * Function to get the device bus.
     *
     * @return the device bus, an empty string if not found.
     */
    public String getDeviceBus() {
        return getOption(KEY_DEVICEBUS);
    }

    /**
     * Function to get the device category.
     *
     * @return the device category, an empty string if not found.
     */
    public String getDeviceCategory() {
        return getOption(KEY_DEVICECATEGORY);
    }

    /**
     * Function to get the device class.
     *
     * @return the device class, an empty string if not found.
     */
    public String getDeviceClass() {
        return getOption(KEY_DEVICECLASS);
    }

    /**
     * Function to get the device description.
     *
     * @return the device description, an empty string if not found.
     */
    public String getDeviceDescription() {
        return getOption(KEY_DEVICEDESCRIPTION);
    }

    /**
     * Function to get the device name.
     *
     * @return the device name, an empty string if not found.
     */
    public String getDeviceName() {
        return getOption(KEY_DEVICENAME);
    }

    /**
     * Function to get the device number.
     *
     * @return the device number, 0 if not found.
     */
    public int getDeviceNumber() {
        return this.deviceNumber;
    }

    /**
     * Function to get the flow control.
     *
     * @return the flow control, an empty string if not found.
     */
    public String getFlowControl() {
        return getOption(KEY_FLOWCONTROL);
    }

    /**
     * Function to get HDL record timeout.
     *
     * @return the HDL record timeout, 0 if not found.
     */
    public int getHDLRecordTimeout() {
        return getOptionAsInt(KEY_RECORDTIMEOUT);
    }

    /**
     * Function to get image buffers.
     *
     * @return image buffers, an empty string if not found.
     */
    public String getImageBuffers() {
        return getOption(KEY_IMAGEBUFFERS);
    }

    /**
     * Function to get the IP address.
     *
     * @return the IP address, an empty string if not found.
     */
    public String getIPAddress() {
        return getOption(KEY_IPADDRESS);
    }

    /**
     * Function to get the IP port.
     *
     * @return the IP port, an empty string if not found.
     */
    public String getIPPort() {
        return getOption(KEY_IPPORT);
    }

    /**
     * Function to get the JPos version.
     *
     * @return the JPos version, an empty string if not found.
     */
    public String getJposVersion() {
        return getOption(KEY_JPOSVERSION);
    }

    /**
     * Function to get the lane number.
     *
     * @return the lane number, an empty string if not found.
     */
    public String getLaneNumber() {
        return getOption(KEY_LANENUMBER);
    }

    /**
     * Function to get the local port name.
     *
     * @return the local port name, an empty string if not found.
     */
    public String getLocalPortName() {
        return this.portName;
    }

    /**
     * Function to get the logical flash name.
     *
     * @return the logical flash name, an empty string if not found.
     */
    public String getLogicalFlashName() {
        return getOption(KEY_LOGICALFLASHNAME);
    }

    /**
     * Function to get the parity.
     *
     * @return the parity, an empty string if not found.
     */
    public String getParity() {
        return getOption(KEY_PARITY);
    }

    /**
     * Function to get the port.
     *
     * @return the port, 0 if not found.
     */
    public int getPort() {
        return getOptionAsInt(KEY_PORTNAME);
    }

    /**
     * Function to get the port as string.
     *
     * @return the port as a string, an empty string if not found.
     */
    public String getPortAsString() {
        return getOption(KEY_PORTNAME);
    }

    /**
     * Function to get the product description.
     *
     * @return the product description, an empty string if not found.
     */
    public String getProductDescription() {
        return getOption(KEY_PRODUCTDESCRIPTION);
    }

    /**
     * Function to get the product id.
     *
     * @return the product id, an empty string if not found.
     */
    public int getProductId() {
        return getHexOptionAsInt(KEY_PRODUCTID);
    }

    /**
     * Function to get the product Url.
     *
     * @return the product Url, an empty string if not found.
     */
    public String getProductUrl() {
        return getOption(KEY_PRODUCTURL);
    }

    /**
     * Function to get the product name.
     *
     * @return the product name, an empty string if not found.
     */
    public String getProductName() {
        return getOption(KEY_PRODUCTNAME);
    }

    /**
     * Function to get the rxPrefix.
     *
     * @return the rxPrefix, '\r' if not found.
     */
    public byte getRxPrefix() {
        return getHexOptionAsByte(KEY_RXPREFIX, (byte) '\r');
    }

    /**
     * Function to get the rxTrailer.
     *
     * @return the rxTrailer, '\r' if not found.
     */
    public byte getRxTrailer() {
        return getHexOptionAsByte(KEY_RXTRAILER, (byte) '\r');
    }

    /**
     * Function to get the service class.
     *
     * @return the service class, an empty string if not found.
     */
    public String getServiceClass() {
        return getOption(KEY_SERVICECLASS);
    }

    /**
     * Function to get the service instance factory class.
     *
     * @return the service instance factory class, an empty string if not found.
     */
    public String getServiceInstanceFactoryClass() {
        return getOption(KEY_SERVICEINSTANCEFACTORYCLASS);
    }

    /**
     * Function to get the service version.
     *
     * @return the service version, an empty string if not found.
     */
    public String getServiceVersion() {
        return getOption(KEY_SERVICEVERSION);
    }

    /**
     * Function to get stop bits.
     *
     * @return stop bits, 0 if not found.
     */
    public int getStopBits() {
        return getOptionAsInt(KEY_STOPBITS);
    }

    /**
     * Function to get the txPrefix.
     *
     * @return the txPrefix, '\r' if not found.
     */
    public byte getTxPrefix() {
        return getHexOptionAsByte(KEY_TXPREFIX, (byte) '\r');
    }

    /**
     * Function to get the txTrailer.
     *
     * @return the txTrailer, '\r' if not found.
     */
    public byte getTxTrailer() {
        return getHexOptionAsByte(KEY_TXTRAILER, (byte) '\r');
    }

    /**
     * Function to get the update usage number.
     *
     * @return the update usage number, an empty string if not found.
     */
    public String getUpdateUsageNumber() {
        return getOption(KEY_UPDATEUSAGE);
    }

    /**
     * Function to get the usage.
     *
     * @return the usage, 0 if not found.
     */
    public int getUsage() {
        return getHexOptionAsInt(KEY_USAGE);
    }

    /**
     * Function to get the vendor id.
     *
     * @return the vendor id, 0 if not found.
     */
    public int getVendorId() {
        return getHexOptionAsInt(KEY_VENDORID);
    }

    /**
     * Function to get the vendor name.
     *
     * @return the vendor name, an empty string if not found.
     */
    public String getVendorName() {
        return getOption(KEY_VENDORNAME);
    }

    /**
     * Function to get the vendor Url.
     *
     * @return the vendor Url, an empty string if not found.
     */
    public String getVendorUrl() {
        return getOption(KEY_VENDORURL);
    }

    /**
     * Function to check if the device is a PowerScan 8xxx with the HomeDepot personalization.
     *
     * @return true if the device is a PowerScan 8xx with the HomeDepot personalization, false otherwise.
     */
    public boolean is8xxx() {
        return getOptionAsBool(KEY_8XXX);
    }

    /**
     * Function to check if the device is a PowerScan 9xxx with the HomeDepot personalization.
     *
     * @return true if the device is a PowerScan 9xx with the HomeDepot personalization, false otherwise.
     */
    public boolean is9xxx() {
        return getOptionAsBool(KEY_9XXX);
    }

    /**
     * Function to check if the auto search communication is enabled.
     *
     * @return true if the auto search communication is enabled, false otherwise.
     */
    public boolean isAutoSearchComm() {
        return getOptionAsBool(KEY_AUTOSEARCHCOM);
    }

    /**
     * Function to check if configuration on claim is enabled.
     *
     * @return true if configuration on claim is enabled, false otherwise.
     */
    public boolean isConfigOnClaim() {
        return getOptionAsBool(KEY_CONFIGONCLAIM);
    }

    /**
     * Function to check if configuration with DIO is enabled.
     *
     * @return true if configuration with DIO is enabled, false otherwise.
     */
    public boolean isConfigWithDIO() {
        return getOptionAsBool(KEY_CONFIGWITHDIO);
    }

    /**
     * Function to check if disable on exit is enabled.
     *
     * @return true if disable on exit is enabled, false otherwise.
     */
    public boolean isDisableOnExit() {
        return getOptionAsBool(KEY_DISABLEONEXIT);
    }

    /**
     * Function to check if full disable is enabled.
     *
     * @return true if full disable is enabled, false otherwise.
     */
    public boolean isFullDisable() {
        return getOptionAsBool(KEY_FULLDISABLE);
    }

    /**
     * Function to check if MBeans are enabled.
     *
     * @return true if MBeans are enabled, false otherwise.
     */
    public boolean isMBeansEnabled() {
        return getOptionAsBool(KEY_MBEANSENABLED);
    }

    /**
     * Function to check if scan control is enabled.
     *
     * @return true if scan control is enabled, false otherwise.
     */
    public boolean isScanControl() {
        return getOptionAsBool(KEY_SCANCONTROL);
    }

    /**
     * Function to check if single cable device.
     *
     * @return true if the device is single cabled, false otherwise.
     */
    public boolean isSingleCable() {
        return getOptionAsBool(KEY_SINGLECABLE);
    }

    /**
     * Function to check if BCC is in use.
     *
     * @return true if BCC is in use, false otherwise.
     */
    public boolean isUseBCC() {
        return getOptionAsBool(KEY_USEBCC);
    }

    /**
     * Function to check if the bluetooth dongle is in use.
     *
     * @return true if the bluetooth is in use, false otherwise.
     */
    public boolean isUseBluetoothDongle() {
        return getOptionAsBool(KEY_USEBLUETOOTHDONGLE);
    }

    /**
     * Function to check if the COMX is enabled on Linux.
     *
     * @return true if the COMX is enabled, false otherwise.
     */
    public boolean isUseCOMXOnLinux() {
        return getOptionAsBool(KEY_USECOMXONLINUX);
    }

    /**
     * Function to check if Sun Javax communication is in use.
     *
     * @return true if Sun Javax communication is in use, false otherwise.
     */
    public boolean isUseSunJavaxComm() {
        return getOptionAsBool(KEY_USESUNJAVAXCOMM);
    }

    /**
     * Function to check if the symbolic link is in use.
     *
     * @return true id the symbolic link is in use, false otherwise.
     */
    public boolean isUseSymbolicLink() {
        return getOptionAsBool(KEY_USESYMBOLICLINK);
    }

    /**
     * Function to check if a virtual port is in use.
     *
     * @return true if a virtual port is in use, false otherwise.
     */
    public boolean isUseVirtualPort() {
        return getOptionAsBool(KEY_USEVIRTUALPORT);
    }

    /**
     * Function to check if the WMI is enabled.
     *
     * @return true if the WMI is enabled, false otherwise.
     */
    public boolean isWMIEnabled() {
        return getOptionAsBool(KEY_WMIENABLED);
    }

    /**
     * Function to flag the device as a PowerScan 8xxx with the HomeDepot personalization.
     *
     * @param value true to flag the device as a PowerScan 8xxx with the HomeDepot personalization, false otherwise.
     */
    public void set8xxx(boolean value) {
        setOption(KEY_8XXX, value);
    }

    /**
     * Function to flag the device as a PowerScan 9xxx with the HomeDepot personalization.
     *
     * @param value true to flag the device as a PowerScan 9xxx with the HomeDepot personalization, false otherwise.
     */
    public void set9xxx(boolean value) {
        setOption(KEY_9XXX, value);
    }

    /**
     * Function to set the device in the auto search communication mode.
     *
     * @param value true to set the device in the auto search communication mode, false otherwise.
     */
    public void setAutoSearchComm(boolean value) {
        setOption(KEY_AUTOSEARCHCOM, value);
    }

    /**
     * Function to set the baud rate.
     *
     * @param value the desired baud rate.
     */
    public void setBaudRate(int value) {
        setOption(KEY_BAUDRATE, value);
    }

    /**
     * Function to set the configuration on claim.
     *
     * @param value true to set the configuration on claim, false otherwise.
     */
    public void setConfigOnClaim(boolean value) {
        setOption(KEY_CONFIGONCLAIM, value);
    }

    /**
     * Function to enable configuration with DIO.
     *
     * @param value true to enable configuration with DIO.
     */
    public void setConfigWithDIO(boolean value) {
        setOption(KEY_CONFIGWITHDIO, value);
    }

    /**
     * Function to set data bits.
     *
     * @param value desired data bits.
     */
    public void setDataBits(int value) {
        setOption(KEY_DATABITS, value);
    }

    /**
     * Function to set the data prefix.
     *
     * @param value the desired data prefix.
     */
    public void setDataPrefix(byte value) {
        setByteOptionAsHex(KEY_DATAPREFIX, value);
    }

    /**
     * Function to set the data suffix.
     *
     * @param value the desired data suffix.
     */
    public void setDataSuffix(byte value) {
        setByteOptionAsHex(KEY_DATASUFFIX, value);
    }

    /**
     * Function to set the disable on exit.
     *
     * @param value true to set the disable on exit, false otherwise.
     */
    public void setDisableOnExit(boolean value) {
        setOption(KEY_DISABLEONEXIT, value);
    }

    /**
     * Function to set the decoding type.
     *
     * @param value the desired decoding type.
     */
    public void setDecodingType(String value) {
        setOption(KEY_DECODETYPE, value);
    }

    /**
     * Function to set the device bus.
     *
     * @param value the desired device bus.
     */
    public void setDeviceBus(String value) {
        setOption(KEY_DEVICEBUS, value);
    }

    /**
     * Function to set the device category.
     *
     * @param value the desired device category.
     */
    public void setDeviceCategory(String value) {
        setOption(KEY_DEVICECATEGORY, value);
    }

    /**
     * Function to set the device class.
     *
     * @param value the desired device class.
     */
    public void setDeviceClass(String value) {
        setOption(KEY_DEVICECLASS, value);
    }

    /**
     * Function to set the device description.
     *
     * @param value the desired device description.
     */
    public void setDeviceDescription(String value) {
        setOption(KEY_DEVICEDESCRIPTION, value);
    }

    /**
     * Function to set the device name.
     *
     * @param value the desired device name.
     */
    public void setDeviceName(String value) {
        setOption(KEY_DEVICENAME, value);
    }

    /**
     * Function to set the device number.
     *
     * @param value the desired device number.
     */
    public void setDeviceNumber(int value) {
        this.deviceNumber = value;
    }

    /**
     * Function to set the flow control.
     *
     * @param value the desired flow control.
     */
    public void setFlowControl(String value) {
        setOption(KEY_FLOWCONTROL, value);
    }

    /**
     * Function to activate the full disable mode.
     *
     * @param value true to activate the full disable mode, false otherwise.
     */
    public void setFullDisable(boolean value) {
        setOption(KEY_FULLDISABLE, value);
    }

    /**
     * Function to set the HDL record timeout.
     *
     * @param value the desired HDL record timeout.
     */
    public void setHDLRecordTimeout(int value) {
        setOption(KEY_RECORDTIMEOUT, value);
    }

    /**
     * Function to set image buffers.
     *
     * @param value the desired image buffers.
     */
    public void setImageBuffers(String value) {
        setOption(KEY_IMAGEBUFFERS, value);
    }

    /**
     * Function to set the IP address.
     *
     * @param value the desired IP address.
     */
    public void setIPAddress(String value) {
        setOption(KEY_IPADDRESS, value);
    }

    /**
     * Function to set the IP port.
     *
     * @param value the desired IP port.
     */
    public void setIPPort(String value) {
        setOption(KEY_IPPORT, value);
    }

    /**
     * Function to set the jpos version.
     *
     * @param value the desired jpos version.
     */
    public void setJposVersion(String value) {
        setOption(KEY_JPOSVERSION, value);
    }

    /**
     * Function to set the lane number.
     *
     * @param value the desired lane number.
     */
    public void setLaneNumber(String value) {
        setOption(KEY_LANENUMBER, value);
    }

    /**
     * Function to set the local port name.
     *
     * @param value the desired local port name.
     */
    public void setLocalPortName(String value) {
        this.portName = value;
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
     * Function to enable MBeans.
     *
     * @param value true to enable MBeans, false otherwise.
     */
    public void setMBeansEnabled(boolean value) {
        setOption(KEY_MBEANSENABLED, value);
    }

    /**
     * Function to set parity.
     *
     * @param value the desired parity.
     */
    public void setParity(String value) {
        setOption(KEY_PARITY, value);
    }

    /**
     * Function to set the port.
     *
     * @param value the desired port.
     */
    public void setPort(int value) {
        setOption(KEY_PORTNAME, value);
    }

    /**
     * Function to set the port.
     *
     * @param value the desired port.
     */
    public void setPort(String value) {
        setOption(KEY_PORTNAME, value);
    }

    /**
     * Function to set the product description.
     *
     * @param value the desired product description.
     */
    public void setProductDescription(String value) {
        setOption(KEY_PRODUCTDESCRIPTION, value);
    }

    /**
     * Function to set the product id.
     *
     * @param value the desired product id.
     */
    public void setProductId(int value) {
        setIntOptionAsHex(KEY_PRODUCTID, value);
    }

    /**
     * Function to set the product name.
     *
     * @param value the desired product name.
     */
    public void setProductName(String value) {
        setOption(KEY_PRODUCTNAME, value);
    }

    /**
     * Function to set the product Url.
     *
     * @param value the desired product Url.
     */
    public void setProductUrl(String value) {
        setOption(KEY_PRODUCTURL, value);
    }

    /**
     * Function to set the rx prefix.
     *
     * @param value the desired rx prefix.
     */
    public void setRxPrefix(byte value) {
        setByteOptionAsHex(KEY_RXPREFIX, value);
    }

    /**
     * Function to set the rx trailer.
     *
     * @param value the desired rx trailer.
     */
    public void setRxTrailer(byte value) {
        setByteOptionAsHex(KEY_RXTRAILER, value);
    }

    /**
     * Function to set the service class.
     *
     * @param value the desired service class.
     */
    public void setServiceClass(String value) {
        setOption(KEY_SERVICECLASS, value);
    }

    /**
     * Function to set the service instance factory class.
     *
     * @param value the desired service instance factory class.
     */
    public void setServiceInstanceFactoryClass(String value) {
        setOption(KEY_SERVICEINSTANCEFACTORYCLASS, value);
    }

    /**
     * Function to set the service version.
     *
     * @param value the desired service version.
     */
    public void setServiceVersion(String value) {
        setOption(KEY_SERVICEVERSION, value);
    }

    /**
     * Function to flag the device as single cable.
     *
     * @param value true to flag the device as single cable, false otherwise.
     */
    public void setSingleCable(boolean value) {
        setOption(KEY_SINGLECABLE, value);
    }

    /**
     * Function to enable the scan control.
     *
     * @param value true to enable the scan control, false otherwise.
     */
    public void setScanControl(boolean value) {
        setOption(KEY_SCANCONTROL, value);
    }

    /**
     * Function to set stop bits.
     *
     * @param value desired stop bits.
     */
    public void setStopBits(int value) {
        setOption(KEY_STOPBITS, value);
    }

    /**
     * Function to set the tx prefix.
     *
     * @param value the desired tx prefix.
     */
    public void setTxPrefix(byte value) {
        setByteOptionAsHex(KEY_TXPREFIX, value);
    }

    /**
     * Function to set the tx trailer.
     *
     * @param value the desired tx trailer.
     */
    public void setTxTrailer(byte value) {
        setByteOptionAsHex(KEY_TXTRAILER, value);
    }

    /**
     * Function to set the update usage number.
     *
     * @param value the desired update usage message.
     */
    public void setUpdateUsageNumber(String value) {
        setOption(KEY_UPDATEUSAGE, value);
    }

    /**
     * Function to set the usage.
     *
     * @param value the desired usage.
     */
    public void setUsage(int value) {
        setIntOptionAsHex(KEY_USAGE, value);
    }

    /**
     * Function to use the BCC.
     *
     * @param value true to use the BCC to true, false otherwise.
     */
    public void setUseBCC(boolean value) {
        setOption(KEY_USEBCC, value);
    }

    /**
     * Function to use the bluetooth dongle.
     *
     * @param value true to use the Bluetooth Dongle usage.
     */
    public void setUseBluetoothDongle(boolean value) {
        setOption(KEY_USEBLUETOOTHDONGLE, value);
    }

    /**
     * Function to use the COMX on Linux.
     *
     * @param value true to use the COMX on Linux.
     */
    public void setUseCOMXOnLinux(boolean value) {
        setOption(KEY_USECOMXONLINUX, value);
    }

    /**
     * Function to use the Sun Javax communication.
     *
     * @param value true to use the sun javaX, false otherwise.
     */
    public void setUseSunJavaxComm(boolean value) {
        setOption(KEY_USESUNJAVAXCOMM, value);
    }

    /**
     * Function to use a symbolic link.
     *
     * @param value true to use a symbolic link.
     */
    public void setUseSymbolicLink(boolean value) {
        setOption(KEY_USESYMBOLICLINK, value);
    }

    /**
     * Function to use a virtual port.
     *
     * @param value true to use a virtual port, false otherwise.
     */
    public void setUseVirtualPort(boolean value) {
        setOption(KEY_USEVIRTUALPORT, value);
    }

    /**
     * Function to set the vendor id.
     *
     * @param value the desired vendor id.
     */
    public void setVendorId(int value) {
        setIntOptionAsHex(KEY_VENDORID, value);
    }

    /**
     * Function to set vendor name.
     *
     * @param value the desired vendor name.
     */
    public void setVendorName(String value) {
        setOption(KEY_VENDORNAME, value);
    }

    /**
     * Function to set the vendor Url.
     *
     * @param value the desired vendor Url.
     */
    public void setVendorUrl(String value) {
        setOption(KEY_VENDORURL, value);
    }

    /**
     * Function to enable the WMI.
     *
     * @param value true to enable the WMI, false otherwise.
     */
    public void setWMIEnabled(boolean value) {
        setOption(KEY_WMIENABLED, value);
    }
}
