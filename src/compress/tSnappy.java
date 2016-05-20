package compress;

import org.xerial.snappy.Snappy;

/**
 * Created by yibai on 2016/5/20.
 */
public class tSnappy implements Compressor {
    @Override
    public int compress(byte[] data, int len, byte[] out) {
        try {
            return Snappy.compress(data, 0, len, out, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    @Override
    public int decompress(byte[] data, int len, byte[] out) {
        try {
            return Snappy.uncompress(data, 0, len, out, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }
}
