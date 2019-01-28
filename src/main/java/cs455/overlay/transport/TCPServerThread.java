package main.java.cs455.overlay.transport;

import java.io.*;
import java.net.*;

public class TCPServerThread implements Runnable {

  public ServerSocket serverSocket;
  public TCPSender sender;
  public TCPReceiverThread receiver;
  public int port;
  public boolean isRegistry = true;

  // Default ctor for MessagingNodes which won't have a hardcoded port.
  public TCPServerThread() {
    this(0);
  }

  public TCPServerThread(int port) {
    // If port comes in as 0, we are a MessagingNode.
    this.isRegistry = port == 0 ? false : true;
    this.port = port;
    try {
      createServerSocket();
    } catch (IOException e) {
      System.err.println(e.getMessage());
    }
  }

  // TODO: Remove testing statements
  public void createServerSocket() throws IOException {

    // Create unbound server socket
    this.serverSocket = new ServerSocket();

    if (isRegistry) { // Server is being created for Registry
      this.serverSocket = new ServerSocket(this.port);
    }
    else { // Server is being created for MessagingNode
      this.port = 1024;
      while (!serverSocket.isBound()) {
        System.out.println("MessagingNode looking for a valid port...");

        // Try to bind the ServerSocket to this.port, if it fails, increment the port by 1.
        try {
          this.serverSocket.bind(new InetSocketAddress(this.port));
        } catch (SocketException e) {
          System.out.printf("Port number: %d was already taken!\n", this.port);
          ++this.port;
        }
      }
    }

    if (serverSocket.isBound()) {
      String whichNode = (this.isRegistry) ? "Registry" : "MessagingNode";
      System.out.printf("%s successfully bound to port number: %d.\n", whichNode, this.port);
    }
  }

  public void startReceiverThread() {
    Thread receiverThread = new Thread(receiver);
    receiverThread.start();
  }

  public void sendData(int port, String host, int dataLength) throws IOException {
    this.sender = new TCPSender(new Socket(host, port));
    this.sender.sendData(dataLength);
  }

  @Override
  public void run() {

    while (true) {
      // Create a receiver thread when we receive a connection.
      try {
        this.receiver = new TCPReceiverThread(this.serverSocket.accept());
        this.startReceiverThread();
      } catch (IOException e) {
        System.err.println(e.getMessage());
      }

    }
  }
}
