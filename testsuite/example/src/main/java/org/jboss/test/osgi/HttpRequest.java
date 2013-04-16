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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.concurrent.TimeUnit;


/**
 * HTTP integration test support.
 *
 * @author thomas.diesler@jboss.com
 * @since 26-Jan-2012
 */
public final class HttpRequest {
    
    /**
     * Execute a HTTP request.
     */
    public static String get(String urlspec) throws IOException {
        URL requrl = new URL(urlspec);
        BufferedReader br = new BufferedReader(new InputStreamReader(requrl.openStream()));
        String line = br.readLine();
        br.close();
        return line;
    }

    /**
     * Execute a HTTP request with a given timeout in milliseconds.
     */
    public static String get(String urlspec, long timeout, TimeUnit unit) throws IOException {
        String line = null;
        IOException lastException = null;
        long fraction = 200;
        long timeoutMillis = unit.toMillis(timeout);
        while (line == null && 0 < (timeoutMillis -= fraction)) {
            try {
                line = get(urlspec);
            } catch (IOException ex) {
                lastException = ex;
                try {
                    Thread.sleep(fraction);
                } catch (InterruptedException ie) {
                    // ignore
                }
            }
        }
        if (line == null && lastException != null) {
            throw lastException;
        }
        return line;
    }
}