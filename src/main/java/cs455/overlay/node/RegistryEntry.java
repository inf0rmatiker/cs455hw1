package main.java.cs455.overlay.node;

import java.net.Socket;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import main.java.cs455.overlay.util.GraphEdge;

public class RegistryEntry {

  public int portNumber;
  public String hostName;
  public String ipAddress;
  public Socket socket;

  public int numSent;
  public int numReceived;
  public int numRelayed;
  public long sentSummation;
  public long receiveSummation;

  public List<GraphEdge> edges;

  public RegistryEntry(int portNumber, String hostName, String ipAddress, Socket socket) {
    this.hostName = hostName;
    this.portNumber = portNumber;
    this.ipAddress = ipAddress;
    this.socket = socket;
    this.edges = new ArrayList<>();
  }

  public String getEdgeConnections() {
    String result = "";
    for (GraphEdge ge: edges) {
      result += String.format("Edge from index %d to index %d with weight %d\n", ge.from, ge.to, ge.weight);
    }

    return result;
  }

  public String getKey() {
    return hostName + ":" + portNumber;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof RegistryEntry)) return false;
    RegistryEntry other = (RegistryEntry) o;
    return other.portNumber == this.portNumber &&
           other.hostName.equals(this.hostName) &&
           other.ipAddress.equals(this.ipAddress);
  }

  @Override
  public String toString() {
    String socketStatus = (socket == null) ? "IS" : "IS NOT";
    return String.format("Node registered on %s at port number: %d, socket %s null",
        this.hostName, this.portNumber, socketStatus);
  }

}
