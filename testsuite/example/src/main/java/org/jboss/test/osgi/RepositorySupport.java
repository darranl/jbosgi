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

package org.jboss.test.osgi;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.Properties;

import org.jboss.osgi.repository.XRepository;
import org.jboss.osgi.repository.XRequirementBuilder;
import org.jboss.osgi.resolver.MavenCoordinates;
import org.jboss.osgi.resolver.XRequirement;
import org.jboss.osgi.resolver.XResource;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;
import org.osgi.resource.Capability;
import org.osgi.service.repository.RepositoryContent;

/**
 * @author Thomas.Diesler@jboss.com
 * @since 16-Jan-2012
 */
public class RepositorySupport {

    public static final String BUNDLE_VERSIONS_FILE = "3rdparty-bundle.versions";

    public static XRepository getRepository(BundleContext context) {
        ServiceReference<XRepository> sref = context.getServiceReference(XRepository.class);
        return context.getService(sref);
    }

    public static Bundle installSupportBundle(BundleContext context, String coordinates) throws BundleException {
        XRepository repository = getRepository(context);
        XRequirement req = XRequirementBuilder.create(MavenCoordinates.parse(coordinates)).getRequirement();
        Collection<Capability> caps = repository.findProviders(req);
        if (caps.isEmpty())
            throw new IllegalStateException("Cannot find capability for: " + req);
        Capability cap = caps.iterator().next();
        XResource xres = (XResource) cap.getResource();
        RepositoryContent content = (RepositoryContent) xres;
        return context.installBundle(coordinates, content.getContent());
    }

    public static String getCoordinates(Bundle bundle, String artifactid) {
        Properties props = new Properties();
        URL entry = bundle.getEntry("META-INF/" + BUNDLE_VERSIONS_FILE);
        if (entry == null)
            throw new IllegalStateException("Cannot find resource: META-INF/" + BUNDLE_VERSIONS_FILE);
        try {
            InputStream input = entry.openStream();
            props.load(input);
            input.close();
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
        return artifactid + ":" + props.getProperty(artifactid);
    }
}
