/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat, Inc., and individual contributors
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

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.osgi.resolver.v2.XRequirementBuilder;
import org.jboss.osgi.testing.OSGiManifestBuilder;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.test.osgi.ConfigurationAdminSupport;
import org.jboss.test.osgi.RepositorySupport;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.resource.Resource;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.ConfigurationEvent;
import org.osgi.service.cm.ConfigurationListener;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.repository.Repository;

import javax.inject.Inject;
import java.io.InputStream;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.jboss.test.osgi.ConfigurationAdminSupport.provideConfigurationAdmin;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * A test that shows how an OSGi {@link org.osgi.service.cm.ManagedService}
 * can be configured through the {@link org.osgi.service.cm.ConfigurationAdmin}.
 *
 * @author Thomas.Diesler@jboss.com
 * @since 11-Dec-2010
 */
@RunWith(Arquillian.class)
public class ConfigurationAdminTestCase {

    @Inject
    public BundleContext context;

    @Inject
    public Bundle bundle;

    @Deployment
    public static JavaArchive createdeployment() {
        final JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "example-configadmin");
        archive.addClasses(ConfiguredService.class, ConfigurationAdminSupport.class, RepositorySupport.class);
        archive.addAsManifestResource(RepositorySupport.BUNDLE_VERSIONS_FILE);
        archive.setManifest(new Asset() {
            public InputStream openStream() {
                OSGiManifestBuilder builder = OSGiManifestBuilder.newInstance();
                builder.addBundleSymbolicName(archive.getName());
                builder.addBundleManifestVersion(2);
                builder.addImportPackages(ConfigurationAdmin.class);
                builder.addImportPackages(XRequirementBuilder.class, Repository.class, Resource.class);
                return builder.openStream();
            }
        });
        return archive;
    }

    @Test
    public void testManagedService() throws Exception {

        // Get the {@link ConfigurationAdmin}
        ConfigurationAdmin configAdmin = provideConfigurationAdmin(context, bundle);

        // Start the test bundle
        bundle.start();

        final CountDownLatch latch = new CountDownLatch(1);
        ConfigurationListener listener = new ConfigurationListener() {
            @Override
            public void configurationEvent(ConfigurationEvent event) {
                if (ConfiguredService.SERVICE_PID.equals(event.getPid()))
                    latch.countDown();
            }
        };
        BundleContext bundlecontext = bundle.getBundleContext();
        bundlecontext.registerService(ConfigurationListener.class.getName(), listener, null);

        // Get the {@link Configuration} for the given PID
        Configuration config = configAdmin.getConfiguration(ConfiguredService.SERVICE_PID);
        assertNotNull("Config not null", config);
        try
        {
            Dictionary<String, String> configProps = new Hashtable<String, String>();
            configProps.put("foo", "bar");
            config.update(configProps);

            // Register a {@link ManagedService}
            Dictionary<String, String> serviceProps = new Hashtable<String, String>();
            serviceProps.put(Constants.SERVICE_PID, ConfiguredService.SERVICE_PID);
            context.registerService(new String[] { ConfiguredService.class.getName(), ManagedService.class.getName() }, new ConfiguredService(), serviceProps);

            // Wait a little for the update event
            if (latch.await(5, TimeUnit.SECONDS) == false)
                throw new TimeoutException();

            // Verify service property
            ServiceReference sref = bundlecontext.getServiceReference(ConfiguredService.class.getName());
            ConfiguredService service = (ConfiguredService) bundlecontext.getService(sref);
            assertEquals("bar", service.getValue("foo"));
        }
        finally
        {
            config.delete();
        }
    }
}
