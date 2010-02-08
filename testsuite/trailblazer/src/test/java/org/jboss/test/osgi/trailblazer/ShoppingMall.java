package org.jboss.test.osgi.trailblazer;

// $Id$

import java.util.Collection;

/**
 * the trailblazer shopping mall.
 * 
 * @author thomas.diesler@jboss.com
 * @since 10-May-2009
 */
public interface ShoppingMall
{
   /**
    * Get the collection of currently registered shops. 
    */
   Collection<Shop> getShops();

   /**
    * Get a shop by name. 
    */
   Shop getShopByName(String shopName);
   
   /**
    * Purchase a product
    */
   void purchaseProduct(Product product);
}