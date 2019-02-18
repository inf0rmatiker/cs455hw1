package main.java.cs455.overlay.util;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class GraphNode {

  private int portNumber;
  private String hostName;
  private String key;
  public List<GraphEdge> edges;


  public GraphNode(int portNumber, String hostName, String key) {
    this.portNumber = portNumber;
    this.hostName = hostName;
    this.key = key;
    this.edges = new ArrayList<>();
  }

  public int getPortNumber() {
    return portNumber;
  }

  public String getHostName() {
    return hostName;
  }

  public String getKey() {
    return key;
  }

  @Override
  public String toString() {
    String result = String.format("\nGraphNode:\n%s\n", key);
    result += String.join("", edges.stream().map(Object::toString).collect(
        Collectors.toList()));
    return result;
  }

}
