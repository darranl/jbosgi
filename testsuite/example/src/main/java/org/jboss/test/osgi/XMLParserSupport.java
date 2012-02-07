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
package org.jboss.test.osgi;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParserFactory;

/**
 * @author thomas.diesler@jboss.com
 * @since 26-Jan-2012
 */
public class XMLParserSupport extends RepositorySupport {

    public static final String JBOSS_OSGI_XERCES = "org.jboss.osgi.xerces:jbosgi-xerces";

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