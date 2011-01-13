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
package org.jboss.osgi.test.performance.bundle;

import org.jboss.osgi.test.common.CommonClass;
import org.jboss.osgi.test.versioned.VersionedInterface;
import org.jboss.osgi.test.versioned.impl.VersionedClass;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * @author <a href="david@redhat.com">David Bosschaert</a>
 */
public class BundlePerfTestActivator implements BundleActivator {
    @Override
    public void start(BundleContext context) throws Exception {
        String common = CommonClass.getStaticValue();
        VersionedInterface vi = VersionedClass.create();
        String versioned = vi.getValue();
        String utilVal = vi.getUtilValue();
        // System.out.println("*** Common: " + common + " Versioned: " + versioned + " Util: " + utilVal);

        if (!common.equals(versioned))
            throw new IllegalStateException("Expected the same version of the common and versioned classes, found. Found: common " + common + ", versioned: "
                    + versioned);

        if (!utilVal.equals(versioned))
            throw new IllegalStateException("Expected the same number for version of the versioned and util class, found. Found: util " + utilVal + ", versioned: "
                    + versioned);

        String bsn = context.getBundle().getSymbolicName();
        String threadName = getThreadName(bsn);
        synchronized (threadName.intern()) // VM-Global lock, outside of measuring section
        {
            String propName = threadName + "started-bundles";
            int num = Integer.parseInt(System.getProperty(propName, "0"));
            System.setProperty(propName, "" + (num + 1));
        }
    }

    @Override
    public void stop(BundleContext context) throws Exception {
    }

    private static String getThreadName(String bsn) {
        int idx1 = bsn.indexOf('-');
        int idx2 = bsn.lastIndexOf('-');

        return bsn.substring(idx1 + 1, idx2);
    }
}
