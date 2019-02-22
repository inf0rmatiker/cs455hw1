package main.java.cs455.overlay.wireformats;

import java.io.*;

public class Message extends Protocol implements Event {

  public int payload;
  public String destination;

  public Message(int portNumber, String hostName, String ipAddress, int payload, String destination)
      throws IOException {
    super(8, portNumber, hostName, ipAddress);
    this.payload = payload;
    this.destination = destination;
    this.marshallBytes();
  }

  public Message(byte[] eventBytes) throws IOException {
    super(eventBytes);
  }

  @Override
  public EventType getType() {
    return EventType.MESSAGE;
  }

  @Override
  public void marshallBytes() throws IOException {
    byte[] marshalledBytes;
    ByteArrayOutputStream baOutStream = new ByteArrayOutputStream();
    DataOutputStream dataOut = new DataOutputStream(new BufferedOutputStream(baOutStream));

    // Pack the type, port, hostname, and ip address into a marshalled byte array.
    this.packProtocol(dataOut);

    // Write the payload
    dataOut.writeInt(payload);

    // Write the destination
    dataOut.writeInt(destination.length());
    byte[] destinationBytes = destination.getBytes();
    dataOut.write(destinationBytes);

    // Flush the DataOutputStream, convert the ByteArrayOutputStream into a byte array,
    // then close the streams.
    dataOut.flush();
    marshalledBytes = baOutStream.toByteArray();
    baOutStream.close();
    dataOut.close();

    this.eventBytes = marshalledBytes;
  }

  @Override
  public void unmarshallBytes() throws IOException {

    // Open data a data stream out of the raw eventBytes field of this instance.
    ByteArrayInputStream baInput = new ByteArrayInputStream(this.eventBytes);
    DataInputStream dataInput = new DataInputStream(new BufferedInputStream(baInput));

    // Unpacks the type, port, host, and ip from eventBytes into this instance's fields.
    this.unpackProtocol(dataInput);

    // Read the payload
    this.payload = dataInput.readInt();

    // Read destination
    int destinationLength = dataInput.readInt();
    byte[] destinationBytes = new byte[destinationLength];
    dataInput.readFully(destinationBytes, 0, destinationLength);
    this.destination = new String(destinationBytes);

    // Close the streams.
    baInput.close();
    dataInput.close();
  }

}
