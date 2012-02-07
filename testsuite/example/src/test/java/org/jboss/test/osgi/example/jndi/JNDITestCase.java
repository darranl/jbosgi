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

import javax.inject.Inject;
import javax.naming.Context;
import javax.naming.spi.InitialContextFactory;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.osgi.resolver.v2.XRequirementBuilder;
import org.jboss.osgi.testing.OSGiManifestBuilder;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.test.osgi.AriesSupport;
import org.jboss.test.osgi.BlueprintSupport;
import org.jboss.test.osgi.NamingSupport;
import org.jboss.test.osgi.RepositorySupport;
import org.jboss.test.osgi.example.jndi.bundle.MyInterface;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.resource.Resource;
import org.osgi.service.jndi.JNDIContextManager;
import org.osgi.service.packageadmin.ExportedPackage;
import org.osgi.service.packageadmin.PackageAdmin;
import org.osgi.service.repository.Repository;

/**
 * This test exercises the OSGi-JNDI integration
 *
 * @author David Bosschaert
 */
@RunWith(Arquillian.class)
public class JNDITestCase {

    @Inject
    public BundleContext context;

    @Inject
    public Bundle bundle;

    @Deployment
    public static JavaArchive createdeployment() {
        final JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "example-jndi");
        archive.addClasses(RepositorySupport.class, NamingSupport.class, AriesSupport.class, BlueprintSupport.class);
        archive.addClasses(MyInterface.class);
        archive.addAsManifestResource(RepositorySupport.BUNDLE_VERSIONS_FILE);
        archive.setManifest(new Asset() {
            @Override
            public InputStream openStream() {
                OSGiManifestBuilder builder = OSGiManifestBuilder.newInstance();
                builder.addBundleSymbolicName(archive.getName());
                builder.addBundleManifestVersion(2);
                builder.addImportPackages(PackageAdmin.class);
                builder.addImportPackages(Context.class, InitialContextFactory.class, JNDIContextManager.class);
                builder.addImportPackages(XRequirementBuilder.class, Repository.class, Resource.class);
                builder.addExportPackages(MyInterface.class);
                return builder.openStream();
            }
        });
        return archive;
    }

    @Test
    public void testOSGiNamingContext() throws Exception {
        JNDIContextManager mgr = NamingSupport.provideJNDIIntegration(context, bundle);

        // Get the InitialContext and lookup the PackageAdmin OSGi service through JNDI
        Context ictx = mgr.newInitialContext();
        Object viaJNDI = ictx.lookup("osgi:service/" + PackageAdmin.class.getName());

        // Make an invocation on PackageAdmin
        PackageAdmin pa = (PackageAdmin) viaJNDI;
        ExportedPackage ep = pa.getExportedPackage(MyInterface.class.getPackage().getName());
        Assert.assertEquals(bundle, ep.getExportingBundle());
    }
}
