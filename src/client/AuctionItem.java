package client;/* EE422C Final Project submission by
 * Carlos A Borja
 * cab6523
 * 16295
 * Spring 2020
 */

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.text.DecimalFormat;

public class AuctionItem {
    private int id;
    private String name;
    private String description;
    private String[] imageURLs;
    
    /* TODO: use the duration to calculate the
        exact time when the item will run out.
        This will enable the client to keep an
        accurate countdown even when downloading
        Requirements:
            Current time + duration = endTime (final)
    */
    private int duration;
    private double timeLeft;
    
    private double minPrice;
    private double buyNowPrice;
    private String seller;
    private double currentBid;
    private String soldTo;
    
    
    private BooleanProperty isSold = new SimpleBooleanProperty();
    private StringProperty topBidText = new SimpleStringProperty();
    private StringProperty promptText = new SimpleStringProperty();
    private StringProperty buyNowText = new SimpleStringProperty();
    
    public void initProperties() {
        setIsSold(!soldTo.isEmpty());
        setCurrentBid(currentBid);
    }
    
    /* Observable Property Methods */
    public BooleanProperty isSoldProperty() {
        return isSold;
    }
    
    public void setIsSold(boolean status) {
        isSold.setValue(status);
    }
    
    public Boolean getIsSold() {
        return isSold.getValue();
    }
    
    public StringProperty topBidProperty() {
        return topBidText;
    }
    
    public void setTopBid(double bid) {
        topBidText.setValue("Top Bid: $" + new DecimalFormat("0.00").format(bid));
    }
    
    public String getTopBid() {
        return this.topBidText.getValue();
    }
    
    public StringProperty promptTextProperty() {
        return promptText;
    }
    
    public void setPromptText(double bid) {
        if (bid >= buyNowPrice || !soldTo.isEmpty()) {
            promptText.setValue("");
        } else {
            promptText.setValue("$" + new DecimalFormat("0.00").format(bid + 0.01));
        }
    }
    
    public String getPromptText() {
        return promptText.getValue();
    }
    
    public StringProperty buyNowProperty() {
        return buyNowText;
    }
    
    public void setBuyNowText(double price) {
        if (!soldTo.isEmpty()) {
            buyNowText.setValue("Auction is over!");
            return;
        }
        buyNowText.setValue("BuyNow Price: $" + new DecimalFormat("0.00").format(price));
    }
    
    public String getBuyNowText() {
        return this.buyNowText.getValue();
    }
    
    /* Getters and Setters for regular class members */
    public double getCurrentBid() {
        return currentBid;
    }
    
    public int getID() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public double getBuyNowPrice () {
        return buyNowPrice;
    }
    
    @Override
    public String toString () {
        return name;
    }
    
    public void setCurrentBid (double bid) {
        setTopBid(bid);
        setPromptText(bid);
        setBuyNowText(buyNowPrice);
    }
    
    public String getDescription () {
        return description;
    }
    
    public String[] getImageURLs () {
        return imageURLs;
    }
    
    public String getSeller () {
        return seller;
    }
    
    public double getTimeLeft () {
        return timeLeft;
    }
    
    public String getSoldTo () {
        return soldTo;
    }
    
    public void setSoldTo (String soldTo) {
        setIsSold(!soldTo.isEmpty());
        this.soldTo = soldTo;
    }
    
    public void setBuyNowPrice (double buyNowPrice) {
        setBuyNowText(buyNowPrice);
        this.buyNowPrice = buyNowPrice;
    }
}
