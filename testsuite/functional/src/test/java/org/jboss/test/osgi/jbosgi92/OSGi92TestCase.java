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

import static org.junit.Assert.assertEquals;

import java.io.InputStream;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParserFactory;

import org.jboss.osgi.spi.capability.CompendiumCapability;
import org.jboss.osgi.testing.OSGiBundle;
import org.jboss.osgi.testing.OSGiManifestBuilder;
import org.jboss.osgi.testing.OSGiRuntime;
import org.jboss.osgi.testing.OSGiRuntimeTest;
import org.jboss.osgi.testing.OSGiServiceReference;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.test.osgi.jbosgi92.bundleA.ActivatorBundleA;
import org.jboss.test.osgi.jbosgi92.bundleA.DocumentBuilderFactoryImpl;
import org.jboss.test.osgi.jbosgi92.bundleA.SAXParserFactoryImpl;
import org.junit.BeforeClass;
import org.junit.Test;
import org.osgi.framework.Bundle;

/**
 * [JBOSGI-92] Class.forName issue with XMLParserActivator
 * 
 * https://jira.jboss.org/jira/browse/JBOSGI-92
 * 
 * @author thomas.diesler@jboss.com
 * @since 21-Jul-2009
 */
public class OSGi92TestCase extends OSGiRuntimeTest {
    @BeforeClass
    public static void beforeClass() throws Exception {
        OSGiRuntime runtime = createDefaultRuntime();
        runtime.addCapability(new CompendiumCapability());
    }

    @Test
    public void testDeployParsers() throws Exception {
        OSGiBundle bundleA = getRuntime().installBundle(getBundleA());
        bundleA.start();

        assertBundleState(Bundle.ACTIVE, bundleA.getState());

        String filter = "(parser.factoryname=org.jboss.test.osgi.jbosgi92.bundleA.*)";
        OSGiServiceReference[] domRefs = getRuntime().getServiceReferences(DocumentBuilderFactory.class.getName(), filter);
        assertEquals("DocumentBuilderFactory service available", 1, domRefs.length);

        OSGiServiceReference[] saxRefs = getRuntime().getServiceReferences(SAXParserFactory.class.getName(), filter);
        assertEquals("SAXParserFactory service available", 1, saxRefs.length);
    }

    private JavaArchive getBundleA() {
        // Bundle-SymbolicName: jbosgi92-bundleA
        // Bundle-Activator: org.jboss.test.osgi.jbosgi92.bundleA.ActivatorBundleA
        // Export-Package: org.jboss.test.osgi.jbosgi92.bundleA
        final JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "jbosgi92-bundleA");
        archive.addClasses(ActivatorBundleA.class, DocumentBuilderFactoryImpl.class, SAXParserFactoryImpl.class);
        archive.addResource("jbosgi92/javax.xml.parsers.DocumentBuilderFactory", "META-INF/services/javax.xml.parsers.DocumentBuilderFactory");
        archive.addResource("jbosgi92/javax.xml.parsers.SAXParserFactory", "META-INF/services/javax.xml.parsers.SAXParserFactory");
        archive.setManifest(new Asset() {
            public InputStream openStream() {
                OSGiManifestBuilder builder = OSGiManifestBuilder.newInstance();
                builder.addBundleManifestVersion(2);
                builder.addBundleSymbolicName(archive.getName());
                builder.addBundleActivator(ActivatorBundleA.class);
                builder.addExportPackages("org.jboss.test.osgi.jbosgi92.bundleA");
                builder.addImportPackages("javax.xml.parsers", "org.osgi.framework", "org.osgi.util.xml", "org.xml.sax");
                return builder.openStream();
            }
        });
        return archive;
    }
}