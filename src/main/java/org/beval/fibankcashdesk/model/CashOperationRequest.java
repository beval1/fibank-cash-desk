package org.beval.fibankcashdesk.model;

import javax.validation.constraints.NotNull;
import java.util.Map;
import java.util.UUID;

public class CashOperationRequest {

    public enum Action {
        DEPOSIT, WITHDRAW
    }

    public enum Currency {
        BGN, EUR
    }

    @NotNull
    private UUID idempotencyId; // Unique ID for idempotency

    @NotNull
    private Action action;

    @NotNull
    private Currency currency;

    @NotNull
    private Map<String, Integer> denominations;


    public UUID getIdempotencyId() {
        return idempotencyId;
    }

    public void setIdempotencyId(UUID idempotencyId) {
        this.idempotencyId = idempotencyId;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public Map<String, Integer> getDenominations() {
        return denominations;
    }

    public void setDenominations(Map<String, Integer> denominations) {
        this.denominations = denominations;
    }
}
