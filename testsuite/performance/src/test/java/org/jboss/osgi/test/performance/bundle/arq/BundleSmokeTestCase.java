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

import javax.inject.Inject;

import org.jboss.arquillian.api.DeploymentProvider;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.osgi.test.performance.bundle.BundleInstallAndStartBenchmark;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osgi.framework.BundleContext;

/**
 * This test exercises the Bundle Performance test code in a very basic manner to ensure that it works. Passing this test is a
 * precondition for running the real Bundle Performance tests.
 * 
 * @author <a href="david@redhat.com">David Bosschaert</a>
 */
@RunWith(Arquillian.class)
public class BundleSmokeTestCase extends BundleTestBase {
    
    @Inject
    public DeploymentProvider provider;

    @Inject
    public BundleContext bundleContext;

    @Override
    DeploymentProvider getDeploymentProvider() {
        return provider;
    }

    BundleContext getBundleContext() {
        return bundleContext;
    }

    @Test
    public void test5() throws Exception {
        BundleInstallAndStartBenchmark bm = new BundleInstallAndStartBenchmark(new TestBundleProviderImpl(getDeploymentProvider()), getBundleContext());
        bm.prepareTest(1, 5);
        bm.runThread("Thread_1", 5);
        bm.cleanUp();
    }
}
