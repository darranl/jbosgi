/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
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

package org.jboss.test.osgi.example.xservice;

import java.io.IOException;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;

import org.jboss.modules.ModuleIdentifier;
import org.jboss.msc.service.ServiceController.State;
import org.jboss.msc.service.ServiceName;
import org.jboss.osgi.framework.BundleManagerMBean;
import org.jboss.osgi.jmx.MBeanProxy;
import org.jboss.osgi.jmx.ObjectNameFactory;
import org.jboss.osgi.testing.OSGiRemoteRuntime;
import org.jboss.osgi.testing.OSGiRuntimeTest;
import org.junit.Before;

/**
 * Abstract base for XService testing.
 *
 * @author Thomas.Diesler@jboss.com
 * @since 14-Oct-2010
 */
public abstract class AbstractXServiceTestCase extends OSGiRuntimeTest {
    private static ObjectName SERVICE_CONTAINER_OBJECTNAME = ObjectNameFactory.create("jboss.msc:type=container,name=jboss-as");
    private BundleManagerMBean bundleManager;
    private OSGiRemoteRuntime runtime;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        runtime = (OSGiRemoteRuntime) getRuntime();
    }

    public OSGiRemoteRuntime getRemoteRuntime() {
        return runtime;
    }

    protected long registerModule(ModuleIdentifier moduleId) throws Exception {
        return getBundleManager().installBundle(moduleId);
    }

    private BundleManagerMBean getBundleManager() throws IOException, InstanceNotFoundException {
        if (bundleManager == null) {
            MBeanServerConnection mbeanServer = runtime.getMBeanServer();
            ObjectName oname = ObjectNameFactory.create(BundleManagerMBean.OBJECT_NAME);
            if (isRegisteredWithTimeout(mbeanServer, oname, 10000) == false)
                throw new InstanceNotFoundException(oname.getCanonicalName());

            bundleManager = MBeanProxy.get(mbeanServer, oname, BundleManagerMBean.class);
        }
        return bundleManager;
    }

    protected State getServiceState(ServiceName serviceName, State expState, long timeout) throws Exception {
        State state = getServiceStateInternal(serviceName);
        while ((state == null || state != expState) && timeout > 0) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                // ignore
            }
            state = getServiceStateInternal(serviceName);
            timeout -= 100;
        }
        return state;
    }

    private State getServiceStateInternal(ServiceName serviceName) throws Exception {
        MBeanServerConnection mbeanServer = runtime.getMBeanServer();
        Object[] params = new Object[] { serviceName.getCanonicalName() };
        String[] signature = new String[] { String.class.getName() };
        CompositeData data = (CompositeData) mbeanServer.invoke(SERVICE_CONTAINER_OBJECTNAME, "getServiceStatus", params, signature);
        if (data == null) {
            return null;
        }
        return State.valueOf((String) (data.get("stateName")));
    }

    private boolean isRegisteredWithTimeout(MBeanServerConnection mbeanServer, ObjectName objectName, int timeout) throws IOException {
        boolean registered = mbeanServer.isRegistered(objectName);
        while (registered == false && timeout > 0) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                // ignore
            }
            registered = mbeanServer.isRegistered(objectName);
            timeout -= 100;
        }
        return registered;
    }
}
