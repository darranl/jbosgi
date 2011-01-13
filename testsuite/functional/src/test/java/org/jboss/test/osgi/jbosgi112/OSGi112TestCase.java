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
package org.jboss.test.osgi.jbosgi112;

import org.jboss.osgi.testing.OSGiBundle;
import org.jboss.osgi.testing.OSGiRuntimeTest;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.SynchronousBundleListener;

/**
 * [JBOSGI-112] Investigate Exception in SynchronousBundleListener
 * 
 * https://jira.jboss.org/jira/browse/JBOSGI-112
 * 
 * @author thomas.diesler@jboss.com
 * @since 19-Jun-2009
 */
public class OSGi112TestCase extends OSGiRuntimeTest {
    /**
     * BundleA registers a {@link SynchronousBundleListener} which throws an exception. Start of BundleB is started and expected
     * to be ACTIVE.
     */
    @Test
    public void testInstallBundles() throws Exception {
        OSGiBundle bundleA = getRuntime().installBundle("jbosgi112-bundleA.jar");
        bundleA.start();

        OSGiBundle bundleB = getRuntime().installBundle("jbosgi112-bundleB.jar");
        bundleB.start();

        // Exceptions thrown from listeners are logged but otherwise ignored.
        // Throwing an exception from a listener will not prevent the bundle from proceeding with starting.
        // If the BP extender detects some issue with the BP configuration, it can/should log this information
        // but it cannot prevent the bundle from starting.

        assertBundleState(Bundle.ACTIVE, bundleB.getState());
    }
}