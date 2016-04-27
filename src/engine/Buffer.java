package engine;

import macros.Macros;

/**
 * Created by yibai on 2016/3/28.
 */
public class Buffer {
    public byte[] buffer;
    public int top;
    public int size;
    public boolean end;

    final Object readLock = new Object();
    final Object writeLock = new Object();

    public Buffer() {
        buffer = new byte[Macros.buffer_size];
        top = 0;
        size = 0;
        end = false;
    }

    public final int read() throws Exception {
//        System.out.println("reading");
        if (end && size == 0)
            return -1;
        if (size == 0) {
            synchronized (readLock) {
                readLock.wait();
            }
        }
        if (end && size == 0)
            return -1;
        return top;
    }

    public final void free() {
        top = (top + Macros.trunk_size) % buffer.length;
        size = size - Macros.trunk_size;
        synchronized (writeLock) {
            writeLock.notify();
        }
    }

    public final void write(byte[] readerBuffer) throws Exception {
//        System.out.println("writing");
        if (size == buffer.length) {
            synchronized (writeLock) {
                writeLock.wait();
            }
        }
        int pos = (top + size) % buffer.length;
//        System.out.println(pos);
        System.arraycopy(readerBuffer, 0, buffer, pos, Macros.trunk_size);
        size = size + Macros.trunk_size;
        synchronized (readLock) {
            readLock.notify();
        }
    }

    public final void endWrite() {
        end = true;
        synchronized (readLock) {
            readLock.notify();
        }
    }

    public final void restart() {
        top = 0;
        size = 0;
        end = false;
    }
}
