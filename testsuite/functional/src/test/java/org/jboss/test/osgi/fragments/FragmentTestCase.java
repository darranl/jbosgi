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
package org.jboss.test.osgi.fragments;

//$Id$

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.net.URL;

import org.jboss.osgi.jmx.BundleStateMBeanExt;
import org.jboss.osgi.jmx.FrameworkMBeanExt;
import org.jboss.osgi.jmx.JMXCapability;
import org.jboss.osgi.spi.NotImplementedException;
import org.jboss.osgi.spi.capability.LogServiceCapability;
import org.jboss.osgi.testing.OSGiBundle;
import org.jboss.osgi.testing.OSGiRuntime;
import org.jboss.osgi.testing.OSGiRuntimeTest;
import org.jboss.test.osgi.fragments.fragA.FragBeanA;
import org.jboss.test.osgi.fragments.subA.SubBeanA;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;

/**
 * Test Fragment functionality
 * 
 * @author thomas.diesler@jboss.com
 * @since 07-Jan-2010
 */
public class FragmentTestCase extends OSGiRuntimeTest
{
   private OSGiRuntime runtime;

   @Before
   public void setUp() throws Exception
   {
      runtime = getDefaultRuntime();
      runtime.addCapability(new LogServiceCapability());
      runtime.addCapability(new JMXCapability());
   }

   @After
   public void tearDown() throws Exception
   {
      if (runtime != null)
      {
         runtime.shutdown();
         runtime = null;
      }
   }

   @Test
   public void testHostOnly() throws Exception
   {
      // Bundle-SymbolicName: simple-hostA
      // Private-Package: org.jboss.test.osgi.fragments.hostA, org.jboss.test.osgi.fragments.subA 
      OSGiBundle hostA = runtime.installBundle("fragments-simple-hostA.jar");
      assertBundleState(Bundle.INSTALLED, hostA.getState());

      hostA.start();
      assertBundleState(Bundle.ACTIVE, hostA.getState());

      URL entryURL = hostA.getEntry("resources/resource.txt");
      assertNull("Entry URL null", entryURL);

      URL resourceURL = hostA.getResource("resources/resource.txt");
      assertNull("Resource URL null", resourceURL);

      // Load a private class
      OSGiBundle subBeanProvider = hostA.loadClass(SubBeanA.class.getName());
      assertEquals("Class provided by host", hostA, subBeanProvider);

      hostA.uninstall();
      assertBundleState(Bundle.UNINSTALLED, hostA.getState());
   }

   @Test
   public void testFragmentOnly() throws Exception
   {
      // Bundle-SymbolicName: simple-fragA
      // Export-Package: org.jboss.test.osgi.fragments.fragA
      // Include-Resource: resources/resource.txt=resource.txt
      // Fragment-Host: simple-hostA
      OSGiBundle fragA = runtime.installBundle("fragments-simple-fragA.jar");
      assertBundleState(Bundle.INSTALLED, fragA.getState());

      // Use the BundleStateMBeanExt.getEntry() instead of OSGiBundle.getEntry() 
      // to normalize the differences in VFS protocols when running against a VFS21 target container.
      BundleStateMBeanExt bundleState = (BundleStateMBeanExt)runtime.getBundleStateMBean();
      
      String entryURL = bundleState.getEntry(fragA.getBundleId(), "resources/resource.txt");
      assertNotNull("Entry URL not null", entryURL);

      String resourceURL = bundleState.getResource(fragA.getBundleId(), "resources/resource.txt");
      assertNull("Resource URL null", resourceURL);

      try
      {
         fragA.start();
         fail("Fragment bundles can not be started");
      }
      catch (BundleException e)
      {
         assertBundleState(Bundle.INSTALLED, fragA.getState());
      }

      fragA.uninstall();
      assertBundleState(Bundle.UNINSTALLED, fragA.getState());
   }

   @Test
   public void testAttachedFragment() throws Exception
   {
      // Bundle-SymbolicName: simple-hostA
      // Private-Package: org.jboss.test.osgi.fragments.hostA, org.jboss.test.osgi.fragments.subA 
      OSGiBundle hostA = runtime.installBundle("fragments-simple-hostA.jar");
      assertBundleState(Bundle.INSTALLED, hostA.getState());

      // Bundle-SymbolicName: simple-fragA
      // Export-Package: org.jboss.test.osgi.fragments.fragA
      // Include-Resource: resources/resource.txt=resource.txt
      // Fragment-Host: simple-hostA
      OSGiBundle fragA = runtime.installBundle("fragments-simple-fragA.jar");
      assertBundleState(Bundle.INSTALLED, fragA.getState());

      hostA.start();
      assertBundleState(Bundle.ACTIVE, hostA.getState());
      assertBundleState(Bundle.RESOLVED, fragA.getState());

      // Use the BundleStateMBeanExt.getEntry() instead of OSGiBundle.getEntry() 
      // to normalize the differences in VFS protocols when running against a VFS21 target container.
      BundleStateMBeanExt bundleState = (BundleStateMBeanExt)runtime.getBundleStateMBean();
      
      String entryURL = bundleState.getEntry(hostA.getBundleId(), "resources/resource.txt");
      assertNull("Entry URL null", entryURL);

      String resourceURL = bundleState.getResource(hostA.getBundleId(), "resources/resource.txt");
      assertNotNull("Resource URL not null", resourceURL);

      OSGiBundle fragBeanProvider = hostA.loadClass(FragBeanA.class.getName());
      assertEquals("Class provided by fragment", hostA, fragBeanProvider);

      // Load a private class
      OSGiBundle subBeanProvider = hostA.loadClass(SubBeanA.class.getName());
      assertEquals("Class provided by fragment", hostA, subBeanProvider);

      hostA.uninstall();
      assertBundleState(Bundle.UNINSTALLED, hostA.getState());
      assertBundleState(Bundle.RESOLVED, fragA.getState());

      fragA.uninstall();
      assertBundleState(Bundle.UNINSTALLED, fragA.getState());
   }

   @Test
   public void testHiddenPrivatePackage() throws Exception
   {
      if ("jbossmc".equals(getFrameworkName()))
      {
         System.out.println("FIXME [JBOSGI-339] Fragment failures in functional runtime tests");
         return;
      }
      
      // Bundle-SymbolicName: simple-hostA
      // Private-Package: org.jboss.test.osgi.fragments.hostA, org.jboss.test.osgi.fragments.subA 
      OSGiBundle hostA = runtime.installBundle("fragments-simple-hostA.jar");
      assertBundleState(Bundle.INSTALLED, hostA.getState());

      // Bundle-SymbolicName: simple-hostB
      // Export-Package: org.jboss.test.osgi.fragments.subA
      // Private-Package: org.jboss.test.osgi.fragments.hostB 
      OSGiBundle hostB = runtime.installBundle("fragments-simple-hostB.jar");
      assertBundleState(Bundle.INSTALLED, hostB.getState());

      // Bundle-SymbolicName: simple-fragB
      // Import-Package: org.jboss.test.osgi.fragments.subA
      // Fragment-Host: simple-hostA
      OSGiBundle fragB = runtime.installBundle("fragments-simple-fragB.jar");
      assertBundleState(Bundle.INSTALLED, fragB.getState());

      hostA.start();
      assertBundleState(Bundle.ACTIVE, hostA.getState());
      assertBundleState(Bundle.RESOLVED, fragB.getState());

      // The fragment contains an overwrites Private-Package with Import-Package
      // The SubBeanA is expected to come from HostB, which exports that package
      OSGiBundle subBeanProvider = hostA.loadClass(SubBeanA.class.getName());
      assertEquals("Class provided by host", hostB, subBeanProvider);

      hostA.uninstall();
      assertBundleState(Bundle.UNINSTALLED, hostA.getState());
      assertBundleState(Bundle.RESOLVED, fragB.getState());

      hostB.uninstall();
      assertBundleState(Bundle.UNINSTALLED, hostB.getState());

      fragB.uninstall();
      assertBundleState(Bundle.UNINSTALLED, fragB.getState());
   }

   @Test
   public void testFragmentExportsPackage() throws Exception
   {
      // Bundle-SymbolicName: simple-hostA
      // Private-Package: org.jboss.test.osgi.fragments.hostA, org.jboss.test.osgi.fragments.subA 
      OSGiBundle hostA = runtime.installBundle("fragments-simple-hostA.jar");
      assertBundleState(Bundle.INSTALLED, hostA.getState());

      // Bundle-SymbolicName: simple-hostC
      // Import-Package: org.jboss.test.osgi.fragments.fragA
      // Private-Package: org.jboss.test.osgi.fragments.hostC 
      OSGiBundle hostC = runtime.installBundle("fragments-simple-hostC.jar");
      assertBundleState(Bundle.INSTALLED, hostA.getState());

      hostA.start();
      assertBundleState(Bundle.ACTIVE, hostA.getState());

      try
      {
         // HostA does not export the package needed by HostC
         hostC.start();
         fail("Unresolved constraint expected");
      }
      catch (BundleException ex)
      {
         assertBundleState(Bundle.INSTALLED, hostC.getState());
      }

      // Bundle-SymbolicName: simple-fragA
      // Export-Package: org.jboss.test.osgi.fragments.fragA
      // Include-Resource: resources/resource.txt=resource.txt
      // Fragment-Host: simple-hostA
      OSGiBundle fragA = runtime.installBundle("fragments-simple-fragA.jar");
      assertBundleState(Bundle.INSTALLED, fragA.getState());

      try
      {
         // FragA does not attach to the aleady resolved HostA
         // HostA does not export the package needed by HostC
         hostC.start();
         fail("Unresolved constraint expected");
      }
      catch (BundleException ex)
      {
         assertBundleState(Bundle.INSTALLED, hostC.getState());
      }

      // Refreshing HostA causes the FragA to get attached
      FrameworkMBeanExt frameworkMBean = (FrameworkMBeanExt)runtime.getFrameworkMBean();
      try
      {
         frameworkMBean.refreshBundle(hostA.getBundleId());
      }
      catch (NotImplementedException ex)
      {
         System.out.println("FIXME [JBOSGI-336] Implement PackageAdmin.refreshPackages(Bundle[])");
         return;
      }

      // Wait for the fragment to get attached
      int timeout = 2000;
      while (timeout > 0 && fragA.getState() != Bundle.RESOLVED)
      {
         Thread.sleep(200);
         timeout -= 200;
      }

      // HostC should now resolve and start
      hostC.start();
      assertBundleState(Bundle.ACTIVE, hostC.getState());

      hostA.uninstall();
      assertBundleState(Bundle.UNINSTALLED, hostA.getState());

      hostC.uninstall();
      assertBundleState(Bundle.UNINSTALLED, hostC.getState());

      fragA.uninstall();
      assertBundleState(Bundle.UNINSTALLED, fragA.getState());
   }

   @Test
   public void testFragmentRequireBundle() throws Exception
   {
      if ("jbossmc".equals(getFrameworkName()))
      {
         System.out.println("FIXME [JBOSGI-339] Fragment failures in functional runtime tests");
         return;
      }
      
      // Bundle-SymbolicName: simple-hostA
      // Private-Package: org.jboss.test.osgi.fragments.hostA, org.jboss.test.osgi.fragments.subA 
      OSGiBundle hostA = runtime.installBundle("fragments-simple-hostA.jar");
      assertBundleState(Bundle.INSTALLED, hostA.getState());

      // Bundle-SymbolicName: simple-fragC
      // Export-Package: org.jboss.test.osgi.fragments.fragC
      // Require-Bundle: simple-hostB
      // Fragment-Host: simple-hostA
      OSGiBundle fragC = runtime.installBundle("fragments-simple-fragC.jar");
      assertBundleState(Bundle.INSTALLED, fragC.getState());

      try
      {
         // The attached FragA requires bundle HostB, which is not yet installed  
         hostA.start();

         // Clarify error behaviour when fragments fail to attach
         // https://www.osgi.org/members/bugzilla/show_bug.cgi?id=1524
         //
         // Felix: Merges FragC Require-Bundle into HostA and fails to resolve
         // Equinox: Resolves HostA but does not attach FragA
         if (hostA.getState() == Bundle.ACTIVE)
            assertBundleState(Bundle.INSTALLED, fragC.getState());
      }
      catch (BundleException ex)
      {
         assertBundleState(Bundle.INSTALLED, hostA.getState());
      }

      // Bundle-SymbolicName: simple-hostB
      // Export-Package: org.jboss.test.osgi.fragments.subA
      // Private-Package: org.jboss.test.osgi.fragments.hostB 
      OSGiBundle hostB = runtime.installBundle("fragments-simple-hostB.jar");
      assertBundleState(Bundle.INSTALLED, hostB.getState());

      // HostA should resolve and start after HostB got installed
      hostA.start();
      assertBundleState(Bundle.ACTIVE, hostA.getState());

      hostA.uninstall();
      assertBundleState(Bundle.UNINSTALLED, hostA.getState());

      fragC.uninstall();
      assertBundleState(Bundle.UNINSTALLED, fragC.getState());
   }
}