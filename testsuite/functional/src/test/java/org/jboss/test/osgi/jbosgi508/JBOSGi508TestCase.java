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
package org.jboss.test.osgi.jbosgi508;

import org.jboss.arquillian.container.test.api.Deployer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.osgi.resolver.v2.XRequirementBuilder;
import org.jboss.osgi.testing.OSGiManifestBuilder;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.test.osgi.DeclarativeServicesSupport;
import org.jboss.test.osgi.RepositorySupport;
import org.jboss.test.osgi.jbosgi508.bundle1.Service1;
import org.jboss.test.osgi.jbosgi508.bundle2.Service2;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.resource.Resource;
import org.osgi.service.repository.Repository;

import javax.inject.Inject;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Declarative Services don't start if bundles are activated in different order
 * https://issues.jboss.org/browse/JBOSGI-508
 *
 * @author Thomas.Diesler@jboss.com
 */
@RunWith(Arquillian.class)
public class JBOSGi508TestCase {

    @Inject
    public BundleContext context;

    @Inject
    public Bundle bundle;

    @ArquillianResource
    public Deployer deployer;

    @Deployment
    public static JavaArchive create() {
        final JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "jbosgi508");
        archive.addClasses(DeclarativeServicesSupport.class, RepositorySupport.class);
        archive.addAsManifestResource(RepositorySupport.BUNDLE_VERSIONS_FILE);
        archive.setManifest(new Asset() {
            public InputStream openStream() {
                OSGiManifestBuilder builder = OSGiManifestBuilder.newInstance();
                builder.addBundleSymbolicName(archive.getName());
                builder.addBundleManifestVersion(2);
                builder.addImportPackages(XRequirementBuilder.class, Repository.class, Resource.class);
                return builder.openStream();
            }
        });
        return archive;
    }

    @Test
    public void testStartInOrder() throws Exception {
        DeclarativeServicesSupport.provideDeclarativeServices(context, bundle);
        InputStream input1 = deployer.getDeployment("service1");
        Bundle bundle1 = context.installBundle("service1", input1);
        try {
            InputStream input2 = deployer.getDeployment("service2");
            Bundle bundle2 = context.installBundle("service2", input2);
            try {
                bundle1.start();
                bundle2.start();
                Assert.assertEquals(Bundle.ACTIVE, bundle1.getState());
                Assert.assertEquals(Bundle.ACTIVE, bundle2.getState());
                assertServices();
            } finally {
                bundle2.uninstall();
            }
        } finally {
            bundle1.uninstall();
        }
    }

    //@Test
    public void testStartReverse() throws Exception {
        DeclarativeServicesSupport.provideDeclarativeServices(context, bundle);
        InputStream input1 = deployer.getDeployment("service1");
        Bundle bundle1 = context.installBundle("service1", input1);
        try {
            InputStream input2 = deployer.getDeployment("service2");
            Bundle bundle2 = context.installBundle("service2", input2);
            try {
                bundle2.start();
                bundle1.start();
                Assert.assertEquals(Bundle.ACTIVE, bundle1.getState());
                Assert.assertEquals(Bundle.ACTIVE, bundle2.getState());
                assertServices();
            } finally {
                bundle2.uninstall();
            }
        } finally {
            bundle1.uninstall();
        }
    }

    private void assertServices() throws InvalidSyntaxException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        ServiceReference[] srefs = context.getAllServiceReferences("org.jboss.test.osgi.jbosgi508.bundle2.Service2", null);
        Object service2 = context.getService(srefs[0]);
        Assert.assertNotNull("Service2 not null", service2);
        Method method = service2.getClass().getMethod("getService1", null);
        Object service1 = method.invoke(service2, null);
        Assert.assertNotNull("Service1 not null", service1);
    }

    @Deployment(name = "service1", managed = false, testable = false)
    public static JavaArchive getService1() {
        final JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "service1");
        archive.addClass(Service1.class);
        archive.addAsResource("jbosgi508/service1.xml", "OSGI-INF/service1.xml");
        archive.setManifest(new Asset() {
            @Override
            public InputStream openStream() {
                OSGiManifestBuilder builder = OSGiManifestBuilder.newInstance();
                builder.addBundleManifestVersion(2);
                builder.addBundleSymbolicName(archive.getName());
                builder.addExportPackages(Service1.class.getPackage().getName() + ";version=1.0.0");
                builder.addManifestHeader("Service-Component", "OSGI-INF/service1.xml");
                return builder.openStream();
            }
        });
        return archive;
    }

    @Deployment(name = "service2", managed = false, testable = false)
    public static JavaArchive getService2() {
        final JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "service2");
        archive.addClass(Service2.class);
        archive.addAsResource("jbosgi508/service2.xml", "OSGI-INF/service2.xml");
        archive.setManifest(new Asset() {
            @Override
            public InputStream openStream() {
                OSGiManifestBuilder builder = OSGiManifestBuilder.newInstance();
                builder.addBundleManifestVersion(2);
                builder.addBundleSymbolicName(archive.getName());
                builder.addImportPackages(Service1.class.getPackage().getName() + ";version=\"[1.0,2.0)\"");
                builder.addManifestHeader("Service-Component", "OSGI-INF/service2.xml");
                return builder.openStream();
            }
        });
        return archive;
    }
}
