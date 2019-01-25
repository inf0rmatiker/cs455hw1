package main.java.cs455.overlay.node;

import java.io.*;
import java.net.*;

public class MessagingNode {

  public static final int REGISTRY_PORT = 5003; // HARDCODED

  public MessagingNode() {

  }

  public static void main(String[] args) throws IOException {
    Socket connection = new Socket("pierre", REGISTRY_PORT);

    InputStream inputStream = connection.getInputStream();
    OutputStream outputStream = connection.getOutputStream();

    DataInputStream dataInputStream = new DataInputStream(inputStream);
    DataOutputStream dataOutputStream = new DataOutputStream(outputStream);

    dataOutputStream.writeInt(3);
    //System.out.printf("Message of length %d received on port %d.\n", messageLength, REGISTRY_PORT);

    dataInputStream.close();
    dataOutputStream.close();
    inputStream.close();
    outputStream.close();
    connection.close();
  }
}
