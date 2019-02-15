package main.java.cs455.overlay.wireformats;

import java.io.*;
import main.java.cs455.overlay.node.MessagingNode;

public class EventFactory {

  private static EventFactory single_instance = null;

  /**
   * Private constructor that can only be called within the EventFactory class.
   */
  private EventFactory() {

  }

  /**
   * Creates and returns the appropriate polymorphic Event object based on the type
   * specified in the first integer of the byte stream.
   * @param marshalledBytes Marshalled bytes received from the TCPReceiverThread
   * @return an Event object (Register, Deregister, etc...) based on raw bytes received
   * @throws IOException
   */
  public Event createEvent(byte[] marshalledBytes) throws IOException {
    // Create a DataInputStream from our marshalled bytes
    ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(marshalledBytes);
    DataInputStream dataInputStream = new DataInputStream(new BufferedInputStream(byteArrayInputStream));

    // Read the int type
    int type = dataInputStream.readInt();

    switch (Protocol.getType(type)) {
      case REGISTER:   return createRegisterEvent(marshalledBytes);
      case DEREGISTER: return createDeregisterEvent(marshalledBytes);
      case REGISTRATION_RESPONSE: return createRegistrationResponse(marshalledBytes);
      case MESSAGING_NODES_LIST: return createMessagingNodesListEvent(marshalledBytes);
      case CONNECTION_REQUEST: return createConnectionRequest(marshalledBytes);
      case CONNECTION_RESPONSE: return createConnectionResponse(marshalledBytes);
      default: return null;
    }
  }

  public Register createRegisterEvent(byte[] marshalledBytes) throws IOException {
    return new Register(marshalledBytes);
  }

  public RegistrationResponse createRegistrationResponse(byte[] marshalledBytes) throws IOException {
    return new RegistrationResponse(marshalledBytes);
  }

  public Deregister createDeregisterEvent(byte[] marshalledBytes) throws IOException {
    return new Deregister(marshalledBytes);
  }

  public MessagingNodesList createMessagingNodesListEvent(byte[] marshalledBytes) throws IOException {
    return new MessagingNodesList(marshalledBytes);
  }

  public ConnectionRequest createConnectionRequest(byte[] marshalledBytes) throws IOException {
    return new ConnectionRequest(marshalledBytes);
  }

  public ConnectionResponse createConnectionResponse(byte[] marshalledBytes) throws IOException {
    return new ConnectionResponse(marshalledBytes);
  }

  /**
   * Gets a Singleton instance of EventFactory if it has not already been initiated
   */
  public static EventFactory getInstance() {
    if (single_instance == null) {
      single_instance = new EventFactory();
    }

    return single_instance;
  }

}
