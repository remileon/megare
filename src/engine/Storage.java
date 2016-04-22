package engine;

import macros.Macros;
import model.SavableImp;
import model.SimpleEdge;

import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by yibai on 2016/3/22.
 */
public class Storage<Node extends SavableImp, Accum, Update extends SavableImp, Edge extends SimpleEdge> implements Runnable {
    RandomAccessFile[] eFile;
    RandomAccessFile[] uFile;

    Edge edgeInstance;
    Update updateInstance;

    ServerSocket ss;

    public Storage(Edge edgeInstance, Update updateInstance) throws Exception {
        this.edgeInstance = edgeInstance;
        this.updateInstance = updateInstance;

        eFile = new RandomAccessFile[Macros.k];
        uFile = new RandomAccessFile[Macros.k * Macros.total_machine_number];

        for (int i = 0; i < Macros.k; ++i) {
            eFile[i] = new RandomAccessFile(Macros.eFilename(Macros.k * Macros.machine_number + i), "r");
        }

        for (int i = 0; i < Macros.k * Macros.total_machine_number; ++i) {
            new File(Macros.uFilename(i)).createNewFile();
            uFile[i] = new RandomAccessFile(Macros.uFilename(i), "r");
        }

        ss = new ServerSocket(5765);
    }

    public void run() {
        System.out.println("running storage");
        try {
            while (true) {
                Socket socket = ss.accept();
                new Thread(new Dealer(socket)).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class Dealer implements Runnable {
        Socket socket;
        byte[] buffer;

        public Dealer(Socket socket) {
            this.socket = socket;
            buffer = new byte[Macros.trunk_size];
        }

        public void run() {
            try {
                InputStream is = socket.getInputStream();
                int op = is.read();
                int p_num, size;
                switch (op) {
                    case Macros.OP_FIGHT:
                        socket.getOutputStream().write("765pro fight~ oh!".getBytes());
                        break;
                    case Macros.OP_GET_EDGE:
                        p_num = is.read();
                        synchronized (eFile[p_num]) {
                            size = eFile[p_num].read(buffer, 4, edgeInstance.align(buffer.length - 4));
                        }
                        Macros.encodeInt(size, buffer, 0);
                        socket.getOutputStream().write(buffer);
                        break;
                    case Macros.OP_SAVE_UPDATE:
                        break;
                    case Macros.OP_GET_UPDATE:
                        p_num = is.read();
                        synchronized (uFile[p_num]) {
                            size = uFile[p_num].read(buffer, 4, updateInstance.align(buffer.length - 4));
                        }
                        Macros.encodeInt(size, buffer, 0);
                        socket.getOutputStream().write(buffer);
                        break;
                    case Macros.OP_BARRIER:
                        for (int i = 0; i < Macros.k; ++i) {
                            eFile[i].seek(0);
                        }
                        for (int i = 0; i < Macros.k * Macros.total_machine_number; ++i) {
                            uFile[i].seek(0);
                        }
                }
                socket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
