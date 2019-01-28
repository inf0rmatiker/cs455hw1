package main.java.cs455.overlay.node;

import java.io.*;
import java.net.*;
import main.java.cs455.overlay.transport.TCPServerThread;

public class MessagingNode implements Node {

  public int REGISTRY_PORT;
  public String REGISTRY_HOST;
  public TCPServerThread messagingServer;

  // Default ctor incase registry host and port were not specified
  public MessagingNode() {
    this("denver", 5000);
  }

  // Overloaded ctor made from args[]
  public MessagingNode(String registryHost, int registryPort) {
    this.REGISTRY_HOST = registryHost;
    this.REGISTRY_PORT = registryPort;

    this.messagingServer = new TCPServerThread();
    this.startServerThread();
  }

  // Starts TCPServerThread's run() method
  public void startServerThread() {
    Thread serverThread = new Thread(this.messagingServer);
    serverThread.start();
  }

  @Override
  public void onEvent() {

  }

  public static void main(String[] args) throws IOException {
    MessagingNode messagingNode = new MessagingNode(args[0], Integer.parseInt(args[1]));

    // Testing send data '24' to Registry node
    messagingNode.messagingServer.sendData(messagingNode.REGISTRY_PORT,
        messagingNode.REGISTRY_HOST, messagingNode.messagingServer.port);

  }
}
