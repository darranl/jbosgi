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
package org.jboss.test.osgi.example.simple;

import org.jboss.osgi.testing.OSGiBundle;
import org.jboss.osgi.testing.OSGiRuntime;
import org.jboss.osgi.testing.OSGiRuntimeTest;
import org.junit.Test;
import org.osgi.framework.Bundle;

/**
 * A test that deployes a bundle and verifies its state
 * 
 * @author thomas.diesler@jboss.com
 * @since 12-Feb-2009
 */
public class SimpleRuntimeTestCase extends OSGiRuntimeTest {
    @Test
    public void testSimpleBundle() throws Exception {
        // Get the default runtime
        OSGiRuntime runtime = getRuntime();
        try {
            // Install the bundle
            OSGiBundle bundle = runtime.installBundle("example-simple.jar");
            assertBundleState(Bundle.INSTALLED, bundle.getState());

            // Start the bundle
            bundle.start();
            assertBundleState(Bundle.ACTIVE, bundle.getState());

            // Uninstall the bundle
            bundle.uninstall();
            assertBundleState(Bundle.UNINSTALLED, bundle.getState());
        } finally {
            // Shutdown the runtime
            runtime.shutdown();
        }
    }
}