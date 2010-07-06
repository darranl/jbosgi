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
import java.io.OutputStream;
import java.util.Properties;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * @author <a href="david@redhat.com">David Bosschaert</a>
 */
public class BundlePerfTestActivator implements BundleActivator
{
   private File getPrivateStorageDir(String threadName)
   {
      return new File(System.getProperty("basedir") + "/target/performance-bundle-data/" + threadName);
   }

   @Override
   public void start(BundleContext context) throws Exception
   {
      long started = System.currentTimeMillis();
      String bsn = context.getBundle().getSymbolicName();

      String threadName = getThreadName(bsn);
      writeData(getPrivateStorageDir(threadName), "started", bsn, started);
      synchronized (threadName.intern()) // VM-Global lock, outside of measuring section
      {
         int num = Integer.parseInt(System.getProperty("started-bundles", "0"));
         System.setProperty("started-bundles", "" + (num + 1));
      }
   }

   @Override
   public void stop(BundleContext context) throws Exception
   {
   }

   private void writeData(File dir, String event, Object x, Object y) throws Exception
   {
      File f = File.createTempFile("perf", "." + event, dir);
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

   private static String getThreadName(String bsn)
   {
      int idx1 = bsn.indexOf('-');
      int idx2 = bsn.lastIndexOf('-');

      return bsn.substring(idx1 + 1, idx2);
   }
}
