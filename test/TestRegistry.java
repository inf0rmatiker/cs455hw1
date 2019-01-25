import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import main.java.cs455.overlay.node.Registry;
import main.java.cs455.overlay.node.Node;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(JUnit4.class)
public class TestRegistry {

  Registry registry;

  @Test
  public void testBoolean() {
    assertEquals(true, true);
  }

  @Test
  public void testIsNodeRegistered() {
    registry = new Registry();
    for (int i = 1; i < 9; i++) {
      registry.nodeList.add(new Node(i));
    }

    for (int i = 1; i < 9; i++) {
      assertEquals(true, registry.isNodeRegistered(i));
    }
  }

  @Test
  public void testIsNodeNotRegistered() {
    registry = new Registry();
    for (int i = 1; i < 9; i++) {
      registry.nodeList.add(new Node(i));
    }

    assertFalse(registry.isNodeRegistered(0));
  }

  @Test
  public void testDeregisterNode() {
    registry = new Registry();
    for (int i = 1; i < 9; i++) {
      registry.nodeList.add(new Node(i));
    }

    registry.deregisterMessagingNode(3);
    assertFalse(registry.nodeList.contains(new Node(3)));
  }


}
