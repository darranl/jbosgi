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
package org.jboss.test.osgi.trailblazer.frontend;

//$Id$

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.jboss.test.osgi.trailblazer.Product;
import org.jboss.test.osgi.trailblazer.Shop;

/**
 * A basic implementation of a shopping cart that is maintained in the user session
 * 
 * @author thomas.diesler@jboss.com
 * @since 10-May-2009
 */
public class ShoppingCart
{
   private Map<Shop, List<Product>> content = new HashMap<Shop, List<Product>>();
   
   // Hide ctor
   private ShoppingCart()
   {
   }

   static ShoppingCart getShoppingCart(HttpServletRequest req)
   {
      HttpSession session = req.getSession(true);
      ShoppingCart content = (ShoppingCart)session.getAttribute("cart");
      if (content == null)
      {
         content = new ShoppingCart();
         session.setAttribute("cart", content);
      }
      return content;
   }
   
   Set<Shop> getShops()
   {
      return content.keySet();
   }

   List<Product> getProductsByShop(Shop shop)
   {
      List<Product> products = content.get(shop);
      if (products == null)
      {
         products = new ArrayList<Product>();
         content.put(shop, products);
      }
      return products; 
   }
   
   void clearProductsByShop(Shop shop)
   {
      content.remove(shop);
   }
   
   void addToCart(Product product)
   {
      List<Product> products = getProductsByShop(product.getShop());
      products.add(product);
   }
}
