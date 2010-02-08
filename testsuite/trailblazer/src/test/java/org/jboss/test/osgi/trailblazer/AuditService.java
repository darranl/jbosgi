package org.jboss.test.osgi.trailblazer;

/**
 * An audit service interface.
 * 
 * @author thomas.diesler@jboss.com
 * @since 10-May-2009
 */
public interface AuditService
{
   int getTotalPurchases();

   void purchaseProduct(Product product);
}