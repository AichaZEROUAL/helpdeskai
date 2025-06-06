package ma.enset.demo.controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import ma.enset.demo.dao.ConversationDao;
import ma.enset.demo.dao.MessageDao;
import ma.enset.demo.dao.impl.ConversationDAOImpl;
import ma.enset.demo.dao.impl.MessageDAOImpl;
import ma.enset.demo.entities.Conversation;
import ma.enset.demo.entities.Message;
import ma.enset.demo.entities.User;
import ma.enset.demo.utils.DBConnection;
import ma.enset.demo.utils.SessionManager;
import org.kordamp.ikonli.javafx.FontIcon;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class ChatbotController implements Initializable {
    @FXML
    private TextField userInput;

    @FXML
    private VBox chatHistory;

    @FXML
    private Label headerLabel;

    @FXML
    private FontIcon helpIcon;
    @FXML
    private ListView<Conversation> chatTitleList; // Updated to use Conversation objects

    // Current logged-in user
    private User currentUser;

    // Current selected conversation
    private Conversation currentConversation;

    // DAOs for database operations
    private ConversationDao conversationDao;
    private MessageDao messageDao;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize DAOs
        conversationDao = new ConversationDAOImpl();
        messageDao = new MessageDAOImpl();

        // Get current user from session
        currentUser = SessionManager.getInstance().getCurrentUser();

        // Update header to show current user if available
        if (currentUser != null) {
            headerLabel.setText("Assistance Technique - Bienvenue " + currentUser.getFullName());

            // Load user conversations from database
            loadUserConversations();

            // Set up cell factory to display conversation titles
            chatTitleList.setCellFactory(param -> new ListCell<Conversation>() {
                private final Label titleLabel = new Label();
                private final Button deleteButton = new Button();
                private final FontIcon trashIcon = new FontIcon("fas-trash-alt"); // FontAwesome Solid trash icon

                {
                    trashIcon.setIconSize(12);
                    trashIcon.setIconColor(Color.WHITE);
                    deleteButton.setGraphic(trashIcon);
                    deleteButton.setStyle("-fx-background-color: transparent;");
                    deleteButton.setOnAction(event -> {
                        Conversation conversation = getItem();
                        if (conversation != null) {
                            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                            alert.setTitle("Confirmation");
                            alert.setHeaderText("Supprimer la conversation ?");
                            alert.setContentText("Êtes-vous sûr de vouloir supprimer cette conversation ?");

                            Optional<ButtonType> result = alert.showAndWait();
                            if (result.isPresent() && result.get() == ButtonType.OK) {
                                conversationDao.delete(conversation.getId());
                                chatTitleList.getItems().remove(conversation);
                                if (conversation.equals(currentConversation)) {
                                    chatHistory.getChildren().clear();
                                    currentConversation = null;
                                }
                            }
                        }
                    });

                }

                @Override

                protected void updateItem(Conversation conversation, boolean empty) {
                    super.updateItem(conversation, empty);
                    if (empty || conversation == null) {
                        setText(null);
                        setGraphic(null);
                        setStyle("");
                    } else {
                        titleLabel.setText(conversation.getTitle());
                        titleLabel.setStyle("-fx-text-fill: white;");

                        // Create a spacer
                        Region spacer = new Region();
                        HBox.setHgrow(spacer, Priority.ALWAYS); // let it grow to push the delete button

                        HBox container = new HBox(10, titleLabel, spacer, deleteButton);
                        container.setAlignment(Pos.CENTER_LEFT);

                        // Style background based on selection
                        if (isSelected()) {
                            setStyle("-fx-background-color: #2a2a2a;");
                        } else {
                            setStyle("-fx-background-color: transparent;");
                        }

                        setGraphic(container);
                    }
                }


            });

            // Handle click event on a chat to load the selected chat history
            chatTitleList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null) {
                    currentConversation = newValue;
                    loadChatHistory(newValue);
                }
            });

            // Select the first conversation if available
            if (!chatTitleList.getItems().isEmpty()) {
                chatTitleList.getSelectionModel().selectFirst();
            }
        }
    }

    // Method to load user conversations from database
    private void loadUserConversations() {
        if (currentUser != null) {
            List<Conversation> userConversations = conversationDao.findByUserId(currentUser.getId());
            ObservableList<Conversation> conversations = FXCollections.observableArrayList(userConversations);
            chatTitleList.setItems(conversations);
        }
    }

    @FXML
    protected void handleSend() {
        String messageContent = userInput.getText().trim();
        if (!messageContent.isEmpty() && currentConversation != null) {
            // Create and save user message
            Message userMessage = new Message();
            userMessage.setConversationId(currentConversation.getId());
            userMessage.setSender(Message.Sender.USER);
            userMessage.setContent(messageContent);

            if (messageDao.create(userMessage)) {
                displayUserMessage(messageContent);
                userInput.clear();

                // Send to Flask API
                new Thread(() -> {
                    String botResponse = sendQuestionToAPI(messageContent);
                    Platform.runLater(() -> {
                        // Create and save bot message
                        Message botMessage = new Message();
                        botMessage.setConversationId(currentConversation.getId());
                        botMessage.setSender(Message.Sender.CHATBOT);
                        botMessage.setContent(botResponse);

                        if (messageDao.create(botMessage)) {
                            // Display it in UI without re-saving
                            displayBotMessage(botResponse, false,null, botMessage.getId());
                        }
                    });
                }).start();
            }
        } else if (currentConversation == null) {
            // Create new conversation if none exists
            handleNewChat();
            if (!messageContent.isEmpty()) {
                userInput.setText(messageContent);
                handleSend(); // Retry sending
            }
        }
    }

    @FXML
    private void helpmessage(){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Demande d'assistance.");
        alert.setHeaderText(null);
        alert.setContentText("Demande d'assistance envoyée.Un administrateur vous contactera bientôt.");
        alert.showAndWait();
    }

    // Display user message in UI
    private void displayUserMessage(String message) {
        Label messageLabel = new Label(message);
        messageLabel.setStyle(
                "-fx-background-color: #3c3c3c; " +  // Gray background
                        "-fx-text-fill: white; " +
                        "-fx-padding: 10px; " +
                        "-fx-background-radius: 10px;"
        );
        messageLabel.setWrapText(true);
        HBox messageContainer = new HBox(messageLabel);
        messageContainer.setStyle("-fx-padding: 5px;");
        messageContainer.setAlignment(Pos.CENTER_RIGHT);  // Align to the right
        chatHistory.getChildren().add(messageContainer);
    }

    private void displayBotMessage(String message, boolean saveToDb, Integer feedback, Integer  msgId) {

        Message botMessage = new Message();

        if (saveToDb) {

            botMessage.setConversationId(currentConversation.getId());
            botMessage.setSender(Message.Sender.CHATBOT);
            botMessage.setContent(message);

            if (!messageDao.create(botMessage)) {
                System.err.println("Failed to save bot message.");
                return;
            }
        }

        Label messageLabel = new Label(message);
        messageLabel.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-text-fill: white;" +
                        "-fx-padding: 6px 6px 2px 6px;"
        );
        messageLabel.setWrapText(true);

        HBox messageContainer = new HBox(messageLabel);
        messageContainer.setAlignment(Pos.CENTER_LEFT);
        messageContainer.setSpacing(4);

        // Icons
        FontIcon likeIcon = new FontIcon(feedback != null && feedback == 1 ? "fas-thumbs-up" : "far-thumbs-up");
        FontIcon dislikeIcon = new FontIcon(feedback != null && feedback == 0 ? "fas-thumbs-down" : "far-thumbs-down");
        likeIcon.setIconSize(14);
        dislikeIcon.setIconSize(14);
        likeIcon.setIconColor(Color.WHITE);
        dislikeIcon.setIconColor(Color.WHITE);

        Button likeButton = new Button("", likeIcon);
        Button dislikeButton = new Button("", dislikeIcon);
        likeButton.setStyle("-fx-background-color: transparent; -fx-padding: 2;");
        dislikeButton.setStyle("-fx-background-color: transparent; -fx-padding: 2;");

        likeButton.setOnAction(e -> {
            boolean isLikedNow = likeIcon.getIconLiteral().equals("far-thumbs-up");
            if (isLikedNow) {
                likeIcon.setIconLiteral("fas-thumbs-up");
                dislikeIcon.setIconLiteral("far-thumbs-down");
                System.out.println("id"+ msgId);
                messageDao.updateFeedback(msgId, 1);
            } else {
                likeIcon.setIconLiteral("far-thumbs-up");
                messageDao.updateFeedback(msgId, null);
            }
        });

        dislikeButton.setOnAction(e -> {
            boolean isDislikedNow = dislikeIcon.getIconLiteral().equals("far-thumbs-down");
            if (isDislikedNow) {
                dislikeIcon.setIconLiteral("fas-thumbs-down");
                likeIcon.setIconLiteral("far-thumbs-up");
                messageDao.updateFeedback(msgId, 0);
            } else {
                dislikeIcon.setIconLiteral("far-thumbs-down");
                messageDao.updateFeedback(msgId, null);
            }
        });

        HBox feedbackContainer = new HBox(6, likeButton, dislikeButton);
        feedbackContainer.setAlignment(Pos.CENTER_LEFT);
        feedbackContainer.setPadding(new Insets(0, 0, 6, 6));

        VBox botMessageBox = new VBox(2, messageContainer, feedbackContainer);
        chatHistory.getChildren().add(botMessageBox);
    }

    private String sendQuestionToAPI(String question) {
        try {
            System.out.println("Sending question: " + question);
            String jsonInput = String.format("{\"question\": \"%s\"}", question);
            URL url = new URL("http://localhost:5000/ask");
            java.net.HttpURLConnection connection = (java.net.HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            try (java.io.OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonInput.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            StringBuilder responseBuilder = new StringBuilder();
            try (java.io.BufferedReader br = new java.io.BufferedReader(
                    new java.io.InputStreamReader(connection.getInputStream(), "utf-8"))) {
                String line;
                while ((line = br.readLine()) != null) {
                    responseBuilder.append(line.trim());
                }
            }

            // Parse JSON using org.json
            org.json.JSONObject json = new org.json.JSONObject(responseBuilder.toString());
            return json.getString("response");
        } catch (Exception e) {
            e.printStackTrace();
            return "Erreur lors de la communication avec le serveur.";
        }
    }

    // Method to load selected chat history into the center area
    private void loadChatHistory(Conversation conversation) {
        chatHistory.getChildren().clear(); // Clear current messages

        // Get messages for the selected conversation from database
        List<Message> messages = messageDao.findByConversationId(conversation.getId());

        for (Message message : messages) {
            if (message.getSender() == Message.Sender.USER) {
                displayUserMessage(message.getContent());
            } else {
                Integer feed = null;
                if (message.getFeedback() != null) {
                    feed = message.getFeedback() ? 1 : 0;
                }
                displayBotMessage(message.getContent(), false, feed, message.getId());
            }
        }

    }

    @FXML
    private void handleNewChat() {
        if (currentUser == null) {
            return;
        }

        // Prompt user for chat title
        TextInputDialog dialog = new TextInputDialog("Nouvelle conversation");
        dialog.setTitle("Nouvelle conversation");
        dialog.setHeaderText("Veuillez entrer un titre pour cette conversation");
        dialog.setContentText("Titre:");

        Optional<String> result = dialog.showAndWait();

        result.ifPresent(title -> {
            // Create new conversation
            Conversation newConversation = new Conversation();
            newConversation.setUserId(currentUser.getId());
            newConversation.setTitle(title);

            // Save to database
            if (conversationDao.create(newConversation)) {
                // Add to UI list
                chatTitleList.getItems().add(newConversation);
                chatTitleList.getSelectionModel().select(newConversation);
                currentConversation = newConversation;
                chatHistory.getChildren().clear();

                // Add welcome message
                Message welcomeMessage = new Message();
                welcomeMessage.setConversationId(newConversation.getId());
                welcomeMessage.setSender(Message.Sender.CHATBOT);
                welcomeMessage.setContent("Bonjour " + currentUser.getFullName() + "! Comment puis-je vous aider aujourd'hui?");

                if (messageDao.create(welcomeMessage)) {
                    displayBotMessage(welcomeMessage.getContent(),false,null, welcomeMessage.getId());
                }
            }
        });
    }

    @FXML
    private void handleEditTitle() {
        if (currentConversation == null) {
            return;
        }

        TextInputDialog dialog = new TextInputDialog(currentConversation.getTitle());
        dialog.setTitle("Modifier le titre");
        dialog.setHeaderText("Modifier le titre de la conversation");
        dialog.setContentText("Nouveau titre:");

        Optional<String> result = dialog.showAndWait();

        result.ifPresent(title -> {
            // Update conversation title
            currentConversation.setTitle(title);

            // Save to database
            if (conversationDao.updateTitle(currentConversation)) {
                // Refresh ListView
                chatTitleList.refresh();
            }
        });
    }

    // Method to logout the current user
    @FXML
    private void handleLogout() {
        // Clear the user session
        SessionManager.getInstance().clearSession();

        // Close database connection
        DBConnection dbConnection = new DBConnection(); // If you're using a singleton pattern, get the instance instead
        dbConnection.closeConnection();

        try {
            // Navigate back to login screen
            URL loginFXML = getClass().getResource("/ma/enset/demo/login.fxml");
            if (loginFXML == null) {
                System.err.println("Login FXML not found!");
                return;
            }
            javafx.scene.Parent loginPane = FXMLLoader.load(loginFXML);
            userInput.getScene().setRoot(loginPane);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}