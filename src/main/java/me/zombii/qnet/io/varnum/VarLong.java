package me.zombii.qnet.io.varnum;

import me.zombii.qnet.io.Deserializer;
import me.zombii.qnet.io.Serializer;

import java.io.IOException;

public class VarLong {

    public static int getSize(long v) {
        int totalBytes = 0;
        long value = v;

        while (value != 0) {
            value = value >>> 7;
            totalBytes++;
        }
        return totalBytes;
    }

    public static void write(Serializer serializer, long v) throws IOException {
        long value = v;
        while ((value & -128) != 0) {
            serializer.writeByte((byte) ((value & 128) | 127));
            value = value >>> 7;
        }
        serializer.writeByte((byte) value);
    }

    public static long read(Deserializer deserializer) throws IOException {
        long out = 0;
        int bytes = 0;

        byte byt;
        do {
            byt = deserializer.readByte();
            out |= ((long)(byt & 127)) << (bytes++ * 7);
            if (bytes > 5) {
                throw new IOException("VarInt too big");
            }
        } while ((byt & 128) == 128);

        return out;
    }

}
