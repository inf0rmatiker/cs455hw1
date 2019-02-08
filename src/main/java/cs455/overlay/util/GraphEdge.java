package main.java.cs455.overlay.util;

/**
 * Class describing the connection between two indexes in nodeList.
 */
public class GraphEdge {
  public int from;
  public int to;
  public int weight;

  public GraphEdge (int from, int to, int weight) {
    this.from = from;
    this.to = to;
    this.weight = weight;
  }
}