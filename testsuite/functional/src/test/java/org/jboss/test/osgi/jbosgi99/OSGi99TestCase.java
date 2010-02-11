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
package org.jboss.test.osgi.jbosgi99;

//$Id: OSGI39TestCase.java 87103 2009-04-09 22:18:31Z thomas.diesler@jboss.com $

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.jboss.logging.Logger;
import org.jboss.osgi.spi.util.ConstantsHelper;
import org.jboss.osgi.testing.OSGiBundle;
import org.jboss.osgi.testing.OSGiRuntime;
import org.jboss.osgi.testing.OSGiTest;
import org.jboss.osgi.testing.OSGiTestHelper;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;

/**
 * [JBOSGI-99] No explicit control over bundle.start()
 * 
 * https://jira.jboss.org/jira/browse/JBOSGI-99
 * 
 * @author thomas.diesler@jboss.com
 * @since 08-Jul-2009
 */
public class OSGi99TestCase extends OSGiTest
{
   // Provide logging
   private static final Logger log = Logger.getLogger(OSGi99TestCase.class);

   private static OSGiRuntime runtime;

   @BeforeClass
   public static void beforeClass()
   {
      runtime = new OSGiTestHelper().getDefaultRuntime();
   }

   @AfterClass
   public static void afterClass()
   {
      if (runtime != null)
      {
         runtime.shutdown();
         runtime = null;
      }
   }

   @Test
   public void testAllGood() throws Exception
   {
      OSGiBundle bundle = runtime.installBundle("jbosgi99-allgood.jar");
      assertBundleState(Bundle.INSTALLED, bundle.getState());

      bundle.start();
      assertBundleState(Bundle.ACTIVE, bundle.getState());

      bundle.uninstall();
      assertBundleState(Bundle.UNINSTALLED, bundle.getState());
   }

   @Test
   public void testFailOnResolve() throws Exception
   {
      OSGiBundle bundle = runtime.installBundle("jbosgi99-failonresolve.jar");
      assertBundleState(Bundle.INSTALLED, bundle.getState());

      try
      {
         bundle.start();
         fail("BundleException expected");
      }
      catch (BundleException ex)
      {
         log.error("State on error: " + ConstantsHelper.bundleState(bundle.getState()), ex);
         assertBundleState(Bundle.INSTALLED, bundle.getState());
      }

      bundle.uninstall();
      assertBundleState(Bundle.UNINSTALLED, bundle.getState());
   }

   @Test
   public void testFailOnStart() throws Exception
   {
      OSGiBundle bundle = runtime.installBundle("jbosgi99-failonstart.jar");
      assertBundleState(Bundle.INSTALLED, bundle.getState());

      try
      {
         bundle.start();
         fail("BundleException expected");
      }
      catch (BundleException ex)
      {
         log.error("State on error: " + ConstantsHelper.bundleState(bundle.getState()), ex);
         assertBundleState(Bundle.RESOLVED, bundle.getState());
      }

      bundle.uninstall();
      assertBundleState(Bundle.UNINSTALLED, bundle.getState());
   }

   @Test
   // [JBOSGI-210] Bundle installed but not started with hot deploy
   public void testHotDeploy() throws Exception
   {
      if (runtime.isRemoteRuntime() == false)
         return;

      File inFile = getTestArchiveFile("jbosgi99-allgood.jar");

      // Copy the bundle to the data directory
      String outPath = runtime.getBundle(0).getDataFile("jbosgi99-allgood.jar").getAbsolutePath();
      File outFile = new File(outPath);
      copyfile(inFile, outFile);
      
      // Move the bundle to the deploy directory
      outPath = outPath.substring(0, outPath.indexOf("data/osgi-store"));
      File deployFile = new File(outPath + "deploy/jbosgi99-allgood.jar");
      outFile.renameTo(deployFile);

      int timeout = 5000;
      OSGiBundle bundle = null;
      while (timeout > 0)
      {
         bundle = runtime.getBundle("jbosgi99-allgood", null);
         if (bundle != null && bundle.getState() == Bundle.ACTIVE)
            break;

         Thread.sleep(200);
         timeout -= 200;
      }

      assertNotNull("Bundle not null", bundle);
      assertBundleState(Bundle.ACTIVE, bundle.getState());

      // Delete the bundle from the deploy directory
      deployFile.delete();

      timeout = 5000;
      while (timeout > 0)
      {
         if (bundle.getState() == Bundle.UNINSTALLED)
            break;

         Thread.sleep(200);
         timeout -= 200;
      }

      assertBundleState(Bundle.UNINSTALLED, bundle.getState());
   }

   private void copyfile(File inFile, File outFile) throws IOException
   {
      FileInputStream in = new FileInputStream(inFile);
      FileOutputStream out = new FileOutputStream(outFile);

      int c;
      while ((c = in.read()) != -1)
         out.write(c);

      in.close();
      out.close();
   }
}