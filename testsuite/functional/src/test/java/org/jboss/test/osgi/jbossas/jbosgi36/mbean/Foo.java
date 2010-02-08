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
package org.jboss.test.osgi.jbossas.jbosgi36.mbean;

import org.jboss.aop.microcontainer.aspects.jmx.JMX;

//$Id: Foo.java 86968 2009-04-08 15:51:12Z thomas.diesler@jboss.com $

@JMX (exposedInterface = FooMBean.class, name = "jboss.osgi:test=jbosgi36", registerDirectly = true)
public class Foo implements FooMBean
{
   public String echo(String msg)
   {
      return msg;
   }

   public String accessSomeService() throws ClassNotFoundException
   {
      ClassLoader loader = getClass().getClassLoader();
      loader.loadClass("org.jboss.test.osgi.deployer.jbosgi36.bundle.SomeService");
      return loader.toString();
   }

   public String accessSomeInternal() throws ClassNotFoundException
   {
      ClassLoader loader = getClass().getClassLoader();
      loader.loadClass("org.jboss.test.osgi.deployer.jbosgi36.bundle.internal.SomeInternal");
      return loader.toString();
   }
}
