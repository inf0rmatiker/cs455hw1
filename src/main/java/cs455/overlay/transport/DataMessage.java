package main.java.cs455.overlay.transport;

import java.net.Socket;

public class DataMessage {

  public Socket socket;
  public byte[] marshalledBytes;

  public DataMessage(Socket socket, byte[] marshalledBytes) {
    this.marshalledBytes = marshalledBytes;
    this.socket = socket;
  }

}
