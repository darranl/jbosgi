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
import static org.jboss.osgi.test.performance.bundle.BundleInstallAndStartBenchmark.COMMON_BUNDLE_PREFIX;
import static org.jboss.osgi.test.performance.bundle.BundleInstallAndStartBenchmark.TEST_BUNDLE_PREFIX;
import static org.jboss.osgi.test.performance.bundle.BundleInstallAndStartBenchmark.UTIL_BUNDLE_PREFIX;
import static org.jboss.osgi.test.performance.bundle.BundleInstallAndStartBenchmark.VERSIONED_IMPL_BUNDLE_PREFIX;
import static org.jboss.osgi.test.performance.bundle.BundleInstallAndStartBenchmark.VERSIONED_INTF_BUNDLE_PREFIX;

import java.io.File;
import java.io.InputStream;

import org.jboss.arquillian.container.test.api.Deployer;
import org.jboss.arquillian.container.test.api.Deployment;
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
import org.jboss.shrinkwrap.api.ShrinkWrap;
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
    static final String COUNTER_UNDEFINED = "-8888888";
    static final String THREAD_NAME_UNDEFINED = "THREAD_NAME_UNDEFINED";
    static final String VERSION_UNDEFINED = "9999999";

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

    abstract Deployer getDeploymentProvider();

    abstract BundleContext getBundleContext();

    void testPerformance(int size) throws Exception {
        TestBundleProviderImpl testBP = new TestBundleProviderImpl(getDeploymentProvider(), getBundleContext());
        BundleInstallAndStartBenchmark bm = new BundleInstallAndStartBenchmark(testBP, getBundleContext());

        // There is a problem with concurrent bundle installs it seems
        // int threads = Runtime.getRuntime().availableProcessors();
        int threads = 1;
        bm.run(threads, size / threads);

        File f = new File(getResultsDir(), "testBundlePerf" + size + "-" + System.currentTimeMillis() + ".xml");
        bm.reportXML(f, new Parameter("Threads", threads), new Parameter("Population", size));
    }

    @Deployment(name = COMMON_BUNDLE_PREFIX, managed = false, testable = false)
    public static JavaArchive getCommonBundle() {
        final String version = VERSION_UNDEFINED;

        final JavaArchive archive = ShrinkWrap.create(JavaArchive.class);
        archive.setManifest(new Asset() {
            @Override
            public InputStream openStream() {
                OSGiManifestBuilder builder = OSGiManifestBuilder.newInstance();
                builder.addBundleSymbolicName("CommonBundle" + version);
                builder.addBundleManifestVersion(2);
                builder.addImportPackages("org.osgi.framework");
                builder.addImportPackages(CommonClass.class.getPackage().getName() + ";version=\"[" + version + ".0," + version + ".0]\"");
                builder.addExportPackages(CommonClass.class.getPackage().getName() + ";version=\"" + version + ".0\"");
                return builder.openStream();
            }
        });
        archive.addClasses(CommonClass.class);
        return archive;
    }

    @Deployment(name = UTIL_BUNDLE_PREFIX + "1", managed = false, testable = false)
    public static JavaArchive getUtilBundle1() {
        return getUtilBundle(1);
    }

    @Deployment(name = UTIL_BUNDLE_PREFIX + "2", managed = false, testable = false)
    public static JavaArchive getUtilBundle2() {
        return getUtilBundle(2);
    }

    @Deployment(name = UTIL_BUNDLE_PREFIX + "3", managed = false, testable = false)
    public static JavaArchive getUtilBundle3() {
        return getUtilBundle(3);
    }

    @Deployment(name = UTIL_BUNDLE_PREFIX + "4", managed = false, testable = false)
    public static JavaArchive getUtilBundle4() {
        return getUtilBundle(4);
    }

    @Deployment(name = UTIL_BUNDLE_PREFIX + "5", managed = false, testable = false)
    public static JavaArchive getUtilBundle5() {
        return getUtilBundle(5);
    }

    private static JavaArchive getUtilBundle(final int i) {
        final Class<?> utilClass = getUtilClass(i);

        final JavaArchive archive = ShrinkWrap.create(JavaArchive.class);
        archive.setManifest(new Asset() {
            @Override
            public InputStream openStream() {
                OSGiManifestBuilder builder = OSGiManifestBuilder.newInstance();
                builder.addBundleSymbolicName("Util" + i);
                builder.addBundleManifestVersion(2);
                builder.addImportPackages("org.osgi.framework");
                builder.addImportPackages(CommonClass.class.getPackage().getName() + ";version=\"[" + i + "," + i + "]\"");
                builder.addExportPackages(utilClass.getPackage().getName() + ";uses:=\"" + CommonClass.class.getPackage().getName() + "\"");
                return builder.openStream();
            }
        });
        archive.addClasses(utilClass);
        return archive;
    }

    private static Class<?> getUtilClass(final int i) {
        switch (i) {
            case 1:
                return Util1.class;
            case 2:
                return Util2.class;
            case 3:
                return Util3.class;
            case 4:
                return Util4.class;
            case 5:
                return Util5.class;
        }
        return null;
    }

    @Deployment(name = VERSIONED_INTF_BUNDLE_PREFIX, managed = false, testable = false)
    public static JavaArchive getVersionedIntfBundle() {
        final String version = VERSION_UNDEFINED;

        final JavaArchive archive = ShrinkWrap.create(JavaArchive.class);
        archive.setManifest(new Asset() {
            @Override
            public InputStream openStream() {
                OSGiManifestBuilder builder = OSGiManifestBuilder.newInstance();
                builder.addBundleSymbolicName("VersionedIntfBundle" + version);
                builder.addBundleManifestVersion(2);
                builder.addImportPackages("org.osgi.framework");
                builder.addExportPackages(VersionedInterface.class.getPackage().getName() + ";version=\"" + version + ".0\"");
                return builder.openStream();
            }
        });
        archive.addClasses(VersionedInterface.class);
        return archive;
    }

    @Deployment(name = VERSIONED_IMPL_BUNDLE_PREFIX + "Util1", managed = false, testable = false)
    public static JavaArchive getVersionedImplBundle1() {
        return getVersionedImplBundle(1);
    }

    @Deployment(name = VERSIONED_IMPL_BUNDLE_PREFIX + "Util2", managed = false, testable = false)
    public static JavaArchive getVersionedImplBundle2() {
        return getVersionedImplBundle(2);
    }

    @Deployment(name = VERSIONED_IMPL_BUNDLE_PREFIX + "Util3", managed = false, testable = false)
    public static JavaArchive getVersionedImplBundle3() {
        return getVersionedImplBundle(3);
    }

    @Deployment(name = VERSIONED_IMPL_BUNDLE_PREFIX + "Util4", managed = false, testable = false)
    public static JavaArchive getVersionedImplBundle4() {
        return getVersionedImplBundle(4);
    }

    @Deployment(name = VERSIONED_IMPL_BUNDLE_PREFIX + "Util5", managed = false, testable = false)
    public static JavaArchive getVersionedImplBundle5() {
        return getVersionedImplBundle(5);
    }

    private static JavaArchive getVersionedImplBundle(final int i) {
        final String version = VERSION_UNDEFINED;

        final JavaArchive archive = ShrinkWrap.create(JavaArchive.class);
        archive.setManifest(new Asset() {
            @Override
            public InputStream openStream() {
                Class<?> utilClass = getUtilClass(i);

                OSGiManifestBuilder builder = OSGiManifestBuilder.newInstance();
                builder.addBundleSymbolicName("VersionedImplBundle" + version);
                builder.addBundleManifestVersion(2);
                builder.addImportPackages("org.osgi.framework");
                builder.addImportPackages(CommonClass.class.getPackage().getName() + ";version=\"[" + version + ".0," + version + ".0]\"");
                builder.addImportPackages(VersionedInterface.class.getPackage().getName() + ";version=\"[" + version + ".0," + version + ".0]\"");
                builder.addImportPackages(utilClass.getPackage().getName());
                builder.addExportPackages(VersionedClass.class.getPackage().getName() + ";version=\"" + version + ".0\";uses:=\""
                        + utilClass.getPackage().getName() + "\"");
                return builder.openStream();
            }
        });
        archive.addClasses(VersionedClass.class);
        return archive;
    }

    @Deployment(name = TEST_BUNDLE_PREFIX, managed = false, testable = false)
    public static JavaArchive getTestBundle() {
        final String threadName = THREAD_NAME_UNDEFINED;
        final String counter = COUNTER_UNDEFINED;
        final String version = VERSION_UNDEFINED;

        final JavaArchive archive = ShrinkWrap.create(JavaArchive.class);
        archive.setManifest(new Asset() {
            @Override
            public InputStream openStream() {
                OSGiManifestBuilder builder = OSGiManifestBuilder.newInstance();
                builder.addBundleSymbolicName("Bundle-" + threadName + "-" + counter);
                builder.addBundleActivator(BundlePerfTestActivator.class.getName());
                builder.addBundleManifestVersion(2);
                builder.addImportPackages("org.osgi.framework");

                builder.addImportPackages(CommonClass.class.getPackage().getName() + ";version=\"[" + version + ".0," + version + ".0]\"");
                builder.addImportPackages(VersionedInterface.class.getPackage().getName() + ";version=\"[" + version + ".0," + version + ".0]\"");
                builder.addImportPackages(VersionedClass.class.getPackage().getName() + ";version=\"[" + version + ".0," + version + ".0]\"");
                return builder.openStream();
            }
        });
        archive.addClasses(BundlePerfTestActivator.class);

        return archive;
    }
}
