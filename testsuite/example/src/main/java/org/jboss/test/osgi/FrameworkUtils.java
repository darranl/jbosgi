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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.jboss.osgi.resolver.XBundle;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.FrameworkListener;
import org.osgi.framework.VersionRange;
import org.osgi.framework.startlevel.FrameworkStartLevel;


/**
 * OSGi integration test support.
 *
 * @author thomas.diesler@jboss.com
 * @since 24-May-2011
 */
public final class FrameworkUtils {

    // Hide ctor
    private FrameworkUtils() {
    }

    /**
     * Get the current framework start level.
     */
    public static int getFrameworkStartLevel(BundleContext context)  {
        FrameworkStartLevel fwkStartLevel = context.getBundle().adapt(FrameworkStartLevel.class);
        return fwkStartLevel.getStartLevel();
    }

    /**
     * Changes the framework start level and waits for the STARTLEVEL_CHANGED event
     * Note, changing the framework start level is an asynchronous operation.
     */
    public static void setFrameworkStartLevel(BundleContext context, final int level, long timeout, TimeUnit units) throws InterruptedException, TimeoutException {
        final FrameworkStartLevel fwkStartLevel = context.getBundle().adapt(FrameworkStartLevel.class);
        if (level != fwkStartLevel.getStartLevel()) {
            final CountDownLatch latch = new CountDownLatch(1);
            FrameworkListener listener = new FrameworkListener() {
                @Override
                public void frameworkEvent(FrameworkEvent event) {
                    if (event.getType() == FrameworkEvent.STARTLEVEL_CHANGED && level == fwkStartLevel.getStartLevel()) {
                        latch.countDown();
                    }
                }
            };
            fwkStartLevel.setStartLevel(level, listener);
            if (latch.await(timeout, units) == false)
                throw new TimeoutException("Timeout changing start level");
        }
    }

    public static Set<XBundle> getBundles(BundleContext context, String symbolicName, VersionRange versionRange) {
        Set<XBundle> resultSet = new HashSet<XBundle>();
        if (Constants.SYSTEM_BUNDLE_SYMBOLICNAME.equals(symbolicName) && versionRange == null) {
            resultSet.add((XBundle) context.getBundle(0));
        } else {
            for (Bundle aux : context.getBundles()) {
                if (symbolicName == null || symbolicName.equals(aux.getSymbolicName())) {
                    if (versionRange == null || versionRange.includes(aux.getVersion())) {
                        resultSet.add((XBundle) aux);
                    }
                }
            }
        }
        return Collections.unmodifiableSet(resultSet);
    }
}