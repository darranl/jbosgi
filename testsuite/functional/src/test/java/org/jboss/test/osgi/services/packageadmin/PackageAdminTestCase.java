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
package org.jboss.test.osgi.services.packageadmin;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.InputStream;
import java.util.concurrent.CountDownLatch;

import javax.inject.Inject;

import org.jboss.arquillian.api.ArchiveProvider;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.osgi.OSGiContainer;
import org.jboss.osgi.testing.OSGiManifestBuilder;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.test.osgi.bundles.exporter.ExportedClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.FrameworkListener;
import org.osgi.framework.ServiceReference;
import org.osgi.service.packageadmin.PackageAdmin;

/**
 * @author <a href="david@redhat.com">David Bosschaert</a>
 */
@RunWith(Arquillian.class)
public class PackageAdminTestCase {
    @Inject
    public OSGiContainer container;

    @Inject
    public BundleContext context;

    @Test
    public void testRefreshPackages() throws Exception {
        // Bundle bundle = container.installBundle(container.getTestArchive("initial"));
        // try
        Bundle importing = null, exporting = null;
        FrameworkListener frameworkListener = null;
        try {
            importing = container.installBundle(container.getTestArchive("importing"));
            importing.start();
            try {
                importing.loadClass("org.jboss.test.osgi.bundles.exporter.ExportedClass");
                fail("Should not be able to load the class: ExportedClass");
            } catch (ClassNotFoundException cnfe) {
                // good
            }

            exporting = container.installBundle(container.getTestArchive("exporting"));
            exporting.start();
            try {
                importing.loadClass("org.jboss.test.osgi.bundles.exporter.ExportedClass");
                fail("Should still not be able to load the class, as this bundle is not yet refreshed: ExportedClass");
            } catch (ClassNotFoundException cnfe) {
                // good
            }

            final CountDownLatch latch = new CountDownLatch(1);
            frameworkListener = new FrameworkListener() {
                @Override
                public void frameworkEvent(FrameworkEvent event) {
                    if (event.getType() == FrameworkEvent.PACKAGES_REFRESHED)
                        latch.countDown();
                }
            };
            context.addFrameworkListener(frameworkListener);

            ServiceReference sref = context.getServiceReference(PackageAdmin.class.getName());
            PackageAdmin pa = (PackageAdmin) context.getService(sref);
            pa.refreshPackages(new Bundle[] { importing });

            latch.await(60, SECONDS);

            // This call should now succeed.
            assertNotNull(importing.loadClass("org.jboss.test.osgi.bundles.exporter.ExportedClass"));
        } finally {
            if (frameworkListener != null)
                context.removeFrameworkListener(frameworkListener);

            if (importing != null)
                importing.uninstall();
            if (exporting != null)
                exporting.uninstall();
        }
    }

    @ArchiveProvider
    public static JavaArchive getTestArchive(String name) {
        if ("importing".equals(name))
            return getImportingBundle();
        else if ("exporting".equals(name))
            return getExportingBundle();
        return null;
    }

    private static JavaArchive getImportingBundle() {
        // Bundle-SymbolicName: opt-import-bundle
        // Import-Package: org.jboss.test.osgi.bundles.exporter;resolution:=optional
        final JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "opt-import-bundle");
        archive.setManifest(new Asset() {
            @Override
            public InputStream openStream() {
                OSGiManifestBuilder builder = OSGiManifestBuilder.newInstance();
                builder.addBundleManifestVersion(2);
                builder.addBundleSymbolicName(archive.getName());
                builder.addImportPackages(ExportedClass.class.getPackage().getName() + ";resolution:=optional");
                return builder.openStream();
            }
        });

        return archive;
    }

    private static JavaArchive getExportingBundle() {
        // Bundle-SymbolicName: exporting-bundle
        // Export-Package: org.jboss.test.osgi.bundles.exporter
        final JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "exporting-bundle");
        archive.addClasses(ExportedClass.class);
        archive.setManifest(new Asset() {
            @Override
            public InputStream openStream() {
                OSGiManifestBuilder builder = OSGiManifestBuilder.newInstance();
                builder.addBundleManifestVersion(2);
                builder.addBundleSymbolicName(archive.getName());
                builder.addExportPackages(ExportedClass.class);
                return builder.openStream();
            }
        });
        return archive;
    }
}
