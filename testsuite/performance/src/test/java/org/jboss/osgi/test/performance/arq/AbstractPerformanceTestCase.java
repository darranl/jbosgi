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
package org.jboss.osgi.test.performance.arq;

import java.io.File;

import org.jboss.osgi.test.performance.AbstractBenchmark;
import org.jboss.osgi.test.performance.AbstractThreadedBenchmark;
import org.jboss.osgi.test.performance.ChartType;
import org.jboss.osgi.test.performance.ChartTypeImpl;
import org.jboss.osgi.test.performance.Parameter;
import org.jboss.osgi.test.performance.PerformanceBenchmark;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

/**
 * Abstract Base Class for the Arquillian-based performance tests. Provides a basic test.jar which contains shared classes and
 * provides a number of utility methods.
 * 
 * @author <a href="david@redhat.com">David Bosschaert</a>
 */
public abstract class AbstractPerformanceTestCase {
    protected static JavaArchive getTestBundleArchive() {
        final JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "test.jar");
        archive.addClasses(AbstractBenchmark.class, AbstractPerformanceTestCase.class, AbstractThreadedBenchmark.class, ChartType.class, ChartTypeImpl.class,
                Parameter.class, PerformanceBenchmark.class);
        return archive;
    }

    protected File getResultsDir() {
        File f = new File(System.getProperty("basedir") + "/target/performance-results");
        f.mkdirs();
        return f;
    }

    protected <T> T getService(BundleContext bc, Class<T> c) throws InterruptedException {
        ServiceTracker st = new ServiceTracker(bc, c.getName(), null);
        st.open();
        try {
            Object svc = st.waitForService(100000);
            if (svc == null)
                throw new NullPointerException("Service not available: " + c.getName());

            return c.cast(svc);
        } finally {
            st.close();
        }
    }
}
