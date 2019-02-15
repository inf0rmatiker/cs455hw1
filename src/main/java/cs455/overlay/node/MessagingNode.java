package main.java.cs455.overlay.node;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import main.java.cs455.overlay.transport.TCPServerThread;
import main.java.cs455.overlay.wireformats.*;

public class MessagingNode implements Node {

  public int REGISTRY_PORT;
  public String REGISTRY_HOST;
  public int messagingPort;
  public String messagingHost;
  public String messagingIpAddress;
  public TCPServerThread messagingServer;
  public boolean isRegistered = false;

  public List<RegistryEntry> nodeList;

  /**
   * Initializes the registry host and port specified from args.
   * Also initializes the TCPServerThread object.
   * @param registryHost The host name of the Registry server.
   * @param registryPort The port number of the Registry server.
   */
  public MessagingNode(String registryHost, int registryPort) throws IOException {
    this.REGISTRY_HOST = registryHost;
    this.REGISTRY_PORT = registryPort;
    this.messagingServer = new TCPServerThread(this);
    this.messagingHost = this.getMessagingHost();
    this.messagingPort = this.getMessagingPort();
    this.messagingIpAddress = this.getMessagingIpAddress();
    this.nodeList = new ArrayList<>();
    this.startServerThread();
    this.sendRegistrationRequest();
  }

  /**
   * Sets the messagingHost field to the local machine's name.
   */
  public String getMessagingHost() {
    String host = "Unknown host";
    try {
      host = this.messagingServer.serverSocket.getInetAddress().getLocalHost().getHostName();
    } catch (UnknownHostException e) {
      System.err.println("Unknown host!");
    }
    return host;
  }

  /**
   * Sets the messagingPort field to the port that the server is bound to.
   */
  public int getMessagingPort() {
    return messagingServer.port;
  }

  public String getMessagingIpAddress() throws IOException {
     return this.messagingServer.serverSocket.getInetAddress().getLocalHost().getHostAddress();
  }

  /**
   * Starts TCPServerThread's thread.
   */
  public void startServerThread() {
    Thread serverThread = new Thread(this.messagingServer, "Messaging Server Thread");
    serverThread.start();
  }

  @Override
  public void onEvent(Event event, Socket socket) throws IOException {
    switch (event.getType()) {
      case REGISTRATION_RESPONSE:
        System.out.println(event);
        RegistrationResponse response = (RegistrationResponse) event;
        if (response.isForDeregistration()) {
          this.handleDeregistrationResponse(response);
        }
        else {
          this.handleRegistrationResponse(response);
        }
        break;
      case MESSAGING_NODES_LIST:
        this.connectToOtherMessagingNodes((MessagingNodesList) event); break;
      case CONNECTION_REQUEST:
        this.handleConnectionRequest((ConnectionRequest) event); break;
      case CONNECTION_RESPONSE:
        this.handleConnectionResponse((ConnectionResponse) event); break;
      default: System.out.println("Unknown event type!");
    }
  }

  /**
   * Establishes socket connections with the nodes in nodeList.
   * @param nodeList The list of nodes that need to have a connection established with
   */
  public void connectToOtherMessagingNodes(MessagingNodesList nodeList) throws IOException {
    // Add all the nodes in the request list to our nodeList
    for (RegistryEntry re: nodeList.nodeList) {
      this.nodeList.add(re);
    }

    for (int i = 0; i < this.nodeList.size(); i++) {
      // Establish a socket by sending a message over
      ConnectionRequest connectionRequest = new ConnectionRequest(this.messagingPort, this.messagingHost,
          this.messagingIpAddress);
      this.messagingServer.sendData(this.nodeList.get(i).portNumber, this.nodeList.get(i).hostName,
          connectionRequest.getBytes());

      // Save the socket that was created when sending data to the RegistryEntry's socket field
      this.nodeList.get(i).socket = this.messagingServer.currentSocket;
    }
  }

  /**
   * Creates a RegistryEntry object to save the socket/connection of the incoming request.
   * @param request The message containing the originating port, host, and IP address of the sender
   * @throws IOException
   */
  public void handleConnectionRequest(ConnectionRequest request) throws IOException {
    RegistryEntry entry = new RegistryEntry(request.getPortNumber(), request.getHostName(),
        request.getIpAddress(), this.messagingServer.currentSocket);

    System.out.printf("Received connection from %s on port %d\n", entry.hostName, entry.portNumber);

    this.sendConnectionResponse(entry.hostName, entry.portNumber, entry.socket);

  }

  public void sendConnectionResponse(String hostName, int portNumber, Socket socket)  throws IOException {
    ConnectionResponse response = new ConnectionResponse(this.messagingPort, this.messagingHost,
        this.messagingIpAddress, (byte) 1, "Connected successfully");

    this.messagingServer.listenForResponse(socket);
    this.messagingServer.sendData(response.getBytes(), socket);

  }

  public void handleConnectionResponse(ConnectionResponse response) throws IOException {
    System.out.println(response);
  }

  public void sendRegistrationRequest() throws IOException {
    // Create a registration request with the node's host port and name
    Register registerRequest = new Register(this.messagingPort,
        this.messagingHost, this.messagingIpAddress);

    // Send registration request using TCPServerThread's Sender
    this.messagingServer.sendData(this.REGISTRY_PORT,
        this.REGISTRY_HOST, registerRequest.getBytes());

    // messagingServer's currentSocket should now be connected to Registry's server
    this.messagingServer.listenForResponse(this.messagingServer.currentSocket);
  }

  public void sendDeregistrationRequest() throws IOException {
    // Create a deregistration request with the node's host port and name
    Deregister deregisterRequest = new Deregister(this.messagingPort,
        this.messagingHost, this.messagingIpAddress);

    // Send registration request using TCPServerThread's Sender
    this.messagingServer.sendData(deregisterRequest.getBytes());

    // messagingServer's currentSocket should now be connected to Registry's server
    this.messagingServer.listenForResponse(this.messagingServer.currentSocket);
  }

  public void handleRegistrationResponse(RegistrationResponse response) {
    if (response.isSuccess()) {
      this.isRegistered = true;
      System.out.println("Registration request successful!");
    }
    else {
      System.out.println("Registration request unsuccessful!");
    }
  }

  /**
   * If the Registry was able to successfully deregister the node, close all connections and
   * terminate.
   * @param response The response from the Registry for the Deregistration request.
   */
  public void handleDeregistrationResponse(RegistrationResponse response) {
    if (response.isSuccess()) {
      this.isRegistered = false;
      System.out.println("Deregistration request successful!");

      try {
        if (this.messagingServer.currentSocket != null)
          this.messagingServer.receiver.dataInputStream.close();
          this.messagingServer.currentSocket.close();
        this.messagingServer.serverSocket.close();
      } catch (IOException e) {
        System.err.println("Unable to close socket!");
      }

      System.exit(0);
    }
    else {
      System.out.println("Deregistration request unsuccessful!");
    }
  }

  /** Returns a String containing information about the MessagingNode
   * instance, including its port and hostname.
   * @return String representation of the MessagingNode instance
   */
  @Override
  public String toString() {
    return String.format("MessagingNode created on host: %s, running on port: %d\n",
        this.messagingHost, this.messagingPort);
  }

  public static void main(String[] args) throws IOException {
    MessagingNode messagingNode = new MessagingNode(args[0], Integer.parseInt(args[1]));


    System.out.println(messagingNode);


    Scanner reader = new Scanner(System.in);  // Reading user input
    System.out.println("Enter a command: ");
    String command = reader.next(); // Scans the next token of the input as an int.
    reader.close();

    if (command.toLowerCase().equals("exit")) {
      messagingNode.sendDeregistrationRequest();
      System.exit(0);
    }
  }
}
