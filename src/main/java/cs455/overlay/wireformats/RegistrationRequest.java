package main.java.cs455.overlay.wireformats;

import java.io.*;

public abstract class RegistrationRequest extends Protocol implements Event {

  /**
   * Takes raw bytes and unmarshalls them into the fields of the Register instance.
   *
   * @param eventBytes Raw byte array of marshalled data
   * @throws IOException
   */
  public RegistrationRequest(byte[] eventBytes) throws IOException {
    super(eventBytes);
  }

  /**
   * Fills out the fields of the Register instance and creates a marshalled byte array from them.
   *
   * @param portNumber The port number of the Node requesting registration.
   * @param hostName The host name of the Node requesting registration.
   * @throws IOException
   */
  public RegistrationRequest(int type, int portNumber, String hostName, String ipAddress)
      throws IOException {
    super(type, portNumber, hostName, ipAddress);
    this.marshallBytes();
  }

  @Override
  public void unmarshallBytes() throws IOException {
    // Open data a data stream out of the raw eventBytes field of this instance.
    ByteArrayInputStream baInput = new ByteArrayInputStream(this.eventBytes);
    DataInputStream dataInput = new DataInputStream(new BufferedInputStream(baInput));

    // Unpacks the type, port, host, and ip from eventBytes into this instance's fields.
    this.unpackProtocol(dataInput);

    // Close the streams.
    baInput.close();
    dataInput.close();
  }

  @Override
  public void marshallBytes() throws IOException {
    byte[] marshalledBytes;
    ByteArrayOutputStream baOutStream = new ByteArrayOutputStream();
    DataOutputStream dataOut = new DataOutputStream(new BufferedOutputStream(baOutStream));

    // Pack the type, port, hostname, and ip address into a marshalled byte array.
    this.packProtocol(dataOut);

    // Flush the DataOutputStream, convert the ByteArrayOutputStream into a byte array,
    // then close the streams.
    dataOut.flush();
    marshalledBytes = baOutStream.toByteArray();
    baOutStream.close();
    dataOut.close();

    this.eventBytes = marshalledBytes;
  }

  @Override
  public String toString() {
    String result = "REGISTRATION REQUEST\n";
    result += "Request Type: ";
    switch (this.type) {
      case 0: result += "Register\n"; break;
      case 1: result += "Deregister\n"; break;
      case 2: result += "Connection Request\n"; break;
      default: result += "Unknown request type!\n";
    }
    result += String.format("Request Origin:\n\tPort %d\n\tHost %s\n\tIP %s\n", this.portNumber,
        this.hostName, this.ipAddress);
    return result;
  }

}
