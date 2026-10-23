public class CSSTask7 {
    static int N = 30000000;
    static double[] A = new double[N];
    static double[] B = new double[N];

    static double run(int kernel, int threads) throws Exception {
        Thread[] t = new Thread[threads];
        int chunk = N / threads;

        long start = System.nanoTime();
        for (int k = 0; k < threads; k++) {
            int s = k * chunk;
            int e = (k == threads - 1) ? N : s + chunk;
            t[k] = new Thread(() -> {
                for (int i = s; i < e; i++) {
                    if (kernel == 0) {
                        B[i] = A[i];
                    } else {
                        B[i] = Math.sin(A[i]) * Math.cos(A[i]) + Math.tan(A[i]);
                    }
                }
            });
            t[k].start();
        }
        for (int k = 0; k < threads; k++) {
            t[k].join();
        }
        long end = System.nanoTime();
        return (end - start) / 1e6;
    }

    public static void main(String[] args) throws Exception {
        int max = Runtime.getRuntime().availableProcessors();
        int[] cores = {1, 2, max};

        for (int i = 0; i < N; i++) {
            A[i] = (i % 1000) * 0.001 + 0.1;
        }

        run(0, max);
        run(1, max);

        double[] effA = new double[3];
        double[] effB = new double[3];

        System.out.println("Kernel A (Memory-Bound): B[i] = A[i]");
        double t1 = 0;
        for (int i = 0; i < 3; i++) {
            double time = run(0, cores[i]);
            if (i == 0) t1 = time;
            double speedup = t1 / time;
            effA[i] = speedup / cores[i];
            System.out.printf("Cores: %d | Time: %.3f ms | Speedup: %.2f | Efficiency: %.2f%n",
                    cores[i], time, speedup, effA[i]);
        }

        System.out.println("Kernel B (Compute-Bound): sin(A[i])*cos(A[i])+tan(A[i])");
        for (int i = 0; i < 3; i++) {
            double time = run(1, cores[i]);
            if (i == 0) t1 = time;
            double speedup = t1 / time;
            effB[i] = speedup / cores[i];
            System.out.printf("Cores: %d | Time: %.3f ms | Speedup: %.2f | Efficiency: %.2f%n",
                    cores[i], time, speedup, effB[i]);
        }

        if (effA[2] < effB[2]) {
            System.out.println("Kernel A (vector copy) stalled in scaling due to memory channel saturation.");
        } else {
            System.out.println("Kernel B scaled worse than Kernel A, memory channels were not saturated.");
        }
    }
}