package main.java.cs455.overlay.util;


import java.util.ArrayList;
import java.util.List;
import main.java.cs455.overlay.node.RegistryEntry;

public class OverlayCreator {

  public List<RegistryEntry> nodeList;
  public List<GraphEdge> edgesList;

  public OverlayCreator(List<RegistryEntry> nodeList, int linksPerNode) {
    this.nodeList = nodeList;
    this.edgesList = new ArrayList<>();
    this.configureLinks(linksPerNode);
  }


  /**
   * Algorithm developed partially from Allen Downey's book Think Complexity:
   * https://math.stackexchange.com/questions/142112/how-to-construct-a-k-regular-graph
   * @param linksPerNode The number of connections each node will have
   */
  public void configureLinks(int linksPerNode) {
    if (isOdd(linksPerNode) && isOdd(nodeList.size())) {
      System.err.println("Registered " + nodeList.size() + " nodes.");
      System.err.printf("Can not configure an overlay with odd nodes and odd links per node!\n");
      System.exit(1);
    }
    else if (nodeList.size() == 2 && linksPerNode == 1) {
      GraphEdge edge = new GraphEdge (0,1, 0);
      edgesList.add(edge);
      nodeList.get(0).edges.add(edge);
      nodeList.get(1).edges.add(edge);
    }
    else if (linksPerNode > nodeList.size()-1 || linksPerNode < 1) {
      System.err.printf("Incorrect number of links per node!\n");
      System.exit(1);
    } else {
      createCircleLinks();
      // If k is odd, create link between the node and every node x steps away, where x is all the
      // odd numbers between (1, k).
      for (int i = 0; i < nodeList.size(); i++) {
        if (isOdd(nodeList.size())) { // n is odd

          for (int j = 3; j < linksPerNode; j += 2) {
            addGraphNodeToVertices(i, (j + i) % nodeList.size());
          }

        } else { // n is even
          if (isEven(linksPerNode)) { // k is even
            int numEvens = this.countEvenNumbersBetweenOneAndK(linksPerNode);

            for (int j = 2; j < (numEvens + 1) && j < nodeList.size() / 2; j++) {
              addGraphNodeToVertices(i, (j + i) % nodeList.size());
            }

          } else if (isOdd(linksPerNode)) { // k is odd
            int numOdds = countOddNumbersBetweenOneAndK(linksPerNode);
            for (int j = 0; j < numOdds; j++) {
              if (!nodeList.get(i).edges.contains(
                  new GraphEdge(i, (nodeList.size()/2-j+i) % nodeList.size(), 0))
                  ) {
                addGraphNodeToVertices(i, ((nodeList.size()/2-j) + i) % nodeList.size());
              }
            }
          }
        }
      }
    }

  }

  public void addGraphNodeToVertices(int from, int to) {
    GraphEdge ge = new GraphEdge(from, to, 0);
    edgesList.add(ge);
    nodeList.get(from).edges.add(ge);
    nodeList.get(to).edges.add(ge);
  }

  public int countEvenNumbersBetweenOneAndK(int k) {
    int result = 0;
    for (int i = 2; i <= k; i++) {
      if (isEven(i)) result++;
    }
    return result;
  }

  public int countOddNumbersBetweenOneAndK(int k) {
    int result = 0;
    for (int i = 2; i <= k; i++) {
      if (isOdd(i)) result++;
    }
    return result;
  }


  public void createCircleLinks() {
    int nextIndex = 1;
    for (int i = 0; i < nodeList.size(); i++) {
      nextIndex = nextIndex % nodeList.size();
      GraphEdge graphEdge = new GraphEdge(i, nextIndex, 0);
      edgesList.add(graphEdge);
      nodeList.get(i).edges.add(graphEdge);
      nodeList.get(nextIndex).edges.add(graphEdge);
      nextIndex++;
    }
  }

  public boolean isOdd(int number) {
    return number % 2 == 1;
  }

  public boolean isEven(int number) {
    return number % 2 == 0;
  }



}
