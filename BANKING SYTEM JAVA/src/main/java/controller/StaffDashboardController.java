package controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.util.Pair;
import dao.CustomerDAO;
import dao.AccountDAO;
import model.*;
import util.AlertUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Node;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
public class StaffDashboardController {

    @FXML private TextField searchField;
    @FXML private Label statsLabel;
    @FXML private TextArea customerListArea;
    @FXML private TextArea accountListArea;
    @FXML private Button approveButton;
    @FXML private Button rejectButton;
    @FXML private Button createAccountButton;
    @FXML private Button refreshButton;
    @FXML private Button approveAccountsButton;
    @FXML private Button rejectAccountsButton;

    private final AccountDAO accountDAO = new AccountDAO();

    @FXML
    public void initialize() {
        refreshButton.setOnAction(e -> refreshPendingCustomers());
        approveButton.setOnAction(e -> onApproveCustomer());
        rejectButton.setOnAction(e -> onRejectCustomer());
        createAccountButton.setOnAction(e -> onCreateAccount());
        approveAccountsButton.setOnAction(e -> onApproveAccount());
        rejectAccountsButton.setOnAction(e -> onRejectAccount());
        refreshAll();
    }

    // Refresh both customers and accounts 
    private void refreshAll() {
        refreshPendingCustomers();
        refreshPendingAccounts();
    }

    //  Pending Customers Section 
    private void refreshPendingCustomers() {
        try {
            List<Customer> pending = CustomerDAO.allPending();
            StringBuilder sb = new StringBuilder("Pending Customers:\n");
            sb.append("ID | Full Name | Username | Status\n");
            sb.append("-----------------------------------------\n");
            for (Customer c : pending) {
                sb.append(String.format("%-4d | %-20s | %-15s | %s%n",
                        c.getId(), c.getFullName(), c.getUsername(), "PENDING"));
            }
            customerListArea.setText(sb.toString());

            Map<String, Integer> stats = CustomerDAO.stats();
            statsLabel.setText(String.format(
                    "Pending: %d | Approved: %d | Rejected: %d",
                    stats.getOrDefault("PENDING", 0),
                    stats.getOrDefault("APPROVED", 0),
                    stats.getOrDefault("REJECTED", 0)
            ));
        } catch (Exception e) {
            AlertUtil.error("Failed to load customers: " + e.getMessage());
        }
    }

    private void onApproveCustomer() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Approve Customer");
        dialog.setHeaderText("Approve Customer by ID");
        dialog.setContentText("Enter Customer ID:");
        dialog.showAndWait().ifPresent(idStr -> {
            try {
                long id = Long.parseLong(idStr.trim());
                CustomerDAO.approveCustomer(id);
                AlertUtil.info("Customer approved successfully.");
                refreshAll();
            } catch (Exception e) {
                AlertUtil.error("Error approving customer: " + e.getMessage());
            }
        });
    }

    private void onRejectCustomer() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Reject Customer");
        dialog.setHeaderText("Reject Customer by ID");
        dialog.setContentText("Enter Customer ID:");
        dialog.showAndWait().ifPresent(idStr -> {
            try {
                long id = Long.parseLong(idStr.trim());
                CustomerDAO.rejectCustomer(id);
                AlertUtil.info("Customer rejected successfully.");
                refreshAll();
            } catch (Exception e) {
                AlertUtil.error("Error rejecting customer: " + e.getMessage());
            }
        });
    }

    //  Account Creation Section 
    private void onCreateAccount() {
        Dialog<Account> dialog = new Dialog<>();
        dialog.setTitle("Create Account");
        dialog.setHeaderText("Enter details for new account");

        ButtonType createBtn = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createBtn, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        TextField customerIdField = new TextField();
        TextField accountNumberField = new TextField();
        TextField branchField = new TextField();
        TextField depositField = new TextField();
        ChoiceBox<String> typeBox = new ChoiceBox<>();
        typeBox.getItems().addAll("SAVINGS", "INVESTMENT", "CHEQUE");
        typeBox.setValue("SAVINGS");

        grid.add(new Label("Customer ID:"), 0, 0);
        grid.add(customerIdField, 1, 0);
        grid.add(new Label("Account Type:"), 0, 1);
        grid.add(typeBox, 1, 1);
        grid.add(new Label("Account Number:"), 0, 2);
        grid.add(accountNumberField, 1, 2);
        grid.add(new Label("Branch:"), 0, 3);
        grid.add(branchField, 1, 3);
        grid.add(new Label("Initial Deposit:"), 0, 4);
        grid.add(depositField, 1, 4);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == createBtn) {
                try {
                    long cid = Long.parseLong(customerIdField.getText());
                    String accNo = accountNumberField.getText();
                    String type = typeBox.getValue();
                    String branch = branchField.getText();
                    double deposit = Double.parseDouble(depositField.getText());

                    Account acc = switch (type) {
                        case "INVESTMENT" -> new InvestmentAccount(cid, accNo, deposit, branch);
                        case "CHEQUE" -> new ChequeAccount(cid, accNo, deposit, branch);
                        default -> new SavingsAccount(cid, accNo, deposit, branch);
                    };
                    accountDAO.createAccount(acc);
                    return acc;
                } catch (Exception e) {
                    AlertUtil.error("Invalid input: " + e.getMessage());
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(a -> {
            AlertUtil.info("Account created successfully (Pending Approval).");
            refreshAll();
        });
    }

    // --- Pending Accounts Section ---
    private void refreshPendingAccounts() {
        try (var conn = dao.DBConnection.getConnection();
             var st = conn.createStatement();
             var rs = st.executeQuery("SELECT * FROM accounts WHERE status='PENDING'")) {

            StringBuilder sb = new StringBuilder("Pending Accounts:\n");
            sb.append("ID | Account No | Type | Branch | Balance\n");
            sb.append("-------------------------------------------\n");
            while (rs.next()) {
                sb.append(String.format("%-4d | %-12s | %-10s | %-10s | %.2f%n",
                        rs.getLong("id"),
                        rs.getString("account_number"),
                        rs.getString("account_type"),
                        rs.getString("branch"),
                        rs.getDouble("balance")));
            }
            accountListArea.setText(sb.toString());

        } catch (SQLException e) {
            AlertUtil.error("Error loading pending accounts: " + e.getMessage());
        }
    }

    private void onApproveAccount() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Approve Account");
        dialog.setHeaderText("Approve Account by ID");
        dialog.setContentText("Enter Account ID:");
        dialog.showAndWait().ifPresent(idStr -> {
            try (var conn = dao.DBConnection.getConnection();
                 var ps = conn.prepareStatement("UPDATE accounts SET status='ACTIVE' WHERE id=?")) {
                ps.setLong(1, Long.parseLong(idStr.trim()));
                ps.executeUpdate();
                AlertUtil.info("Account approved successfully.");
                refreshAll();
            } catch (Exception e) {
                AlertUtil.error("Error approving account: " + e.getMessage());
            }
        });
    }

    private void onRejectAccount() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Reject Account");
        dialog.setHeaderText("Reject Account by ID");
        dialog.setContentText("Enter Account ID:");
        dialog.showAndWait().ifPresent(idStr -> {
            try (var conn = dao.DBConnection.getConnection();
                 var ps = conn.prepareStatement("UPDATE accounts SET status='REJECTED' WHERE id=?")) {
                ps.setLong(1, Long.parseLong(idStr.trim()));
                ps.executeUpdate();
                AlertUtil.info("Account rejected successfully.");
                refreshAll();
            } catch (Exception e) {
                AlertUtil.error("Error rejecting account: " + e.getMessage());
            }
        });
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/views/LoginView.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Banking System - Login");
            stage.show();
        } catch (Exception e) {
            AlertUtil.error("Logout failed: " + e.getMessage());
        }
    }
}
