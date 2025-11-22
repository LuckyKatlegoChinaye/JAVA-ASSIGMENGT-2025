package com.bank.dao;

import com.bank.model.Customer;
import java.util.List;

public interface CustomerDAO {
    Customer getCustomerById(int id);
    List<Customer> getAllCustomers();
    boolean addCustomer(Customer customer);
    boolean updateCustomer(Customer customer);
    boolean deleteCustomer(int id);
}
