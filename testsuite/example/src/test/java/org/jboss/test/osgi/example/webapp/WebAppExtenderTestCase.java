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
package org.jboss.test.osgi.example.webapp;

import static org.jboss.osgi.http.HttpServiceCapability.DEFAULT_HTTP_SERVICE_PORT;
import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.jboss.osgi.http.HttpServiceCapability;
import org.jboss.osgi.testing.OSGiRuntime;
import org.jboss.osgi.testing.OSGiRuntimeTest;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * A test that deployes a WAR bundle
 * 
 * Due to the nature of asynchronous event processing by the extender pattern, we cannot assume that the endpoint is available
 * immediately
 * 
 * @author thomas.diesler@jboss.com
 * @since 06-Oct-2009
 */
public class WebAppExtenderTestCase extends OSGiRuntimeTest {
    
    @BeforeClass
    public static void beforeClass() throws Exception {
        OSGiRuntime runtime = createDefaultRuntime();
        runtime.addCapability(new HttpServiceCapability());

        // Conditionally install the webapp extender in case we test against
        // a runtime where it is not installed already
        if (runtime.getBundle("org.ops4j.pax.web.pax-web-extender-war", null) == null)
            runtime.installBundle("bundles/pax-web-extender-war.jar").start();

        runtime.installBundle("example-webapp.war").start();
    }

    @Test
    public void testServletAccess() throws Exception {
        String line = getHttpResponse("/example-webapp/servlet?test=plain", 5000);
        assertEquals("Hello from Servlet", line);
    }

    @Test
    public void testServletInitProps() throws Exception {
        String line = getHttpResponse("/example-webapp/servlet?test=initProp", 5000);
        assertEquals("initProp=SomeValue", line);
    }

    @Test
    public void testResourceAccess() throws Exception {
        String line = getHttpResponse("/example-webapp/message.txt", 5000);
        assertEquals("Hello from Resource", line);
    }

    private String getHttpResponse(String reqPath, int timeout) throws IOException {
        return HttpServiceCapability.getHttpResponse(getServerHost(), DEFAULT_HTTP_SERVICE_PORT, reqPath, timeout);
    }
}