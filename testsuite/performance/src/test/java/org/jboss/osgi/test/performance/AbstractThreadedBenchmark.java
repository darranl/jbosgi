package org.jboss.osgi.test.performance;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.osgi.framework.BundleContext;

public abstract class AbstractThreadedBenchmark<T> extends AbstractBenchmark implements PerformanceTest
{
   protected AbstractThreadedBenchmark(BundleContext bc)
   {
      super(bc);
   }

   public void runTest(int numThreads, final T parameters) throws Exception
   {
      final List<Throwable> exceptions = Collections.synchronizedList(new ArrayList<Throwable>());
      List<Thread> threads = new ArrayList<Thread>(numThreads);
      for (int i = 0; i < numThreads; i++)
      {
         final String threadName = "Thread " + i;

         threads.add(new Thread(new Runnable()
         {
            @Override
            public void run()
            {
               try
               {
                  runThread(threadName, parameters);
               }
               catch (Throwable e)
               {
                  exceptions.add(e);
               }
            }
         }));
      }

      System.out.println("Starting " + numThreads + " threads");
      for (Thread t : threads)
         t.start();

      for (Thread t : threads)
         t.join();
      System.out.println("All threads finished");

      if (exceptions.size() > 0)
         throw new Exception("One or more performance test threads failed: " + exceptions);
   }
   
   abstract protected void runThread(String threadName, T parameters) throws Exception;
}
