/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
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

package org.jboss.test.osgi.example.xservice;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.InputStream;
import java.util.jar.JarFile;

import org.jboss.modules.ModuleIdentifier;
import org.jboss.msc.service.ServiceActivator;
import org.jboss.msc.service.ServiceController.State;
import org.jboss.osgi.testing.OSGiBundle;
import org.jboss.osgi.testing.OSGiManifestBuilder;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.test.osgi.example.xservice.api.Echo;
import org.jboss.test.osgi.example.xservice.bundle.TargetBundleActivator;
import org.jboss.test.osgi.example.xservice.module.ClientModuleActivator;
import org.jboss.test.osgi.example.xservice.module.EchoInvokerService;
import org.junit.Test;
import org.osgi.framework.Bundle;

/**
 * A test that shows how a module can access an OSGi service.
 *
 * @author Thomas.Diesler@jboss.com
 * @since 14-Oct-2010
 */
public class ModuleAccessesBundleServiceTestCase extends AbstractXServiceTestCase
{
   @Test
   public void moduleInvokesBundleService() throws Exception
   {
      // Deploy the module that contains the API
      String apiDeploymentName = getRemoteRuntime().deploy(getAPIModuleArchive());
      assertNotNull("Deployment name not null", apiDeploymentName);
      try
      {
         // Register the API module with the OSGi layer
         ModuleIdentifier apiModuleId = ModuleIdentifier.create("deployment." + apiDeploymentName);
         OSGiBundle apiBundle = getRemoteRuntime().getBundle(registerModuleWithBundleManager(apiModuleId));
         try
         {
            // Install the bundle that contains the target service
            OSGiBundle targetBundle = getRemoteRuntime().installBundle(getTargetBundleArchive());
            assertBundleState(Bundle.INSTALLED, targetBundle.getState());
            try
            {
               // Start the target service bundle
               targetBundle.start();
               assertBundleState(Bundle.ACTIVE, targetBundle.getState());

               // Deploy the non-osgi client module
               String clientDeploymentName = getRemoteRuntime().deploy(getClientModuleArchive());
               assertNotNull("Deployment name not null", clientDeploymentName);
               try
               {
                  // Wait for the client service to come up. Check the console log for echo message
                  State state = getServiceState(EchoInvokerService.SERVICE_NAME, State.UP, 5000);
                  assertEquals("EchoInvokerService is UP", State.UP, state);
               }
               finally
               {
                  // Undeploy the client module
                  getRemoteRuntime().undeploy(clientDeploymentName);
               }
            }
            finally
            {
               // Uninstall the target bundle
               targetBundle.uninstall();
            }
         }
         finally
         {
            // Uninstall the API bundle
            apiBundle.uninstall();
         }
      }
      finally
      {
         // Undeploy the API module
         getRemoteRuntime().undeploy(apiDeploymentName);
      }
   }

   private JavaArchive getAPIModuleArchive() throws Exception
   {
      final JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "example-xservice-api-module");
      archive.addClasses(Echo.class);
      archive.addDirectory("META-INF");
      return archive;
   }

   private JavaArchive getTargetBundleArchive() throws Exception
   {
      final JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "example-xservice-target-bundle");
      archive.addClass(TargetBundleActivator.class);
      archive.setManifest(new Asset()
      {
         public InputStream openStream()
         {
            OSGiManifestBuilder builder = OSGiManifestBuilder.newInstance();
            builder.addBundleManifestVersion(2);
            builder.addBundleSymbolicName(archive.getName());
            builder.addBundleVersion("1.0.0");
            builder.addBundleActivator(TargetBundleActivator.class);
            builder.addImportPackages(Echo.class);
            return builder.openStream();
         }
      });
      return archive;
   }

   private JavaArchive getClientModuleArchive() throws Exception
   {
      final JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "example-xservice-client-module");
      archive.addClasses(EchoInvokerService.class, ClientModuleActivator.class);
      String activatorPath = "META-INF/services/" + ServiceActivator.class.getName();
      archive.addResource(getResourceFile("xservice/client-module/" + activatorPath), activatorPath);
      archive.setManifest(getResourceFile("xservice/client-module/" + JarFile.MANIFEST_NAME));
      return archive;
   }
}
