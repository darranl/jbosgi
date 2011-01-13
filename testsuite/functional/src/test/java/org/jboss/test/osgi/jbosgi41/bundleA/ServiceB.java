/*
 * JBoss, Home of Professional Open Source
 * Copyright 2005, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.test.osgi.jbosgi41.bundleA;

import java.util.Dictionary;
import java.util.Hashtable;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.osgi.util.tracker.ServiceTracker;

/**
 * ServiceB is a ManagedService
 * 
 * @author thomas.diesler@jboss.com
 * @since 05-Mar-2009
 */
public class ServiceB implements ManagedService {
    private ConfigurationAdmin configAdmin;

    ServiceB(BundleContext context) {
        ServiceTracker tracker = new ServiceTracker(context, ConfigurationAdmin.class.getName(), null) {
            @Override
            public Object addingService(ServiceReference sref) {
                configAdmin = (ConfigurationAdmin) super.addingService(sref);
                return configAdmin;
            }
        };
        tracker.open();
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void updateConfig(String key, String value) {
        if (configAdmin != null) {
            try {
                Configuration config = configAdmin.getConfiguration(ServiceB.class.getName());
                Dictionary props = config.getProperties();

                if (props == null)
                    props = new Hashtable<String, String>();

                props.put(key, value);

                config.update(props);

                configAdmin.listConfigurations(null);
            } catch (Exception ex) {
                throw new RuntimeException("Cannot access ConfigurationAdmin", ex);
            }
        }
    }

    @SuppressWarnings("rawtypes")
    public void updated(Dictionary props) throws ConfigurationException {
    }
}
