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
package org.jboss.test.osgi.example.webapp;

// $Id$

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import org.jboss.osgi.testing.OSGiTest;

/**
 * Abstract base class for webapp example. 
 * 
 * @author thomas.diesler@jboss.com
 * @since 06-Oct-2009
 */
public abstract class AbstractWebAppTestCase extends OSGiTest
{
   protected String getHttpResponse(String reqPath) throws Exception
   {
      return getHttpResponse(reqPath, 0);
   }
   
   protected String getHttpResponse(String reqPath, int timeout) throws Exception
   {
      int fraction = 200;
      
      String line = null;
      IOException lastException = null;
      while (line == null && 0 < (timeout -= fraction))
      {
         try
         {
            URL url = new URL("http://" + getServerHost() + ":8090/example-webapp" + reqPath);
            BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
            line = br.readLine();
            br.close();
         }
         catch (IOException ex)
         {
            lastException = ex;
            Thread.sleep(fraction);
         }
      }

      if (line == null && lastException != null)
         throw lastException;

      return line;
   }
}