package main.java.cs455.overlay.wireformats;

import java.io.*;

public class TaskInitiate extends Protocol implements Event {

  public int numRoundsPerNode;

  public TaskInitiate(int portNumber, String hostName, String ipAddress, int numRoundsPerNode)
      throws IOException {
    super(7, portNumber, hostName, ipAddress);
    this.numRoundsPerNode = numRoundsPerNode;
    this.marshallBytes();
  }

  public TaskInitiate(byte[] eventBytes) throws IOException {
    super(eventBytes);
  }

  @Override
  public EventType getType() {
    return EventType.TASK_INITIATE;
  }

  @Override
  public void marshallBytes() throws IOException {
    byte[] marshalledBytes;
    ByteArrayOutputStream baOutStream = new ByteArrayOutputStream();
    DataOutputStream dataOut = new DataOutputStream(new BufferedOutputStream(baOutStream));

    // Pack the type, port, hostname, and ip address into a marshalled byte array.
    this.packProtocol(dataOut);

    // Write number of rounds per node
    dataOut.writeInt(this.numRoundsPerNode);

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

    // Read number of rounds per node
    this.numRoundsPerNode = dataInput.readInt();

    // Close the streams.
    baInput.close();
    dataInput.close();
  }


}
