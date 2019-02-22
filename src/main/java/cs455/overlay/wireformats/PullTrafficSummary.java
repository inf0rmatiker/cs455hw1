package main.java.cs455.overlay.wireformats;

    import java.io.*;


public class PullTrafficSummary extends Protocol implements Event {

  public PullTrafficSummary(int portNumber, String hostName, String ipAddress) throws IOException {
    super(10, portNumber, hostName, ipAddress);
    this.marshallBytes();
  }

  public PullTrafficSummary(byte[] eventBytes) throws IOException {
    super(eventBytes);
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
  public EventType getType() {
    return EventType.PULL_TRAFFIC_SUMMARY;
  }

}
