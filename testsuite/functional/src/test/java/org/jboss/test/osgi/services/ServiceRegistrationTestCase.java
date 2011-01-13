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
package org.jboss.test.osgi.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.jboss.osgi.testing.OSGiFrameworkTest;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;

/**
 * Test service related functionality.
 * 
 * @author thomas.diesler@jboss.com
 * @since 20-Mar-2010
 */
public class ServiceRegistrationTestCase extends OSGiFrameworkTest {
    @Test
    public void testUsingBundles() throws Exception {
        Runnable exp = new Runnable() {
            public void run() {
            }
        };

        BundleContext context = getFramework().getBundleContext();
        ServiceRegistration sreg = context.registerService(Runnable.class.getName(), exp, null);
        ServiceReference sref = sreg.getReference();

        Bundle[] users = sref.getUsingBundles();
        assertNull("Null users", users);

        Runnable was = (Runnable) context.getService(sref);
        users = sref.getUsingBundles();
        assertSame(exp, was);
        assertEquals(1, users.length);
        assertEquals(context.getBundle(), users[0]);

        was = (Runnable) context.getService(sref);
        users = sref.getUsingBundles();
        assertSame(exp, was);
        assertEquals(1, users.length);
        assertEquals(context.getBundle(), users[0]);
    }

    @Test
    public void testServiceFactoryUsingBundles() throws Exception {
        final boolean[] allGood = new boolean[2];
        ServiceFactory factory = new ServiceFactory() {
            @Override
            public Object getService(Bundle bundle, ServiceRegistration sreg) {
                ServiceReference sref = sreg.getReference();
                Bundle[] users = sref.getUsingBundles();
                assertNotNull("Users not null", users);
                assertEquals(1, users.length);
                assertEquals(bundle, users[0]);
                allGood[0] = true;
                return new Runnable() {
                    public void run() {
                    }
                };
            }

            @Override
            public void ungetService(Bundle bundle, ServiceRegistration sreg, Object service) {
                ServiceReference sref = sreg.getReference();
                Bundle[] users = sref.getUsingBundles();

                System.out.println("FIXME [JBOSGI-305] Clarify ServiceReference.getUsingBundles() in ServiceFactory.ungetService()");
                if ("equinox".equals(getFrameworkName()) == false) {
                    assertNotNull("Users not null", users);
                    assertEquals(1, users.length);
                    assertEquals(bundle, users[0]);
                }

                allGood[1] = true;
            }
        };
        BundleContext context = getFramework().getBundleContext();
        ServiceRegistration sreg = context.registerService(Runnable.class.getName(), factory, null);
        ServiceReference sref = sreg.getReference();

        Bundle[] users = sref.getUsingBundles();
        assertNull("Null users", users);

        Runnable was = (Runnable) context.getService(sref);
        assertNotNull("Service not null", was);
        users = sref.getUsingBundles();
        assertNotNull("Users not null", users);
        assertEquals(1, users.length);
        assertEquals(context.getBundle(), users[0]);
        assertTrue("getService good", allGood[0]);

        sreg.unregister();

        was = (Runnable) context.getService(sref);
        assertNull("Service null", was);
        assertTrue("ungetService good", allGood[1]);
    }
}