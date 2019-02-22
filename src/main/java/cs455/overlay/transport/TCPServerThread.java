package main.java.cs455.overlay.transport;

import java.io.*;
import java.net.*;
import java.util.concurrent.LinkedBlockingQueue;
import main.java.cs455.overlay.node.Node;
import main.java.cs455.overlay.node.Registry;

public class TCPServerThread implements Runnable {

  public ServerSocket serverSocket;
  public Socket currentSocket;
  //public TCPReceiverThread receiver;
  public int port;
  public boolean isRegistry = true;
  public Node node;
  TCPConsumer consumer;
  private final LinkedBlockingQueue<DataMessage> buffer;

  // Default ctor for MessagingNodes which won't have a hardcoded port.
  public TCPServerThread(Node node) {
    this(node, 0);
  }

  public TCPServerThread(Node node, int port) {
    this.node = node;
    this.port = port;
    this.isRegistry = (node instanceof Registry);
    this.buffer = new LinkedBlockingQueue<>();
    try {
      createServerSocket();
    } catch (IOException e) {
      System.err.println(e.getMessage());
    }
  }

  // TODO: Remove testing statements

  /**
   * Creates a ServerSocket for a Node. If the requesting Node is a Registry, it binds the
   * ServerSocket to the port specified from args[]. If it is a MessagingNode, it starts looking for
   * an unbound port at 1024 and incrementing upwards.
   */
  public void createServerSocket() throws IOException {

    // Create unbound server socket
    this.serverSocket = new ServerSocket();

    if (isRegistry) { // Server is being created for Registry
      this.serverSocket = new ServerSocket(this.port);
    } else { // Server is being created for MessagingNode
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

  /**
   * Spins up a TCPReceiverThread to listen for incoming data on its given Socket.
   */
  public void startReceiverThread(TCPReceiverThread receiver) {
    Thread receiverThread = new Thread(receiver, "Receiver Thread");
    receiverThread.start();
  }

  public void startConsumerThread() {
    consumer = new TCPConsumer(this.node, this, buffer);
    Thread consumerThread = new Thread(consumer, "Consumer Thread");
    consumerThread.start();
  }

  /**
   * Establishes a new Socket and sends data to the recipient.
   *
   * @param port the port number of the recipient.
   * @param host the hostname of the recipient.
   * @param bytesToSend the raw data.
   */
  public Socket sendData(int port, String host, byte[] bytesToSend) throws IOException {
    Socket socket = new Socket(host, port);
    this.sendData(bytesToSend, socket);
    return socket;
  }

  /**
   * Sends a data across the socket that has been established previously.
   */
  public synchronized void sendData(byte[] bytesToSend, Socket socket) throws IOException {
    //this.currentSocket = socket;
    TCPSender sender = new TCPSender(socket);
    sender.sendBytes(bytesToSend);
  }

  /**
   * Assumes that data has just been sent, and currentSocket is set.
   */
  public void listenForResponse(Socket socket) throws IOException {
    TCPReceiverThread receiver = new TCPReceiverThread(this.node, socket, this, buffer);
    this.startReceiverThread(receiver);
  }

  @Override
  public void run() {

    while (true) {
      try {
        // Creates a receiver thread from the node instance and socket that gets made from
        // the connection.

        Socket newSocket = this.serverSocket.accept();
        //this.currentSocket = newSocket;
        TCPReceiverThread receiver = new TCPReceiverThread(this.node, newSocket, this, buffer);
        this.startReceiverThread(receiver);
      } catch (IOException e) {
        System.err.println(e.getMessage());
      }

    }
  }
}
