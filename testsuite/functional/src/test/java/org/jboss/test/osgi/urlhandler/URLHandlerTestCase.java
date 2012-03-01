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
package org.jboss.test.osgi.urlhandler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Dictionary;
import java.util.Hashtable;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.osgi.spi.OSGiManifestBuilder;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.url.AbstractURLStreamHandlerService;
import org.osgi.service.url.URLConstants;
import org.osgi.service.url.URLStreamHandlerService;

/**
 * @author <a href="david@redhat.com">David Bosschaert</a>
 */
@RunWith(Arquillian.class)
public class URLHandlerTestCase {

    @Inject
    public BundleContext context;

    @Deployment
    public static JavaArchive create() {
        final JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "urlhandler-bundle");
        archive.setManifest(new Asset() {
            public InputStream openStream() {
                OSGiManifestBuilder builder = OSGiManifestBuilder.newInstance();
                builder.addBundleSymbolicName(archive.getName());
                builder.addBundleManifestVersion(2);
                builder.addImportPackages(URLStreamHandlerService.class);
                return builder.openStream();
            }
        });
        return archive;
    }

    @Test
    public void testURLHandlerService() throws Exception {
        try {
            new URL("foobar://hi");
            fail("No handler registered. Should throw a MalformedURLException.");
        } catch (MalformedURLException mue) {
            // good
        }

        // A simple URL handler which returns the URL reversed when opening a
        // stream to it...
        URLStreamHandlerService myHandler = new TestURLHandler();

        Dictionary<String, Object> props = new Hashtable<String, Object>();
        props.put(URLConstants.URL_HANDLER_PROTOCOL, "foobar");
        ServiceRegistration reg = context.registerService(URLStreamHandlerService.class.getName(), myHandler, props);

        // Give the system a chance to pick up the service
        Thread.sleep(500);

        try {
            assertEquals("ih//:raboof", new String(suckStream(new URL("foobar://hi").openStream())));
        } finally {
            reg.unregister();
        }

        // Give the system a chance to unregister the service
        Thread.sleep(500);

        try {
            new URL("foobar://hi");
            fail("No handler registered any more. Should throw a MalformedURLException.");
        } catch (MalformedURLException mue) {
            // good
        }
    }

    private static void pumpStream(InputStream is, OutputStream os) throws IOException {
        byte[] bytes = new byte[8192];

        int length = 0;
        int offset = 0;

        while ((length = is.read(bytes, offset, bytes.length - offset)) != -1) {
            offset += length;

            if (offset == bytes.length) {
                os.write(bytes, 0, bytes.length);
                offset = 0;
            }
        }
        if (offset != 0) {
            os.write(bytes, 0, offset);
        }
    }

    private static byte[] suckStream(InputStream is) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            pumpStream(is, baos);
            return baos.toByteArray();
        } finally {
            is.close();
        }
    }

    private static class TestURLHandler extends AbstractURLStreamHandlerService {
        @Override
        public URLConnection openConnection(URL u) throws IOException {
            return new TestURLConnection(u);
        }
    }

    public static class TestURLConnection extends URLConnection {

        protected TestURLConnection(URL url) {
            super(url);
        }

        @Override
        public void connect() throws IOException {
        }

        @Override
        public InputStream getInputStream() throws IOException {
            InputStream is = new ByteArrayInputStream(new StringBuilder(getURL().toString()).reverse().toString().getBytes());
            return is;
        }
    }
}
