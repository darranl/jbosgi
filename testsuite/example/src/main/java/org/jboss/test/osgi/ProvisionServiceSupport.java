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

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jboss.osgi.provision.ProvisionException;
import org.jboss.osgi.provision.ProvisionResult;
import org.jboss.osgi.provision.ProvisionService;
import org.jboss.osgi.resolver.XEnvironment;
import org.jboss.osgi.resolver.XRequirement;
import org.jboss.osgi.resolver.XRequirementBuilder;
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

    public static ProvisionService getProvisionService(BundleContext context) {
        ServiceReference<ProvisionService> sref = context.getServiceReference(ProvisionService.class);
        return context.getService(sref);
    }

    public static XEnvironment getEnvironment(BundleContext context) {
        ServiceReference<XEnvironment> sref = context.getServiceReference(XEnvironment.class);
        return context.getService(sref);
    }

    public static List<Bundle> installCapabilities(BundleContext context, String... features) throws ProvisionException, BundleException {
        XRequirement[] reqs = new XRequirement[features.length];
        for (int i = 0; i < features.length; i++) {
            XRequirementBuilder reqbuilder = XRequirementBuilder.create(IdentityNamespace.IDENTITY_NAMESPACE, features[i]);
            reqs[i] = reqbuilder.getRequirement();
        }
        return installCapabilities(context, reqs);
    }

    public static List<Bundle> installCapabilities(BundleContext context, XRequirement... reqs) throws ProvisionException, BundleException {
        XEnvironment env = getEnvironment(context);
        ProvisionService provision = getProvisionService(context);
        ProvisionResult result = provision.findResources(env, new HashSet<XRequirement>(Arrays.asList(reqs)));
        Set<XRequirement> unsat = result.getUnsatisfiedRequirements();
        Assert.assertTrue("Nothing unsatisfied: " + unsat, unsat.isEmpty());
        List<Bundle> bundles = provision.installResources(result.getResources());
        for (Bundle bundle : bundles) {
            bundle.start();
        }
        return bundles;
    }
}
