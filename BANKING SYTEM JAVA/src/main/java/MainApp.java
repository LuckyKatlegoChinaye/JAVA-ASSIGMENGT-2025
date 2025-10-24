import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.sql.*;
import java.nio.file.*;
import java.io.IOException;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        // Initialize the database before showing GUI
        initDatabase();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/LoginView.fxml"));
        Scene scene = new Scene(loader.load());
        stage.setTitle("Banking System");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    // Automatically create tables from schema.sql if missing
    private void initDatabase() {
        try (Connection conn = DriverManager.getConnection("jdbc:h2:./bankdb", "sa", "")) {
            String schema = Files.readString(Path.of("schema.sql"));
            try (Statement stmt = conn.createStatement()) {
                stmt.execute(schema);
            }
            System.out.println("Database initialized successfully.");
        } catch (IOException e) {
            System.err.println("Failed to read schema.sql: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("Database initialization failed: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        launch();
    }
}
