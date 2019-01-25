package main.java.cs455.overlay.routing;

import java.io.*;
import java.net.*;

public class Client {
  public static Integer SERVER_PORT = 5001;

  public static void main(String[] args) throws IOException {
    Socket connection = new Socket("boise.cs.colostate.edu", SERVER_PORT);

    InputStream inputStream = connection.getInputStream();
    OutputStream outputStream = connection.getOutputStream();

    DataInputStream dataInputStream = new DataInputStream(inputStream);
    DataOutputStream dataOutputStream = new DataOutputStream(outputStream);

    dataOutputStream.writeInt(13);

    dataInputStream.close();
    dataOutputStream.close();
    inputStream.close();
    outputStream.close();
    connection.close();
  }

}
