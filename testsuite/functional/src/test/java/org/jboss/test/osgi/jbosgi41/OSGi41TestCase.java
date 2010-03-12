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
package org.jboss.test.osgi.jbosgi41;

//$Id: OSGI41RemoteTestCase.java 87182 2009-04-13 13:47:53Z thomas.diesler@jboss.com $

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import org.jboss.osgi.spi.capability.ConfigAdminCapability;
import org.jboss.osgi.testing.OSGiBundle;
import org.jboss.osgi.testing.OSGiRuntime;
import org.jboss.osgi.testing.OSGiRuntimeTest;
import org.junit.Test;
import org.osgi.framework.Bundle;

/**
 * [JBOSGI-41] Verify persistent file storage
 * 
 * https://jira.jboss.org/jira/browse/JBOSGI-41
 * 
 * @author thomas.diesler@jboss.com
 * @since 05-Mar-2009
 */
public class OSGi41TestCase extends OSGiRuntimeTest
{
   @Test
   public void testFirstRun() throws Exception
   {
      OSGiRuntime runtime = getDefaultRuntime();
      try
      {
         runtime.addCapability(new ConfigAdminCapability());
         
         OSGiBundle bundleA = runtime.installBundle("jbosgi41-bundleA.jar");
         bundleA.start();
         
         assertBundleState(Bundle.ACTIVE, bundleA.getState());
         
         File dataFile = getBundleDataFile(bundleA, "config/jbosgi41.txt");
         assertTrue("File exists: " + dataFile, dataFile.exists());
         
         BufferedReader br = new BufferedReader(new FileReader(dataFile));
         assertEquals("jbosgi41-bundleA", br.readLine());
         
         bundleA.uninstall();
      }
      finally
      {
         runtime.shutdown();
      }
   }

   private File getBundleDataFile(OSGiBundle bundleA, String filename)
   {
      OSGiBundle systemBundle = bundleA.getRuntime().getBundle(0);
      String storageRoot = systemBundle.getProperty("org.osgi.framework.storage");
      assertNotNull("Storage dir not null", storageRoot);

      File dataFile;
      if ("equinox".equals(getFrameworkName()))
      {
         dataFile = new File(storageRoot + "/org.eclipse.osgi/bundles/" + bundleA.getBundleId() + "/data/" + filename);
      }
      else
      {
         dataFile = new File(storageRoot + "/bundle" + bundleA.getBundleId() + "/data/" + filename);
      }
      return dataFile;
   }
}