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
package org.jboss.test.osgi.jbossas.example.payment;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleReference;

/**
 * A provider for the system bundle context.
 *
 * [TODO] should be provided as injectable resource
 *
 * @author thomas.diesler@jboss.com
 * @since 18-Jul-2011
 */
public abstract class BundleContextProvider {

    public static BundleContext getBundleContext() {
        ClassLoader classLoader = BundleContextProvider.class.getClassLoader();
        Bundle bundle = ((BundleReference) classLoader).getBundle();
        BundleContext context = bundle.getBundleContext();
        return context.getBundle(0).getBundleContext();
    };
}
