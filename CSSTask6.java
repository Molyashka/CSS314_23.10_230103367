import java.util.concurrent.atomic.AtomicInteger;

public class CSSTask6 {
    static int counter = 0;
    static final AtomicInteger atomicCounter = new AtomicInteger(0);
    static final int THREADS = 4;
    static final int INCREMENTS = 2_000_000;

    static long run(Runnable r) throws InterruptedException {
        Thread[] ts = new Thread[THREADS];
        for (int i = 0; i < THREADS; i++) ts[i] = new Thread(r);
        long start = System.nanoTime();
        for (Thread t : ts) t.start();
        for (Thread t : ts) t.join();
        return System.nanoTime() - start;
    }

    public static void main(String[] args) throws Exception {
        long expected = (long) THREADS * INCREMENTS;

        long unsafeTime = run(() -> {
            for (int i = 0; i < INCREMENTS; i++) counter++;
        });
        long actual = counter;
        double lost = (expected - actual) * 100.0 / expected;

        System.out.println("=== Unsynchronized ===");
        System.out.println("Expected final value: " + expected);
        System.out.println("Actual final value:   " + actual);
        System.out.printf("Updates lost: %.4f%%%n", lost);
        System.out.printf("Time: %.3f ms%n", unsafeTime / 1e6);

        long atomicTime = run(() -> {
            for (int i = 0; i < INCREMENTS; i++) atomicCounter.incrementAndGet();
        });

        System.out.println("=== Atomic (AtomicInteger) ===");
        System.out.println("Expected final value: " + expected);
        System.out.println("Actual final value:   " + atomicCounter.get());
        System.out.printf("Time: %.3f ms%n", atomicTime / 1e6);
        System.out.printf("Wall-clock penalty from synchronization: %.3f ms (%.2fx slower)%n",
                (atomicTime - unsafeTime) / 1e6, (double) atomicTime / unsafeTime);
    }
}
