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
package org.jboss.test.osgi.jbosgi151;

import static org.junit.Assert.assertEquals;

import org.jboss.osgi.testing.OSGiFrameworkTest;
import org.jboss.test.osgi.jbosgi151.bundleA.BeanA;
import org.jboss.test.osgi.jbosgi151.bundleB.BeanB;
import org.junit.After;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

/**
 * [JBOSGI-151] Cannot resolve circular dependencies
 * 
 * https://jira.jboss.org/jira/browse/JBOSGI-151
 * 
 * BundleA exports A imports B
 * 
 * BundleB exports B imports A
 * 
 * BundleC exports A, B imports A
 * 
 * BundleD exports A, imports A, B
 * 
 * @author thomas.diesler@jboss.com
 * @since 07-Sep-2009
 */
public class OSGi151TestCase extends OSGiFrameworkTest {
    @After
    public void tearDown() throws Exception {
        shutdownFramework();
    }

    @Test
    public void testCircularNoSelfDependency() throws Exception {
        BundleContext sysContext = getFramework().getBundleContext();

        // Bundle-SymbolicName: jbosgi151-bundleA
        // Export-Package: org.jboss.test.osgi.jbosgi151.bundleA
        // Import-Package: org.jboss.test.osgi.jbosgi151.bundleB
        Bundle bundleA = sysContext.installBundle(getTestArchiveURL("jbosgi151-bundleA.jar").toExternalForm());
        assertBundleState(Bundle.INSTALLED, bundleA.getState());

        // Bundle-SymbolicName: jbosgi151-bundleB
        // Export-Package: org.jboss.test.osgi.jbosgi151.bundleB
        // Import-Package: org.jboss.test.osgi.jbosgi151.bundleA
        Bundle bundleB = sysContext.installBundle(getTestArchiveURL("jbosgi151-bundleB.jar").toExternalForm());
        assertBundleState(Bundle.INSTALLED, bundleB.getState());

        bundleB.start();
        assertBundleState(Bundle.ACTIVE, bundleB.getState());
        assertBundleState(Bundle.RESOLVED, bundleA.getState());

        Class<?> classAA = bundleA.loadClass(BeanA.class.getName());
        Class<?> classAB = bundleB.loadClass(BeanA.class.getName());
        assertEquals("Class for BeanA", classAA, classAB);

        Class<?> classBA = bundleA.loadClass(BeanB.class.getName());
        Class<?> classBB = bundleB.loadClass(BeanB.class.getName());
        assertEquals("Class for BeanB", classBA, classBB);

        bundleB.uninstall();
        bundleA.uninstall();
    }

    @Test
    public void testCircularInstallCbeforeD() throws Exception {
        BundleContext sysContext = getFramework().getBundleContext();

        // Bundle-SymbolicName: jbosgi151-bundleC
        // Export-Package: org.jboss.test.osgi.jbosgi151.bundleA, org.jboss.test.osgi.jbosgi151.bundleB
        // Import-Package: org.jboss.test.osgi.jbosgi151.bundleA
        Bundle bundleC = sysContext.installBundle(getTestArchiveURL("jbosgi151-bundleC.jar").toExternalForm());
        assertBundleState(Bundle.INSTALLED, bundleC.getState());

        // Bundle-SymbolicName: jbosgi151-bundleD
        // Export-Package: org.jboss.test.osgi.jbosgi151.bundleA
        // Import-Package: org.jboss.test.osgi.jbosgi151.bundleA, org.jboss.test.osgi.jbosgi151.bundleB
        Bundle bundleD = sysContext.installBundle(getTestArchiveURL("jbosgi151-bundleD.jar").toExternalForm());
        assertBundleState(Bundle.INSTALLED, bundleD.getState());

        bundleD.start();
        assertBundleState(Bundle.ACTIVE, bundleD.getState());
        assertBundleState(Bundle.RESOLVED, bundleC.getState());

        Class<?> classBC = bundleC.loadClass(BeanB.class.getName());
        Class<?> classBD = bundleD.loadClass(BeanB.class.getName());
        assertEquals("Class for BeanB", classBC, classBD);

        Class<?> classAC = bundleC.loadClass(BeanA.class.getName());
        Class<?> classAD = bundleD.loadClass(BeanA.class.getName());
        assertEquals("Class for BeanA", classAC, classAD);

        bundleD.uninstall();
        bundleC.uninstall();
    }

    @Test
    public void testCircularInstallDbeforeC() throws Exception {
        BundleContext sysContext = getFramework().getBundleContext();

        // Bundle-SymbolicName: jbosgi151-bundleD
        // Export-Package: org.jboss.test.osgi.jbosgi151.bundleA
        // Import-Package: org.jboss.test.osgi.jbosgi151.bundleA, org.jboss.test.osgi.jbosgi151.bundleB
        Bundle bundleD = sysContext.installBundle(getTestArchiveURL("jbosgi151-bundleD.jar").toExternalForm());
        assertBundleState(Bundle.INSTALLED, bundleD.getState());

        // Bundle-SymbolicName: jbosgi151-bundleC
        // Export-Package: org.jboss.test.osgi.jbosgi151.bundleA, org.jboss.test.osgi.jbosgi151.bundleB
        // Import-Package: org.jboss.test.osgi.jbosgi151.bundleA
        Bundle bundleC = sysContext.installBundle(getTestArchiveURL("jbosgi151-bundleC.jar").toExternalForm());
        assertBundleState(Bundle.INSTALLED, bundleC.getState());

        bundleD.start();
        assertBundleState(Bundle.ACTIVE, bundleD.getState());
        assertBundleState(Bundle.RESOLVED, bundleC.getState());

        Class<?> classBC = bundleC.loadClass(BeanB.class.getName());
        Class<?> classBD = bundleD.loadClass(BeanB.class.getName());
        assertEquals("Class for BeanB", classBC, classBD);

        Class<?> classAC = bundleC.loadClass(BeanA.class.getName());
        Class<?> classAD = bundleD.loadClass(BeanA.class.getName());
        assertEquals("Class for BeanA", classAC, classAD);

        bundleD.uninstall();
        bundleC.uninstall();
    }
}