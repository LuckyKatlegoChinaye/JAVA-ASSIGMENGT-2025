package com.bank.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Label;
import javafx.scene.control.Hyperlink;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.event.ActionEvent;

public class LoginController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;
    @FXML private Hyperlink registerLink;

    private com.bank.dao.UserDAO userDAO = new com.bank.dao.UserDAOImpl();
    private com.bank.dao.AccountDAO accountDAO = new com.bank.dao.AccountDAOImpl();
    private com.bank.dao.CustomerDAO customerDAO = new com.bank.dao.CustomerDAOImpl();

    @FXML
    public void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();
        errorLabel.setText("");
        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            errorLabel.setText("Please enter username and password.");
            return;
        }
        com.bank.model.User user = userDAO.getUserByUsername(username);
        if (user == null || !user.getPassword().equals(password)) {
            errorLabel.setText("Invalid username or password.");
            return;
        }
        if ("CUSTOMER".equals(user.getRole())) {
            java.util.List<com.bank.model.Account> accounts = accountDAO.getAccountsByUserId(user.getId());
            boolean approved = accounts.stream().anyMatch(a -> "APPROVED".equals(a.getStatus()));
            if (!approved) {
                errorLabel.setText("Account not approved by admin yet.");
                return;
            }
            try {
                Stage stage = (Stage) usernameField.getScene().getWindow();
                javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/customer_dashboard.fxml"));
                Parent root = loader.load();
                com.bank.controller.CustomerDashboardController controller = loader.getController();
                com.bank.model.Customer customer = customerDAO.getCustomerById(user.getId());
                if (customer != null) controller.setCurrentCustomer(customer);
                Scene scene = new Scene(root, 900, 600);
                try { scene.getStylesheets().add(getClass().getResource("/css/sidebar.css").toExternalForm()); } catch (Exception ex) {}
                stage.setScene(scene);
            } catch (Exception e) {
                e.printStackTrace();
                String msg = e.getMessage() == null ? "Failed to load customer dashboard." : "Failed to load customer dashboard: " + e.getMessage();
                errorLabel.setText(msg);
            }
        } else if ("ADMIN".equals(user.getRole())) {
            try {
                Stage stage = (Stage) usernameField.getScene().getWindow();
                Parent root = FXMLLoader.load(getClass().getResource("/fxml/admin_dashboard.fxml"));
                Scene scene = new Scene(root, 900, 600);
                try { scene.getStylesheets().add(getClass().getResource("/css/sidebar.css").toExternalForm()); } catch (Exception ex) {}
                stage.setScene(scene);
            } catch (Exception e) {
                e.printStackTrace();
                String msg = e.getMessage() == null ? "Failed to load admin dashboard." : "Failed to load admin dashboard: " + e.getMessage();
                errorLabel.setText(msg);
            }
        } else {
            errorLabel.setText("Unknown user role.");
        }
    }

    @FXML
    public void handleRegisterRedirect(ActionEvent event) {
        try {
            Stage stage = (Stage) ((Hyperlink) event.getSource()).getScene().getWindow();
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/register.fxml"));
            stage.setScene(new Scene(root, 900, 600));
        } catch (Exception e) {
            errorLabel.setText("Failed to load registration view.");
        }
    }
}
