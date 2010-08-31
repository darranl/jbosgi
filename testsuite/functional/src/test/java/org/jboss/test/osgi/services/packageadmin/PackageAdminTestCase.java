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
package org.jboss.test.osgi.services.packageadmin;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeNotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CountDownLatch;

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
import org.jboss.test.osgi.bundles.exporter.ExportedClass;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.FrameworkListener;
import org.osgi.framework.ServiceReference;
import org.osgi.service.packageadmin.PackageAdmin;

/**
 * Tests PackageAdmin functionality.
 * 
 * @author <a href="david@redhat.com">David Bosschaert</a>
 */
public class PackageAdminTestCase extends OSGiRuntimeTest
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

         streamFileToRuntime(runtime, getImportingBundle());
         streamFileToRuntime(runtime, getExportingBundle());

         // Install the test case bundle
         OSGiBundle bundle = runtime.installBundle(getTestBundle());
         bundle.start();
      }
   }

   @Test
   public void testRefreshPackages() throws Exception
   {
      if (context == null)
         BridgeFactory.getBridge().run();

      assumeNotNull(context);
      
      File ibFile = context.getBundle(0).getBundleContext().getDataFile("opt-import-bundle.jar");
      File ebFile = context.getBundle(0).getBundleContext().getDataFile("exporting-bundle.jar");

      Bundle importing = null, exporting = null;
      FrameworkListener frameworkListener = null;
      try
      {
         importing = context.installBundle(ibFile.toURI().toString());
         importing.start();
         try
         {
            importing.loadClass("org.jboss.test.osgi.bundles.exporter.ExportedClass");
            fail("Should not be able to load the class: ExportedClass");
         }
         catch (ClassNotFoundException cnfe)
         {
            // good
         }

         exporting = context.installBundle(ebFile.toURI().toString());
         exporting.start();
         try
         {
            importing.loadClass("org.jboss.test.osgi.bundles.exporter.ExportedClass");
            fail("Should still not be able to load the class, as this bundle is not yet refreshed: ExportedClass");
         }
         catch (ClassNotFoundException cnfe)
         {
            // good
         }
         
         final CountDownLatch latch = new CountDownLatch(1);
         frameworkListener = new FrameworkListener()
         {
            @Override
            public void frameworkEvent(FrameworkEvent event)
            {
               if (event.getType() == FrameworkEvent.PACKAGES_REFRESHED)
                  latch.countDown();
            }
         };
         context.addFrameworkListener(frameworkListener);

         ServiceReference sref = context.getServiceReference(PackageAdmin.class.getName());
         PackageAdmin pa = (PackageAdmin)context.getService(sref);
         pa.refreshPackages(new Bundle[] { importing });

         latch.await(60, SECONDS);

         // This call should now succeed.
         assertNotNull(importing.loadClass("org.jboss.test.osgi.bundles.exporter.ExportedClass"));
      }
      finally
      {
         if (frameworkListener != null)
            context.removeFrameworkListener(frameworkListener);

         if (importing != null)
            importing.uninstall();
         if (exporting != null)
            exporting.uninstall();
      }
   }

   private void streamFileToRuntime(OSGiRuntime runtime, JavaArchive archive) throws IOException
   {
      VirtualFile inFile = toVirtualFile(archive);
      File outFile = runtime.getBundle(0).getDataFile(archive.getName() + ".jar").getAbsoluteFile();

      InputStream in = inFile.openStream();
      FileOutputStream out = new FileOutputStream(outFile);

      VFSUtils.copyStream(in, out);

      in.close();
      out.close();
   }

   private JavaArchive getImportingBundle()
   {
      // Bundle-SymbolicName: opt-import-bundle
      // Import-Package: org.jboss.test.osgi.bundles.exporter;resolution:=optional
      final JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "opt-import-bundle");
      archive.setManifest(new Asset()
      {
         @Override
         public InputStream openStream()
         {
            OSGiManifestBuilder builder = OSGiManifestBuilder.newInstance();
            builder.addBundleManifestVersion(2);
            builder.addBundleSymbolicName(archive.getName());
            builder.addImportPackages(ExportedClass.class.getPackage().getName() + ";resolution:=optional");
            return builder.openStream();
         }
      });

      return archive;
   }

   private JavaArchive getExportingBundle()
   {
      // Bundle-SymbolicName: exporting-bundle
      // Export-Package: org.jboss.test.osgi.bundles.exporter
      final JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "exporting-bundle");
      archive.addClasses(ExportedClass.class);
      archive.setManifest(new Asset()
      {
         @Override
         public InputStream openStream()
         {
            OSGiManifestBuilder builder = OSGiManifestBuilder.newInstance();
            builder.addBundleManifestVersion(2);
            builder.addBundleSymbolicName(archive.getName());
            builder.addExportPackages(ExportedClass.class);
            return builder.openStream();
         }
      });
      return archive;
   }

   private JavaArchive getTestBundle()
   {
      // Bundle-SymbolicName: package-admin-test
      // Export-Package: org.jboss.test.osgi.services.packageadmin
      final JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "package-admin-test");
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
