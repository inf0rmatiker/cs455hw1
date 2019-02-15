package main.java.cs455.overlay.transport;

import java.io.*;
import java.net.*;

public class TCPSender {

  public Socket socket;
  public DataOutputStream dataOutputStream;

  public TCPSender(Socket socket) throws IOException {
    this.socket = socket;
    this.dataOutputStream = new DataOutputStream(socket.getOutputStream());
  }

  public void sendBytes(byte[] bytesToSend) throws IOException {
    // Send the length of bytes as an integer first
    int dataLength = bytesToSend.length;
    dataOutputStream.writeInt(dataLength);

    // Send actual bytes
    dataOutputStream.write(bytesToSend, 0, dataLength);
    dataOutputStream.flush();
    //dataOutputStream.close();
  }
}
