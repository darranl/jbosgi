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
package org.jboss.osgi.test.performance.bundle;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.jboss.osgi.test.performance.AbstractThreadedBenchmark;
import org.jboss.osgi.test.performance.ChartType;
import org.jboss.osgi.test.performance.ChartTypeImpl;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

/**
 * This Benchmark tests how much time it takes to install and start bundles in a number of class-spaces. It creates the
 * following configuration:
 *
 * 5 (Versioned) Common Bundles - Exports org.jboss.osgi.test.common;version=x 5 Numbered (but not versioned) Util Bundles -
 * Imports org.jboss.osgi.test.common;version=[x,x] - Exports org.jboss.osgi.test.util[x];uses="org.jboss.osgi.test.common" 5
 * Versioned Interfaces Bundles - Exports org.jboss.osgi.test.versioned;version=x 5 Versioned Impl Bundles - Imports
 * org.jboss.osgi.test.common;version=[x,x] - Imports org.jboss.osgi.test.versioned;version=[x,x] - Imports
 * org.jboss.osgi.test.util[x] - Exports org.jboss.osgi.test.versioned.impl;version=x;uses=org.jboss.osgi.test.util[x] a large
 * number of test bundles - Imports org.jboss.osgi.test.common;version=[x,x] - Imports
 * org.jboss.osgi.test.versioned;version=[x,x] - Imports org.jboss.osgi.test.versioned.impl;version=[x,x]
 *
 * Each test bundle loads a class of each of its 3 dependency packages in its activator. This also triggers an indirect load on
 * the Util[x] class.
 *
 * Time is measured for installing and activating of all the bundles.
 *
 * The original intention was to make the Common Bundles all of the same version, but this caused issues with the jbossmc
 * resolver.
 *
 * @author <a href="david@redhat.com">David Bosschaert</a>
 */
public class BundleInstallAndStartBenchmark extends AbstractThreadedBenchmark<Integer> {
    public static final String COMMON_BUNDLE_PREFIX = "commonBundle#";
    public static final String UTIL_BUNDLE_PREFIX = "utilBundle#";
    public static final String VERSIONED_IMPL_BUNDLE_PREFIX = "versionedImplBundle#";
    public static final String VERSIONED_INTF_BUNDLE_PREFIX = "versionedIntfBundle#";
    public static final String TEST_BUNDLE_PREFIX = "testBundle#";

    private static final ChartType INSTALL_START = new ChartTypeImpl("IS", "Bundle Install and Start Time", "Number of Bundles", "Time (ms)");
    private final File bundleStorage;
    private final TestBundleProvider testBundleProvider;

    public BundleInstallAndStartBenchmark(TestBundleProvider tbp, BundleContext bc) {
        super(bc);
        bundleStorage = new File(tempDir, "bundles");
        bundleStorage.mkdirs();
        testBundleProvider = tbp;
    }

    @Override
    protected ChartType[] getAllChartTypes() {
        return new ChartType[] { INSTALL_START };
    }

    /**
     * This method kicks off the test.
     *
     * @param numThreads number of concurrent threads to use.
     * @param numBundlesPerThread number of bundles to install in each thread.
     * @throws Exception if anything goes wrong.
     */
    public void run(int numThreads, int numBundlesPerThread) throws Exception {
        prepareTest(numThreads, numBundlesPerThread);
        runTest(numThreads, numBundlesPerThread);
    }

    public void prepareTest(int numThreads, int numBundlesPerThread) throws Exception {
        installBaseLineBundles();

        for (String threadName : getThreadNames(numThreads))
            createTestBundles(threadName, numBundlesPerThread);
    }

    private void installBaseLineBundles() throws Exception {
        for (int i = 1; i <= 5; i++) {
            addedBundle(bundleContext.installBundle("common" + i, getCommonBundle("" + i)));
            addedBundle(bundleContext.installBundle("util" + i, getUtilBundle(i)));
            addedBundle(bundleContext.installBundle("versioned-intf" + i, getVersionedIntfBundle("" + i)));
            addedBundle(bundleContext.installBundle("versioned-impl" + i, getVersionedImplBundle(i)));
        }
    }

    private void createTestBundles(String threadName, int numBundlesPerThread) throws Exception {
        System.out.println("Creating test bundles in " + bundleStorage);
        for (int i = 0; i < numBundlesPerThread; i++) {
            InputStream is = getTestBundle(threadName, i);
            FileOutputStream fos = new FileOutputStream(new File(bundleStorage, threadName + "_" + i + ".jar"));
            try {
                pumpStreams(is, fos);
            } finally {
                fos.close();
                is.close();
            }
        }
    }

    @Override
    public void runThread(String threadName, Integer numBundlesPerThread) throws Exception {
        List<Bundle> installedBundles = new ArrayList<Bundle>(numBundlesPerThread);

        System.out.println("Starting at " + new Date());
        long start = System.currentTimeMillis();
        for (int i = 0; i < numBundlesPerThread; i++) {
            URI uri = new File(bundleStorage, threadName + "_" + i + ".jar").toURI();
            Bundle bundle = bundleContext.installBundle(uri.toString());
            bundle.start();
            installedBundles.add(bundle);
        }

        // Wait until all bundles have been started
        int numStartedBundles = 0;
        while (numStartedBundles < numBundlesPerThread) {
            Thread.sleep(200);
            synchronized (threadName.intern()) {
                numStartedBundles = Integer.parseInt(System.getProperty(threadName + "started-bundles", "0"));
            }
        }
        long end = System.currentTimeMillis();
        writeData(INSTALL_START, numBundlesPerThread, end - start);
        System.out.println("Installed Bundles " + new Date());

        addedBundles(installedBundles);
    }

    private InputStream getCommonBundle(final String version) throws Exception {
        return testBundleProvider.getTestArchiveStream(COMMON_BUNDLE_PREFIX + version);
    }

    private InputStream getUtilBundle(final int i) throws Exception {
        return testBundleProvider.getTestArchiveStream(UTIL_BUNDLE_PREFIX + i);
    }

    private InputStream getVersionedIntfBundle(final String version) throws Exception {
        return testBundleProvider.getTestArchiveStream(VERSIONED_INTF_BUNDLE_PREFIX + version);
    }

    private InputStream getVersionedImplBundle(final int version) throws Exception {
        return testBundleProvider.getTestArchiveStream(VERSIONED_IMPL_BUNDLE_PREFIX + version);
    }

    private InputStream getTestBundle(final String threadName, final int counter) throws Exception {
        return testBundleProvider.getTestArchiveStream(TEST_BUNDLE_PREFIX + threadName + "#" + counter);
    }

    public static void pumpStreams(InputStream is, OutputStream os) throws IOException {
        byte[] bytes = new byte[8192];

        int length = 0;
        int offset = 0;

        while ((length = is.read(bytes, offset, bytes.length - offset)) != -1) {
            offset += length;

            if (offset == bytes.length) {
                os.write(bytes, 0, bytes.length);
                offset = 0;
            }
        }
        if (offset != 0) {
            os.write(bytes, 0, offset);
        }
    }
}
