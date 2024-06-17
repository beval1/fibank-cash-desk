package org.beval.fibankcashdesk.exception;

import org.springframework.http.HttpStatus;

public class CurrencyNotFoundException extends ApiException
{
    public CurrencyNotFoundException()
    {
        super(HttpStatus.NOT_FOUND, "Currency not found");
    }
}
