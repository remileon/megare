import algorithm.PageRank;
import algorithm.PageRankH;
import api.Algorithm;
import compress.ZLib;
import compress.Zip;
import compress.tSnappy;
import engine.Barrier;
import engine.Calculate;
import engine.Storage;
import macros.Macros;
import model.AccumDouble;
import model.NodeWithDegreeDouble;
import model.SimpleEdge;
import model.UpdateDouble;

import javax.crypto.Mac;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;
import java.util.concurrent.CyclicBarrier;

/**
 * Created by yibai on 2016/3/15.
 */
public class Chaos {
    public static void main(String args[]) throws Exception {
        Macros.machine_number = Integer.parseInt(args[0]);
        Algorithm algorithm = new PageRank();
        if (args.length > 1) {
            for (int i = 1; i < args.length; ++i) {
                if (args[i].equals("g"))
                    generateTestEdge();
                if (args[i].equals("gather"))
                    Macros.start_gather = true;
                if (args[i].equals("H"))
                    algorithm = new PageRankH();
                if (args[i].equals("zlib")) {
                    Macros.compressor = new ZLib();
                }
                if (args[i].equals("zip")) {
                    Macros.compressor = new Zip();
                }
                if (args[i].equals("snappy")) {
                    Macros.compressor = new tSnappy();
                }
            }
        }
        if (Macros.machine_number <= 0) {
            new Thread(new Barrier()).start();
        }
        if (Macros.machine_number == -1) {
            Macros.total_machine_number = 1;
            Macros.machine_number = 0;
            Macros.machine_ips[0] = "127.0.0.1";
        }
        new Thread(new Storage<NodeWithDegreeDouble, AccumDouble, UpdateDouble, SimpleEdge>(new SimpleEdge(), new UpdateDouble())).start();
        NetBarrier netBarrier = new NetBarrier();
        netBarrier.run();
        CyclicBarrier barrier = new CyclicBarrier(Macros.k, netBarrier);
        Thread[] calculators = new Thread[Macros.k];
        for (int i = 0; i < Macros.k; ++i) {
            calculators[i] = new Thread(new Calculate<>(Macros.machine_number * Macros.k + i, barrier, algorithm, new SimpleEdge(), new UpdateDouble(), new NodeWithDegreeDouble(), new AccumDouble()));
        }
        for (int i = 0; i < Macros.k; ++i) {
            calculators[i].start();
        }
//        for (int i = 0; i < 4; ++i) {
//            new Thread(() -> {
//                long start = System.currentTimeMillis();
//                int b = 0;
//                int d = 1;
//                int c = Integer.parseInt(args[0]);
//                Random r = new Random();
//                int[] place = new int[100000000];
//                for (int i1 = 0; i1 < 1000000000; ++i1) {
//                    int g = b;
//                    b = b + d;
//                    d = g;
//                    place[i1 % 1000000] = b;
//                }
//                long stop = System.currentTimeMillis();
//                System.out.println(b);
//                System.out.println(place[c]);
//                System.out.println(stop - start);
//            }).start();
//        }
    }

    private static void generateTestEdge() throws Exception {
        byte[] temp = new byte[8];
        SimpleEdge simpleEdge = new SimpleEdge();
        Random random = new Random();
        for (int i = 0; i < Macros.k * Macros.total_machine_number; ++i) {
            File file = new File(Macros.eFilename(i));
            file.createNewFile();
            FileOutputStream os = new FileOutputStream(file);
            BufferedOutputStream bos = new BufferedOutputStream(os);
            for (int j = 0; j < Macros.p_size << 4; ++j) {
                simpleEdge.from = random.nextInt(Macros.p_size) + i * Macros.p_size;
                simpleEdge.to = random.nextInt(Macros.p_size * Macros.k * Macros.total_machine_number);
                simpleEdge.save(temp, 0);
                bos.write(temp);
            }
            bos.flush();
        }
    }

    public static class NetBarrier implements Runnable {
        ServerSocket ss;

        public NetBarrier() {
            try {
                ss = new ServerSocket(Macros.barrier_port2);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void run() {
            try {
                new Socket(Macros.machine_ips[0], Macros.barrier_port);
                System.out.println("waiting for barrier");
                Socket socket = ss.accept();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
