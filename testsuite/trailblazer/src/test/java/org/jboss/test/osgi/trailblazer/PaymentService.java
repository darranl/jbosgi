package org.jboss.test.osgi.trailblazer;

import java.util.Collection;

/**
 * A payment service used by the various {@link Shop}s.
 *  
 * A shop can define specific requirements on a payment service
 * i.e. must support VISA
 * 
 * @author thomas.diesler@jboss.com
 * @since 10-May-2009
 */
public interface PaymentService
{
   /**
    * The type of the payment service. (type=[credit|paypal])
    */
   String SERVICE_PROPERTY_TYPE = "type";
   
   /**
    * Returns the list of supported payment types
    */
   Collection<String> listPaymentTypes();
}