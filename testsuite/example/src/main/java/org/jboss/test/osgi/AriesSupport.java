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
package org.jboss.test.osgi;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;

/**
 * @author thomas.diesler@jboss.com
 * @since 26-Jan-2012
 */
public final class AriesSupport extends RepositorySupport {

    public static final String APACHE_ARIES_PROXY = "org.apache.aries.proxy:org.apache.aries.proxy";
    public static final String APACHE_ARIES_UTIL = "org.apache.aries:org.apache.aries.util";

    public static void provideAriesUtil(BundleContext syscontext, Bundle bundle) throws BundleException {
        if (getPackageAdmin(syscontext).getBundles("org.apache.aries.util", null) == null) {
            installSupportBundle(syscontext, getCoordinates(bundle, APACHE_ARIES_UTIL)).start();
        }
    }

    public static void provideAriesProxy(BundleContext syscontext, Bundle bundle) throws BundleException {
        AriesSupport.provideAriesUtil(syscontext, bundle);
        if (getPackageAdmin(syscontext).getBundles("org.apache.aries.proxy", null) == null) {
            installSupportBundle(syscontext, getCoordinates(bundle, APACHE_ARIES_PROXY)).start();
        }
    }
}