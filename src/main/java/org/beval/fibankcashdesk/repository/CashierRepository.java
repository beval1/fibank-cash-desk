package org.beval.fibankcashdesk.repository;

import org.beval.fibankcashdesk.model.Cashier;

import java.util.List;
import java.util.Optional;

public interface CashierRepository
{
    List<Cashier> getCashiers();

    void updateCashier(Cashier updateCashiers);

    Optional<Cashier> getCashierByName(String cashierName);

}
