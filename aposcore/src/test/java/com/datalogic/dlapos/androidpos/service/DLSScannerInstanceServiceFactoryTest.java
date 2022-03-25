package com.datalogic.dlapos.androidpos.service;

import android.content.Context;

import com.datalogic.dlapos.commons.support.APosException;

import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;

public class DLSScannerInstanceServiceFactoryTest {

    @Test
    public void createService() throws APosException {
        Context context = mock(Context.class);
        DLSScannerInstanceServiceFactory factory = new DLSScannerInstanceServiceFactory();
        assertThat(factory.createService(context, DLSBaseService.class.getCanonicalName()) instanceof DLSBaseService).isTrue();
    }

    @Test(expected = APosException.class)
    public void createServiceWrongClass() throws APosException {
        Context context = mock(Context.class);
        DLSScannerInstanceServiceFactory factory = new DLSScannerInstanceServiceFactory();
        factory.createService(context, EnableDisablePoll.class.getCanonicalName());
    }
}