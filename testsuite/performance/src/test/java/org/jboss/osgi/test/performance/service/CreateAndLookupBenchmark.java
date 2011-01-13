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

import java.util.Arrays;
import java.util.Date;

import org.jboss.osgi.test.performance.AbstractThreadedBenchmark;
import org.jboss.osgi.test.performance.ChartType;
import org.jboss.osgi.test.performance.ChartTypeImpl;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;

/**
 * This benchmark measures the time it takes to register and then to look up services in the OSGi framework. The test can use
 * multiple threads. The number of threads and services to be used is specified in the {@link #run(int, int)} method.
 * 
 * @author <a href="david@redhat.com">David Bosschaert</a>
 */
public class CreateAndLookupBenchmark extends AbstractThreadedBenchmark<Integer> {
    private static final ChartType REGISTRATION = new ChartTypeImpl("REG", "Service Registration Time", "Number of Services", "Time (ms)");
    private static final ChartType LOOKUP = new ChartTypeImpl("LKU", "Service Lookup Time", "Number of Services", "Time (ms)");

    @SuppressWarnings("unchecked")
    private static final Class<SvcCls>[] CLASSES = new Class[] { SvcCls1.class, SvcCls2.class, SvcCls3.class, SvcCls4.class, SvcCls5.class, SvcCls6.class,
            SvcCls7.class, SvcCls8.class, SvcCls9.class, SvcCls10.class, SvcCls11.class, SvcCls12.class, SvcCls13.class, SvcCls14.class, SvcCls15.class,
            SvcCls16.class, SvcCls17.class, SvcCls18.class, SvcCls19.class, SvcCls20.class };

    public CreateAndLookupBenchmark(BundleContext context) {
        super(context);
    }

    @Override
    protected ChartType[] getAllChartTypes() {
        return new ChartType[] { REGISTRATION, LOOKUP };
    }

    /**
     * This method kicks off the test.
     * 
     * @param numthreads the number of concurrent threads to use.
     * @param numServicesPerThread the number of services each thread should create and look up.
     * @throws Exception if anything goes wrong.
     */
    public void run(int numthreads, int numServicesPerThread) throws Exception {
        runTest(numthreads, numServicesPerThread);
    }

    @Override
    public void runThread(String threadName, Integer numServicesPerThread) throws Exception {
        long[] serviceIDs = new long[numServicesPerThread];

        System.out.println("Starting at " + new Date());
        long regStart = System.currentTimeMillis();
        // Service Registrations
        for (int i = 0; i < numServicesPerThread; i++) {
            SvcCls svc = SvcCls.createInst(CLASSES[i % CLASSES.length], threadName + i);
            ServiceRegistration reg = bundleContext.registerService(svc.getClass().getName(), svc, null);
            serviceIDs[i] = (Long) reg.getReference().getProperty(Constants.SERVICE_ID);
        }
        long regEnd = System.currentTimeMillis();
        writeData(REGISTRATION, numServicesPerThread, regEnd - regStart);
        System.out.println("Registered Services " + new Date());

        long lkuStart = System.currentTimeMillis();
        // Lookup & Invoke Services
        for (int i = 0; i < numServicesPerThread; i++) {
            String className = CLASSES[i % CLASSES.length].getName();
            String filter = "(" + Constants.SERVICE_ID + "=" + serviceIDs[i] + ")";
            ServiceReference[] srs = bundleContext.getServiceReferences(className, filter);
            if (srs.length != 1) {
                String message = "getServiceReferences(" + className + "," + filter + ") => " + Arrays.toString(srs);
                throw new IllegalStateException("Invalid return from " + message);
            }

            SvcCls ti = (SvcCls) bundleContext.getService(srs[0]);
            if (!ti.toString().equals(threadName + i))
                throw new IllegalStateException("Wrong service used, expected: " + i + " but got " + ti);
        }
        long lkuEnd = System.currentTimeMillis();
        writeData(LOOKUP, numServicesPerThread, lkuEnd - lkuStart);
        System.out.println("Invoked Services " + new Date());
    }
}
