package org.jboss.osgi.test.performance.service;

import java.io.File;

import javax.inject.Inject;

import org.jboss.arquillian.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.osgi.test.performance.AbstractPerformanceTestCase;
import org.jboss.osgi.testing.OSGiManifestBuilder;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

@RunWith(Arquillian.class)
public class ServiceTestCase extends AbstractPerformanceTestCase
{
   @Deployment
   public static JavaArchive createDeployment()
   {
      JavaArchive archive = getTestBundleArchive();
      OSGiManifestBuilder manifest = getTestManifestBuilder(archive);

      manifest.addBundleActivator(CreateAndLookupTestActivator.class.getName());
      manifest.addExportPackages(ServiceTestCase.class);
      manifest.addImportPackages("org.osgi.util.tracker");

      archive.addClasses(CreateAndLookupTestActivator.class, CreateAndLookupBenchmark.class, ServiceTestCase.class);
      return archive;
   }

   @Inject
   public BundleContext context;
   @Inject
   public Bundle bundle;

   @Test
   public void testPerformance() throws Exception
   {
      System.out.println("BundleContext: " + context.getBundle().getSymbolicName());
      System.out.println("Bundle: " + bundle.getSymbolicName());
      bundle.start();

      BundleContext bc = bundle.getBundleContext();
      CreateAndLookupBenchmark tc = getService(bc, CreateAndLookupBenchmark.class);
      tc.run(2, 500);
      File f = new File(getResultsDir(), "testPerformance.xml");
      tc.reportXML(f);
      bundle.stop();
   }
}
