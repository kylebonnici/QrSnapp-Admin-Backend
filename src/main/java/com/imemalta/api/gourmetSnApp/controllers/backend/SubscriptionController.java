package com.imemalta.api.gourmetSnApp.controllers.backend;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.imemalta.api.gourmetSnApp.services.backend.StripeSubscriptionManagement;
import com.stripe.exception.StripeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/")
@ResponseBody
public class SubscriptionController {
    @Autowired
    private StripeSubscriptionManagement stripeSubscriptionManagement;

    @RequestMapping(value = "/profile/{username}/subscriptions/", method = RequestMethod.GET)
    @PreAuthorize("@identityPermissionsEvaluators.isLoggedInUser(#username) || hasRole('ROLE_SUPER_ADMIN_VIEW_SUBSCRIPTION')")
    public List<Object> getUserSubscriptions(@PathVariable("username") String username) throws JsonProcessingException {
        return stripeSubscriptionManagement.getUserSubscriptions(username);
    }

    @RequestMapping(value = "/profile/{username}/subscriptions/product/qrcode/", method = RequestMethod.GET)
    @PreAuthorize("@identityPermissionsEvaluators.isLoggedInUser(#username) || hasRole('ROLE_SUPER_ADMIN_VIEW_SUBSCRIPTION')")
    public Object getUserQrcodeSubscriptions(@PathVariable("username") String username) throws JsonProcessingException {
        return stripeSubscriptionManagement.getQrCodeSubscription(username);
    }

    @RequestMapping(value = "/profile/{username}/subscriptions/has/product/qrcode/", method = RequestMethod.GET)
    @PreAuthorize("@identityPermissionsEvaluators.isLoggedInUser(#username) || hasRole('ROLE_SUPER_ADMIN_VIEW_SUBSCRIPTION')")
    public boolean getUserHasQrcodeSubscriptions(@PathVariable("username") String username){
        return stripeSubscriptionManagement.hasQrCodeSubscription(username);
    }

    @RequestMapping(value = "/profile/{username}/subscriptions/product/any", method = RequestMethod.GET)
    @PreAuthorize("@identityPermissionsEvaluators.isLoggedInUser(#username) || hasRole('ROLE_SUPER_ADMIN_VIEW_SUBSCRIPTION')")
    public boolean getUserHasAnySubscriptions(@PathVariable("username") String username){
        return stripeSubscriptionManagement.hasSubscription(username);
    }

    @RequestMapping(value = "/profile/{username}/subscriptions/qrcode/", method = RequestMethod.POST)
    @PreAuthorize("@identityPermissionsEvaluators.isLoggedInUser(#username) || hasRole('ROLE_SUPER_ADMIN_VIEW_SUBSCRIPTION')")
    public Map<String, String> addQrcode(@PathVariable("username") String username, @RequestBody String[] priceIds){
        String out = stripeSubscriptionManagement.createQrCodeCheckoutSession(priceIds, username);
        Map<String, String> outMap = new HashMap<>();
        outMap.put("sessionKey", out);
        return outMap;
    }

    @RequestMapping(value = "/profile/{username}/subscriptions/qrcode", method = RequestMethod.DELETE)
    @PreAuthorize("@identityPermissionsEvaluators.isLoggedInUser(#username) || hasRole('ROLE_SUPER_ADMIN_VIEW_SUBSCRIPTION')")
    public void deleteQrcode(@PathVariable("username") String username){
        stripeSubscriptionManagement.cancelQrCodeSubscription(username);
    }

    @RequestMapping(value = "/webhook/stripe", method = RequestMethod.POST)
    public void eventWebhook(@RequestHeader("Stripe-Signature") String sigHeader, @RequestBody  String payload) throws StripeException {
        stripeSubscriptionManagement.processWebhookEvents(payload, sigHeader);
    }

    @RequestMapping(value = "/profile/{username}/subscriptions/qrcode/usage/", method = RequestMethod.GET)
    @PreAuthorize("@identityPermissionsEvaluators.isLoggedInUser(#username) || hasRole('ROLE_SUPER_ADMIN_VIEW_SUBSCRIPTION')")
    public Map<String, Long> getQrCodeUsage(@PathVariable("username") String username){
        return stripeSubscriptionManagement.getQrCodeSubscriptionUsage(username);
    }

    @RequestMapping(value = "/profile/{username}/subscriptions/qrcode/{qrcodeId}/usage/", method = RequestMethod.GET)
    @PreAuthorize("@identityPermissionsEvaluators.isLoggedInUser(#username) || hasRole('ROLE_SUPER_ADMIN_VIEW_SUBSCRIPTION')")
    public Map<String, Long> getQrCodeUsage(@PathVariable("username") String username, @PathVariable("qrcodeId") UUID qrcodeId){
        return stripeSubscriptionManagement.getSpecificQrCodeUsage(username, qrcodeId);
    }
}
