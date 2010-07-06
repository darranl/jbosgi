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
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.Properties;

import org.jboss.arquillian.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.osgi.test.performance.AbstractPerformanceTestCase;
import org.jboss.osgi.testing.OSGiManifestBuilder;
import org.jboss.shrinkwrap.api.Asset;
import org.jboss.shrinkwrap.api.Assignable;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.runner.RunWith;
import org.osgi.framework.Bundle;
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
      archive.addClasses(BundleTestBase.class, BundlePerfTestActivator.class);
      archive.addClasses(Bundle100TestCase.class);
      return archive;
   }

   abstract BundleContext getBundleContext();

   private File getPrivateStorageDir()
   {
      File f = new File(System.getProperty("basedir") + "/target/performance-storage");
      f.mkdirs();
      return f;
   }

   void testPerformance(int size) throws Exception
   {
      File dir = getPrivateStorageDir();
      for (int i = 0; i < size; i++)
      {
         InputStream is = getBundle(i);
         writeData(dir, "inst", "Bundle-" + i, System.currentTimeMillis());
         Bundle bundle = getBundleContext().installBundle("someloc" + i, is);
         bundle.start();
      }

      // Wait until all bundles have been started
      int numStartedBundles = 0;
      while (numStartedBundles < size)
      {
         System.out.println("Waiting for all bundles to be started");
         Thread.sleep(1000);
         synchronized ("bundle-test")
         {
            numStartedBundles = Integer.parseInt(System.getProperty("started-bundles", "0"));
         }
      }
   }

   private void writeData(File dir, String type, Object x, Object y) throws Exception
   {
      File f = File.createTempFile("perf", "." + type, dir);
      OutputStream fos = null;
      try
      {
         fos = new FileOutputStream(f);
         Properties p = new Properties();
         p.setProperty(x.toString(), y.toString());
         p.store(fos, "");
      }
      finally
      {
         if (fos != null)
         {
            fos.close();
         }
      }
   }

   private InputStream getBundle(final int counter) throws Exception
   {
      final JavaArchive jar = ShrinkWrap.create("test-" + counter + ".jar", JavaArchive.class);
      jar.setManifest(new Asset()
      {
         @Override
         public InputStream openStream()
         {
            OSGiManifestBuilder builder = OSGiManifestBuilder.newInstance();
            builder.addBundleSymbolicName("Bundle-" + counter);
            builder.addBundleActivator(BundlePerfTestActivator.class.getName());
            builder.addBundleManifestVersion(2);
            return builder.openStream();
         }
      });
      jar.addClasses(BundlePerfTestActivator.class);

      // Temp workaround - need to use reflection to obtain ZipExporter to avoid classloader issues
      // Can use normal types once ARQ-208 is fixed.
      @SuppressWarnings("unchecked")
      Class<Assignable> zeClass = (Class<Assignable>)getClass().getClassLoader().loadClass("org.jboss.shrinkwrap.api.exporter.ZipExporter");
      Object ze = jar.as(zeClass);
      Method m = zeClass.getMethod("exportZip");
      return (InputStream)m.invoke(ze);
   }
   
}
