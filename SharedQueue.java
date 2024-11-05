/*
Shared Queue
1. Synchronized Queue: Manages concurrent access by using intrinsic locks and wait()/notifyAll() to ensure consumers don’t block each other.
2. Single Writer Thread: Adds items at a fixed rate (5 messages per second).
3. Multiple Consumer Threads: Each consumer waits for the next available item and handles it upon notification.

The implementation minimizes locked conditions by using a circular buffer within the queue and by keeping the locking granularity small (on insertion and retrieval).

a. Synchronization between Readers and Writer: wait() and notifyAll() handle synchronization, suspending threads when they cannot proceed and notifying all waiting threads when an action is completed. The enqueue and dequeue methods are synchronized to ensure that only one thread can modify the shared queue at any given time.

b. Multiple Consumers Waiting without Blocking Each Other: When a consumer calls dequeue, it only blocks if the queue is empty, otherwise, it proceeds immediately to process the item. If empty, the consumer waits until an item is available.  All consumers are notified whenever a new item is enqueued, allowing the next available consumer to proceed, preventing blocking on queue access.

c. Minimized Time in Locked Condition: Only enqueue and dequeue are synchronized, reducing the time spent in a locked state. The writer and consumers quickly release locks after their enqueue or dequeue operation, avoiding unnecessary contention. This enables an efficient producer-consumer pattern.
*/


import java.util.concurrent.atomic.AtomicInteger;

class SharedQueue {
    private final String[] buffer;
    private int head, tail, count;
    private final int capacity;

    public SharedQueue(int capacity) {
        this.capacity = capacity;
        this.buffer = new String[capacity];
        this.head = 0;
        this.tail = 0;
        this.count = 0;
    }

    // Add message to the queue, notify consumers if a new message is added
    public synchronized void enqueue(String message) throws InterruptedException {
        while (count == capacity) { // Wait if the buffer is full
            wait();
        }
        buffer[tail] = message;
        tail = (tail + 1) % capacity;
        count++;
        notifyAll(); // Notify all waiting consumers
    }

    // Retrieve message from the queue
    public synchronized String dequeue() throws InterruptedException {
        while (count == 0) { // Wait if the buffer is empty
            wait();
        }
        String message = buffer[head];
        head = (head + 1) % capacity;
        count--;
        notifyAll(); // Notify the writer if the buffer has space
        return message;
    }
}

class Writer implements Runnable {
    private final SharedQueue queue;
    private final AtomicInteger messageCounter = new AtomicInteger(1);

    public Writer(SharedQueue queue) {
        this.queue = queue;
    }

    @Override
    public void run() {
        try {
            while (true) {
                String message = "Message " + messageCounter.getAndIncrement();
                queue.enqueue(message);
                System.out.println("Writer added: " + message);
                Thread.sleep(200); // 5 messages per second
            }
        } catch (InterruptedException e) {
            System.out.println("Writer interrupted.");
        }
    }
}

class Consumer implements Runnable {
    private final SharedQueue queue;
    private final int consumerId;

    public Consumer(SharedQueue queue, int consumerId) {
        this.queue = queue;
        this.consumerId = consumerId;
    }

    @Override
    public void run() {
        try {
            while (true) {
                String message = queue.dequeue();
                System.out.println("Consumer " + consumerId + " processed: " + message);
            }
        } catch (InterruptedException e) {
            System.out.println("Consumer " + consumerId + " interrupted.");
        }
    }
}

public class SharedQueueTest {
    public static void main(String[] args) {
        final int capacity = 10;
        SharedQueue queue = new SharedQueue(capacity);

        // Start writer thread
        Thread writerThread = new Thread(new Writer(queue));
        writerThread.start();

        // Start consumer threads
        int numConsumers = 5;
        for (int i = 0; i < numConsumers; i++) {
            Thread consumerThread = new Thread(new Consumer(queue, i + 1));
            consumerThread.start();
        }
    }
}