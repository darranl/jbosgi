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
package org.jboss.test.osgi.example.enterprise.jmx;

//$Id: JMXTestCase.java 95465 2009-10-23 05:59:57Z thomas.diesler@jboss.com $

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.openmbean.TabularData;

import org.jboss.osgi.jmx.JMXCapability;
import org.jboss.osgi.spi.management.MBeanProxy;
import org.jboss.osgi.spi.management.ObjectNameFactory;
import org.jboss.osgi.testing.OSGiRuntime;
import org.jboss.osgi.testing.OSGiTest;
import org.jboss.osgi.testing.OSGiTestHelper;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.osgi.jmx.framework.BundleStateMBean;

/**
 * A test that deployes a bundle that registeres an MBean
 * 
 * @author thomas.diesler@jboss.com
 * @since 15-Feb-2010
 */
public class JMXEnterpriseTestCase extends OSGiTest
{
   private static OSGiRuntime runtime;
   
   private BundleStateMBean bundleState;

   @BeforeClass
   public static void setUpClass() throws Exception
   {
      OSGiTestHelper helper = new OSGiTestHelper();
      runtime = helper.getDefaultRuntime();
      runtime.addCapability(new JMXCapability());
      
      // Optionally install/start the Apache Aries JMX bundle
      if (runtime.getBundle("org.apache.aries.jmx", null) == null)
         runtime.installBundle("bundles/org.apache.aries.jmx.jar").start();
   }

   @AfterClass
   public static void tearDownClass() throws Exception
   {
      runtime.shutdown();
      runtime = null;
   }

   @Test
   public void testBundleStateMBean() throws Exception
   {
      BundleStateMBean bundleState = getBundleStateMBean();
      assertNotNull("BundleStateMBean not null", bundleState);
      
      TabularData bundleData = bundleState.listBundles();
      assertNotNull("TabularData not null", bundleData);
      assertFalse("TabularData not empty", bundleData.isEmpty());
   }

   private BundleStateMBean getBundleStateMBean() throws Exception
   {
      if (bundleState == null)
      {
         int timeout = 5000;
         MBeanServerConnection mbeanServer = runtime.getMBeanServer();
         ObjectName objectName = ObjectNameFactory.create(BundleStateMBean.OBJECTNAME);
         while (bundleState == null && 0 < (timeout -= 200))
         {
            if (mbeanServer.isRegistered(objectName))
            {
               bundleState = MBeanProxy.get(BundleStateMBean.class, objectName, mbeanServer);
               break;
            }
            Thread.sleep(200);
         }
      }
      return bundleState;
   }
}