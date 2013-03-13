/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012, JBoss Inc., and individual contributors as indicated
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
import org.osgi.framework.ServiceReference;
import org.osgi.service.jndi.JNDIContextManager;

/**
 * Provide the org.apache.aries.jndi bundle
 *
 * @author David Bosschaert
 * @author Thomas.Diesler@jboss.com
 */
public class NamingSupport extends RepositorySupport {

    public static final String CONTEXT_MANAGER_SERVICE = "org.osgi.service.jndi.JNDIContextManager";
    public static final String APACHE_ARIES_JNDI = "org.apache.aries.jndi:org.apache.aries.jndi";

    public static void provideJNDIIntegration(BundleContext syscontext, Bundle bundle) throws BundleException {
        ServiceReference<?> sref = syscontext.getServiceReference(CONTEXT_MANAGER_SERVICE);
        if (sref == null) {
            AriesSupport.provideAriesUtil(syscontext, bundle);
            AriesSupport.provideAriesProxy(syscontext, bundle);
            // Version 1.0.0 depends on Blueprint
            BlueprintSupport.provideBlueprint(syscontext, bundle);
            installSupportBundle(syscontext, getCoordinates(bundle, APACHE_ARIES_JNDI)).start();
        }
   }

    public static JNDIContextManager getContextManager(Bundle bundle) {
        BundleContext context = bundle.getBundleContext();
        ServiceReference<JNDIContextManager> sref = context.getServiceReference(JNDIContextManager.class);
        return context.getService(sref);
    }
}
