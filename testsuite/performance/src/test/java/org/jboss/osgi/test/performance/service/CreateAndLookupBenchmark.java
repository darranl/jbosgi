package org.jboss.osgi.test.performance.service;

import java.util.Arrays;
import java.util.Date;
import java.util.Dictionary;
import java.util.Hashtable;

import org.jboss.osgi.test.performance.AbstractThreadedBenchmark;
import org.jboss.osgi.test.performance.ChartType;
import org.jboss.osgi.test.performance.ChartTypeImpl;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;

public class CreateAndLookupBenchmark extends AbstractThreadedBenchmark<Integer>
{
   private static final ChartType REGISTRATION = new ChartTypeImpl("REG", "Service Registration Time", "Number", "Time");
   private static final ChartType LOOKUP = new ChartTypeImpl("LKU", "Service Lookup Time", "Number", "Time");
   
   public CreateAndLookupBenchmark(BundleContext context)
   {
      super(context);
   }

   @Override
   protected ChartType[] getAllChartTypes()
   {
      return new ChartType[] { REGISTRATION, LOOKUP };
   }

   public void run(int numthreads, int numservices) throws Exception
   {
      runTest(numthreads, numservices);
   }

   @Override
   protected void runThread(String threadName, Integer numServicesPerThread) throws Exception
   {
      Dictionary<String, Object> props = new Hashtable<String, Object>();
      ServiceRegistration[] regs = new ServiceRegistration[numServicesPerThread];

      System.out.println("Starting at " + new Date());
      long regStart = System.currentTimeMillis();
      // Service Registrations
      for (int i = 0; i < numServicesPerThread; i++)
      {
         String svc = threadName + i;
         props.put(threadName, i);
         regs[i] = bundleContext.registerService(String.class.getName(), svc, props);
      }
      long regEnd = System.currentTimeMillis();
      writeData(REGISTRATION, numServicesPerThread, regEnd - regStart);
      System.out.println("Registered Services " + new Date());

      try
      {
         long lkuStart = System.currentTimeMillis();
         // Lookup & Invoke Services
         for (int i = 0; i < numServicesPerThread; i++)
         {
            ServiceReference[] srs = bundleContext.getServiceReferences(String.class.getName(), "(" + threadName + "=" + i + ")");
            if (srs.length != 1)
               throw new IllegalStateException("Should only have found 1 service: " + Arrays.toString(srs));

            String s = (String)bundleContext.getService(srs[0]);
            if (!s.toString().equals(threadName + i))
               throw new IllegalStateException("Wrong service used, expected: " + i + " but got " + s);
         }
         long lkuEnd = System.currentTimeMillis();
         writeData(LOOKUP, numServicesPerThread, lkuEnd - lkuStart);
         System.out.println("Invoked Services " + new Date());
      }
      finally
      {
         // unregister services
         for (int i = 0; i < numServicesPerThread; i++)
         {
            regs[i].unregister();
         }
         System.out.println("Unregistered Services" + new Date());
      }
   }
}
