JBoss OSGi
==========

This is the umbrella project for our OSGi effort. It is the home of the integration testsuite, the installer, documentation, etc.

Building From Source
--------------------

> git clone git://github.com/jbosgi/jbosgi.git

When you work with the latest head it is likely that the umbrella project contains references to external submodules. In which case you have a .gitmodules file in the project root. Before you do the actual maven build, you need to initialize and update the submodules like this

> git submodule init  
> git submodule update

Setup the JBoss Maven Repository
--------------------------------

To use dependencies from JBoss.org, you need to add the JBoss Maven Repositories to your Maven settings.xml. For details see http://community.jboss.org/wiki/MavenGettingStarted-Users

Build with Maven
----------------

The command below builds all the modules and runs all the suites in embedded mode.

<pre>
$ mvn clean install
...
[INFO] ------------------------------------------------------------------------
[INFO] Reactor Summary:
[INFO] ------------------------------------------------------------------------
[INFO] JBossOSGi ............................................. SUCCESS [0.071s]
[INFO] JBossOSGi Reactor ..................................... SUCCESS [0.067s]
[INFO] JBossOSGi Testsuite ................................... SUCCESS [1.095s]
[INFO] JBossOSGi Testsuite - Examples ........................ SUCCESS [1:35.635s]
[INFO] JBossOSGi Testsuite - Functional ...................... SUCCESS [1:03.949s]
[INFO] ------------------------------------------------------------------------
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESSFUL
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 4 minutes 51 seconds
[INFO] Finished at: Thu Mar 04 08:21:04 CET 2010
[INFO] Final Memory: 74M/148M
[INFO] ------------------------------------------------------------------------
</pre>

To setup a local Hudson QA environment have a look at http://community.jboss.org/wiki/JBossOSGiHudsonQA

Running Tests
-------------

There are two properties that can be specified when running tests

> -Dtarget.container=[runtime|jboss501|jboss510|jboss???]  
> -Dframework=[felix|equinox|jbossmc]

The default uses the native MC based Framework in embedded mode (i.e. no target container)
