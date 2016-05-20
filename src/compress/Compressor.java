package compress;

/**
 * Created by yibai on 2016/5/20.
 */
public interface Compressor {
    public byte[] compress(byte[] data, int len);
    public byte[] decompress(byte[] data, int len);
}
