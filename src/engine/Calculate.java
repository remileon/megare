package engine;

import api.Algorithm;
import macros.Macros;
import model.*;

import java.lang.reflect.Array;
import java.net.Socket;
import java.util.concurrent.CyclicBarrier;

/**
 * Created by yibai on 2016/3/22.
 */
public class Calculate<Node extends SavableImp, Accum extends SavableImp, Update extends SimpleUpdate, Edge extends SimpleEdge> implements Runnable {
    public int p_num;
    public CyclicBarrier barrier;
    Algorithm<Node, Accum, Update> algorithm;
    byte[] nodes;
    byte[] accums;
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
        nodes = new byte[Macros.p_size * nodeInstance.size()];
        accums = new byte[Macros.p_size * nodeInstance.size()];
        for (int i = 0; i < Macros.p_size; ++i) {
            nodeInstance.save(nodes, i * nodeInstance.size());
            accumInstance.save(accums, i * accumInstance.size());
        }
        flags = new boolean[Macros.total_machine_number];
        System.out.println("calculate" + p_num + ": construct completed");
    }

    public void run() {
        System.out.println("running calculate " + p_num);
        Thread edgeReader = new Thread(new Reader(Macros.OP_GET_EDGE));
        Thread updateReader = new Thread(new Reader(Macros.OP_GET_UPDATE));

        for (int k = 0; k < 10; ++k) {
            try {
                int pos;
                Edge edge = (Edge) edgeInstance.getClass().newInstance();
                Update update = (Update) updateInstance.getClass().newInstance();

                if (Macros.start_gather) {
                    System.out.println("calculate" + p_num + ": scatter start");
                    edgeReader.start();
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
//                        nodeInstance.load(nodes, (edge.from - p_num * Macros.p_size) * nodeInstance.size());
                            nodeInstance.load(nodes, (edge.from % Macros.p_size) * nodeInstance.size());
                            algorithm.scatter(update, nodeInstance);
//                        nodeInstance.save(nodes, (edge.from - p_num * Macros.p_size) * nodeInstance.size());
                            nodeInstance.save(nodes, (edge.from % Macros.p_size) * nodeInstance.size());
                            wBuffer.write(update);
                        }
                        buffer.free();
                    }
                    // TODO STEAL
                    wBuffer.flush();
                    System.out.println("calculate" + p_num + ": scatter complete:" + cnt + "edges");
                    barrier.await();
                }

                System.out.println("calculate" + p_num + ": gather start");
                buffer.restart();
                updateReader.start();
                while ((pos = buffer.read()) > -1) {
                    edge.load(buffer.buffer, pos);
                    int size = Macros.decodeInt(buffer.buffer, pos);
                    for (int i = pos + 4; i < pos + 4 + size; i = i + update.size()) {
                        update.load(buffer.buffer, i);
//                        accumInstance.load(accums, (update.to - p_num * Macros.p_size) * accumInstance.size());
                        accumInstance.load(accums, (update.to % Macros.p_size) * accumInstance.size());
                        algorithm.gather(accumInstance, update);
//                        accumInstance.save(accums, (update.to - p_num * Macros.p_size) * accumInstance.size());
                        accumInstance.save(accums, (update.to % Macros.p_size) * accumInstance.size());
                    }
                    buffer.free();
                }
                for (int i = 0; i < Macros.p_size; ++i) {
                    accumInstance.load(accums, i * accumInstance.size());
                    nodeInstance.load(nodes, i * nodeInstance.size());
                    algorithm.apply(nodeInstance, accumInstance);
                    nodeInstance.save(nodes, i * nodeInstance.size());
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
        boolean[] accessible;
        int op;
        int gap;
        Thread[] innerReaders;

        Reader(int op) {
            this.op = op;
            accessible = new boolean[Macros.total_machine_number];
            for (int i = 0; i < Macros.total_machine_number; ++i) {
                accessible[i] = true;
            }
            int nThreads = Math.min(Macros.total_machine_number, Macros.max_request_num);
            innerReaders = new Thread[nThreads];
            for (int i = 0; i < innerReaders.length; ++i) {
                innerReaders[i] = new Thread(new InnerReader());
            }
        }

        public void run() {
            gap = Macros.total_machine_number - innerReaders.length;
            for (Thread innerReader : innerReaders) {
                innerReader.start();
            }
            try {
                for (Thread innerReader : innerReaders) {
                    innerReader.join();
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
                synchronized (accessible) {
                    do {
                        target = (int) (Math.random() * Macros.total_machine_number);
                    } while (!accessible[target]);
                    accessible[target] = false;
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
                                accessible[target] = true;
                                synchronized (accessible) {
                                    do {
                                        target = (int) (Math.random() * Macros.total_machine_number);
                                    } while (!accessible[target]);
                                    accessible[target] = false;
                                }
                            }
                        } else {
                            if (gap > 0) {
                                --gap;
                                synchronized (accessible) {
                                    do {
                                        target = (int) (Math.random() * Macros.total_machine_number);
                                    } while (!accessible[target]);
                                    accessible[target] = false;
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
