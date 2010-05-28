Setup the OSGi TCK
------------------

Checkout the OSGi TCK setup project

    git clone git://github.com/jbosgi/jbosgi.git
    cd jbosgi
    git submodule init
    git submodele update

Copy and edit the setup properties

    cd osgitck-setup
    cp ant.properties.example ant.properties
    vi ant.properties

Running the OSGi TCK against the RI (Equinox)

    ant clean setup.ri
    ant run-core-tests
    ant test-reports

Running the OSGi TCK against the JBoss OSGi Framework
    (This only works after having run 'ant clean setup.ri' at least once)

    ant clean setup.vi
    ant run-core-tests
    ant test-reports

Setup the OSGi TCK Hudson instance

    ant hudson-setup
    ant hudson-start
    sign up in hudson using your username. 

The Hudson setup uses the RI. You should see no errors when you run the jobs initially.
 
Running Tests with JPDA
-----------------------

export ANT_OPTS="-Djpda=-Xrunjdwp:transport=dt_socket,address=8787,server=y,suspend=y"
