package com.imemalta.api.gourmetSnApp.exceptions.common;

import com.stripe.exception.StripeException;

public class StripeRuntimeException extends RuntimeException {
    private StripeException stripeException;

    public StripeRuntimeException(StripeException stripeException) {
        this.stripeException = stripeException;
    }

    public StripeException getStripeException() {
        return stripeException;
    }

    public void setStripeException(StripeException stripeException) {
        this.stripeException = stripeException;
    }
}
