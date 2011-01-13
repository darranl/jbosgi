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

package org.jboss.test.osgi.jbosgi298;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.jboss.logging.Logger;
import org.jboss.osgi.jmx.JMXCapability;
import org.jboss.osgi.spi.capability.LogServiceCapability;
import org.jboss.osgi.testing.OSGiRuntime;
import org.jboss.osgi.testing.OSGiRuntimeTest;
import org.junit.Before;
import org.junit.Test;
import org.osgi.jmx.framework.BundleStateMBean;
import org.osgi.jmx.framework.FrameworkMBean;
import org.osgi.jmx.framework.ServiceStateMBean;

/**
 * [JBOSGI-298] InstanceAlreadyExistsException: osgi.core:type=framework,version=1.5
 * 
 * https://jira.jboss.org/jira/browse/JBOSGI-298
 * 
 * @author thomas.diesler@jboss.com
 * @since 05-Mar-2010
 */
public class OSGi298TestCase extends OSGiRuntimeTest {
    // Provide logging
    private static final Logger log = Logger.getLogger(OSGi298TestCase.class);

    @Before
    public void setUp() throws Exception {
        super.setUp();
        OSGiRuntime runtime = createEmbeddedRuntime();
        runtime.addCapability(new LogServiceCapability());
    }

    @Test
    public void testJMXCapability() throws Exception {
        JMXCapability capability = new JMXCapability();
        getRuntime().addCapability(capability);

        assertTrue("FrameworkMBean registered", isRegistered(FrameworkMBean.OBJECTNAME));
        assertTrue("BundleStateMBean registered", isRegistered(BundleStateMBean.OBJECTNAME));
        assertTrue("ServiceStateMBean registered", isRegistered(ServiceStateMBean.OBJECTNAME));

        getRuntime().removeCapability(capability);

        assertFalse("FrameworkMBean not registered", isRegistered(FrameworkMBean.OBJECTNAME));
        assertFalse("BundleStateMBean not registered", isRegistered(BundleStateMBean.OBJECTNAME));
        assertFalse("ServiceStateMBean not registered", isRegistered(ServiceStateMBean.OBJECTNAME));
    }

    /*
     * @Test public void testJMXBundles() throws Exception { OSGiBundle jbossJMX =
     * getRuntime().installBundle("bundles/jboss-osgi-jmx.jar"); OSGiBundle ariesJMX =
     * getRuntime().installBundle("bundles/org.apache.aries.jmx.jar");
     * 
     * jbossJMX.start(); ariesJMX.start();
     * 
     * Thread.sleep(2000);
     * 
     * ariesJMX.stop();
     * 
     * Thread.sleep(2000); }
     */

    private boolean isRegistered(String oname) throws MalformedObjectNameException {
        MBeanServer server = (MBeanServer) getRuntime().getMBeanServer();
        boolean registered = server.isRegistered(ObjectName.getInstance(oname));
        log.debug(oname + " registered: " + registered);
        return registered;
    }
}