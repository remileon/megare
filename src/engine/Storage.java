package engine;

import macros.Macros;
import model.SavableImp;
import model.SimpleEdge;

import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by yibai on 2016/3/22.
 */
public class Storage<Node extends SavableImp, Accum, Update extends SavableImp, Edge extends SimpleEdge> implements Runnable {
    RandomAccessFile[] eFile;
    RandomAccessFile[][] uFile;
    int uFileTop[];

    Edge edgeInstance;
    Update updateInstance;

    ServerSocket ss;

    public Storage(Edge edgeInstance, Update updateInstance) throws Exception {
        this.edgeInstance = edgeInstance;
        this.updateInstance = updateInstance;

        eFile = new RandomAccessFile[Macros.k * Macros.total_machine_number];
        uFile = new RandomAccessFile[Macros.k * Macros.total_machine_number][Macros.k];
        uFileTop = new int[Macros.k * Macros.total_machine_number];

        for (int i = 0; i < Macros.k * Macros.total_machine_number; ++i) {
            eFile[i] = new RandomAccessFile(Macros.eFilename(i), "r");
        }

        for (int i = 0; i < Macros.k * Macros.total_machine_number; ++i) {
            for (int from = 0; from < Macros.k; ++from) {
                new File(Macros.uFilename(i, from + Macros.machine_number * Macros.k)).createNewFile();
                uFile[i][from] = new RandomAccessFile(Macros.uFilename(i, from + Macros.machine_number * Macros.k), "r");
            }
        }

        ss = new ServerSocket(5765);
    }

    public void run() {
        System.out.println("running storage");
        try {
            ExecutorService fixedThreadPool = Executors.newFixedThreadPool(Macros.total_machine_number * Macros.k);
            while (true) {
                Socket socket = ss.accept();
                fixedThreadPool.execute(new Dealer(socket));
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
                            size = uFile[p_num][uFileTop[p_num]].read(buffer, 4, updateInstance.align(buffer.length - 4));
                            while (size == -1) {
                                ++uFileTop[p_num];
                                if (uFileTop[p_num] >= uFile[p_num].length) {
                                    break;
                                }
                                size = uFile[p_num][uFileTop[p_num]].read(buffer, 4, updateInstance.align(buffer.length - 4));
                            }
                        }
                        Macros.encodeInt(size, buffer, 0);
                        socket.getOutputStream().write(buffer);
                        break;
                    case Macros.OP_BARRIER:
                        for (int i = 0; i < Macros.k * Macros.total_machine_number; ++i) {
                            eFile[i].seek(0);
                        }
                        for (int i = 0; i < uFile.length; ++i) {
                            uFileTop[i] = 0;
                            for (int j = 0; j < uFile[i].length; ++j) {
                                uFile[i][j].seek(0);
                            }
                        }
                }
                socket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
