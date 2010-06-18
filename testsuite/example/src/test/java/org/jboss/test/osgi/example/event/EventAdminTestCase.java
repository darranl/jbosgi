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
package org.jboss.test.osgi.example.event;

//$Id$

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assume.assumeNotNull;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

import org.jboss.osgi.husky.BridgeFactory;
import org.jboss.osgi.husky.HuskyCapability;
import org.jboss.osgi.husky.RuntimeContext;
import org.jboss.osgi.spi.capability.EventAdminCapability;
import org.jboss.osgi.testing.OSGiBundle;
import org.jboss.osgi.testing.OSGiRuntime;
import org.jboss.osgi.testing.OSGiRuntimeTest;
import org.jboss.osgi.testing.OSGiServiceReference;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;

/**
 * A test that deployes the EventAdmin and sends/receives messages on a topic. 
 * 
 * @author thomas.diesler@jboss.com
 * @since 08-Dec-2009
 */
public class EventAdminTestCase extends OSGiRuntimeTest
{
   static String TOPIC = "org/jboss/test/osgi/example/event";

   @RuntimeContext
   public BundleContext context;

   private OSGiRuntime runtime;

   @Before
   public void setUp() throws Exception
   {
      if (context == null)
      {
         runtime = getDefaultRuntime();
         runtime.addCapability(new HuskyCapability());
         runtime.addCapability(new EventAdminCapability());

         OSGiServiceReference sref = runtime.getServiceReference(EventAdmin.class.getName(), 5000);
         assertNotNull("EventAdmin available", sref);
         
         OSGiBundle bundle = runtime.installBundle("example-event.jar");
         bundle.start();
      }
   }

   @After
   public void tearDown() throws BundleException
   {
      if (context == null)
         runtime.shutdown();
   }

   @Test
   @SuppressWarnings({ "unchecked", "rawtypes" })
   public void testEventHandler() throws Exception
   {
      if (context == null)
         BridgeFactory.getBridge().run();

      assumeNotNull(context);

      TestEventHandler eventHandler = new TestEventHandler();

      // Register the EventHandler
      Dictionary param = new Hashtable();
      param.put(EventConstants.EVENT_TOPIC, new String[] { TOPIC });
      context.registerService(EventHandler.class.getName(), eventHandler, param);

      // Send event through the the EventAdmin
      ServiceReference sref = context.getServiceReference(EventAdmin.class.getName());
      EventAdmin eventAdmin = (EventAdmin)context.getService(sref);
      eventAdmin.sendEvent(new Event(TOPIC, (Dictionary)null));

      // Verify received event
      assertEquals("Event received", 1, eventHandler.received.size());
      assertEquals(TOPIC, eventHandler.received.get(0).getTopic());
   }

   static class TestEventHandler implements EventHandler
   {
      List<Event> received = new ArrayList<Event>();

      public void handleEvent(Event event)
      {
         received.add(event);
      }
   }
}