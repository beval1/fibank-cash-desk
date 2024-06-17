package org.beval.fibankcashdesk.model;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

public class Transaction {
    private int transactionId;
    private UUID idempotencyId;
    private String cashier;
    private String currency;
    private CashOperationRequest.Action action;
    private Map<String, Integer> denominations;
    private LocalDateTime timestamp;

    public Transaction(final int transactionId,
                       final UUID idempotencyId,
                       final String cashier,
                       final String currency,
                       final CashOperationRequest.Action action,
                       final Map<String, Integer> denominations,
                       final LocalDateTime timestamp)
    {
        this.transactionId = transactionId;
        this.idempotencyId = idempotencyId;
        this.cashier = cashier;
        this.currency = currency;
        this.action = action;
        this.denominations = denominations;
        this.timestamp = timestamp;
    }

    public Transaction() {
    }

    public int getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(int transactionId) {
        this.transactionId = transactionId;
    }

    public UUID getIdempotencyId() {
        return idempotencyId;
    }

    public void setIdempotencyId(UUID idempotencyId) {
        this.idempotencyId = idempotencyId;
    }

    public String getCashier() {
        return cashier;
    }

    public void setCashier(String cashier) {
        this.cashier = cashier;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public CashOperationRequest.Action getAction() {
        return action;
    }

    public void setAction(CashOperationRequest.Action action) {
        this.action = action;
    }

    public Map<String, Integer> getDenominations() {
        return denominations;
    }

    public void setDenominations(Map<String, Integer> denominations) {
        this.denominations = denominations;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

}
