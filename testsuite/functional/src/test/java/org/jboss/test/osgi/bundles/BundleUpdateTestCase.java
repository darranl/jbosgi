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
package org.jboss.test.osgi.bundles;

import static org.junit.Assume.assumeNotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.jboss.osgi.husky.BridgeFactory;
import org.jboss.osgi.husky.HuskyCapability;
import org.jboss.osgi.husky.RuntimeContext;
import org.jboss.osgi.testing.OSGiBundle;
import org.jboss.osgi.testing.OSGiManifestBuilder;
import org.jboss.osgi.testing.OSGiRuntime;
import org.jboss.osgi.testing.OSGiRuntimeTest;
import org.jboss.osgi.vfs.VFSUtils;
import org.jboss.osgi.vfs.VirtualFile;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.test.osgi.bundles.update1.A1;
import org.jboss.test.osgi.bundles.update2.A2;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Version;

/**
 * Tests Bundle API methods.
 * 
 * @author <a href="david@redhat.com">David Bosschaert</a>
 */
public class BundleUpdateTestCase extends OSGiRuntimeTest
{
   @RuntimeContext
   public BundleContext context;

   @Before
   public void setUp() throws Exception
   {
      if (context == null)
      {
         OSGiRuntime runtime = createDefaultRuntime();
         runtime.addCapability(new HuskyCapability());

         streamFileToRuntime(runtime, getInitialBundle(), "initial.jar");
         streamFileToRuntime(runtime, getUpdatedBundle(), "updated.jar");

         // Install the test case bundle
         OSGiBundle bundle = runtime.installBundle(getTestBundle());
         bundle.start();
      }
   }

   @Test
   public void testUpdateBundle() throws Exception
   {
      if (context == null)
         BridgeFactory.getBridge().run();

      assumeNotNull(context);
      File initialFile = context.getBundle(0).getBundleContext().getDataFile("initial.jar");
      File updatedFile = context.getBundle(0).getBundleContext().getDataFile("updated.jar");
      Bundle bundle = null;
      try
      {
         bundle = context.installBundle(initialFile.toURI().toString());
         bundle.start();

         assertLoadClass(bundle, "org.jboss.test.osgi.bundles.update1.A1");
         assertLoadClassFail(bundle, "org.jboss.test.osgi.bundles.update2.A2");

         bundle.update(updatedFile.toURI().toURL().openStream());
         assertLoadClass(bundle, "org.jboss.test.osgi.bundles.update2.A2");
         assertLoadClassFail(bundle, "org.jboss.test.osgi.bundles.update1.A1");
      }
      finally
      {
         bundle.uninstall();
      }
   }

   private void streamFileToRuntime(OSGiRuntime runtime, JavaArchive archive, String fileName) throws IOException
   {
      VirtualFile inFile = toVirtualFile(archive);
      File outFile = runtime.getBundle(0).getDataFile(fileName).getAbsoluteFile();

      InputStream in = inFile.openStream();
      FileOutputStream out = new FileOutputStream(outFile);

      VFSUtils.copyStream(in, out);

      in.close();
      out.close();
   }

   private JavaArchive getInitialBundle()
   {
      // Bundle-SymbolicName: update-test
      // Bundle-Version: 1
      // Export-Package: org.jboss.test.osgi.bundles.update1
      final JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "update-test");
      archive.addClass(A1.class);
      archive.setManifest(new Asset()
      {
         @Override
         public InputStream openStream()
         {
            OSGiManifestBuilder builder = OSGiManifestBuilder.newInstance();
            builder.addBundleManifestVersion(2);
            builder.addBundleSymbolicName(archive.getName());
            builder.addBundleVersion(Version.parseVersion("1"));
            builder.addExportPackages(A1.class);
            return builder.openStream();
         }
      });
      return archive;
   }

   private JavaArchive getUpdatedBundle()
   {
      // Bundle-SymbolicName: update-test
      // Bundle-Version: 2
      // Export-Package: org.jboss.test.osgi.bundles.update2
      final JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "update-test");
      archive.addClass(A2.class);
      archive.setManifest(new Asset()
      {
         @Override
         public InputStream openStream()
         {
            OSGiManifestBuilder builder = OSGiManifestBuilder.newInstance();
            builder.addBundleManifestVersion(2);
            builder.addBundleSymbolicName(archive.getName());
            builder.addBundleVersion(Version.parseVersion("2"));
            builder.addExportPackages(A2.class);
            return builder.openStream();
         }
      });
      return archive;
   }

   private JavaArchive getTestBundle()
   {
      // Bundle-SymbolicName: bundle-update-test
      // Export-Package: org.jboss.test.osgi.bundles
      final JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "bundle-update-test");
      archive.addClasses(getClass());
      archive.setManifest(new Asset()
      {
         @Override
         public InputStream openStream()
         {
            OSGiManifestBuilder builder = OSGiManifestBuilder.newInstance();
            builder.addBundleManifestVersion(2);
            builder.addBundleSymbolicName(archive.getName());
            builder.addExportPackages(getClass().getPackage().getName());
            builder.addImportPackages("org.jboss.osgi.spi.capability", "org.jboss.osgi.husky", "org.jboss.osgi.testing", "org.junit", "org.osgi.framework");
            builder.addImportPackages("org.jboss.shrinkwrap.api", "org.jboss.shrinkwrap.api.asset", "org.jboss.shrinkwrap.api.spec");
            builder.addManifestHeader("Test-Package", getClass().getPackage().getName());
            return builder.openStream();
         }
      });
      return archive;
   }
}
