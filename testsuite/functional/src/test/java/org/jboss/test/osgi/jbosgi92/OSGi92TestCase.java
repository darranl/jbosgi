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
package org.jboss.test.osgi.jbosgi92;

//$Id$

import static org.junit.Assert.assertEquals;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParserFactory;

import org.jboss.osgi.spi.capability.CompendiumCapability;
import org.jboss.osgi.testing.OSGiBundle;
import org.jboss.osgi.testing.OSGiRuntime;
import org.jboss.osgi.testing.OSGiRuntimeTest;
import org.jboss.osgi.testing.OSGiServiceReference;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;

/**
 * [JBOSGI-92] Class.forName issue with XMLParserActivator
 * 
 * https://jira.jboss.org/jira/browse/JBOSGI-92
 * 
 * @author thomas.diesler@jboss.com
 * @since 21-Jul-2009
 */
public class OSGi92TestCase extends OSGiRuntimeTest
{
   private static OSGiRuntime runtime;

   @BeforeClass
   public static void beforeClass() throws Exception
   {
      runtime = createDefaultRuntime();
      runtime.addCapability(new CompendiumCapability());
   }

   @AfterClass
   public static void afterClass() throws BundleException
   {
      if (runtime != null)
      {
         runtime.shutdown();
         runtime = null;
      }
   }

   @Test
   public void testDeployParsers() throws Exception
   {
      OSGiBundle bundleA = runtime.installBundle("jbosgi92-bundleA.jar");
      bundleA.start();

      assertBundleState(Bundle.ACTIVE, bundleA.getState());
      
      String filter = "(parser.factoryname=org.jboss.test.osgi.jbosgi92.bundleA.*)";
      OSGiServiceReference[] domRefs = runtime.getServiceReferences(DocumentBuilderFactory.class.getName(), filter);
      assertEquals("DocumentBuilderFactory service available", 1, domRefs.length);

      OSGiServiceReference[] saxRefs = runtime.getServiceReferences(SAXParserFactory.class.getName(), filter);
      assertEquals("SAXParserFactory service available", 1, saxRefs.length);
   }
}