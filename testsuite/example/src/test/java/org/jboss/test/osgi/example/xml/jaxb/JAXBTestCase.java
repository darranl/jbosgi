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
package org.jboss.test.osgi.example.xml.jaxb;

//$Id: DOMParserTestCase.java 91490 2009-07-21 08:39:45Z thomas.diesler@jboss.com $

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assume.assumeNotNull;

import java.net.URL;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.jboss.osgi.husky.BridgeFactory;
import org.jboss.osgi.husky.HuskyCapability;
import org.jboss.osgi.husky.RuntimeContext;
import org.jboss.osgi.jaxb.JAXBCapability;
import org.jboss.osgi.jaxb.JAXBService;
import org.jboss.osgi.testing.OSGiBundle;
import org.jboss.osgi.testing.OSGiRuntime;
import org.jboss.osgi.testing.OSGiRuntimeTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.packageadmin.PackageAdmin;

/**
 * A test that uses JAXB to read an XML document.
 * 
 * @author thomas.diesler@jboss.com
 * @since 21-Jul-2009
 */
public class JAXBTestCase
{
   @RuntimeContext
   public static BundleContext context;
   private static OSGiRuntime runtime;

   @Before
   public void beforeClass() throws Exception
   {
      // Only do this if we are not within the OSGi Runtime
      if (context == null)
      {
         runtime = OSGiRuntimeTest.createDefaultRuntime();
         runtime.addCapability(new JAXBCapability());
         runtime.addCapability(new HuskyCapability());

         OSGiBundle bundle = runtime.installBundle("example-xml-jaxb.jar");
         bundle.start();
      }
   }

   @After
   public void afterClass() throws Exception
   {
      // Only do this if we are not within the OSGi Runtime
      if (context == null)
      {
         runtime.shutdown();
         runtime = null;
      }
      context = null;
   }

   @Test
   public void testWiring() throws Exception
   {
      // Tell Husky to run this test method within the OSGi Runtime
      if (context == null)
         BridgeFactory.getBridge().run();
      
      // Stop here if the context is not injected
      assumeNotNull(context);

      ServiceReference sref = context.getServiceReference(PackageAdmin.class.getName());
      PackageAdmin packageAdmin = (PackageAdmin)context.getService(sref);
      
      Bundle serviceBundle = packageAdmin.getBundle(JAXBService.class);
      Bundle contextBundle = packageAdmin.getBundle(JAXBContext.class);
      
      // Test that the JAXBService as well as the JAXBContext come from the provided bundle
      assertEquals("jboss-osgi-jaxb", serviceBundle.getSymbolicName());
      assertEquals("jboss-osgi-jaxb", serviceBundle, contextBundle);
   }
   
   @Test
   @SuppressWarnings("unchecked")
   public void testUnmarshaller() throws Exception
   {
      // Tell Husky to run this test method within the OSGi Runtime
      if (context == null)
         BridgeFactory.getBridge().run();
      
      // Stop here if the context is not injected
      assumeNotNull(context);

      JAXBContext jaxbContext = getJAXBContext();
      Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
      URL resURL = context.getBundle().getResource("booking.xml");
      JAXBElement<CourseBooking> rootElement = (JAXBElement<CourseBooking>)unmarshaller.unmarshal(resURL.openStream());
      assertNotNull("root element not null", rootElement);
      
      CourseBooking booking = rootElement.getValue();
      assertNotNull("booking not null", booking);
      
      CompanyType company = booking.getCompany();
      assertNotNull("company not null", company);
      assertEquals("ACME Consulting", company.getName());
   }

   private JAXBContext getJAXBContext() throws JAXBException 
   {
      // This service gets registerd by the jboss-osgi-apache-xerces service
      ServiceReference sref = context.getServiceReference(JAXBService.class.getName());
      if (sref == null)
         throw new IllegalStateException("JAXBService not available");
      
      JAXBService service = (JAXBService)context.getService(sref);
      JAXBContext jaxbContext = service.newJAXBContext(getClass().getPackage().getName());
      return jaxbContext;
   }
}