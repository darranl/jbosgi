package org.jboss.test.osgi.trailblazer;

import java.util.Collection;


/**
 * A {@link Shop} represents and individual shop implementation.
 * <p/> 
 * The trailblazer supports multiple shops. Each shop maintains a collection of
 * {@link Products}s and a {@link PaymentService}
 * <p/>
 * Shops can get registered and unregistered at any time. 
 * 
 * @author thomas.diesler@jboss.com
 * @since 10-May-2009
 */
public interface Shop
{
   /**
    * Get the name of the shop
    */
   String getName();
   
   /**
    * Get the product list
    * @return an empty collection if there are no products
    */
   Collection<Product> getProductList();
   
   /**
    * Get a product by name
    * @return null if the product does not exist
    */
   Product getProductByName(String name);
   
   /**
    * Get the associated payment service. 
    */
   PaymentService getPaymentService();
}