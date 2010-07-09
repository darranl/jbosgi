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
import java.util.Collections;
import java.util.List;

import org.jboss.logging.Logger;
import org.osgi.framework.BundleContext;

/**
 * @author <a href="david@redhat.com">David Bosschaert</a>
 */
public abstract class AbstractThreadedBenchmark<T> extends AbstractBenchmark implements PerformanceBenchmark
{
   // Provide logging
   private final Logger log = Logger.getLogger(AbstractThreadedBenchmark.class);
   
   protected AbstractThreadedBenchmark(BundleContext bc)
   {
      super(bc);
   }

   protected List<String> getThreadNames(int numThreads)
   {
      List<String> names = new ArrayList<String>(numThreads);
      for (int i = 0; i < numThreads; i++)
      {
         names.add("Thread_" + (i + 1) + "_");
      }
      return names;
   }

   public void runTest(int numThreads, final T parameters) throws Exception
   {
      final List<Throwable> exceptions = Collections.synchronizedList(new ArrayList<Throwable>());
      List<Thread> threads = new ArrayList<Thread>(numThreads);
      for (final String threadName : getThreadNames(numThreads))
      {
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
      {
         for (Throwable th : exceptions)
            log.error("Test error", th);
         
         throw new RuntimeException("One or more performance test threads failed", exceptions.get(0));
      }
   }
   
   abstract protected void runThread(String threadName, T parameters) throws Exception;
}
