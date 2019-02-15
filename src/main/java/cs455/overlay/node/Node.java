package main.java.cs455.overlay.node;

import java.io.*;
import java.net.*;
import main.java.cs455.overlay.wireformats.Event;

public interface Node {

  public void onEvent(Event event, Socket socket) throws IOException;

}
