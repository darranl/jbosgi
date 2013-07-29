JBoss OSGi
==========

This is the umbrella project for our OSGi effort. It is the home of the integration testsuite, the installer, documentation, etc.

Building From Source
--------------------

> git clone git://github.com/jbosgi/jbosgi.git

Setup the JBoss Maven Repository
--------------------------------

To use dependencies from JBoss.org, you need to add the JBoss Maven Repositories to your Maven settings.xml. For details see http://community.jboss.org/wiki/MavenGettingStarted-Users

Build with Maven
----------------

The command below builds all modules and runs the embedded suite.

<pre>
$ mvn clean install
...
[INFO] ------------------------------------------------------------------------
[INFO] Reactor Summary:
[INFO] 
[INFO] JBossOSGi ......................................... SUCCESS [0.801s]
[INFO] JBossOSGi Testsuite ............................... SUCCESS [0.201s]
[INFO] JBossOSGi Testsuite Examples ...................... SUCCESS [15.912s]
[INFO] JBossOSGi Testsuite Functional .................... SUCCESS [4.984s]
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 22.504s
[INFO] Finished at: Mon Jul 29 10:49:01 CEST 2013
[INFO] Final Memory: 26M/201M
[INFO] ------------------------------------------------------------------------
</pre>

Build/Test the WildFly OSGi subsystem
-------------------------------------

To build the OSGi subsystem specify the target container like this

<pre>
$ mvn -Dtarget.container=wildfly800 clean install
...
[INFO] ------------------------------------------------------------------------
[INFO] Reactor Summary:
[INFO] 
[INFO] JBossOSGi ......................................... SUCCESS [0.386s]
[INFO] JBossOSGi WildFly ................................. SUCCESS [0.016s]
[INFO] JBossOSGi WildFly Subsystem ....................... SUCCESS [11.367s]
[INFO] JBossOSGi WildFly Integration ..................... SUCCESS [0.013s]
[INFO] JBossOSGi WildFly Integration: Configadmin ........ SUCCESS [3.443s]
[INFO] JBossOSGi WildFly Integration: Http ............... SUCCESS [1.308s]
[INFO] JBossOSGi WildFly Integration: JMX ................ SUCCESS [0.150s]
[INFO] JBossOSGi WildFly Integration: JPA ................ SUCCESS [1.445s]
[INFO] JBossOSGi WildFly Integration: JTA ................ SUCCESS [0.159s]
[INFO] JBossOSGi WildFly Integration: Naming ............. SUCCESS [0.243s]
[INFO] JBossOSGi WildFly Integration: WebApp ............. SUCCESS [0.359s]
[INFO] JBossOSGi WildFly Build ........................... SUCCESS [17.692s]
[INFO] JBossOSGi Testsuite ............................... SUCCESS [1.388s]
[INFO] JBossOSGi Testsuite Examples ...................... SUCCESS [37.201s]
[INFO] JBossOSGi Testsuite Functional .................... SUCCESS [14.247s]
[INFO] JBossOSGi Testsuite Integration ................... SUCCESS [51.954s]
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 2:22.915s
[INFO] Finished at: Mon Jul 29 10:53:23 CEST 2013
[INFO] Final Memory: 79M/254M
[INFO] ------------------------------------------------------------------------
</pre>
