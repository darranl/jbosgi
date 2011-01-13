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
package org.jboss.test.osgi.jbosgi286;

import org.jboss.osgi.jaxb.JAXBCapability;
import org.jboss.osgi.testing.OSGiBundle;
import org.jboss.osgi.testing.OSGiRuntimeTest;
import org.junit.Test;
import org.osgi.framework.Bundle;

/**
 * [JBOSGI-286] Investigate classloading of javax.* classes
 * 
 * https://jira.jboss.org/jira/browse/JBOSGI-286
 * 
 * The activator verifies that the javax.xml.bind.JAXBContext is wired to jboss-osgi-jaxb
 * 
 * @author thomas.diesler@jboss.com
 * @since 01-Feb-2010
 */
public class OSGi286TestCase extends OSGiRuntimeTest {
    @Test
    public void testJAXBContextWiring() throws Exception {
        getRuntime().addCapability(new JAXBCapability());

        OSGiBundle bundleX = getRuntime().installBundle("jbosgi286-bundle.jar");
        assertBundleState(Bundle.INSTALLED, bundleX.getState());

        bundleX.start();
        assertBundleState(Bundle.ACTIVE, bundleX.getState());

        bundleX.uninstall();
        assertBundleState(Bundle.UNINSTALLED, bundleX.getState());
    }
}