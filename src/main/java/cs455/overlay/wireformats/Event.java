package main.java.cs455.overlay.wireformats;

import java.io.*;

public interface Event {

  public Protocol.EventType getType();

  public byte[] getBytes();

  public int getPortNumber();

  public String getHostName();

  public String getIpAddress();

  /**
   * Takes the fields of the Event object and packs them into a byte array
   * according to Protocol
   * @throws IOException
   */
  public void marshallBytes() throws IOException;

  /**
   * Takes the marshalled byte array and unpacks them into the fields of the Event object
   * @throws IOException
   */
  public void unmarshallBytes() throws IOException;

}
