/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
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

package org.jboss.test.osgi.example;

import org.jboss.osgi.repository.MavenCoordinates;
import org.jboss.osgi.repository.RepositoryRequirementBuilder;
import org.jboss.osgi.repository.XRepository;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.resource.Capability;
import org.osgi.framework.resource.Requirement;
import org.osgi.service.repository.Repository;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.Properties;

import static org.junit.Assert.fail;

/**
 * @author Thomas.Diesler@jboss.com
 * @since 16-Jan-2012
 */
public abstract class AbstractTestSupport {

    public static final String BUNDLE_VERSIONS_FILE = "3rdparty-bundle.versions";

    public static final String APACHE_ARIES_JMX = "org.apache.aries.jmx:org.apache.aries.jmx";
    public static final String APACHE_ARIES_UTIL = "org.apache.aries:org.apache.aries.util";
    public static final String APACHE_FELIX_CONFIGADMIN = "org.apache.felix:org.apache.felix.configadmin";
    public static final String APACHE_FELIX_EVENTADMIN = "org.apache.felix:org.apache.felix.eventadmin";
    public static final String APACHE_FELIX_SCR = "org.apache.felix:org.apache.felix.scr";
    public static final String JBOSS_OSGI_HTTP = "org.jboss.osgi.http:jbosgi-http";
    public static final String JBOSS_OSGI_JMX = "org.jboss.osgi.jmx:jboss-osgi-jmx";
    public static final String JBOSS_OSGI_WEBAPP = "org.jboss.osgi.webapp:jbosgi-webapp";
    public static final String JBOSS_OSGI_XERCES = "org.jboss.osgi.xerces:jboss-osgi-xerces";

    public static Bundle installSupportBundle(BundleContext context, String coordinates) throws BundleException {
        XRepository repository = (XRepository) getRepository(context);
        RepositoryRequirementBuilder builder = repository.getRequirementBuilder();
        Requirement req = builder.createArtifactRequirement(MavenCoordinates.parse(coordinates));
        Collection<Capability> caps = repository.findProviders(req);
        if (caps.isEmpty())
            fail("Cannot find capability for: " + req);
        Capability cap = caps.iterator().next();
        return context.installBundle(coordinates, cap.getResource().getContent());
    }

    public static Repository getRepository(BundleContext context) {
        ServiceReference sref = context.getServiceReference(Repository.class.getName());
        return (Repository) context.getService(sref);
    }

    public static String getCoordinates(Bundle bundle, String artifactid) {
        Properties props = new Properties();
        URL entry = bundle.getEntry("META-INF/" + BUNDLE_VERSIONS_FILE);
        try {
            InputStream input = entry.openStream();
            props.load(input);
            input.close();
        } catch (IOException ex) {
            fail(ex.getMessage());
        }
        return artifactid + ":" + props.getProperty(artifactid);
    }
}
