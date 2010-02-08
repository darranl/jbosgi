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
package org.jboss.test.osgi.trailblazer.pay.paypal;

//$Id$

import static org.jboss.test.osgi.trailblazer.PaymentService.SERVICE_PROPERTY_TYPE;

import java.util.Hashtable;

import org.jboss.osgi.common.log.LogServiceTracker;
import org.jboss.test.osgi.trailblazer.PaymentService;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.log.LogService;

/**
 * The activator for the {@link PayPalService}
 * 
 * @author thomas.diesler@jboss.com
 * @since 10-May-2009
 */
public class PayPalServiceActivator implements BundleActivator
{
   private LogService log;
   private ServiceRegistration serviceReg;
   
   @SuppressWarnings("unchecked")
   public void start(BundleContext context)
   {
      this.log = new LogServiceTracker(context);
      
      Hashtable props = new Hashtable();
      props.put(SERVICE_PROPERTY_TYPE, "paypal");
      
      serviceReg = context.registerService(PaymentService.class.getName(), new PayPalService(), props);
      log.log(LogService.LOG_INFO, "Register the PayPalService");
   }

   public void stop(BundleContext context)
   {
      if (serviceReg != null)
      {
         log.log(LogService.LOG_INFO, "Unregister the PayPalService");
         serviceReg.unregister();
         serviceReg = null;
      }
   }
}