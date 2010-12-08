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
package org.jboss.test.osgi.example.jmx;

import static java.util.concurrent.TimeUnit.SECONDS;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

import java.util.Collection;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.TabularData;

import junit.framework.Assert;

import org.jboss.osgi.testing.OSGiBundle;
import org.jboss.osgi.testing.OSGiRuntimeTest;
import org.junit.Test;
import org.osgi.jmx.framework.BundleStateMBean;
import org.osgi.jmx.framework.FrameworkMBean;

/**
 * [TODO]
 *
 * @author <a href="david@redhat.com">David Bosschaert</a>
 */
public class StartLevelTestCase extends OSGiRuntimeTest
{
   @Test
   @SuppressWarnings("unchecked")
   public void testStartLevelMBean() throws Exception
   {
      FrameworkMBean fw = getRuntime().getFrameworkMBean();
      try
      {
         fw.setInitialBundleStartLevel(2);

         Assert.assertEquals(1, fw.getFrameworkStartLevel());
         OSGiBundle bundle = getRuntime().installBundle("example-jmx.jar"); // TODO maybe use another bundle

         BundleStateMBean bs = getRuntime().getBundleStateMBean();
         TabularData td = bs.listBundles();

         long bundleId = -1;
         for (CompositeData row : (Collection<CompositeData>)td.values())
         {
            if (bundle.getSymbolicName().equals(row.get("SymbolicName")))
            {
               bundleId = Long.parseLong(row.get("Identifier").toString());
               break;
            }
         }
         assertTrue("Could not find test bundle through JMX", bundleId != -1);

         fw.startBundle(bundleId);

         assertEquals(2, bs.getStartLevel(bundleId));
         fw.setBundleStartLevel(bundleId, 5);
         assertEquals(5, bs.getStartLevel(bundleId));
         waitForBundleState("INSTALLED", bs, bundleId);

         fw.setFrameworkStartLevel(10);
         waitForBundleState("ACTIVE", bs, bundleId);

         bundle.uninstall();
      }
      finally
      {
         // reset the start level old value
         fw.setInitialBundleStartLevel(1);
         fw.setFrameworkStartLevel(1);
      }
   }

   private void waitForBundleState(String state, BundleStateMBean bs, long bundleId) throws Exception
   {
      int secs = 10;
      while (secs > 0)
      {
         String s = bs.getState(bundleId);
         if (state.equals(s))
            return;

         secs--;
         SECONDS.sleep(1);
      }
      Assert.fail("Did not reach bundle state " + state);
   }
}
