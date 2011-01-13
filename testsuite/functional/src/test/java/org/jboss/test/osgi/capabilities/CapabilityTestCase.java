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

package org.jboss.test.osgi.capabilities;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import javax.management.MBeanServer;
import javax.naming.InitialContext;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParserFactory;

import org.jboss.osgi.jaxb.JAXBCapability;
import org.jboss.osgi.jaxb.JAXBService;
import org.jboss.osgi.jmx.JMXCapability;
import org.jboss.osgi.jndi.JNDICapability;
import org.jboss.osgi.spi.capability.CompendiumCapability;
import org.jboss.osgi.spi.capability.LogServiceCapability;
import org.jboss.osgi.testing.OSGiBundle;
import org.jboss.osgi.testing.OSGiRuntime;
import org.jboss.osgi.testing.OSGiRuntimeTest;
import org.jboss.osgi.testing.OSGiServiceReference;
import org.jboss.osgi.xml.XMLParserCapability;
import org.junit.Test;

/**
 * Test OSGi runtime capabilities
 * 
 * @author thomas.diesler@jboss.com
 * @since 23-Jan-2009
 */
public class CapabilityTestCase extends OSGiRuntimeTest {
    @Test
    public void testXMLParserCapability() throws Exception {
        OSGiRuntime runtime = createEmbeddedRuntime();
        try {
            runtime.addCapability(new LogServiceCapability());

            OSGiBundle bundle = runtime.getBundle("jboss-osgi-apache-xerces", null);
            assertNull("Test bundle null", bundle);

            runtime.addCapability(new XMLParserCapability());

            String filter = "(" + XMLParserCapability.PARSER_PROVIDER + "=" + XMLParserCapability.PROVIDER_JBOSS_OSGI + ")";
            OSGiServiceReference[] saxRefs = runtime.getServiceReferences(SAXParserFactory.class.getName(), filter);
            assertNotNull("SAXParserFactory registered", saxRefs);
            assertEquals("SAXParserFactory registered", 1, saxRefs.length);

            OSGiServiceReference saxRef = saxRefs[0];
            assertEquals("namespaceAware", Boolean.TRUE, saxRef.getProperty("parser.namespaceAware"));
            assertEquals("validating", Boolean.TRUE, saxRef.getProperty("parser.validating"));
            assertEquals("xincludeAware", Boolean.TRUE, saxRef.getProperty("parser.xincludeAware"));

            OSGiServiceReference[] domRefs = runtime.getServiceReferences(DocumentBuilderFactory.class.getName(), filter);
            assertNotNull("DocumentBuilderFactory registered", domRefs);
            assertEquals("DocumentBuilderFactory registered", 1, domRefs.length);

            OSGiServiceReference domRef = domRefs[0];
            assertEquals("namespaceAware", Boolean.TRUE, domRef.getProperty("parser.namespaceAware"));
            assertEquals("validating", Boolean.TRUE, domRef.getProperty("parser.validating"));
            assertEquals("xincludeAware", Boolean.TRUE, domRef.getProperty("parser.xincludeAware"));
        } finally {
            runtime.shutdown();
        }
    }

    @Test
    public void testJAXBCapability() throws Exception {
        OSGiRuntime runtime = createEmbeddedRuntime();
        try {
            runtime.addCapability(new LogServiceCapability());

            OSGiBundle bundle = runtime.getBundle("jboss-osgi-jaxb", null);
            assertNull("Test bundle null", bundle);

            runtime.addCapability(new JAXBCapability());

            OSGiServiceReference sref = runtime.getServiceReference(JAXBService.class.getName());
            assertNotNull("JAXBService registered", sref);
        } finally {
            runtime.shutdown();
        }
    }

    @Test
    public void testJNDICapability() throws Exception {
        OSGiRuntime runtime = createEmbeddedRuntime();
        try {
            runtime.addCapability(new LogServiceCapability());

            OSGiBundle bundle = runtime.getBundle("jboss-osgi-jndi", null);
            assertNull("Test bundle null", bundle);

            runtime.addCapability(new JNDICapability());

            OSGiServiceReference sref = runtime.getServiceReference(InitialContext.class.getName());
            assertNotNull("InitialContext registered", sref);
        } finally {
            runtime.shutdown();
        }
    }

    @Test
    public void testJMXCapability() throws Exception {
        OSGiRuntime runtime = createEmbeddedRuntime();
        try {
            runtime.addCapability(new CompendiumCapability());

            OSGiBundle bundle = runtime.getBundle("jboss-osgi-jmx", null);
            assertNull("Test bundle null", bundle);

            runtime.addCapability(new JMXCapability());

            OSGiServiceReference sref = runtime.getServiceReference(MBeanServer.class.getName());
            assertNotNull("MBeanServer registered", sref);
        } finally {
            runtime.shutdown();
        }
    }
}