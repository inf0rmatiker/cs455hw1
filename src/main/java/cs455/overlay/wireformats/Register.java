package main.java.cs455.overlay.wireformats;

import java.io.*;

public class Register extends RegistrationRequest implements Event {

  public Register(byte[] eventBytes) throws IOException {
    super(eventBytes);
  }

  public Register(int portNumber, String hostName, String ipAddress) throws IOException {
    super(0, portNumber, hostName, ipAddress);
  }

  @Override
  public Protocol.EventType getType() {
    return EventType.REGISTER;
  }


}
