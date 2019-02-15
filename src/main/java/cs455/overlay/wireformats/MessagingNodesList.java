package main.java.cs455.overlay.wireformats;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import main.java.cs455.overlay.node.RegistryEntry;

public class MessagingNodesList extends Protocol implements Event {

  public List<RegistryEntry> nodeList;

  public MessagingNodesList(byte[] eventBytes) throws IOException {
    super(eventBytes);
  }

  public MessagingNodesList(int portNumber, String hostName, String ipAddress,
      List<RegistryEntry> listOfNodesToConnectTo) throws IOException {
    super(3, portNumber, hostName, ipAddress);
    this.nodeList = listOfNodesToConnectTo;
    this.marshallBytes();
  }

  public void unmarshallBytes() throws IOException {
    this.nodeList = new ArrayList<RegistryEntry>();
    // Open data a data stream out of the raw eventBytes field of this instance.
    ByteArrayInputStream baInput = new ByteArrayInputStream(this.eventBytes);
    DataInputStream dataInput = new DataInputStream(new BufferedInputStream(baInput));

    // Unpacks the type, port, host, and ip from eventBytes into this instance's fields.
    this.unpackProtocol(dataInput);

    // Read how many nodes we will be connecting to
    int sizeOfNodeList = dataInput.readInt();

    // Unpack every node from the byte[] into RegistryEvent objects and store them in nodeList
    for (int i = 0; i < sizeOfNodeList; i++) {
      unpackNodeFromBytes(dataInput);
    }

    // Close the streams.
    baInput.close();
    dataInput.close();
  }

  public void marshallBytes() throws IOException {
    byte[] marshalledBytes;
    ByteArrayOutputStream baOutStream = new ByteArrayOutputStream();
    DataOutputStream dataOut = new DataOutputStream(new BufferedOutputStream(baOutStream));

    // Pack the type, port, hostname, and ip address into the marshalled byte array.
    this.packProtocol(dataOut);

    // Pack the size of nodeList
    dataOut.writeInt(nodeList.size());

    // Pack every node into the stream
    for (int i = 0; i < nodeList.size(); i++) {
      packNodeIntoBytes(i, dataOut);
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
   * Packs port number, followed by hostName length, and hostName.
   * @param index index of the RegistryEntry in nodeList
   * @param dataOut data stream which we are packing bytes into
   */
  public void packNodeIntoBytes(int index, DataOutputStream dataOut) throws IOException {
    int portNumber = nodeList.get(index).portNumber;
    dataOut.writeInt(portNumber);

    int hostNameLength = nodeList.get(index).hostName.length();
    dataOut.writeInt(hostNameLength);

    String hostName = nodeList.get(index).hostName;
    byte[] hostNameBytes = hostName.getBytes();
    dataOut.write(hostNameBytes);
  }

  /**
   * Unpacks a node by reading a portNumber and hostName into a new RegistryEntry object and
   * adding it to the nodeList field.
   * @param dataInput The stream we are reading raw node data from
   * @throws IOException
   */
  public void unpackNodeFromBytes(DataInputStream dataInput) throws IOException {
    RegistryEntry node;

    // Read port number
    int portNumber = dataInput.readInt();

    // read hostName length, followed by hostName
    int hostNameLength = dataInput.readInt();
    byte[] hostNameBytes = new byte[hostNameLength];
    dataInput.readFully(hostNameBytes, 0, hostNameLength);
    String hostName = new String(hostNameBytes);

    nodeList.add(new RegistryEntry(portNumber, hostName, "", null));
  }

  public Protocol.EventType getType() {
    return EventType.MESSAGING_NODES_LIST;
  }

}
