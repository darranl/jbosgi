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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import org.jboss.osgi.husky.BridgeFactory;
import org.jboss.osgi.husky.HuskyCapability;
import org.jboss.osgi.husky.RuntimeContext;
import org.jboss.osgi.testing.OSGiBundle;
import org.jboss.osgi.testing.OSGiRuntime;
import org.jboss.osgi.testing.OSGiRuntimeTest;
import org.junit.Before;
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

         streamFileToRuntime(runtime, "bundles-simple-bundleA.jar");
         streamFileToRuntime(runtime, "bundles-simple-bundleB.jar");

         // Install the bundle
         OSGiBundle bundle = runtime.installBundle("service-startlevel.jar");
         bundle.start();
      }
   }
   
   private void streamFileToRuntime(OSGiRuntime runtime, String fname) throws IOException
   {
      File in = getTestArchiveFile(fname);
      OSGiBundle bundle = runtime.getBundle(0);
      File out = bundle.getDataFile(fname).getAbsoluteFile();
      copyFile(in, out);
   }

   private static void copyFile(File inFile, File outFile) throws IOException
   {
      FileInputStream in = new FileInputStream(inFile);
      FileOutputStream out = new FileOutputStream(outFile);

      int c;
      while ((c = in.read()) != -1)
         out.write(c);

      in.close();
      out.close();
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
      try
      {
         // In this try block the state of the framework is modified. Any modifications
         // need to be reverted in the finally block as the OSGi runtime is reused for 
         // subsequent tests.
         sls.setInitialBundleStartLevel(5);

         File baurl = context.getBundle(0).getBundleContext().getDataFile("bundles-simple-bundleA.jar");
         File bburl = context.getBundle(0).getBundleContext().getDataFile("bundles-simple-bundleB.jar");
         ba = context.installBundle(baurl.toURI().toString());
         bb = context.installBundle(bburl.toURI().toString());

         // david temp workaround to initialize start level
         ba.start();
         ba.stop();
         bb.start();
         bb.stop();
         // david end temp workaround

         setStartLevelLatch(new CountDownLatch(1));
         FrameworkListener fl = new FrameworkListener()
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
         context.addFrameworkListener(fl);

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

         if (ba != null)
         {
            ba.uninstall();
         }
         if (bb != null)
         {
            bb.uninstall();
         }
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
}
