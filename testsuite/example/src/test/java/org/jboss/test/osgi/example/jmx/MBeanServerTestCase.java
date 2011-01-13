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
package org.jboss.test.osgi.example.jmx;

import static org.jboss.test.osgi.example.jmx.bundle.FooMBean.MBEAN_NAME;
import static org.junit.Assert.assertEquals;

import org.jboss.osgi.testing.OSGiBundle;
import org.jboss.osgi.testing.OSGiRuntimeTest;
import org.jboss.test.osgi.example.jmx.bundle.FooMBean;
import org.junit.Test;

/**
 * A test that deployes a bundle that registeres an MBean
 * 
 * @author thomas.diesler@jboss.com
 * @since 12-Feb-2009
 */
public class MBeanServerTestCase extends OSGiRuntimeTest {
    @Test
    public void testMBeanAccess() throws Exception {
        OSGiBundle bundle = getRuntime().installBundle("example-jmx.jar");
        try {
            bundle.start();

            FooMBean foo = getRuntime().getMBeanProxy(MBEAN_NAME, FooMBean.class);
            assertEquals("hello", foo.echo("hello"));
        } finally {
            bundle.uninstall();
        }
    }
}