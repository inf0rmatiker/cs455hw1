package main.java.cs455.overlay.node;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import main.java.cs455.overlay.dijkstra.RoutingCache;
import main.java.cs455.overlay.transport.TCPServerThread;
import main.java.cs455.overlay.util.GraphEdge;
import main.java.cs455.overlay.util.GraphNode;
import main.java.cs455.overlay.wireformats.*;

public class MessagingNode implements Node {

  public int REGISTRY_PORT;
  public String REGISTRY_HOST;
  public int messagingPort;
  public String messagingHost;
  public String messagingIpAddress;
  public TCPServerThread messagingServer;
  public boolean isRegistered = false;
  RoutingCache rCache;

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
        this.handleConnectionRequest((ConnectionRequest) event, socket); break;
      case CONNECTION_RESPONSE:
        this.handleConnectionResponse((ConnectionResponse) event, socket); break;
      case LINK_WEIGHTS:
        this.handleLinkWeightsMessage((LinkWeights) event); break;
      default: System.out.println("Unknown event type!");
    }
  }

  /**
   * Establishes socket connections with the nodes in nodeList.
   * @param nodeList The list of nodes that need to have a connection established with
   */
  public void connectToOtherMessagingNodes(MessagingNodesList nodeList) throws IOException {
    // Add all the nodes in the request list to our nodeList
    System.out.printf(">> Received MessagingNodesList Request <<\n%s", nodeList);
    for (RegistryEntry re: nodeList.nodeList) {
      this.nodeList.add(re);
    }

    for (int i = 0; i < this.nodeList.size(); i++) {
      // Establish a socket by sending a message over
      ConnectionRequest connectionRequest = new ConnectionRequest(this.messagingPort, this.messagingHost,
          this.messagingIpAddress);


      // Establish a socket by sending a message over
      // Save the socket that was created when sending data to the RegistryEntry's socket field
      this.nodeList.get(i).socket = this.messagingServer.sendData(this.nodeList.get(i).portNumber,
          this.nodeList.get(i).hostName, connectionRequest.getBytes());
      this.messagingServer.listenForResponse(this.nodeList.get(i).socket);
    }
  }

  /**
   * Creates a RegistryEntry object to save the socket/connection of the incoming request.
   * @param request The message containing the originating port, host, and IP address of the sender
   * @throws IOException
   */
  public void handleConnectionRequest(ConnectionRequest request, Socket socket) throws IOException {
    RegistryEntry entry = new RegistryEntry(request.getPortNumber(), request.getHostName(),
        request.getIpAddress(), socket);

    System.out.printf("Received connection from %s on port %d\n", entry.hostName, entry.portNumber);

    this.sendConnectionResponse(entry.hostName, entry.portNumber, entry.socket);

  }

  public void sendConnectionResponse(String hostName, int portNumber, Socket socket)  throws IOException {
    ConnectionResponse response = new ConnectionResponse(this.messagingPort, this.messagingHost,
        this.messagingIpAddress, (byte) 1, "Connected successfully");


    this.messagingServer.sendData(response.getBytes(), socket);

  }

  public void handleConnectionResponse(ConnectionResponse response, Socket socket) throws IOException {
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

  public void handleLinkWeightsMessage(LinkWeights linkWeights) {
    setupRoutingCache(linkWeights);
  }

  public void setupRoutingCache(LinkWeights linkWeights) {
    rCache = new RoutingCache();

    // Create all GraphNodes and add them to rCache.nodes
    for (String info: linkWeights.getLinksInformation()) {
      // Should split the info string into an array like:
      // [hostnameA, portnumA, hostnameB, portnumB, weight]
      String[] infoArray = info.split("[ :]");

      // Creates a key in the form: "hostnameA:portnumA" and adds it to nodes if it has not
      // been already added.
      String key = infoArray[0] + ":" + infoArray[1];
      if (getNodeIndex(key) == -1)
        rCache.nodes.add(new GraphNode(Integer.parseInt(infoArray[1]), infoArray[0], key));
    }

    // Create all GraphEdges and add them to rCache.edges
    for (String info: linkWeights.getLinksInformation()) {
      String[] infoArray = info.split("[ :]");
      String keyA = infoArray[0] + ":" + infoArray[1];
      String keyB = infoArray[2] + ":" + infoArray[3];

      int from = getNodeIndex(keyA);
      int to = getNodeIndex(keyB);
      rCache.edges.add(new GraphEdge(from, to, Integer.parseInt(infoArray[4])));
    }

    // Sort edges by weight
    Collections.sort(rCache.edges);

    // Add all edges to GraphNode edges lists
    for (GraphEdge ge: rCache.edges) {
      rCache.nodes.get(ge.from).edges.add(ge);
      rCache.nodes.get(ge.to).edges.add(ge);
    }

    System.out.println(rCache);
  }

  public int getNodeIndex(String key) {
    for (int i = 0; i < rCache.nodes.size(); i++) {
      if (rCache.nodes.get(i).getKey().equals(key)) {
        return i;
      }
    }
    return -1;
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
    String command = reader.next(); // Scans the next token of the input as an int.
    reader.close();

    if (command.toLowerCase().equals("exit")) {
      messagingNode.sendDeregistrationRequest();
      System.exit(0);
    }
  }
}
