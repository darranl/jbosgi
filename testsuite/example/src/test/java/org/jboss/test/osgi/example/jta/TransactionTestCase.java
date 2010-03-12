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
package org.jboss.test.osgi.example.jta;

//$Id$

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assume.assumeNotNull;

import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;

import org.jboss.osgi.husky.BridgeFactory;
import org.jboss.osgi.husky.HuskyCapability;
import org.jboss.osgi.husky.RuntimeContext;
import org.jboss.osgi.jmx.JMXCapability;
import org.jboss.osgi.jta.TransactionCapability;
import org.jboss.osgi.testing.OSGiRuntime;
import org.jboss.osgi.testing.OSGiRuntimeHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

/**
 * An example of OSGi JTA.
 * 
 * @author thomas.diesler@jboss.com
 * @since 23-Oct-2009
 */
public class TransactionTestCase
{
   @RuntimeContext
   public BundleContext context;
   
   private OSGiRuntime runtime;

   @Before
   public void setUp() throws Exception
   {
      if (context == null)
      {
         runtime = new OSGiRuntimeHelper().getDefaultRuntime();
         runtime.addCapability(new JMXCapability());
         runtime.addCapability(new HuskyCapability());
         runtime.addCapability(new TransactionCapability());
         runtime.installBundle("example-jta.jar").start();
      }
   }

   @After
   public void tearDown() throws Exception
   {
      if (context == null)
         runtime.shutdown();
   }

   @Test
   public void testUserTransaction() throws Exception
   {
      if (context == null)
         BridgeFactory.getBridge().run();
      
      // Tell Husky to run this test method within the OSGi Runtime
      if (context == null)
         BridgeFactory.getBridge().run();
      
      // Stop here if the context is not injected
      assumeNotNull(context);
      
      Transactional txObj = new Transactional();
      
      ServiceReference userTxRef = context.getServiceReference(UserTransaction.class.getName());
      assertNotNull("UserTransaction service not null", userTxRef);
      
      UserTransaction userTx = (UserTransaction)context.getService(userTxRef);
      assertNotNull("UserTransaction not null", userTx);
      
      userTx.begin();
      try
      {
         ServiceReference tmRef = context.getServiceReference(TransactionManager.class.getName());
         assertNotNull("TransactionManager service not null", tmRef);
         
         TransactionManager tm = (TransactionManager)context.getService(tmRef);
         assertNotNull("TransactionManager not null", tm);
         
         Transaction tx = tm.getTransaction();
         assertNotNull("Transaction not null", tx);
         
         tx.registerSynchronization(txObj);
         
         txObj.setMessage("Donate $1.000.000");
         assertNull("Uncommited message null", txObj.getMessage());
         
         userTx.commit();
      }
      catch (Exception e)
      {
         userTx.setRollbackOnly();
      }

      assertEquals("Donate $1.000.000", txObj.getMessage());
   }

   class Transactional implements Synchronization
   {
      private String volatileMessage;
      private String message;
      
      public void beforeCompletion()
      {
      }
      
      public void afterCompletion(int status)
      {
         if (status == Status.STATUS_COMMITTED)
            message = volatileMessage;
      }
      
      public String getMessage()
      {
         return message;
      }

      public void setMessage(String message)
      {
         this.volatileMessage = message;
      }
   }
}