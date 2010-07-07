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
package org.jboss.osgi.test.performance.bundle;

import java.io.File;
import java.io.InputStream;

import org.jboss.arquillian.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.osgi.test.performance.AbstractPerformanceTestCase;
import org.jboss.osgi.test.performance.Parameter;
import org.jboss.osgi.test.shared.SharedClass;
import org.jboss.osgi.test.versioned.VersionedClass;
import org.jboss.osgi.testing.OSGiManifestBuilder;
import org.jboss.shrinkwrap.api.Asset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.runner.RunWith;
import org.osgi.framework.BundleContext;

/**
 * @author <a href="david@redhat.com">David Bosschaert</a>
 */
@RunWith(Arquillian.class)
public abstract class BundleTestBase extends AbstractPerformanceTestCase
{
   @Deployment
   public static JavaArchive createDeployment()
   {
      final JavaArchive archive = getTestBundleArchive();
      archive.setManifest(new Asset()
      {
         public InputStream openStream()
         {
            OSGiManifestBuilder builder = OSGiManifestBuilder.newInstance();
            builder.addBundleSymbolicName(archive.getName());
            builder.addBundleManifestVersion(2);
            builder.addExportPackages(BundleTestBase.class);
            builder.addImportPackages("org.jboss.arquillian.junit", "org.jboss.shrinkwrap.api", "org.jboss.shrinkwrap.api.spec");
            builder.addImportPackages("javax.inject", "org.junit", "org.junit.runner");
            builder.addImportPackages("org.osgi.framework");
            builder.addImportPackages("org.osgi.util.tracker");
            builder.addImportPackages("org.osgi.service.log");
            return builder.openStream();
         }
      });
      archive.addClasses(BundleTestBase.class, BundlePerfTestActivator.class, BundleInstallAndStartBenchmark.class);
      archive.addClasses(SharedClass.class, VersionedClass.class);
      archive.addClasses(Bundle100TestCase.class);
      return archive;
   }

   abstract BundleContext getBundleContext();

   void testPerformance(int size) throws Exception
   {
      BundleInstallAndStartBenchmark bm = new BundleInstallAndStartBenchmark(getBundleContext());

      // There is a problem with concurrent bundle installs it seems
      // int threads = Runtime.getRuntime().availableProcessors();
      int threads = 1;
      bm.run(threads, size / threads);
      
      File f = new File(getResultsDir(), "testBundlePerf" + size + "-" + System.currentTimeMillis() + ".xml");
      bm.reportXML(f, new Parameter("Threads", threads), new Parameter("Total Bundles", size));
   }
}
