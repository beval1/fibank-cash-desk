package org.beval.fibankcashdesk.exception;

import org.springframework.http.HttpStatus;

public class CashierNotFoundException extends ApiException
{
    public CashierNotFoundException()
    {
        super(HttpStatus.NOT_FOUND, "Cashier not found");
    }
}
