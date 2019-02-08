package main.java.cs455.overlay.util;


import java.util.ArrayList;
import java.util.List;
import main.java.cs455.overlay.node.RegistryEntry;

public class OverlayCreator {

  public List<RegistryEntry> nodeList;
  public List<GraphEdge> edgesList;

  public OverlayCreator(List<RegistryEntry> nodeList, int linksPerNode) {
    this.nodeList = nodeList;
    this.configureLinks(linksPerNode);
    this.edgesList = new ArrayList<>();
  }

  public void configureLinks(int linksPerNode) {
    if (isOdd(linksPerNode) && isOdd(nodeList.size())) {
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
    }
    else if () {

    }



  }

  public boolean isOdd(int number) {
    return number % 2 == 1;
  }

  public boolean isEven(int number) {
    return number % 2 == 0;
  }



}
