package main.java.cs455.overlay.wireformats;

import java.io.*;


public class TrafficSummary extends Protocol implements Event {

  private int numMessagesSent;
  private long summationSent;
  private int numMessagesReceived;
  private long summationReceived;
  private int numMessagesRelayed;

  public TrafficSummary(int portNumber, String hostName, String ipAddress,
      int numMessagesSent, long summationSent, int numMessagesReceived, long summationReceived,
      int numMessagesRelayed) throws IOException {
    super(11, portNumber, hostName, ipAddress);
    this.numMessagesSent = numMessagesSent;
    this.summationSent = summationSent;
    this.numMessagesReceived = numMessagesReceived;
    this.summationReceived = summationReceived;
    this.numMessagesRelayed = numMessagesRelayed;
    this.marshallBytes();
  }

  public TrafficSummary(byte[] eventBytes) throws IOException {
    super(eventBytes);
  }

  public int getNumMessagesSent() {
    return numMessagesSent;
  }

  public long getSummationSent() {
    return summationSent;
  }

  public int getNumMessagesReceived() {
    return numMessagesReceived;
  }

  public long getSummationReceived() {
    return summationReceived;
  }

  public int getNumMessagesRelayed() {
    return numMessagesRelayed;
  }

  @Override
  public void unmarshallBytes() throws IOException {
    // Open data a data stream out of the raw eventBytes field of this instance.
    ByteArrayInputStream baInput = new ByteArrayInputStream(this.eventBytes);
    DataInputStream dataInput = new DataInputStream(new BufferedInputStream(baInput));

    // Unpacks the type, port, host, and ip from eventBytes into this instance's fields.
    this.unpackProtocol(dataInput);

    this.numMessagesSent = dataInput.readInt();
    this.summationSent = dataInput.readLong();
    this.numMessagesReceived = dataInput.readInt();
    this.summationReceived = dataInput.readLong();
    this.numMessagesRelayed = dataInput.readInt();

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

    dataOut.writeInt(numMessagesSent);
    dataOut.writeLong(summationSent);
    dataOut.writeInt(numMessagesReceived);
    dataOut.writeLong(summationReceived);
    dataOut.writeInt(numMessagesRelayed);

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
    return EventType.TRAFFIC_SUMMARY;
  }

}

