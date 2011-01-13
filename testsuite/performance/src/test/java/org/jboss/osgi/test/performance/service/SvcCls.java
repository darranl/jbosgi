package org.jboss.osgi.test.performance.service;

public class SvcCls {
    private String value;

    protected SvcCls() {
    }

    public void setValue(String val) {
        value = val;
    }

    @Override
    public String toString() {
        return value;
    }

    public static SvcCls createInst(Class<SvcCls> clz, String val) throws Exception {
        SvcCls inst = clz.newInstance();
        inst.setValue(val);
        return inst;
    }
}
