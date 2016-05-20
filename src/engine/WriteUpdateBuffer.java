package engine;

import compress.ZLib;
import macros.Macros;
import model.SimpleUpdate;

import java.io.File;
import java.io.FileOutputStream;

/**
 * Created by yibai on 2016/3/29.
 */
public class WriteUpdateBuffer<Update extends SimpleUpdate> {
    public byte[][] buffer;
    public int[] size;
    public FileOutputStream[] fileOutputStream;
    int p_num;

    long total_length = 0;
    long total_length_compressed = 0;
    long total_num = 0;

    private byte[] temp = new byte[4];

    public WriteUpdateBuffer(int p_num) throws Exception {
        buffer = new byte[Macros.total_machine_number * Macros.k][Macros.buffer_size >> 2];
        size = new int[Macros.total_machine_number * Macros.k];
        fileOutputStream = new FileOutputStream[Macros.total_machine_number * Macros.k];
        this.p_num = p_num;
        for (int i = 0; i < Macros.total_machine_number * Macros.k; ++i) {
            File file = new File(Macros.uFilename(i, p_num));
            file.createNewFile();
            if (!Macros.start_gather) {
                fileOutputStream[i] = new FileOutputStream(file);
            }
        }
    }

    public void write(Update u) throws Exception {
        int p_num = u.to / Macros.p_size % buffer.length;
        synchronized (buffer[p_num]) {
            if (size[p_num] + u.size() > buffer[p_num].length) {
                if (Macros.compressor != null) {
                    byte[] compressed = Macros.compressor.compress(buffer[p_num], size[p_num]);
                    total_length += size[p_num];
                    total_length_compressed += compressed.length + 4;
                    total_num += 1;
                    if (total_num % 10 == 0) {
                        System.out.println("total_num:" + total_num + " total_length:" + total_length + " compressed:" + total_length_compressed);
                    }
                    Macros.encodeInt(compressed.length, temp, 0);
                    fileOutputStream[p_num].write(temp);
                    fileOutputStream[p_num].write(compressed);
                } else {
                    fileOutputStream[p_num].write(buffer[p_num], 0, size[p_num]);
                }
                size[p_num] = 0;
            }
            u.save(buffer[p_num], size[p_num]);
            size[p_num] += u.size();
        }
    }

    public void flush() throws Exception {
        for (int i = 0; i < buffer.length; ++i) {
            if (Macros.compressor != null) {
                byte[] compressed = Macros.compressor.compress(buffer[p_num], size[p_num]);
                total_length += size[p_num];
                total_length_compressed += compressed.length + 4;
                total_num += 1;
                if (total_num % 10 == 0) {
                    System.out.println("total_num:" + total_num + " total_length:" + total_length + " compressed:" + total_length_compressed);
                }
                Macros.encodeInt(compressed.length, temp, 0);
                fileOutputStream[p_num].write(temp);
                fileOutputStream[p_num].write(compressed);
            } else {
                fileOutputStream[i].write(buffer[i], 0, size[i]);
            }
            size[i] = 0;
        }
    }

    public void clearFile() throws Exception {
        for (int i = 0; i < Macros.total_machine_number * Macros.k; ++i) {
            File file = new File(Macros.uFilename(i, p_num));
            fileOutputStream[i] = new FileOutputStream(file);
        }
    }
}
