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

  public Registry(int portNumber) throws IOException {
    this.registryEntries = new LinkedList<>();
    this.registryServer = new TCPServerThread(this, portNumber);
    this.REGISTRY_PORT = portNumber;
    this.REGISTRY_HOSTNAME = registryServer.serverSocket.getInetAddress().getLocalHost().getHostName();
    this.REGISTRY_IP = this.getRegistryIpAddress();
    this.startServerThread();
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
    this.printOverlay(overlayCreator);
    for (int i = 0; i < registryEntries.size(); i++) {

      //System.out.printf("Node at index %d has edges:\n%s\n", i, registryEntries.get(i).getEdgeConnections());

      //System.out.printf("Index %d entry: %s\n",i, registryEntries.get(i));
      this.sendMessagingNodesList(i);
      //System.out.printf("%s\n", String.join("\n", registryEntries.get(i).edges.stream().map(Object::toString).collect(
          //Collectors.toList())));
      //System.out.printf("Index %d socket: %s\n",i, registryEntries.get(i).socket.getInetAddress().getHostName());
    }
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
    // TODO: Remove testing print statements
    System.out.printf("\n%s\n", linkWeights);
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
  public void registerMessagingNode(Event event, Socket socket) throws IOException {
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
      System.out.printf("Registered Node on %s at port %d\n", request.hostName, request.portNumber);
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

  // TODO REMOVE DEPRECATION, not being used anymore
  /**
   * Compares the IP address of the request's origin to the IP address contained in the request.
   * @param requestIpAddress The IP address contained in the request.
   * @return Boolean true or false if they match.
   */
  private boolean ipAddressesMatch(String requestIpAddress) {
    String originSocketAddress = this.registryServer.currentSocket.getRemoteSocketAddress().toString();

    // Strip off the port number and leading '/'
    originSocketAddress = originSocketAddress.substring(1, originSocketAddress.indexOf(':'));
    System.err.printf("Request IP: %s\nOrigin IP: %s\n", requestIpAddress, originSocketAddress);
    if (!requestIpAddress.equals(originSocketAddress)) {
      System.err.println("IP ADDRESSES DO NOT MATCH");
    }
    return requestIpAddress.equals(originSocketAddress);
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
      default: System.out.println("Unknown event type!");
    }
  }

  public void printOverlay(OverlayCreator overlayCreator) {
    System.out.println(">> PRINTING OVERLAY <<\n");
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

    System.out.println(registry);

    // Handle user input
    Scanner scan = new Scanner(System.in);
    while (true) {
      String command = scan.next();

      if (command.toLowerCase().equals("exit")) break;
      if (command.toLowerCase().equals("setup-overlay")) {
        int numLinksPerNode = scan.nextInt();
        registry.createOverlay(numLinksPerNode);
      }
      else if (command.toLowerCase().equals("send-link-weights")) {
        registry.sendLinkWeights();
      }
      else {
        System.out.printf(
            "Commands:\tDescriptions:\n"
                + "exit\t\tExits the process.\n"
                + "setup-overlay <int>\tCreates an overlay with n links per node.\n"
                + "send-link-weights\tSends a message to all nodes with link weights.\n"
                + "help\t\tShow usage message."
        );
      }
    }

    scan.close();
    System.exit(0);
  }

}
