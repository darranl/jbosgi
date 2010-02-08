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

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jboss.test.osgi.trailblazer.PaymentService;
import org.jboss.test.osgi.trailblazer.Product;
import org.jboss.test.osgi.trailblazer.Shop;
import org.jboss.test.osgi.trailblazer.ShoppingMall;

/**
 * An HTML request handler.
 * 
 * @author thomas.diesler@jboss.com
 * @since 10-May-2009
 */
public class RequestHandler
{
   private ShoppingMall mallService;

   public RequestHandler(ShoppingMall mallService)
   {
      this.mallService = mallService;
   }

   public void renderHomePage(HttpServletRequest req, HttpServletResponse res) throws IOException
   {
      PrintWriter out = res.getWriter();
      out.println("<h1>Welcome to the Mall</h1>");

      Collection<Shop> activeShops = mallService.getShops();
      if (activeShops.size() > 0)
      {
         for (Shop shop : activeShops)
         {
            String shopName = shop.getName();
            out.println("<a href='" + req.getServletPath() + "/list?shop=" + shopName + "'>" + shopName + "</a>");
            out.println("<br>");
         }
      }
      else
      {
         out.println("No active shops, please try later.");
      }
   }

   public void renderListPage(HttpServletRequest req, HttpServletResponse res) throws IOException
   {
      PrintWriter out = res.getWriter();

      Shop shop = getShopFromRequestParam(req);
      
      out.println("<h2>" + shop.getName() + "</h2");

      out.println("<table>");
      out.println("<tr><th>Product</th><th>Price</th></tr>");
      for (Product p : shop.getProductList())
      {
         StringBuffer price = new StringBuffer(p.getPrice().toString());
         price.insert(price.length() - 2, ".");

         out.println("<tr>");
         out.println("<td>" + p.getName() + "</td><td>" + price + "</td>");
         out.println("<td><a href='" + req.getServletPath() + "/buy?shop=" + shop.getName() + "&name=" + p.getName() + "'>buy</a></td>");
         out.println("</tr>");
      }
      out.println("</table>");
      out.println("<p/>");
      
      PaymentService payService = shop.getPaymentService();
      if (payService != null)
      {
         out.println("Supported payments: ");
         for (String type : payService.listPaymentTypes())
         {
            out.println(type + "&nbsp;");
         }
      }
   }

   public void renderCartPage(HttpServletRequest req, HttpServletResponse res) throws IOException
   {
      PrintWriter out = res.getWriter();

      out.println("<h2>Shopping Cart</h2>");

      ShoppingCart shoppingCart = ShoppingCart.getShoppingCart(req);
      Set<Shop> shops = shoppingCart.getShops();
      if (shops.size() == 0)
      {
         out.println("Your shopping cart is empty");
         return;
      }

      for (Shop shop : shops)
      {
         Integer total = new Integer(0);
         
         String shopName = shop.getName();
         out.println("<h3><a href='" + req.getServletPath() + "/list?shop=" + shopName + "'>" + shopName + "</a></h3>");
         out.println("<table>");
         out.println("<tr><th>Product</th><th>Price</th></tr>");
         for (Product p : shoppingCart.getProductsByShop(shop))
         {
            StringBuffer price = new StringBuffer(p.getPrice().toString());
            price.insert(price.length() - 2, ".");
            out.println("<tr>");
            out.println("<td>" + p.getName() + "</td><td>" + price + "</td>");
            out.println("</tr>");

            total += p.getPrice();
         }
         
         StringBuffer totalStr = new StringBuffer(total.toString());
         totalStr.insert(totalStr.length() - 2, ".");
         out.println("<tr><td>Total</td><th>" + totalStr + "</th></tr>");
         out.println("</table>");
         out.println("<p/>");

         PaymentService payService = shop.getPaymentService();
         if (payService != null)
         {
            out.println("<a href='" + req.getServletPath() + "/clear?shop=" + shopName + "'>Clear<a/> products or pay using: ");
            for (String type : payService.listPaymentTypes())
            {
               out.println("<a href='" + req.getServletPath() + "/checkout?shop=" + shopName + "&pay=" + type + "'>" + type + "</a>&nbsp;");
            }
         }
         else
         {
            out.println("Payment service not available, please try later.");
         }
      }
   }

   public void renderConfirmPage(HttpServletRequest req, HttpServletResponse res) throws IOException
   {
      PrintWriter out = res.getWriter();
      Shop shop = getShopFromRequestParam(req);
      out.println("Thanks for shopping with " + shop.getName());
   }

   public void actionAddToCart(HttpServletRequest req, HttpServletResponse res)
   {
      Shop shopService = getShopFromRequestParam(req);
      String name = req.getParameter("name");
      Product prod = shopService.getProductByName(name);
      if (prod == null)
         throw new IllegalArgumentException("Invalid product name: " + name);

      ShoppingCart shoppingCart = ShoppingCart.getShoppingCart(req);
      shoppingCart.addToCart(prod);
   }

   public void actionCheckout(HttpServletRequest req, HttpServletResponse res)
   {
      Shop shop = getShopFromRequestParam(req);
      ShoppingCart shoppingCart = ShoppingCart.getShoppingCart(req);
      for (Product prod : shoppingCart.getProductsByShop(shop))
      {
         mallService.purchaseProduct(prod);
      }
   }

   public void actionClearProducts(HttpServletRequest req, HttpServletResponse res)
   {
      Shop shop = getShopFromRequestParam(req);
      ShoppingCart shoppingCart = ShoppingCart.getShoppingCart(req);
      shoppingCart.clearProductsByShop(shop);
   }

   private Shop getShopFromRequestParam(HttpServletRequest req)
   {
      String shopName = req.getParameter("shop");
      Shop shop = mallService.getShopByName(shopName);
      if (shop == null)
         throw new IllegalArgumentException("Invalid request for shop: " + shopName);
      return shop;
   }
}
