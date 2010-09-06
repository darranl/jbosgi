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
import static org.junit.Assume.assumeNotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CountDownLatch;

import org.jboss.osgi.husky.BridgeFactory;
import org.jboss.osgi.husky.HuskyCapability;
import org.jboss.osgi.husky.RuntimeContext;
import org.jboss.osgi.testing.OSGiBundle;
import org.jboss.osgi.testing.OSGiManifestBuilder;
import org.jboss.osgi.testing.OSGiRuntime;
import org.jboss.osgi.testing.OSGiRuntimeTest;
import org.jboss.osgi.vfs.VFSUtils;
import org.jboss.osgi.vfs.VirtualFile;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.test.osgi.bundles.bundleA.SimpleActivatorA;
import org.jboss.test.osgi.bundles.bundleB.SimpleActivatorB;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.osgi.framework.Bundle;
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
 * @author thomas.diesler@jboss.com
 * @author <a href="david@redhat.com">David Bosschaert</a>
 * @since 04-Mar-2009
 */
@Ignore("Migrate to Arquillian")
public class StartLevelTestCase extends OSGiRuntimeTest
{
   @RuntimeContext
   public BundleContext context;

   // Guarded by this as this latch is used to synchronize threads in this test.
   private CountDownLatch startLevelLatch;
   
   @Before
   public void setUp() throws Exception
   {
      // Only do this if we are not within the OSGi Runtime
      if (context == null)
      {
         OSGiRuntime runtime = createDefaultRuntime();
         runtime.addCapability(new HuskyCapability());

         streamFileToRuntime(runtime, getBundleA());
         streamFileToRuntime(runtime, getBundleB());

         // Install the bundle
         OSGiBundle bundle = runtime.installBundle(getStartLevelBundle());
         bundle.start();
      }
   }
   
   @Test
   public void testStartLevel() throws Exception
   {
      // Tell Husky to run this test method within the OSGi Runtime
      if (context == null)
         BridgeFactory.getBridge().run();
      
      // Stop here if the context is not injected
      assumeNotNull(context);
      
      ServiceReference sref = context.getServiceReference(StartLevel.class.getName());
      StartLevel sls = (StartLevel)context.getService(sref);
      assertEquals(1, sls.getStartLevel());

      assertEquals(1, sls.getInitialBundleStartLevel());

      Bundle ba = null;
      Bundle bb = null;
      FrameworkListener frameworkListener = null;
      try
      {
         // In this try block the state of the framework is modified. Any modifications
         // need to be reverted in the finally block as the OSGi runtime is reused for 
         // subsequent tests.
         sls.setInitialBundleStartLevel(5);

         File baFile = context.getBundle(0).getBundleContext().getDataFile("simple-bundleA.jar");
         File bbFile = context.getBundle(0).getBundleContext().getDataFile("simple-bundleB.jar");
         ba = context.installBundle(baFile.toURI().toString());
         bb = context.installBundle(bbFile.toURI().toString());

         setStartLevelLatch(new CountDownLatch(1));
         frameworkListener = new FrameworkListener()
         {
            @Override
            public void frameworkEvent(FrameworkEvent event)
            {
               if (event.getType() == FrameworkEvent.STARTLEVEL_CHANGED)
               {
                  getStartLevelLatch().countDown();
                  setStartLevelLatch(new CountDownLatch(1));
               }
            }
         };
         context.addFrameworkListener(frameworkListener);

         assertEquals(5, sls.getBundleStartLevel(ba));
         assertEquals(5, sls.getBundleStartLevel(bb));
         ba.start();
         assertTrue("Bundle should not yet be started", (ba.getState() & (Bundle.RESOLVED | Bundle.INSTALLED)) != 0);
         assertTrue("Bundle should not be started", (bb.getState() & (Bundle.RESOLVED | Bundle.INSTALLED)) != 0);

         CountDownLatch latch = getStartLevelLatch();
         sls.setStartLevel(5);

         assertTrue(latch.await(60, SECONDS));
         assertTrue("Bundle should be started", (ba.getState() & Bundle.ACTIVE) != 0);
         assertTrue("Bundle should not be started", (bb.getState() & (Bundle.RESOLVED | Bundle.INSTALLED)) != 0);

         final CountDownLatch bundleStoppedLatch = new CountDownLatch(1);
         BundleListener bl = new BundleListener()
         {
            @Override
            public void bundleChanged(BundleEvent event)
            {
               if (event.getType() == BundleEvent.STOPPED)
               {
                  bundleStoppedLatch.countDown();
               }
            }
         };
         context.addBundleListener(bl);

         sls.setBundleStartLevel(ba, 10);
         assertTrue(bundleStoppedLatch.await(60, SECONDS));
         assertTrue("Bundle should not be started", (ba.getState() & (Bundle.RESOLVED | Bundle.INSTALLED)) != 0);
         assertTrue("Bundle should not be started", (bb.getState() & (Bundle.RESOLVED | Bundle.INSTALLED)) != 0);

         bb.start();
         assertTrue("Bundle should not be started", (ba.getState() & (Bundle.RESOLVED | Bundle.INSTALLED)) != 0);
         assertTrue("Bundle should be started", (bb.getState() & Bundle.ACTIVE) != 0);

         latch = getStartLevelLatch();
         sls.setStartLevel(1);
         assertTrue(latch.await(60, SECONDS));
         assertTrue("Bundle should not be started", (ba.getState() & (Bundle.RESOLVED | Bundle.INSTALLED)) != 0);
         assertTrue("Bundle should not be started", (bb.getState() & (Bundle.RESOLVED | Bundle.INSTALLED)) != 0);
      }
      finally
      {
         sls.setInitialBundleStartLevel(1);
         sls.setStartLevel(1);

         if (frameworkListener != null)
            context.removeFrameworkListener(frameworkListener);

         if (ba != null)
            ba.uninstall();

         if (bb != null)
            bb.uninstall();
      }
   }

   private synchronized CountDownLatch getStartLevelLatch()
   {
      return startLevelLatch;
   }

   private synchronized void setStartLevelLatch(CountDownLatch l)
   {
      startLevelLatch = l;
   }

   private void streamFileToRuntime(OSGiRuntime runtime, JavaArchive archive) throws IOException
   {
      VirtualFile inFile = toVirtualFile(archive);
      File outFile = runtime.getBundle(0).getDataFile(archive.getName() + ".jar").getAbsoluteFile();
      
      InputStream in = inFile.openStream();
      FileOutputStream out = new FileOutputStream(outFile);
      
      VFSUtils.copyStream(in, out);
      
      in.close();
      out.close();
   }
   
   private JavaArchive getBundleA()
   {
      //Bundle-SymbolicName: simple-bundleA
      //Bundle-Activator: org.jboss.test.osgi.bundles.bundleA.SimpleActivatorA
      final JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "simple-bundleA");
      archive.addClasses(SimpleActivatorA.class);
      archive.setManifest(new Asset()
      {
         public InputStream openStream()
         {
            OSGiManifestBuilder builder = OSGiManifestBuilder.newInstance();
            builder.addBundleManifestVersion(2);
            builder.addBundleSymbolicName(archive.getName());
            builder.addBundleActivator(SimpleActivatorA.class);
            return builder.openStream();
         }
      });
      return archive;
   }
   
   private JavaArchive getBundleB()
   {
      //Bundle-SymbolicName: simple-bundleB
      //Bundle-Activator: org.jboss.test.osgi.bundles.bundleB.SimpleActivatorB
      final JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "simple-bundleB");
      archive.addClasses(SimpleActivatorB.class);
      archive.setManifest(new Asset()
      {
         public InputStream openStream()
         {
            OSGiManifestBuilder builder = OSGiManifestBuilder.newInstance();
            builder.addBundleManifestVersion(2);
            builder.addBundleSymbolicName(archive.getName());
            builder.addBundleActivator(SimpleActivatorB.class);
            return builder.openStream();
         }
      });
      return archive;
   }
   
   private JavaArchive getStartLevelBundle()
   {
      // Bundle-SymbolicName: service-startlevel
      // Bundle-Activator: org.jboss.test.osgi.services.startlevel.StartLevelActivator
      // Export-Package: org.jboss.test.osgi.services.startlevel
      final JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "service-startlevel");
      archive.addClasses(StartLevelActivator.class, StartLevelTestCase.class);
      archive.setManifest(new Asset()
      {
         public InputStream openStream()
         {
            OSGiManifestBuilder builder = OSGiManifestBuilder.newInstance();
            builder.addBundleManifestVersion(2);
            builder.addBundleSymbolicName(archive.getName());
            builder.addBundleActivator(StartLevelActivator.class);
            builder.addExportPackages("org.jboss.test.osgi.services.startlevel");
            builder.addImportPackages("org.jboss.osgi.spi.capability", "org.jboss.osgi.husky", "org.jboss.osgi.testing", "org.junit", "org.osgi.framework");
            builder.addImportPackages("org.jboss.shrinkwrap.api", "org.jboss.shrinkwrap.api.asset", "org.jboss.shrinkwrap.api.spec");
            builder.addManifestHeader("Test-Package", "org.jboss.test.osgi.services.startlevel");
            return builder.openStream();
         }
      });
      return archive;
   }
}
