package com.datalogic.dlapos.androidpos.service;

import android.content.Context;

import com.datalogic.dlapos.androidpos.common.DLSCConfig;
import com.datalogic.dlapos.androidpos.common.DLSDeviceInfo;
import com.datalogic.dlapos.androidpos.common.DLSException;
import com.datalogic.dlapos.androidpos.common.DLSJposConst;
import com.datalogic.dlapos.androidpos.common.DLSProperties;
import com.datalogic.dlapos.androidpos.common.DLSScannerConfig;
import com.datalogic.dlapos.androidpos.common.DLSState;
import com.datalogic.dlapos.androidpos.common.DLSStatistics;
import com.datalogic.dlapos.androidpos.interpretation.DLSDevice;
import com.datalogic.dlapos.androidpos.interpretation.DeviceErrorStatusListener;
import com.datalogic.dlapos.androidpos.interpretation.DirectIODataListener;
import com.datalogic.dlapos.commons.constant.CommonsConstants;
import com.datalogic.dlapos.commons.constant.ErrorConstants;
import com.datalogic.dlapos.commons.event.DirectIOEvent;
import com.datalogic.dlapos.commons.event.EventCallback;
import com.datalogic.dlapos.commons.service.BaseService;
import com.datalogic.dlapos.commons.support.APosException;
import com.datalogic.dlapos.commons.upos.RequestListener;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Base service for Datalogic services.
 */
public class DLSBaseService implements BaseService, DeviceErrorStatusListener, DirectIODataListener {

    /**
     * Service version as integer.
     */
    public final static int DEVICE_SERVICE_VERSION = 1014061;
    /**
     * Service version string.
     */
    public static final String VERSION = "1.14.000";
    private final static String APP_NAME = "DLAndroidPOS";

    protected DLSProperties options;
    private EventCallback eventCallbacks;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    protected HashMap<String, Object> statistics;
    private List<String> scannerInfo = new ArrayList<>(); //TODO: never used

    private boolean enablePoll;
    protected boolean freezeEvents;
    private String busType;
    protected String category;
    private String checkHealthText;
    private String logicalName;
    private final String serviceVersion;
    protected int deviceState;
    private int powerNotify;
    protected int powerState;
    private boolean autoDisable;
    private boolean dataEventEnabled;
    protected DLSDevice device;
    protected Context context;
    protected DLSCConfig config;
    protected String decodeType;

    /**
     * Constructor.
     */
    public DLSBaseService() {
        String ver = Integer.toString(DEVICE_SERVICE_VERSION);
        this.serviceVersion = Integer.valueOf(ver.substring(0, 1)) + "." +
                Integer.valueOf(ver.substring(1, 4)) + "." +
                Integer.valueOf(ver.substring(4, 7));
        this.statistics = new HashMap<>();
        this.powerNotify = CommonsConstants.PN_DISABLED;
        this.category = "Scanner";
    }

    /**
     * Function to get the device managed by this service.
     *
     * @return the device.
     */
    public DLSDevice getDevice() {
        return this.device;
    }

    /**
     * Function to check if the connection with the device is closed.
     *
     * @return true if the connection with the device is closed, false otherwise.
     */
    public boolean isClosed() {
        return getDevice() == null || getDevice().getState() == DLSState.CLOSED;
    }

    /**
     * Function to check if the device is enabled to interact.
     *
     * @return true if the device is enabled, false otherwise.
     */
    public boolean isEnabled() {
        return (device != null && device.getState() == DLSState.ENABLED);
    }

    /**
     * Function to build the version string of the service.
     *
     * @return the version string.
     */
    public String getVersionString() {
        StringBuilder sb = new StringBuilder();
        sb.append(DLSBaseService.APP_NAME);
        sb.append(" ");
        String ver = Integer.toString(DEVICE_SERVICE_VERSION);
        sb.append(Integer.valueOf(ver.substring(0, 1)));
        sb.append(".");
        sb.append(Integer.valueOf(ver.substring(1, 4)));
        sb.append(".");
        sb.append(ver.substring(4, 7));
        sb.append(" (");
        sb.append(System.getProperty("os.arch"));
        sb.append("; ");
        sb.append(System.getProperty("os.name"));
        sb.append(")");
        return sb.toString();
    }

    /**
     * Function to get the port name. If the device is not initialized, an empty string is returned.
     *
     * @return the port name.
     */
    public String getPortName() {
        String result = "";
        if (this.device != null) {
            result = this.device.getDeviceInfo().getPortAsString();
        }
        return result;
    }

    /**
     * Function to get the logical name of the service.
     *
     * @return the logical name of the service.
     */
    public String getLogicalName() {
        return this.logicalName;
    }

    protected DLSProperties getOptions() {
        return this.options;
    }

    protected DLSDeviceInfo getDeviceInfo() {
        DLSDeviceInfo info = null;
        DLSDevice device = getDevice();
        if (device != null) {
            info = device.getDeviceInfo();
        }
        return info;
    }

    /**
     * Function to get the device number.
     *
     * @return the device number.
     * @throws APosException when:
     *                       <li>the device is not initialized (error code {@link ErrorConstants#APOS_E_NOEXISTS APOS_E_NOEXISTS})</li>
     *                       <li>the device is not opened (error code {@link ErrorConstants#APOS_E_CLOSED APOS_E_CLOSED})</li>
     *                       <li>the device is not claimed (error code {@link ErrorConstants#APOS_E_NOTCLAIMED APOS_E_NOTCLAIMED})</li>
     */
    public int getDeviceNumber() throws APosException {
        DLSDeviceInfo info = getDeviceInfo();
        if (info == null) {
            throw new APosException("No device present.", ErrorConstants.APOS_E_NOEXISTS);
        }

        checkClaimed();
        return info.getDeviceNumber();
    }

    /**
     * Function to get the bus type.
     *
     * @return the bus type.
     */
    public String getBusType() {
        return this.busType;
    }

    /**
     * Function to set the logical name.
     *
     * @param logicalName the desired logical name.
     */
    protected void setLogicalName(String logicalName) {
        this.logicalName = logicalName;
    }

    /**
     * Function to set event callbacks.
     *
     * @param eventCallbacks event callbacks to set.
     */
    protected void setEventCallbacks(EventCallback eventCallbacks) {
        this.eventCallbacks = eventCallbacks;
    }

    /**
     * Function to get event callbacks.
     *
     * @return active event callbacks.
     */
    public EventCallback getEventCallbacks() {
        return this.eventCallbacks;
    }

    /**
     * Assign the device number of a device.
     *
     * @param deviceNum int indicating the number of the device to assign.
     * @throws APosException thrown if the device is closed, or is not claimed.
     */
    public void setDeviceNumber(int deviceNum) throws APosException {
        checkClaimed();

        //setEnabled(deviceEnabled);
        device.getDeviceInfo().setDeviceNumber(deviceNum);
    }

    /**
     * Function to get the device category.
     *
     * @return the device category.
     */
    public String getCategory() {
        return this.category;
    }

    /**
     * Function to send data events.
     *
     * @throws APosException on errors.
     */
    protected void sendDataEvent() throws APosException {
        // This method is intentionally blank.
    }

    /**
     * Function to complete statistics data.
     *
     * @throws APosException on errors.
     */
    protected void completeStatistics() throws APosException {
        String v = Integer.toString(DEVICE_SERVICE_VERSION);
        String controlV = Integer.valueOf(v.substring(0, 1)) + "." +
                Integer.valueOf(v.substring(2, 4));
        statistics.put(DLSJposConst.DLS_S_SERVICE_VERSION, serviceVersion);
        float fcv = Float.parseFloat(controlV.trim());
        int cv = (int) (fcv * 100000);
        statistics.put(DLSJposConst.DLS_S_UPOS_VERSION, controlV);
        statistics.put(DLSJposConst.DLS_S_CONTROL_VERSION, cv);

        String pn = "PN_DISABLED";
        if (getPowerNotify() == CommonsConstants.PN_ENABLED) {
            pn = "PN_ENABLED";
        }
        statistics.put(DLSJposConst.DLS_S_POWER_NOTIFY, pn);
        statistics.put(DLSJposConst.DLS_S_POWER_STATE, getPowerState());
        String pr;
        int value = getCapPowerReporting();
        switch (value) {
            case CommonsConstants.PR_ADVANCED:
                pr = "Advanced";
                break;
            case CommonsConstants.PR_STANDARD:
                pr = "Standard";
                break;
            default:
                pr = "None";
        }
        statistics.put(DLSJposConst.DLS_S_CAP_PWR_REPORT, pr);
        statistics.put(DLSJposConst.DLS_S_CAP_STATS_REPORT, getCapStatisticsReporting());
        statistics.put(DLSJposConst.DLS_S_CAP_UPDATE_STATS, getCapUpdateStatistics());
        statistics.put(DLSJposConst.DLS_S_CAP_COMP_FW_VER, getCapCompareFirmwareVersion());
        statistics.put(DLSJposConst.DLS_S_CAP_UPDATE_FW, getCapUpdateFirmware());
        // TODO: These are WMI and MBeans specific apparently. May be able to be removed.
        /* I agree, so long as we are not supporting physical statistics files on Android.  There
           are three ways for customers to have Remote Management.  The first is a flat text file
           called scanner_info.txt that is created each time a device is claimed.  The second is WMI
           and the third is jmx.  If WMI and JMX are not supported, our customers expect to be
           able to see the statistics through the text file.  If we remove this, there is no way
           for customers to be able to see statistics without writing an implementation that gathers
           statistics and presents them in a format suitable for the customer.
         */
        DLSDeviceInfo info = device.getDeviceInfo();
        statistics.put(DLSJposConst.DLS_S_CAPTION, info.getVendorName());
        statistics.put(DLSJposConst.DLS_S_DESCRIPTION, info.getDeviceDescription());
        statistics.put(DLSJposConst.DLS_S_PHYSICAL_DESCRIPTION, info.getProductDescription());
        statistics.put(DLSJposConst.DLS_S_PHYSICAL_NAME, info.getProductName());
        statistics.put(DLSJposConst.DLS_S_ASYNC_MODE, 0);
        statistics.put(DLSJposConst.DLS_S_MANUFACTURE_NAME, info.getVendorName());
    }

    //region Checkers

    /**
     * Function to check if a device is opened.
     *
     * @throws APosException if the device is closed (error code {@link ErrorConstants#APOS_E_CLOSED APOS_E_CLOSED}).
     */
    protected void checkOpened() throws APosException {
        if (isClosed()) {
            throw new APosException("Device not open.", ErrorConstants.APOS_E_CLOSED);
        }
    }

    /**
     * Function to check if a device is claimed.
     *
     * @throws APosException when:
     *                       <li>the device is closed (error code {@link ErrorConstants#APOS_E_CLOSED APOS_E_CLOSED})</li>
     *                       <li>the device is not claimed (error code {@link ErrorConstants#APOS_E_NOTCLAIMED APOS_E_NOTCLAIMED})</li>
     */
    protected void checkClaimed() throws APosException {
        if (!getClaimed()) {
            throw new APosException("Device not claimed.", ErrorConstants.APOS_E_NOTCLAIMED);
        }
    }

    /**
     * Function to check if a device is enabled.
     *
     * @throws APosException if the device is not enabled (error code {@link ErrorConstants#APOS_E_DISABLED APOS_E_DISABLED})
     */
    protected void checkEnabled() throws APosException {
        if (!isEnabled()) {
            throw new APosException("Device not enabled.", ErrorConstants.APOS_E_DISABLED);
        }
    }
    //endregion

    //region Methods

    /**
     * {@inheritDoc}
     */
    @Override
    public void checkHealth(int level) throws APosException {
        checkClaimed();

        DLSDevice device = getDevice();
        switch (level) {
            case CommonsConstants.CH_INTERNAL:
                try {
                    if (device.isAlive()) {
                        checkHealthText = "Internal Health Check: Successful.";
                    } else {
                        checkHealthText = "Internal Health Check: Not Successful.";
                    }
                } catch (DLSException de) {
                    checkHealthText = "Internal Health Check: Not Successful, " + de.getMessage();
                }
                break;
            case CommonsConstants.CH_EXTERNAL:
                try {
                    if (device.doHealthCheck()) {
                        checkHealthText = "External Health Check: Successful.";
                    } else {
                        checkHealthText = "External Health Check: Not Successful.";
                    }
                } catch (DLSException de) {
                    checkHealthText = "External Health Check: Not Successful, " + de.getMessage();
                }
                break;
            case CommonsConstants.CH_INTERACTIVE:
                checkHealthText = "Interactive Health Check: Not supported.";
                break;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void claim(int timeOut) throws APosException {
        if (device == null) {
            throw new APosException("Device does not exist.", ErrorConstants.APOS_E_NOHARDWARE);
        }

        if (getClaimed()) {
            return;
        }

        DLSDeviceInfo info = device.getDeviceInfo();
        this.enablePoll = options.isEnableDisablePoll();
        this.category = info.getDeviceCategory();
        this.busType = info.getDeviceBus();

        checkOpened();

        config = device.getConfiguration();
        try {
            this.device.claim(timeOut);
        } catch (DLSException de) {
            throw new APosException(de.getLocalizedMessage(), ErrorConstants.APOS_E_FAILURE);
        }

        this.device.addDeviceErrorListener(this);
        this.device.addDeviceStatusListener(this);
        this.device.addDirectIODataListener(this);
        info.setServiceVersion(serviceVersion);
    }

    @Override
    public void claim(RequestListener listener) throws APosException {
        if (device == null) {
            throw new APosException("Device does not exist.", ErrorConstants.APOS_E_NOHARDWARE);
        }

        if (getClaimed()) {
            return;
        }

        DLSDeviceInfo info = device.getDeviceInfo();
        this.enablePoll = options.isEnableDisablePoll();
        this.category = info.getDeviceCategory();
        this.busType = info.getDeviceBus();

        checkOpened();

        config = device.getConfiguration();
        try {
            this.device.claim(listener);
        } catch (DLSException de) {
            throw new APosException(de.getLocalizedMessage(), ErrorConstants.APOS_E_FAILURE, de);
        }

        this.device.addDeviceErrorListener(this);
        this.device.addDeviceStatusListener(this);
        this.device.addDirectIODataListener(this);
        info.setServiceVersion(serviceVersion);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws APosException {
        try {
            if (getClaimed()) {
                release();
            }
        } catch (APosException je) {
            //TODO: correct throwing after fixes in Control
            // The exception is intentionally not thrown here.
            // This is to make sure that the device instance sets the closed flag to false.
        }
        try {
            device.close();
            deviceState = CommonsConstants.S_CLOSED;
        } catch (DLSException de) {
            //TODO: correct throwing after fixes in Control
        }
    }

    /**
     * {@inheritDoc}
     */
    //This method is meant to be overridden in sub-classes.
    @Override
    public void compareFirmwareVersion(String firmwareFileName, int[] result) throws APosException {
        checkClaimed();
        checkEnabled();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void directIO(int command, int[] data, Object obj) throws APosException {
        // command of -7 is a special case for switching baud rates during firmware update.
        if (command != -7) {
            checkClaimed();
        }

        try {
            device.directIO(command, data, obj);
        } catch (DLSException de) {
            throw new APosException(de.getLocalizedMessage(), ErrorConstants.APOS_E_FAILURE);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void open(String logicalName, EventCallback cb, Context context) throws APosException {
        this.options = DLSProperties.getInstance(context);
        setLogicalName(logicalName);
        setEventCallbacks(cb);
        this.deviceState = CommonsConstants.S_IDLE;
        this.context = context;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void release() throws APosException {
        checkClaimed();

        this.device.removeDirectIODataListener(this);
        this.device.removeDeviceErrorListener(this);
        this.device.removeDeviceStatusListener(this);
        try {
            this.device.release();
        } catch (DLSException de) {
            throw new APosException(de.getLocalizedMessage(), ErrorConstants.APOS_E_FAILURE);
        }
        // TODO: If notRun is needed, set it to true here.
        // TODO: If claim lock file is needed, put the removal here.
        // TODO: If WMI is needed, put unregister here.
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void resetStatistics(String statisticsBuffer) throws APosException {
        if (!getCapStatisticsReporting()) {
            throw new APosException("CapStatisticsReporting is set to false", ErrorConstants.APOS_E_ILLEGAL);
        }

        if (!getCapUpdateStatistics()) {
            throw new APosException("CapUpdateStatistics is set to false", ErrorConstants.APOS_E_ILLEGAL);
        }

        checkClaimed();
        throw new APosException("Reset Statistics not supported.", ErrorConstants.APOS_E_ILLEGAL);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void retrieveStatistics(String[] statisticsBuffer) throws APosException {
        checkOpened();
        checkClaimed();

        if (!getCapStatisticsReporting()) {
            throw new APosException("CapStatisticsReporting is set to false", ErrorConstants.APOS_E_ILLEGAL);
        }

        if (statisticsBuffer == null || statisticsBuffer.length == 0) {
            statisticsBuffer = new String[]{"", ""};
        }
        if (statisticsBuffer.length == 1) {
            String tempStats = statisticsBuffer[0];
            statisticsBuffer = new String[]{tempStats, ""};
        }

        try {
            this.statistics = this.device.getStatistics();
        } catch (DLSException de) {
            throw new APosException(de.getLocalizedMessage(), ErrorConstants.APOS_E_ILLEGAL);
        }

        if (statistics.containsKey(DLSJposConst.DLS_S_DEVICE_CATEGORY)) {
            if (null == statistics.get(DLSJposConst.DLS_S_DEVICE_CATEGORY)) {
                throw new APosException("Device Category unknown.", ErrorConstants.APOS_E_ILLEGAL);
            }
        }

        completeStatistics();

        //Fetch the singleton object that deals with i-h-s data.
        DLSStatistics stats = DLSStatistics.getInstance(context);

        //Build the XML string to return via reference variable statisticsBuffer.
        stats.buildXMLString(statistics, statisticsBuffer);

        //TODO: WMI related, probably can be removed
        //Perform service level operations and call stats object version.
        //this.loadWMI();

        //DLSDeviceInfo info = device.getDeviceInfo();
        //String scannerInfoFilename = info.getDeviceClass() + "_" + options.getInfoFilename() + ".txt";
        //TODO: What does it means to have avalance enabled?
//        boolean avalancheEnabled = options.getAvalancheEnabled();
        //      boolean avalancheEnabled = false;

        //TODO: here we have to decide how to handle the file, save it is not the right thing to do.

        //The second item of the buffer is used to store the info file.
        statisticsBuffer[1] = stats.scannerInfoFile(statistics, false);

        scannerInfo.clear(); // we are done with this list!
    }

    /**
     * Operation not supported, use the one with InputStream.
     */
    @Override
    public void updateFirmware(String firmwareFileName) throws APosException {
        checkOpened();
        checkClaimed();
        checkEnabled();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateStatistics(String statisticsBuffer) throws APosException {
        checkClaimed();
        if (!getCapStatisticsReporting()) {
            throw new APosException("CapStatisticReporting is false.", ErrorConstants.APOS_E_ILLEGAL);
        }
        if (!getCapUpdateStatistics()) {
            throw new APosException("CapUpdateStatistics is false.", ErrorConstants.APOS_E_ILLEGAL);
        }

        throw new APosException("Update statistics not supported.", ErrorConstants.APOS_E_ILLEGAL);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clearInput() throws APosException {
        checkClaimed();
    }
    //endregion

    //region Capabilities

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getCapCompareFirmwareVersion() throws APosException {
        checkOpened();
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getCapPowerReporting() throws APosException {
        checkOpened();
        return CommonsConstants.PR_STANDARD;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getCapStatisticsReporting() throws APosException {
        checkOpened();
//        if (this.device == null) {
//            return false;
//        }
        return this.device.hasStatisticsReporting();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getCapUpdateFirmware() throws APosException {
        checkOpened();
        return false;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getCapUpdateStatistics() throws APosException {
        checkOpened();
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getCheckHealthText() throws APosException {
        checkOpened();
        return checkHealthText;
    }
    //endregion

    //region Properties

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getAutoDisable() throws APosException {
        checkOpened();
        return autoDisable;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setAutoDisable(boolean enabled) throws APosException {
        checkOpened();
        autoDisable = enabled;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getClaimed() throws APosException {
        boolean claimed;

        checkOpened();

        DLSState state = getDevice().getState();
        claimed = (state == DLSState.CLAIMED || state == DLSState.ENABLED ||
                state == DLSState.DISABLED);

        return claimed;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getDataEventEnabled() throws APosException {
        checkOpened();
        return dataEventEnabled;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setDataEventEnabled(boolean enabled) throws APosException {
        checkOpened();
        dataEventEnabled = enabled;

        if (dataEventEnabled && getClaimed() && !getFreezeEvents()) {
            sendDataEvent();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getDeviceEnabled() throws APosException {
        checkOpened();
        return isEnabled();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setDeviceEnabled(Boolean enabled) throws APosException {
        checkClaimed();
        this.deviceState = CommonsConstants.S_BUSY;

        if (enabled) {
            try {
                this.device.enable();
            } catch (DLSException de) {
                throw new APosException("Exception enabling device. " +
                        de.getLocalizedMessage(), ErrorConstants.APOS_E_FAILURE);
            }
            if (this.enablePoll) {
                int rate = this.options.getEnableDisablePollRate();
                EnableDisablePoll poll = new EnableDisablePoll(this.device, this.options, true);
                this.scheduler.scheduleAtFixedRate(poll, rate, rate, TimeUnit.SECONDS);
            }
        } else {
            try {
                this.device.disable();
            } catch (DLSException de) {
                throw new APosException("Exception disabling device. " +
                        de.getLocalizedMessage(), ErrorConstants.APOS_E_FAILURE);
            }
        }

        this.deviceState = CommonsConstants.S_IDLE;
        DLSCConfig config = this.device.getConfiguration();
        boolean canNotifyPowerChange = config.getOptionAsBool(
                DLSScannerConfig.KEY_CANNOTIFYPOWERCHANGE);
        sendDataEvent();
        boolean isScanner = this.category.contains("Scanner");

        if (isEnabled()) {
            if (canNotifyPowerChange && isScanner) {
                if (getPowerNotify() == CommonsConstants.PN_ENABLED) {
                    boolean alive;

                    try {
                        alive = this.device.isAlive();
                    } catch (DLSException de) {
                        throw new APosException("Exception determining " +
                                "if device is alive. " + de.getLocalizedMessage(), ErrorConstants.APOS_E_FAILURE);
                    }
                    if (alive) {
                        this.powerState = CommonsConstants.PS_ONLINE;
                        onDeviceStatus(this.powerState);
                    }
                }
            }
        } else {
            if (canNotifyPowerChange && isScanner) {
                this.powerState = CommonsConstants.PS_UNKNOWN;
                onDeviceStatus(this.powerState);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDeviceServiceDescription() throws APosException {
        checkOpened();
        String result = "Scanner";
        if (this.device != null) {
            result = device.getDeviceInfo().getProductDescription();
        }
        // TODO: In JavaPOS, this method contains a scale setting routine. Fix it.
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getDeviceServiceVersion() throws APosException {
        checkOpened();
        return DEVICE_SERVICE_VERSION;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getFreezeEvents() throws APosException {
        checkOpened();
        return freezeEvents;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setFreezeEvents(boolean value) throws APosException {
        checkOpened();
        this.freezeEvents = value;
        if (!this.freezeEvents) {
            sendDataEvent();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getPhysicalDeviceDescription() throws APosException {
        checkOpened();

        if (device == null) {
            return "";
        }

        return this.device.getDeviceInfo().getDeviceDescription();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getPhysicalDeviceName() throws APosException {
        checkOpened();
        if (device == null) {
            return "";
        }
        return this.device.getDeviceInfo().getDeviceName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getPowerNotify() throws APosException {
        checkOpened();
        return powerNotify;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setPowerNotify(int mode) throws APosException {
        checkOpened();
        powerNotify = mode;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getPowerState() throws APosException {
        checkOpened();
        return CommonsConstants.PS_UNKNOWN;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getState() {
        return deviceState;
    }
    //endregion

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete() throws APosException {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getDataCount() throws APosException {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDeviceError(int nErrorCode) {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDeviceStatus(int nStatusCode) {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDirectIOData(int cmd, byte[] buf) {
        if ((buf != null) && (buf.length > 0)) {
            ByteArrayOutputStream bs = new ByteArrayOutputStream();
            try {
                bs.write(buf);
            } catch (IOException ioe) {
                return;
            }
            if (this.eventCallbacks == null) {
                return;
            }
            DirectIOEvent de = new DirectIOEvent(this, cmd, 0, bs);
            this.eventCallbacks.fireEvent(de, EventCallback.EventType.DirectIO);
        }
    }
}
