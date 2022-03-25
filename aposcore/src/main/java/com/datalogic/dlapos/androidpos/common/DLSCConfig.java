package com.datalogic.dlapos.androidpos.common;

import com.datalogic.dlapos.commons.support.APosException;
import com.datalogic.dlapos.confighelper.configurations.accessor.ProfileManager;
import com.datalogic.dlapos.confighelper.configurations.support.DLAPosProfile;

import java.util.HashMap;
import java.util.Set;

/**
 * Abstract class defining common methods and fields for devices configuration.
 */
public abstract class DLSCConfig {
    private String strLogicalName;
    private final HashMap<String, String> mapConfig;
    private final HashMap<String, Object> configItems;

    /**
     * Default constructor
     */
    public DLSCConfig() {
        mapConfig = new HashMap<>();
        configItems = new HashMap<>();
    }

    /**
     * Loads the configuration for the specified logical name from the
     * {@code apos.json} file.
     *
     * @param logicalName    String containing the logical name to load
     * @param profileManager The profileManagerToUse
     * @return boolean indicating success
     */
    public boolean loadConfiguration(String logicalName, ProfileManager profileManager) throws APosException {
        if (profileManager == null)
            throw new IllegalArgumentException("Profile Manager can not be null.");

        strLogicalName = logicalName;
        DLAPosProfile profile = profileManager.getConfigurationForProfileId(logicalName);

        if (profile == null)
            return false;

        loadConfigurationItems(profile);

        for (String entry : mapConfig.keySet()) {
            mapConfig.put(entry, readOption(profile, entry, mapConfig.get(entry)));
        }

        return true;
    }

    private void loadConfigurationItems(DLAPosProfile profile) {
        Set<String> propertiesNames = profile.getAsProfileConfiguration().toHashMap().keySet();
        for (String propName : propertiesNames) {
            boolean isHex = propName.matches("[0-9a-fA-F]+");
            if (propName.length() == 4 && isHex) {
                String value = readOption(profile, propName, "0000");
                configItems.put(propName, value);
            }
        }
    }

    /**
     * Returns the current value of the logical name for a configuration.
     *
     * @return String containing the logical name
     */
    public String getLogicalName() {
        return strLogicalName;
    }

    /**
     * Function to read a String option of the configuration.
     *
     * @param profile  the profile to read.
     * @param key      the name of the option to read.
     * @param defValue the value returned when the option is not found.
     * @return the value related to the desired key, or the default value.
     */
    protected String readOption(DLAPosProfile profile, String key, String defValue) {
        String value = profile.getProperty(key);
        return value != null ? value : defValue;
    }

    /**
     * Function to set a byte option as hex string.
     *
     * @param name  the name of the option to set.
     * @param value the desired value.
     */
    public void setByteOptionAsHex(String name, byte value) {
        setOption(name, Integer.toHexString(value));
    }

    /**
     * Function to set a int option as hex string.
     *
     * @param name  the name of the option to set.
     * @param value the desired value.
     */
    public void setIntOptionAsHex(String name, int value) {
        setOption(name, Integer.toHexString(value));
    }

    /**
     * Function to set a String option.
     *
     * @param name  the name of the option to set.
     * @param value the desired value.
     */
    public void setOption(String name, String value) {
        mapConfig.put(name, value);
    }

    /**
     * Function to set a boolean option.
     *
     * @param name  the name of the option to set.
     * @param value the desired value.
     */
    public void setOption(String name, boolean value) {
        setOption(name, (value) ? "True" : "False");
    }

    /**
     * Function to set an int option.
     *
     * @param name  the name of the option to set.
     * @param value the desired value.
     */
    public void setOption(String name, int value) {
        setOption(name, Integer.toString(value));
    }

    /**
     * Function to read an Hex option as a byte.
     *
     * @param name     the name of the option to read.
     * @param defValue the value returned if the option is not found.
     * @return the value of the option of the requested name, or the default value.
     */
    public byte getHexOptionAsByte(String name, byte defValue) {
        byte value;
        try {
            value = Byte.parseByte(getOption(name), 16);
        } catch (NumberFormatException nfe) {
            value = defValue;
        }
        return value;
    }

    /**
     * Function to read an Hex option as a byte.
     *
     * @param name the name of the option to read.
     * @return the value of the option with the quested name, or 0x00b.
     */
    public byte getHexOptionAsByte(String name) {
        return getHexOptionAsByte(name, (byte) 0);
    }

    /**
     * Function to read an Hex option as an int.
     *
     * @param name the name of the option to read.
     * @return the value of the option with the requested name, or 0.
     */
    public int getHexOptionAsInt(String name) {
        int value;
        try {
            value = Integer.parseInt(getOption(name), 16);
        } catch (NumberFormatException nfe) {
            value = 0;
        }
        return value;
    }

    /**
     * Function to read a String option.
     *
     * @param name the name of the option to read.
     * @return the value of the option with the requested name, or a void String.
     */
    public String getOption(String name) {
        return getOption(name, "");
    }

    /**
     * Function to read a String option.
     *
     * @param name     the name of the option to read.
     * @param defValue the value returned when the option is not found.
     * @return the value of the option with the requested name, or the default value.
     */
    public String getOption(String name, String defValue) {
        String value = mapConfig.get(name);
        if (value == null) {
            value = defValue;
        }
        return value;
    }

    /**
     * Function to read a boolean option.
     *
     * @param name the name of the option to read.
     * @return the value of the option with the requested name.
     */
    public boolean getOptionAsBool(String name) {
        return getOption(name).toUpperCase().equals("TRUE");
    }

    /**
     * Function to read an int option.
     *
     * @param name the name of the option to read.
     * @return the value of the option with the requested name.
     */
    public int getOptionAsInt(String name) {
        return getOptionAsInt(name, 0);
    }

    /**
     * Function to read an int option.
     *
     * @param name     the name of the option to read.
     * @param defValue the value returned when the option is not found.
     * @return the value of the option with the requested name, or the default value.
     */
    public int getOptionAsInt(String name, int defValue) {
        int value;
        try {
            value = Integer.parseInt(getOption(name));
        } catch (NumberFormatException nfe) {
            value = defValue;
        }
        return value;
    }
}
