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

import org.osgi.framework.BundleContext;

/**
 * @author <a href="david@redhat.com">David Bosschaert</a>
 */
public abstract class AbstractThreadedBenchmark<T> extends AbstractBenchmark implements PerformanceBenchmark
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
         final String threadName = "Thread_" + (i + 1);

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
