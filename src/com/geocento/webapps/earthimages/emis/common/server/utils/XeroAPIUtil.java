package com.geocento.webapps.earthimages.emis.common.server.utils;

import com.metaaps.webapps.libraries.client.widget.util.ListUtil;
import com.xero.api.Config;
import com.xero.api.JsonConfig;
import com.xero.api.RsaSignerFactory;
import com.xero.api.XeroClient;
import com.xero.model.*;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.util.Calendar;
import java.util.List;

public class XeroAPIUtil {

    static private XeroClient client;

    static public XeroClient getClient() throws Exception {

        if(true) { //client == null) {
            Config config = JsonConfig.getInstance();
            config.setConsumerKey(Utils.getSettings().getXeroConsumerKey());
            config.setConsumerSecret(Utils.getSettings().getXeroConsumerSecret());
            config.setKeyStorePath("");
            config.setKeyStorePassword("");

            client = new XeroClient(config, new RsaSignerFactory(new FileInputStream(Utils.getSettings().getXeroPathToPrivateKey()),
                    Utils.getSettings().getXeroPrivateKeyPassword()));
        }

        return client;
    }

    public static Contact findContact(String emailAddress) throws Exception {
        XeroClient client = getClient();
        List<Contact> contactList = client.getContacts();
        return ListUtil.findValue(contactList, value -> {return value.getEmailAddress() != null && value.getEmailAddress().contentEquals(emailAddress);});
    }

    public static Contact createContact(String emailAddress, String fullName, String phoneNumber,
                                        String addressValue, String country, String organisation) throws Exception {
        Contact contact = new Contact();
        contact.setEmailAddress(emailAddress);
        contact.setIsCustomer(true);
        contact.setName(fullName);
        ArrayOfPhone phones = new ArrayOfPhone();
        Phone phone = new Phone();
        phone.setPhoneNumber(phoneNumber);
        phones.getPhone().add(phone);
        contact.setPhones(phones);
        ArrayOfAddress addresses = new ArrayOfAddress();
        Address address = new Address();
        address.setAddressLine1(addressValue);
        address.setCountry(country);
        contact.setAddresses(addresses);
        // save the contact
        return createContact(contact);
    }

    private static Contact createContact(Contact contact) throws Exception {
        XeroClient client = getClient();
        return client.createContact(ListUtil.toList(contact)).get(0);
    }

    public static String getContactLink(Contact contact) {
        return Utils.getSettings().getXeroContactLink().
                replace("$contact", contact.getContactID());
    }

    public static String getInvoiceLink(Invoice invoice) {
        return getInvoiceLink(invoice.getInvoiceID());
    }

    public static String getInvoiceLink(String invoiceId) {
        return Utils.getSettings().getXeroInvoiceLink().
                replace("$invoice", invoiceId);
    }

    public static Invoice getInvoice(String invoiceId) throws Exception {
        XeroClient client = getClient();
        return client.getInvoice(invoiceId);
    }

    public static ByteArrayInputStream getInvoicePDFLink(String invoiceId) throws Exception {
        XeroClient client = getClient();
        return client.getInvoicePdfContent(invoiceId);
    }

    public static Invoice createInvoice(Invoice invoice) throws Exception {
        XeroClient client = getClient();
        return client.createInvoices(ListUtil.toList(invoice)).get(0);
    }

    public static void payInvoice(Invoice invoice) throws Exception {
        XeroClient client = getClient();
        Payment payment = new Payment();
        payment.setInvoice(invoice);
        payment.setAmount(invoice.getAmountDue());
        // set payment account to the current bank account where the credit card payment has been made
        payment.setAccount(client.getAccount("3B1E08A7-E55C-423E-B465-8842A40FD8C7"));
        payment.setDate(Calendar.getInstance());
        payment.setPaymentType(PaymentType.ACCPAYPAYMENT);
        //payment.setAccount();
        client.createPayments(ListUtil.toList(payment));
    }
}
