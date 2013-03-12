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
package org.jboss.test.osgi.example.jmx;

import static org.junit.Assert.assertEquals;

import java.io.InputStream;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.jboss.arquillian.container.test.api.Deployer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.osgi.metadata.OSGiManifestBuilder;
import org.jboss.osgi.repository.XRequirementBuilder;
import org.jboss.osgi.resolver.XRequirement;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.test.osgi.AriesSupport;
import org.jboss.test.osgi.JMXSupport;
import org.jboss.test.osgi.RepositorySupport;
import org.jboss.test.osgi.example.jmx.bundle.Foo;
import org.jboss.test.osgi.example.jmx.bundle.FooMBean;
import org.jboss.test.osgi.example.jmx.bundle.MBeanActivator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;
import org.osgi.resource.Resource;
import org.osgi.service.repository.Repository;
import org.osgi.util.tracker.ServiceTracker;

/**
 * A test that deployes a bundle that registeres an MBean
 *
 * @author thomas.diesler@jboss.com
 * @since 12-Feb-2009
 */
@RunWith(Arquillian.class)
public class MBeanServerTestCase {

    static final String JMX_PROVIDER = "jmx-provider";
    static final String JMX_BUNDLE = "jmx-bundle";

    @ArquillianResource
    Deployer deployer;

    @ArquillianResource
    BundleContext context;

    @Deployment(name = JMX_PROVIDER)
    public static JavaArchive jmxProvider() {
        final JavaArchive archive = ShrinkWrap.create(JavaArchive.class, JMX_PROVIDER);
        archive.addClasses(Foo.class, FooMBean.class, MBeanActivator.class);
        archive.addClasses(RepositorySupport.class, JMXSupport.class, AriesSupport.class);
        archive.addAsManifestResource(RepositorySupport.BUNDLE_VERSIONS_FILE);
        archive.setManifest(new Asset() {
            @Override
            public InputStream openStream() {
                OSGiManifestBuilder builder = OSGiManifestBuilder.newInstance();
                builder.addBundleSymbolicName(archive.getName());
                builder.addBundleManifestVersion(2);
                builder.addImportPackages(XRequirementBuilder.class, XRequirement.class, Repository.class, Resource.class);
                builder.addImportPackages(MBeanServer.class, ServiceTracker.class);
                builder.addExportPackages(FooMBean.class);
                return builder.openStream();
            }
        });
        return archive;
    }

    @Deployment(name = JMX_BUNDLE, managed = false, testable = false)
    public static JavaArchive testBundle() {
        final JavaArchive archive = ShrinkWrap.create(JavaArchive.class, JMX_BUNDLE);
        archive.setManifest(new Asset() {
            @Override
            public InputStream openStream() {
                OSGiManifestBuilder builder = OSGiManifestBuilder.newInstance();
                builder.addBundleSymbolicName(archive.getName());
                builder.addBundleManifestVersion(2);
                builder.addBundleActivator(MBeanActivator.class);
                builder.addImportPackages(BundleActivator.class, MBeanServer.class);
                builder.addImportPackages(FooMBean.class);
                return builder.openStream();
            }
        });
        return archive;
    }

    @Test
    @InSequence(0)
    public void addJMXSupport(@ArquillianResource Bundle bundle) throws BundleException {
        JMXSupport.provideMBeanServer(context, bundle);
    }

    @Test
    @InSequence(1)
    public void testMBeanAccess() throws Exception {
        InputStream input = deployer.getDeployment(JMX_BUNDLE);
        Bundle bundle = context.installBundle(JMX_BUNDLE, input);
        try {
            bundle.start();

            ServiceReference<MBeanServer> sref = context.getServiceReference(MBeanServer.class);
            MBeanServer server = context.getService(sref);

            ObjectName oname = ObjectName.getInstance(FooMBean.MBEAN_NAME);
            FooMBean foo = JMXSupport.getMBeanProxy(server, oname, FooMBean.class);
            assertEquals("hello", foo.echo("hello"));
        } finally {
            bundle.uninstall();
        }
    }
}