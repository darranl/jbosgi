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
package org.jboss.test.osgi.service.startlevel;

//$Id: StartLevelRemoteTestCase.java 87336 2009-04-15 11:31:26Z thomas.diesler@jboss.com $

import org.jboss.osgi.spi.capability.CompendiumCapability;
import org.jboss.osgi.testing.OSGiBundle;
import org.jboss.osgi.testing.OSGiRuntime;
import org.jboss.osgi.testing.OSGiRuntimeTest;
import org.junit.Test;
import org.osgi.framework.Bundle;

/**
 * Deploy a bundle that accesses the StartLevel service
 * 
 * @author thomas.diesler@jboss.com
 * @since 04-Mar-2009
 */
public class StartLevelTestCase extends OSGiRuntimeTest
{
   @Test
   public void testStartLevel() throws Exception
   {
      OSGiRuntime runtime = getDefaultRuntime();
      try
      {
         runtime.addCapability(new CompendiumCapability());
         
         OSGiBundle bundle = runtime.installBundle("service/startlevel.jar");
         bundle.start();
         
         assertBundleState(Bundle.ACTIVE, bundle.getState());
         
         bundle.uninstall();
      }
      finally
      {
         runtime.shutdown();
      }
   }
}