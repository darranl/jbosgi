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

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * This activator can be used to run the service performance test outside of a test framework. Just start the bundle that
 * contains this activator in an OSGi framework. The test benchmark subsystem will report to the screen where the results can be
 * found.
 * 
 * @author <a href="david@redhat.com">David Bosschaert</a>
 */
public class StandaloneActivator implements BundleActivator {
    private static final String PERFORMANCE_TEST_BUNDLE_SIZE = "org.jboss.osgi.test.performance.bundle.size";
    public static final String PERFORMANCE_TEST_BUNDLES_DIR = "org.jboss.osgi.test.performance.bundles.dir";

    @Override
    public void start(final BundleContext context) throws Exception {
        new Thread(new Runnable() {
            @Override
            public void run() {
                BundleInstallAndStartBenchmark bm = new BundleInstallAndStartBenchmark(new LocalTestBundleProviderImpl(), context);
                try {
                    int size;
                    try {
                        size = Integer.parseInt(System.getProperty(PERFORMANCE_TEST_BUNDLE_SIZE, "25"));
                    } catch (Throwable th) {
                        size = 25;
                    }
                    System.out.println("*** Running service benchmark with size: " + size);
                    System.out.println("*** For other propulations set system property:\n    " + PERFORMANCE_TEST_BUNDLE_SIZE);

                    bm.run(1, size);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    public void stop(BundleContext context) throws Exception {
    }
}
