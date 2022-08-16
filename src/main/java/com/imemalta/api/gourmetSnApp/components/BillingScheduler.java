package com.imemalta.api.gourmetSnApp.components;

import com.imemalta.api.gourmetSnApp.services.backend.StripeSubscriptionManagement;
import com.stripe.exception.StripeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class BillingScheduler {

    private final StripeSubscriptionManagement stripeSubscriptionManagement;

    @Autowired
    public BillingScheduler(StripeSubscriptionManagement stripeSubscriptionManagement) {
        this.stripeSubscriptionManagement = stripeSubscriptionManagement;
    }

//    @Scheduled(fixedRate = 1500)
//    public void updateUnmonitoredQrcodeUsage() {
//        try {
//            stripeSubscriptionManagement.updateUnmonitoredQrcodeUsage();
//        } catch (StripeException stripeException) {
//            stripeException.printStackTrace();
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//    }
}
