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
package org.jboss.test.osgi.bootstrap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;

import org.jboss.osgi.spi.framework.OSGiBootstrap;
import org.jboss.osgi.spi.framework.OSGiBootstrapProvider;
import org.jboss.osgi.testing.OSGiTest;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.launch.Framework;

/**
 * Test the embedded bootstrap of the framework
 * 
 * @author thomas.diesler@jboss.com
 * @since 25-Feb-2009
 */
public class BootstrapTestCase extends OSGiTest {
    @Test
    public void testFrameworkBootstrap() throws Exception {
        OSGiBootstrapProvider bootProvider = OSGiBootstrap.getBootstrapProvider();
        Framework framework = bootProvider.getFramework();
        assertNotNull("Framework not null", framework);
        try {
            assertBundleState(Bundle.INSTALLED, framework.getState());
            assertEquals("BundleId == 0", 0, framework.getBundleId());
            assertNotNull("SymbolicName not null", framework.getSymbolicName());
            
            framework.init();
            assertBundleState(Bundle.STARTING, framework.getState());
            
        } finally {
            framework.stop();
            framework.waitForStop(2000);
        }
    }

    @Test
    public void testGetBootstrapProvider() throws Exception {
        OSGiBootstrapProvider bp1 = OSGiBootstrap.getBootstrapProvider();
        OSGiBootstrapProvider bp2 = OSGiBootstrap.getBootstrapProvider();
        assertNotSame("Multiple provider instances", bp1, bp2);
    }
}