package lab06.assignments;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class IntBlockingQueue {
    private final int capacity;
    private final int[] buffer;
    private int size;
    private int firstOccupied;
    private int firstEmpty;
    private boolean isShutdown;
    private ReentrantLock lock;
    private Condition notEmpty;
    private Condition notFull;

    public IntBlockingQueue(int capacity) {
        this.capacity = capacity;
        this.buffer = new int[capacity];
        this.size = 0;
        this.firstOccupied = 0;
        this.firstEmpty = 0;
        this.isShutdown = false;
        this.lock = new ReentrantLock();
        this.notEmpty = lock.newCondition();
        this.notFull = lock.newCondition();
    }

    public int take() throws InterruptedException, ShutdownException {
        lock.lock();
        try {
            if (isShutdown && size == 0)
                throw new ShutdownException();
            while (size == 0) {
                notEmpty.await();
                if (isShutdown)
                    throw new ShutdownException();
            } 
            int first = buffer[firstOccupied];
            firstOccupied = (firstOccupied + 1) % capacity;
            size--;
            notFull.signalAll();
            return first;
        }
        finally {
            lock.unlock();
        }
    }

    public void put(int item) throws InterruptedException, ShutdownException {
        lock.lock();
        try {
            if (isShutdown)
                throw new ShutdownException();
            while (size == capacity) {
                notFull.await();
                if (isShutdown)
                    throw new ShutdownException();
            } 
            buffer[firstEmpty] = item;
            firstEmpty = (firstEmpty + 1) % capacity;
            size++;
            notEmpty.signalAll();
        }
        finally {
            lock.unlock();
        }
    }

    public void shutdown() {
        lock.lock();
        try {
            isShutdown = true;
            notEmpty.signalAll();
            notFull.signalAll();
        }
        finally {
            lock.unlock();
        }
    }

    public int getSize() {
        return size;
    }

    public int getCapacity() {
        return capacity;
    }

    public static class ShutdownException extends Exception {
        public ShutdownException() {
            super("Queue has been shut down.");
        }
    }
}
