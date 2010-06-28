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

//$Id$

import static org.jboss.test.osgi.example.jmx.bundle.FooMBean.MBEAN_NAME;
import static org.junit.Assert.assertEquals;

import org.jboss.osgi.jmx.JMXCapability;
import org.jboss.osgi.jndi.JNDICapability;
import org.jboss.osgi.testing.OSGiBundle;
import org.jboss.osgi.testing.OSGiRuntime;
import org.jboss.osgi.testing.OSGiRuntimeHelper;
import org.jboss.test.osgi.example.jmx.bundle.FooMBean;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * A test that deployes a bundle that registeres an MBean
 * 
 * @author thomas.diesler@jboss.com
 * @since 12-Feb-2009
 */
public class MBeanServerTestCase
{
   private static OSGiRuntime runtime;

   @BeforeClass
   public static void setUpClass() throws Exception
   {
      runtime = new OSGiRuntimeHelper().getDefaultRuntime();
      runtime.addCapability(new JNDICapability());
      runtime.addCapability(new JMXCapability());
   }

   @AfterClass
   public static void tearDownClass() throws Exception
   {
      runtime.shutdown();
      runtime = null;
   }

   @Test
   public void testMBeanAccess() throws Exception
   {
      OSGiBundle bundle = runtime.installBundle("example-jmx.jar");
      try
      {
         bundle.start();

         FooMBean foo = runtime.getMBeanProxy(MBEAN_NAME, FooMBean.class);
         assertEquals("hello", foo.echo("hello"));
      }
      finally
      {
         bundle.uninstall();
      }
   }
}