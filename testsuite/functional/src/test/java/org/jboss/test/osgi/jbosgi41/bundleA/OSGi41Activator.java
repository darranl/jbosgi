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
package org.jboss.test.osgi.jbosgi41.bundleA;

import java.util.Dictionary;
import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class OSGi41Activator implements BundleActivator {
    public void start(BundleContext context) {
        ServiceA serviceA = new ServiceA(context);
        context.registerService(ServiceA.class.getName(), serviceA, null);

        Dictionary<String, String> props = new Hashtable<String, String>();
        props.put("service.pid", ServiceB.class.getName());

        ServiceB serviceB = new ServiceB(context);
        context.registerService(ServiceB.class.getName(), serviceB, props);

        serviceB.updateConfig("xxx", "yyy");
        serviceB.updateConfig("xxx", "zzz");
    }

    public void stop(BundleContext context) {
    }
}