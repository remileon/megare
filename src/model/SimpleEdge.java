package model;

import macros.Macros;

/**
 * Created by yibai on 2016/3/28.
 */
public class SimpleEdge extends SavableImp {
    public int from;
    public int to;

    @Override
    public void save(byte[] buffer, int offset) {
        Macros.encodeInt(from, buffer, offset);
        Macros.encodeInt(to, buffer, offset + 4);
    }

    @Override
    public void load(byte[] buffer, int offset) {
        from = Macros.decodeInt(buffer, offset);
        to = Macros.decodeInt(buffer, offset + 4);
    }

    @Override
    public int size() {
        return 8;
    }

    @Override
    public String toString() {
        return "from:" + from + "to:" + to;
    }
}
