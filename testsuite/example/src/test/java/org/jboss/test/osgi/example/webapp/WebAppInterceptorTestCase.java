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
import static org.junit.Assert.fail;

import java.io.IOException;

import org.jboss.osgi.http.HttpServiceCapability;
import org.jboss.osgi.testing.OSGiBundle;
import org.jboss.osgi.testing.OSGiRuntime;
import org.jboss.osgi.testing.OSGiRuntimeTest;
import org.jboss.osgi.webapp.WebAppCapability;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * A test that deployes a WAR bundle
 * 
 * @author thomas.diesler@jboss.com
 * @since 06-Oct-2009
 */
public class WebAppInterceptorTestCase extends OSGiRuntimeTest {
    
    static OSGiBundle webappBundle;
    
    @BeforeClass
    public static void setUpClass() throws Exception {
        OSGiRuntime runtime = createDefaultRuntime();
        runtime.addCapability(new WebAppCapability());
        webappBundle = runtime.installBundle("example-webapp.war");
        webappBundle.start();
    }
    
    @Test
    public void testResourceAccess() throws Exception {
        // FIXME: http://issues.ops4j.org/browse/PAXWEB-182
        String line = getHttpResponse("/example-webapp/message.txt", 20000);
        assertEquals("Hello from Resource", line);
    }

    @Test
    public void testServletAccess() throws Exception {
        String line = getHttpResponse("/example-webapp/servlet?test=plain", 20000);
        assertEquals("Hello from Servlet", line);
    }

    @Test
    public void testServletInitProps() throws Exception {
        String line = getHttpResponse("/example-webapp/servlet?test=initProp", 20000);
        assertEquals("initProp=SomeValue", line);
    }

    @Test
    public void testServletAccessAfterStop() throws Exception {
        webappBundle.stop();
        try {
            getHttpResponse("/example-webapp/servlet?test=plain", 20000);
            fail("IOException expected");
        }
        catch (IOException ex) {
            // expected
        }
    }

    private String getHttpResponse(String reqPath, int timeout) throws IOException {
        return HttpServiceCapability.getHttpResponse(getServerHost(), DEFAULT_HTTP_SERVICE_PORT, reqPath, timeout);
    }
}