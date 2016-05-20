package compress;

/**
 * Created by yibai on 2016/5/20.
 */
public interface Compressor {
    public int compress(byte[] data, int len, byte[] out);
    public int decompress(byte[] data, int len, byte[] out);
}