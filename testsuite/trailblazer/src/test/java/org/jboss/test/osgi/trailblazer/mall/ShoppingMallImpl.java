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
package org.jboss.test.osgi.trailblazer.mall;

//$Id$

import java.util.ArrayList;
import java.util.Collection;

import org.jboss.test.osgi.trailblazer.AuditService;
import org.jboss.test.osgi.trailblazer.PaymentService;
import org.jboss.test.osgi.trailblazer.Product;
import org.jboss.test.osgi.trailblazer.Shop;
import org.jboss.test.osgi.trailblazer.ShoppingMall;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

/**
 * An implementation of {@link ShoppingMall}
 * 
 * @author thomas.diesler@jboss.com
 * @since 10-May-2009
 */
public class ShoppingMallImpl implements ShoppingMall
{
   private BundleContext context;
   
   public ShoppingMallImpl(BundleContext context)
   {
      this.context = context;
   }

   public Shop getShopByName(String shopName)
   {
      Shop shop = null;
      for (Shop aux : getShops())
      {
         if (aux.getName().equals(shopName))
         {
            shop = aux;
            break;
         }
      }
      return shop;
   }

   public Collection<Shop> getShops()
   {
      Collection<Shop> shops = new ArrayList<Shop>();
      try
      {
         ServiceReference[] shoprefs = context.getServiceReferences(Shop.class.getName(), null);
         if (shoprefs != null)
         {
            for(ServiceReference sref : shoprefs)
            {
               shops.add((Shop)context.getService(sref));
            }
         }
      }
      catch (InvalidSyntaxException ex)
      {
         // ignore, because we don't have a filter
      }
      return shops;
   }

   public void purchaseProduct(Product product)
   {
      Shop shop = product.getShop();
      PaymentService paymentService = shop.getPaymentService();
      if (paymentService == null)
         throw new IllegalStateException("Payment service not available");
      
      ServiceReference sref = context.getServiceReference(AuditService.class.getName());
      if (sref != null)
      {
         AuditService auditService = (AuditService)context.getService(sref);
         auditService.purchaseProduct(product);
      }
   }
   
}