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
package org.jboss.test.osgi.example.http;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.osgi.resolver.v2.XRequirementBuilder;
import org.jboss.osgi.testing.OSGiManifestBuilder;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.test.osgi.HttpServiceSupport;
import org.jboss.test.osgi.HttpSupport;
import org.jboss.test.osgi.RepositorySupport;
import org.jboss.test.osgi.example.http.bundle.EndpointServlet;
import org.jboss.test.osgi.example.http.bundle.HttpExampleActivator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.resource.Resource;
import org.osgi.service.http.HttpService;
import org.osgi.service.packageadmin.PackageAdmin;
import org.osgi.service.repository.Repository;
import org.osgi.util.tracker.ServiceTracker;

import javax.inject.Inject;
import javax.servlet.Servlet;
import javax.servlet.http.HttpServlet;
import java.io.IOException;
import java.io.InputStream;

import static org.jboss.test.osgi.HttpServiceSupport.provideHttpService;
import static org.junit.Assert.assertEquals;

/**
 * A test that deployes a bundle that containes a HttpServlet which is registered through the OSGi HttpService
 *
 * @author thomas.diesler@jboss.com
 * @since 23-Jan-2009
 */
@RunWith(Arquillian.class)
public class HttpServiceTestCase {

    @Inject
    public BundleContext context;

    @Inject
    public Bundle bundle;

    @Deployment
    public static JavaArchive createdeployment() {
        final JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "example-http");
        archive.addClasses(HttpExampleActivator.class, EndpointServlet.class, RepositorySupport.class, HttpServiceSupport.class, HttpSupport.class);
        archive.addAsResource("http/message.txt", "res/message.txt");
        archive.addAsManifestResource(RepositorySupport.BUNDLE_VERSIONS_FILE);
        archive.setManifest(new Asset() {
            public InputStream openStream() {
                OSGiManifestBuilder builder = OSGiManifestBuilder.newInstance();
                builder.addBundleSymbolicName(archive.getName());
                builder.addBundleManifestVersion(2);
                builder.addBundleActivator(HttpExampleActivator.class);
                builder.addImportPackages(BundleActivator.class, PackageAdmin.class, ServiceTracker.class);
                builder.addImportPackages(HttpService.class, HttpServlet.class, Servlet.class);
                builder.addImportPackages(XRequirementBuilder.class, Repository.class, Resource.class);
                return builder.openStream();
            }
        });
        return archive;
    }

    @Test
    public void testServletAccess() throws Exception {
        provideHttpService(context, bundle);
        bundle.start();
        String line = getHttpResponse("/example-http/servlet?test=plain", 5000);
        assertEquals("Hello from Servlet", line);
    }

    @Test
    public void testServletInitProps() throws Exception {
        provideHttpService(context, bundle);
        bundle.start();
        String line = getHttpResponse("/example-http/servlet?test=initProp", 5000);
        assertEquals("initProp=SomeValue", line);
    }

    @Test
    public void testServletBundleContext() throws Exception {
        provideHttpService(context, bundle);
        bundle.start();
        String line = getHttpResponse("/example-http/servlet?test=context", 5000);
        assertEquals("example-http", line);
    }

    @Test
    public void testResourceAccess() throws Exception {
        provideHttpService(context, bundle);
        bundle.start();
        String line = getHttpResponse("/example-http/file/message.txt", 5000);
        assertEquals("Hello from Resource", line);
    }

    private String getHttpResponse(String reqPath, int timeout) throws IOException {
        return HttpSupport.getHttpResponse("localhost", 8090, reqPath, timeout);
    }
}