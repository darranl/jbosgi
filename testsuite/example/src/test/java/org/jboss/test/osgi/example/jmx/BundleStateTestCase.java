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

import static java.util.concurrent.TimeUnit.SECONDS;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.net.URL;
import java.util.Arrays;

import javax.management.openmbean.TabularData;

import org.apache.aries.jmx.framework.BundleState;
import org.jboss.osgi.testing.OSGiBundle;
import org.jboss.osgi.testing.OSGiRuntimeTest;
import org.junit.Test;
import org.osgi.jmx.framework.BundleStateMBean;
import org.osgi.jmx.framework.FrameworkMBean;

/**
 * Test {@link BundleState} functionality
 * 
 * @author thomas.diesler@jboss.com
 * @author <a href="david@redhat.com">David Bosschaert</a>
 * @since 15-Feb-2010
 */
public class BundleStateTestCase extends OSGiRuntimeTest {
    
    @Test
    public void testBundleStateMBean() throws Exception {
        BundleStateMBean bundleState = getRuntime().getBundleStateMBean();
        assertNotNull("BundleStateMBean not null", bundleState);

        TabularData bundleData = bundleState.listBundles();
        assertNotNull("TabularData not null", bundleData);
        assertFalse("TabularData not empty", bundleData.isEmpty());
    }

    @Test
    public void testUpdateBundle() throws Exception {
        FrameworkMBean fw = getRuntime().getFrameworkMBean();
        BundleStateMBean bs = getRuntime().getBundleStateMBean();

        // Install and start a bundle via JMX that exports a package
        URL bundleURL = getTestArchiveURL("example-jmx-update1.jar");
        long bundleId = fw.installBundle(bundleURL.toString());
        fw.startBundle(bundleId);

        // Obtain the exported packages through JMX
        assertEquals("[org.jboss.test.osgi.example.jmx.bundle.update1;0.0.0]", Arrays.toString(bs.getExportedPackages(bundleId)));
        long lm = bs.getLastModified(bundleId);

        // Update the bundle into a new bundle
        URL updatedURL = getTestArchiveURL("example-jmx-update2.jar");
        fw.updateBundleFromURL(bundleId, updatedURL.toString());

        assertTrue("The lastmodified should be changed", lm < bs.getLastModified(bundleId));
        // The bundle should now export both the old and the new packages...
        assertTrue(Arrays.toString(bs.getExportedPackages(bundleId)).contains("org.jboss.test.osgi.example.jmx.bundle.update2;"));
        assertTrue(Arrays.toString(bs.getExportedPackages(bundleId)).contains("org.jboss.test.osgi.example.jmx.bundle.update1;"));

        // Refreshing the bundle should get rid of the old packages that were exported
        fw.refreshBundle(bundleId);

        // PackageAdmin refreshBundles is async, so we have to wait on a condition
        waitForExportedPackagesCondition(bs, bundleId);

        assertEquals("[org.jboss.test.osgi.example.jmx.bundle.update2;0.0.0]", Arrays.toString(bs.getExportedPackages(bundleId)));

        // Install a bundle that depends on the updated bundle
        OSGiBundle depBundle = getRuntime().installBundle("example-jmx-update2-user.jar");
        depBundle.start();
        long depId = depBundle.getBundleId();
        assertEquals("[org.jboss.test.osgi.example.jmx.bundle.update2;0.0.0]", Arrays.toString(bs.getImportedPackages(depId)));
        assertEquals("ACTIVE", bs.getState(depId));

        // Install an unrelated bundle, this should return to active when refreshed
        OSGiBundle bundle3 = getRuntime().installBundle("example-jmx-update3.jar");
        bundle3.start();
        long b3Id = bundle3.getBundleId();
        assertEquals("ACTIVE", bs.getState(b3Id));

        // Uninstall the updated bundle, because it has dependencies it will remain to be
        // available until we call refreshBundle...
        fw.uninstallBundle(bundleId);
        assertEquals("[org.jboss.test.osgi.example.jmx.bundle.update2;0.0.0]", Arrays.toString(bs.getExportedPackages(bundleId)));
        assertEquals("ACTIVE", bs.getState(depId));

        // Refresh the uninstalled bundle and the unrelated bundle, this should really
        // remove it and unresolve the dependent bundle. The unrelated bundle should go back
        // to being active.
        fw.refreshBundles(new long[] { bundleId, b3Id });
        waitForBundleStateCondition(bs, depId, "INSTALLED");
        assertEquals("INSTALLED", bs.getState(depId));

        waitForBundleStateCondition(bs, b3Id, "ACTIVE");
        assertEquals("ACTIVE", bs.getState(b3Id));
    }

    private void waitForExportedPackagesCondition(BundleStateMBean bs, long bundleId) throws Exception {
        int secs = 10;
        while (secs > 0) {
            String exported = Arrays.toString(bs.getExportedPackages(bundleId));
            if ("[org.jboss.test.osgi.example.jmx.bundle.update2;0.0.0]".equals(exported))
                return;

            secs--;
            SECONDS.sleep(1);
        }
        fail("Did not reach the expected state with packages refreshed");
    }

    private void waitForBundleStateCondition(BundleStateMBean bs, long bundleId, String expectedState) throws Exception {
        int secs = 10;
        while (secs > 0) {
            String state = bs.getState(bundleId);
            if (expectedState.equals(state))
                return;

            secs--;
            SECONDS.sleep(1);
        }
        fail("Did not reach the expected state with packages refreshed");
    }
}