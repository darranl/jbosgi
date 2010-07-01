package org.jboss.osgi.test.performance;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Map.Entry;
import java.util.Properties;

import org.osgi.framework.BundleContext;

public abstract class AbstractBenchmark implements PerformanceTest
{
   protected final BundleContext bundleContext;
   protected final File tempDir;

   protected AbstractBenchmark(BundleContext bc)
   {
      bundleContext = bc;
      tempDir = new File(bc.getDataFile(""), getClass().getSimpleName() + "-" + System.currentTimeMillis());
      tempDir.mkdirs();
      if (tempDir.listFiles().length > 0)
      {
         throw new IllegalStateException("The target directory is not empty");
      }
      System.out.println("Writing to: " + tempDir);
   }

   protected abstract ChartType[] getAllChartTypes();
   
   @Override
   public void reportXML(File targetFile) throws Exception
   {
      StringBuilder sb = new StringBuilder();
      sb.append("<test name='");
      sb.append(getClass().getName());
      sb.append("'>");

      /* TODO Obtain parameters from individual tests.
      sb.append("<parameters><parameter name='numThreads' value='");
      sb.append(numThreads);
      sb.append("'/><parameter name='servicesPerThread   ' value='");
      sb.append(numServicesPerThread);
      sb.append("'/><parameter name='totalServices' value='");
      sb.append(numThreads * numServicesPerThread);
      sb.append("'/></parameters>");
      */

      for (ChartType type : getAllChartTypes())
      {
         final String suffix = type.getName();
         for (File f : tempDir.listFiles(new FilenameFilter()
         {
            @Override
            public boolean accept(File dir, String name)
            {
               return name.endsWith(suffix);
            }
         }))
         {
            Properties p = new Properties();
            InputStream is = new FileInputStream(f);
            try
            {
               p.load(is);

               for (Entry<Object, Object> entry : p.entrySet())
               {
                  sb.append("<result type='");
                  sb.append(type.getName());
                  sb.append("' title='");
                  sb.append(type.getChartTitle());
                  sb.append("' x-axis='");
                  sb.append(type.getXAxisLabel());
                  sb.append("' y-axis='");
                  sb.append(type.getYAxisLabel());
                  sb.append("' x-value='");
                  sb.append(entry.getKey());
                  sb.append("' y-value='");
                  sb.append(entry.getValue());
                  sb.append("'/>");
               }
            }
            finally
            {
               if (is != null)
                  is.close();
            }
         }
      }
      sb.append("</test>");

      OutputStreamWriter writer = new FileWriter(targetFile);
      try
      {
         writer.write(sb.toString());
      }
      finally
      {
         if (writer != null)
            writer.close();
      }
      System.out.println("Wrote results to " + targetFile);
   }

   protected void writeData(ChartType c, Object x, Object y) throws IOException
   {
      File f = File.createTempFile("perf", "." + c.getName(), tempDir);
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
