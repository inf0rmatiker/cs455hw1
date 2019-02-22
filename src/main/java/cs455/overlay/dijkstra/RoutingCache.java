package main.java.cs455.overlay.dijkstra;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import main.java.cs455.overlay.util.GraphEdge;
import main.java.cs455.overlay.util.GraphNode;

public class RoutingCache {

  private int startingIndex;
  public List<GraphNode> nodes; // master list of nodes
  public List<GraphEdge> edges; // master list of edges
  public HashMap<String, List<GraphNode>> shortestPaths;

  public RoutingCache() {
    this.nodes = new ArrayList<>();
    this.edges = new ArrayList<>();
    this.shortestPaths = new HashMap<>();
  }

  public int getStartingIndex() {
    return startingIndex;
  }

  public void setStartingIndex(int index) {
    this.startingIndex = index;
  }

  public void addShortestRouteToPaths(String key, List<GraphNode> orderedVertices) {
    this.shortestPaths.put(key, orderedVertices);
  }

  @Override
  public String toString() {
    String result = "\nRouting Cache:\n";
    result += String.join("\n", nodes.stream().map(Object::toString).collect(
        Collectors.toList()));
    return result;
  }
}
