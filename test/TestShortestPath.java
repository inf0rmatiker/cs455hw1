import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import main.java.cs455.overlay.dijkstra.ShortestPath;
import main.java.cs455.overlay.util.OverlayCreator;
import org.junit.Assert;
import main.java.cs455.overlay.node.Registry;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;



@RunWith(JUnit4.class)
public class TestShortestPath {

  ShortestPath sp;

  @Test
  public void testGetMinDistance() {
    ArrayList<Integer> distances = new ArrayList(Arrays.asList(new int[] {2, 6, 3, 2, 4, 9, 1, 7}));
  }

}
