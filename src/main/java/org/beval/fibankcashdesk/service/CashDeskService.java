package org.beval.fibankcashdesk.service;

import org.beval.fibankcashdesk.exception.CashierNotFoundException;
import org.beval.fibankcashdesk.exception.CurrencyNotFoundException;
import org.beval.fibankcashdesk.exception.InvalidDenominationException;
import org.beval.fibankcashdesk.model.CashOperationRequest;
import org.beval.fibankcashdesk.model.Cashier;
import org.beval.fibankcashdesk.model.Transaction;
import org.beval.fibankcashdesk.repository.CashierRepository;
import org.beval.fibankcashdesk.repository.TransactionRepository;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.IntStream;

@Service
public class CashDeskService
{

    private final CashierRepository cashierRepository;
    private final TransactionRepository transactionRepository;
    private final Logger logger = org.slf4j.LoggerFactory.getLogger(CashDeskService.class);

    public CashDeskService(final CashierRepository cashierRepository, final TransactionRepository transactionRepository)
    {
        this.cashierRepository = cashierRepository;
        this.transactionRepository = transactionRepository;
    }

    @PostConstruct
    public void init()
    {
        try
        {
            recoverIfNecessary();
        }
        catch (final IOException e)
        {
            throw new RuntimeException("Failed to recover transactions", e);
        }
    }

    private void recoverIfNecessary() throws IOException
    {
        cashierRepository.getCashiers().forEach(this::recoverCashier);
    }

    private void recoverCashier(final Cashier cashier)
    {
        final String cashierName = cashier.getCashierName();
        final Optional<Transaction> lastTransactionOpt =
                transactionRepository.getLastTransactionForCashier(cashierName);

        lastTransactionOpt.ifPresent(lastTransaction -> {
            final int lastProcessedTransactionId = cashier.getLastTransactionId();
            final int lastTransactionId = lastTransaction.getTransactionId();

            IntStream.rangeClosed(lastProcessedTransactionId + 1, lastTransactionId)
                     .forEach(transactionId -> recoverTransactionByIdAndCashierName(transactionId, cashierName));
        });
    }

    private void recoverTransactionByIdAndCashierName(final int transactionId, final String cashierName)
    {
        final Optional<Transaction> transactionOpt = transactionRepository.getTransactionById(transactionId);
        logger.info("Recovering transaction with id: {}", transactionId);
        transactionOpt.ifPresent(transaction -> {
            processTransaction(transaction, cashierName);
        });
    }

    public void handleCashOperation(final String cashierName,
                                    final String currency,
                                    final Map<String, Integer> denominations,
                                    final UUID idempotencyId,
                                    final CashOperationRequest.Action action)
    {
        if (action == CashOperationRequest.Action.WITHDRAW)
        {
            handleTransaction(cashierName, currency, denominations, idempotencyId,
                              CashOperationRequest.Action.WITHDRAW);
        }
        else
        {
            handleTransaction(cashierName, currency, denominations, idempotencyId, CashOperationRequest.Action.DEPOSIT);
        }
    }


    private void handleTransaction(final String cashierName,
                                   final String currency,
                                   final Map<String, Integer> denominations,
                                   final UUID idempotencyId,
                                   final CashOperationRequest.Action action)
    {
        if (isIdempotencyIdUsed(cashierName, idempotencyId))
        {
            return; // Transaction already applied
        }

        final Cashier cashier = getCashierByName(cashierName);
        final Cashier.CurrencyBalance currencyBalance = getCurrencyBalance(cashier, currency);

        if (action == CashOperationRequest.Action.WITHDRAW && areDenominationsInvalid(currencyBalance, denominations))
        {
            throw new InvalidDenominationException();
        }

        final int transactionId = transactionRepository.generateNewTransactionId();
        logTransaction(transactionId, idempotencyId, cashierName, currency, action, denominations);
        processTransaction(new Transaction(transactionId, idempotencyId, cashierName, currency, action, denominations
                , LocalDateTime.now()), cashierName);
    }

    private Cashier getCashierByName(final String cashierName)
    {
        return cashierRepository.getCashiers().stream()
                                .filter(c -> c.getCashierName().equals(cashierName))
                                .findFirst()
                                .orElseThrow(CashierNotFoundException::new);
    }

    private Cashier.CurrencyBalance getCurrencyBalance(final Cashier cashier, final String currency)
    {
        return cashier.getCurrencies().stream()
                      .filter(curr -> curr.getSymbol().equals(currency))
                      .findFirst()
                      .orElseThrow(CurrencyNotFoundException::new);
    }

    private void processTransaction(final Transaction transaction, final String cashierName)
    {
        final Cashier cashier = getCashierByName(cashierName);
        final Cashier.CurrencyBalance currencyBalance = getCurrencyBalance(cashier, transaction.getCurrency());

        if (transaction.getAction() == CashOperationRequest.Action.WITHDRAW)
        {
            updateDenominations(currencyBalance, transaction.getDenominations(), false);
        }
        else if (transaction.getAction() == CashOperationRequest.Action.DEPOSIT)
        {
            updateDenominations(currencyBalance, transaction.getDenominations(), true);
        }
        updateAmount(currencyBalance);
        cashier.setLastTransactionId(transaction.getTransactionId());
        cashierRepository.updateCashier(cashier);
    }

    private boolean areDenominationsInvalid(final Cashier.CurrencyBalance currencyBalance,
                                            final Map<String, Integer> denominations)
    {
        return denominations.entrySet().stream()
                            .anyMatch(entry -> currencyBalance.getDenominations()
                                                              .getOrDefault(entry.getKey(), 0) < entry.getValue());
    }

    private void updateDenominations(final Cashier.CurrencyBalance currencyBalance,
                                     final Map<String, Integer> denominations,
                                     final boolean isDeposit)
    {
        int multiplier = isDeposit
                         ? 1
                         : -1;
        denominations.forEach((denomination, count) ->
                                      currencyBalance.getDenominations()
                                                     .merge(denomination, count * multiplier, Integer::sum));
    }

    private void updateAmount(final Cashier.CurrencyBalance currencyBalance)
    {
        currencyBalance.setAmount(currencyBalance.getDenominations().entrySet().stream()
                                                 .mapToInt(entry -> Integer.parseInt(entry.getKey()) * entry.getValue())
                                                 .sum());
    }

    public List<Cashier.CurrencyBalance> getBalanceForCashier(final String cashierName)
    {
        Cashier cashier = cashierRepository.getCashierByName(cashierName).orElseThrow(CashierNotFoundException::new);
        return cashier.getCurrencies();
    }

    private boolean isIdempotencyIdUsed(final String cashierName, final UUID idempotencyId)
    {
        return cashierRepository.getCashierByName(cashierName)
                                .flatMap(balance -> transactionRepository.getLastTransactionForCashier(cashierName))
                                .map(transaction -> transaction.getIdempotencyId().equals(idempotencyId))
                                .orElse(false);
    }

    private void logTransaction(final int transactionId,
                                final UUID idempotencyId,
                                final String cashierName,
                                final String currency,
                                final CashOperationRequest.Action action,
                                final Map<String, Integer> denominations)
    {
        final Transaction transaction = new Transaction();
        transaction.setTransactionId(transactionId);
        transaction.setIdempotencyId(idempotencyId);
        transaction.setCashier(cashierName);
        transaction.setCurrency(currency);
        transaction.setAction(action);
        transaction.setDenominations(denominations);
        transaction.setTimestamp(LocalDateTime.now());
        transactionRepository.logTransaction(transaction);
    }
}
