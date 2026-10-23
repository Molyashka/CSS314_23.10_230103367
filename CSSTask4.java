import java.util.concurrent.atomic.AtomicInteger;

public class CSSTask4 {
    static final int ITEMS = 1000;
    static final int WORKERS = 4;

    static void work(int i) {
        long nanos = (long) (0.0001 * (i % 20) * 1e9);
        if (nanos <= 0) return;
        long end = System.nanoTime() + nanos;
        while (System.nanoTime() < end) { } 
    }

    static void report(String name, long total, long[] finish) {
        long min = Long.MAX_VALUE, max = Long.MIN_VALUE;
        for (long f : finish) { min = Math.min(min, f); max = Math.max(max, f); }
        System.out.println("=== " + name + " ===");
        System.out.printf("Total time: %.3f ms%n", total / 1e6);
        System.out.printf("First thread finished: %.3f ms | Last thread finished: %.3f ms%n", min / 1e6, max / 1e6);
        System.out.printf("Completion delta (max - min): %.3f ms%n", (max - min) / 1e6);
    }

    static void runStatic() throws InterruptedException {
        Thread[] ts = new Thread[WORKERS];
        long[] finish = new long[WORKERS];
        int chunk = ITEMS / WORKERS;
        long start = System.nanoTime();
        for (int t = 0; t < WORKERS; t++) {
            final int id = t, s = t * chunk, e = (t == WORKERS - 1) ? ITEMS : s + chunk;
            ts[t] = new Thread(() -> {
                for (int i = s; i < e; i++) work(i);
                finish[id] = System.nanoTime() - start;
            });
        }
        for (Thread t : ts) t.start();
        for (Thread t : ts) t.join();
        report("Strategy A: Static chunks", System.nanoTime() - start, finish);
    }

    static void runDynamic(int chunk) throws InterruptedException {
        Thread[] ts = new Thread[WORKERS];
        long[] finish = new long[WORKERS];
        AtomicInteger next = new AtomicInteger(0);
        long start = System.nanoTime();
        for (int t = 0; t < WORKERS; t++) {
            final int id = t;
            ts[t] = new Thread(() -> {
                int s;
                while ((s = next.getAndAdd(chunk)) < ITEMS) {
                    int e = Math.min(s + chunk, ITEMS);
                    for (int i = s; i < e; i++) work(i);
                }
                finish[id] = System.nanoTime() - start;
            });
        }
        for (Thread t : ts) t.start();
        for (Thread t : ts) t.join();
        report("Strategy B: Dynamic scheduling (chunk=" + chunk + ")", System.nanoTime() - start, finish);
    }

    public static void main(String[] args) throws Exception {
        runStatic();
        runDynamic(1);
        runDynamic(5);
    }
}
