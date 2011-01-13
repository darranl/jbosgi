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
package org.jboss.osgi.test.performance.bundle.tool;

import static org.jboss.osgi.test.performance.bundle.BundleInstallAndStartBenchmark.COMMON_BUNDLE_PREFIX;
import static org.jboss.osgi.test.performance.bundle.BundleInstallAndStartBenchmark.TEST_BUNDLE_PREFIX;
import static org.jboss.osgi.test.performance.bundle.BundleInstallAndStartBenchmark.UTIL_BUNDLE_PREFIX;
import static org.jboss.osgi.test.performance.bundle.BundleInstallAndStartBenchmark.VERSIONED_IMPL_BUNDLE_PREFIX;
import static org.jboss.osgi.test.performance.bundle.BundleInstallAndStartBenchmark.VERSIONED_INTF_BUNDLE_PREFIX;

import java.io.File;

import org.jboss.osgi.test.performance.bundle.LocalTestBundleProviderImpl;
import org.jboss.osgi.test.performance.bundle.TestBundleProvider;
import org.jboss.osgi.test.performance.bundle.arq.BundleArchiveProvider;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;

/**
 * This tool creates test bundles for use in a the standalone case. When this test is run as a pure standalone bundle.
 * 
 * @see {@link LocalTestBundleProviderImpl}, {@link TestBundleProvider}
 * 
 * @author <a href="david@redhat.com">David Bosschaert</a>
 */
public class BundleCreator {
    public static void main(String[] args) {
        if (args.length < 2)
            throw new IllegalArgumentException("Please provide the following command line arguments: <#bundles> <directory>");

        int numBundles = Integer.parseInt(args[0]);
        File targetDir = new File(args[1]);
        targetDir.mkdirs();

        System.out.println("Generating " + numBundles + " test bundles and a number of additional bundles\nin directory: " + targetDir);

        BundleArchiveProvider bap = new BundleArchiveProvider();
        for (int i = 1; i <= 5; i++) {
            writeBundle(targetDir, bap.getTestArchive(COMMON_BUNDLE_PREFIX + i));
            writeBundle(targetDir, bap.getTestArchive(UTIL_BUNDLE_PREFIX + i));
            writeBundle(targetDir, bap.getTestArchive(VERSIONED_INTF_BUNDLE_PREFIX + i));
            writeBundle(targetDir, bap.getTestArchive(VERSIONED_IMPL_BUNDLE_PREFIX + i));
        }

        for (int i = 0; i < numBundles; i++) {
            writeBundle(targetDir, bap.getTestArchive(TEST_BUNDLE_PREFIX + "Thread_1" + "#" + i));
        }
    }

    private static void writeBundle(File targetDir, Archive<?> testArchive) {
        ZipExporter ze = testArchive.as(ZipExporter.class);
        String name = testArchive.getName() + ".jar";
        File bundleFile = new File(targetDir, name);
        if (bundleFile.exists())
            return;

        System.out.println("Generating " + name);
        ze.exportZip(bundleFile);
    }
}
