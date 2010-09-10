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

import static org.jboss.osgi.test.performance.bundle.BundleInstallAndStartBenchmark.COMMON_BUNDLE_PREFIX;
import static org.jboss.osgi.test.performance.bundle.BundleInstallAndStartBenchmark.TEST_BUNDLE_PREFIX;
import static org.jboss.osgi.test.performance.bundle.BundleInstallAndStartBenchmark.UTIL_BUNDLE_PREFIX;
import static org.jboss.osgi.test.performance.bundle.BundleInstallAndStartBenchmark.VERSIONED_IMPL_BUNDLE_PREFIX;
import static org.jboss.osgi.test.performance.bundle.BundleInstallAndStartBenchmark.VERSIONED_INTF_BUNDLE_PREFIX;
import static org.junit.Assert.assertEquals;

import java.io.InputStream;

import javax.inject.Inject;

import org.jboss.arquillian.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.osgi.ArchiveProvider;
import org.jboss.arquillian.osgi.OSGiContainer;
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
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osgi.framework.BundleContext;

/**
 * This test exercises the Bundle Performance test code in a very basic manner to
 * ensure that it works. Passing this test is a precondition for running the real 
 * Bundle Performance tests.
 * 
 * @author <a href="david@redhat.com">David Bosschaert</a>
 */
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
            builder.addImportPackages("org.jboss.shrinkwrap.api", "org.jboss.shrinkwrap.api.asset");
            builder.addImportPackages("org.jboss.shrinkwrap.api.exporter", "org.jboss.shrinkwrap.api.spec");
            builder.addImportPackages("org.jboss.osgi.testing");
            builder.addImportPackages("org.junit", "org.junit.runner");
            builder.addImportPackages("org.osgi.framework", "org.osgi.util.tracker");
            builder.addImportPackages("javax.inject");
            return builder.openStream();
         }
      });
      archive.addClasses(BundleSmokeTestCase.class, BundlePerfTestActivator.class, BundleInstallAndStartBenchmark.class);
      archive.addClasses(CommonClass.class, VersionedInterface.class, VersionedClass.class);
      archive.addClasses(Util1.class, Util2.class, Util3.class, Util4.class, Util5.class);
      return archive;
   }

   @Inject
   public OSGiContainer container;

   OSGiContainer getOSGiContainer()
   {
      return container;
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
      BundleInstallAndStartBenchmark bm = new BundleInstallAndStartBenchmark(getOSGiContainer(), getBundleContext());
      bm.prepareTest(1, 5);
      bm.runThread("Thread_1_", 5);
   }

   public static class BundleArchiveProvider implements ArchiveProvider
   {
      @Override
      public JavaArchive getTestArchive(String name)
      {
         if (name.startsWith(TEST_BUNDLE_PREFIX))
            return getTestBundle(name);
         else if (name.startsWith(COMMON_BUNDLE_PREFIX))
            return getCommonBundle(name);
         else if (name.startsWith(UTIL_BUNDLE_PREFIX))
            return getUtilBundle(name);
         else if (name.startsWith(VERSIONED_INTF_BUNDLE_PREFIX))
            return getVersionedIntfBundle(name);
         else if (name.startsWith(VERSIONED_IMPL_BUNDLE_PREFIX))
            return getVersionedImplBundle(name);
         return null;
      }

      private JavaArchive getCommonBundle(String identifier)
      {
         String[] parts = identifier.split("#");
         assertEquals("Internal test error, invalid common bundle identifier: " + identifier,
               2, parts.length);
         final String version = parts[1];

         final JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "common-bundle-" + version);
         archive.setManifest(new Asset()
         {
            @Override
            public InputStream openStream()
            {
               OSGiManifestBuilder builder = OSGiManifestBuilder.newInstance();
               builder.addBundleSymbolicName("CommonBundle" + version);
               builder.addBundleManifestVersion(2);
               builder.addImportPackages("org.osgi.framework");
               builder.addImportPackages(CommonClass.class.getPackage().getName() + ";version=\"[" + version + ".0," + version + ".0]\"");
               builder.addExportPackages(CommonClass.class.getPackage().getName() + ";version=\"" + version + ".0\"");
               return builder.openStream();
            }
         });
         archive.addClasses(CommonClass.class);
         return archive;
      }

      private JavaArchive getUtilBundle(String identifier)
      {
         String[] parts = identifier.split("#");
         assertEquals("Internal test error, invalid util bundle identifier: " + identifier,
               2, parts.length);
         final int i = Integer.parseInt(parts[1]);

         final Class<?> utilClass = getUtilClass(i);

         final JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "util_" + i);
         archive.setManifest(new Asset()
         {
            @Override
            public InputStream openStream()
            {
               OSGiManifestBuilder builder = OSGiManifestBuilder.newInstance();
               builder.addBundleSymbolicName("Util" + i);
               builder.addBundleManifestVersion(2);
               builder.addImportPackages("org.osgi.framework");
               builder.addImportPackages(CommonClass.class.getPackage().getName() + ";version=\"[" + i + "," + i + "]\"");
               builder.addExportPackages(utilClass.getPackage().getName() + ";uses:=\"" + CommonClass.class.getPackage().getName() + "\"");
               return builder.openStream();
            }
         });
         archive.addClasses(utilClass);
         return archive;
      }

      private Class<?> getUtilClass(final int i)
      {
         switch (i)
         {
            case 1:
               return Util1.class;
            case 2:
               return Util2.class;
            case 3:
               return Util3.class;
            case 4:
               return Util4.class;
            case 5:
               return Util5.class;
         }
         return null;
      }

      private JavaArchive getVersionedIntfBundle(String identifier)
      {
         String[] parts = identifier.split("#");
         assertEquals("Internal test error, invalid versioned interfaces bundle identifier: " + identifier,
               2, parts.length);
         final String version = parts[1];

         final JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "versioned-intf-bundle-" + version);
         archive.setManifest(new Asset()
         {
            @Override
            public InputStream openStream()
            {
               OSGiManifestBuilder builder = OSGiManifestBuilder.newInstance();
               builder.addBundleSymbolicName("VersionedIntfBundle" + version);
               builder.addBundleManifestVersion(2);
               builder.addImportPackages("org.osgi.framework");
               builder.addExportPackages(VersionedInterface.class.getPackage().getName() + ";version=\"" + version + ".0\"");
               return builder.openStream();
            }
         });
         archive.addClasses(VersionedInterface.class);
         return archive;
      }

      private JavaArchive getVersionedImplBundle(String identifier)
      {
         String[] parts = identifier.split("#");
         assertEquals("Internal test error, invalid versioned implementation bundle identifier: " + identifier,
               2, parts.length);
         final int version = Integer.parseInt(parts[1]);

         final JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "versioned-impl-bundle-" + version);
         archive.setManifest(new Asset()
         {
            @Override
            public InputStream openStream()
            {
               Class<?> utilClass = getUtilClass(version);

               OSGiManifestBuilder builder = OSGiManifestBuilder.newInstance();
               builder.addBundleSymbolicName("VersionedImplBundle" + version);
               builder.addBundleManifestVersion(2);
               builder.addImportPackages("org.osgi.framework");
               builder.addImportPackages(CommonClass.class.getPackage().getName() + ";version=\"[" + version + ".0," + version + ".0]\"");
               builder.addImportPackages(VersionedInterface.class.getPackage().getName() + ";version=\"[" + version + ".0," + version + ".0]\"");
               builder.addImportPackages(utilClass.getPackage().getName());
               builder.addExportPackages(VersionedClass.class.getPackage().getName() + ";version=\"" + version + ".0\";uses:=\""
                     + utilClass.getPackage().getName() + "\"");
               return builder.openStream();
            }
         });
         archive.addClasses(VersionedClass.class);
         return archive;
      }

      private JavaArchive getTestBundle(String identifier)
      {
         String[] parts = identifier.split("#");
         assertEquals("Internal test error, invalid test bundle identifier: " + identifier,
               3, parts.length);
         final String threadName = parts[1];
         final int counter = Integer.parseInt(parts[2]);

         final JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "test-" + threadName + counter + ".jar");
         archive.setManifest(new Asset()
         {
            @Override
            public InputStream openStream()
            {
               OSGiManifestBuilder builder = OSGiManifestBuilder.newInstance();
               builder.addBundleSymbolicName(getBSN(threadName, counter));
               builder.addBundleActivator(BundlePerfTestActivator.class.getName());
               builder.addBundleManifestVersion(2);
               builder.addImportPackages("org.osgi.framework");

               int ver = (counter % 5) + 1;
               builder.addImportPackages(CommonClass.class.getPackage().getName() + ";version=\"[" + ver + ".0," + ver + ".0]\"");
               builder.addImportPackages(VersionedInterface.class.getPackage().getName() + ";version=\"[" + ver + ".0," + ver + ".0]\"");
               builder.addImportPackages(VersionedClass.class.getPackage().getName() + ";version=\"[" + ver + ".0," + ver + ".0]\"");
               return builder.openStream();
            }
         });
         archive.addClasses(BundlePerfTestActivator.class);

         return archive;
      }

      private static String getBSN(String threadName, int counter)
      {
         return "Bundle-" + threadName + "-" + counter;
      }
   }
}
