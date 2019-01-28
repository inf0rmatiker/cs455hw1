package main.java.cs455.overlay.transport;


import java.io.*;
import java.net.*;

/**
 * For now, Receiver and Sender are only working with sending an integer across.
 */
public class TCPReceiverThread implements Runnable {

  public Socket socket;
  public DataInputStream dataInputStream;

  public TCPReceiverThread(Socket socket) throws IOException {
    this.socket = socket;
    this.dataInputStream = new DataInputStream(socket.getInputStream());
  }

  @Override
  public void run() {
    int portNumber;
    while (socket != null) {
      try {
        portNumber = dataInputStream.readInt();
        //byte[] dataRead = new byte[portNumber];
        System.out.println(portNumber); // Read a num

        dataInputStream.close();
        socket.close();

      } catch (SocketException e) {

      } catch (IOException e) {

      }
    }
  }
}