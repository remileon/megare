package engine;

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
        int p_num = u.to / Macros.p_size;
        synchronized (buffer[p_num]) {
            if (size[p_num] + u.size() > buffer[p_num].length) {
                fileOutputStream[p_num].write(buffer[p_num], 0, size[p_num]);
                size[p_num] = 0;
            }
            u.save(buffer[p_num], size[p_num]);
            size[p_num] += u.size();
        }
    }

    public void flush() throws Exception {
        for (int i = 0; i < buffer.length; ++i) {
            fileOutputStream[i].write(buffer[i], 0, size[i]);
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
