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
package org.jboss.test.osgi.example.microcontainer.bundleA;

//$Id$

import javax.management.MBeanServer;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

/**
 * An MC bean that accesses some OSGi service
 * 
 * @author thomas.diesler@jboss.com
 * @since 24-Apr-2009
 */
public class SomeBean implements SomeBeanMBean
{
   private BundleContext bundleContext;
   private MBeanServer mbeanServer;

   public void setMbeanServer(MBeanServer server)
   {
      this.mbeanServer = server;
   }

   public void setBundleContext(BundleContext systemContext)
   {
      this.bundleContext = systemContext;
   }

   public String echo(String msg)
   {
      return msg;
   }
   
   public String callSomeService(String msg)
   {
      ServiceReference sref = bundleContext.getServiceReference(SomeService.class.getName());
      SomeService service = (SomeService)bundleContext.getService(sref);
      return service.callSomeBean(msg);
   }

   public void create() throws Exception
   {
      mbeanServer.registerMBean(this, MBEAN_NAME);
   }
   
   public void destroy() throws Exception
   {
      mbeanServer.unregisterMBean(MBEAN_NAME);
   }
}
