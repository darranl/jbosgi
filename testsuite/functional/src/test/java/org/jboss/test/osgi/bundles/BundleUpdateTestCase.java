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
package org.jboss.test.osgi.bundles;

import java.io.InputStream;

import javax.inject.Inject;

import org.jboss.arquillian.api.ArchiveProvider;
import org.jboss.arquillian.jmx.DeploymentProvider;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.osgi.testing.OSGiManifestBuilder;
import org.jboss.osgi.testing.OSGiTest;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.test.osgi.bundles.update1.A1;
import org.jboss.test.osgi.bundles.update2.A2;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Version;

/**
 * @author <a href="david@redhat.com">David Bosschaert</a>
 */
@RunWith(Arquillian.class)
public class BundleUpdateTestCase extends OSGiTest {

    @Inject
    public BundleContext context;

    @Inject
    public DeploymentProvider provider;

    @Test
    public void testUpdateBundle() throws Exception {
        InputStream input = provider.getClientDeploymentAsStream("initial");
        Bundle bundle = context.installBundle("initial", input);
        try {
            bundle.start();
            assertLoadClass(bundle, "org.jboss.test.osgi.bundles.update1.A1");
            assertLoadClassFail(bundle, "org.jboss.test.osgi.bundles.update2.A2");

            InputStream is = provider.getClientDeploymentAsStream("updated");
            bundle.update(is);
            assertLoadClass(bundle, "org.jboss.test.osgi.bundles.update2.A2");
            assertLoadClassFail(bundle, "org.jboss.test.osgi.bundles.update1.A1");
        } finally {
            bundle.uninstall();
        }
    }

    @ArchiveProvider
    public static JavaArchive getTestArchive(String name) {
        if ("initial".equals(name))
            return getInitialBundle();
        else if ("updated".equals(name))
            return getUpdatedBundle();
        return null;
    }

    private static JavaArchive getInitialBundle() {
        // Bundle-SymbolicName: update-test
        // Bundle-Version: 1
        // Export-Package: org.jboss.test.osgi.bundles.update1
        final JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "update-test");
        archive.addClass(A1.class);
        archive.setManifest(new Asset() {
            @Override
            public InputStream openStream() {
                OSGiManifestBuilder builder = OSGiManifestBuilder.newInstance();
                builder.addBundleManifestVersion(2);
                builder.addBundleSymbolicName(archive.getName());
                builder.addBundleVersion(Version.parseVersion("1"));
                builder.addExportPackages(A1.class);
                return builder.openStream();
            }
        });
        return archive;
    }

    private static JavaArchive getUpdatedBundle() {
        // Bundle-SymbolicName: update-test
        // Bundle-Version: 2
        // Export-Package: org.jboss.test.osgi.bundles.update2
        final JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "update-test");
        archive.addClass(A2.class);
        archive.setManifest(new Asset() {
            @Override
            public InputStream openStream() {
                OSGiManifestBuilder builder = OSGiManifestBuilder.newInstance();
                builder.addBundleManifestVersion(2);
                builder.addBundleSymbolicName(archive.getName());
                builder.addBundleVersion(Version.parseVersion("2"));
                builder.addExportPackages(A2.class);
                return builder.openStream();
            }
        });
        return archive;
    }
}
