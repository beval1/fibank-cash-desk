package org.beval.fibankcashdesk.exception;

import org.springframework.http.HttpStatus;

//General Api Exception
public class ApiException extends RuntimeException{
    private final HttpStatus status;

    public ApiException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }

}
