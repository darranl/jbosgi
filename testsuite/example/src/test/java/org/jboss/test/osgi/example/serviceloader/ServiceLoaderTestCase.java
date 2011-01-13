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
package org.jboss.test.osgi.example.serviceloader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import org.jboss.osgi.serviceloader.ServiceLoaderCapability;
import org.jboss.osgi.spi.util.ServiceLoader;
import org.jboss.osgi.testing.OSGiBundle;
import org.jboss.osgi.testing.OSGiRuntimeTest;
import org.jboss.osgi.testing.OSGiServiceReference;
import org.jboss.test.osgi.example.serviceloader.service.AccountService;
import org.jboss.test.osgi.example.serviceloader.service.internal.AccountServiceImpl;
import org.junit.Test;
import org.osgi.framework.Bundle;

/**
 * A test that deployes a bundle and verifies its state
 * 
 * @author thomas.diesler@jboss.com
 * @since 26-Jan-2010
 */
public class ServiceLoaderTestCase extends OSGiRuntimeTest {
    @Test
    public void testTraditionalServiceLoaderAPI() throws Exception {
        // Use the traditional ServiceLoader API to get the service
        AccountService service = ServiceLoader.loadService(AccountService.class);
        assertNotNull("AccountService loaded", service);

        assertEquals(0, service.getBalance());
        assertEquals(100, service.credit(100));
        assertEquals(80, service.withdraw(20));

        try {
            service.withdraw(100);
            fail("Insuffient funds expected");
        } catch (RuntimeException e) {
            assertEquals(80, service.getBalance());
        }
    }

    @Test
    public void testOSGiServiceAPI() throws Exception {
        // Get the default runtime
        getRuntime().addCapability(new ServiceLoaderCapability());

        // Install the API bundle
        OSGiBundle apiBundle = getRuntime().installBundle("example-serviceloader-api.jar");

        // Install/Start the client bundle
        OSGiBundle implBundle = getRuntime().installBundle("example-serviceloader-impl.jar");
        implBundle.start();

        // Install/Start the client bundle
        OSGiBundle clientBundle = getRuntime().installBundle("example-serviceloader-client.jar");
        clientBundle.start();

        OSGiServiceReference[] srefs = getRuntime().getServiceReferences(AccountService.class.getName(), "(service.vendor=JBoss*)");
        assertNotNull("AccountService not null", srefs);
        assertEquals("One AccountService available", 1, srefs.length);

        assertBundleState(Bundle.ACTIVE, implBundle.getState());
        assertBundleState(Bundle.ACTIVE, clientBundle.getState());

        OSGiBundle apiProvider = clientBundle.loadClass(AccountService.class.getName());
        assertEquals(apiBundle, apiProvider);

        try {
            // The client cannot access the implementation class directly
            clientBundle.loadClass(AccountServiceImpl.class.getName());
            fail("ClassNotFoundException expected");
        } catch (ClassNotFoundException ex) {
            // expected
        }

        // Stopping the implementation bundle should unregister the service
        implBundle.stop();

        OSGiServiceReference sref = getRuntime().getServiceReference(AccountService.class.getName());
        assertNull("AccountService not available", sref);
    }
}