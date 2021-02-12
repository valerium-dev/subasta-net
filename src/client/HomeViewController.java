package client;/* EE422C Final Project submission by
 * Carlos A Borja
 * cab6523
 * 16295
 * Spring 2020
 */

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.File;
import java.io.IOException;

public class HomeViewController {
    
    @FXML Text buyNowPrice;
    @FXML Text itemName;
    @FXML Button moreInfo;
    @FXML TextField bidAmount;
    @FXML Text bidStatus;
    @FXML ImageView primaryImage;
    @FXML Text countdown;
    @FXML Text currentTopBid;
    @FXML ListView<AuctionItem> itemListView;
    @FXML SplitPane splitPane;
    @FXML StackPane container;
    @FXML VBox infoBox;
    @FXML VBox labels;
    @FXML VBox bidInfo;
    @FXML HBox controls;
    @FXML Button bid;
    @FXML Button quitButton;
    
    @FXML
    protected void updateInfoBox(MouseEvent event) {
        if (itemListView.getItems().isEmpty()) {
            return;
        }
        if (controls.isDisabled() && !Client.isGuest()) {
            controls.setDisable(false);
        }
        moreInfo.setDisable(false);
        AuctionItem selection = itemListView.getSelectionModel().getSelectedItem();
        itemName.setText(selection.getName());
        countdown.setOpacity(0.0);//countdown.setText("Countdown - Needs Implementation :: Ends in ##days | ##hrs
        // ##min");
        bidAmount.setText("");
        currentTopBid.textProperty().bind(selection.topBidProperty());
        bidAmount.promptTextProperty().bind(selection.promptTextProperty());
        bidAmount.disableProperty().bind(selection.isSoldProperty());
        buyNowPrice.textProperty().bind(selection.buyNowProperty());
        bid.disableProperty().bind(selection.isSoldProperty());
        selection.setCurrentBid(selection.getCurrentBid());
        primaryImage.setImage(new Image(new File(System.getProperty("user.dir") + "/src/client/images/MissingFile" +
                ".png").toURI().toString()));
        
        if (selection.isSoldProperty().getValue()) {
            bidStatus.setOpacity(1.0);
            bidStatus.setText("Sold to: " + selection.getSoldTo());
        } else {
            bidStatus.setOpacity(0.0);
        }
    }
    
    @FXML
    protected void showMoreInfo(ActionEvent event) {
        AuctionItem selection = itemListView.getSelectionModel().getSelectedItem();
        Stage home = Client.getUpdater().getCurrentStage();
        
        Parent root = null;
        FXMLLoader loader = null;
        try {
            loader = new FXMLLoader(getClass().getResource("InfoView.fxml"));
            root = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        assert root != null;
    
        InfoViewController eview = loader.getController();
        eview.setAuctionItem(selection);
        
        Stage stage = new Stage();
        stage.setScene(new Scene(root));
        stage.initOwner(Client.getUpdater().getCurrentStage());
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle (WindowEvent event) {
                Client.getUpdater().setCurrentStage(home);
            }
        });
        Client.getUpdater().setCurrentStage(stage);
        stage.show();
    }
    
    @FXML
    protected void bidOnItem(ActionEvent event) {
        AuctionItem selection = itemListView.getSelectionModel().getSelectedItem();
        String givenBid = bidAmount.getText();
        bidStatus.setOpacity(1.0);
        
        if (givenBid.isEmpty()) {
            bidStatus.setText("Bid cannot be empty");
        }
        if (!givenBid.matches("^[0-9]+(\\.[0-9]{1,2})?$")) {
            bidStatus.setText("Please enter a valid bid.");
            return;
        }
        if (!(Double.parseDouble(givenBid) > selection.getCurrentBid())) {
            bidStatus.setText("Bid must be greater than current bid!");
            return;
        }
        
        bidStatus.setText("Sending bid...");
        sendBid(Double.parseDouble(givenBid), selection);
    }
    
    @FXML
    protected void exitProgram(ActionEvent event) {
        Platform.exit();
        System.exit(0);
    }
    
    public void initialize() {
        if (Client.isGuest()) {
            Tooltip tooltip = new Tooltip();
            tooltip.setText("You must be logged in to place a bid");
            bid.setTooltip(tooltip);
        } else {
            bid.setTooltip(null);
        }
        
        moreInfo.setDisable(true);
        
        itemListView.setItems(Client.getItems());
        
        itemListView.maxWidthProperty().bind(splitPane.widthProperty().multiply(0.25));
        itemListView.minWidthProperty().bind(splitPane.widthProperty().multiply(0.25));
        
        primaryImage.fitWidthProperty().bind(splitPane.widthProperty().multiply(0.75));
        primaryImage.fitHeightProperty().bind(splitPane.heightProperty().multiply(0.333333));
    }
    
    private void sendBid(double bid, AuctionItem item) {
        BiddingService bidservice = new BiddingService();
        bidservice.setBid(bid);
        bidservice.setItem(item);
        bidservice.setOnSucceeded(event -> {
            String status = bidservice.getBidStatus();
            if (status.equals("ACCEPT")) {
                bidStatus.setText("Bid was accepted. Best of luck!");
            } else if (status.equals("REJECT")) {
                bidStatus.setText("Looks like someone beat you to it :(");
            } else if (status.equals("SOLD")) {
                bidStatus.setText("Congratulations! You won the bid!");
            } else {
                bidStatus.setText(status);
            }
            bidAmount.setText("");
        });
        
        bidservice.setOnFailed(event -> bidStatus.setText("A communication error occurred. Please try again."));
        
        bidservice.start();
    }
}
