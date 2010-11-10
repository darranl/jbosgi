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

package org.jboss.test.osgi.example.xservice.module;

import org.jboss.logging.Logger;
import org.jboss.msc.service.BatchBuilder;
import org.jboss.msc.service.BatchServiceBuilder;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceContainer;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceController.Mode;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceNotFoundException;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.test.osgi.example.xservice.api.Echo;
import org.jboss.test.osgi.example.xservice.api.EchoInvoker;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class EchoInvokerService implements EchoInvoker, Service<EchoInvoker>
{
   private static final Logger log = Logger.getLogger(EchoInvokerService.class);
   public static final ServiceName SERVICE_NAME = ServiceName.JBOSS.append(ServiceName.parse("osgi.xservice.invoker"));

   private BundleContext systemContext;

   public static void addService(BatchBuilder batchBuilder)
   {
      BatchServiceBuilder<EchoInvoker> serviceBuilder = batchBuilder.addService(SERVICE_NAME, new EchoInvokerService());
      serviceBuilder.setInitialMode(Mode.ACTIVE);
      log.infof("Service added: %s", SERVICE_NAME);
      log.infof("Echo Loader: %s", Echo.class.getClassLoader());
   }

   @Override
   public String invoke(String message)
   {
      ServiceReference sref = systemContext.getServiceReference(Echo.class.getName());
      Echo service = (Echo)systemContext.getService(sref);
      return service.echo(message);
   }

   @Override
   public void start(StartContext context) throws StartException
   {
      ServiceName serviceName = ServiceName.JBOSS.append("osgi", "context");
      try
      {
         ServiceContainer serviceContainer = context.getController().getServiceContainer();
         ServiceController<?> controller = serviceContainer.getRequiredService(serviceName);
         systemContext = (BundleContext)controller.getValue();
      }
      catch (ServiceNotFoundException e)
      {
         throw new IllegalStateException("Cannot obtain service: " + serviceName);
      }
   }

   @Override
   public void stop(StopContext context)
   {
   }

   @Override
   public EchoInvoker getValue() throws IllegalStateException
   {
      return this;
   }
}
