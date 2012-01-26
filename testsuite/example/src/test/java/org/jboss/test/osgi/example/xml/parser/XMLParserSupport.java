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

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.osgi.repository.XRepository;
import org.jboss.osgi.testing.OSGiManifestBuilder;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.test.osgi.example.AbstractTestSupport;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.resource.Resource;
import org.osgi.service.repository.Repository;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.inject.Inject;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParserFactory;
import java.io.InputStream;
import java.net.URL;

import static org.jboss.test.osgi.example.AbstractTestSupport.JBOSS_OSGI_XERCES;
import static org.jboss.test.osgi.example.AbstractTestSupport.getCoordinates;
import static org.jboss.test.osgi.example.AbstractTestSupport.installSupportBundle;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author thomas.diesler@jboss.com
 * @since 26-Jan-2012
 */
public class XMLParserSupport {

    public static SAXParserFactory provideSAXParserFactory(BundleContext syscontext, Bundle bundle) throws BundleException, InvalidSyntaxException {
        ServiceReference sref = syscontext.getServiceReference(SAXParserFactory.class.getName());
        if (sref == null) {
            installSupportBundle(syscontext, getCoordinates(bundle, JBOSS_OSGI_XERCES)).start();
            sref = syscontext.getServiceReference(SAXParserFactory.class.getName());
        }
        return (SAXParserFactory) syscontext.getService(sref);
    }

    public static DocumentBuilderFactory provideDocumentBuilderFactory(BundleContext syscontext, Bundle bundle) throws BundleException {
        ServiceReference sref = syscontext.getServiceReference(DocumentBuilderFactory.class.getName());
        if (sref == null) {
            installSupportBundle(syscontext, getCoordinates(bundle, JBOSS_OSGI_XERCES)).start();
            sref = syscontext.getServiceReference(DocumentBuilderFactory.class.getName());
        }
        return (DocumentBuilderFactory) syscontext.getService(sref);
    }

}