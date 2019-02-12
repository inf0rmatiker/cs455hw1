package main.java.cs455.overlay.node;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.io.*;
import java.net.*;
import java.util.Scanner;
import java.util.stream.Collectors;
import main.java.cs455.overlay.transport.*;
import main.java.cs455.overlay.util.OverlayCreator;
import main.java.cs455.overlay.wireformats.Deregister;
import main.java.cs455.overlay.wireformats.Event;
import main.java.cs455.overlay.wireformats.EventFactory;
import main.java.cs455.overlay.wireformats.RegistrationResponse;

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
    Thread serverThread = new Thread(this.registryServer);
    serverThread.start();
  }

  public void createOverlay(int numLinksPerNode) {
    this.overlayCreator = new OverlayCreator(registryEntries, numLinksPerNode);
    for (int i = 0; i < registryEntries.size(); i++) {
      System.out.printf("Node at index %d has edges:\n%s\n", i, registryEntries.get(i).getEdgeConnections());
    }
  }

  /**
   *  If the host name of the request and the origin match, and the node is not
   *  already registered, register the information from the request. Finally, send a
   *  success response back to the origin verifying the registration.
   * @param event The Register request
   * @throws IOException
   */
  public void registerMessagingNode(Event event) throws IOException {
    RegistryEntry request = new RegistryEntry(
        event.getPortNumber(), event.getHostName(), event.getIpAddress());

    RegistrationResponse response;
    hostNamesMatch(request.hostName);
    // Verify that:
    // 1: The address of the request matches the origin address.
    // 2: The node is not already registered.
    // Register the node by adding it to registryEntries, then send a success response back to origin.
    if (hostNamesMatch(request.hostName) && !isNodeRegistered(request)) {
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
    this.registryServer.sendData(response.getBytes());
  }

  public synchronized void deregisterMessagingNode(Event event) throws IOException  {
    RegistryEntry request = new RegistryEntry(
        event.getPortNumber(), event.getHostName(), event.getIpAddress());

    RegistrationResponse response;
    // Verify that:
    // 1: The address of the request matches the origin address.
    // 2: The node is already registered.
    // Deregister the node by adding removing its entry from registryEntries list,
    // then send a succeooss response back to origin.
    if (hostNamesMatch(request.hostName) && isNodeRegistered(request)) {
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
    this.registryServer.sendData(response.getBytes());
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
  public boolean hostNamesMatch(String requestHostName) {
    String originHostName = this.registryServer.currentSocket.getInetAddress().getHostName();
    return originHostName.equals(requestHostName + ".cs.colostate.edu");
  }

  /**
   * Checks to see if the registryEntries already contains an entry with the matching port and host.
   * @param request The request containing a port, host name, and ip address.
   * @return The boolean value true or false if it already is registered.
   */
  public boolean isNodeRegistered(RegistryEntry request) {
    for (int i = 0; i < registryEntries.size(); i++) {
      if (request.equals(registryEntries.get(i))) {
        return true;
      }
    }
    return false;
  }

  @Override
  public void onEvent(Event event) throws IOException {
    switch (event.getType()) {
      case REGISTER: registerMessagingNode(event); break;
      case DEREGISTER: deregisterMessagingNode(event); break;
      default: System.out.println("Unknown event type!");
    }
  }

  public String getRegisteredNodesInString() {
    return String.join("\n", this.registryEntries.stream().map(Object::toString).collect(Collectors.toList()));
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

    Scanner scan = new Scanner(System.in);
    String command = scan.next();

    switch (command) {
      case "exit": System.exit(0);
      case "createOverlay":
        int numLinksPerNode = scan.nextInt();
        registry.createOverlay(numLinksPerNode); break;
      default: break;
    }

    scan.close();
  }

}
