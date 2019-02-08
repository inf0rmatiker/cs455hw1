package main.java.cs455.overlay.wireformats;

import java.io.*;

/**
 * RegistrationResponse contains the success response of either registration or deregistration
 * requests.
 *
 * A status code represents success or failure of the registration type.
 * Status code 0: Failure
 * Status code 1: Success
 */
public class RegistrationResponse extends Protocol implements Event {

  public byte registrationTypeCode;
  public byte statusCode;
  public String description;

  /**
   * Fills out the fields of the RegistrationResponse instance and creates a marshalled
   * byte array from them.
   *
   * @param statusCode Byte representing the status code of the registration success or failure.
   * @param description String representing any additional information included with response.
   * @throws IOException
   */
  public RegistrationResponse(int portNumber, String hostName, String ipAddress,
      byte registrationTypeCode, byte statusCode, String description) throws IOException {
    this.type = 2;
    this.portNumber = portNumber;
    this.hostName = hostName;
    this.ipAddress = ipAddress;
    this.registrationTypeCode = registrationTypeCode;
    this.statusCode = statusCode;
    this.description = description;
    this.marshallBytes();
  }

  /**
   * Takes raw bytes and unmarshalls them into the fields of the RegistrationResponse instance.
   *
   * @param eventBytes Raw byte array of marshalled data
   * @throws IOException
   */
  public RegistrationResponse(byte[] eventBytes) throws IOException {
    this.eventBytes = eventBytes;
    this.unmarshallBytes();
  }


  public boolean isSuccess() {
    // 1 is success, 0 is failure.
    return this.statusCode == 1;
  }

  /**
   * @return True if this response is for deregistration
   */
  public boolean isForDeregistration() {
    // 1 is deregistration, 0 is registration.
    return this.registrationTypeCode == 1;
  }

  @Override
  public EventType getType() {
    return EventType.REGISTRATION_RESPONSE;
  }

  @Override
  public void marshallBytes() throws IOException {
    byte[] marshalledBytes;
    ByteArrayOutputStream baOutStream = new ByteArrayOutputStream();
    DataOutputStream dataOut = new DataOutputStream(new BufferedOutputStream(baOutStream));

    // Pack the type, port, hostname, and ip address into a marshalled byte array.
    this.packProtocol(dataOut);

    // Write registrationTypeCode, statusCode, description length, and description.
    dataOut.writeByte(this.registrationTypeCode);
    dataOut.writeByte(this.statusCode);
    dataOut.writeInt(this.description.length());

    byte[] descriptionBytes = description.getBytes();
    dataOut.write(descriptionBytes);

    dataOut.flush();
    marshalledBytes = baOutStream.toByteArray();
    baOutStream.close();
    dataOut.close();

    this.eventBytes = marshalledBytes;
  }

  @Override
  public void unmarshallBytes() throws IOException {
    // TODO: Unpack bytes of REGISTRATION RESPONSE into appropriate fields.
    // Open data a data stream out of the raw eventBytes field of this instance.
    ByteArrayInputStream baInput = new ByteArrayInputStream(this.eventBytes);
    DataInputStream dataInput = new DataInputStream(new BufferedInputStream(baInput));

    // Read type, port, host name, and ip address.
    this.unpackProtocol(dataInput);

    // Read registration response type code
    this.registrationTypeCode = dataInput.readByte();

    // Read status byte
    this.statusCode = dataInput.readByte();

    // Read description
    int descriptionLength = dataInput.readInt();
    byte[] descriptionBytes = new byte[descriptionLength];
    dataInput.readFully(descriptionBytes, 0, descriptionLength);
    this.description = new String(descriptionBytes);

    // Close the streams.
    baInput.close();
    dataInput.close();
  }

  @Override
  public String toString() {
    String result = "REGISTRATION RESPONSE\n";
    result += "Response Type: ";
    result += (this.isForDeregistration()) ? "Deregistration\n" : "Registration\n";
    result += (this.isSuccess()) ? "Success\n" : "Failure\n";
    result += String.format("Description: %s\n", this.description);
    return result;
  }

}
