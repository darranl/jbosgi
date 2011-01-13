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
package org.jboss.test.osgi.example.serviceloader.client;

import org.jboss.osgi.spi.util.ServiceLoader;
import org.jboss.test.osgi.example.serviceloader.service.AccountService;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

/**
 * A Service Activator
 * 
 * @author thomas.diesler@jboss.com
 * @since 26-Jan-2010
 */
public class ServiceLoaderClientActivator implements BundleActivator {
    private AccountService service;

    // Note, the example-serviceloader-impl does NOT register the service itself.
    // Instead, jboss-osgi-serviceloader generically registeres all services in META-INF/services
    public void start(BundleContext context) {
        // Use the traditional ServiceLoader API to get the service
        service = ServiceLoader.loadService(AccountService.class);
        if (service != null)
            throw new IllegalStateException("Traditional ServiceLoader API, expected to fail");

        checkStaticServiceAccess(context);

        checkDynamicServiceAccess(context);
    }

    public void stop(BundleContext context) {
        if (service == null)
            throw new IllegalStateException("ServiceTracker could not obtain the service");
    }

    private void checkStaticServiceAccess(BundleContext context) {
        ServiceReference[] srefs = null;
        try {
            String filter = "(service.vendor=JBoss*)";
            srefs = context.getServiceReferences(AccountService.class.getName(), filter);
        } catch (InvalidSyntaxException ex) {
            // ignore
        }
        if (srefs == null || srefs.length != 1)
            throw new IllegalStateException("Cannot obtain service");
    }

    private void checkDynamicServiceAccess(BundleContext context) {
        Filter filter = null;
        try {
            String filterstr = "(&(" + Constants.OBJECTCLASS + "=" + AccountService.class.getName() + ")(service.vendor=JBoss*))";
            filter = FrameworkUtil.createFilter(filterstr);
        } catch (InvalidSyntaxException ex) {
            throw new IllegalStateException(ex);
        }

        // Track the service
        ServiceTracker tracker = new ServiceTracker(context, filter, null) {
            public Object addingService(ServiceReference reference) {
                service = (AccountService) super.addingService(reference);
                return service;
            }

            @Override
            public void removedService(ServiceReference reference, Object tracked) {
                super.removedService(reference, tracked);
                service = null;
            }
        };
        tracker.open();
    }

}