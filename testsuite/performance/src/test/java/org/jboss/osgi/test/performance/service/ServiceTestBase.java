package org.jboss.osgi.test.performance.service;

import java.io.File;

import org.jboss.arquillian.api.Deployment;
import org.jboss.osgi.test.performance.AbstractPerformanceTestCase;
import org.jboss.osgi.test.performance.Parameter;
import org.jboss.osgi.testing.OSGiManifestBuilder;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

public abstract class ServiceTestBase extends AbstractPerformanceTestCase
{
   @Deployment
   public static JavaArchive createDeployment()
   {
      JavaArchive archive = getTestBundleArchive();
      OSGiManifestBuilder manifest = getTestManifestBuilder(archive);

      manifest.addBundleActivator(CreateAndLookupTestActivator.class.getName());
      manifest.addExportPackages(ServiceTestBase.class);
      manifest.addImportPackages("org.osgi.util.tracker");

      archive.addClasses(CreateAndLookupTestActivator.class, CreateAndLookupBenchmark.class, ServiceTestBase.class);
      return archive;
   }

   abstract Bundle getBundle();

   void testPerformance(int size) throws Exception
   {
      getBundle().start();

      BundleContext bc = getBundle().getBundleContext();
      CreateAndLookupBenchmark tc = getService(bc, CreateAndLookupBenchmark.class);
      int processors = Runtime.getRuntime().availableProcessors();
      tc.run(processors, size / processors);
      File f = new File(getResultsDir(), "testPerformance" + size + ".xml");
      tc.reportXML(f, new Parameter("Threads", processors), new Parameter("Total Services", size));

      getBundle().stop();
   }
}
