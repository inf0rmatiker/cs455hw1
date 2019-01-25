package main.java.cs455.overlay.node;

import java.util.ArrayList;
import java.io.*;
import java.net.*;

public class Registry {

  public final int REGISTRY_PORT = 5003;
  public final int nodeCapacity;

  public ArrayList<Node> nodeList;
  public ServerSocket serverSocket;
  public DataOutputStream dataOutputStream;
  public DataInputStream dataInputStream;
  public Socket socket;

  public Registry() {
    this.nodeCapacity = 3;
    this.nodeList = new ArrayList<Node>();
  }

  public void createServerSocket (int nodeCapacity) throws IOException  {
    this.serverSocket = new ServerSocket(REGISTRY_PORT, nodeCapacity);
  }

  public void registerMessagingNode(int nodeId) {
    if (!isNodeRegistered(nodeId)) {
      this.nodeList.add(new Node(nodeId));
      System.out.printf("Node %d has been registered!\n", nodeId);
    }
    else deregisterMessagingNode(nodeId);
  }

  public void deregisterMessagingNode(int nodeId) throws IllegalStateException {
    if (!isNodeRegistered(nodeId)) {
      throw new IllegalStateException("Unregistered node " + nodeId + " cannot be deregistered!");
    }
    for (int i = 0; i < this.nodeList.size(); i++) {
      if (this.nodeList.get(i).getId() == nodeId) {
        this.nodeList.remove(i);
        break;
      }
    }
    System.out.printf("Node %d has been deregistered!\n", nodeId);
  }

  public boolean isNodeRegistered(int nodeId) {
    for (Node n: this.nodeList) {
      if (n.getId() == nodeId) {
        return true;
      }
    }
    return false;
  }

  public String toString() {
    String result = "";
    for (Node registeredNode: this.nodeList) {
      result += registeredNode;
    }
    return result;
  }

  public static void main(String[] args) throws IOException {

    Registry registry = new Registry();
    registry.createServerSocket(registry.nodeCapacity);

    while (true) {
      System.out.printf("Now listening on port # %d", registry.REGISTRY_PORT);
      registry.socket = registry.serverSocket.accept();
      registry.dataInputStream = new DataInputStream(registry.socket.getInputStream());
      registry.dataOutputStream = new DataOutputStream(registry.socket.getOutputStream());

      int nodeId = registry.dataInputStream.readInt();
      System.out.printf("Registry received nodeId: %d\n", nodeId);

      if (registry.nodeList.size() >= registry.nodeCapacity) {
        break;
      }
      else {
        registry.registerMessagingNode(nodeId);
      }
    }

    System.out.println(registry);





  }



}
