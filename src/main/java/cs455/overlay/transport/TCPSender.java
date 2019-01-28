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

  public void sendData(int dataLength) throws IOException {
    dataOutputStream.writeInt(dataLength);
    dataOutputStream.flush();
    dataOutputStream.close();
  }


}
