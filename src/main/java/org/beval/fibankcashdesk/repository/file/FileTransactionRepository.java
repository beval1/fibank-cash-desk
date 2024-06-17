package org.beval.fibankcashdesk.repository.file;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.beval.fibankcashdesk.model.CashOperationRequest;
import org.beval.fibankcashdesk.model.Transaction;
import org.beval.fibankcashdesk.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Repository
public class FileTransactionRepository implements TransactionRepository {

    @Value("${fibank.transactions.directory}")
    private String transactionsDirectory;

    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final AtomicInteger currentTransactionId = new AtomicInteger(0);

    private static final String TRANSACTIONS_FILE = "transactions.txt";
    private static final String[] HEADERS = {"transactionId", "idempotencyId", "cashier", "currency", "action", "denominations", "timestamp"};

    @PostConstruct
    public void init() throws IOException {
        initializeTransactionId();
    }

    private void initializeTransactionId() throws IOException {
        lock.writeLock().lock();
        try {
            final Path filePath = Paths.get(transactionsDirectory, TRANSACTIONS_FILE);
            if (Files.exists(filePath)) {
                try (final BufferedReader reader = Files.newBufferedReader(filePath)) {
                    final Iterable<CSVRecord> records = CSVFormat.DEFAULT.withHeader(HEADERS).parse(reader);
                    skipHeader(records);
                    records.forEach(record -> {
                        final int transactionId = Integer.parseInt(record.get("transactionId"));
                        currentTransactionId.set(Math.max(currentTransactionId.get(), transactionId));
                    });
                }
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void logTransaction(final Transaction transaction) {
        lock.writeLock().lock();
        try {
            final Path filePath = Paths.get(transactionsDirectory, TRANSACTIONS_FILE);
            Files.createDirectories(filePath.getParent());

            final boolean fileExists = Files.exists(filePath);
            final boolean fileEmpty = fileExists && Files.size(filePath) == 0;

            try (final BufferedWriter writer = Files.newBufferedWriter(filePath, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                 final CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT)) {
                if (!fileExists || fileEmpty) {
                    csvPrinter.printRecord((Object[]) HEADERS);
                }
                csvPrinter.printRecord(
                        transaction.getTransactionId(),
                        transaction.getIdempotencyId().toString(),
                        transaction.getCashier(),
                        transaction.getCurrency(),
                        transaction.getAction(),
                        formatDenominations(transaction.getDenominations()),
                        transaction.getTimestamp().toString()
                                      );
            }
        } catch (final IOException e) {
            throw new TransactionLoggingException("Failed to log transaction", e);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public Optional<Transaction> getLastTransactionForCashier(final String cashier) {
        lock.readLock().lock();
        try {
            return getAllTransactions().stream()
                                       .filter(transaction -> transaction.getCashier().equals(cashier))
                                       .max(Comparator.comparing(Transaction::getTimestamp));
        } finally {
            lock.readLock().unlock();
        }
    }

    private List<Transaction> getAllTransactions() {
        lock.readLock().lock();
        try {
            final List<Transaction> transactions = new ArrayList<>();
            final Path filePath = Paths.get(transactionsDirectory, TRANSACTIONS_FILE);
            if (Files.exists(filePath)) {
                try (final BufferedReader reader = Files.newBufferedReader(filePath)) {
                    final Iterable<CSVRecord> records = CSVFormat.DEFAULT.withHeader(HEADERS).parse(reader);
                    skipHeader(records);
                    transactions.addAll(StreamSupport.stream(records.spliterator(), false)
                                                     .map(this::parseTransaction)
                                                     .collect(Collectors.toList()));
                } catch (final IOException e) {
                    throw new TransactionReadingException("Failed to read transactions from file: " + filePath, e);
                }
            }
            return transactions;
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public Optional<Transaction> getTransactionById(final int transactionId) {
        lock.readLock().lock();
        try {
            return getAllTransactions().stream()
                                       .filter(transaction -> transaction.getTransactionId() == transactionId)
                                       .findFirst();
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public int generateNewTransactionId() {
        return currentTransactionId.incrementAndGet();
    }

    private void skipHeader(final Iterable<CSVRecord> records) {
        Iterator<CSVRecord> iterator = records.iterator();
        if (iterator.hasNext()) {
            iterator.next(); // Skip the header row
        }
    }

    private Transaction parseTransaction(final CSVRecord record) {
        final Transaction transaction = new Transaction();
        transaction.setTransactionId(Integer.parseInt(record.get("transactionId")));
        transaction.setIdempotencyId(UUID.fromString(record.get("idempotencyId")));
        transaction.setCashier(record.get("cashier"));
        transaction.setCurrency(record.get("currency"));
        transaction.setAction(CashOperationRequest.Action.valueOf(record.get("action")));
        transaction.setDenominations(parseDenominations(record.get("denominations")));
        transaction.setTimestamp(LocalDateTime.parse(record.get("timestamp")));
        return transaction;
    }

    private Map<String, Integer> parseDenominations(final String denominationsString) {
        return Arrays.stream(denominationsString.split(";"))
                     .map(pair -> pair.split("x"))
                     .filter(parts -> parts.length == 2)
                     .collect(Collectors.toMap(parts -> parts[0], parts -> Integer.parseInt(parts[1])));
    }

    private String formatDenominations(final Map<String, Integer> denominations) {
        return denominations.entrySet().stream()
                            .map(entry -> entry.getKey() + "x" + entry.getValue())
                            .collect(Collectors.joining(";"));
    }

    private static class TransactionLoggingException extends RuntimeException {
        public TransactionLoggingException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    private static class TransactionReadingException extends RuntimeException {
        public TransactionReadingException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
