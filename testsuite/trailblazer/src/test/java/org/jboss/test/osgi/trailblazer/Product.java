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
package org.jboss.test.osgi.trailblazer;

//$Id$

import java.io.Serializable;

/**
 * A product sold by the various {@link Shop}s. 
 * 
 * @author thomas.diesler@jboss.com
 * @since 10-May-2009
 */
public class Product implements Serializable
{
   private static final long serialVersionUID = 1L;
   
   private String name;
   private Integer price;
   private Shop shop;
   
   public Product(Shop shop, String name, Integer price)
   {
      this.shop = shop;
      this.name = name;
      this.price = price;
   }

   public Product(String name, Integer price)
   {
      this.name = name;
      this.price = price;
   }

   public Shop getShop()
   {
      return shop;
   }

   
   public void setShop(Shop shop)
   {
      this.shop = shop;
   }

   public String getName()
   {
      return name;
   }

   public Integer getPrice()
   {
      return price;
   }
}
