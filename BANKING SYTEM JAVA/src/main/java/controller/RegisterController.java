package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import dao.CustomerDAO;
import model.Customer;
import util.AlertUtil;
import java.io.IOException;

public class RegisterController {

    @FXML private TextField fullNameField;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private TextField emailField;
    @FXML private ChoiceBox<String> roleChoice;

    @FXML
    private void initialize() {
        roleChoice.getItems().addAll("CUSTOMER", "STAFF");
        roleChoice.setValue("CUSTOMER");
    }

    @FXML
    private void onRegister(ActionEvent event) {
        String fullName = fullNameField.getText().trim();
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        String email = emailField.getText().trim();
        String role = roleChoice.getValue();

        if (fullName.isEmpty() || username.isEmpty() || password.isEmpty() || email.isEmpty()) {
            AlertUtil.error("Please fill in all fields.");
            return;
        }

        try {
            Customer existing = CustomerDAO.findByUsername(username);
            if (existing != null) {
                AlertUtil.error("Username already exists!");
                return;
            }

            CustomerDAO.register(fullName, username, password, email, role);
            AlertUtil.info(role.equals("STAFF")
                    ? "Staff account created and approved immediately!"
                    : "Registration successful! Please wait for staff approval.");
        } catch (Exception e) {
            AlertUtil.error("Registration failed: " + e.getMessage());
        }
    }

    @FXML
    private void onBack(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/views/LoginView.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Banking System - Login");
            stage.show();
        } catch (IOException e) {
            AlertUtil.error("Failed to load LoginView.fxml: " + e.getMessage());
        }
    }
}
