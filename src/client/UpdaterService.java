package client;

import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class UpdaterService extends Service<AuctionItem> {
    
    private ObservableList<AuctionItem> items;
    private Socket server;
    private Stage currentStage;
    
    public void setItems(ObservableList<AuctionItem> items) {
        this.items = items;
    }
    
    public void setCurrentStage(Stage stage) { currentStage = stage; }
    
    public Stage getCurrentStage() {return currentStage;}
    
    @Override
    protected Task<AuctionItem> createTask () {
        return new Task<AuctionItem> () {
            @Override
            protected AuctionItem call () throws Exception {
                server = new Socket(Client.getHOST(), Client.getPORT());
                server.setKeepAlive(true);
                BufferedReader in = new BufferedReader(new InputStreamReader(server.getInputStream()));
                PrintWriter out = new PrintWriter(server.getOutputStream(), true);
                
                out.println("wait");
                AuctionItem item;
                
                while(true) {
                    if (in.readLine().equals("ITEM_UPDATE")) {
                        int itemID = Integer.parseInt(in.readLine());
                        double currentBid = Double.parseDouble(in.readLine());
                        double buyNow = Double.parseDouble(in.readLine());
                        String sold = in.readLine();
                        item = updateAuctionItem(itemID, currentBid, buyNow, sold);
                        System.out.println("Updated " + item);
                    }
                }
            }
    
            @Override
            protected void failed () {
                super.failed();
                if (server != null) {
                    try {
                        server.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
    
            @Override
            protected void cancelled () {
                super.cancelled();
                if (server != null) {
                    try {
                        server.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
    
            private AuctionItem updateAuctionItem(int id, double bid, double buyNow, String user) {
                AuctionItem item = null;
                for (AuctionItem auctionItem : items) {
                    item = auctionItem;
                    if (item.getID() == id) {
                        item.setSoldTo(user);
                        item.setBuyNowPrice(buyNow);
                        item.setCurrentBid(bid);
                        break;
                    }
                }
                return item;
            }
        };
    }
}
