/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012, JBoss Inc., and individual contributors as indicated
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
package org.jboss.test.osgi.example.jndi.bundle;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import javax.naming.Binding;
import javax.naming.CompositeName;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NameClassPair;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.spi.InitialContextFactory;
import javax.naming.spi.NamingManager;
import javax.naming.spi.ObjectFactory;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.service.jndi.JNDIConstants;

/**
 * @author Thomas.Diesler@jboss.com
 */
public class JNDITestActivator implements BundleActivator {

    @Override
    public void start(BundleContext context) throws Exception {

        // Register a simple service
        Hashtable<String, String> props = new Hashtable<String, String>();
        props.put(JNDIConstants.JNDI_SERVICENAME, "foo");
        JNDITestService service = new JNDITestService() {
            @Override
            public String getValue() {
                return "jndi-value";
            }
        };
        context.registerService(JNDITestService.class.getName(), service, props);

        // Register a {@link InitialContextFactory} provider
        String[] classes = new String[] { SimpleInitalContextFactory.class.getName(), InitialContextFactory.class.getName() };
        context.registerService(classes, new SimpleInitalContextFactory(), null);

        // Register a {@link ObjectFactory} provider
        classes = new String[] { StringObjectFactory.class.getName(), ObjectFactory.class.getName() };
        context.registerService(classes, new StringObjectFactory(), null);
    }

    @Override
    public void stop(BundleContext context) throws Exception {
    }

    @SuppressWarnings("serial")
    public static final class StringReference extends Reference {

        private final String value;

        public StringReference(String value) {
            super(String.class.getName(), StringObjectFactory.class.getName(), null);
            this.value = value;
        }
    }

    public static final class StringObjectFactory implements ObjectFactory {
        @Override
        public Object getObjectInstance(Object obj, Name name, Context nameCtx, Hashtable<?, ?> environment) throws Exception {
            StringReference ref = (StringReference) obj;
            return ref.value;
        }
    }

    public static class SimpleInitalContextFactory implements InitialContextFactory {
        @Override
        public Context getInitialContext(Hashtable<?, ?> environment) throws NamingException {
            return new BasicContext();
        }
    }

    static final class BasicContext implements Context {

        private final Map<String, Object> mapping = new HashMap<String, Object>();
        private final Hashtable<String, Object> env = new Hashtable<String, Object>();

        BasicContext() {
            env.put(Context.INITIAL_CONTEXT_FACTORY, SimpleInitalContextFactory.class.getName());
        }

        @Override
        public Object lookup(Name name) throws NamingException {
            return lookup(name.toString());
        }

        @Override
        public Object lookup(String name) throws NamingException {
            Object val = mapping.get(name);
            if (val instanceof Reference) {
                CompositeName compname = new CompositeName(name);
                try {
                    val = NamingManager.getObjectInstance(val, compname, this, null);
                } catch (Exception ex) {
                    NamingException nex = new NamingException(ex.getMessage());
                    nex.initCause(ex);
                }
            }
            return val;
        }

        @Override
        public void bind(Name name, Object obj) throws NamingException {
            bind(name.toString(), obj);
        }

        @Override
        public void bind(String name, Object obj) throws NamingException {
            mapping.put(name, obj);
        }

        @Override
        public void rebind(Name name, Object obj) throws NamingException {
            rebind(name.toString(), obj);
        }

        @Override
        public void rebind(String name, Object obj) throws NamingException {
            mapping.put(name, obj);
        }

        @Override
        public void unbind(Name name) throws NamingException {
            unbind(name.toString());
        }

        @Override
        public void unbind(String name) throws NamingException {
            mapping.remove(name);
        }

        @Override
        public void rename(Name oldName, Name newName) throws NamingException {
            throw new UnsupportedOperationException();
        }

        @Override
        public void rename(String oldName, String newName) throws NamingException {
            throw new UnsupportedOperationException();
        }

        @Override
        public NamingEnumeration<NameClassPair> list(Name name) throws NamingException {
            throw new UnsupportedOperationException();
        }

        @Override
        public NamingEnumeration<NameClassPair> list(String name) throws NamingException {
            throw new UnsupportedOperationException();
        }

        @Override
        public NamingEnumeration<Binding> listBindings(Name name) throws NamingException {
            throw new UnsupportedOperationException();
        }

        @Override
        public NamingEnumeration<Binding> listBindings(String name) throws NamingException {
            throw new UnsupportedOperationException();
        }

        @Override
        public void destroySubcontext(Name name) throws NamingException {
            throw new UnsupportedOperationException();
        }

        @Override
        public void destroySubcontext(String name) throws NamingException {
            throw new UnsupportedOperationException();
        }

        @Override
        public Context createSubcontext(Name name) throws NamingException {
            throw new UnsupportedOperationException();
        }

        @Override
        public Context createSubcontext(String name) throws NamingException {
            throw new UnsupportedOperationException();
        }

        @Override
        public Object lookupLink(Name name) throws NamingException {
            throw new UnsupportedOperationException();
        }

        @Override
        public Object lookupLink(String name) throws NamingException {
            throw new UnsupportedOperationException();
        }

        @Override
        public NameParser getNameParser(Name name) throws NamingException {
            throw new UnsupportedOperationException();
        }

        @Override
        public NameParser getNameParser(String name) throws NamingException {
            throw new UnsupportedOperationException();
        }

        @Override
        public Name composeName(Name name, Name prefix) throws NamingException {
            throw new UnsupportedOperationException();
        }

        @Override
        public String composeName(String name, String prefix) throws NamingException {
            throw new UnsupportedOperationException();
        }

        @Override
        public Object addToEnvironment(String propName, Object propVal) throws NamingException {
            return env.put(propName, propVal);
        }

        @Override
        public Object removeFromEnvironment(String propName) throws NamingException {
            return env.remove(propName);
        }

        @Override
        public Hashtable<?, ?> getEnvironment() throws NamingException {
            return new Hashtable<String, Object>(env);
        }

        @Override
        public void close() throws NamingException {
        }

        @Override
        public String getNameInNamespace() throws NamingException {
            throw new UnsupportedOperationException();
        }
    }
}
