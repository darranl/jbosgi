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
package org.jboss.test.osgi.jbosgi37;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.jboss.osgi.spi.capability.CompendiumCapability;
import org.jboss.osgi.testing.OSGiBundle;
import org.jboss.osgi.testing.OSGiRuntime;
import org.jboss.osgi.testing.OSGiRuntimeTest;
import org.junit.Test;
import org.osgi.framework.Bundle;

/**
 * [JBOSGI-37] Prevent creation of deployment unit for nested jars
 * 
 * https://jira.jboss.org/jira/browse/JBOSGI-37
 * 
 * @author thomas.diesler@jboss.com
 * @since 09-Apr-2009
 */
public class OSGi37TestCase extends OSGiRuntimeTest {
    @Test
    public void testNestedBundle() throws Exception {
        OSGiRuntime runtime = createDefaultRuntime();
        try {
            runtime.addCapability(new CompendiumCapability());

            OSGiBundle bundleA = runtime.installBundle("jbosgi37-bundleA.jar");
            bundleA.start();

            assertBundleState(Bundle.ACTIVE, bundleA.getState());

            List<String> relevant = new ArrayList<String>();
            for (OSGiBundle bundle : runtime.getBundles()) {
                String symbolicName = bundle.getSymbolicName();
                if (symbolicName.startsWith("jbosgi37"))
                    relevant.add(symbolicName);
            }

            assertEquals("No Sub Bundle", 1, relevant.size());
            assertEquals("jbosgi37-bundleA", relevant.get(0));

            bundleA.uninstall();
        } finally {
            runtime.shutdown();
        }
    }
}