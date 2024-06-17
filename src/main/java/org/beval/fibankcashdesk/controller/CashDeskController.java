package org.beval.fibankcashdesk.controller;

import org.beval.fibankcashdesk.model.CashOperationRequest;
import org.beval.fibankcashdesk.model.Cashier;
import org.beval.fibankcashdesk.model.ServerResponse;
import org.beval.fibankcashdesk.service.CashDeskService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RequestMapping("/api/v1")
@RestController
public class CashDeskController
{
    private final CashDeskService cashDeskService;

    public CashDeskController(final CashDeskService cashDeskService)
    {
        this.cashDeskService = cashDeskService;
    }

    @PreAuthorize("#apiKey == @fibankConfig.getApiKey()")
    @PostMapping("/cash-operation")
    public ResponseEntity<ServerResponse<Object>> handleCashOperation(
            @RequestHeader("FIB-X-AUTH") String apiKey,
            @RequestParam String cashier,
            @RequestBody @Valid CashOperationRequest request)
    {
        cashDeskService.handleCashOperation(cashier, request.getCurrency().toString(), request.getDenominations(),
                                            request.getIdempotencyId(), request.getAction());
        return ResponseEntity.ok(new ServerResponse(String.format("%s operation completed successfully!", request
                .getAction().toString()), null, HttpStatus.OK));
    }

    @PreAuthorize("#apiKey == @fibankConfig.getApiKey()")
    @GetMapping("/cash-balance")
    public ResponseEntity<ServerResponse<List<Cashier.CurrencyBalance>>> getCashBalance(
            @RequestHeader("FIB-X-AUTH") String apiKey,
            @RequestParam String cashier)
    {
        List<Cashier.CurrencyBalance> balance = cashDeskService.getBalanceForCashier(cashier);
        return ResponseEntity.ok(new ServerResponse<>("Fetched cashier balance successfully!", balance, HttpStatus.OK));
    }
}
