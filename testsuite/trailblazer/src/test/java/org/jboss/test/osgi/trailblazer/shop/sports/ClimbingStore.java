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
package org.jboss.test.osgi.trailblazer.shop.sports;

//$Id$

import java.util.ArrayList;
import java.util.Collection;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

import org.jboss.test.osgi.trailblazer.PaymentService;
import org.jboss.test.osgi.trailblazer.Product;
import org.jboss.test.osgi.trailblazer.Shop;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

/**
 * The climbing store uses JNDI to bind product information.
 * 
 * It requires a {@link PaymentService} that supports credit cards.
 * 
 * @author thomas.diesler@jboss.com
 * @since 10-May-2009
 */
public class ClimbingStore implements Shop
{
   private BundleContext context;
   
   public ClimbingStore(BundleContext context)
   {
      this.context = context;
      
      try
      {
         Context ctx = getInitialContext(context);
         ctx = ctx.createSubcontext("shop");
         ctx = ctx.createSubcontext("climbing");
         ctx = ctx.createSubcontext("products");
         add(ctx, new Product(null, "Shoes", 9500));
         add(ctx, new Product(null, "Chalk", 150));
         add(ctx, new Product(null, "Harness", 7500));
         add(ctx, new Product(null, "Rope", 13500));
         add(ctx, new Product(null, "Pants", 7500));
         add(ctx, new Product(null, "Shirt", 3500));
      }
      catch (NamingException ex)
      {
         throw new IllegalStateException("Cannot bind products", ex);
      }
   }

   public String getName()
   {
      return "ClimbingStore";
   }
   
   public PaymentService getPaymentService()
   {
      ServiceReference[] srefs;
      try
      {
         srefs = context.getServiceReferences(PaymentService.class.getName(), "(type=credit)");
      }
      catch (InvalidSyntaxException ex)
      {
         throw new IllegalArgumentException(ex);
      }
      ServiceReference sref = srefs != null && srefs.length > 0 ? srefs[0] : null;
      return sref != null ? (PaymentService)context.getService(sref) : null;
   }
   
   public Collection<Product> getProductList()
   {
      Collection<Product> products = new ArrayList<Product>();
      try
      {
         InitialContext iniCtx = getInitialContext(context);
         
         NamingEnumeration<NameClassPair> list = iniCtx.list("shop/climbing/products");
         while (list.hasMoreElements())
         {
            NameClassPair pair = list.nextElement();
            String name = pair.getName();
            products.add(getProductByName(name));
         }
      }
      catch (NamingException ex)
      {
         throw new IllegalStateException("Cannot lookup products", ex);
      }
      return products;
   }

   public Product getProductByName(String name)
   {
      Product product;
      
      ClassLoader ctxLoader = Thread.currentThread().getContextClassLoader();
      try
      {
         Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
         
         InitialContext iniCtx = getInitialContext(context);
         product = (Product)iniCtx.lookup("shop/climbing/products/" + name);
         product.setShop(this);
      }
      catch (NamingException ex)
      {
         throw new IllegalStateException("Cannot lookup product: " + name, ex);
      }
      finally
      {
         Thread.currentThread().setContextClassLoader(ctxLoader);
      }
      return product;
   }

   private void add(Context ctx, Product p) throws NamingException
   {
      ctx.bind(p.getName(), p);
   }

   private InitialContext getInitialContext(BundleContext context)
   {
      ServiceReference sref = context.getServiceReference(InitialContext.class.getName());
      if (sref == null)
         throw new IllegalStateException("Cannot access the InitialContext");
      
      InitialContext initContext = (InitialContext)context.getService(sref);
      return initContext;
   }
}
