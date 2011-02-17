/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.test.osgi.example.simple;

import static org.junit.Assert.assertEquals;

import java.io.InputStream;

import javax.inject.Inject;

import org.jboss.arquillian.api.ArchiveProvider;
import org.jboss.arquillian.api.DeploymentProvider;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.osgi.testing.OSGiManifestBuilder;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.test.osgi.example.simple.bundle.SimpleActivator;
import org.jboss.test.osgi.example.simple.bundle.SimpleService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * Test the arquillian callback to a client provided archive
 *
 * @author thomas.diesler@jboss.com
 * @since 09-Sep-2010
 */
@RunWith(Arquillian.class)
public class SimpleArchiveProviderTestCase {

   @Inject
   public DeploymentProvider provider;

   @Inject
   public BundleContext context;

    @Test
    public void testBundleInjection() throws Exception {
        InputStream input = provider.getClientDeploymentAsStream("example-archive-provider");
        Bundle bundle = context.installBundle("example-archive-provider", input);
        try {
            assertEquals("Bundle INSTALLED", Bundle.INSTALLED, bundle.getState());

            bundle.start();
            assertEquals("Bundle ACTIVE", Bundle.ACTIVE, bundle.getState());

            bundle.stop();
            assertEquals("Bundle RESOLVED", Bundle.RESOLVED, bundle.getState());
        } finally {
            bundle.uninstall();
            assertEquals("Bundle UNINSTALLED", Bundle.UNINSTALLED, bundle.getState());
        }
    }

    @ArchiveProvider
    public static JavaArchive getTestArchive(String name) {
        final JavaArchive archive = ShrinkWrap.create(JavaArchive.class, name);
        archive.addClasses(SimpleActivator.class, SimpleService.class);
        archive.setManifest(new Asset() {
            public InputStream openStream() {
                OSGiManifestBuilder builder = OSGiManifestBuilder.newInstance();
                builder.addBundleSymbolicName(archive.getName());
                builder.addBundleManifestVersion(2);
                builder.addBundleActivator(SimpleActivator.class.getName());
                builder.addImportPackages(BundleActivator.class);
                return builder.openStream();
            }
        });
        return archive;
    }
}
