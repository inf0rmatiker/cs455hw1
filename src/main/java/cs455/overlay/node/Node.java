package main.java.cs455.overlay.node;

public class Node {

  public int nodeId;

  public Node(int nodeId) {
    this.nodeId = nodeId;
  }

  public int getId() {
    return this.nodeId;
  }

  public boolean equals(Object other) {
    if (this == other) return true;
    if (!(other instanceof Node)) return false;
    Node otherNode = (Node) other;
    return otherNode.getId() == this.getId();
  }

  public String toString() {
    return String.format("Node ID: %d\n", this.nodeId);
  }

}
