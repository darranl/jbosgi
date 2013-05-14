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

import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jboss.osgi.provision.ProvisionException;
import org.jboss.osgi.provision.ProvisionResult;
import org.jboss.osgi.provision.ProvisionService;
import org.jboss.osgi.repository.RepositoryReader;
import org.jboss.osgi.repository.RepositoryXMLReader;
import org.jboss.osgi.repository.XPersistentRepository;
import org.jboss.osgi.resolver.XEnvironment;
import org.jboss.osgi.resolver.XIdentityCapability;
import org.jboss.osgi.resolver.XRequirement;
import org.jboss.osgi.resolver.XRequirementBuilder;
import org.jboss.osgi.resolver.XResource;
import org.junit.Assert;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.namespace.IdentityNamespace;

/**
 * @author Thomas.Diesler@jboss.com
 * @since 10-May-2013
 */
public class ProvisionServiceSupport {

    public static List<Bundle> installCapabilities(BundleContext context, String... features) throws ProvisionException, BundleException {
        XRequirement[] reqs = new XRequirement[features.length];
        for (int i = 0; i < features.length; i++) {
            XRequirementBuilder reqbuilder = XRequirementBuilder.create(IdentityNamespace.IDENTITY_NAMESPACE, features[i]);
            reqs[i] = reqbuilder.getRequirement();
        }
        return installCapabilities(context, reqs);
    }

    private static List<Bundle> installCapabilities(BundleContext context, XRequirement... reqs) throws ProvisionException, BundleException {
        XEnvironment env = getEnvironment(context);
        ProvisionService provision = getProvisionService(context);
        XPersistentRepository repository = provision.getRepository();
        populateRepository(repository, reqs);
        ProvisionResult result = provision.findResources(env, new HashSet<XRequirement>(Arrays.asList(reqs)));
        Set<XRequirement> unsat = result.getUnsatisfiedRequirements();
        Assert.assertTrue("Nothing unsatisfied: " + unsat, unsat.isEmpty());
        List<Bundle> bundles = provision.installResources(result.getResources());
        for (Bundle bundle : bundles) {
            bundle.start();
        }
        return bundles;
    }
    
    private static void populateRepository(XPersistentRepository repository, XRequirement[] reqs) {
        for (XRequirement req : reqs) {
            String nsvalue = (String) req.getAttribute(IdentityNamespace.IDENTITY_NAMESPACE);
            InputStream input = ProvisionServiceSupport.class.getResourceAsStream("/repository/" + nsvalue + ".xml");
            if (input != null) {
                RepositoryReader reader = RepositoryXMLReader.create(input);
                XResource auxres = reader.nextResource();
                while (auxres != null) {
                    XIdentityCapability icap = auxres.getIdentityCapability();
                    nsvalue = (String) icap.getAttribute(IdentityNamespace.IDENTITY_NAMESPACE);
                    XRequirement ireq = XRequirementBuilder.create(IdentityNamespace.IDENTITY_NAMESPACE, nsvalue).getRequirement();
                    if (repository.findProviders(ireq).isEmpty()) {
                        repository.getRepositoryStorage().addResource(auxres);
                    }
                    auxres = reader.nextResource();
                }
            }
        }
    }

    private static ProvisionService getProvisionService(BundleContext context) {
        ServiceReference<ProvisionService> sref = context.getServiceReference(ProvisionService.class);
        return context.getService(sref);
    }

    private static XEnvironment getEnvironment(BundleContext context) {
        ServiceReference<XEnvironment> sref = context.getServiceReference(XEnvironment.class);
        return context.getService(sref);
    }
}
