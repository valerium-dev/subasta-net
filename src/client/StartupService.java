package client;/* EE422C Final Project submission by
 * Carlos A Borja
 * cab6523
 * 16295
 * Spring 2020
 */

import com.google.gson.Gson;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

public class StartupService extends Service<ObservableList<AuctionItem>> {
    private ObservableList<AuctionItem> items;
    
    public void setItems(ObservableList<AuctionItem> items) {
        this.items = items;
    }
    
    protected Task<ObservableList<AuctionItem>> createTask() {
        final String command = "getItemDB";
        return new Task<ObservableList<AuctionItem>>() {
            protected ObservableList<AuctionItem> call() throws IOException {
                ArrayList<Object> streams = Client.getStreams();
                BufferedReader in = (BufferedReader) streams.get(1);
                PrintWriter out = (PrintWriter) streams.get(2);
                
                out.println(command);
                int numItems = Integer.parseInt(in.readLine());
                
                updateMessage("Beginning download...");
                Gson gson = new Gson();
                while (items.size() != numItems) {
                    AuctionItem item = gson.fromJson(in.readLine(), AuctionItem.class);
                    item.initProperties();
                    items.add(item);
                }
                
                // Update JavaFX
                updateMessage("Done Downloading!");
                return items;
            }
        };
    }
}
