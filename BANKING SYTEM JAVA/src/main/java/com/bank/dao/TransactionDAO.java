package com.bank.dao;

import com.bank.model.Transaction;
import java.util.List;

public interface TransactionDAO {
    Transaction getTransactionById(int id);
    List<Transaction> getTransactionsByAccountId(int accountId);
    List<Transaction> getAllTransactions();
    boolean addTransaction(Transaction transaction);
    boolean deleteTransaction(int id);
}
