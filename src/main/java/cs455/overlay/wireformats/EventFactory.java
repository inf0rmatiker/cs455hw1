package main.java.cs455.overlay.wireformats;

public class EventFactory {

  private static EventFactory single_instance = null;

  private EventFactory() {

  }

  public static EventFactory getInstance() {
    if (single_instance == null) {
      single_instance = new EventFactory();
    }

    return single_instance;
  }

}
