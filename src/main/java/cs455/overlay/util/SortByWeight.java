package main.java.cs455.overlay.util;

import java.util.Comparator;

public class SortByWeight implements Comparator<GraphEdge>
{
  // Used for sorting in ascending order of
  // roll number
  @Override
  public int compare(GraphEdge a, GraphEdge b)
  {
    return a.weight - b.weight;
  }
}
