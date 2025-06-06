package ma.enset.demo.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
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

public class SignupController implements Initializable {

    @FXML
    private TextField nameField;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

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
    public void handleSignup() {
        String name = nameField.getText();
        String email = emailField.getText();
        String password = passwordField.getText();
        String confirm = confirmPasswordField.getText();

        // Validate inputs
        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
            showAlert(AlertType.WARNING, "Inscription incomplète", "Veuillez remplir tous les champs.");
            return;
        }

        if (!password.equals(confirm)) {
            showPasswordMismatchAlert();
            return;
        }

        // Check if user with this email already exists
        User existingUser = userDAO.findByEmail(email);
        if (existingUser != null) {
            showAlert(AlertType.WARNING, "Email déjà utilisé",
                    "Un compte existe déjà avec cette adresse email. Veuillez utiliser une autre adresse.");
            return;
        }

        try {
            // Hash the password using SHA-256
            String hashedPassword = hashPassword(password);

            if (hashedPassword != null) {
                // Create user object
                User newUser = new User(name, email, hashedPassword);

                // Save user to database through DAO
                boolean created = userDAO.create(newUser);

                if (created) {
                    // Show success message
                    showSignupSuccessAlert();

                    // Navigate to the login screen after successful signup
                    goToLogin();
                } else {
                    showAlert(AlertType.ERROR, "Erreur d'inscription",
                            "Une erreur est survenue lors de la création du compte. Veuillez réessayer.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Erreur d'inscription",
                    "Une erreur est survenue. Veuillez réessayer plus tard.");
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

    // Show alert when passwords don't match
    private void showPasswordMismatchAlert() {
        showAlert(AlertType.WARNING, "Mot de passe incorrect",
                "Les mots de passe ne correspondent pas. Veuillez réessayer.");
    }

    // Show alert when signup is successful
    private void showSignupSuccessAlert() {
        showAlert(AlertType.INFORMATION, "Inscription réussie",
                "Vous êtes maintenant inscrit. Vous pouvez vous connecter.");
    }

    // Generic method to show alerts
    private void showAlert(AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void goToLogin() {
        try {
            // Load the login page FXML
            URL loginFXML = getClass().getResource("/ma/enset/demo/login.fxml");
            if (loginFXML == null) {
                System.err.println("Login FXML not found!");
                return;
            }

            AnchorPane loginPane = FXMLLoader.load(loginFXML);
            nameField.getScene().setRoot(loginPane); // Replaces current scene root
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}