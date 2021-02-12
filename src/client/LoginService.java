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
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

public class LoginService extends Service<String> {
    private String username;
    private String password;
    
    public void setUsername (String username) {
        this.username = username;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public StringProperty status = new SimpleStringProperty();
    
    public void setStatus (String status) {
        this.status.set(status);
    }
    
    public String getStatus() {
        return status.getValue();
    }
    
    @Override
    protected Task<String> createTask () {
        return new Task<String>() {
            @Override
            protected String call () throws Exception {
                ArrayList<Object> streams = Client.getStreams();
                BufferedReader in = (BufferedReader) streams.get(1);
                PrintWriter out = (PrintWriter) streams.get(2);
    
                updateMessage("Communicating with server...");
                
                out.println("login");
                
                updateMessage("Encrypting password before sending.");
                String salt = generateHash(username, "");
                String hash = generateHash(password, salt);
                
                updateMessage("Sending credentials...");
                out.println(username);
                out.println(hash);
                
                updateMessage("Waiting for auth code...");
                String auth_code = receiveAuthCode(in);
                if (auth_code.equals("ACCEPT")) {
                    updateMessage("Receiving user data...");
                    String something = receiveUserData(in);
                    updateMessage("Received " + something + " !");
                } else {
                    if (auth_code.equals("BADPASSWORD")) {
                        updateMessage("Password was incorrect. Please try again.");
                    } else if (auth_code.equals("NOUSER")) {
                        updateMessage("User not found. Please continue as guest or register on our website!");
                    }
                }
                return auth_code;
            }
        };
    }
    
    private String receiveAuthCode(BufferedReader in) throws IOException {
        return in.readLine();
    }
    
    private String receiveUserData(BufferedReader in) throws IOException {
        // TODO: Download user data as JSON and convert to object
        // return object
        return in.readLine();
    }
    
    /**
     * A function to generate a SHA-512 hash of the text+salt
     * Copied try catch
     * from https://www.geeksforgeeks.org/sha-512-hash-in-java/?ref=rp
     * @param text the text to generate a hash
     * @param salt a randomly generated string
     * @return hash of text + salt
     */
    private String generateHash(String text, String salt) {
        String input = text + salt;
        try {
            // getInstance() method is called with algorithm SHA-512
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            
            // digest() method is called
            // to calculate message digest of the input string
            // returned as array of byte
            byte[] messageDigest = md.digest(input.getBytes());
            
            // Convert byte array into signum representation
            BigInteger no = new BigInteger(1, messageDigest);
            
            // Convert message digest into hex value
            String hashtext = no.toString(16);
            
            // Add preceding 0s to make it 32 bit
            while (hashtext.length() < 32) {
                hashtext = "0" + hashtext;
            }
            
            // return the HashText
            return hashtext;
        }
        
        // For specifying wrong message digest algorithms
        catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
