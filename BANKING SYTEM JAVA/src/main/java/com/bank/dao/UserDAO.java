package com.bank.dao;

import com.bank.model.User;
import java.util.List;

public interface UserDAO {
    User getUserById(int id);
    User getUserByUsername(String username);
    List<User> getAllUsers();
    boolean addUser(User user);
    boolean updateUser(User user);
    boolean deleteUser(int id);
}
