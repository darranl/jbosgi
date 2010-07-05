package org.jboss.osgi.test.performance.service;

public class TestClass
{
   private String value;

   protected TestClass()
   {
   }

   public void setValue(String val)
   {
      value = val;
   }

   @Override
   public String toString()
   {
      return value;
   }

   public static TestClass createInst(Class<TestClass> clz, String val) throws Exception
   {
      TestClass inst = clz.newInstance();
      inst.setValue(val);
      return inst;
   }
}
