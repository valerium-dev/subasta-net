/* EE422C Final Project submission by
 * Carlos A Borja
 * cab6523
 * 16295
 * Spring 2020
 */
package server;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class Server {
    private static ServerSocket serverSocket;
    private static HashMap<Integer, AuctionItem> items;
    private static final int PORT = 4321;
    private static CopyOnWriteArrayList<ClientHandler> observers;
    
    public static void main(String[] args) {
        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("Port " + PORT + " opened on " + serverSocket.getInetAddress());
        } catch (IOException e) {
            System.out.println("Could not open port " + PORT);
            e.printStackTrace();
        }
        System.out.println("Server is now online :3");
        
        readInItems();
        observers = new CopyOnWriteArrayList<>();
        
        while (true) {
            observers.removeIf(clientHandler -> clientHandler.getClient().isClosed());
            Socket clientSocket = null;
            
            try {
                clientSocket = serverSocket.accept();
                clientSocket.setKeepAlive(true);
                System.out.println("Got a connection!");
            } catch (IOException e) {
                e.printStackTrace();
            }
            
            ClientHandler client = new ClientHandler(clientSocket);
            Thread t = new Thread(client);
            t.start();
        }
        
    }
    
    private static void readInItems() {
        Gson g = new Gson();
        items = new HashMap<>();
        File itemsDir = new File("items");
        for (File f : Objects.requireNonNull(itemsDir.listFiles())) {
            try {
                AuctionItem item = g.fromJson(new FileReader(f.getAbsoluteFile()), AuctionItem.class);
                item.initialize();
                items.put(item.getID(), item);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
    
    public static Map<Integer, AuctionItem> getItems (){
        return items;
    }
    
    public static CopyOnWriteArrayList<ClientHandler> getItemObservers() { return observers; }
}
