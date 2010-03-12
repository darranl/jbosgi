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
package org.jboss.test.osgi.jbossas.jbosgi58;

//$Id$

import static org.junit.Assert.assertEquals;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jboss.osgi.testing.OSGiRuntimeTest;
import org.jboss.osgi.testing.internal.RemoteRuntime;
import org.jboss.test.osgi.jbossas.jbosgi58.ejb.StatelessBean;
import org.junit.Ignore;
import org.junit.Test;

/**
 * [JBOSGI-58] Framework injection in SLSB
 * 
 * https://jira.jboss.org/jira/browse/JBOSGI-58
 * 
 * @author thomas.diesler@jboss.com
 * @since 07-Dec-2009
 */
@Ignore
public class OSGi58TestCase extends OSGiRuntimeTest
{
   @Test
   public void testEJB() throws Exception
   {
      RemoteRuntime runtime = (RemoteRuntime)getRemoteRuntime();
      
      String location = getTestArchivePath("jbosgi58-ejb.jar");
      
      runtime.deploy(location);
      try
      {
         StatelessBean bean = getRemoteBean(StatelessBean.class);
         String symbolicName = bean.getFrameworkSymbolicName();
         assertEquals("foo", symbolicName);
      }
      finally
      {
         runtime.undeploy(location);
      }
   }

   @SuppressWarnings("unchecked")
   private <T> T getRemoteBean(Class<T> beanClass) throws NamingException
   {
      InitialContext initContext = getInitialContext();
      T bean = (T)initContext.lookup(beanClass.getSimpleName() + "/remote");
      return bean;
   }
}