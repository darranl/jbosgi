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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.jboss.osgi.test.performance.AbstractThreadedBenchmark;
import org.jboss.osgi.test.performance.ChartType;
import org.jboss.osgi.test.performance.ChartTypeImpl;
import org.jboss.osgi.testing.OSGiManifestBuilder;
import org.jboss.shrinkwrap.api.Asset;
import org.jboss.shrinkwrap.api.Assignable;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

/**
 * @author <a href="david@redhat.com">David Bosschaert</a>
 */
public class BundleInstallAndStartBenchmark extends AbstractThreadedBenchmark<Integer>
{
   private static final ChartType INSTALL_START = new ChartTypeImpl("IS", "Bundle Install and Start Time", "Number of Bundles", "Time (ms)");

   protected BundleInstallAndStartBenchmark(BundleContext bc)
   {
      super(bc);
   }
   
   @Override
   protected ChartType[] getAllChartTypes()
   {
      return new ChartType[] { INSTALL_START };
   }

   public void run(int numThreads, int numBundlesPerThread) throws Exception
   {
      runTest(numThreads, numBundlesPerThread);

      // Once all the tests are finished, collect the measurements and write them to disk
      long totalDuration = 0;
      File parent = getPrivateStorageDir("");
      for (File dir : parent.listFiles())
      {
         if (!dir.isDirectory())
            continue;

         Map<String, Long> startTimes = new HashMap<String, Long>();
         for (File f : dir.listFiles(new FilenameFilter()
         {
            @Override
            public boolean accept(File dir, String name)
            {
               return name.endsWith(".inst");
            }
         }))
         {
            Properties p = new Properties();
            InputStream is = new FileInputStream(f);
            try
            {
               p.load(is);

               if (p.size() != 1)
               {
                  throw new IllegalStateException("Unexpected size of properties file");
               }
               String bsn = p.propertyNames().nextElement().toString();
               long value = Long.parseLong(p.getProperty(bsn));
               startTimes.put(bsn, value);
            }
            finally
            {
               is.close();
            }
         }

         for (File f : dir.listFiles(new FilenameFilter()
         {
            @Override
            public boolean accept(File dir, String name)
            {
               return name.endsWith(".started");
            }
         }))
         {
            Properties p = new Properties();
            InputStream is = new FileInputStream(f);
            try
            {
               p.load(is);

               if (p.size() != 1)
               {
                  throw new IllegalStateException("Unexpected size of properties file");
               }

               String bsn = p.propertyNames().nextElement().toString();
               long endTime = Long.parseLong(p.getProperty(bsn));
               if (!startTimes.containsKey(bsn))
               {
                  throw new IllegalStateException("Start time of bundle " + bsn + " not reported");
               }
               totalDuration += (endTime - startTimes.get(bsn));
            }
            finally
            {
               is.close();
            }
         }
      }
      writeData(INSTALL_START, numBundlesPerThread, totalDuration);
   }

   @Override
   protected void runThread(String threadName, Integer numBundlesPerThread) throws Exception
   {
      File tempDir = getPrivateStorageDir(threadName);
      for (int i = 0; i < numBundlesPerThread; i++)
      {
         InputStream is = getBundle(threadName, i);

         writeData(tempDir, "inst", getBSN(threadName, i), System.currentTimeMillis());
         Bundle bundle = bundleContext.installBundle(threadName + i, is);
         bundle.start();
      }

      // Wait until all bundles have been started
      int numStartedBundles = 0;
      while (numStartedBundles < numBundlesPerThread)
      {
         System.out.println("Waiting for all bundles to be started: " + threadName);
         Thread.sleep(1000);
         synchronized (threadName.intern())
         {
            numStartedBundles = Integer.parseInt(System.getProperty("started-bundles", "0"));
         }
      }
   }

   private File getPrivateStorageDir(String threadName)
   {
      File f = new File(System.getProperty("basedir") + "/target/performance-bundle-data/" + threadName);
      f.mkdirs();
      return f;
   }

   private InputStream getBundle(final String threadName, final int counter) throws Exception
   {
      final JavaArchive jar = ShrinkWrap.create("test-" + threadName + "_" + counter + ".jar", JavaArchive.class);
      jar.setManifest(new Asset()
      {
         @Override
         public InputStream openStream()
         {
            OSGiManifestBuilder builder = OSGiManifestBuilder.newInstance();
            builder.addBundleSymbolicName(getBSN(threadName, counter));
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

   private static String getBSN(String threadName, int counter)
   {
      return "Bundle-" + threadName + "-" + counter;
   }
}
