package model;

/**
 * Created by yibai on 2016/3/28.
 */
public interface Savable {
    public void save(byte[] buffer, int offset);
    public void load(byte[] buffer, int offset);
    public int size();
    public int align(int size);
}
