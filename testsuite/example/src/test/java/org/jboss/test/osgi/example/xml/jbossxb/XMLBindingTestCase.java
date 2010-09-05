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

import java.io.InputStream;
import java.net.URL;

import javax.inject.Inject;

import org.jboss.arquillian.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.osgi.jbossxb.UnmarshallerService;
import org.jboss.osgi.testing.OSGiManifestBuilder;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.xb.annotations.JBossXmlSchema;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

/**
 * Test XMLBindingService
 * 
 * @author thomas.diesler@jboss.com
 * @since 26-Nov-2009
 */
@RunWith(Arquillian.class)
public class XMLBindingTestCase
{
   @Inject
   public Bundle bundle;

   @Deployment
   public static JavaArchive createdeployment()
   {
      final JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "example-xml-binding");
      archive.addClasses(CompanyType.class, ContactType.class, CourseBooking.class, StudentType.class);
      archive.addResource("xml/jbossxb/booking.xml", "booking.xml");
      archive.addResource("xml/jbossxb/booking.xsd", "booking.xsd");
      archive.setManifest(new Asset()
      {
         public InputStream openStream()
         {
            OSGiManifestBuilder builder = OSGiManifestBuilder.newInstance();
            builder.addBundleSymbolicName(archive.getName());
            builder.addBundleManifestVersion(2);
            builder.addImportPackages(UnmarshallerService.class, JBossXmlSchema.class);
            builder.addImportPackages("javax.xml.bind.annotation");
            return builder.openStream();
         }
      });
      return archive;
   }

   @Test
   public void testUnmarshaller() throws Exception
   {
      bundle.start();
      
      BundleContext context = bundle.getBundleContext();
      ServiceReference sref = context.getServiceReference(UnmarshallerService.class.getName());
      assertNotNull("UnmarshallerService available", sref);
      
      UnmarshallerService unmarshaller = (UnmarshallerService)context.getService(sref);
      unmarshaller.setSchemaValidation(true);
      unmarshaller.setNamespaceAware(true);
      unmarshaller.setValidation(true);
      
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
