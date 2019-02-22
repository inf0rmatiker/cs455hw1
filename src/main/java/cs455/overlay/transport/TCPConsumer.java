package main.java.cs455.overlay.transport;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.LinkedBlockingQueue;
import main.java.cs455.overlay.node.Node;
import main.java.cs455.overlay.wireformats.Event;
import main.java.cs455.overlay.wireformats.EventFactory;

public class TCPConsumer implements Runnable {

  //private LinkedBlockingQueue<DataMessage> buffer;
  private Node node;
  private TCPServerThread server;
  private final LinkedBlockingQueue<DataMessage> buffer;

  public TCPConsumer(Node node, TCPServerThread server, LinkedBlockingQueue<DataMessage> buffer) {
    this.node = node;
    this.server = server;
    this.buffer = buffer;
  }

  @Override
  public void run() {

    while (true) {
      DataMessage message;
      try {
        message = buffer.take();
        byte[] marshalledBytes = message.marshalledBytes;
        Socket socket = message.socket;
        EventFactory eventFactory = EventFactory.getInstance();
        try {
          Event event = eventFactory.createEvent(marshalledBytes);
          this.node.onEvent(event, socket);
        } catch (IOException e) {

        }
      } catch (InterruptedException e) {

      }
    }
  }
}





