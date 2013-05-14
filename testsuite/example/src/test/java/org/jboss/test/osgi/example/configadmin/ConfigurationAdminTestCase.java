/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
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

package org.jboss.test.osgi.example.configadmin;

import java.io.InputStream;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.concurrent.TimeUnit;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.osgi.metadata.OSGiManifestBuilder;
import org.jboss.osgi.provision.XResourceProvisioner;
import org.jboss.osgi.repository.XRepository;
import org.jboss.osgi.resolver.XResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.test.osgi.ConfigurationAdminSupport;
import org.jboss.test.osgi.ProvisionerSupport;
import org.jboss.test.osgi.example.api.ConfiguredService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.resource.Resource;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.repository.Repository;

/**
 * A test that shows how an OSGi {@link ManagedService} can be configured through the {@link ConfigurationAdmin}.
 *
 * @author Thomas.Diesler@jboss.com
 * @author David Bosschaert
 * @since 11-Dec-2010
 */
@RunWith(Arquillian.class)
public class ConfigurationAdminTestCase {

    static final String PID_A = "ConfigurationAdmin-PID-A";

    @ArquillianResource
    BundleContext context;

    @Deployment
    public static JavaArchive deployment() {
        final JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "configadmin-tests");
        archive.addClasses(ProvisionerSupport.class, ConfigurationAdminSupport.class);
        archive.addClasses(ConfiguredService.class);
        archive.addAsResource("repository/felix.configadmin.feature.xml");
        archive.setManifest(new Asset() {
            @Override
            public InputStream openStream() {
                OSGiManifestBuilder builder = OSGiManifestBuilder.newInstance();
                builder.addBundleSymbolicName(archive.getName());
                builder.addBundleManifestVersion(2);
                builder.addImportPackages(XRepository.class, Repository.class, XResource.class, Resource.class, XResourceProvisioner.class);
                builder.addDynamicImportPackages(ConfigurationAdmin.class);
                return builder.openStream();
            }
        });
        return archive;
    }

    @Test
    @InSequence(0)
    public void addConfigurationAdminSupport() throws Exception {
        ProvisionerSupport.installCapabilities(context, "felix.configadmin.feature");
    }

    @Test
    @InSequence(1)
    public void testManagedService(@ArquillianResource Bundle bundle) throws Exception {

        bundle.start();
        
        // Get the {@link Configuration} for the given PID
        ConfigurationAdmin configAdmin = ConfigurationAdminSupport.getConfigurationAdmin(bundle);
        Configuration config = configAdmin.getConfiguration(PID_A);
        Assert.assertNotNull("Config not null", config);
        Assert.assertNull("Config is empty, but was: " + config.getProperties(), config.getProperties());

        try {
            Dictionary<String, String> configProps = new Hashtable<String, String>();
            configProps.put("foo", "bar");
            config.update(configProps);

            // Register a {@link ManagedService}
            ConfiguredService service = new ConfiguredService();
            Dictionary<String, String> serviceProps = new Hashtable<String, String>();
            serviceProps.put(Constants.SERVICE_PID, PID_A);
            String[] clazzes = new String[] { ConfiguredService.class.getName(), ManagedService.class.getName() };
            bundle.getBundleContext().registerService(clazzes, service, serviceProps);

            // Wait a little for the update event
            Assert.assertTrue("Service updated", service.awaitUpdate(3, TimeUnit.SECONDS));

            // Verify service property
            Assert.assertEquals("bar", service.getProperties().get("foo"));
        } finally {
            config.delete();
        }
    }
}

