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
package org.jboss.test.osgi.jbosgi142;

//$Id: OSGI142TestCase.java 87103 2009-04-09 22:18:31Z thomas.diesler@jboss.com $


import org.jboss.osgi.spi.framework.OSGiBootstrap;
import org.jboss.osgi.spi.framework.OSGiBootstrapProvider;
import org.jboss.osgi.testing.OSGiTest;
import org.jboss.test.osgi.jbosgi142.bundleA.BeanA;
import org.jboss.test.osgi.jbosgi142.bundleB.BeanB;
import org.jboss.test.osgi.jbosgi142.bundleX.BeanX;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.launch.Framework;

/**
 * [JBOSGI-142] Investigate classloading space
 * 
 * https://jira.jboss.org/jira/browse/JBOSGI-142
 * 
 * A imports X
 * B imports X
 * 
 * Can X load a class from A or B?
 * Can A load a class from B and vice versa?
 * 
 * @author thomas.diesler@jboss.com
 * @since 28-Aug-2009
 */
public class OSGi142TestCase extends OSGiTest
{
   @Test
   public void testLoadClass() throws Exception
   {
      OSGiBootstrapProvider bootProvider = OSGiBootstrap.getBootstrapProvider();
      Framework framework = bootProvider.getFramework();
      try
      {
         framework.start();
         
         BundleContext sysContext = framework.getBundleContext();
         Bundle bundleX = sysContext.installBundle(getTestArchiveURL("jbosgi142-bundleX.jar").toExternalForm());
         bundleX.start();
         
         assertBundleLoadClass(bundleX, BeanX.class.getName(), true);
         
         Bundle bundleA = sysContext.installBundle(getTestArchiveURL("jbosgi142-bundleA.jar").toExternalForm());
         bundleA.start();
         
         assertBundleLoadClass(bundleA, BeanA.class.getName(), true);
         
         Bundle bundleB = sysContext.installBundle(getTestArchiveURL("jbosgi142-bundleB.jar").toExternalForm());
         bundleB.start();
         
         assertBundleLoadClass(bundleB, BeanB.class.getName(), true);
         
         assertBundleLoadClass(bundleA, BeanX.class.getName(), true);
         assertBundleLoadClass(bundleB, BeanX.class.getName(), true);
    
         assertBundleLoadClass(bundleX, BeanA.class.getName(), false);
         assertBundleLoadClass(bundleX, BeanB.class.getName(), false);
         
         assertBundleLoadClass(bundleA, BeanB.class.getName(), false);
         assertBundleLoadClass(bundleB, BeanA.class.getName(), false);
      }
      finally
      {
         framework.stop();
         framework.waitForStop(1000);
      }
   }
}