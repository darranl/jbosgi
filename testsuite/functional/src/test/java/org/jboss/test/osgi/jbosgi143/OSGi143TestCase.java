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
package org.jboss.test.osgi.jbosgi143;

import java.io.InputStream;

import org.jboss.osgi.testing.OSGiFrameworkTest;
import org.jboss.osgi.testing.OSGiManifestBuilder;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.test.osgi.jbosgi143.bundleA.BeanA;
import org.jboss.test.osgi.jbosgi143.bundleX.BeanX;
import org.junit.Test;
import org.osgi.framework.Bundle;

/**
 * [JBOSGI-143] Add initial support for DynamicImport-Package
 *
 * https://jira.jboss.org/jira/browse/JBOSGI-143
 *
 * @author thomas.diesler@jboss.com
 * @since 28-Aug-2009
 */
public class OSGi143TestCase extends OSGiFrameworkTest {

    @Test
    public void testLoadClass() throws Exception {

        Bundle bundleX = installBundle(getBundleX());
        Bundle bundleA = installBundle(getBundleA());

        assertLoadClass(bundleX, BeanA.class.getName());
        assertLoadClass(bundleX, BeanX.class.getName());

        bundleA.uninstall();
        bundleX.uninstall();
    }

    private JavaArchive getBundleA() {
        final JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "jbosgi143-bundleA");
        archive.addClasses(BeanA.class);
        archive.setManifest(new Asset() {
            public InputStream openStream() {
                OSGiManifestBuilder builder = OSGiManifestBuilder.newInstance();
                builder.addBundleManifestVersion(2);
                builder.addBundleSymbolicName(archive.getName());
                builder.addExportPackages(BeanA.class);
                builder.addImportPackages(BeanX.class);
                return builder.openStream();
            }
        });
        return archive;
    }

    private JavaArchive getBundleX() {
        final JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "jbosgi143-bundleX");
        archive.addClasses(BeanX.class);
        archive.setManifest(new Asset() {
            public InputStream openStream() {
                OSGiManifestBuilder builder = OSGiManifestBuilder.newInstance();
                builder.addBundleManifestVersion(2);
                builder.addBundleSymbolicName(archive.getName());
                builder.addExportPackages(BeanX.class);
                builder.addDynamicImportPackages("*");
                return builder.openStream();
            }
        });
        return archive;
    }
}