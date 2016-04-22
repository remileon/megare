package model;

import macros.Macros;

/**
 * Created by yibai on 2016/3/16.
 */
public class UpdateDouble extends SimpleUpdate {
    public UpdateDouble() {
        this(0.0);
    }

    ;

    public UpdateDouble(double value) {
        this.value = value;
    }

    public double value;

    @Override
    public int size() {
        return 12;
    }

    @Override
    public void save(byte[] buffer, int offset) {
        Macros.encodeInt(to, buffer, offset);
        Macros.encodeDouble(value, buffer, offset + 4);
    }

    @Override
    public void load(byte[] buffer, int offset) {
        to = Macros.decodeInt(buffer, offset);
        value = Macros.decodeDouble(buffer, offset + 4);
    }
}
