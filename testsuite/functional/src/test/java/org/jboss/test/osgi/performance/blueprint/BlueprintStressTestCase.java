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
package org.jboss.test.osgi.performance.blueprint;

//$Id: BlueprintTestCase.java 91550 2009-07-22 13:10:41Z thomas.diesler@jboss.com $

import static org.junit.Assert.assertNotNull;

import org.jboss.osgi.blueprint.BlueprintCapability;
import org.jboss.osgi.testing.OSGiBundle;
import org.jboss.osgi.testing.OSGiRuntime;
import org.jboss.osgi.testing.OSGiServiceReference;
import org.jboss.osgi.testing.OSGiTestHelper;
import org.jboss.test.osgi.performance.blueprint.bundle.ServiceA;
import org.jboss.test.osgi.performance.blueprint.bundle.ServiceB;
import org.junit.Test;
import org.osgi.service.blueprint.container.BlueprintContainer;

/**
 * A simple Blueprint Container test.
 * 
 * @author thomas.diesler@jboss.com
 * @since 12-Jul-2009
 */
public class BlueprintStressTestCase
{
   @Test
   public void testBlueprintBundleInstall() throws Exception
   {
      OSGiRuntime runtime = new OSGiTestHelper().getDefaultRuntime();
      BlueprintCapability blueprintCapability = new BlueprintCapability();
      try
      {
         long lastTime = System.currentTimeMillis();
         for (int i = 0; i < 100; i++)
         {
            runtime.addCapability(blueprintCapability);

            OSGiBundle bundle = runtime.installBundle("performance-blueprint.jar");
            bundle.start();

            OSGiServiceReference sref = runtime.getServiceReference(BlueprintContainer.class.getName());
            assertNotNull("BlueprintContainer service not null", sref);

            OSGiServiceReference srefA = runtime.getServiceReference(ServiceA.class.getName());
            assertNotNull("ServiceA not null", srefA);

            OSGiServiceReference srefB = runtime.getServiceReference(ServiceB.class.getName());
            assertNotNull("ServiceB not null", srefB);

            bundle.uninstall();
            runtime.removeCapability(blueprintCapability);

            long currTime = System.currentTimeMillis();
            System.out.println("#" + (i + 1) + " " + (currTime - lastTime) + "ms ");

            lastTime = currTime;
         }
      }
      finally
      {
         runtime.shutdown();
      }
   }
}