/* EE422C Final Project submission by
 * Carlos A Borja
 * cab6523
 * 16295
 * Spring 2020
 */
package server;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;

public class ClientHandler implements Runnable {
    private Socket client;
    private int userID;
    private String username;
    private String pwdhash;
    private PrintWriter clientOut;
    private BufferedReader in;
    
    public ClientHandler(Socket client) {
        System.out.println("Handling client on " + client);
        this.client = client;
    }
    
    @Override
    public void run () {
        boolean success = false;
        while (!success) {
            success = setupIO();
        }
        
        while (client.isConnected() && !client.isClosed()) {
            try {
                String command = in.readLine();
                parseAndExecute(command);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    private void parseAndExecute(String command) {
        if (command == null)
            return;
        
        System.out.println("Executing " + command);
        switch (command) {
            case "login":
                getCredentials();
                int auth_val = authenticateUser();
                sendAuthResult(auth_val);
                sendUserData(auth_val);
                break;
            case "getItemDB":
                sendItems();
                break;
            case "bidUpdate":
                ArrayList<Number> bidInfo = receiveBidInfo();
                String result = updateItem(bidInfo);
                boolean changed = sendBidResult(result);
                System.out.println(updateObservers(changed, bidInfo) + " observers notified.");
                break;
            case "wait":
                try {
                    client.setKeepAlive(true);
                    Server.getItemObservers().add(this);
                } catch (SocketException e) {
                    e.printStackTrace();
                }
                break;
        }
    }
    
    // BID FUNCTIONS
    
    private ArrayList<Number> receiveBidInfo() {
        Number itemID;
        Number bid;
        try {
            itemID = Integer.parseInt(in.readLine());
            bid = Double.parseDouble(in.readLine());
        } catch (IOException e) {
            clientOut.println("An error occurred. Please try again. (code=CH_rBI_0)");
            return null;
        }
        
        ArrayList<Number> nums = new ArrayList<>();
        nums.add(itemID);
        nums.add(bid);
        return nums;
    }
    
    private String updateItem(ArrayList<Number> bid) {
        if (bid == null) {
            return "";
        }
        Integer itemID = (Integer) bid.get(0);
        AuctionItem item = Server.getItems().get(itemID);
        Double amount = (Double) bid.get(1);
        return item.updateBid(amount, username);
    }
    
    private boolean sendBidResult(String result) {
        if (result.isEmpty()) {
            clientOut.println("An error occurred. (code=CH_sBR_1)");
            return false;
        }
        if (result.equals("ACCEPT") || result.equals("REJECT") || result.equals("SOLD")) {
            clientOut.println(result);
            return result.equals("ACCEPT") || result.equals("SOLD");
        } else {
            clientOut.println("Oops! You can't bid against yourself.");
            return false;
        }
    }
    
    private int updateObservers(boolean changed, ArrayList<Number> bidInfo) {
        if (!changed) {
            return 0;
        }
        
        int updatedClients = 0;
        Integer itemID = (Integer) bidInfo.get(0);
        AuctionItem item = Server.getItems().get(itemID);
        
        for (ClientHandler client : Server.getItemObservers()) {
            client.getClientOut().println("ITEM_UPDATE");
            client.getClientOut().println(item.getID());
            client.getClientOut().println(item.getCurrentBid());
            client.getClientOut().println(item.getBuyNowPrice());
            client.getClientOut().println(item.getSoldTo());
            updatedClients++;
        }
        
        return updatedClients;
    }
    
    // CONNECT FUNCTIONS
    
    private boolean setupIO() {
        boolean success = true;
        try {
            clientOut = new PrintWriter(client.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(client.getInputStream()));
        } catch (IOException e) {
            success = false;
        }
        
        return success;
    }
    
    private void sendItems() {
        int numItems = Server.getItems().values().size();
        clientOut.println(numItems);
        System.out.print("Sending item database to client...");
        Gson gson = new Gson();
        for (AuctionItem item : Server.getItems().values()) {
            clientOut.println(gson.toJson(item));
        }
        System.out.println("done");
    }
    
    // LOGIN FUNCTIONS
    
    private void getCredentials() {
        System.out.print("Getting credentials from client...");
        try {
            username = in.readLine();
            System.out.println("Got username: " + username);
            pwdhash = in.readLine();
            System.out.println("Got pwdhash: " + pwdhash);
        } catch (IOException e) {
            System.out.println("Could not get user credentials.");
        }
        System.out.println("done");
    }
    
    private int authenticateUser() {
        System.out.println("Authenticating user...");
        System.out.print("Username: " + username);
        System.out.println(" | Hash: " + pwdhash);
        
        // TODO: Implement SQL queries
        // userID = queryDB(username)
        // hash = (bool) queryDB(userID, pwdhash)
        // if uID > 0 && hash
            // return 0;
        // if !hash
            // return 1;
        // if uID <= 0
            // return 2;
        return 0;
    }
    
    // TODO: After SQL Implementation
    private void sendUserData(int auth_val) {
        // get extra data from tables linked in user.loginInfo
        if (auth_val != 0) {
            return;
        }
        clientOut.println("USER_DATA");
    }
    
    private void sendAuthResult(int errorCode) {
        switch (errorCode) {
            case 0:
                clientOut.println("ACCEPT");
                break;
            case 1:
                clientOut.println("BADPASSWORD");
                break;
            case 2:
                clientOut.println("NOUSER");
                break;
            default:
                break;
        }
    }
    
    // GETTERS & SETTERS
    public BufferedReader getIn () {
        return in;
    }
    
    public Socket getClient () {
        return client;
    }
    
    public void setClient (Socket client) {
        this.client = client;
    }
    
    public PrintWriter getClientOut () {
        return clientOut;
    }
}
