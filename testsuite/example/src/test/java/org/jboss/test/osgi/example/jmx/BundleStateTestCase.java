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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.io.InputStream;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.openmbean.TabularData;

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
import org.jboss.test.osgi.FrameworkUtils;
import org.jboss.test.osgi.JMXSupport;
import org.jboss.test.osgi.RepositorySupport;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;
import org.osgi.jmx.framework.BundleStateMBean;
import org.osgi.resource.Resource;
import org.osgi.service.repository.Repository;

/**
 * Test {@link BundleStateMBean} functionality
 *
 * @author thomas.diesler@jboss.com
 * @since 15-Feb-2010
 */
@RunWith(Arquillian.class)
@Ignore("[ARIES-1029] Provide a JMX implementation that works with R5")
public class BundleStateTestCase {

    static final String JMX_PROVIDER = "jmx-provider";

    @ArquillianResource
    BundleContext context;

    @Deployment(name = JMX_PROVIDER)
    public static JavaArchive jmxProvider() {
        final JavaArchive archive = ShrinkWrap.create(JavaArchive.class, JMX_PROVIDER);
        archive.addClasses(RepositorySupport.class, JMXSupport.class, AriesSupport.class, FrameworkUtils.class);
        archive.addAsManifestResource(RepositorySupport.BUNDLE_VERSIONS_FILE);
        archive.setManifest(new Asset() {
            @Override
            public InputStream openStream() {
                OSGiManifestBuilder builder = OSGiManifestBuilder.newInstance();
                builder.addBundleSymbolicName(archive.getName());
                builder.addBundleManifestVersion(2);
                builder.addImportPackages(XRequirementBuilder.class, XRequirement.class, Repository.class, Resource.class);
                builder.addImportPackages(MBeanServer.class, TabularData.class);
                builder.addDynamicImportPackages(BundleStateMBean.class.getPackage().getName());
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
    public void testBundleStateMBean() throws Exception {

        ServiceReference<MBeanServer> sref = context.getServiceReference(MBeanServer.class);
        MBeanServer server = context.getService(sref);

        ObjectName oname = ObjectName.getInstance("osgi.core:type=bundleState,version=1.5");
        BundleStateMBean bundleState = JMXSupport.getMBeanProxy(server, oname, BundleStateMBean.class);
        assertNotNull("BundleStateMBean not null", bundleState);

        TabularData bundleData = bundleState.listBundles();
        assertNotNull("TabularData not null", bundleData);
        assertFalse("TabularData not empty", bundleData.isEmpty());
    }
}