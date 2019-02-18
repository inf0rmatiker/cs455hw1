package main.java.cs455.overlay.wireformats;

import java.io.*;
import main.java.cs455.overlay.wireformats.Protocol.EventType;

public class ConnectionResponse extends Protocol implements Event {

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
    public ConnectionResponse(int portNumber, String hostName, String ipAddress, byte statusCode,
        String description) throws IOException {
      super(5, portNumber, hostName, ipAddress);
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
    public ConnectionResponse(byte[] eventBytes) throws IOException {
      super(eventBytes);
    }


    public boolean isSuccess() {
      // 1 is success, 0 is failure.
      return this.statusCode == 1;
    }

    @Override
    public EventType getType() {
      return EventType.CONNECTION_RESPONSE;
    }

    @Override
    public void marshallBytes() throws IOException {
      byte[] marshalledBytes;
      ByteArrayOutputStream baOutStream = new ByteArrayOutputStream();
      DataOutputStream dataOut = new DataOutputStream(new BufferedOutputStream(baOutStream));

      // Pack the type, port, hostname, and ip address into a marshalled byte array.
      this.packProtocol(dataOut);

      // Write statusCode, description length, and description.
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
      // Open data a data stream out of the raw eventBytes field of this instance.
      ByteArrayInputStream baInput = new ByteArrayInputStream(this.eventBytes);
      DataInputStream dataInput = new DataInputStream(new BufferedInputStream(baInput));

      // Read type, port, host name, and ip address.
      this.unpackProtocol(dataInput);

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
      return String.format("Connection Response:\nOrigin: %s on port %d\n",
          this.hostName, this.portNumber);
    }



  }
