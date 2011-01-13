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
package org.jboss.test.osgi.jbosgi41;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;

import org.jboss.osgi.spi.capability.ConfigAdminCapability;
import org.jboss.osgi.testing.OSGiBundle;
import org.jboss.osgi.testing.OSGiManifestBuilder;
import org.jboss.osgi.testing.OSGiRuntime;
import org.jboss.osgi.testing.OSGiRuntimeTest;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.test.osgi.jbosgi41.bundleA.OSGi41Activator;
import org.jboss.test.osgi.jbosgi41.bundleA.ServiceA;
import org.jboss.test.osgi.jbosgi41.bundleA.ServiceB;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;

/**
 * [JBOSGI-41] Verify persistent file storage
 * 
 * https://jira.jboss.org/jira/browse/JBOSGI-41
 * 
 * @author thomas.diesler@jboss.com
 * @since 05-Mar-2009
 */
public class OSGi41TestCase extends OSGiRuntimeTest {
    @Test
    public void testFirstRun() throws Exception {
        OSGiRuntime runtime = createDefaultRuntime();
        try {
            runtime.addCapability(new ConfigAdminCapability());

            OSGiBundle bundleA = runtime.installBundle(getBundleA());
            bundleA.start();

            assertBundleState(Bundle.ACTIVE, bundleA.getState());

            File dataFile = getBundleDataFile(bundleA, "config/jbosgi41.txt");
            assertTrue("File exists: " + dataFile, dataFile.exists());

            BufferedReader br = new BufferedReader(new FileReader(dataFile));
            assertEquals("jbosgi41-bundleA", br.readLine());

            bundleA.uninstall();
        } finally {
            runtime.shutdown();
        }
    }

    private File getBundleDataFile(OSGiBundle bundleA, String filename) {
        OSGiBundle systemBundle = bundleA.getRuntime().getBundle(0);
        String storageRoot = systemBundle.getProperty(Constants.FRAMEWORK_STORAGE);
        assertNotNull("Storage dir not null", storageRoot);

        File dataFile = new File(storageRoot + "/bundle-" + bundleA.getBundleId() + "/" + filename);
        return dataFile;
    }

    private JavaArchive getBundleA() {
        // Bundle-SymbolicName: jbosgi41-bundleA
        // Bundle-Activator: org.jboss.test.osgi.jbosgi41.bundleA.OSGi41Activator
        // Export-Package: org.jboss.test.osgi.jbosgi41.bundleA
        final JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "jbosgi41-bundleA");
        archive.addClasses(OSGi41Activator.class, ServiceA.class, ServiceB.class);
        archive.setManifest(new Asset() {
            public InputStream openStream() {
                OSGiManifestBuilder builder = OSGiManifestBuilder.newInstance();
                builder.addBundleManifestVersion(2);
                builder.addBundleSymbolicName(archive.getName());
                builder.addBundleActivator(OSGi41Activator.class);
                builder.addExportPackages("org.jboss.test.osgi.jbosgi41.bundleA");
                builder.addImportPackages("org.osgi.framework", "org.osgi.service.cm", "org.osgi.util.tracker");
                return builder.openStream();
            }
        });
        return archive;
    }
}