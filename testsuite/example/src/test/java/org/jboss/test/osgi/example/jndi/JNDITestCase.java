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
package org.jboss.test.osgi.example.jndi;

//$Id: JNDITestCase.java 88356 2009-05-07 13:13:26Z thomas.diesler@jboss.com $

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;

import org.jboss.osgi.jndi.JNDICapability;
import org.jboss.osgi.testing.OSGiBundle;
import org.jboss.osgi.testing.OSGiRuntime;
import org.jboss.osgi.testing.OSGiRuntimeTest;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * A test that deployes a bundle that binds a String to JNDI
 * 
 * @author thomas.diesler@jboss.com
 * @since 05-May-2009
 */
public class JNDITestCase extends OSGiRuntimeTest
{
   private static OSGiRuntime runtime;

   @BeforeClass
   public static void setUpClass() throws Exception
   {
      runtime = createDefaultRuntime();
      runtime.addCapability(new JNDICapability());
   }

   @AfterClass
   public static void tearDownClass() throws Exception
   {
      runtime.shutdown();
      runtime = null;
   }

   @Test
   public void testJNDIAccess() throws Exception
   {
      OSGiBundle bundle = runtime.installBundle("example-jndi.jar");
      bundle.start();

      InitialContext iniCtx = runtime.getInitialContext();
      String lookup = (String)iniCtx.lookup("test/Foo");
      assertEquals("JNDI bound String expected", "Bar", lookup);

      // Uninstall should unbind the object
      bundle.uninstall();

      try
      {
         iniCtx.lookup("test/Foo");
         fail("NameNotFoundException expected");
      }
      catch (NameNotFoundException ex)
      {
         // expected
      }
   }
}