package engine;

import macros.Macros;

import javax.crypto.Mac;
import java.io.RandomAccessFile;

/**
 * Created by yibai on 2016/5/20.
 */
public class UpdateReader {
    public RandomAccessFile uFiles[];
    public int top;


    public byte raw[];
    public byte buffer[];
    public int now;
    public int size;

    private byte[] temp = new byte[4];

    public UpdateReader(RandomAccessFile[] uFiles) {
        this.uFiles = uFiles;
        buffer = new byte[Macros.buffer_size];
        raw = new byte[Macros.buffer_size >> 1];
    }
    public int get(byte[] dst, int off, int len) throws Exception {
        while (size < len) {
            byte[] result;
            int result_size;
            if (Macros.compressor != null) {
                boolean has_value = uFiles[top].read(temp) > 0;
                while (!has_value && top < uFiles.length) {
                    ++top;
                    if (top < uFiles.length) {
                        has_value = uFiles[top].read(temp) > 0;
                    }
                }
                if (top >= uFiles.length) {
                    return -1;
                }
                int size = Macros.decodeInt(temp, 0);
                uFiles[top].read(raw, 0, size);
                result = Macros.compressor.decompress(raw, size);
                result_size = result.length;
            } else {
                int value_size = uFiles[top].read(raw);
                while (value_size <= 0 && top < uFiles.length) {
                    ++top;
                    if (top < uFiles.length) {
                        value_size = uFiles[top].read(raw);
                    }
                }
                if (top >= uFiles.length) {
                    return -1;
                }
                result = raw;
                result_size = value_size;
            }
            int first_length = Math.min(result_size, buffer.length - (now + size) % buffer.length);
            System.arraycopy(result, 0, buffer, (now + size) % buffer.length, first_length);
            System.arraycopy(result, first_length, buffer, 0, result_size - first_length);
            size += result_size;
        }
        System.arraycopy(buffer, now, dst, off, Math.min(len, buffer.length - now));
        System.arraycopy(buffer, 0, dst, off + Math.min(len, buffer.length - now), len - Math.min(len, buffer.length - now));
        now = (now + len) % buffer.length;
        size -= len;
        return len;
    }
}
