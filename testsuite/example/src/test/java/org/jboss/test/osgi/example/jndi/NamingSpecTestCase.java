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

import javax.inject.Inject;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.naming.Reference;
import javax.naming.spi.InitialContextFactory;

import org.jboss.arquillian.container.test.api.Deployer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.osgi.repository.XRequirementBuilder;
import org.jboss.osgi.resolver.MavenCoordinates;
import org.jboss.osgi.metadata.OSGiManifestBuilder;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.test.osgi.AriesSupport;
import org.jboss.test.osgi.BlueprintSupport;
import org.jboss.test.osgi.NamingSupport;
import org.jboss.test.osgi.RepositorySupport;
import org.jboss.test.osgi.example.jndi.bundle.JNDITestActivator;
import org.jboss.test.osgi.example.jndi.bundle.JNDITestService;
import org.jboss.test.osgi.example.jndi.bundle.JNDITestActivator.SimpleInitalContextFactory;
import org.jboss.test.osgi.example.jndi.bundle.JNDITestActivator.StringReference;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.resource.Resource;
import org.osgi.service.jndi.JNDIConstants;
import org.osgi.service.jndi.JNDIContextManager;
import org.osgi.service.packageadmin.ExportedPackage;
import org.osgi.service.packageadmin.PackageAdmin;
import org.osgi.service.repository.Repository;

/**
 * This test exercises the OSGi-JNDI integration
 *
 * @author David Bosschaert
 * @author Thomas.Diesler@jboss.com
 */
@RunWith(Arquillian.class)
public class NamingSpecTestCase {

    @Inject
    public BundleContext context;

    @Inject
    public Bundle bundle;

    @ArquillianResource
    Deployer deployer;

    @Deployment
    public static JavaArchive createdeployment() {
        final JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "example-jndi");
        archive.addClasses(RepositorySupport.class, NamingSupport.class, AriesSupport.class, BlueprintSupport.class);
        archive.addClasses(JNDITestService.class, JNDITestActivator.class);
        archive.addAsManifestResource(RepositorySupport.BUNDLE_VERSIONS_FILE);
        archive.setManifest(new Asset() {
            @Override
            public InputStream openStream() {
                OSGiManifestBuilder builder = OSGiManifestBuilder.newInstance();
                builder.addBundleSymbolicName(archive.getName());
                builder.addBundleManifestVersion(2);
                builder.addBundleActivator(JNDITestActivator.class);
                builder.addImportPackages(PackageAdmin.class, BundleActivator.class);
                builder.addImportPackages(Context.class, InitialContextFactory.class, JNDIContextManager.class);
                builder.addImportPackages(XRequirementBuilder.class, MavenCoordinates.class, Repository.class, Resource.class);
                builder.addExportPackages(JNDITestService.class);
                return builder.openStream();
            }
        });
        return archive;
    }

    @Test
    public void testContextManager() throws Exception {

        // Make sure we have a valid BundleContext
        bundle.start();

        // Get the InitialContext via {@link JNDIContextManager}
        JNDIContextManager contextManager = NamingSupport.provideJNDIIntegration(context, bundle);
        Context initialContext = contextManager.newInitialContext();

        // Lookup the PackageAdmin OSGi service through JNDI
        PackageAdmin pa = (PackageAdmin) initialContext.lookup("osgi:service/" + PackageAdmin.class.getName());

        // Make an invocation on PackageAdmin
        ExportedPackage ep = pa.getExportedPackage(JNDITestService.class.getPackage().getName());
        Assert.assertEquals(bundle, ep.getExportingBundle());

        // Lookup the {@link JNDITestService} service
        JNDITestService service = (JNDITestService) initialContext.lookup("osgi:service/" + JNDITestService.class.getName());
        Assert.assertEquals("jndi-value", service.getValue());

        // Lookup the {@link JNDITestService} service by name
        service = (JNDITestService) initialContext.lookup("osgi:service/foo");
        Assert.assertEquals("jndi-value", service.getValue());
    }

    @Test
    @Ignore
    public void testContextManagerOwnerContext() throws Exception {

        // Make sure we have a valid BundleContext
        bundle.start();

        // Get the InitialContext via {@link JNDIContextManager}
        JNDIContextManager contextManager = NamingSupport.provideJNDIIntegration(context, bundle);
        Context initialContext = contextManager.newInitialContext();

        // Get the context of the owner bundle
        BundleContext context = (BundleContext) initialContext.lookup("osgi:framework/bundleContext");
        Assert.assertEquals(bundle.getBundleContext(), context);
    }

    @Test
    public void testContextManagerValueBinding() throws Exception {

        // Make sure we have a valid BundleContext
        bundle.start();

        // Get the InitialContext via {@link JNDIContextManager}
        JNDIContextManager contextManager = NamingSupport.provideJNDIIntegration(context, bundle);
        Hashtable<String, String> env = new Hashtable<String, String>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, SimpleInitalContextFactory.class.getName());
        Context initialContext = contextManager.newInitialContext(env);

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
    @Ignore
    public void testContextManagerReferenceBinding() throws Exception {

        // Make sure we have a valid BundleContext
        bundle.start();

        // Get the InitialContext via {@link JNDIContextManager}
        JNDIContextManager contextManager = NamingSupport.provideJNDIIntegration(context, bundle);
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
    public void testContextManagerValueLookup() throws Exception {

        // Make sure we have a valid BundleContext
        bundle.start();

        InputStream inputB = deployer.getDeployment("bundleB");
        Bundle bundleB = context.installBundle("bundleB", inputB);
        try {
            bundleB.start();

            // Access the service directly
            BundleContext contextB = bundleB.getBundleContext();
            ServiceReference sref = contextB.getServiceReference(JNDITestService.class.getName());
            JNDITestService service = (JNDITestService) contextB.getService(sref);
            Assert.assertEquals("jndi-value", service.getValue());

            // Get the InitialContext via {@link JNDIContextManager} for bundleB
            JNDIContextManager contextManager = NamingSupport.provideJNDIIntegration(context, bundleB);
            Context initialContext = contextManager.newInitialContext();

            // Lookup the {@link JNDITestService} service
            service = (JNDITestService) initialContext.lookup("osgi:service/" + JNDITestService.class.getName());
            Assert.assertEquals("jndi-value", service.getValue());

            // Lookup the {@link JNDITestService} service by name
            service = (JNDITestService) initialContext.lookup("osgi:service/foo");
            Assert.assertEquals("jndi-value", service.getValue());
        } finally {
            bundleB.uninstall();
        }
    }

    @Test
    public void testContextManagerValueLookupNegative() throws Exception {

        // Make sure we have a valid BundleContext
        bundle.start();

        InputStream inputC = deployer.getDeployment("bundleC");
        Bundle bundleC = context.installBundle("bundleC", inputC);
        try {
            bundleC.start();

            // Access the service directly
            BundleContext contextC = bundleC.getBundleContext();
            ServiceReference sref = contextC.getServiceReference(JNDITestService.class.getName());
            Assert.assertNull("ServiceReference is null", sref);

            // Get the InitialContext via {@link JNDIContextManager} for bundleC
            JNDIContextManager contextManager = NamingSupport.provideJNDIIntegration(context, bundleC);
            Context initialContext = contextManager.newInitialContext();

            // Lookup the {@link JNDITestService} service
            try {
                initialContext.lookup("osgi:service/" + JNDITestService.class.getName());
                Assert.fail("NameNotFoundException expected");
            } catch (NameNotFoundException ex) {
                //expected
            }

            // [FIXME] Lookup the {@link JNDITestService} service by name
            // with jndi-0.3.1 we get an IllegalArgumentException
            //try {
            //    initialContext.lookup("osgi:service/foo");
            //    Assert.fail("NameNotFoundException expected");
            //} catch (NameNotFoundException ex) {
            //    //expected
            //}

        } finally {
            bundleC.uninstall();
        }
    }

    @Test
    public void testTraditionalAPI() throws Exception {

        // Make sure we have a valid BundleContext
        bundle.start();

        // Get the InitialContext via API
        Context initialContext = new InitialContext();

        // Lookup the PackageAdmin OSGi service through JNDI
        PackageAdmin pa = (PackageAdmin) initialContext.lookup("osgi:service/" + PackageAdmin.class.getName());

        // Make an invocation on PackageAdmin
        ExportedPackage ep = pa.getExportedPackage(JNDITestService.class.getPackage().getName());
        Assert.assertEquals(bundle, ep.getExportingBundle());

        // Lookup the {@link JNDITestService} service
        JNDITestService service = (JNDITestService) initialContext.lookup("osgi:service/" + JNDITestService.class.getName());
        Assert.assertEquals("jndi-value", service.getValue());

        // Lookup the {@link JNDITestService} service by name
        service = (JNDITestService) initialContext.lookup("osgi:service/foo");
        Assert.assertEquals("jndi-value", service.getValue());
    }

    @Test
    @Ignore
    public void testTraditionalAPIOwnerContext() throws Exception {

        // Make sure we have a valid BundleContext
        bundle.start();

        // Get the InitialContext via API
        Context initialContext = new InitialContext();

        // Get the context of the owner bundle
        BundleContext context = (BundleContext) initialContext.lookup("osgi:framework/bundleContext");
        Assert.assertEquals(bundle.getBundleContext(), context);
    }

    @Test
    @Ignore
    public void testTraditionalAPIValueBinding() throws Exception {

        // Make sure we have a valid BundleContext
        bundle.start();

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
    @Ignore
    public void testTraditionalAPIReferenceBinding() throws Exception {

        // Make sure we have a valid BundleContext
        bundle.start();

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

    @Test
    public void testTraditionalAPIValueLookup() throws Exception {

        // Make sure we have a valid BundleContext
        bundle.start();

        InputStream inputB = deployer.getDeployment("bundleB");
        Bundle bundleB = context.installBundle("bundleB", inputB);
        try {
            bundleB.start();

            // Access the service directly
            BundleContext contextB = bundleB.getBundleContext();
            ServiceReference sref = contextB.getServiceReference(JNDITestService.class.getName());
            JNDITestService service = (JNDITestService) contextB.getService(sref);
            Assert.assertEquals("jndi-value", service.getValue());

            // Get the InitialContext via {@link JNDIContextManager} for bundleB
            Hashtable<String, Object> env = new Hashtable<String, Object>();
            env.put(JNDIConstants.BUNDLE_CONTEXT, contextB);
            Context initialContext = new InitialContext(env);

            // Lookup the {@link JNDITestService} service
            service = (JNDITestService) initialContext.lookup("osgi:service/" + JNDITestService.class.getName());
            Assert.assertEquals("jndi-value", service.getValue());

            // Lookup the {@link JNDITestService} service by name
            service = (JNDITestService) initialContext.lookup("osgi:service/foo");
            Assert.assertEquals("jndi-value", service.getValue());
        } finally {
            bundleB.uninstall();
        }
    }

    @Test
    public void testTraditionalAPIValueLookupNegative() throws Exception {

        // Make sure we have a valid BundleContext
        bundle.start();

        InputStream inputC = deployer.getDeployment("bundleC");
        Bundle bundleC = context.installBundle("bundleC", inputC);
        try {
            bundleC.start();

            // Access the service directly
            BundleContext contextC = bundleC.getBundleContext();
            ServiceReference sref = contextC.getServiceReference(JNDITestService.class.getName());
            Assert.assertNull("ServiceReference is null", sref);

            // Get the InitialContext via {@link JNDIContextManager} for bundleB
            Hashtable<String, Object> env = new Hashtable<String, Object>();
            env.put(JNDIConstants.BUNDLE_CONTEXT, contextC);
            Context initialContext = new InitialContext(env);

            // Lookup the {@link JNDITestService} service
            try {
                initialContext.lookup("osgi:service/" + JNDITestService.class.getName());
                Assert.fail("NameNotFoundException expected");
            } catch (NameNotFoundException ex) {
                //expected
            }

            // [FIXME] Lookup the {@link JNDITestService} service by name
            // with jndi-0.3.1 we get an IllegalArgumentException
            //try {
            //    initialContext.lookup("osgi:service/foo");
            //    Assert.fail("NameNotFoundException expected");
            //} catch (NameNotFoundException ex) {
            //    //expected
            //}

        } finally {
            bundleC.uninstall();
        }
    }

    @Deployment(name = "bundleB", managed = false, testable = false)
    public static JavaArchive getBundleB() {
        final JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "bundleB");
        archive.addAsManifestResource(RepositorySupport.BUNDLE_VERSIONS_FILE);
        archive.setManifest(new Asset() {
            @Override
            public InputStream openStream() {
                OSGiManifestBuilder builder = OSGiManifestBuilder.newInstance();
                builder.addBundleSymbolicName(archive.getName());
                builder.addBundleManifestVersion(2);
                builder.addImportPackages(Context.class, InitialContextFactory.class, JNDIContextManager.class);
                builder.addImportPackages(JNDITestService.class);
                return builder.openStream();
            }
        });
        return archive;
    }

    @Deployment(name = "bundleC", managed = false, testable = false)
    public static JavaArchive getBundleC() {
        final JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "bundleC");
        archive.addClasses(JNDITestService.class);
        archive.addAsManifestResource(RepositorySupport.BUNDLE_VERSIONS_FILE);
        archive.setManifest(new Asset() {
            @Override
            public InputStream openStream() {
                OSGiManifestBuilder builder = OSGiManifestBuilder.newInstance();
                builder.addBundleSymbolicName(archive.getName());
                builder.addBundleManifestVersion(2);
                builder.addImportPackages(Context.class, InitialContextFactory.class, JNDIContextManager.class);
                // do include but not import JNDITestService.class
                return builder.openStream();
            }
        });
        return archive;
    }
}
