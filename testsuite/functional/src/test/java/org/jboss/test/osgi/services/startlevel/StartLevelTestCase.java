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

import java.net.URL;
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

   // Guarded by this as this latch is used to synchronize threads 
   // in this test.
   private CountDownLatch startLevelLatch;

   private synchronized CountDownLatch getStartLevelLatch()
   {
      return startLevelLatch;
   }

   private synchronized void setStartLevelLatch(CountDownLatch l)
   {
      startLevelLatch = l;
   }

   @Before
   public void setUp() throws Exception
   {
      // Only do this if we are not within the OSGi Runtime
      if (context == null)
      {
         // Get the default runtime
         OSGiRuntime runtime = getDefaultRuntime();
         runtime.addCapability(new HuskyCapability());
         
         // Install the bundle
         OSGiBundle bundle = runtime.installBundle("service-startlevel.jar");
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
      sls.setInitialBundleStartLevel(5);

      URL baurl = getTestArchiveURL("fragments-simple-hostA.jar");
      Bundle ba = context.installBundle(baurl.toString());
      URL bburl = getTestArchiveURL("fragments-simple-hostB.jar");
      Bundle bb = context.installBundle(bburl.toString());

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
}
