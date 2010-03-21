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
package org.jboss.test.osgi.services;

//$Id: StartLevelRemoteTestCase.java 87336 2009-04-15 11:31:26Z thomas.diesler@jboss.com $

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import org.jboss.osgi.spi.framework.OSGiBootstrap;
import org.jboss.osgi.spi.framework.OSGiBootstrapProvider;
import org.jboss.osgi.testing.OSGiTest;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.framework.launch.Framework;

/**
 * Test service related functionality.
 * 
 * @author thomas.diesler@jboss.com
 * @since 20-Mar-2010
 */
public class ServiceRegistrationTestCase extends OSGiTest
{
   public static Framework framework;

   @BeforeClass
   public static void beforeClass() throws BundleException
   {
      OSGiBootstrapProvider bootProvider = OSGiBootstrap.getBootstrapProvider();
      framework = bootProvider.getFramework();
      framework.start();
   }

   @AfterClass
   public static void afterClass() throws Exception
   {
      if (framework != null)
      {
         framework.stop();
         framework.waitForStop(2000);
         framework = null;
      }
   }

   @Test
   public void testUsingBundles() throws Exception
   {
      Runnable exp = new Runnable()
      {
         public void run()
         {
         }
      };

      BundleContext context = framework.getBundleContext();
      ServiceRegistration sreg = context.registerService(Runnable.class.getName(), exp, null);
      ServiceReference sref = sreg.getReference();

      Bundle[] users = sref.getUsingBundles();
      assertNull("Null users", users);

      Runnable was = (Runnable)context.getService(sref);
      users = sref.getUsingBundles();
      assertSame(exp, was);
      assertEquals(1, users.length);
      assertEquals(context.getBundle(), users[0]);

      was = (Runnable)context.getService(sref);
      users = sref.getUsingBundles();
      assertSame(exp, was);
      assertEquals(1, users.length);
      assertEquals(context.getBundle(), users[0]);
   }

   @Test
   public void testServiceFactoryUsingBundles() throws Exception
   {
      ServiceFactory factory = new ServiceFactory()
      {
         @Override
         public Object getService(Bundle bundle, ServiceRegistration sreg)
         {
            ServiceReference sref = sreg.getReference();
            Bundle[] users = sref.getUsingBundles();
            assertEquals(1, users.length);
            assertEquals(bundle, users[0]);
            
            return new Runnable()
            {
               public void run()
               {
               }
            };
         }

         @Override
         public void ungetService(Bundle bundle, ServiceRegistration registration, Object service)
         {
            // nothing to do
         }
      };
      BundleContext context = framework.getBundleContext();
      ServiceRegistration sreg = context.registerService(Runnable.class.getName(), factory, null);
      ServiceReference sref = sreg.getReference();

      Bundle[] users = sref.getUsingBundles();
      assertNull("Null users", users);

      Runnable was1 = (Runnable)context.getService(sref);
      users = sref.getUsingBundles();
      assertNotNull("Service not null", was1);
      assertEquals(1, users.length);
      assertEquals(context.getBundle(), users[0]);

      Runnable was2 = (Runnable)context.getService(sref);
      users = sref.getUsingBundles();
      assertSame(was1, was2);
      assertEquals(1, users.length);
      assertEquals(context.getBundle(), users[0]);
   }
}