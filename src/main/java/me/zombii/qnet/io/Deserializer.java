package me.zombii.qnet.io;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

public class Deserializer {

    final InputStream in;
    ByteOrder byteOrder;

    public Deserializer(InputStream in) {
        this(in, ByteOrder.BIG_ENDIAN);
    }

    public Deserializer(
            InputStream inputStream,
            ByteOrder byteOrder
    ) {
        this.in = inputStream;
        this.byteOrder = byteOrder;
    }

    public void setByteOrder(ByteOrder byteOrder) {
        this.byteOrder = byteOrder;
    }

    public ByteOrder getByteOrder() {
        return byteOrder;
    }

    public byte readByte() throws IOException {
        return (byte) this.in.read();
    }

    public boolean readBoolean() throws IOException {
        return (this.readByte() & 0x01) != 0;
    }

    public short readShort() throws IOException {
        int b1 = this.in.read();
        int b2 = this.in.read();

        if (byteOrder == ByteOrder.LITTLE_ENDIAN) {
            return (short) (b2 << 8 | b1);
        }

        return (short) (b1 << 8 | b2);
    }

    public char readChar() throws IOException {
        return (char) readShort();
    }

    public int readInt() throws IOException {
        int b1 = this.in.read();
        int b2 = this.in.read();
        int b3 = this.in.read();
        int b4 = this.in.read();

        if (byteOrder == ByteOrder.LITTLE_ENDIAN) {
            return b4 << 24 | b3 << 16 | b2 << 8 | b1;
        }

        return b1 << 24 | b2 << 16 | b3 << 8 | b4;
    }

    public long readLong() throws IOException {
        long b1 = (((long)this.in.read()) & 0xFF);
        long b2 = (((long)this.in.read()) & 0xFF);
        long b3 = (((long)this.in.read()) & 0xFF);
        long b4 = (((long)this.in.read()) & 0xFF);
        long b5 = (((long)this.in.read()) & 0xFF);
        long b6 = (((long)this.in.read()) & 0xFF);
        long b7 = (((long)this.in.read()) & 0xFF);
        long b8 = (((long)this.in.read()) & 0xFF);

        if (byteOrder == ByteOrder.LITTLE_ENDIAN) {
            return b8 << 56 | b7 << 48 | b6 << 40 | b5 << 32 | b4 << 24 | b3 << 16 | b2 << 8 | b1;
        }
        return b1 << 56 | b2 << 48 | b3 << 40 | b4 << 32 | b5 << 24 | b6 << 16 | b7 << 8 | b8;
    }

    public float readFloat() throws IOException {
        return Float.intBitsToFloat(readInt());
    }

    public double readDouble() throws IOException {
        return Double.longBitsToDouble(readLong());
    }

    public String readString() throws IOException {
        byte[] bytes = readByteArray();
        return new String(bytes, StandardCharsets.UTF_8);
    }

    public String[] readStringArray() throws IOException {
        int size = readInt();
        String[] result = new String[size];
        for (int i = 0; i < size; i++) {
            result[i] = readString();
        }
        return result;
    }

    public byte[] readByteArray() throws IOException {
        int size = readInt();
        byte[] b = new byte[size];
        for (int i = 0; i < b.length; i++) {
            b[i] = this.readByte();
        }
        return b;
    }

    public boolean[] readBooleanArray() throws IOException {
        int size = readInt();
        boolean[] b = new boolean[size];
        for (int i = 0; i < size; i++) {
            b[i] = readBoolean();
        }
        return b;
    }

    public short[] readShortArray() throws IOException {
        int size = readInt();
        short[] b = new short[size];
        for (int i = 0; i < size; i++) {
            b[i] = this.readShort();
        }
        return b;
    }

    public char[] readCharArray() throws IOException {
        int size = readInt();
        char[] b = new char[size];
        for (int i = 0; i < size; i++) {
            b[i] = this.readChar();
        }
        return b;
    }

    public int[] readIntArray() throws IOException {
        int size = readInt();
        int[] b = new int[size];
        for (int i = 0; i < size; i++) {
            b[i] = this.readInt();
        }
        return b;
    }

    public long[] readLongArray() throws IOException {
        int size = readInt();
        long[] b = new long[size];
        for (int i = 0; i < size; i++) {
            b[i] = this.readLong();
        }
        return b;
    }

    public float[] readFloatArray() throws IOException {
        int size = readInt();
        float[] b = new float[size];
        for (int i = 0; i < size; i++) {
            b[i] = this.readFloat();
        }
        return b;
    }

    public double[] readDoubleArray() throws IOException {
        int size = readInt();
        double[] b = new double[size];
        for (int i = 0; i < size; i++) {
            b[i] = this.readDouble();
        }
        return b;
    }

    public InputStream getInputStream() {
        return in;
    }

    public void close() throws IOException {
        in.close();
    }
    
}
