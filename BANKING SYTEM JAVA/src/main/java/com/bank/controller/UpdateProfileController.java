package com.bank.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;

public class UpdateProfileController {
    @FXML private TextField fullNameField;
    @FXML private TextField emailField;
    @FXML private PasswordField oldPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    private com.bank.model.Customer currentCustomer;
    private com.bank.dao.CustomerDAO customerDAO = new com.bank.dao.CustomerDAOImpl();

    public void setCustomer(com.bank.model.Customer customer) {
        this.currentCustomer = customer;
        fullNameField.setText(customer.getName());
        emailField.setText(customer.getEmail());
    }

    @FXML
    public void handleSave(javafx.event.ActionEvent event) {
        if (currentCustomer == null) return;
        String name = fullNameField.getText();
        String email = emailField.getText();
        String oldPassword = oldPasswordField.getText();
        String newPassword = newPasswordField.getText();
        if (name == null || name.isEmpty() || email == null || email.isEmpty()) return;
        if (oldPassword != null && !oldPassword.isEmpty() && newPassword != null && !newPassword.isEmpty()) {
            if (!currentCustomer.getPassword().equals(oldPassword)) {
                // TODO: Show error - current password incorrect
                return;
            }
            currentCustomer.setPassword(newPassword);
        }
        currentCustomer.setName(name);
        currentCustomer.setEmail(email);
        customerDAO.updateCustomer(currentCustomer);
        // Show confirmation and either close modal or navigate to dashboard if standalone
        try {
            javafx.stage.Stage stage = (javafx.stage.Stage) ((javafx.scene.control.Button) event.getSource()).getScene().getWindow();
            if (stage.getModality() == javafx.stage.Modality.APPLICATION_MODAL) {
                // If this was a modal, simply close it (caller will refresh)
                stage.close();
                return;
            }
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/customer_dashboard.fxml"));
            javafx.scene.Parent root = loader.load();
            com.bank.controller.CustomerDashboardController ctrl = loader.getController();
            if (currentCustomer != null) ctrl.setCurrentCustomer(currentCustomer);
            javafx.scene.Scene sc = new javafx.scene.Scene(root, 900, 600);
            try { sc.getStylesheets().add(getClass().getResource("/css/sidebar.css").toExternalForm()); } catch (Exception ex) {}
            stage.setScene(sc);
        } catch (Exception e) {
            // Optionally log error
        }
    }

    @FXML
    public void handleCancel(javafx.event.ActionEvent event) {
        try {
            javafx.stage.Stage stage = (javafx.stage.Stage) ((javafx.scene.control.Button) event.getSource()).getScene().getWindow();
            if (stage.getModality() == javafx.stage.Modality.APPLICATION_MODAL) {
                stage.close();
                return;
            }
            javafx.scene.Parent root = javafx.fxml.FXMLLoader.load(getClass().getResource("/fxml/customer_dashboard.fxml"));
            javafx.scene.Scene sc = new javafx.scene.Scene(root, 900, 600);
            try { sc.getStylesheets().add(getClass().getResource("/css/sidebar.css").toExternalForm()); } catch (Exception ex) {}
            stage.setScene(sc);
        } catch (Exception e) {
            // Optionally log error
        }
    }
}
