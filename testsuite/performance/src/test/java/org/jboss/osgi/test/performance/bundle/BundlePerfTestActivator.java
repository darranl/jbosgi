package org.jboss.osgi.test.performance.bundle;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Properties;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class BundlePerfTestActivator implements BundleActivator
{
   private File getPrivateStorageDir()
   {
      return new File(System.getProperty("basedir") + "/target/performance-storage");
   }

   @Override
   public void start(BundleContext context) throws Exception
   {
      System.out.println("### Bundle Started " + context.getBundle().getSymbolicName());
      writeData(getPrivateStorageDir(), "started", context.getBundle().getSymbolicName(), System.currentTimeMillis());

      synchronized ("bundle-test") // VM-Global lock, outside of measuring section
      {
         int num = Integer.parseInt(System.getProperty("started-bundles", "0"));
         System.setProperty("started-bundles", "" + (num + 1));
      }
   }

   @Override
   public void stop(BundleContext context) throws Exception
   {
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
}
