package org.beval.fibankcashdesk.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

public class Cashier
{
    @JsonProperty("cashierName")
    private String cashierName;
    @JsonProperty("currencies")
    private List<CurrencyBalance> currencies;
    @JsonProperty("lastTransactionId")
    private int lastTransactionId;

    // Getters and setters

    public String getCashierName() {
        return cashierName;
    }

    public void setCashierName(String cashierName) {
        this.cashierName = cashierName;
    }

    public List<CurrencyBalance> getCurrencies() {
        return currencies;
    }

    public void setCurrencies(List<CurrencyBalance> currencies) {
        this.currencies = currencies;
    }

    public int getLastTransactionId() {
        return lastTransactionId;
    }

    public void setLastTransactionId(int lastTransactionId) {
        this.lastTransactionId = lastTransactionId;
    }

    public static class CurrencyBalance
    {
        @JsonProperty("symbol")
        private String symbol;
        @JsonProperty("amount")
        private int amount;
        @JsonProperty("denominations")
        private Map<String, Integer> denominations;

        // Getters and setters

        public String getSymbol() {
            return symbol;
        }

        public void setSymbol(String symbol) {
            this.symbol = symbol;
        }

        public int getAmount() {
            return amount;
        }

        public void setAmount(int amount) {
            this.amount = amount;
        }

        public Map<String, Integer> getDenominations() {
            return denominations;
        }

        public void setDenominations(Map<String, Integer> denominations) {
            this.denominations = denominations;
        }
    }
}


