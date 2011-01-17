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
package org.jboss.test.osgi.fragments;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.io.InputStream;
import java.net.URL;

import org.jboss.osgi.jmx.BundleStateMBeanExt;
import org.jboss.osgi.jmx.JMXCapability;
import org.jboss.osgi.testing.OSGiBundle;
import org.jboss.osgi.testing.OSGiManifestBuilder;
import org.jboss.osgi.testing.OSGiRuntime;
import org.jboss.osgi.testing.OSGiRuntimeTest;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.test.osgi.fragments.fragA.FragBeanA;
import org.jboss.test.osgi.fragments.fragB.FragBeanB;
import org.jboss.test.osgi.fragments.fragC.FragBeanC;
import org.jboss.test.osgi.fragments.hostA.HostAActivator;
import org.jboss.test.osgi.fragments.hostB.HostBActivator;
import org.jboss.test.osgi.fragments.hostC.HostCActivator;
import org.jboss.test.osgi.fragments.subA.SubBeanA;
import org.junit.BeforeClass;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;

/**
 * Test Fragment functionality
 *
 * @author thomas.diesler@jboss.com
 * @since 07-Jan-2010
 */
public class FragmentRuntimeTest extends OSGiRuntimeTest {

    @BeforeClass
    public static void beforeClass() throws Exception {
        OSGiRuntime runtime = createDefaultRuntime();
        runtime.addCapability(new JMXCapability());
    }

    @Test
    public void testHostOnly() throws Exception {
        // Bundle-SymbolicName: simple-hostA
        // Private-Package: org.jboss.test.osgi.fragments.hostA, org.jboss.test.osgi.fragments.subA
        OSGiBundle hostA = getRuntime().installBundle(getHostA());
        assertBundleState(Bundle.INSTALLED, hostA.getState());

        hostA.start();
        assertBundleState(Bundle.ACTIVE, hostA.getState());

        URL entryURL = hostA.getEntry("resource.txt");
        assertNull("Entry URL null", entryURL);

        URL resourceURL = hostA.getResource("resource.txt");
        assertNull("Resource URL null", resourceURL);

        // Load a private class
        OSGiBundle subBeanProvider = hostA.loadClass(SubBeanA.class.getName());
        assertEquals("Class provided by host", hostA, subBeanProvider);

        hostA.uninstall();
        assertBundleState(Bundle.UNINSTALLED, hostA.getState());
    }

    @Test
    public void testFragmentOnly() throws Exception {
        // Bundle-SymbolicName: simple-fragA
        // Export-Package: org.jboss.test.osgi.fragments.fragA
        // Include-Resource: resources/resource.txt=resource.txt
        // Fragment-Host: simple-hostA
        OSGiBundle fragA = getRuntime().installBundle(getFragmentA());
        assertBundleState(Bundle.INSTALLED, fragA.getState());

        // Use the BundleStateMBeanExt.getEntry() instead of OSGiBundle.getEntry()
        // to normalize the differences in VFS protocols when running against a VFS21 target container.
        BundleStateMBeanExt bundleState = (BundleStateMBeanExt) getRuntime().getBundleStateMBean();

        String entryURL = bundleState.getEntry(fragA.getBundleId(), "resource.txt");
        assertNotNull("Entry URL not null", entryURL);

        String resourceURL = bundleState.getResource(fragA.getBundleId(), "resource.txt");
        assertNull("Resource URL null", resourceURL);

        try {
            fragA.start();
            fail("Fragment bundles can not be started");
        } catch (BundleException e) {
            assertBundleState(Bundle.INSTALLED, fragA.getState());
        }

        fragA.uninstall();
        assertBundleState(Bundle.UNINSTALLED, fragA.getState());
    }

    @Test
    public void testAttachedFragment() throws Exception {
        // Bundle-SymbolicName: simple-hostA
        // Private-Package: org.jboss.test.osgi.fragments.hostA, org.jboss.test.osgi.fragments.subA
        OSGiBundle hostA = getRuntime().installBundle(getHostA());
        assertBundleState(Bundle.INSTALLED, hostA.getState());

        // Bundle-SymbolicName: simple-fragA
        // Export-Package: org.jboss.test.osgi.fragments.fragA
        // Include-Resource: resources/resource.txt=resource.txt
        // Fragment-Host: simple-hostA
        OSGiBundle fragA = getRuntime().installBundle(getFragmentA());
        assertBundleState(Bundle.INSTALLED, fragA.getState());

        hostA.start();
        assertBundleState(Bundle.ACTIVE, hostA.getState());
        assertBundleState(Bundle.RESOLVED, fragA.getState());

        URL entryURL = hostA.getEntry("resource.txt");
        assertNull("Entry URL null", entryURL);

        URL resourceURL = hostA.getResource("resource.txt");
        assertNotNull("Resource URL not null", resourceURL);

        OSGiBundle fragBeanProvider = hostA.loadClass(FragBeanA.class.getName());
        assertEquals("Class provided by fragment", hostA, fragBeanProvider);

        // Load a private class
        OSGiBundle subBeanProvider = hostA.loadClass(SubBeanA.class.getName());
        assertEquals("Class provided by fragment", hostA, subBeanProvider);

        hostA.uninstall();
        assertBundleState(Bundle.UNINSTALLED, hostA.getState());
        assertBundleState(Bundle.RESOLVED, fragA.getState());

        fragA.uninstall();
        assertBundleState(Bundle.UNINSTALLED, fragA.getState());
    }

    @Test
    public void testHiddenPrivatePackage() throws Exception {
        // Bundle-SymbolicName: simple-hostA
        // Private-Package: org.jboss.test.osgi.fragments.hostA, org.jboss.test.osgi.fragments.subA
        OSGiBundle hostA = getRuntime().installBundle(getHostA());
        assertBundleState(Bundle.INSTALLED, hostA.getState());

        // Bundle-SymbolicName: simple-hostB
        // Export-Package: org.jboss.test.osgi.fragments.subA
        // Private-Package: org.jboss.test.osgi.fragments.hostB
        OSGiBundle hostB = getRuntime().installBundle(getHostB());
        assertBundleState(Bundle.INSTALLED, hostB.getState());

        // Bundle-SymbolicName: simple-fragB
        // Import-Package: org.jboss.test.osgi.fragments.subA
        // Fragment-Host: simple-hostA
        OSGiBundle fragB = getRuntime().installBundle(getFragmentB());
        assertBundleState(Bundle.INSTALLED, fragB.getState());

        hostA.start();
        assertBundleState(Bundle.ACTIVE, hostA.getState());
        assertBundleState(Bundle.RESOLVED, fragB.getState());

        // The fragment contains an overwrites Private-Package with Import-Package
        // The SubBeanA is expected to come from HostB, which exports that package
        OSGiBundle subBeanProvider = hostA.loadClass(SubBeanA.class.getName());
        assertEquals("Class provided by host", hostB, subBeanProvider);

        hostA.uninstall();
        assertBundleState(Bundle.UNINSTALLED, hostA.getState());
        assertBundleState(Bundle.RESOLVED, fragB.getState());

        hostB.uninstall();
        assertBundleState(Bundle.UNINSTALLED, hostB.getState());

        fragB.uninstall();
        assertBundleState(Bundle.UNINSTALLED, fragB.getState());
    }

    @Test
    public void testFragmentExportsPackage() throws Exception {
        // Bundle-SymbolicName: simple-hostA
        // Private-Package: org.jboss.test.osgi.fragments.hostA, org.jboss.test.osgi.fragments.subA
        OSGiBundle hostA = getRuntime().installBundle(getHostA());
        assertBundleState(Bundle.INSTALLED, hostA.getState());

        // Bundle-SymbolicName: simple-hostC
        // Import-Package: org.jboss.test.osgi.fragments.fragA
        // Private-Package: org.jboss.test.osgi.fragments.hostC
        OSGiBundle hostC = getRuntime().installBundle(getHostC());
        assertBundleState(Bundle.INSTALLED, hostA.getState());

        hostA.start();
        assertBundleState(Bundle.ACTIVE, hostA.getState());

        try {
            // HostA does not export the package needed by HostC
            hostC.start();
            fail("Unresolved constraint expected");
        } catch (BundleException ex) {
            assertBundleState(Bundle.INSTALLED, hostC.getState());
        }

        // Bundle-SymbolicName: simple-fragA
        // Export-Package: org.jboss.test.osgi.fragments.fragA
        // Include-Resource: resources/resource.txt=resource.txt
        // Fragment-Host: simple-hostA
        OSGiBundle fragA = getRuntime().installBundle(getFragmentA());
        assertBundleState(Bundle.INSTALLED, fragA.getState());

        try {
            // FragA does not attach to the aleady resolved HostA
            // HostA does not export the package needed by HostC
            hostC.start();
            fail("Unresolved constraint expected");
        } catch (BundleException ex) {
            assertBundleState(Bundle.INSTALLED, hostC.getState());
        }

        // Refreshing HostA causes the FragA to get attached
        getRuntime().refreshPackages(new OSGiBundle[] { hostA });

        // HostC should now resolve and start
        hostC.start();
        assertBundleState(Bundle.ACTIVE, hostC.getState());

        hostA.uninstall();
        assertBundleState(Bundle.UNINSTALLED, hostA.getState());

        hostC.uninstall();
        assertBundleState(Bundle.UNINSTALLED, hostC.getState());

        fragA.uninstall();
        assertBundleState(Bundle.UNINSTALLED, fragA.getState());
    }

    @Test
    public void testFragmentRequireBundle() throws Exception {

        // Bundle-SymbolicName: simple-hostA
        // Private-Package: org.jboss.test.osgi.fragments.hostA, org.jboss.test.osgi.fragments.subA
        OSGiBundle hostA = getRuntime().installBundle(getHostA());
        assertBundleState(Bundle.INSTALLED, hostA.getState());

        // Bundle-SymbolicName: simple-fragC
        // Export-Package: org.jboss.test.osgi.fragments.fragC
        // Require-Bundle: simple-hostB
        // Fragment-Host: simple-hostA
        OSGiBundle fragC = getRuntime().installBundle(getFragmentC());
        assertBundleState(Bundle.INSTALLED, fragC.getState());

        try {
            // The attached FragA requires bundle HostB, which is not yet installed
            hostA.start();

            // Clarify error behaviour when fragments fail to attach
            // https://www.osgi.org/members/bugzilla/show_bug.cgi?id=1524
            //
            // Felix: Merges FragC Require-Bundle into HostA and fails to resolve
            // Equinox: Resolves HostA but does not attach FragA
            if (hostA.getState() == Bundle.ACTIVE)
                assertBundleState(Bundle.INSTALLED, fragC.getState());
        } catch (BundleException ex) {
            assertBundleState(Bundle.INSTALLED, hostA.getState());
        }

        // Bundle-SymbolicName: simple-hostB
        // Export-Package: org.jboss.test.osgi.fragments.subA
        // Private-Package: org.jboss.test.osgi.fragments.hostB
        OSGiBundle hostB = getRuntime().installBundle(getHostB());
        assertBundleState(Bundle.INSTALLED, hostB.getState());

        // HostA should resolve and start after HostB got installed
        hostA.start();
        assertBundleState(Bundle.ACTIVE, hostA.getState());

        hostA.uninstall();
        assertBundleState(Bundle.UNINSTALLED, hostA.getState());

        fragC.uninstall();
        assertBundleState(Bundle.UNINSTALLED, fragC.getState());
    }

    private JavaArchive getHostA() {
        // Bundle-SymbolicName: simple-hostA
        // Bundle-Activator: org.jboss.test.osgi.fragments.hostA.HostAActivator
        final JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "simple-hostA");
        archive.addClasses(HostAActivator.class, SubBeanA.class);
        archive.setManifest(new Asset() {
            public InputStream openStream() {
                OSGiManifestBuilder builder = OSGiManifestBuilder.newInstance();
                builder.addBundleManifestVersion(2);
                builder.addBundleSymbolicName(archive.getName());
                builder.addBundleActivator(HostAActivator.class);
                return builder.openStream();
            }
        });
        return archive;
    }

    private JavaArchive getHostB() {
        // Bundle-SymbolicName: simple-hostB
        // Bundle-Activator: org.jboss.test.osgi.fragments.hostB.HostBActivator
        // Export-Package: org.jboss.test.osgi.fragments.subA
        final JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "simple-hostB");
        archive.addClasses(HostBActivator.class, SubBeanA.class);
        archive.setManifest(new Asset() {
            public InputStream openStream() {
                OSGiManifestBuilder builder = OSGiManifestBuilder.newInstance();
                builder.addBundleManifestVersion(2);
                builder.addBundleSymbolicName(archive.getName());
                builder.addBundleActivator(HostBActivator.class);
                builder.addExportPackages(SubBeanA.class);
                return builder.openStream();
            }
        });
        return archive;
    }

    private JavaArchive getHostC() {
        // Bundle-SymbolicName: simple-hostC
        // Bundle-Activator: org.jboss.test.osgi.fragments.hostC.HostCActivator
        // Import-Package: org.osgi.framework, org.jboss.test.osgi.fragments.fragA
        final JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "simple-hostC");
        archive.addClasses(HostCActivator.class);
        archive.setManifest(new Asset() {
            public InputStream openStream() {
                OSGiManifestBuilder builder = OSGiManifestBuilder.newInstance();
                builder.addBundleManifestVersion(2);
                builder.addBundleSymbolicName(archive.getName());
                builder.addBundleActivator(HostCActivator.class);
                builder.addImportPackages(FragBeanA.class);
                return builder.openStream();
            }
        });
        return archive;
    }

    private JavaArchive getFragmentA() {
        // Bundle-SymbolicName: simple-fragA
        // Export-Package: org.jboss.test.osgi.fragments.fragA
        // Fragment-Host: simple-hostA
        final JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "simple-fragA");
        archive.addClasses(FragBeanA.class);
        archive.addResource(getResourceFile("fragments/resource.txt"));
        archive.setManifest(new Asset() {
            public InputStream openStream() {
                OSGiManifestBuilder builder = OSGiManifestBuilder.newInstance();
                builder.addBundleManifestVersion(2);
                builder.addBundleSymbolicName(archive.getName());
                builder.addExportPackages(FragBeanA.class);
                builder.addFragmentHost("simple-hostA");
                return builder.openStream();
            }
        });
        return archive;
    }

    private JavaArchive getFragmentB() {
        // Bundle-SymbolicName: simple-fragB
        // Export-Package: org.jboss.test.osgi.fragments.fragB
        // Import-Package: org.jboss.test.osgi.fragments.subA
        // Fragment-Host: simple-hostA
        final JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "simple-fragB");
        archive.addClasses(FragBeanB.class);
        archive.setManifest(new Asset() {
            public InputStream openStream() {
                OSGiManifestBuilder builder = OSGiManifestBuilder.newInstance();
                builder.addBundleManifestVersion(2);
                builder.addBundleSymbolicName(archive.getName());
                builder.addExportPackages(FragBeanB.class);
                builder.addImportPackages(SubBeanA.class);
                builder.addFragmentHost("simple-hostA");
                return builder.openStream();
            }
        });
        return archive;
    }

    private JavaArchive getFragmentC() {
        // Bundle-SymbolicName: simple-fragC
        // Export-Package: org.jboss.test.osgi.fragments.fragC
        // Require-Bundle: simple-hostB
        // Fragment-Host: simple-hostA
        final JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "simple-fragC");
        archive.addClasses(FragBeanC.class);
        archive.setManifest(new Asset() {
            public InputStream openStream() {
                OSGiManifestBuilder builder = OSGiManifestBuilder.newInstance();
                builder.addBundleManifestVersion(2);
                builder.addBundleSymbolicName(archive.getName());
                builder.addExportPackages(FragBeanC.class);
                builder.addRequireBundle("simple-hostB");
                builder.addFragmentHost("simple-hostA");
                return builder.openStream();
            }
        });
        return archive;
    }
}