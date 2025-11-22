package com.bank.dao;

import com.bank.model.Account;
import java.util.List;

public interface AccountDAO {
    Account getAccountById(int id);
    List<Account> getAccountsByUserId(int userId);
    List<Account> getAllAccounts();
    boolean addAccount(Account account);
    boolean updateAccount(Account account);
    boolean deleteAccount(int id);
    boolean approveAccount(int id);
    boolean rejectAccount(int id);
    boolean closeAccount(int id);
}
