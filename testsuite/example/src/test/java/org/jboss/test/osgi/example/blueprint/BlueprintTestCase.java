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

import static org.junit.Assert.assertNotNull;

import java.io.InputStream;

import javax.inject.Inject;
import javax.management.MBeanServer;

import org.jboss.arquillian.container.test.api.Deployer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.osgi.repository.XRequirementBuilder;
import org.jboss.osgi.resolver.XRequirement;
import org.jboss.osgi.spi.OSGiManifestBuilder;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.test.osgi.AriesSupport;
import org.jboss.test.osgi.BlueprintSupport;
import org.jboss.test.osgi.JMXSupport;
import org.jboss.test.osgi.RepositorySupport;
import org.jboss.test.osgi.example.blueprint.bundle.BeanA;
import org.jboss.test.osgi.example.blueprint.bundle.BeanB;
import org.jboss.test.osgi.example.blueprint.bundle.ServiceA;
import org.jboss.test.osgi.example.blueprint.bundle.ServiceB;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.resource.Resource;
import org.osgi.service.blueprint.container.BlueprintContainer;
import org.osgi.service.packageadmin.PackageAdmin;
import org.osgi.service.repository.Repository;
import org.osgi.util.tracker.ServiceTracker;

/**
 * A simple Blueprint Container test.
 *
 * @author thomas.diesler@jboss.com
 * @since 12-Jul-2009
 */
@RunWith(Arquillian.class)
public class BlueprintTestCase {

    static final String BLUEPRINT_PROVIDER = "blueprint-provider";
    static final String BLUEPRINT_BUNDLE = "blueprint-bundle";

    @ArquillianResource
    Deployer deployer;

    @Inject
    public BundleContext context;

    @Inject
    public PackageAdmin packageAdmin;

    @Deployment(name = BLUEPRINT_PROVIDER)
    public static JavaArchive blueprintProvider() {
        final JavaArchive archive = ShrinkWrap.create(JavaArchive.class, BLUEPRINT_PROVIDER);
        archive.addClasses(JMXSupport.class, BlueprintSupport.class, AriesSupport.class, RepositorySupport.class);
        archive.addClasses(BeanA.class, ServiceA.class, BeanB.class, ServiceB.class);
        archive.addAsManifestResource(RepositorySupport.BUNDLE_VERSIONS_FILE);
        archive.setManifest(new Asset() {
            public InputStream openStream() {
                OSGiManifestBuilder builder = OSGiManifestBuilder.newInstance();
                builder.addBundleSymbolicName(archive.getName());
                builder.addBundleManifestVersion(2);
                builder.addImportPackages(XRequirementBuilder.class, XRequirement.class, Repository.class, Resource.class);
                builder.addImportPackages(PackageAdmin.class, MBeanServer.class, ServiceTracker.class);
                builder.addExportPackages(ServiceA.class);
                return builder.openStream();
            }
        });
        return archive;
    }

    @Deployment(name = BLUEPRINT_BUNDLE, managed = false, testable = false)
    public static JavaArchive testBundle() {
        final JavaArchive archive = ShrinkWrap.create(JavaArchive.class, BLUEPRINT_BUNDLE);
        archive.addAsResource("blueprint/blueprint-example.xml", "OSGI-INF/blueprint/blueprint-example.xml");
        archive.setManifest(new Asset() {
            public InputStream openStream() {
                OSGiManifestBuilder builder = OSGiManifestBuilder.newInstance();
                builder.addBundleSymbolicName(archive.getName());
                builder.addBundleManifestVersion(2);
                builder.addImportPackages("org.osgi.service.blueprint; version='[1.0.0,2.0.0)'");
                builder.addImportPackages(BlueprintContainer.class, MBeanServer.class);
                builder.addImportPackages(ServiceA.class);
                return builder.openStream();
            }
        });
        return archive;
    }

    @Test
    @InSequence(0)
    public void addBlueprintSupport() throws BundleException {
        Bundle bundle = packageAdmin.getBundles(BLUEPRINT_PROVIDER, null)[0];
        JMXSupport.provideMBeanServer(context, bundle);
        BlueprintSupport.provideBlueprint(context, bundle);
    }

    @Test
    @InSequence(1)
    public void testBlueprintContainerAvailable() throws Exception {
        InputStream input = deployer.getDeployment(BLUEPRINT_BUNDLE);
        Bundle bundle = context.installBundle(BLUEPRINT_BUNDLE, input);
        try {
            bundle.start();
            BlueprintContainer container = getBlueprintContainer(bundle.getSymbolicName());
            assertNotNull("BlueprintContainer available", container);

            ServiceReference sref = context.getServiceReference(ServiceA.class.getName());
            assertNotNull("ServiceA not null", sref);
            ServiceA serviceA = (ServiceA) context.getService(sref);
            MBeanServer mbeanServer = serviceA.getMbeanServer();
            assertNotNull("MBeanServer not null", mbeanServer);

            sref = context.getServiceReference(ServiceB.class.getName());
            assertNotNull("ServiceB not null", sref);
            ServiceB serviceB = (ServiceB) context.getService(sref);
            BeanA beanA = serviceB.getBeanA();
            assertNotNull("BeanA not null", beanA);
        } finally {
            bundle.uninstall();
        }
    }

    private BlueprintContainer getBlueprintContainer(String symbolicName) throws Exception {
        String objectclass = "(objectclass=" + BlueprintContainer.class.getName() + ")";
        String symbolicname = "(osgi.blueprint.container.symbolicname=" + symbolicName + ")";
        Filter filter = FrameworkUtil.createFilter("(&" + objectclass + symbolicname +")");
        ServiceTracker tracker = new ServiceTracker(context, filter, null);
        tracker.open();
        return (BlueprintContainer) tracker.waitForService(10000);
    }
}