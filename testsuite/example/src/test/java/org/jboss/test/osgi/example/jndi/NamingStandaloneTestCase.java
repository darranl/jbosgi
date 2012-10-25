/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012, JBoss Inc., and individual contributors as indicated
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
package org.jboss.test.osgi.example.jndi;

import java.io.InputStream;
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.Reference;
import javax.naming.spi.InitialContextFactory;

import org.jboss.arquillian.container.test.api.Deployer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.osgi.metadata.OSGiManifestBuilder;
import org.jboss.osgi.repository.XRequirementBuilder;
import org.jboss.osgi.resolver.MavenCoordinates;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.test.osgi.AriesSupport;
import org.jboss.test.osgi.BlueprintSupport;
import org.jboss.test.osgi.ConfigurationAdminSupport;
import org.jboss.test.osgi.NamingSupport;
import org.jboss.test.osgi.RepositorySupport;
import org.jboss.test.osgi.example.jndi.bundle.JNDITestActivator;
import org.jboss.test.osgi.example.jndi.bundle.JNDITestActivator.SimpleInitalContextFactory;
import org.jboss.test.osgi.example.jndi.bundle.JNDITestActivator.StringReference;
import org.jboss.test.osgi.example.jndi.bundle.JNDITestService;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.resource.Resource;
import org.osgi.service.jndi.JNDIContextManager;
import org.osgi.service.packageadmin.PackageAdmin;
import org.osgi.service.repository.Repository;

/**
 * This test exercises the OSGi-JNDI integration
 *
 * @author David Bosschaert
 * @author Thomas.Diesler@jboss.com
 */
@RunWith(Arquillian.class)
public class NamingStandaloneTestCase {

    private static final String JNDI_PROVIDER = "jndi-provider";

    @ArquillianResource
    BundleContext context;

    @ArquillianResource
    Deployer deployer;

    @Deployment(name = JNDI_PROVIDER)
    public static JavaArchive jndiProvider() {
        final JavaArchive archive = ShrinkWrap.create(JavaArchive.class, JNDI_PROVIDER);
        archive.addClasses(RepositorySupport.class, NamingSupport.class, AriesSupport.class, BlueprintSupport.class, ConfigurationAdminSupport.class);
        archive.addClasses(JNDITestService.class, JNDITestActivator.class);
        archive.addAsManifestResource(RepositorySupport.BUNDLE_VERSIONS_FILE);
        archive.setManifest(new Asset() {
            @Override
            public InputStream openStream() {
                OSGiManifestBuilder builder = OSGiManifestBuilder.newInstance();
                builder.addBundleSymbolicName(archive.getName());
                builder.addBundleManifestVersion(2);
                builder.addBundleActivator(JNDITestActivator.class);
                builder.addImportPackages(XRequirementBuilder.class, MavenCoordinates.class, Repository.class, Resource.class);
                builder.addImportPackages(PackageAdmin.class, Context.class, InitialContextFactory.class);
                builder.addDynamicImportPackages(JNDIContextManager.class.getPackage().getName());
                builder.addExportPackages(JNDITestService.class);
                return builder.openStream();
            }
        });
        return archive;
    }

    @Test
    @InSequence(0)
    public void addNamingSupport(@ArquillianResource Bundle bundle) throws Exception {
        NamingSupport.provideJNDIIntegration(context, bundle);
        bundle.start();
    }

    @Test
    @InSequence(1)
    public void testContextManagerOwnerContext(@ArquillianResource Bundle bundle) throws Exception {

        // Get the InitialContext via {@link JNDIContextManager}
        JNDIContextManager contextManager = NamingSupport.getContextManager(bundle);
        Context initialContext = contextManager.newInitialContext();

        // Get the context of the owner bundle
        BundleContext context = (BundleContext) initialContext.lookup("osgi:framework/bundleContext");
        Assert.assertEquals(bundle.getBundleContext(), context);
    }

    @Test
    @InSequence(1)
    public void testContextManagerReferenceBinding(@ArquillianResource Bundle bundle) throws Exception {

        // Get the InitialContext via {@link JNDIContextManager}
        JNDIContextManager contextManager = NamingSupport.getContextManager(bundle);
        Hashtable<String, String> env = new Hashtable<String, String>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, SimpleInitalContextFactory.class.getName());
        Context initialContext = contextManager.newInitialContext(env);

        // Bind a some value reference under some key
        Reference ref = new StringReference("bar");
        initialContext.bind("test/foo", ref);
        try {
            // Lookup the value
            Assert.assertEquals("bar", initialContext.lookup("test/foo"));
        } finally {
            initialContext.unbind("test/foo");
        }
    }

    @Test
    @Ignore
    @InSequence(1)
    public void testTraditionalAPIOwnerContext(@ArquillianResource Bundle bundle) throws Exception {

        // Get the InitialContext via API
        Context initialContext = new InitialContext();

        // Get the context of the owner bundle
        BundleContext context = (BundleContext) initialContext.lookup("osgi:framework/bundleContext");
        Assert.assertEquals(bundle.getBundleContext(), context);
    }

    @Test
    @InSequence(1)
    public void testTraditionalAPIValueBinding(@ArquillianResource Bundle bundle) throws Exception {

        // Get the InitialContext via API
        Hashtable<String, String> env = new Hashtable<String, String>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, SimpleInitalContextFactory.class.getName());
        Context initialContext = new InitialContext(env);

        // Bind a some value under some key
        initialContext.bind("test/foo", "bar");
        try {
            // Lookup the value
            Assert.assertEquals("bar", initialContext.lookup("test/foo"));
        } finally {
            initialContext.unbind("test/foo");
        }
    }

    @Test
    @InSequence(1)
    public void testTraditionalAPIReferenceBinding(@ArquillianResource Bundle bundle) throws Exception {

        // Get the InitialContext via API
        Hashtable<String, String> env = new Hashtable<String, String>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, SimpleInitalContextFactory.class.getName());
        Context initialContext = new InitialContext(env);

        // Bind a some value reference under some key
        Reference ref = new StringReference("bar");
        initialContext.bind("test/foo", ref);
        try {
            // Lookup the value
            Assert.assertEquals("bar", initialContext.lookup("test/foo"));
        } finally {
            initialContext.unbind("test/foo");
        }
    }
}
