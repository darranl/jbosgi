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
package org.jboss.osgi.test.performance.service;

import java.io.File;
import java.io.InputStream;

import org.jboss.arquillian.api.Deployment;
import org.jboss.osgi.test.performance.AbstractPerformanceTestCase;
import org.jboss.osgi.test.performance.Parameter;
import org.jboss.osgi.testing.OSGiManifestBuilder;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

/**
 * @author <a href="david@redhat.com">David Bosschaert</a>
 */
public abstract class ServiceTestBase extends AbstractPerformanceTestCase
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
            builder.addBundleActivator(CreateAndLookupTestActivator.class.getName());
            builder.addExportPackages(ServiceTestBase.class);
            builder.addImportPackages("org.jboss.arquillian.junit", "org.jboss.logging");
            builder.addImportPackages("org.jboss.shrinkwrap.api", "org.jboss.shrinkwrap.api.spec");
            builder.addImportPackages("org.junit", "org.junit.runner");
            builder.addImportPackages("org.osgi.framework", "org.osgi.util.tracker");
            builder.addImportPackages("javax.inject");
            return builder.openStream();
         }
      });
      archive.addClasses(CreateAndLookupTestActivator.class, CreateAndLookupBenchmark.class, ServiceTestBase.class);
      archive.addClasses(SvcCls.class, SvcCls1.class, SvcCls2.class, SvcCls3.class, SvcCls4.class, SvcCls5.class);
      archive.addClasses(SvcCls6.class, SvcCls7.class, SvcCls8.class, SvcCls9.class, SvcCls10.class);
      archive.addClasses(SvcCls11.class, SvcCls12.class, SvcCls13.class, SvcCls14.class, SvcCls15.class);
      archive.addClasses(SvcCls16.class, SvcCls17.class, SvcCls18.class, SvcCls19.class, SvcCls20.class);
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
      File f = new File(getResultsDir(), "testPerformance" + size + "-" + System.currentTimeMillis() + ".xml");
      tc.reportXML(f, new Parameter("Threads", processors), new Parameter("Population", size));

      getBundle().stop();
   }
}
