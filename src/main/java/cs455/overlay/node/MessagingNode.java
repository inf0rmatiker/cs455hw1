package main.java.cs455.overlay.node;

import java.io.*;
import java.net.*;
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

    this.sendRegistrationRequest();
    this.startServerThread();
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
    Thread serverThread = new Thread(this.messagingServer);
    serverThread.start();
  }

  @Override
  public void onEvent(Event event) {
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
      default: System.out.println("Unknown event type!");
    }

  }

  public void sendRegistrationRequest() throws IOException {
    // Create a registration request with the node's host port and name
    Register registerRequest = new Register(this.messagingPort,
        this.messagingHost, this.messagingIpAddress);

    // Send registration request using TCPServerThread's Sender
    this.messagingServer.sendData(this.REGISTRY_PORT,
        this.REGISTRY_HOST, registerRequest.getBytes());

    // messagingServer's currentSocket should now be connected to Registry's server
    this.messagingServer.listenForResponse();
  }

  public void sendDeregistrationRequest() throws IOException {
    // Create a deregistration request with the node's host port and name
    Deregister deregisterRequest = new Deregister(this.messagingPort,
        this.messagingHost, this.messagingIpAddress);

    // Send registration request using TCPServerThread's Sender
    this.messagingServer.sendData(deregisterRequest.getBytes());

    // messagingServer's currentSocket should now be connected to Registry's server
    this.messagingServer.listenForResponse();
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
