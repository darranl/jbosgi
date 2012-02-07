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

import org.jboss.osgi.resolver.v2.MavenCoordinates;
import org.jboss.osgi.resolver.v2.XRequirementBuilder;
import org.junit.Assert;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.resource.Capability;
import org.osgi.framework.resource.Requirement;
import org.osgi.service.packageadmin.PackageAdmin;
import org.osgi.service.repository.Repository;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.Properties;

/**
 * @author Thomas.Diesler@jboss.com
 * @since 16-Jan-2012
 */
public class RepositorySupport {

    public static final String BUNDLE_VERSIONS_FILE = "3rdparty-bundle.versions";

    public static Repository getRepository(BundleContext context) {
        ServiceReference sref = context.getServiceReference(Repository.class.getName());
        return (Repository) context.getService(sref);
    }

    public static PackageAdmin getPackageAdmin(BundleContext syscontext) {
        ServiceReference sref = syscontext.getServiceReference(PackageAdmin.class.getName());
        return (PackageAdmin) syscontext.getService(sref);
    }

    public static Bundle installSupportBundle(BundleContext context, String coordinates) throws BundleException {
        Repository repository = getRepository(context);
        Requirement req = XRequirementBuilder.createArtifactRequirement(MavenCoordinates.parse(coordinates));
        Collection<Capability> caps = repository.findProviders(req);
        if (caps.isEmpty())
            Assert.fail("Cannot find capability for: " + req);
        Capability cap = caps.iterator().next();
        return context.installBundle(coordinates, cap.getResource().getContent());
    }

    public static String getCoordinates(Bundle bundle, String artifactid) {
        Properties props = new Properties();
        URL entry = bundle.getEntry("META-INF/" + BUNDLE_VERSIONS_FILE);
        if (entry == null)
            Assert.fail("Cannot find resource: META-INF/" + BUNDLE_VERSIONS_FILE);
        try {
            InputStream input = entry.openStream();
            props.load(input);
            input.close();
        } catch (IOException ex) {
            Assert.fail(ex.getMessage());
        }
        return artifactid + ":" + props.getProperty(artifactid);
    }
}
