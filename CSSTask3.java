public class CSSTask3 {
    static final int INCREMENTS = 50_000_000;
    static final int PAD = 16; 

    static volatile long[] shared;
    static volatile long[] padded;

    static double run(Runnable[] tasks) throws InterruptedException {
        Thread[] ts = new Thread[tasks.length];
        for (int i = 0; i < tasks.length; i++) ts[i] = new Thread(tasks[i]);
        long start = System.nanoTime();
        for (Thread t : ts) t.start();
        for (Thread t : ts) t.join();
        return (System.nanoTime() - start) / 1e6;
    }

    public static void main(String[] args) throws Exception {
        int logical = Runtime.getRuntime().availableProcessors();
        int n = Math.max(2, logical / 2);
        System.out.println("Logical cores: " + logical + " | Threads used (physical): " + n);

        shared = new long[n];
        Runnable[] unpadded = new Runnable[n];
        for (int i = 0; i < n; i++) {
            final int id = i;
            unpadded[i] = () -> {
                long[] a = shared;
                for (int k = 0; k < INCREMENTS; k++) a[id]++;
            };
        }

        padded = new long[n * PAD];
        Runnable[] pad = new Runnable[n];
        for (int i = 0; i < n; i++) {
            final int idx = i * PAD;
            pad[i] = () -> {
                long[] a = padded;
                for (int k = 0; k < INCREMENTS; k++) a[idx]++;
            };
        }

        long[] localOut = new long[n];
        Runnable[] local = new Runnable[n];
        for (int i = 0; i < n; i++) {
            final int id = i;
            local[i] = () -> {
                long c = 0;
                for (int k = 0; k < INCREMENTS; k++) c++;
                localOut[id] = c;
            };
        }

        run(unpadded); run(pad); 
        shared = new long[n];
        padded = new long[n * PAD];

        double tShared = run(unpadded);
        double tPadded = run(pad);
        double tLocal = run(local);

        System.out.printf("Unpadded Shared : %.3f ms%n", tShared);
        System.out.printf("Padded (128B)   : %.3f ms%n", tPadded);
        System.out.printf("Local variable  : %.3f ms%n", tLocal);
        System.out.printf("Ratio Unpadded/Padded: %.2fx%n", tShared / tPadded);
        System.out.printf("Ratio Unpadded/Local : %.2fx%n", tShared / Math.max(tLocal, 0.001));
    }
}
