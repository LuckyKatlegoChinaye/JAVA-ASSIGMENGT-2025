package com.bank.controller;
import javafx.scene.control.Hyperlink;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Label;

public class RegisterController {
    @FXML private TextField fullNameField;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private TextField emailField;
    @FXML private javafx.scene.control.ComboBox<String> accountTypeBox;
    @FXML private Label errorLabel;
    @FXML private Hyperlink loginLink;

    private com.bank.dao.CustomerDAO customerDAO = new com.bank.dao.CustomerDAOImpl();
    private com.bank.dao.AccountDAO accountDAO = new com.bank.dao.AccountDAOImpl();

    @FXML
    public void initialize() {
        if (accountTypeBox != null) {
            accountTypeBox.getItems().addAll("SAVINGS", "INVESTMENT", "CHEQUE");
        }
    }

    @FXML
    public void handleRegister(javafx.event.ActionEvent event) {
        String fullName = fullNameField.getText();
        String username = usernameField.getText();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        String email = emailField.getText();
        errorLabel.setText("");
        if (fullName == null || fullName.isEmpty() || username == null || username.isEmpty() || password == null || password.isEmpty() || confirmPassword == null || confirmPassword.isEmpty() || email == null || email.isEmpty()) {
            errorLabel.setText("All fields are required.");
            return;
        }
        if (!password.equals(confirmPassword)) {
            errorLabel.setText("Passwords do not match.");
            return;
        }
        if (customerDAO.getAllCustomers().stream().anyMatch(c -> c.getUsername().equals(username))) {
            errorLabel.setText("Username already exists.");
            return;
        }
        com.bank.model.Customer customer = new com.bank.model.Customer(0, username, password, fullName, email);
        boolean added = customerDAO.addCustomer(customer);
        if (!added) {
            errorLabel.setText("Registration failed. Try again.");
            return;
        }
        String acctType = accountTypeBox == null ? "SAVINGS" : accountTypeBox.getValue();
        if (acctType == null || acctType.isEmpty()) acctType = "SAVINGS";
        com.bank.model.Account account;
        switch (acctType) {
            case "INVESTMENT":
                account = new com.bank.model.InvestmentAccount(0, customer.getId(), 0.0, "PENDING");
                break;
            case "CHEQUE":
                account = new com.bank.model.ChequeAccount(0, customer.getId(), 0.0, "PENDING");
                break;
            default:
                account = new com.bank.model.SavingsAccount(0, customer.getId(), 0.0, "PENDING");
        }
        boolean accAdded = accountDAO.addAccount(account);
        if (!accAdded) {
            errorLabel.setText("Account creation failed. Try again.");
            return;
        }
        errorLabel.setText("Registration successful! Await admin approval.");
        // Navigate to login view
        try {
            javafx.stage.Stage stage = (javafx.stage.Stage) ((javafx.scene.control.Button) event.getSource()).getScene().getWindow();
            javafx.scene.Parent root = javafx.fxml.FXMLLoader.load(getClass().getResource("/fxml/login.fxml"));
            stage.setScene(new javafx.scene.Scene(root, 900, 600));
        } catch (Exception e) {
            errorLabel.setText("Failed to load login view.");
        }
    }

    @FXML
    public void handleLoginRedirect(javafx.event.ActionEvent event) {
        try {
            javafx.stage.Stage stage = (javafx.stage.Stage) ((javafx.scene.control.Hyperlink) event.getSource()).getScene().getWindow();
            javafx.scene.Parent root = javafx.fxml.FXMLLoader.load(getClass().getResource("/fxml/login.fxml"));
            stage.setScene(new javafx.scene.Scene(root, 900, 600));
        } catch (Exception e) {
            errorLabel.setText("Failed to load login view.");
        }
    }
}
