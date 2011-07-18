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
package org.jboss.osgi.test.performance.service.arq;

import java.io.File;
import java.io.InputStream;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.osgi.test.performance.Parameter;
import org.jboss.osgi.test.performance.arq.AbstractPerformanceTestCase;
import org.jboss.osgi.test.performance.service.CreateAndLookupBenchmark;
import org.jboss.osgi.test.performance.service.CreateAndLookupTestActivator;
import org.jboss.osgi.test.performance.service.SvcCls;
import org.jboss.osgi.test.performance.service.SvcCls1;
import org.jboss.osgi.test.performance.service.SvcCls10;
import org.jboss.osgi.test.performance.service.SvcCls11;
import org.jboss.osgi.test.performance.service.SvcCls12;
import org.jboss.osgi.test.performance.service.SvcCls13;
import org.jboss.osgi.test.performance.service.SvcCls14;
import org.jboss.osgi.test.performance.service.SvcCls15;
import org.jboss.osgi.test.performance.service.SvcCls16;
import org.jboss.osgi.test.performance.service.SvcCls17;
import org.jboss.osgi.test.performance.service.SvcCls18;
import org.jboss.osgi.test.performance.service.SvcCls19;
import org.jboss.osgi.test.performance.service.SvcCls2;
import org.jboss.osgi.test.performance.service.SvcCls20;
import org.jboss.osgi.test.performance.service.SvcCls3;
import org.jboss.osgi.test.performance.service.SvcCls4;
import org.jboss.osgi.test.performance.service.SvcCls5;
import org.jboss.osgi.test.performance.service.SvcCls6;
import org.jboss.osgi.test.performance.service.SvcCls7;
import org.jboss.osgi.test.performance.service.SvcCls8;
import org.jboss.osgi.test.performance.service.SvcCls9;
import org.jboss.osgi.testing.OSGiManifestBuilder;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

/**
 * This is the base class for the Service Performance tests. The actual testing is delegated to the
 * {@link CreateAndLookupBenchmark}.
 * <p/>
 *
 * This test is abstract. Every population is isolated in a unique subclass. This is to enable maven to run every test in a
 * separate VM (when forkMode=always is specified).
 *
 * @author <a href="david@redhat.com">David Bosschaert</a>
 */
public abstract class ServiceTestBase extends AbstractPerformanceTestCase {
    @Deployment
    public static JavaArchive createDeployment() {
        final JavaArchive archive = getTestBundleArchive();
        archive.setManifest(new Asset() {
            public InputStream openStream() {
                OSGiManifestBuilder builder = OSGiManifestBuilder.newInstance();
                builder.addBundleSymbolicName(archive.getName());
                builder.addBundleManifestVersion(2);
                builder.addBundleActivator(CreateAndLookupTestActivator.class.getName());
                builder.addExportPackages(ServiceTestBase.class);
                builder.addImportPackages("org.jboss.arquillian.junit", "org.jboss.logging");
                builder.addImportPackages("org.jboss.shrinkwrap.api", "org.jboss.shrinkwrap.api.asset", "org.jboss.shrinkwrap.api.spec");
                builder.addImportPackages("org.junit", "org.junit.runner");
                builder.addImportPackages("org.osgi.framework", "org.osgi.util.tracker");
                builder.addImportPackages("javax.inject");
                return builder.openStream();
            }
        });
        archive.addClasses(CreateAndLookupTestActivator.class, CreateAndLookupBenchmark.class, ServiceTestBase.class);
        archive.addClasses(SvcCls.class, SvcCls1.class, SvcCls2.class, SvcCls3.class, SvcCls4.class, SvcCls5.class);
        archive.addClasses(SvcCls6.class, SvcCls7.class, SvcCls8.class, SvcCls9.class, SvcCls10.class);
        archive.addClasses(SvcCls11.class, SvcCls12.class, SvcCls13.class, SvcCls14.class, SvcCls15.class);
        archive.addClasses(SvcCls16.class, SvcCls17.class, SvcCls18.class, SvcCls19.class, SvcCls20.class);
        return archive;
    }

    abstract Bundle getBundle();

    void testPerformance(int size) throws Exception {
        getBundle().start();

        BundleContext bc = getBundle().getBundleContext();
        CreateAndLookupBenchmark tc = getService(bc, CreateAndLookupBenchmark.class);
        int processors = Runtime.getRuntime().availableProcessors();
        tc.run(processors, size / processors);
        File f = new File(getResultsDir(), "testPerformance" + size + "-" + System.currentTimeMillis() + ".xml");
        tc.reportXML(f, new Parameter("Threads", processors), new Parameter("Population", size));

        getBundle().stop();
    }
}
