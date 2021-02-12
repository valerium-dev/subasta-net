/* EE422C Final Project submission by
 * Carlos A Borja
 * cab6523
 * 16295
 * Spring 2020
 */
package server;

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
    private String soldTo = "";
    
    @Override
    public String toString () {
        return name + ", $"  + new DecimalFormat("#.00").format(currentBid);
    }
    
    public int getID() {
        return id;
    }
    
    public void initialize() {
        currentBid = minPrice;
        buyNowPrice = currentBid * 1.3;
    }
    
    public double getBuyNowPrice () {
        return buyNowPrice;
    }
    
    public String getSoldTo () {
        return soldTo;
    }
    
    /**
     * Updates the bid on current object if and only if bid value is greater than current
     * and bidding user is not the current winning bidder. Only one customer can bid at a time.
     * @param price The new bid value
     * @param username The username of the bidder
     * @return true if successful in updating bid, false otherwise.
     */
    public synchronized String updateBid(double price, String username) {
        if (username.equals(soldTo)) {
            // Cannot allow same person to bid against themselves
            return "SAME_PERSON";
        }
        
        if (!soldTo.equals("")) {
            return "DONE";
        }
        
        if (price > currentBid) {
            this.currentBid = price;
            if (price >= buyNowPrice) {
                this.soldTo = username;
                return "SOLD";
            } else {
                buyNowPrice = currentBid * 1.3;
                return "ACCEPT";
            }
        }
        
        return "REJECT";
    }
    
    public double getCurrentBid () {
        return currentBid;
    }
}