package model;

import macros.Macros;

/**
 * Created by yibai on 2016/3/16.
 */
public class NodeWithDegreeDouble extends SavableImp {
    public double value;
    public int degree;

    public NodeWithDegreeDouble() {
        value = 1.0;
        degree = 1;
    }

    @Override
    public int size() {
        return 12;
    }

    @Override
    public void save(byte[] buffer, int offset) {
        Macros.encodeDouble(value, buffer, offset);
        Macros.encodeInt(degree, buffer, offset + 8);
    }

    @Override
    public void load(byte[] buffer, int offset) {
        value = Macros.decodeDouble(buffer, offset);
        degree = Macros.decodeInt(buffer, offset + 8);
    }
}
