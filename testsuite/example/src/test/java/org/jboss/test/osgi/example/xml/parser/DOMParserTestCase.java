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
package org.jboss.test.osgi.example.xml.parser;

//$Id$

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assume.assumeNotNull;

import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.jboss.osgi.husky.BridgeFactory;
import org.jboss.osgi.husky.HuskyCapability;
import org.jboss.osgi.husky.RuntimeContext;
import org.jboss.osgi.testing.OSGiBundle;
import org.jboss.osgi.testing.OSGiRuntime;
import org.jboss.osgi.testing.OSGiRuntimeHelper;
import org.jboss.osgi.xml.XMLParserCapability;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * A test that uses a DOM parser to read an XML document.
 * 
 * @see http://www.osgi.org/javadoc/r4v41/org/osgi/util/xml/XMLParserActivator.html
 * 
 * @author thomas.diesler@jboss.com
 * @since 21-Jul-2009
 */
public class DOMParserTestCase
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
         runtime = new OSGiRuntimeHelper().getDefaultRuntime();
         runtime.addCapability(new XMLParserCapability());
         runtime.addCapability(new HuskyCapability());

         OSGiBundle bundle = runtime.installBundle("example-xml-parser.jar");
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
   public void testDOMParser() throws Exception
   {
      // Tell Husky to run this test method within the OSGi Runtime
      if (context == null)
         BridgeFactory.getBridge().run();
      
      // Stop here if the context is not injected
      assumeNotNull(context);

      DocumentBuilder domBuilder = getDocumentBuilder();
      URL resURL = context.getBundle().getResource("example-xml-parser.xml");
      Document dom = domBuilder.parse(resURL.openStream());
      assertNotNull("Document not null", dom);
      
      Element root = dom.getDocumentElement();
      assertEquals("root", root.getLocalName());
      
      Node child = root.getFirstChild();
      assertEquals("child", child.getLocalName());
      assertEquals("content", child.getTextContent());
   }

   private DocumentBuilder getDocumentBuilder() throws ParserConfigurationException, InvalidSyntaxException
   {
      // This service gets registerd by the jboss-osgi-apache-xerces service
      String filter = "(" + XMLParserCapability.PARSER_PROVIDER + "=" + XMLParserCapability.PROVIDER_JBOSS_OSGI + ")";
      ServiceReference[] srefs = context.getServiceReferences(DocumentBuilderFactory.class.getName(), filter);
      if (srefs == null)
         throw new IllegalStateException("DocumentBuilderFactory not available");
      
      DocumentBuilderFactory factory = (DocumentBuilderFactory)context.getService(srefs[0]);
      factory.setValidating(false);
      
      DocumentBuilder domBuilder = factory.newDocumentBuilder();
      return domBuilder;
   }
}