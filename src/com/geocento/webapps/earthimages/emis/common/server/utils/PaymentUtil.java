package com.geocento.webapps.earthimages.emis.common.server.utils;

import com.braintreegateway.*;
import com.braintreegateway.exceptions.NotFoundException;
import com.geocento.webapps.earthimages.emis.common.server.domain.TransactionToken;
import com.geocento.webapps.earthimages.emis.common.share.EIException;
import com.geocento.webapps.earthimages.emis.common.share.entities.Settings;
import com.geocento.webapps.earthimages.emis.application.share.CreditCardToken;
import com.metaaps.webapps.libraries.client.widget.util.ListUtil;
import org.apache.commons.collections4.IteratorUtils;
import org.apache.log4j.Logger;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;

/**
 * Created by thomas on 02/07/2014.
 */
public class PaymentUtil {

    private static HashMap<String, String> currencyAccounts = new HashMap<String, String>();
    static {
        currencyAccounts.put("EUR", "GeoCento_EUR");
        currencyAccounts.put("GBP", "GeoCento_GBP");
        currencyAccounts.put("USD", "GeoCento_USD");
    };

    private static PaymentUtil instance;

    private BraintreeGateway gateway;

    private final Logger logger;

    protected PaymentUtil() {

        // create the logger
        logger = Logger.getLogger(PaymentUtil.class);
        logger.info("Starting payment service");

    }

    static public PaymentUtil getInstance() {
        if(instance == null) {
            instance = new PaymentUtil();
        }
        instance.loadGateway();
        return instance;
    }

    private void loadGateway() {
        Settings applicationSettings = Utils.getSettings();
        gateway = new BraintreeGateway(
                        applicationSettings.isBraintreeSandbox() ? Environment.SANDBOX : Environment.PRODUCTION,
                        applicationSettings.getBraintreeMerchantId(),
                        applicationSettings.getBraintreePublicKey(),
                        applicationSettings.getBraintreePrivateKey()
                );
    }

    public boolean checkCustomerRegistered(String userName) {
        try {
            Customer customer = gateway.customer().find(userName);
            return customer != null;
        } catch (NotFoundException e) {
            return false;
        }
    }

    public void registerCustomer(String userName, String firstName, String lastName, String company, String email) throws EIException {
        CustomerRequest request = new CustomerRequest();
        request.id(userName);
        if(firstName != null) {
            request.firstName(firstName);
        }
        if(lastName != null) {
            request.lastName(lastName);
        }
        if(company != null) {
            request.company(company);
        }
        Result<Customer> result = gateway.customer().create(request);
        if(!result.isSuccess()) {
            throw new EIException(result.getMessage());
        }
    }

    public String makeCreditCardPayment(String userName, double amount, String currency, String number, String cvv, String month, String year, String orderReference, boolean storeCard) throws EIException {
        return makeCreditCardPayment(userName, amount, currency, null, number, cvv, month, year, orderReference, storeCard);
    }

    public String makeCreditCardPayment(String userName, double amount, String currency, String token, String cvv, String orderReference) throws EIException {
        return makeCreditCardPayment(userName, amount, currency, token, null, cvv, null, null, orderReference, false);
    }

    private String makeCreditCardPayment(String userName, double amount, String currency, String token, String number, String cvv, String month, String year, String orderReference, boolean storeCard) throws EIException {
        String account = currencyAccounts.get(currency);
        if(account == null) {
            throw new EIException("Currency " + currency + " not supported");
        }
        TransactionRequest request = new TransactionRequest()
                .amount(new BigDecimal(amount).setScale(2, BigDecimal.ROUND_CEILING))
                .merchantAccountId(account);
        if(userName != null) {
            request.customerId(userName);
        }
        if(orderReference != null) {
            // defined in the API custom fields
            request.customField("orderreference", orderReference);
        }
        if(token != null) {
            request.paymentMethodToken(token);
            request.creditCard()
                    .cvv(cvv)
                    .done();
        } else {
            request.creditCard()
                    .number(number)
                    .cvv(cvv)
                    .expirationMonth(month)
                    .expirationYear(year)
                    .done();
        }
        request.options()
            .submitForSettlement(true)
            .storeInVaultOnSuccess(storeCard)
        .done();

        Result<Transaction> result = gateway.transaction().sale(request);

        if (result.isSuccess()) {
            Transaction transaction = result.getTarget();
            logger.info("Success!: " + transaction.getId());
        } else if (result.getTransaction() != null) {
            logger.info("Message: " + result.getMessage());
            Transaction transaction = result.getTransaction();
            logger.error("Error processing transaction:");
            logger.error("  Status: " + transaction.getStatus());
            logger.error("  Code: " + transaction.getProcessorResponseCode());
            logger.error("  Text: " + transaction.getProcessorResponseText());
            throw new EIException("Error processing transaction: " + transaction.getProcessorResponseText());
        } else {
            logger.error("Message: " + result.getMessage());
            for (ValidationError error : result.getErrors().getAllDeepValidationErrors()) {
                logger.error("Attribute: " + error.getAttribute());
                logger.error("  Code: " + error.getCode());
                logger.error("  Message: " + error.getMessage());
            }
            throw new EIException("Error processing transaction: " + result.getMessage());
        }

        return result.getTarget().getId();
    }

    public List<CreditCardToken> getUserCreditCards(String userName) throws EIException {
        try {
            Customer customer = gateway.customer().find(userName);
            return ListUtil.mutate(customer.getCreditCards(), new ListUtil.Mutate<CreditCard, CreditCardToken>() {
                @Override
                public CreditCardToken mutate(CreditCard creditCard) {
                    return new CreditCardToken(creditCard.getToken(), creditCard.getCardType(), creditCard.getImageUrl(), creditCard.getMaskedNumber());
                }
            });
        } catch(Exception e) {
            throw new EIException("Could not retrieve user data");
        }
    }

    public List<TransactionToken> getUserTransactions(String userName, int start, int pageSize) throws EIException {
        try {
            TransactionSearchRequest request = new TransactionSearchRequest()
                    .customerId().is(userName);

            ResourceCollection<Transaction> collection = gateway.transaction().search(request);
            return ListUtil.mutate(IteratorUtils.toList(collection.iterator()), new ListUtil.Mutate<Transaction, TransactionToken>() {
                @Override
                public TransactionToken mutate(Transaction transaction) {
                    return new TransactionToken(transaction.getId(), transaction.getAmount().doubleValue(), transaction.getCreatedAt().getTime(), transaction.getCurrencyIsoCode(), transaction.getStatus().toString());
                }
            });
        } catch(Exception e) {
            throw new EIException("Could not retrieve user data");
        }
    }
}
