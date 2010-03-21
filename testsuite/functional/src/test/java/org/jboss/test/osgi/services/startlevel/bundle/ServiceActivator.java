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
package org.jboss.test.osgi.services.startlevel.bundle;

//$Id: ServiceActivator.java 87336 2009-04-15 11:31:26Z thomas.diesler@jboss.com $

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.service.startlevel.StartLevel;
import org.osgi.util.tracker.ServiceTracker;

/**
 * Try to access the StartLevel service 
 * 
 * @author thomas.diesler@jboss.com
 * @since 04-Feb-2009
 */
public class ServiceActivator implements BundleActivator
{
   public void start(BundleContext context)
   {
      ServiceTracker tracker = new ServiceTracker(context, StartLevel.class.getName(), null);
      tracker.open();
      
      StartLevel service = (StartLevel)tracker.getService();
      if (service == null)
         throw new IllegalStateException("Cannot get StartLevel. Loaded with: " + StartLevel.class.getClassLoader());
   }

   /*
    * Implements BundleActivator.stop().
    */
   public void stop(BundleContext context)
   {
   }
}