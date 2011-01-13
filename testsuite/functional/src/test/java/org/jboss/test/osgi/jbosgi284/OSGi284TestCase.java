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
package org.jboss.test.osgi.jbosgi284;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.jboss.osgi.spi.framework.OSGiBootstrap;
import org.jboss.osgi.spi.framework.OSGiBootstrapProvider;
import org.jboss.osgi.testing.OSGiRuntimeTest;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Version;
import org.osgi.framework.launch.Framework;

/**
 * [JBOSGI-284] Investigate version numbering scheme
 * 
 * https://jira.jboss.org/jira/browse/JBOSGI-284
 * 
 * @author thomas.diesler@jboss.com
 * @since 01-Feb-2010
 */
public class OSGi284TestCase extends OSGiRuntimeTest {
    @Test
    public void testVersionParser() throws Exception {
        Version dotAlpha = Version.parseVersion("1.0.0.Alpha");
        assertEquals("1.0.0.Alpha", dotAlpha.toString());

        Version dotBeta = Version.parseVersion("1.0.0.Beta");
        assertEquals("1.0.0.Beta", dotBeta.toString());

        try {
            Version.parseVersion("1.0.0-Alpha");
            fail("NumberFormatException expected");
        } catch (NumberFormatException ex) {
            // expected
        }
    }

    @Test
    public void testInstallInvalidBundleVersion() throws Exception {
        OSGiBootstrapProvider provider = OSGiBootstrap.getBootstrapProvider();
        Framework framework = provider.getFramework();
        try {
            framework.start();

            BundleContext context = framework.getBundleContext();
            try {
                // Bundle-SymbolicName: jbosgi284-bundleA
                // Bundle-Version: 1.0.0-Alpha
                // Bundle-Activator: org.jboss.test.osgi.jbosgi284.bundleA.OSGi284ActivatorA
                // Import-Package: org.osgi.framework
                context.installBundle(getTestArchiveURL("jbosgi284-bundleA.jar").toExternalForm());
                fail("BundleException expected");
            } catch (BundleException ex) {
                // expected
            }
        } finally {
            framework.stop();
            framework.waitForStop(2000);
        }
    }

    @Test
    public void testExportInvalidPackageVersion() throws Exception {
        OSGiBootstrapProvider provider = OSGiBootstrap.getBootstrapProvider();
        Framework framework = provider.getFramework();
        try {
            framework.start();

            BundleContext context = framework.getBundleContext();
            try {
                // Bundle-SymbolicName: jbosgi284-bundleB
                // Export-Package: org.jboss.test.osgi.jbosgi284.bundleB;version="1.0.0-Alpha1"
                context.installBundle(getTestArchiveURL("jbosgi284-bundleB.jar").toExternalForm());
                fail("BundleException expected");
            } catch (BundleException ex) {
                // expected
            }
        } finally {
            framework.stop();
            framework.waitForStop(2000);
        }
    }
}