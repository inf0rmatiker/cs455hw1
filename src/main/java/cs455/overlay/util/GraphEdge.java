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

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof GraphEdge)) return false;
    GraphEdge other = (GraphEdge) o;

    if (this.from == other.from && this.to == other.to) return true;
    if (this.from == other.to && this.to == other.from) return true;
    return false;
  }

  @Override
  public String toString() {
    return String.format("Edge from index %d to index %d, with weight %d.\n", from, to, weight);
  }
}