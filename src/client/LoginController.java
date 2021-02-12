package client;/* EE422C Final Project submission by
 * Carlos A Borja
 * cab6523
 * 16295
 * Spring 2020
 */

import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;


import java.io.File;
import java.io.IOException;

public class LoginController {
    
    @FXML VBox parent;
    @FXML ImageView imageView;
    @FXML Text errorMessage;
    @FXML TextField username;
    @FXML PasswordField password;
    @FXML Button loginButton;
    @FXML Hyperlink guestLogin;
    
    @FXML
    protected void loginAsGuestAction (ActionEvent actionEvent) {
        loginAction(actionEvent);
    }
    
    @FXML
    protected void loginAction (ActionEvent actionEvent) {
        Stage login = (Stage) parent.getScene().getWindow();
        
        if (actionEvent.getSource().equals(guestLogin)) {
            Client.setGuest(true);
            login.close();
            showHomeView();
            return;
        }
        
        errorMessage.setOpacity(1.0);
        errorMessage.setText("");
        
        String uname = username.getText();
        String pwd = password.getText();
        if (uname.isEmpty() || pwd.isEmpty()){
            errorMessage.setText(uname.isEmpty() ? "Username cannot be blank." : "Please enter a password.");
        } else {
            LoginService lservice = new LoginService();
            lservice.setUsername(username.getText());
            lservice.setPassword(password.getText());
            lservice.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle (WorkerStateEvent event) {
                    login.close();
                    Client.username = username.getText();
                    showHomeView();
                }
            });
            errorMessage.textProperty().bind(lservice.messageProperty());
            lservice.start();
        }
    }
    
    public void initialize() {
        imageView.setImage(new Image(new File("client/images/ServiceLogo.png").toURI().toString()));
    }
    
    private void showHomeView() {
        Parent root = null;
        try {
            root = FXMLLoader.load(getClass().getResource("../../Final Exam Source Code/HomeView.fxml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        assert(root != null);
        Stage stage = new Stage();
        stage.setScene(new Scene(root));
        stage.setTitle(Client.username + "'s Main Page - Subasta-Net");
        Client.getUpdater().setCurrentStage(stage);
        stage.show();
    }
}
