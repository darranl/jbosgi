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
package org.jboss.test.osgi.trailblazer.frontend;

//$Id$

import org.jboss.osgi.common.log.LogServiceTracker;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpService;
import org.osgi.service.log.LogService;
import org.osgi.util.tracker.ServiceTracker;

/**
 * A bundle activator that uses the {@link ServiceTracker} 
 * to registers the {@link HttpRenderServlet} with the {@link HttpService}
 * 
 * @author thomas.diesler@jboss.com
 * @since 10-May-2009
 */
public class ServiceActivator implements BundleActivator
{
   private static final String CONTEXT_PATH = "/osgishop";

   private HttpService httpService;
   private LogService log;

   public void start(BundleContext context)
   {
      this.log = new LogServiceTracker(context);

      log.log(LogService.LOG_INFO, "Start HttpService tracking");
      ServiceTracker tracker = new ServiceTracker(context, HttpService.class.getName(), null)
      {
         @Override
         public Object addingService(ServiceReference reference)
         {
            httpService = (HttpService)super.addingService(reference);
            try
            {
               log.log(LogService.LOG_INFO, "Register " + CONTEXT_PATH);
               HttpRenderServlet servlet = new HttpRenderServlet(context);
               httpService.registerServlet(CONTEXT_PATH, servlet, null, null);
               httpService.registerResources(CONTEXT_PATH + "/style", "/style", null);
               httpService.registerResources(CONTEXT_PATH + "/notes", "/notes", null);
            }
            catch (Exception ex)
            {
               throw new IllegalStateException("Cannot register: " + CONTEXT_PATH, ex);
            }
            return httpService;
         }

         @Override
         public void removedService(ServiceReference reference, Object service)
         {
            super.removedService(reference, service);

            log.log(LogService.LOG_INFO, "Unregister " + CONTEXT_PATH);
            httpService.unregister(CONTEXT_PATH);
            httpService = null;
         }
      };
      tracker.open();
   }

   public void stop(BundleContext context)
   {
      if (httpService != null)
      {
         log.log(LogService.LOG_INFO, "Unregister " + CONTEXT_PATH);
         httpService.unregister(CONTEXT_PATH);
         httpService = null;
      }
   }
}