package ma.enset.demo;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class chatApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(chatApplication.class.getResource("login.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 800, 500);
        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
        stage.setTitle("chatbot");
        stage.setScene(scene);
        stage.show();
    }
    public static void main(String[] args) {
        launch();
    }
}