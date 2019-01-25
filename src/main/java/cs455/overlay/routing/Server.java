package main.java.cs455.overlay.routing;

import java.io.*;
import java.net.*;

public class Server {

  public static Integer PORT = 5001;

  public static void main(String[] args) throws IOException {
    ServerSocket serverSocket = new ServerSocket(PORT, 100);
    Socket normalSocket = serverSocket.accept(); // blocking call until someone connects

    System.out.println("Received a connection!");


    InputStream inputStream = normalSocket.getInputStream();
    OutputStream outputStream = normalSocket.getOutputStream();
    // Create wrappers around streams
    DataInputStream dataInputStream = new DataInputStream(inputStream);
    DataOutputStream dataOutputStream = new DataOutputStream(outputStream);

    Integer requestNumber = 0;
    requestNumber = dataInputStream.readInt();

    System.out.printf("Received request number: %d\n", requestNumber);

    dataInputStream.close();
    dataOutputStream.close();
    inputStream.close();
    outputStream.close();
    normalSocket.close();
    serverSocket.close();

    System.out.println("Closed all connections!");
  }

}
