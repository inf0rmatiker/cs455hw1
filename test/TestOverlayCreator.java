import static org.junit.Assert.assertEquals;

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
public class TestOverlayCreator {

  OverlayCreator oc;

  @Test
  public void testGetRandomWeight() {
    // Verify that getRandomWeight always returns a value [1, 9]
    oc = new OverlayCreator();

    for (int i = 0; i < 50; i++) {
      int actual = oc.getRandomWeight();
      assertTrue((actual > 0 && actual < 10));
    }
  }

}
