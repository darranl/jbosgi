package org.jboss.test.osgi.jbosgi508.bundle2;


import org.jboss.test.osgi.jbosgi508.bundle1.Service1;

public class Service2 {

    private Service1 service1;

    private void activate() throws InterruptedException {
        //System.out.println("activate 2");
    }


    private void deactivate() throws InterruptedException {
        //System.out.println("deactivate 2");
    }


    private void setService1(final Service1 service1) {
        //System.out.println("setService1: " + service1);
        this.service1 = service1;
    }

    public Service1 getService1() {
        return service1;
    }
}