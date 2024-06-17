package org.beval.fibankcashdesk.repository;

import org.beval.fibankcashdesk.model.Transaction;

import java.io.IOException;
import java.util.Optional;

public interface TransactionRepository {

    void logTransaction(Transaction transaction);

    Optional<Transaction> getLastTransactionForCashier(String cashier);

    int generateNewTransactionId();

    Optional<Transaction> getTransactionById(int transactionId);
}

