package lab03.assignments;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.function.IntBinaryOperator;

public class VectorStream {
    private static final int STREAM_LENGTH = 10;
    private static final int VECTOR_LENGTH = 100;
    private static volatile int sum = 0;
    private static CountDownLatch latchMain = new CountDownLatch(VECTOR_LENGTH);
    private static CountDownLatch latchSub[] = new CountDownLatch[2];

    static {
        latchSub[0] = new CountDownLatch(1);
        latchSub[1] = new CountDownLatch(1);
    }
    /**
     * Function that defines how vectors are computed: the i-th element depends on
     * the previous sum and the index i.
     * The sum of elements in the previous vector is initially given as zero.
     */
    private final static IntBinaryOperator vectorDefinition = (previousSum, i) -> {
        int a = 2 * i + 1;
        return (previousSum / VECTOR_LENGTH + 1) * (a % 4 - 2) * a + 1;
    };

    private static void computeVectorStreamSequentially() {
        int[] vector = new int[VECTOR_LENGTH];
        int sum = 0;
        for (int vectorNo = 0; vectorNo < STREAM_LENGTH; ++vectorNo) {
            for (int i = 0; i < VECTOR_LENGTH; ++i) {
                vector[i] = vectorDefinition.applyAsInt(sum, i);
            }
            sum = 0;
            for (int x : vector) {
                sum += x;
            }
            System.out.println(vectorNo + " -> " + sum);
        }
    }

    private static class Helper implements Runnable {
        private int index;
        private int[] vector;
        
        public Helper(int index, int[] vector) {
            this.index = index;
            this.vector = vector;
        }
        
        @Override
        public void run() {
            for (int i = 0; i < STREAM_LENGTH; i++) {
                vector[index] = vectorDefinition.applyAsInt(sum, index);
                latchMain.countDown();
                try {
                    latchSub[i % 2].await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    private static void computeVectorStreamInParallel() throws InterruptedException {
        int[] vector = new int[VECTOR_LENGTH];
        ArrayList<Thread> threads = new ArrayList<>();
        for (int i = 0; i < VECTOR_LENGTH; i++) {
            Thread t = new Thread(new Helper(i, vector));
            threads.add(t);
        }
        for (Thread t : threads) {
            t.start();
        }

        for (int i = 0; i < STREAM_LENGTH; i++) {
            try {
                latchMain.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw e;
            }
            latchMain = new CountDownLatch(VECTOR_LENGTH);
            latchSub[(i + 1) % 2] = new CountDownLatch(1);
            sum = 0;
            for (int x : vector) {
                sum += x;
            }
            System.out.println(i + " -> " + sum);
            latchSub[i % 2].countDown();
        }

        for (Thread t : threads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw e;
            }
        }
    }

    public static void main(String[] args) {
        try {
            System.out.println("-- Sequentially --");
            computeVectorStreamSequentially();
            System.out.println("-- Parallel --");
            computeVectorStreamInParallel();
            System.out.println("-- End --");
        } catch (InterruptedException e) {
            System.err.println("Main interrupted.");
        }
    }
}
