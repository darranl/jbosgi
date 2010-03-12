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
package org.jboss.test.osgi.jbossas.jbosgi36;

//$Id: OSGI36TestCase.java 86968 2009-04-08 15:51:12Z thomas.diesler@jboss.com $

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.jboss.osgi.jmx.JMXCapability;
import org.jboss.osgi.jmx.MBeanProxy;
import org.jboss.osgi.testing.OSGiRuntimeTest;
import org.jboss.osgi.testing.OSGiRuntimeHelper;
import org.jboss.osgi.testing.internal.RemoteRuntime;
import org.jboss.test.osgi.jbossas.jbosgi36.mbean.FooMBean;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * [JBOSGI-36] Bundle classes leak into system classloader
 * 
 * https://jira.jboss.org/jira/browse/JBOSGI-36
 * 
 * @author thomas.diesler@jboss.com
 * @since 25-Feb-2009
 */
public class OSGi36TestCase extends OSGiRuntimeTest
{
   private static RemoteRuntime runtime;

   @BeforeClass
   public static void setUpClass() throws Exception
   {
      runtime = (RemoteRuntime)new OSGiRuntimeHelper().getRemoteRuntime();
      runtime.addCapability(new JMXCapability());

      runtime.installBundle("jbosgi36-bundle.jar");
      runtime.deploy("jbosgi36-mbean.jar");
   }

   @AfterClass
   public static void tearDownClass() throws Exception
   {
      if (runtime != null)
      {
         runtime.undeploy("jbosgi36-mbean.jar");
         runtime.shutdown();
         runtime = null;
      }
   }

   @Test
   public void testAccessMBean() throws Exception
   {
      assertEquals("hello", getFooMBean().echo("hello"));
   }

   @Test
   public void testAccessSomeService() throws Exception
   {
      try
      {
         String loaderName = getFooMBean().accessSomeService();
         fail("Unexpected classloader: " + loaderName);
      }
      catch (ClassNotFoundException ex)
      {
         // expected
      }
   }

   @Test
   public void testAccessSomeInternal() throws Exception
   {
      try
      {
         String loaderName = getFooMBean().accessSomeInternal();
         fail("Unexpected classloader: " + loaderName);
      }
      catch (ClassNotFoundException ex)
      {
         // expected
      }
   }

   private FooMBean getFooMBean() 
   {
      FooMBean foo = (FooMBean)MBeanProxy.get(runtime.getMBeanServer(), FooMBean.OBJECT_NAME, FooMBean.class);
      return foo;
   }
}