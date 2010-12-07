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


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.InputStream;
import java.net.URL;

import javax.inject.Inject;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.jboss.arquillian.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.osgi.jaxb.JAXBService;
import org.jboss.osgi.testing.OSGiManifestBuilder;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
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
@RunWith(Arquillian.class)
public class JAXBTestCase
{
   @Inject
   public BundleContext context;

   @Inject
   public Bundle bundle;

   @Deployment
   public static JavaArchive createdeployment()
   {
      final JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "example-jaxb");
      archive.addClasses(CompanyType.class, ContactType.class, CourseBooking.class, ObjectFactory.class, StudentType.class);
      archive.addResource("xml/jaxb/booking.xml", "booking.xml");
      archive.addResource("xml/jaxb/booking.xsd", "booking.xsd");
      archive.setManifest(new Asset()
      {
         public InputStream openStream()
         {
            OSGiManifestBuilder builder = OSGiManifestBuilder.newInstance();
            builder.addBundleSymbolicName(archive.getName());
            builder.addBundleManifestVersion(2);
            builder.addImportPackages("com.sun.xml.bind.v2", "javax.xml.bind", "javax.xml.bind.annotation");
            builder.addImportPackages("javax.xml.datatype", "javax.xml.namespace", "org.apache.xerces.jaxp.datatype", "org.jboss.osgi.xml");
            return builder.openStream();
         }
      });
      return archive;
   }

   @Test
   public void testWiring() throws Exception
   {
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
      bundle.start();

      JAXBContext jaxbContext = getJAXBContext();
      Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
      URL resURL = bundle.getResource("booking.xml");
      JAXBElement<CourseBooking> rootElement = (JAXBElement<CourseBooking>)unmarshaller.unmarshal(resURL.openStream());
      assertNotNull("root element not null", rootElement);

      CourseBooking booking = rootElement.getValue();
      assertNotNull("booking not null", booking);

      CompanyType company = booking.getCompany();
      assertNotNull("company not null", company);
      assertEquals("ACME Consulting", company.getName());
   }

   private JAXBService getJAXBService() throws JAXBException
   {
      // This service gets registerd by the jboss-osgi-jaxb activator
      BundleContext context = bundle.getBundleContext();
      ServiceReference sref = context.getServiceReference(JAXBService.class.getName());
      if (sref == null)
         throw new IllegalStateException("JAXBService not available");

      JAXBService jaxbService = (JAXBService)context.getService(sref);
      return jaxbService;
   }

   private JAXBContext getJAXBContext() throws JAXBException
   {
      JAXBContext jaxbContext = getJAXBService().newJAXBContext(getClass().getPackage().getName());
      return jaxbContext;
   }
}