package me.zombii.qnet.io;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

public class Serializer {

    final OutputStream outputStream;
    ByteOrder byteOrder;

    public Serializer(OutputStream outputStream) {
        this(outputStream, ByteOrder.BIG_ENDIAN);
    }

    public Serializer(
            OutputStream outputStream,
            ByteOrder byteOrder
    ) {
        this.outputStream = outputStream;
        this.byteOrder = byteOrder;
    }

    public void setByteOrder(ByteOrder byteOrder) {
        this.byteOrder = byteOrder;
    }

    public ByteOrder getByteOrder() {
        return byteOrder;
    }

    public void writeByte(byte v) throws IOException {
        this.outputStream.write(v);
    }

    public void writeBoolean(boolean v) throws IOException {
        this.outputStream.write(v ? 1 : 0);
    }

    public void writeShort(short v) throws IOException {
        byte high = (byte) ((v >> 8) & 0xFF);
        byte low = (byte) ((v) & 0xFF);

        if (byteOrder == ByteOrder.LITTLE_ENDIAN) {
            outputStream.write(low);
            outputStream.write(high);
            return;
        }

        outputStream.write(high);
        outputStream.write(low);
    }

    public void writeChar(char v) throws IOException {
        writeShort((short) v);
    }

    public void writeInt(int v) throws IOException {
        byte high = (byte) ((v >> 24) & 0xFF);
        byte low = (byte) ((v >> 16) & 0xFF);
        byte mid = (byte) ((v >> 8) & 0xFF);
        byte bottom = (byte) ((v) & 0xFF);

        if (byteOrder == ByteOrder.LITTLE_ENDIAN) {
            outputStream.write(bottom);
            outputStream.write(mid);
            outputStream.write(low);
            outputStream.write(high);
            return;
        }

        outputStream.write(high);
        outputStream.write(low);
        outputStream.write(mid);
        outputStream.write(bottom);
    }

    public void writeLong(long v) throws IOException {
        byte b1 = (byte) ((v >> 56) & 0xFF);
        byte b2 = (byte) ((v >> 48) & 0xFF);
        byte b3 = (byte) ((v >> 40) & 0xFF);
        byte b4 = (byte) ((v >> 32) & 0xFF);
        byte b5 = (byte) ((v >> 24) & 0xFF);
        byte b6 = (byte) ((v >> 16) & 0xFF);
        byte b7 = (byte) ((v >> 8) & 0xFF);
        byte b8 = (byte) ((v) & 0xFF);

        if (byteOrder == ByteOrder.LITTLE_ENDIAN) {
            outputStream.write(b8);
            outputStream.write(b7);
            outputStream.write(b6);
            outputStream.write(b5);
            outputStream.write(b4);
            outputStream.write(b3);
            outputStream.write(b2);
            outputStream.write(b1);
            return;
        }

        outputStream.write(b1);
        outputStream.write(b2);
        outputStream.write(b3);
        outputStream.write(b4);
        outputStream.write(b5);
        outputStream.write(b6);
        outputStream.write(b7);
        outputStream.write(b8);
    }

    public void writeFloat(float v) throws IOException {
        writeInt(Float.floatToIntBits(v));
    }

    public void writeDouble(double v) throws IOException {
        writeLong(Double.doubleToLongBits(v));
    }

    public void writeString(String v) throws IOException {
        writeString(v, Integer.MAX_VALUE);
    }

    public void writeString(String v, int maxLength) throws IOException {
        if (maxLength < v.length()) {
            throw new IllegalArgumentException("string length cannot exceed " + maxLength + ", got " + v.length());
        }

        writeByteArray(v.getBytes(StandardCharsets.UTF_8));
    }

    public void writeStringArray(String[] v) throws IOException {
        writeStringArray(v, Integer.MAX_VALUE);
    }

    public void writeStringArray(String[] v, int maxLength) throws IOException {
        if (maxLength < v.length) {
            throw new IllegalArgumentException("string array length cannot exceed " + maxLength + ", got " + v.length);
        }

        writeInt(v.length);
        for (String s : v) {
            writeString(s, Integer.MAX_VALUE);
        }
    }

    public void writeByteArray(byte[] v) throws IOException {
        writeByteArray(v, Integer.MAX_VALUE);
    }

    public void writeByteArray(byte[] v, int maxLength) throws IOException {
        if (maxLength < v.length) {
            throw new IllegalArgumentException("byte array length cannot exceed " + maxLength + ", got " + v.length);
        }

        writeInt(v.length);
        outputStream.write(v);
    }

    public void writeBooleanArray(boolean[] v) throws IOException {
        writeBooleanArray(v, Integer.MAX_VALUE);
    }

    public void writeBooleanArray(boolean[] v, int maxLength) throws IOException {
        if (maxLength < v.length) {
            throw new IllegalArgumentException("boolean array length cannot exceed " + maxLength + ", got " + v.length);
        }

        writeInt(v.length);
        for (boolean i : v) {
            writeBoolean(i);
        }
    }

    public void writeShortArray(short[] v) throws IOException {
        writeShortArray(v, Integer.MAX_VALUE);
    }

    public void writeShortArray(short[] v, int maxLength) throws IOException {
        if (maxLength < v.length) {
            throw new IllegalArgumentException("short array length cannot exceed " + maxLength + ", got " + v.length);
        }

        writeInt(v.length);
        for (short i : v) {
            writeShort(i);
        }
    }

    public void writeCharArray(char[] v) throws IOException {
        writeCharArray(v, Integer.MAX_VALUE);
    }

    public void writeCharArray(char[] v, int maxLength) throws IOException {
        if (maxLength < v.length) {
            throw new IllegalArgumentException("char array length cannot exceed " + maxLength + ", got " + v.length);
        }

        writeInt(v.length);
        for (char i : v) {
            writeChar(i);
        }
    }

    public void writeIntArray(int[] v) throws IOException {
        writeIntArray(v, Integer.MAX_VALUE);
    }

    public void writeIntArray(int[] v, int maxLength) throws IOException {
        if (maxLength < v.length) {
            throw new IllegalArgumentException("int array length cannot exceed " + maxLength + ", got " + v.length);
        }

        writeInt(v.length);
        for (int i : v) {
            writeInt(i);
        }
    }

    public void writeLongArray(long[] v) throws IOException {
        writeLongArray(v, Integer.MAX_VALUE);
    }

    public void writeLongArray(long[] v, int maxLength) throws IOException {
        if (maxLength < v.length) {
            throw new IllegalArgumentException("long array length cannot exceed " + maxLength + ", got " + v.length);
        }

        writeInt(v.length);
        for (long i : v) {
            writeLong(i);
        }
    }

    public void writeFloatArray(float[] v) throws IOException {
        writeFloatArray(v, Integer.MAX_VALUE);
    }

    public void writeFloatArray(float[] v, int maxLength) throws IOException {
        if (maxLength < v.length) {
            throw new IllegalArgumentException("float array length cannot exceed " + maxLength + ", got " + v.length);
        }

        writeInt(v.length);
        for (float i : v) {
            writeFloat(i);
        }
    }

    public void writeDoubleArray(double[] v) throws IOException {
        writeDoubleArray(v, Integer.MAX_VALUE);
    }

    public void writeDoubleArray(double[] v, int maxLength) throws IOException {
        if (maxLength < v.length) {
            throw new IllegalArgumentException("double array length cannot exceed " + maxLength + ", got " + v.length);
        }

        writeInt(v.length);
        for (double i : v) {
            writeDouble(i);
        }
    }

    public void writeByteArray(Byte[] v) throws IOException {
        writeByteArray(v, Integer.MAX_VALUE);
    }

    public void writeByteArray(Byte[] v, int maxLength) throws IOException {
        if (maxLength < v.length) {
            throw new IllegalArgumentException("byte array length cannot exceed " + maxLength + ", got " + v.length);
        }

        writeInt(v.length);
        for (Byte b : v) {
            writeByte(b);
        }
    }

    public void writeBooleanArray(Boolean[] v) throws IOException {
        writeBooleanArray(v, Integer.MAX_VALUE);
    }

    public void writeBooleanArray(Boolean[] v, int maxLength) throws IOException {
        if (maxLength < v.length) {
            throw new IllegalArgumentException("boolean array length cannot exceed " + maxLength + ", got " + v.length);
        }

        writeInt(v.length);
        for (boolean i : v) {
            writeBoolean(i);
        }
    }

    public void writeShortArray(Short[] v) throws IOException {
        writeShortArray(v, Integer.MAX_VALUE);
    }

    public void writeShortArray(Short[] v, int maxLength) throws IOException {
        if (maxLength < v.length) {
            throw new IllegalArgumentException("short array length cannot exceed " + maxLength + ", got " + v.length);
        }

        writeInt(v.length);
        for (short i : v) {
            writeShort(i);
        }
    }

    public void writeCharArray(Character[] v) throws IOException {
        writeCharArray(v, Integer.MAX_VALUE);
    }

    public void writeCharArray(Character[] v, int maxLength) throws IOException {
        if (maxLength < v.length) {
            throw new IllegalArgumentException("char array length cannot exceed " + maxLength + ", got " + v.length);
        }

        writeInt(v.length);
        for (char i : v) {
            writeChar(i);
        }
    }

    public void writeIntArray(Integer[] v) throws IOException {
        writeIntArray(v, Integer.MAX_VALUE);
    }

    public void writeIntArray(Integer[] v, int maxLength) throws IOException {
        if (maxLength < v.length) {
            throw new IllegalArgumentException("int array length cannot exceed " + maxLength + ", got " + v.length);
        }

        writeInt(v.length);
        for (int i : v) {
            writeInt(i);
        }
    }

    public void writeLongArray(Long[] v) throws IOException {
        writeLongArray(v, Integer.MAX_VALUE);
    }

    public void writeLongArray(Long[] v, int maxLength) throws IOException {
        if (maxLength < v.length) {
            throw new IllegalArgumentException("long array length cannot exceed " + maxLength + ", got " + v.length);
        }

        writeInt(v.length);
        for (long i : v) {
            writeLong(i);
        }
    }

    public void writeFloatArray(Float[] v) throws IOException {
        writeFloatArray(v, Integer.MAX_VALUE);
    }

    public void writeFloatArray(Float[] v, int maxLength) throws IOException {
        if (maxLength < v.length) {
            throw new IllegalArgumentException("float array length cannot exceed " + maxLength + ", got " + v.length);
        }

        writeInt(v.length);
        for (float i : v) {
            writeFloat(i);
        }
    }

    public void writeDoubleArray(Double[] v) throws IOException {
        writeDoubleArray(v, Integer.MAX_VALUE);
    }

    public void writeDoubleArray(Double[] v, int maxLength) throws IOException {
        if (maxLength < v.length) {
            throw new IllegalArgumentException("double array length cannot exceed " + maxLength + ", got " + v.length);
        }

        writeInt(v.length);
        for (double i : v) {
            writeDouble(i);
        }
    }

    public OutputStream getOutputStream() {
        return outputStream;
    }

    public void close() throws IOException {
        outputStream.close();
    }
}
