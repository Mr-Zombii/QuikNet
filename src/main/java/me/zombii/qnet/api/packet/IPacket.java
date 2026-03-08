package me.zombii.qnet.api.packet;

import me.zombii.qnet.io.Deserializer;
import me.zombii.qnet.io.Serializer;

import java.io.IOException;

public interface IPacket {

    default void write(Serializer serializer) throws IOException {

    }

    default void read(Deserializer deserializer) throws IOException {

    }

}
