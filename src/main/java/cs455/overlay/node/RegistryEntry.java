package main.java.cs455.overlay.node;

import java.util.ArrayList;
import java.util.List;
import main.java.cs455.overlay.util.GraphEdge;

public class RegistryEntry {

  public int portNumber;
  public String hostName;
  public String ipAddress;

  public List<GraphEdge> edges;

  public RegistryEntry(int portNumber, String hostName, String ipAddress) {
    this.hostName = hostName;
    this.portNumber = portNumber;
    this.ipAddress = ipAddress;
    this.edges = new ArrayList<>();
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
    return String.format("Node registered on %s at port number: %d\n",
        this.hostName, this.portNumber);
  }

}
