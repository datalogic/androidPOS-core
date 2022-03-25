package com.datalogic.dlapos.androidpos.service;

import android.content.Context;

import com.datalogic.dlapos.commons.support.APosException;

import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;

public class DLSScaleInstanceServiceFactoryTest {

    @Test
    public void createService() throws APosException {
        Context context = mock(Context.class);
        DLSScaleInstanceServiceFactory factory = new DLSScaleInstanceServiceFactory();
        assertThat(factory.createService(context, DLSBaseService.class.getCanonicalName()) instanceof DLSBaseService).isTrue();
    }

    @Test(expected = APosException.class)
    public void createServiceWrongClass() throws APosException {
        Context context = mock(Context.class);
        DLSScaleInstanceServiceFactory factory = new DLSScaleInstanceServiceFactory();
        factory.createService(context, EnableDisablePoll.class.getCanonicalName());
    }
}