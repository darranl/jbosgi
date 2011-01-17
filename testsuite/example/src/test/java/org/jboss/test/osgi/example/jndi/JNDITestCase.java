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
package org.jboss.test.osgi.example.jndi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.InputStream;

import javax.inject.Inject;
import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;

import org.jboss.arquillian.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.osgi.testing.OSGiManifestBuilder;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.test.osgi.example.jndi.bundle.JNDIExampleActivator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

/**
 * A test that deployes a bundle that binds a String to JNDI
 *
 * @author thomas.diesler@jboss.com
 * @since 05-May-2009
 */
@RunWith(Arquillian.class)
public class JNDITestCase {
    @Inject
    public Bundle bundle;

    @Deployment
    public static JavaArchive createdeployment() {
        final JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "example-jndi");
        archive.addClasses(JNDIExampleActivator.class);
        archive.setManifest(new Asset() {
            public InputStream openStream() {
                OSGiManifestBuilder builder = OSGiManifestBuilder.newInstance();
                builder.addBundleSymbolicName(archive.getName());
                builder.addBundleManifestVersion(2);
                builder.addBundleActivator(JNDIExampleActivator.class.getName());
                return builder.openStream();
            }
        });
        return archive;
    }

    @Test
    public void testJNDIAccess() throws Exception {
        bundle.start();
        assertEquals("Bundle ACTIVE", Bundle.ACTIVE, bundle.getState());

        InitialContext iniCtx = getInitialContext(bundle.getBundleContext());
        String lookup = (String) iniCtx.lookup("test/Foo");
        assertEquals("JNDI bound String expected", "Bar", lookup);

        // Uninstall should unbind the object
        bundle.uninstall();

        // Wait a little for dependent services to come down
        long timeout = 10000;
        boolean unbound = false;
        while (timeout > 0 && unbound == false) {
            Thread.sleep(200);
            timeout -= 200;
            try {
                iniCtx.lookup("test/Foo");
            } catch (NameNotFoundException ex) {
                unbound = true;
            }
        }
        if (unbound == false)
            fail("NameNotFoundException expected");
    }

    private InitialContext getInitialContext(BundleContext context) {
        ServiceReference sref = context.getServiceReference(InitialContext.class.getName());
        if (sref == null)
            throw new IllegalStateException("Cannot access the InitialContext");

        InitialContext initContext = (InitialContext) context.getService(sref);
        return initContext;
    }
}