package org.jboss.osgi.test.performance;

import java.io.File;

public interface PerformanceBenchmark
{
   void reportXML(File targetFile, Parameter... parameters) throws Exception;
}
