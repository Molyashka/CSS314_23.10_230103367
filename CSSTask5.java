public class CSSTask5 {
    static final double A = 2.5, B = 1.5;

    static void serial(double[] x, double[] y, int n) {
        for (int i = 0; i < n; i++) y[i] = A * x[i] + B;
    }

    static void parallel(double[] x, double[] y, int n, int threads) throws InterruptedException {
        Thread[] ts = new Thread[threads];
        int chunk = n / threads;
        for (int t = 0; t < threads; t++) {
            final int s = t * chunk;
            final int e = (t == threads - 1) ? n : s + chunk;
            ts[t] = new Thread(() -> {
                for (int i = s; i < e; i++) y[i] = A * x[i] + B;
            });
        }
        for (Thread t : ts) t.start();
        for (Thread t : ts) t.join();
    }

    static double bestSerial(double[] x, double[] y, int n, int reps) {
        double best = Double.MAX_VALUE;
        for (int r = 0; r < reps; r++) {
            long st = System.nanoTime();
            serial(x, y, n);
            best = Math.min(best, (System.nanoTime() - st) / 1e6);
        }
        return best;
    }

    static double bestParallel(double[] x, double[] y, int n, int threads, int reps) throws InterruptedException {
        double best = Double.MAX_VALUE;
        for (int r = 0; r < reps; r++) {
            long st = System.nanoTime();
            parallel(x, y, n, threads);
            best = Math.min(best, (System.nanoTime() - st) / 1e6);
        }
        return best;
    }

    public static void main(String[] args) throws Exception {
        int cores = Runtime.getRuntime().availableProcessors();
        int[] sizes = {100, 1_000, 10_000, 100_000, 1_000_000, 10_000_000};
        System.out.println("Logical cores: " + cores);

        // warm-up
        double[] wx = new double[1_000_000], wy = new double[1_000_000];
        for (int i = 0; i < 20; i++) { serial(wx, wy, wx.length); parallel(wx, wy, wx.length, cores); }

        System.out.println("---------------------------------------------------------------");
        System.out.printf("%-12s | %-14s | %-14s | %-10s | %-8s%n", "N", "Serial (ms)", "Parallel (ms)", "Speedup", "Winner");
        System.out.println("---------------------------------------------------------------");

        int lastSlowerN = -1, firstFasterN = -1;
        boolean prevSlower = false, haveCrossover = false;

        for (int n : sizes) {
            double[] x = new double[n], y = new double[n];
            for (int i = 0; i < n; i++) x[i] = i * 0.001;
            int reps = n <= 10_000 ? 200 : (n <= 1_000_000 ? 50 : 10);

            double ts = bestSerial(x, y, n, reps);
            double tp = bestParallel(x, y, n, cores, reps);
            boolean parFaster = tp < ts;

            System.out.printf("%-12d | %14.6f | %14.6f | %9.2fx | %-8s%n",
                    n, ts, tp, ts / tp, parFaster ? "Parallel" : "Serial");

            if (!parFaster) {
                lastSlowerN = n;
                haveCrossover = false;
                firstFasterN = -1;
            } else if (lastSlowerN != -1 && !haveCrossover) {
                firstFasterN = n;
                haveCrossover = true;
            }
        }
        System.out.println("---------------------------------------------------------------");

        if (lastSlowerN == -1) {
            System.out.println("Parallel was faster than serial for every tested N (crossover below N=" + sizes[0] + ").");
        } else if (!haveCrossover) {
            System.out.println("Parallel never became faster than serial in the tested range (crossover above N=" + sizes[sizes.length - 1] + ").");
        } else {
            System.out.println("Crossover range: multi-threading shifts from SLOWER to FASTER between N=" + lastSlowerN + " and N=" + firstFasterN + ".");
        }
    }
}