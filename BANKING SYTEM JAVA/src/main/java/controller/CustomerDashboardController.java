package controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Node;
import dao.AccountDAO;
import dao.TransactionDAO;
import model.*;
import util.AlertUtil;

import java.sql.SQLException;
import java.util.List;

public class CustomerDashboardController {

    @FXML private Label nameLabel;
    @FXML private TextArea accountDisplay;
    @FXML private TextField depositAmount;
    @FXML private TextField withdrawAmount;
    @FXML private TextField transferToField;
    @FXML private TextField transferAmountField;
    @FXML private ChoiceBox<String> accountChoice;
    @FXML private ChoiceBox<String> applyChoice;

    private final AccountDAO accountDAO = new AccountDAO();
    private final TransactionDAO transactionDAO = new TransactionDAO();
    private Customer currentCustomer;

    public void setCustomer(Customer customer) {
        this.currentCustomer = customer;
        nameLabel.setText("Welcome, " + customer.getFullName() + "!");
        refreshAccounts();
    }

    @FXML
    private void initialize() {
        applyChoice.getItems().addAll("SAVINGS", "INVESTMENT", "CHEQUE");
    }

    private void refreshAccounts() {
        try {
            List<Account> accounts = accountDAO.findAccountsByCustomer(currentCustomer.getId());
            accountChoice.getItems().clear();
            StringBuilder sb = new StringBuilder("Your Accounts:\n\n");
            for (Account acc : accounts) {
                sb.append(String.format("%s | %s | %.2f | %s\n",
                        acc.getAccountType(), acc.getAccountNumber(), acc.getBalance(), acc.getStatus()));
                if ("ACTIVE".equalsIgnoreCase(acc.getStatus())) {
                    accountChoice.getItems().add(acc.getAccountNumber());
                }
            }
            accountDisplay.setText(sb.toString());
        } catch (SQLException e) {
            AlertUtil.error("Failed to load accounts: " + e.getMessage());
        }
    }

    @FXML
    private void onApplyAccount(ActionEvent event) {
        try {
            String type = applyChoice.getValue();
            if (type == null) {
                AlertUtil.error("Select an account type to apply for.");
                return;
            }
            String accNum = type.substring(0, 2) + "-" + System.currentTimeMillis();
            Account account;
            switch (type) {
                case "INVESTMENT" -> account = new InvestmentAccount(currentCustomer.getId(), accNum, 0, "Main Branch");
                case "CHEQUE" -> account = new ChequeAccount(currentCustomer.getId(), accNum, 0, "Main Branch");
                default -> account = new SavingsAccount(currentCustomer.getId(), accNum, 0, "Main Branch");
            }
            account.setStatus("PENDING");
            accountDAO.createAccount(account);
            AlertUtil.info("Account application submitted for approval.");
            refreshAccounts();
        } catch (Exception e) {
            AlertUtil.error("Application failed: " + e.getMessage());
        }
    }

    @FXML
    private void onDeposit(ActionEvent event) {
        try {
            String accNo = accountChoice.getValue();
            double amount = Double.parseDouble(depositAmount.getText());
            if (accNo == null || amount <= 0) {
                AlertUtil.error("Select an account and enter a positive amount.");
                return;
            }
            Account acc = accountDAO.findByAccountNumber(accNo);
            acc.deposit(amount);
            accountDAO.updateBalance(accNo, acc.getBalance());
            transactionDAO.recordTransaction(acc.getId(), "DEPOSIT", amount, "Deposit made");
            AlertUtil.info("Deposit successful.");
            refreshAccounts();
        } catch (Exception e) {
            AlertUtil.error("Deposit failed: " + e.getMessage());
        }
    }

    @FXML
    private void onWithdraw(ActionEvent event) {
        try {
            String accNo = accountChoice.getValue();
            double amount = Double.parseDouble(withdrawAmount.getText());
            if (accNo == null || amount <= 0) {
                AlertUtil.error("Select an account and enter a positive amount.");
                return;
            }
            Account acc = accountDAO.findByAccountNumber(accNo);
            if (acc.getBalance() < amount) {
                AlertUtil.error("Insufficient balance.");
                return;
            }
            acc.deposit(-amount);
            accountDAO.updateBalance(accNo, acc.getBalance());
            transactionDAO.recordTransaction(acc.getId(), "WITHDRAW", amount, "Withdrawal made");
            AlertUtil.info("Withdrawal successful.");
            refreshAccounts();
        } catch (Exception e) {
            AlertUtil.error("Withdrawal failed: " + e.getMessage());
        }
    }

    @FXML
    private void onTransfer(ActionEvent event) {
        try {
            String fromAccNo = accountChoice.getValue();
            String toAccNo = transferToField.getText().trim();
            double amount = Double.parseDouble(transferAmountField.getText());
            if (fromAccNo == null || toAccNo.isEmpty() || amount <= 0) {
                AlertUtil.error("Please select accounts and enter valid amount.");
                return;
            }

            Account from = accountDAO.findByAccountNumber(fromAccNo);
            Account to = accountDAO.findByAccountNumber(toAccNo);

            if (to == null) {
                AlertUtil.error("Target account not found.");
                return;
            }

            if (from.getBalance() < amount) {
                AlertUtil.error("Insufficient balance for transfer.");
                return;
            }

            from.deposit(-amount);
            to.deposit(amount);
            accountDAO.updateBalance(fromAccNo, from.getBalance());
            accountDAO.updateBalance(toAccNo, to.getBalance());
            transactionDAO.recordTransaction(from.getId(), "TRANSFER_OUT", amount, "Sent to " + toAccNo);
            transactionDAO.recordTransaction(to.getId(), "TRANSFER_IN", amount, "Received from " + fromAccNo);

            AlertUtil.info("Transfer successful.");
            refreshAccounts();
        } catch (Exception e) {
            AlertUtil.error("Transfer failed: " + e.getMessage());
        }
    }

    @FXML
    private void onLogout(ActionEvent event) {
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
