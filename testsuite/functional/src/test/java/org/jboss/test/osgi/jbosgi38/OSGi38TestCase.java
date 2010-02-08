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
package org.jboss.test.osgi.jbosgi38;

//$Id: OSGI38TestCase.java 87103 2009-04-09 22:18:31Z thomas.diesler@jboss.com $

import static org.junit.Assert.fail;

import org.jboss.osgi.spi.capability.CompendiumCapability;
import org.jboss.osgi.testing.OSGiBundle;
import org.jboss.osgi.testing.OSGiRuntime;
import org.jboss.osgi.testing.OSGiTest;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;

/**
 * [JBOSGI-38] Investigate bundle install/start behaviour with random deployment order
 * 
 * https://jira.jboss.org/jira/browse/JBOSGI-38
 * 
 * Bundle A depends on bundle B, both share bundle X.
 * 
 * A ---> B 
 * A ---> X <--- B
 * 
 * [TODO] Use default runtime for in container testing
 * 
 * @author thomas.diesler@jboss.com
 * @since 02-Mar-2009
 */
public class OSGi38TestCase extends OSGiTest
{
   @Test
   public void testInstallStartX() throws Exception
   {
      OSGiRuntime runtime = getEmbeddedRuntime();
      try
      {
         runtime.addCapability(new CompendiumCapability());
         
         OSGiBundle bundleX = runtime.installBundle("jbosgi38-bundleX.jar");
         assertBundleState(Bundle.INSTALLED, bundleX.getState());

         bundleX.start();
         assertBundleState(Bundle.ACTIVE, bundleX.getState());
         
         bundleX.uninstall();
         assertBundleState(Bundle.UNINSTALLED, bundleX.getState());
      }
      finally
      {
         runtime.shutdown();
      }
   }

   /*
    * Install X, B
    */
   @Test
   public void testInstallXBeforeB() throws Exception
   {
      OSGiRuntime runtime = getEmbeddedRuntime();
      try
      {
         runtime.addCapability(new CompendiumCapability());
         
         OSGiBundle bundleX = runtime.installBundle("jbosgi38-bundleX.jar");
         assertBundleState(Bundle.INSTALLED, bundleX.getState());

         OSGiBundle bundleB = runtime.installBundle("jbosgi38-bundleB.jar");
         assertBundleState(Bundle.INSTALLED, bundleB.getState());

         bundleB.start();
         assertBundleState(Bundle.RESOLVED, bundleX.getState());
         assertBundleState(Bundle.ACTIVE, bundleB.getState());
         
         bundleB.uninstall();
         bundleX.uninstall();
      }
      finally
      {
         runtime.shutdown();
      }
   }

   /*
    * Install X, B, A
    */
   @Test
   public void testInstallBBeforeA() throws Exception
   {
      OSGiRuntime runtime = getEmbeddedRuntime();
      try
      {
         runtime.addCapability(new CompendiumCapability());
         
         OSGiBundle bundleX = runtime.installBundle("jbosgi38-bundleX.jar");
         assertBundleState(Bundle.INSTALLED, bundleX.getState());

         OSGiBundle bundleB = runtime.installBundle("jbosgi38-bundleB.jar");
         assertBundleState(Bundle.INSTALLED, bundleB.getState());

         OSGiBundle bundleA = runtime.installBundle("jbosgi38-bundleA.jar");
         assertBundleState(Bundle.INSTALLED, bundleA.getState());

         bundleA.start();
         assertBundleState(Bundle.RESOLVED, bundleX.getState());
         assertBundleState(Bundle.RESOLVED, bundleB.getState());
         assertBundleState(Bundle.ACTIVE, bundleA.getState());
         
         bundleA.uninstall();
         bundleB.uninstall();
         bundleX.uninstall();
      }
      finally
      {
         runtime.shutdown();
      }
   }

   /*
    * Install B, X
    */
   @Test
   public void testInstallBBeforeX() throws Exception
   {
      OSGiRuntime runtime = getEmbeddedRuntime();
      try
      {
         runtime.addCapability(new CompendiumCapability());
         
         OSGiBundle bundleB = runtime.installBundle("jbosgi38-bundleB.jar");
         assertBundleState(Bundle.INSTALLED, bundleB.getState());

         try
         {
            bundleB.start();
            fail("Unresolved constraint expected");
         }
         catch (BundleException ex)
         {
            // expected
         }

         OSGiBundle bundleX = runtime.installBundle("jbosgi38-bundleX.jar");
         assertBundleState(Bundle.INSTALLED, bundleX.getState());

         bundleB.start();
         assertBundleState(Bundle.RESOLVED, bundleX.getState());
         assertBundleState(Bundle.ACTIVE, bundleB.getState());

         bundleB.uninstall();
         bundleX.uninstall();
      }
      finally
      {
         runtime.shutdown();
      }
   }

   /*
    * Install A, B, X
    */
   @Test
   public void testInstallABeforeB() throws Exception
   {
      OSGiRuntime runtime = getEmbeddedRuntime();
      try
      {
         runtime.addCapability(new CompendiumCapability());
         
         OSGiBundle bundleA = runtime.installBundle("jbosgi38-bundleA.jar");
         assertBundleState(Bundle.INSTALLED, bundleA.getState());

         OSGiBundle bundleB = runtime.installBundle("jbosgi38-bundleB.jar");
         assertBundleState(Bundle.INSTALLED, bundleB.getState());

         try
         {
            bundleB.start();
            fail("Unresolved constraint expected");
         }
         catch (BundleException ex)
         {
            // expected
         }

         OSGiBundle bundleX = runtime.installBundle("jbosgi38-bundleX.jar");
         assertBundleState(Bundle.INSTALLED, bundleX.getState());

         bundleB.start();
         assertBundleState(Bundle.RESOLVED, bundleX.getState());
         assertBundleState(Bundle.ACTIVE, bundleB.getState());
         
         bundleA.start();
         assertBundleState(Bundle.ACTIVE, bundleA.getState());

         bundleA.uninstall();
         bundleB.uninstall();
         bundleX.uninstall();
      }
      finally
      {
         runtime.shutdown();
      }
   }

   /*
    * Uninstall X, B stays active
    */
   @Test
   public void testUninstallX() throws Exception
   {
      if ("jbossmc".equals(getFrameworkName()))
      {
         System.out.println("FIXME [JBOSGI-213] Unexpected dependee state changes");
         return;
      }
         
      OSGiRuntime runtime = getEmbeddedRuntime();
      try
      {
         runtime.addCapability(new CompendiumCapability());
         
         OSGiBundle bundleX = runtime.installBundle("jbosgi38-bundleX.jar");
         assertBundleState(Bundle.INSTALLED, bundleX.getState());

         OSGiBundle bundleB = runtime.installBundle("jbosgi38-bundleB.jar");
         assertBundleState(Bundle.INSTALLED, bundleB.getState());

         bundleB.start();
         assertBundleState(Bundle.RESOLVED, bundleX.getState());
         assertBundleState(Bundle.ACTIVE, bundleB.getState());
         
         bundleX.stop();
         assertBundleState(Bundle.RESOLVED, bundleX.getState());
         assertBundleState(Bundle.ACTIVE, bundleB.getState());
         
         bundleX.uninstall();
         assertBundleState(Bundle.UNINSTALLED, bundleX.getState());
         assertBundleState(Bundle.ACTIVE, bundleB.getState());
         
         bundleB.uninstall();
         assertBundleState(Bundle.UNINSTALLED, bundleB.getState());
      }
      finally
      {
         runtime.shutdown();
      }
   }

}