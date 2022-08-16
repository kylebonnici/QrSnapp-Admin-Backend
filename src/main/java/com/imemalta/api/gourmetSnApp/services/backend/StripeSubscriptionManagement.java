package com.imemalta.api.gourmetSnApp.services.backend;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.imemalta.api.gourmetSnApp.entities.authentication.User;
import com.imemalta.api.gourmetSnApp.entities.authentication.repositories.UserRepository;
import com.imemalta.api.gourmetSnApp.entities.backend.QRCodeRuleUsage;
import com.imemalta.api.gourmetSnApp.entities.backend.UserMetadata;
import com.imemalta.api.gourmetSnApp.entities.backend.repositories.QRCodeRuleUsageRepository;
import com.imemalta.api.gourmetSnApp.entities.backend.repositories.UserMetadataRepository;
import com.imemalta.api.gourmetSnApp.exceptions.authentication.UserNotFoundException;
import com.imemalta.api.gourmetSnApp.exceptions.common.AlreadySubscribedException;
import com.imemalta.api.gourmetSnApp.exceptions.common.InvalidPricesForCheckout;
import com.imemalta.api.gourmetSnApp.exceptions.common.NoSubscriptionException;
import com.imemalta.api.gourmetSnApp.exceptions.common.StripeRuntimeException;
import com.stripe.exception.StripeException;
import com.stripe.model.*;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.*;

@Service
public class StripeSubscriptionManagement {
    @Value("${stripe.webhook_key}")
    private String webhookKey;

    private final UserMetadataRepository userMetadataRepository;
    private final UserRepository userRepository;
    private final QRCodeRuleUsageRepository qrCodeRuleUsageRepository;

    @Value("${stripe.qrcodeScans_product}")
    private String qrcodeScansProduct;

    @Value("${stripe.uniqueQrcodes_product}")
    private String uniqueQrcodesProduct;

    @Value("${stripe.canceled_url}")
    private String canceledUrl;

    @Value("${stripe.success_url}")
    private String successUrl;

    @Autowired
    public StripeSubscriptionManagement(UserMetadataRepository userMetadataRepository, UserRepository userRepository, QRCodeRuleUsageRepository qrCodeRuleUsageRepository) {
        this.userMetadataRepository = userMetadataRepository;
        this.userRepository = userRepository;
        this.qrCodeRuleUsageRepository = qrCodeRuleUsageRepository;
    }

    @Transactional(readOnly = true)
    public boolean hasQrCodeSubscription(String username) {
        Optional<UserMetadata> userMetadata = userMetadataRepository.findByUserUsername(username);

        if (userMetadata.isEmpty()) {
            throw new UserNotFoundException();
        }

        return userMetadata.get().getQrCodeSubscriptionId() != null;
    }

    @Transactional(readOnly = true)
    public boolean hasSubscription(String username) {
        return hasQrCodeSubscription(username);
    }

    @Transactional(readOnly = true)
    public Object getQrCodeSubscription(String username) throws JsonProcessingException {
        Optional<UserMetadata> userMetadata = userMetadataRepository.findByUserUsername(username);

        if (userMetadata.isEmpty()) {
            throw new UserNotFoundException();
        }

        try {
            if (userMetadata.get().getQrCodeSubscriptionId() == null || userMetadata.get().getQrCodeSubscriptionId().isEmpty()) {
                return null;
            }

            Subscription subscription = Subscription.retrieve(userMetadata.get().getQrCodeSubscriptionId());
            ObjectMapper mapper = new ObjectMapper();
            JsonNode actualObj = mapper.readTree(subscription.toJson());
            return actualObj;
        } catch (StripeException stripeException) {
            throw new StripeRuntimeException(stripeException);
        }
    }

    @Transactional(readOnly = true)
    public List<Object> getUserSubscriptions(String username) throws JsonProcessingException {
        Optional<UserMetadata> userMetadata = userMetadataRepository.findByUserUsername(username);

        if (userMetadata.isEmpty()) {
            throw new UserNotFoundException();
        }

        Map<String, Object> params = new HashMap<>();
        params.put("customer", userMetadata.get().getCustomerID());

        try {
            List<Object> subscriptionsOut = new LinkedList<>();
            SubscriptionCollection subscriptions =
                    Subscription.list(params);
            for (Subscription subscription : subscriptions.autoPagingIterable()) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode actualObj = mapper.readTree(subscription.toJson());
                subscriptionsOut.add(actualObj);
            }

            return subscriptionsOut;
        } catch (StripeException stripeException) {
            throw new StripeRuntimeException(stripeException);
        }
    }

    @Transactional()
    public void createCustomer(User user) throws StripeException {
        Map<String, Object> params = new HashMap<>();
        params.put("name", user.getName() + ' ' + user.getSurname());
        params.put("email", user.getEmail());

        Customer customer = Customer.create(params);

        UserMetadata userMetadata = user.getUserMetadata();
        userMetadata.setCustomerID(customer.getId());
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public void updateCustomer(User user) throws StripeException {
        Customer customer = Customer.retrieve(user.getUserMetadata().getCustomerID());

        Map<String, Object> params = new HashMap<>();
        params.put("name", user.getName() + ' ' + user.getSurname());
        params.put("email", user.getEmail());
        customer.update(params);
    }

    @Transactional()
    public void deleteCustomer(User user) throws StripeException {
        Customer customer = Customer.retrieve(user.getUserMetadata().getCustomerID());

        UserMetadata userMetadata = user.getUserMetadata();
        userMetadata.setCustomerID(null);
        userMetadata.setQrCodeSubscriptionId(null);
        userRepository.save(user);

        customer.delete();
    }

    @Transactional(readOnly = true)
    public Map<String, Long>  getSpecificQrCodeUsage(String username, UUID qrCodeId) {
        Optional<UserMetadata> userMetadata = userMetadataRepository.findByUserUsername(username);

        if (userMetadata.isEmpty()) {
            throw new UserNotFoundException();
        }

        Map<String, Long> out = new HashMap<>();

        out.put("totalScans", qrCodeRuleUsageRepository
                .countByQrCodeId(qrCodeId));
        out.put("billPeriod", qrCodeRuleUsageRepository
                .countByQrCodeIdAndReportedInSubscriptionId(qrCodeId, userMetadata.get().getQrCodeSubscriptionId()) +
                qrCodeRuleUsageRepository
                        .countByQrCodeIdAndReportedInSubscriptionId(qrCodeId, null));

        return out;
    }

    @Transactional()
    public void cancelQrCodeSubscription(String username) {
        Optional<UserMetadata> userMetadata = userMetadataRepository.findByUserUsername(username);

        if (userMetadata.isEmpty()) {
            throw new UserNotFoundException();
        }

        String subscriptionId = userMetadata.get().getQrCodeSubscriptionId();
        userMetadata.get().setQrCodeSubscriptionId(null);
        userMetadata.get().setQrCodeSubscriptionStartCurrentPeriod(null);
        userMetadata.get().setQrCodeSubscriptionEndCurrentPeriod(null);
        userMetadata.get().setReportedQrCodes(null);
        userMetadata.get().setQrCodeSubscriptionActive(false);
        userMetadataRepository.save(userMetadata.get());
        cancelSubscription(subscriptionId);
    }

    private void cancelSubscription(String subscriptionId) {
        try {
            Subscription subscription = Subscription.retrieve(subscriptionId);
            if (subscription == null) {
                throw new NoSubscriptionException();
            }

            Map<String, Object> params = new HashMap<>();
            params.put("invoice_now", true);

            subscription.cancel(params);
        } catch (StripeException stripeException) {
            throw new StripeRuntimeException(stripeException);
        }
    }

    public void processWebhookEvents(String payload, String sigHeader) throws StripeException {
        Event event = Webhook.constructEvent(payload, sigHeader, webhookKey);

        // Deserialize the nested object inside the event
        EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();
        StripeObject stripeObject;
        if (dataObjectDeserializer.getObject().isPresent()) {
            stripeObject = dataObjectDeserializer.getObject().get();

            // Unhandled event type
            if ("checkout.session.completed".equals(event.getType())) {// Used to provision services after the trial has ended.
                Session session = (Session) stripeObject;

                Optional<UserMetadata> userMetadata = userMetadataRepository.findByCustomerID(session.getCustomer());

                if (userMetadata.isEmpty()) {
                    // TODO LOG ERROR TO ADMIN
                    throw new UserNotFoundException();
                }

                userMetadata.get().setQrCodeSubscriptionId(session.getSubscription());

                Subscription subscription = Subscription.retrieve(session.getSubscription());
                userMetadata.get().setQrCodeSubscriptionActive(subscription.getStatus().equals("active"));
                userMetadata.get().setQrCodeSubscriptionStartCurrentPeriod(subscription.getCurrentPeriodStart());
                userMetadata.get().setQrCodeSubscriptionEndCurrentPeriod(subscription.getCurrentPeriodEnd());
                userMetadataRepository.save(userMetadata.get());

            } else if ("invoice.payment_failed".equals(event.getType())) {
                Invoice invoice = (Invoice) stripeObject;

                Optional<UserMetadata> userMetadata = userMetadataRepository.findByCustomerID(invoice.getCustomer());

                if (userMetadata.isEmpty()) {
                    // TODO LOG ERROR TO ADMIN
                    throw new UserNotFoundException();
                }

                userMetadata.get().setQrCodeSubscriptionActive(false);
                userMetadataRepository.save(userMetadata.get());
            } else if ("invoice.paid".equals(event.getType())) {
                Invoice invoice = (Invoice) stripeObject;

                Optional<UserMetadata> userMetadata = userMetadataRepository.findByCustomerID(invoice.getCustomer());

                if (userMetadata.isEmpty()) {
                    // TODO LOG ERROR TO ADMIN
                    throw new UserNotFoundException();
                }

                userMetadata.get().setQrCodeSubscriptionActive(true);
                userMetadataRepository.save(userMetadata.get());
            }
        }
    }

    public Map<String, Long> getQrCodeSubscriptionUsage(String username) {
        Optional<UserMetadata> userMetadata = userMetadataRepository.findByUserUsername(username);

        if (userMetadata.isEmpty()) {
            throw new UserNotFoundException();
        }

        Map<String, Long> out = new HashMap<>();

        out.put("totalScans", qrCodeRuleUsageRepository
                .countByReportedInSubscriptionId(userMetadata.get().getQrCodeSubscriptionId()));
        out.put("uniqueQrCodeScans", qrCodeRuleUsageRepository
                .countSubscriptionUniqueQrCodeUsage(username, userMetadata.get().getQrCodeSubscriptionId()));

        return out;
    }

    public String createQrCodeCheckoutSession(String[] priceIds, String username) {
        Set<String> productIds;
        try {
            productIds = convertPricesToProductIds(priceIds);
        } catch (StripeException stripeException) {
            throw new StripeRuntimeException(stripeException);
        }

        if (productIds.size() != 2 &&
                !productIds.contains(qrcodeScansProduct) &&
                !productIds.contains(uniqueQrcodesProduct)) {
            throw new InvalidPricesForCheckout();
        }

        Optional<UserMetadata> userMetadata = userMetadataRepository.findByUserUsername(username);

        if (userMetadata.isEmpty()) {
            throw new UserNotFoundException();
        }

        if (userMetadata.get().isQrCodeSubscriptionActive()) {
            throw new AlreadySubscribedException();
        }

        return createCheckoutSessionExitingUser(priceIds, username);
    }

    public void updateUnmonitoredQrcodeUsage() {
        List<QRCodeRuleUsage> qrCodeRuleUsageList = qrCodeRuleUsageRepository.findByReportedUsage(false);

        for (QRCodeRuleUsage qrCodeRuleUsage : qrCodeRuleUsageList) {
            try {
                updateQrcodeUsage(qrCodeRuleUsage);
            } catch (StripeException stripeException) {
                // TODO LOG BILLING ERROR
            }
        }
    }

    @Transactional
    void updateQrcodeUsage(QRCodeRuleUsage qrCodeRuleUsage) throws StripeException {
        Optional<UserMetadata> userMetadata = userMetadataRepository.findByUserId(qrCodeRuleUsage.getQrCode().getGroup().getOwner().getId());

        if (userMetadata.isEmpty()) {
            // TODO REPORT BILLING ERROR TO ADMIN
            return;
        }

        Subscription subscription = Subscription.retrieve(userMetadata.get().getQrCodeSubscriptionId());

        if (subscription == null) {
            // TODO REPORT BILLING ERROR TO ADMIN
            return;
        }

        long eventTime = Timestamp.valueOf(qrCodeRuleUsage.getTimestamp()).getTime() / 1000;

        userMetadata.get().setQrCodeSubscriptionActive(subscription.getStatus().equals("active"));

        if (!subscription.getCurrentPeriodStart().equals(userMetadata.get().getQrCodeSubscriptionStartCurrentPeriod())) {
            userMetadata.get().setQrCodeSubscriptionStartCurrentPeriod(subscription.getCurrentPeriodStart());
            userMetadata.get().setQrCodeSubscriptionEndCurrentPeriod(subscription.getCurrentPeriodEnd());
            userMetadata.get().getReportedQrCodes().clear();

            userMetadataRepository.saveAndFlush(userMetadata.get());
        }

        SubscriptionItemCollection subscriptionItemCollection = subscription.getItems();
        for (SubscriptionItem subscriptionItem : subscriptionItemCollection.autoPagingIterable()) {
            if (subscriptionItem.getPrice().getProduct().equals(qrcodeScansProduct)) {
                Map<String, Object> params = new HashMap<>();
                params.put("quantity", 1);
                params.put("action", "increment");
                params.put("timestamp", eventTime);

                qrCodeRuleUsage.setReportedInSubscriptionId(subscription.getId());
                qrCodeRuleUsageRepository.save(qrCodeRuleUsage);

                UsageRecord.createOnSubscriptionItem(
                        subscriptionItem.getId(),
                        params,
                        null
                );
            }
            else if (subscriptionItem.getPrice().getProduct().equals(uniqueQrcodesProduct) &&
                    !userMetadata.get().getReportedQrCodes().contains(qrCodeRuleUsage.getQrCode().getId().toString()) &&
                    eventTime > subscription.getCurrentPeriodStart() &&
                    eventTime < subscription.getCurrentPeriodEnd()) {
                // has this qrCode already been recorded?
                Map<String, Object> params = new HashMap<>();
                params.put("quantity", 1);
                params.put("action", "increment");
                params.put("timestamp", eventTime);

                userMetadata.get().getReportedQrCodes().add(qrCodeRuleUsage.getQrCode().getId().toString());
                userMetadataRepository.saveAndFlush(userMetadata.get());

                UsageRecord.createOnSubscriptionItem(
                        subscriptionItem.getId(),
                        params,
                        null
                );
            }
        }

        qrCodeRuleUsage.setReportedUsage(true);
        qrCodeRuleUsageRepository.save(qrCodeRuleUsage);
    }

    @Transactional(readOnly = true)
    protected String createCheckoutSessionExitingUser(String[] priceIds, String username) {
        Optional<User> user = userRepository.findByUsername(username);

        if (user.isEmpty()) {
            throw new UserNotFoundException();
        }

        List<SessionCreateParams.LineItem> lineItems = new LinkedList<>();

        for (String priceId : priceIds) {
            lineItems.add(new SessionCreateParams.LineItem.Builder()
                    .setPrice(priceId)
                    .build());
        }

        SessionCreateParams sessionCreateParams = new SessionCreateParams.Builder()
                .setSuccessUrl(successUrl)
                .setCancelUrl(canceledUrl)
                .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
                .setMode(SessionCreateParams.Mode.SUBSCRIPTION)
                .setCustomer(user.get().getUserMetadata().getCustomerID())
                .addAllLineItem(lineItems)
                .build();

        try {
            return Session.create(sessionCreateParams).getId();
        } catch (StripeException stripeException) {
            throw new StripeRuntimeException(stripeException);
        }
    }

    private Set<String> convertPricesToProductIds(String[] priceIds) throws StripeException {
        Set<String> productIds = new HashSet<>();

        for (String priceId : priceIds) {
            Price price = Price.retrieve(priceId);
            productIds.add(price.getProduct());
        }

        return productIds;
    }
}
