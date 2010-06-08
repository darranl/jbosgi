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
package org.jboss.test.osgi.services.startlevel;

//$Id: StartLevelRemoteTestCase.java 87336 2009-04-15 11:31:26Z thomas.diesler@jboss.com $

import static org.junit.Assert.assertNotNull;
import static org.junit.Assume.assumeNotNull;

import org.jboss.osgi.husky.BridgeFactory;
import org.jboss.osgi.husky.HuskyCapability;
import org.jboss.osgi.husky.RuntimeContext;
import org.jboss.osgi.testing.OSGiBundle;
import org.jboss.osgi.testing.OSGiRuntime;
import org.jboss.osgi.testing.OSGiRuntimeTest;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

/**
 * Deploy a bundle that accesses the StartLevel service
 * 
 * @author thomas.diesler@jboss.com
 * @since 04-Mar-2009
 */
public class StartLevelTestCase extends OSGiRuntimeTest
{
   @RuntimeContext
   public BundleContext context;

   @Before
   public void setUp() throws Exception
   {
      // Only do this if we are not within the OSGi Runtime
      if (context == null)
      {
         // Get the default runtime
         OSGiRuntime runtime = getDefaultRuntime();
         runtime.addCapability(new HuskyCapability());
         
         // Install the bundle
         OSGiBundle bundle = runtime.installBundle("service-startlevel.jar");
         bundle.start();
      }
   }
   
   @Test
   public void testStartLevel() throws Exception
   {
      // Tell Husky to run this test method within the OSGi Runtime
      if (context == null)
         BridgeFactory.getBridge().run();
      
      // Stop here if the context is not injected
      assumeNotNull(context);
      
      Bundle bundle = null;
      for (Bundle aux : context.getBundles())
      {
         System.out.println(aux);
         if ("service-startlevel".equals(aux.getSymbolicName()))
            bundle = aux;
      }
      
      assertNotNull("Test bundle found", bundle);
   }
}