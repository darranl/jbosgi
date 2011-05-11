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

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.jboss.osgi.deployment.interceptor.LifecycleInterceptorException;
import org.jboss.osgi.testing.OSGiBundle;
import org.jboss.osgi.testing.OSGiRuntime;
import org.jboss.osgi.testing.OSGiRuntimeTest;
import org.jboss.osgi.webapp.WebAppCapability;
import org.junit.BeforeClass;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;

/**
 * A test that deployes a WAR bundle that contains no WEB-INF/web.xml
 * 
 * @author thomas.diesler@jboss.com
 * @since 26-Oct-2009
 */
public class WebAppNegativeTestCase extends OSGiRuntimeTest {
    
    @BeforeClass
    public static void setUpClass() throws Exception {
        OSGiRuntime runtime = createDefaultRuntime();
        runtime.addCapability(new WebAppCapability());
    }

    @Test
    public void testServletAccess() throws Exception {
        OSGiBundle bundle = getRuntime().installBundle("example-webapp-negative.war");
        assertBundleState(Bundle.INSTALLED, bundle.getState());
        try {
            bundle.start();
            fail("BundleException expected");
        } catch (BundleException ex) {
            Throwable cause = ex.getCause();
            assertTrue(cause instanceof LifecycleInterceptorException);
        }
    }
}
