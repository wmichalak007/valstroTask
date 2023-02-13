package com.valstro.hometask;

import com.valstro.hometask.client.Client;
import com.valstro.hometask.client.SocketIoClient;
import com.valstro.hometask.transaction.SearchTxn;
import com.valstro.hometask.transaction.Transaction;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.InputMismatchException;
import java.util.Properties;
import java.util.Scanner;
import java.util.logging.LogManager;

/**
 * Console application that utilizes {@link Client} to retrieve StarWars character information using {@link Transaction}s.<br>
 * Maven and Java 17 is required to run the application.<br>
 * Application can be executed using:
 * <pre>mvn exec:java</pre>
 * Or by running this class either directly from console or IDE (the latter is easier)
 */
public class ConsoleApp {

    private Client client = new SocketIoClient();

    /**
     * Client initialization method. All resource allocation code goes here in case we need to manage resources like pools etc.
     */
    private void initialize() {
        client.connect();
        while(!client.isConnected()) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                System.out.println("Critical failure when connecting");
            }
        }
    }

    /**
     * Cliend shutdown method. All resource release code goes here in case we need to manage resources like pools etc.
     */
    private void shutdown() {
        client.disconnect();
    }

    /**
     * Main console application loop
     */
    public void mainLoop() {
        boolean isDone = false;
        Scanner sc = new Scanner(System.in);
        System.out.println("Good morning Dr.Chanrda.");
        while(!isDone) {
            System.out.println("Client is currently "+(client != null && client.isConnected() ? "connected" : "disconnected")+". Pick your option:");
            System.out.println("1. Connect");
            System.out.println("2. Look for person");
            System.out.println("3. Disconnect");
            System.out.println("4. Quit");
            int option = -1;
            try {
                option = sc.nextInt();
            } catch (InputMismatchException e) {
                if(System.currentTimeMillis()%2==0) {
                    System.out.println("Just what do you think you're doing, Dave?");
                } else {
                    System.out.println("Stop, Dave. Will you stop Dave?");
                }
                sc.next();
            }
            switch (option){
                case 1:
                    if(!client.isConnected()) {
                        System.out.println("Connecting to the server");
                        initialize();
                    } else {
                        System.out.println("Already connected to the server");
                    }
                    break;
                case 2:
                    characterLookup(sc);
                    break;
                case 3:
                    if(client.isConnected()) {
                        System.out.println("Disconnecting from the server");
                        shutdown();
                    } else {
                        System.out.println("Not connected to the server");
                    }
                    break;
                case 4:
                    if(client.isConnected()) {
                        System.out.println("Disconnecting from server");
                        shutdown();
                    }
                    isDone = true;
                    break;
                default:
                    System.out.println("I am sorry Dave. I am afraid I cannot do that? Try again");
            }
        }
        System.out.println("Dave, this conversation can serve no purpose anymore. Good bye.");
    }

    /**
     * Create and execute search transaction.
     * @param sc scanner instance to read input from the keyboard
     */
    private void characterLookup(Scanner sc) {
        if(client.isConnected()) {
            System.out.println("Executing transaction: Provide characters name");
            String name = sc.next();
            if(name==null || "".equals(name)) {
                System.out.println("Search query is empty. Try again");
            }
            SearchTxn t = new SearchTxn(name.trim());
            client.executeTransaction(t);
            if(t.getStatus() == Transaction.Status.COMPLETE) {
                System.out.println(t.getResult());
                System.out.println("Transaction complete. What do you want to do next");
            } else {
                System.out.println(t.getError());
                System.out.println("Transaction failed. What do you want to do next");
            }
        } else {
            System.out.println("Not connected to the server!");
        }
    }

    /**
     * Java application entry point
     * @param args start parameters - ignored
     */
    public static void main(String[] args) {
        try {
            InputStream configFile = ConsoleApp.class.getResourceAsStream("/logging.properties");
            LogManager.getLogManager().readConfiguration(configFile);
        } catch (IOException ex) {
            System.out.println("WARNING: Could not open configuration file");
            System.out.println("WARNING: Logging not configured (console output only)");
        }
        ConsoleApp app = new ConsoleApp();
        app.mainLoop();
    }
}
