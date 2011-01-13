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
package org.jboss.test.osgi.example.simple;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.net.URL;

import org.jboss.osgi.testing.OSGiFrameworkTest;
import org.jboss.test.osgi.example.simple.bundle.SimpleService;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

/**
 * A test that deployes a bundle and verifies its state
 * 
 * @author thomas.diesler@jboss.com
 * @since 12-Feb-2009
 */
public class SimpleFrameworkTestCase extends OSGiFrameworkTest {
    @Test
    public void testSimpleBundle() throws Exception {
        // Get the bundle location
        URL url = getTestArchiveURL("example-simple.jar");

        // Install the Bundle
        BundleContext sysContext = getFramework().getBundleContext();
        Bundle bundle = sysContext.installBundle(url.toExternalForm());
        assertBundleState(Bundle.INSTALLED, bundle.getState());

        // Check that the BundleContext is still null
        BundleContext context = bundle.getBundleContext();
        assertNull("BundleContext null", context);

        // Start the bundle
        bundle.start();
        assertBundleState(Bundle.ACTIVE, bundle.getState());

        // Check that the BundleContext is not null
        context = bundle.getBundleContext();
        assertNotNull("BundleContext not null", context);

        // Get a service from the bundle's context
        ServiceReference sref = context.getServiceReference(SimpleService.class.getName());
        assertNotNull("ServiceReference not null", sref);
        Object service = context.getService(sref);
        assertNotNull("Service not null", service);

        // Get a service from the system context
        sref = sysContext.getServiceReference(SimpleService.class.getName());
        assertNotNull("ServiceReference not null", sref);
        service = context.getService(sref);
        assertNotNull("Service not null", service);

        // Stop the bundle
        bundle.stop();
        assertBundleState(Bundle.RESOLVED, bundle.getState());

        try {
            context.getServiceReference(SimpleService.class.getName());
            fail("Invalid BundleContext expected");
        } catch (IllegalStateException ex) {
            // expected
        }

        // Get a service from the system context
        sref = sysContext.getServiceReference(SimpleService.class.getName());
        assertNull("ServiceReference null", sref);

        // Uninstall the bundle
        bundle.uninstall();
        assertBundleState(Bundle.UNINSTALLED, bundle.getState());
    }
}