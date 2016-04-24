package engine;

import api.Algorithm;
import macros.Macros;
import model.SavableImp;
import model.SimpleEdge;
import model.SimpleUpdate;

import java.lang.reflect.Array;
import java.net.Socket;
import java.util.concurrent.CyclicBarrier;

/**
 * Created by yibai on 2016/3/22.
 */
public class Calculate<Node extends SavableImp, Accum, Update extends SimpleUpdate, Edge extends SimpleEdge> implements Runnable {
    public int p_num;
    public CyclicBarrier barrier;
    Algorithm<Node, Accum, Update> algorithm;
    Node[] nodes;
    Accum[] accums;
    boolean[] flags;

    Buffer buffer;
    WriteUpdateBuffer<Update> wBuffer;

    Edge edgeInstance;
    Update updateInstance;
    Node nodeInstance;
    Accum accumInstance;

    public Calculate(int p_num, CyclicBarrier barrier, Algorithm<Node, Accum, Update> algorithm, Edge edgeInstance, Update updateInstance, Node nodeInstance, Accum accumInstance) throws Exception {
        System.out.println("calculate" + p_num + ": constructing");
        this.p_num = p_num;
        this.barrier = barrier;
        this.algorithm = algorithm;
        this.edgeInstance = edgeInstance;
        this.updateInstance = updateInstance;
        this.nodeInstance = nodeInstance;
        this.accumInstance = accumInstance;

        System.out.println("creating buffer");
        buffer = new Buffer();
        System.out.println("creating wBuffer");
        wBuffer = new WriteUpdateBuffer<>(p_num);
        System.out.println("crating Others");
        nodes = (Node[]) Array.newInstance(nodeInstance.getClass(), Macros.p_size);
        accums = (Accum[]) Array.newInstance(accumInstance.getClass(), Macros.p_size);
        for (int i = 0; i < Macros.p_size; ++i) {
//            nodes[i] = (Node) nodeInstance.getClass().newInstance();
//            accums[i] = (Accum) accumInstance.getClass().newInstance();
        }
        flags = new boolean[Macros.total_machine_number];
        System.out.println("calculate" + p_num + ": construct completed");
    }

    public static int Puzzle(int[] a) {
        int ret = 0;
        for (int i = 0; i < a.length; ++i) {
            ret += new Integer(a[i]).compareTo(0);
        }
        return ret;
    }

    public void run() {
        System.out.println("running calculate " + p_num);
        for (int k = 0; k < 1; ++k) {
            try {
                System.out.println("calculate" + p_num + ": scatter start");
                new Thread(new Reader(Macros.OP_GET_EDGE)).start();
                int pos;
                Edge edge = (Edge) edgeInstance.getClass().newInstance();
                Update update = (Update) updateInstance.getClass().newInstance();
                wBuffer.clearFile();
                buffer.restart();
                int cnt = 0;
                while ((pos = buffer.read()) > -1) {
                    edge.load(buffer.buffer, pos);
                    int size = Macros.decodeInt(buffer.buffer, pos);
                    for (int i = pos + 4; i < pos + 4 + size; i = i + edge.size()) {
                        ++cnt;
                        edge.load(buffer.buffer, i);
                        update.to = edge.to;
                        algorithm.scatter(update, nodes[edge.from - p_num * Macros.p_size]);
                        wBuffer.write(update);
                    }
                    buffer.free();
                }
                // TODO STEAL
                wBuffer.flush();
                System.out.println("calculate" + p_num + ": scatter complete:" + cnt + "edges");
                barrier.await();
                System.out.println("calculate" + p_num + ": gather start");
                buffer.restart();
                new Thread(new Reader(Macros.OP_GET_UPDATE)).start();
                while ((pos = buffer.read()) > -1) {
                    edge.load(buffer.buffer, pos);
                    int size = Macros.decodeInt(buffer.buffer, pos);
                    for (int i = pos + 4; i < pos + 4 + size; i = i + update.size()) {
                        update.load(buffer.buffer, i);
                        algorithm.gather(accums[update.to - p_num * Macros.p_size], update);
                    }
                    buffer.free();
                }
                for (int i = 0; i < Macros.k; ++i) {
                    algorithm.apply(nodes[i], accums[i]);
                }
                // TODO STEAL
                System.out.println("calculate" + p_num + ":gather complete");
                barrier.await();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private class Reader implements Runnable {
        boolean[] accessable;
        int op;
        int gap;

        Reader(int op) {
            this.op = op;
            accessable = new boolean[Macros.total_machine_number];
            for (int i = 0; i < Macros.total_machine_number; ++i) {
                accessable[i] = true;
            }
        }

        public void run() {
            int nThreads = Math.min(Macros.total_machine_number, Macros.max_request_num);
            Thread[] threads = new Thread[nThreads];
            gap = Macros.total_machine_number - nThreads;
            for (int i = 0; i < nThreads; ++i) {
                threads[i] = new Thread(new InnerReader());
                threads[i].start();
            }
            try {
                for (int i = 0; i < nThreads; ++i) {
                    threads[i].join();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            buffer.endWrite();
        }

        private class InnerReader implements Runnable {
            byte[] readerBuffer;

            public InnerReader() {
                readerBuffer = new byte[Macros.trunk_size];
            }

            @Override
            public void run() {
                int target;
                synchronized (accessable) {
                    do {
                        target = (int) (Math.random() * Macros.total_machine_number);
                    } while (!accessable[target]);
                    accessable[target] = false;
                }
                while (true) {
                    try {
                        Socket socket = new Socket(Macros.machine_ips[target], 5765);
                        int result;
                        int offset = 0;
                        socket.getOutputStream().write(op);
                        socket.getOutputStream().write(p_num);
                        do {
//                        System.out.println(offset);
                            result = socket.getInputStream().read(readerBuffer, offset, readerBuffer.length - offset);
                            offset += result;
                        } while (result > 0);
                        int size = Macros.decodeInt(readerBuffer, 0);
                        if (size > 0) {
                            synchronized (buffer) {
                                buffer.write(readerBuffer);
                            }
                            if (gap > 0) {
                                accessable[target] = true;
                                synchronized (accessable) {
                                    do {
                                        target = (int) (Math.random() * Macros.total_machine_number);
                                    } while (!accessable[target]);
                                    accessable[target] = false;
                                }
                            }
                        } else {
                            if (gap > 0) {
                                --gap;
                                synchronized (accessable) {
                                    do {
                                        target = (int) (Math.random() * Macros.total_machine_number);
                                    } while (!accessable[target]);
                                    accessable[target] = false;
                                }
                            } else {
                                break;
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
