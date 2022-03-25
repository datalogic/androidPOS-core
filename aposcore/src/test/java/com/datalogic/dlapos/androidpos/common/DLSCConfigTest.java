package com.datalogic.dlapos.androidpos.common;

import com.datalogic.dlapos.commons.support.APosException;
import com.datalogic.dlapos.confighelper.configurations.accessor.ProfileManager;

import org.junit.Test;
import org.mockito.Mock;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DLSCConfigTest {

    @Mock
    private final ProfileManager _profileManager = mock(ProfileManager.class);

    @Test(expected = IllegalArgumentException.class)
    public void loadNullProfile() throws APosException {
        TestConfig config = new TestConfig();
        config.loadConfiguration("TEST", null);
    }

    @Test
    public void loadUnknownProfile() throws APosException {
        when(_profileManager.getConfigurationForProfileId("TEST")).thenReturn(null);
        TestConfig config = new TestConfig();
        assertThat(config.loadConfiguration("TEST", _profileManager)).isFalse();
    }

    @Test
    public void getHexOptionsAsByteFailParse(){
        TestConfig config = new TestConfig();
        config.setOption("TEST","BAH");
        assertThat(config.getHexOptionAsByte("TEST",(byte)1)).isEqualTo((byte)1);
    }

    @Test
    public void getHexOptionAsIntFailsParse(){
        TestConfig config = new TestConfig();
        config.setOption("TEST","BAH");
        assertThat(config.getHexOptionAsInt("TEST")).isEqualTo(0);
    }

    @Test
    public void getOptionNullString(){
        TestConfig config = new TestConfig();
        config.setOption("TEST",null);
        assertThat(config.getOption("TEST","DEF")).isEqualTo("DEF");
    }

    @Test
    public void getOptionAsIntFailParse(){
        TestConfig config = new TestConfig();
        config.setOption("TEST","BAH");
        assertThat(config.getOptionAsInt("TEST",3)).isEqualTo(3);
    }

    public static class TestConfig extends DLSCConfig {

    }

}