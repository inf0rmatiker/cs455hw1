package main.java.cs455.overlay.wireformats;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import main.java.cs455.overlay.node.RegistryEntry;
import main.java.cs455.overlay.util.GraphEdge;

public class LinkWeights extends Protocol implements Event {

  private int numberOfLinks;
  private List<RegistryEntry> registryEntries;
  private List<GraphEdge> edges;
  private List<String> linksInformation;

  public LinkWeights(byte[] eventBytes) throws IOException {
    super(eventBytes);
  }

  public LinkWeights(int portNumber, String hostName, String ipAddress, int numberOfLinks,
      List<RegistryEntry> registryEntries, List<GraphEdge> edges) throws IOException {
    super(6, portNumber, hostName, ipAddress);
    this.numberOfLinks = numberOfLinks;
    this.registryEntries = registryEntries;
    this.edges = edges;
    this.marshallBytes();
  }


  public int getNumberOfLinks() {
    return this.numberOfLinks;
  }

  public List<RegistryEntry> getRegistryEntries() {
    return registryEntries;
  }

  public List<GraphEdge> getEdges() {
    return edges;
  }

  public List<String> getLinksInformation() {
    return linksInformation;
  }

  @Override
  public EventType getType() {
    return EventType.LINK_WEIGHTS;
  }

  @Override
  public void marshallBytes() throws IOException {
    byte[] marshalledBytes;
    ByteArrayOutputStream baOutStream = new ByteArrayOutputStream();
    DataOutputStream dataOut = new DataOutputStream(new BufferedOutputStream(baOutStream));

    // Pack the type, port, hostname, and ip address into a marshalled byte array.
    this.packProtocol(dataOut);

    // Write number of links
    dataOut.writeInt(numberOfLinks);

    for (int i = 0; i < numberOfLinks; i++) {
      this.writeLinkInfo(i, dataOut);
    }

    // Flush the DataOutputStream, convert the ByteArrayOutputStream into a byte array,
    // then close the streams.
    dataOut.flush();
    marshalledBytes = baOutStream.toByteArray();
    baOutStream.close();
    dataOut.close();

    this.eventBytes = marshalledBytes;
  }

  /**
   * Writes a String in the form "hostnameA:portnumA hostnameB:portnumB weight" to dataOut
   * @param index The index of the GraphEdge in edges.
   * @param dataOut The DataOutputStream we are writing to
   * @throws IOException
   */
  public void writeLinkInfo(int index, DataOutputStream dataOut) throws IOException {
    GraphEdge ge = edges.get(index);
    String stringToWrite = String.format("%s:%d %s:%d %d",
        registryEntries.get(ge.from).hostName,
        registryEntries.get(ge.from).portNumber,
        registryEntries.get(ge.to).hostName,
        registryEntries.get(ge.to).portNumber,
        ge.weight);

    int lengthOfString = stringToWrite.length();
    dataOut.writeInt(lengthOfString);
    dataOut.write(stringToWrite.getBytes());
  }


  @Override
  public void unmarshallBytes() throws IOException {
    // Open data a data stream out of the raw eventBytes field of this instance.
    ByteArrayInputStream baInput = new ByteArrayInputStream(this.eventBytes);
    DataInputStream dataInput = new DataInputStream(new BufferedInputStream(baInput));

    // Unpacks the type, port, host, and ip from eventBytes into this instance's fields.
    this.unpackProtocol(dataInput);

    // Read how many links we need to read
    this.numberOfLinks = dataInput.readInt();

    // Read n links info into Strings, storing them in linksInformation
    linksInformation = new ArrayList<>();
    for (int i = 0; i < numberOfLinks; i++) {
      this.readLinkInfo(dataInput);
    }

    // Close the streams.
    baInput.close();
    dataInput.close();
  }

  public void readLinkInfo(DataInputStream dataInput) throws IOException {
    int linkInfoLength = dataInput.readInt();
    byte[] linkInfoBytes = new byte[linkInfoLength];
    dataInput.readFully(linkInfoBytes, 0, linkInfoLength);
    String linkInfo = new String(linkInfoBytes);

    linksInformation.add(linkInfo);
  }

  @Override
  public String toString() {
    String result = "Link Weights:\n";
    result += String.format("Number of links: %d\n\n", this.numberOfLinks);
    for (GraphEdge ge: edges) {
      result += String.format("%s:%d %s:%d weight: %d\n",
          registryEntries.get(ge.from).hostName,
          registryEntries.get(ge.from).portNumber,
          registryEntries.get(ge.to).hostName,
          registryEntries.get(ge.to).portNumber,
          ge.weight);
    }
    return result;
  }


}
