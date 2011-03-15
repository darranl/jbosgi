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
package org.jboss.test.osgi.jbosgi99;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStreamWriter;

import org.jboss.logging.Logger;
import org.jboss.osgi.spi.util.ConstantsHelper;
import org.jboss.osgi.testing.OSGiBundle;
import org.jboss.osgi.testing.OSGiRuntimeTest;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;

/**
 * [JBOSGI-99] No explicit control over bundle.start()
 * 
 * https://jira.jboss.org/jira/browse/JBOSGI-99
 * 
 * @author thomas.diesler@jboss.com
 * @since 08-Jul-2009
 */
public class OSGi99TestCase extends OSGiRuntimeTest {
    // Provide logging
    private static final Logger log = Logger.getLogger(OSGi99TestCase.class);

    @Test
    public void testAllGood() throws Exception {
        OSGiBundle bundle = getRuntime().installBundle("jbosgi99-allgood.jar");
        try {
            assertBundleState(Bundle.INSTALLED, bundle.getState());

            bundle.start();
            assertBundleState(Bundle.ACTIVE, bundle.getState());
        } finally {
            bundle.uninstall();
            assertBundleState(Bundle.UNINSTALLED, bundle.getState());
        }
    }

    @Test
    public void testFailOnResolve() throws Exception {
        OSGiBundle bundle = getRuntime().installBundle("jbosgi99-failonresolve.jar");
        try {
            assertBundleState(Bundle.INSTALLED, bundle.getState());

            try {
                bundle.start();
                fail("BundleException expected");
            } catch (BundleException ex) {
                log.error("State on error: " + ConstantsHelper.bundleState(bundle.getState()), ex);
                assertBundleState(Bundle.INSTALLED, bundle.getState());
            }
        } finally {
            bundle.uninstall();
            assertBundleState(Bundle.UNINSTALLED, bundle.getState());
        }
    }

    @Test
    public void testFailOnStart() throws Exception {
        OSGiBundle bundle = getRuntime().installBundle("jbosgi99-failonstart.jar");
        try {
            assertBundleState(Bundle.INSTALLED, bundle.getState());

            try {
                bundle.start();
                fail("BundleException expected");
            } catch (BundleException ex) {
                log.error("State on error: " + ConstantsHelper.bundleState(bundle.getState()), ex);
                assertBundleState(Bundle.RESOLVED, bundle.getState());
            }
        } finally {
            bundle.uninstall();
            assertBundleState(Bundle.UNINSTALLED, bundle.getState());
        }
    }

    @Test
    public void testHotDeploy() throws Exception {
        String targetContainer = getTargetContainer();
        if (targetContainer == null)
            return;

        boolean isRuntimeTarget = targetContainer.equals("runtime");
        boolean isAS7Target = targetContainer.startsWith("jboss70");

        String depoydir = null;
        if (isRuntimeTarget == true)
            depoydir = "deploy";
        else if (isAS7Target == true)
            depoydir = "deployments";
        else
            fail("Unsupported target container: " + targetContainer);

        // [JBOSGI-210] Bundle installed but not started with hot deploy
        File inFile = getTestArchiveFile("jbosgi99-allgood.jar");

        // Copy the bundle to the data directory
        String outPath = getRuntime().getBundle(0).getDataFile("jbosgi99-allgood.jar").getAbsolutePath();
        File outFile = new File(outPath);
        copyfile(inFile, outFile);

        // Move the bundle to the deploy directory
        outPath = outPath.substring(0, outPath.indexOf("data/osgi-store"));
        File deployFile = new File(outPath + depoydir + "/jbosgi99-allgood.jar");
        outFile.renameTo(deployFile);

        // Write the .dodeploy marker file
        if (isAS7Target == true) {
            File undeployedFile = new File(outPath + depoydir + "/jbosgi99-allgood.jar.undeployed");
            if (undeployedFile.exists())
                undeployedFile.delete();
            
            File dodeployFile = new File(outPath + depoydir + "/jbosgi99-allgood.jar.dodeploy");
            FileOutputStream fos = new FileOutputStream(dodeployFile);
            new OutputStreamWriter(fos).write("*.dodeploy marker");
            fos.close();
        }

        try {
            int timeout = 8000;
            OSGiBundle bundle = null;
            while (timeout > 0) {
                bundle = getRuntime().getBundle("jbosgi99-allgood", null);
                if (bundle != null && bundle.getState() == Bundle.ACTIVE)
                    break;

                Thread.sleep(200);
                timeout -= 200;
            }

            assertNotNull("Bundle not null", bundle);
            assertBundleState(Bundle.ACTIVE, bundle.getState());

            // Delete the bundle from the deploy directory
            String[] fileList = new File(outPath + depoydir).list(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.startsWith("jbosgi99-allgood");
                }
            });
            for (String filename : fileList) {
                File file = new File(outPath + depoydir + "/" + filename);
                file.delete();
            }

            timeout = 8000;
            while (timeout > 0) {
                if (bundle.getState() == Bundle.UNINSTALLED)
                    break;

                Thread.sleep(200);
                timeout -= 200;
            }

            assertBundleState(Bundle.UNINSTALLED, bundle.getState());
        } finally {
            if (deployFile.exists())
                deployFile.delete();
        }
    }

    private void copyfile(File inFile, File outFile) throws IOException {
        FileInputStream in = new FileInputStream(inFile);
        FileOutputStream out = new FileOutputStream(outFile);

        int c;
        while ((c = in.read()) != -1)
            out.write(c);

        in.close();
        out.close();
    }
}