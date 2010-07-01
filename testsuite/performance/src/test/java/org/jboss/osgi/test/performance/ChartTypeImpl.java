package org.jboss.osgi.test.performance;

public class ChartTypeImpl implements ChartType
{
   private final String name, title, xLabel, yLabel;

   public ChartTypeImpl(String name, String title, String xLabel, String yLabel)
   {
      this.name = name;
      this.title = title;
      this.xLabel = xLabel;
      this.yLabel = yLabel;
   }

   @Override
   public String getName()
   {
      return name;
   }

   @Override
   public String getChartTitle()
   {
      return title;
   }

   @Override
   public String getXAxisLabel()
   {
      return xLabel;
   }

   @Override
   public String getYAxisLabel()
   {
      return yLabel;
   }
}
