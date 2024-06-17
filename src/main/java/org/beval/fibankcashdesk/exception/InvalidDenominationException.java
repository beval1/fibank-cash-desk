package org.beval.fibankcashdesk.exception;

import org.springframework.http.HttpStatus;

public class InvalidDenominationException extends ApiException
{
    public InvalidDenominationException()
    {
        super(HttpStatus.BAD_REQUEST, "Invalid denomination");
    }
}
