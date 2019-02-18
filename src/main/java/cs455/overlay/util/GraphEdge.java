package main.java.cs455.overlay.util;


/**
 * Class describing the connection between two indexes in nodeList.
 */
public class GraphEdge implements Comparable<GraphEdge> {
  public int from;
  public int to;
  public int weight;

  public GraphEdge (int from, int to, int weight) {
    this.from = from;
    this.to = to;
    this.weight = weight;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (!(obj instanceof GraphEdge)) return false;
    GraphEdge other = (GraphEdge) obj;

    if (this.from == other.from && this.to == other.to) return true;
    if (this.from == other.to && this.to == other.from) return true;
    return false;
  }

  /**
   * Specifies that the GraphEdge with the lower weight should come first in a list.
   * @param other The GraphEdge object we are being compared against
   * @return 0 if weights are equal, 1 if other should come first, -1 if (this) should come first.
   */
  @Override
  public int compareTo(GraphEdge other) {
    return this.weight - other.weight;
  }

  @Override
  public String toString() {
    return String.format("Edge from index %d to index %d, with weight %d.\n", from, to, weight);
  }
}