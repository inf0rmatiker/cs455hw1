package main.java.cs455.overlay.transport;


import java.io.*;
import java.net.*;
import java.util.concurrent.LinkedBlockingQueue;
import main.java.cs455.overlay.node.Node;
import main.java.cs455.overlay.wireformats.Event;
import main.java.cs455.overlay.wireformats.EventFactory;

/**
 * For now, Receiver and Sender are only working with sending an integer across.
 */
public class TCPReceiverThread implements Runnable {

  public Socket socket;
  private DataInputStream dataInputStream;
  public Node node;
  private final LinkedBlockingQueue<DataMessage> buffer;
  private TCPServerThread server;

  public TCPReceiverThread(Node node, Socket socket, TCPServerThread server, LinkedBlockingQueue<DataMessage> buffer)
      throws IOException {
    this.socket = socket;
    this.node = node;
    this.dataInputStream = new DataInputStream(socket.getInputStream());
    this.buffer = buffer;
    this.server = server;
  }

  @Override
  public void run() {
    int dataLength;
    while (socket != null) {
      try {

        dataLength = dataInputStream.readInt();
        byte[] marshalledBytes = new byte[dataLength];
        dataInputStream.readFully(marshalledBytes, 0, dataLength);

        DataMessage dataMessage = new DataMessage(this.socket, marshalledBytes);
        try {
          buffer.put(dataMessage);
        } catch (InterruptedException e) {

        }

      } catch (SocketException e) {

      } catch (IOException e) {

      }
    }
  }
}