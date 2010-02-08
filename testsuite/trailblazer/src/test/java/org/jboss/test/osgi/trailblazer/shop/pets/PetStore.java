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
package org.jboss.test.osgi.trailblazer.shop.pets;

//$Id$

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.jboss.test.osgi.trailblazer.PaymentService;
import org.jboss.test.osgi.trailblazer.Product;
import org.jboss.test.osgi.trailblazer.Shop;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

/**
 * The pet store requires a {@link PaymentService} that supports PayPal.
 * 
 * @author thomas.diesler@jboss.com
 * @since 10-May-2009
 */
public class PetStore implements Shop
{
   private BundleContext context;
   private Map<String, Product> products = new LinkedHashMap<String, Product>();
   
   public PetStore(BundleContext context)
   {
      this.context = context;
      add(new Product(this, "Cat", 7500));
      add(new Product(this, "Dog", 9500));
      add(new Product(this, "Hamster", 500));
      add(new Product(this, "Bird", 1500));
      add(new Product(this, "Snail", 27500));
   }

   public String getName()
   {
      return "PetStore";
   }
   
   public Collection<Product> getProductList()
   {
      return Collections.unmodifiableCollection(products.values());
   }

   public Product getProductByName(String name)
   {
      return products.get(name);
   }
   
   public PaymentService getPaymentService()
   {
      ServiceReference[] srefs;
      try
      {
         srefs = context.getServiceReferences(PaymentService.class.getName(), "(type=paypal)");
      }
      catch (InvalidSyntaxException ex)
      {
         throw new IllegalArgumentException(ex);
      }
      ServiceReference sref = srefs != null && srefs.length > 0 ? srefs[0] : null;
      return sref != null ? (PaymentService)context.getService(sref) : null;
   }

   private void add(Product p)
   {
      products.put(p.getName(), p);
   }
}
