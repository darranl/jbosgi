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

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Thomas.Diesler@jboss.com
 * @since 16-Jan-2012
 */
public abstract class AbstractExampleTestCase {

    public static final String APACHE_ARIES_JMX = "org.apache.aries.jmx:org.apache.aries.jmx";
    public static final String APACHE_ARIES_UTIL = "org.apache.aries:org.apache.aries.util";
    public static final String APACHE_FELIX_CONFIGADMIN = "org.apache.felix:org.apache.felix.configadmin";
    public static final String APACHE_FELIX_EVENTADMIN = "org.apache.felix:org.apache.felix.eventadmin";
    public static final String APACHE_FELIX_SCR = "org.apache.felix:org.apache.felix.scr";
    public static final String JBOSS_OSGI_HTTP = "org.jboss.osgi.http:jbosgi-http";
    public static final String JBOSS_OSGI_JMX = "org.jboss.osgi.jmx:jboss-osgi-jmx";
    public static final String JBOSS_OSGI_WEBAPP = "org.jboss.osgi.webapp:jbosgi-webapp";
    public static final String JBOSS_OSGI_XERCES = "org.jboss.osgi.xerces:jboss-osgi-xerces";

    // [TODO] generate this map from the POM somehow
    private static Map<String, String> versionmap = new HashMap<String,String>();
    static {
        versionmap.put(APACHE_ARIES_JMX, "0.3");
        versionmap.put(APACHE_ARIES_UTIL, "0.3");
        versionmap.put(APACHE_FELIX_CONFIGADMIN, "1.2.8");
        versionmap.put(APACHE_FELIX_EVENTADMIN, "1.2.6");
        versionmap.put(APACHE_FELIX_SCR, "1.6.0");
        versionmap.put(JBOSS_OSGI_HTTP, "1.0.5");
        versionmap.put(JBOSS_OSGI_JMX, "1.0.10");
        versionmap.put(JBOSS_OSGI_WEBAPP, "1.0.5");
        versionmap.put(JBOSS_OSGI_XERCES, "2.9.1.SP7");
    }

    protected Bundle installSupportBundle(BundleContext context, String coordinates) throws BundleException {
        XRepository repository = (XRepository) getRepository(context);
        RepositoryRequirementBuilder builder = repository.getRequirementBuilder();
        Requirement req = builder.createArtifactRequirement(MavenCoordinates.parse(coordinates));
        Capability cap = repository.findProviders(req).iterator().next();
        return context.installBundle(coordinates, cap.getResource().getContent());
    }

    protected Repository getRepository(BundleContext context) {
        ServiceReference sref = context.getServiceReference(Repository.class.getName());
        return (Repository) context.getService(sref);
    }

    protected String getCoordinates(String artifactid) {
        return artifactid + ":" + versionmap.get(artifactid);
    }
}
