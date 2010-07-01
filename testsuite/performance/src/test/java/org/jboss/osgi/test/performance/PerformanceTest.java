package org.jboss.osgi.test.performance;

import java.io.File;

public interface PerformanceTest
{
   void reportXML(File targetFile) throws Exception;
}
