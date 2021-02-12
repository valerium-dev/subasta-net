package client;/* EE422C Final Project submission by
 * Carlos A Borja
 * cab6523
 * 16295
 * Spring 2020
 */

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;

import java.io.File;

public class InfoViewController {
    
    private AuctionItem item;
    
    public void setAuctionItem (AuctionItem item) {
        this.item = item;
    }
    
    @FXML HBox parentContainer;
    
    @FXML ImageView galleryImage;
    @FXML Button prevImage;
    @FXML Text imageNum;
    @FXML Button nextImage;
    
    @FXML Text itemName;
    @FXML Text currentBid;
    @FXML Text countdown;
    @FXML Text description;
    @FXML Text sellerID;
    @FXML TextField bidAmount;
    @FXML Text buyNowPrice;
    @FXML Button bidButton;
    @FXML Text bidStatus;
    
    @FXML
    protected void bidOnItem(ActionEvent event) {
        String givenBid = bidAmount.getText();
        bidStatus.setOpacity(1.0);
    
        if (givenBid.isEmpty()) {
            bidStatus.setText("Bid cannot be empty");
        }
        if (!givenBid.matches("^[0-9]+(\\.[0-9]{1,2})?$")) {
            bidStatus.setText("Please enter a valid bid.");
            return;
        }
        if (!(Double.parseDouble(givenBid) > item.getCurrentBid())) {
            bidStatus.setText("Bid must be greater than current bid!");
            return;
        }
    
        bidStatus.setText("Sending bid...");
        sendBid(Double.parseDouble(givenBid), item);
    }
    
    public void initialize() {
        galleryImage.fitHeightProperty().bind(parentContainer.heightProperty().multiply(0.8));
        galleryImage.fitWidthProperty().bind(parentContainer.widthProperty().multiply(0.5));
        
        Platform.runLater(() -> {
            itemName.setText(item.getName());
            bidStatus.setOpacity(0.0);
            countdown.setOpacity(0.0);//countdown.setText("Time Left: Needs implementation - Double: " + item
            // .getTimeLeft());
            description.setText("Description: \n" + item.getDescription());
            sellerID.setText("Seller: " + item.getSeller());
            imageNum.setText("1 of " + item.getImageURLs().length);
            currentBid.textProperty().bind(item.topBidProperty());
            buyNowPrice.textProperty().bind(item.buyNowProperty());
            bidAmount.promptTextProperty().bind(item.promptTextProperty());
            bidAmount.disableProperty().bind(item.isSoldProperty());
            bidButton.disableProperty().bind(item.isSoldProperty());
            itemName.wrappingWidthProperty().bind(parentContainer.widthProperty().multiply(0.4));
            description.wrappingWidthProperty().bind(parentContainer.widthProperty().multiply(0.4));
            galleryImage.setImage(new Image(new File("client/images/MissingFile.png").toURI().toString()));
        });
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
