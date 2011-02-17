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
package org.jboss.osgi.test.performance.bundle.arq;

import java.io.File;
import java.io.InputStream;

import org.jboss.arquillian.api.ArchiveProvider;
import org.jboss.arquillian.api.Deployment;
import org.jboss.arquillian.api.DeploymentProvider;
import org.jboss.osgi.test.common.CommonClass;
import org.jboss.osgi.test.performance.Parameter;
import org.jboss.osgi.test.performance.arq.AbstractPerformanceTestCase;
import org.jboss.osgi.test.performance.bundle.BundleInstallAndStartBenchmark;
import org.jboss.osgi.test.performance.bundle.BundlePerfTestActivator;
import org.jboss.osgi.test.performance.bundle.TestBundleProvider;
import org.jboss.osgi.test.util1.Util1;
import org.jboss.osgi.test.util2.Util2;
import org.jboss.osgi.test.util3.Util3;
import org.jboss.osgi.test.util4.Util4;
import org.jboss.osgi.test.util5.Util5;
import org.jboss.osgi.test.versioned.VersionedInterface;
import org.jboss.osgi.test.versioned.impl.VersionedClass;
import org.jboss.osgi.testing.OSGiManifestBuilder;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.osgi.framework.BundleContext;

/**
 * This is the base class for the Bundle Performance tests. The actual testing is delegated to the
 * {@link BundleInstallAndStartBenchmark}.
 * <p/>
 * 
 * This test is abstract. Every population is isolated in a unique subclass. This is to enable maven to run every test in a
 * separate VM (when forkMode=always is specified).
 * 
 * @author <a href="david@redhat.com">David Bosschaert</a>
 */
public abstract class BundleTestBase extends AbstractPerformanceTestCase {
    @Deployment
    public static JavaArchive createDeployment() {
        final JavaArchive archive = getTestBundleArchive();
        archive.setManifest(new Asset() {
            public InputStream openStream() {
                OSGiManifestBuilder builder = OSGiManifestBuilder.newInstance();
                builder.addBundleSymbolicName(archive.getName());
                builder.addBundleManifestVersion(2);
                builder.addExportPackages(BundleTestBase.class);
                builder.addImportPackages("org.jboss.arquillian.junit", "org.jboss.logging");
                // builder.addImportPackages("org.jboss.shrinkwrap.api", "org.jboss.shrinkwrap.api.spec");
                builder.addImportPackages("org.junit", "org.junit.runner");
                builder.addImportPackages("org.osgi.framework", "org.osgi.util.tracker");
                builder.addImportPackages("javax.inject");
                builder.addImportPackages("org.jboss.osgi.testing");
                return builder.openStream();
            }
        });
        archive.addClasses(BundleTestBase.class, TestBundleProvider.class);
        archive.addClasses(BundlePerfTestActivator.class, BundleInstallAndStartBenchmark.class);
        archive.addClasses(TestBundleProviderImpl.class);
        archive.addClasses(CommonClass.class, VersionedInterface.class, VersionedClass.class);
        archive.addClasses(Util1.class, Util2.class, Util3.class, Util4.class, Util5.class);
        return archive;
    }

    abstract DeploymentProvider getDeploymentProvider();

    abstract BundleContext getBundleContext();

    void testPerformance(int size) throws Exception {
        BundleInstallAndStartBenchmark bm = new BundleInstallAndStartBenchmark(new TestBundleProviderImpl(getDeploymentProvider()), getBundleContext());

        // There is a problem with concurrent bundle installs it seems
        // int threads = Runtime.getRuntime().availableProcessors();
        int threads = 1;
        bm.run(threads, size / threads);

        File f = new File(getResultsDir(), "testBundlePerf" + size + "-" + System.currentTimeMillis() + ".xml");
        bm.reportXML(f, new Parameter("Threads", threads), new Parameter("Population", size));
    }

    @ArchiveProvider
    public static JavaArchive getTestArchive(String name) {
        return new BundleArchiveProvider().getTestArchive(name);
    }
}
