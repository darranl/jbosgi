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
package org.jboss.test.osgi.jbosgi161.bundle;

import java.util.ArrayList;
import java.util.List;

public class LoggingDelegate {
    public static void assertJBossLogging(String message) {
        org.jboss.logging.Logger log = org.jboss.logging.Logger.getLogger(LoggingDelegate.class);

        String loggerClass = log.getClass().getName();

        List<String> expected = new ArrayList<String>();
        expected.add("org.jboss.logging.Log4jLogger"); // Runtime
        expected.add("org.jboss.logging.JBossLogManagerLogger"); // AS7

        if (expected.contains(loggerClass) == false)
            throw new IllegalStateException("Unexpected logger: " + loggerClass);

        log.info("*******************************************");
        log.info("* jboss: " + message);
        log.info("*******************************************");
    }

    public static void assertCommonsLogging(String message) {
        org.apache.commons.logging.Log log = org.apache.commons.logging.LogFactory.getLog(LoggingDelegate.class);

        String loggerClass = log.getClass().getName();

        List<String> expected = new ArrayList<String>();
        expected.add("org.apache.commons.logging.impl.SLF4JLog");
        expected.add("org.apache.commons.logging.impl.Log4JLogger");
        expected.add("org.apache.commons.logging.impl.SLF4JLocationAwareLog");

        if (expected.contains(loggerClass) == false)
            throw new IllegalStateException("Unexpected logger: " + loggerClass);

        log.info("*******************************************");
        log.info("* jcl: " + message);
        log.info("*******************************************");
    }

    public static void assertSL4J(String message) {
        org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LoggingDelegate.class);

        String loggerClass = log.getClass().getName();

        List<String> expected = new ArrayList<String>();
        expected.add("org.jboss.slf4j.JBossLoggerAdapter");
        expected.add("org.slf4j.impl.Log4jLoggerAdapter");
        expected.add("org.slf4j.impl.Slf4jLogger");

        if (expected.contains(loggerClass) == false)
            throw new IllegalStateException("Unexpected logger: " + loggerClass);

        log.info("*******************************************");
        log.info("* slf4j: " + message);
        log.info("*******************************************");
    }
}