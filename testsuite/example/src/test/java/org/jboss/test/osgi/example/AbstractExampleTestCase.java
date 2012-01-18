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

import org.jboss.osgi.repository.RequirementBuilder;
import org.jboss.osgi.repository.XRepository;
import org.jboss.osgi.resolver.v2.XCapability;
import org.jboss.osgi.resolver.v2.XRequirement;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.repository.Repository;

/**
 *
 * @author Thomas.Diesler@jboss.com
 * @since 16-Jan-2012
 */
public abstract class AbstractExampleTestCase {

    public static final String APACHE_FELIX_CONFIGADMIN = "org.apache.felix:org.apache.felix.configadmin";
    public static final String JBOSS_OSGI_HTTP = "org.jboss.osgi.http:jbosgi-http";
    public static final String JBOSS_OSGI_XERCES = "org.jboss.osgi.xerces:jboss-osgi-xerces";

    protected Bundle installSupportBundle(BundleContext context, String coordinates) throws BundleException {
        XRepository repository = getRepository(context);
        RequirementBuilder builder = repository.getRequirementBuilder();
        XRequirement req = builder.createArtifactRequirement(coordinates);
        XCapability cap = repository.findProvider(req);
        return context.installBundle(coordinates, cap.getResource().getContent());
    }

    protected XRepository getRepository(BundleContext context) {
        ServiceReference sref = context.getServiceReference(Repository.class.getName());
        return (XRepository) context.getService(sref);
    }

    protected String getCoordinates(BundleContext context, String artifactid) {
        return artifactid + ":" + context.getProperty(artifactid);
    }
}
