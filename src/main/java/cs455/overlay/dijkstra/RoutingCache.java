package main.java.cs455.overlay.dijkstra;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import main.java.cs455.overlay.util.GraphEdge;
import main.java.cs455.overlay.util.GraphNode;

public class RoutingCache {

  public List<GraphNode> nodes; // master list of nodes
  public List<GraphEdge> edges; // master list of edges

  public RoutingCache() {
    this.nodes = new ArrayList<>();
    this.edges = new ArrayList<>();
  }

  @Override
  public String toString() {
    String result = "\nRouting Cache:\n";
    result += String.join("\n", nodes.stream().map(Object::toString).collect(
        Collectors.toList()));
    return result;
  }


}
