package org.jboss.osgi.test.performance;

import java.io.File;
import java.io.InputStream;

import org.jboss.osgi.testing.OSGiManifestBuilder;
import org.jboss.shrinkwrap.api.Asset;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

public abstract class AbstractPerformanceTestCase
{
   protected static JavaArchive getTestBundleArchive()
   {
      final JavaArchive archive = ShrinkWrap.create("test.jar", JavaArchive.class);
      archive.addClasses(AbstractBenchmark.class, AbstractThreadedBenchmark.class, ChartType.class, ChartTypeImpl.class, Parameter.class,
            PerformanceBenchmark.class);
      return archive;
   }

   protected static OSGiManifestBuilder getTestManifestBuilder(JavaArchive archive)
   {
      final OSGiManifestBuilder builder = OSGiManifestBuilder.newInstance();
      builder.addBundleSymbolicName(archive.getName());
      builder.addBundleManifestVersion(2);
      builder.addExportPackages(ChartType.class);
      builder.addImportPackages("org.jboss.arquillian.junit", "org.jboss.shrinkwrap.api", "org.jboss.shrinkwrap.api.spec");
      builder.addImportPackages("javax.inject", "org.junit", "org.junit.runner");

      archive.setManifest(new Asset()
      {
         @Override
         public InputStream openStream()
         {
            return builder.openStream();
         }
      });

      return builder;
   }

   protected File getResultsDir()
   {
      File f = new File(System.getProperty("basedir") + "/target/performance-results");
      f.mkdirs();
      return f;
   }

   protected <T> T getService(BundleContext bc, Class<T> c) throws InterruptedException
   {
      ServiceTracker st = new ServiceTracker(bc, c.getName(), null);
      st.open();
      try
      {
         return c.cast(st.waitForService(100000));
      }
      finally
      {
         st.close();
      }
   }
}
