package org.beval.fibankcashdesk.exception;

import org.beval.fibankcashdesk.model.ServerResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler
{
    @ExceptionHandler({ApiException.class})
    public ResponseEntity<Object> handleCustomExceptions(ApiException ex)
    {
        return ResponseEntity.status(ex.getStatus())
                             .body(new ServerResponse<>(ex.getMessage(), null, ex.getStatus()));
    }
}
