package main.java.cs455.overlay.wireformats;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public abstract class Protocol implements Event {

  public enum EventType {
    REGISTER, DEREGISTER, REGISTRATION_RESPONSE
  }

  protected byte[] eventBytes;
  protected int portNumber;
  protected String hostName;
  protected String ipAddress;
  public int type;

  /**
   * Returns an enum specifying the type of Event based on
   * the type int passed in.
   * @param type the integer representation of the type of Event
   * @return the enum representation of the type of Event
   */
  public static EventType getType(int type) {
    switch (type) {
      case 0: return EventType.REGISTER;
      case 1: return EventType.DEREGISTER;
      case 2: return EventType.REGISTRATION_RESPONSE;
      default: return null;
    }
  }

  /**
   * Unpacks the portNumber, hostName, and ipAddress data from the raw byte array.
   *
   * @param dataInput The DataInputStream wrapped around the byte array.
   * @throws IOException
   */
  public void unpackProtocol(DataInputStream dataInput) throws IOException {
    this.type = dataInput.readInt(); // Advance the data stream past the 'type' integer.
    this.portNumber = dataInput.readInt();

    int hostNameLength = dataInput.readInt();
    byte[] hostNameBytes = new byte[hostNameLength];
    dataInput.readFully(hostNameBytes, 0, hostNameLength);
    this.hostName = new String(hostNameBytes);

    int ipAddressLength = dataInput.readInt();
    byte[] ipAddressBytes = new byte[ipAddressLength];
    dataInput.readFully(ipAddressBytes, 0, ipAddressLength);
    this.ipAddress = new String(ipAddressBytes);
  }

  /**
   * Packs the type, portNumber, hostName, and ipAddress fields of the instance into the byte array.
   *
   * @param dataOut The DataOutputStream wrapped around the byte array.
   * @throws IOException
   */
  public void packProtocol(DataOutputStream dataOut) throws IOException {
    // Write the originating port number
    dataOut.writeInt(this.type);
    dataOut.writeInt(this.portNumber);

    // Write the host name length and host name
    dataOut.writeInt(this.hostName.length());
    byte[] hostNameBytes = this.hostName.getBytes();
    dataOut.write(hostNameBytes);

    // Write the ip length and bytes
    dataOut.writeInt(this.ipAddress.length());
    byte[] ipAddressBytes = this.ipAddress.getBytes();
    dataOut.write(ipAddressBytes);
  }

  /**
   * Gets the port number of the Event.
   */
  @Override
  public int getPortNumber() {
    return this.portNumber;
  }

  /**
   * Gets the host name of the Event.
   */
  @Override
  public String getHostName() {
    return this.hostName;
  }

  /**
   * Gets the IP address of the Event.
   */
  @Override
  public String getIpAddress() {
    return this.ipAddress;
  }

  /**
   * Gets the raw bytes associated with the Event.
   * @return the raw byte array that contains the Event
   */
  @Override
  public byte[] getBytes() {
    return this.eventBytes;
  }

  public abstract void marshallBytes() throws IOException;

  public abstract void unmarshallBytes() throws IOException;


}
