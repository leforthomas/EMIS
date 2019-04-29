package com.geocento.webapps.earthimages.emis.common.server.utils;

import com.geocento.webapps.earthimages.emis.common.server.domain.Credit;
import com.geocento.webapps.earthimages.emis.common.server.domain.Transaction;
import com.geocento.webapps.earthimages.emis.common.server.domain.User;
import com.geocento.webapps.earthimages.emis.common.share.EIException;
import com.geocento.webapps.earthimages.emis.common.share.entities.Price;
import com.geocento.webapps.earthimages.emis.common.share.entities.TRANSACTION_TYPE;
import com.geocento.webapps.earthimages.emis.application.share.PaymentTransactionDTO;
import com.metaaps.webapps.libraries.client.widget.util.StringUtils;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TransactionsHelper {

    public static Transaction addUserTransaction(EntityManager em, User user, TRANSACTION_TYPE transactionType, double amount, String currency, String comment, Date date) throws EIException {
        Credit credit = user.getCredit();
        if(credit.getCurrency() != null && !credit.getCurrency().contentEquals(currency)) {
            throw new EIException("Currency needs to be the same");
        }
        Transaction transaction = new Transaction();
        transaction.setTransactionType(transactionType);
        transaction.setAmount(amount);
        transaction.setCurrency(currency);
        transaction.setComment(comment);
        transaction.setDate(date);
        transaction.setCredit(credit);
        em.persist(transaction);
        List<Transaction> transactions = credit.getTransactions();
        if(transactions == null) {
            transactions = new ArrayList<Transaction>();
            credit.setTransactions(transactions);
        }
        transactions.add(transaction);

        // update credit amount
        credit.setCurrent(credit.getCurrent() + amount);
        credit.setCurrency(currency);

        return transaction;
    }

    public static PaymentTransactionDTO convertTransaction(Transaction transaction) {
        PaymentTransactionDTO paymentTransactionDTO = new PaymentTransactionDTO();
        paymentTransactionDTO.setId(transaction.getId());
        paymentTransactionDTO.setType(transaction.getTransactionType());
        paymentTransactionDTO.setOrderId(transaction.getEventOrder() == null ? null : transaction.getEventOrder().getId());
        paymentTransactionDTO.setDescription(transaction.getComment());
        paymentTransactionDTO.setAmount(new Price(transaction.getAmount(), transaction.getCurrency()));
        if(!StringUtils.isEmpty(transaction.getInvoiceId())) {
            paymentTransactionDTO.setInvoiceUrl("./api/document/download/invoice/" + transaction.getId());
        }
        paymentTransactionDTO.setCreatedOn(transaction.getDate());
        return paymentTransactionDTO;
    }
}
