package org.jboss.osgi.test.performance;

public class Parameter
{
   private final String key;
   private final Object value;

   public Parameter(String key, Object value)
   {
      this.key = key;
      this.value = value;
   }

   public String getName()
   {
      return key;
   }

   public Object getValue()
   {
      return value;
   }
}
