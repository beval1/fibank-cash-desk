package org.beval.fibankcashdesk.repository.file;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.beval.fibankcashdesk.model.Cashier;
import org.beval.fibankcashdesk.repository.CashierRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Repository
public class FileCashierRepository implements CashierRepository
{

    private final ObjectMapper objectMapper;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    @Value("${fibank.cashiers.file}")
    private String balancesFilePath;

    public FileCashierRepository(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public List<Cashier> getCashiers()  {
        lock.readLock().lock();
        try {
            File file = new File(balancesFilePath);
            if (!file.exists()) {
                throw new RuntimeException("Cashiers file does not exist");
            }
            return Arrays.asList(objectMapper.readValue(file, Cashier[].class));
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public void updateCashier(final Cashier cashier)
    {
        lock.writeLock().lock();
        try {
            List<Cashier> cashiers = getCashiers();
            List<Cashier> updatedCashiers = new ArrayList<>();
            cashiers.stream()
                    .filter(balance -> balance.getCashierName().equals(cashier.getCashierName()))
                    .findFirst()
                    .ifPresent(balance -> updatedCashiers.add(cashier));
            cashiers.stream()
                    .filter(balance -> !balance.getCashierName().equals(cashier.getCashierName()))
                    .forEach(updatedCashiers::add);
            updateCashiers(updatedCashiers);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public Optional<Cashier> getCashierByName(String cashier) {
        lock.readLock().lock();
        try {
            return getCashiers().stream()
                                .filter(balance -> balance.getCashierName().equals(cashier))
                                .findFirst();
        } finally {
            lock.readLock().unlock();
        }
    }

    private void updateCashiers(List<Cashier> cashiers) {
        lock.writeLock().lock();
        try {
            objectMapper.writeValue(new File(balancesFilePath), cashiers);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            lock.writeLock().unlock();
        }
    }
}
