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
package org.jboss.osgi.test.performance.bundle.arq;

import static org.jboss.osgi.test.performance.bundle.BundleInstallAndStartBenchmark.COMMON_BUNDLE_PREFIX;
import static org.jboss.osgi.test.performance.bundle.BundleInstallAndStartBenchmark.PREFIX_SEPARATOR;
import static org.jboss.osgi.test.performance.bundle.BundleInstallAndStartBenchmark.TEST_BUNDLE_PREFIX;
import static org.jboss.osgi.test.performance.bundle.BundleInstallAndStartBenchmark.UTIL_BUNDLE_PREFIX;
import static org.jboss.osgi.test.performance.bundle.BundleInstallAndStartBenchmark.VERSIONED_IMPL_BUNDLE_PREFIX;
import static org.jboss.osgi.test.performance.bundle.BundleInstallAndStartBenchmark.VERSIONED_INTF_BUNDLE_PREFIX;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import org.jboss.arquillian.container.test.api.Deployer;
import org.jboss.osgi.test.performance.bundle.BundleInstallAndStartBenchmark;
import org.jboss.osgi.test.performance.bundle.TestBundleProvider;
import org.osgi.framework.BundleContext;

/**
 * This {@link TestBundleProvider} provides the test bundles via the (remote) OSGiContainer.
 *
 * @author <a href="david@redhat.com">David Bosschaert</a>
 */
public class TestBundleProviderImpl implements TestBundleProvider {
    private final Deployer deployer;
    private final File tempDir;

    public TestBundleProviderImpl(Deployer deployer, BundleContext bc) {
        this.deployer = deployer;
        this.tempDir = new File(bc.getDataFile(""), getClass().getSimpleName() + "-" + System.currentTimeMillis());
    }

    @Override
    public InputStream getTestArchiveStream(String name) throws IOException {
        int idx = name.indexOf(PREFIX_SEPARATOR);
        if (idx < 0)
            throw new IllegalStateException("Archive name is missing required separator");

        String prefix = name.substring(0, idx + 1);
        String suffix = name.substring(idx + 1).trim();
        if (COMMON_BUNDLE_PREFIX.equals(prefix)) {
            return getVersionBasedBundle(prefix, suffix);
        } else if (TEST_BUNDLE_PREFIX.equals(prefix)) {
            return getTestBundle(prefix, suffix);
        } else if (UTIL_BUNDLE_PREFIX.equals(prefix)) {
            // There are only 5 of these which are hard coded in the test client.
            return deployer.getDeployment(name);
        } else if (VERSIONED_INTF_BUNDLE_PREFIX.equals(prefix)) {
            return getVersionBasedBundle(prefix, suffix);
        } else if (VERSIONED_IMPL_BUNDLE_PREFIX.equals(prefix)) {
            return getVersionedImplBundle(prefix, suffix);
        }
        throw new IllegalStateException("Unexpected archive request: " + name);
    }

    private InputStream getVersionBasedBundle(String prefix, String version) throws IOException, FileNotFoundException {
        File exploded = getRawBundleDir(prefix);

        Manifest mf = new Manifest(new FileInputStream(new File(exploded, "META-INF/MANIFEST.MF")));
        Attributes attrs = mf.getMainAttributes();
        for(Map.Entry<Object, Object> entry : new HashSet<Map.Entry<Object, Object>>(attrs.entrySet())) {
            String newVal = entry.getValue().toString().replaceAll(BundleTestBase.VERSION_UNDEFINED, version);
            attrs.put(entry.getKey(), newVal);
        }
        return jar(exploded, mf);
    }

    private InputStream getTestBundle(String prefix, String suffix) throws IOException, FileNotFoundException {
        String[] parts = suffix.split("#");
        if (parts.length != 2)
            throw new IllegalStateException("Incorrect request: " + prefix + suffix);

        String threadName = parts[0];
        String counter = parts[1];
        String version = "" + ((Integer.parseInt(counter) % 5) + 1);

        File exploded = getRawBundleDir(prefix);
        Manifest mf = new Manifest(new FileInputStream(new File(exploded, "META-INF/MANIFEST.MF")));
        Attributes attrs = mf.getMainAttributes();
        for(Map.Entry<Object, Object> entry : new HashSet<Map.Entry<Object, Object>>(attrs.entrySet())) {
            String newVal = entry.getValue().toString().replaceAll(BundleTestBase.THREAD_NAME_UNDEFINED, threadName);
            String newerVal = newVal.replaceAll(BundleTestBase.COUNTER_UNDEFINED, counter);
            String newestVal = newerVal.replaceAll(BundleTestBase.VERSION_UNDEFINED, version);
            attrs.put(entry.getKey(), newestVal);
        }
        return jar(exploded, mf);
    }

    private InputStream getVersionedImplBundle(String prefix, String suffix) throws IOException, FileNotFoundException {
        int version = Integer.parseInt(suffix);
        File exploded = getRawBundleDir(prefix + "Util" + (((version - 1) % 5) + 1));

        Manifest mf = new Manifest(new FileInputStream(new File(exploded, "META-INF/MANIFEST.MF")));
        Attributes attrs = mf.getMainAttributes();
        for(Map.Entry<Object, Object> entry : new HashSet<Map.Entry<Object, Object>>(attrs.entrySet())) {
            String newVal = entry.getValue().toString().replaceAll(BundleTestBase.VERSION_UNDEFINED, "" + version);
            attrs.put(entry.getKey(), newVal);
        }
        return jar(exploded, mf);
    }

    // Returns the directory containing the content of the 'raw' archive which is the archive
    // that is provided by the deployer before it was modified.
    private File getRawBundleDir(String prefix) throws IOException {
        File exploded = new File(tempDir, prefix);
        if (!exploded.isDirectory()) {
            unJar(deployer.getDeployment(prefix), exploded);
        }
        return exploded;
    }

    static Manifest unJar(InputStream jarStream, File tempDir) throws IOException {
        ensureDirectory(tempDir);

        JarInputStream jis = new JarInputStream(jarStream);
        JarEntry je = null;
        while((je = jis.getNextJarEntry()) != null) {
            if (je.isDirectory()) {
                File outDir = new File(tempDir, je.getName());
                ensureDirectory(outDir);

                continue;
            }

            File outFile = new File(tempDir, je.getName());
            File outDir = outFile.getParentFile();
            ensureDirectory(outDir);

            OutputStream out = new FileOutputStream(outFile);
            try {
                BundleInstallAndStartBenchmark.pumpStreams(jis, out);
            } finally {
                out.flush();
                out.close();
                jis.closeEntry();
            }
            outFile.setLastModified(je.getTime());
        }

        Manifest manifest = jis.getManifest();
        if (manifest != null) {
            File mf = new File(tempDir, "META-INF/MANIFEST.MF");
            File mfDir = mf.getParentFile();
            ensureDirectory(mfDir);

            OutputStream out = new FileOutputStream(mf);
            try {
                manifest.write(out);
            } finally {
                out.flush();
                out.close();
            }
        }

        jis.close();
        return manifest;
    }

    static InputStream jar(File rootFile, Manifest manifest) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        JarOutputStream jos = new JarOutputStream(baos, manifest);
        try {
            addToJarRecursively(jos, rootFile.getAbsoluteFile(), rootFile.getAbsolutePath());
        } finally {
            jos.close();
        }
        return new ByteArrayInputStream(baos.toByteArray());
    }

    static void addToJarRecursively(JarOutputStream jar, File source, String rootDirectory) throws IOException {
        String sourceName = source.getAbsolutePath().replace("\\", "/");
        sourceName = sourceName.substring(rootDirectory.length());

        if (sourceName.startsWith("/")) {
            sourceName = sourceName.substring(1);
        }

        if ("META-INF/MANIFEST.MF".equals(sourceName))
            return;

        if (source.isDirectory()) {
            for (File nested : source.listFiles()) {
                addToJarRecursively(jar, nested, rootDirectory);
            }
            return;
        }

        JarEntry entry = new JarEntry(sourceName);
        jar.putNextEntry(entry);
        InputStream is = new FileInputStream(source);
        try {
            BundleInstallAndStartBenchmark.pumpStreams(is, jar);
        } finally {
            jar.closeEntry();
            is.close();
        }
    }

    private static void ensureDirectory(File outDir) throws IOException {
        if (!outDir.isDirectory())
            if (!outDir.mkdirs())
                throw new IOException("Unable to create directory " + outDir.getAbsolutePath());
    }
}
