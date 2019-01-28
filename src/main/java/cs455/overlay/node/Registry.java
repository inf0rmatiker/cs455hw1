package main.java.cs455.overlay.node;

import java.util.ArrayList;
import java.io.*;
import java.net.*;
import main.java.cs455.overlay.transport.*;

public class Registry implements Node {

  public final int REGISTRY_PORT;
  public TCPServerThread registryServer;

  public Registry(int portNumber) {
    // Assign port number from args.
    this.REGISTRY_PORT = portNumber;
    this.registryServer = new TCPServerThread(5003);
    this.startServerThread();
  }

  // Starts TCPServerThread's run() method
  public void startServerThread() {
    Thread serverThread = new Thread(this.registryServer);
    serverThread.start();
  }

  public void registerMessagingNode(int nodeId) {}

  public void deregisterMessagingNode(int nodeId) throws IllegalStateException {}

  public boolean isNodeRegistered(int nodeId) {
    return false;
  }

  @Override
  public void onEvent() {}

  public String toString() {
    return String.format("Registry live on port: %d", this.REGISTRY_PORT);
  }

  public static void main(String[] args) throws IOException {

    // Create the registry from the port specified by command-line arg.
    Registry registry = new Registry(Integer.parseInt(args[0]));
    System.out.println(registry);

  }



}
