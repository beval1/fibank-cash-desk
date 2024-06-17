package org.beval.fibankcashdesk.cron;

import org.beval.fibankcashdesk.repository.CashierRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class CashierBalanceResetScheduler {

    private final CashierRepository cashierRepository;

    public CashierBalanceResetScheduler(CashierRepository cashierRepository) {
        this.cashierRepository = cashierRepository;
    }

    @Scheduled(cron = "0 0 0 * * ?") // Every day at 12:00 midnight
    public void resetCashierBalances() {
        final Map<String, Integer> fixedDenominationsBGN = new HashMap<>();
        fixedDenominationsBGN.put("10", 50);
        fixedDenominationsBGN.put("50", 10);
        final int fixedAmountBGN = 1000;

        final Map<String, Integer> fixedDenominationsEUR = new HashMap<>();
        fixedDenominationsEUR.put("10", 100);
        fixedDenominationsEUR.put("50", 20);
        final int fixedAmountEUR = 2000;

        cashierRepository.getCashiers().forEach(cashier -> {
            cashier.getCurrencies().forEach(currencyBalance -> {
                if (currencyBalance.getSymbol().equals("BGN")) {
                    currencyBalance.setAmount(fixedAmountBGN);
                    currencyBalance.setDenominations(fixedDenominationsBGN);
                } else if (currencyBalance.getSymbol().equals("EUR")) {
                    currencyBalance.setAmount(fixedAmountEUR);
                    currencyBalance.setDenominations(fixedDenominationsEUR);
                }
            });
            cashierRepository.updateCashier(cashier);
        });
    }
}
