package model;

import macros.Macros;

/**
 * Created by yibai on 2016/4/3.
 */
public class SimpleUpdate extends SavableImp {
    public int to;

    @Override
    public void save(byte[] buffer, int offset) {
        Macros.encodeInt(to, buffer, offset);
    }

    @Override
    public void load(byte[] buffer, int offset) {
        to = Macros.decodeInt(buffer, offset);
    }

    @Override
    public int size() {
        return 4;
    }
}
