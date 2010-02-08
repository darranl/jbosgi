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
package org.jboss.test.osgi.example.microcontainer;

//$Id$

import static org.jboss.test.osgi.example.microcontainer.bundleA.SomeBeanMBean.MBEAN_NAME;
import static org.junit.Assert.assertEquals;

import org.jboss.osgi.jmx.JMXCapability;
import org.jboss.osgi.spi.management.MBeanProxy;
import org.jboss.osgi.testing.OSGiBundle;
import org.jboss.osgi.testing.OSGiRuntime;
import org.jboss.osgi.testing.OSGiTestHelper;
import org.jboss.test.osgi.example.microcontainer.bundleA.SomeBeanMBean;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * A test that calls a service from an MC bean and vica versa
 * 
 * @author thomas.diesler@jboss.com
 * @since 12-Feb-2009
 */
public class MicrocontainerTestCase
{
   static OSGiRuntime runtime;
   static OSGiBundle bundleA;

   @BeforeClass
   public static void setUpClass() throws Exception
   {
      runtime = new OSGiTestHelper().getDefaultRuntime();
      runtime.addCapability(new JMXCapability());

      bundleA = runtime.installBundle("example-mcservice-bundleA.jar");
      bundleA.start();
   }

   @AfterClass
   public static void tearDownClass() throws Exception
   {
      runtime.shutdown();
      runtime = null;
      bundleA = null;
   }

   @Test
   public void testBeanAccess() throws Exception
   {
      SomeBeanMBean someBean = MBeanProxy.get(SomeBeanMBean.class, MBEAN_NAME, runtime.getMBeanServer());
      assertEquals("hello", someBean.echo("hello"));
   }

   @Test
   public void testServiceAccess() throws Exception
   {
      SomeBeanMBean someBean = MBeanProxy.get(SomeBeanMBean.class, MBEAN_NAME, runtime.getMBeanServer());
      assertEquals("hello", someBean.callSomeService("hello"));
   }
}