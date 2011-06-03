package net.xy.codebase;

/**
 * high performance dynamic byte array for using in streamcopy.
 * Not synchronized.
 * 
 * @author xyan
 * 
 */
public class ByteArray {
    private byte[] data;
    private final int growth;
    private int lastIndex;

    /**
     * creates an empty array and growth by 10%
     */
    public ByteArray() {
        this(1, 10);
    }

    /**
     * creates an capacity array with growth of 10%
     * 
     * @param capacity
     */
    public ByteArray(final int capacity) {
        this(capacity, 10);
    }

    /**
     * creates an array with capacity and growth specified
     * 
     * @param capacity
     * @param growth
     */
    public ByteArray(final int capacity, final int growth) {
        data = new byte[capacity];
        this.growth = growth;
        lastIndex = 0;
    }

    /**
     * if able increases the capacity by capacity
     * 
     * @param capacity
     */
    public void upsize(final int capacity) {
        if (capacity > data.length) {
            final byte[] newOne = new byte[capacity];
            System.arraycopy(data, 0, newOne, 0, lastIndex);
            data = newOne;
        }
    }

    /**
     * increases by growth but at least by 1
     */
    public void upsize() {
        upsize(data.length + data.length / 100 * growth + 1); // grows by x
                                                              // percent
    }

    /**
     * caps any null values beyond the last used index
     */
    public void cap() {
        final byte[] newOne = new byte[lastIndex];
        System.arraycopy(data, 0, newOne, 0, lastIndex);
        data = newOne;
    }

    /**
     * add one byte
     * 
     * @param b
     */
    public void add(final byte b) {
        if (lastIndex + 1 > data.length) {
            upsize();
        }
        data[lastIndex] = b;
        lastIndex++;
    }

    /**
     * appends bytearray
     * 
     * @param b
     */
    public void add(final byte[] b) {
        while (lastIndex + b.length > data.length) {
            upsize();
        }
        System.arraycopy(b, 0, data, lastIndex, b.length);
        lastIndex += b.length;
    }

    /**
     * adds only an range
     * 
     * @param b
     * @param off
     * @param len
     */
    public void add(final byte[] b, final int off, final int len) {
        while (lastIndex + len > data.length) {
            upsize();
        }
        System.arraycopy(b, off, data, lastIndex, len);
        lastIndex += len;
    }

    /**
     * gets an reference to the array and calls cap
     * 
     * @return
     */
    public byte[] get() {
        cap();
        return data;
    }

    /**
     * returns the last used index or -1 in case of is empty
     * 
     * @return
     */
    public int getLastIndex() {
        return lastIndex - 1;
    }
}
