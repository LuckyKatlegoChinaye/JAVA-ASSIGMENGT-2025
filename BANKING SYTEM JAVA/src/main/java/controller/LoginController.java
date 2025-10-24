package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import dao.CustomerDAO;
import model.Customer;
import util.AlertUtil;
import java.io.IOException;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;

    @FXML
    private void handleLogin(ActionEvent event) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        try {
            // Default admin shortcut (bypass DB)
            if (username.equals("admin") && password.equals("admin123")) {
                loadView(event, "/views/StaffDashboard.fxml", "Staff Dashboard", null);
                return;
            }

            // Customer login from DB
            Customer user = CustomerDAO.login(username, password);
            if (user == null) {
                AlertUtil.error("Invalid credentials!");
                return;
            }

            if (!user.isApproved()) {
                AlertUtil.info("Account pending staff approval.");
                return;
            }

            // Redirect based on role
            loadView(event, "/views/CustomerDashboard.fxml", "Customer Dashboard", user);

        } catch (Exception e) {
            AlertUtil.error("Login error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleRegister(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/views/RegisterView.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Register - Banking System");
            stage.show();
        } catch (IOException e) {
            AlertUtil.error("Failed to load RegisterView.fxml: " + e.getMessage());
        }
    }

    private void loadView(ActionEvent event, String fxmlPath, String title, Customer user) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        Parent root = loader.load();

        // Pass logged-in user to CustomerDashboard
        if (user != null && fxmlPath.contains("CustomerDashboard.fxml")) {
            CustomerDashboardController controller = loader.getController();
            controller.setCustomer(user);
        }

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle(title);
        stage.show();
    }
}
