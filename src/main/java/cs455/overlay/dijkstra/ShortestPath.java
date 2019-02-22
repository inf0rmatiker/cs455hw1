package main.java.cs455.overlay.dijkstra;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import main.java.cs455.overlay.util.GraphEdge;
import main.java.cs455.overlay.util.GraphNode;

/**
 * Computes the Dijkstra's shortest path algorithm for a given starting node.
 */
public class ShortestPath {

  private RoutingCache rCache;

  private int startIndex;
  private List<GraphNode> vertices;
  private List<Integer> distances;
  private List<Integer> previous;
  private List<Integer> visited;



  public ShortestPath(RoutingCache rCache) {
    this.rCache = rCache;
    this.startIndex = rCache.getStartingIndex();
    this.vertices = rCache.nodes;
    this.initializeDistances();
    this.initializePrevious();
    this.initializeVisited();
  }

  private void initializeDistances() {
    this.distances = new ArrayList<>(vertices.size());
    for (int i = 0; i < vertices.size(); i++) {
      distances.add((i == startIndex) ? 0 : Integer.MAX_VALUE);
    }
  }

  private void initializePrevious() {
    this.previous = new ArrayList<>(vertices.size());
    for (int i = 0; i < vertices.size(); i++) {
      previous.add(null);
    }
  }

  private void initializeVisited() {
    visited = new ArrayList<>();
  }

  /**
   * @return int index of unvisited node with smallest distance
   */
  private int getMinIndex() {
    int minIndex = -1;
    int minDistance = Integer.MAX_VALUE;

    for (int i = 0; i < distances.size(); i++) {
      if (!visited.contains(i) && distances.get(i) < minDistance) {
        minIndex = i;
        minDistance = distances.get(i);
      }
    }

    return minIndex;
  }

  public void calculateShortestPaths() {
    // While there are still nodes to visit
    while (visited.size() < vertices.size()) {
      int evalNodeIndex = getMinIndex(); // Our index, starts at startIndex
      GraphNode evalNode = vertices.get(evalNodeIndex); // Get unvisited node with lowest distance

      for (GraphEdge ge: evalNode.edges) {
        int toIndex = (ge.from == evalNodeIndex) ? ge.to : ge.from; // Get neighbor's index
        // If the the node is unvisited and the distance to the current node + the edge distance is
        // smaller than the current recorded distance for that node
        if (!visited.contains(toIndex) &&
            distances.get(evalNodeIndex) + ge.weight < distances.get(toIndex)) {
          distances.set(toIndex, distances.get(evalNodeIndex) + ge.weight); // Update entry in distances
          previous.set(toIndex, evalNodeIndex);
        }

      }
      visited.add(evalNodeIndex); // Add evalNodeIndex to visited
    }
  }

  public void convertResultsToPaths() {
    for (int i = 0; i < vertices.size(); i++) {
      // We don't need a shortest path from ourself to ourself!
      if (i != startIndex) {
        LinkedList<Integer> indexPath = new LinkedList<>();
        indexPath.add(0, i);
        convertPreviousToPath(indexPath, i);
        String key = vertices.get(i).getKey();

        List<GraphNode> path = indexesToGraphNodes(indexPath);
        rCache.addShortestRouteToPaths(key, path);
        // Uncomment to see entries in rCache's hash map
        /*
        System.out.println("Key:" + key);
        System.out.print("Path: ");
        for (GraphNode ge: path) {
          System.out.print(ge.getKey() + " ");
        }
        System.out.println();
        */
      }
    }
  }

  public List<GraphNode> indexesToGraphNodes(List<Integer> indexPath) {
    LinkedList<GraphNode> path = new LinkedList<>();
    for (int i = 0; i < indexPath.size(); i++) {
      path.add(vertices.get(indexPath.get(i)));
    }
    return path;
  }

  public void convertPreviousToPath(List<Integer> path, Integer index) {
    while (previous.get(index) != null) {
      path.add(0, previous.get(index));
      index = previous.get(index);
//      System.out.println(index);
//      System.out.println(path);
//      System.out.println(previous);
    }
  }

  public void printShortestPaths() {

    System.out.printf("\n>> Printing paths to all nodes from node at %s <<\n\n", vertices.get(startIndex).getKey());
    for (int i = 0; i < vertices.size(); i++) {
      if (i != startIndex) {
        List<Integer> path = new LinkedList<>();
        path.add(i);
        convertPreviousToPath(path, i);
        String result = vertices.get(startIndex).getHostName();
        for (int j = 1; j < path.size(); j++){
          int from = path.get(j-1);
          int to = path.get(j);
          result += "--" + getWeight(from, to) + "--" + vertices.get(to).getHostName();
        }
        System.out.println(result);
      }
    }
  }

  private int getWeight(int from, int to) {
    for ( GraphEdge ge: rCache.edges ) {
      if ((ge.from == from && ge.to == to) || (ge.to == from && ge.from == to )) {
        return ge.weight;
      }
    }
    return -1;
  }

  public void printArrays() {
    System.out.print("Distances: [");
    for (Integer i: distances) {
      System.out.print(" " + i + " ");
    }
    System.out.println("]");
    System.out.print("Previous: [");
    for (Integer i: previous) {
      System.out.print(" " + i + " ");
    }
    System.out.println("]");
  }



}
