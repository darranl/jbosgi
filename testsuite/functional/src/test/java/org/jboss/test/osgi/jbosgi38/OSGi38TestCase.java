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
package org.jboss.test.osgi.jbosgi38;

import static org.junit.Assert.fail;

import java.io.InputStream;

import org.jboss.osgi.spi.capability.CompendiumCapability;
import org.jboss.osgi.testing.OSGiBundle;
import org.jboss.osgi.testing.OSGiManifestBuilder;
import org.jboss.osgi.testing.OSGiRuntime;
import org.jboss.osgi.testing.OSGiRuntimeTest;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.test.osgi.jbosgi38.bundleA.OSGi38ActivatorA;
import org.jboss.test.osgi.jbosgi38.bundleA.ServiceA;
import org.jboss.test.osgi.jbosgi38.bundleB.OSGi38ActivatorB;
import org.jboss.test.osgi.jbosgi38.bundleB.ServiceB;
import org.jboss.test.osgi.jbosgi38.bundleX.SomePojo;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleException;
import org.osgi.util.tracker.ServiceTracker;

/**
 * [JBOSGI-38] Investigate bundle install/start behaviour with random deployment order
 * 
 * https://jira.jboss.org/jira/browse/JBOSGI-38
 * 
 * Bundle A depends on B and X Bundle B depends on X
 * 
 * [TODO] Use default runtime for in container testing
 * 
 * @author thomas.diesler@jboss.com
 * @since 02-Mar-2009
 */
public class OSGi38TestCase extends OSGiRuntimeTest {
    @Test
    public void testInstallStartX() throws Exception {
        OSGiRuntime runtime = createEmbeddedRuntime();
        try {
            runtime.addCapability(new CompendiumCapability());

            OSGiBundle bundleX = runtime.installBundle(getBundleX());
            assertBundleState(Bundle.INSTALLED, bundleX.getState());

            bundleX.start();
            assertBundleState(Bundle.ACTIVE, bundleX.getState());

            bundleX.uninstall();
            assertBundleState(Bundle.UNINSTALLED, bundleX.getState());
        } finally {
            runtime.shutdown();
        }
    }

    /*
     * Install X, B
     */
    @Test
    public void testInstallXBeforeB() throws Exception {
        OSGiRuntime runtime = createEmbeddedRuntime();
        try {
            runtime.addCapability(new CompendiumCapability());

            OSGiBundle bundleX = runtime.installBundle(getBundleX());
            assertBundleState(Bundle.INSTALLED, bundleX.getState());

            OSGiBundle bundleB = runtime.installBundle(getBundleB());
            assertBundleState(Bundle.INSTALLED, bundleB.getState());

            bundleB.start();
            assertBundleState(Bundle.RESOLVED, bundleX.getState());
            assertBundleState(Bundle.ACTIVE, bundleB.getState());

            bundleB.uninstall();
            bundleX.uninstall();
        } finally {
            runtime.shutdown();
        }
    }

    /*
     * Install X, B, A
     */
    @Test
    public void testInstallBBeforeA() throws Exception {
        OSGiRuntime runtime = createEmbeddedRuntime();
        try {
            runtime.addCapability(new CompendiumCapability());

            OSGiBundle bundleX = runtime.installBundle(getBundleX());
            assertBundleState(Bundle.INSTALLED, bundleX.getState());

            OSGiBundle bundleB = runtime.installBundle(getBundleB());
            assertBundleState(Bundle.INSTALLED, bundleB.getState());

            OSGiBundle bundleA = runtime.installBundle(getBundleA());
            assertBundleState(Bundle.INSTALLED, bundleA.getState());

            bundleA.start();
            assertBundleState(Bundle.RESOLVED, bundleX.getState());
            assertBundleState(Bundle.RESOLVED, bundleB.getState());
            assertBundleState(Bundle.ACTIVE, bundleA.getState());

            bundleA.uninstall();
            bundleB.uninstall();
            bundleX.uninstall();
        } finally {
            runtime.shutdown();
        }
    }

    /*
     * Install B, X
     */
    @Test
    public void testInstallBBeforeX() throws Exception {
        OSGiRuntime runtime = createEmbeddedRuntime();
        try {
            runtime.addCapability(new CompendiumCapability());

            OSGiBundle bundleB = runtime.installBundle(getBundleB());
            assertBundleState(Bundle.INSTALLED, bundleB.getState());

            try {
                bundleB.start();
                fail("Unresolved constraint expected");
            } catch (BundleException ex) {
                // expected
            }

            OSGiBundle bundleX = runtime.installBundle(getBundleX());
            assertBundleState(Bundle.INSTALLED, bundleX.getState());

            bundleB.start();
            assertBundleState(Bundle.RESOLVED, bundleX.getState());
            assertBundleState(Bundle.ACTIVE, bundleB.getState());

            bundleB.uninstall();
            bundleX.uninstall();
        } finally {
            runtime.shutdown();
        }
    }

    /*
     * Install A, B, X
     */
    @Test
    public void testInstallABeforeB() throws Exception {
        OSGiRuntime runtime = createEmbeddedRuntime();
        try {
            runtime.addCapability(new CompendiumCapability());

            OSGiBundle bundleA = runtime.installBundle(getBundleA());
            assertBundleState(Bundle.INSTALLED, bundleA.getState());

            OSGiBundle bundleB = runtime.installBundle(getBundleB());
            assertBundleState(Bundle.INSTALLED, bundleB.getState());

            try {
                bundleB.start();
                fail("Unresolved constraint expected");
            } catch (BundleException ex) {
                // expected
            }

            OSGiBundle bundleX = runtime.installBundle(getBundleX());
            assertBundleState(Bundle.INSTALLED, bundleX.getState());

            bundleB.start();
            assertBundleState(Bundle.RESOLVED, bundleX.getState());
            assertBundleState(Bundle.ACTIVE, bundleB.getState());

            bundleA.start();
            assertBundleState(Bundle.ACTIVE, bundleA.getState());

            bundleA.uninstall();
            bundleB.uninstall();
            bundleX.uninstall();
        } finally {
            runtime.shutdown();
        }
    }

    /*
     * Uninstall X, B stays active
     */
    @Test
    public void testUninstallX() throws Exception {
        OSGiRuntime runtime = createEmbeddedRuntime();
        try {
            runtime.addCapability(new CompendiumCapability());

            OSGiBundle bundleX = runtime.installBundle(getBundleX());
            assertBundleState(Bundle.INSTALLED, bundleX.getState());

            OSGiBundle bundleB = runtime.installBundle(getBundleB());
            assertBundleState(Bundle.INSTALLED, bundleB.getState());

            bundleB.start();
            assertBundleState(Bundle.RESOLVED, bundleX.getState());
            assertBundleState(Bundle.ACTIVE, bundleB.getState());

            bundleX.uninstall();
            assertBundleState(Bundle.UNINSTALLED, bundleX.getState());
            assertBundleState(Bundle.ACTIVE, bundleB.getState());

            bundleB.uninstall();
            assertBundleState(Bundle.UNINSTALLED, bundleB.getState());
        } finally {
            runtime.shutdown();
        }
    }

    private JavaArchive getBundleA() {
        // Bundle-SymbolicName: jbosgi38-bundleA
        // Bundle-Activator: org.jboss.test.osgi.jbosgi38.bundleA.OSGi38ActivatorA
        // Export-Package: org.jboss.test.osgi.jbosgi38.bundleA
        // Import-Package: org.jboss.test.osgi.jbosgi38.bundleB, org.jboss.test.osgi.jbosgi38.bundleX
        final JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "jbosgi38-bundleA");
        archive.addClasses(OSGi38ActivatorA.class, ServiceA.class);
        archive.setManifest(new Asset() {
            public InputStream openStream() {
                OSGiManifestBuilder builder = OSGiManifestBuilder.newInstance();
                builder.addBundleManifestVersion(2);
                builder.addBundleSymbolicName(archive.getName());
                builder.addBundleActivator(OSGi38ActivatorA.class);
                builder.addExportPackages("org.jboss.test.osgi.jbosgi38.bundleA");
                builder.addImportPackages("org.jboss.test.osgi.jbosgi38.bundleB", "org.jboss.test.osgi.jbosgi38.bundleX");
                builder.addImportPackages(BundleActivator.class, ServiceTracker.class);
                return builder.openStream();
            }
        });
        return archive;
    }

    private JavaArchive getBundleB() {
        // Bundle-SymbolicName: jbosgi38-bundleB
        // Bundle-Activator: org.jboss.test.osgi.jbosgi38.bundleB.OSGi38ActivatorB
        // Export-Package: org.jboss.test.osgi.jbosgi38.bundleB
        // Import-Package: org.jboss.test.osgi.jbosgi38.bundleX
        final JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "jbosgi38-bundleB");
        archive.addClasses(OSGi38ActivatorB.class, ServiceB.class);
        archive.setManifest(new Asset() {
            public InputStream openStream() {
                OSGiManifestBuilder builder = OSGiManifestBuilder.newInstance();
                builder.addBundleManifestVersion(2);
                builder.addBundleSymbolicName(archive.getName());
                builder.addBundleActivator(OSGi38ActivatorB.class);
                builder.addExportPackages("org.jboss.test.osgi.jbosgi38.bundleB");
                builder.addImportPackages("org.jboss.test.osgi.jbosgi38.bundleX");
                builder.addImportPackages(BundleActivator.class);
                return builder.openStream();
            }
        });
        return archive;
    }

    private JavaArchive getBundleX() {
        // Bundle-SymbolicName: jbosgi38-bundleX
        // Export-Package: org.jboss.test.osgi.jbosgi38.bundleX
        final JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "jbosgi38-bundleX");
        archive.addClasses(SomePojo.class);
        archive.setManifest(new Asset() {
            public InputStream openStream() {
                OSGiManifestBuilder builder = OSGiManifestBuilder.newInstance();
                builder.addBundleManifestVersion(2);
                builder.addBundleSymbolicName(archive.getName());
                builder.addExportPackages("org.jboss.test.osgi.jbosgi38.bundleX");
                return builder.openStream();
            }
        });
        return archive;
    }
}