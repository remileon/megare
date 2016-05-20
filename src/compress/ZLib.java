package compress;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

/**
 * Created by yibai on 2016/5/19.
 */
public class ZLib implements Compressor {
    public int compress(byte[] data, int len, byte[] out) {
        Deflater compresser = new Deflater();
        compresser.reset();
        compresser.setInput(data, 0, len);
        compresser.finish();
        int size = -1;
        try {
            size = compresser.deflate(out);
            System.out.println("i" + size + len);
        } catch (Exception e) {
            e.printStackTrace();
        }
        compresser.end();
        return size;
    }

    public void compress(byte[] data, OutputStream os) {
        DeflaterOutputStream dos = new DeflaterOutputStream(os);
        try {
            dos.write(data, 0, data.length);
            dos.finish();
            dos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int decompress(byte[] data, int len, byte[] out) {
        Inflater decompresser = new Inflater();
        decompresser.reset();
        decompresser.setInput(data, 0, len);
        int size = 0;
        try {
            while (!decompresser.finished()) {
                size = decompresser.inflate(out);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        decompresser.end();
        return size;
    }

    public byte[] decompress(InputStream is) {
        InflaterInputStream iis = new InflaterInputStream(is);
        ByteArrayOutputStream o = new ByteArrayOutputStream(1024);
        try {
            int i = 1024;
            byte[] buf = new byte[i];
            while ((i = iis.read(buf, 0, i)) > 0) {
                o.write(buf, 0, i);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return o.toByteArray();
    }
}