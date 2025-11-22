package com.bank.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
// Removed unused import
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import com.bank.model.*;
import com.bank.dao.*;

public class AdminDashboardController {
    @FXML private Label welcomeLabel;
    @FXML private TabPane mainTabPane;

    // Customer Management Tab
    @FXML private TableView<Customer> customersTable;
    @FXML private TableColumn<Customer, Integer> customerIdCol;
    @FXML private TableColumn<Customer, String> customerNameCol;
    @FXML private TableColumn<Customer, String> customerUsernameCol;
    @FXML private TableColumn<Customer, String> customerStatusCol;

    // Account Management Tab
    @FXML private TableView<Account> accountsTable;
    @FXML private TableColumn<Account, Integer> accountNumberCol;
    @FXML private TableColumn<Account, Integer> accountCustomerIdCol;
    @FXML private TableColumn<Account, String> accountTypeCol;
    @FXML private TableColumn<Account, Double> accountBalanceCol;
    @FXML private TableColumn<Account, String> accountStatusCol;

    // Removed unused field adminDAO
    private AccountDAO accountDAO = new AccountDAOImpl();
    private CustomerDAO customerDAO = new CustomerDAOImpl();

    @FXML
    public void initialize() {
        setupCustomerTable();
        setupAccountTable();
        refreshCustomers();
        refreshAccounts();
        // Ensure welcomeLabel reflects admin title if available
        if (welcomeLabel != null && welcomeLabel.getText() != null && welcomeLabel.getText().isEmpty()) {
            welcomeLabel.setText("Admin Dashboard");
        }
    }

    private void setupCustomerTable() {
        customerIdCol.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getId()));
        customerNameCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getName()));
        customerUsernameCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getUsername()));
        customerStatusCol.setCellValueFactory(data -> {
            int id = data.getValue().getId();
            java.util.List<Account> accounts = accountDAO.getAccountsByUserId(id);
            String status = accounts.stream().anyMatch(a -> "APPROVED".equals(a.getStatus())) ? "APPROVED" : "PENDING";
            return new javafx.beans.property.SimpleStringProperty(status);
        });
    }

    private void setupAccountTable() {
        accountNumberCol.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getId()));
        accountCustomerIdCol.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getUserId()));
        accountTypeCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getType()));
        accountBalanceCol.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getBalance()));
        accountStatusCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getStatus()));
    }

    private void refreshCustomers() {
        ObservableList<Customer> customers = FXCollections.observableArrayList(customerDAO.getAllCustomers());
        customersTable.setItems(customers);
    }

    private void refreshAccounts() {
        ObservableList<Account> accounts = FXCollections.observableArrayList(accountDAO.getAllAccounts());
        accountsTable.setItems(accounts);
    }

    @FXML
    public void handleRefresh() {
        refreshCustomers();
        refreshAccounts();
    }

    @FXML
    public void handleLogout() {
        try {
            javafx.stage.Stage stage = (javafx.stage.Stage) welcomeLabel.getScene().getWindow();
            javafx.scene.Parent root = javafx.fxml.FXMLLoader.load(getClass().getResource("/fxml/login.fxml"));
            stage.setScene(new javafx.scene.Scene(root, 900, 600));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleApproveCustomer() {
        Customer selected = customersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            javafx.scene.control.Alert warn = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.WARNING, "Please select a customer first.");
            warn.setHeaderText("No Selection");
            warn.show();
            return;
        }
        if (selected != null) {
            javafx.scene.control.Alert confirm = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.CONFIRMATION,
                    "Approve all pending accounts for " + selected.getName() + "?", javafx.scene.control.ButtonType.YES, javafx.scene.control.ButtonType.NO);
            confirm.setHeaderText("Confirm Approval");
            confirm.showAndWait().ifPresent(b -> {
                if (b == javafx.scene.control.ButtonType.YES) {
                    java.util.List<Account> accounts = accountDAO.getAccountsByUserId(selected.getId());
                    for (Account acc : accounts) {
                        if ("PENDING".equals(acc.getStatus())) {
                            accountDAO.approveAccount(acc.getId());
                        }
                    }
                    refreshCustomers();
                    refreshAccounts();
                    javafx.scene.control.Alert info = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION, "Customer approved and accounts updated.");
                    info.show();
                }
            });
        }
    }

    @FXML
    public void handleRejectCustomer() {
        Customer selected = customersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            javafx.scene.control.Alert warn = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.WARNING, "Please select a customer first.");
            warn.setHeaderText("No Selection");
            warn.show();
            return;
        }
        if (selected != null) {
            javafx.scene.control.Alert confirm = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.CONFIRMATION,
                    "Reject all pending accounts for " + selected.getName() + "?", javafx.scene.control.ButtonType.YES, javafx.scene.control.ButtonType.NO);
            confirm.setHeaderText("Confirm Rejection");
            confirm.showAndWait().ifPresent(b -> {
                if (b == javafx.scene.control.ButtonType.YES) {
                    java.util.List<Account> accounts = accountDAO.getAccountsByUserId(selected.getId());
                    for (Account acc : accounts) {
                        if ("PENDING".equals(acc.getStatus())) {
                            accountDAO.rejectAccount(acc.getId());
                        }
                    }
                    refreshCustomers();
                    refreshAccounts();
                    javafx.scene.control.Alert info = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION, "Customer accounts rejected.");
                    info.show();
                }
            });
        }
    }

    @FXML
    public void handleOpenAccountForCustomer() {
        Customer selected = customersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            javafx.scene.control.Alert warn = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.WARNING, "Please select a customer first.");
            warn.setHeaderText("No Selection");
            warn.show();
            return;
        }
        if (selected != null) {
            try {
                javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/open_account.fxml"));
                javafx.scene.Parent root = loader.load();
                com.bank.controller.OpenAccountController controller = loader.getController();
                controller.setTargetUserId(selected.getId());
                controller.setAdminMode(true);

                javafx.stage.Stage modal = new javafx.stage.Stage();
                modal.initOwner(welcomeLabel.getScene().getWindow());
                modal.initModality(javafx.stage.Modality.APPLICATION_MODAL);
                modal.setTitle("Open Account for " + selected.getName());
                modal.setScene(new javafx.scene.Scene(root));
                modal.showAndWait();

                // Refresh lists after modal closes
                refreshAccounts();
                refreshCustomers();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    public void handleApproveAccount() {
        Account selected = accountsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            javafx.scene.control.Alert warn = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.WARNING, "Please select an account first.");
            warn.setHeaderText("No Selection");
            warn.show();
            return;
        }
        if (selected != null && "PENDING".equals(selected.getStatus())) {
            javafx.scene.control.Alert confirm = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.CONFIRMATION,
                    "Approve account #" + selected.getId() + "?", javafx.scene.control.ButtonType.YES, javafx.scene.control.ButtonType.NO);
            confirm.setHeaderText("Confirm Account Approval");
            confirm.showAndWait().ifPresent(b -> {
                if (b == javafx.scene.control.ButtonType.YES) {
                    accountDAO.approveAccount(selected.getId());
                    refreshAccounts();
                    refreshCustomers();
                    javafx.scene.control.Alert info = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION, "Account approved.");
                    info.show();
                }
            });
        }
    }

    @FXML
    public void handleRejectAccount() {
        Account selected = accountsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            javafx.scene.control.Alert warn = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.WARNING, "Please select an account first.");
            warn.setHeaderText("No Selection");
            warn.show();
            return;
        }
        if (selected != null && "PENDING".equals(selected.getStatus())) {
            javafx.scene.control.Alert confirm = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.CONFIRMATION,
                    "Reject account #" + selected.getId() + "?", javafx.scene.control.ButtonType.YES, javafx.scene.control.ButtonType.NO);
            confirm.setHeaderText("Confirm Account Rejection");
            confirm.showAndWait().ifPresent(b -> {
                if (b == javafx.scene.control.ButtonType.YES) {
                    accountDAO.rejectAccount(selected.getId());
                    refreshAccounts();
                    refreshCustomers();
                    javafx.scene.control.Alert info = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION, "Account rejected.");
                    info.show();
                }
            });
        }
    }

    @FXML
    public void handleDeleteAccount() {
        Account selected = accountsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            javafx.scene.control.Alert warn = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.WARNING, "Please select an account first.");
            warn.setHeaderText("No Selection");
            warn.show();
            return;
        }
        javafx.scene.control.Alert confirm = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.CONFIRMATION,
                "Permanently delete account #" + selected.getId() + "? This cannot be undone.", javafx.scene.control.ButtonType.YES, javafx.scene.control.ButtonType.NO);
        confirm.setHeaderText("Confirm Delete");
        confirm.showAndWait().ifPresent(b -> {
            if (b == javafx.scene.control.ButtonType.YES) {
                accountDAO.deleteAccount(selected.getId());
                refreshAccounts();
                refreshCustomers();
                javafx.scene.control.Alert info = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION, "Account deleted.");
                info.show();
            }
        });
    }
}
