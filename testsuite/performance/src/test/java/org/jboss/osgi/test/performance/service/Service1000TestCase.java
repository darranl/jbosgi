package org.jboss.osgi.test.performance.service;

import javax.inject.Inject;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osgi.framework.Bundle;

@RunWith(Arquillian.class)
public class Service1000TestCase extends ServiceTestBase
{
   @Inject
   public Bundle bundle;

   Bundle getBundle()
   {
      return bundle;
   }
   
   @Test
   public void test1000() throws Exception
   {
      testPerformance(1000);
   }
}
