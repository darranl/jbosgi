/*
 * JBoss, Home of Professional Open Source.
 * Copyright (c) 2011, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
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
package org.jboss.test.osgi.example.jbossas.ejb3;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;

import org.jboss.logging.Logger;
import org.jboss.test.osgi.example.jbossas.api.PaymentService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

/**
 * A simple stateless session bean.
 *
 * @author thomas.diesler@jboss.com
 */
@Stateless
@LocalBean
public class SimpleStatelessSessionBean {

    // Provide logging
    static final Logger log = Logger.getLogger(SimpleStatelessSessionBean.class);

    @Resource
    private BundleContext context;

    private PaymentService service;

    @PostConstruct
    public void init() {

        final SimpleStatelessSessionBean bean = this;

        log.infof("BundleContext symbolic name: %s", context.getBundle().getSymbolicName());

        // Track {@link PaymentService} implementations
        ServiceTracker tracker = new ServiceTracker(context, PaymentService.class.getName(), null) {

            @Override
            public Object addingService(ServiceReference sref) {
                log.infof("Adding service: %s to %s", sref, bean);
                service = (PaymentService) super.addingService(sref);
                return service;
            }

            @Override
            public void removedService(ServiceReference sref, Object sinst) {
                super.removedService(sref, service);
                log.infof("Removing service: %s from %s", sref, bean);
                service = null;
            }
        };
        tracker.open();
    }

    public String process(String account, String amount) {

        if (service == null)
            return "PaymentService not available";

        return service.process(account, amount != null ? Float.valueOf(amount) : null);
    }
}
