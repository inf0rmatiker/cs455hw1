package main.java.cs455.overlay.transport;


import java.io.*;
import java.net.*;
import main.java.cs455.overlay.node.Node;
import main.java.cs455.overlay.wireformats.Event;
import main.java.cs455.overlay.wireformats.EventFactory;

/**
 * For now, Receiver and Sender are only working with sending an integer across.
 */
public class TCPReceiverThread implements Runnable {

  public Socket socket;
  public DataInputStream dataInputStream;
  public Node node;

  public TCPReceiverThread(Node node, Socket socket) throws IOException {
    this.socket = socket;
    this.node = node;
    this.dataInputStream = new DataInputStream(socket.getInputStream());
  }



  @Override
  public void run() {
    int dataLength;
    while (socket != null) {
      try {
        dataLength = dataInputStream.readInt();
        byte[] marshalledBytes = new byte[dataLength];
        dataInputStream.readFully(marshalledBytes, 0, dataLength);

        EventFactory eventFactory = EventFactory.getInstance();
        Event event = eventFactory.createEvent(marshalledBytes);
        //System.out.println(event);
        this.node.onEvent(event);

        //dataInputStream.close();

      } catch (SocketException e) {

      } catch (IOException e) {

      }
    }
  }
}