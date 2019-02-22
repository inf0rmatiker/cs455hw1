package main.java.cs455.overlay.node;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import main.java.cs455.overlay.dijkstra.RoutingCache;
import main.java.cs455.overlay.dijkstra.ShortestPath;
import main.java.cs455.overlay.transport.TCPServerThread;
import main.java.cs455.overlay.util.GraphEdge;
import main.java.cs455.overlay.util.GraphNode;
import main.java.cs455.overlay.util.SortByWeight;
import main.java.cs455.overlay.wireformats.*;
import java.util.Random;

public class MessagingNode implements Node {

  public int REGISTRY_PORT;
  public String REGISTRY_HOST;
  public int messagingPort;
  public String messagingHost;
  public String messagingIpAddress;
  public TCPServerThread messagingServer;
  public boolean isRegistered = false;
  public RoutingCache rCache;
  public ShortestPath dijkstra;
  public int numRounds;
  public long sendSummation;
  public long receiveSummation;
  private int sendTracker;
  private int receiveTracker;
  private int relayTracker;
  private Socket registrySocket;

  public List<RegistryEntry> nodeList;
  private List<Message> buffer;

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
    this.resetCounters(); // Sets all counters to zero
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
      case TASK_INITIATE:
        this.handleTaskInitiate((TaskInitiate) event); break;
      case MESSAGE:
        this.handleMessage((Message) event); break;
      case PULL_TRAFFIC_SUMMARY:
        this.handlePullTrafficSummary(); break;
      default: System.out.println("Unknown event type!");
    }
  }

  public void handlePullTrafficSummary() throws IOException {
    TrafficSummary summary = new TrafficSummary(messagingPort, messagingHost, messagingIpAddress,
        sendTracker, sendSummation, receiveTracker, receiveSummation, relayTracker);

    messagingServer.sendData(summary.getBytes(), registrySocket);
    System.out.println("Summation Received: " + receiveSummation);
    System.out.println("Relay Tracker:: " + relayTracker);
    resetCounters();
  }

  public void resetCounters() {
    this.receiveTracker = 0;
    this.sendTracker = 0;
    this.relayTracker = 0;
    this.sendSummation = 0;
    this.receiveSummation = 0;
  }

  /**
   * Establishes socket connections with the nodes in nodeList.
   * @param nodeList The list of nodes that need to have a connection established with
   */
  public void connectToOtherMessagingNodes(MessagingNodesList nodeList) throws IOException {
    // Add all the nodes in the request list to our nodeList
    System.out.printf("\n\n>> Received MessagingNodesList Request <<\n%s", nodeList);
    synchronized (this.nodeList) {
      for (RegistryEntry re : nodeList.nodeList) {
        if (!this.nodeList.contains(re)) {

          // Establish a socket by sending a message over
          ConnectionRequest connectionRequest = new ConnectionRequest(this.messagingPort,
              this.messagingHost,
              this.messagingIpAddress);

          re.socket = this.messagingServer.sendData(re.portNumber, re.hostName,
              connectionRequest.getBytes());

          this.nodeList.add(re);
          this.messagingServer.listenForResponse(re.socket);
        }
      }
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

    synchronized (nodeList) {
      nodeList.add(entry);
    }

    System.out.printf("\nReceived connection from %s on port %d\n", entry.hostName, entry.portNumber);

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
    this.registrySocket = this.messagingServer.sendData(this.REGISTRY_PORT,
        this.REGISTRY_HOST, registerRequest.getBytes());

    // messagingServer's currentSocket should now be connected to Registry's server
    this.messagingServer.listenForResponse(this.registrySocket);
  }

  public void sendDeregistrationRequest() throws IOException {
    // Create a deregistration request with the node's host port and name
    Deregister deregisterRequest = new Deregister(this.messagingPort,
        this.messagingHost, this.messagingIpAddress);

    // Send registration request using TCPServerThread's Sender
    this.messagingServer.sendData(deregisterRequest.getBytes(), registrySocket);

    // messagingServer's currentSocket should now be connected to Registry's server
    this.messagingServer.listenForResponse(this.registrySocket);
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
    System.out.print("\n\n>> Received Link Weights Message <<\n");

    RoutingCache rCache = setupRoutingCache(linkWeights);
    ShortestPath dijkstra = calculateShortestPaths(rCache);

    this.rCache = rCache;
    this.dijkstra = dijkstra;
    System.out.println("\nLink weights are received and processed. Ready to send messages\n");
  }

  public RoutingCache setupRoutingCache(LinkWeights linkWeights) {
    RoutingCache rCache = new RoutingCache();

    // Create all GraphNodes and add them to rCache.nodes
    for (String info: linkWeights.getLinksInformation()) {
      // Should split the info string into an array like:
      // [hostnameA, portnumA, hostnameB, portnumB, weight]
      String[] infoArray = info.split("[ :]");

      // Creates a key in the form: "hostnameA:portnumA" and adds it to nodes if it has not
      // been already added.
      String key = infoArray[0] + ":" + infoArray[1];
      if (getNodeIndex(key, rCache) == -1)
        rCache.nodes.add(new GraphNode(Integer.parseInt(infoArray[1]), infoArray[0], key));
    }

    // Create all GraphEdges and add them to rCache.edges
    for (String info: linkWeights.getLinksInformation()) {
      String[] infoArray = info.split("[ :]");
      String keyA = infoArray[0] + ":" + infoArray[1];
      String keyB = infoArray[2] + ":" + infoArray[3];

      int from = getNodeIndex(keyA, rCache);
      int to = getNodeIndex(keyB, rCache);
      rCache.edges.add(new GraphEdge(from, to, Integer.parseInt(infoArray[4])));
    }

    // Sort edges by weight
    Collections.sort(rCache.edges, new SortByWeight());

    // Add all edges to GraphNode edges lists
    for (GraphEdge ge: rCache.edges) {
      rCache.nodes.get(ge.from).edges.add(ge);
      rCache.nodes.get(ge.to).edges.add(ge);
    }

    rCache.setStartingIndex(getNodeIndex(messagingHost + ":" + messagingPort, rCache));
    //System.out.println(rCache);
    return rCache;
  }

  public int getNodeIndex(String key, RoutingCache rCache) {
    for (int i = 0; i < rCache.nodes.size(); i++) {
      if (rCache.nodes.get(i).getKey().equals(key)) {
        return i;
      }
    }
    return -1;
  }

  public ShortestPath calculateShortestPaths(RoutingCache rCache) {
    ShortestPath dijkstra = new ShortestPath(rCache);
    dijkstra.calculateShortestPaths();
    dijkstra.convertResultsToPaths();

    return dijkstra;
  }

  /**
   * Handles a TaskInitiate message from the Registry's "start" command.
   * @param taskInitiate The TaskInitiate message containing the number of rounds to send.
   */
  public void handleTaskInitiate(TaskInitiate taskInitiate) throws IOException {
    System.out.printf("\n>> Received Task Initiate Message for %d rounds per Node\n",
        taskInitiate.numRoundsPerNode);

    this.numRounds = taskInitiate.numRoundsPerNode;
    this.sendRounds();
  }

  /**
   * Sends numRounds messages to random other nodes.
   * @throws IOException
   */
  public void sendRounds() throws IOException {
    int indexInRoutingCache = rCache.getStartingIndex();
    int messagesSent = 0;
    Random generator = new Random();

    while (this.getSendTracker() < this.numRounds * 5) {
      int randomIndex = generator.nextInt(rCache.nodes.size());
      if (randomIndex != indexInRoutingCache) { // Don't send a message to yourself!
        int randomPayload = generator.nextInt();
        String destinationKey = rCache.nodes.get(randomIndex).getKey();
        Message message = new Message(messagingPort, messagingHost, messagingIpAddress,
            randomPayload, destinationKey);

        GraphNode nextNode = rCache.shortestPaths.get(destinationKey).get(1);
        Socket nextNodeSocket = getNextNodeSocket(nextNode);
        if (nextNodeSocket == null) {
          System.err.println("Could not find a valid socket!");
        }
        else {
          this.addToSendSummation(randomPayload); // Synchronized
          messagingServer.sendData(message.getBytes(), nextNodeSocket);
        }
        this.incrementSendTracker();
      }
    }

    // We are finished sending rounds, send a TaskComplete message to Registry
    this.sendTaskCompleteMessage();
  }

  public void sendTaskCompleteMessage() throws IOException {
    TaskComplete taskCompleteMessage = new TaskComplete(messagingPort, messagingHost, messagingIpAddress);
    messagingServer.sendData(taskCompleteMessage.getBytes(), this.registrySocket);
  }

  private void incrementSendTracker() {
    this.sendTracker++;
  }

  public int getSendTracker() {
    return this.sendTracker;
  }

  private synchronized void incrementReceiveTracker() {
    this.receiveTracker++;
  }

  public synchronized int getReceiveTracker() {
    return this.receiveTracker;
  }

  private synchronized void incrementRelayTracker() {
    this.relayTracker++;
  }

  public synchronized int getRelayTracker() {
    return this.relayTracker;
  }

  private synchronized void addToSendSummation(int value) {
    this.sendSummation += value;
  }

  private synchronized void addToReceiveSummation(int value) {
    this.receiveSummation += value;
  }

  public synchronized long getSendSummation() {
    return this.sendSummation;
  }

  public synchronized long getReceiveSummation() {
    return this.receiveSummation;
  }

  public Socket getNextNodeSocket(GraphNode nextNode) {
    for (RegistryEntry re: nodeList) {
      if (re.getKey().equals(nextNode.getKey())) {
        return re.socket;
      }
    }
    return null;
  }

  public void handleMessage(Message message) throws IOException {
    // TODO: NOT THE FINAL DESTINATION (PROBABLY)
    // If the message was not intended for us, send it on
    if (!(message.destination.equals(getKey()))) {
      GraphNode nextNode = rCache.shortestPaths.get(message.destination).get(1);
      Socket nextNodeSocket = getNextNodeSocket(nextNode);
      if (nextNodeSocket == null) {
        System.err.println("Could not find a valid socket!");
      }
      else {
        messagingServer.sendData(message.getBytes(), nextNodeSocket);
        this.incrementRelayTracker(); // Synchronized
      }
    }
    else { // The message was intended for us
      //System.out.printf("Payload %d\n", message.payload);
      this.addToReceiveSummation(message.payload); // Synchronized
      this.incrementReceiveTracker(); // Synchronized
    }
  }

  private String getKey() {
    return messagingHost + ":" + messagingPort;
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
    messagingNode.startServerThread();
    messagingNode.messagingServer.startConsumerThread();
    messagingNode.sendRegistrationRequest();

    System.out.println(messagingNode);
    Scanner reader = new Scanner(System.in);  // Reading user input

    while (true) {
      String command = reader.next(); // Scans the next token of the input as an int.
      if (command.toLowerCase().equals("exit")) {
        messagingNode.sendDeregistrationRequest();
        System.exit(0);
      }
      else if (command.toLowerCase().equals("print-shortest-path")) {
        messagingNode.dijkstra.printShortestPaths();
      }
      else if (command.toLowerCase().equals("print-receive-tracker")) {
        System.out.println("Receive tracker: " + messagingNode.getReceiveTracker());
      }
      else if (command.toLowerCase().equals("print-relay-tracker")) {
        System.out.println("Relay tracker: " + messagingNode.getRelayTracker());
      }
      else break;
    }


    reader.close();


  }
}
