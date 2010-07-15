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

import java.io.InputStream;

import javax.inject.Inject;

import org.jboss.arquillian.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.osgi.test.common.CommonClass;
import org.jboss.osgi.test.performance.AbstractPerformanceTestCase;
import org.jboss.osgi.test.util1.Util1;
import org.jboss.osgi.test.util2.Util2;
import org.jboss.osgi.test.util3.Util3;
import org.jboss.osgi.test.util4.Util4;
import org.jboss.osgi.test.util5.Util5;
import org.jboss.osgi.test.versioned.VersionedInterface;
import org.jboss.osgi.test.versioned.impl.VersionedClass;
import org.jboss.osgi.testing.OSGiManifestBuilder;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osgi.framework.BundleContext;

/**
 * @author <a href="david@redhat.com">David Bosschaert</a>
 */
@Ignore
@RunWith(Arquillian.class)
public class BundleSmokeTestCase extends AbstractPerformanceTestCase
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
            builder.addExportPackages(BundleSmokeTestCase.class);
            builder.addImportPackages("org.jboss.arquillian.junit", "org.jboss.logging");
            builder.addImportPackages("org.jboss.shrinkwrap.api", "org.jboss.shrinkwrap.api.spec");
            builder.addImportPackages("org.jboss.shrinkwrap.api.exporter");
            builder.addImportPackages("org.junit", "org.junit.runner");
            builder.addImportPackages("org.osgi.framework", "org.osgi.util.tracker");
            builder.addImportPackages("javax.inject");
            builder.addImportPackages("org.jboss.osgi.testing");
            return builder.openStream();
         }
      });
      archive.addClasses(BundleSmokeTestCase.class, BundlePerfTestActivator.class, BundleInstallAndStartBenchmark.class);
      archive.addClasses(CommonClass.class, VersionedInterface.class, VersionedClass.class);
      archive.addClasses(Util1.class, Util2.class, Util3.class, Util4.class, Util5.class);
      return archive;
   }

   @Inject
   public BundleContext bundleContext;

   BundleContext getBundleContext()
   {
      return bundleContext;
   }

   @Test
   public void test5() throws Exception
   {
      BundleInstallAndStartBenchmark bm = new BundleInstallAndStartBenchmark(getBundleContext());
      bm.prepareTest(1, 5);
      bm.runThread("Thread_1_", 5);
   }
}
