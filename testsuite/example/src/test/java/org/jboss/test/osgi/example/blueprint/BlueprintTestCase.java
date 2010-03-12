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
package org.jboss.test.osgi.example.blueprint;

//$Id$

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assume.assumeNotNull;

import javax.management.MBeanServer;

import org.jboss.osgi.blueprint.BlueprintCapability;
import org.jboss.osgi.husky.BridgeFactory;
import org.jboss.osgi.husky.HuskyCapability;
import org.jboss.osgi.husky.RuntimeContext;
import org.jboss.osgi.jmx.JMXCapability;
import org.jboss.osgi.testing.OSGiBundle;
import org.jboss.osgi.testing.OSGiRuntime;
import org.jboss.osgi.testing.OSGiRuntimeHelper;
import org.jboss.test.osgi.example.blueprint.bundle.BeanA;
import org.jboss.test.osgi.example.blueprint.bundle.ServiceA;
import org.jboss.test.osgi.example.blueprint.bundle.ServiceB;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.blueprint.container.BlueprintContainer;

/**
 * A simple Blueprint Container test.
 * 
 * @author thomas.diesler@jboss.com
 * @since 12-Jul-2009
 */
public class BlueprintTestCase
{
   @RuntimeContext
   public static BundleContext context;
   private static OSGiRuntime runtime;

   @BeforeClass
   public static void beforeClass() throws Exception
   {
      if (context == null)
      {
         runtime = new OSGiRuntimeHelper().getDefaultRuntime();
         runtime.addCapability(new HuskyCapability());
         runtime.addCapability(new JMXCapability());
         runtime.addCapability(new BlueprintCapability());
         
         OSGiBundle bundle = runtime.installBundle("example-blueprint.jar");
         bundle.start();
      }
   }

   @AfterClass
   public static void afterClass() throws Exception
   {
      if (context == null)
      {
         runtime.shutdown();
         runtime = null;
      }
      context = null;
   }

   @Test
   public void testBlueprintContainerAvailable() throws Exception
   {
      if (context == null)
         BridgeFactory.getBridge().run();
      
      assumeNotNull(context);
      
      Bundle bundle = context.getBundle();
      assertEquals("example-blueprint", bundle.getSymbolicName());
      
      BlueprintContainer bpContainer = getBlueprintContainer();
      assertNotNull("BlueprintContainer available", bpContainer);
   }

   @Test
   public void testServiceA() throws Exception
   {
      if (context == null)
         BridgeFactory.getBridge().run();
      
      assumeNotNull(context);

      ServiceReference sref = context.getServiceReference(ServiceA.class.getName());
      assertNotNull("ServiceA not null", sref);
      
      ServiceA service = (ServiceA)context.getService(sref);
      MBeanServer mbeanServer = service.getMbeanServer();
      assertNotNull("MBeanServer not null", mbeanServer);
   }

   @Test
   public void testServiceB() throws Exception
   {
      if (context == null)
         BridgeFactory.getBridge().run();
      
      assumeNotNull(context);

      ServiceReference sref = context.getServiceReference(ServiceB.class.getName());
      assertNotNull("ServiceB not null", sref);
      
      ServiceB service = (ServiceB)context.getService(sref);
      BeanA beanA = service.getBeanA();
      assertNotNull("BeanA not null", beanA);
   }

   private BlueprintContainer getBlueprintContainer() throws Exception
   {
      // 10sec for processing of STARTING event
      int timeout = 10000;
      
      ServiceReference sref = null;
      while (sref == null && 0 < (timeout -= 200))
      {
         String filter = "(osgi.blueprint.container.symbolicname=example-blueprint)";
         ServiceReference[] srefs = context.getServiceReferences(BlueprintContainer.class.getName(), filter);
         if (srefs != null && srefs.length > 0)
            sref = srefs[0];
         
         Thread.sleep(200);
      }
      assertNotNull("BlueprintContainer not null", sref);
      
      BlueprintContainer bpContainer = (BlueprintContainer)context.getService(sref);
      return bpContainer;
   }
}