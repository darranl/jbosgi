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
package org.jboss.test.osgi.example.interceptor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

import org.jboss.osgi.http.HttpServiceCapability;
import org.jboss.osgi.testing.OSGiRuntime;
import org.jboss.osgi.testing.OSGiRuntimeTest;
import org.jboss.osgi.testing.OSGiServiceReference;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.osgi.service.http.HttpService;

/**
 * A test that deployes a bundle that contains some metadata and an interceptor bundle that processes the metadata and
 * registeres an http endpoint from it.
 * 
 * The idea is that the bundle does not process its own metadata. Instead this work is delegated to some specialized metadata
 * processor (i.e. the interceptor)
 * 
 * @author thomas.diesler@jboss.com
 * @since 23-Oct-2009
 */
public class InterceptorTestCase extends OSGiRuntimeTest {
    private static OSGiRuntime runtime;

    @BeforeClass
    public static void setUpClass() throws Exception {
        runtime = createDefaultRuntime();
        runtime.addCapability(new HttpServiceCapability());

        // Allow 10s for the HttpService to become available
        OSGiServiceReference sref = runtime.getServiceReference(HttpService.class.getName(), 10000);
        assertNotNull("HttpService not null", sref);

        runtime.installBundle("example-interceptor.jar").start();
        runtime.installBundle("example-interceptor-bundle.jar").start();
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        runtime.shutdown();
        runtime = null;
    }

    @Test
    public void testServletAccess() throws Exception {
        String line = getHttpResponse("/servlet");
        assertEquals("Hello from Servlet", line);
    }

    private String getHttpResponse(String reqPath) throws Exception {
        URL url = new URL("http://" + runtime.getServerHost() + ":8090" + reqPath);
        BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
        return br.readLine();
    }
}