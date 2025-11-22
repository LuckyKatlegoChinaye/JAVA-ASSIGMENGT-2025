package com.bank.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.stage.Stage;
import javafx.stage.Window;
import com.bank.model.Transaction;
import com.bank.dao.TransactionDAO;
import com.bank.dao.TransactionDAOImpl;

import java.util.List;

public class ManageTransactionsController {
    @FXML private TextField filterAccountField;
    @FXML private TableView<Transaction> transactionsTable;
    @FXML private Button deleteButton;

    private TransactionDAO transactionDAO = new TransactionDAOImpl();
    private Stage ownerStage; // the dashboard stage we should restore when closing

    @FXML
    public void initialize() {
        refreshTable();
        transactionsTable.getSelectionModel().selectedItemProperty().addListener((o,old,sel) -> {
            deleteButton.setDisable(sel == null);
        });
    }

    private void refreshTable() {
        List<Transaction> all = transactionDAO.getAllTransactions();
        transactionsTable.setItems(FXCollections.observableArrayList(all));
    }

    @FXML
    public void handleRefresh() {
        filterAccountField.clear();
        refreshTable();
    }

    @FXML
    public void handleFilter() {
        String txt = filterAccountField.getText();
        if (txt == null || txt.isEmpty()) { refreshTable(); return; }
        try {
            int acctId = Integer.parseInt(txt);
            List<Transaction> list = transactionDAO.getTransactionsByAccountId(acctId);
            transactionsTable.setItems(FXCollections.observableArrayList(list));
        } catch (NumberFormatException e) {
            Alert a = new Alert(Alert.AlertType.WARNING, "Enter a valid account id to filter.");
            a.show();
        }
    }

    @FXML
    public void handleDelete() {
        Transaction sel = transactionsTable.getSelectionModel().getSelectedItem();
        if (sel == null) return;
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Delete transaction ID " + sel.getId() + "?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(bt -> {
            if (bt == ButtonType.YES) {
                boolean ok = transactionDAO.deleteTransaction(sel.getId());
                if (ok) {
                    refreshTable();
                    Alert info = new Alert(Alert.AlertType.INFORMATION, "Transaction deleted.");
                    info.show();
                } else {
                    Alert err = new Alert(Alert.AlertType.ERROR, "Failed to delete transaction.");
                    err.show();
                }
            }
        });
    }

    @FXML
    public void handleClose() {
        Stage st = (Stage) transactionsTable.getScene().getWindow();
        st.close();
        if (ownerStage != null) {
            try { ownerStage.show(); } catch (Exception e) { /* ignore */ }
        }
    }

    public void setOwnerStage(Stage owner) {
        this.ownerStage = owner;
    }
}
