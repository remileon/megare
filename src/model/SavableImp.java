package model;

/**
 * Created by yibai on 2016/4/2.
 */
public abstract class SavableImp implements Savable {

    @Override
    public abstract void save(byte[] buffer, int offset);

    @Override
    public abstract void load(byte[] buffer, int offset);

    public abstract int size();

    public int align(int size) {
        return size / size() * size();
    }
}
