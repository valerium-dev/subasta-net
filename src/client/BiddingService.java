package client;/* EE422C Final Project submission by
 * Carlos A Borja
 * cab6523
 * 16295
 * Spring 2020
 */

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

public class BiddingService extends Service<String> {
    
    public StringProperty bidStatus = new SimpleStringProperty();
    
    private AuctionItem item;
    private double bidAmount;
    
    public void setBid(double bidAmount) {
        this.bidAmount = bidAmount;
    }
    
    public void setItem(AuctionItem item) {
        this.item = item;
    }
    
    public final void setBidStatus(String value) {
        bidStatus.setValue(value);
    }
    
    public final String getBidStatus() {
        return bidStatus.getValue();
    }
    
    @Override
    protected Task<String> createTask () {
        final AuctionItem auctionItem = item;
        final String command = "bidUpdate";
        final double amount = bidAmount;
        return new Task<String>() {
            protected String call() throws IOException {
                ArrayList<Object> streams = Client.getStreams();
                BufferedReader in = (BufferedReader) streams.get(1);
                PrintWriter out = (PrintWriter) streams.get(2);
            
                updateMessage("Communicating with server...");
                
                out.println(command);
                out.println(auctionItem.getID());
                out.println(amount);
                
                updateMessage("Bid sent!");
                
                String status = in.readLine();
                setBidStatus(status);
                return status;
            }
        };
    }
}
