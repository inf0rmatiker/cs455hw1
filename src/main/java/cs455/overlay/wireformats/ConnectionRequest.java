package main.java.cs455.overlay.wireformats;

import java.io.*;

public class ConnectionRequest extends RegistrationRequest implements Event {

  public ConnectionRequest(int portNumber, String hostName, String ipAddress) throws IOException {
    super(4, portNumber, hostName, ipAddress);
    this.marshallBytes();
  }

  public ConnectionRequest(byte[] eventBytes) throws IOException {
    super(eventBytes);
  }

  public Protocol.EventType getType() {
    return EventType.CONNECTION_REQUEST;
  }




}
