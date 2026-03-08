package me.zombii.qnet.api.packet;

import me.zombii.qnet.io.Deserializer;
import me.zombii.qnet.io.Serializer;

import java.io.IOException;

/**
 * The basic packet that defines read &amp; write functions.
 *
 * @since 1.0.0
 * @author Mr-Zombii
 */
public interface IPacket {

    default void write(Serializer serializer) throws IOException {}
    default void read(Deserializer deserializer) throws IOException {}

}
