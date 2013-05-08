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
package org.jboss.test.osgi.example.eventadmin;

import static org.junit.Assert.assertEquals;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

import org.jboss.arquillian.container.test.api.Deployer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.osgi.metadata.OSGiManifestBuilder;
import org.jboss.osgi.repository.XRepository;
import org.jboss.osgi.resolver.XRequirement;
import org.jboss.osgi.resolver.XRequirementBuilder;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.test.osgi.ConfigurationAdminSupport;
import org.jboss.test.osgi.EventAdminSupport;
import org.jboss.test.osgi.FrameworkUtils;
import org.jboss.test.osgi.MetatypeSupport;
import org.jboss.test.osgi.RepositorySupport;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;
import org.osgi.resource.Resource;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.osgi.service.repository.Repository;

/**
 * A test that deployes the EventAdmin and sends/receives messages on a topic.
 *
 * @author thomas.diesler@jboss.com
 * @since 08-Dec-2009
 */
@RunWith(Arquillian.class)
public class EventAdminTestCase {

    static final String EVENT_ADMIN_PROVIDER = "event-admin-provider";
    static final String EVENT_ADMIN_BUNDLE = "event-admin-bundle";

    static final String TOPIC = "org/jboss/test/osgi/example/event";

    @ArquillianResource
    Deployer deployer;

    @ArquillianResource
    BundleContext context;

    @Deployment(name = EVENT_ADMIN_PROVIDER)
    public static JavaArchive eventadminProvider() {
        final JavaArchive archive = ShrinkWrap.create(JavaArchive.class, EVENT_ADMIN_PROVIDER);
        archive.addClasses(EventAdminSupport.class, ConfigurationAdminSupport.class, MetatypeSupport.class, FrameworkUtils.class, RepositorySupport.class);
        archive.addAsManifestResource(RepositorySupport.BUNDLE_VERSIONS_FILE);
        archive.setManifest(new Asset() {
            @Override
            public InputStream openStream() {
                OSGiManifestBuilder builder = OSGiManifestBuilder.newInstance();
                builder.addBundleSymbolicName(archive.getName());
                builder.addBundleManifestVersion(2);
                builder.addImportPackages(XRequirementBuilder.class, XRequirement.class, XRepository.class, Repository.class, Resource.class);
                builder.addDynamicImportPackages(EventAdmin.class.getPackage().getName());
                return builder.openStream();
            }
        });
        return archive;
    }

    @Deployment(name = EVENT_ADMIN_BUNDLE, managed = false, testable = false)
    public static JavaArchive testBundle() {
        final JavaArchive archive = ShrinkWrap.create(JavaArchive.class, EVENT_ADMIN_BUNDLE);
        archive.setManifest(new Asset() {
            @Override
            public InputStream openStream() {
                OSGiManifestBuilder builder = OSGiManifestBuilder.newInstance();
                builder.addBundleSymbolicName(archive.getName());
                builder.addBundleManifestVersion(2);
                builder.addImportPackages(EventAdmin.class);
                return builder.openStream();
            }
        });
        return archive;
    }

    @Test
    @InSequence(0)
    public void addEventAdminSupport(@ArquillianResource Bundle bundle) throws BundleException {
        EventAdminSupport.provideEventAdmin(context, bundle);
    }

    @Test
    @InSequence(1)
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testEventHandler() throws Exception {
        InputStream input = deployer.getDeployment(EVENT_ADMIN_BUNDLE);
        Bundle bundle = context.installBundle(EVENT_ADMIN_BUNDLE, input);
        try {
            bundle.start();

            BundleContext context = bundle.getBundleContext();

            // Register the EventHandler
            Dictionary param = new Hashtable();
            param.put(EventConstants.EVENT_TOPIC, new String[] { TOPIC });
            final List<Event> received = new ArrayList<Event>();
            EventHandler eventHandler = new EventHandler() {
                @Override
                public void handleEvent(Event event) {
                    received.add(event);
                }
            };
            context.registerService(EventHandler.class.getName(), eventHandler, param);

            // Send event through the the EventAdmin
            ServiceReference sref = context.getServiceReference(EventAdmin.class.getName());
            EventAdmin eventAdmin = (EventAdmin) context.getService(sref);
            eventAdmin.sendEvent(new Event(TOPIC, (Dictionary) null));

            // Verify received event
            assertEquals("Event received", 1, received.size());
            assertEquals(TOPIC, received.get(0).getTopic());
        } finally {
            bundle.uninstall();
        }
    }
}