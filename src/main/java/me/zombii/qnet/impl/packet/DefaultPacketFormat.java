package me.zombii.qnet.impl.packet;

import me.zombii.qnet.api.packet.IPacket;
import me.zombii.qnet.api.packet.IPacketFormat;
import me.zombii.qnet.api.packet.IPacketProtocol;
import me.zombii.qnet.io.Deserializer;
import me.zombii.qnet.io.Serializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public class DefaultPacketFormat implements IPacketFormat {

    final int compressionLevel;
    final boolean compressionEnabled;

    public DefaultPacketFormat() {
        this(true, Deflater.DEFAULT_COMPRESSION);
    }

    public DefaultPacketFormat(boolean doCompression) {
        this(doCompression, Deflater.DEFAULT_COMPRESSION);
    }

    public DefaultPacketFormat(boolean doCompression, int compressionLevel) {
        this.compressionEnabled = doCompression;
        this.compressionLevel = compressionLevel;
    }

    Deflater deflater = new Deflater();
    byte[] deflationBuffer = new byte[1024];
    byte[] inflationBuffer = new byte[1024];

    Inflater inflater = new Inflater();

    public byte[] deflate(byte[] bytes) {
        if (!compressionEnabled)
            return bytes;

        Arrays.fill(deflationBuffer, (byte) 0);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        deflater.setInput(bytes);
        deflater.finish();

        while (!deflater.finished()) {
            int count = deflater.deflate(deflationBuffer);
            baos.write(deflationBuffer, 0, count);
        }
        deflater.reset();

        return baos.toByteArray();
    }

    public byte[] inflate(int expectedSize, byte[] bytes) throws DataFormatException {
        if (!compressionEnabled)
            return bytes;

        byte[] inflated = new byte[expectedSize];

        Arrays.fill(inflationBuffer, (byte) 0);

        inflater.setInput(bytes);
        inflater.inflate(inflated);

        inflater.reset();
        return inflated;
    }

    @Override
    public void write(IPacketProtocol protocol, Serializer serializer, IPacket packet) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Serializer packetSerializer = new Serializer(baos);
        packet.write(packetSerializer);
        baos.close();
        byte[] bytes = baos.toByteArray();
        byte[] deflated = deflate(bytes);

        int id = protocol.getPacketId(packet);
        serializer.writeInt(id);  // packet-id
        if (compressionEnabled) {
            serializer.writeInt(bytes.length);  // decompressed-size
        }
        serializer.writeByteArray(deflated);
    }

    @Override
    public IPacket read(IPacketProtocol protocol, Deserializer deserializer) throws IOException, DataFormatException {
        int packetId = deserializer.readInt();
        if (!protocol.hasPacketIdBeenTaken(packetId))
            throw new DataFormatException("Received packet of id: " + packetId + " that doesn't exist in current protocol!");

        int decompressedSize = 0;

        if (compressionEnabled)
            decompressedSize = deserializer.readInt();

        byte[] bytes = deserializer.readByteArray();
        byte[] decompressed = inflate(decompressedSize, bytes);

        ByteArrayInputStream bais = new ByteArrayInputStream(decompressed);
        Deserializer packetDeserializer = new Deserializer(bais);

        IPacket packet = protocol.instancePacket(packetId);
        packet.read(packetDeserializer);
        bais.close();

        return packet;
    }

}
