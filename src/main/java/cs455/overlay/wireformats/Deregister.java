package main.java.cs455.overlay.wireformats;

import java.io.*;

public class Deregister extends RegistrationRequest implements Event {

  public Deregister(byte[] eventBytes) throws IOException {
    super(eventBytes);
  }

  public Deregister(int portNumber, String hostName, String ipAddress) throws IOException {
    super(1, portNumber, hostName, ipAddress);
  }

  @Override
  public Protocol.EventType getType() {
    return EventType.DEREGISTER;
  }


}
