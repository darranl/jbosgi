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
package org.jboss.test.osgi.jbosgi39;

import static org.junit.Assert.fail;

import java.io.InputStream;

import org.jboss.osgi.jmx.JMXCapability;
import org.jboss.osgi.spi.capability.LogServiceCapability;
import org.jboss.osgi.testing.OSGiBundle;
import org.jboss.osgi.testing.OSGiManifestBuilder;
import org.jboss.osgi.testing.OSGiRuntime;
import org.jboss.osgi.testing.OSGiRuntimeTest;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.test.osgi.jbosgi39.bundleB.OSGi39ActivatorB;
import org.jboss.test.osgi.jbosgi39.bundleX.OSGi39BeanX;
import org.junit.BeforeClass;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleException;

/**
 * [JBOSGI-39] Bundle gets wired to an already uninstalled bundle
 * 
 * https://jira.jboss.org/jira/browse/JBOSGI-39
 * 
 * Bundle B depends on bundle X.
 * 
 * B ---> X
 * 
 * @author thomas.diesler@jboss.com
 * @since 04-Mar-2009
 */
public class OSGi39TestCase extends OSGiRuntimeTest {
    @BeforeClass
    public static void beforeClass() throws Exception {
        OSGiRuntime runtime = createDefaultRuntime();
        runtime.addCapability(new LogServiceCapability());
        runtime.addCapability(new JMXCapability());
    }

    @Test
    public void testVerifyUnresolved() throws Exception {
        OSGiBundle bundleB1 = getRuntime().installBundle(getBundleB1());
        assertBundleState(Bundle.INSTALLED, bundleB1.getState());

        try {
            bundleB1.start();
            fail("Unresolved constraint expected");
        } catch (BundleException ex) {
            // expected
        }

        OSGiBundle bundleX = getRuntime().installBundle(getBundleX());

        bundleB1.start();

        assertBundleState(Bundle.RESOLVED, bundleX.getState());
        assertBundleState(Bundle.ACTIVE, bundleB1.getState());

        bundleB1.uninstall();
        bundleX.uninstall();
    }

    /*
     * 4.3.11 Uninstalling Bundles
     * 
     * Once this method returns, the state of the OSGi Service Platform must be the same as if the bundle had never been
     * installed, unless:
     * 
     * - The uninstalled bundle has exported any packages (via its Export-Package manifest header) - The uninstalled bundle was
     * selected by the Framework as the exporter of these packages.
     * 
     * If none of the old exports are used, then the old exports must be removed. Otherwise, all old exports must remain
     * available for existing bundles and future resolves until the refreshPackages method is called or the Framework is
     * restarted.
     */
    @Test
    public void testWiringToUninstalled() throws Exception {
        OSGiBundle bundleX = getRuntime().installBundle(getBundleX());
        OSGiBundle bundleB1 = getRuntime().installBundle(getBundleB1());

        bundleB1.start();

        assertBundleState(Bundle.RESOLVED, bundleX.getState());
        assertBundleState(Bundle.ACTIVE, bundleB1.getState());

        // Uninstall X
        bundleX.uninstall();

        // Install B without X
        OSGiBundle bundleB2 = getRuntime().installBundle(getBundleB2());

        bundleB2.start();

        assertBundleState(Bundle.ACTIVE, bundleB2.getState());

        bundleB1.uninstall();
        bundleB2.uninstall();
    }

    @Test
    public void testWiringToUninstalledPackageAdmin() throws Exception {
        OSGiBundle bundleX = getRuntime().installBundle(getBundleX());
        OSGiBundle bundleB1 = getRuntime().installBundle(getBundleB1());

        bundleB1.start();

        assertBundleState(Bundle.RESOLVED, bundleX.getState());
        assertBundleState(Bundle.ACTIVE, bundleB1.getState());

        // Uninstall X
        bundleX.uninstall();

        // Forces the update (replacement) or removal of packages exported by the specified bundles.
        getRuntime().refreshPackages(new OSGiBundle[] { bundleB1, bundleX });

        assertBundleState(Bundle.UNINSTALLED, bundleX.getState());
        assertBundleState(Bundle.INSTALLED, bundleB1.getState());

        try {
            bundleB1.start();
            fail("Unresolved constraint expected");
        } catch (BundleException ex) {
            // expected
        }

        bundleX = getRuntime().installBundle(getBundleX());

        bundleB1.start();

        assertBundleState(Bundle.RESOLVED, bundleX.getState());
        assertBundleState(Bundle.ACTIVE, bundleB1.getState());

        bundleB1.uninstall();
        bundleX.uninstall();
    }

    private JavaArchive getBundleB1() {
        // Bundle-SymbolicName: jbosgi39-bundleB1
        // Bundle-Activator: org.jboss.test.osgi.jbosgi39.bundleB.OSGi39ActivatorB
        // Import-Package: org.jboss.test.osgi.jbosgi39.bundleX
        final JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "jbosgi39-bundleB1");
        archive.addClasses(OSGi39ActivatorB.class);
        archive.setManifest(new Asset() {
            public InputStream openStream() {
                OSGiManifestBuilder builder = OSGiManifestBuilder.newInstance();
                builder.addBundleManifestVersion(2);
                builder.addBundleSymbolicName(archive.getName());
                builder.addBundleActivator(OSGi39ActivatorB.class);
                builder.addImportPackages(BundleActivator.class, OSGi39BeanX.class);
                return builder.openStream();
            }
        });
        return archive;
    }

    private JavaArchive getBundleB2() {
        // Bundle-SymbolicName: jbosgi39-bundleB2
        // Bundle-Activator: org.jboss.test.osgi.jbosgi39.bundleB.OSGi39ActivatorB
        // Import-Package: org.jboss.test.osgi.jbosgi39.bundleX
        final JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "jbosgi39-bundleB2");
        archive.addClasses(OSGi39ActivatorB.class);
        archive.setManifest(new Asset() {
            public InputStream openStream() {
                OSGiManifestBuilder builder = OSGiManifestBuilder.newInstance();
                builder.addBundleManifestVersion(2);
                builder.addBundleSymbolicName(archive.getName());
                builder.addBundleActivator(OSGi39ActivatorB.class);
                builder.addImportPackages(BundleActivator.class, OSGi39BeanX.class);
                return builder.openStream();
            }
        });
        return archive;
    }

    private JavaArchive getBundleX() {
        // Bundle-SymbolicName: jbosgi39-bundleX
        // Export-Package: org.jboss.test.osgi.jbosgi39.bundleX
        final JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "jbosgi39-bundleX");
        archive.addClasses(OSGi39BeanX.class);
        archive.setManifest(new Asset() {
            public InputStream openStream() {
                OSGiManifestBuilder builder = OSGiManifestBuilder.newInstance();
                builder.addBundleManifestVersion(2);
                builder.addBundleSymbolicName(archive.getName());
                builder.addExportPackages(OSGi39BeanX.class);
                return builder.openStream();
            }
        });
        return archive;
    }
}