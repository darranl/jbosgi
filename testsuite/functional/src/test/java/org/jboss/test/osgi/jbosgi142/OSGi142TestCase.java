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
package org.jboss.test.osgi.jbosgi142;

import org.jboss.osgi.testing.OSGiFrameworkTest;
import org.jboss.test.osgi.jbosgi142.bundleA.BeanA;
import org.jboss.test.osgi.jbosgi142.bundleB.BeanB;
import org.jboss.test.osgi.jbosgi142.bundleX.BeanX;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

/**
 * [JBOSGI-142] Investigate classloading space
 * 
 * https://jira.jboss.org/jira/browse/JBOSGI-142
 * 
 * A imports X B imports X
 * 
 * Can X load a class from A or B? Can A load a class from B and vice versa?
 * 
 * @author thomas.diesler@jboss.com
 * @since 28-Aug-2009
 */
public class OSGi142TestCase extends OSGiFrameworkTest {
    @Test
    public void testLoadClass() throws Exception {
        BundleContext sysContext = getFramework().getBundleContext();
        Bundle bundleX = sysContext.installBundle(getTestArchiveURL("jbosgi142-bundleX.jar").toExternalForm());
        bundleX.start();

        assertLoadClass(bundleX, BeanX.class.getName());

        Bundle bundleA = sysContext.installBundle(getTestArchiveURL("jbosgi142-bundleA.jar").toExternalForm());
        bundleA.start();

        assertLoadClass(bundleA, BeanA.class.getName());

        Bundle bundleB = sysContext.installBundle(getTestArchiveURL("jbosgi142-bundleB.jar").toExternalForm());
        bundleB.start();

        assertLoadClass(bundleB, BeanB.class.getName());

        assertLoadClass(bundleA, BeanX.class.getName());
        assertLoadClass(bundleB, BeanX.class.getName());

        assertLoadClassFail(bundleX, BeanA.class.getName());
        assertLoadClassFail(bundleX, BeanB.class.getName());

        assertLoadClassFail(bundleA, BeanB.class.getName());
        assertLoadClassFail(bundleB, BeanA.class.getName());
    }
}