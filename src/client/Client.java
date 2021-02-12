package client;/* EE422C Final Project submission by
 * Carlos A Borja
 * cab6523
 * 16295
 * Spring 2020
 */

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Optional;

import static javafx.fxml.FXMLLoader.load;

public class Client extends Application {
    private final static String HOST = "valerium.dev";
    private final static int PORT = 4321;
    public static String username;
    private static UpdaterService updater;
    private static ObservableList<AuctionItem> items = FXCollections.observableArrayList();
    private static boolean isGuest;
    private static ArrayList<Object> streams;
    
    @Override
    public void init () throws Exception {
        super.init();
        streams = setupIO();
        updater = new UpdaterService();
        updater.setItems(items);
        updater.setOnFailed(event -> {
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setTitle("Connection Error");
            a.setHeaderText("Could not connect to server.");
            a.setContentText("Please check your internet connection then restart the program.");
            a.initOwner(updater.getCurrentStage());
            a.initModality(Modality.WINDOW_MODAL);
            
            ButtonType exit = new ButtonType("Exit");
            a.getButtonTypes().clear();
            a.getButtonTypes().addAll(exit);
            Optional<ButtonType> option = a.showAndWait();

            if (option.isPresent() && option.get() == exit) {
                Platform.exit();
                System.exit(0);
            }
        });
        updater.start();
        
        StartupService itemDownload = new StartupService();
        itemDownload.setItems(items);
        itemDownload.setOnSucceeded(event -> System.out.println("Downloaded items!"));
        itemDownload.start();
    }
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        updater.setCurrentStage(primaryStage);
        Parent root = load(getClass().getResource("client/LoginView.fxml"));
        primaryStage.setTitle("Subasta-Net");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }
    
    @Override
    public void stop () throws Exception {
        super.stop();
        updater.cancel();
        Socket s = (Socket) streams.get(0);
        s.close();
    }
    
    public static void main(String[] args) {
        launch();
    }
    
    /**
     * Connects to the server and creates input and output streams
     * based on the connection. Mostly used by services in the
     * background.
     * @return A list with the socket, input, and output for communicating
     *         with the server, in that order.
     */
    public static ArrayList<Object> setupIO () throws IOException {
        ArrayList<Object> streams = new ArrayList<>();
        Socket connection = new Socket(HOST, PORT);
        connection.setKeepAlive(true);
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        PrintWriter out = new PrintWriter(connection.getOutputStream(), true);
        streams.add(connection);
        streams.add(in);
        streams.add(out);
        return streams;
    }
    
    public static ArrayList<Object> getStreams () {
        return streams;
    }
    
    public static String getHOST () {
        return HOST;
    }
    
    public static int getPORT () {
        return PORT;
    }
    
    public static void setGuest (boolean guest) {
        isGuest = guest;
    }
    
    public static boolean isGuest () {
        return isGuest;
    }
    
    public static ObservableList<AuctionItem> getItems () {
        return items;
    }
    
    public static UpdaterService getUpdater () { return updater; }
}