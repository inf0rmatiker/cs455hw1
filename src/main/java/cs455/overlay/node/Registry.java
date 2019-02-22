package main.java.cs455.overlay.node;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.io.*;
import java.net.*;
import java.util.Scanner;
import java.util.stream.Collectors;
import main.java.cs455.overlay.transport.*;
import main.java.cs455.overlay.util.GraphEdge;
import main.java.cs455.overlay.util.OverlayCreator;
import main.java.cs455.overlay.wireformats.*;

public class Registry implements Node {

  public final int REGISTRY_PORT;
  public String REGISTRY_HOSTNAME;
  public String REGISTRY_IP;
  public TCPServerThread registryServer;
  public List<RegistryEntry> registryEntries;
  public OverlayCreator overlayCreator;
  private int tasksComplete;
  private int summariesReceived;

  public Registry(int portNumber) throws IOException {
    this.registryEntries = new LinkedList<>();
    this.registryServer = new TCPServerThread(this, portNumber);
    this.REGISTRY_PORT = portNumber;
    this.REGISTRY_HOSTNAME = registryServer.serverSocket.getInetAddress().getLocalHost().getHostName();
    this.REGISTRY_IP = this.getRegistryIpAddress();
    this.resetSummaries();
  }

  public String getRegistryIpAddress() throws IOException {
    return this.registryServer.serverSocket.getInetAddress().getLocalHost().getHostAddress();
  }

  // Starts TCPServerThread's run() method
  public void startServerThread() {
    Thread serverThread = new Thread(this.registryServer, "Registry Server Thread");
    serverThread.start();
  }

  public void createOverlay(int numLinksPerNode) throws IOException {
    this.overlayCreator = new OverlayCreator(registryEntries, numLinksPerNode);
    //this.printOverlay(overlayCreator);
    for (int i = 0; i < registryEntries.size(); i++) {
      this.sendMessagingNodesList(i);
    }
  }

  public void resetSummaries() {
    this.tasksComplete = 0;
    this.summariesReceived = 0;
  }

  // TODO: Remove print statements
  /**
   * Create list of nodes that the recipient should connect to.
   * @param index The index of the RegistryEntry in registryEntries
   */
  public void sendMessagingNodesList(int index) throws IOException {
    RegistryEntry fromNode = registryEntries.get(index);
    //System.out.printf("Index %d entry: %s\n",index, registryEntries.get(index));
    ArrayList<RegistryEntry> listOfToNodes = new ArrayList<>();
    //System.out.printf("From index: %d\n", index);
    for (GraphEdge ge: fromNode.edges) {
      // Only include nodes which have from coming out of it and to going to a different node
      if (ge.from == index) {

        RegistryEntry toNode = registryEntries.get(ge.to);
        //System.out.printf("To index: %d, Node at toindex: %s\n", ge.to, toNode);
        listOfToNodes.add(toNode);
        //System.out.printf("To index: %d\n", ge.to);
        //System.out.printf("To Node: %s\n", toNode);
      }
    }

    MessagingNodesList messagingNodesList = new MessagingNodesList(REGISTRY_PORT,
        REGISTRY_HOSTNAME, REGISTRY_IP, listOfToNodes);
    this.registryServer.sendData(messagingNodesList.getBytes(), fromNode.socket);
  }

  /**
   * Sends a message to all registered nodes containing the weights for each link in the graph.
   */
  public void sendLinkWeights() throws IOException {
    int totalNumLinks = this.overlayCreator.edgesList.size();
    LinkWeights linkWeights = new LinkWeights(REGISTRY_PORT, REGISTRY_HOSTNAME, REGISTRY_IP,
        totalNumLinks, this.registryEntries, this.overlayCreator.edgesList);
    for (int i = 0; i < registryEntries.size(); i++) {
      this.registryServer.sendData(linkWeights.getBytes(), registryEntries.get(i).socket);
    }
  }

  /**
   *  If the host name of the request and the origin match, and the node is not
   *  already registered, register the information from the request. Finally, send a
   *  success response back to the origin verifying the registration.
   * @param event The Register request
   * @throws IOException
   */
  public synchronized void registerMessagingNode(Event event, Socket socket) throws IOException {
    RegistryEntry request = new RegistryEntry(
        event.getPortNumber(), event.getHostName(), event.getIpAddress(), socket);

    RegistrationResponse response;
    hostNamesMatch(request.hostName, socket);
    // Verify that:
    // 1: The address of the request matches the origin address.
    // 2: The node is not already registered.
    // Register the node by adding it to registryEntries, then send a success response back to origin.
    if (hostNamesMatch(request.hostName, socket) && !isNodeRegistered(request)) {
      registryEntries.add(request);
      response = new RegistrationResponse(REGISTRY_PORT, REGISTRY_HOSTNAME, REGISTRY_IP,
          (byte) 0, (byte) 1, request.toString()); // 1 = success
      // TODO: Remove test print statements
      System.out.printf("Registered Node on %s at port %d, at index [%d]\n",
          request.hostName, request.portNumber, registryEntries.size()-1);
    }
    else { // Either the host names mismatched, or the node was already registered.
      response = new RegistrationResponse(REGISTRY_PORT, REGISTRY_HOSTNAME, REGISTRY_IP,
          (byte) 0, (byte) 0, "Failed to register."); // 0 = failure
      // TODO: Remove test print statements
      System.err.printf("Failed to register Node on %s at port %d\n",
          request.hostName, request.portNumber);
    }

    System.out.printf("Sending registration response to %s...\n\n", socket.getInetAddress().getHostName());
    this.registryServer.sendData(response.getBytes(), socket);
  }

  public synchronized void deregisterMessagingNode(Event event, Socket socket) throws IOException  {
    RegistryEntry request = new RegistryEntry(
        event.getPortNumber(), event.getHostName(), event.getIpAddress(), socket);

    RegistrationResponse response;
    // Verify that:
    // 1: The address of the request matches the origin address.
    // 2: The node is already registered.
    // Deregister the node by adding removing its entry from registryEntries list,
    // then send a success response back to origin.
    if (hostNamesMatch(request.hostName, socket) && isNodeRegistered(request)) {
      for (int i = 0; i < registryEntries.size(); ++i) {
        if (registryEntries.get(i).equals(request)) {
          registryEntries.remove(i);
        }
      }
      response = new RegistrationResponse(REGISTRY_PORT, REGISTRY_HOSTNAME, REGISTRY_IP,
          (byte) 1, (byte) 1, request.toString()); // 1 = success
      // TODO: Remove test print statements
      System.out.printf("Deregistered Node on %s at port %d\n", request.hostName, request.portNumber);
    }
    else {
      response = new RegistrationResponse(REGISTRY_PORT, REGISTRY_HOSTNAME, REGISTRY_IP,
          (byte) 1, (byte) 0, "Failed to deregister."); // 0 = failure
      // TODO: Remove test print statements
      System.err.printf("Failed to deregister Node on %s at port %d\n",
          request.hostName, request.portNumber);
    }

    this.registryServer.sendData(response.getBytes(), socket);
  }

  /**
   * Compares the host name of the request's origin to the host name contained in the request.
   * @param requestHostName The host name contained in the request.
   * @return Boolean true or false if they match.
   */
  public boolean hostNamesMatch(String requestHostName, Socket socket) {
    String originHostName = socket.getInetAddress().getHostName();
    return originHostName.contains(requestHostName);
  }

  /**
   * Checks to see if the registryEntries already contains an entry with the matching port and host.
   * @param request The request containing a port, host name, and ip address.
   * @return The boolean value true or false if it already is registered.
   */
  public synchronized boolean isNodeRegistered(RegistryEntry request) {
    for (int i = 0; i < registryEntries.size(); i++) {
      if (request.equals(registryEntries.get(i))) {
        return true;
      }
    }
    return false;
  }

  @Override
  public void onEvent(Event event, Socket socket) throws IOException {
    switch (event.getType()) {
      case REGISTER: registerMessagingNode(event, socket); break;
      case DEREGISTER: deregisterMessagingNode(event, socket); break;
      case TASK_COMPLETE: handleTaskCompleteMessage(); break;
      case TRAFFIC_SUMMARY: handleTrafficSummary((TrafficSummary) event); break;
      default: System.out.println("Unknown event type!");
    }
  }

  // Synchronized because we may have multiple nodes sending summaries at once
  public synchronized void handleTrafficSummary(TrafficSummary summary) {
    RegistryEntry entry = getRegistryEntry(summary);
    if (entry != null) {
      entry.sentSummation = summary.getSummationSent();
      entry.receiveSummation = summary.getSummationReceived();
      entry.numSent = summary.getNumMessagesSent();
      entry.numReceived = summary.getNumMessagesReceived();
      entry.numRelayed = summary.getNumMessagesRelayed();

      this.summariesReceived++;
    }
    else {
      System.err.println("Could not find that node!");
    }

    if (summariesReceived == registryEntries.size()) {
      this.printSummaries();
      this.resetSummaries();
    }
  }

  public RegistryEntry getRegistryEntry(TrafficSummary summary) {
    String summaryKey = summary.getHostName() + ":" + summary.getPortNumber();
    for (RegistryEntry re: registryEntries) {
      if (re.getKey().equals(summaryKey)) {
        return re;
      }
    }
    return null;
  }

  private void printSummaries() {
    System.out.printf("\n\t\tNumber Messages Sent\tNumber Messages Received\tSummation Sent\tSummation Received\tNumber Messages Relayed\n\n");
    String result = "";
    int totalSent = 0;
    int totalReceived = 0;
    long sumSent = 0L;
    long sumReceived = 0L;
    for (int i = 0; i < registryEntries.size(); i++) {
      RegistryEntry re = registryEntries.get(i);
      result += String.format("Node %d\t\t%d\t\t\t%d\t\t\t\t%d\t%d\t\t\t%d\n", i, re.numSent, re.numReceived,
          re.sentSummation, re.receiveSummation, re.numRelayed);

      totalSent +=  re.numSent;
      totalReceived += re.numReceived;
      sumSent += re.sentSummation;
      sumReceived += re.receiveSummation;
    }
    result += String.format("Sum\t\t%d\t\t\t%d\t\t\t\t%d\t%d\n", totalSent, totalReceived, sumSent,
        sumReceived);
    System.out.println(result);
  }

  public synchronized void handleTaskCompleteMessage()  throws IOException {
    this.incrementTasksComplete();
    if (allNodesFinished()) {
      //System.out.println(">>>>> ALL NODES FINISHED ! <<<<<<");
      try {
        Thread.sleep(15000); // Sleep for 15 seconds to wait for all nodes to receive messages

      } catch (InterruptedException e) {
        System.err.printf("%s\n", e.getMessage());
      }

      PullTrafficSummary pullRequest = new PullTrafficSummary(REGISTRY_PORT, REGISTRY_HOSTNAME, REGISTRY_IP);
      sendPullTrafficSummaryToAllNodes(pullRequest);
    }
  }

  public void sendPullTrafficSummaryToAllNodes(PullTrafficSummary request) throws IOException {
    for (RegistryEntry re: registryEntries) {
      registryServer.sendData(request.getBytes(), re.socket);
    }
  }

  public synchronized void incrementTasksComplete() {
    this.tasksComplete++;
  }

  private synchronized boolean allNodesFinished() {
    return this.tasksComplete == registryEntries.size();
  }

  public void startRounds(int numRoundsPerNode) throws IOException {
    TaskInitiate taskInitiate = new TaskInitiate(REGISTRY_PORT, REGISTRY_HOSTNAME, REGISTRY_IP,
        numRoundsPerNode);

    for (int i = 0; i < registryEntries.size(); i++) {
      this.registryServer.sendData(taskInitiate.getBytes(), registryEntries.get(i).socket);
    }
  }

  public void printOverlay(OverlayCreator overlayCreator) {
    System.out.println("\n>> OVERLAY <<\n");
    System.out.printf("Total number of nodes: %d\n", registryEntries.size());
    System.out.printf("Total number of links: %d\n", overlayCreator.edgesList.size());
    for (int i = 0; i < registryEntries.size(); i++) {
      String entry = String.format("Index %d entry: %s\n", i, registryEntries.get(i));
      for (GraphEdge ge: registryEntries.get(i).edges) {
        entry += String.format("\t%s",ge);
      }
      System.out.println(entry);
    }
  }

  public void printMessagingNodes() {
    System.out.println("\n>> MESSAGING NODES LIST <<\n");
    System.out.printf("Total number of nodes: %d\n", registryEntries.size());
    for (RegistryEntry re: registryEntries) {
      System.out.printf("Host name: %s, port: %d\n", re.hostName, re.portNumber);
    }
  }

  public void printWeights() throws IOException {
    int totalNumLinks = this.overlayCreator.edgesList.size();
    LinkWeights linkWeights = new LinkWeights(REGISTRY_PORT, REGISTRY_HOSTNAME, REGISTRY_IP,
        totalNumLinks, this.registryEntries, this.overlayCreator.edgesList);
    System.out.println("\n>> LINK LIST <<\n");
    System.out.printf("%s\n", linkWeights);
  }

  @Override
  public String toString() {
    String registryString = "";
    registryString += String.format("Registry created on host: %s, running on port: %d\n",
        this.REGISTRY_HOSTNAME, this.REGISTRY_PORT);
    return registryString;
  }

  public static void main(String[] args) throws IOException {
    // Create the registry from the port specified by command-line arg.
    // This also creates and starts running the TCPServerThread in the Registry instance.

    Registry registry = new Registry(Integer.parseInt(args[0]));

    registry.registryServer.startConsumerThread();
    registry.startServerThread();

    System.out.println(registry);

    // Handle user input
    Scanner scan = new Scanner(System.in);
    while (true) {
      String command = scan.next();

      // Decided to use if-else instead of switch so I could use the "break" keyword to break
      // out of the while-loop.
      if (command.toLowerCase().equals("exit")) break;
      if (command.toLowerCase().equals("setup-overlay")) {
        int numLinksPerNode = scan.nextInt();
        registry.createOverlay(numLinksPerNode);
      }
      else if (command.toLowerCase().equals("send-overlay-link-weights")) {
        registry.sendLinkWeights();
      }
      else if (command.toLowerCase().equals("list-messaging-nodes")) {
        registry.printMessagingNodes();
      }
      else if (command.toLowerCase().equals("list-weights")) {
        registry.printWeights();
      }
      else if (command.toLowerCase().equals("start")) {
        int numRoundsPerNode = scan.nextInt();
        registry.startRounds(numRoundsPerNode);
      }
      else if (command.toLowerCase().equals("print-overlay")) {
        registry.printOverlay(registry.overlayCreator);
      }
      else {
        System.out.printf(
            "\nCommands:\t\t\tDescriptions:\n\n"
                + "exit\t\t\t\tExits the process.\n"
                + "setup-overlay <int>\t\tCreates an overlay with n links per node.\n"
                + "send-overlay-link-weights\tSends a message to all nodes with link weights.\n"
                + "start <num rounds>\t\tSends a message to all nodes initiating the rounds.\n"
                + "list-messaging-nodes\t\tLists the messaging nodes in the overlay.\n"
                + "list-weights\t\t\tLists the weights of the links in the overlay.\n"
                + "print-overlay\t\t\tPrints the overlay.\n"
                + "help\t\t\t\tShow usage message.\n\n"
        );
      }
    }

    scan.close();
    System.exit(0);
  }

}
