package com.bank.dao;

import com.bank.model.Admin;
import java.util.List;

public interface AdminDAO {
    Admin getAdminById(int id);
    List<Admin> getAllAdmins();
    boolean addAdmin(Admin admin);
    boolean updateAdmin(Admin admin);
    boolean deleteAdmin(int id);
}
