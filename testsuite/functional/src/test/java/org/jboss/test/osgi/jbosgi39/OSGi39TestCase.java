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
package org.jboss.test.osgi.jbosgi39;

//$Id: OSGI39TestCase.java 87103 2009-04-09 22:18:31Z thomas.diesler@jboss.com $

import static org.junit.Assert.fail;

import org.jboss.osgi.jmx.FrameworkMBeanExt;
import org.jboss.osgi.jmx.JMXCapability;
import org.jboss.osgi.spi.capability.LogServiceCapability;
import org.jboss.osgi.testing.OSGiBundle;
import org.jboss.osgi.testing.OSGiRuntime;
import org.jboss.osgi.testing.OSGiRuntimeTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;

/**
 * [JBOSGI-39] Bundle gets wired to an already uninstalled bundle
 * 
 * https://jira.jboss.org/jira/browse/JBOSGI-39
 * 
 * Bundle B depends on bundle X.
 * 
 * B ---> X 
 * 
 * @author thomas.diesler@jboss.com
 * @since 04-Mar-2009
 */
public class OSGi39TestCase extends OSGiRuntimeTest
{
   private OSGiRuntime runtime;

   @Before
   public void setUp() throws Exception
   {
      runtime = getDefaultRuntime();
      runtime.addCapability(new LogServiceCapability());
      runtime.addCapability(new JMXCapability());
      
      FrameworkMBeanExt frameworkMBean = (FrameworkMBeanExt)runtime.getFrameworkMBean();
      frameworkMBean.refreshBundles(null);
   }

   @After
   public void tearDown() throws Exception
   {
      runtime.shutdown();
   }

   @Test
   public void testVerifyUnresolved() throws Exception
   {
      OSGiBundle bundleB = runtime.installBundle("jbosgi39-bundleB.jar");
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

      OSGiBundle bundleX = runtime.installBundle("jbosgi39-bundleX.jar");

      bundleB.start();

      assertBundleState(Bundle.RESOLVED, bundleX.getState());
      assertBundleState(Bundle.ACTIVE, bundleB.getState());

      bundleB.uninstall();
      bundleX.uninstall();
   }

   /*
    * 4.3.11 Uninstalling Bundles
    * 
    * Once this method returns, the state of the OSGi Service Platform must be the same as if the bundle had never been installed, unless:
    * 
    * - The uninstalled bundle has exported any packages (via its Export-Package manifest header)
    * - The uninstalled bundle was selected by the Framework as the exporter of these packages.
    * 
    * If none of the old exports are used, then the old exports must be removed. Otherwise, all old exports must remain available
    * for existing bundles and future resolves until the refreshPackages method is called or the Framework is restarted.
    */
   @Test
   public void testWiringToUninstalled() throws Exception
   {
      OSGiBundle bundleX = runtime.installBundle("jbosgi39-bundleX.jar");
      OSGiBundle bundleB = runtime.installBundle("jbosgi39-bundleB.jar");

      bundleB.start();

      assertBundleState(Bundle.RESOLVED, bundleX.getState());
      assertBundleState(Bundle.ACTIVE, bundleB.getState());

      // Uninstall X before B
      bundleX.uninstall();
      bundleB.uninstall();

      // Install B without X
      bundleB = runtime.installBundle("jbosgi39-bundleB.jar");

      bundleB.start();

      assertBundleState(Bundle.ACTIVE, bundleB.getState());

      bundleB.uninstall();
   }

   @Test
   public void testWiringToUninstalledPackageAdmin() throws Exception
   {
      OSGiBundle bundleX = runtime.installBundle("jbosgi39-bundleX.jar");
      OSGiBundle bundleB = runtime.installBundle("jbosgi39-bundleB.jar");

      bundleB.start();

      assertBundleState(Bundle.RESOLVED, bundleX.getState());
      assertBundleState(Bundle.ACTIVE, bundleB.getState());

      // Uninstall X before B
      bundleX.uninstall();
      bundleB.uninstall();

      // Forces the update (replacement) or removal of packages exported by the specified bundles.
      FrameworkMBeanExt frameworkMBean = (FrameworkMBeanExt)runtime.getFrameworkMBean();
      frameworkMBean.refreshBundles(null);

      // Install B without X
      bundleB = runtime.installBundle("jbosgi39-bundleB.jar");

      try
      {
         bundleB.start();
         fail("Unresolved constraint expected");
      }
      catch (BundleException ex)
      {
         // expected
      }

      bundleX = runtime.installBundle("jbosgi39-bundleX.jar");

      bundleB.start();

      assertBundleState(Bundle.RESOLVED, bundleX.getState());
      assertBundleState(Bundle.ACTIVE, bundleB.getState());

      bundleB.uninstall();
      bundleX.uninstall();
   }
}