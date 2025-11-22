package com.bank.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Button;

public class OpenAccountController {
    @FXML private ComboBox<String> accountTypeBox;
    @FXML private TextField initialDepositField;
    @FXML private Button createAccountButton;
    @FXML private Button cancelButton;

    private com.bank.dao.AccountDAO accountDAO = new com.bank.dao.AccountDAOImpl();
    private com.bank.model.Customer currentCustomer;
    // When opened by admin, targetUserId is used and adminMode=true
    private Integer targetUserId = null;
    private boolean adminMode = false;

    public void setCustomer(com.bank.model.Customer customer) {
        this.currentCustomer = customer;
    }

    public void setTargetUserId(int userId) {
        this.targetUserId = userId;
    }

    public void setAdminMode(boolean adminMode) {
        this.adminMode = adminMode;
    }

    @FXML
    public void initialize() {
        accountTypeBox.getItems().addAll("SAVINGS", "INVESTMENT", "CHEQUE");
    }

    @FXML
    public void handleCreateAccount() {
        String type = accountTypeBox.getValue();
        String depositStr = initialDepositField.getText();
        double deposit = 0.0;
        try {
            deposit = Double.parseDouble(depositStr);
        } catch (Exception e) {
            // Invalid deposit value
            javafx.scene.control.Alert err = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR, "Invalid deposit amount.");
            err.setHeaderText("Invalid Input");
            err.show();
            return;
        }
        int userId;
        if (adminMode && targetUserId != null) {
            userId = targetUserId;
        } else if (currentCustomer != null) {
            userId = currentCustomer.getId();
        } else {
            javafx.scene.control.Alert err = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR, "No customer specified.");
            err.setHeaderText("Missing Customer");
            err.show();
            return;
        }
        if (type == null || type.isEmpty()) {
            javafx.scene.control.Alert err = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR, "Please select an account type.");
            err.setHeaderText("Missing Account Type");
            err.show();
            return;
        }
        com.bank.model.Account account;
        switch (type) {
            case "SAVINGS":
                account = new com.bank.model.SavingsAccount(0, userId, deposit, adminMode ? "APPROVED" : "PENDING");
                break;
            case "INVESTMENT":
                account = new com.bank.model.InvestmentAccount(0, userId, deposit, adminMode ? "APPROVED" : "PENDING");
                break;
            case "CHEQUE":
                account = new com.bank.model.ChequeAccount(0, userId, deposit, adminMode ? "APPROVED" : "PENDING");
                break;
            default:
                return;
        }
        accountDAO.addAccount(account);
        javafx.scene.control.Alert info = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION, adminMode ? "Account created and approved." : "Account request created. Await admin approval.");
        info.setHeaderText("Account Created");
        info.showAndWait();
        try {
            javafx.stage.Stage stage = (javafx.stage.Stage) createAccountButton.getScene().getWindow();
            // If opened as a modal (owner set and modality), simply close the modal so the caller can refresh
            if (stage.getModality() == javafx.stage.Modality.APPLICATION_MODAL) {
                stage.close();
                return;
            }
            // Otherwise (launched standalone), navigate back appropriately
            if (adminMode) {
                javafx.scene.Parent root = javafx.fxml.FXMLLoader.load(getClass().getResource("/fxml/admin_dashboard.fxml"));
                javafx.scene.Scene sc = new javafx.scene.Scene(root, 1000, 700);
                try { sc.getStylesheets().add(getClass().getResource("/css/sidebar.css").toExternalForm()); } catch (Exception ex) {}
                stage.setScene(sc);
            } else {
                javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/customer_dashboard.fxml"));
                javafx.scene.Parent root = loader.load();
                com.bank.controller.CustomerDashboardController ctrl = loader.getController();
                if (currentCustomer != null) ctrl.setCurrentCustomer(currentCustomer);
                javafx.scene.Scene sc = new javafx.scene.Scene(root, 900, 600);
                try { sc.getStylesheets().add(getClass().getResource("/css/sidebar.css").toExternalForm()); } catch (Exception ex) {}
                stage.setScene(sc);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleCancel() {
        try {
            javafx.stage.Stage stage = (javafx.stage.Stage) cancelButton.getScene().getWindow();
            if (stage.getModality() == javafx.stage.Modality.APPLICATION_MODAL) {
                stage.close();
                return;
            }
            if (adminMode) {
                javafx.scene.Parent root = javafx.fxml.FXMLLoader.load(getClass().getResource("/fxml/admin_dashboard.fxml"));
                javafx.scene.Scene sc = new javafx.scene.Scene(root, 1000, 700);
                try { sc.getStylesheets().add(getClass().getResource("/css/sidebar.css").toExternalForm()); } catch (Exception ex) {}
                stage.setScene(sc);
            } else {
                javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/customer_dashboard.fxml"));
                javafx.scene.Parent root = loader.load();
                com.bank.controller.CustomerDashboardController ctrl = loader.getController();
                if (currentCustomer != null) ctrl.setCurrentCustomer(currentCustomer);
                javafx.scene.Scene sc = new javafx.scene.Scene(root, 900, 600);
                try { sc.getStylesheets().add(getClass().getResource("/css/sidebar.css").toExternalForm()); } catch (Exception ex) {}
                stage.setScene(sc);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
