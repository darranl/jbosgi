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
package org.jboss.test.osgi.localization;

import static org.junit.Assert.assertEquals;

import java.util.Dictionary;
import java.util.Locale;

import org.jboss.osgi.testing.OSGiBundle;
import org.jboss.osgi.testing.OSGiRuntimeTest;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;

/**
 * Test the Localization
 * 
 * @author thomas.diesler@jboss.com
 * @since 25-Jan-2010
 */
public class LocalizationRuntimeTestCase extends OSGiRuntimeTest {
    @Test
    public void testHostLocalization() throws Exception {
        OSGiBundle host = getRuntime().installBundle("localization-simple-host.jar");
        assertBundleState(Bundle.INSTALLED, host.getState());

        // Test default locale
        Dictionary<String, String> headers = host.getHeaders();
        String bundleName = headers.get(Constants.BUNDLE_NAME);
        assertEquals("English Bundle Name", bundleName);

        // Test explicit default locale
        headers = host.getHeaders(null);
        bundleName = headers.get(Constants.BUNDLE_NAME);
        assertEquals("English Bundle Name", bundleName);

        // Test raw headers
        headers = host.getHeaders("");
        bundleName = headers.get(Constants.BUNDLE_NAME);
        assertEquals("%bundle-name", bundleName);

        host.uninstall();
        assertBundleState(Bundle.UNINSTALLED, host.getState());

        // Test default locale after uninstall
        headers = host.getHeaders();
        bundleName = headers.get(Constants.BUNDLE_NAME);
        assertEquals("English Bundle Name", bundleName);
    }

    @Test
    public void testFragmentLocalization() throws Exception {
        OSGiBundle host = getRuntime().installBundle("localization-simple-host.jar");
        OSGiBundle frag = getRuntime().installBundle("localization-simple-frag.jar");

        host.start();
        assertBundleState(Bundle.ACTIVE, host.getState());
        assertBundleState(Bundle.RESOLVED, frag.getState());

        // Test explicit locale
        Dictionary<String, String> headers = host.getHeaders(Locale.GERMAN.toString());
        String bundleName = headers.get(Constants.BUNDLE_NAME);
        assertEquals("Deutscher Bundle Name", bundleName);

        host.uninstall();
        assertBundleState(Bundle.UNINSTALLED, host.getState());

        frag.uninstall();
        assertBundleState(Bundle.UNINSTALLED, frag.getState());

        // Test default locale after uninstall
        headers = host.getHeaders();
        bundleName = headers.get(Constants.BUNDLE_NAME);
        assertEquals("English Bundle Name", bundleName);
    }
}