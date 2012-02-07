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
package org.jboss.test.osgi.example.blueprint;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.osgi.resolver.v2.XRequirementBuilder;
import org.jboss.osgi.testing.OSGiManifestBuilder;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.test.osgi.AriesSupport;
import org.jboss.test.osgi.BlueprintSupport;
import org.jboss.test.osgi.ManagementSupport;
import org.jboss.test.osgi.RepositorySupport;
import org.jboss.test.osgi.example.blueprint.bundle.BeanA;
import org.jboss.test.osgi.example.blueprint.bundle.BeanB;
import org.jboss.test.osgi.example.blueprint.bundle.ServiceA;
import org.jboss.test.osgi.example.blueprint.bundle.ServiceB;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.resource.Resource;
import org.osgi.service.blueprint.container.BlueprintContainer;
import org.osgi.service.packageadmin.PackageAdmin;
import org.osgi.service.repository.Repository;

import javax.inject.Inject;
import javax.management.MBeanServer;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * A simple Blueprint Container test.
 *
 * @author thomas.diesler@jboss.com
 * @since 12-Jul-2009
 */
@RunWith(Arquillian.class)
public class BlueprintTestCase {

    @Inject
    public BundleContext context;

    @Inject
    public Bundle bundle;

    @Deployment
    public static JavaArchive createdeployment() {
        final JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "example-blueprint");
        archive.addClasses(BeanA.class, ServiceA.class, BeanB.class, ServiceB.class);
        archive.addClasses(ManagementSupport.class, BlueprintSupport.class, AriesSupport.class, RepositorySupport.class);
        archive.addAsResource("blueprint/blueprint-example.xml", "OSGI-INF/blueprint/blueprint-example.xml");
        archive.addAsManifestResource(RepositorySupport.BUNDLE_VERSIONS_FILE);
        archive.setManifest(new Asset() {
            public InputStream openStream() {
                OSGiManifestBuilder builder = OSGiManifestBuilder.newInstance();
                builder.addBundleSymbolicName(archive.getName());
                builder.addBundleManifestVersion(2);
                builder.addImportPackages(PackageAdmin.class, BlueprintContainer.class, MBeanServer.class);
                builder.addImportPackages(XRequirementBuilder.class, Repository.class, Resource.class);
                return builder.openStream();
            }
        });
        return archive;
    }

    @Test
    public void testBlueprintContainerAvailable() throws Exception {
        ManagementSupport.provideMBeanServer(context, bundle);
        BlueprintSupport.provideBlueprint(context, bundle);
        bundle.start();
        assertEquals("example-blueprint", bundle.getSymbolicName());
        BlueprintContainer container = getBlueprintContainer();
        assertNotNull("BlueprintContainer available", container);
    }

    @Test
    public void testServiceA() throws Exception {
        ServiceReference sref = context.getServiceReference(ServiceA.class.getName());
        assertNotNull("ServiceA not null", sref);
        ServiceA service = (ServiceA) context.getService(sref);
        MBeanServer mbeanServer = service.getMbeanServer();
        assertNotNull("MBeanServer not null", mbeanServer);
    }

    @Test
    public void testServiceB() throws Exception {
        bundle.start();
        ServiceReference sref = context.getServiceReference(ServiceB.class.getName());
        assertNotNull("ServiceB not null", sref);
        ServiceB service = (ServiceB) context.getService(sref);
        BeanA beanA = service.getBeanA();
        assertNotNull("BeanA not null", beanA);
    }

    private BlueprintContainer getBlueprintContainer() throws Exception {
        // 10sec for processing of STARTING event
        int timeout = 10000;
        ServiceReference sref = null;
        while (sref == null && 0 < (timeout -= 200)) {
            String filter = "(osgi.blueprint.container.symbolicname=example-blueprint)";
            ServiceReference[] srefs = context.getServiceReferences(BlueprintContainer.class.getName(), filter);
            if (srefs != null && srefs.length > 0)
                sref = srefs[0];

            Thread.sleep(200);
        }
        assertNotNull("BlueprintContainer not null", sref);
        return (BlueprintContainer) context.getService(sref);
    }
}