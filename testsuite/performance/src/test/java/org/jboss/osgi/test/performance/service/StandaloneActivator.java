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
package org.jboss.osgi.test.performance.service;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;

/**
 * This activator can be used to run the service performance test outside of a test framework. Just start the bundle that
 * contains this activator in an OSGi framework. The test benchmark subsystem will report to the screen where the results can be
 * found.
 * 
 * @author <a href="david@redhat.com">David Bosschaert</a>
 */
public class StandaloneActivator implements BundleActivator {
    private static final String PERFORMANCE_TEST_SERVICE_SIZE = "org.jboss.osgi.test.performance.service.size";

    @Override
    public void start(final BundleContext context) throws Exception {
        new Thread(new Runnable() {
            @Override
            public void run() {
                CreateAndLookupBenchmark bm = new CreateAndLookupBenchmark(context);
                try {
                    int size;
                    try {
                        size = Integer.parseInt(System.getProperty(PERFORMANCE_TEST_SERVICE_SIZE, "25"));
                    } catch (Throwable th) {
                        size = 25;
                    }
                    System.out.println("*** Running service benchmark with size: " + size);
                    System.out.println("*** For other propulations set system property:\n    " + PERFORMANCE_TEST_SERVICE_SIZE);

                    int processors = Runtime.getRuntime().availableProcessors();
                    bm.run(processors, size / processors);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        context.getBundle().stop();
                        System.out.println("*** Performance test finished, stopped bundle");
                    } catch (BundleException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    @Override
    public void stop(BundleContext context) throws Exception {
    }
}
