package macros;

/**
 * Created by yibai on 2016/3/22.
 */
public class Macros {
    public static int k = 8;
    public static int machine_number = 0;
    public static int total_machine_number = 1;
    public static int p_size = 1 << 22;

    public static int barrier_port = 8862;
    public static int barrier_port2 = 7862;
    public static String[] machine_ips;

    public static int buffer_size = 1 << 24;
    public static int trunk_size = 1 << 20;

    static {
        machine_ips = new String[total_machine_number];
        machine_ips[0] = "127.0.0.1";
    }

    public static final int OP_ERROR = -1;
    public static final int OP_FIGHT = 0;
    public static final int OP_GET_EDGE = 1;
    public static final int OP_SAVE_UPDATE = 2;
    public static final int OP_GET_UPDATE = 3;
    public static final int OP_BARRIER = 4;
    public static int max_request_num = 4;

    public static String eFilename(int p_num) {
        return "EDGE" + p_num;
    }

    public static String vFilename(int p_num) {
        return "VERTEX" + p_num;
    }

    public static String uFilename(int p_num) {
        return "UPDATE" + p_num;
    }

    public static void encodeInt(int x, byte[] b, int offset) {
        b[offset] = (byte) (x & 0xff);
        b[offset + 1] = (byte) ((x >> 8) & 0xff);
        b[offset + 2] = (byte) ((x >> 16) & 0xff);
        b[offset + 3] = (byte) ((x >> 24) & 0xff);
//        System.out.println("" + x + " " + b[offset] + " " + b[offset+1] + " " + b[offset+2] + " " + b[offset+3]);
    }

    public static int decodeInt(byte[] b, int offset) {
        return (int)(b[offset] & 0xff) | ((int)(b[offset + 1] & 0xff) << 8) | ((int)(b[offset + 2] & 0xff) << 16) | ((int)(b[offset + 3] & 0xff) << 24);
    }

    public static void encodeDouble(double x, byte[] b, int offset) {
        long lx = Double.doubleToLongBits(x);
        b[offset] = (byte) (lx & 0xff);
        b[offset + 1] = (byte) ((lx >> 8) & 0xff);
        b[offset + 2] = (byte) ((lx >> 16) & 0xff);
        b[offset + 3] = (byte) ((lx >> 24) & 0xff);
        b[offset + 4] = (byte) ((lx >> 32) & 0xff);
        b[offset + 5] = (byte) ((lx >> 40) & 0xff);
        b[offset + 6] = (byte) ((lx >> 48) & 0xff);
        b[offset + 7] = (byte) ((lx >> 56) & 0xff);
    }

    public static double decodeDouble(byte[] b, int offset) {
        return Double.longBitsToDouble((long)b[offset] | ((long)b[offset + 1] << 8) | ((long)b[offset + 2] << 16) | ((long)b[offset + 3] << 24) |
                ((long)b[offset + 4] << 32) | ((long)b[offset + 5] << 40) | ((long)b[offset + 6] << 48) | ((long)b[offset + 7] << 56));
    }
}
