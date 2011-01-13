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

import static org.jboss.osgi.http.HttpServiceCapability.DEFAULT_HTTP_SERVICE_PORT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import org.jboss.osgi.http.HttpServiceCapability;
import org.jboss.osgi.testing.OSGiRuntime;
import org.jboss.osgi.testing.OSGiRuntimeTest;
import org.jboss.osgi.testing.OSGiServiceReference;
import org.junit.BeforeClass;
import org.junit.Test;
import org.osgi.service.http.HttpService;

/**
 * A test that deployes a bundle that containes a HttpServlet which is registered through the OSGi HttpService
 * 
 * @author thomas.diesler@jboss.com
 * @since 23-Jan-2009
 */
public class HttpServiceTestCase extends OSGiRuntimeTest {
    @BeforeClass
    public static void setUpClass() throws Exception {
        OSGiRuntime runtime = createDefaultRuntime();
        runtime.addCapability(new HttpServiceCapability());

        // Allow 10s for the HttpService to become available
        OSGiServiceReference sref = runtime.getServiceReference(HttpService.class.getName(), 10000);
        assertNotNull("HttpService not null", sref);

        runtime.installBundle("example-http.jar").start();
    }

    @Test
    public void testServletAccess() throws Exception {
        String line = getHttpResponse("/servlet?test=plain", 5000);
        assertEquals("Hello from Servlet", line);
    }

    @Test
    public void testServletInitProps() throws Exception {
        String line = getHttpResponse("/servlet?test=initProp", 5000);
        assertEquals("initProp=SomeValue", line);
    }

    @Test
    public void testServletBundleContext() throws Exception {
        String line = getHttpResponse("/servlet?test=context", 5000);
        assertEquals("example-http", line);
    }

    @Test
    public void testServletStartLevel() throws Exception {
        String line = getHttpResponse("/servlet?test=startLevel", 5000);
        assertEquals("startLevel=1", line);
    }

    @Test
    public void testResourceAccess() throws Exception {
        String line = getHttpResponse("/file/message.txt", 5000);
        assertEquals("Hello from Resource", line);
    }

    private String getHttpResponse(String reqPath, int timeout) throws IOException {
        return HttpServiceCapability.getHttpResponse(getServerHost(), DEFAULT_HTTP_SERVICE_PORT, reqPath, timeout);
    }
}