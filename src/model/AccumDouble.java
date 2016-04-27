package model;

import macros.Macros;

/**
 * Created by yibai on 2016/4/4.
 */
public class AccumDouble extends SavableImp {
    public double value;

    @Override
    public void save(byte[] buffer, int offset) {
        Macros.encodeDouble(value, buffer, offset);
    }

    @Override
    public void load(byte[] buffer, int offset) {
        value = Macros.decodeDouble(buffer, offset);
    }

    @Override
    public int size() {
        return 8;
    }
}
