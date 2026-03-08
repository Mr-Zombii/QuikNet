package me.zombii.qnet.api.packet;

import me.zombii.qnet.impl.packet.DefaultPacketFormat;
import me.zombii.qnet.io.Deserializer;
import me.zombii.qnet.io.Serializer;

import java.io.IOException;
import java.util.zip.DataFormatException;

public interface IPacketFormat {

    static IPacketFormat makeDefault() {
        return new DefaultPacketFormat();
    }

    static IPacketFormat makeDefault(boolean compressionEnabled) {
        return new DefaultPacketFormat(compressionEnabled);
    }

    static IPacketFormat makeDefault(int compressionLevel) {
        return new DefaultPacketFormat(true, compressionLevel);
    }

    void write(IPacketProtocol protocol, Serializer serializer, IPacket packet) throws IOException;
    IPacket read(IPacketProtocol protocol, Deserializer deserializer) throws IOException, DataFormatException;

}
