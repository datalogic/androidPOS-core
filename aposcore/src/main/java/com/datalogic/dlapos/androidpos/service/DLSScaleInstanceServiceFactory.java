package com.datalogic.dlapos.androidpos.service;

import android.content.Context;

import com.datalogic.dlapos.commons.service.APosServiceInstance;
import com.datalogic.dlapos.commons.support.APosException;
import com.datalogic.dlapos.confighelper.ServiceFactory;

/**
 * Factory for scale services.
 */
public class DLSScaleInstanceServiceFactory implements ServiceFactory {
    /**
     * Function to create a Service knowing its class full package name.
     *
     * @param context          the application context.
     * @param serviceClassName the full package name of the Service to create.
     * @return an instance of the required Service.
     * @throws APosException when it is not possible to instantiate the Service.
     */
    @Override
    public APosServiceInstance createService(Context context, String serviceClassName) throws APosException {
        try {
            Class<?> factoryClass = Class.forName(serviceClassName);
            return (APosServiceInstance) factoryClass.newInstance();
        } catch (Exception e) {
            throw new APosException("can not create service", e);
        }
    }
}
