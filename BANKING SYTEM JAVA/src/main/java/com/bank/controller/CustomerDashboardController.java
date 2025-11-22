package com.bank.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.util.StringConverter;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.stage.Window;
import javafx.stage.Stage;
import com.bank.model.*;
import com.bank.dao.*;
import java.util.Date;
// removed unused imports

public class CustomerDashboardController {
    // Top bar
    @FXML private Label welcomeLabel;
    // New dashboard card labels 
    @FXML private Label checkingBalanceLabel;
    @FXML private Label savingsBalanceLabel;
    @FXML private Label investmentBalanceLabel;
    
    @FXML private VBox checkingCard;
    @FXML private VBox savingsCard;
    @FXML private VBox investmentCard;
    @FXML private Label checkingNumberLabel;
    @FXML private Label savingsNumberLabel;
    @FXML private Label investmentNumberLabel;
    @FXML private Label checkingTypeLabel;
    @FXML private Label savingsTypeLabel;
    @FXML private Label investmentTypeLabel;
    // Sidebar and section nodes
    @FXML private Button sidebarDashboardBtn;
    @FXML private Button sidebarTransactionsBtn;
    @FXML private Button sidebarAccountsBtn;
    @FXML private Button sidebarProfileBtn;
    @FXML private HBox accountsCards;
    @FXML private VBox transactionsBox;
    @FXML private VBox sendMoneyBox;
    // New FXML panes from user's layout
    @FXML private StackPane contentArea;
    @FXML private VBox dashboardPane;
    @FXML private VBox sendMoneyPane;
    @FXML private VBox accountsPane;
    @FXML private TableView<Account> accountsTable;

    // Customer Management Tab
    @FXML private TableView<Customer> customersTable;
    @FXML private TableColumn<Customer, Integer> customerIdCol;
    @FXML private TableColumn<Customer, String> customerNameCol;
    @FXML private TableColumn<Customer, String> customerUsernameCol;
    @FXML private TableColumn<Customer, String> customerStatusCol;

    // Account Management Tab (customer view uses ComboBox)
    @FXML private ComboBox<Account> accountsCombo;
    @FXML private javafx.scene.control.Label selectedBalanceLabel;
    @FXML private javafx.scene.control.TextField amountField;
    @FXML private javafx.scene.control.Button depositButton;
    @FXML private javafx.scene.control.Button withdrawButton;
    @FXML private javafx.scene.control.TextField transferTargetField;
    @FXML private javafx.scene.control.Button transferButton;

    // Use BWP currency prefix and two-decimal formatting
    private final java.text.DecimalFormat currencyFmt = new java.text.DecimalFormat("#,##0.00");

    private Customer currentCustomer;
    private AccountDAO accountDAO = new AccountDAOImpl();
    private CustomerDAO customerDAO = new CustomerDAOImpl();
    private TransactionDAO transactionDAO = new TransactionDAOImpl();
    // Optional transaction table and send-money fields from updated FXML
    @FXML private TableView<com.bank.model.Transaction> transactionsTable;
    @FXML private TextField payeeField;
    @FXML private TextField sendAmountField;
    @FXML private TextField messageField;

    @FXML
    public void initialize() {
        // Initialize only the parts that exist in the loaded FXML to avoid NPEs
        if (customerIdCol != null) {
            setupCustomerTable();
            refreshCustomers();
        }
        if (accountsCombo != null) {
            setupAccountsCombo();
            refreshAccounts();
            accountsCombo.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
                updateSelectedBalance(newSel);
                // sync selection into accountsTable and transactions
                if (newSel != null) {
                    if (accountsTable != null) accountsTable.getSelectionModel().select(newSel);
                    if (transactionsTable != null) {
                        java.util.List<com.bank.model.Transaction> txs = transactionDAO.getTransactionsByAccountId(newSel.getId());
                        transactionsTable.setItems(FXCollections.observableArrayList(txs));
                    }
                }
            });

            // enable/disable transaction buttons depending on selection
            if (depositButton != null && withdrawButton != null && transferButton != null) {
                boolean has = accountsCombo.getSelectionModel().getSelectedItem() != null;
                depositButton.setDisable(!has);
                withdrawButton.setDisable(!has);
                transferButton.setDisable(!has);
                accountsCombo.getSelectionModel().selectedItemProperty().addListener((o,old,sel) -> {
                    boolean ok = sel != null;
                    depositButton.setDisable(!ok);
                    withdrawButton.setDisable(!ok);
                    transferButton.setDisable(!ok);
                });
            }
        }

        // configure table columns for readable output
        setupTables();
        // wire card clicks for FXML variants that include them
        if (checkingCard != null) checkingCard.setOnMouseClicked(e -> handleSelectAccountByType("CHECKING"));
        if (savingsCard != null) savingsCard.setOnMouseClicked(e -> handleSelectAccountByType("SAVINGS"));
        if (investmentCard != null) investmentCard.setOnMouseClicked(e -> handleSelectAccountByType("INVESTMENT"));
        if (welcomeLabel != null) {
            if (currentCustomer == null) {
                welcomeLabel.setText("Welcome, Customer");
            } else {
                welcomeLabel.setText("Welcome, " + currentCustomer.getName());
            }
        }
    }

    private void setupCustomerTable() {
        if (customerIdCol == null) return;
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

    private void setupAccountsCombo() {
        if (accountsCombo == null) return;
        accountsCombo.setConverter(new StringConverter<Account>() {
            @Override
            public String toString(Account acct) {
                if (acct == null) return "";
                return String.format("%d — %s (%s)", acct.getId(), acct.getType(), acct.getStatus());
            }

            @Override
            public Account fromString(String string) { return null; }
        });
    }

    private void refreshCustomers() {
        if (customersTable == null) return;
        ObservableList<Customer> customers = FXCollections.observableArrayList(customerDAO.getAllCustomers());
        customersTable.setItems(customers);
    }

    private void refreshAccounts() {
        java.util.List<Account> list;
        if (currentCustomer != null) {
            list = accountDAO.getAccountsByUserId(currentCustomer.getId());
        } else {
            list = accountDAO.getAllAccounts();
        }
        // exclude REJECTED accounts from the customer's view
        java.util.List<Account> filtered = new java.util.ArrayList<>();
        for (Account a : list) {
            if (a == null) continue;
            String s = a.getStatus();
            if (s == null) s = "";
            if ("REJECTED".equalsIgnoreCase(s)) continue;
            filtered.add(a);
        }
        ObservableList<Account> accounts = FXCollections.observableArrayList(filtered);
        if (accountsCombo != null) {
            accountsCombo.setItems(accounts);
            if (!accounts.isEmpty()) accountsCombo.getSelectionModel().selectFirst();
        }
        // also populate accounts table (customer account management view)
        if (accountsTable != null) {
            accountsTable.setItems(accounts);
        }
        // update account cards (if present)
        updateAccountCards();
    }

    @FXML
    public void handleRefresh() {
        refreshCustomers();
        refreshAccounts();
    }

    private void updateAccountCards() {
        if (currentCustomer == null) return;

        // make accounts table rows clickable to pick an account
        if (accountsTable != null) {
            accountsTable.setRowFactory(tv -> {
                TableRow<Account> row = new TableRow<>();
                row.setOnMouseClicked(ev -> {
                    if (!row.isEmpty()) {
                        Account acc = row.getItem();
                        if (accountsCombo != null) accountsCombo.getSelectionModel().select(acc);
                        updateSelectedBalance(acc);
                        if (transactionsTable != null) {
                            java.util.List<com.bank.model.Transaction> txs = transactionDAO.getTransactionsByAccountId(acc.getId());
                            transactionsTable.setItems(FXCollections.observableArrayList(txs));
                        }
                    }
                });
                return row;
            });
        }

        // make transactions rows respond to double-click to show details
        if (transactionsTable != null) {
            transactionsTable.setRowFactory(tv -> {
                TableRow<com.bank.model.Transaction> row = new TableRow<>();
                row.setOnMouseClicked(ev -> {
                    if (!row.isEmpty() && ev.getClickCount() == 2) {
                        com.bank.model.Transaction t = row.getItem();
                        javafx.scene.control.Alert info = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION,
                            String.format("Date: %s\nType: %s\nAmount: %s\nDesc: %s", t.getDate(), t.getType(), currencyFmt.format(t.getAmount()), t.getDescription()));
                        info.setHeaderText("Transaction Details (ID: " + t.getId() + ")");
                        Window owner = (welcomeLabel == null ? null : welcomeLabel.getScene().getWindow());
                        if (owner != null) info.initOwner(owner);
                        info.show();
                    }
                });
                return row;
            });
        }
        java.util.List<Account> accounts = accountDAO.getAccountsByUserId(currentCustomer.getId());
        double checking = 0, savings = 0, invest = 0;
        for (Account a : accounts) {
            if (a == null) continue;
            String type = a.getType() == null ? "" : a.getType().toUpperCase();
            if ("CHEQUE".equals(type) || "CHECKING".equals(type)) checking += a.getBalance();
            else if ("SAVINGS".equals(type)) savings += a.getBalance();
            else if ("INVESTMENT".equals(type) || "INVEST".equals(type)) invest += a.getBalance();
        }
        if (checkingBalanceLabel != null) checkingBalanceLabel.setText("BWP " + currencyFmt.format(checking));
        if (savingsBalanceLabel != null) savingsBalanceLabel.setText("BWP " + currencyFmt.format(savings));
        if (investmentBalanceLabel != null) investmentBalanceLabel.setText("BWP " + currencyFmt.format(invest));
        // set masked numbers and types if available
        if (checkingNumberLabel != null) checkingNumberLabel.setText(maskAccountNumber(findFirstByType("CHECKING")));
        if (savingsNumberLabel != null) savingsNumberLabel.setText(maskAccountNumber(findFirstByType("SAVINGS")));
        if (investmentNumberLabel != null) investmentNumberLabel.setText(maskAccountNumber(findFirstByType("INVESTMENT")));
        if (checkingTypeLabel != null) checkingTypeLabel.setText("Checking Account");
        if (savingsTypeLabel != null) savingsTypeLabel.setText("Savings Account");
        if (investmentTypeLabel != null) investmentTypeLabel.setText("Investment Account");
    }

    private Account findFirstByType(String typeStr) {
        if (currentCustomer == null) return null;
        java.util.List<Account> accounts = accountDAO.getAccountsByUserId(currentCustomer.getId());
        if (accounts == null) return null;
        for (Account a : accounts) {
            if (a == null || a.getType() == null) continue;
            String t = a.getType().toString();
            if (t == null) continue;
            if (t.equalsIgnoreCase(typeStr)) return a;
        }
        return null;
    }

    private String maskAccountNumber(Account a) {
        if (a == null) return "**** **** **** 0000";
        String id = String.valueOf(a.getId());
        String suff = id.length() > 4 ? id.substring(id.length() - 4) : String.format("%04d", a.getId());
        return "**** **** **** " + suff;
    }

    private void handleSelectAccountByType(String typeStr) {
        Account found = findFirstByType(typeStr);
        if (found != null && accountsCombo != null) {
            accountsCombo.getSelectionModel().select(found);
            updateSelectedBalance(found);
            // refresh transactions for selected account
            if (transactionsTable != null) {
                java.util.List<com.bank.model.Transaction> txs = transactionDAO.getTransactionsByAccountId(found.getId());
                transactionsTable.setItems(FXCollections.observableArrayList(txs));
            }
            // select row in accounts table if present
            if (accountsTable != null) {
                accountsTable.getSelectionModel().select(found);
            }
            // show the send money pane on selection to allow quick send
            if (sendMoneyBox != null) { sendMoneyBox.setVisible(true); sendMoneyBox.setManaged(true); }
            if (sendMoneyPane != null) { sendMoneyPane.setVisible(true); sendMoneyPane.setManaged(true); }
        }
    }

    // FXML-accessible handlers for card clicks (referenced from some FXML variants)
    @FXML
    public void handleSelectChecking() { handleSelectAccountByType("CHECKING"); }

    @FXML
    public void handleSelectSavings() { handleSelectAccountByType("SAVINGS"); }

    @FXML
    public void handleSelectInvestment() { handleSelectAccountByType("INVESTMENT"); }

    // FXML handlers for the user's StackPane navigation
    @FXML
    public void showDashboard() {
        if (dashboardPane != null) {
            dashboardPane.setVisible(true);
            dashboardPane.setManaged(true);
        }
        if (sendMoneyPane != null) {
            sendMoneyPane.setVisible(false);
            sendMoneyPane.setManaged(false);
        }
        if (accountsPane != null) {
            accountsPane.setVisible(false);
            accountsPane.setManaged(false);
        }
    }

    @FXML
    public void showSendMoney() {
        if (dashboardPane != null) {
            dashboardPane.setVisible(false);
            dashboardPane.setManaged(false);
        }
        if (sendMoneyPane != null) {
            sendMoneyPane.setVisible(true);
            sendMoneyPane.setManaged(true);
        }
        if (accountsPane != null) {
            accountsPane.setVisible(false);
            accountsPane.setManaged(false);
        }
    }

    @FXML
    public void showAccounts() {
        if (dashboardPane != null) {
            dashboardPane.setVisible(false);
            dashboardPane.setManaged(false);
        }
        if (sendMoneyPane != null) {
            sendMoneyPane.setVisible(false);
            sendMoneyPane.setManaged(false);
        }
        if (accountsPane != null) {
            accountsPane.setVisible(true);
            accountsPane.setManaged(true);
        }
    }

    private void refreshTransactions() {
        if (transactionsTable == null || currentCustomer == null) return;
        java.util.List<Account> accounts = accountDAO.getAccountsByUserId(currentCustomer.getId());
        java.util.List<com.bank.model.Transaction> all = new java.util.ArrayList<>();
        for (Account a : accounts) {
            if (a == null) continue;
            all.addAll(transactionDAO.getTransactionsByAccountId(a.getId()));
        }
        all.sort((a,b) -> b.getDate().compareTo(a.getDate()));
        transactionsTable.setItems(FXCollections.observableArrayList(all));
    }

    @FXML
    public void handleShowDashboard() {
        if (accountsCards != null) accountsCards.setVisible(true);
        if (transactionsBox != null) transactionsBox.setVisible(true);
        if (sendMoneyBox != null) sendMoneyBox.setVisible(true);
    }

    @FXML
    public void handleShowTransactions() {
        try {
            // hide the dashboard window and open a standalone transactions window
            Stage owner = (Stage) welcomeLabel.getScene().getWindow();
            owner.hide();

            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/manage_transactions.fxml"));
            javafx.scene.Parent root = loader.load();
            ManageTransactionsController ctrl = loader.getController();

            Stage txStage = new Stage();
            txStage.initOwner(owner);
            txStage.initModality(javafx.stage.Modality.NONE);
            txStage.setTitle("Manage Transactions");
            txStage.setScene(new javafx.scene.Scene(root));
            // apply sidebar stylesheet if available
            try { txStage.getScene().getStylesheets().add(getClass().getResource("/css/sidebar.css").toExternalForm()); } catch (Exception ex) {}

            // give controller the owner so it can restore it
            ctrl.setOwnerStage(owner);

            // when tx window is closed via window manager, restore dashboard
            txStage.setOnCloseRequest(ev -> {
                try { owner.show(); } catch (Exception e) { /* ignore */ }
                // refresh dashboard data
                refreshAccounts();
                updateAccountCards();
                refreshTransactions();
            });

            txStage.show();
        } catch (Exception e) {
            e.printStackTrace();
            // fallback to in-dashboard transactions pane
            if (accountsCards != null) accountsCards.setVisible(false);
            if (transactionsBox != null) transactionsBox.setVisible(true);
            if (sendMoneyBox != null) sendMoneyBox.setVisible(false);
        }
    }

    @FXML
    public void handleShowAccounts() {
        if (accountsCards != null) accountsCards.setVisible(true);
        if (transactionsBox != null) transactionsBox.setVisible(false);
        if (sendMoneyBox != null) sendMoneyBox.setVisible(false);
    }

    @FXML
    public void handleProfile() {
        // open update profile modal if available
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/update_profile.fxml"));
            javafx.scene.Parent root = loader.load();
            com.bank.controller.UpdateProfileController upc = loader.getController();
            if (upc != null && currentCustomer != null) upc.setCustomer(currentCustomer);
            javafx.stage.Stage modal = new javafx.stage.Stage();
            modal.initOwner(welcomeLabel.getScene().getWindow());
            modal.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            modal.setTitle("Update Profile");
            modal.setScene(new javafx.scene.Scene(root));
            modal.showAndWait();
            // refresh after profile changes
            refreshAccounts();
            updateAccountCards();
            refreshTransactions();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleDeposit() {
        Account selected = (accountsCombo != null) ? accountsCombo.getSelectionModel().getSelectedItem() : null;
        if (selected == null) {
            showWarning("Please select an account to deposit into.");
            return;
        }
        double amt;
        try { amt = Double.parseDouble(amountField.getText()); }
        catch (Exception e) { showWarning("Enter a valid amount."); return; }
        if (amt <= 0) { showWarning("Amount must be greater than zero."); return; }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Confirm deposit of " + currencyFmt.format(amt) + " to " + accountsCombo.getConverter().toString(selected) + "?", ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Confirm Deposit");
        confirm.showAndWait().ifPresent(bt -> {
                if (bt == ButtonType.YES) {
                    selected.setBalance(selected.getBalance() + amt);
                    accountDAO.updateAccount(selected);
                    // record transaction
                    com.bank.model.Transaction t = new com.bank.model.Transaction(0, selected.getId(), "DEPOSIT", amt, new Date(), "Deposit via dashboard");
                    transactionDAO.addTransaction(t);
                    refreshAccounts();
                    updateSelectedBalance(selected);
                    refreshTransactions();
                    if (amountField != null) amountField.clear();
                    Window owner = (welcomeLabel == null ? null : welcomeLabel.getScene().getWindow());
                    showInfo("Deposit successful: BWP " + currencyFmt.format(amt), owner);
                }
        });
    }

    @FXML
    public void handleWithdraw() {
        Account selected = (accountsCombo != null) ? accountsCombo.getSelectionModel().getSelectedItem() : null;
        if (selected == null) {
            showWarning("Please select an account to withdraw from.");
            return;
        }
        double amt;
        try { amt = Double.parseDouble(amountField.getText()); } catch (Exception e) { showWarning("Enter a valid amount."); return; }
        if (amt <= 0) { showWarning("Amount must be greater than zero."); return; }
        if (selected.getBalance() < amt) { showWarning("Insufficient funds."); return; }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Confirm withdrawal of " + currencyFmt.format(amt) + " from " + accountsCombo.getConverter().toString(selected) + "?", ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Confirm Withdrawal");
        confirm.showAndWait().ifPresent(bt -> {
            if (bt == ButtonType.YES) {
                    selected.setBalance(selected.getBalance() - amt);
                    accountDAO.updateAccount(selected);
                    // record transaction
                    com.bank.model.Transaction t = new com.bank.model.Transaction(0, selected.getId(), "WITHDRAWAL", amt, new Date(), "Withdrawal via dashboard");
                    transactionDAO.addTransaction(t);
                    refreshAccounts();
                    updateSelectedBalance(selected);
                    refreshTransactions();
                    if (amountField != null) amountField.clear();
                    Window owner = (welcomeLabel == null ? null : welcomeLabel.getScene().getWindow());
                    showInfo("Withdrawal successful: BWP " + currencyFmt.format(amt), owner);
                }
        });
    }

    @FXML
    public void handleTransfer() {
        Account source = (accountsCombo != null) ? accountsCombo.getSelectionModel().getSelectedItem() : null;
        if (source == null) { showWarning("Select source account."); return; }
        int targetId;
        try {
            targetId = Integer.parseInt(transferTargetField.getText());
        } catch (Exception e) { showWarning("Enter valid target account id."); return; }
        if (source.getId() == targetId) { showWarning("Cannot transfer to same account."); return; }
        com.bank.dao.AccountDAO dao = new com.bank.dao.AccountDAOImpl();
        Account target = dao.getAccountById(targetId);
        if (target == null) { showWarning("Target account not found."); return; }
        double amt;
        try { amt = Double.parseDouble(amountField.getText()); } catch (Exception e) { showWarning("Enter a valid amount."); return; }
        if (amt <= 0) { showWarning("Amount must be greater than zero."); return; }
        if (source.getBalance() < amt) { showWarning("Insufficient funds."); return; }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Confirm transfer of " + currencyFmt.format(amt) + " from " + accountsCombo.getConverter().toString(source) + " to " + targetId + "?", ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Confirm Transfer");
        confirm.showAndWait().ifPresent(bt -> {
            if (bt == ButtonType.YES) {
                    source.setBalance(source.getBalance() - amt);
                    target.setBalance(target.getBalance() + amt);
                    dao.updateAccount(target);
                    accountDAO.updateAccount(source);
                    // record transactions for both accounts
                    com.bank.model.Transaction out = new com.bank.model.Transaction(0, source.getId(), "TRANSFER_OUT", amt, new Date(), "Transfer to account " + targetId);
                    com.bank.model.Transaction in = new com.bank.model.Transaction(0, target.getId(), "TRANSFER_IN", amt, new Date(), "Transfer from account " + source.getId());
                    transactionDAO.addTransaction(out);
                    transactionDAO.addTransaction(in);
                    refreshAccounts();
                    updateSelectedBalance(source);
                    refreshTransactions();
                    if (amountField != null) amountField.clear();
                    if (transferTargetField != null) transferTargetField.clear();
                    Window owner = (welcomeLabel == null ? null : welcomeLabel.getScene().getWindow());
                    showInfo("Transfer completed: BWP " + currencyFmt.format(amt), owner);
                }
        });
    }

    private void updateSelectedBalance(Account acct) {
        if (selectedBalanceLabel == null) return;
        if (acct == null) {
            selectedBalanceLabel.setText("BWP " + currencyFmt.format(0));
            return;
        }
        selectedBalanceLabel.setText("BWP " + currencyFmt.format(acct.getBalance()));
    }

    private void showWarning(String msg, Window owner) {
        javafx.scene.control.Alert a = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.WARNING, msg);
        if (owner != null) a.initOwner(owner);
        a.show();
    }

    private void showWarning(String msg) { showWarning(msg, (welcomeLabel == null ? null : welcomeLabel.getScene().getWindow())); }

    private void showInfo(String msg, Window owner) {
        javafx.scene.control.Alert a = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION, msg);
        if (owner != null) a.initOwner(owner);
        a.show();
    }

    private void showInfo(String msg) { showInfo(msg, (welcomeLabel == null ? null : welcomeLabel.getScene().getWindow())); }

    /**
     * Configure table columns for readable values (masking and formatting).
     */
    private void setupTables() {
        if (accountsTable != null) {
            try {
                if (accountsTable.getColumns().size() >= 3) {
                    // Account Number (masked)
                    ((TableColumn<Account, String>)accountsTable.getColumns().get(0)).setCellValueFactory(c -> new SimpleStringProperty(maskAccountNumber(c.getValue())));
                    // Type
                    ((TableColumn<Account, String>)accountsTable.getColumns().get(1)).setCellValueFactory(c -> new SimpleStringProperty(c.getValue() == null ? "" : c.getValue().getType()));
                    // Balance formatted
                    ((TableColumn<Account, String>)accountsTable.getColumns().get(2)).setCellValueFactory(c -> new SimpleStringProperty(c.getValue() == null ? "" : "BWP " + currencyFmt.format(c.getValue().getBalance())));
                }
            } catch (Exception ex) { /* tolerant: table may be empty or columns absent in some FXML variants */ }
        }
        if (transactionsTable != null) {
            try {
                if (transactionsTable.getColumns().size() >= 4) {
                    ((TableColumn<com.bank.model.Transaction, java.util.Date>)transactionsTable.getColumns().get(0)).setCellValueFactory(c -> new SimpleObjectProperty<java.util.Date>(c.getValue().getDate()));
                    ((TableColumn<com.bank.model.Transaction, String>)transactionsTable.getColumns().get(1)).setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getAccountId())));
                    ((TableColumn<com.bank.model.Transaction, String>)transactionsTable.getColumns().get(2)).setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getType()));
                    ((TableColumn<com.bank.model.Transaction, String>)transactionsTable.getColumns().get(3)).setCellValueFactory(c -> new SimpleStringProperty("BWP " + currencyFmt.format(c.getValue().getAmount())));
                }
            } catch (Exception ex) { /* tolerant */ }
        }
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
        // Customers cannot approve other customers; disable or hide this button for customers
    }

    @FXML
    public void handleRejectCustomer() {
        // Customers cannot reject other customers; disable or hide this button for customers
    }

    @FXML
    public void handleOpenAccountForCustomer() {
        // Customers can open new accounts for themselves via the account modal
        if (currentCustomer != null) {
            try {
                javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/open_account.fxml"));
                javafx.scene.Parent root = loader.load();
                com.bank.controller.OpenAccountController controller = loader.getController();
                controller.setCustomer(currentCustomer);
                controller.setAdminMode(false);

                javafx.stage.Stage modal = new javafx.stage.Stage();
                modal.initOwner(welcomeLabel.getScene().getWindow());
                modal.initModality(javafx.stage.Modality.APPLICATION_MODAL);
                modal.setTitle("Open Account");
                modal.setScene(new javafx.scene.Scene(root));
                modal.showAndWait();

                refreshAccounts();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    public void handleSendMoney() {
        if (currentCustomer == null) { showWarning("No customer loaded."); return; }
        Account source = (accountsCombo != null) ? accountsCombo.getSelectionModel().getSelectedItem() : null;
        if (source == null) {
            java.util.List<Account> list = accountDAO.getAccountsByUserId(currentCustomer.getId());
            if (list.isEmpty()) { showWarning("No source account available."); return; }
            source = list.get(0);
        }
        int targetId;
        try { targetId = Integer.parseInt(payeeField.getText()); } catch (Exception e) { showWarning("Enter valid payee account id."); return; }
        com.bank.dao.AccountDAO dao = new com.bank.dao.AccountDAOImpl();
        Account target = dao.getAccountById(targetId);
        if (target == null) { showWarning("Payee account not found."); return; }
        double amt;
        try { amt = Double.parseDouble(sendAmountField.getText()); } catch (Exception e) { showWarning("Enter valid amount."); return; }
        if (amt <= 0) { showWarning("Amount must be greater than zero."); return; }
        if (source.getBalance() < amt) { showWarning("Insufficient funds."); return; }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Send BWP " + currencyFmt.format(amt) + " to account " + targetId + "?", ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Confirm Send");
        final Account src = source;
        confirm.showAndWait().ifPresent(bt -> {
            if (bt == ButtonType.YES) {
                src.setBalance(src.getBalance() - amt);
                target.setBalance(target.getBalance() + amt);
                dao.updateAccount(target);
                accountDAO.updateAccount(src);
                com.bank.model.Transaction out = new com.bank.model.Transaction(0, src.getId(), "SEND_OUT", amt, new Date(), messageField == null ? "" : messageField.getText());
                com.bank.model.Transaction in = new com.bank.model.Transaction(0, target.getId(), "RECEIVE_IN", amt, new Date(), messageField == null ? "" : messageField.getText());
                transactionDAO.addTransaction(out);
                transactionDAO.addTransaction(in);
                refreshAccounts();
                updateAccountCards();
                refreshTransactions();
                showInfo("Money sent: BWP " + currencyFmt.format(amt));
            }
        });
    }

    public void setCurrentCustomer(Customer customer) {
        this.currentCustomer = customer;
        if (welcomeLabel != null && customer != null) welcomeLabel.setText("Welcome, " + customer.getName());
        refreshAccounts();
        updateAccountCards();
        refreshTransactions();
    }

    @FXML
    public void handleApproveAccount() {
        // Customers cannot approve accounts; disable or hide this button for customers
    }

    @FXML
    public void handleRejectAccount() {
        // Customers cannot reject accounts; disable or hide this button for customers
    }
}
