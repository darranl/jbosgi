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
package org.jboss.osgi.test.performance;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.jboss.logging.Logger;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

/**
 * Abstract Base Class for performance benchmarks that can run in multiple threads. A benchmark supporting
 * execution in multiple threads can extend this class and should invoke the {@link #runTest(int, Object)} method
 * providing the number of concurrent threads to use for the current run. The benchmark must provide a 
 * {@link #runThread(String, Object)} method with will be invoked for every thread taking part.
 *  
 * @author <a href="david@redhat.com">David Bosschaert</a>
 */
public abstract class AbstractThreadedBenchmark<T> extends AbstractBenchmark implements PerformanceBenchmark
{
   // Provide logging
   private final Logger log = Logger.getLogger(AbstractThreadedBenchmark.class);
   
   // Contains the list of all the bundles that have been installed, so they can be uninstalled when the test is finished
   private final List<Bundle> installedBundles = Collections.synchronizedList(new ArrayList<Bundle>());

   protected AbstractThreadedBenchmark(BundleContext bc)
   {
      super(bc);
   }

   // Used to record any bundles installed by the test so that it can be uninstalled at cleanup
   protected void addedBundle(Bundle bundle)
   {
      installedBundles.add(bundle);
   }

   // Used to record any bundles installed by the test so that it can be uninstalled at cleanup
   protected void addedBundles(Collection<Bundle> bundles)
   {
      installedBundles.addAll(bundles);
   }

   /**
    * Provides a unique name for each thread.
    * @param numThreads The number of threads used.
    * @return a list of strings containing the thread names used.
    */
   protected List<String> getThreadNames(int numThreads)
   {
      List<String> names = new ArrayList<String>(numThreads);
      for (int i = 0; i < numThreads; i++)
      {
         names.add("Thread_" + (i + 1));
      }
      return names;
   }

   /**
    * Run the benchmark in a number of threads. This method will spawn the threads and
    * wait until they've all been finished.  
    * @param numThreads the number of threads required.
    * @param parameter arbitrary parameter which will be passed to each thread.
    * @throws Exception if one of the threads throws an exception it will be rethrown.
    */
   public void runTest(int numThreads, final T parameter) throws Exception
   {
      final List<Throwable> exceptions = Collections.synchronizedList(new ArrayList<Throwable>());
      List<Thread> threads = new ArrayList<Thread>(numThreads);
      for (final String threadName : getThreadNames(numThreads))
      {
         Thread t = new Thread(new Runnable()
         {
            @Override
            public void run()
            {
               try
               {
                  runThread(threadName, parameter);
               }
               catch (Throwable e)
               {
                  exceptions.add(e);
               }
            }
         });
         t.setName(threadName);
         threads.add(t);
      }

      System.out.println("Starting " + numThreads + " threads");
      for (Thread t : threads)
         t.start();

      for (Thread t : threads)
         t.join();
      System.out.println("All threads finished");

      cleanUp();

      if (exceptions.size() > 0)
      {
         for (Throwable th : exceptions)
            log.error("Test error", th);
         
         Throwable firstError = exceptions.get(0);
         if (firstError instanceof Exception)
            throw (Exception)firstError;
         
         throw new RuntimeException("One or more tests failures", firstError);
      }
   }

   public void cleanUp()
   {
      // now uninstall any of the installed bundles, in reverse order
      for (int i = installedBundles.size() - 1; i > 0; i--)
      {
         Bundle b = installedBundles.get(i);
         try
         {
            b.uninstall();
         }
         catch (Exception e)
         {
            log.error("Problem uninstalling bundle " + b, e);
         }
      }
   }

   /**
    * A benchmark implementation must provide this method which does the actual testing.
    * 
    * @param threadName the name of the current thread. Can be used to make things unique.
    * @param parameter the parameter passed into the {@link #runTest(int, Object)} method.
    * @throws Exception if anything goes wrong throw an exception. This will fail the test.
    */
   abstract public void runThread(String threadName, T parameter) throws Exception;
}
