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
package org.jboss.osgi.test.versioned.impl;

import org.jboss.osgi.test.versioned.VersionedInterface;

/**
 * @author <a href="david@redhat.com">David Bosschaert</a>
 */
public class VersionedClass implements VersionedInterface {
    private static final String staticValue;
    static {
        synchronized (VersionedClass.class.getName().intern()) {
            int counter = Integer.parseInt(System.getProperty(VersionedClass.class.getName(), "0"));
            counter++;
            System.setProperty(VersionedClass.class.getName(), "" + counter);
            staticValue = "" + counter;
        }
    }

    @Override
    public String getValue() {
        return staticValue;
    }

    @Override
    public String getUtilValue() throws Exception {
        Class<?> cls = getClass().getClassLoader().loadClass("org.jboss.osgi.test.util" + getValue() + ".Util" + getValue());
        String name = cls.getName();
        return name.substring(name.length() - 1, name.length());
    }

    public static VersionedInterface create() {
        return new VersionedClass();
    }
}
