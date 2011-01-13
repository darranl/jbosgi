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
package org.jboss.test.osgi.jbosgi287;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.jboss.osgi.testing.OSGiBundle;
import org.jboss.osgi.testing.OSGiRuntimeTest;
import org.jboss.test.osgi.jbosgi287.bundleA.OSGi287BeanA;
import org.jboss.test.osgi.jbosgi287.bundleB.OSGi287BeanB;
import org.junit.Test;
import org.osgi.framework.Bundle;

/**
 * [JBOSGI-287] Optional import loaded from system classloader
 * 
 * https://jira.jboss.org/jira/browse/JBOSGI-287
 * 
 * @author thomas.diesler@jboss.com
 * @since 01-Feb-2010
 */
public class OSGi287TestCase extends OSGiRuntimeTest {
    @Test
    public void testUnresolvedOptionalImport() throws Exception {
        // Bundle-SymbolicName: jbosgi287-bundleA
        // Export-Package: org.jboss.test.osgi.jbosgi287.bundleA
        // Import-Package: org.jboss.test.osgi.jbosgi287.bundleB;resolution:=optional
        OSGiBundle bundleA = getRuntime().installBundle("jbosgi287-bundleA.jar");

        OSGiBundle exporterA = bundleA.loadClass(OSGi287BeanA.class.getName());
        assertEquals(Bundle.RESOLVED, bundleA.getState());
        assertEquals(bundleA, exporterA);

        try {
            bundleA.loadClass(OSGi287BeanB.class.getName());
            fail("ClassNotFoundException expected");
        } catch (ClassNotFoundException ex) {
            // expected
        }
        bundleA.uninstall();
    }

    @Test
    public void testResolvedOptionalImport() throws Exception {
        // Bundle-SymbolicName: jbosgi287-bundleB
        // Export-Package: org.jboss.test.osgi.jbosgi287.bundleB
        OSGiBundle bundleB = getRuntime().installBundle("jbosgi287-bundleB.jar");

        // Bundle-SymbolicName: jbosgi287-bundleA
        // Export-Package: org.jboss.test.osgi.jbosgi287.bundleA
        // Import-Package: org.jboss.test.osgi.jbosgi287.bundleB;resolution:=optional
        OSGiBundle bundleA = getRuntime().installBundle("jbosgi287-bundleA.jar");

        OSGiBundle exporterB = bundleB.loadClass(OSGi287BeanB.class.getName());
        assertEquals(Bundle.RESOLVED, bundleB.getState());
        assertEquals(bundleB, exporterB);

        OSGiBundle exporterA = bundleA.loadClass(OSGi287BeanA.class.getName());
        assertEquals(Bundle.RESOLVED, bundleA.getState());
        assertEquals(bundleA, exporterA);

        // Load B through A
        exporterB = bundleA.loadClass(OSGi287BeanB.class.getName());
        assertEquals(bundleB, exporterB);

        bundleA.uninstall();
        bundleB.uninstall();
    }

    @Test
    public void testResolvedOptionalImportReverse() throws Exception {
        // Bundle-SymbolicName: jbosgi287-bundleA
        // Export-Package: org.jboss.test.osgi.jbosgi287.bundleA
        // Import-Package: org.jboss.test.osgi.jbosgi287.bundleB;resolution:=optional
        OSGiBundle bundleA = getRuntime().installBundle("jbosgi287-bundleA.jar");

        // Bundle-SymbolicName: jbosgi287-bundleB
        // Export-Package: org.jboss.test.osgi.jbosgi287.bundleB
        OSGiBundle bundleB = getRuntime().installBundle("jbosgi287-bundleB.jar");

        OSGiBundle exporterA = bundleA.loadClass(OSGi287BeanA.class.getName());
        assertEquals(Bundle.RESOLVED, bundleA.getState());
        assertEquals(bundleA, exporterA);

        OSGiBundle exporterB = bundleB.loadClass(OSGi287BeanB.class.getName());
        assertEquals(Bundle.RESOLVED, bundleB.getState());
        assertEquals(bundleB, exporterB);

        // Load B through A
        exporterB = bundleA.loadClass(OSGi287BeanB.class.getName());
        assertEquals(bundleB, exporterB);

        bundleA.uninstall();
        bundleB.uninstall();
    }

    @Test
    public void testMessagingAPI() throws Exception {
        // Bundle-SymbolicName: jbosgi287-bundleC
        // Export-Package: javax.xml.ws
        // Import-Package: javax.xml.ws
        OSGiBundle bundleA = getRuntime().installBundle("jbosgi287-bundleC.jar");

        OSGiBundle exporterA = bundleA.loadClass("javax.jms.JMSException");
        assertEquals(Bundle.RESOLVED, bundleA.getState());
        assertEquals(bundleA, exporterA);

        // Try to load a class that is not part of the bundle
        try {
            bundleA.loadClass("javax.jms.MessageProducer");
            fail("ClassNotFoundException expected");
        } catch (ClassNotFoundException ex) {
            // expected
        }

        bundleA.uninstall();
    }
}
