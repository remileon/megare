package compress;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Created by yibai on 2016/5/20.
 */
public class Zip implements Compressor {
    @Override
    public int compress(byte[] data, int len, byte[] out) {
        int size = -1;
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ZipOutputStream zip = new ZipOutputStream(bos);
            ZipEntry entry = new ZipEntry("zip");
            entry.setSize(len);
            zip.putNextEntry(entry);
            zip.write(data);
            zip.closeEntry();
            zip.close();
            size = bos.size();
            bos.write(out, 0, bos.size());
            bos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return size;
    }

    @Override
    public int decompress(byte[] data, int len, byte[] out) {
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(data);
            ZipInputStream zip = new ZipInputStream(bis);
            return zip.read(out);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }
}
