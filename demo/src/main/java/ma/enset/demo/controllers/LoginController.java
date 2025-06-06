package ma.enset.demo.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import ma.enset.demo.dao.UserDao;
import ma.enset.demo.dao.impl.UserDAOImpl;
import ma.enset.demo.entities.User;

import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ResourceBundle;

public class LoginController implements Initializable {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private ImageView logoImage;

    // Reference to the UserDAO implementation
    private UserDao userDAO;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize the UserDAO
        userDAO = new UserDAOImpl();

        // Load the logo image
        try {
            Image img = new Image(getClass().getResource("/images/chatbotlogo.png").toExternalForm());
            logoImage.setImage(img);
        } catch (Exception e) {
            System.err.println("Error loading logo image: " + e.getMessage());
        }
    }

    @FXML
    public void handleLogin() {
        String email = emailField.getText();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showAlert("Erreur de connexion", "Veuillez entrer un email et un mot de passe.");
            return;
        }

        try {
            // Hash the entered password
            String hashedPassword = hashPassword(password);

            // Authenticate user through DAO
            User authenticatedUser = userDAO.authenticate(email, hashedPassword);

            if (authenticatedUser != null) {
                // Store the authenticated user in the session
                ma.enset.demo.utils.SessionManager.getInstance().setCurrentUser(authenticatedUser);

                // Successful login, load the chatbot view
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/ma/enset/demo/chatbot-view.fxml"));
                Parent root = loader.load();

                emailField.getScene().setRoot(root); // Switch to the chatbot screen
            } else {
                // Invalid credentials, show alert
                showAlert("Erreur de connexion", "Email ou mot de passe incorrect.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur de connexion", "Une erreur est survenue. Veuillez réessayer plus tard.");
        }
    }

    // Method to hash the password using SHA-256
    private String hashPassword(String password) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = messageDigest.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();

            for (byte b : hashBytes) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Method to show an alert
    private void showAlert(String title, String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void goTosignup() {
        try {
            // Load the signup page FXML
            URL signupFXML = getClass().getResource("/ma/enset/demo/signup.fxml");
            if (signupFXML == null) {
                System.err.println("Signup FXML not found!");
                return;
            }

            AnchorPane signupPane = FXMLLoader.load(signupFXML);
            emailField.getScene().setRoot(signupPane); // Replaces current scene root with signup page
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}