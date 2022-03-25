package com.datalogic.dlapos.androidpos.utils;

import com.datalogic.dlapos.androidpos.common.DLSProperties;
import com.datalogic.dlapos.confighelper.DLAPosConfigHelper;
import com.datalogic.dlapos.confighelper.configurations.accessor.ProfileManager;

import java.lang.reflect.Field;

import static org.mockito.Mockito.when;

public class MockUtils {
    public static void mockConfigHelperSingleton(DLAPosConfigHelper dlaPosConfigHelper, ProfileManager profileManager) {
        try {
            Field instance = DLAPosConfigHelper.class.getDeclaredField("_instance");
            instance.setAccessible(true);
            instance.set(instance, dlaPosConfigHelper);
            when(dlaPosConfigHelper.getProfileManager()).thenReturn(profileManager);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public static void cleanConfigHelper() {
        try {
            Field instance = DLAPosConfigHelper.class.getDeclaredField("_instance");
            instance.setAccessible(true);
            instance.set(null, null);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public static void mockDLSPropertiesSingleton(DLSProperties properties) {
        try {
            Field instance = DLSProperties.class.getDeclaredField("sm_instance");
            instance.setAccessible(true);
            instance.set(instance, properties);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public static void cleanDLSProperties() {
        try {
            Field instance = DLSProperties.class.getDeclaredField("sm_instance");
            instance.setAccessible(true);
            instance.set(null, null);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
