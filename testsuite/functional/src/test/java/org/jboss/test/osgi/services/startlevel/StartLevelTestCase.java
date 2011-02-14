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
package org.jboss.test.osgi.services.startlevel;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.util.concurrent.CountDownLatch;

import javax.inject.Inject;

import org.jboss.arquillian.api.ArchiveProvider;
import org.jboss.arquillian.jmx.DeploymentProvider;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.osgi.testing.OSGiManifestBuilder;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.test.osgi.bundles.bundleA.SimpleActivatorA;
import org.jboss.test.osgi.bundles.bundleB.SimpleActivatorB;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.FrameworkListener;
import org.osgi.framework.ServiceReference;
import org.osgi.service.startlevel.StartLevel;

/**
 * Tests Start Level functionality.
 * 
 * @author <a href="david@redhat.com">David Bosschaert</a>
 */
@RunWith(Arquillian.class)
public class StartLevelTestCase {
    
    @Inject
    public DeploymentProvider provider;

    @Inject
    public BundleContext context;

    private CountDownLatch startLevelLatch;

    @Test
    public void testStartLevel() throws Exception {
        
        StartLevel startLevel = getStartLevel();
        assertEquals(1, startLevel.getStartLevel());

        assertEquals(1, startLevel.getInitialBundleStartLevel());

        Bundle ba = null;
        Bundle bb = null;
        FrameworkListener frameworkListener = null;
        try {
            // In this try block the state of the framework is modified. Any modifications
            // need to be reverted in the finally block as the OSGi runtime is reused for
            // subsequent tests.
            startLevel.setInitialBundleStartLevel(5);
            ba = context.installBundle("BundleA", provider.getClientDeploymentAsStream("BundleA"));
            bb = context.installBundle("BundleB", provider.getClientDeploymentAsStream("BundleB"));

            setStartLevelLatch(new CountDownLatch(1));
            frameworkListener = new FrameworkListener() {
                @Override
                public void frameworkEvent(FrameworkEvent event) {
                    if (event.getType() == FrameworkEvent.STARTLEVEL_CHANGED) {
                        getStartLevelLatch().countDown();
                        setStartLevelLatch(new CountDownLatch(1));
                    }
                }
            };
            context.addFrameworkListener(frameworkListener);

            assertEquals(5, startLevel.getBundleStartLevel(ba));
            assertEquals(5, startLevel.getBundleStartLevel(bb));
            ba.start();
            assertTrue("Bundle should not yet be started", (ba.getState() & (Bundle.RESOLVED | Bundle.INSTALLED)) != 0);
            assertTrue("Bundle should not be started", (bb.getState() & (Bundle.RESOLVED | Bundle.INSTALLED)) != 0);

            CountDownLatch latch = getStartLevelLatch();
            startLevel.setStartLevel(5);

            assertTrue(latch.await(60, SECONDS));
            assertTrue("Bundle should be started", (ba.getState() & Bundle.ACTIVE) != 0);
            assertTrue("Bundle should not be started", (bb.getState() & (Bundle.RESOLVED | Bundle.INSTALLED)) != 0);

            final CountDownLatch bundleStoppedLatch = new CountDownLatch(1);
            BundleListener bl = new BundleListener() {
                @Override
                public void bundleChanged(BundleEvent event) {
                    if (event.getType() == BundleEvent.STOPPED) {
                        bundleStoppedLatch.countDown();
                    }
                }
            };
            context.addBundleListener(bl);

            startLevel.setBundleStartLevel(ba, 10);
            assertTrue(bundleStoppedLatch.await(60, SECONDS));
            assertTrue("Bundle should not be started", (ba.getState() & (Bundle.RESOLVED | Bundle.INSTALLED)) != 0);
            assertTrue("Bundle should not be started", (bb.getState() & (Bundle.RESOLVED | Bundle.INSTALLED)) != 0);

            bb.start();
            assertTrue("Bundle should not be started", (ba.getState() & (Bundle.RESOLVED | Bundle.INSTALLED)) != 0);
            assertTrue("Bundle should be started", (bb.getState() & Bundle.ACTIVE) != 0);

            latch = getStartLevelLatch();
            startLevel.setStartLevel(1);
            assertTrue(latch.await(60, SECONDS));
            assertTrue("Bundle should not be started", (ba.getState() & (Bundle.RESOLVED | Bundle.INSTALLED)) != 0);
            assertTrue("Bundle should not be started", (bb.getState() & (Bundle.RESOLVED | Bundle.INSTALLED)) != 0);
        } finally {
            startLevel.setInitialBundleStartLevel(1);
            startLevel.setStartLevel(1);

            if (frameworkListener != null)
                context.removeFrameworkListener(frameworkListener);

            if (ba != null)
                ba.uninstall();

            if (bb != null)
                bb.uninstall();
        }
    }

    private StartLevel getStartLevel() {
        ServiceReference sref = context.getServiceReference(StartLevel.class.getName());
        StartLevel sls = (StartLevel) context.getService(sref);
        return sls;
    }

    private synchronized CountDownLatch getStartLevelLatch() {
        return startLevelLatch;
    }

    private synchronized void setStartLevelLatch(CountDownLatch l) {
        startLevelLatch = l;
    }

    @ArchiveProvider
    public static JavaArchive getTestArchive(String name) {
        if ("BundleA".equals(name))
            return getTestBundleA();
        else if ("BundleB".equals(name))
            return getTestBundleB();
        return null;
    }

    private static JavaArchive getTestBundleA() {
        // Bundle-SymbolicName: simple-bundleA
        // Bundle-Activator: org.jboss.test.osgi.bundles.bundleA.SimpleActivatorA
        final JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "simple-bundleA");
        archive.addClasses(SimpleActivatorA.class);
        archive.setManifest(new Asset() {
            public InputStream openStream() {
                OSGiManifestBuilder builder = OSGiManifestBuilder.newInstance();
                builder.addBundleManifestVersion(2);
                builder.addBundleSymbolicName(archive.getName());
                builder.addBundleActivator(SimpleActivatorA.class);
                builder.addImportPackages(BundleActivator.class);
                return builder.openStream();
            }
        });
        return archive;
    }

    private static JavaArchive getTestBundleB() {
        // Bundle-SymbolicName: simple-bundleB
        // Bundle-Activator: org.jboss.test.osgi.bundles.bundleB.SimpleActivatorB
        final JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "simple-bundleB");
        archive.addClasses(SimpleActivatorB.class);
        archive.setManifest(new Asset() {
            public InputStream openStream() {
                OSGiManifestBuilder builder = OSGiManifestBuilder.newInstance();
                builder.addBundleManifestVersion(2);
                builder.addBundleSymbolicName(archive.getName());
                builder.addBundleActivator(SimpleActivatorB.class);
                builder.addImportPackages(BundleActivator.class);
                return builder.openStream();
            }
        });
        return archive;
    }
}
