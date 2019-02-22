package main.java.cs455.overlay.transport;

import java.io.*;
import java.net.*;
import java.util.List;
import main.java.cs455.overlay.wireformats.Message;

public class TCPSender {

  public Socket socket;
  private List<Message> sendBuffer;

  public TCPSender(Socket socket) throws IOException {
    this.socket = socket;
  }

//  public TCPSender(List<Message> sendBuffer, ) throws IOException {
//    this.sendBuffer = sendBuffer;
//  }

  public void sendBytes(byte[] bytesToSend) throws IOException {
      DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
      // Send the length of bytes as an integer first
      int dataLength = bytesToSend.length;
      dataOutputStream.writeInt(dataLength);

      // Send actual bytes
      dataOutputStream.write(bytesToSend, 0, dataLength);
      dataOutputStream.flush();
  }
}
