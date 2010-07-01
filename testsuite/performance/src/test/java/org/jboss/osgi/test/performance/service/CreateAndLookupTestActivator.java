package org.jboss.osgi.test.performance.service;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

public class CreateAndLookupTestActivator implements BundleActivator
{
   private ServiceRegistration reg;

   @Override
   public void start(BundleContext context) throws Exception
   {
      reg = context.registerService(CreateAndLookupBenchmark.class.getName(), new CreateAndLookupBenchmark(context), null);
   }

   @Override
   public void stop(BundleContext context) throws Exception
   {
      reg.unregister();
   }
}
