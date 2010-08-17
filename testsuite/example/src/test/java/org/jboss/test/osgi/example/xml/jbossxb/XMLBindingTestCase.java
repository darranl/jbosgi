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
package org.jboss.test.osgi.example.xml.jbossxb;

//$Id$

import static org.junit.Assert.assertNotNull;
import static org.junit.Assume.assumeNotNull;

import java.net.URL;

import org.jboss.osgi.husky.BridgeFactory;
import org.jboss.osgi.husky.HuskyCapability;
import org.jboss.osgi.husky.RuntimeContext;
import org.jboss.osgi.jbossxb.UnmarshallerService;
import org.jboss.osgi.jbossxb.XMLBindingCapability;
import org.jboss.osgi.testing.OSGiBundle;
import org.jboss.osgi.testing.OSGiRuntime;
import org.jboss.osgi.testing.OSGiRuntimeTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;

/**
 * Test XMLBindingService
 * 
 * @author thomas.diesler@jboss.com
 * @since 26-Nov-2009
 */
public class XMLBindingTestCase extends OSGiRuntimeTest
{
   @RuntimeContext
   public BundleContext context;

   private OSGiRuntime runtime;

   @Before
   public void setUp() throws Exception
   {
      if (context == null)
      {
         runtime = createDefaultRuntime();
         runtime.addCapability(new HuskyCapability());
         runtime.addCapability(new XMLBindingCapability());
         
         OSGiBundle bundle = runtime.installBundle("example-xml-binding.jar");
         bundle.start();
      }
   }

   @After
   public void tearDown() throws BundleException
   {
      if (context == null)
         runtime.shutdown();
   }

   @Test
   public void testUnmarshaller() throws Exception
   {
      if (context == null)
         BridgeFactory.getBridge().run();
      
      assumeNotNull(context);
      
      ServiceReference sref = context.getServiceReference(UnmarshallerService.class.getName());
      assertNotNull("UnmarshallerService available", sref);
      
      UnmarshallerService unmarshaller = (UnmarshallerService)context.getService(sref);
      unmarshaller.setSchemaValidation(true);
      unmarshaller.setNamespaceAware(true);
      unmarshaller.setValidation(true);
      
      Bundle bundle = context.getBundle();
      URL xsdurl = bundle.getEntry("booking.xsd");
      assertNotNull("booking.xsd available", xsdurl);
      
      URL xmlurl = bundle.getEntry("booking.xml");
      assertNotNull("booking.xml available", xmlurl);
      
      unmarshaller.registerSchemaLocation("http://org.jboss.test.osgi.jbossxb.simple/booking.xsd", xsdurl.toExternalForm());
      unmarshaller.addClassBinding(CourseBooking.NAMESPACE_XML_SIMPLE, CourseBooking.class);
      
      CourseBooking booking = (CourseBooking)unmarshaller.unmarshal(xmlurl.toExternalForm());
      assertNotNull("booking not null", booking);
   }
}
