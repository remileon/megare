package engine;

import macros.Macros;

import javax.crypto.Mac;
import java.io.FileOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by yibai on 2016/3/27.
 */
public class Barrier implements Runnable {
    public void run() {
        try {
            ServerSocket ss = new ServerSocket(Macros.barrier_port);
            int t = Macros.total_machine_number;
            while (true) {
                Socket socket = ss.accept();
                --t;
                if (t == 0) {
                    new Thread(new Sender()).start();
                    t = Macros.total_machine_number;
                }
                //System.out.println(new String(input));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public class Sender implements Runnable {
        public void run() {
            try {
                for (int i = 0; i < Macros.total_machine_number; ++i) {
                    new Socket(Macros.machine_ips[i], 5765).getOutputStream().write(Macros.OP_BARRIER);
                    new Socket(Macros.machine_ips[i], Macros.barrier_port2).close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
