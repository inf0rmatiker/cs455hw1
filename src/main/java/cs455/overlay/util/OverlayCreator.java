package main.java.cs455.overlay.util;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import main.java.cs455.overlay.node.RegistryEntry;

public class OverlayCreator {

  public List<RegistryEntry> nodeList;
  public List<GraphEdge> edgesList;

  // Default ctor for testing
  public OverlayCreator() {

  }

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
      addGraphEdgeToEdgesList(0, 1);
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
            addGraphEdgeToEdgesList(i, (j + i) % nodeList.size());
          }
        } else { // n is even
          if (isEven(linksPerNode)) { // k is even
            int numEvens = this.countEvenNumbersBetweenOneAndK(linksPerNode);
            for (int j = 2; j < (numEvens + 1) && j < nodeList.size() / 2; j++) {
              addGraphEdgeToEdgesList(i, (j + i) % nodeList.size());
            }
          } else if (isOdd(linksPerNode)) { // k is odd
            int numOdds = countOddNumbersBetweenOneAndK(linksPerNode);
            for (int j = 0; j < numOdds; j++) {
              if (!nodeList.get(i).edges.contains(
                  new GraphEdge(i, (nodeList.size()/2-j+i) % nodeList.size(), 0))
                  ) {
                addGraphEdgeToEdgesList(i, ((nodeList.size()/2-j) + i) % nodeList.size());
              }
            }
          }
        }
      }
    }

    this.addEdgesToRegistryEntries();
  }

  public void addGraphEdgeToEdgesList(int from, int to) {
    GraphEdge ge = new GraphEdge(from, to, getRandomWeight());
    edgesList.add(ge);
  }

  public void addEdgesToRegistryEntries() {
    // Sort edges by weight
    Collections.sort(edgesList);

    // Add every edge to both "from" and "to" RegistryEntries edges list
    for (int i = 0; i < edgesList.size(); i++){
      nodeList.get(edgesList.get(i).from).edges.add(edgesList.get(i));
      nodeList.get(edgesList.get(i).to).edges.add(edgesList.get(i));
    }
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

  public int getRandomWeight() {
    Random randomGenerator = new Random();
    int result = randomGenerator.nextInt(10 - 1) + 1;
    return result;
  }

  public void createCircleLinks() {
    int nextIndex = 1;
    for (int i = 0; i < nodeList.size(); i++) {
      nextIndex = nextIndex % nodeList.size();
      addGraphEdgeToEdgesList(i, nextIndex);
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
