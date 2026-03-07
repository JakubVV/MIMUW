package lab03.assignments;

import java.util.function.IntBinaryOperator;
import java.util.ArrayList;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public class VectorStream {
    private static final int STREAM_LENGTH = 10;
    private static final int VECTOR_LENGTH = 100;
    volatile private static int vectorNo = 0; 


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
    static class sharedData {
        volatile static int sum = 0;

    }
    private static class VectorStreamHelper implements Runnable {

        private int index;
        private static int[] vector = new int[VECTOR_LENGTH];
        private static final CyclicBarrier barrier = new CyclicBarrier(VECTOR_LENGTH,  () -> {
            sharedData.sum = 0;
            for (int i = 0; i < VECTOR_LENGTH; i++)
                sharedData.sum += vector[i];
            System.out.println(vectorNo + " -> " + sharedData.sum);
            vectorNo++;  
        }); 
            
        public VectorStreamHelper(int index) {
            this.index = index;
        }

        @Override
        public void run() {
            try {
                for (int vectorNo = 0; vectorNo < STREAM_LENGTH; vectorNo++) {
                    vector[index] = vectorDefinition.applyAsInt(sharedData.sum, index);
                
                barrier.await();
                }
            } catch (BrokenBarrierException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private static void computeVectorStreamInParallel() throws InterruptedException {
        ArrayList<Thread> threads = new ArrayList<>();
        
        for (int i = 0; i < VECTOR_LENGTH; i++) {
            threads.add(new Thread(new VectorStreamHelper(i)));
        }
        for (Thread thread : threads) {
            thread.start();
        }

        for (Thread thread : threads) {
            try {
                thread.join();
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
