package org.jboss.test.osgi.trailblazer.pay.creditcard;

import java.util.ArrayList;
import java.util.Collection;

import org.jboss.test.osgi.trailblazer.PaymentService;


/**
 * A {@link PaymentService} that supports credit cards.
 * 
 * @author thomas.diesler@jboss.com
 * @since 10-May-2009
 */
public class CreditCardService implements PaymentService
{
   public Collection<String> listPaymentTypes()
   {
      ArrayList<String> list = new ArrayList<String>();
      list.add("Visa");
      list.add("MasterCard");
      return list;
   }
}