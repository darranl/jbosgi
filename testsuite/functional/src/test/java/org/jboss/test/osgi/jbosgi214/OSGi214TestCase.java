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
package org.jboss.test.osgi.jbosgi214;

import static org.junit.Assert.assertNotNull;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.jboss.osgi.framework.Constants;
import org.jboss.osgi.spi.util.ServiceLoader;
import org.jboss.osgi.testing.OSGiRuntimeTest;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;
import org.osgi.util.tracker.ServiceTracker;

/**
 * [JBOSGI-214] Cannot repeatedly register service bound to an interface from the system classpath
 * 
 * https://jira.jboss.org/jira/browse/JBOSGI-214
 * 
 * @author thomas.diesler@jboss.com
 * @since 03-Dec-2009
 */
public class OSGi214TestCase extends OSGiRuntimeTest {
    @Test
    public void testFirstRun() throws Exception {
        runSystemServiceTest();
    }

    @Test
    public void testSecondRun() throws Exception {
        runSystemServiceTest();
    }

    private void runSystemServiceTest() throws BundleException, InterruptedException {
        // Setup some package on the system classpath
        Map<String, String> props = new HashMap<String, String>();
        props.put(Constants.FRAMEWORK_STORAGE, "target/osgi-store");
        props.put(Constants.FRAMEWORK_STORAGE_CLEAN, Constants.FRAMEWORK_STORAGE_CLEAN_ONFIRSTINIT);
        props.put(Constants.FRAMEWORK_SYSTEMPACKAGES_EXTRA, SomeService.class.getPackage().getName());
        props.put("felix.bootdelegation.implicit", "false");

        // Bootstrap and start the framework
        FrameworkFactory factory = ServiceLoader.loadService(FrameworkFactory.class);
        Framework framework = factory.newFramework(props);
        framework.start();

        // Start the ServiceTracker
        BundleContext context = framework.getBundleContext();
        new SomeServiceTracker(context).open();

        try {
            // Install and start the test bundle
            URL bundleURL = getTestArchiveURL("jbosgi214-bundle.jar");
            Bundle bundle = context.installBundle(bundleURL.toExternalForm());
            bundle.start();

            // Verify that the service is there and can be cast to an interface
            // from the system classpath
            ServiceReference sref = context.getServiceReference(SomeService.class.getName());
            SomeService service = (SomeService) context.getService(sref);
            assertNotNull("Service not null", service);

            // Uninstall the test bundle
            bundle.uninstall();
        } finally {
            // Stop the framework
            framework.stop();
            framework.waitForStop(5000);
        }
    }

    class SomeServiceTracker extends ServiceTracker {
        public SomeServiceTracker(BundleContext context) {
            super(context, SomeService.class.getName(), null);
        }

        @Override
        public Object addingService(ServiceReference sref) {
            Object serviceObj = super.addingService(sref);
            // System.out.println("addingService: " + serviceObj);
            return (SomeService) serviceObj;
        }

        @Override
        public void removedService(ServiceReference reference, Object serviceObj) {
            // System.out.println("removedService: " + serviceObj);
            super.removedService(reference, serviceObj);
        }
    }
}