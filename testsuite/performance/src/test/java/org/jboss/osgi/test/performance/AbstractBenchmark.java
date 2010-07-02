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

/**
 * @author <a href="david@redhat.com">David Bosschaert</a>
 */
public abstract class AbstractBenchmark implements PerformanceBenchmark
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
   public void reportXML(File targetFile, Parameter... parameters) throws Exception
   {
      StringBuilder sb = new StringBuilder();
      sb.append("<test name='");
      sb.append(getClass().getName());
      sb.append("'>");

      sb.append("<parameters>");
      for (Parameter p : parameters)
      {
         sb.append("<parameter name='");
         sb.append(p.getName());
         sb.append("' value='");
         sb.append(p.getValue());
         sb.append("'/>");
      }
      sb.append("</parameters>");

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
      
      Runtime r = Runtime.getRuntime();
      sb.append("<runtime processors='" + r.availableProcessors());
      sb.append("' total-memory='" + r.totalMemory());
      sb.append("' max-memory='" + r.maxMemory());
      sb.append("'/>");

      sb.append("<sysprops>");
      Properties p = System.getProperties();
      for (Entry<Object, Object> entry : p.entrySet())
      {
         sb.append("<prop name='");
         sb.append(entry.getKey().toString());
         sb.append("' value='");
         sb.append(entry.getValue().toString());
         sb.append("'/>");
      }
      sb.append("</sysprops>");
      
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
